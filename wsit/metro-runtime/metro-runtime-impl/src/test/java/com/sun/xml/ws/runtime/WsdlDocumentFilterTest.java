/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

/*
 * WsdlDocumentFilterTest.java
 * JUnit based test
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */

package com.sun.xml.ws.runtime;

import java.io.StringWriter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.sun.xml.ws.api.server.SDDocumentFilter;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import com.sun.xml.ws.xmlfilter.EnhancedXmlStreamWriterProxy;
import com.sun.xml.ws.xmlfilter.InvocationProcessorFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import javax.xml.stream.XMLStreamException;
import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class WsdlDocumentFilterTest extends TestCase {
    private static final PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);

    private static final String[] testPolicyResources = new String[] {
        "policy_0",
        "policy_1",
        "policy_2",
        "policy_3",
        "policy_4"
    };
    
    private static final String[] testWsdlResources = new String[] {
        "PingService",
        "W2JRLR2010TestService"
    };
    
    private static final SDDocumentFilter filter = new WsdlDocumentFilter();
    
    public WsdlDocumentFilterTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of createProxy method, of class com.sun.xml.ws.policy.jaxws.documentfilter.FilteringXmlStreamWriterProxy.
     */
    public void testCreateProxy() throws Exception {
        XMLStreamWriter result = openFilteredWriter(new StringWriter(), filter);
        
        assertNotNull(result);
    }
    
    public void testFilterPolicyExpression() throws Exception {
        performResourceBasedTest(testPolicyResources, "", ".xml", filter);
    }
    
    public void testFilterWSDL() throws Exception {
        for (String wsdlResource : testWsdlResources) {
            StringWriter filteredBuffer = new StringWriter();
            StringWriter unfilteredBuffer = new StringWriter();
            
            readAndWriteWsdl(wsdlResource, filteredBuffer, true);
            readAndWriteWsdl(wsdlResource + "_expected", unfilteredBuffer, false);
            
            assertEquals(unfilteredBuffer.toString(), filteredBuffer.toString());
        }
    }
    
    private void readAndWriteWsdl(String wsdlName, StringWriter buffer, boolean filter) throws Exception {
        XMLStreamReader reader = null;
        XMLStreamWriter writer = null;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(ResourceLoader.getResourceStream(wsdlName + ".wsdl"));
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(buffer /*, "UTF-8"*/);
            //generate the WSDL with utf-8 encoding and XML version 1.0
            writer.writeStartDocument("UTF-8", "1.0");
            if (filter) {
                writer = new WsdlDocumentFilter().filter(null, writer);
            }
            
            new XMLStreamReaderToXMLStreamWriter().bridge(reader, writer);
            
            writer.writeEndDocument();
        } finally {
            if (writer != null) try {writer.close();} finally {
                if (reader != null) reader.close();
            }
        }
        
    }
    
    private final XMLStreamWriter openFilteredWriter(Writer outputStream, InvocationProcessorFactory factory) throws XMLStreamException {
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
        return EnhancedXmlStreamWriterProxy.createProxy(writer, factory);
    }

    private final XMLStreamWriter openFilteredWriter(Writer outputStream, SDDocumentFilter filter) throws XMLStreamException, IOException {
        return filter.filter(null, XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream));
    }

    private final void performResourceBasedTest(String[] resourceNames, String resourcePrefix, String resourceSuffix, InvocationProcessorFactory factory) throws PolicyException, IOException, XMLStreamException {
        for (String testResourceName : resourceNames) {
            PolicySourceModel model = ResourceLoader.unmarshallModel(resourcePrefix + testResourceName + resourceSuffix);
            PolicySourceModel expected = ResourceLoader.unmarshallModel(resourcePrefix + testResourceName + "_expected" + resourceSuffix);

            StringWriter buffer = new StringWriter();
            XMLStreamWriter writer = openFilteredWriter(buffer, factory);
            marshaller.marshal(model, writer);
            writer.close();

            String marshalledData = buffer.toString();

            PolicySourceModel result = ResourceLoader.unmarshallModel(new StringReader(marshalledData));
            assertEquals("Result is not as expected for '" + testResourceName + "' test resource.", expected, result);
        }
    }

    private final void performResourceBasedTest(String[] resourceNames, String resourcePrefix, String resourceSuffix, SDDocumentFilter filter) throws PolicyException, IOException, XMLStreamException {
        for (String testResourceName : resourceNames) {
            PolicySourceModel model = ResourceLoader.unmarshallModel(resourcePrefix + testResourceName + resourceSuffix);
            PolicySourceModel expected = ResourceLoader.unmarshallModel(resourcePrefix + testResourceName + "_expected" + resourceSuffix);

            StringWriter buffer = new StringWriter();
            XMLStreamWriter writer = openFilteredWriter(buffer, filter);
            marshaller.marshal(model, writer);
            writer.close();

            String marshalledData = buffer.toString();

            PolicySourceModel result = ResourceLoader.unmarshallModel(new StringReader(marshalledData));
            assertEquals("Result is not as expected for '" + testResourceName + "' test resource.", expected, result);
        }
    }

    private final PolicyModelMarshaller getPolicyModelMarshaller() {
        return marshaller;
    }
}
