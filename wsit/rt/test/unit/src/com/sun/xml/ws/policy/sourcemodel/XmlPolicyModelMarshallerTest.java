/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.TestCase;

public class XmlPolicyModelMarshallerTest extends TestCase {
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(false);
    private PolicyModelUnmarshaller unmarshaller = PolicyModelUnmarshaller.getXmlUnmarshaller();
    private XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
    
    public XmlPolicyModelMarshallerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
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
                
        for (String modelFileName : modelFileNames) {
            PolicySourceModel model = PolicyResourceLoader.unmarshallModel("visibility/" + modelFileName + ".xml");
            PolicySourceModel resultModel = marshalAndUnmarshalModel(model);        
            PolicySourceModel expectedModel = PolicyResourceLoader.unmarshallModel("visibility/" + modelFileName + "_expected.xml");            
            assertEquals(modelFileName, expectedModel, resultModel);            
        }
    }

    public void testMarshallModelWithProperPolicyNamespaceVersion() throws Exception {
        PolicySourceModel model = marshalAndUnmarshalModel(PolicyResourceLoader.unmarshallModel("namespaces/policy-v1.2.xml"));
        assertEquals("Namespace version does not match after marshalling.", NamespaceVersion.v1_2, model.getNamespaceVersion());

        model = marshalAndUnmarshalModel(PolicyResourceLoader.unmarshallModel("namespaces/policy-v1.5.xml"));
        assertEquals("Namespace version does not match after marshalling.", NamespaceVersion.v1_5, model.getNamespaceVersion());
    }

    private PolicySourceModel marshalAndUnmarshalModel(PolicySourceModel model) throws PolicyException, XMLStreamException {
        StringWriter writer = new StringWriter();
        XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(writer);
        marshaller.marshal(model, streamWriter);

        StringReader reader = new StringReader(writer.toString());
        PolicySourceModel resultModel = unmarshaller.unmarshalModel(reader);
        return resultModel;
    }
}
