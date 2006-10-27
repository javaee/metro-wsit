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

package com.sun.xml.ws.policy.jaxws.encoding;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.fastinfoset.FastInfosetFeature;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * A configurator provider for FastInfoset policy assertions.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FastInfosetModelConfiguratorProvider implements ModelConfiguratorProvider{
    
    public static final QName fastInfosetAssertion = new QName(
            "http://java.sun.com/xml/ns/wsit/2006/09/policy/fastinfoset/service",
            "OptimizedFastInfosetSerialization");
        
    public static final QName enabled = new QName("enabled");
    
    /**
     * Process FastInfoset policy assertions.
     *
     * @param model the WSDL model.
     * @param policyMap the policy map.
     */
    public void configure(@NotNull WSDLModel model, @NotNull PolicyMap policyMap) throws PolicyException {
        assert model != null;
        assert policyMap != null;
        
        for (WSDLService service:model.getServices().values()) {
            for (WSDLPort port : service.getPorts()) {
                PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(service.getName(),port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(key);
                if (null!=policy && policy.contains(fastInfosetAssertion)) {
                    Iterator <AssertionSet> assertions = policy.iterator();
                    while(assertions.hasNext()){
                        AssertionSet assertionSet = assertions.next();
                        Iterator<PolicyAssertion> policyAssertion = assertionSet.iterator();
                        while(policyAssertion.hasNext()){
                            PolicyAssertion assertion = policyAssertion.next();
                            if(assertion.getName().equals(fastInfosetAssertion)){
                                String value = assertion.getAttributeValue(enabled);
                                boolean isFastInfosetEnabled = Boolean.valueOf(value.trim());
                                port.getBinding().addFeature(new FastInfosetFeature(isFastInfosetEnabled));
                            } // end-if non optional mtom assertion found
                        } // next assertion
                    } // next alternative
                } // end-if policy contains mtom assertion
            } // end foreach port
        } // end foreach service
    }
}
