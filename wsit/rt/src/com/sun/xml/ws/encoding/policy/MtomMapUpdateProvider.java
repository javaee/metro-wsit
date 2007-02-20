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

package com.sun.xml.ws.encoding.policy;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicyMerger;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.ws.soap.MTOMFeature;

import static com.sun.xml.ws.encoding.policy.EncodingConstants.OPTIMIZED_MIME_SERIALIZATION_ASSERTION;
import javax.xml.namespace.QName;

/**
 * Generate an MTOM policy if MTOM was enabled.
 *
 * @author Jakub Podlesak (japod at sun.com)
 */
public class MtomMapUpdateProvider implements PolicyMapUpdateProvider{
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(MtomMapUpdateProvider.class);
    
    static class MtomAssertion extends PolicyAssertion {
        
        private static final AssertionData mtomData = AssertionData.createAssertionData(OPTIMIZED_MIME_SERIALIZATION_ASSERTION);
        
        MtomAssertion() {
            super(mtomData, null, null);
        }
    }

    /**
     * Generates an MTOM policy if MTOM is enabled.
     *
     * <ol>
     * <li>If MTOM is enabled
     * <ol>
     * <li>If MTOM policy does not already exist, generate
     * <li>Otherwise do nothing
     * </ol>
     * <li>Otherwise, do nothing (that implies that we do not remove any MTOM policies if MTOM is disabled)
     * </ol>
     *
     */
    public void update(PolicyMapExtender policyMapMutator, PolicyMap policyMap, SEIModel model, WSBinding wsBinding) throws PolicyException {
        logger.entering("update", new Object[] {policyMapMutator, policyMap, model, wsBinding});

        if (policyMap != null) {
            final MTOMFeature mtomFeature = wsBinding.getFeature(MTOMFeature.class);
            logger.finest("update", "mtomFeature = " + mtomFeature);
            if ((mtomFeature != null) && mtomFeature.isEnabled()) {
                final PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(model.getServiceQName(), model.getPortName());
                final Policy existingPolicy = policyMap.getEndpointEffectivePolicy(endpointKey);
                if ((existingPolicy == null) || ! existingPolicy.contains(OPTIMIZED_MIME_SERIALIZATION_ASSERTION)) {
                    final QName bindingName = model.getBoundPortTypeName();
                    final Policy mtomPolicy = createMtomPolicy(bindingName);
                    PolicySubject mtomPolicySubject = new PolicySubject(bindingName, mtomPolicy);
                    PolicyMapKey aKey = PolicyMap.createWsdlEndpointScopeKey(model.getServiceQName(), model.getPortName());
                    policyMapMutator.putEndpointSubject(aKey, mtomPolicySubject);
                    logger.fine("update", "Added MTOM policy with ID \"" + mtomPolicy.getIdOrName() +
                                  "\" to binding element \"" + bindingName + "\"");
                }
                else {
                    logger.fine("update", "MTOM policy exists already, doing nothing");
                }
            }
        } // endif policy map not null
        
        logger.exiting("update");
    }
        

    /**
     * Create a policy with an MTOM assertion.
     *
     * @param model The binding element name. Used to generate a (locally) unique ID for the policy.
     * @return The policy.
     */
    private Policy createMtomPolicy(final QName bindingName) {
        ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>(1);
        assertions.add(new MtomAssertion());
        assertionSets.add(AssertionSet.createAssertionSet(assertions));
        return Policy.createPolicy(null, bindingName.getLocalPart() + "_MTOM_Policy", assertionSets);
    }
    
}
