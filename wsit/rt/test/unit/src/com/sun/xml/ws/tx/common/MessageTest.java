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
