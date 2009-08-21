/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import static com.sun.xml.ws.policy.testutils.PolicyResourceLoader.loadPolicy;

import java.util.HashSet;
import javax.xml.namespace.QName;

import junit.framework.TestCase;


/**
 *
 * @author japod
 */
public class EffectiveAlternativeSelectorTest extends TestCase {
    
    private static final ModelUnmarshaller xmlUnmarshaller = ModelUnmarshaller.getUnmarshaller();
    
    public EffectiveAlternativeSelectorTest(String testName) {
        super(testName);
    }
       
    /**
     * Test of doSelection method, of class com.sun.xml.ws.policy.EffectiveAlternativeSelector.
     * @throws Exception
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
        }
        
        EffectiveAlternativeSelector.doSelection(myModifier);
        
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
        }
        
        EffectiveAlternativeSelector.doSelection(myModifier);
        
        if(1!=myExtender.getMap().getEndpointEffectivePolicy(aKey).getNumberOfAssertionSets()) {
            fail("Too many alternatives has left.");
        }
        
        EffectiveAlternativeSelector.doSelection(myModifier);
        
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
        
        EffectiveAlternativeSelector.doSelection(modifier);
        
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
        
        EffectiveAlternativeSelector.doSelection(modifier);
        
        assertTrue(extender.getMap().getEndpointEffectivePolicy(key).isNull());
    }
    
}
