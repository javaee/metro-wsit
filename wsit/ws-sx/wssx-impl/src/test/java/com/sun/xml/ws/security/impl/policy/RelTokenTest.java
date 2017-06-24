/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import junit.framework.*;

/**
 *
 * @author Mayank.Mishra@sun.com
 */
public class RelTokenTest extends TestCase {
    
    public RelTokenTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RelTokenTest.class);
        
        return suite;
    }
    
    private PolicySourceModel unmarshalPolicyResource(String resource) throws PolicyException, IOException {
        Reader reader = getResourceReader(resource);
        PolicySourceModel model = ModelUnmarshaller.getUnmarshaller().unmarshalModel(reader);
        reader.close();
        return model;
    }
    
    private Reader getResourceReader(String resourceName) {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
    }
    
    public Policy unmarshalPolicy(String xmlFile)throws Exception{
        PolicySourceModel model =  unmarshalPolicyResource(
                xmlFile);
        Policy mbp = ModelTranslator.getTranslator().translate(model);
        return mbp;
    }
    
    public void testRelToken1()throws Exception{
        testRelToken_Keys_Reference("security/RelToken1.xml", "");
        testRelToken_Keys_Reference("security/RelToken2.xml", "RequireKeyIdentifierReference");
    }
    
    public void testRelToken2() throws Exception{
        testRelTokenType("security/RelToken1.xml", com.sun.xml.ws.security.policy.RelToken.WSS_REL_V10_TOKEN10);
        testRelTokenType("security/RelToken2.xml", com.sun.xml.ws.security.policy.RelToken.WSS_REL_V20_TOKEN10);
        testRelTokenType("security/RelToken3.xml", com.sun.xml.ws.security.policy.RelToken.WSS_REL_V10_TOKEN11);
        testRelTokenType("security/RelToken4.xml", com.sun.xml.ws.security.policy.RelToken.WSS_REL_V20_TOKEN11);
    }
    
    public void testRelToken_Keys_Reference(String fileName, String param) throws Exception {
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion","RelToken",assertion.getName().getLocalPart());
                com.sun.xml.ws.security.impl.policy.RelToken rt=(com.sun.xml.ws.security.impl.policy.RelToken)assertion;
                if(param.equals(""))
                    assertTrue(rt.isRequireDerivedKeys());
                else {
                    Iterator itrRt = rt.getTokenRefernceType();
                    if(itrRt.hasNext()) {
                        assertTrue(((String)itrRt.next()).equals(com.sun.xml.ws.security.policy.RelToken.REQUIRE_KEY_IDENTIFIER_REFERENCE));
                    }
                }
            }
        } else {
            throw new Exception("No Assertions found!. Unmarshalling of "+fileName+" failed!");
        }
    }
    
    public void testRelTokenType(String fileName, String tokenType) throws Exception {
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion","RelToken",assertion.getName().getLocalPart());
                assertion = (PolicyAssertion)assertion;
                com.sun.xml.ws.security.impl.policy.RelToken rt = (com.sun.xml.ws.security.impl.policy.RelToken)assertion;
                assertTrue(rt.getTokenType().equals(tokenType));
            }
        } else {
            throw new Exception("No Assertions found!. Unmarshalling of "+fileName+" failed!");
        }
    }
    

}
