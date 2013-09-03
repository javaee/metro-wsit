/*
 * JAXBEncryptedData.java
 *
 * Created on August 4, 2006, 2:56 PM
 */

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

package com.sun.xml.ws.security.opt.impl.enc;

import com.sun.xml.security.core.xenc.CVAdapter;
import com.sun.xml.security.core.xenc.EncryptedDataType;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.security.opt.api.EncryptedData;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import javax.xml.crypto.Data;
import javax.crypto.Cipher;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author K.Venugopal@sun.com
 */

public class JAXBEncryptedData implements EncryptedData,
        SecurityHeaderElement, SecurityElementWriter {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private EncryptedDataType  edt = null;
    private Data data = null;
    private Key key = null;
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    private CryptoProcessor dep = null;
    /** Creates a new instance of JAXBEncryptedData */
    public JAXBEncryptedData(EncryptedDataType edt,Data data,Key key,SOAPVersion soapVersion) {
        this.edt = edt;
        this.key  = key;
        this.data = data;
        this.soapVersion = soapVersion;
    }
    
    public JAXBEncryptedData(EncryptedDataType edt,Data data,SOAPVersion soapVersion) {
        this.edt = edt;
        this.data = data;
        this.soapVersion = soapVersion;
    }
    
    public String getEncryptedLocalName(){
        if(data instanceof SSEData){
            SecurityElement se = ((SSEData)data).getSecurityElement();
            return se.getLocalPart();
        }
        return "";
    }
    
    public String getEncryptedId(){
        if(data instanceof SSEData){
            SecurityElement se = ((SSEData)data).getSecurityElement();
            return se.getId();
        }
        return "";
    }
    
    public void encrypt() {        
    }
    
    public void decrypt() {        
    }
    
    public String getId() {
        return edt.getId();
    }
    
    public void setId(String id) {
        if(edt.getId() == null || edt.getId().length() ==0){
            edt.setId(id);
        }
    }    
    
    public String getNamespaceURI() {
        return MessageConstants.XENC_NS;
    }    
    
    public String getLocalPart() {
        return MessageConstants.ENCRYPTED_DATA_LNAME;
    }
    /**
     * writes the jaxb encrypted data to an XMLStreamWriter
     * @param streamWriter javax.xml.stream.XMLStreamWriter
     * @throws javax.xml.stream.XMLStreamException
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        try {
            
            if (streamWriter instanceof Map && !(dep != null)) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    writeTo(os);
                    return;
                }
            }
            Marshaller writer = getMarshaller();
            
            if(dep == null){
                dep = new CryptoProcessor(Cipher.ENCRYPT_MODE, edt.getEncryptionMethod().getAlgorithm(), data, key);
                
                if(streamWriter instanceof StAXEXC14nCanonicalizerImpl){
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try{
                        dep.encryptData(bos);
                        
                    }catch(IOException ie){
                        logger.log(Level.SEVERE, LogStringsMessages.WSS_1920_ERROR_CALCULATING_CIPHERVALUE(),ie);
                        throw new XMLStreamException("Error occurred while calculating Cipher Value");
                    }
                    dep.setEncryptedDataCV(bos.toByteArray());
                }
            }
            CVAdapter adapter = new CVAdapter(dep);
            writer.setAdapter(CVAdapter.class,adapter);
            
            com.sun.xml.security.core.xenc.ObjectFactory obj = new com.sun.xml.security.core.xenc.ObjectFactory();
            JAXBElement ed = obj.createEncryptedData(edt);
            writer.marshal(ed,streamWriter);
        }catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1919_ERROR_WRITING_ENCRYPTEDDATA(ex.getMessage()), ex);
        }catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1919_ERROR_WRITING_ENCRYPTEDDATA(ex.getMessage()), ex);
        }
    }
    /**
     * writes the jaxb encrypted data to an XMLStreamWriter
     * @param streamWriter javax.xml.stream.XMLStreamWriter
     * @param props HashMap
     * @throws XMLStreamException
     */
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException {
        try{
            Marshaller marshaller = getMarshaller();
            Iterator<Map.Entry<Object, Object>> itr = props.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Object, Object> entry = itr.next();
                marshaller.setProperty((String)entry.getKey(), entry.getValue());
            }
            writeTo(streamWriter);
        }catch(JAXBException jbe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1919_ERROR_WRITING_ENCRYPTEDDATA(jbe.getMessage()), jbe);
            throw new XMLStreamException(jbe);
        }
    }
    /**
     * writes the jaxb encrypted data to an XMLStreamWriter
     * @param os java.io.OutputStream
     */
    public void writeTo(java.io.OutputStream os)  {
        try {
            Marshaller writer = getMarshaller();
            CryptoProcessor dep;
            
            dep = new CryptoProcessor(Cipher.ENCRYPT_MODE, edt.getEncryptionMethod().getAlgorithm(), data, key);
            
            CVAdapter adapter = new CVAdapter(dep);
            writer.setAdapter(CVAdapter.class,adapter);
            com.sun.xml.security.core.xenc.ObjectFactory obj = new com.sun.xml.security.core.xenc.ObjectFactory();
            JAXBElement ed = obj.createEncryptedData(edt);
            writer.marshal(ed,os);
        }catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1919_ERROR_WRITING_ENCRYPTEDDATA(ex.getMessage()), ex);
        }catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1919_ERROR_WRITING_ENCRYPTEDDATA(ex.getMessage()), ex);
        }
    }
    
    public void writeTo(javax.xml.soap.SOAPMessage saaj) throws javax.xml.soap.SOAPException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        throw new UnsupportedOperationException();
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
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    /**
     * finds whether the this security header element refers to the element with given id
     * @param id String
     * @return boolean
     */
    public boolean refersToSecHdrWithId(String id) {
        KeyInfo ki = (KeyInfo) this.edt.getKeyInfo();
        if(ki != null){
            List list = ki.getContent();
            if(list.size() >0 ){
                Object data = ((JAXBElement)list.get(0)).getValue();
                if(data instanceof SecurityHeaderElement){
                   if(((SecurityHeaderElement)data).refersToSecHdrWithId(id)){
                       return true;
                   }
                }
            }
        }        
        if(data instanceof SSEData){
            SecurityElement se = ((SSEData)data).getSecurityElement();
            if(se instanceof SecurityHeaderElement ){
                return ((SecurityHeaderElement)se).refersToSecHdrWithId(id);
            }
        }
        return false;
    }
        
}
