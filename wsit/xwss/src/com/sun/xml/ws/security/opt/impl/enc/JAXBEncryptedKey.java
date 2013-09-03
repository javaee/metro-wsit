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
import com.sun.xml.security.core.xenc.EncryptedKeyType;
import com.sun.xml.security.core.xenc.ReferenceList;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.security.opt.api.EncryptedKey;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.wss.logging.LogDomainConstants;
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
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import javax.crypto.Cipher;
import javax.xml.crypto.Data;
import javax.xml.bind.JAXBException;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.security.core.xenc.ReferenceType;
import javax.xml.stream.XMLStreamException;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class JAXBEncryptedKey implements EncryptedKey,
        SecurityHeaderElement, SecurityElementWriter {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private EncryptedKeyType ekt = null;
    //private Data data = null;
    private Key dataEnckey = null;
    private Key dkEK = null;
    CryptoProcessor dep = null;
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    
    /** Creates a new instance of JAXBEncryptedKey */
    public JAXBEncryptedKey(EncryptedKeyType ekt,Key kk,Key dk,SOAPVersion soapVersion) throws XWSSecurityException{
        this.ekt = ekt;
        this.dkEK = kk;
        this.dataEnckey = dk;
        this.soapVersion = soapVersion;
        dep = new CryptoProcessor(Cipher.WRAP_MODE, ekt.getEncryptionMethod().getAlgorithm(),dataEnckey,dkEK);
        
    }
    
    public void encrypt() {
    }
    
    public void decrypt() {
    }
    
    public String getId() {
        return ekt.getId();
    }
    
    public void setId(String id) {
        ekt.setId(id);
    }
    
    public String getNamespaceURI() {
        return "http://www.w3.org/2001/04/xmlenc#";
    }
    
    
    public String getLocalPart() {
        return "EncryptedKey";
    }
    
    
    public String getAttribute( String nsUri, String localName) {
        throw new UnsupportedOperationException();
    }
    
    
    public String getAttribute( QName name) {
        throw new UnsupportedOperationException();
    }
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public <T> T readAsJAXB(Unmarshaller unmarshaller) throws javax.xml.bind.JAXBException {
        throw new UnsupportedOperationException();
    }
    
    public <T> T readAsJAXB(com.sun.xml.bind.api.Bridge<T> bridge, com.sun.xml.bind.api.BridgeContext context) throws javax.xml.bind.JAXBException {
        throw new UnsupportedOperationException();
    }
    
    public <T> T readAsJAXB(com.sun.xml.bind.api.Bridge<T> bridge) throws javax.xml.bind.JAXBException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        Marshaller writer;
        try {
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    writeTo(os);
                    return;
                }
            }
            writer = getMarshaller();
            JAXBElement ed = getEK(writer);
            writer.marshal(ed,streamWriter);
        } catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1921_ERROR_WRITING_ENCRYPTEDKEY(ex.getMessage()), ex);
        } catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1921_ERROR_WRITING_ENCRYPTEDKEY(ex.getMessage()), ex);
        }
    }
    
    public void writeTo(OutputStream os)  {
        Marshaller writer;
        try {
            writer = getMarshaller();
            
            JAXBElement ed = getEK(writer);
            writer.marshal(ed,os);
        } catch (javax.xml.bind.JAXBException ex) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1921_ERROR_WRITING_ENCRYPTEDKEY(ex.getMessage()), ex);
        } catch (com.sun.xml.wss.XWSSecurityException ex) {
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1921_ERROR_WRITING_ENCRYPTEDKEY(ex.getMessage()), ex);
        }
    }
    
    private JAXBElement getEK(Marshaller writer) throws JAXBException, XWSSecurityException{
        
        CVAdapter adapter = new CVAdapter(dep);
        writer.setAdapter(CVAdapter.class,adapter);
        com.sun.xml.security.core.xenc.ObjectFactory obj = new com.sun.xml.security.core.xenc.ObjectFactory();
        return obj.createEncryptedKey(ekt);
    }
    
    public void writeTo(javax.xml.soap.SOAPMessage saaj) throws javax.xml.soap.SOAPException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        throw new UnsupportedOperationException();
    }
    
    public byte[] canonicalize(String algorithm, List<com.sun.xml.wss.impl.c14n.AttributeNS> namespaceDecls) {
        throw new UnsupportedOperationException();
    }
    
    public boolean isCanonicalized() {
        throw new UnsupportedOperationException();
    }
    
    private Marshaller getMarshaller() throws JAXBException{
        return JAXBUtil.createMarshaller(soapVersion);
    }
    
    public ReferenceList getReferenceList() {
        return ekt.getReferenceList();
    }
    
    public boolean hasReferenceList() {
        return (ekt.getReferenceList() != null);
    }
    public boolean refersToSecHdrWithId(String id) {
        KeyInfo ki = (KeyInfo) this.ekt.getKeyInfo();
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
        List list = null;
        if(getReferenceList() != null){
            list = getReferenceList().getDataReferenceOrKeyReference();
        }
        if(list == null){
            return false;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("#");
        sb.append(id);
        String idref = sb.toString();
        for(int i=0;i< list.size();i++){
            JAXBElement<ReferenceType> rt =(JAXBElement<ReferenceType> )list.get(i);
            ReferenceType ref = (ReferenceType) rt.getValue();
            if(ref.getURI().equals(idref)){
                return true;
            }
        }
        return false;
    }
    
    public void setReferenceList(ReferenceList list) {
        ekt.setReferenceList(list);
    }
    
    public Key getKey() {
        return dataEnckey;
    }
    
    public byte[] getCipherValue(){
        return dep.getCipherValueOfEK();
    }
    
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
            logger.log(Level.SEVERE,LogStringsMessages.WSS_1921_ERROR_WRITING_ENCRYPTEDKEY(jbe.getMessage()), jbe);
            throw new XMLStreamException(jbe);
        }
    }
    
}
