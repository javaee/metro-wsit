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

import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.PolicyBuilder;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.enc.CryptoProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.CipherDataProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.KeyInfoProcessor;
import com.sun.xml.ws.security.opt.impl.incoming.processor.ReferenceListProcessor;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Cipher;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class EncryptedKey implements SecurityHeaderElement, NamespaceContextInfo, SecurityElementWriter, PolicyBuilder{
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private static final String ENCRYPTION_METHOD = "EncryptionMethod".intern();
    private static final String REFERENCE_LIST = "ReferenceList".intern();
    private static final String CIPHER_DATA = "CipherData".intern();
    private static final String KEYINFO = "KeyInfo".intern();
    private static final String DIGEST_METHOD = "DigestMethod".intern();
    private static final String KEY_SIZE = "KeySize".intern();
    
    private static final int KEYINFO_ELEMENT = 1;
    private static final int ENCRYPTIONMETHOD_ELEMENT = 2;
    
    private static final int REFERENCE_LIST_ELEMENT = 4;
    private static final int CIPHER_DATA_ELEMENT = 5;
    private static final int KEY_SIZE_ELEMENT = 6;
    private static final int DIGEST_METHOD_ELEMENT = 7;
    
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private String encryptionMethod ="";
    private Key keyEncKey = null;
    private ArrayList<String> referenceList = null;
    private JAXBFilterProcessingContext pc = null;
    private Key dataEncKey = null;
    private ArrayList<String> pendingRefList = null;
    
    private HashMap<String,String> nsDecls;
    
    private CryptoProcessor cp = null;
    private CipherDataProcessor cdp = null;
    
    private EncryptionPolicy encPolicy = null;
    private WSSPolicy inferredKB = null;
    private boolean ignoreEKSHA1 = false;
    private boolean emPresent = false;
    /** Creates a new instance of EncryptedKey */
    @SuppressWarnings("unchecked")
    public EncryptedKey(XMLStreamReader reader,JAXBFilterProcessingContext pc, HashMap nsDecls) throws XMLStreamException, XWSSecurityException {
        this.pc = pc;
        this.nsDecls = nsDecls;
        process(reader);
    }
    @SuppressWarnings("unchecked")
    public EncryptedKey(XMLStreamReader reader,JAXBFilterProcessingContext pc, HashMap nsDecls,boolean ignoreEKSHA1) throws XMLStreamException, XWSSecurityException {
        this.pc = pc;
        this.ignoreEKSHA1 = ignoreEKSHA1;
        this.nsDecls = nsDecls;
        process(reader);
    }
    
    public boolean refersToSecHdrWithId(final String id) {
        throw new UnsupportedOperationException();
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
    @SuppressWarnings("unchecked")
    private void process(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException{
        id = reader.getAttributeValue(null,"Id");
        
        if(pc.isBSP()){
            String tmp = reader.getAttributeValue(null,"Recipient");
            if(tmp != null){
                //log BSP R5602
                logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_5602_ENCRYPTEDKEY_RECIPIENT(id));
                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5602_ENCRYPTEDKEY_RECIPIENT(id));
            }
            
            String mt = reader.getAttributeValue(null,"MimeType");
            if(mt != null){
                //log BSP R5622
                logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_5622_ENCRYPTEDKEY_MIMETYPE(id));
                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5622_ENCRYPTEDKEY_MIMETYPE(id));
            }
            
            String et = reader.getAttributeValue(null,"Encoding");
            if(et != null){
                //log BSP R5623
                logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_5623_ENCRYPTEDKEY_ENCODING(id));
                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5623_ENCRYPTEDKEY_ENCODING(id));
            }
        }
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        
        if(StreamUtil.moveToNextElement(reader)){
            int refElement = getEventType(reader);
            while(reader.getEventType() != reader.END_DOCUMENT){
                switch(refElement){
                    case ENCRYPTIONMETHOD_ELEMENT : {
                        processEncryptionMethod(reader);
                        reader.next();
                        emPresent = true;
                        break;
                    }
                    case KEYINFO_ELEMENT:{
                        pc.getSecurityContext().setInferredKB(null);
                        pc.setExtraneousProperty("EncryptedKey", "true");
                        KeyInfoProcessor kip = new KeyInfoProcessor(pc,Purpose.DECRYPT);
                        keyEncKey = kip.getKey(reader);
                        pc.removeExtraneousProperty("EncryptedKey");
                        inferredKB = (WSSPolicy) pc.getSecurityContext().getInferredKB();
                        pc.getSecurityContext().setInferredKB(null);
                        break;
                    }
                    case REFERENCE_LIST_ELEMENT :{
                        encPolicy = new EncryptionPolicy();
                        encPolicy.setFeatureBinding(new EncryptionPolicy.FeatureBinding());
                        ReferenceListProcessor rlp = new ReferenceListProcessor(encPolicy);
                        rlp.process(reader);
                        referenceList = rlp.getReferences();
                        pendingRefList = (ArrayList<String>) referenceList.clone();
                        break;
                    }
                    case CIPHER_DATA_ELEMENT :{
                        cdp = new CipherDataProcessor(pc);
                        cdp.process(reader);
                        if(!ignoreEKSHA1){
                            try{
                                byte[] decodedCipher = cdp.readAsBytes();//Base64.decode(cipherValue);
                                byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                                String encEkSha1 = Base64.encode(ekSha1);
                                if(!pc.isSAMLEK()){
                                    //added for handling PDK with Initiator Token;
                                    if(pc.getExtraneousProperty(MessageConstants.EK_SHA1_VALUE) == null){
                                    pc.setExtraneousProperty(MessageConstants.EK_SHA1_VALUE, encEkSha1);
                                    }
                                }
                            } catch(NoSuchAlgorithmException nsae){
                                throw new XWSSecurityException(nsae);
                            }
                        }
                        cp = new CryptoProcessor(Cipher.UNWRAP_MODE,encryptionMethod,keyEncKey);
                        
                        break;
                    }
                    default :{
                        if(StreamUtil.isStartElement(reader)){
                            throw new XWSSecurityException("Element name "+reader.getName()+" is not recognized under EncryptedKey");
                        }
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
        
        if(pc.isBSP()){
            if(emPresent){
                logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_5603_ENCRYPTEDKEY_ENCRYPTIONMEHOD(id));
                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_5603_ENCRYPTEDKEY_ENCRYPTIONMEHOD(id));
            }
        }
    }
    
    private boolean shouldBreak(XMLStreamReader reader)throws XMLStreamException{
        if(StreamUtil._break(reader, "EncryptedKey", MessageConstants.XENC_NS)){
            return true;
        }
        if(reader.getEventType() == XMLStreamReader.END_DOCUMENT ){
            return true;
        }
        return false;
    }
    
    private void processEncryptionMethod(XMLStreamReader reader) throws XMLStreamException,XWSSecurityException{
        encryptionMethod = reader.getAttributeValue(null,"Algorithm");
        if(encryptionMethod == null || encryptionMethod.length() == 0){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1925_EMPTY_ENCMETHOD_ED());
            throw new XWSSecurityException(LogStringsMessages.WSS_1925_EMPTY_ENCMETHOD_ED());
        }
        
        while(reader.getEventType() != reader.END_DOCUMENT){
            int eventType = getEventType(reader);
            switch(eventType){
                case DIGEST_METHOD_ELEMENT :{
                    //String localName = reader.getLocalName();
                    break;
                }
                case KEY_SIZE_ELEMENT :{
                    //String localName = reader.getLocalName();
                    break;
                }
                default:{
                    break;
                }
            }
            if(reader.getEventType() == XMLStreamReader.END_ELEMENT && reader.getLocalName() == ENCRYPTION_METHOD){
                break;
            }
            reader.next();
        }
    }
    
    private int getEventType(XMLStreamReader reader){
        if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == ENCRYPTION_METHOD){
                return ENCRYPTIONMETHOD_ELEMENT;
            }
            
            if(reader.getLocalName() == CIPHER_DATA){
                return CIPHER_DATA_ELEMENT;
            }
            if(reader.getLocalName() == REFERENCE_LIST){
                return REFERENCE_LIST_ELEMENT;
            }
            
            if(reader.getLocalName() == KEYINFO){
                return KEYINFO_ELEMENT;
            }
            
            if(reader.getLocalName() == DIGEST_METHOD){
                return DIGEST_METHOD_ELEMENT;
            }
            if(reader.getLocalName() == KEY_SIZE){
                return KEY_SIZE_ELEMENT;
            }
        }
        return -1;
    }
    
    public List<String> getReferenceList() {
        return referenceList;
    }
    
    public List<String> getPendingReferenceList() {
        return pendingRefList;
    }
    
    public Key getKey(String encAlgo) throws XWSSecurityException{
        if(dataEncKey == null){
            try {
                dataEncKey = cp.decryptKey(cdp.readAsBytes(), encAlgo);
            }catch (IOException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1927_ERROR_DECRYPT_ED("EncryptedKey"));
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1927_ERROR_DECRYPT_ED("EncryptedKey"),ex);
            }
            if (!ignoreEKSHA1) {
                //Added for handling PDK for Inittiator Token;
                if (pc.getExtraneousProperty(MessageConstants.SECRET_KEY_VALUE) == null) {
                    pc.setExtraneousProperty(MessageConstants.SECRET_KEY_VALUE, dataEncKey);
                }
            }
        }
        return dataEncKey;
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public WSSPolicy getPolicy() {
        return encPolicy;
    }
    
    public WSSPolicy getInferredKB(){
        return inferredKB;
    }
    
}

