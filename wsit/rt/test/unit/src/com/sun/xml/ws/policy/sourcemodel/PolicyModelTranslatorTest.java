/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar
 */
public class PolicyModelTranslatorTest extends TestCase {
    private static final String COMPACT_MODEL_SUFFIX = ".xml";
    private static final String NORMALIZED_MODEL_SUFFIX = "_normalized.xml";
    private static final Map<String, Integer> COMPLEX_POLICIES;
    
    static {
        Map<String, Integer> tempMap = new HashMap<String, Integer>();
        tempMap.put("complex_policy/nested_assertions_with_alternatives", 3);
        tempMap.put("complex_policy/single_choice1", 3);
        tempMap.put("complex_policy/single_choice2", 5);
        tempMap.put("complex_policy/nested_choice1", 3);
        tempMap.put("complex_policy/nested_choice2", 3);
        tempMap.put("complex_policy/assertion_parameters1", 1);
        COMPLEX_POLICIES = Collections.unmodifiableMap(tempMap);
    }
    
    private PolicyModelTranslator translator;
    
    public PolicyModelTranslatorTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        translator = PolicyModelTranslator.getTranslator();
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of getTranslator method, of class com.sun.xml.ws.policy.PolicyModelTranslator.
     */
    public void testGetTranslator() throws Exception {
        PolicyModelTranslator result = PolicyModelTranslator.getTranslator();
        assertNotNull(result);
    }
    
    /**
     * Test of translate method, of class com.sun.xml.ws.policy.PolicyModelTranslator.
     */
    public void testTranslateComplexPoliciesWithMultipleNestedPolicyAlternatives() throws Exception {
        int index = 0;
        for (Map.Entry<String, Integer> entry : COMPLEX_POLICIES.entrySet()) {
            String compactResourceName = entry.getKey() + COMPACT_MODEL_SUFFIX;
            String normalizedResouceName = entry.getKey() + NORMALIZED_MODEL_SUFFIX;
            int expectedNumberOfAlternatives = entry.getValue();
            
            PolicySourceModel compactModel = PolicyResourceLoader.unmarshallModel(compactResourceName);
            PolicySourceModel normalizedModel = PolicyResourceLoader.unmarshallModel(normalizedResouceName);

//            System.out.println(index + ". compact model: " + compactModel);
//            System.out.println("===========================================================");
//            System.out.println(index + ". normalized model: " + normalizedModel);
//            System.out.println("===========================================================");
            
            Policy compactModelPolicy = translator.translate(compactModel);
            Policy normalizedModelPolicy = translator.translate(normalizedModel);
            
            assertEquals("Normalized and compact model policy instances should contain equal number of alternatives", normalizedModelPolicy.getNumberOfAssertionSets(), compactModelPolicy.getNumberOfAssertionSets());
            assertEquals("This policy should contain '" + expectedNumberOfAlternatives + "' alternatives", expectedNumberOfAlternatives, compactModelPolicy.getNumberOfAssertionSets());
            assertEquals("Normalized and compact policy expression should form equal Policy instances", normalizedModelPolicy, compactModelPolicy);
            
//            System.out.println(index + ". compact model policy: " + compactModelPolicy);
//            System.out.println("===========================================================");
//            System.out.println(index + ". normalized model policy: " + normalizedModelPolicy);
            
            index++;
        }        
    }
    
    public void testPreserveOriginalNamespaceInformation() throws Exception {
        Policy policy = translator.translate(PolicyResourceLoader.unmarshallModel("namespaces/policy-v1.2.xml"));
        assertEquals("Namespace does not match original", NamespaceVersion.v1_2, policy.getNamespaceVersion());
        policy = translator.translate(PolicyResourceLoader.unmarshallModel("namespaces/policy-v1.5.xml"));
        assertEquals("Namespace does not match original", NamespaceVersion.v1_5, policy.getNamespaceVersion());
    }            
}
