/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

/*
 * UsernameToken.java
 *
 * Created on September 6, 2006, 10:02 AM
 */

package com.sun.xml.ws.security.opt.impl.tokens;

import com.sun.xml.ws.security.opt.api.SecurityElementWriter;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.secext10.AttributedString;
import com.sun.xml.ws.security.secext10.UsernameTokenType;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.security.secext10.ObjectFactory;
import javax.xml.stream.XMLStreamException;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.wss.logging.LogStringsMessages;

/**
 * Representation of UsernameToken SecurityHeaderElement
 * @author Ashutosh.Shahi@sun.com
 */
public class UsernameToken extends UsernameTokenType
        implements com.sun.xml.ws.security.opt.api.tokens.UsernameToken,
        SecurityHeaderElement, SecurityElementWriter{
    
    public static final long MAX_NONCE_AGE = 900000; //milliseconds
    
    // password type
    private String passwordType = MessageConstants.PASSWORD_TEXT_NS;
    
    private String usernameValue = null;
    
    private String passwordValue = null;
    
    // password Digest value
    private String passwordDigestValue = null;
    
    private byte[] decodedNonce = null;
    
    // specifies a cryptographically random sequence
    private String nonceValue = null;
    
    // default nonce encoding
    private String nonceEncodingType = MessageConstants.BASE64_ENCODING_NS;
    
    // time stamp to indicate creation time
    private String createdValue = null;
    
    // flag to indicate whether BSP checks should be made or not.
    private boolean bsp = false;
    
    private boolean valuesSet = false;
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    private ObjectFactory objFac = new ObjectFactory();
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /** Creates a new instance of UsernameToken */
    public UsernameToken(SOAPVersion sv) {
        this.soapVersion = sv;
    }
       
    /**
     * @return Returns the username.
     */
    public String getUsernameValue() {
        //AttributedString userName
        return usernameValue;
    }
    
    public void setUsernameValue(String username) {
        /*AttributedString ut = objFac.createAttributedString();
        ut.setValue(username);
        setUsername(ut);*/
        this.usernameValue = username;
    }
    
    /**
     * @return Returns the password which may be null meaning no password.
     */
    public String getPasswordValue() {
        return passwordValue;
    }
    
    /**
     * Sets the password.
     * @param passwd
     */
    public void setPasswordValue(String passwd){
        /*AttributedString password = objFac.createAttributedString();
        password.setValue(passwd);
        setPassword(password);*/
        passwordValue = passwd;
    }
    
    /**
     * @return Returns the passwordType.
     */
    public String getPasswordType() {
        return passwordType;
    }
    
    private void setPasswordType(String passwordType)
    throws SecurityTokenException {
        if (MessageConstants.PASSWORD_TEXT_NS.equals(passwordType)) {
            this.passwordType = MessageConstants.PASSWORD_TEXT_NS;
        } else if (MessageConstants.PASSWORD_DIGEST_NS.equals(passwordType)) {
            this.passwordType =  MessageConstants.PASSWORD_DIGEST_NS;
        } else {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0306_INVALID_PASSWD_TYPE(MessageConstants.PASSWORD_TEXT_NS, MessageConstants.PASSWORD_DIGEST_NS),
                    new Object[] {
                MessageConstants.PASSWORD_TEXT_NS,
                MessageConstants.PASSWORD_DIGEST_NS});
            throw new SecurityTokenException(
                    "Invalid password type. Must be one of   " +
                    MessageConstants.PASSWORD_TEXT_NS + " or " +
                    MessageConstants.PASSWORD_DIGEST_NS);
        }
    }
    
    /**
     * @return Returns the Nonce Encoding type.
     */
    public String getNonceEncodingType() {
        return this.nonceEncodingType;
    }    
    
    /**
     * Sets the nonce encoding type.
     * As per WSS:UserNameToken profile, for valid values, refer to
     * wsse:BinarySecurityToken schema.
     */
    private void setNonceEncodingType(String nonceEncodingType) {
        
        if (!MessageConstants.BASE64_ENCODING_NS.equals(nonceEncodingType)) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0307_NONCE_ENCTYPE_INVALID());
            throw new RuntimeException("Nonce encoding type invalid");
        }
        this.nonceEncodingType = MessageConstants.BASE64_ENCODING_NS;
    }
    
    /**
     * @return Returns the encoded nonce. Null indicates no nonce was set.
     */
    public String getNonceValue() throws SecurityTokenException {
        return nonceValue;
    }
    
    /**
     * Returns the created which may be null meaning no time of creation.
     */
    public String getCreatedValue() {
        return createdValue;
    }
    
    public String getPasswordDigestValue() {
        return this.passwordDigestValue;
    }
    
    /**
     * set the nonce value.If nonce value is null then it will create one.
     * @param nonceValue
     */
    public void setNonce(String nonceValue){
        if(nonceValue == null || MessageConstants.EMPTY_STRING.equals(nonceValue)){
            createNonce();
        }else{
            this.nonceValue = nonceValue;
        }
    }
    
    /**
     * set the creation time.
     * @param time If null or empty then this method would create one.
     */
    public void setCreationTime(String time) throws XWSSecurityException {
        if(time == null || MessageConstants.EMPTY_STRING.equals(time)){
            this.createdValue = getCreatedFromTimestamp();
        }else{
            this.createdValue = time;
        }
    }
          
    public void setDigestOn() throws SecurityTokenException {
        setPasswordType(MessageConstants.PASSWORD_DIGEST_NS);
    }
    
    public void isBSP(boolean flag) {
        bsp = flag;
    }
    
    public boolean isBSP() {
        return bsp;
    }
    
    public String getNamespaceURI() {
        return MessageConstants.WSSE_NS;
    }
    
    public String getLocalPart() {
        return MessageConstants.USERNAME_TOKEN_LNAME;
    }
    
    public String getAttribute(String nsUri, String localName) {
        QName qname = new QName(nsUri, localName);
        Map<QName, String> otherAttributes = this.getOtherAttributes();
        return otherAttributes.get(qname);
    }
    
    public String getAttribute(QName name) {
        Map<QName, String> otherAttributes = this.getOtherAttributes();
        return otherAttributes.get(name);
    }
    
    public javax.xml.stream.XMLStreamReader readHeader() throws javax.xml.stream.XMLStreamException {
        if(!this.valuesSet)
            setValues();
        XMLStreamBufferResult xbr = new XMLStreamBufferResult();
        JAXBElement<UsernameTokenType> utElem = objFac.createUsernameToken(this);
        try{
            getMarshaller().marshal(utElem, xbr);
            
        } catch(JAXBException je){
            throw new XMLStreamException(je);
        }
        return xbr.getXMLStreamBuffer().readAsXMLStreamReader();
    }
    
    public void writeTo(OutputStream os) {
        if(!this.valuesSet)
            setValues();
    }
    
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter) throws javax.xml.stream.XMLStreamException {
        if(!this.valuesSet)
            setValues();
        JAXBElement<UsernameTokenType> utElem = objFac.createUsernameToken(this);
        try {
            // If writing to Zephyr, get output stream and use JAXB UTF-8 writer
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    getMarshaller().marshal(utElem, os);
                    return;
                }
            }
            
            getMarshaller().marshal(utElem,streamWriter);
        } catch (JAXBException e) {
            throw new XMLStreamException(e);
        }
    }
    
    private Marshaller getMarshaller() throws JAXBException{
        return JAXBUtil.createMarshaller(soapVersion);
    }
    
    /*
     * Create a unique nonce. Default encoded with base64.
     * A nonce is a random value that the sender creates
     * to include in the username token that it sends.
     * Nonce is an effective counter measure against replay attacks.
     */
    private void createNonce() {
        
        this.decodedNonce = new byte[18];
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.nextBytes(decodedNonce);
        } catch (NoSuchAlgorithmException e) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0310_NO_SUCH_ALGORITHM(e.getMessage()),new Object[] {e.getMessage()});
            throw new RuntimeException(
                    "No such algorithm found" + e.getMessage());
        }
        if (MessageConstants.BASE64_ENCODING_NS == nonceEncodingType)
            this.nonceValue = Base64.encode(decodedNonce);
        else {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0389_UNRECOGNIZED_NONCE_ENCODING(nonceEncodingType), nonceEncodingType);
            throw new RuntimeException(
                    "Unrecognized encoding: " + nonceEncodingType);
        }
    }
    
    private String getCreatedFromTimestamp() throws XWSSecurityException {
        Timestamp ts = new Timestamp(soapVersion);
        ts.createDateTime();
        return ts.getCreated().getValue();
    }
    
    /*
     * Password Digest creation.
     * As per WSS-UsernameToken spec, if either or both of <wsse:Nonce>
     * and <wsu:Created> are present, then they must be included in the
     * digest as follows:
     *
     * Password_digest = Base64( SHA_1 (nonce + created + password) )
     *
     */
    private void createDigest() throws SecurityTokenException {
        
        String utf8String = "";
        if (createdValue != null) {
            utf8String = utf8String + createdValue;
        }
        
        // password is also optional
        if (passwordValue != null) {
            utf8String = utf8String + passwordValue;
        }
        
        byte[] utf8Bytes;
        try {
            utf8Bytes = utf8String.getBytes("utf-8");
        } catch (UnsupportedEncodingException uee) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0390_UNSUPPORTED_CHARSET_EXCEPTION());
            throw new SecurityTokenException(uee);
        }
        
        byte[] bytesToHash;
        if (decodedNonce != null) {
            bytesToHash = new byte[utf8Bytes.length + decodedNonce.length];
            for (int i = 0; i < decodedNonce.length; i++)
                bytesToHash[i] = decodedNonce[i];
            for (int i = decodedNonce.length; i < utf8Bytes.length + decodedNonce.length; i++)
                bytesToHash[i] = utf8Bytes[i - decodedNonce.length];
        } else {
            bytesToHash = utf8Bytes;
        }
        
        byte[] hash;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            hash = sha.digest(bytesToHash);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0311_PASSWD_DIGEST_COULDNOT_BE_CREATED(e.getMessage()), new Object[] {e.getMessage()});
            throw new SecurityTokenException(
                    "Password Digest could not be created. " + e.getMessage());
        }
        this.passwordDigestValue = Base64.encode(hash);
    }
    
    private void setValues(){
        if(usernameValue != null){
            AttributedString ut = objFac.createAttributedString();
            ut.setValue(usernameValue);
            setUsername(ut);
        }
        
        if (passwordValue != null && !MessageConstants._EMPTY.equals(passwordValue) ){
            AttributedString pw = objFac.createAttributedString();
            if (MessageConstants.PASSWORD_DIGEST_NS == passwordType) {
                try {
                    createDigest();
                } catch (com.sun.xml.wss.impl.SecurityTokenException ex) {
                    ex.printStackTrace();
                }
                pw.setValue(passwordDigestValue);
                setPassword(pw);
            } else{
                pw.setValue(passwordValue);
                setPassword(pw);
            }
            QName qname = new QName("Type");
            pw.getOtherAttributes().put(qname, passwordType);
            
        }
        
        if(nonceValue != null){
            AttributedString non = objFac.createAttributedString();
            non.setValue(nonceValue);
            setNonce(non);
            if (nonceEncodingType != null) {
                QName qname = new QName("EncodingType");
                non.getOtherAttributes().put(qname, nonceEncodingType);
            }
        }
        
        if(createdValue != null){
            AttributedString cr = objFac.createAttributedString();
            cr.setValue(createdValue);
            setCreated(cr);
        }
        
        valuesSet = true;
    }
    
    /**
     * 
     * @param id 
     * @return 
     */
    public boolean refersToSecHdrWithId(String id) {
        return false;
    }
    @SuppressWarnings("unchecked")
    public void writeTo(javax.xml.stream.XMLStreamWriter streamWriter, HashMap props) throws javax.xml.stream.XMLStreamException {
        try{
            if(!this.valuesSet)
                setValues();
            Marshaller marshaller = getMarshaller();
            Iterator<Map.Entry<Object, Object>> itr = props.entrySet().iterator();
            while(itr.hasNext()){
                Map.Entry<Object, Object> entry = itr.next();
                marshaller.setProperty((String)entry.getKey(), entry.getValue());
            }
            JAXBElement<UsernameTokenType> utElem = objFac.createUsernameToken(this);
            if (streamWriter instanceof Map) {
                OutputStream os = (OutputStream) ((Map) streamWriter).get("sjsxp-outputstream");
                if (os != null) {
                    streamWriter.writeCharacters("");        // Force completion of open elems
                    marshaller.marshal(utElem, os);
                    return;
                }
            }
            marshaller.marshal(utElem,streamWriter);
        }catch(JAXBException jbe){
            throw new XMLStreamException(jbe);
        }
    }
}
