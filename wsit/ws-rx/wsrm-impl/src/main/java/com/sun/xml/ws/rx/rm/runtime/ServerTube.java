/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.assembler.dev.ServerTubelineAssemblyContext;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.runtime.dev.Session;
import com.sun.xml.ws.runtime.dev.SessionManager;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmSecurityException;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException.Code;
import com.sun.xml.ws.rx.rm.faults.CreateSequenceRefusedFault;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData;
import com.sun.xml.ws.rx.rm.protocol.AcknowledgementData.Builder;
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
import com.sun.xml.ws.rx.rm.runtime.transaction.TransactionPropertySet;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.security.secconv.STRValidationHelper;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

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
     * Property that is exposing current sequence identifier through the message context
     */
    static final String SEQUENCE_PROPERTY = "com.sun.xml.ws.sequence";
    /**
     * Property that is exposing current message number through the message context
     */
    static final String MESSAGE_NUMBER_PROPERTY = "com.sun.xml.ws.messagenumber";
    //
    private final RuntimeContext rc;
    private final WSEndpoint endpoint;
    private STRValidationHelper validator;

    public ServerTube(ServerTube original, TubeCloner cloner) {
        super(original, cloner);

        this.rc = original.rc;
        this.endpoint = original.endpoint;
        this.validator = original.validator;
    }

    public ServerTube(RmConfiguration configuration, ServerTubelineAssemblyContext context) {
        super(context.getTubelineHead());

        this.endpoint = context.getEndpoint();

        // TODO P3 don't take the first config alternative automatically...
        if (configuration.getAddressingVersion() == null) {
            throw new RxRuntimeException(LocalizationMessages.WSRM_1140_NO_ADDRESSING_VERSION_ON_ENDPOINT());
        }

        RuntimeContext.Builder rcBuilder = RuntimeContext.builder(
                configuration,
                Communicator.builder("rm-server-tube-communicator")
                .tubelineHead(super.next)
                .addressingVersion(configuration.getAddressingVersion())
                .soapVersion(configuration.getSoapVersion())
                .jaxbContext(configuration.getRuntimeVersion().getJaxbContext(configuration.getAddressingVersion()))
                .container(context.getEndpoint().getContainer())
                .build());
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
                configuration.getRmFeature().isPersistenceEnabled(),
                context.getEndpoint().getServiceName() + "::" + context.getEndpoint().getPortName(),
                inboundQueueBuilder,
                outboundQueueBuilder,
                configuration,
                context.getEndpoint().getContainer());

        this.rc.setSequenceManager(sequenceManager);
        
        // TODO instead of default, consider adding Metro impl to the container
        validator = context.getEndpoint().getContainer().getSPI(STRValidationHelper.class);
        if (validator == null) {
            validator = new MetroSTRValidationHelper();
        }

        LOGGER.fine("STRValidationHelper: " + validator.getClass().getName());
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
            HaContext.initFrom(request);
            if (HaContext.failoverDetected()) {
                rc.sequenceManager().invalidateCache();
            }

            String wsaAction = rc.communicator.getWsaAction(request);
            if (rc.rmVersion.protocolVersion.isProtocolAction(wsaAction)) { // protocol message
                return doReturnWith(processProtocolMessage(request, wsaAction));
            }

            // This is an application message
            // prevent closing of TBC in case of one-way - we want to send acknowledgement back at least
            request.keepTransportBackChannelOpen();

            final JaxwsApplicationMessage message = new JaxwsApplicationMessage(request, request.getMessage().getID(rc.addressingVersion, rc.soapVersion));
            rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
            rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());

            validateSecurityContextTokenId(rc.sequenceManager().getInboundSequence(message.getSequenceId()).getBoundSecurityTokenReferenceId(), message.getPacket());
            if (!hasSession(request)) { // security did not set session - we must do it
                setSession(message.getSequenceId(), request);
            }
            exposeSequenceDataToUser(message);

            rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());

            boolean useTXConfigEnabled = rc.configuration.getRmFeature().isDistributedTXForServerRMDEnabled();
            if (useTXConfigEnabled) {
                boolean canBegin = rc.transactionHandler.canBegin();
                if (canBegin) {
                    int txTimeout = getTransactionTimeout();
                    rc.transactionHandler.begin(txTimeout);

                    TransactionPropertySet ps = new TransactionPropertySet();
                    ps.setTransactionOwned(true);
                    message.getPacket().addSatellite(ps);
                } else {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        //TODO i18n
                        LOGGER.warning("Found isDistributedTXForServerRMDEnabled() true but could not "
                                + "begin transaction. Existing transaction found.");
                    }
                }
            }

            try {
                rc.destinationMessageHandler.registerMessage(message);
            } catch (DuplicateMessageRegistrationException ex) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(LocalizationMessages.WSRM_1145_DUPLICATE_MSG_NUMBER_RECEIVED(
                            message.getMessageNumber(),
                            message.getSequenceId()), ex);
                }
                return handleDuplicateMessageException(message, request);
            }

            synchronized (message.getCorrelationId()) {
                // this synchronization is needed so that all 3 operations occur before
                // AbstractResponseHandler.getParentFiber() is invoked on the response thread
                rc.suspendedFiberStorage.register(message.getCorrelationId(), Fiber.current());
                return doSuspend(new Runnable() {
                    @Override
                    public void run() {
                        rc.destinationMessageHandler.putToDeliveryQueue(message);
                    }
                });
            }
        } catch (AbstractSoapFaultException ex) {
            LOGGER.logException(ex, PROTOCOL_FAULT_LOGGING_LEVEL);
            return doReturnWith(ex.toResponse(rc, request));
        } catch (RxRuntimeException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } finally {
            HaContext.clear();
            LOGGER.exiting();
        }
    }

    private int getTransactionTimeout() {
        int txTimeout = -1;
        //TODO System property is temporary.
        String txTimeoutString = 
                System.getProperty("metro.rm.server.rmd.tx.timeout.seconds");
        if (txTimeoutString != null && !txTimeoutString.isEmpty()) {
            try {
                txTimeout = Integer.parseInt(txTimeoutString);
            } catch (NumberFormatException e) {
                // Ignore
            }
        }

        if (txTimeout == -1) {
            txTimeout = rc.configuration.getRmFeature().getDistributedTXForServerRMDTimeoutInSeconds();
        }
        return txTimeout;
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

    private NextAction handleDuplicateMessageException(final JaxwsApplicationMessage message, Packet request) throws UnknownSequenceException, RxRuntimeException {
        // Is failed over during request processing?
        final Sequence inboundSequence = rc.sequenceManager().getInboundSequence(message.getSequenceId());
        if (inboundSequence.isFailedOver(message.getMessageNumber())) {
            synchronized (message.getCorrelationId()) {
                // this synchronization is needed so that all 3 operations occur before
                // AbstractResponseHandler.getParentFiber() is invoked on the response thread
                rc.suspendedFiberStorage.register(message.getCorrelationId(), Fiber.current());
                return doSuspend(new Runnable() {
                    @Override
                    public void run() {
                        rc.destinationMessageHandler.putToDeliveryQueue(message);
                    }
                });
            }
        }

        // Microsoft replay model behavior where a request is sent only to give a ride to a 
        // previously generated and retained response
        final Sequence outboundSequence = rc.sequenceManager().getBoundSequence(message.getSequenceId());
        if (outboundSequence != null) {
            final ApplicationMessage _responseMessage = outboundSequence.retrieveMessage(message.getCorrelationId());
            if (_responseMessage == null) {
                return doReturnWith(rc.protocolHandler.createEmptyAcknowledgementResponse(
                        rc.destinationMessageHandler.getAcknowledgementData(message.getSequenceId()),
                        request));
            }
            if (rc.configuration.getRmFeature().isPersistenceEnabled() || HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
                if (_responseMessage instanceof JaxwsApplicationMessage) {
                    JaxwsApplicationMessage jaxwsAppMsg = (JaxwsApplicationMessage) _responseMessage;
                    if (jaxwsAppMsg.getPacket() == null) {
                        // FIXME: loaded from DB without a valid packet - create one
                        // ...this is a workaround until JAX-WS RI API provides a mechanism how to (de)serialize whole Packet
                        jaxwsAppMsg.setPacket(rc.communicator.createEmptyResponsePacket(request, jaxwsAppMsg.getWsaAction()));
                    }
                }
            }
            // retrieved response is not null
            Fiber oldRegisteredFiber = rc.suspendedFiberStorage.register(_responseMessage.getCorrelationId(), Fiber.current());
            if (oldRegisteredFiber != null) {
                oldRegisteredFiber.resume(rc.protocolHandler.createEmptyAcknowledgementResponse(
                        rc.destinationMessageHandler.getAcknowledgementData(message.getSequenceId()),
                        request));
            }

            return doSuspend(new Runnable() {
                @Override
                public void run() {
                    rc.sourceMessageHandler.putToDeliveryQueue(_responseMessage);
                }
            });
        } else {
            return doReturnWith(rc.protocolHandler.createEmptyAcknowledgementResponse(
                    rc.destinationMessageHandler.getAcknowledgementData(message.getSequenceId()),
                    request));
        }
    }

    private Packet processProtocolMessage(Packet request, String wsaAction) throws AbstractSoapFaultException {
        if (rc.rmVersion.protocolVersion.createSequenceAction.equals(wsaAction)) {
            return handleCreateSequenceAction(request);
        } else if (rc.rmVersion.protocolVersion.closeSequenceAction.equals(wsaAction)) {
            return handleCloseSequenceAction(request);
        } else if (rc.rmVersion.protocolVersion.terminateSequenceAction.equals(wsaAction)) {
            return handleTerminateSequenceAction(request);
        } else if (rc.rmVersion.protocolVersion.ackRequestedAction.equals(wsaAction)) {
            return handleAckRequestedAction(request);
        } else if (rc.rmVersion.protocolVersion.sequenceAcknowledgementAction.equals(wsaAction)) {
            return handleSequenceAcknowledgementAction(request);
        } else if (rc.rmVersion.protocolVersion.terminateSequenceResponseAction.equals(wsaAction)) {
            return handleTerminateSequenceResponseAction(request);
        } else {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1134_UNSUPPORTED_PROTOCOL_MESSAGE(wsaAction)));
        }
    }

    private Packet handleCreateSequenceAction(Packet request) throws CreateSequenceRefusedFault {
        CreateSequenceData requestData = rc.protocolHandler.toCreateSequenceData(request);

        EndpointReference requestDestination = null;
        if (requestData.getOfferedSequenceId() != null && rc.configuration.requestResponseOperationsDetected()) {
            // there is an offered sequence and this endpoint does contain some 2-way operations
            // if this is a oneway-only endpoint, we simply ignore the offered sequence (WS-I RSP R0011)
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
            String activeSctId = validator.getSecurityContextTokenId(request);
            if (activeSctId == null) {
                throw new CreateSequenceRefusedFault(
                        LocalizationMessages.WSRM_1133_NO_SECURITY_TOKEN_IN_REQUEST_PACKET(),
                        Code.Sender);
            }

            try {
                receivedSctId = validator.extractSecurityTokenId(requestData.getStrType());
            } catch (Exception ex) {
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

        final CreateSequenceResponseData.Builder responseBuilder = CreateSequenceResponseData.getBuilder(inboundSequence.getId());
        // TODO P2 set expiration time, incomplete sequence behavior

        if (requestData.getOfferedSequenceId() != null && rc.configuration.requestResponseOperationsDetected()) {
            // there is an offered sequence and this endpoint does contain some 2-way operations
            // if this is a oneway-only endpoint, we simply ignore the offered sequence (WS-I RSP R0011)
            Sequence outboundSequence = rc.sequenceManager().createOutboundSequence(
                    requestData.getOfferedSequenceId(),
                    receivedSctId,
                    calculateSequenceExpirationTime(requestData.getOfferedSequenceExpiry()));
            rc.sequenceManager().bindSequences(inboundSequence.getId(), outboundSequence.getId());
            rc.sequenceManager().bindSequences(outboundSequence.getId(), inboundSequence.getId());

            responseBuilder.acceptedSequenceAcksTo(requestDestination);
        }

        if (!hasSession(request)) { // security did not start session - we must do it
            Utilities.startSession(request.endpoint, inboundSequence.getId());
        }

        return rc.protocolHandler.toPacket(responseBuilder.build(), request, false);
    }
    
    private class MetroSTRValidationHelper implements STRValidationHelper {
        @Override
        public String getSecurityContextTokenId(final Packet packet) {
            final Session session = getSession(packet);
            return (session != null) ? session.getSecurityInfo().getIdentifier() : null;
        }
    
        @Override
        public String extractSecurityTokenId(final SecurityTokenReferenceType str) throws Exception {
            return Utilities.extractSecurityContextTokenId(str);
        }
    }

    private Packet handleCloseSequenceAction(Packet request) {
        CloseSequenceData requestData = rc.protocolHandler.toCloseSequenceData(request);

        rc.destinationMessageHandler.processAcknowledgements(requestData.getAcknowledgementData());

        Sequence inboundSequence = rc.sequenceManager().getInboundSequence(requestData.getSequenceId());

        // TODO handle last message number - pass it to the sequence so that it can allocate new unacked messages if necessary
        // int lastMessageNumber = closeSeqElement.getLastMsgNumber();

        String boundSequenceId = rc.getBoundSequenceId(inboundSequence.getId());

        try {
            rc.sequenceManager().closeSequence(inboundSequence.getId());

            final CloseSequenceResponseData.Builder responseBuilder = CloseSequenceResponseData.getBuilder(inboundSequence.getId());
            // override the final sequence acknowledgement data as this sequence is not closed yet, but is closing already
            Builder ackDataBuilder = AcknowledgementData.getBuilder(rc.destinationMessageHandler.getAcknowledgementData(inboundSequence.getId()));

            ackDataBuilder.acknowledgements(inboundSequence.getId(), inboundSequence.getAcknowledgedMessageNumbers(), true);
            inboundSequence.clearAckRequestedFlag();

            responseBuilder.acknowledgementData(ackDataBuilder.build());

            return rc.protocolHandler.toPacket(responseBuilder.build(), request, false);
        } finally {
            if (boundSequenceId != null) {
                rc.sequenceManager().closeSequence(boundSequenceId);
            }
        }
    }

    private Packet handleTerminateSequenceAction(Packet request) {
        TerminateSequenceData requestData = rc.protocolHandler.toTerminateSequenceData(request);

        rc.destinationMessageHandler.processAcknowledgements(requestData.getAcknowledgementData());

        Sequence inboundSequence = rc.sequenceManager().getInboundSequence(requestData.getSequenceId());
        Sequence outboundSeqence = rc.sequenceManager().getBoundSequence(requestData.getSequenceId());
        try {
            final TerminateSequenceResponseData.Builder responseBuilder = TerminateSequenceResponseData.getBuilder(inboundSequence.getId());
            responseBuilder.acknowledgementData(rc.destinationMessageHandler.getAcknowledgementData(inboundSequence.getId()));

            if (outboundSeqence != null) {
                responseBuilder.boundSequenceData(outboundSeqence.getId(), outboundSeqence.getLastMessageNumber());
            }

            return rc.protocolHandler.toPacket(responseBuilder.build(), request, false);
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

    private Packet handleTerminateSequenceResponseAction(Packet request) {
        TerminateSequenceResponseData data = rc.protocolHandler.toTerminateSequenceResponseData(request);

        rc.destinationMessageHandler.processAcknowledgements(data.getAcknowledgementData());

        // TODO P1 add more TSR data handling

        request.transportBackChannel.close();
        return rc.communicator.createNullResponsePacket(request);
    }

    private Packet handleSequenceAcknowledgementAction(Packet request) { // TODO move packet creation processing to protocol handler
        AcknowledgementData ackData = rc.protocolHandler.getAcknowledgementData(request.getMessage());
        rc.destinationMessageHandler.processAcknowledgements(ackData);

        request.transportBackChannel.close();
        return rc.communicator.createNullResponsePacket(request);
    }

    private Packet handleAckRequestedAction(Packet request) { // TODO move packet creation processing to protocol handler
        //when true, RM_SEQUENCES DB table row contention is avoided when handling AckRequested
        boolean noStateUpdate = rc.configuration.getRmFeature().isStateUpdateOnReceivedAckRequestedDisabled();
        
        AcknowledgementData ackData = rc.protocolHandler.getAcknowledgementData(request.getMessage());
        rc.destinationMessageHandler.processAcknowledgements(ackData, noStateUpdate);

        return rc.protocolHandler.createEmptyAcknowledgementResponse(rc.destinationMessageHandler.getAcknowledgementData(ackData.getAckReqestedSequenceId(), true, noStateUpdate), request);
    }

    /**
     * TODO javadoc
     */
    private Session getSession(Packet packet) {
        String sessionId = (String) packet.invocationProperties.get(Session.SESSION_ID_KEY);
        if (sessionId == null) {
            return null;
        }

        return SessionManager.getSessionManager(packet.endpoint,null).getSession(sessionId);
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

        Session session = SessionManager.getSessionManager(packet.endpoint,null).getSession(sessionId);

        if (session == null) {
            session = Utilities.startSession(packet.endpoint, sessionId);
        }

        packet.invocationProperties.put(Session.SESSION_KEY, session.getUserData());
    }

    private void exposeSequenceDataToUser(JaxwsApplicationMessage message) {
        message.getPacket().invocationProperties.put(SEQUENCE_PROPERTY, message.getSequenceId());
        message.getPacket().invocationProperties.put(MESSAGE_NUMBER_PROPERTY, message.getMessageNumber());
    }

    /**
     * Determines whether the security context token identifier used to secure the message
     * wrapped in the packet is the expected one
     *
     * @param expectedStrId expected security context token identifier
     * @param packet packet wrapping the checked message
     * @throws RmSecurityException if the actual security context token identifier does not equal to the expected one
     */
    private void validateSecurityContextTokenId(String expectedSctId, Packet packet) throws RmSecurityException {
        String actualSctId = validator.getSecurityContextTokenId(packet);
        boolean isValid = (expectedSctId != null) ? expectedSctId.equals(actualSctId) : actualSctId == null;

        if (!isValid) {
            throw new RmSecurityException(LocalizationMessages.WSRM_1131_SECURITY_TOKEN_AUTHORIZATION_ERROR(actualSctId, expectedSctId));
        }
    }

    private long calculateSequenceExpirationTime(long expiryDuration) {
        if (expiryDuration == Sequence.NO_EXPIRY) {
            return Sequence.NO_EXPIRY;
        } else {
            return expiryDuration + rc.sequenceManager().currentTimeInMillis();
        }
    }
}
