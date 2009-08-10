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

import com.sun.xml.ws.commons.VolatileReference;
import com.sun.xml.ws.commons.MaintenanceTaskExecutor;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.mc.runtime.McClientTube;
import com.sun.xml.ws.rx.mc.runtime.spi.ProtocolMessageHandler;
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
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManagerFactory;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Attaches additional RM-specific headers to each request message and ensures the reliable delivery of the message (in
 * case of any problems with sending the message, the exception is evaluated and the message is scheduled for a resend
 * if possible.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class ClientTube extends AbstractFilterTubeImpl {
    //
    private static final Logger LOGGER = Logger.getLogger(ClientTube.class);
    private static final Lock INIT_LOCK = new ReentrantLock();
    //
    private final RuntimeContext rc;
    private final WSEndpointReference rmSourceReference;
    //
    private volatile VolatileReference<String> outboundSequenceId;

    ClientTube(ClientTube original, TubeCloner cloner) {
        super(original, cloner);
        this.rc = original.rc;

        this.rmSourceReference = original.rmSourceReference;
        this.outboundSequenceId = original.outboundSequenceId;
    }

    ClientTube(RmConfiguration configuration, Tube tubelineHead, ClientTubelineAssemblyContext context) throws RxRuntimeException {
        super(tubelineHead); // cannot use context.getTubelineHead as McClientTube might have been created in RxTubeFactory

        this.outboundSequenceId = new VolatileReference<String>(null);

        SecureConversationInitiator scInitiator = context.getImplementation(SecureConversationInitiator.class);
        if (scInitiator == null) {
            // TODO P3 remove this condition and remove context.getScInitiator() method
            scInitiator = context.getScInitiator();
        }

        this.rc = RuntimeContext.getBuilder(
                configuration,
                new Communicator(
                "rm-client-tube-communicator",
                context.getAddress(),
                super.next,
                scInitiator,
                configuration.getAddressingVersion(),
                configuration.getSoapVersion(),
                configuration.getRmVersion().getJaxbContext(configuration.getAddressingVersion()))).build();

        DeliveryQueueBuilder outboundQueueBuilder = DeliveryQueueBuilder.getBuilder(
                rc.configuration,
                PostmanPool.INSTANCE.getPostman(),
                new ClientSourceDeliveryCallback(rc));

        DeliveryQueueBuilder inboundQueueBuilder = null;
        if (rc.configuration.requestResponseOperationsDetected()) {
            inboundQueueBuilder = DeliveryQueueBuilder.getBuilder(
                    rc.configuration,
                    PostmanPool.INSTANCE.getPostman(),
                    new ClientDestinationDeliveryCallback(rc));
        }

        SequenceManager sequenceManager = SequenceManagerFactory.INSTANCE.createSequenceManager(
                false, // TODO right now we decided not to support client side presisitence, revisit later
                context.getAddress().getURI().toString(),
                inboundQueueBuilder,
                outboundQueueBuilder,
                rc.configuration);
        rc.setSequenceManager(sequenceManager);

        // TODO P3 we should also take into account addressable clients
        final McClientTube mcClientTube = context.getImplementation(McClientTube.class);
        if (configuration.isMakeConnectionSupportEnabled()) {
            assert mcClientTube != null;

            this.rmSourceReference = mcClientTube.getWsmcAnonymousEndpointReference();
            mcClientTube.registerProtocolMessageHandler(createRmProtocolMessageHandler(
                    rc.configuration,
                    rc.protocolHandler,
                    rc.destinationMessageHandler));
        } else {
            this.rmSourceReference = configuration.getAddressingVersion().anonymousEpr;
        }
    }

    @Override
    public ClientTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new ClientTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet request) {
        LOGGER.entering();
        try {
            try {
                INIT_LOCK.lock();
                if (outboundSequenceId.value == null) { // RM session not initialized yet - need to synchronize
                    openRmSession(request);
                }
            } finally {
                INIT_LOCK.unlock();
            }
            assert outboundSequenceId != null;

            JaxwsApplicationMessage message = new JaxwsApplicationMessage(
                    request,
                    request.getMessage().getID(rc.addressingVersion, rc.soapVersion));


            rc.sourceMessageHandler.registerMessage(message, outboundSequenceId.value);

            synchronized (message.getCorrelationId()) {
                // this synchronization is needed so that all 3 operations occur before
                // AbstractResponseHandler.getParentFiber() is invoked on the response thread
                rc.suspendedFiberStorage.register(message.getCorrelationId(), Fiber.current());
                rc.sourceMessageHandler.putToDeliveryQueue(message);
                return super.doSuspend();
            }
        } catch (DuplicateMessageRegistrationException ex) {
            // TODO P2 duplicate message exception handling
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } catch (RxRuntimeException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet repsonse) {
        LOGGER.entering();
        try {
            return super.processResponse(repsonse);
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
            if (outboundSequenceId.value != null) { // RM session has already started
                closeRmSession();
            }
        } catch (RuntimeException ex) {
            LOGGER.warning(LocalizationMessages.WSRM_1103_RM_SEQUENCE_NOT_TERMINATED_NORMALLY(), ex);
        } finally {
            super.preDestroy();
            LOGGER.exiting();
        }
    }

    static final ProtocolMessageHandler createRmProtocolMessageHandler(
            final RmConfiguration configuration,
            final WsrmProtocolHandler protocolHandler,
            final DestinationMessageHandler dstMsgHandler) {
        return new ProtocolMessageHandler() {

            Collection<String> SUPPORTED_WSA_ACTIONS = Collections.unmodifiableCollection(Arrays.asList(new String[]{
                        configuration.getRmVersion().ackRequestedAction,
                        // configuration.getRmVersion().closeSequenceAction,
                        // configuration.getRmVersion().closeSequenceResponseAction,
                        // configuration.getRmVersion().createSequenceAction,
                        // configuration.getRmVersion().createSequenceResponseAction,
                        // configuration.getRmVersion().lastAction,
                        configuration.getRmVersion().sequenceAcknowledgementAction, // configuration.getRmVersion().terminateSequenceAction,
                    // configuration.getRmVersion().terminateSequenceResponseAction,
                    // configuration.getRmVersion().wsrmFaultAction
                    }));

            public Collection<String> getSuportedWsaActions() {
                return SUPPORTED_WSA_ACTIONS;
            }

            public void processProtocolMessage(Packet protocolMessagePacket) {
                if (protocolHandler.containsProtocolMessage(protocolMessagePacket)) {
                    // TODO L10N
                    LOGGER.finer("Processing RM protocol response message.");
                    AcknowledgementData ackData = protocolHandler.getAcknowledgementData(protocolMessagePacket.getMessage());
                    dstMsgHandler.processAcknowledgements(ackData);
                } else {
                    // TODO L10N
                    LOGGER.severe("Unable to process response packet - the packet was not identified as an RM protocol message");
                }
            }
        };
    }

    private void openRmSession(Packet request) {
        createSequences(request);

        ClientAckRequesterTask cart = new ClientAckRequesterTask(rc, outboundSequenceId.value);
        MaintenanceTaskExecutor.INSTANCE.register(cart, cart.getExecutionDelay(), cart.getExecutionDelayTimeUnit());
    }

    private void closeRmSession() {
        try {
            closeSequence();
            waitUntilAllRequestsAckedOrCloseOperationTimeout();
            terminateSequence();
        } finally {
            rc.close();
        }
    }

    private void createSequences(Packet appRequest) throws RxRuntimeException, DuplicateSequenceException {
        final CreateSequenceData.Builder csBuilder = CreateSequenceData.getBuilder(this.rmSourceReference.toSpec());
        csBuilder.strType(rc.communicator.tryStartSecureConversation(appRequest));
        if (rc.configuration.requestResponseOperationsDetected()) {
            csBuilder.offeredInboundSequenceId(rc.sequenceManager().generateSequenceUID());
            // TODO P2 add offered sequence expiration configuration
        }
        final CreateSequenceData requestData = csBuilder.build();
        final Packet request = rc.protocolHandler.toPacket(requestData, null);

        final CreateSequenceResponseData responseData = rc.protocolHandler.toCreateSequenceResponseData(verifyResponse(rc.communicator.send(request), "CreateSequence", Level.SEVERE));

        if (requestData.getOfferedSequenceId() != null) {
            // we offered an inbound sequence
            if (responseData.getAcceptedSequenceAcksTo() == null) {
                throw new RxRuntimeException(LocalizationMessages.WSRM_1116_ACKS_TO_NOT_EQUAL_TO_ENDPOINT_DESTINATION(null, rc.communicator.getDestinationAddress()));
            } else if (!rc.communicator.getDestinationAddress().getURI().toString().equals(new WSEndpointReference(responseData.getAcceptedSequenceAcksTo()).getAddress())) {
                throw new RxRuntimeException(LocalizationMessages.WSRM_1116_ACKS_TO_NOT_EQUAL_TO_ENDPOINT_DESTINATION(responseData.getAcceptedSequenceAcksTo().toString(), rc.communicator.getDestinationAddress()));
            }
        }

        this.outboundSequenceId.value = rc.sequenceManager().createOutboundSequence(
                responseData.getSequenceId(),
                (requestData.getStrType() != null) ? requestData.getStrType().getId() : null,
                (responseData.getDuration() == Sequence.NO_EXPIRY) ? Sequence.NO_EXPIRY : responseData.getDuration() + rc.sequenceManager().currentTimeInMillis()).getId();

        if (requestData.getOfferedSequenceId() != null) {
            Sequence inboundSequence = rc.sequenceManager().createInboundSequence(
                    requestData.getOfferedSequenceId(),
                    (requestData.getStrType() != null) ? requestData.getStrType().getId() : null,
                    (responseData.getDuration() == Sequence.NO_EXPIRY) ? Sequence.NO_EXPIRY : responseData.getDuration() + rc.sequenceManager().currentTimeInMillis());

            rc.sequenceManager().bindSequences(outboundSequenceId.value, inboundSequence.getId());
            rc.sequenceManager().bindSequences(inboundSequence.getId(), outboundSequenceId.value);
        }
    }

    private void closeSequence() {
        CloseSequenceData.Builder dataBuilder = CloseSequenceData.getBuilder(
                outboundSequenceId.value,
                rc.sequenceManager().getSequence(outboundSequenceId.value).getLastMessageNumber());
        dataBuilder.acknowledgementData(rc.sourceMessageHandler.getAcknowledgementData(outboundSequenceId.value));

        final Packet request = rc.protocolHandler.toPacket(dataBuilder.build(), null);
        final CloseSequenceResponseData responseData = rc.protocolHandler.toCloseSequenceResponseData(verifyResponse(rc.communicator.send(request), "CloseSequence", Level.WARNING));

        rc.destinationMessageHandler.processAcknowledgements(responseData.getAcknowledgementData());

        if (!outboundSequenceId.value.equals(responseData.getSequenceId())) {
            LOGGER.warning(LocalizationMessages.WSRM_1119_UNEXPECTED_SEQUENCE_ID_IN_CLOSE_SR(responseData.getSequenceId(), outboundSequenceId));
        }

        String boundSequenceId = rc.getBoundSequenceId(outboundSequenceId.value);
        try {
            rc.sequenceManager().closeSequence(outboundSequenceId.value);
        } finally {
            if (boundSequenceId != null) {
                rc.sequenceManager().closeSequence(boundSequenceId);
            }
        }
    }

    private void terminateSequence() {

        TerminateSequenceData.Builder dataBuilder = TerminateSequenceData.getBuilder(
                outboundSequenceId.value,
                rc.sequenceManager().getSequence(outboundSequenceId.value).getLastMessageNumber());
        dataBuilder.acknowledgementData(rc.sourceMessageHandler.getAcknowledgementData(outboundSequenceId.value));

        final Packet request = rc.protocolHandler.toPacket(dataBuilder.build(), null);
        final Packet response = verifyResponse(rc.communicator.send(request), "TerminateSequence", Level.FINE);

        if (response.getMessage() != null) {
            final String responseAction = rc.communicator.getWsaAction(response);
            if (rc.rmVersion.terminateSequenceAction.equals(responseAction)) {
                TerminateSequenceData responseData = rc.protocolHandler.toTerminateSequenceData(response);

                rc.destinationMessageHandler.processAcknowledgements(responseData.getAcknowledgementData());

                final String boundSequenceId = rc.getBoundSequenceId(outboundSequenceId.value);
                if (!areEqual(boundSequenceId, responseData.getSequenceId())) {
                    LOGGER.warning(LocalizationMessages.WSRM_1117_UNEXPECTED_SEQUENCE_ID_IN_TERMINATE_SR(responseData.getSequenceId(), boundSequenceId));
                }
            } else if (rc.rmVersion.terminateSequenceResponseAction.equals(responseAction)) {
                TerminateSequenceResponseData responseData = rc.protocolHandler.toTerminateSequenceResponseData(response);

                rc.destinationMessageHandler.processAcknowledgements(responseData.getAcknowledgementData());

                if (!outboundSequenceId.value.equals(responseData.getSequenceId())) {
                    LOGGER.warning(LocalizationMessages.WSRM_1117_UNEXPECTED_SEQUENCE_ID_IN_TERMINATE_SR(responseData.getSequenceId(), outboundSequenceId.value));
                }
            }
        }

        // TODO P2 pass last message id into terminateSequence method
        final String boundSequenceId = rc.getBoundSequenceId(outboundSequenceId.value);
        try {
            rc.sequenceManager().terminateSequence(outboundSequenceId.value);
        } finally {
            if (boundSequenceId != null) {
                rc.sequenceManager().terminateSequence(boundSequenceId);
            }
        }
    }

    private boolean areEqual(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    private void waitUntilAllRequestsAckedOrCloseOperationTimeout() {
        final CountDownLatch doneSignal = new CountDownLatch(1);
        ScheduledFuture<?> taskHandle = rc.scheduledTaskManager.startTask(new Runnable() {

            public void run() {
                try {
                    if (!rc.sequenceManager().getSequence(outboundSequenceId.value).hasUnacknowledgedMessages()) {
                        doneSignal.countDown();
                    } 
                } catch (UnknownSequenceException ex) {
                    LOGGER.severe(LocalizationMessages.WSRM_1111_UNEXPECTED_EXCEPTION_WHILE_WAITING_FOR_SEQ_ACKS(), ex);
                    doneSignal.countDown();
                }
            }
        }, 10, 10);

        try {
            if (rc.configuration.getCloseSequenceOperationTimeout() > 0) {
                boolean waitResult = doneSignal.await(rc.configuration.getCloseSequenceOperationTimeout(), TimeUnit.MILLISECONDS);
                if (!waitResult) {
                    LOGGER.info(LocalizationMessages.WSRM_1112_CLOSE_OUTBOUND_SEQUENCE_TIMED_OUT(outboundSequenceId));
                }
            } else {
                doneSignal.await();
            }
        } catch (InterruptedException ex) {
            LOGGER.fine(LocalizationMessages.WSRM_1113_CLOSE_OUTBOUND_SEQUENCE_INTERRUPTED(outboundSequenceId), ex);
        } finally {
            taskHandle.cancel(true);
        }
    }

    private Packet verifyResponse(final Packet response, final String requestId, Level logLevel) throws RxRuntimeException {
        if (response == null || response.getMessage() == null) {
            final String logMessage = LocalizationMessages.WSRM_1114_NULL_RESPONSE_ON_PROTOCOL_MESSAGE_REQUEST(requestId);
            if (logLevel == Level.SEVERE) {
                throw LOGGER.logSevereException(new RxRuntimeException(logMessage));
            } else {
                LOGGER.log(logLevel, logMessage);
            }
        } else if (response.getMessage().isFault()) {
            final String logMessage = LocalizationMessages.WSRM_1115_PROTOCOL_MESSAGE_REQUEST_REFUSED(requestId);
            // FIXME P2 pass fault data into exception
            if (logLevel == Level.SEVERE) {
                throw LOGGER.logSevereException(new RxRuntimeException(logMessage));
            } else {
                LOGGER.log(logLevel, logMessage);
            }
        }

        return response;
    }
}
