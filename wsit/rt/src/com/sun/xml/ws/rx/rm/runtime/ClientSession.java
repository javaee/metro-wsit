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

import com.sun.xml.ws.rx.mc.runtime.spi.ProtocolMessageHandler;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.xml.ws.rx.RxException;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.util.ScheduledTaskManager;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManagerFactory;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * <p>
 * RM client session represents a contract between single WS proxy and it's corresponding service. Multiple tubelines (of the same
 * WS proxy) may share a single RM session, each WS proxy however creates it's own client session.
 * </p>
 * <p>
 * RM client session performs all tasks related to RM message processing, while being focused on a single reliable connection.
 * </p>
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
abstract class ClientSession {

    private static final Logger LOGGER = Logger.getLogger(ClientSession.class);
    private static final int MAX_INITIATE_SESSION_ATTEMPTS = 3;
    //
    String inboundSequenceId = null;
    String outboundSequenceId = null;
    final RxConfiguration configuration;
    final SequenceManager sequenceManager;
    final Communicator communicator;

    final WSEndpointReference rmSourceReference;
    //
    private final Lock initLock;
    private final ScheduledTaskManager scheduledTaskManager;
    private final AtomicLong lastAckRequestedTime = new AtomicLong(0);
    private final FiberResumeTask fiberResumeTask; // resumes Req/Resp resend processing
    private final RequestResendTask requestResendTask; // resumes On-Way request processing
    //

    static ClientSession create(RxConfiguration configuration, WSEndpointReference rmSourceReference, Communicator communicator) {
        switch (configuration.getRmVersion()) {
            case WSRM200502:
                return new Rm10ClientSession(configuration, rmSourceReference, communicator);
            case WSRM200702:
                return new Rm11ClientSession(configuration, rmSourceReference, communicator);
            default:
                throw new IllegalStateException(LocalizationMessages.WSRM_1104_RM_VERSION_NOT_SUPPORTED(configuration.getRmVersion().namespaceUri));
        }
    }

    ClientSession(RxConfiguration configuration, WSEndpointReference rmsEndpointReference, Communicator communicator) {
        this.initLock = new ReentrantLock();
        this.configuration = configuration;
        this.rmSourceReference = rmsEndpointReference;
        this.sequenceManager = SequenceManagerFactory.INSTANCE.getClientSequenceManager();
        this.communicator = communicator;
        this.scheduledTaskManager = new ScheduledTaskManager();
        this.fiberResumeTask = new FiberResumeTask(this);
        this.requestResendTask = new RequestResendTask(communicator, this);
    }

    abstract void openRmSession(String offerInboundSequenceId, SecurityTokenReferenceType strType) throws RxRuntimeException;

    abstract void closeOutboundSequence() throws RxException;

    abstract void terminateOutboundSequence() throws RxException;

    final void processInboundMessageHeaders(PacketAdapter responseAdapter, boolean expectSequenceHeader) throws RxRuntimeException {
        if (expectSequenceHeader) {
            String sequenceId = responseAdapter.getSequenceId();
            if (sequenceId != null) {
                Utilities.assertSequenceId(inboundSequenceId, sequenceId);
                sequenceManager.getSequence(sequenceId).acknowledgeMessageId(responseAdapter.getMessageNumber());
            } else {
                throw new RxRuntimeException(LocalizationMessages.WSRM_1118_MANDATORY_HEADER_NOT_PRESENT("wsrm:Sequence"));
            }
        }

        String ackRequestedSequenceId = responseAdapter.getAckRequestedHeaderSequenceId();
        if (ackRequestedSequenceId != null) {
            Utilities.assertSequenceId(inboundSequenceId, ackRequestedSequenceId);
            sequenceManager.getSequence(ackRequestedSequenceId).setAckRequestedFlag();
        }

        responseAdapter.processAcknowledgements(sequenceManager, outboundSequenceId);
    }

    final void requestAcknowledgement() throws RxException {
        PacketAdapter responseAdapter = null;
        try {
            PacketAdapter requestAdapter = PacketAdapter.getInstance(configuration, communicator.createEmptyRequestPacket(false));
            requestAdapter.setEmptyRequestMessage(configuration.getRmVersion().ackRequestedAction).appendAckRequestedHeader(outboundSequenceId);

            responseAdapter = PacketAdapter.getInstance(configuration, communicator.send(requestAdapter.getPacket()));
            if (!responseAdapter.containsMessage()) {
                throw new RxException(LocalizationMessages.WSRM_1108_NULL_RESPONSE_FOR_ACK_REQUEST());
            }

            processInboundMessageHeaders(responseAdapter, false);

            if (responseAdapter.isFault()) {
                // FIXME: refactor the exception creation - we should somehow pass the SOAP fault information into the exception
                throw new RxException(LocalizationMessages.WSRM_1109_SOAP_FAULT_RESPONSE_FOR_ACK_REQUEST());
            }
        } finally {
            if (responseAdapter != null) {
                responseAdapter.consume();
            }
        }
    }

    /**
     * Initalizes this client session if necessary and assigns Sequence and MessageId
     * RM headers to the outgoing request.
     */
    final Packet registerOutgoingRequest(Packet requestPacket) {
        PacketAdapter requestAdapter = PacketAdapter.getInstance(configuration, requestPacket);
        initializeIfNecessary(requestAdapter);

        requestAdapter.appendSequenceHeader(
                outboundSequenceId,
                sequenceManager.getSequence(outboundSequenceId).generateNextMessageId());

        return requestAdapter.getPacket();
    }

    /**
     * Appends AcksRequested and/or SequenceAcknowledgement headers to the outgoing request
     */
    final Packet appendOutgoingAcknowledgementHeaders(Packet requestPacket) throws RxRuntimeException {
        PacketAdapter requestAdapter = PacketAdapter.getInstance(configuration, requestPacket);
        if (sequenceManager.getSequence(outboundSequenceId).hasPendingAcknowledgements()) {
            requestAdapter.appendAckRequestedHeader(outboundSequenceId);
            requestAdapter.setAckRequestdFlag();
            lastAckRequestedTime.set(System.currentTimeMillis());
        }
        if (inboundSequenceId != null) {
            Sequence inboundSequence = sequenceManager.getSequence(inboundSequenceId);
            if (inboundSequence.getLastMessageId() > 0) {
                // FIXME: create an API method to test this
                requestAdapter.appendSequenceAcknowledgementHeader(sequenceManager.getSequence(inboundSequenceId));
            }
        }

        return requestAdapter.getPacket();
    }

    final ProtocolMessageHandler getRmProtocolResponseHandler() {
        return new ProtocolMessageHandler() {
            Collection<String> SUPPORTED_WSA_ACTIONS = Collections.unmodifiableCollection(Arrays.asList(new String[] {
                    configuration.getRmVersion().ackRequestedAction,
                    // configuration.getRmVersion().closeSequenceAction,
                    // configuration.getRmVersion().closeSequenceResponseAction,
                    // configuration.getRmVersion().createSequenceAction,
                    // configuration.getRmVersion().createSequenceResponseAction,
                    // configuration.getRmVersion().lastAction,
                    configuration.getRmVersion().sequenceAcknowledgementAction,
                    // configuration.getRmVersion().terminateSequenceAction,
                    // configuration.getRmVersion().terminateSequenceResponseAction,
                    // configuration.getRmVersion().wsrmFaultAction
                }));

            public Collection<String> getSuportedWsaActions() {
                return SUPPORTED_WSA_ACTIONS;
            }

            public void processProtocolMessage(Packet protocolMessagePacket) {
                PacketAdapter responseAdapter = PacketAdapter.getInstance(configuration, protocolMessagePacket);
                if (responseAdapter.isProtocolMessage()) {
                    // TODO L10N
                    LOGGER.finer("Processing RM protocol message.");
                    processInboundMessageHeaders(responseAdapter, false);
                } else {
                    // TODO L10N
                    LOGGER.severe("Unable to process packet - the packet was not identified as an RM protocol message");
                }
            }
        };
    }


    final Packet processIncommingPacket(Packet responsePacket, boolean responseToOneWayRequest) throws RxRuntimeException {
        PacketAdapter responseAdapter = PacketAdapter.getInstance(configuration, responsePacket);
        if (responseAdapter.containsMessage()) {
            processInboundMessageHeaders(responseAdapter, !responseToOneWayRequest && !responseAdapter.isProtocolMessage());
        }

        return responseAdapter.getPacket();
    }

    /**
     * Registers given fiber for a resume in an attempt to resend a request which
     * is part of a request-response MEP.
     * 
     * @param fiber a fiber to be resumed after resend interval has passed
     * @param request request packet to be passed into the resumed fiber
     * @param resendCounter number of the resend attempt for a given packet
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    final boolean registerForResend(Fiber fiber, Packet request, int resendCounter) {
        return fiberResumeTask.register(
                fiber,
                request,
                configuration.getRetransmissionBackoffAlgorithm().nextResendTime(resendCounter, configuration.getMessageRetransmissionInterval()));
    }

    /**
     * Registers given one-way packet for a resend
     *
     * @param packet packet to be resent
     * @param resendCounter number of the resend attempt for a given packet
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    final boolean registerForResend(Packet packet, int resendCounter) {
        return requestResendTask.register(
                packet,
                resendCounter,
                configuration.getRetransmissionBackoffAlgorithm().nextResendTime(resendCounter, configuration.getMessageRetransmissionInterval()));
    }

    final boolean isRequestAcknowledged(Packet request) {
        return isRequestAcknowledged(PacketAdapter.getInstance(configuration, request));
    }

    final boolean isRequestAcknowledged(PacketAdapter request) {
        return sequenceManager.getSequence(outboundSequenceId).isAcknowledged(request.getMessageNumber());
    }

    /**
     * Closes and terminates associated sequences and releases other resources associated with this RM session
     */
    final void close() {
        // TODO: make sure we have all the acknowledgements and no requests pending a resend before closing
        try {
            if (outboundSequenceId != null && sequenceManager.isValid(outboundSequenceId)) {
                try {
                    closeOutboundSequence();
                } catch (RxException ex) {
                    LOGGER.logException(ex, Level.WARNING);
                } finally {
                    try {
                        sequenceManager.closeSequence(outboundSequenceId);
                    } catch (UnknownSequenceException ex) {
                        LOGGER.logException(ex, Level.WARNING);
                    }
                }

                try {
                    waitUntilAllRequestsAckedOrCloseOperationTimeout();
                    terminateOutboundSequence();
                } catch (RxException ex) {
                    LOGGER.logException(ex, Level.WARNING);
                } finally {
                    try {
                        sequenceManager.terminateSequence(outboundSequenceId);
                    } catch (UnknownSequenceException ex) {
                        LOGGER.logException(ex, Level.WARNING);
                    }
                }
            }

            if (inboundSequenceId != null && sequenceManager.isValid(inboundSequenceId)) {
                try {
                    if (!sequenceManager.getSequence(inboundSequenceId).isClosed()) {
                        sequenceManager.closeSequence(inboundSequenceId);
                    }
                    // TODO wait for an external event?
                    sequenceManager.terminateSequence(inboundSequenceId);
                } catch (UnknownSequenceException ex) {
                    LOGGER.logException(ex, Level.WARNING);
                }
            }
        } finally {
            scheduledTaskManager.stopAll();
        }
    }

    /**
     * Performs late initialization of sequences and timer task, provided those have not yet been initialized.
     * The actual initialization thus happens only once in the lifetime of each client RM session object.
     */
    private void initializeIfNecessary(PacketAdapter request) throws RxRuntimeException {
        initLock.lock();
        try {
            if (!isInitialized()) {
                int numberOfInitiateSessionAttempts = 0;
                while (true) {
                    try {
                        openRmSession(
                                (configuration.requestResponseOperationsDetected()) ? sequenceManager.generateSequenceUID() : null,
                                communicator.tryStartSecureConversation());
                        break;
                    } catch (Exception ex) {
                        LOGGER.warning(LocalizationMessages.WSRM_1106_RM_SESSION_INIT_ATTEMPT_FAILED(), ex);
                    } finally {
                        if (++numberOfInitiateSessionAttempts > MAX_INITIATE_SESSION_ATTEMPTS) {
                            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1107_MAX_RM_SESSION_INIT_ATTEMPTS_REACHED()));
                        }
                    }
                }

                scheduledTaskManager.startTask(fiberResumeTask, configuration.getMessageRetransmissionInterval(), configuration.getMessageRetransmissionInterval());
                scheduledTaskManager.startTask(requestResendTask, configuration.getMessageRetransmissionInterval(), configuration.getMessageRetransmissionInterval());
                scheduledTaskManager.startTask(createAckRequesterTask(), configuration.getAcknowledgementRequestInterval(), configuration.getAcknowledgementRequestInterval());
            }
        } finally {
            initLock.unlock();
        }
    }

    private boolean isInitialized() {
        return outboundSequenceId != null;
    }

    /**
     * Send Message with empty body and a AckRequestedElement (with Last child) down the pipe. Process the response,
     * which may contain a SequenceAcknowledgementElement.
     *
     * @param seq Outbound sequence to which SequenceHeaderElement will belong.
     *
     */
    private Runnable createAckRequesterTask() {
        return new Runnable() {

            public void run() {
                try {
                    if (isAutomaticAckRequestPending()) {
                        requestAcknowledgement();
                        lastAckRequestedTime.set(System.currentTimeMillis());
                    }
                } catch (RxException ex) {
                    LOGGER.warning(LocalizationMessages.WSRM_1110_ACK_REQUEST_FAILED(), ex);
                }
            }
        };
    }

    private boolean isAutomaticAckRequestPending() throws RxRuntimeException {
        return lastAckRequestedTime.get() - System.currentTimeMillis() > configuration.getAcknowledgementRequestInterval() &&
                sequenceManager.getSequence(outboundSequenceId).hasPendingAcknowledgements();
    }

    private void waitUntilAllRequestsAckedOrCloseOperationTimeout() {
        final CountDownLatch doneSignal = new CountDownLatch(1);
        ScheduledFuture<?> taskHandle = scheduledTaskManager.startTask(new Runnable() {

            public void run() {
                try {
                    if (!sequenceManager.getSequence(outboundSequenceId).hasPendingAcknowledgements()) {
                        doneSignal.countDown();
                    }
                } catch (UnknownSequenceException ex) {
                    LOGGER.severe(LocalizationMessages.WSRM_1111_UNEXPECTED_EXCEPTION_WHILE_WAITING_FOR_SEQ_ACKS(), ex);
                    doneSignal.countDown();
                }
            }
        });
        try {
            if (configuration.getCloseSequenceOperationTimeout() > 0) {
                boolean waitResult = doneSignal.await(configuration.getCloseSequenceOperationTimeout(), TimeUnit.MILLISECONDS);
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
}
