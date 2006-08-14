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

import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyTest extends TestCase {
    public PolicyTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testEmptyPolicyReturnsTrueOnIsEmptyAndFalseOnIsNull() {
        Policy tested = Policy.createEmptyPolicy();
        assertTrue("Empty policy must return 'true' on isEmpty() call", tested.isEmpty());
        assertFalse("Empty policy must return 'false' on isNull() call", tested.isNull());
        
    }
    
    public void testNullPolicyReturnsFalseOnIsEmptyAndTrueOnIsNull() {
        Policy tested = Policy.createNullPolicy();
        assertFalse("Null policy must return 'false' on isEmpty() call", tested.isEmpty());
        assertTrue("Null policy must return 'true' on isNull() call", tested.isNull());
    }

    public void testNullPolicyFactoryMethodReturnsConstantObjectOnNullArguments () {
        Policy tested = Policy.createNullPolicy(null, null);
        Policy expected = Policy.createNullPolicy();
        
        assertTrue("The createNullPolicy(String, String) factory method should return the same instance as createNullPolicy()", tested == expected);
    }    

    public void testEmptyPolicyFactoryMethodReturnsConstantObjectOnNullArguments () {
        Policy tested = Policy.createEmptyPolicy(null, null);
        Policy expected = Policy.createEmptyPolicy();
        
        assertTrue("The createEmptyPolicy(String, String) factory method should return the same instance as createEmptyPolicy()", tested == expected);
    }    

    public void testNullPolicyFactoryMethodReturnsProperObjectOnNonNullArguments () {
        Policy tested = Policy.createNullPolicy("aaa", "bbb");
        
        assertEquals("The name is not initialized as expected", tested.getName(), "aaa");
        assertEquals("The ID is not initialized as expected", tested.getId(), "bbb");
    }    

    public void testEmptyPolicyFactoryMethodReturnsProperObjectOnNonNullArguments () {
        Policy tested = Policy.createEmptyPolicy("aaa", "bbb");
        
        assertEquals("The name is not initialized as expected", tested.getName(), "aaa");
        assertEquals("The ID is not initialized as expected", tested.getId(), "bbb");
    }    
}
