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
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
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
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.TerminateSequenceException;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
import com.sun.xml.ws.rm.protocol.AbstractAckRequested;
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
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

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
public class ClientRmSession {

    private static final RmLogger LOGGER = RmLogger.getLogger(ClientRmSession.class);

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
    
    private final Configuration configuration;
    private final SequenceManager sequenceManager;
    private final ProtocolCommunicator communicator;
    private final boolean isRequestResponseSession;
    private final Lock initLock = new ReentrantLock();
    private String inboundSequenceId = null;
    private String outboundSequenceId = null;
    private final Queue<FiberRegistration> fibersToResend = new LinkedList<FiberRegistration>();
    private final ResendTimer resendTimer;
    private final Unmarshaller jaxbUnmarshaller;

    public ClientRmSession(WSDLPort wsdlPort, WSBinding binding, ProtocolCommunicator communicator) {
        // take the first config alternative for now...
        this.configuration = ConfigurationManager.createClientConfigurationManager(wsdlPort, binding).getConfigurationAlternatives()[0];
        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.communicator = communicator;
        this.isRequestResponseSession = checkForRequestResponseOperations(wsdlPort);
        this.resendTimer = new ResendTimer(this);
        this.jaxbUnmarshaller = createUnmarshaller(configuration.getRMVersion().jaxbContext);
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
     * Closes and terminates associated sequences and releases other resources associated with this RM session
     */
    void close() {
        try {
            sendCloseSequence(outboundSequenceId, sequenceManager.getSequence(outboundSequenceId).getLastMessageId());
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

    public Packet processOutgoingPacket(Packet requestPacket) throws UnknownSequenceException, CreateSequenceException, RmException {
        initializeIfNecessary(requestPacket);

        Message message = requestPacket.getMessage();
        message = sequenceManager.getSequence(outboundSequenceId).processOutgoingMessage(message);
        if (isRequestResponseSession) {
            message = sequenceManager.getSequence(inboundSequenceId).processOutgoingMessage(message);
        }
        requestPacket.setMessage(message);

        return requestPacket;
    }

    Packet processIncommingPacket(Packet responsePacket) throws UnknownSequenceException {
//        initializeIfNecessary();

        Message message = responsePacket.getMessage();
        message = sequenceManager.getSequence(outboundSequenceId).processIncommingMessage(message);
        if (isRequestResponseSession) {
            message = sequenceManager.getSequence(inboundSequenceId).processIncommingMessage(message);
        }
        responsePacket.setMessage(message);

        return responsePacket;
    }

    /**
     * Registers given fiber for a resend (resume)
     * 
     * @param fiber a fiber to be resumed after resend interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    boolean registerForResend(Fiber fiber, Packet packet) {
        return fibersToResend.offer(new FiberRegistration(fiber, packet));
    }

    void resend() {
        while (!fibersToResend.isEmpty() && fibersToResend.peek().expired(configuration.getMessageRetransmissionInterval())) {
            FiberRegistration registration = fibersToResend.poll();
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
                outboundSequenceId = sendHandshakeMessage(inboundSequenceId);

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

    private Message prepareRm10HandshakeRequest(String offerInboundSequenceId, SecurityTokenReferenceType strType) {
        com.sun.xml.ws.rm.v200502.CreateSequenceElement scElement = new com.sun.xml.ws.rm.v200502.CreateSequenceElement();

        if (configuration.getAddressingVersion() == AddressingVersion.W3C) {
            scElement.setAcksTo(new W3CEndpointReference(AddressingVersion.W3C.anonymousEpr.asSource("AcksTo")));
        } else {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("Unsupported addressing version"));
        }

        if (offerInboundSequenceId != null) {
            com.sun.xml.ws.rm.v200502.Identifier offerIdentifier = new com.sun.xml.ws.rm.v200502.Identifier();
            offerIdentifier.setValue(offerInboundSequenceId);

            com.sun.xml.ws.rm.v200502.OfferType offer = new com.sun.xml.ws.rm.v200502.OfferType();
            offer.setIdentifier(offerIdentifier);

            scElement.setOffer(offer);
        }
        if (strType != null) {
            scElement.setSecurityTokenReference(strType);
        }

        return Messages.create(configuration.getRMVersion().jaxbContext, scElement, configuration.getSoapVersion());
    }

    private Message prepareRm11HandshakeRequest(String offerInboundSequenceId, SecurityTokenReferenceType strType) {
        com.sun.xml.ws.rm.v200702.CreateSequenceElement csElement = new com.sun.xml.ws.rm.v200702.CreateSequenceElement();
        if (configuration.getAddressingVersion() == AddressingVersion.W3C) {
            csElement.setAcksTo(new W3CEndpointReference(AddressingVersion.W3C.anonymousEpr.asSource("AcksTo")));
        } else {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("Unsupported addressing version"));
        }

        if (offerInboundSequenceId != null) {
            com.sun.xml.ws.rm.v200702.Identifier offerIdentifier = new com.sun.xml.ws.rm.v200702.Identifier();
            offerIdentifier.setValue(offerInboundSequenceId);

            com.sun.xml.ws.rm.v200702.OfferType offer = new com.sun.xml.ws.rm.v200702.OfferType();
            offer.setIdentifier(offerIdentifier);
            // Microsoft does not accept CreateSequence messages if AcksTo and Offer/Endpoint are not the same
            offer.setEndpoint(csElement.getAcksTo());
            ((com.sun.xml.ws.rm.v200702.CreateSequenceElement) csElement).setOffer(offer);
        }
        if (strType != null) {
            csElement.setSecurityTokenReference(strType);
        }

        Message csMessage = Messages.create(configuration.getRMVersion().jaxbContext, csElement, configuration.getSoapVersion());
        if (strType != null) {
            HeaderList headerList = csMessage.getHeaders();
            com.sun.xml.ws.rm.v200702.UsesSequenceSTR usesSequenceSTR = new com.sun.xml.ws.rm.v200702.UsesSequenceSTR();
            usesSequenceSTR.getOtherAttributes().put(new QName(configuration.getSoapVersion().nsUri, "mustUnderstand"), "true");
            headerList.add(Headers.create(configuration.getRMVersion().jaxbContext, usesSequenceSTR));
        }
        return csMessage;
    }

    private String processRm10HandshakeResponseMessage(Message handshakeResponseMessage) throws CreateSequenceException, RmException {
        if (handshakeResponseMessage.isFault()) {
            // TODO L10N
            throw LOGGER.logSevereException(new CreateSequenceException("CreateSequence was refused by the RMDestination \n ", handshakeResponseMessage));
        }

        com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement csrMessage = unmarshallResponse(handshakeResponseMessage);
        com.sun.xml.ws.rm.v200502.Identifier idOutbound = ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) csrMessage).getIdentifier();
        // TODO accept = ((com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement) csr).getAccept();
        return idOutbound.getValue();
    }

    private String processRm11HandshakeResponseMessage(Message handshakeResponseMessage) throws CreateSequenceException, RmException {
        if (handshakeResponseMessage.isFault()) {
            // TODO L10N
            throw LOGGER.logSevereException(new CreateSequenceException("CreateSequence was refused by the RMDestination", handshakeResponseMessage));
        }

        com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement csrMessage = unmarshallResponse(handshakeResponseMessage);
        com.sun.xml.ws.rm.v200702.Identifier idOutbound = csrMessage.getIdentifier();
        // TODO accept = ((com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement) csr).getAccept();
        return idOutbound.getValue();
    }

    /**
     * Sends handshake message (CreateSequence) in order to establish the RM session.
     * 
     * @param offerInboundSequenceId nullable, if not {@code null} the value will be used as an offered response messages sequence identifier
     * @return identifier of the outgoing messages sequence.
     */
    private String sendHandshakeMessage(String offerInboundSequenceId) throws CreateSequenceException, RmException {
        Message handshakeRequestMessage;
        if (configuration.getRMVersion() == RmVersion.WSRM10) {
            handshakeRequestMessage = prepareRm10HandshakeRequest(offerInboundSequenceId, communicator.tryStartSecureConversation());
        } else {
            handshakeRequestMessage = prepareRm11HandshakeRequest(offerInboundSequenceId, communicator.tryStartSecureConversation());
        }

        Message handshakeResponseMessage = communicator.send(handshakeRequestMessage, configuration.getRMVersion().createSequenceAction);

        if (handshakeResponseMessage != null) {
            if (configuration.getRMVersion() == RmVersion.WSRM10) {
                // TODO
                return processRm10HandshakeResponseMessage(handshakeResponseMessage);
            } else {
                return processRm11HandshakeResponseMessage(handshakeResponseMessage);
            }
        } else {
            // TODO: create sequence response was null... throw an exception or handle CreateSequenceRefused fault?
            // TODO L10N
            throw LOGGER.logSevereException(new CreateSequenceException("CreateSequenceResponse was null"));
        }
    }

    private <T> T unmarshallResponse(Message response) throws RmException {
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
    public void sendAckRequested() throws RmException {
        try {
            Message ackRequestMessage = Messages.createEmpty(configuration.getSoapVersion());
            AbstractAckRequested ackRequestedElement = null;
            if (configuration.getRMVersion() == RmVersion.WSRM10) {
                ackRequestedElement = new com.sun.xml.ws.rm.v200502.AckRequestedElement();
                ackRequestedElement.setId(outboundSequenceId);
            } else {
                ackRequestedElement = new com.sun.xml.ws.rm.v200702.AckRequestedElement();
                ackRequestedElement.setId(outboundSequenceId);
            }
            ackRequestMessage.getHeaders().add(Headers.create(configuration.getRMVersion().jaxbContext, ackRequestedElement));

            Message ackResponse = null;
            try {
                ackResponse = communicator.send(ackRequestMessage, configuration.getRMVersion().ackRequestedAction);
                if (ackResponse != null && ackResponse.isFault()) {
                    // TODO L10N
                    throw LOGGER.logException(new RmException("Error sending AckRequestedElement", ackResponse), Level.WARNING);
                }

// TODO                InboundMessageProcessor.processMessage(new RMMessage(ackResponse), unmarshaller, RMSource.getRMSource(), config.getRMVersion());
            } finally {
                if (ackResponse != null) {
                    ackResponse.consume();
                }

            }
        } finally {
            //Make sure that alarm is reset.
// TODO            ((ClientOutboundSequence) seq).resetLastActivityTime();
        }
    }

    /**
     * Send Message with empty body and a single SequenceElement (with Last child) down the pipe.  Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    public void sendLast(OutboundSequence seq) throws RmException, UnknownSequenceException {
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
// TODO:            InboundMessageProcessor.processMessage(new RMMessage(response), unmarshaller, RMSource.getRMSource(), config.getRMVersion());
        } finally {
            if (response != null) {
                response.consume();
            }
        }
    }

    public void sendCloseSequence(String outboundSequenceId, long lastMessageNumber) throws RmException, CloseSequenceException {

        com.sun.xml.ws.rm.v200702.Identifier idClose = new com.sun.xml.ws.rm.v200702.Identifier();
        idClose.setValue(outboundSequenceId);

        com.sun.xml.ws.rm.v200702.CloseSequenceElement cs = new com.sun.xml.ws.rm.v200702.CloseSequenceElement();
        cs.setIdentifier(idClose);
        cs.setLastMsgNumber(lastMessageNumber); // TODO: modify the JAXB object to long

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

    public void sendTerminateSequence(AbstractTerminateSequence ts, OutboundSequence seq) throws RmException, TerminateSequenceException {
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
        
}
