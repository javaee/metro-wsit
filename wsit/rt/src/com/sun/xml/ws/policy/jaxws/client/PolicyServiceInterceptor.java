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
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyServiceInterceptor.class);
    
    public List<WebServiceFeature> preCreateBinding(WSPortInfo port,
            java.lang.Class<?> serviceEndpointInterface,
            WSFeatureList defaultFeatures) {
        logger.entering("preCreateBinding",
                new Object[] {port, serviceEndpointInterface, defaultFeatures});
        LinkedList<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        try {
            WSDLPort wsdlPort = port.getPort();
            // We only need to read the client config if the server WSDL was not parsed
            if (wsdlPort == null) {
                String clientCfgFileName = PolicyUtils.ConfigFile.generateFullName(PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER);
                URL clientCfgFileUrl = PolicyUtils.ConfigFile.loadAsResource(clientCfgFileName, null);
                if (clientCfgFileUrl != null) {
                    logger.config("preCreateBinding", LocalizationMessages.LOADING_CLIENT_CFG_FILE(clientCfgFileUrl));
                    WSDLModel clientModel = PolicyConfigParser.parseModel(clientCfgFileUrl, true);
                    WSDLPolicyMapWrapper clientWrapper = clientModel.getExtension(WSDLPolicyMapWrapper.class);
                    if (clientWrapper != null) {
                        PolicyMap map = clientWrapper.getPolicyMap();
                        if (map != null) {
                            logger.config("preCreateBinding", LocalizationMessages.INVOKING_CLI_SIDE_ALTERNATIVE_SELECTION());
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
                    logger.config("preCreateBinding",
                            LocalizationMessages.COULD_NOT_FIND_CLIENT_CFG_FILE_ON_CLASSPATH(clientCfgFileName));
                }
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
        
        logger.exiting("preCreateBinding", features);
        return features;
    }
    
    /**
     * Add the features in the WSDL model for the given port to the list
     */
    private void addFeatures(List<WebServiceFeature> features, WSDLModel model, QName portName) {
        logger.entering("addFeatures", new Object[] { features, model, portName });
        for (WSDLService service : model.getServices().values()) {
            WSDLPort port = service.get(portName);
            if (port != null) {
                addFeatureListToList(features, port.getFeatures());
                addFeatureListToList(features, port.getBinding().getFeatures());
                break;
            }
        }
        logger.exiting("addFeatures", features);
    }
    
    private void addFeatureListToList(List<WebServiceFeature> list, WSFeatureList featureList) {
        for (WebServiceFeature feature : featureList) {
            list.add(feature);
        }
    }
    
}
