/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyTest extends TestCase {
    public PolicyTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
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
