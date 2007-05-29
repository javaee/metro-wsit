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

package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import junit.framework.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyIntersectorTest extends TestCase {
    PolicyIntersector intersector = PolicyIntersector.createPolicyIntersector();
    
    public PolicyIntersectorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testPolicyIntersectorFactoryMethodDoesNotReturnNull() throws Exception {
        assertNotNull(PolicyIntersector.createPolicyIntersector());
    }

    public void testIntersectNullPolicyCollectionThrowsIAE() throws Exception {
        Collection<Policy> policies = null;
        try {            
            Policy result = intersector.intersect(policies);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // ok
        }                
    }
    
    public void testIntersectEmptyPolicyCollectionThrowsIAE() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        try {            
            Policy result = intersector.intersect(policies);
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
            // ok
        }                
    }
    
    public void testIntersectSinglePolicyInCollectionReturnsTheSameSinglePolicyInstance() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        Policy expected = Policy.createEmptyPolicy("fake", null);
        policies.add(expected);
        Policy result = intersector.intersect(policies);
        
        assertTrue("Intersecting collection with single policy instance should return the very same policy instance", expected == result);
    }
    
    public void testIntersectNullPolicyInCollectionReturnsNullPolicy() throws Exception {
        Collection<AssertionSet> alternatives = Arrays.asList(new AssertionSet[] {AssertionSet.createAssertionSet(Arrays.asList(new PolicyAssertion[] {new PolicyAssertion(AssertionData.createAssertionData(new QName("A")), null, null){}}))});
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createPolicy("fake", null, alternatives));
        }
        policies.add(Policy.createNullPolicy());
        
        Policy result = intersector.intersect(policies);
        
        assertTrue("Intersecting collection with null policy instance should return null policy as a result", result.isNull());
    }
    
    public void testIntersectEmptyPolicyInCollectionReturnsNullPolicy() throws Exception {
        Collection<AssertionSet> alternatives = Arrays.asList(new AssertionSet[] {AssertionSet.createAssertionSet(Arrays.asList(new PolicyAssertion[] {new PolicyAssertion(AssertionData.createAssertionData(new QName("A")), null, null){}}))});
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createPolicy("fake", null, alternatives));
        }
        policies.add(Policy.createEmptyPolicy());
        
        Policy result = intersector.intersect(policies);
        
        assertTrue("Intersecting collection with empty policy instance should return null policy as a result", result.isNull());
    }
    
    public void testIntersectAllPolicyInCollectionEmptyReturnsEmptyPolicy() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createEmptyPolicy("fake" + i, null));
        }
        
        Policy result = intersector.intersect(policies);
        
        assertTrue("Intersecting collection with all policy instances empty should return empty policy as a result", result.isEmpty());
    }
    
    public void testIntersectSimplePolicies() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        
        policies.add(PolicyResourceLoader.loadPolicy("intersection/policy1.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("intersection/policy2.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("intersection/policy3.xml"));
        
        Policy expectedResult = PolicyResourceLoader.loadPolicy("intersection/intersection_1-2-3.xml");
        Policy result = intersector.intersect(policies);
        assertEquals("Intersection did not provide expected result.", expectedResult, result);
    }
    
}
