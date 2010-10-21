/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.tcp.wsit;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.transport.tcp.client.ServiceChannelTransportPipe;
import com.sun.xml.ws.transport.tcp.client.TCPTransportPipe;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.servicechannel.stubs.ServiceChannelWSImplService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * @author Alexey Stashok
 */
public class TCPTransportPipeFactory extends com.sun.xml.ws.transport.tcp.client.TCPTransportPipeFactory {
    private static final QName serviceChannelServiceName = new ServiceChannelWSImplService().getServiceName();

    @Override
    public Tube doCreate(ClientTubeAssemblerContext context) {
        return doCreate(context, true);
    }

    public static Tube doCreate(@NotNull final ClientTubeAssemblerContext context, final boolean checkSchema) {
        if (checkSchema && !TCPConstants.PROTOCOL_SCHEMA.equalsIgnoreCase(context.getAddress().getURI().getScheme())) {
            return null;
        }
        
        initializeConnectionManagement(context.getWsdlModel());
        if (context.getService().getServiceName().equals(serviceChannelServiceName)) {
            return new ServiceChannelTransportPipe(context);
        }
        
        return new TCPTransportPipe(context);
    }    
    
    /**
     * Sets the client ConnectionManagement settings, which are passed via cliend
     * side policies for ServiceChannelWS
     */
    private static void initializeConnectionManagement(WSDLPort port) {
        PolicyConnectionManagementSettingsHolder instance = 
                PolicyConnectionManagementSettingsHolder.getInstance();
        
        if (instance.clientSettings == null) {
            synchronized(instance) {
                if (instance.clientSettings == null) {
                    instance.clientSettings = 
                            PolicyConnectionManagementSettingsHolder.createSettingsInstance(port);
                }
            }
        }
    }
    
    private static int retrieveCustomTCPPort(WSDLPort port) {
        try {
            WSDLModel model = port.getBinding().getOwner();
            PolicyMap policyMap = model.getPolicyMap();
            if (policyMap != null) {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                if (policy != null && policy.contains(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.TCPTRANSPORT_POLICY_ASSERTION)) {
                    /* if client set to choose optimal transport and server has TCP transport policy
                    then need to check server side policy "enabled" attribute*/
                    for (AssertionSet assertionSet : policy) {
                        for (PolicyAssertion assertion : assertionSet) {
                            if (assertion.getName().equals(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.TCPTRANSPORT_POLICY_ASSERTION)) {
                                String value = assertion.getAttributeValue(new QName("port"));
                                if (value == null) {
                                    return -1;
                                }
                                value = value.trim();

                                try {
                                    return Integer.parseInt(value);
                                } catch(NumberFormatException e) {
                                }
                                
                                return -1;
                            }
                        }
                    }
                }
            }

            return -1;
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }
}
