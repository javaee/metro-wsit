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

package wsrm.v1_0.invm.dispatch.server;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

@WebServiceProvider(
    portName="WSHttpBinding_IPing",
    serviceName="PingService",
    targetNamespace="http://tempuri.org/",
    wsdlLocation="WEB-INF/wsdl/EchoService.wsdl"
)
@BindingType(value="http://schemas.xmlsoap.org/wsdl/soap/http")
@javax.xml.ws.RespectBinding
@ServiceMode(value=javax.xml.ws.Service.Mode.MESSAGE)
public class PingProvider implements Provider<SOAPMessage> {

    private static final String helloResponse = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "  <S:Header>" +
        "  </S:Header>" +
        "  <S:Body>" +
        "    <echoStringResponse xmlns=\"http://tempuri.org/\">" +
        "      <EchoStringReturn>Returning Hello There! no1Sequenceseq! no1</EchoStringReturn>" +
        "    </echoStringResponse>" +
        "  </S:Body>" +
        "</S:Envelope>";

    public SOAPMessage invoke(SOAPMessage req)  {
	System.out.println("invoke: Request: " + getSOAPMessageAsString(req));
        SOAPMessage res = null;
	try {
            res = makeSOAPMessage(helloResponse);
	} catch (Exception e) {
	    System.out.println("Exception: occurred " + e);
	}
	System.out.println("invoke: Response: " + getSOAPMessageAsString(res));
        return res;
    }

    private String getSOAPMessageAsString(SOAPMessage msg)
    {
	ByteArrayOutputStream baos = null;
	String s = null;
        try {
	    baos = new ByteArrayOutputStream();
            msg.writeTo(baos);
	    s = baos.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
	return s;
    }

    private SOAPMessage makeSOAPMessage(String msg)
    {
	try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage();
            message.getSOAPPart().setContent((Source)new StreamSource(new StringReader(msg)));
            message.saveChanges();
            return message;
	}
	catch (Exception e) {
	    return null;
	}
    }
}

