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

import com.sun.xml.ws.rx.util.TimestampedCollection;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.xml.ws.rx.RxRuntimeException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
class WsMcResponseHandler extends AbstractResponseHandler {

    private static final Logger LOGGER = Logger.getLogger(WsMcResponseHandler.class);

    public WsMcResponseHandler(RxConfiguration configuration, MakeConnectionSenderTask mcSenderTask, TimestampedCollection<String, Fiber> suspendedFiberStorage) {
        super(configuration, mcSenderTask, suspendedFiberStorage);
    }

    public void onCompletion(Packet response) {
        Message responseMessage = response.getMessage();

        super.processMakeConnectionHeaders(responseMessage);

        if (responseMessage != null && responseMessage.isFault()) {
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


        if (responseMessage == null) {
            // TODO L10N
            LOGGER.warning("No response returned for a WS-MakeConnection request");
            return;
        }

        Header relatesToHeader;
        if (!responseMessage.hasHeaders() || (relatesToHeader = responseMessage.getHeaders().get(configuration.getAddressingVersion().relatesToTag, false)) == null) {
            // TODO L10N
            LOGGER.severe("Unable to find request for a response: The response to a WS-MakeConnection request returned for a WS-MakeConnection request" +
                    "does not contain wsa:RelatesTo header");
            return;
        }

        // find original request fiber
        setCorrelationId(relatesToHeader.getStringContent()); // initializing correlation id for getParentFiber()
        Fiber originalFiber = getParentFiber();
        if (originalFiber == null) {
            // TODO L10N
            LOGGER.warning("No suspended fiber found for a response to a WS-MakeConnection request");
            return;
        }

        originalFiber.resume(response);
    }

    public void onCompletion(Throwable error) {
        // TODO L10N
        throw LOGGER.logSevereException(new RxRuntimeException("Sening WS-MakeConnection request failed", error));
    }
}
