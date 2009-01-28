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
package com.sun.xml.ws.assembler.jaxws;

import com.sun.xml.ws.assembler.TubeFactory;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.assembler.ServerTubelineAssemblyContext;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.wsit.TCPTransportPipeFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * TubeFactory implementation creating one of the standard JAX-WS RI tubes
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class TransportTubeFactory implements TubeFactory {

    private static final String AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/transport/client";
    private static final QName AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION = new QName(AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI, "AutomaticallySelectOptimalTransport");

    public Tube createTube(ClientTubelineAssemblyContext context) throws WebServiceException {
        if (isOptimizedTransportEnabled(context.getPolicyMap(), context.getWsdlPort(), context.getPortInfo())) {
            return TCPTransportPipeFactory.doCreate(context.getWrappedContext(), false);
        } else {
            return context.getWrappedContext().createTransportTube();
        }
    }

    public Tube createTube(ServerTubelineAssemblyContext context) throws WebServiceException {
        return context.getTubelineHead();
    }

    /**
     * Checks to see whether OptimizedTransport is enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param port the WSDLPort object
     * @param portInfo the WSPortInfo object
     * @return true if OptimizedTransport is enabled, false otherwise
     */
    private boolean isOptimizedTransportEnabled(PolicyMap policyMap, WSDLPort port, WSPortInfo portInfo) {
        if (policyMap == null || (port == null && portInfo == null)) {
            return false;
        }
        String schema;
        QName serviceName;
        QName portName;
        if (port != null) {
            schema = port.getAddress().getURI().getScheme();
            serviceName = port.getOwner().getName();
            portName = port.getName();
        } else {
            schema = portInfo.getEndpointAddress().getURI().getScheme();
            serviceName = portInfo.getServiceName();
            portName = portInfo.getPortName();
        }
        if (TCPConstants.PROTOCOL_SCHEMA.equals(schema)) {
            // if target endpoint URI starts with TCP schema - dont check policies, just return true
            return true;
        }
        try {
            PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
            if (policy != null && policy.contains(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.TCPTRANSPORT_POLICY_ASSERTION) && policy.contains(AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION)) {
                /* if client set to choose optimal transport and server has TCP transport policy
                then need to check server side policy "enabled" attribute*/
                for (AssertionSet assertionSet : policy) {
                    for (PolicyAssertion assertion : assertionSet) {
                        if (assertion.getName().equals(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.TCPTRANSPORT_POLICY_ASSERTION)) {
                            String value = assertion.getAttributeValue(new QName("enabled"));
                            if (value == null) {
                                return false;
                            }
                            value = value.trim();
                            return Boolean.valueOf(value) || value.equalsIgnoreCase("yes");
                        }
                    }
                }
            }
            return false;
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }
}
