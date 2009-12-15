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

package com.sun.xml.ws.runtime;

import com.sun.xml.ws.api.server.SDDocumentFilter;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelMarshaller;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import com.sun.xml.ws.xmlfilter.EnhancedXmlStreamWriterProxy;
import com.sun.xml.ws.xmlfilter.InvocationProcessorFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.TestCase;

/**
 * Abstract base class for filtering tests
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class AbstractFilteringTestCase extends TestCase { // must not be abstract to avoid JUnit warnings
    private static final PolicyModelMarshaller marshaller = PolicyModelMarshaller.getXmlMarshaller(true);    
    
    /** Creates a new instance of AbstractFilteringTestCase */
    public AbstractFilteringTestCase(String testName) {super(testName);}
    
    protected final XMLStreamWriter openFilteredWriter(Writer outputStream, InvocationProcessorFactory factory) throws XMLStreamException {
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
        return EnhancedXmlStreamWriterProxy.createProxy(writer, factory);
    }
    
    protected final XMLStreamWriter openFilteredWriter(Writer outputStream, SDDocumentFilter filter) throws XMLStreamException, IOException {
        return filter.filter(null, XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream));
    }
    
    protected final void performResourceBasedTest(String[] resourceNames, String resourcePrefix, String resourceSuffix, InvocationProcessorFactory factory) throws PolicyException, IOException, XMLStreamException {
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

    protected final void performResourceBasedTest(String[] resourceNames, String resourcePrefix, String resourceSuffix, SDDocumentFilter filter) throws PolicyException, IOException, XMLStreamException {
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
    
    protected final PolicyModelMarshaller getPolicyModelMarshaller() {
        return marshaller;
    }
    
}
