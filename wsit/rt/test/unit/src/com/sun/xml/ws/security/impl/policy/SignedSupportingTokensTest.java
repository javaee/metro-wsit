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
 * SignedSupportingTokensTest.java
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
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author Mayank.Mishra@SUN.com
 */
public class SignedSupportingTokensTest extends TestCase {
    
    public SignedSupportingTokensTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SignedSupportingTokensTest.class);
        
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
    
    public boolean hasXPathTarget(String xpathExpr , Iterator itr){
        while(itr.hasNext()){
            if(xpathExpr.equals(itr.next())){
                return true;
            }
        }
        return false;
    }
    
    
    public void testSignedSupportingToken() throws Exception {
        String fileName="security/SignedSupportingTokenAssertion.xml";
        Policy policy = unmarshalPolicy(fileName);
        Iterator <AssertionSet> itr = policy.iterator();
        if(itr.hasNext()) {
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as) {
                assertEquals("Invalid assertion", "SignedSupportingTokens",assertion.getName().getLocalPart());
                SignedSupportingTokens sst = (SignedSupportingTokens)assertion;
                
                AlgorithmSuite aSuite = (AlgorithmSuite) sst.getAlgorithmSuite();
                assertEquals("Unmatched Algorithm",aSuite.getEncryptionAlgorithm(), AlgorithmSuiteValue.TripleDesRsa15.getEncAlgorithm());
                
                Iterator itrTkn = sst.getTokens();
                if(itrTkn.hasNext()) {
                    assertTrue(((com.sun.xml.ws.security.policy.UserNameToken)itrTkn.next()).getType().equals(com.sun.xml.ws.security.policy.UserNameToken.WSS_USERNAME_TOKEN_10));
                }
                Iterator itrSparts = sst.getSignedElements();
                if(itrSparts.hasNext()) {
                    SignedElements se = (SignedElements)itrSparts.next();
                    assertTrue(hasXPathTarget("//soapEnv:Body",se.getTargets()));
                    assertTrue(hasXPathTarget("//addr:To",se.getTargets()));
                    assertTrue(hasXPathTarget("//addr:From",se.getTargets()));
                    assertTrue(hasXPathTarget("//addr:RealtesTo",se.getTargets()));
                }
            }
        }
    }
    
    
}
