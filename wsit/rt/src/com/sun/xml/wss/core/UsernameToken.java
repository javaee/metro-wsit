/*
 * $Id: UsernameToken.java,v 1.5 2010-04-27 14:20:30 m_potociar Exp $
 */

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

package com.sun.xml.wss.core;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;

import com.sun.xml.ws.security.Token;

/*
<xsd:complexType name="UsernameTokenType">
- <xsd:annotation>
- <xsd:documentation>
This type represents a username token per Section 4.1
</xsd:documentation>
</xsd:annotation>
- <xsd:sequence>
<xsd:element name="Username" type="wsse:AttributedString"/>
<xsd:any processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
</xsd:sequence>
<xsd:attribute ref="wsu:Id"/>
<xsd:anyAttribute namespace="##other" processContents="lax"/>
</xsd:complexType>
 */

/**
 *
 * Support for a WSS:Username Token Profile.
 *
 * Represents a wsse:UsernameToken.
 *
 * @author Manveen Kaur
 * @author Edwin Goei
 */
public class UsernameToken extends SecurityHeaderBlockImpl
implements SecurityToken, Token {
    
    public static final long MAX_NONCE_AGE = 900000; //milliseconds

    private String username;
    
    private String password = null;
    
    // password type
    private String passwordType = MessageConstants.PASSWORD_TEXT_NS;
    
    // password Digest value
    private String passwordDigest = null;
    
    private byte[] decodedNonce = null;
    
    // specifies a cryptographically random sequence
    private String nonce = null;
    
    // default nonce encoding
    private String nonceEncodingType = MessageConstants.BASE64_ENCODING_NS;
    
    // time stamp to indicate creation time
    private String created = null;

    // flag to indicate whether BSP checks should be made or not.
    private boolean bsp = false;
    
    private Document soapDoc;
    
    private static Logger log =
    Logger.getLogger(
    LogDomainConstants.WSS_API_DOMAIN,
    LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    public static SecurityHeaderBlock fromSoapElement(SOAPElement element) throws XWSSecurityException {
        return SecurityHeaderBlockImpl.fromSoapElement(
        element, UsernameToken.class);
    }
    
    public UsernameToken(Document document, String username) throws SecurityTokenException {
        this.soapDoc = document;
        this.username = username;
        // set default password type        
        setPasswordType(MessageConstants.PASSWORD_TEXT_NS);
    }
    
    public UsernameToken( Document document,String username,String password,boolean digestPassword) throws SecurityTokenException {
        this(document, username);
        this.password = password;
        if (digestPassword) {
            setPasswordType(MessageConstants.PASSWORD_DIGEST_NS);
        } 
    }
    
    public UsernameToken(Document document, String username, String password) throws SecurityTokenException {
        this(document, username, password, false);
    }
    
    /**
     * C'tor that creates the optional element nonce, created is not set.
     */
    public UsernameToken(Document document,String username, String password,  boolean setNonce, boolean digestPassword) throws SecurityTokenException {
        
        this(document, username, password, digestPassword);
        if (setNonce) {
            createNonce();
        }
    }
    
    /**
     * C'tor that creates the optional elements of nonce and created.
     */
    public UsernameToken(Document document,String username,String password,boolean setNonce,boolean setCreatedTimestamp,boolean digestPassword)
    throws SecurityTokenException {
    
        this(document, username, password, setNonce, digestPassword);
        if (setCreatedTimestamp) {
            try {
                this.created = getCreatedFromTimestamp();
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0280.failed.create.UsernameToken", e);
                throw new SecurityTokenException(e);
            }
        }        
    }

    public UsernameToken(SOAPElement usernameTokenSoapElement, boolean bspFlag) throws XWSSecurityException {
        this(usernameTokenSoapElement);
        isBSP(bspFlag);
    }

    /**
     * Extracts info from SOAPElement representation
     *
     * @param usernameTokenSoapElement
     */
    public UsernameToken(SOAPElement usernameTokenSoapElement) throws XWSSecurityException {
        
        setSOAPElement(usernameTokenSoapElement);
        this.soapDoc = getOwnerDocument();
        
        if (!("UsernameToken".equals(getLocalName()) &&
        XMLUtil.inWsseNS(this))) {
            log.log(
            Level.SEVERE,
            "WSS0329.usernametoken.expected",
            new Object[] {getLocalName()});
            
            throw new SecurityTokenException(
            "Expected UsernameToken Element, but Found " + getLocalName());
        }
        
        boolean invalidToken = false;
        
        Iterator children = getChildElements();
        
        // Check that the first child element is a Username
        
        Node object = null;
        while (children.hasNext() && !(object instanceof SOAPElement)) {
            object = (Node)children.next();
        }

        if ((object != null) && (object.getNodeType() == Node.ELEMENT_NODE)) {
            SOAPElement element = (SOAPElement) object;
            if ("Username".equals(element.getLocalName()) &&
            XMLUtil.inWsseNS(element)) {
                username = element.getValue();
            } else {
                log.log(Level.SEVERE,"WSS0330.usernametoken.firstchild.mustbe.username");
                throw new SecurityTokenException("The first child of a UsernameToken Element, should be"
                + " a Username ");
            }
        } else {
            invalidToken = true;
        }
        
        while (children.hasNext()) {
            
            object = (Node)children.next();

            if (object.getNodeType() == Node.ELEMENT_NODE) {

                SOAPElement element = (SOAPElement) object;
                if ("Password".equals(element.getLocalName()) &&
                XMLUtil.inWsseNS(element)) {
                    String passwordType = element.getAttribute("Type");
                    
                    if (isBSP() && passwordType.length() < 1) {
                        // Type should be specified
                        log.log(Level.SEVERE,"BSP4201.PasswordType.Username");
                        throw new XWSSecurityException(" A wsse:UsernameToken/wsse:Password element in a SECURITY_HEADER MUST specify a Type attribute.");
                    }
                    
                    if (!"".equals(passwordType))                        
                        setPasswordType(passwordType);
                    
                    if (MessageConstants.PASSWORD_TEXT_NS == this.passwordType)
                        password = element.getValue();
                    else
                        passwordDigest = element.getValue();
                }
                else if ("Nonce".equals(element.getLocalName()) && XMLUtil.inWsseNS(element)) {
                    nonce = element.getValue();
                    String encodingType =
                    element.getAttribute("EncodingType");
                    if (!"".equals(encodingType))
                        setNonceEncodingType(encodingType);
                    try {
                        decodedNonce = Base64.decode(nonce);
                    } catch (Base64DecodingException bde) {
                        log.log(Level.SEVERE, "WSS0309.couldnot.decode.base64.nonce", bde);
                        throw new XWSSecurityException(bde);
                    }
                }
                else if ("Created".equals(element.getLocalName()) &&
                XMLUtil.inWsuNS(element)) {
                    created = element.getValue();
                } else {
                    invalidToken = true;
                }
            }
        }
        
        if (invalidToken) {
            log.log(Level.SEVERE, "WSS0331.invalid.usernametoken");
            throw new SecurityTokenException(
            "Element passed was not a SOAPElement or"
            + " is not a proper UsernameToken");
        }
        
        if (null == username) {
            log.log(Level.SEVERE, "WSS0332.usernametoken.null.username");
            throw new SecurityTokenException(
            "Username token does not contain the username");
        }
    }
    
    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * @return Returns the password which may be null meaning no password.
     */
    public String getPassword() {
        return password;
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
            log.log(Level.SEVERE, "WSS0306.invalid.passwd.type",
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
            log.log(Level.SEVERE,"WSS0307.nonce.enctype.invalid");
            throw new RuntimeException("Nonce encoding type invalid");
        }
        this.nonceEncodingType = MessageConstants.BASE64_ENCODING_NS;
    }
    
    
    /**
     * @return Returns the encoded nonce. Null indicates no nonce was set.
     */
    public String getNonce() throws SecurityTokenException {
        return nonce;
    }
    
    /**
     * Returns the created which may be null meaning no time of creation.
     */
    public String getCreated() {
        return created;
    }
    
    public String getPasswordDigest() {
        return this.passwordDigest;
    }
    
    /**
     * Sets the password.
     * @param passwd
     */    
    public void setPassword(String passwd){
        this.password = passwd;
    }
    
    /**
     * set the nonce value.If nonce value is null then it will create one.
     * @param nonceValue
     */    
    public void setNonce(String nonceValue){
        if(nonceValue == null || MessageConstants.EMPTY_STRING.equals(nonceValue)){
            createNonce();
        }else{
            this.nonce = nonceValue;
        }
    }
    /**
     * set the creation time.
     * @param time If null or empty then this method would create one.
     */    
    public void setCreationTime(String time) throws XWSSecurityException {
        if(time == null || MessageConstants.EMPTY_STRING.equals(time)){
            this.created = getCreatedFromTimestamp();
        }else{
            this.created = time;
        }
    }
    
    public void setDigestOn() throws SecurityTokenException {
        setPasswordType(MessageConstants.PASSWORD_DIGEST_NS);
    }
    
    public SOAPElement getAsSoapElement() throws SecurityTokenException {
        
        if (null != delegateElement)
            return delegateElement;
        try {
            setSOAPElement(
            (SOAPElement) soapDoc.createElementNS(
            MessageConstants.WSSE_NS,
            MessageConstants.WSSE_PREFIX + ":UsernameToken"));
            
            addNamespaceDeclaration(
            MessageConstants.WSSE_PREFIX,
            MessageConstants.WSSE_NS);
            
            if (null == username || MessageConstants._EMPTY.equals(username) ) {
                log.log(Level.SEVERE, "WSS0387.error.creating.usernametoken");
                throw new SecurityTokenException("username was not set");
            } else {
                addChildElement("Username", MessageConstants.WSSE_PREFIX)
                .addTextNode(username);
            }
            
            if (password != null && !MessageConstants._EMPTY.equals(password) ) {
                SOAPElement wssePassword =
                addChildElement("Password", MessageConstants.WSSE_PREFIX);
                
                if (MessageConstants.PASSWORD_DIGEST_NS == passwordType) {
                    createDigest();
                    wssePassword.addTextNode(passwordDigest);
                } else {
                    wssePassword.addTextNode(password);
                }
                wssePassword.setAttribute("Type", passwordType);
            }
            
            if (nonce != null) {
                SOAPElement wsseNonce =
                addChildElement("Nonce", MessageConstants.WSSE_PREFIX);
                wsseNonce.addTextNode(nonce);
                
                if (nonceEncodingType != null) {
                    wsseNonce.setAttribute("EncodingType", nonceEncodingType);
                }
            }
            
            if (created != null) {
                SOAPElement wsuCreated =
                addChildElement(
                "Created",
                MessageConstants.WSU_PREFIX,
                MessageConstants.WSU_NS);
                wsuCreated.addTextNode(created);
            }
            
        } catch (SOAPException se) {
            log.log(Level.SEVERE, "WSS0388.error.creating.usernametoken", se.getMessage());
            throw new SecurityTokenException(
            "There was an error creating Username Token " +
            se.getMessage());
        }
        return delegateElement;
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
            log.log(Level.SEVERE, "WSS0310.no.such.algorithm",
            new Object[] {e.getMessage()});
            throw new RuntimeException(
            "No such algorithm found" + e.getMessage());
        }
        if (MessageConstants.BASE64_ENCODING_NS == nonceEncodingType)
            this.nonce = Base64.encode(decodedNonce);
        else {
            log.log(Level.SEVERE, "WSS0389.unrecognized.nonce.encoding", nonceEncodingType);
            throw new RuntimeException(
            "Unrecognized encoding: " + nonceEncodingType);
        }
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
        if (created != null) {
            utf8String = utf8String + created;
        }
        
        // password is also optional
        if (password != null) {
            utf8String = utf8String + password;
        }
        
        byte[] utf8Bytes;
        try {
            utf8Bytes = utf8String.getBytes("utf-8");
        } catch (UnsupportedEncodingException uee) {
            log.log(Level.SEVERE, "WSS0390.unsupported.charset.exception");
            throw new SecurityTokenException(uee);
        }
        
        byte[] bytesToHash;
        if (decodedNonce != null) {
            bytesToHash = new byte[utf8Bytes.length + 18];
            for (int i = 0; i < 18; i++)
                bytesToHash[i] = decodedNonce[i];
            for (int i = 18; i < utf8Bytes.length + 18; i++)
                bytesToHash[i] = utf8Bytes[i - 18];
        } else {
            bytesToHash = utf8Bytes;
        }
        
        byte[] hash;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            hash = sha.digest(bytesToHash);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0311.passwd.digest.couldnot.be.created",
            new Object[] {e.getMessage()});
            throw new SecurityTokenException(
            "Password Digest could not be created. " + e.getMessage());
        }
        this.passwordDigest = Base64.encode(hash);
    }
    
    private String getCreatedFromTimestamp() throws XWSSecurityException {
        Timestamp ts = new Timestamp();
        ts.createDateTime();
        return ts.getCreated();
    }
    
    public void isBSP(boolean flag) {
        bsp = flag;
    }

    public boolean isBSP() {
        return bsp;
    }

    public String getType() {
        return MessageConstants.USERNAME_TOKEN_NS;
    }

    public Object getTokenValue() {
        log.log(Level.SEVERE, "WSS0281.unsupported.operation");
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

