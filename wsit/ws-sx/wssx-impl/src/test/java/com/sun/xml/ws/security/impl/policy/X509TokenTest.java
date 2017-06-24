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

import com.sun.xml.ws.api.policy.ModelTranslator;
import com.sun.xml.ws.api.policy.ModelUnmarshaller;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import junit.framework.*;

/**
 *
 * @author Mayank.Mishra@SUN.com
 */
public class X509TokenTest extends TestCase {
    
    public X509TokenTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(X509TokenTest.class);
        
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
    
    public void testX509TokensAssertions_Types_8() throws Exception{
        testX509TokenAssertionsType("security/X509TokenAssertions1.xml", X509Token.WSSX509V1TOKEN10);
        testX509TokenAssertionsType("security/X509TokenAssertions2.xml", X509Token.WSSX509V3TOKEN10);
        testX509TokenAssertionsType("security/X509TokenAssertions3.xml", X509Token.WSSX509PKCS7TOKEN10);
        testX509TokenAssertionsType("security/X509TokenAssertions4.xml", X509Token.WSSX509PKIPATHV1TOKEN10);
        testX509TokenAssertionsType("security/X509TokenAssertions5.xml", X509Token.WSSX509V1TOKEN11);
        testX509TokenAssertionsType("security/X509TokenAssertions6.xml", X509Token.WSSX509V3TOKEN11);
        testX509TokenAssertionsType("security/X509TokenAssertions7.xml", X509Token.WSSX509PKCS7TOKEN11);
        testX509TokenAssertionsType("security/X509TokenAssertions8.xml", X509Token.WSSX509PKIPATHV1TOKEN11);
    }
    
    public void testX509TokensAssertions_Reference_4() throws Exception{
        testX509TokenAssertionsReference("security/X509TokenAssertions9.xml", X509Token.REQUIRE_KEY_IDENTIFIER_REFERENCE);
        testX509TokenAssertionsReference("security/X509TokenAssertions10.xml", X509Token.REQUIRE_ISSUER_SERIAL_REFERENCE);
        testX509TokenAssertionsReference("security/X509TokenAssertions11.xml", X509Token.REQUIRE_EMBEDDED_TOKEN_REFERENCE);
        testX509TokenAssertionsReference("security/X509TokenAssertions12.xml", X509Token.REQUIRE_THUMBPRINT_REFERENCE);
    }
    
    public void testX509TokenAssertionsType(String fileName, String tokenType) throws Exception{
        
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion","X509Token",assertion.getName().getLocalPart());
                X509Token xt = (X509Token)assertion;
                assertTrue(tokenType.equals(xt.getTokenType()));
            }
        } else {
            throw new Exception("No Assertions found!. Unmarshalling of "+fileName+" failed!");
        }
    }
    
    public void testX509TokenAssertionsReference(String fileName, String referenceType) throws Exception{
        
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion","X509Token",assertion.getName().getLocalPart());
                X509Token xt = (X509Token)assertion;
                Iterator itrref = xt.getTokenRefernceType().iterator();
                assertTrue(xt.getTokenRefernceType().contains(referenceType));
            }
        } else {
            throw new Exception("No Assertions found!. Unmarshalling of "+fileName+" failed!");
        }
    }
 
}
