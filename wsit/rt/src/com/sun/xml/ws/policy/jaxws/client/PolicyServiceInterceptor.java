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
    
    public List<WebServiceFeature> preCreateBinding(final WSPortInfo port,
            final java.lang.Class<?> serviceEndpointInterface,
            final WSFeatureList defaultFeatures) {
        LOGGER.entering("preCreateBinding",
                new Object[] {port, serviceEndpointInterface, defaultFeatures});
        final LinkedList<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        try {
            final WSDLPort wsdlPort = port.getPort();
            // We only need to read the client config if the server WSDL was not parsed
            if (wsdlPort == null) {
                final String clientCfgFileName = PolicyUtils.ConfigFile.generateFullName(PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER);
                final URL clientCfgFileUrl = PolicyUtils.ConfigFile.loadFromClasspath(clientCfgFileName);
                if (clientCfgFileUrl != null) {
                    LOGGER.config("preCreateBinding", LocalizationMessages.WSP_001022_LOADING_CLIENT_CFG_FILE(clientCfgFileUrl));
                    final WSDLModel clientModel = PolicyConfigParser.parseModel(clientCfgFileUrl, true);
                    final WSDLPolicyMapWrapper clientWrapper = clientModel.getExtension(WSDLPolicyMapWrapper.class);
                    if (clientWrapper != null) {
                        final PolicyMap map = clientWrapper.getPolicyMap();
                        if (map != null) {
                            LOGGER.config("preCreateBinding", LocalizationMessages.WSP_001023_INVOKING_CLI_SIDE_ALTERNATIVE_SELECTION());
                            clientWrapper.doAlternativeSelection();
                            for (ModelConfiguratorProvider configurator : PolicyUtils.ServiceProvider.load(ModelConfiguratorProvider.class)) {
                                configurator.configure(clientModel, map);
                            }
                            // We can not read the features directly from port.getPort() because in the
                            // case of dispatch that object may be null.
                            addFeatures(features, clientModel, port.getPortName());
                            features.add(new PolicyFeature(map, clientModel, port));
                        }
                    }
                } else {
                    LOGGER.config("preCreateBinding",
                            LocalizationMessages.WSP_001035_COULD_NOT_FIND_CLIENT_CFG_FILE_ON_CLASSPATH(clientCfgFileName));
                }
            }
        } catch (PolicyException e) {
            LOGGER.severe("preCreateBinding", e.getMessage(), e);
            throw new WebServiceException(e);
        }
        
        LOGGER.exiting("preCreateBinding", features);
        return features;
    }
    
    /**
     * Add the features in the WSDL model for the given port to the list
     */
    private void addFeatures(
            final List<WebServiceFeature> features, final WSDLModel model, final QName portName) {
        LOGGER.entering("addFeatures", new Object[] { features, model, portName });
        for (WSDLService service : model.getServices().values()) {
            final WSDLPort port = service.get(portName);
            if (port != null) {
                addFeatureListToList(features, port.getFeatures());
                addFeatureListToList(features, port.getBinding().getFeatures());
                break;
            }
        }
        LOGGER.exiting("addFeatures", features);
    }
    
    private void addFeatureListToList(final List<WebServiceFeature> list, final WSFeatureList featureList) {
        for (WebServiceFeature feature : featureList) {
            list.add(feature);
        }
    }
    
}
