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
/*
 * SymmetricBindingTest.java
 * JUnit based test
 *
 * Created on August 24, 2006, 12:27 AM
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelUnmarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.security.policy.AlgorithmSuiteValue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import junit.framework.*;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.MessageLayout;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author Mayank.Mishra@SUN.com
 */
public class SymmetricBindingTest extends TestCase {
    
    public SymmetricBindingTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SymmetricBindingTest.class);
        
        return suite;
    }
    
    private PolicySourceModel unmarshalPolicyResource(String resource) throws PolicyException, IOException {
        Reader reader = getResourceReader(resource);
        PolicySourceModel model = PolicyModelUnmarshaller.getXmlUnmarshaller().unmarshalModel(reader);
        reader.close();
        return model;
    }
    
    private Reader getResourceReader(String resourceName) {
        return new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
    }
    
    public Policy unmarshalPolicy(String xmlFile)throws Exception{
        PolicySourceModel model =  unmarshalPolicyResource(
                xmlFile);
        Policy mbp = PolicyModelTranslator.getTranslator().translate(model);
        return mbp;
        
    }
    
    public void testSymmerticBinding1() throws Exception {
        String fileName="security/SymmetricBindingAssertion1.xml";
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion", "SymmetricBinding",assertion.getName().getLocalPart());
                SymmetricBinding sb = (SymmetricBinding)assertion;
                
                //  System.out.println((sb.getProtectionToken().getIncludeToken()));
                assertTrue(sb.getTokenProtection());
                
                AlgorithmSuite aSuite = sb.getAlgorithmSuite();
                assertEquals("Unmatched Algorithm",aSuite.getEncryptionAlgorithm(), AlgorithmSuiteValue.Basic256.getEncAlgorithm());
                
                assertTrue(sb.isIncludeTimeStamp());
                
                assertFalse("Signature is Encrypted", sb.getSignatureProtection());
                
                assertFalse(sb.isSignContent());
            }
        }
    }
    
    
    public void testSymmerticBinding2() throws Exception {
        String fileName="security/SymmetricBindingAssertion2.xml";
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion", "SymmetricBinding",assertion.getName().getLocalPart());
                SymmetricBinding sb = (SymmetricBinding)assertion;
                
                X509Token tkn1 = (X509Token)sb.getSignatureToken();
                assertTrue(tkn1.getTokenType().equals(com.sun.xml.ws.security.impl.policy.X509Token.WSSX509V1TOKEN10));
                
                X509Token tkn2 = (X509Token)sb.getEncryptionToken();
                assertTrue(tkn2.getTokenType().equals(com.sun.xml.ws.security.impl.policy.X509Token.WSSX509V3TOKEN10));
                
                AlgorithmSuite aSuite = sb.getAlgorithmSuite();
                assertEquals("Unmatched Algorithm",aSuite.getEncryptionAlgorithm(), AlgorithmSuiteValue.TripleDesRsa15.getEncAlgorithm());
                
                assertTrue(sb.isIncludeTimeStamp());
                
                assertTrue("Signature is not Encypted", sb.getSignatureProtection());
                
                assertFalse("Tokens are protected", sb.getTokenProtection());
            }
        }
    }
    
    
    public void testSymmetricIssuedTokenCR6419493() throws Exception {
        String fileName="security/IssuedTokenCR.xml";
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion", "SymmetricBinding",assertion.getName().getLocalPart());
                SymmetricBinding sb = (SymmetricBinding)assertion;
                
                IssuedToken tkn1 = (IssuedToken)sb.getProtectionToken();
                assertTrue(tkn1.getIncludeToken().equals(Token.INCLUDE_ALWAYS));
            }
        }
    }
    
    
//    /**
//     * Test of getEncryptionToken method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetEncryptionToken() {
//        System.out.println("getEncryptionToken");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        Token expResult = null;
//        Token result = instance.getEncryptionToken();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSignatureToken method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetSignatureToken() {
//        System.out.println("getSignatureToken");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        Token expResult = null;
//        Token result = instance.getSignatureToken();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProtectionToken method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetProtectionToken() {
//        System.out.println("getProtectionToken");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        Token expResult = null;
//        Token result = instance.getProtectionToken();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setAlgorithmSuite method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetAlgorithmSuite() {
//        System.out.println("setAlgorithmSuite");
//
//        AlgorithmSuite algSuite = null;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setAlgorithmSuite(algSuite);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAlgorithmSuite method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetAlgorithmSuite() {
//        System.out.println("getAlgorithmSuite");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        AlgorithmSuite expResult = null;
//        AlgorithmSuite result = instance.getAlgorithmSuite();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of includeTimeStamp method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testIncludeTimeStamp() {
//        System.out.println("includeTimeStamp");
//
//        boolean value = true;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.includeTimeStamp(value);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isIncludeTimeStamp method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testIsIncludeTimeStamp() {
//        System.out.println("isIncludeTimeStamp");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        boolean expResult = true;
//        boolean result = instance.isIncludeTimeStamp();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setLayout method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetLayout() {
//        System.out.println("setLayout");
//
//        MessageLayout layout = null;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setLayout(layout);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLayout method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetLayout() {
//        System.out.println("getLayout");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        MessageLayout expResult = null;
//        MessageLayout result = instance.getLayout();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setEncryptionToken method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetEncryptionToken() {
//        System.out.println("setEncryptionToken");
//
//        Token token = null;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setEncryptionToken(token);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSignatureToken method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetSignatureToken() {
//        System.out.println("setSignatureToken");
//
//        Token token = null;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setSignatureToken(token);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setProtectionToken method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetProtectionToken() {
//        System.out.println("setProtectionToken");
//
//        Token token = null;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setProtectionToken(token);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isSignContent method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testIsSignContent() {
//        System.out.println("isSignContent");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        boolean expResult = true;
//        boolean result = instance.isSignContent();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSignContent method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetSignContent() {
//        System.out.println("setSignContent");
//
//        boolean contentOnly = true;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setSignContent(contentOnly);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setProtectionOrder method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetProtectionOrder() {
//        System.out.println("setProtectionOrder");
//
//        String order = "";
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setProtectionOrder(order);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getProtectionOrder method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetProtectionOrder() {
//        System.out.println("getProtectionOrder");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        String expResult = "";
//        String result = instance.getProtectionOrder();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTokenProtection method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetTokenProtection() {
//        System.out.println("setTokenProtection");
//
//        boolean value = true;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setTokenProtection(value);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSignatureProtection method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testSetSignatureProtection() {
//        System.out.println("setSignatureProtection");
//
//        boolean value = true;
//        SymmetricBinding instance = new SymmetricBinding();
//
//        instance.setSignatureProtection(value);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTokenProtection method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetTokenProtection() {
//        System.out.println("getTokenProtection");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        boolean expResult = true;
//        boolean result = instance.getTokenProtection();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSignatureProtection method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testGetSignatureProtection() {
//        System.out.println("getSignatureProtection");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        boolean expResult = true;
//        boolean result = instance.getSignatureProtection();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validate method, of class com.sun.xml.ws.security.impl.policy.SymmetricBinding.
//     */
//    public void testValidate() {
//        System.out.println("validate");
//
//        SymmetricBinding instance = new SymmetricBinding();
//
//        boolean expResult = true;
//        boolean result = instance.validate();
//        assertEquals(expResult, result);
//
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
}
