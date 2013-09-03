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

package com.sun.xml.messaging.saaj.soap.ver1_1;

import com.sun.xml.jaxws.ExpressSOAPXMLEncoder;
import com.sun.xml.jaxws.JAXWSMessage;
import com.sun.xml.messaging.saaj.SOAPExceptionImpl;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParameterList;
import com.sun.xml.messaging.saaj.soap.ExpressMessage;
import com.sun.xml.messaging.saaj.soap.SOAPPartImpl;
import com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExpressMessage1_1Impl extends  Message1_1Impl implements ExpressMessage{
    boolean debug = false;
    JAXWSMessage jxMessage = null;
    public ExpressMessage1_1Impl(MimeHeaders headers, InputStream in) throws IOException, SOAPExceptionImpl {
        super(headers, in);
    }
    
    public ExpressMessage1_1Impl(MimeHeaders headers,JAXWSMessage jMessage ){
        super();
        this.headers = headers;
        this.jxMessage = jMessage;
        soapPartImpl = new ExpressSOAPPart1_1Impl(this);
    }
    public ExpressMessage1_1Impl() {
        super();
    }
    
    public ExpressMessage1_1Impl(boolean isFastInfoset, boolean acceptFastInfoset) {
        super(isFastInfoset, acceptFastInfoset);
    }
    
    public ExpressMessage1_1Impl(SOAPMessage msg) {
        super(msg);
    }
    
    public void setJAXWSMessage(JAXWSMessage message){
        this.jxMessage  = message;
    }
    
    
    public SOAPPart getSOAPPart() {
        if (soapPartImpl == null) {
            soapPartImpl = new ExpressSOAPPart1_1Impl(this);
        }
        return soapPartImpl;
    }
    
    public SOAPBody getSOAPBody()throws SOAPException{
        if(debug)
            System.out.println(getSOAPPart());
        
        ExpressSOAPPart1_1Impl soapPart = (ExpressSOAPPart1_1Impl) getSOAPPart();
        if(debug)
            System.out.println(soapPart.getEnvelope());
        ExpressEnvelope1_1Impl envelope = (ExpressEnvelope1_1Impl)soapPart.getEnvelope();
        SOAPBody body = envelope.getBodyWC();        
        if(jxMessage.isBodyUsed()){
            
            return (SOAPBody)body;
        }else{
            try {
               
                jxMessage.constructSOAPBody((Node)body);
            } catch (IOException ex) {
                throw new SOAPException(ex);
            } catch (XMLStreamException ex) {
                throw new SOAPException(ex);
            }
            jxMessage.setBodyUsed(true);
            return(SOAPBody) body;
        }
    }
    
    
    public SOAPHeader getSOAPHeader() throws SOAPException{
        ExpressSOAPPart1_1Impl soapPart = (ExpressSOAPPart1_1Impl) getSOAPPart();
        ExpressEnvelope1_1Impl envelope =  (ExpressEnvelope1_1Impl)soapPart.getEnvelope();
        if(debug){
            System.out.println(soapPart);
            System.out.println(envelope);
        }
        SOAPHeader header = envelope.getHeaderWC();
        if(jxMessage.isHeaderUsed()){
            return (SOAPHeader)header;
        }else{
            
            try {
                jxMessage.constructSOAPHeaders((Node)header);
            } catch (XMLStreamException ex) {
                throw new SOAPException(ex);
            }
            jxMessage.setHeaderUsed(true);
            return (SOAPHeader)header;
        }
    }
    /*
    public void writeTo (OutputStream out) throws SOAPException, IOException {
     
        if(jxMessage == null){
            super.writeTo (out);
            return;
        }
     
        if(jxMessage != null && jxMessage.isBodyUsed () && jxMessage.isHeaderUsed ()){
            super.writeTo (out);
        }else{
            ExpressSOAPXMLEncoder encoder = new ExpressSOAPXMLEncoder ();
            encoder.writeSOAPMessage (out, jxMessage.getInternalMessage (),jxMessage.getMessageInfo ());
            jxMessage.setBodyUsed (true);
            jxMessage.setHeaderUsed (true);
            return;
        }
    }
     */
    
    
    public synchronized boolean saveRequired() {
        if(jxMessage != null && jxMessage.isBodyUsed() && jxMessage.isHeaderUsed()){
            return false;
        }else{
            return super.saveRequired();
        }
        
    }
    
    public SOAPBody getEMBody()throws SOAPException {
        ExpressSOAPPart1_1Impl soapPart = (ExpressSOAPPart1_1Impl) getSOAPPart();
        if(debug)
            System.out.println(soapPart.getEnvelope());
        ExpressEnvelope1_1Impl envelope = (ExpressEnvelope1_1Impl)soapPart.getEnvelope();
        SOAPBody body = envelope.getBodyWC();
        return body;
    }
    
    public SOAPHeader getEMHeader() throws SOAPException{
        ExpressSOAPPart1_1Impl soapPart = (ExpressSOAPPart1_1Impl) getSOAPPart();
        ExpressEnvelope1_1Impl envelope =  (ExpressEnvelope1_1Impl)soapPart.getEnvelope();
        if(debug){
            System.out.println(soapPart);
            System.out.println(envelope);
        }
        SOAPHeader header = envelope.getHeaderWC();
        return header;
    }
    public JAXWSMessage getJAXWSMessage(){
        return jxMessage;
    }
}
