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

import com.sun.xml.ws.policy.*;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import junit.framework.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PolicyModelGeneratorTest extends TestCase {
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
        tempMap.put("complex_policy/assertion_parameters2", 1);
        COMPLEX_POLICIES = Collections.unmodifiableMap(tempMap);
    }
    
    private PolicyModelGenerator generator;
    private PolicyModelTranslator translator;
    
    public PolicyModelGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        translator = PolicyModelTranslator.getTranslator();
        generator = PolicyModelGenerator.getGenerator();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testGetTranslator() throws Exception {
        PolicyModelGenerator result = PolicyModelGenerator.getGenerator();
        assertNotNull(result);
    }
    
    public void testGenerate() throws Exception {
        for (Map.Entry<String, Integer> entry : COMPLEX_POLICIES.entrySet()) {
            String compactResourceName = entry.getKey() + COMPACT_MODEL_SUFFIX;
            String normalizedResouceName = entry.getKey() + NORMALIZED_MODEL_SUFFIX;
            int expectedNumberOfAlternatives = entry.getValue();
            
            PolicySourceModel compactModel = PolicyResourceLoader.unmarshallModel(compactResourceName);
            PolicySourceModel normalizedModel = PolicyResourceLoader.unmarshallModel(normalizedResouceName);
            Policy compactModelPolicy = translator.translate(compactModel);
            Policy normalizedModelPolicy = translator.translate(normalizedModel);
            
            PolicySourceModel generatedCompactModel = generator.translate(compactModelPolicy);
            PolicySourceModel generatedNormalizedModel = generator.translate(normalizedModelPolicy);

            Policy generatedCompactModelPolicy = translator.translate(generatedCompactModel);
            Policy generatedNormalizedModelPolicy = translator.translate(generatedNormalizedModel);
            
            assertEquals("Generated compact policy should contain '" + expectedNumberOfAlternatives + "' alternatives", expectedNumberOfAlternatives,generatedCompactModelPolicy.getNumberOfAssertionSets());
            assertEquals("Generated and translated compact model policy instances should contain equal number of alternatives", compactModelPolicy.getNumberOfAssertionSets(), generatedCompactModelPolicy.getNumberOfAssertionSets());
            assertEquals("Generated and translated compact policy expression should form equal Policy instances", compactModelPolicy, generatedCompactModelPolicy);

            assertEquals("Generated normalized policy should contain '" + expectedNumberOfAlternatives + "' alternatives", expectedNumberOfAlternatives, generatedNormalizedModelPolicy.getNumberOfAssertionSets());
            assertEquals("Generated and translated normalized model policy instances should contain equal number of alternatives", normalizedModelPolicy.getNumberOfAssertionSets(), generatedNormalizedModelPolicy.getNumberOfAssertionSets());
            assertEquals("Generated and translated normalized policy expression should form equal Policy instances", normalizedModelPolicy, generatedNormalizedModelPolicy);
            
            // TODO: somehow compare models, because now the test only checks if the translation does not end in some exception...
        }
    }
}
