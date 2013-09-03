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

package com.sun.xml.messaging.saaj.soap.ver1_2;

import com.sun.xml.jaxws.ExpressSOAPXMLEncoder;
import com.sun.xml.jaxws.JAXWSMessage;
import com.sun.xml.messaging.saaj.SOAPExceptionImpl;
import com.sun.xml.messaging.saaj.soap.ExpressMessage;

import com.sun.xml.messaging.saaj.soap.ver1_1.Message1_1Impl;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamException;

public class ExpressMessage1_2Impl extends Message1_2Impl implements ExpressMessage{
    
    JAXWSMessage jxMessage = null;
    
    public ExpressMessage1_2Impl(MimeHeaders headers,JAXWSMessage jMessage ){
        
        super();
        this.headers = headers;
        
        this.jxMessage = jMessage;
    }
    public ExpressMessage1_2Impl() {
        super();
    }
    
    public ExpressMessage1_2Impl(SOAPMessage msg) {
        super(msg);
    }
    
    public ExpressMessage1_2Impl(boolean isFastInfoset, boolean acceptFastInfoset) {
        super(isFastInfoset, acceptFastInfoset);
    }
    
    public ExpressMessage1_2Impl(MimeHeaders headers, InputStream in)
    throws IOException, SOAPExceptionImpl {
        super(headers, in);
    }
    
    
    public SOAPPart getSOAPPart() {
        if (soapPartImpl == null) {
            soapPartImpl = new ExpressSOAPPart1_2Impl();
        }
        return soapPartImpl;
    }
    
    public SOAPBody getSOAPBody()throws SOAPException{
        ExpressSOAPPart1_2Impl soapPart = (ExpressSOAPPart1_2Impl) getSOAPPart();
        ExpressEnvelope1_2Impl envelope = (ExpressEnvelope1_2Impl) soapPart.getEnvelope();
        SOAPBody body = envelope.getBodyWC();
        if(jxMessage.isBodyUsed()){
            return (SOAPBody)body;
        }else{
            
            try {
                
                jxMessage.constructSOAPBody(body);
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
        ExpressSOAPPart1_2Impl soapPart = (ExpressSOAPPart1_2Impl) getSOAPPart();
        ExpressEnvelope1_2Impl envelope =  (ExpressEnvelope1_2Impl)soapPart.getEnvelope();
        SOAPHeader header = envelope.getHeaderWC();
        if(jxMessage.isHeaderUsed()){
            return (SOAPHeader)header;
        }else{
            try {
                jxMessage.constructSOAPHeaders(header);
            } catch (XMLStreamException ex) {
                throw new SOAPException(ex);
            }
            jxMessage.setHeaderUsed(true);
            return (SOAPHeader)header;
        }
    }
    public SOAPBody getEMBody() throws SOAPException{
        ExpressSOAPPart1_2Impl soapPart = (ExpressSOAPPart1_2Impl) getSOAPPart();        
        ExpressEnvelope1_2Impl envelope = (ExpressEnvelope1_2Impl)soapPart.getEnvelope();
        SOAPBody body = envelope.getBodyWC();
        return body;
    }
    
    public SOAPHeader getEMHeader()throws SOAPException {
        ExpressSOAPPart1_2Impl soapPart = (ExpressSOAPPart1_2Impl) getSOAPPart();
        ExpressEnvelope1_2Impl envelope =  (ExpressEnvelope1_2Impl)soapPart.getEnvelope();
        
        SOAPHeader header = envelope.getHeaderWC();
        return header;
    }
    public JAXWSMessage getJAXWSMessage(){
        return jxMessage;
    }
}
