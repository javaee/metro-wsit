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

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public class PolicyAssertionTest extends AbstractPolicyApiClassTestBase {    
    public PolicyAssertionTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
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
                    rows.add(new PolicyAssertion[] {assertionsA.next(), assertionsB.next()});
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
        Iterator<PolicyAssertion> iterator = signedParts.getNestedAssertionsIterator();
        while (iterator.hasNext()) {
            PolicyAssertion assertion = iterator.next();
            if (assertion.getName().equals(headerParameterName)) {
                System.out.println(assertion.toString());
                String nameValue = assertion.getAttributeValue(nameAttributeName);
                String namespaceValue = assertion.getAttributeValue(namespaceAttributeName);
                System.out.println();
                System.out.println("Name value: '" + nameValue + "'");
                System.out.println("Namespace value: '" + namespaceValue + "'");
                System.out.println("==========================================");
                assertNotNull("'Name' attribute of 'Header' parameter is expected to be not null.", nameValue);
                assertNotNull("'Namespace' attribute of 'Header' parameter is expected to be not null.", namespaceValue);
            }           
        }
        
    }
}
