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

package com.sun.xml.ws.policy.jaxws.addressing;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author japod
 */
public class AddressingModelConfiguratorProvider implements ModelConfiguratorProvider{

    private static final PolicyLogger logger = PolicyLogger.getLogger(AddressingModelConfiguratorProvider.class);

    private static final QName[] AddressingAssertions = {
     new QName(AddressingVersion.MEMBER.policyNsUri,"UsingAddressing"),
     new QName(AddressingVersion.W3C.policyNsUri,"UsingAddressing")};

    /**
     * Creates a new instance of AddressingModelConfiguratorProvider
     */
    public AddressingModelConfiguratorProvider() {
    }

    /**
     * process addressing policy assertions and if found and are not optional then addressing is enabled on the
     * {@link com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType}
     *
     * @param model must be non-null
     * @param policyMap must be non-null
     */
    public void configure(WSDLModel model, PolicyMap policyMap) throws PolicyException {
        logger.entering("configure", new Object[]{model, policyMap});
        if ((null==model) || (null==policyMap)) {
            return;
        }
        for (WSDLService service:model.getServices().values()) {
            for (WSDLPort port : service.getPorts()) {
                PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(service.getName(),port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(key);
                for (QName addressingAssertionQName : AddressingAssertions) {
                    if (null!=policy && policy.contains(addressingAssertionQName)) {
                        Iterator <AssertionSet> assertions = policy.iterator();
                        while(assertions.hasNext()){
                            AssertionSet assertionSet = assertions.next();
                            Iterator<PolicyAssertion> policyAssertion = assertionSet.iterator();
                            while(policyAssertion.hasNext()){
                                PolicyAssertion assertion = policyAssertion.next();
                                if(assertion.getName().equals(addressingAssertionQName) && !assertion.isOptional()){
                                    WebServiceFeature feature = AddressingVersion.getFeature(addressingAssertionQName.getNamespaceURI(), true, true);
                                    // TODO: clean up logging
                                    //logger.fine("configure", "calling port.addFeature " + feature);
                                    port.addFeature(feature);
                                } // end-if non optional wsa assertion found
                            } // next assertion
                        } // next alternative
                    } // end-if policy contains wsa assertion
                } // end foreach port
            } //end foreach addr assertion
        } // end foreach service
        logger.exiting("configure");
    }
}
