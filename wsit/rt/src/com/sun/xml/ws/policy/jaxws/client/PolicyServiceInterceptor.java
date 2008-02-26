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

package com.sun.xml.ws.policy.jaxws.client;

import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.client.ServiceInterceptor;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.PolicyConfigParser;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

public class PolicyServiceInterceptor extends ServiceInterceptor {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyServiceInterceptor.class);
    
    public List<WebServiceFeature> preCreateBinding(final WSPortInfo port, final java.lang.Class<?> serviceEndpointInterface, final WSFeatureList defaultFeatures) {
        LOGGER.entering(port, serviceEndpointInterface, defaultFeatures);
        final LinkedList<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        try {
            final WSDLPort wsdlPort = port.getPort();
            // We only need to read the client config if the server WSDL was not parsed
            if (wsdlPort == null) {
                final WSDLModel clientModel;
                try {
                    clientModel = PolicyConfigParser.parseModel(PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER, null);
                } catch (PolicyException pe) {
                    throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1017_ERROR_WHILE_PROCESSING_CLIENT_CONFIG(), pe));
                }
                
                if (clientModel != null) {
                    final WSDLPolicyMapWrapper policyMapWrapper = clientModel.getExtension(WSDLPolicyMapWrapper.class);
                    if (policyMapWrapper == null) {
                        LOGGER.config(LocalizationMessages.WSP_1022_POLICY_MAP_NOT_IN_MODEL());
                    } else {
                        LOGGER.config(LocalizationMessages.WSP_1024_INVOKING_CLIENT_POLICY_ALTERNATIVE_SELECTION());
                        try {
                            policyMapWrapper.doAlternativeSelection();
                        } catch (PolicyException e) {
                            throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1003_VALID_POLICY_ALTERNATIVE_NOT_FOUND(), e));
                        }
                        
                        final PolicyMap map = policyMapWrapper.getPolicyMap();
                        
                        try {
                            for (ModelConfiguratorProvider configurator : PolicyUtils.ServiceProvider.load(ModelConfiguratorProvider.class)) {
                                configurator.configure(clientModel, map);
                            }
                        } catch (PolicyException e) {
                            throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSP_1023_ERROR_WHILE_CONFIGURING_MODEL(), e));
                        }
                        // We can not read the features directly from port.getPort() because in the
                        // case of dispatch that object may be null.
                        addFeatures(features, clientModel, port.getPortName());
                        features.add(new PolicyFeature(map, clientModel, port));
                    }
                }
            }
            
            return features;
        } finally {
            LOGGER.exiting(features);
        }
    }
    
    /**
     * Add the features in the WSDL model for the given port to the list
     */
    private void addFeatures(final List<WebServiceFeature> features, final WSDLModel model, final QName portName) {
        LOGGER.entering(features, model, portName);
        try {
            for (WSDLService service : model.getServices().values()) {
                final WSDLPort port = service.get(portName);
                if (port != null) {
                    addFeatureListToList(features, port.getFeatures());
                    addFeatureListToList(features, port.getBinding().getFeatures());
                    break;
                }
            }
        } finally {
            LOGGER.exiting(features);
        }
    }
    
    private void addFeatureListToList(final List<WebServiceFeature> list, final WSFeatureList featureList) {
        for (WebServiceFeature feature : featureList) {
            list.add(feature);
        }
    }
}
