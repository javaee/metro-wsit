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
import com.sun.xml.ws.rm.CloseSequenceException;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
    protected String inboundSequenceId;
    protected String outboundSequenceId;
    protected final Configuration configuration;
    protected final SequenceManager sequenceManager;
    protected final ProtocolCommunicator communicator;
    private final Lock initLock;
    private final boolean isRequestResponseSession;
    private final Unmarshaller jaxbUnmarshaller;
    private final ScheduledTaskManager scheduledTaskManager;
    private final Queue<FiberRegistration> fibersToResend = new LinkedList<FiberRegistration>();
    private final AtomicLong lastAckRequestedTime = new AtomicLong(0);

    protected ClientSession(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator, Configuration configuration) {
        this.inboundSequenceId = null;
        this.outboundSequenceId = null;
        this.initLock = new ReentrantLock();
        this.configuration = configuration;
        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.communicator = communicator;
        this.isRequestResponseSession = checkForRequestResponseOperations(wsdlPort);
        this.scheduledTaskManager = new ScheduledTaskManager();
        this.jaxbUnmarshaller = createUnmarshaller(configuration.getRMVersion().jaxbContext);
    }

    protected abstract Message prepareHandshakeRequest(String offerInboundSequenceId, SecurityTokenReferenceType strType);

    protected abstract String processHandshakeResponseMessage(Message handshakeResponseMessage) throws RmException;

    protected abstract void appendSequenceHeader(Message outboundMessage) throws RmException;

    protected abstract void appendAckRequestedHeader(Message outboundMessage) throws RmException;

    protected abstract void appendSequenceAcknowledgementHeader(Message outboundMessage) throws RmException;

    protected abstract void processInboundMessageHeaders(Message inboundMessage) throws RmException;

    protected abstract void closeOutboundSequence() throws RmException;

    protected abstract void terminateOutboundSequence() throws RmException;

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

    protected final Header createHeader(Object headerContent) {
        return Headers.create(configuration.getRMVersion().jaxbContext, headerContent);
    }

    protected final <T> T unmarshallResponse(Message response) throws RmException {
        try {
            return (T) response.readPayloadAsJAXB(jaxbUnmarshaller);
        } catch (JAXBException e) {
            // TODO L10N
            throw LOGGER.logSevereException(new RmException("Unable to unmarshall response", e));
        }
    }

    final Packet processOutgoingPacket(Packet requestPacket) throws RmException {
        initializeIfNecessary(requestPacket);

        appendSequenceHeader(requestPacket.getMessage());
        if (checkPendingAckRequest()) {
            appendAckRequestedHeader(requestPacket.getMessage());
            lastAckRequestedTime.set(System.currentTimeMillis());
        }
        if (inboundSequenceId != null) {
            // we are always sending acknowledgements if there is an inbound sequence
            appendSequenceAcknowledgementHeader(requestPacket.getMessage());
        }

        return requestPacket;
    }

    final Packet processIncommingPacket(Packet responsePacket) throws RmException {
        processInboundMessageHeaders(responsePacket.getMessage());

        return responsePacket;
    }

    /**
     * Registers given fiber for a resend (resume)
     * 
     * @param fiber a fiber to be resumed after resend interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    final boolean registerForResend(Fiber fiber, Packet packet) {
        synchronized (fibersToResend) {
            return fibersToResend.offer(new FiberRegistration(fiber, packet));
        }
    }

    /**
     * Closes and terminates associated sequences and releases other resources associated with this RM session
     */
    final void close() {
        try {
            try {
                closeOutboundSequence();
                sequenceManager.closeSequence(outboundSequenceId);
            } finally {
                waitUntilAllRequestsAckedOrTimeout();
                terminateOutboundSequence();
                sequenceManager.terminateSequence(outboundSequenceId);
            }
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
            // TODO wait for an external event?
            sequenceManager.closeSequence(inboundSequenceId);
        } catch (UnknownSequenceException ex) {
            LOGGER.logException(ex, Level.WARNING);
        }
        scheduledTaskManager.stopAll();
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

                scheduledTaskManager.startTasks(
                        new Runnable() {

                            public void run() {
                                resend();
                            }
                        },
                        new Runnable() {

                            public void run() {
                                sendAckRequested();
                            }
                        });
            }
        } finally {
            initLock.unlock();
        }
    }

    private boolean isInitialized() {
        return outboundSequenceId != null;
    }

    /**
     * Resumes all suspended fibers registered for a resend which have an expired retransmission inteval.
     */
    private void resend() {
        while (!fibersToResend.isEmpty() && fibersToResend.peek().expired(configuration.getMessageRetransmissionInterval())) {
            FiberRegistration registration;
            synchronized (fibersToResend) {
                registration = fibersToResend.poll();
            }
            registration.fiber.resume(registration.packet);
        }
    }

    private Unmarshaller createUnmarshaller(JAXBContext jaxbContext) {
        try {
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            // TODO L10N            
            throw LOGGER.logSevereException(new IllegalStateException("Unable to create JAXB unmarshaller", e));
        }
    }

    /**
     * Send Message with empty body and a AckRequestedElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    private void sendAckRequested() {
        Message ackResponse = null;
        try {
            if (checkPendingAckRequest()) {
                Message ackRequestMessage = Messages.createEmpty(configuration.getSoapVersion());
                appendAckRequestedHeader(ackRequestMessage);
                lastAckRequestedTime.set(System.currentTimeMillis());

                ackResponse = communicator.send(ackRequestMessage, configuration.getRMVersion().ackRequestedAction);
                if (ackResponse != null && ackResponse.isFault()) {
                    // TODO L10N
                    throw LOGGER.logException(new RmException("Error sending AckRequestedElement", ackResponse), Level.WARNING);
                }

                processInboundMessageHeaders(ackResponse);
            }
        } catch (RmException ex) {
            // TODO L10N
            LOGGER.warning("Acknowledgement request failed", ex);
        } finally {
            if (ackResponse != null) {
                ackResponse.consume();
            }
        }
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
    private boolean checkPendingAckRequest() throws UnknownSequenceException {
        return lastAckRequestedTime.get() - System.currentTimeMillis() > configuration.getAcknowledgementRequestInterval() &&
                sequenceManager.getSequence(outboundSequenceId).hasPendingAcknowledgements();
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

    private void waitUntilAllRequestsAckedOrTimeout() {
        final CountDownLatch doneSignal = new CountDownLatch(1);
        ScheduledFuture<?> taskHandle = scheduledTaskManager.startTask(new Runnable() {

            public void run() {
                try {
                    if (!sequenceManager.getSequence(outboundSequenceId).hasPendingAcknowledgements()) {
                        doneSignal.countDown();
                    }
                } catch (UnknownSequenceException ex) {
                    // TODO L10N
                    LOGGER.severe("Unexpected exception occured while waiting for sequence acknowledgements", ex);
                    doneSignal.countDown();
                }
            }
        });
        try {
            boolean waitResult = doneSignal.await(configuration.getCloseSequenceOperationTimeout(), TimeUnit.MILLISECONDS);
            if (!waitResult) {
                // TODO L10N
                LOGGER.info("Close sequence operation timed out for outbound sequence [" + outboundSequenceId + "]");
            }
        } catch (InterruptedException ex) {
            // TODO L10N
            LOGGER.fine("Got interrupted while waiting for close sequence operation", ex);
        } finally {
            taskHandle.cancel(true);
        }
    }
}
