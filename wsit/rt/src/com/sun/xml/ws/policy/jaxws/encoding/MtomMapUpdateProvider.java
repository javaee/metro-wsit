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
import com.sun.xml.ws.policy.sourcemodel.ModelNode;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelGenerator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.MTOMFeature;

/**
 *
 * @author Jakub Podlesak (japod at sun.com)
 */
public class MtomMapUpdateProvider implements PolicyMapUpdateProvider{
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(MtomMapUpdateProvider.class);
    
    private static final QName mtomName = new QName("http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization", "OptimizedMimeSerialization");
    
    static class MtomAssertion extends PolicyAssertion {
        
        private static final AssertionData mtomData = new AssertionData(mtomName, ModelNode.Type.ASSERTION);
        
        MtomAssertion() {
            super(mtomData, null, null);
        }
    }
    
    /** Creates a new instance of MtomMapUpdateProvider */
    public MtomMapUpdateProvider() {
    }
    
    public void update(PolicyMapExtender policyMapMutator, PolicyMap policyMap, SEIModel model, WSBinding wsBinding) throws PolicyException {
        logger.entering("update");
        MTOMFeature mtomFeature =  (MTOMFeature)wsBinding.getFeature(MTOMFeature.ID);
        if (policyMap != null) {
            Collection<PolicySubject> subjects = policyMap.getPolicySubjects();
            PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);
            PolicyModelGenerator generator = PolicyModelGenerator.getGenerator();
            PolicyMerger merger = PolicyMerger.getMerger();
            boolean usingPolicy = false;
            for (PolicySubject subject : subjects) {
                Object wsdlSubject = subject.getSubject();
                if (wsdlSubject != null) {
                    Policy policy = subject.getEffectivePolicy(merger);
                    if (wsdlSubject instanceof WSDLBoundPortType) {
                        WSDLBoundPortType binding = (WSDLBoundPortType)wsdlSubject;
                        // TODO: ? should we make sure binding.getName().equals(model.getPort().getBinding().getName()) ???
                        if (mtomFeature != null) {
                            if (mtomFeature.isEnabled()) {
                                if (policy.contains(mtomName)) {
                                    // TODO: make sure the attached policy is in sync with mtomEnabled setting
                                } else { // policy does not contain mtom assertion yet
                                    PolicySubject mtomPolicySubject = new PolicySubject(wsdlSubject, createMtomPolicy());
                                    PolicyMapKey aKey = PolicyMap.createWsdlEndpointScopeKey(
                                                                    model.getPort().getOwner().getName(), model.getPortName());
                                    policyMapMutator.putEndpointSubject(aKey, mtomPolicySubject);
                                    logger.fine("update","a new mtom endpoint subject just added to policy map");
                                } // endif policy already contains an mtom assertion
                            } else { // mtom is disabled
                                // TODO: make sure there is no mtom assertion attached already or it disables mtom setting
                            } // endif mtom enabled
                        } // endif mtomFeature not null
                    } // endif wsdlSubject instanceof WSDLBoundPortType
                } // endif wsdlSubject not null
            } //endforall policy map subjects
        } // endif policy map not null
        logger.exiting("update");
    }
    
    private Policy createMtomPolicy() {
        ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>(1);
        assertions.add(new MtomAssertion());
        assertionSets.add(AssertionSet.createAssertionSet(assertions));
        return Policy.createPolicy(null, null, assertionSets);
    }
    
}
