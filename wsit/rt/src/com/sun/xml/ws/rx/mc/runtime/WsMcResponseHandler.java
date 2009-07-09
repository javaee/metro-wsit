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
package com.sun.xml.ws.rx.mc.runtime;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.mc.runtime.spi.ProtocolMessageHandler;
import com.sun.xml.ws.rx.util.ResumeFiberException;
import com.sun.xml.ws.rx.util.SuspendedFiberStorage;
import java.util.Map;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
class WsMcResponseHandler extends McResponseHandlerBase {

    private static final Logger LOGGER = Logger.getLogger(WsMcResponseHandler.class);
    //
    private final Map<String, ProtocolMessageHandler> actionToProtocolHandlerMap;

    public WsMcResponseHandler(
            final RxConfiguration configuration,
            final MakeConnectionSenderTask mcSenderTask,
            final SuspendedFiberStorage suspendedFiberStorage,
            final Map<String, ProtocolMessageHandler> protocolHandlerMap) {

        super(configuration, mcSenderTask, suspendedFiberStorage);

        this.actionToProtocolHandlerMap = protocolHandlerMap;
    }

    public void onCompletion(Packet response) {
        try {
            Message responseMessage = response.getMessage();

            if (responseMessage == null) {
                // TODO L10N
                LOGGER.warning("No response returned for a WS-MakeConnection request");
                return;
            }

            if (!responseMessage.hasHeaders()) {
                // TODO L10N
                LOGGER.severe("Unable to find a proper response receiver: " +
                        "The response to a WS-MakeConnection request does not contain any headers.");
                return;
            }

            super.processMakeConnectionHeaders(responseMessage);

            if (responseMessage.isFault()) {
                // processing WS-MC SOAP faults
                String faultAction = responseMessage.getHeaders().getAction(configuration.getAddressingVersion(), configuration.getSoapVersion());
                if (configuration.getMcVersion().isMcFault(faultAction)) {
                    SOAPFault fault = null;
                    try {
                        fault = responseMessage.readAsSOAPMessage().getSOAPBody().getFault();
                    } catch (SOAPException ex) {
                        throw LOGGER.logSevereException(new RxRuntimeException("Unable to unmarshall SOAP fault from the SOAP message.", ex));
                    }

                    throw LOGGER.logSevereException(new RxRuntimeException(String.format("Unexpected WS-MakeConnection protocol error: %s", fault.getFaultString())));
                }
            }

            Header wsaRelatesToHeader = responseMessage.getHeaders().get(configuration.getAddressingVersion().relatesToTag, false);
            if (wsaRelatesToHeader != null) {
                // find original request fiber
                setCorrelationId(wsaRelatesToHeader.getStringContent()); // initializing correlation id for getParentFiber()
                try {
                    resumeParentFiber(response);
                } catch (ResumeFiberException ex) {
                    // TODO L10N
                    LOGGER.warning("Unable to resume parent fiber for a response to a WS-MakeConnection request", ex);
                }
            }

            // TODO L10N
            LOGGER.finer("Proceeding with processing the response as a protocol message.");
            Header wsaActionHeader = responseMessage.getHeaders().get(configuration.getAddressingVersion().actionTag, false);
            if (wsaActionHeader != null) {
                String wsaAction = wsaActionHeader.getStringContent();
                ProtocolMessageHandler handler = actionToProtocolHandlerMap.get(wsaAction);
                if (handler != null) {
                    LOGGER.finer(String.format(
                            "Processing WS-MC response with WS-Addressing action [ %s ] using ProtocolMessageHandler of class [ %s ]",
                            wsaAction,
                            handler.getClass().getName()));

                    handler.processProtocolMessage(response);
                } else {
                    LOGGER.warning(String.format(
                            "Unable to find a ProtocolMessageHandler to process WS-MC response with WS-Addressing action [ %s ]",
                            wsaAction));
                }
            } else {
                LOGGER.severe("Unable to find a proper response receiver: " +
                        "The response to a WS-MakeConnection request does not contain WS-Addressing Action header.");
            }
        } finally {
            mcSenderTask.clearMcRequestPendingFlag();
        }
    }

    public void onCompletion(Throwable error) {
        try {
            // TODO L10N
            throw LOGGER.logSevereException(new RxRuntimeException("Sening WS-MakeConnection request failed", error));
        } finally {
            mcSenderTask.clearMcRequestPendingFlag();
        }
    }
}
