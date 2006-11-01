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
 * FilteringXmlStreamWriterProxyTest.java
 * JUnit based test
 *
 * Created on October 4, 2006, 6:02 PM
 */

package com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter;

import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.*;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import junit.framework.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class PrivateAssertionFilteringXmlStreamWriterTest extends TestCase {
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);
    private String[] testResources = new String[] {
        "policy_0_visible",
        "policy_1_visible",
        "policy_2_visible",
        "policy_3_visible"
    };
    
    public PrivateAssertionFilteringXmlStreamWriterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of createProxy method, of class com.sun.xml.ws.policy.jaxws.documentfilter.FilteringXmlStreamWriterProxy.
     */
    public void testCreateProxy() throws Exception {
        XMLStreamWriter result = openFilteredWriter(new StringWriter());

        assertNotNull(result);
    }
    
    public void testFilterPrivateAssertionsFromPolicyExpression() throws Exception {
        for (String testResourceName : testResources) {
            PolicySourceModel model = PolicyResourceLoader.unmarshallModel("visibility/" + testResourceName + ".xml");
            PolicySourceModel expected = PolicyResourceLoader.unmarshallModel("visibility/" + testResourceName + "_expected.xml");
            
            StringWriter buffer = new StringWriter();
            XMLStreamWriter writer = openFilteredWriter(buffer);
            marshaller.marshal(model, writer);
            writer.close();
            
            String marshalledData = buffer.toString();
            System.out.println("Filtered output: \n" + marshalledData);
            
            PolicySourceModel result = PolicyResourceLoader.unmarshallModel(new StringReader(marshalledData));
            assertEquals("Result is not as expected for '" + testResourceName + "' test resource.", expected, result);            
        }
    }
    
    private XMLStreamWriter openFilteredWriter(Writer outputStream) throws XMLStreamException {
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
        return EnhancedXmlStreamWriterProxy.createProxy(writer, new FilteringInvocationProcessorFactory(FilteringInvocationProcessorFactory.FilterType.PRIVATE_ASSERTION_FILTER));        
    }
}
