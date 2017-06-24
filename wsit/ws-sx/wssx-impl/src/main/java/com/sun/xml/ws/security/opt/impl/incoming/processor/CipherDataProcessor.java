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
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import com.sun.xml.wss.logging.impl.opt.crypto.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class CipherDataProcessor {
    
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_OPT_CRYPTO_DOMAIN_BUNDLE);
    
    private static String CIPHER_VALUE = "CipherValue".intern();
    private static String CIPHER_REFERENCE = "CipherReference".intern();
    private static String TRANSFORMS = "Transforms".intern();
    private static String TRANSFORM = "Transform".intern();
    private Base64Data bd = null;
    private byte[] cipherValue;
    private JAXBFilterProcessingContext pc = null;
    boolean hasCipherReference = false;
    String attachmentContentId = null;
    String attachmentContentType = null;
    /** Creates a new instance of CipherDataProcessor */
    public CipherDataProcessor(JAXBFilterProcessingContext pc) {
        this.pc = pc;
    }
    /**
     * processes the cipher data and sets the cipher value
     * @param reader XMLStreamReader
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public void process(XMLStreamReader reader) throws XWSSecurityException{
        try {
            if(StreamUtil.moveToNextElement(reader)){
                if(reader.getLocalName() == CIPHER_VALUE){
                    if(reader instanceof XMLStreamReaderEx){
                        reader.next();
                        if(reader.getEventType() == XMLStreamReader.CHARACTERS){
                            CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                            if(charSeq instanceof Base64Data){
                                bd = (Base64Data) charSeq;
                            }else{
                                try {
                                    cipherValue = Base64.decode(StreamUtil.getCV((XMLStreamReaderEx)reader));
                                } catch (Base64DecodingException ex) {
                                    logger.log(Level.SEVERE, LogStringsMessages.WSS_1922_ERROR_DECODING_CIPHERVAL(ex),ex);
                                    throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK, LogStringsMessages.WSS_1922_ERROR_DECODING_CIPHERVAL(ex), ex);
                                }
                            }
                        }
                    }else{
                        
                        try {
                            //cipherValue = Base64.decode(reader.getElementText());
                            cipherValue = Base64.decode(StreamUtil.getCV(reader));
                        } catch (Base64DecodingException ex) {
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1922_ERROR_DECODING_CIPHERVAL(ex),ex);
                            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK, LogStringsMessages.WSS_1922_ERROR_DECODING_CIPHERVAL(ex), ex);
                        }
                        
                    }
                    //reader.next();//move to END OF CIPHER VALUE
                    reader.next();//move to END OF CIPHER DATA
                    reader.next();//move to NEXT ELEMENT
                    return;
                } else if(reader.getLocalName() == CIPHER_REFERENCE){
                    hasCipherReference = true;
                    String attachUri = reader.getAttributeValue(null,"URI");
                    if (attachUri.startsWith("cid:")) {
                        attachUri = attachUri.substring("cid:".length());
                    }
                    String algorithm = null;
                    if(StreamUtil.moveToNextElement(reader)){
                        if(reader.getLocalName() == TRANSFORMS){
                            if(StreamUtil.moveToNextElement(reader)){
                                if(reader.getLocalName() == TRANSFORM){
                                    algorithm = reader.getAttributeValue(null,"Algorithm");
                                    reader.next(); // Move to end of Transform
                                }
                            }
                            reader.next(); // Move to end of Transforms
                        }
                    }
                    if(algorithm != null && algorithm.equals(MessageConstants.SWA11_ATTACHMENT_CIPHERTEXT_TRANSFORM)){
                        AttachmentSet attachmentSet = pc.getSecurityContext().getAttachmentSet();
                        Attachment as = attachmentSet.get(attachUri);//sm.getAttachment(attachUri);
                        cipherValue = as.asByteArray();
                        attachmentContentId = as.getContentId();
                        attachmentContentType = as.getContentType();
                        reader.next(); // Move to end of CipherReference
                        reader.next(); // Move to NEXT ELEMENT
                        return;
                    } else {
                        logger.log(Level.SEVERE, LogStringsMessages.WSS_1928_UNRECOGNIZED_CIPHERTEXT_TRANSFORM(algorithm));
                        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK, LogStringsMessages.WSS_1928_UNRECOGNIZED_CIPHERTEXT_TRANSFORM(algorithm), 
                                new XWSSecurityException(LogStringsMessages.WSS_1928_UNRECOGNIZED_CIPHERTEXT_TRANSFORM(algorithm)));
                    }
                }
            }
            reader.next();
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1923_ERROR_PROCESSING_CIPHERVAL(ex),ex);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1923_ERROR_PROCESSING_CIPHERVAL(ex),ex);
        }
        logger.log(Level.SEVERE, LogStringsMessages.WSS_1923_ERROR_PROCESSING_CIPHERVAL("unexpected element:"+reader.getLocalName()));
        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1923_ERROR_PROCESSING_CIPHERVAL("unexpected element:"+reader.getLocalName()), null);
        
    }
    /**
     *
     * @return InputStream
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public InputStream readAsStream() throws XWSSecurityException{
        
        if(bd != null ){
            try {
                return bd.getInputStream();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, LogStringsMessages.WSS_1923_ERROR_PROCESSING_CIPHERVAL(ex),ex);
                throw new XWSSecurityException(LogStringsMessages.WSS_1923_ERROR_PROCESSING_CIPHERVAL(ex));
            }
        }
        if(cipherValue != null){
            return new ByteArrayInputStream(cipherValue);
        }
        logger.log(Level.SEVERE, LogStringsMessages.WSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA());
        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA(),null);
    }
    
    /**
     *
     * @return byte[] cipherValue
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public byte[] readAsBytes() throws XWSSecurityException{
        if(cipherValue != null){
            return cipherValue;
        }
        if(bd != null ){
            cipherValue = bd.getExact();
            return cipherValue;
        }
        logger.log(Level.SEVERE, LogStringsMessages.WSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA());
        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_FAILED_CHECK,LogStringsMessages.WSS_1924_CIPHERVAL_MISSINGIN_CIPHERDATA(),null);
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
}
