/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.commons.DelayedTaskManager;
import com.sun.xml.ws.commons.DelayedTaskManager.DelayedTask;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class ClientAckRequesterTask implements DelayedTask {

    private static final Logger LOGGER = Logger.getLogger(ClientAckRequesterTask.class);
    //
    private final RuntimeContext rc;
    private final String outboundSequenceId;
    private final long acknowledgementRequestInterval;

    public ClientAckRequesterTask(RuntimeContext rc, String outboundSequenceId) {
        this.rc = rc;
        this.acknowledgementRequestInterval = rc.configuration.getRmFeature().getAckRequestTransmissionInterval();
        this.outboundSequenceId = outboundSequenceId;
    }

    public void run(DelayedTaskManager manager) {
        LOGGER.entering(outboundSequenceId);
        try {
            if (rc.communicator.isClosed()) {
                // Our communication channel has been closed - let the task die
                return;
            }

            if (rc.sequenceManager().isValid(outboundSequenceId)) {
                final Sequence sequence = rc.sequenceManager().getOutboundSequence(outboundSequenceId);
                if (!sequence.isClosed() && !sequence.isExpired()) {
                    try {
                        if (sequence.isStandaloneAcknowledgementRequestSchedulable(acknowledgementRequestInterval)) {
                            requestAcknowledgement();
                            sequence.updateLastAcknowledgementRequestTime();
                        }
                    } finally {
                        LOGGER.finer(String.format("Scheduling next run for an outbound sequence with id [ %s ]", outboundSequenceId));
                        manager.register(this, getExecutionDelay(), getExecutionDelayTimeUnit());
                    }
                }
            }
            // else sequence is no longer valid / ready to accept acknowledgements - let the task die
        } finally {
            LOGGER.exiting(outboundSequenceId);
        }
    }

    private void requestAcknowledgement() {
        Packet request = rc.communicator.createEmptyRequestPacket(rc.rmVersion.protocolVersion.ackRequestedAction, true);
        request.setIsProtocolMessage();
        if (rc.getUserStateID() != null) {
            request.setUserStateId(rc.getUserStateID());
        }

        JaxwsApplicationMessage requestMessage = new JaxwsApplicationMessage(
                request,
                request.getMessage().getID(rc.addressingVersion, rc.soapVersion));

        // setting sequence id and fake message number so source message handler can attach a proper sequence acknowledgement info
        requestMessage.setSequenceData(outboundSequenceId, 0);

        rc.sourceMessageHandler.attachAcknowledgementInfo(requestMessage);
        rc.protocolHandler.appendAcknowledgementHeaders(requestMessage.getPacket(), requestMessage.getAcknowledgementData());

        rc.communicator.sendAsync(request, new Fiber.CompletionCallback() {

            public void onCompletion(Packet response) {
                if (response == null || response.getMessage() == null) {
                    LOGGER.warning(LocalizationMessages.WSRM_1108_NULL_RESPONSE_FOR_ACK_REQUEST());
                    return;
                }

                try {
                    if (rc.protocolHandler.containsProtocolMessage(response)) {
                        LOGGER.finer("Processing RM protocol response message.");
                        JaxwsApplicationMessage message = new JaxwsApplicationMessage(response, "");
                        rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());

                        rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());

                        rc.outboundDeliveredHandler.processAcknowledgements(message.getAcknowledgementData());
                    } else {
                        LOGGER.severe(LocalizationMessages.WSRM_1120_RESPONSE_NOT_IDENTIFIED_AS_PROTOCOL_MESSAGE());
                    }

                    if (response.getMessage().isFault()) {
                        LOGGER.warning(LocalizationMessages.WSRM_1109_SOAP_FAULT_RESPONSE_FOR_ACK_REQUEST());
                    }
                } finally {
                    response.getMessage().consume();
                }
            }

            public void onCompletion(Throwable error) {
                LOGGER.warning(LocalizationMessages.WSRM_1127_UNEXPECTED_EXCEPTION_WHEN_SENDING_ACK_REQUEST(), error);
            }
        });
    }

    public long getExecutionDelay() {
        return acknowledgementRequestInterval;
    }

    public TimeUnit getExecutionDelayTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    public String getName() {
        return "client acknowledgement requester task";
    }
}
