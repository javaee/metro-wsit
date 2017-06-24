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

package com.sun.xml.ws.security.opt.impl.incoming.processor;

import org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.EncryptedKey;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.security.Key;
import java.util.logging.Logger;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.jvnet.staxex.XMLStreamReaderEx;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;
import java.security.PublicKey;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class KeyInfoProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);
    
    private static String KEYINFO = "KeyInfo".intern();
    private static String SECURITY_TOKEN_REFERENCE = "SecurityTokenReference".intern();
    private static final int SECURITY_TOKEN_REFERENCE_ELEMENT = 3;
    private static final int ENCRYPTED_KEY_ELEMENT = 4;
    private static final int KEY_VALUE_ELEMENT = 5;
    private static final int RSA_KEY_VALUE_ELEMENT = 6;
    private static final int DSA_KEY_VALUE_ELEMENT = 7;
    private static final int MODULUS_ELEMENT = 8;
    private static final int EXPONENT_ELEMENT = 9;
    private static final int X509_DATA_ELEMENT = 10;
    private static final int BINARY_SECRET_ELEMENT = 11;
    private static final String RSA_KEY_VALUE = "RSAKeyValue";
    private static final String DSA_KEY_VALUE = "DSAKeyValue";
    private static final String ENCRYPTED_KEY = "EncryptedKey";
    private static final String KEY_VALUE = "KeyValue";
    private static final String EXPONENT = "Exponent";
    private static final String MODULUS = "Modulus";
    private static final String X509_DATA = "X509Data";
    private static final String X509Certificate = "X509Certificate";
    private static final String BINARY_SECRET = "BinarySecret";
    private boolean strPresent = false;
    private JAXBFilterProcessingContext pc = null;
    private XMLStreamWriter canonWriter = null;
    
    private boolean isSAMLSubjectConfirmationKeyInfo = false;
    
    private Purpose purpose = null;
    /** Creates a new instance of KeyInfoProcessor */
    public KeyInfoProcessor(JAXBFilterProcessingContext pc) {
        this.pc = pc;
        ((NamespaceContextEx)pc.getNamespaceContext()).addSignatureNS();
    }
    
    public KeyInfoProcessor(JAXBFilterProcessingContext pc, XMLStreamWriter canonWriter,Purpose purpose) {
        this.pc = pc;
        this.canonWriter = canonWriter;
        this.purpose = purpose;
    }
    
    public KeyInfoProcessor(JAXBFilterProcessingContext pc,Purpose purpose) {
        this.pc = pc;
        this.purpose = purpose;
    }

    public KeyInfoProcessor(JAXBFilterProcessingContext pc,Purpose purpose, boolean isSAMLSCKey) {
        this.pc = pc;
        this.purpose = purpose;
        this.isSAMLSubjectConfirmationKeyInfo = isSAMLSCKey;
    }

    public Key getKey(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException{
        return processKeyInfo(reader);
    }
    /**
     * gets the event type from the reader,processes it and returns the key calculated from it
     * @param reader XMLStreamReader
     * @return Key
     * @throws javax.xml.stream.XMLStreamException
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private Key processKeyInfo(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException{
        //Start element for KeyInfo
        Key retKey =  null;
        if(canonWriter != null)
            StreamUtil.writeStartElement(reader, canonWriter);
        while(reader.hasNext() && !StreamUtil._break(reader,KEYINFO,MessageConstants.DSIG_NS)){
            reader.next();
            int eventType = getEventType(reader);
            switch(eventType){
                case SECURITY_TOKEN_REFERENCE_ELEMENT : {
                    SecurityTokenProcessor stp = new SecurityTokenProcessor(pc, canonWriter, purpose);
                    retKey =  stp.resolveReference(reader);
                    strPresent = true;
                    break;
                }
                case ENCRYPTED_KEY_ELEMENT :{
                    EncryptedKey ek = new EncryptedKey(reader,pc,null,true);
                    String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                    if (pc.getAlgorithmSuite() != null) {
                        dataEncAlgo = pc.getAlgorithmSuite().getEncryptionAlgorithm();
                    }
                    retKey = ek.getKey(dataEncAlgo);
                    break;
                }
                case KEY_VALUE_ELEMENT :{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                    retKey = new KeyValueProcessor(pc,canonWriter).processKeyValue(reader);
                    //if the purpose is signature verification, we need to make sure we
                    //trust the certificate. in case of HOK SAML this can be the cert of the IP
                    if (!this.isSAMLSubjectConfirmationKeyInfo && this.purpose == Purpose.VERIFY ) {
                        X509Certificate cer = null;
                        try{
                            cer = pc.getSecurityEnvironment().getCertificate(pc.getExtraneousProperties(),(PublicKey)retKey, false);
                        }catch(XWSSecurityException ex){
                            //Ignore it. Its a RSA KeyPair scenario, so certificate won't be present in the server truststore'
                        }
                        
                        if(cer != null){
                            pc.getSecurityEnvironment().validateCertificate(cer, pc.getExtraneousProperties());
                        }
                        pc.getSecurityContext().setInferredKB(new AuthenticationTokenPolicy.KeyValueTokenBinding());
                    }
                    break;
                }
                case BINARY_SECRET_ELEMENT:{
                    reader.next();
                    retKey = buildBinarySecret(reader);
                    break;
                }
                case X509_DATA_ELEMENT:{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                    StreamUtil.moveToNextStartOREndElement(reader, canonWriter);
                    if(reader.getLocalName() == X509Certificate && reader.getNamespaceURI() == MessageConstants.DSIG_NS){
                        reader.next();
                        StringBuffer sb = null;
                        byte [] value = null;
                        CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                        if(charSeq instanceof Base64Data){
                            Base64Data bd = (Base64Data) ((XMLStreamReaderEx)reader).getPCDATA();
                            value = bd.getExact();
                            if(canonWriter != null){
                                String ev = Base64.encode(value);
                                canonWriter.writeCharacters(ev);
                            }
                        }else {
                            sb = new StringBuffer();
                            while(reader.getEventType() == reader.CHARACTERS && reader.getEventType() != reader.END_ELEMENT){
                                charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                                for(int i=0;i<charSeq.length();i++){
                                    sb.append(charSeq.charAt(i));
                                }
                                reader.next();
                            }
                            String dv = sb.toString();
                            if(canonWriter != null){
                                canonWriter.writeCharacters(dv);
                            }
                            try{
                                value = Base64.decode(dv);
                            }catch(Base64DecodingException dec){
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("MODULUS"),dec);
                                throw new XWSSecurityException(LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("MODULUS"));
                            }
                            X509Certificate cert = buildCertificate(new ByteArrayInputStream(value));
                            if(purpose == Purpose.DECRYPT){
                                retKey = pc.getSecurityEnvironment().getPrivateKey(
                                        pc.getExtraneousProperties(),cert);
                            } else  if (purpose == Purpose.VERIFY) {
                                retKey = cert.getPublicKey();
                            }
                            
                            if(!this.isSAMLSubjectConfirmationKeyInfo && purpose == Purpose.VERIFY){
                                //if the purpose is signature verification, we need to make sure we
                                //trust the certificate. in case of HOK SAML this can be the cert of the IP
                                pc.getSecurityEnvironment().validateCertificate(cert, pc.getExtraneousProperties());
                            }
                           
                        }
                    }
                    break;
                }
            }
        }
        if(reader.hasNext()){
            if(canonWriter != null){
                StreamUtil.writeCurrentEvent(reader,canonWriter);
            }
        }
        reader.next();
        return retKey;
    }
    /**
     * parses the BinarySecret element and returns the Key
     * @param reader XMLStreamReader
     * @return Key
     * @throws com.sun.xml.wss.XWSSecurityException
     * @throws javax.xml.stream.XMLStreamException
     */
    private Key buildBinarySecret(XMLStreamReader reader)throws XWSSecurityException,XMLStreamException{
        byte [] value = null;
        if(reader.getEventType() == reader.CHARACTERS){
            if(reader instanceof XMLStreamReaderEx){
                CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                if(charSeq instanceof Base64Data){
                    Base64Data bd = (Base64Data) ((XMLStreamReaderEx)reader).getPCDATA();
                    value = bd.getExact();
                    if(canonWriter != null){
                        String ev = Base64.encode(value);
                        canonWriter.writeCharacters(ev);
                    }
                }else{
                    String dv = readCharacters(reader);
                    try{
                        value = Base64.decode(dv);
                    }catch(Base64DecodingException dec){
                        logger.log(Level.SEVERE, LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("MODULUS"),dec);
                        throw new XWSSecurityException(LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("MODULUS"));
                    }
                }
            }else{
                String dv = readCharacters(reader);
                try{
                    value = Base64.decode(dv);
                }catch(Base64DecodingException dec){
                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("MODULUS"),dec);
                    throw new XWSSecurityException(LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("MODULUS"));
                }
            }
        }
        String algorithm = "AES"; // hardcoding for now
        if (pc.getAlgorithmSuite() != null) {
            algorithm = SecurityUtil.getSecretKeyAlgorithm(pc.getAlgorithmSuite().getEncryptionAlgorithm());
        }
        return new SecretKeySpec(value,algorithm);
    }
    
    private String readCharacters(XMLStreamReader reader)throws XMLStreamException{
        StringBuffer sb = new StringBuffer();
        while(reader.getEventType() == reader.CHARACTERS && reader.getEventType() != reader.END_ELEMENT){
            CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
            for(int i=0;i<charSeq.length();i++){
                sb.append(charSeq.charAt(i));
            }
            reader.next();
        }
        String dv = sb.toString();
        if(canonWriter != null){
            canonWriter.writeCharacters(dv);
        }
        return dv;
    }
    /**
     * generates a X509 certificate from the caertificate value 
     * @param certValue  InputStream
     * @return X509Certificate
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private X509Certificate buildCertificate(InputStream certValue)throws XWSSecurityException{
        try {
            CertificateFactory certFact;
            certFact = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFact.generateCertificate(certValue);
        } catch (CertificateException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1605_ERROR_GENERATING_CERTIFICATE(ex),ex);
            throw new XWSSecurityException(LogStringsMessages.WSS_1605_ERROR_GENERATING_CERTIFICATE(ex));
        }
    }
    
    /**
     * returns the event type of the XMLStreamReader..
     * @param reader XMLStreamReader
     * @return  int
     * @throws javax.xml.stream.XMLStreamException
     */
    private int getEventType(XMLStreamReader reader) throws XMLStreamException{
        if(reader.getEventType() == reader.START_ELEMENT){
            if(reader.getLocalName() == SECURITY_TOKEN_REFERENCE){
                return SECURITY_TOKEN_REFERENCE_ELEMENT;
            }
            if(reader.getLocalName() == ENCRYPTED_KEY){
                return ENCRYPTED_KEY_ELEMENT;
            }
            if(reader.getLocalName() == KEY_VALUE){
                return KEY_VALUE_ELEMENT;
            }
            if(reader.getLocalName() ==X509_DATA){
                return X509_DATA_ELEMENT;
            }
            if(reader.getLocalName() == BINARY_SECRET){
                return BINARY_SECRET_ELEMENT;
            }
        }
        return -1;
    }
    
    public boolean hasSTR(){
        return strPresent;
    }
}
