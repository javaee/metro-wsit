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
import junit.framework.*;
import java.util.ArrayList;
import javax.xml.namespace.QName;

public class PolicyModelGeneratorTest extends TestCase {
    PolicyModelGenerator instance;
    
    
    public PolicyModelGeneratorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        instance = PolicyModelGenerator.getGenerator();
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of getTranslator method, of class com.sun.xml.ws.policy.PolicyModelTranslator.
     */
    public void testGetTranslator() throws Exception {
        PolicyModelGenerator result = PolicyModelGenerator.getGenerator();
        assertNotNull(result);
    }
    
    /**
     * Test of translate method, of class com.sun.xml.ws.policy.PolicyModelTranslator.
     */
    public void testTranslate() throws Exception {
        Policy policy = null;
        
        PolicySourceModel result = instance.translate(policy);
        assertNull(result);
        
        AssertionData data1 = new AssertionData(new QName("testns", "assertion1"), ModelNode.Type.ASSERTION);
        PolicyAssertion assertion1 = new WSPolicyAssertion(data1);
        ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>();
        assertions.add(assertion1);
        AssertionSet set1 = AssertionSet.createAssertionSet(assertions);
        ArrayList<AssertionSet> sets = new ArrayList<AssertionSet>();
        sets.add(set1);
        Policy policy1 = Policy.createPolicy(sets);
        
        PolicySourceModel expResult = null;
        result = instance.translate(policy1);
        
        result.toString();
        //System.out.println(result.toString());
    }
    
    
    class WSPolicyAssertion extends PolicyAssertion {
                
        public WSPolicyAssertion(AssertionData assertionData) {
            super(assertionData, null, null);
        }
    }
}
