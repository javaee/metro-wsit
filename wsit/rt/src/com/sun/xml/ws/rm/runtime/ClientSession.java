/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.rm.runtime;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.rm.BufferFullException;
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.DuplicateMessageException;
import com.sun.xml.ws.rm.InvalidMessageNumberException;
import com.sun.xml.ws.rm.InvalidSequenceException;
import com.sun.xml.ws.rm.MessageNumberRolloverException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
import com.sun.xml.ws.rm.protocol.AbstractSequenceAcknowledgement;
import com.sun.xml.ws.rm.protocol.AbstractTerminateSequence;
import com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * <p>
 * RM session represents a contract between single WS proxy and it's corresponding service. Multiple tubelines (of the same
 * WS proxy) may share a single RM session, each WS proxy however creates it's own session.
 * </p>
 * <p>
 * RM session performs all tasks related to RM message processing, while being focused on a single reliable connection.
 * </p>
 * 
 * TODO: Decide: is this going to be considered as a key element of a RM failover implementation?
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
abstract class ClientSession {

    private static final RmLogger LOGGER = RmLogger.getLogger(ClientSession.class);

    static ClientSession create(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator) {
        // TODO don't take the first config alternative automatically...
        Configuration configuration = ConfigurationManager.createClientConfigurationManager(wsdlPort, binding).getConfigurationAlternatives()[0];
        switch (configuration.getRMVersion()) {
            case WSRM10:
                return new Rm10ClientSession(wsdlPort, binding, communicator, configuration);
            case WSRM11:
                return new Rm11ClientSession(wsdlPort, binding, communicator, configuration);
        }
        return null; // TODO throw exception here?
    }

    private static class FiberRegistration {

        private final long timestamp;
        final Fiber fiber;
        final Packet packet;

        FiberRegistration(Fiber fiber, Packet packet) {
            this.timestamp = System.currentTimeMillis();
            this.fiber = fiber;
            this.packet = packet;
        }

        boolean expired(long period) {
            return System.currentTimeMillis() - timestamp >= period;
        }
    }
    protected final Configuration configuration;
    protected final SequenceManager sequenceManager;
    private final ProtocolCommunicator communicator;
    private final boolean isRequestResponseSession;
    private final Lock initLock = new ReentrantLock();
    protected String inboundSequenceId = null;
    protected String outboundSequenceId = null;
    private final Queue<FiberRegistration> fibersToResend = new LinkedList<FiberRegistration>();
    private final ResendTimer resendTimer;
    private final Unmarshaller jaxbUnmarshaller;

    protected ClientSession(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator, Configuration configuration) {
        this.configuration = configuration;
        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.communicator = communicator;
        this.isRequestResponseSession = checkForRequestResponseOperations(wsdlPort);
        this.resendTimer = new ResendTimer(this);
        this.jaxbUnmarshaller = createUnmarshaller(configuration.getRMVersion().jaxbContext);
    }

    protected abstract Message prepareHandshakeRequest(String offerInboundSequenceId, SecurityTokenReferenceType strType);

    protected abstract String processHandshakeResponseMessage(Message handshakeResponseMessage) throws RmException;

    protected abstract void appendSequenceHeader(Message outboundMessage) throws RmException;
    
    protected abstract void appendAckRequestedHeader(Message outboundMessage) throws RmException;
    
    protected abstract void appendSequenceAcknowledgementHeader(Message outboundMessage) throws RmException;
    
    protected abstract void processInboundMessageHeaders(Message inboundMessage) throws RmException;

    protected abstract void disconnect() throws RmException;

    private Unmarshaller createUnmarshaller(JAXBContext jaxbContext) {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            // TODO L10N            
            throw LOGGER.logSevereException(new IllegalStateException("Unable to create JAXB unmarshaller", e));
        }
    }

    /**
     * Closes and terminates associated sequences and releases other resources associated with this RM session
     */
    void close() {
        try {            
            disconnect();
            sequenceManager.getSequence(outboundSequenceId).close();
        } catch (CloseSequenceException ex) {
            // TODO: is the exception handled correctly?
            LOGGER.logException(ex, Level.WARNING);
        } catch (UnknownSequenceException ex) {
            // TODO: is the exception handled correctly?
            LOGGER.logException(ex, Level.WARNING);
        } catch (RmException ex) {
            // TODO: is the exception handled correctly?
            LOGGER.logException(ex, Level.WARNING);
        }
        try {
            sequenceManager.getSequence(inboundSequenceId).close();
        } catch (UnknownSequenceException ex) {
            LOGGER.logException(ex, Level.WARNING);
        }
        resendTimer.stop();
    }

    Packet processOutgoingPacket(Packet requestPacket) throws RmException {
        initializeIfNecessary(requestPacket);

        appendSequenceHeader(requestPacket.getMessage());
        if (true) { // TODO method to determine if ackRequested header should be added
            appendAckRequestedHeader(requestPacket.getMessage());
        }
        if (inboundSequenceId != null) {
            appendSequenceAcknowledgementHeader(requestPacket.getMessage());
        }
        
        return requestPacket;
    }

    Packet processIncommingPacket(Packet responsePacket) throws RmException {

        processInboundMessageHeaders(responsePacket.getMessage());

        return responsePacket;
    }

    /**
     * Registers given fiber for a resend (resume)
     * 
     * @param fiber a fiber to be resumed after resend interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    boolean registerForResend(Fiber fiber, Packet packet) {
        synchronized (fibersToResend) {
            return fibersToResend.offer(new FiberRegistration(fiber, packet));
        }
    }

    /**
     * Resumes all suspended fibers registered for a resend which have an expired retransmission inteval.
     */
    void resend() {
        while (!fibersToResend.isEmpty() && fibersToResend.peek().expired(configuration.getMessageRetransmissionInterval())) {
            FiberRegistration registration;
            synchronized (fibersToResend) {
                registration = fibersToResend.poll();
            }
            registration.fiber.resume(registration.packet);
        }
    }

    /**
     * Performs late initialization of sequences and timer task, provided those have not yet been initialized.
     * The actual initialization thus happens only once in the lifetime of each client RM session object.
     */
    private void initializeIfNecessary(Packet requestPacket) throws CreateSequenceException, RmException {
        initLock.lock();
        try {
            if (!isInitialized()) {
                communicator.registerMusterRequestPacket(requestPacket.copy(false));

                if (isRequestResponseSession) {
                    inboundSequenceId = sequenceManager.generateSequenceUID();
                }
                outboundSequenceId = connect(inboundSequenceId);

                sequenceManager.createInboundSequence(inboundSequenceId);
                sequenceManager.createOutboudSequence(outboundSequenceId);

                resendTimer.start();
            }
        } finally {
            initLock.unlock();
        }
    }

    private boolean isInitialized() {
        return outboundSequenceId != null;
    }

    /**
     * Sends handshake message (CreateSequence) in order to establish the RM session.
     * 
     * @param offerInboundSequenceId nullable, if not {@code null} the value will be used as an offered response messages sequence identifier
     * @return identifier of the outgoing messages sequence.
     */
    protected String connect(String offerInboundSequenceId) throws CreateSequenceException, RmException {
        Message handshakeRequestMessage = prepareHandshakeRequest(offerInboundSequenceId, communicator.tryStartSecureConversation());
        Message handshakeResponseMessage = communicator.send(handshakeRequestMessage, configuration.getRMVersion().createSequenceAction);
        if (handshakeResponseMessage != null) {
            return processHandshakeResponseMessage(handshakeResponseMessage);
        } else {
            // TODO: create sequence response was null... throw an exception or handle CreateSequenceRefused fault?
            // TODO L10N
            throw LOGGER.logSevereException(new CreateSequenceException("CreateSequenceResponse was null"));
        }
    }

    protected final <T> T unmarshallResponse(Message response) throws RmException {
        try {
            return (T) response.readPayloadAsJAXB(jaxbUnmarshaller);
        } catch (JAXBException e) {
            // TODO L10N
            throw LOGGER.logSevereException(new RmException("Unable to unmarshall response", e));
        }
    }

    /**
     * Send Message with empty body and a AckRequestedElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    private void sendAckRequested() throws RmException {
        try {
            Message ackRequestMessage = Messages.createEmpty(configuration.getSoapVersion());
            appendAckRequestedHeader(ackRequestMessage);

            Message ackResponse = null;
            try {
                ackResponse = communicator.send(ackRequestMessage, configuration.getRMVersion().ackRequestedAction);
                if (ackResponse != null && ackResponse.isFault()) {
                    // TODO L10N
                    throw LOGGER.logException(new RmException("Error sending AckRequestedElement", ackResponse), Level.WARNING);
                }

                processInboundMessageHeaders(ackResponse);
            } finally {
                if (ackResponse != null) {
                    ackResponse.consume();
                }
            }
        } finally {
            // TODO Make sure that inactivity timeout is reset.
        }
    }

    /**
     * Send Message with empty body and a single SequenceElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    private void sendLast(OutboundSequence seq) throws RmException {
        com.sun.xml.ws.rm.v200502.SequenceElement sequenceElement = new com.sun.xml.ws.rm.v200502.SequenceElement();
        sequenceElement.setId(outboundSequenceId);
        sequenceElement.setNumber(sequenceManager.getSequence(outboundSequenceId).getLastMessageId());
        sequenceElement.setLastMessage(new com.sun.xml.ws.rm.v200502.SequenceElement.LastMessage());

        Message lastMessage = Messages.createEmpty(configuration.getSoapVersion());
        lastMessage.getHeaders().add(Headers.create(configuration.getRMVersion().jaxbContext, sequenceElement));
        Message response = null;
        try {
            response = communicator.send(lastMessage, configuration.getRMVersion().lastAction);
            if (response != null && response.isFault()) {
                // TODO L10N
                throw LOGGER.logException(new RmException("Error sending Last message", response), Level.WARNING);
            }
            processInboundMessageHeaders(response);
        } finally {
            if (response != null) {
                response.consume();
            }
        }
    }
    
    private void sendCloseSequence() throws RmException {
        com.sun.xml.ws.rm.v200702.Identifier idClose = new com.sun.xml.ws.rm.v200702.Identifier();
        idClose.setValue(outboundSequenceId);

        com.sun.xml.ws.rm.v200702.CloseSequenceElement cs = new com.sun.xml.ws.rm.v200702.CloseSequenceElement();
        cs.setIdentifier(idClose);        
        cs.setLastMsgNumber(sequenceManager.getSequence(outboundSequenceId).getLastMessageId());

        Message closeSequenceRequest = Messages.create(configuration.getRMVersion().jaxbContext, cs, configuration.getSoapVersion());

        // TODO: check why only WS-RM1.1 ???
        Message response = communicator.send(closeSequenceRequest, RmVersion.WSRM11.closeSequenceAction);
        if (response != null && response.isFault()) {
            // TODO L10N
            throw LOGGER.logException(new CloseSequenceException("CloseSequence was refused by the RMDestination", response), Level.WARNING);
        }

        CloseSequenceResponseElement csr = unmarshallResponse(response);
        // TODO process CloseSequenceRespose element...
    }

    private void sendTerminateSequence(AbstractTerminateSequence ts, OutboundSequence seq) throws RmException {
        //TODO piggyback an acknowledgement if one is pending
        //seq.processAcknowledgement(new RMMessage(request));

        Message terminateSequenceRequest = Messages.create(configuration.getRMVersion().jaxbContext, ts, configuration.getSoapVersion());
        Message response = null;
        try {
            response = communicator.send(terminateSequenceRequest, configuration.getRMVersion().terminateSequenceAction);
            if (response != null && response.isFault()) {
                throw LOGGER.logException(new TerminateSequenceException("There was an error trying to terminate the sequence ", response), Level.WARNING);
            }
        //TODO What to do with response?
        //It may have a TerminateSequence for reverse sequence on it as well as ack headers
        //Process these.
        } finally {
            if (response != null) {
                response.consume();
            }
        }
    }

    /**
     * Determine whether wsdl port contains any two-way operations.
     * 
     * @param port WSDL port to check
     * @return {@code true} if there are request/response present on the port; returns {@code false} otherwise
     */
    private boolean checkForRequestResponseOperations(WSDLPort port) {
        WSDLBoundPortType portType;
        if (port == null || null == (portType = port.getBinding())) {
            //no WSDL perhaps? Returning false here means that will be no reverse sequence. That is the correct behavior.
            return false;
        }

        for (WSDLBoundOperation boundOperation : portType.getBindingOperations()) {
            if (!boundOperation.getOperation().isOneWay()) {
                return true;
            }
        }
        return false;
    }

//    private void processAckRequestHeader(Header header, Message message) throws RmException, InvalidMessageNumberException {
//        try {
//            //dispatch to InboundSequence to construct response.
//            //TODO handle error condition no such sequence
//            AbstractAckRequested el = header.readAsJAXB(jaxbUnmarshaller);
//            message.setAckRequestedElement(el);
//
//            String id = null;
//            if (el instanceof com.sun.xml.ws.rm.v200502.AckRequestedElement) {
//                id = ((com.sun.xml.ws.rm.v200502.AckRequestedElement) el).getId();
//            } else {
//                id = ((com.sun.xml.ws.rm.v200702.AckRequestedElement) el).getId();
//            }
//
//            InboundSequence seq = provider.getInboundSequence(id);
//            if (seq != null) {
//                seq.handleAckRequested();
//            }
//        } catch (JAXBException e) {
//            throw LOGGER.logSevereException(new RmException("Unable to unmarshall AckRequested RM header", e));
//        }
//    }
//
//    private void processAckHeader(Header header, Message message) throws InvalidMessageNumberException, RmException {
//        try {
//            AbstractSequenceAcknowledgement ackHeader = header.readAsJAXB(jaxbUnmarshaller);
//
//            String ackHeaderId = null;
//            if (ackHeader instanceof com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) {
//                ackHeaderId = ((com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement) ackHeader).getId();
//            } else {
//                ackHeaderId = ((com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement) ackHeader).getId();
//            }
//
//            message.setSequenceAcknowledgementElement(ackHeader);
//            OutboundSequence seq = provider.getOutboundSequence(ackHeaderId);
//            if (seq != null) {
//                seq.handleAckResponse(ackHeader);
//            }
//        } catch (JAXBException e) {
//            throw LOGGER.logSevereException(new RmException("Unable to unmarshall SequenceAcknowledgement RM header", e));
//        }
//    }
//
//    private InboundSequence processSequenceHeader(Header header, Message message) throws DuplicateMessageException, InvalidSequenceException, InvalidMessageNumberException, BufferFullException, CloseSequenceException, MessageNumberRolloverException, RmException {
//        try {
//            //identify sequence and message number from data in header and add
//            //the message to the sequence at the specified index.
//            //TODO handle error condition seq == null
//            AbstractSequence el = header.readAsJAXB(jaxbUnmarshaller);
//            message.setSequenceElement(el);
//
//            String seqid = null;
//            long messageNumber;
//            if (el instanceof com.sun.xml.ws.rm.v200502.SequenceElement) {
//                seqid = ((com.sun.xml.ws.rm.v200502.SequenceElement) el).getId();
//                messageNumber = ((com.sun.xml.ws.rm.v200502.SequenceElement) el).getNumber();
//            } else {
//                seqid = ((com.sun.xml.ws.rm.v200702.SequenceElement) el).getId();
//                messageNumber = ((com.sun.xml.ws.rm.v200702.SequenceElement) el).getNumber();
//            }
//
//            if (messageNumber == Integer.MAX_VALUE) {
//                throw LOGGER.logSevereException(new MessageNumberRolloverException(LocalizationMessages.WSRM_3026_MESSAGE_NUMBER_ROLLOVER(messageNumber), messageNumber));
//            }
//
//            inseq = provider.getInboundSequence(seqid);
//            if (inseq != null) {
//                if (inseq.isClosed()) {
//                    throw LOGGER.logSevereException(new CloseSequenceException(LocalizationMessages.WSRM_3029_SEQUENCE_CLOSED(seqid), seqid));
//                }
//                //add message to ClientInboundSequence
//                inseq.set((int) messageNumber, message);
//            } else {
//                throw LOGGER.logSevereException(new InvalidSequenceException(LocalizationMessages.WSRM_3022_UNKNOWN_SEQUENCE_ID_IN_MESSAGE(seqid), seqid));
//            }
//        } catch (JAXBException e) {
//            throw LOGGER.logSevereException(new RmException("Unable to unmarshall Sequence RM header", e));
//        }
//        return inseq;
//    }
//
//    /**
//     * For each inbound <code>Message</code>, invokes protocol logic dictated by the contents of the
//     * WS-RM protocol headers on the message.
//     * <ul>
//     *  <li><b>Sequence Header</b><br>Adds the message to the instance data of this incoming sequence according using the
//     *          Sequence Identifier and Message Number in the header.</li>
//     *  <li><b>SequenceAcknowledgement Header</b><br>Invokes the <code>handleAckResponse</code> method of the companion 
//     *          <code>ClientOutboundSequence</code> which marks acknowledged messages as delivered.</li>
//     *  <li><b>AckRequested Header</b><br>Constructs a <code>SequenceAcknowledgementElement</code> reflecting the messages 
//     *          belonging to this sequence that have been received.  Sets the resulting 
//     *      
//     * <code>SequenceAcknowledgementElement</code> in the state of the companion <code>ClientOutboundSequence</code>.</li>
//     * </ul>
//     * <br>
//     * @param message The inbound <code>Message</code>.
//     */
//    private void processInboundMessage(Message message) throws RmException {
//        /*
//         * Check for each RM header type and do the right thing in RMProvider
//         * depending on the type.
//         */
//        InboundSequence inseq = null;
//        Header header = getHeader(message, "Sequence");
//        if (header != null) {
//            inseq = processSequenceHeader(header, message);
//        }
//
//        header = getHeader(message, "SequenceAcknowledgement");
//        if (header != null) {
//            processAckHeader(header, message);
//        }
//
//        header = getHeader(message, "AckRequested");
//        if (header != null) {
//            processAckRequestHeader(header, message);
//        } else {
//            // FIXME - We need to be checking whether this is a ServerInboundSequence
//            // in a port with a two-way operation.  This is the case where MS
//            // puts a SequenceAcknowledgement on every message.
//            // Need to check this with the latest CTP
//            // Currently with Dec CTP the client message
//            // does not have AckRequested element
//            // but they are expecting a SequenceAcknowledgement
//            // Hack for now
//            if (inseq != null) {
//                inseq.handleAckRequested();
//            } else {
//            //we can get here if there is no sequence header.  Perhaps this
//            //is a ClientInboundSequence where the OutboundSequence has no two-ways
//            }
//        }
//    }
//    
//    /**
//     * Get the RM Header Element with the specified name from the underlying
//     * JAX-WS message's HeaderList
//     * @param name The name of the Header to find.
//     */
//    private Header getHeader(Message message, String name) {
//        return (message.getHeaders() != null) ? message.getHeaders().get(configuration.getRMVersion().namespaceUri, name, true) : null;
//    }    
    protected Header createHeader(Object headerContent) {
        return Headers.create(configuration.getRMVersion().jaxbContext, headerContent);
    }
}
