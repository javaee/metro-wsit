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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.Policy;
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
    
    protected void setUp() throws Exception {
        translator = PolicyModelTranslator.getTranslator();
    }
    
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
}
