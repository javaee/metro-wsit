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

/**
 *
 * @author SAAJ RI Development Team
 */
package com.sun.xml.messaging.saaj.soap.ver1_1;


import com.sun.xml.jaxws.ExpressSOAPXMLEncoder;
import com.sun.xml.jaxws.JAXWSMessage;
import java.io.IOException;
import java.io.OutputStream;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;

import com.sun.xml.messaging.saaj.soap.SOAPDocumentImpl;
import com.sun.xml.messaging.saaj.soap.impl.EnvelopeImpl;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPHeader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class ExpressEnvelope1_1Impl extends Envelope1_1Impl {
    
    JAXWSMessage jxMessage = null;
    
    public ExpressEnvelope1_1Impl (SOAPDocumentImpl ownerDoc, String prefix){
        super (ownerDoc,prefix);
    }
    
    ExpressEnvelope1_1Impl (SOAPDocumentImpl ownerDoc,String prefix,boolean createHeader,boolean createBody)throws SOAPException {
        super (  ownerDoc,  prefix, createHeader, createBody);
    }
    
    public SOAPBody getBodyWC (){
        return body;
    }
    
    public SOAPHeader getHeaderWC (){
        return header;
    }
    
    public void setJXMessage (JAXWSMessage message){
        this.jxMessage = message;
    }
    
    public void output (OutputStream outputStream, boolean fastInfoset) throws IOException {
        try {
           /*
            if (omitXmlDecl.equals ("no") && xmlDecl == null) {
                xmlDecl = "<?xml version=\"" + getOwnerDocument ().getXmlVersion () + "\" encoding=\"" +
                        charset + "\" ?>";
            }
            
            if (xmlDecl != null) {
                OutputStreamWriter writer = new OutputStreamWriter (out, charset);
                writer.write (xmlDecl);
                writer.flush ();
            }
            
            */
            
            if(jxMessage.isBodyUsed () && jxMessage.isHeaderUsed ()){
                super.output (outputStream,fastInfoset);
            }else{
                jxMessage.writeSOAPMessage(outputStream);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
