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

package com.sun.xml.ws.policy.jaxws;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.ws.api.model.wsdl.WSDLExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.EffectiveAlternativeSelector;
import com.sun.xml.ws.policy.EffectivePolicyModifier;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

/**
 *
 * @author japod
 */
public class WSDLPolicyMapWrapper implements WSDLExtension {
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyWSDLParserExtension.class);
    private static final QName NAME = new QName(null, "WSDLPolicyMapWrapper");
    
    private PolicyMap policyMap;
    private EffectivePolicyModifier mapModifier;
    private PolicyMapExtender mapExtender;
    
    protected WSDLPolicyMapWrapper(PolicyMap policyMap) {
        this.policyMap = policyMap;
    }
    
    public WSDLPolicyMapWrapper(PolicyMap policyMap, EffectivePolicyModifier modifier, PolicyMapExtender extender) {
        this(policyMap);
        this.mapModifier = modifier;
        this.mapExtender = extender;
    }
    
    public PolicyMap getPolicyMap() {
        return policyMap;
    }
    
    public void addClientConfigToMap(URL clientWsitConfig) throws PolicyException {
        logger.entering("addClientConfigToMap");
        
        logger.fine("addClientConfigToMap", "wsit-client.xml resource url: '" + clientWsitConfig + "'");
        
        try {
            InputStream is = clientWsitConfig.openStream();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            XMLStreamBuffer buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
            logger.fine("addClientConfigToMap", "Client configuration resource opened.");
            
            WSDLModel wsdlModel = PolicyConfigParser.parse(buffer);
            WSDLPolicyMapWrapper mapWrapper = wsdlModel.getExtension(WSDLPolicyMapWrapper.class);
            PolicyMap clientPolicyMap = mapWrapper.getPolicyMap();
            logger.fine("addClientConfigToMap", "Client configuration resource parsed into a policy map: " + clientPolicyMap);
            
            for (PolicyMapKey key : clientPolicyMap.getAllServiceScopeKeys()) {
                Policy policy = clientPolicyMap.getServiceEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putServiceSubject(key, new PolicySubject(clientWsitConfig, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllEndpointScopeKeys()) {
                Policy policy = clientPolicyMap.getEndpointEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putEndpointSubject(key, new PolicySubject(clientWsitConfig, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllOperationScopeKeys()) {
                Policy policy = clientPolicyMap.getOperationEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putOperationSubject(key, new PolicySubject(clientWsitConfig, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllInputMessageScopeKeys()) {
                Policy policy = clientPolicyMap.getInputMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putInputMessageSubject(key, new PolicySubject(clientWsitConfig, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllOutputMessageScopeKeys()) {
                Policy policy = clientPolicyMap.getOutputMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putOutputMessageSubject(key, new PolicySubject(clientWsitConfig, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllFaultMessageScopeKeys()) {
                Policy policy = clientPolicyMap.getFaultMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putFaultMessageSubject(key, new PolicySubject(clientWsitConfig, policy));
            }
            logger.fine("addClientConfigToMap", "Client configuration policies transfered into final policy map: " + policyMap);
        } catch (FactoryConfigurationError ex) {
            throw new PolicyException(ex);
        } catch (XMLStreamBufferException ex) {
            throw new PolicyException(ex);
        } catch (IOException ex) {
            throw new PolicyException(ex);
        } catch (XMLStreamException ex) {
            throw new PolicyException(ex);
        }
        
        logger.exiting("addClientConfigToMap");
    }
    
    public void doAlternativeSelection() {
        try {
            EffectiveAlternativeSelector.doSelection(mapModifier);
        } catch (PolicyException e) {
            throw new WebServiceException("Failed to find a valid policy alternative", e);
        }
    }
    
    public QName getName() {
        return NAME;
    }
}