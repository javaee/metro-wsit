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
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.runtime.delivery.Postman;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateMessageRegistrationException;
import com.sun.xml.ws.rx.util.AbstractResponseHandler;
import java.io.IOException;
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
                rc.redeliveryTask.register(request, rc.configuration.getRetransmissionBackoffAlgorithm().nextResendTime(request.getNextResendCount(), rc.configuration.getMessageRetransmissionInterval(), rc.sequenceManager));
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
                rc.redeliveryTask.register(request, rc.configuration.getRetransmissionBackoffAlgorithm().nextResendTime(request.getNextResendCount(), rc.configuration.getMessageRetransmissionInterval(), rc.sequenceManager));
            }
        }

        public void onCompletion(Throwable error) {
            if (ClientSourceDeliveryCallback.isResendPossible(error)) {
                rc.redeliveryTask.register(request, rc.configuration.getRetransmissionBackoffAlgorithm().nextResendTime(request.getNextResendCount(), rc.configuration.getMessageRetransmissionInterval(), rc.sequenceManager));
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
                rc.redeliveryTask.register(request, rc.configuration.getRetransmissionBackoffAlgorithm().nextResendTime(request.getNextResendCount(), rc.configuration.getMessageRetransmissionInterval(), rc.sequenceManager));
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
            // TODO L10N
            throw LOGGER.logSevereException(new RxRuntimeException(String.format(
                    "Unexpected message class '%s', expected class '%s'",
                    message.getClass().getName(),
                    JaxwsApplicationMessage.class.getName())));
        }
    }

    private void deliver(JaxwsApplicationMessage message) {
        rc.sourceMessageHandler.attachAcknowledgementInfo(message);

        Packet outboundPacketCopy = message.getPacket().copy(true);

        rc.protocolHandler.appendSequenceHeader(outboundPacketCopy.getMessage(), message);
        rc.protocolHandler.appendAcknowledgementHeaders(outboundPacketCopy, message.getAcknowledgementData());

        Fiber.CompletionCallback responseCallback;
        if (outboundPacketCopy.expectReply == null) {
            responseCallback = new AmbiguousMepCallbackHandler(message, rc);
        } else if (outboundPacketCopy.expectReply.booleanValue()) {
            responseCallback = new ReqRespMepCallbackHandler(message, rc);
        } else {
            responseCallback = new OneWayMepCallbackHandler(message, rc);
        }

        rc.communicator.sendAsync(outboundPacketCopy, responseCallback);
    }

    private static boolean isResendPossible(Throwable throwable) {
        if (throwable instanceof IOException) {
            return true;
        } else if (throwable instanceof WebServiceException) {
            // Unwrap exception and see if it makes sense to retry this request
            // (no need to check for null - handled by instanceof)
            if (throwable.getCause() instanceof IOException) {
                return true;
            }
        }
        return false;
    }
}
