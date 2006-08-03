/*
 * PolicyIntersectorTest.java
 * JUnit based test
 *
 * Created on April 21, 2006, 11:55 AM
 */

package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import junit.framework.*;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
        Policy expected = Policy.createPolicy("fake", null);
        policies.add(expected);
        Policy result = intersector.intersect(policies);
        
        assertTrue("Intersecting collection with single policy instance should return the very same policy instance", expected == result);
    }
    
    public void testIntersectNullPolicyInCollectionReturnsNullPolicy() throws Exception {
        Collection<AssertionSet> alternatives = Arrays.asList(new AssertionSet[] {AssertionSet.createAssertionSet(Arrays.asList(new PolicyAssertion[] {new PolicyAssertion(){}}))});
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createPolicy("fake", null, alternatives));
        }
        policies.add(Policy.createNullPolicy());
        
        Policy result = intersector.intersect(policies);
        
        assertTrue("Intersecting collection with null policy instance should return null policy as a result", result.isNull());
    }
    
    public void testIntersectEmptyPolicyInCollectionReturnsNullPolicy() throws Exception {
        Collection<AssertionSet> alternatives = Arrays.asList(new AssertionSet[] {AssertionSet.createAssertionSet(Arrays.asList(new PolicyAssertion[] {new PolicyAssertion(){}}))});
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
