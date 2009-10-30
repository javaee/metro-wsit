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

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.delivery.Postman;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.util.AbstractResponseHandler;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.ws.WebServiceException;

class ClientSourceDeliveryCallback implements Postman.Callback {

    private static final Logger LOGGER = Logger.getLogger(ClientSourceDeliveryCallback.class);

    private static class OneWayMepCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {

        private final RuntimeContext rc;
        private final ApplicationMessage request;

        public OneWayMepCallbackHandler(ApplicationMessage request, RuntimeContext rc) {
            super(rc.suspendedFiberStorage, request.getCorrelationId());
            this.request = request;
            this.rc = rc;
        }

        public void onCompletion(Packet response) {
            if (response.getMessage() != null) {
                // TODO handle RM faults
                JaxwsApplicationMessage message = new JaxwsApplicationMessage(response, getCorrelationId());
                rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
                rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());

                rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());
            }

            resumeParentFiber(response);
        }

        public void onCompletion(Throwable error) {
            if (ClientSourceDeliveryCallback.isResendPossible(error)) {
                RedeliveryTaskExecutor.INSTANCE.register(
                        request,
                        rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(request.getNextResendCount(), rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                        TimeUnit.MILLISECONDS,
                        rc.sourceMessageHandler);
            } else {
                resumeParentFiber(error);
            }
        }
    }

    private static class ReqRespMepCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {

        private final ApplicationMessage request;
        private final RuntimeContext rc;

        public ReqRespMepCallbackHandler(ApplicationMessage request, RuntimeContext rc) {
            super(rc.suspendedFiberStorage, request.getCorrelationId());
            this.request = request;
            this.rc = rc;
        }

        public void onCompletion(Packet response) {
            if (response.getMessage() != null) {
                // TODO handle RM faults
                JaxwsApplicationMessage message = new JaxwsApplicationMessage(response, getCorrelationId());
                rc.protocolHandler.loadSequenceHeaderData(message, message.getJaxwsMessage());
                rc.protocolHandler.loadAcknowledgementData(message, message.getJaxwsMessage());
                try {
                    rc.destinationMessageHandler.registerMessage(message);
                    rc.destinationMessageHandler.processAcknowledgements(message.getAcknowledgementData());
                    rc.destinationMessageHandler.putToDeliveryQueue(message); // resuming parent fiber there
                } catch (DuplicateMessageRegistrationException ex) {
                    onCompletion(ex);
                }
            } else {
                RedeliveryTaskExecutor.INSTANCE.register(
                        request,
                        rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(request.getNextResendCount(), rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                        TimeUnit.MILLISECONDS,
                        rc.sourceMessageHandler);
            }
        }

        public void onCompletion(Throwable error) {
            if (ClientSourceDeliveryCallback.isResendPossible(error)) {
                RedeliveryTaskExecutor.INSTANCE.register(
                        request,
                        rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(request.getNextResendCount(), rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                        TimeUnit.MILLISECONDS,
                        rc.sourceMessageHandler);
            } else {
                resumeParentFiber(error);
            }
        }
    }

    private static class AmbiguousMepCallbackHandler extends AbstractResponseHandler implements Fiber.CompletionCallback {

        private final ApplicationMessage request;
        private final RuntimeContext rc;

        public AmbiguousMepCallbackHandler(ApplicationMessage request, RuntimeContext rc) {
            super(rc.suspendedFiberStorage, request.getCorrelationId());
            this.request = request;
            this.rc = rc;
        }

        public void onCompletion(Packet response) {
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
        }

        public void onCompletion(Throwable error) {
            if (ClientSourceDeliveryCallback.isResendPossible(error)) {
                RedeliveryTaskExecutor.INSTANCE.register(
                        request,
                        rc.configuration.getRmFeature().getRetransmissionBackoffAlgorithm().getDelayInMillis(request.getNextResendCount(), rc.configuration.getRmFeature().getMessageRetransmissionInterval()),
                        TimeUnit.MILLISECONDS,
                        rc.sourceMessageHandler);
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

            // TODO: remove the workaround bellow when JAX-WS RI fixes the Packet.copy(boolean) method
            outboundPacketCopy.expectReply = message.getPacket().expectReply;

            // TODO remove the workaround bellow when JAX-WS RI fixes ignoring server response in HttpTransportPipe.process(Packet) method
            Boolean isResquestResponseMep = outboundPacketCopy.expectReply; // remembering original value to be able to pick proper CallbackHandler
            outboundPacketCopy.expectReply = Boolean.TRUE; // setting to true to prevent HttpTransportPipe from ignoring server response (acknowledgement)

            rc.protocolHandler.appendSequenceHeader(outboundPacketCopy.getMessage(), message);
            rc.protocolHandler.appendAcknowledgementHeaders(outboundPacketCopy, message.getAcknowledgementData());

            Fiber.CompletionCallback responseCallback;
            if (isResquestResponseMep == null) {
                responseCallback = new AmbiguousMepCallbackHandler(message, rc);
            } else if (isResquestResponseMep.booleanValue()) {
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

    private static boolean isResendPossible(Throwable throwable) {
        if (throwable instanceof IOException) {
            return true;
        } else if (throwable instanceof WebServiceException) {
            if (throwable instanceof ClientTransportException) {
                return true; // if endpint went down, let's try to resend, as it may come up again
            }
            // Unwrap exception and see if it makes sense to retry this request
            // (no need to check for null - handled by instanceof)
            if (throwable.getCause() instanceof IOException) {
                return true;
            }
        }
        return false;
    }
}
