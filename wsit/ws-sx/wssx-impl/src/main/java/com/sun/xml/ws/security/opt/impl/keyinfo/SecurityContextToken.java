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

package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.secconv.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.secconv.impl.bindings.SecurityContextTokenType;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.stream.XMLStreamException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.wss.WSITXMLFactory;

/**
 * SecurityContextToken Implementation
 * @author Manveen Kaur manveen.kaur@sun.com
 * @author K.Venugopal@sun.com
 */
public class SecurityContextToken extends SecurityContextTokenType implements SecurityHeaderElement, SecurityElementWriter, com.sun.xml.ws.security.SecurityContextToken {
    
    public final String SECURITY_CONTEXT_TOKEN = "SecurityContextToken";
    
    private String instance = null;
    private URI identifier = null;
    private List extElements = null;
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    
    public SecurityContextToken(URI identifier, String instance, String wsuId, SOAPVersion sv) {
        if (identifier != null) {
            setIdentifier(identifier);
        }
        if (instance != null) {
            setInstance(instance);
        }
        
        if (wsuId != null){
            setWsuId(wsuId);
        }
        this.soapVersion = sv;
    }
    
    // useful for converting from JAXB to our owm impl class
    @SuppressWarnings("unchecked")
    public SecurityContextToken(SecurityContextTokenType sTokenType, SOAPVersion sv){
        List<Object> list = sTokenType.getAny();
        for (int i = 0; i < list.size(); i++) {
            Object object = list.get(i);
            if(object instanceof JAXBElement){
                JAXBElement obj = (JAXBElement)object;
                
                String local = obj.getName().getLocalPart();
                if (local.equalsIgnoreCase("Instance")) {
                    setInstance((String)obj.getValue());
                } else if (local.equalsIgnoreCase("Identifier")){
                    try {
                        setIdentifier(new URI((String)obj.getValue()));
                    }catch (URISyntaxException ex){
                        throw new RuntimeException(ex);
                    }
                }
            }else{
                getAny().add(object);
                if(extElements == null){
                    extElements = new ArrayList();
                    extElements.add(object);
                }
            }
        }
        
        setWsuId(sTokenType.getId());
        this.soapVersion = sv;
    }
    
    public URI getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(URI identifier) {
        this.identifier = identifier;
        JAXBElement<String> iElement =
                  (new ObjectFactory()).createIdentifier(identifier.toString());
        getAny().add(iElement);
    }
    
    public String getInstance() {
        return instance;
    }
    
    public void setInstance(String instance) {
        this.instance = instance;
        JAXBElement<String> iElement =
                  (new ObjectFactory()).createInstance(instance);
        getAny().add(iElement);
    }
    
    public void setWsuId(String wsuId){
        setId(wsuId);
        
    }
    
    public String getWsuId(){
        return getId();
    }
    
    public String getType() {
        return SECURITY_CONTEXT_TOKEN;
    }
    
    public Object getTokenValue() {
        try {
            DocumentBuilderFactory dbf = WSITXMLFactory.createDocumentBuilderFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING);
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            javax.xml.bind.Marshaller marshaller = WSTrustElementFactory.getContext().createMarshaller();
            JAXBElement<SecurityContextTokenType> tElement =  (new ObjectFactory()).createSecurityContextToken((SecurityContextTokenType)this);
            marshaller.marshal(tElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    public List getExtElements() {
        return extElements;
    }
    
    public String getNamespaceURI() {
        return "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    }
    
    public String getLocalPart() {
        return "SecurityContextToken";
    }
    
    public String getAttribute(String nsUri, String localName) {
        throw new UnsupportedOperationException();
    }
    
    public String getAttribute(QName name) {
        throw new UnsupportedOperationException();
    }
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        try {
            JAXBElement<SecurityContextTokenType> sct =
                      new com.sun.xml.ws.security.secconv.impl.bindings.ObjectFactory().createSecurityContextToken(this);
            Marshaller writer = getMarshaller();
            writer.marshal(sct, os);
        } catch (javax.xml.bind.JAXBException ex) {
            throw new XWSSecurityRuntimeException(ex);
        }
    }
    /**
     * writes the SecurityContextToken to the XMLStreamWriter
     * @param streamWriter
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        JAXBElement<SecurityContextTokenType> sct =
                  new com.sun.xml.ws.security.secconv.impl.bindings.ObjectFactory().createSecurityContextToken(this);
        try {
            // If writing to Zephyr, get output stream and use JAXB UTF-8 writer
            Marshaller writer = getMarshaller();
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    
                    writer.marshal(sct, os);
                    return;
                }
            }
            writer.marshal(sct, streamWriter);
        } catch (JAXBException e) {
            throw new XMLStreamException(e);
        }
    }
    
    public byte[] canonicalize(String algorithm, List<AttributeNS> namespaceDecls) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isCanonicalized() {
        return false;
    }
    
    
    private Marshaller getMarshaller() throws JAXBException{
        return JAXBUtil.createMarshaller(soapVersion);
    }

    public boolean refersToSecHdrWithId(String id) {
        return false;
    }
    /**
     * writes the SecurityContextToken to the XMLStreamWriter
     * @param streamWriter
     * @param props
     * @throws javax.xml.stream.XMLStreamException
     */
    @SuppressWarnings("unchecked")
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        try{
            Marshaller marshaller = getMarshaller();
            Iterator<Map.Entry<Object, Object>> itr = props.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Object, Object> entry = itr.next();
                marshaller.setProperty((String)entry.getKey(), entry.getValue());
            }
            writeTo(streamWriter);
        }catch(JAXBException jbe){
            throw new XMLStreamException(jbe);
        }
    }       
}
