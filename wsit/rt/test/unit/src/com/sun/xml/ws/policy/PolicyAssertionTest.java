/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyAssertionTest extends TestCase {

    private static final QName attribName = new QName("http://foo.com", "attribute");
    private static final String attribValue = "avalue";
    private static final QName assertionName = new QName("http://foo.com", "assertion");
    private static final AssertionData data = AssertionData.createAssertionData(assertionName);

    private Map<QName, String> attributes;

    public PolicyAssertionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        attributes = new HashMap<QName, String>();
        attributes.put(attribName, attribValue);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testIsOptionalInIssue723() throws Exception {
        PolicyMap map = PolicyResourceLoader.getPolicyMap("bug_reproduction/SimpleService.wsdl");
        PolicyMapKey operationScopeKey = PolicyMap.createWsdlOperationScopeKey(
                new QName("http://tempuri.org/", "SimpleService"), 
                new QName("http://tempuri.org/", "SimpleServiceBinding"),
                new QName("http://tempuri.org/", "init"));

        Policy policy = map.getOperationEffectivePolicy(operationScopeKey);
        assertEquals("There should be only one alternative in this policy.", 1, policy.getNumberOfAssertionSets());

        QName atAssertionName = new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "ATAssertion");
        AssertionSet alternative = policy.iterator().next();
        boolean atAssertionFound = false;
        for (PolicyAssertion assertion : alternative) {
            if (atAssertionName.equals(assertion.getName())) {
                assertTrue("ATAssertion is supposed to be optional.", assertion.isOptional());
                atAssertionFound = true;
            }
        }

        assertTrue("ATAssertion should be available in the policy.", atAssertionFound);
    }
}
