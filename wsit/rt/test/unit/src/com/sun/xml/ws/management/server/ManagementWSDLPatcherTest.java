/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.management.server;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.TestCase;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagementWSDLPatcherTest extends TestCase {

    private static final String WSDL_NO_POLICY = "<?xml version='1.0' encoding='UTF-8'?><definitions xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsp=\"http://www.w3.org/ns/ws-policy\" xmlns:wsp1_2=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://test.ws.xml.sun.com/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://test.ws.xml.sun.com/\" name=\"NewWebServiceService\">"
  + "<types>"
  + "<xsd:schema>"
  + "<xsd:import namespace=\"http://test.ws.xml.sun.com/\" schemaLocation=\"http://localhost:8080/WebApplicationSunJAXWSFromJava/NewWebService?xsd=1\" />"
  + "</xsd:schema>"
  + "</types>"
  + "<message name=\"echo\">"
  + "<part name=\"parameters\" element=\"tns:echo\" />"
  + "</message>"
  + "<message name=\"echoResponse\">"
  + "<part name=\"parameters\" element=\"tns:echoResponse\" />"
  + "</message>"
  + "<portType name=\"NewWebService\">"
  + "<operation name=\"echo\">"
  + "<input wsam:Action=\"http://test.ws.xml.sun.com/NewWebService/echoRequest\" message=\"tns:echo\" />"
  + "<output wsam:Action=\"http://test.ws.xml.sun.com/NewWebService/echoResponse\" message=\"tns:echoResponse\" />"
  + "</operation>"
  + "</portType>"
  + "<binding name=\"NewWebServicePortBinding\" type=\"tns:NewWebService\">"
  + "<soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\" />"
  + "<operation name=\"echo\">"
  + "<soap:operation soapAction=\"\" />"
  + "<input>"
  + "<soap:body use=\"literal\" />"
  + "</input>"
  + "<output>"
  + "<soap:body use=\"literal\" />"
  + "</output>"
  + "</operation>"
  + "</binding>"
  + "<service name=\"NewWebServiceService\">"
  + "<port name=\"NewWebServicePort\" binding=\"tns:NewWebServicePortBinding\">"
  + "<soap:address location=\"http://localhost:8080/WebApplicationSunJAXWSFromJava/NewWebService\" />"
  + "</port>"
  + "</service>"
  + "</definitions>";

//    private static final String WSDL_NO_POLICY = "<?xml version='1.0' encoding='UTF-8'?>"
//  + "<definitions xmlns:tns=\"http://test.ws.xml.sun.com/\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://test.ws.xml.sun.com/\" name=\"NewWebServiceService\">"
//  + "  <binding name=\"NewWebServicePortBinding\" type=\"tns:NewWebService\">"
//  + "  </binding>"
//  + "</definitions>";

    public ManagementWSDLPatcherTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBridgeNoPolicy() throws XMLStreamException {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);
        final StringReader reader = new StringReader(WSDL_NO_POLICY);
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();
        assertTrue(writer.getBuffer().length() > 0);
    }

    public void testBridgePolicy() throws XMLStreamException {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        final Policy policy = Policy.createEmptyPolicy(null, "test-policy");
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_ID, policy);
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);
        final StringReader reader = new StringReader(WSDL_NO_POLICY);
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();
        System.out.println(writer.getBuffer());
    }

}