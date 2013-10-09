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

import com.oracle.webservices.oracle_internal_api.rm.OutboundDelivered;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.rx.RxException;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.delivery.Postman;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.util.AbstractResponseHandler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class ClientSourceDeliveryCallback implements Postman.Callback {
    private static final Logger LOGGER = Logger.getLogger(ClientSourceDeliveryCallback.class);

    private static class ResponseCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {
        private final JaxwsApplicationMessage request;
        private final RuntimeContext rc;

        public ResponseCallbackHandler(JaxwsApplicationMessage request, RuntimeContext rc) {
            super(rc.suspendedFiberStorage, request.getCorrelationId());
            this.request = request;
            this.rc = rc;
        }

        public void onCompletion(Packet response) {
            try {
                HaContext.initFrom(response);

                if (response.getMessage() != null) {
                    JaxwsApplicationMessage message = new JaxwsApplicationMessage(response, getCorrelationId());
                    rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
                    rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());

                    rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());

                    invokeOutboundDeliveredTrueIfRequestAcked();

                    if (rc.configuration.getRuntimeVersion().protocolVersion.isFault(message.getWsaAction())) {
                        // TODO handle RM faults
                        // TODO i18n
                        LOGGER.severe("Received WS-RM fault response: "+message.getWsaAction());
                    }

                    if (message.getSequenceId() != null) { //two-way
                        rc.destinationMessageHandler.registerMessage(message);
                        rc.destinationMessageHandler.putToDeliveryQueue(message); //resuming parent fiber there
                    } else { //one-way response likely with empty soap body but with ack header
                        resumeParentFiber(response);
                    }
                } else { //maybe HTTP 202 in response to a one-way request
                    final int nextResendCount = request.getNextResendCount();
                    if (!rc.configuration.getRmFeature().canRetransmitMessage(nextResendCount)) {
                        invokeOutboundDeliveredFalse();
                        resumeParentFiber(new RxRuntimeException((LocalizationMessages.WSRM_1159_MAX_MESSAGE_RESEND_ATTEMPTS_REACHED())));
                        return;
                    }

                    RedeliveryTaskExecutor.deliverUsingCurrentThread(
                            request,
                            rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(nextResendCount, rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                            TimeUnit.MILLISECONDS,
                            rc.sourceMessageHandler);
                }
            } catch (RxRuntimeException ex) {
                onCompletion(ex);
            } catch (RxException ex) {
                onCompletion(ex);
            } finally {
                HaContext.clear();
            }
        }

        public void onCompletion(Throwable error) {
            if (Utilities.isResendPossible(error)) {
                final int nextResendCount = request.getNextResendCount();
                if (!rc.configuration.getRmFeature().canRetransmitMessage(nextResendCount)) {
                    invokeOutboundDeliveredFalse();
                    resumeParentFiber(new RxRuntimeException((LocalizationMessages.WSRM_1159_MAX_MESSAGE_RESEND_ATTEMPTS_REACHED())));
                    return;
                }

                try {
                    HaContext.initFrom(request.getPacket());

                    RedeliveryTaskExecutor.deliverUsingCurrentThread(
                            request,
                            rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(nextResendCount, rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                            TimeUnit.MILLISECONDS,
                            rc.sourceMessageHandler);
                } finally {
                    HaContext.clear();
                }
            } else {
                invokeOutboundDeliveredFalse();
                resumeParentFiber(error);
            }
        }

        private void invokeOutboundDeliveredTrueIfRequestAcked() {
            Packet requestPacket = request.getPacket();
            OutboundDelivered outboundDelivered = requestPacket.getSatellite(OutboundDelivered.class);

            if (outboundDelivered != null) {
                long messageNumber = request.getMessageNumber();
                Sequence outboundSequence = rc.sequenceManager().getOutboundSequence(request.getSequenceId());
                boolean isRequestAcked = outboundSequence.isAcknowledged(messageNumber);
                if (isRequestAcked) {
                    //TODO change it to FINE later
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Invoking outboundDelivered.setDelivered(true) for " +
                                "seq id:"+outboundSequence.getId()+" and " +
                                "message number:"+messageNumber);
                    }
                    outboundDelivered.setDelivered(Boolean.TRUE);
                }
            }
        }

        private void invokeOutboundDeliveredFalse() {
            Packet requestPacket = request.getPacket();
            OutboundDelivered outboundDelivered = requestPacket.getSatellite(OutboundDelivered.class);
            if (outboundDelivered != null) {
                //TODO change it to FINE later
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Invoking outboundDelivered.setDelivered(false)");
                }
                outboundDelivered.setDelivered(Boolean.FALSE);
            }
        }
    }

    private final RuntimeContext rc;

    public ClientSourceDeliveryCallback(RuntimeContext rc) {
        this.rc = rc;
    }

    public void deliver(ApplicationMessage message) {
        if (message instanceof JaxwsApplicationMessage) {
            deliver(JaxwsApplicationMessage.class.cast(message));
        } else {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1141_UNEXPECTED_MESSAGE_CLASS(
                    message.getClass().getName(),
                    JaxwsApplicationMessage.class.getName())));
        }
    }

    private void deliver(JaxwsApplicationMessage message) {
        LOGGER.entering(message);
        try {
            rc.sourceMessageHandler.attachAcknowledgementInfo(message);

            Packet outboundPacketCopy = message.getPacket().copy(true);

            rc.protocolHandler.appendSequenceHeader(outboundPacketCopy.getMessage(), message);
            rc.protocolHandler.appendAcknowledgementHeaders(outboundPacketCopy, message.getAcknowledgementData());

            Fiber.CompletionCallback responseCallback = new ResponseCallbackHandler(message, rc);

            rc.communicator.sendAsync(outboundPacketCopy, responseCallback);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public RuntimeContext getRuntimeContext() {
        return rc;
    }
}
