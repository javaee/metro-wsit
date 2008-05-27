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

import com.sun.xml.ws.rm.runtime.sequence.SequenceManagerFactory;
import com.sun.xml.ws.rm.runtime.sequence.UnknownSequenceException;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.rm.CreateSequenceException;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
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

/**
 * <p>
 * RM session represents a contract between single WS proxy and it's corresponding service. Multiple tubelines (of the same
 * WS proxy) may share a single RM session, each WS proxy however creates it's own session.
 * </p>
 * <p>
 * RM session performs all tasks related to RM message processing, while being focused on a single reliable connection.
 * </p>
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
abstract class ClientSession {

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

    static ClientSession create(Configuration configuration, ProtocolCommunicator communicator) {
        switch (configuration.getRmVersion()) {
            case WSRM10:
                return new Rm10ClientSession(configuration, communicator);
            case WSRM11:
                return new Rm11ClientSession(configuration, communicator);
            default:
                throw new IllegalStateException(LocalizationMessages.WSRM_1104_RM_VERSION_NOT_SUPPORTED(configuration.getRmVersion().namespaceUri));
        }
    }
    private static final RmLogger LOGGER = RmLogger.getLogger(ClientSession.class);
    private static final int MAX_INITIATE_SESSION_ATTEMPTS = 3;
    protected String inboundSequenceId;
    protected String outboundSequenceId;
    protected final SequenceManager sequenceManager;
    protected final ProtocolCommunicator communicator;
    protected final Configuration configuration;
    private final Lock initLock;
    private final ScheduledTaskManager scheduledTaskManager;
    private final Queue<FiberRegistration> fibersToResend = new LinkedList<FiberRegistration>();
    private final AtomicLong lastAckRequestedTime = new AtomicLong(0);

    protected ClientSession(Configuration configuration, ProtocolCommunicator communicator) {
        this.inboundSequenceId = null;
        this.outboundSequenceId = null;
        this.initLock = new ReentrantLock();
        this.configuration = configuration;
        this.sequenceManager = SequenceManagerFactory.getInstance().getSequenceManager();
        this.communicator = communicator;
        this.scheduledTaskManager = new ScheduledTaskManager();
    }

    protected abstract void openRmSession(String offerInboundSequenceId, SecurityTokenReferenceType strType) throws RmException;

    protected abstract void closeOutboundSequence() throws RmException;

    protected abstract void terminateOutboundSequence() throws RmException;

    protected final void processInboundMessageHeaders(PacketAdapter responseAdapter, boolean expectSequenceHeader) throws RmException {
        if (expectSequenceHeader) {
            String sequenceId = responseAdapter.getSequenceId();
            if (sequenceId != null) {
                assertSequenceId(inboundSequenceId, sequenceId);
                sequenceManager.getSequence(sequenceId).acknowledgeMessageId(responseAdapter.getMessageNumber());
            } else {
                throw new RmException(LocalizationMessages.WSRM_1118_MANDATORY_HEADER_NOT_PRESENT("wsrm:Sequence"));
            }
        }

        String ackRequestedSequenceId = responseAdapter.getAckRequestedHeaderSequenceId();
        if (ackRequestedSequenceId != null) {
            assertSequenceId(inboundSequenceId, ackRequestedSequenceId);
            sequenceManager.getSequence(ackRequestedSequenceId).setAckRequestedFlag();
        }

        responseAdapter.processAcknowledgements(sequenceManager, outboundSequenceId);
    }

    /**
     * TODO javadoc
     */
    protected final void assertSequenceId(String expected, String actual) {
        if (expected != null && !expected.equals(actual)) {
            throw LOGGER.logSevereException(new IllegalStateException(LocalizationMessages.WSRM_1105_INBOUND_SEQUENCE_ID_NOT_RECOGNIZED(actual, expected)));
        }
    }

    protected final void requestAcknowledgement() throws RmException {
        PacketAdapter responseAdapter = PacketAdapter.create(configuration);
        try {

            PacketAdapter requestAdapter = PacketAdapter.create(configuration, communicator.createEmptyPacket());
            requestAdapter.setEmptyRequestMessage(configuration.getRmVersion().ackRequestedAction).appendAckRequestedHeader(outboundSequenceId);

            responseAdapter.attach(communicator.send(requestAdapter.detach()));
            if (!responseAdapter.containsMessage()) {
                throw new RmException(LocalizationMessages.WSRM_1108_NULL_RESPONSE_FOR_ACK_REQUEST());
            }

            processInboundMessageHeaders(responseAdapter, false);

            if (responseAdapter.isFault()) {
                // FIXME: refactor the exception creation - we should not pass the SOAP fault directly into the exception
                throw new RmException(LocalizationMessages.WSRM_1109_SOAP_FAULT_RESPONSE_FOR_ACK_REQUEST(), responseAdapter.message);
            }
        } finally {
            responseAdapter.consumeAndDetach();
        }
    }

    final Packet processOutgoingPacket(Packet requestPacket) throws RmException {
        PacketAdapter requestAdapter = PacketAdapter.create(configuration, requestPacket);
        initializeIfNecessary(requestAdapter);

        requestAdapter.appendSequenceHeader(
                outboundSequenceId,
                sequenceManager.getSequence(outboundSequenceId).getNextMessageId());
        if (checkPendingAckRequest()) {
            requestAdapter.appendAckRequestedHeader(outboundSequenceId);
            lastAckRequestedTime.set(System.currentTimeMillis());
        }
        if (inboundSequenceId != null) {
            requestAdapter.appendSequenceAcknowledgementHeader(
                    inboundSequenceId,
                    sequenceManager.getSequence(inboundSequenceId).getAcknowledgedMessageIds());
        }

        return requestAdapter.detach();
    }

    final Packet processIncommingPacket(Packet responsePacket, boolean responseToOneWayRequest) throws RmException {
        PacketAdapter responseAdapter = PacketAdapter.create(configuration, responsePacket);
        if (responseAdapter.containsMessage()) {
            processInboundMessageHeaders(responseAdapter, !responseToOneWayRequest && !responseAdapter.isProtocolMessage());
        }

        return responseAdapter.detach();
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
            } catch (RmException ex) {
                LOGGER.logException(ex, Level.WARNING);
            } finally {
                try {
                    sequenceManager.closeSequence(outboundSequenceId);
                } catch (UnknownSequenceException ex) {
                    LOGGER.logException(ex, Level.WARNING);
                }
            }

            try {
                waitUntilAllRequestsAckedOrTimeout();
                terminateOutboundSequence();
            } catch (RmException ex) {
                LOGGER.logException(ex, Level.WARNING);
            } finally {
                try {
                    sequenceManager.terminateSequence(outboundSequenceId);
                } catch (UnknownSequenceException ex) {
                    LOGGER.logException(ex, Level.WARNING);
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
    private void initializeIfNecessary(PacketAdapter request) throws CreateSequenceException, RmException {
        initLock.lock();
        try {
            if (!isInitialized()) {
                communicator.registerMusterRequestPacket(request.copyPacket(false));

                int numberOfInitiateSessionAttempts = 0;
                while (true) {
                    try {
                        openRmSession(
                                (configuration.requestResponseOperationsDetected()) ? sequenceManager.generateSequenceUID() : null,
                                communicator.tryStartSecureConversation());
                        break;
                    } catch (RuntimeException ex) {
                        LOGGER.warning(LocalizationMessages.WSRM_1106_RM_SESSION_INIT_ATTEMPT_FAILED(), ex);
                    } finally {
                        if (++numberOfInitiateSessionAttempts > MAX_INITIATE_SESSION_ATTEMPTS) {
                            throw LOGGER.logSevereException(new CreateSequenceException(LocalizationMessages.WSRM_1107_MAX_RM_SESSION_INIT_ATTEMPTS_REACHED()));
                        }
                    }
                }


                scheduledTaskManager.startTasks(createResendTask(), createAckRequesterTask());
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
    private Runnable createResendTask() {
        return new Runnable() {

            public void run() {
                while (!fibersToResend.isEmpty() && fibersToResend.peek().expired(configuration.getMessageRetransmissionInterval())) {
                    FiberRegistration registration;
                    synchronized (fibersToResend) {
                        registration = fibersToResend.poll();
                    }
                    registration.fiber.resume(registration.packet);
                }
            }
        };
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
                    if (checkPendingAckRequest()) {
                        requestAcknowledgement();
                        lastAckRequestedTime.set(System.currentTimeMillis());
                    }
                } catch (RmException ex) {
                    LOGGER.warning(LocalizationMessages.WSRM_1110_ACK_REQUEST_FAILED(), ex);
                }
            }
        };
    }

    private boolean checkPendingAckRequest() throws UnknownSequenceException {
        return lastAckRequestedTime.get() - System.currentTimeMillis() > configuration.getAcknowledgementRequestInterval() &&
                sequenceManager.getSequence(outboundSequenceId).hasPendingAcknowledgements();
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
