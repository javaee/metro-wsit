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

package com.sun.xml.ws.security.opt.impl.message;

import com.sun.xml.security.core.xenc.ReferenceList;
import com.sun.xml.security.core.xenc.ReferenceType;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.DOMUtil;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

import com.sun.xml.ws.api.SOAPVersion;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class GSHeaderElement implements SecurityHeaderElement, SecurityElementWriter{
    JAXBElement element = null;
    Object obj = null;
    private String id="";
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    private Element domElement = null;
    private XMLStreamBuffer buffer;
    public GSHeaderElement(JAXBElement el, SOAPVersion sv ){
        this.element = el;
        this.soapVersion = sv;
    }
    
    public GSHeaderElement(Object obj, SOAPVersion sv ){
        this.obj = obj;
        this.soapVersion = sv;
    }
    
    public GSHeaderElement(Element obj, SOAPVersion sv ){
        this.domElement = obj;
        this.soapVersion = sv;
        if(domElement.getLocalName().equals(MessageConstants.SAML_ASSERTION_LNAME)){
            id = domElement.getAttribute(MessageConstants.SAML_ASSERTIONID_LNAME);
            if(id == null || id.equals(""))
                id = domElement.getAttribute("ID");
        }
    }
    
    public GSHeaderElement(Element obj){
        this.domElement = obj;
        if(domElement.getLocalName() == MessageConstants.SAML_ASSERTION_LNAME ){
            id = domElement.getAttribute(MessageConstants.SAML_ASSERTIONID_LNAME);
            if(id == null || id.equals(""))
                id = domElement.getAttribute("ID");
        }
    }

    public GSHeaderElement(XMLStreamBuffer buffer){
       this.buffer = buffer;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    public String getNamespaceURI() {
        if(element != null){
            return element.getName().getNamespaceURI();
        }
        if(domElement != null){
            return domElement.getNamespaceURI();
        }
       
        return "";
    }
    
    
    public String getLocalPart() {
        if(element != null){
            return element.getName().getLocalPart();
        }
        
        if(domElement != null){
            return domElement.getLocalName();
        }
        
         if(obj != null){
            if(obj instanceof ReferenceList){
                return MessageConstants.XENC_REFERENCE_LIST_LNAME;
            }
        }
        return "";
    }
    
    
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        try{
            Marshaller writer =  getMarshaller();
            if(buffer != null){
                buffer.writeToXMLStreamWriter(streamWriter);
            }else if(element != null){
                writer.marshal(element,streamWriter);
            }else if(domElement != null){
                DOMUtil.serializeNode(domElement,streamWriter);
            }else{
                writer.marshal(obj,streamWriter);
            }
        } catch (javax.xml.bind.JAXBException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
    }
    
    public void writeTo(javax.xml.soap.SOAPMessage saaj) throws javax.xml.soap.SOAPException {
        throw new UnsupportedOperationException();
    }
    
    
    public byte[] canonicalize(String algorithm, List<com.sun.xml.wss.impl.c14n.AttributeNS> namespaceDecls) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isCanonicalized() {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        try{
            Marshaller writer =  getMarshaller();
            if(element != null){
                writer.marshal(element,os);
            }else{
                writer.marshal(obj,os);
            }
        } catch (javax.xml.bind.JAXBException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
    }
    
    public String getAttribute(String nsUri, String localName) {
        throw new UnsupportedOperationException();
    }
    
    public String getAttribute(QName name) {
        throw new UnsupportedOperationException();
    }
    
    public boolean refersToSecHdrWithId(String id) {
        String tmpId = "#"+id;
        if(element != null){
            if(element.getName().getLocalPart() == MessageConstants.XENC_REFERENCE_LIST_LNAME){
                ReferenceList list = (ReferenceList)element.getValue();
                List<JAXBElement<ReferenceType>> listElems= list.getDataReferenceOrKeyReference();
                for (int i=0;i<listElems.size();i++){
                    JAXBElement<ReferenceType> ref =listElems.get(i);
                    ReferenceType rt = ref.getValue();
                    if(rt.getURI().equals(tmpId)){
                        return true;
                    }
                }
            }
        }
        if(obj != null){
            if(obj instanceof ReferenceList){
                ReferenceList rl =  (ReferenceList)obj;
                List<JAXBElement<ReferenceType>> listElems= rl.getDataReferenceOrKeyReference();
                for (int i=0;i<listElems.size();i++){
                    JAXBElement<ReferenceType> ref =listElems.get(i);
                    ReferenceType rt = ref.getValue();
                    if(rt.getURI().equals(tmpId)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        writeTo(streamWriter);
    }
    
    private Marshaller getMarshaller() throws javax.xml.bind.JAXBException {      
        return JAXBUtil.createMarshaller(soapVersion);
    }
}
