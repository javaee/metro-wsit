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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.rx.util.TimestampedCollection;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class RequestResendTask implements Runnable {

    private static final class RequestResendCallbackHandler implements Fiber.CompletionCallback {
        private final Packet requestPacketCopy;
        private final ClientSession session;
        private final int nextResendAttemptNumber;

        public RequestResendCallbackHandler(ClientSession session, Packet requestCopy, int nextResendAttemptNumber) {
            this.requestPacketCopy = requestCopy;
            this.session = session;
            this.nextResendAttemptNumber = nextResendAttemptNumber;
        }

        public void onCompletion(Packet response) {
            if (!session.isRequestAcknowledged(requestPacketCopy)) {
                session.registerForResend(requestPacketCopy, nextResendAttemptNumber); // don't need to do another request packet copy
            }
        }

        public void onCompletion(Throwable error) {
            if (RmClientTube.isResendPossible(error) && !session.isRequestAcknowledged(requestPacketCopy)) {
                    session.registerForResend(requestPacketCopy, nextResendAttemptNumber); // don't need to do another request packet copy
            } else {
                PacketAdapter request = PacketAdapter.getInstance(session.configuration, requestPacketCopy);
                // TODO L10N
                throw LOGGER.logSevereException(new RxRuntimeException(
                        String.format("Resend of a one-way message with message id [ %d ] on the sequence [ %s ] failed with an exception", request.getMessageNumber(), request.getSequenceId()),
                        error));
            }
        }
    }

    private static final class RequestRegistration {
        final Packet request;
        final int nextResendAttemptNumber;

        public RequestRegistration(Packet request, int nextResendAttemptNumber) {
            this.request = request;
            this.nextResendAttemptNumber = nextResendAttemptNumber;
        }
    }

    //
    private static final Logger LOGGER = Logger.getLogger(RequestResendTask.class);
    //
    private final TimestampedCollection<Object, RequestRegistration> scheduledPackets = new TimestampedCollection<Object, RequestRegistration>();
    private final Communicator communicator;
    private final ClientSession session;

    RequestResendTask(@NotNull Communicator communicator, @NotNull ClientSession session) {
        this.communicator = communicator;
        this.session = session;
    }

    public void run() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(String.format("Periodic request resend task executed - suspended queue size: [ %d ]", scheduledPackets.size()));
        }
        while (!scheduledPackets.isEmpty() && expired(scheduledPackets.getOldestRegistrationTimestamp())) {
            final RequestRegistration requestRegistration = scheduledPackets.removeOldest();
            PacketAdapter request = PacketAdapter.getInstance(session.configuration, requestRegistration.request);

            if (!session.isRequestAcknowledged(request)) { // request not acknowledged, need to resend
                Packet requestPacketCopy = request.getPacket().copy(true);

                communicator.sendAsync(
                        session.appendOutgoingAcknowledgementHeaders(request.getPacket()), // appending latest RM headers
                        new RequestResendCallbackHandler(session, requestPacketCopy, requestRegistration.nextResendAttemptNumber + 1));
                
                if (LOGGER.isLoggable(Level.FINER)) {
                    // TODO L10N
                    // LOGGER.fine(LocalizationMessages.WSRM_1102_RESENDING_DROPPED_MESSAGE());
                    LOGGER.finer(String.format(
                            "Resending request packet with message id [ %d ] on the sequence [ %s ]",
                            request.getMessageNumber(),
                            request.getSequenceId()));
                }
            } else { // request has already been acknowledged, no need to resend
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format(
                            "Request packet with message id [ %d ] on the sequence [ %s ] already acknowledged - resend cancelled.",
                            request.getMessageNumber(),
                            request.getSequenceId()));
                }
            }
        }
    }

    private final boolean expired(long resumeTime) {
        return System.currentTimeMillis() >= resumeTime;
    }

    /**
     * Registers data for a timed execution
     *
     * @param request a packet to be resent at a given {@code executionTime}.
     * @param resendCounter number of the resend attempt for a given packet
     * @param executionTime determines the time of execution for a given {@code packet}
     * 
     * @return {@code true} if the {@code request} has been successfully registered, {@code false} otherwise.
     */
    final boolean register(@NotNull Packet request, int resendCounter, long executionTime) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("A packet has been scheduled for a resend:%n%s", request));
        }
        return scheduledPackets.register(executionTime, new RequestRegistration(request, resendCounter));
    }
}
