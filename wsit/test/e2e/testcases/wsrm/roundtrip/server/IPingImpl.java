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
package wsrm.roundtrip.server;
import javax.xml.bind.JAXBElement;
import javax.jws.WebService;

import javax.xml.bind.*;
import javax.xml.namespace.*;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.bind.JAXBElement;
import javax.xml.ws.soap.*;
import javax.xml.ws.*;

@WebService(endpointInterface="wsrm.roundtrip.server.IPing")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class IPingImpl {

    /**
     * @param String
     */
       public PingResponseBodyType echoString(
        PingRequestBodyType echoString){
        PingResponseBodyType pr = new ObjectFactory().createPingResponseBodyType();
        JAXBElement<String> val = new JAXBElement<String>(new QName("http://tempuri.org/","EchoStringReturn"),String.class,new String("Returning hello "));
        JAXBElement<String> text = echoString.getText();
        JAXBElement<String> seq = echoString.getSequence();
        val.setValue("Returning " +text.getValue()+"Sequence" +seq.getValue());
        pr.setEchoStringReturn(val);

        return pr;
    }
}
