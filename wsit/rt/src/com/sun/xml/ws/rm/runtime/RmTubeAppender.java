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

import com.sun.xml.ws.assembler.TubeAppender;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.jaxws.runtime.client.RMClientTube;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMServerTube;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class RmTubeAppender implements TubeAppender {

    // TODO: this is a draft class that should serve later as a model for extracting the WSIT/Metro TubeCreator interface
    /**
     * Adds RM tube to the client-side tubeline, depending on whether RM is enabled or not.
     * 
     * @param context wsit client tubeline assembler context
     * @return new tail of the client-side tubeline
     */
    public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
        if (isReliableMessagingEnabled(context.getPolicyMap(), context.getWsdlPort())) {
            return new RMClientTube(
                    context.getWsdlPort(),
                    context.getBinding(),
                    context.getScInitiator(),
                    context.getTubelineHead());
            // TODO uncomment new tube creation
            // return new ClientRmTube(context);
        } else {
            return context.getTubelineHead();
        }
    }

    /**
     * Adds RM tube to the service-side tubeline, depending on whether RM is enabled or not.
     * 
     * @param context wsit service tubeline assembler context
     * @return new head of the service-side tubeline
     */
    public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
        if (isReliableMessagingEnabled(context.getPolicyMap(), context.getWsdlPort())) {
            return new RMServerTube(
                    context.getWsdlPort(),
                    context.getEndpoint().getBinding(),
                    context.getTubelineHead());
            // TODO uncomment new tube creation
            // return new ServerRmTube(context);
        } else {
            return context.getTubelineHead();
        }
    }

    /**
     * Checks to see whether WS-ReliableMessaging is enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param port wsdl:port
     * @return true if ReliableMessaging is enabled, false otherwise
     */
    private boolean isReliableMessagingEnabled(PolicyMap policyMap, WSDLPort port) throws WebServiceException {
        if (policyMap == null || port == null) {
            return false;
        }

        try {
            PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
            if (policy == null) {
                return false;
            } else {
                return policy.contains(RmVersion.WSRM10.policyNamespaceUri) || policy.contains(RmVersion.WSRM11.policyNamespaceUri);
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }
}
