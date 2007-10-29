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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyAssertionTest extends AbstractPolicyApiClassTestBase {

    private static final QName attribName = new QName("http://foo.com", "attribute");
    private static final String attribValue = "avalue";
    private static final QName assertionName = new QName("http://foo.com", "assertion");
    private static final AssertionData data = AssertionData.createAssertionData(assertionName);

    private Map<QName, String> attributes;
    
    private PolicyAssertion a1;
    private PolicyAssertion a2;
    private PolicyAssertion a3;
    private PolicyAssertion a4;
    private PolicyAssertion a5;
    private PolicyAssertion assertionWithAttributes;

    public PolicyAssertionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        a1 = new PolicyAssertion() {
        };
        a2 = new PolicyAssertion(null, null) {
        };
        a3 = new PolicyAssertion(data, null) {
        };
        a4 = new PolicyAssertion(data, new ArrayList<PolicyAssertion>(0)) {
        };
        a5 = new PolicyAssertion(data, Arrays.asList(new PolicyAssertion() {
        })) {
        };
        attributes = new HashMap<QName, String>();
        attributes.put(attribName, attribValue);
        assertionWithAttributes = new PolicyAssertion(AssertionData.createAssertionData(assertionName, "test", attributes), null) {
        };
    }

    @Override
    protected void tearDown() throws Exception {
    }

    protected PolicyAssertion[][] getEqualInstanceRows() throws Exception {
        Collection<PolicyAssertion[]> rows = new LinkedList<PolicyAssertion[]>();

        for (String name : PolicyResourceLoader.SINGLE_ALTERNATIVE_POLICY) {
            Iterator<AssertionSet> setsA = PolicyResourceLoader.loadPolicy(name).iterator();
            Iterator<AssertionSet> setsB = PolicyResourceLoader.loadPolicy(name).iterator();

            if (setsA.hasNext()) {
                AssertionSet setA = setsA.next();
                AssertionSet setB = setsB.next();

                Iterator<PolicyAssertion> assertionsA = setA.iterator();
                Iterator<PolicyAssertion> assertionsB = setB.iterator();

                while (assertionsA.hasNext()) {
                    rows.add(new PolicyAssertion[]{assertionsA.next(), assertionsB.next()});
                }
            }
        }

        return rows.toArray(new PolicyAssertion[rows.size()][]);
    }

    public void testGetAttributesValueReturnsProperValue() throws Exception {
        QName headerParameterName = new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "Header");
        QName nameAttributeName = new QName("Name");
        QName namespaceAttributeName = new QName("Namespace");

        Policy policy = PolicyResourceLoader.loadPolicy("bug_reproduction/securityPolicy1.xml");
        AssertionSet alternative = policy.iterator().next();
        PolicyAssertion signedParts = alternative.get(new QName("http://schemas.xmlsoap.org/ws/2005/07/securitypolicy", "SignedParts")).iterator().next();
        Iterator<PolicyAssertion> iterator = signedParts.getParametersIterator();
        while (iterator.hasNext()) {
            PolicyAssertion assertion = iterator.next();
            if (assertion.getName().equals(headerParameterName)) {
                // System.out.println(assertion.toString());
                String nameValue = assertion.getAttributeValue(nameAttributeName);
                String namespaceValue = assertion.getAttributeValue(namespaceAttributeName);
                // System.out.println();
                // System.out.println("Name value: '" + nameValue + "'");
                // System.out.println("Namespace value: '" + namespaceValue + "'");
                // System.out.println("==========================================");
                assertNotNull("'Name' attribute of 'Header' parameter is expected to be not null.", nameValue);
                assertNotNull("'Namespace' attribute of 'Header' parameter is expected to be not null.", namespaceValue);
            }
        }
    }

    public void testGetName() {
        assertNull(a1.getName());
        assertNull(a2.getName());
        assertEquals(assertionName, a3.getName());
        assertEquals("Name equality check", assertionName, a4.getName());
        assertEquals("Name equality check", assertionName, a5.getName());
    }

    public void testHasParameters() {
        assertFalse(a1.hasParameters());
        assertFalse(a2.hasParameters());
        assertFalse(a3.hasParameters());
        assertFalse("Empty parameter list should result in no parameters", a4.hasParameters());
        assertTrue("Non-empty parameter list should result in existing parameters", a5.hasParameters());
    }

    public void testGetValue() {
        assertNull(a1.getValue());
        assertNull(a2.getValue());
        assertNull(a3.getValue());
        assertNull(a4.getValue());
        assertNull(a5.getValue());

        String expValue = "test";
        PolicyAssertion assertionWithValue = new PolicyAssertion(AssertionData.createAssertionData(assertionName, expValue, null), null) {
        };
        assertEquals(assertionWithValue.getValue(), expValue);
    }

    public void testGetAttributeSet() {
        assertTrue(a1.getAttributesSet().isEmpty());
        assertTrue(a2.getAttributesSet().isEmpty());
        assertTrue(a3.getAttributesSet().isEmpty());
        assertTrue(a4.getAttributesSet().isEmpty());
        assertTrue(a5.getAttributesSet().isEmpty());

        assertFalse(assertionWithAttributes.getAttributesSet().isEmpty());
    }

    public void testGetAttributes() {
        assertTrue(a1.getAttributes().isEmpty());
        assertTrue(a2.getAttributes().isEmpty());
        assertTrue(a3.getAttributes().isEmpty());
        assertTrue(a4.getAttributes().isEmpty());
        assertTrue(a5.getAttributes().isEmpty());

        assertFalse(assertionWithAttributes.getAttributes().isEmpty());
    }

    public void testGetAttributeValue() {
        assertNull(a1.getAttributeValue(attribName));
        assertNull(a2.getAttributeValue(attribName));
        assertNull(a3.getAttributeValue(attribName));
        assertNull(a4.getAttributeValue(attribName));
        assertNull(a5.getAttributeValue(attribName));

        assertEquals(attribValue, assertionWithAttributes.getAttributeValue(attribName));
    }

    public void testIsOptional() {
        assertFalse(a1.isOptional());
        assertFalse(a2.isOptional());
        assertFalse(a3.isOptional());
        assertFalse(a4.isOptional());
        assertFalse(a5.isOptional());

        final AssertionData assertionData = AssertionData.createAssertionData(assertionName, "test", attributes);
        assertionData.setOptionalAttribute(true);
        PolicyAssertion assertion = new PolicyAssertion(assertionData, null) {
        };
        assertTrue(assertion.isOptional());
    }

    public void testIsIgnorable() {
        assertFalse(a1.isIgnorable());
        assertFalse(a2.isIgnorable());
        assertFalse(a3.isIgnorable());
        assertFalse(a4.isIgnorable());
        assertFalse(a5.isIgnorable());

        final AssertionData assertionData = AssertionData.createAssertionData(assertionName, "test", attributes);
        assertionData.setIgnorableAttribute(true);
        PolicyAssertion assertion = new PolicyAssertion(assertionData, null) {
        };
        assertTrue(assertion.isIgnorable());
    }

    public void testIsPrivate() {
        assertFalse(a1.isPrivate());
        assertFalse(a2.isPrivate());
        assertFalse(a3.isPrivate());
        assertFalse(a4.isPrivate());
        assertFalse(a5.isPrivate());

        attributes.put(PolicyConstants.VISIBILITY_ATTRIBUTE, PolicyConstants.VISIBILITY_VALUE_PRIVATE);
        PolicyAssertion assertion = new PolicyAssertion(AssertionData.createAssertionData(assertionName, "test", attributes), null) {
        };
        assertTrue(assertion.isPrivate());
    }
}