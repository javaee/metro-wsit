/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.enc.CryptoProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.CipherDataProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.KeyInfoProcessor;
import com.sun.xml.ws.security.opt.impl.util.CheckedInputStream;
import com.sun.xml.ws.security.opt.impl.util.DecryptedInputStream;
import com.sun.xml.ws.security.opt.impl.util.FilteredXMLStreamReader;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class EncryptedData implements SecurityHeaderElement, SecurityElementWriter {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private static final String ENCRYPTION_METHOD = "EncryptionMethod".intern();
    private static final String CIPHER_DATA = "CipherData".intern();
    private static final String KEY_INFO = "KeyInfo".intern();
    
    private static final int KEYINFO_ELEMENT = 1;
    private static final int ENCRYPTIONMETHOD_ELEMENT = 2;
    private static final int CIPHER_DATA_ELEMENT = 5;
    
    private JAXBFilterProcessingContext pc = null;
    
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private String encryptionMethod ="";
    private Key dataEncKey = null;
    private InputStream cin = null;
    private CryptoProcessor cp = null;
    CipherDataProcessor cdp = null;
    private boolean hasCipherReference = false;
    private byte[] decryptedMimeData = null;
    private String attachmentContentId = null;
    private String attachmentContentType = null;
    private String mimeType = null;
    private WSSPolicy inferredKB = null;
    HashMap<String,String> parentNS = null;
    
    /** Creates a new instance of EncryptedData */
    public EncryptedData(XMLStreamReader reader,JAXBFilterProcessingContext pc, HashMap<String,String> parentNS) throws XMLStreamException, XWSSecurityException{
        this.pc = pc;
        this.parentNS = parentNS;
        process(reader);
    }
    
    public EncryptedData(XMLStreamReader reader, Key dataEncKey,JAXBFilterProcessingContext pc, HashMap<String,String> parentNS) throws XMLStreamException, XWSSecurityException{
        this.dataEncKey = dataEncKey;
        this.pc = pc;
        this.parentNS = parentNS;
        process(reader);
    }
    
    private void process(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException{
        id = reader.getAttributeValue(null,"Id");
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        mimeType = reader.getAttributeValue(null, "MimeType");
        
        if(StreamUtil.moveToNextElement(reader)){
            int refElement = getEventType(reader);
            while(reader.getEventType() != reader.END_DOCUMENT){
                switch(refElement){
                    case ENCRYPTIONMETHOD_ELEMENT :{
                        encryptionMethod = reader.getAttributeValue(null,"Algorithm");
                        if(encryptionMethod == null || encryptionMethod.length() == 0){
                            if(pc.isBSP()){
                                logger.log(Level.SEVERE, com.sun.xml.wss.logging.LogStringsMessages.BSP_5601_ENCRYPTEDDATA_ENCRYPTIONMETHOD(id));
                                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5601_ENCRYPTEDDATA_ENCRYPTIONMETHOD(id));
                            }
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1925_EMPTY_ENCMETHOD_ED());
                            throw new XWSSecurityException(LogStringsMessages.WSS_1925_EMPTY_ENCMETHOD_ED());
                        }
                        
                        if(pc.isBSP() && !(encryptionMethod.equals("http://www.w3.org/2001/04/xmlenc#tripledes-cbc") ||
                                encryptionMethod.equals("http://www.w3.org/2001/04/xmlenc#aes128-cbc")||
                                encryptionMethod.equals( "http://www.w3.org/2001/04/xmlenc#aes256-cbc"))){
                            logger.log(Level.SEVERE, com.sun.xml.wss.logging.LogStringsMessages.BSP_5626_KEYENCRYPTIONALGO());
                            throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5626_KEYENCRYPTIONALGO());
                        }
                        reader.next();
                        break;
                    }
                    case KEYINFO_ELEMENT:{
                        pc.getSecurityContext().setInferredKB(null);
                        KeyInfoProcessor kip = new KeyInfoProcessor(pc,Purpose.DECRYPT);
                        dataEncKey = kip.getKey(reader);
                        inferredKB = (WSSPolicy) pc.getSecurityContext().getInferredKB();
                        pc.getSecurityContext().setInferredKB(null);
                        if(!kip.hasSTR() && pc.isBSP()){
                            logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_5426_ENCRYPTEDKEYINFO(id));
                            throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5426_ENCRYPTEDKEYINFO(id));
                        }
                        break;
                    }
                    case CIPHER_DATA_ELEMENT :{
                        cdp = new CipherDataProcessor(pc);
                        cdp.process(reader);
                        hasCipherReference = cdp.hasCipherReference();
                        if(hasCipherReference){
                            attachmentContentId = cdp.getAttachmentContentId();
                            attachmentContentType = cdp.getAttachmentContentType();
                        }
                        break;
                    }
                    default :{
                        //    throw new XWSSecurityException("Element name "+reader.getName()+" is not recognized under EncryptedData");
                        
                    }
                }
                if(shouldBreak(reader)){
                    break;
                }
                if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
                    if(getEventType(reader) == -1)
                        reader.next();
                    
                }else{
                    reader.next();
                }
                
                refElement = getEventType(reader);
            }
        }
        if(reader.hasNext()){
            reader.next();
        }
    }
    
    public boolean shouldBreak(XMLStreamReader reader)throws XMLStreamException{
        if(StreamUtil._break(reader, "EncryptedData", MessageConstants.XENC_NS)){
            return true;
        }
        if(reader.getEventType() == XMLStreamReader.END_DOCUMENT ){
            return true;
        }
        return false;
    }
    
    public String getEncryptionAlgorithm(){
        return encryptionMethod;
    }
    
    public Key getKey(){
        return dataEncKey;
    }
    
    public InputStream getCipherInputStream() throws XWSSecurityException{
        if(dataEncKey == null){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1926_ED_KEY_NOTSET());
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1926_ED_KEY_NOTSET(),null);
        }
        cp = new CryptoProcessor(Cipher.DECRYPT_MODE,encryptionMethod,dataEncKey);
        try {
            cin = cp.decryptData(cdp.readAsStream());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1927_ERROR_DECRYPT_ED("EncryptedData"));
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1927_ERROR_DECRYPT_ED("EncryptedData"),ex);
        }
        
        return cin;
    }
    
    public InputStream getCipherInputStream(Key key) throws XWSSecurityException{
        dataEncKey = key;
        return getCipherInputStream();
    }
    
    public XMLStreamReader getDecryptedData() throws XMLStreamException, XWSSecurityException{
        if(cin == null){
            cin = getCipherInputStream();
        }
        if(logger.isLoggable(Level.FINEST)){
            ByteArrayOutputStream decryptedContent = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            try {
                for(int len=-1;(len=cin.read(buf))!=-1;){
                    decryptedContent.write(buf,0,len);
                }
                logger.log(Level.FINEST, LogStringsMessages.WSS_1951_ENCRYPTED_DATA_VALUE(new String(decryptedContent.toByteArray())));
                cin =  new ByteArrayInputStream(decryptedContent.toByteArray());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        CheckedInputStream ccin = null;
        try{
            ccin = new CheckedInputStream(cin);
            if(ccin.isEmpty()){
                return null;
            }
        } catch(IOException ioe){
            throw new XWSSecurityException(ioe);
        }
        
        DecryptedInputStream decryptedStream = new DecryptedInputStream(ccin, parentNS);
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader reader = xif.createXMLStreamReader(decryptedStream);
        
        return new FilteredXMLStreamReader(reader);
    }
    
    public XMLStreamReader getDecryptedData(Key key) throws XMLStreamException, XWSSecurityException{
        if(cin == null){
            cin = getCipherInputStream(key);
        }
        return getDecryptedData();
    }
    
    public byte[] getDecryptedMimeData(Key key) throws XWSSecurityException{
        dataEncKey = key;
        return getDecryptedMimeData();
    }
    
    public byte[] getDecryptedMimeData() throws XWSSecurityException{
        if(decryptedMimeData == null){
            if(dataEncKey == null){
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1926_ED_KEY_NOTSET());
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1926_ED_KEY_NOTSET(),null);
            }
            cp = new CryptoProcessor(Cipher.DECRYPT_MODE,encryptionMethod,dataEncKey);
            try {
                decryptedMimeData = cp.decryptData(cdp.readAsBytes());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1927_ERROR_DECRYPT_ED("EncryptedData"));
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1927_ERROR_DECRYPT_ED("EncryptedData"),ex);
            }
        }
        return decryptedMimeData;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    public String getLocalPart() {
        return localName;
    }
    
    public XMLStreamReader readHeader() throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(OutputStream os) {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(XMLStreamWriter streamWriter) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    private int getEventType(XMLStreamReader reader){
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == ENCRYPTION_METHOD){
                return ENCRYPTIONMETHOD_ELEMENT;
            }
            
            if(reader.getLocalName() == KEY_INFO){
                return KEYINFO_ELEMENT;
            }
            
            if(reader.getLocalName() == CIPHER_DATA){
                return CIPHER_DATA_ELEMENT;
            }
        }
        return -1;
    }
    
    public boolean refersToSecHdrWithId(final String id) {
        throw new UnsupportedOperationException();
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public WSSPolicy getInferredKB(){
        return inferredKB;
    }
    
    public boolean hasCipherReference(){
        return hasCipherReference;
    }
    
    public String getAttachmentContentId(){
        return attachmentContentId;
    }
    
    public String getAttachmentContentType(){
        return attachmentContentType;
    }
    
    public String getAttachmentMimeType(){
        return mimeType;
    }
    
}
