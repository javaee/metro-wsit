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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar
 */
public class XmlPolicyModelUnmarshallerTest extends TestCase {
    private static final PolicyModelUnmarshaller xmlUnmarshaller = PolicyModelUnmarshaller.getXmlUnmarshaller();
    
    public XmlPolicyModelUnmarshallerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testUnmarshallSingleSimplePolicyModel() throws Exception {
        PolicySourceModel model = unmarshallModel("single_alternative_policy/policy5.xml");
        System.out.println("model = " + model.toString());
        System.out.println("\n===============================================================================\n");
    }
    
    public void testUnmarshallSingleComplexPolicyModel() throws Exception {
        PolicySourceModel model = unmarshallModel("complex_policy/nested_assertions_with_alternatives.xml");
        System.out.println("model = " + model.toString());
        System.out.println("\n===============================================================================\n");
    }
    
    public void testUnmarshallComplexPolicyModelWithAssertionParameters() throws Exception {
        PolicySourceModel model = unmarshallModel("complex_policy/assertion_parameters1.xml");
        System.out.println("model = " + model.toString());
        System.out.println("\n===============================================================================\n");
    }
    
    private PolicySourceModel unmarshallModel(String resource) throws Exception {
        Reader reader = PolicyResourceLoader.getResourceReader(resource);
        PolicySourceModel model = xmlUnmarshaller.unmarshalModel(reader);
        reader.close();
        return model;
    }    
}
