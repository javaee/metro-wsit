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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.rx.RxException;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.delivery.Postman;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.util.AbstractResponseHandler;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class ClientSourceDeliveryCallback implements Postman.Callback {

    private static final Logger LOGGER = Logger.getLogger(ClientSourceDeliveryCallback.class);

    private static class OneWayMepCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {

        private final RuntimeContext rc;
        private final JaxwsApplicationMessage request;

        public OneWayMepCallbackHandler(JaxwsApplicationMessage request, RuntimeContext rc) {
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

                    if (rc.configuration.getRuntimeVersion().protocolVersion.isFault(message.getWsaAction())) {
                        // TODO handle RM faults
                    }
                }

                resumeParentFiber(response);
            } catch (RxRuntimeException ex) {
                onCompletion(ex);
            } finally {
                HaContext.clear();
            }
        }

        public void onCompletion(Throwable error) {
            if (!Utilities.isResendPossible(error)) {
                resumeParentFiber(error);
                return;
            }

            final int nextResendCount = request.getNextResendCount();
            if (!rc.configuration.getRmFeature().canRetransmitMessage(nextResendCount)) {
                resumeParentFiber(error);
                return;
            }

            try {
                HaContext.initFrom(request.getPacket());

                RedeliveryTaskExecutor.register(
                        request,
                        rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(nextResendCount, rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                        TimeUnit.MILLISECONDS,
                        rc.sourceMessageHandler,
                        request.getPacket().component);
            } finally {
                HaContext.clear();
            }
        }
    }

    private static class ReqRespMepCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {

        private final JaxwsApplicationMessage request;
        private final RuntimeContext rc;

        public ReqRespMepCallbackHandler(JaxwsApplicationMessage request, RuntimeContext rc) {
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

                    if (rc.configuration.getRuntimeVersion().protocolVersion.isFault(message.getWsaAction())) {
                        // TODO handle RM faults
                    }

                    if (message.getSequenceId() != null) {
                        rc.destinationMessageHandler.registerMessage(message);
                        rc.destinationMessageHandler.putToDeliveryQueue(message); // resuming parent fiber there
                    } else {
                        // if the response message does not contain sequence headers, 
                        // process it as a normal, non-RM message
                        resumeParentFiber(response);
                    }

                } else {
                    final int nextResendCount = request.getNextResendCount();
                    if (!rc.configuration.getRmFeature().canRetransmitMessage(nextResendCount)) {
                        resumeParentFiber(new RxRuntimeException((LocalizationMessages.WSRM_1159_MAX_MESSAGE_RESEND_ATTEMPTS_REACHED())));
                        return;
                    }

                    RedeliveryTaskExecutor.register(
                            request,
                            rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(nextResendCount, rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                            TimeUnit.MILLISECONDS,
                            rc.sourceMessageHandler,
                            response.component);
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
                    resumeParentFiber(new RxRuntimeException((LocalizationMessages.WSRM_1159_MAX_MESSAGE_RESEND_ATTEMPTS_REACHED())));
                    return;
                }

                try {
                    HaContext.initFrom(request.getPacket());

                    RedeliveryTaskExecutor.register(
                            request,
                            rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(nextResendCount, rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                            TimeUnit.MILLISECONDS,
                            rc.sourceMessageHandler,
                            request.getPacket().component);

                } finally {
                    HaContext.clear();
                }


            } else {
                resumeParentFiber(error);
            }
        }
    }

    private static class AmbiguousMepCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {

        private final JaxwsApplicationMessage request;
        private final RuntimeContext rc;

        public AmbiguousMepCallbackHandler(JaxwsApplicationMessage request, RuntimeContext rc) {
            super(rc.suspendedFiberStorage, request.getCorrelationId());
            this.request = request;
            this.rc = rc;
        }

        public void onCompletion(Packet response) {
            try {
                HaContext.initFrom(response);

                if (response.getMessage() != null) {
                    // TODO handle RM faults
                    JaxwsApplicationMessage message = new JaxwsApplicationMessage(response, getCorrelationId());
                    rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
                    rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());

                    rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());

                    if (message.getSequenceId() != null) {
                        try {
                            rc.destinationMessageHandler.registerMessage(message);
                            rc.destinationMessageHandler.putToDeliveryQueue(message);
                            return;
                        } catch (DuplicateMessageRegistrationException ex) {
                            onCompletion(ex);
                            return;
                        }
                    }
                }

                resumeParentFiber(response);

            } catch (RxRuntimeException ex) {
                onCompletion(ex);
            } finally {
                HaContext.clear();
            }
        }

        public void onCompletion(Throwable error) {
            if (Utilities.isResendPossible(error)) {
                RedeliveryTaskExecutor.register(
                        request,
                        rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(request.getNextResendCount(), rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                        TimeUnit.MILLISECONDS,
                        rc.sourceMessageHandler,
                        request.getPacket().component);
            } else {
                resumeParentFiber(error);
            }
        }
    }
    //
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

            Fiber.CompletionCallback responseCallback;
            if (outboundPacketCopy.expectReply == null) {
                responseCallback = new AmbiguousMepCallbackHandler(message, rc); // should not really happen on the request packet
            } else if (outboundPacketCopy.expectReply.booleanValue()) {
                responseCallback = new ReqRespMepCallbackHandler(message, rc);
            } else {
                responseCallback = new OneWayMepCallbackHandler(message, rc);
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finer("Selected Response callback class: " + responseCallback.getClass().getName());
            }

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
