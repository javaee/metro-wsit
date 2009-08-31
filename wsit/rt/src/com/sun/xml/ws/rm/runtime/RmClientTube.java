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

import com.sun.xml.internal.ws.client.ClientTransportException;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.policy.ConfigurationManager;
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

    private static final RmLogger LOGGER = RmLogger.getLogger(RmClientTube.class);
    private final ClientSession session;
    private final WSDLPort wsdlPort;
    private Packet requestPacketCopy;

    RmClientTube(RmClientTube original, TubeCloner cloner) {
        super(original, cloner);

        this.session = original.session;
        this.wsdlPort = original.wsdlPort;

        this.requestPacketCopy = null;
    }

    RmClientTube(WsitClientTubeAssemblyContext context) throws RmRuntimeException {
        super(context.getTubelineHead());
        SecureConversationInitiator scInitiator = context.getImplementation(SecureConversationInitiator.class);
        if (scInitiator == null) {
            // TODO remove this condition and remove context.getScInitiator() method
            scInitiator = context.getScInitiator();
        }

        // TODO don't take the first config alternative automatically...
        Configuration configuration = ConfigurationManager.createClientConfigurationManager(context.getWsdlPort(), context.getBinding()).getConfigurationAlternatives()[0];
//        if (configuration.getAddressingVersion() != AddressingVersion.W3C) {
//            throw new RmRuntimeException(LocalizationMessages.WSRM_1120_UNSUPPORTED_WSA_VERSION(configuration.getAddressingVersion().toString()));
//        }
        
        this.session = ClientSession.create(
                configuration,
                new ProtocolCommunicator(super.next, scInitiator, configuration));
        this.wsdlPort = context.getWsdlPort();

        this.requestPacketCopy = null;

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
            if (isResendAttempt()) {
                session.registerForResend(Fiber.current(), requestPacket);
                return doSuspend(next);
            } else { // this is a first-time processing
                // we do not modify original packet in case we wanted to reuse it later                
                
                requestPacket = session.processOutgoingPacket(requestPacket);
                prepareForResend(requestPacket);
                return super.processRequest(requestPacket);
            }
//        } catch (RmSoapFaultException ex) {
//            return doReturnWith(ex.getSoapFaultResponse());
        } catch (RmRuntimeException ex) {
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

            if (
                    (responseToOneWayRequest && requestNotAcknowledged(requestPacketCopy)) ||
                    (!responseToOneWayRequest && responseNotAvailableYet(responsePacket))
            ) {
                LOGGER.fine(LocalizationMessages.WSRM_1102_RESENDING_DROPPED_MESSAGE());
                return doResend();
            } else {
                releaseResendResources();
                return super.processResponse(responsePacket);
            }

        } catch (RmRuntimeException ex) {
            LOGGER.logSevereException(ex);
            releaseResendResources();
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
                releaseResendResources();
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

    private boolean isResendPossible(Throwable throwable) {
        if (throwable instanceof IOException) {
            return true;
        } else if (throwable instanceof WebServiceException) {
            if (throwable instanceof ClientTransportException) {
                return true; // if endpint went down, let's try to resend, as it may come up again
            }

            //Unwrap exception and see if it makes sense to retry this request (no need to check for null).
            if (throwable.getCause() instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    private NextAction doResend() {
        return super.doInvokeAndForget(this, requestPacketCopy.copy(true));
    }

    private boolean isResendAttempt() {
        return requestPacketCopy != null;
    }

    private void prepareForResend(Packet requestPacket) {
        requestPacketCopy = requestPacket.copy(true);
    }
    
    private void releaseResendResources() {
        requestPacketCopy = null;
    }

    private boolean responseNotAvailableYet(Packet responsePacket) {
        // Check for empty body response to two-way message. WCF as well as our implementation 
        // will return empty message with a sequence acknowledgement header when a duplicate 
        // request message was received but response is not available yet (due to a long processing). 
        // In such case we also need to retry.
        Message responseMessage = responsePacket.getMessage();
        return responseMessage != null && !responseMessage.hasPayload();
    }
    
    private boolean requestNotAcknowledged(Packet requestPacket) {
        return !session.sequenceManager.getSequence(session.outboundSequenceId).isAcknowledged(PacketAdapter.getInstance(session.configuration, requestPacket).getMessageNumber());
    }
}
