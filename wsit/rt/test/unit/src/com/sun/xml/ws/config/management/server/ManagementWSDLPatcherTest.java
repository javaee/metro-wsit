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

package com.sun.xml.ws.config.management.server;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller;

import com.sun.xml.ws.policy.testutils.PolicyResourceLoader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagementWSDLPatcherTest extends TestCase {

    private static final String WSDL_NO_POLICY = "<?xml version='1.0' encoding='UTF-8'?>"
  + "<definitions xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsp=\"http://www.w3.org/ns/ws-policy\" xmlns:wsp1_2=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://test.ws.xml.sun.com/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://test.ws.xml.sun.com/\" name=\"NewWebServiceService\">"
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

    private static final String WSDL = "<?xml version='1.0' encoding='UTF-8'?>"
  + "<definitions xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:wsp=\"http://www.w3.org/ns/ws-policy\" xmlns:wsp1_2=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://test.ws.xml.sun.com/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://test.ws.xml.sun.com/\" name=\"NewWebServiceService\">"
  + "<wsp:Policy wsu:Id=\"output-policy\">"
  + "<All/>"
  + "</wsp:Policy>"
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
  + "<wsp:PolicyReference URI=\"#binding-policy\"/>"
  + "<soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\" />"
  + "<operation name=\"echo\">"
  + "<soap:operation soapAction=\"\" />"
  + "<input>"
  + "<wsp:Policy wsu:Id=\"inlined-policy\">"
  + "<All/>"
  + "</wsp:Policy>"
  + "<soap:body use=\"literal\" />"
  + "</input>"
  + "<output>"
  + "<wsp:PolicyReference URI=\"#output-policy\"/>"
  + "<soap:body use=\"literal\" />"
  + "</output>"
  + "</operation>"
  + "</binding>"
  + "<service name=\"NewWebServiceService\">"
  + "<port name=\"NewWebServicePort\" binding=\"tns:NewWebServicePortBinding\">"
  + "<soap:address location=\"http://localhost:8080/WebApplicationSunJAXWSFromJava/NewWebService\" />"
  + "</port>"
  + "</service>"
  + "<wsp:Policy wsu:Id=\"binding-policy\">"
  + "<All/>"
  + "</wsp:Policy>"
  + "<wsp:Policy wsu:Id=\"unreferenced-policy\">"
  + "<All/>"
  + "</wsp:Policy>"
  + "</definitions>";

    private static final String WSDL_WITH_IMPORT = "<!-- Published by JAX-WS RI at http://jax-ws.dev.java.net. RI's version is JAX-WS RI 2.2-hudson-475-rc1. --><definitions xmlns:wsp=\"http://www.w3.org/ns/ws-policy\" xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:sp=\"http://docs.oasis-open.org/ws-sx/ws-securitypolicy/200702\" xmlns:sc=\"http://schemas.sun.com/2006/03/wss/server\" xmlns:wspp=\"http://java.sun.com/xml/ns/wsit/policy\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://test.ws.xml.sun.com/\" xmlns=\"http://schemas.xmlsoap.org/wsdl/\" targetNamespace=\"http://test.ws.xml.sun.com/\" name=\"NewWebServiceService\">"
  + "    <import namespace=\"http://test.ws.xml.sun.com/\" location=\"jndi:/server/WebApplicationSunJAXWSFromWSDL/WEB-INF/wsdl/Type.wsdl\" />"
  + "    <binding name=\"NewWebServicePortBinding\" type=\"tns:NewWebService\">"
  + "       <soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\" />"
  + "        <wsp:PolicyReference URI=\"#NewWebServicePortBindingPolicy\" />"
  + "        <operation name=\"echo\">"
  + "            <soap:operation soapAction=\"\" />"
  + "            <input>"
  + "                <soap:body use=\"literal\" />"
  + "            </input>"
  + "           <output>"
  + "                <soap:body use=\"literal\" />"
  + "            </output>"
  + "        </operation>"
  + "    </binding>"
  + "    <service name=\"NewWebServiceService\">"
  + "        <port name=\"NewWebServicePort\" binding=\"tns:NewWebServicePortBinding\">"
  + "            <wsp:Policy>"
  + "                <sunman:ManagedService xmlns:sunman=\"http://java.sun.com/xml/ns/metro/management\" id=\"A unique ID\" />"
  + "            </wsp:Policy>"
  + "            <soap:address location=\"temporary address after web service reconfiguration\" />"
  + "        </port>"
  + "    </service>"
  + "    <wsp:Policy wsu:Id=\"NewWebServicePortBindingPolicy\">"
  + "        <wsp:ExactlyOne>"
  + "            <wsp:All>"
  + "                <wsam:Addressing wsp:Optional=\"false\" />"
  + "                <sp:TransportBinding>"
  + "                    <wsp:Policy>"
  + "                        <sp:TransportToken>"
  + "                            <wsp:Policy>"
  + "                                <sp:HttpsToken RequireClientCertificate=\"false\" />"
  + "                            </wsp:Policy>"
  + "                        </sp:TransportToken>"
  + "                        <sp:Layout>"
  + "                            <wsp:Policy>"
  + "                                <sp:Lax />"
  + "                            </wsp:Policy>"
  + "                        </sp:Layout>"
  + "                        <sp:IncludeTimestamp />"
  + "                        <sp:AlgorithmSuite>"
  + "                            <wsp:Policy>"
  + "                                <sp:Basic128 />"
  + "                            </wsp:Policy>"
  + "                        </sp:AlgorithmSuite>"
  + "                    </wsp:Policy>"
  + "                </sp:TransportBinding>"
  + "                <sp:Wss10 />"
  + "            </wsp:All>"
  + "        </wsp:ExactlyOne>"
  + "    </wsp:Policy>"
  + "</definitions>";

    private DocumentBuilder builder;


    public ManagementWSDLPatcherTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        documentFactory.setNamespaceAware(true);
        builder = documentFactory.newDocumentBuilder();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBridgeWsdlWithoutPolicy() throws Exception {
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

        final Document result = builder.parse(new InputSource(new StringReader(writer.toString())));
        final NodeList bindingElements = result.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "binding");
        assertEquals(1, bindingElements.getLength());
        final NodeList policyElements = result.getElementsByTagNameNS("http://www.w3.org/ns/ws-policy", "Policy");
        assertEquals(0, policyElements.getLength());
    }

    public void testBridgeWsdlWithoutPolicyAddPolicies() throws Exception {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_ID, Policy.createEmptyPolicy(null, "binding-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_ID, Policy.createEmptyPolicy(null, "operation-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_INPUT_ID, Policy.createEmptyPolicy(null, "input-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_OUTPUT_ID, Policy.createEmptyPolicy(null, "output-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_FAULT_ID, Policy.createEmptyPolicy(null, "fault-policy"));
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);
        final StringReader reader = new StringReader(WSDL_NO_POLICY);
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();

        final Document result = builder.parse(new InputSource(new StringReader(writer.toString())));
        final NodeList bindingElements = result.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "binding");
        assertEquals(1, bindingElements.getLength());
        final NodeList policyElements = result.getElementsByTagNameNS("http://www.w3.org/ns/ws-policy", "Policy");
        assertEquals(4, policyElements.getLength());
    }

    public void testBridgeWsdlRemovePolicies() throws Exception {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);
        final StringReader reader = new StringReader(WSDL);
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();

        final Document result = builder.parse(new InputSource(new StringReader(writer.toString())));
        final NodeList bindingElements = result.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "binding");
        assertEquals(1, bindingElements.getLength());
        final NodeList policyElements = result.getElementsByTagNameNS("http://www.w3.org/ns/ws-policy", "Policy");
        assertEquals(0, policyElements.getLength());
    }

    public void testBridgeWsdl2RemovePolicies() throws Exception {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);
        final StringReader reader = new StringReader(WSDL_WITH_IMPORT);
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();

        final Document result = builder.parse(new InputSource(new StringReader(writer.toString())));
        final NodeList bindingElements = result.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "binding");
        assertEquals(1, bindingElements.getLength());
        final NodeList policyElements = result.getElementsByTagNameNS("http://www.w3.org/ns/ws-policy", "Policy");
        assertEquals(0, policyElements.getLength());
    }

    public void testBridgeWsdlRemoveAndAddPolicies() throws Exception {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_ID, Policy.createEmptyPolicy(null, "binding-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_ID, Policy.createEmptyPolicy(null, "operation-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_INPUT_ID, Policy.createEmptyPolicy(null, "input-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_OUTPUT_ID, Policy.createEmptyPolicy(null, "output-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_FAULT_ID, Policy.createEmptyPolicy(null, "fault-policy"));
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);
        final StringReader reader = new StringReader(WSDL_NO_POLICY);
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();

        final Document result = builder.parse(new InputSource(new StringReader(writer.toString())));
        final NodeList bindingElements = result.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "binding");
        assertEquals(1, bindingElements.getLength());
        final NodeList policyElements = result.getElementsByTagNameNS("http://www.w3.org/ns/ws-policy", "Policy");
        assertEquals(4, policyElements.getLength());
        final NodeList policyReferenceElements = result.getElementsByTagNameNS("http://www.w3.org/ns/ws-policy", "PolicyReference");
        assertEquals(0, policyReferenceElements.getLength());
    }

    public void testOut() throws Exception {
        final HashMap<URI, Policy> urnToPolicy = new HashMap<URI, Policy>();
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_ID, Policy.createEmptyPolicy(null, "binding-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_ID, Policy.createEmptyPolicy(null, "operation-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_INPUT_ID, Policy.createEmptyPolicy(null, "input-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_OUTPUT_ID, Policy.createEmptyPolicy(null, "output-policy"));
        urnToPolicy.put(ExternalAttachmentsUnmarshaller.BINDING_OPERATION_FAULT_ID, Policy.createEmptyPolicy(null, "fault-policy"));
        final ManagementWSDLPatcher instance = new ManagementWSDLPatcher(urnToPolicy);

        final Reader reader = PolicyResourceLoader.getResourceReader("wsdl_filter/PingService.wsdl");

        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(reader);
        final StringWriter writer = new StringWriter();
        final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        final XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(writer);
        instance.bridge(xmlReader, xmlWriter);
        xmlWriter.flush();

        System.out.println(writer.toString());
    }
}
