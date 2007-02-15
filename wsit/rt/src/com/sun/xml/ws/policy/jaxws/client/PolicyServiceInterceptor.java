/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

public class PolicyServiceInterceptor extends ServiceInterceptor {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyServiceInterceptor.class);
    
    public List<WebServiceFeature> preCreateBinding(final WSPortInfo port, final java.lang.Class<?> serviceEndpointInterface, final WSFeatureList defaultFeatures) {
        LOGGER.entering("preCreateBinding", new Object[] {port, serviceEndpointInterface, defaultFeatures});
        final LinkedList<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        try {
            final WSDLPort wsdlPort = port.getPort();
            // We only need to read the client config if the server WSDL was not parsed
            if (wsdlPort == null) {
                final WSDLModel clientModel;
                try {
                    clientModel = PolicyConfigParser.parseModel(PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER, null);
                } catch (PolicyException pe) {
                    throw logAndWrapException("preCreateBinding", LocalizationMessages.WSP_1017_ERROR_WHILE_PROCESSING_CLIENT_CONFIG(), pe);
                }
                
                if (clientModel != null) {
                    final WSDLPolicyMapWrapper policyMapWrapper = clientModel.getExtension(WSDLPolicyMapWrapper.class);
                    if (policyMapWrapper != null) {
                        LOGGER.config("preCreateBinding", LocalizationMessages.WSP_1024_INVOKING_CLIENT_POLICY_ALTERNATIVE_SELECTION());
                        try {
                            policyMapWrapper.doAlternativeSelection();
                        } catch (PolicyException e) {
                            throw logAndWrapException("preCreateBinding", LocalizationMessages.WSP_1003_VALID_POLICY_ALTERNATIVE_NOT_FOUND(), e);
                        }
                        
                        final PolicyMap map = policyMapWrapper.getPolicyMap();
                        
                        try {
                            for (ModelConfiguratorProvider configurator : PolicyUtils.ServiceProvider.load(ModelConfiguratorProvider.class)) {
                                configurator.configure(clientModel, map);
                            }
                        } catch (PolicyException e) {
                            throw logAndWrapException("preCreateBinding", LocalizationMessages.WSP_1023_ERROR_WHILE_CONFIGURING_MODEL(), e);
                        }
                        // We can not read the features directly from port.getPort() because in the
                        // case of dispatch that object may be null.
                        addFeatures(features, clientModel, port.getPortName());
                        features.add(new PolicyFeature(map, clientModel, port));
                    } else {
                        LOGGER.config("preCreateBinding", LocalizationMessages.WSP_1022_POLICY_MAP_NOT_IN_MODEL());
                    }
                }
            }
            
            return features;
        } finally {
            LOGGER.exiting("preCreateBinding", features);
        }
    }
    
    private WebServiceException logAndWrapException(final String methodName, final String message, final Throwable cause) {
        LOGGER.severe(methodName, message, cause);
        return new WebServiceException(message, cause);
    }
    
    /**
     * Add the features in the WSDL model for the given port to the list
     */
    private void addFeatures(final List<WebServiceFeature> features, final WSDLModel model, final QName portName) {
        LOGGER.entering("addFeatures", new Object[] { features, model, portName });
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
            LOGGER.exiting("addFeatures", features);
        }
    }
    
    private void addFeatureListToList(final List<WebServiceFeature> list, final WSFeatureList featureList) {
        for (WebServiceFeature feature : featureList) {
            list.add(feature);
        }
    }
}
