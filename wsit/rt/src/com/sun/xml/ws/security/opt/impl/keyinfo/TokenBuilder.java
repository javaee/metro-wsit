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
package com.sun.xml.ws.security.opt.impl.keyinfo;

import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BinarySecurityToken;
import com.sun.xml.ws.security.opt.api.keyinfo.Token;
import com.sun.xml.ws.security.opt.api.reference.Reference;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader;
import com.sun.xml.ws.security.opt.impl.reference.DirectReference;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyName;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyValue;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.RSAKeyValue;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.tokens.UsernameToken;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.impl.opt.token.LogStringsMessages;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBElement;
import javax.xml.crypto.Data;

/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class TokenBuilder implements com.sun.xml.ws.security.opt.api.keyinfo.TokenBuilder {

    protected static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN,
            LogDomainConstants.IMPL_OPT_TOKEN_DOMAIN_BUNDLE);
    protected JAXBFilterProcessingContext context = null;
    protected SecurityHeader securityHeader = null;
    protected WSSElementFactory elementFactory = null;
    protected KeyInfo keyInfo = null;
    /** Creates a new instance of TokenBuilder */
    public TokenBuilder(JAXBFilterProcessingContext context) {
        this.context = context;
        this.securityHeader = context.getSecurityHeader();
        this.elementFactory = new WSSElementFactory(context.getSOAPVersion());
    }
    /**
     * if a BinarySecurityToken already exists in the security header with the id of the binding
     * returns it else creates a BinarySecurityToken with the X509 certificate provided
     * Adds the username token to the security header
     * @param binding X509CertificateBinding
     * @param x509Cert X509Certificate
     * @return BinarySecurityToken
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    protected BinarySecurityToken createBinarySecurityToken(AuthenticationTokenPolicy.X509CertificateBinding binding,X509Certificate x509Cert) throws XWSSecurityException{
        if(binding.INCLUDE_NEVER.equals(binding.getIncludeToken()) ||
                binding.INCLUDE_NEVER_VER2.equals(binding.getIncludeToken()))
            return null;        
        String id = getID(binding);

        if(logger.isLoggable(Level.FINEST)){
            logger.log(Level.FINEST, "X509 Token id: "+id);
        }

        Token token = (Token)securityHeader.getChildElement(id);
        if(token != null){
            if(token instanceof BinarySecurityToken){
                return (BinarySecurityToken)token;
            }
            logger.log(Level.SEVERE, "Found two tokens with same Id attribute");
            throw new XWSSecurityException("Found two tokens with same Id attribute");
        }
        BinarySecurityToken bst;
        try {
            bst = elementFactory.createBinarySecurityToken(id, x509Cert.getEncoded());
        } catch (CertificateEncodingException ex) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1801_BST_CREATION_FAILED());
            throw new XWSSecurityException("Error occured while constructing BinarySecurityToken",ex);
        }
        context.getSecurityHeader().add((SecurityHeaderElement)bst);
        return bst;
    }
    /**
     * if an UsernameToken already exists in the security header with the id of the binding
     * returns it else sets the id of the binding in the usernametoken provided and returns it
     * Adds the username token to the security header
     * @param binding UsernameTokenBinding
     * @param unToken UsernameToken
     * @return UsernameToken
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    protected UsernameToken createUsernameToken(AuthenticationTokenPolicy.UsernameTokenBinding binding, UsernameToken unToken)
            throws XWSSecurityException {
        String id = getID(binding);
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Username Token id: " + id);
        }
        SecurityHeaderElement token = ( SecurityHeaderElement) securityHeader.getChildElement(id);
        if (token != null) {
            if (token instanceof UsernameToken) {
                return (UsernameToken) token;
            }
           logger.log(Level.SEVERE, "Found two tokens with same Id attribute");
            throw new XWSSecurityException("Found two tokens with same Id attribute");
        }
        unToken.setId(id);
        context.getSecurityHeader().add((SecurityHeaderElement) unToken);
        return unToken;
    }
    /**
     * if an BinarySecurityToken already exists in the security header with the id of the binding
     * returns it else creates a new BinarySecurityToken with the kerboros token  provided
     * Adds the BinarySecurityToken to the security header
     * @param binding KerberosTokenBinding
     * @param kerbToken byte[]
     * @return  BinarySecurityToken
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    protected BinarySecurityToken createKerberosBST(AuthenticationTokenPolicy.KerberosTokenBinding binding,
            byte[] kerbToken) throws XWSSecurityException {
        if (binding.INCLUDE_NEVER.equals(binding.getIncludeToken()) ||
                binding.INCLUDE_NEVER_VER2.equals(binding.getIncludeToken())) {
            return null;
        }
        String id = getID(binding);

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Kerberos Token id: " + id);
        }

        Token token = (Token) securityHeader.getChildElement(id);
        if (token != null) {
            if (token instanceof BinarySecurityToken) {
                return (BinarySecurityToken) token;
            }
            logger.log(Level.SEVERE, "Found two tokens with same Id attribute");
            throw new XWSSecurityException("Found two tokens with same Id attribute");
        }
        BinarySecurityToken bst;
        bst = elementFactory.createKerberosBinarySecurityToken(id, kerbToken);
        context.getSecurityHeader().add((SecurityHeaderElement) bst);
        return bst;
    }
    /**
     * creates a new  SecurityTokenReference with the reference element provided
     * sets the id provided in the  SecurityTokenReference if the security policy is a SignaturePolicy
     * @param strId String
     * @param ref Reference
     * @return SecurityTokenReference
     */
    @SuppressWarnings("unchecked")
    protected SecurityTokenReference buildSTR(String strId, Reference ref){
        SecurityTokenReference str = elementFactory.createSecurityTokenReference(ref);
        if (context.getSecurityPolicy() instanceof SignaturePolicy) {
            ((SecurityElement) str).setId(strId);
        }
        if (context.getWSSAssertion() != null) {
            if ((ref instanceof DirectReference) && context.getWSSAssertion().getType().equals("1.1")) {
                if (MessageConstants.USERNAME_STR_REFERENCE_NS.equals(((DirectReference) ref).getValueType())) {
                    str.setTokenType(MessageConstants.USERNAME_STR_REFERENCE_NS);
                } else if (MessageConstants.EncryptedKey_NS.equals(((DirectReference) ref).getValueType())) {
                    str.setTokenType(MessageConstants.EncryptedKey_NS);
                }
            }
        }
        Data data = new SSEData((SecurityElement)str,false,context.getNamespaceContext());
        context.getElementCache().put(strId,data);
        return str;
    }
    /**
     * creates a new  SecurityTokenReference with the reference element provided
     * @param ref Reference
     * @return SecurityTokenReference
     */
    protected SecurityTokenReference buildSTR(Reference ref){
        SecurityTokenReference str = elementFactory.createSecurityTokenReference(ref);
        return str;
    }
    /**
     * builds SecurityTokenReference with the reference element provided and with the id.
     * creates key info with this  SecurityTokenReference
     * @param ref Referenc
     * @param strId String
     * @return KeyInfo
     */
    protected KeyInfo buildKeyInfo(Reference ref,String strId){
        keyInfo = elementFactory.createKeyInfo(buildSTR(strId,ref));
        return keyInfo;
    }
    /**
     * creates key info with the SecurityTokenReference provided
     * @param str SecurityTokenReference
     * @return KeyInfo
     */
    protected KeyInfo buildKeyInfo(com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference str){
        keyInfo = elementFactory.createKeyInfo(str);
        return keyInfo;
    }
    /**
     * builds key value  with the public key provided
     * Uses thid key value to construct key info
     * @param pubKey PublicKey
     * @return KeyInfo
     */
    protected KeyInfo buildKeyInfo(PublicKey pubKey){
        keyInfo = elementFactory.createKeyInfo(buildKeyValue(pubKey));
        return keyInfo;
    }
   /**
    * builds RSA key value  with the public key provided
    * @param pubKey PublicKey
    * @return  KeyValue
    */
    @SuppressWarnings("unchecked")
    protected KeyValue buildKeyValue(PublicKey pubKey) {
        KeyValue kv = new KeyValue();
        RSAKeyValue rsaKV = new RSAKeyValue(pubKey);
        JAXBElement je = new com.sun.xml.security.core.dsig.ObjectFactory().createRSAKeyValue(rsaKV);
        List strList = Collections.singletonList(je);
        kv.setContent(strList);
        return kv;
    }

    protected KeyInfo buildKIWithKeyName(String name) {
        KeyName kn = new KeyName();
        kn.setKeyName(name);
        keyInfo = elementFactory.createKeyInfo(kn);
        return keyInfo;
    }
    /**
     * builds the direct reference and sets the id and valueType in it
     * @param id String
     * @param valueType  String
     * @return DirectReference
     */
    protected DirectReference buildDirectReference(String id, String valueType) {
        DirectReference dr = elementFactory.createDirectReference();
        dr.setURI("#" + id);
        if (valueType != null) {
            dr.setValueType(valueType);
        }
        return dr;
    }
    /**
     * builds keyInfo with the given X509 certificate binding
     * @param binding X509CertificateBinding
     * @param refType String
     * @return KeyIdentifier
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    protected KeyIdentifier buildKeyInfoWithKI(AuthenticationTokenPolicy.X509CertificateBinding binding, String refType) throws XWSSecurityException {
        KeyIdentifier keyIdentifier = elementFactory.createKeyIdentifier();
        //keyIdentifier.setValue(binding.getCertificateIdentifier());
        keyIdentifier.setValueType(refType);
        keyIdentifier.updateReferenceValue(binding.getX509Certificate());
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        if (keyIdentifier.getValue() == null || keyIdentifier.getValue().length() == 0) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
        }
        buildKeyInfo(keyIdentifier, binding.getSTRID());
        return keyIdentifier;
    }
    /**
     * builds keyInfo with the given kerberos token binding
     * @param binding KerberosTokenBinding
     * @param refType String
     * @return KeyIdentifier
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    protected KeyIdentifier buildKeyInfoWithKIKerberos(AuthenticationTokenPolicy.KerberosTokenBinding binding, String refType) throws XWSSecurityException {
        KeyIdentifier keyIdentifier = elementFactory.createKeyIdentifier();
        keyIdentifier.setValueType(refType);
        keyIdentifier.updateReferenceValue(binding.getTokenValue());
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        if (keyIdentifier.getValue() == null || keyIdentifier.getValue().length() == 0) {
            logger.log(Level.SEVERE, LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
            throw new XWSSecurityException(LogStringsMessages.WSS_1852_KEY_IDENTIFIER_EMPTY());
        }
        buildKeyInfo(keyIdentifier, binding.getSTRID());
        return keyIdentifier;
    }
    /**
     * builds keyInfo with the given encrypted key sha1 reference
     * @param ekSHA1Ref String
     * @return KeyIdentifier
     */
    protected KeyIdentifier buildKeyInfoWithEKSHA1(String ekSHA1Ref) {
        KeyIdentifier keyIdentifier = elementFactory.createKeyIdentifier();
        keyIdentifier.setValueType(MessageConstants.EncryptedKeyIdentifier_NS);
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        keyIdentifier.setReferenceValue(ekSHA1Ref);
        buildKeyInfo(keyIdentifier, null);
        return keyIdentifier;
    }

    protected String getID(WSSPolicy policy) {
        String id = policy.getUUID();
        if (id == null || id.length() == 0) {
            return context.generateID();
        }
        return id;
    }
    /**
     *
     * @return javax.xml.crypto.dsig.keyinfo.KeyInfo
     */
    public javax.xml.crypto.dsig.keyinfo.KeyInfo getKeyInfo() {
        return keyInfo;
    }
    
}
