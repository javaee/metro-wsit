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
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.secext11.EncryptedHeaderType;
import com.sun.xml.ws.security.secext11.ObjectFactory;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.c14n.AttributeNS;
import com.sun.xml.wss.impl.c14n.StAXEXC14nCanonicalizerImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
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
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.Data;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import javax.crypto.Cipher;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class EncryptedHeader
        implements SecurityHeaderElement, SecurityElementWriter{
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private EncryptedHeaderType eht = null;
    private boolean isCanonicalized = false;
    //private ObjectFactory objFac = new ObjectFactory();
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    private Data data = null;
    private Key key = null;
    private CryptoProcessor dep = null;
    
    /** Creates a new instance of EncryptedHeader */
    public EncryptedHeader(EncryptedHeaderType eht, Data data,Key key,SOAPVersion soapVersion) {
        this.eht = eht;
        this.key  = key;
        this.data = data;
        this.soapVersion = soapVersion;
    }
    
    public boolean refersToSecHdrWithId(String id) {
        KeyInfo ki = (KeyInfo) eht.getEncryptedData().getKeyInfo();
        if(ki != null){
            List list = ki.getContent();
            if(list.size() >0 ){
                Object data = list.get(0);
                if(data instanceof SecurityHeaderElement){
                    return ((SecurityHeaderElement)data).refersToSecHdrWithId(id);
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
    
    public String getId() {
        return eht.getId();
    }
    
    public void setId(String id) {
        eht.setId(id);
    }
    
    public String getNamespaceURI() {
        return MessageConstants.WSSE11_NS;
    }
    
    public String getLocalPart() {
        return MessageConstants.ENCRYPTED_HEADER_LNAME;
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public byte[] canonicalize(String algorithm, List<AttributeNS> namespaceDecls) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isCanonicalized() {
        return isCanonicalized;
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
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
                dep = new CryptoProcessor(Cipher.ENCRYPT_MODE, eht.getEncryptedData().getEncryptionMethod().getAlgorithm(), data, key);
                
                if(streamWriter instanceof StAXEXC14nCanonicalizerImpl){
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try{
                        dep.encryptData(bos);
                        
                    }catch(IOException ie){
                        logger.log(Level.SEVERE, LogStringsMessages.WSS_1920_ERROR_CALCULATING_CIPHERVALUE());
                        throw new XMLStreamException("Error occurred while calculating Cipher Value");
                    }
                    dep.setEncryptedDataCV(bos.toByteArray());
                }
            }
            CVAdapter adapter = new CVAdapter(dep);
            writer.setAdapter(CVAdapter.class,adapter);
            
            com.sun.xml.ws.security.secext11.ObjectFactory obj = new com.sun.xml.ws.security.secext11.ObjectFactory();
            JAXBElement eh = obj.createEncryptedHeader(eht);
            writer.marshal(eh,streamWriter);
        } catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1916_ERROR_WRITING_ECRYPTEDHEADER(ex.getMessage()), ex);
        } catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1916_ERROR_WRITING_ECRYPTEDHEADER(ex.getMessage()), ex);
        }
    }
    
    public void writeTo(XMLStreamWriter streamWriter, HashMap props) throws XMLStreamException {
        try{
            Marshaller marshaller = getMarshaller();
            Iterator<Map.Entry<Object, Object>> itr = props.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Object, Object> entry = itr.next();
                marshaller.setProperty((String)entry.getKey(), entry.getValue());
            }
            writeTo(streamWriter);
        }catch(JAXBException jbe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1916_ERROR_WRITING_ECRYPTEDHEADER(jbe.getMessage()), jbe);
            throw new XMLStreamException(jbe);
        }
    }
    
    public void writeTo(OutputStream os) {
        try {
            Marshaller writer = getMarshaller();
            CryptoProcessor dep;
            
            dep = new CryptoProcessor(Cipher.ENCRYPT_MODE, eht.getEncryptedData().getEncryptionMethod().getAlgorithm(), data, key);
            CVAdapter adapter = new CVAdapter(dep);
            writer.setAdapter(CVAdapter.class,adapter);
            com.sun.xml.ws.security.secext11.ObjectFactory obj = new com.sun.xml.ws.security.secext11.ObjectFactory();
            JAXBElement eh = obj.createEncryptedHeader(eht);
            writer.marshal(eh,os);
        }catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1916_ERROR_WRITING_ECRYPTEDHEADER(ex.getMessage()), ex);
        }catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1916_ERROR_WRITING_ECRYPTEDHEADER(ex.getMessage()), ex);
        }
    }
    
    private Marshaller getMarshaller() throws JAXBException{
        return JAXBUtil.createMarshaller(soapVersion);
    }
    
}
