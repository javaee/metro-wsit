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

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import junit.framework.*;
import javax.xml.stream.XMLStreamWriter;

public class XmlPolicyModelMarshallerTest extends TestCase {
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller();
    private PolicyModelUnmarshaller unmarshaller = PolicyModelUnmarshaller.getXmlUnmarshaller();
    
    public XmlPolicyModelMarshallerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of marshal method, of class com.sun.xml.ws.policy.sourcemodel.XmlPolicyModelMarshaller.
     */
    public void testMarshal() throws Exception {
        PolicySourceModel model = null;
        Object storage = null;
        
        try {
            marshaller.marshal(model, storage);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        
        StringWriter writer = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
        storage = streamWriter;
        
        try {
            marshaller.marshal(model, storage);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        
        model = PolicyResourceLoader.unmarshallModel("complex_policy/nested_assertions_with_alternatives.xml");
        storage = null;
        
        try {
            marshaller.marshal(model, storage);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        
        storage = new Object();
        
        try {
            marshaller.marshal(model, storage);
            fail("Expected PolicyException");
        } catch (PolicyException e) {
        }
        
        storage = streamWriter;
        marshaller.marshal(model, storage);
        String policy = writer.toString();
        // Verifying that produced policy String is not empty
        assertTrue(policy.length() > 10);
    }
    
    public void testMarshallingAssertionsWithVisibilityAttribute() throws Exception {
        String[] modelFileNames = new String[]{
            "policy_0_visible",
            "policy_1_visible",
            "policy_2_visible"
        };
        
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        for (String modelFileName : modelFileNames) {
            PolicySourceModel model = PolicyResourceLoader.unmarshallModel("visibility/" + modelFileName + ".xml");
            StringWriter writer = new StringWriter();
            XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
            marshaller.marshal(model, streamWriter);
            
            StringReader reader = new StringReader(writer.toString());
            PolicySourceModel resultModel = unmarshaller.unmarshalModel(reader);        
            PolicySourceModel expectedModel = PolicyResourceLoader.unmarshallModel("visibility/" + modelFileName + "_expected.xml");            
            assertEquals(modelFileName, expectedModel, resultModel);            
            System.out.println(writer.toString());
        }
    }
    
}
