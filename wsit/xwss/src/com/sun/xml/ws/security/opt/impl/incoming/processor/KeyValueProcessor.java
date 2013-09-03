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


package com.sun.xml.ws.security.opt.impl.incoming.processor;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.StreamUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.jvnet.staxex.Base64Data;
import org.jvnet.staxex.XMLStreamReaderEx;
import java.util.logging.Level;
import com.sun.xml.wss.logging.impl.opt.LogStringsMessages;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class KeyValueProcessor {
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_DOMAIN,
            LogDomainConstants.IMPL_OPT_DOMAIN_BUNDLE);
    //private static String KEYINFO = "KeyInfo".intern();
    //private static String SECURITY_TOKEN_REFERENCE = "SecurityTokenReference".intern();
    private static final int SECURITY_TOKEN_REFERENCE_ELEMENT = 3;
    private static final int ENCRYPTED_KEY_ELEMENT = 4;
    private static final int KEY_VALUE_ELEMENT = 5;
    private static final int RSA_KEY_VALUE_ELEMENT = 6;
    private static final int DSA_KEY_VALUE_ELEMENT = 7;
    private static final int MODULUS_ELEMENT = 8;
    private static final int EXPONENT_ELEMENT = 9;
    
    private static final String RSA_KEY_VALUE = "RSAKeyValue";
    private static final String DSA_KEY_VALUE = "DSAKeyValue";
    private static final String ENCRYPTED_KEY = "EncryptedKey";
    private static final String KEY_VALUE = "KeyValue";
    private static final String EXPONENT = "Exponent";
    private static final String MODULUS = "Modulus";
    
    private JAXBFilterProcessingContext pc = null;
    private XMLStreamWriter canonWriter = null;
    
    /** Creates a new instance of KeyValueProcessor */
    public KeyValueProcessor(JAXBFilterProcessingContext pc,XMLStreamWriter writer) {
        this.pc = pc;
        this.canonWriter = writer;
    }
    
    public Key processKeyValue(XMLStreamReader reader)throws XMLStreamException,XWSSecurityException{
        boolean done = false;
        Key retKey = null;
        
        while(!done && (reader.hasNext() && !_breaKeyValue(reader))){
            reader.next();
            int event = getKeyValueEventType(reader);
            switch(event){
                case RSA_KEY_VALUE_ELEMENT :{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                    retKey = processRSAKeyValue(reader);
                    break;
                }
                case DSA_KEY_VALUE_ELEMENT :{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                    break;
                }default :{
                    if(_breaKeyValue(reader)){
                        done = true;
                        break;
                    }
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                }
            }
        }
        return retKey;
    }
    
    
    public Key processRSAKeyValue(XMLStreamReader reader)throws XMLStreamException,XWSSecurityException{
        BigInteger modulus  = null;
        BigInteger exponent = null;
        boolean done = false;
        while(!done && (reader.hasNext() && !_breakRSAKeyValue(reader))){
            reader.next();
            int event  = getRSAKVEventType(reader);
            switch(event){
                case MODULUS_ELEMENT :{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                    reader.next();
                    StringBuffer sb = null;
                    byte [] value = null;
                    CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                    if(charSeq instanceof Base64Data){
                        Base64Data bd = (Base64Data) ((XMLStreamReaderEx)reader).getPCDATA();
                        value = bd.getExact();
                        modulus = new BigInteger(1,value);
                        if(canonWriter != null){
                            String ev = Base64.encode(value);
                            canonWriter.writeCharacters(ev);
                        }
                    }else {
                        sb = new StringBuffer();
                        
                        
                        while(reader.getEventType() == reader.CHARACTERS && reader.getEventType() != reader.END_ELEMENT){
                            charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                            for(int i=0;i<charSeq.length();i++){
                                if(charSeq.charAt(i)== '\n'){
                                    continue;
                                }
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
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("EXPONENT"));
                            throw new XWSSecurityException(LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("EXPONENT"));
                        }
                        modulus = new BigInteger(1,value);
                        
                    }
                    break;
                }
                case EXPONENT_ELEMENT :{
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                    reader.next();
                    StringBuffer sb = null;
                    byte [] value = null;
                    CharSequence charSeq = ((XMLStreamReaderEx)reader).getPCDATA();
                    if(charSeq instanceof Base64Data){
                        Base64Data bd = (Base64Data) ((XMLStreamReaderEx)reader).getPCDATA();
                        value = bd.getExact();
                        exponent = new BigInteger(1,value);
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
                            logger.log(Level.SEVERE, LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("EXPONENT"));
                            throw new XWSSecurityException(LogStringsMessages.WSS_1606_ERROR_RSAKEYINFO_BASE_64_DECODING("EXPONENT"));
                        }
                        exponent = new BigInteger(1,value);
                    }
                    break;
                }
                default:{
                    if(_breakRSAKeyValue(reader)){
                        done = true;
                        break;
                    }
                    if(canonWriter != null){
                        StreamUtil.writeCurrentEvent(reader,canonWriter);
                    }
                }
            }
        }
        try{
            KeyFactory rsaFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaKeyspec = new RSAPublicKeySpec(modulus,exponent);
            PublicKey pk = rsaFactory.generatePublic(rsaKeyspec);
            return pk;
        }catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1607_ERROR_RSAPUBLIC_KEY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1607_ERROR_RSAPUBLIC_KEY(), ex);
        } catch (InvalidKeySpecException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1607_ERROR_RSAPUBLIC_KEY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1607_ERROR_RSAPUBLIC_KEY(), ex);
        }
        
    }
    
    private int getKeyValueEventType(XMLStreamReader reader)throws XMLStreamException{
        if(reader.getEventType() == reader.START_ELEMENT){
            if(reader.getLocalName() == RSA_KEY_VALUE){
                return RSA_KEY_VALUE_ELEMENT;
            }
            
            if(reader.getLocalName() == DSA_KEY_VALUE){
                return DSA_KEY_VALUE_ELEMENT;
            }
        }
        return -1;
    }
    
    private int getRSAKVEventType(XMLStreamReader reader)throws XMLStreamException{
        if(reader.getEventType() == reader.START_ELEMENT){
            if(reader.getLocalName() == MODULUS){
                return MODULUS_ELEMENT;
            }
            
            if(reader.getLocalName() == EXPONENT){
                return EXPONENT_ELEMENT;
            }
        }
        return -1;
    }
    
    private boolean isRSAKeyValue(XMLStreamReader reader)throws XMLStreamException{
        if(reader.getLocalName() == RSA_KEY_VALUE){
            return true;
        }
        return false;
    }
    
    private boolean isKeyValue(XMLStreamReader reader)throws XMLStreamException{
        if(reader.getLocalName() == KEY_VALUE){
            return true;
        }
        return false;
    }
    
    private boolean _breaKeyValue(XMLStreamReader reader)throws XMLStreamException{
        if(reader.getEventType() == XMLStreamReader.END_ELEMENT && isKeyValue(reader)){
            if(canonWriter != null){
                StreamUtil.writeCurrentEvent(reader,canonWriter);
            }
            return true;
        }
        return false;
    }
    
    private boolean _breakRSAKeyValue(XMLStreamReader reader)throws XMLStreamException{
        if(reader.getEventType() == XMLStreamReader.END_ELEMENT && isRSAKeyValue(reader)){
            if(canonWriter != null){
                StreamUtil.writeCurrentEvent(reader,canonWriter);
            }
            return true;
        }
        return false;
    }
    
    
}
