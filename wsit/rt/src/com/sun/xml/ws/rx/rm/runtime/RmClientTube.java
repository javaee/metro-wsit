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

import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.mc.runtime.McClientTube;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import java.io.IOException;
import javax.xml.ws.WebServiceException;

/**
 * Attaches additional RM-specific headers to each request message and ensures the reliable delivery of the message (in 
 * case of any problems with sending the message, the exception is evaluated and the message is scheduled for a resend 
 * if possible.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class RmClientTube extends AbstractFilterTubeImpl {

    private static final Logger LOGGER = Logger.getLogger(RmClientTube.class);
    private final ClientSession session;
    private final WSDLPort wsdlPort;
    // resend resources
    private Packet requestPacketCopy;
    private int resendCounter;

    RmClientTube(RmClientTube original, TubeCloner cloner) {
        super(original, cloner);

        this.session = original.session;
        this.wsdlPort = original.wsdlPort;

        this.requestPacketCopy = null;
        this.resendCounter = 0;
    }

    RmClientTube(RxConfiguration configuration, Tube tubelineHead, ClientTubelineAssemblyContext context) throws RxRuntimeException {
        super(tubelineHead); // cannot use context.getTubelineHead as McClientTube might have been created in RxTubeFactory
        SecureConversationInitiator scInitiator = context.getImplementation(SecureConversationInitiator.class);
        if (scInitiator == null) {
            // TODO remove this condition and remove context.getScInitiator() method
            scInitiator = context.getScInitiator();
        }

        // TODO we should also tak into account addressable clients
        final WSEndpointReference clientEndpointReference;
        if (configuration.isMakeConnectionSupportEnabled()) {
            final McClientTube mcClientTube = context.getImplementation(McClientTube.class);
            assert mcClientTube != null;

            clientEndpointReference = mcClientTube.getWsmcAnonymousEndpointReference();
        } else {
            clientEndpointReference = configuration.getAddressingVersion().anonymousEpr;
        }

        this.session = ClientSession.create(
                configuration,
                clientEndpointReference,
                new Communicator("RmClientTubeCommunicator", context.getAddress(), super.next, scInitiator, configuration.getAddressingVersion(), configuration.getSoapVersion(), configuration.getRmVersion().getJaxbContext(configuration.getAddressingVersion())));
        this.wsdlPort = context.getWsdlPort();

        this.requestPacketCopy = null;
        this.resendCounter = 0;
    }

    @Override
    public RmClientTube copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new RmClientTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processRequest(Packet requestPacket) {
        LOGGER.entering();
        try {
            if (isResendAttempt()) { // resend of reqest/response processing
                session.registerForResend(Fiber.current(), requestPacket, resendCounter);
                return doSuspend(next);
            } else { // this is a first-time processing
                // we do not modify original packet in case we wanted to reuse it later

                requestPacket = session.registerOutgoingRequest(requestPacket);
                initResendResources(requestPacket);

                requestPacket = session.appendOutgoingAcknowledgementHeaders(requestPacket);
                return super.processRequest(requestPacket);
            }
        } catch (RxRuntimeException ex) {
            LOGGER.logSevereException(ex);
            return doThrow(ex);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet responsePacket) {
        LOGGER.entering();
        try {
            boolean responseToOneWayRequest = requestPacketCopy.getMessage().isOneWay(wsdlPort);
            responsePacket = session.processIncommingPacket(responsePacket, responseToOneWayRequest);

            if (responseToOneWayRequest) { // One-way
                if (!session.isRequestAcknowledged(requestPacketCopy)) {
                    session.registerForResend(requestPacketCopy, resendCounter); // don't need to do another request packet copy
                }
            } else { // Request/Response
                if (session.isRequestAcknowledged(requestPacketCopy) || responseNotAvailableYet(responsePacket)) {
                    LOGGER.fine(LocalizationMessages.WSRM_1102_RESENDING_DROPPED_MESSAGE());
                    return doResend();
                }
            }

            resetResendResources();
            return super.processResponse(responsePacket);

        } catch (RxRuntimeException ex) {
            LOGGER.logSevereException(ex);
            resetResendResources();
            return doThrow(ex);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processException(Throwable throwable) {
        LOGGER.entering();
        try {
            if (isResendPossible(throwable)) {
                // eat exception and forward processing to this.processRequest() (INVOKE_AND_FORGET) for request message resend
                return doResend();
            } else {
                resetResendResources();
                return super.processException(throwable);
            }
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        LOGGER.entering();
        try {
            session.close();
        } catch (RuntimeException ex) {
            LOGGER.warning(LocalizationMessages.WSRM_1103_RM_SEQUENCE_NOT_TERMINATED_NORMALLY(), ex);
        } finally {
            super.preDestroy();
            LOGGER.exiting();
        }
    }

    static boolean isResendPossible(Throwable throwable) {
        if (throwable instanceof IOException) {
            return true;
        } else if (throwable instanceof WebServiceException) {
            //Unwrap exception and see if it makes sense to retry this request (no need to check for null).
            if (throwable.getCause() instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    private NextAction doResend() {
        resendCounter++;
        return super.doInvokeAndForget(this, requestPacketCopy.copy(true));
    }

    private boolean isResendAttempt() {
        return requestPacketCopy != null;
    }

    private void initResendResources(Packet requestPacket) {
        requestPacketCopy = requestPacket.copy(true);
    }

    private void resetResendResources() {
        requestPacketCopy = null;
        resendCounter = 0;
    }

    private boolean responseNotAvailableYet(Packet responsePacket) {
        // Check for empty body response to two-way message. WCF as well as our implementation 
        // will return empty message with a sequence acknowledgement header when a duplicate 
        // request message was received but response is not available yet (due to a long processing). 
        // In such case we also need to retry.
        Message responseMessage = responsePacket.getMessage();
        return responseMessage != null && !responseMessage.hasPayload();
    }
}
