/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.transport.tcp.wsit;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelCreator;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelWSImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import javax.xml.namespace.QName;

/**
 * @author Alexey Stashok
 */
public class ConnectionManagementSettings {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain);
    
    private static final int DEFAULT_VALUE = -1;
    
    private int highWatermark = DEFAULT_VALUE;
    private int maxParallelConnections = DEFAULT_VALUE;
    private int numberToReclaim = DEFAULT_VALUE;
    
    static volatile ConnectionManagementSettings serverSettings;
    static volatile ConnectionManagementSettings clientSettings;
    
    private ConnectionManagementSettings(int highWatermark, int maxParallelConnections, int numberToReclaim) {
        this.highWatermark = highWatermark;
        this.maxParallelConnections = maxParallelConnections;
        this.numberToReclaim = numberToReclaim;
    }
    
    public int getHighWatermark(int defaultValue) {
        if (highWatermark == DEFAULT_VALUE) return defaultValue;
        return highWatermark;
    }
    
    public int getMaxParallelConnections(int defaultValue) {
        if (maxParallelConnections == DEFAULT_VALUE) return defaultValue;
        return maxParallelConnections;
    }
    
    public int getNumberToReclaim(int defaultValue) {
        if (numberToReclaim == DEFAULT_VALUE) return defaultValue;
        return numberToReclaim;
    }
    
    public static ConnectionManagementSettings getServerSettingsInstance() {
        if (serverSettings == null) {
            synchronized(ConnectionManagementSettings.class) {
                if (serverSettings == null) {
                    WSEndpoint<ServiceChannelWSImpl> endpoint = ServiceChannelCreator.getServiceChannelEndpointInstance();
                    serverSettings = createSettingsInstance(endpoint.getPort());
                }
            }
        }
        
        return serverSettings;
    }
    
    public static ConnectionManagementSettings getClientSettingsInstance() {
        if (clientSettings == null) {
            synchronized(ConnectionManagementSettings.class) {
                if (clientSettings == null) {
                    clientSettings = new ConnectionManagementSettings(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
                }
            }
        }
        
        return clientSettings;
    }
    
    static @NotNull ConnectionManagementSettings createSettingsInstance(final @NotNull WSDLPort port) {
        try {
            WSDLModel model = port.getBinding().getOwner();
            WSDLPolicyMapWrapper mapWrapper = model.getExtension(WSDLPolicyMapWrapper.class);
            if (mapWrapper != null) {
                PolicyMap policyMap = mapWrapper.getPolicyMap();
                PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(com.sun.xml.ws.transport.tcp.util.TCPConstants.SERVICE_CHANNEL_WS_NAME,
                        com.sun.xml.ws.transport.tcp.util.TCPConstants.SERVICE_CHANNEL_WS_PORT_NAME);
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
                if (policy != null && policy.contains(TCPConstants.TCPTRANSPORT_CONNECTION_MANAGEMENT_ASSERTION)) {
                    for(AssertionSet assertionSet : policy) {
                        for(PolicyAssertion assertion : assertionSet) {
                            if(assertion.getName().equals(TCPConstants.TCPTRANSPORT_CONNECTION_MANAGEMENT_ASSERTION)){
                                int highWatermark = getAssertionAttrValue(assertion, TCPConstants.TCPTRANSPORT_CONNECTION_MANAGEMENT_HIGH_WATERMARK_ATTR);
                                int maxParallelConnections = getAssertionAttrValue(assertion, TCPConstants.TCPTRANSPORT_CONNECTION_MANAGEMENT_MAX_PARALLEL_CONNECTIONS_ATTR);
                                int numberToReclaim = getAssertionAttrValue(assertion, TCPConstants.TCPTRANSPORT_CONNECTION_MANAGEMENT_NUMBER_TO_RECLAIM_ATTR);
                                
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.log(Level.FINE, 
                                            MessagesMessages.WSTCP_1130_CONNECTION_MNGMNT_SETTINGS_LOADED(
                                            highWatermark, 
                                            maxParallelConnections, 
                                            numberToReclaim));
                                }
                                return new ConnectionManagementSettings(highWatermark, maxParallelConnections, numberToReclaim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        
        return new ConnectionManagementSettings(DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
        
    }
    private static int getAssertionAttrValue(PolicyAssertion assertion, String attrName) {
        String strValue = assertion.getAttributeValue(new QName(attrName));
        if (strValue != null) {
            strValue = strValue.trim();
            return Integer.parseInt(strValue);
        }
        
        return DEFAULT_VALUE;
    }
}
