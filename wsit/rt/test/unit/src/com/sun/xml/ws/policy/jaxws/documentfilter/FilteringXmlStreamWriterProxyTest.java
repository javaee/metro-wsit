/*
 * FilteringXmlStreamWriterProxyTest.java
 * JUnit based test
 *
 * Created on October 4, 2006, 6:02 PM
 */

package com.sun.xml.ws.policy.jaxws.documentfilter;

import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.StringWriter;
import java.io.Writer;
import junit.framework.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author kaos
 */
public class FilteringXmlStreamWriterProxyTest extends TestCase {
    private PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);
    
    public FilteringXmlStreamWriterProxyTest(String testName) {
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
        XMLStreamWriter writer = openFilteredWriter(new StringWriter());
        
        XMLStreamWriter result = FilteringXmlStreamWriterProxy.createProxy(writer);
        assertNotNull(result);
    }    
    
    public void testFilterPrivateAssertionsFromPolicyExpression() throws Exception {
/*
        PolicySourceModel model = PolicyResourceLoader.unmarshallModel("");
        
        StringWriter buffer = new StringWriter();
        XMLStreamWriter writer = openFilteredWriter(buffer);
        marshaller.marshal(model, writer);
        writer.close();
        
        System.out.println("Filtered output: \n" + buffer.toString());
*/
        assertTrue(true); // TODO: replace with real implementation
 }
    
    private XMLStreamWriter openFilteredWriter(Writer outputStream) throws XMLStreamException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
    }
}
