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

/*
 * JAXBSignatureHeaderElement.java
 *
 * Created on August 18, 2006, 2:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.opt.impl.dsig;

import com.sun.xml.security.core.dsig.CustomStreamWriterImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.impl.MessageConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import com.sun.xml.ws.security.opt.crypto.dsig.Signature;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import javax.xml.stream.XMLStreamException;
import org.jvnet.staxex.XMLStreamWriterEx;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class JAXBSignatureHeaderElement implements SecurityHeaderElement, SecurityElementWriter {
    
    /* true if this signature header element is canonicalized before*/
    private boolean isCanonicalized = false;
    /*canonicalized signature value - for future use*/
    private byte [] cs = null;
    
    private Signature signature = null;
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    private Marshaller marshaller = null;
    private XMLSignContext signContext = null;
    /** Creates a new instance of JAXBSignatureHeaderElement */
    public JAXBSignatureHeaderElement(Signature signature,SOAPVersion soapVersion) {
        this.signature = signature;
        this.soapVersion = soapVersion;
        
    }
    
    public JAXBSignatureHeaderElement(Signature signature,SOAPVersion soapVersion,XMLSignContext signctx) {
        this.signature = signature;
        this.soapVersion = soapVersion;
        this.signContext = signctx;
    }
    
    public String getId() {
        return signature.getId();
    }
    
    public void setId(String id) {
        throw new  UnsupportedOperationException();
    }
    
    
    public String getNamespaceURI() {
        return  MessageConstants.DSIG_NS;
    }
    
    
    public String getLocalPart() {
        return MessageConstants.SIGNATURE_LNAME;
    }
    
    public javax.xml.stream.XMLStreamReader readHeader() throws XMLStreamException {
        XMLStreamBufferResult xbr = new XMLStreamBufferResult();
        try{
            getMarshaller().marshal(signature, xbr);
        } catch(JAXBException je){
            //log
            throw new XMLStreamException(je);
        }
        return xbr.getXMLStreamBuffer().readAsXMLStreamReader();
    }
    
    /**
     * writes the jaxb signature header element to an XMLStreamWriter
     * @param streamWriter javax.xml.stream.XMLStreamWriter
     * @throws XMLStreamException
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws XMLStreamException {
        try {
            // If writing to Zephyr, get output stream and use JAXB UTF-8 writer
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    getMarshaller().marshal(signature, os);
                    return;
                }
            }else if (streamWriter instanceof XMLStreamWriterEx) {
                CustomStreamWriterImpl swi = new CustomStreamWriterImpl(streamWriter);
                getMarshaller().marshal(signature, swi);
            } else {
                getMarshaller().marshal(signature, streamWriter);
            }

        } catch (JAXBException e) {
            throw new XMLStreamException(e);
        }
    }
    
   /**
    * writes the jaxb signature header element to an XMLStreamWriter
    * @param streamWriter javax.xml.stream.XMLStreamWriter
    * @param props HashMap
    * @throws XMLStreamException
    */
   public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter,HashMap props) throws XMLStreamException {
        try{
            Marshaller marshaller = getMarshaller();
            Iterator<Map.Entry<Object, Object>> itr = props.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Object, Object> entry = itr.next();
                marshaller.setProperty((String)entry.getKey(), entry.getValue());
            }
            
            //writeTo(streamWriter);
            marshaller.marshal(signature,streamWriter);
        }catch(JAXBException jbe){
            //logging
            throw new XMLStreamException(jbe);
        }
    }
    
    
    public byte[] canonicalize(final String algorithm, final List<com.sun.xml.wss.impl.c14n.AttributeNS> namespaceDecls) {
        if(!isCanonicalized()){
            canonicalizeSignature();
        }
        return cs;
    }
    
    public boolean isCanonicalized() {
        return isCanonicalized;
    }
    
    private Marshaller getMarshaller() throws JAXBException{
        if(marshaller == null){
            marshaller = JAXBUtil.createMarshaller(soapVersion);
        }
        return marshaller;
    }
    
    private void canonicalizeSignature() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    /**
     * finds whether this  security header element refers to the element with given id
     * @param id String
     * @return boolean
     */
    public boolean refersToSecHdrWithId(final String id) {
        
        StringBuffer sb = new StringBuffer();
        sb.append("#");
        sb.append(id);
        String refId = sb.toString();
        KeyInfo ki = signature.getKeyInfo();
        if(ki != null){
            List list = ki.getContent();
            if(list.size() >0 ){
                JAXBElement je = (JAXBElement) list.get(0);
                Object data = je.getValue();
                
                if(data instanceof SecurityHeaderElement){
                    if(((SecurityHeaderElement)data).refersToSecHdrWithId(id)){
                        return true;
                    }
                }
            }
        }
        List refList = signature.getSignedInfo().getReferences();
        for(int i=0;i< refList.size();i++){
            com.sun.xml.ws.security.opt.crypto.dsig.Reference ref = (com.sun.xml.ws.security.opt.crypto.dsig.Reference)refList.get(i);
            if(ref.getURI().equals(refId)){
                return true;
            }
        }
        return false;
    }
    /**
     * signs the data using the  signContext
     * @throws XMLStreamException
     */
    public void sign()throws XMLStreamException{
        try{
            signature.sign(signContext);
        }catch(MarshalException me){
            throw new XMLStreamException(me);
        }catch(XMLSignatureException xse){
            throw new XMLStreamException(xse);
        }
    }
}
