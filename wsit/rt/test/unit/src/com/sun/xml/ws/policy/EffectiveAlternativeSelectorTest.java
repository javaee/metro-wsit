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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import java.util.HashSet;
import javax.xml.namespace.QName;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static com.sun.xml.ws.policy.testutils.PolicyResourceLoader.loadPolicy;

/**
 *
 * @author japod
 */
public class EffectiveAlternativeSelectorTest extends TestCase {
    
    private static final PolicyModelUnmarshaller xmlUnmarshaller = PolicyModelUnmarshaller.getXmlUnmarshaller();
    private static final PolicyAssertionValidator[] selectors;
    static {
        selectors = PolicyUtils.ServiceProvider.load(PolicyAssertionValidator.class);
    }
    
    
    public EffectiveAlternativeSelectorTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EffectiveAlternativeSelectorTest.class);
        
        return suite;
    }
    
    /**
     * Test of doSelection method, of class com.sun.xml.ws.policy.EffectiveAlternativeSelector.
     */
    public void testDoPositiveSelection() throws Exception {
        HashSet<PolicyMapMutator> mutators = new HashSet<PolicyMapMutator>();
        EffectivePolicyModifier myModifier = EffectivePolicyModifier.createEffectivePolicyModifier();
        mutators.add(myModifier);
        PolicyMapExtender myExtender = PolicyMapExtender.createPolicyMapExtender();
        mutators.add(myExtender);
        PolicyMap policyMap = PolicyMap.createPolicyMap(mutators);
        
        //Policy pol1 = PolicyModelTranslator.getTranslator()
        //                .translate(unmarshalModel("single_alternative_policy/policy3.xml"));
        Policy pol2 = loadPolicy("complex_policy/nested_assertions_with_alternatives.xml");
        
        PolicyMapKey aKey = PolicyMap.createWsdlEndpointScopeKey(new QName("service"),new QName("port"));
        
        //myExtender.putEndpointSubject(aKey,new PolicySubject("one",pol1));
        myExtender.putEndpointSubject(aKey,new PolicySubject("two",pol2));
        
        //System.out.println(myExtender.getMap());
        
        if(2>myExtender.getMap().getEndpointEffectivePolicy(aKey).getNumberOfAssertionSets()) {
            fail("Insufficient number of alternatives found. At least 2 of them needed.");
        };
        
        EffectiveAlternativeSelector.doSelection(myModifier, selectors);
        
        if(1!=myExtender.getMap().getEndpointEffectivePolicy(aKey).getNumberOfAssertionSets()) {
            fail("Too many alternatives has left.");
        }
    }
    
    public void testDoNegativeSelection() throws Exception {
        HashSet<PolicyMapMutator> mutators = new HashSet<PolicyMapMutator>();
        EffectivePolicyModifier myModifier = EffectivePolicyModifier.createEffectivePolicyModifier();
        mutators.add(myModifier);
        PolicyMapExtender myExtender = PolicyMapExtender.createPolicyMapExtender();
        mutators.add(myExtender);
        PolicyMap policyMap = PolicyMap.createPolicyMap(mutators);
        
        //Policy pol1 = PolicyModelTranslator.getTranslator()
        //                .translate(unmarshalModel("single_alternative_policy/policy3.xml"));
        Policy pol2 = loadPolicy("complex_policy/nested_assertions_with_alternatives.xml");
        
        PolicyMapKey aKey = PolicyMap.createWsdlEndpointScopeKey(new QName("service"),new QName("port"));
        
        //myExtender.putEndpointSubject(aKey,new PolicySubject("one",pol1));
        myExtender.putEndpointSubject(aKey,new PolicySubject("two",pol2));
        
        //System.out.println(myExtender.getMap());
        
        if(2>myExtender.getMap().getEndpointEffectivePolicy(aKey).getNumberOfAssertionSets()) {
            fail("Insufficient number of alternatives found. At least 2 of them needed.");
        };
        
        EffectiveAlternativeSelector.doSelection(myModifier, selectors);
        
        if(1!=myExtender.getMap().getEndpointEffectivePolicy(aKey).getNumberOfAssertionSets()) {
            fail("Too many alternatives has left.");
        }
        
        EffectiveAlternativeSelector.doSelection(myModifier, selectors);
        
        if(1!=myExtender.getMap().getEndpointEffectivePolicy(aKey).getNumberOfAssertionSets()) {
            fail("Too many alternatives has left.");
        }
    }
    
    public void testDoEmptySelection() throws Exception {
        HashSet<PolicyMapMutator> mutators = new HashSet<PolicyMapMutator>();
        EffectivePolicyModifier modifier = EffectivePolicyModifier.createEffectivePolicyModifier();
        mutators.add(modifier);
        PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        mutators.add(extender);
        PolicyMap policyMap = PolicyMap.createPolicyMap(mutators);
        
        Policy emptyPolicy = Policy.createEmptyPolicy();
        PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(new QName("service"), new QName("port"));
        extender.putEndpointSubject(key, new PolicySubject("two", emptyPolicy));
        
        EffectiveAlternativeSelector.doSelection(modifier, selectors);
        
        assertTrue(extender.getMap().getEndpointEffectivePolicy(key).isEmpty());
    }
    
    public void testDoNullSelection() throws Exception {
        HashSet<PolicyMapMutator> mutators = new HashSet<PolicyMapMutator>();
        EffectivePolicyModifier modifier = EffectivePolicyModifier.createEffectivePolicyModifier();
        mutators.add(modifier);
        PolicyMapExtender extender = PolicyMapExtender.createPolicyMapExtender();
        mutators.add(extender);
        PolicyMap policyMap = PolicyMap.createPolicyMap(mutators);
        
        Policy nullPolicy = Policy.createNullPolicy();
        PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(new QName("service"), new QName("port"));
        extender.putEndpointSubject(key, new PolicySubject("two", nullPolicy));
        
        EffectiveAlternativeSelector.doSelection(modifier, selectors);
        
        assertTrue(extender.getMap().getEndpointEffectivePolicy(key).isNull());
    }
    
}
