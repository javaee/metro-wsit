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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.assembler.ServerTubelineAssemblyContext;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.rx.RxException;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmSecurityException;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException.Code;
import com.sun.xml.ws.rx.rm.faults.CreateSequenceRefusedFault;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.protocol.CloseSequenceData;
import com.sun.xml.ws.rx.rm.protocol.CloseSequenceResponseData;
import com.sun.xml.ws.rx.rm.protocol.CreateSequenceData;
import com.sun.xml.ws.rx.rm.protocol.CreateSequenceResponseData;
import com.sun.xml.ws.rx.rm.protocol.TerminateSequenceData;
import com.sun.xml.ws.rx.rm.protocol.TerminateSequenceResponseData;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.rm.runtime.delivery.PostmanPool;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManagerFactory;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.rx.util.Communicator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import javax.xml.ws.EndpointReference;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class ServerTube extends AbstractFilterTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(ServerTube.class);
    private static final Level PROTOCOL_FAULT_LOGGING_LEVEL = Level.WARNING;
    /**
     * TODO javadoc
     */
    private static final String SEQUENCE_PROPERTY = "com.sun.xml.ws.sequence";
    /**
     * TODO javadoc
     */
    private static final String MESSAGE_NUMBER_PROPERTY = "com.sun.xml.ws.messagenumber";
    //
    private final RuntimeContext rc;
    private final WSEndpoint endpoint;

    public ServerTube(ServerTube original, TubeCloner cloner) {
        super(original, cloner);

        this.rc = original.rc;
        this.endpoint = original.endpoint;
    }

    public ServerTube(RmConfiguration configuration, Tube tubelineHead, ServerTubelineAssemblyContext context) {
        super(tubelineHead);

        this.endpoint = context.getEndpoint();

        // TODO P3 don't take the first config alternative automatically...
        if (configuration.getAddressingVersion() == null) {
            throw new RxRuntimeException(LocalizationMessages.WSRM_1140_NO_ADDRESSING_VERSION_ON_ENDPOINT());
        }

        RuntimeContext.Builder rcBuilder = RuntimeContext.getBuilder(
                configuration,
                new Communicator(
                "rm-server-tube-communicator",
                null, // TODO P3 can we get the endpoint address?
                super.next,
                null,
                configuration.getAddressingVersion(),
                configuration.getSoapVersion(),
                configuration.getRmVersion().getJaxbContext(configuration.getAddressingVersion())));
        this.rc = rcBuilder.build();

        DeliveryQueueBuilder inboundQueueBuilder = DeliveryQueueBuilder.getBuilder(
                configuration,
                PostmanPool.INSTANCE.getPostman(),
                new ServerDestinationDeliveryCallback(rc));
        DeliveryQueueBuilder outboundQueueBuilder = null;
        if (configuration.requestResponseOperationsDetected()) {
            outboundQueueBuilder = DeliveryQueueBuilder.getBuilder(
                    configuration,
                    PostmanPool.INSTANCE.getPostman(),
                    new ServerSourceDeliveryCallback(rc));
        }

        SequenceManager sequenceManager = SequenceManagerFactory.INSTANCE.createSequenceManager(
                configuration.isPersistenceEnabled(),
                context.getEndpoint().getServiceName() + "::" + context.getEndpoint().getPortName(),
                inboundQueueBuilder,
                outboundQueueBuilder,
                configuration);

        this.rc.setSequenceManager(sequenceManager);
    }

    @Override
    public ServerTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new ServerTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet request) {
        LOGGER.entering();
        try {
            String wsaAction = rc.communicator.getWsaAction(request);
            if (rc.rmVersion.isRmAction(wsaAction)) { // protocol message
                if (rc.rmVersion.isRmProtocolRequest(wsaAction)) { // protocol request
                    return doReturnWith(processProtocolRequest(request, wsaAction));
                } else { // protocol response
                    return doThrow(new RxRuntimeException(LocalizationMessages.WSRM_1128_INVALID_WSA_ACTION_IN_PROTOCOL_REQUEST(wsaAction)));
                }
            }

            // This is an application message
            // prevent closing of TBC in case of one-way - we want to send acknowledgement back at least
            request.keepTransportBackChannelOpen();

            JaxwsApplicationMessage message = new JaxwsApplicationMessage(request, request.getMessage().getID(rc.addressingVersion, rc.soapVersion));

            rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());

            if (!isSecurityContextTokenIdValid(rc.getSequence(message.getSequenceId()).getBoundSecurityTokenReferenceId(), message.getPacket())) {
                // TODO L10N + maybe throw SOAP fault exception?
                throw new RmSecurityException("Security context token on the message does not match the token bound to the sequence");
            }


            rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());
            rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());

            if (!hasSession(request)) { // security did not set session - we must do it
                setSession(message.getSequenceId(), request);
            }
            exposeSequenceDataToUser(message);

            try {
                rc.destinationMessageHandler.registerMessage(message);
            } catch (DuplicateMessageRegistrationException ex) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("Duplicate message number [ %d ] received on sequence [ %s ]",
                            message.getMessageNumber(),
                            message.getSequenceId()), ex);
                }
                return handleDuplicateMessageException(message, request);
            }

            synchronized (message.getCorrelationId()) {
                // this synchronization is needed so that all 3 operations occur before
                // AbstractResponseHandler.getParentFiber() is invoked on the response thread
                rc.suspendedFiberStorage.register(message.getCorrelationId(), Fiber.current());
                rc.destinationMessageHandler.putToDeliveryQueue(message);

                return doSuspend();
            }
        } catch (AbstractSoapFaultException ex) {
            LOGGER.logException(ex, PROTOCOL_FAULT_LOGGING_LEVEL);
            return doReturnWith(ex.toResponse(rc, request));
        } catch (RxRuntimeException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);

        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet response) {
        LOGGER.entering();
        try {
            return super.processResponse(response);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processException(Throwable throwable) {
        LOGGER.entering();
        try {
            return super.processException(throwable);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        LOGGER.entering();
        try {
            rc.close();

            SessionManager.removeSessionManager(endpoint);
        } finally {
            super.preDestroy();
            LOGGER.exiting();
        }
    }

    private NextAction handleDuplicateMessageException(JaxwsApplicationMessage message, Packet request) throws UnknownSequenceException, RxRuntimeException {
        // Replay model behavior
        Sequence outboundSequence = rc.getBoundSequence(message.getSequenceId());
        if (outboundSequence != null) {
            final ApplicationMessage _responseMessage = outboundSequence.retrieveMessage(message.getCorrelationId());
            if (_responseMessage == null) {
                return doReturnWith(createEmptyAcknowledgementResponse(request, message.getSequenceId()));
            }
            if (rc.configuration.isPersistenceEnabled() && _responseMessage instanceof JaxwsApplicationMessage) {
                JaxwsApplicationMessage jaxwsAppMsg = (JaxwsApplicationMessage) _responseMessage;
                if (jaxwsAppMsg.getPacket() == null) {
                    // FIXME: loaded from DB without a valid packet - create one
                    // ...this is a workaround until JAX-WS RI API provides a mechanism how to (de)serialize whole Packet
                    jaxwsAppMsg.setPacket(rc.communicator.createEmptyResponsePacket(request, jaxwsAppMsg.getWsaAction()));
                }
            }
            // retrieved response is not null
            Fiber oldRegisteredFiber = rc.suspendedFiberStorage.register(_responseMessage.getCorrelationId(), Fiber.current());
            if (oldRegisteredFiber != null) {
                oldRegisteredFiber.resume(createEmptyAcknowledgementResponse(request, message.getSequenceId()));
            }
            rc.sourceMessageHandler.putToDeliveryQueue(_responseMessage);
            return doSuspend();
        } else {
            return doReturnWith(createEmptyAcknowledgementResponse(request, message.getSequenceId()));
        }
    }

    private Packet processProtocolRequest(Packet request, String wsaAction) throws AbstractSoapFaultException {
        if (rc.rmVersion.createSequenceAction.equals(wsaAction)) {
            return handleCreateSequenceAction(request);
        } else if (rc.rmVersion.closeSequenceAction.equals(wsaAction)) {
            return handleCloseSequenceAction(request);
        } else if (rc.rmVersion.terminateSequenceAction.equals(wsaAction)) {
            return handleTerminateSequenceAction(request);
        } else if (rc.rmVersion.ackRequestedAction.equals(wsaAction)) {
            return handleAckRequestedAction(request);
        } else if (rc.rmVersion.sequenceAcknowledgementAction.equals(wsaAction)) {
            return handleSequenceAcknowledgementAction(request);
        } else {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1134_UNSUPPORTED_PROTOCOL_MESSAGE(wsaAction)));
        }
    }

    private Packet handleCreateSequenceAction(Packet request) throws CreateSequenceRefusedFault {
        CreateSequenceData requestData = rc.protocolHandler.toCreateSequenceData(request);

        EndpointReference requestDestination = null;
        if (requestData.getOfferedSequenceId() != null) {
            if (rc.sequenceManager().isValid(requestData.getOfferedSequenceId())) {
                // we already have such sequence
                throw new CreateSequenceRefusedFault(
                        LocalizationMessages.WSRM_1137_OFFERED_ID_ALREADY_IN_USE(requestData.getOfferedSequenceId()),
                        Code.Sender);
            }

            final String wsaTo = rc.communicator.getWsaTo(request);
            try {
                requestDestination = new WSEndpointReference(new URI(wsaTo), rc.addressingVersion).toSpec();
            } catch (URISyntaxException e) {
                throw new CreateSequenceRefusedFault(
                        LocalizationMessages.WSRM_1129_INVALID_VALUE_OF_MESSAGE_HEADER("To", "CreateSequence", wsaTo),
                        Code.Sender,
                        e);
            } catch (NullPointerException e) {
                throw new CreateSequenceRefusedFault(
                        LocalizationMessages.WSRM_1130_MISSING_MESSAGE_HEADER("To", "CreateSequence", wsaTo),
                        Code.Sender,
                        e);
            }
        }


        String receivedSctId = null;
        if (requestData.getStrType() != null) { // RM messaging should be bound to a secured session
            // FIXME: The STR processing should probably only check if the
            // com.sun.xml.ws.runtime.util.Session was started by security tube
            // and if the STR id equals to the one in this session...
            String activeSctId = getSecurityContextTokenId(request);
            if (activeSctId == null) {
                throw new CreateSequenceRefusedFault(
                        LocalizationMessages.WSRM_1133_NO_SECURITY_TOKEN_IN_REQUEST_PACKET(),
                        Code.Sender);
            }
            try {
                receivedSctId = Utilities.extractSecurityContextTokenId(requestData.getStrType());
            } catch (RxException ex) {
                throw new CreateSequenceRefusedFault(
                        ex.getMessage(),
                        Code.Sender);
            }

            if (!activeSctId.equals(receivedSctId)) {
                throw new CreateSequenceRefusedFault(
                        LocalizationMessages.WSRM_1131_SECURITY_TOKEN_AUTHORIZATION_ERROR(receivedSctId, activeSctId),
                        Code.Sender);
            }
        }

        Sequence inboundSequence = rc.sequenceManager().createInboundSequence(
                rc.sequenceManager().generateSequenceUID(),
                receivedSctId,
                calculateSequenceExpirationTime(requestData.getDuration()));

        if (requestData.getOfferedSequenceId() != null) {
            Sequence outboundSequence = rc.sequenceManager().createOutboundSequence(
                    requestData.getOfferedSequenceId(),
                    receivedSctId,
                    calculateSequenceExpirationTime(requestData.getOfferedSequenceExpiry()));
            rc.sequenceManager().bindSequences(inboundSequence.getId(), outboundSequence.getId());
            rc.sequenceManager().bindSequences(outboundSequence.getId(), inboundSequence.getId());
        }

        if (!hasSession(request)) { // security did not start session - we must do it
            Utilities.startSession(request.endpoint, inboundSequence.getId());
        }

        final CreateSequenceResponseData.Builder responseBuilder = CreateSequenceResponseData.getBuilder(inboundSequence.getId());
        // TODO P2 set expiration time, incomplete sequence behavior
        if (requestData.getOfferedSequenceId() != null) {
            responseBuilder.acceptedSequenceAcksTo(requestDestination);
        }

        return rc.protocolHandler.toPacket(responseBuilder.build(), request);
    }

    private Packet handleCloseSequenceAction(Packet request) {
        CloseSequenceData requestData = rc.protocolHandler.toCloseSequenceData(request);

        rc.destinationMessageHandler.processAcknowledgements(requestData.getAcknowledgementData());

        Sequence inboundSequence = rc.getSequence(requestData.getSequenceId());

        // TODO handle last message number - pass it to the sequence so that it can allocate new unacked messages if necessary
        // int lastMessageNumber = closeSeqElement.getLastMsgNumber();

        String boundSequenceId = rc.getBoundSequenceId(inboundSequence.getId());
        try {
            rc.sequenceManager().closeSequence(inboundSequence.getId());
        } finally {
            if (boundSequenceId != null) {
                rc.sequenceManager().closeSequence(boundSequenceId);
            }
        }

        final CloseSequenceResponseData.Builder responseBuilder = CloseSequenceResponseData.getBuilder(inboundSequence.getId());
        responseBuilder.acknowledgementData(rc.destinationMessageHandler.getAcknowledgementData(inboundSequence.getId()));
        return rc.protocolHandler.toPacket(responseBuilder.build(), request);
    }

    private Packet handleTerminateSequenceAction(Packet request) {
        TerminateSequenceData requestData = rc.protocolHandler.toTerminateSequenceData(request);

        rc.destinationMessageHandler.processAcknowledgements(requestData.getAcknowledgementData());

        // Formulating response:
        //   If there is an outbound sequence, client expects us to terminate it => sending TerminateSequence back.
        //   If not, we send TerminateSequenceResponse
        Sequence inboundSequence = rc.getSequence(requestData.getSequenceId());
        Sequence outboundSeqence = rc.getBoundSequence(requestData.getSequenceId());
        try {
            if (outboundSeqence != null) {
                TerminateSequenceData.Builder responseBuilder = TerminateSequenceData.getBuilder(outboundSeqence.getId(), outboundSeqence.getLastMessageNumber());
                responseBuilder.acknowledgementData(rc.destinationMessageHandler.getAcknowledgementData(inboundSequence.getId()));
                return rc.protocolHandler.toPacket(responseBuilder.build(), request);
            } else {
                final TerminateSequenceResponseData.Builder responseBuilder = TerminateSequenceResponseData.getBuilder(inboundSequence.getId());
                responseBuilder.acknowledgementData(rc.destinationMessageHandler.getAcknowledgementData(inboundSequence.getId()));
                return rc.protocolHandler.toPacket(responseBuilder.build(), request);
            }

        } finally {
            Utilities.endSessionIfExists(request.endpoint, inboundSequence.getId());
            try {
                rc.sequenceManager().terminateSequence(inboundSequence.getId());
            } finally {
                if (outboundSeqence != null) {
                    rc.sequenceManager().terminateSequence(outboundSeqence.getId());
                }
            }
        }
    }

    private Packet handleSequenceAcknowledgementAction(Packet request) { // TODO move packet creation processing to protocol handler
        AcknowledgementData ackData = rc.protocolHandler.getAcknowledgementData(request.getMessage());
        rc.destinationMessageHandler.processAcknowledgements(ackData);

        request.transportBackChannel.close();
        return rc.communicator.createNullResponsePacket(request);
    }

    private Packet handleAckRequestedAction(Packet request) { // TODO move packet creation processing to protocol handler
        AcknowledgementData ackData = rc.protocolHandler.getAcknowledgementData(request.getMessage());
        rc.destinationMessageHandler.processAcknowledgements(ackData);

        return createEmptyAcknowledgementResponse(request, ackData.getAckReqestedSequenceId());
    }

    private Packet createEmptyAcknowledgementResponse(Packet request, String sequenceId) throws RxRuntimeException {
        Packet response = rc.communicator.createEmptyResponsePacket(request, rc.rmVersion.sequenceAcknowledgementAction);
        rc.protocolHandler.appendAcknowledgementHeaders(response, rc.destinationMessageHandler.getAcknowledgementData(sequenceId));
        return response;
    }

    /**
     * TODO javadoc
     */
    private Session getSession(Packet packet) {
        String sessionId = (String) packet.invocationProperties.get(Session.SESSION_ID_KEY);
        if (sessionId == null) {
            return null;
        }

        return SessionManager.getSessionManager(packet.endpoint).getSession(sessionId);
    }

    /**
     * TODO javadoc
     */
    private boolean hasSession(Packet packet) {
        return getSession(packet) != null;
    }

    /**
     * TODO javadoc
     */
    private void setSession(String sessionId, Packet packet) {
        packet.invocationProperties.put(Session.SESSION_ID_KEY, sessionId);

        Session session = SessionManager.getSessionManager(packet.endpoint).getSession(sessionId);

        if (session == null && rc.configuration.isPersistenceEnabled()) {
            // inbound sequence id (session id) is valid, but there's no session 
            // in the session manager => there must have been a crash or redeploy
            // starting a new session
            session = Utilities.startSession(packet.endpoint, sessionId);
        }

        packet.invocationProperties.put(Session.SESSION_KEY, session.getUserData());
    }

    private void exposeSequenceDataToUser(JaxwsApplicationMessage message) {
        message.getPacket().invocationProperties.put(SEQUENCE_PROPERTY, message.getSequenceId());
        message.getPacket().invocationProperties.put(MESSAGE_NUMBER_PROPERTY, message.getMessageNumber());
    }

    /**
     * TODO javadoc
     */
    private final String getSecurityContextTokenId(Packet packet) {
        Session session = getSession(packet);
        return (session != null) ? session.getSecurityInfo().getIdentifier() : null;
    }

    /**
     * Determines whether the security context token identifier used to secure the message
     * wrapped in the packet is the expected one
     *
     * @param expectedStrId expected security context token identifier
     * @param packet packet wrapping the checked message
     * @returns {code true} if the actual security context token identifier equals to the expected one
     */
    public final boolean isSecurityContextTokenIdValid(String expectedSctId, Packet packet) {
        String actualSctId = getSecurityContextTokenId(packet);
        return (expectedSctId != null) ? expectedSctId.equals(actualSctId) : actualSctId == null;
    }

    private final long calculateSequenceExpirationTime(long expiryDuration) {
        if (expiryDuration == Sequence.NO_EXPIRY) {
            return Sequence.NO_EXPIRY;
        } else {
            return expiryDuration + rc.sequenceManager().currentTimeInMillis();
        }
    }
}
