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

import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBValidateContext;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.KeySelectorImpl;
import com.sun.xml.ws.security.opt.impl.incoming.StreamWriterData;
import com.sun.xml.ws.security.opt.impl.incoming.URIResolver;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.math.BigInteger;
import java.security.Key;
import java.util.logging.Logger;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import javax.xml.crypto.KeySelectorException;
import javax.xml.stream.XMLStreamWriter;
import com.sun.xml.stream.buffer.AbstractCreatorProcessor;
import com.sun.xml.stream.buffer.XMLStreamBufferMark;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SecurityTokenProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN,
            LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN_BUNDLE);
    
    private static String SECURITY_TOKEN_REF = "SecurityTokenReference";
    private static String DIRECT_REFERENCE_ELEMENT = "Reference";
    private static String KEYIDENTIFIER_ELEMENT = "KeyIdentifier";
    private static String THUMBPRINT_ELEMENT = "Thumbprint";
    private static final String KEY_VALUE = "KeyValue";
    private static final String X509DATA_ELEMENT = "X509Data";
    private static final String X509ISSUERSERIAL_ELEMENT = "X509IssuerSerial";
    private static final String X509ISSUERNAME_ELEMENT = "X509IssuerName";
    private static final String X509SERIALNUMBER_ELEMENT = "X509SerialNumber";
    private static final String KEY_NAME = "KeyName";
    private static final int DIRECT_REFERENCE = 1;
    private static final int KEYIDENTIFIER = 2;
    private static final int THUMBPRINT = 3;
    private static final int KEY_VALUE_ELEMENT = 4;
    private static final int X509DATA = 5;
    private static final int X509ISSUERSERIAL = 6;
    private static final int X509ISSUERNAME = 7;
    private static final int X509SERIALNUMBER = 8;
    private static final int SECURITY_TOKEN_REFERENCE = 9;
    private static final int KEY_NAME_ELEMENT = 10;
    private JAXBFilterProcessingContext pc = null;
    private XMLStreamWriter canonWriter = null;
    private Purpose purpose = null;
    private String id = "";
    
    /** Creates a new instance of SecurityTokenProcessor */
    public SecurityTokenProcessor(JAXBFilterProcessingContext context,Purpose purpose) {
        this.pc = (JAXBFilterProcessingContext) context;
        this.purpose =purpose;
    }
    
    public SecurityTokenProcessor(JAXBFilterProcessingContext context, XMLStreamWriter canonWriter,Purpose purpose) {
        this.pc = (JAXBFilterProcessingContext) context;
        this.canonWriter = canonWriter;
        this.purpose =purpose;
    }
    
    /**
     *  resolves references and returns Key
     * @param reader  XMLStreamReader
     * @return Key
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    public Key resolveReference(XMLStreamReader reader) throws XWSSecurityException{
        
        Key resolvedKey = null;
        try{
            if(canonWriter != null)
                StreamUtil.writeStartElement(reader, canonWriter);
            id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
            
            if(id != null && id.length() >0){
                //cache STR
                if(reader instanceof AbstractCreatorProcessor){
                    Map<String, String> emptyMap = Collections.emptyMap();
                    XMLStreamBufferMark marker=  new XMLStreamBufferMark(emptyMap,(AbstractCreatorProcessor)reader);
                    pc.getElementCache().put(id,new StreamWriterData(marker));
                }
            }
            if(reader.getLocalName() == SECURITY_TOKEN_REF && reader.getNamespaceURI() == MessageConstants.WSSE_NS){
                while(reader.hasNext() && !StreamUtil._break(reader,SECURITY_TOKEN_REF,MessageConstants.WSSE_NS)){
                    reader.next();
                    int refType = getReferenceType(reader);
                    switch(refType){
                    case DIRECT_REFERENCE :{
                        resolvedKey = processDirectReference(reader);
                        break;
                    }
                    case KEYIDENTIFIER :{
                        resolvedKey = processKeyIdentifier(reader);
                        break;
                    }
                    case THUMBPRINT :{
                        break;
                    }
                    case KEY_VALUE_ELEMENT :{
                        if(canonWriter != null){
                            StreamUtil.writeCurrentEvent(reader,canonWriter);
                        }
                        resolvedKey = new KeyValueProcessor(pc,canonWriter).processKeyValue(reader);
                        break;
                    }
                    case X509DATA :{
                        resolvedKey = processX509Data(reader);
                        break;
                    }
                    case SECURITY_TOKEN_REFERENCE :{
                        if(pc.isBSP()){
                            logger.log(Level.SEVERE, com.sun.xml.wss.logging.LogStringsMessages.BSP_3057_STR_NOT_REF_STR());
                            throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_3057_STR_NOT_REF_STR());
                        }
                        break;
                    }
                    case KEY_NAME_ELEMENT :{
                        if(pc.isBSP()){
                            logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_3058_STR_VALUE_TYPE_NOTEMPTY());
                            throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_3058_STR_VALUE_TYPE_NOTEMPTY());
                        }
                        break;
                    }
                    }
                }
            }
            if(canonWriter != null){
                canonWriter.writeEndElement();
            }
            if(reader.hasNext()){
                reader.next();
            }
            
        }catch(XMLStreamException xe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1815_ERROR_PROCESSING_STR(),xe);
            throw new XWSSecurityException(LogStringsMessages.WSS_1815_ERROR_PROCESSING_STR(),xe);
        }
        return resolvedKey;
    }
    /**
     * gets the reference type from the XMLStreamReader like DIRECT_REFERENCE,KEYIDENTIFIER ..etc
     * @param reader XMLStreamReader
     * @return int
     */
    private int getReferenceType(XMLStreamReader reader){
        if(reader.getEventType() == reader.START_ELEMENT){
            if(reader.getLocalName() == DIRECT_REFERENCE_ELEMENT){
                return DIRECT_REFERENCE;
            }else if(reader.getLocalName() == KEYIDENTIFIER_ELEMENT){
                return KEYIDENTIFIER;
            }else if(reader.getLocalName() == THUMBPRINT_ELEMENT){
                return THUMBPRINT;
            }else if(reader.getLocalName() == KEY_VALUE){
                return KEY_VALUE_ELEMENT;
            }else if(reader.getLocalName() == X509DATA_ELEMENT){
                return X509DATA;
            }else if(reader.getLocalName() == KEY_NAME){
                return KEY_NAME_ELEMENT;
            }else if(reader.getLocalName() == SECURITY_TOKEN_REF){
                return SECURITY_TOKEN_REFERENCE;
            }
        }
        
        return -1;
        
    }
    
    private boolean moveToNextElement(XMLStreamReader reader) throws XMLStreamException{
        if(reader.hasNext()){
            reader.next();
            return true;
        }
        return false;
    }
    /**
     * processes the direct references and returns Key
     * @param reader XMLStreamReader
     * @return Key
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private Key processDirectReference(XMLStreamReader reader) throws XWSSecurityException{
        try{
            if(canonWriter != null){
                StreamUtil.writeStartElement(reader, canonWriter);
            }
          
            String uri = reader.getAttributeValue(null, "URI");
            
            if(this.pc.isBSP() && uri == null){
                logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_3062_STR_URIATTRIBUTE());
                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_3062_STR_URIATTRIBUTE());
            }
            String vt =reader.getAttributeValue(null,"ValueType");
            if(this.pc.isBSP() && (vt == null || vt.length() ==0)){
                logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_3058_STR_VALUE_TYPE_NOTEMPTY());
                throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_3058_STR_VALUE_TYPE_NOTEMPTY());
            }
            
            String wscInstance =reader.getAttributeValue(pc.getWSSCVersion(pc.getSecurityPolicyVersion()),"Instance");
            if(wscInstance != null){
                pc.setWSCInstance(wscInstance);
            }
            if(canonWriter != null){
                canonWriter.writeEndElement();
            }
            //resolve Key
            URIResolver resolver = new URIResolver(pc);
            JAXBValidateContext validateContext = new JAXBValidateContext();
            validateContext.setURIDereferencer(resolver);
            validateContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, pc);
            reader.next();
            reader.next();//move to STR End Element
            return KeySelectorImpl.resolveDirectReference(validateContext, vt, uri, purpose);
        } catch(KeySelectorException kse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1816_ERROR_REFERENCE_MECHANISM("Direct Reference"),kse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1816_ERROR_REFERENCE_MECHANISM("Direct Reference"), kse);
        } catch(XMLStreamException xse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("Direct Reference"),xse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("Direct Reference"), xse);
        }
    }
    /**
     * processes X509 Data and returns Key
     * @param reader XMLStreamReader
     * @return Key
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private Key processX509Data(XMLStreamReader reader) throws XWSSecurityException{
        try{
            Key returnKey = null;
            if(canonWriter != null)
                StreamUtil.writeStartElement(reader, canonWriter);
            while(reader.hasNext() && !StreamUtil._break(reader, X509DATA_ELEMENT, MessageConstants.DSIG_NS)){
                reader.next();
                int eventType = getEventTypeForX509Data(reader);
                switch(eventType){
                case X509ISSUERSERIAL :{
                    returnKey = processX509IssuerSerial(reader);
                    break;
                }
                }
            }
            return returnKey;
        } catch(XMLStreamException xse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("Issuer Serial"),xse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("Issuer Serial"), xse);
        }
    }
    /**
     * gets the EventType for X509Data from the given XMLStreamReader
     * @param reader XMLStreamReader
     * @return int
     * @throws javax.xml.stream.XMLStreamException
     */
    private int getEventTypeForX509Data(XMLStreamReader reader) throws XMLStreamException{
        if(reader.getEventType() == reader.START_ELEMENT){
            if(reader.getLocalName() == X509ISSUERSERIAL_ELEMENT){
                return X509ISSUERSERIAL;
            }
        }
        return -1;
    }
    /**
     * processes X509 Isser Serial
     * @param reader XMLStreamReader
     * @return Key
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private Key processX509IssuerSerial(XMLStreamReader reader) throws XWSSecurityException{
        try{
            Key returnKey = null;
            if(canonWriter != null)
                StreamUtil.writeStartElement(reader, canonWriter);
            BigInteger serialNumber = null;
            String issuerName = null;
            while(reader.hasNext() && !StreamUtil._break(reader, X509ISSUERSERIAL_ELEMENT, MessageConstants.DSIG_NS)){
                reader.next();
                int eventType = getEventTypeForX509IssuerSerial(reader);
                switch(eventType){
                case X509ISSUERNAME :{
                    if(canonWriter != null)
                        StreamUtil.writeStartElement(reader, canonWriter);
                    reader.next();
                    
                    issuerName = StreamUtil.getCV(reader);
                    if(canonWriter != null){
                        canonWriter.writeCharacters(issuerName);
                    }
                    break;
                }
                case X509SERIALNUMBER :{
                    if(canonWriter != null)
                        StreamUtil.writeStartElement(reader, canonWriter);
                    reader.next();
                    String tmp = StreamUtil.getCV(reader);
                    serialNumber = new BigInteger(tmp);
                    if(canonWriter != null){
                        canonWriter.writeCharacters(tmp);
                    }
                    break;
                }
                default:{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                }
                }
            }
            //resolve Key.
            if(issuerName != null && serialNumber != null){
                URIResolver resolver = new URIResolver(pc);
                JAXBValidateContext validateContext = new JAXBValidateContext();
                validateContext.setURIDereferencer(resolver);
                validateContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, pc);
                returnKey = KeySelectorImpl.resolveIssuerSerial(validateContext,issuerName,serialNumber,id,purpose);
            }
            return returnKey;
        } catch(KeySelectorException kse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1816_ERROR_REFERENCE_MECHANISM("Issuer Serial"),kse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1816_ERROR_REFERENCE_MECHANISM("Issuer Serial"), kse);
        } catch(XMLStreamException xse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("Issuer Serial"),xse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("Issuer Serial"), xse);
        }
    }
    /**
     * gets the EventType for X509IssuerSerial from the given XMLStreamReader
     * @param reader XMLStreamReader
     * @return int
     * @throws javax.xml.stream.XMLStreamException
     */
    private int getEventTypeForX509IssuerSerial(XMLStreamReader reader) throws XMLStreamException{
        if(reader.getEventType() == reader.START_ELEMENT){
            if(reader.getLocalName() == X509ISSUERNAME_ELEMENT){
                return X509ISSUERNAME;
            } else if(reader.getLocalName() == X509SERIALNUMBER_ELEMENT){
                return X509SERIALNUMBER;
            }
        }
        return -1;
    }
    /**
     * processes the X509 KeyIdentifier 
     * @param reader XMLStreamReader
     * @return Key
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private Key processKeyIdentifier(XMLStreamReader reader) throws XWSSecurityException{
        try{
            if(canonWriter != null)
                StreamUtil.writeStartElement(reader, canonWriter);
            String valueType = reader.getAttributeValue(null,"ValueType");
            //String encodingType = reader.getAttributeValue(null,"EncodingType");
            if(pc.isBSP()){                
                String et = reader.getAttributeValue(null, "EncodingType");                
                if(et == null || et.length() ==0){
                    logger.log(Level.SEVERE,com.sun.xml.wss.logging.LogStringsMessages.BSP_3071_STR_ENCODING_TYPE());
                    throw new XWSSecurityException(com.sun.xml.wss.logging.LogStringsMessages.BSP_3071_STR_ENCODING_TYPE());
                }
            }
            String keyIdentifier = null;
            if(reader instanceof XMLStreamReaderEx){
                reader.next();
                if(reader.getEventType() == XMLStreamReader.CHARACTERS){
                    CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                    if(charSeq instanceof Base64Data){
                        Base64Data bd = (Base64Data)charSeq;
                        keyIdentifier = bd.toString();
                    } else{
                        keyIdentifier = StreamUtil.getCV((XMLStreamReaderEx)reader);
                    }
                }
            } else{
                keyIdentifier = StreamUtil.getCV(reader);
            }
            
            if(canonWriter != null){
                // write KeyIdentifier Value
                canonWriter.writeCharacters(keyIdentifier);
                // End Element for KeyIdentifier
                canonWriter.writeEndElement();
            }
            reader.next();
            //resolve Key.
            URIResolver resolver = new URIResolver(pc);
            JAXBValidateContext validateContext = new JAXBValidateContext();
            validateContext.setURIDereferencer(resolver);
            validateContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, pc);
            return KeySelectorImpl.resolveKeyIdentifier(validateContext,valueType,keyIdentifier,id,purpose);
        }catch(KeySelectorException kse){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1816_ERROR_REFERENCE_MECHANISM("KeyIdentifier"),kse);
            throw new XWSSecurityException(LogStringsMessages.WSS_1816_ERROR_REFERENCE_MECHANISM("KeyIdentifier"), kse);
        }catch(XMLStreamException xe){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("KeyIdentifier"),xe);
            throw new XWSSecurityException(LogStringsMessages.WSS_1817_ERROR_REFERENCE_CANWRITER("KeyIdentifier"), xe);
        }
        
    }
}
