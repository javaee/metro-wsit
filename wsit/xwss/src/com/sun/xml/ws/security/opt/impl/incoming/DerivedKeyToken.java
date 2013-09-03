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

package com.sun.xml.ws.security.opt.impl.incoming;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.security.opt.api.NamespaceContextInfo;
import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.incoming.processor.SecurityTokenProcessor;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.OutputStream;
import java.security.Key;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;

/**
 *
 * @author Ashutosh.Shahi@sun.com
 */
public class DerivedKeyToken implements SecurityHeaderElement, NamespaceContextInfo, SecurityElementWriter {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN,
            LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN_BUNDLE);
    
    private static final String SECURITY_TOKEN_REFERENCE = "SecurityTokenReference".intern();
    private static final String LENGTH = "Length".intern();
    private static final String OFFSET = "Offset".intern();
    private static final String GENERATION = "Generation".intern();
    private static final String NONCE = "Nonce".intern();
    private static final String LABEL = "Label".intern();
    
    private static final int SECURITY_TOKEN_REFERENCE_ELEMENT = 3;
    private static final int LENGTH_ELEMENT = 4;
    private static final int OFFSET_ELEMENT = 5;
    private static final int GENERATION_ELEMENT = 6;
    private static final int NONCE_ELEMENT = 7;
    private static final int LABEL_ELEMENT = 8;
    
    private String id = "";
    private String namespaceURI = "";
    private String localName = "";
    private long offset = 0;
    private long length = 32;
    private String label = null;
    private String nonce = null;
    private byte[] decodedNonce = null;
    private long generation = -1;
    private HashMap<String,String> nsDecls;
    
    private Key originalKey = null;
    
    private JAXBFilterProcessingContext pc = null;
    private WSSPolicy inferredKB = null;
    
    /** Creates a new instance of DerivedKeyTokenProcessor */
    public DerivedKeyToken(XMLStreamReader reader,JAXBFilterProcessingContext pc, HashMap nsDecls) throws XMLStreamException, XWSSecurityException {
        this.pc = pc;
        this.nsDecls = nsDecls;
        process(reader);
        
    }
    
    public Key getKey() throws XWSSecurityException{
        String dataEncAlgo = null;
        if(pc.getAlgorithmSuite() != null){
            dataEncAlgo = ((JAXBFilterProcessingContext)pc).getAlgorithmSuite().getEncryptionAlgorithm();
        } else{
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1818_ALGORITHM_NOTSET_DERIVEDKEY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1818_ALGORITHM_NOTSET_DERIVEDKEY());
        }
        
        try{
            byte[] secret = originalKey.getEncoded();
            com.sun.xml.ws.security.DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret, decodedNonce, label);
            String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo);
            
            return dkt.generateSymmetricKey(jceAlgo);
            
        }catch(Exception ex){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1819_ERROR_SYMMKEY_DERIVEDKEY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1819_ERROR_SYMMKEY_DERIVEDKEY(),ex);
        }
        
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
    
    private void process(XMLStreamReader reader) throws XMLStreamException, XWSSecurityException {
        id = reader.getAttributeValue(MessageConstants.WSU_NS,"Id");
        namespaceURI = reader.getNamespaceURI();
        localName = reader.getLocalName();
        
        boolean offsetSpecified = false;
        boolean genSpecified = false;
        boolean invalidToken = false;
        
        if(StreamUtil.moveToNextElement(reader)){
            int refElement = getEventType(reader);
            while(reader.getEventType() != reader.END_DOCUMENT){
                switch(refElement){
                    case SECURITY_TOKEN_REFERENCE_ELEMENT : {
                        pc.getSecurityContext().setInferredKB(null);
                        SecurityTokenProcessor stp = new SecurityTokenProcessor(pc, null);
                        originalKey = stp.resolveReference(reader);
                        inferredKB = (WSSPolicy) pc.getSecurityContext().getInferredKB();
                        pc.getSecurityContext().setInferredKB(null);
                        break;
                    }
                    case LENGTH_ELEMENT : {
                        length = Integer.parseInt(reader.getElementText());
                        break;
                    }
                    case OFFSET_ELEMENT : {
                        offset = Integer.parseInt(reader.getElementText());
                        offsetSpecified = true;
                        break;
                    }
                    case GENERATION_ELEMENT : {
                        generation = Integer.parseInt(reader.getElementText());
                        genSpecified = true;
                        break;
                    }
                    case LABEL_ELEMENT : {
                        label = reader.getElementText();
                        break;
                    }
                    case NONCE_ELEMENT : {
                        if(reader instanceof XMLStreamReaderEx){
                            reader.next();
                            StringBuffer sb = null;
                            if(reader.getEventType() == XMLStreamReader.CHARACTERS &&
                                    reader.getEventType() != reader.END_ELEMENT){
                                CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                                if(charSeq instanceof Base64Data){
                                    Base64Data bd = (Base64Data) ((XMLStreamReaderEx)reader).getPCDATA();
                                    decodedNonce = bd.getExact();
                                } else{
                                    if(sb == null){
                                        sb = new StringBuffer();
                                    }
                                    for(int i=0;i<charSeq.length();i++){
                                        sb.append(charSeq.charAt(i));
                                    }
                                }
                                reader.next();
                            }
                            if(sb != null){
                                nonce  = sb.toString();
                                try{
                                    decodedNonce = Base64.decode(nonce);
                                }catch(Base64DecodingException dec){
                                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1820_ERROR_NONCE_DERIVEDKEY(id));
                                    throw new XWSSecurityException(LogStringsMessages.WSS_1820_ERROR_NONCE_DERIVEDKEY(id), dec);
                                }
                            }/*else{
                                reader.next();
                            }*/
                        } else{
                            nonce = reader.getElementText();
                            try {
                                decodedNonce = Base64.decode(nonce);
                            } catch (Base64DecodingException ex) {
                                logger.log(Level.SEVERE, LogStringsMessages.WSS_1820_ERROR_NONCE_DERIVEDKEY(id));
                                throw new XWSSecurityException(LogStringsMessages.WSS_1820_ERROR_NONCE_DERIVEDKEY(id), ex);
                            }
                        }
                        
                        break;
                    }
                    default :{
                        throw new XWSSecurityException("Element name "+reader.getName()+" is not recognized under DerivedKeyToken");
                    }
                }
                
                if(!StreamUtil.isStartElement(reader) && StreamUtil.moveToNextStartOREndElement(reader) &&
                        StreamUtil._break(reader, "DerivedKeyToken", MessageConstants.WSSC_NS)){
                    StreamUtil.moveToNextStartOREndElement(reader);
                    break;
                }else{
                    if(reader.getEventType() != XMLStreamReader.START_ELEMENT){
                        StreamUtil.moveToNextStartOREndElement(reader);
                        boolean isBreak = false;
                        while(reader.getEventType() == XMLStreamReader.END_ELEMENT){
                            if(StreamUtil._break(reader,"DerivedKeyToken", MessageConstants.WSSC_NS)){
                                isBreak = true;
                                StreamUtil.moveToNextStartOREndElement(reader);
                                break;
                            }
                            StreamUtil.moveToNextStartOREndElement(reader);
                        }
                        if(isBreak)
                            break;
                    }
                }
                refElement = getEventType(reader);
            }
        }
        
        //Verify if the DKT is correct
        if(offsetSpecified && genSpecified){
            invalidToken = true;
        }
        
        if(invalidToken){
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1821_INVALID_DKT_TOKEN());
            throw new XWSSecurityException(LogStringsMessages.WSS_1821_INVALID_DKT_TOKEN());
        }
    }
    
    
    private int getEventType(XMLStreamReader reader) {
        if(reader.getEventType()== XMLStreamReader.START_ELEMENT){
            if(reader.getLocalName() == SECURITY_TOKEN_REFERENCE){
                return SECURITY_TOKEN_REFERENCE_ELEMENT;
            }
            
            if(reader.getLocalName() == LENGTH){
                return LENGTH_ELEMENT;
            }
            
            if(reader.getLocalName() == OFFSET){
                return OFFSET_ELEMENT;
            }
            
            if(reader.getLocalName() == GENERATION){
                return GENERATION_ELEMENT;
            }
            
            if(reader.getLocalName() == NONCE){
                return NONCE_ELEMENT;
            }
            
            if(reader.getLocalName() == LABEL){
                return LABEL_ELEMENT;
            }
        }
        return -1;
    }
    
    public HashMap<String, String> getInscopeNSContext() {
        return nsDecls;
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        throw new UnsupportedOperationException();
    }
    
    public WSSPolicy getInferredKB(){
        return inferredKB;
    }
    
}
