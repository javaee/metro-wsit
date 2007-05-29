/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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


package wsrm.createsequence.server;

import javax.xml.bind.JAXBElement;
import javax.jws.WebService;
import javax.xml.bind.*;
import javax.xml.namespace.*;
import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.bind.JAXBElement;
import java.util.Map;
import com.sun.xml.ws.rm.jaxws.util.ProcessingFilter;
import com.sun.xml.ws.rm.Message;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMDestination;
import java.lang.reflect.*;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import com.sun.xml.ws.rm.Sequence;

@WebService(endpointInterface="wsrm.createsequence.server.IPing")
@javax.xml.ws.BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class IPingImpl extends TestService {
    
    static int lastMessageNumber = 0;
    static boolean ordered = true;
    
    @Resource
    WebServiceContext context;
    
    public IPingImpl() {
        RMDestination.getRMDestination().setProcessingFilter(this);
    }
    
    public void handleOneWay(String a, String b) {
        
    }
    
    public String handleTwoWay(String a, String b) {
        return a;
    }
    
    public EchoResponseBodyType echoString(EchoRequestBodyType echoString) {
        
        EchoResponseBodyType pr =
                new ObjectFactory().createEchoResponseBodyType();
        
        JAXBElement<String> text = echoString.getText();
        JAXBElement<String> seq = echoString.getSequence();
        
        String ret = handleTwoWay(text.getValue(), seq.getValue());
        
        JAXBElement<String> val =
                new JAXBElement<String>(new QName("http://tempuri.org/",
                "EchoStringReturn"),
                String.class,
                new String("Returning hello "));
        
        val.setValue(ret);
        
        pr.setEchoStringReturn(val);
        
        return pr;
    }
    
    public void ping(PingRequestBodyType echoString) {
        
        JAXBElement<String> text = echoString.getText();
        JAXBElement<String> seq = echoString.getSequence();
        
        handleOneWay(text.getValue(), seq.getValue());
    }
    
    public void process(String param) {
        if (param.equals("test_order")) {
            int messageNumber = (Integer)(context.getMessageContext().get("com.sun.xml.ws.messagenumber"));
            if (messageNumber != lastMessageNumber + 1) {
                ordered  = false;
            }
            lastMessageNumber = messageNumber;
            
        }
        processImpl(param);
    }
    public String clear(String param) {
        clearImpl(param);
        return "";
    }
    
    public String getCount(String param) {
        return reportCount(param);
    }
    
    public String getSequence(String param) {
        return reportSequence(param);   
    }
    
    public String getDuplicates(String param) {
        return reportDuplicates(param);
    }
    public String getAlive(String param) {
        return isSequenceAlive(param);
    }
    
    public String getInactivityTimeout(String s) {
        return super.getInactivityTimeout(s);
    }
    
    public String getOrdered(String s) {
        if (ordered) {
            return "true";
        } else {
            return "false";
        }
    }
    
    public String getSequenceID(String s) {
        Sequence seq = (Sequence)context.getMessageContext().get("com.sun.xml.ws.sequence");
        if (seq != null) {
            return seq.getId();
        } else {
            return null;
        }
    }
    
    public String getMessageNumber(String s) {
        Integer i =  (Integer)context.getMessageContext().get("com.sun.xml.ws.messagenumber");
        if (i != null) {
            return i.toString();
        } else {
            return "-1";
        }
        
    }
    
   
    
}
