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

import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.IOException;
import junit.framework.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PolicyIntersectorTest extends TestCase {

    PolicyIntersector strictIntersector = PolicyIntersector.createStrictPolicyIntersector();
    PolicyIntersector laxIntersector = PolicyIntersector.createLaxPolicyIntersector();

    public PolicyIntersectorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testPolicyIntersectorFactoryMethodsDoNotReturnNull() throws Exception {
        assertNotNull(PolicyIntersector.createStrictPolicyIntersector());
        assertNotNull(PolicyIntersector.createLaxPolicyIntersector());
    }

    public void testStrictIntersectEmptyPolicyCollectionThrowsIAE() throws Exception {
        try {
            Policy result = strictIntersector.intersect();
            fail("IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        // ok
        }
    }

    public void testStrictIntersectSinglePolicyInCollectionReturnsTheSameSinglePolicyInstance() throws Exception {
        Policy expected = Policy.createEmptyPolicy("fake", null);
        Policy result = strictIntersector.intersect(expected);

        assertTrue("Intersecting collection with single policy instance should return the very same policy instance", expected == result);
    }

    public void testStrictIntersectNullPolicyInCollectionReturnsNullPolicy() throws Exception {
        Collection<AssertionSet> alternatives = Arrays.asList(
                new AssertionSet[]{
                AssertionSet.createAssertionSet(
                Arrays.asList(new PolicyAssertion[]{
                new SimpleAssertion(AssertionData.createAssertionData(new QName("A")), null) {
                }}))});
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createPolicy("fake", null, alternatives));
        }
        policies.add(Policy.createNullPolicy());

        Policy result = strictIntersector.intersect(policies.toArray(new Policy[policies.size()]));

        assertTrue("Intersecting collection with null policy instance should return null policy as a result", result.isNull());
    }

    public void testStrictIntersectEmptyPolicyInCollectionReturnsNullPolicy() throws Exception {
        Collection<AssertionSet> alternatives = Arrays.asList(
                new AssertionSet[]{
                AssertionSet.createAssertionSet(
                Arrays.asList(new PolicyAssertion[]{
                new SimpleAssertion(AssertionData.createAssertionData(new QName("A")), null) {
                }}))});
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createPolicy("fake", null, alternatives));
        }
        policies.add(Policy.createEmptyPolicy());

        Policy result = strictIntersector.intersect(policies.toArray(new Policy[policies.size()]));

        assertTrue("Intersecting collection with empty policy instance should return null policy as a result", result.isNull());
    }

    public void testStrictIntersectAllPolicyInCollectionEmptyReturnsEmptyPolicy() throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        for (int i = 0; i < 10; i++) {
            policies.add(Policy.createEmptyPolicy("fake" + i, null));
        }

        Policy result = strictIntersector.intersect(policies.toArray(new Policy[policies.size()]));

        assertTrue("Intersecting collection with all policy instances empty should return empty policy as a result", result.isEmpty());
    }

    public void testStrictIntersectSimplePolicies() throws Exception {
        Policy p1 = PolicyResourceLoader.loadPolicy("intersection/policy1.xml");
        Policy p2 = PolicyResourceLoader.loadPolicy("intersection/policy2.xml");
        Policy p3 = PolicyResourceLoader.loadPolicy("intersection/policy3.xml");

        Policy expectedResult = PolicyResourceLoader.loadPolicy("intersection/policy1-2-3.xml");
        Policy result = strictIntersector.intersect(p1, p2, p3);
        assertEquals("Intersection did not provide expected result.", expectedResult, result);
    }

    public void testStrictInteroperablePolicies() throws Exception {
        performInteroperabilityTests("strict", strictIntersector);
    }

    public void testLaxInteroperablePolicies() throws Exception {
        performInteroperabilityTests("lax", laxIntersector);
    }

    public void performInteroperabilityTests(String mode, PolicyIntersector intersector) throws PolicyException, IOException {
        Policy p4 = PolicyResourceLoader.loadPolicy("intersection/policy4.xml");
        Policy p5 = PolicyResourceLoader.loadPolicy("intersection/policy5.xml");
        Policy p6 = PolicyResourceLoader.loadPolicy("intersection/policy6.xml");
        Policy p7 = PolicyResourceLoader.loadPolicy("intersection/policy7.xml");

        String testId;

        testId = "4-7-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy" + testId + ".xml"),
                p4, p7);
        testId = "7-4-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy" + testId + ".xml"),
                p7, p4);

        testId = "5-7-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy" + testId + ".xml"),
                p5, p7);
        testId = "7-5-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy" + testId + ".xml"),
                p7, p5);

        testId = "6-7-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy" + testId + ".xml"),
                p6, p7);
        testId = "7-6-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy" + testId + ".xml"),
                p7, p6);

        testId = "7-7-" + mode;
        performSingleIntersectionTest(
                intersector,
                testId,
                PolicyResourceLoader.loadPolicy("intersection/policy7-7.xml"),
                p7, p7);
    }

    public void testIntersectNamespaces() throws Exception {
        performSingleIntersectNamespacesTest(strictIntersector);
        performSingleIntersectNamespacesTest(laxIntersector);        
    }

    private void performSingleIntersectionTest(PolicyIntersector intersector, String testId, Policy expectedResult, Policy... inputPolicies) {
        Policy result = intersector.intersect(inputPolicies);
        assertEquals("Intersection for test [" + testId + "] did not provide expected result.", expectedResult, result);
    }
        
    private void performSingleIntersectNamespacesTest(PolicyIntersector intersector) throws Exception {
        Collection<Policy> policies = new LinkedList<Policy>();
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.2.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.2.xml"));
        Policy result = intersector.intersect(policies.toArray(new Policy[] {}));
        assertEquals(
                "When intersecting policies with same original namespace, the namespace should be preserved during intersection operation", 
                NamespaceVersion.v1_2, 
                result.getNamespaceVersion()
                );        

        policies.clear();
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.5.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.5.xml"));
        result = intersector.intersect(policies.toArray(new Policy[] {}));
        assertEquals(
                "When intersecting policies with same original namespace, the namespace should be preserved during intersection operation", 
                NamespaceVersion.v1_5, 
                result.getNamespaceVersion()
                );        
    
        policies.clear();
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.2.xml"));
        policies.add(PolicyResourceLoader.loadPolicy("namespaces/policy-v1.5.xml"));
        result = intersector.intersect(policies.toArray(new Policy[] {}));
        assertEquals(
                "When intersecting policies with different original namespace, the latest namespace should be preserved during intersection operation", 
                NamespaceVersion.v1_5, 
                result.getNamespaceVersion()
                );            
    }   
}
