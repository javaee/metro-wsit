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
 * SignedElementsTest.java
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import junit.framework.*;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author Mayank.Mishra@SUN.com
 */
public class SignedElementsTest extends TestCase {
    
    public SignedElementsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SignedElementsTest.class);
        
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
    
    public void testSignElements1()throws Exception{
        String fileName = "security/SignElements1.xml";
        Policy policy = unmarshalPolicy(fileName);
        assertNotNull(policy);
        Iterator <AssertionSet> itr  = policy.iterator();
        if(itr.hasNext()){
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as){
                assertEquals("Invalid assertion","SignedElements",assertion.getName().getLocalPart());
                SignedElements se = (SignedElements)assertion;
                assertTrue(hasXPathTarget("//soapEnv:Body",se.getTargets()));
                assertTrue(hasXPathTarget("//addr:To",se.getTargets()));
                assertTrue(hasXPathTarget("//addr:From",se.getTargets()));
                assertTrue(hasXPathTarget("//addr:RealtesTo",se.getTargets()));
                
                //   assertFalse("Headers should not be present",se.getHeaders().hasNext());
            }
        }else{
            throw new Exception("No Assertions found!. Unmarshalling of "+fileName+"failed!");
        }
    }
    
    
    public void testSignElements2()throws Exception{
        String fileName = "security/SignElements2.xml";
        Policy policy = unmarshalPolicy(fileName);
        assertNotNull(policy);
        Iterator <AssertionSet> itr  = policy.iterator();
        if(itr.hasNext()){
            AssertionSet as = itr.next();
            for(PolicyAssertion assertion : as){
                assertEquals("Invalid assertion","SignedElements",assertion.getName().getLocalPart());
                SignedElements se = (SignedElements)assertion;
                assertFalse(se.getTargets().hasNext());
                
                //   assertFalse("Headers should not be present",se.getHeaders().hasNext());
            }
        }else{
            throw new Exception("No Assertions found!. Unmarshalling of "+fileName+"failed!");
        }
    }
    
}
