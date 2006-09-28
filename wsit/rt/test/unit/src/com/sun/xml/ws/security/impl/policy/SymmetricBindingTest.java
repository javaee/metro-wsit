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
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl==null) {
            return new InputStreamReader(ClassLoader.getSystemResourceAsStream(resourceName));
        } else {
            return new InputStreamReader(cl.getResourceAsStream(resourceName));
        }
        
        
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
                assertEquals("Unmatched Algorithm",aSuite.getEncryptionAlgorithm(), AlgorithmSuiteValue.Basic128.getEncAlgorithm());
                
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
    
 
}
