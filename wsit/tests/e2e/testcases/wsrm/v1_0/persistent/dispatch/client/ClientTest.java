/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package wsrm.v1_0.persistent.dispatch.client;



import java.io.IOException;
import java.util.logging.Level;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Dispatch;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.StringReader;

import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class ClientTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(ClientTest.class.getName());
    private static final String helloRequest = "<?xml version=\"1.0\" ?>" +
            "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "  <S:Header></S:Header>" +
            "  <S:Body>" +
            "    <echoString xmlns=\"http://tempuri.org/\">" +
            "      <Text>Hello There! no0</Text>" +
            "      <Sequence>seq! no0</Sequence>" +
            "    </echoString>" +
            "  </S:Body>" +
            "</S:Envelope>";

    private static final String NAMESPACEURI = "http://tempuri.org/";
    private static final String SERVICE_NAME = "PingService";
    private static final String PORT_NAME = "WSHttpBinding_IPing";
    private static final QName SERVICE_QNAME = new QName(NAMESPACEURI, SERVICE_NAME);
    private static final QName PORT_QNAME = new QName(NAMESPACEURI, PORT_NAME);


    public void testSendEcho() throws Exception {
        PingService service = new PingService();
        Dispatch<SOAPMessage> dispatchMsg = null;
        try {
            dispatchMsg = service.createDispatch(
                    PORT_QNAME,
                    SOAPMessage.class,
                    javax.xml.ws.Service.Mode.MESSAGE,
                    new WebServiceFeature[] {new javax.xml.ws.RespectBindingFeature()});

            SOAPMessage reqMsg = makeSOAPMessage(helloRequest);

            LOGGER.info(String.format("Sending request message on a dispatch client:%n%s", getSOAPMessageAsString(reqMsg)));
            SOAPMessage resMsg = dispatchMsg.invoke(reqMsg);
            String responseMessage = getSOAPMessageAsString(resMsg);
            LOGGER.info(String.format("Received response message on a dispatch client:%n%s", responseMessage));

            if (!responseMessage.contains(new String("Action"))) {
                fail("The response Message is not as expected");
            }

        } finally {
            if (dispatchMsg instanceof Closeable) {
                try {
                    Closeable.class.cast(dispatchMsg).close();
                    LOGGER.info("Dispatch client successfully closed");
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Error closing dispatch", ex);
                }
            }
        }
    }

    private String getSOAPMessageAsString(SOAPMessage msg) throws Exception {
        ByteArrayOutputStream output = null;
        output = new ByteArrayOutputStream();
        msg.writeTo(output);
        return output.toString();
    }

    private SOAPMessage makeSOAPMessage(String msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent((Source) new StreamSource(new StringReader(msg)));
        message.saveChanges();
        return message;
    }
}
