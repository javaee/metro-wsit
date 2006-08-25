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
 * EndorsingSupportingTokensTest.java
 * JUnit based test
 *
 * Created on August 24, 2006, 12:26 AM
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
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author Mayank.Mishra@SUN.com
 */
public class EndorsingSupportingTokensTest extends TestCase {
    
    public EndorsingSupportingTokensTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(EndorsingSupportingTokensTest.class);
        
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
        public boolean isHeaderPresent(QName expected , Iterator headers){
        while(headers.hasNext()){
            Header header = (Header) headers.next();
            if(expected.getLocalPart().equals(header.getLocalName())){
                if(expected.getNamespaceURI().equals(header.getURI())){
                    return true;
                }
            }
        }
        return false;
    }
    
        public void testEndorsingSupportingToken() throws Exception {
        String fileName="security/EndorsingSupportingToken.xml";
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion", "EndorsingSupportingTokens",assertion.getName().getLocalPart());
                EndorsingSupportingTokens est = (EndorsingSupportingTokens)assertion;
                
                AlgorithmSuite aSuite = (AlgorithmSuite) est.getAlgorithmSuite();
                assertEquals("Unmatched Algorithm",aSuite.getEncryptionAlgorithm(), AlgorithmSuiteValue.TripleDesRsa15.getEncAlgorithm());
                
                Iterator itrest = est.getTokens();
                if(itrest.hasNext()) {
                    assertTrue(X509Token.WSSX509V3TOKEN10.equals(((X509Token)itrest.next()).getTokenType()));
                }
                
                Iterator itrTkn = est.getSignedParts();
                if(itrTkn.hasNext()) {
                    SignedParts sp = ((SignedParts)itrTkn.next());
                    
                    assertEquals("Body should be present",true,sp.hasBody());
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","To"),sp.getHeaders()));
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","From"),sp.getHeaders()));
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","FaultTo"),sp.getHeaders()));
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","ReplyTo"),sp.getHeaders()));
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","MessageID"),sp.getHeaders()));
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","RelatesTo"),sp.getHeaders()));
                    assertTrue(isHeaderPresent(new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing","Action"),sp.getHeaders()));
                    
                }
            }
        }
    }
}
