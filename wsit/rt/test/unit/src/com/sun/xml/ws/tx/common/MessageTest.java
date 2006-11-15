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

package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import junit.framework.TestCase;

/**
 *
 * @author Ryan.Shoemaker@Sun.COM
 */
public class MessageTest extends TestCase {
    
    /** Creates a new instance of MessageTest */
    public MessageTest(String name) {
        super(name);
    }

    private final String messageSource = 
        "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
          "<S:Header>" +
            "<To xmlns=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://tini.east.sun.com/WcfInterop/TransactionalService.svc</To>" +
            "<Action xmlns=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://tempuri.org/ITransactionalService/Commit</Action>" +
            "<ReplyTo xmlns=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
              "<Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">uuid:655bd1d1-5085-4b88-832e-c813254b4d3d</MessageID>" +
            "<CoordinationContext xmlns=\"http://schemas.xmlsoap.org/ws/2004/10/wscoor\" xmlns:ns2=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\" xmlns:ns3=\"http://java.sun.com/xml/ns/wsit/coord\" xmlns:ns4=\"http://schemas.xmlsoap.org/soap/envelope/\" ns4:mustUnderstand=\"true\">" +
              "<Identifier>uuid:WSCOOR-SUN-2</Identifier>" +
              "<Expires>37376</Expires>" +
              "<CoordinationType>http://schemas.xmlsoap.org/ws/2004/10/wsat</CoordinationType>" +
              "<RegistrationService>" +
                "<ns2:Address>https://tini.east.sun.com:8181/wstx-services/wscoor/coordinator/register</ns2:Address>" +
                "<ns2:ReferenceParameters>" +
                  "<jaxws:objectId xmlns:jaxws=\"http://jax-ws.dev.java.net/xml/ns/\" xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">dc9cde07-108c-4d1f-9410-507867fa1dfa</jaxws:objectId>" +
                "</ns2:ReferenceParameters>" +
              "</RegistrationService>" +
            "</CoordinationContext>" +
          "</S:Header>" +
          "<S:Body>" +
            "<Commit xmlns=\"http://tempuri.org/\"/>" +
          "</S:Body>" +
        "</S:Envelope>";

    private final com.sun.xml.ws.api.message.Message jaxwsMessage = 
        Messages.create(new StreamSource(new StringReader(messageSource)), SOAPVersion.SOAP_11);
    
    private final com.sun.xml.ws.tx.common.Message txMessage = 
        new com.sun.xml.ws.tx.common.Message(jaxwsMessage);
    
    public void testGetCtxHeader() throws Exception {
        Header h = txMessage.getCoordCtxHeader();
        assertNotNull(h);
        assertEquals("http://schemas.xmlsoap.org/ws/2004/10/wscoor", h.getNamespaceURI());
        assertEquals("CoordinationContext", h.getLocalPart());
    }
    
    public void testGetContext() throws Exception {
        CoordinationContextInterface cc = 
            txMessage.getCoordinationContext(TxJAXBContext.createUnmarshaller());
        // we really only need to test that JAXB can unmarshal and hand us the object
        assertEquals("http://schemas.xmlsoap.org/ws/2004/10/wsat", cc.getCoordinationType());
        assertEquals("uuid:WSCOOR-SUN-2", cc.getIdentifier());
        assertEquals(37376l, cc.getExpires());
    }

    public void testGetCtxHeader2() throws Exception {
        assertNotNull(txMessage.getCoordCtxHeader(Constants.WSCOOR_SOAP_NSURI, Constants.COORDINATION_CONTEXT));
    }
}
