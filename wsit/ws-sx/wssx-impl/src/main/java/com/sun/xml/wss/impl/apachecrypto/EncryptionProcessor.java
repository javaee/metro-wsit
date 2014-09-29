/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2014 Oracle and/or its affiliates. All rights reserved.
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
 * EncryptionProcessor.java
 *
 * Created on March 17, 2005, 5:22 PM
 */

package com.sun.xml.wss.impl.apachecrypto;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.exceptions.Base64DecodingException;

import com.sun.xml.util.XMLCipherAdapter;
import com.sun.xml.wss.core.EncryptedHeaderBlock;
import com.sun.xml.wss.impl.misc.Base64;

import com.sun.xml.wss.impl.FilterProcessingContext;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.core.DerivedKeyTokenHeaderBlock;
import com.sun.xml.wss.core.EncryptedDataHeaderBlock;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.ReferenceListHeaderBlock;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.wss.core.reference.DirectReference;
import com.sun.xml.wss.core.reference.EncryptedKeySHA1Identifier;

import com.sun.xml.wss.impl.resolver.AttachmentSignatureInput;

import com.sun.xml.wss.impl.keyinfo.KeyIdentifierStrategy;

import com.sun.xml.wss.impl.misc.KeyResolver;

import com.sun.xml.wss.swa.MimeConstants;

import com.sun.xml.wss.saml.Assertion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import java.security.Key;
import com.sun.xml.wss.impl.keyinfo.KeyInfoStrategy;
import com.sun.xml.wss.impl.keyinfo.KeyNameStrategy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.MessageDigest;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.impl.AlgorithmSuite;
import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.security.DerivedKeyToken;

import javax.crypto.spec.SecretKeySpec;

import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;


/**
 *
 * @author XWSS Security Team
 * @author K.Venugopal@sun.com
 */

public class EncryptionProcessor {
    
    private static byte[] crlf = null;
    protected static final Logger log =  Logger.getLogger( LogDomainConstants.IMPL_CRYPTO_DOMAIN,
            LogDomainConstants.IMPL_CRYPTO_DOMAIN_BUNDLE);
    static{
        try{
            crlf =  "\r\n".getBytes("US-ASCII");
        }catch( java.io.UnsupportedEncodingException ue){
            //log;
            if(log != null){
                log.log(Level.SEVERE,"WSS1204.crlf.init.failed",ue);
            }
        }
    }
    /** Creates a new instance of EncryptionProcessor */
    public EncryptionProcessor() {
    }
    @SuppressWarnings("unchecked")
    public static void encrypt(FilterProcessingContext context) throws XWSSecurityException{
        
        //TODO: support for QName and XPath
        SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
        SecurityHeader _secHeader = secureMsg.findOrCreateSecurityHeader();
        
        boolean _exportCertificate = false;
        SecretKey _symmetricKey = null;
        SecretKey  keyEncSK = null;
        
        X509Certificate _x509Cert = null;
        Key samlkey = null;
        KeyInfoStrategy keyInfoStrategy =  null;
        
        String referenceType = null;
        String x509TokenId = null;
        String keyEncAlgo = XMLCipher.RSA_v1dot5;
        String dataEncAlgo = MessageConstants.TRIPLE_DES_BLOCK_ENCRYPTION;
        String symmetricKeyName = null;
        
        AuthenticationTokenPolicy.X509CertificateBinding certificateBinding = null;
        
        WSSPolicy wssPolicy = (WSSPolicy)context.getSecurityPolicy();
        EncryptionPolicy.FeatureBinding featureBinding =(EncryptionPolicy.FeatureBinding)  wssPolicy.getFeatureBinding();
        WSSPolicy keyBinding = (WSSPolicy)wssPolicy.getKeyBinding();
        
        AlgorithmSuite algSuite = context.getAlgorithmSuite();
        
        SecurityTokenReference samlTokenRef = null;
        SecurityTokenReference secConvRef = null;
        SecurityTokenReference ekTokenRef = null;
        SecurityTokenReference dktSctTokenRef = null;
        SecurityTokenReference issuedTokenRef = null;
        //adding EncryptedKey Direct Reference to handle EncryptBeforeSigning
        SecurityTokenReference ekDirectRef = null;
        
        DerivedKeyTokenHeaderBlock dktHeadrBlock = null;
        
        SecurityContextTokenImpl sct = null;
        boolean sctTokenInserted = false;
        SOAPElement sctElement = null;
        boolean sctWithDKT = false;
        boolean includeSCT = true;
        
        boolean issuedWithDKT = false;
        SecurityTokenReference dktIssuedTokenRef = null;
        SOAPElement issuedTokenElement =  null;
        Element issuedTokenElementFromMsg =  null;
        boolean issuedTokenInserted = false;
        boolean includeIST = true;
        
        boolean dktSender = false;
        
        //Key obtained from SymmetricKeyBinding in case of DKT
        Key originalKey = null;
        String ekId = context.getSecurableSoapMessage().generateId();
        String insertedEkId = null;
        //Check to see if same x509 token used for Signature and Encryption
        boolean skbX509TokenInserted = false;
        
        boolean useStandaloneRefList = false;
        
        HashMap ekCache = context.getEncryptedKeyCache();
        
        SOAPElement x509TokenElement = null;
        
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        
        if(log.isLoggable(Level.FINEST)){
            log.log(Level.FINEST, "KeyBinding in Encryption is "+keyBinding);
        }
        
        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
        boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getEKSHA1Ref(context) != null);
        boolean wss10 = !wss11Sender;
        
        String tmp = featureBinding.getDataEncryptionAlgorithm();
        if (tmp == null || "".equals(tmp)) {
            if (context.getAlgorithmSuite() != null) {
                tmp = context.getAlgorithmSuite().getEncryptionAlgorithm();
            } else {
                // warn that no dataEncAlgo was set
            }
        }
        //TODO :: Change to getDataEncryptionAlgorith,
        if(tmp != null && !"".equals(tmp)){
            dataEncAlgo = tmp;
        }
        
        if (context.getAlgorithmSuite() != null) {
            keyEncAlgo = context.getAlgorithmSuite().getAsymmetricKeyAlgorithm();
        }
        
        // derivedTokenKeyBinding with x509 as originalkeyBinding is to be treated same as
        // DerivedKey with Symmetric binding and X509 as key binding of Symmetric binding
        if(PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)){
            DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
            WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
            
            if (PolicyTypeUtil.x509CertificateBinding(originalKeyBinding)){
                AuthenticationTokenPolicy.X509CertificateBinding ckBindingClone =
                        (AuthenticationTokenPolicy.X509CertificateBinding)originalKeyBinding.clone();
                //create a symmetric key binding and set it as original key binding of dkt
                SymmetricKeyBinding skb = new SymmetricKeyBinding();
                skb.setKeyBinding(ckBindingClone);
                // set the x509 binding as key binding of symmetric binding
                dtk.setOriginalKeyBinding(skb);
                keyBinding = dtk;
            }
        }
        
        if (PolicyTypeUtil.usernameTokenPolicy(keyBinding)) {
            log.log(Level.SEVERE,"WSS1210.unsupported.UsernameToken.AsKeyBinding.EncryptionPolicy");
            throw new XWSSecurityException("UsernameToken as KeyBinding for EncryptionPolicy is Not Yet Supported");
        } else if(PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
            //we need to use standalone reflist to support EncryptBeforeSigning
            useStandaloneRefList=true;
            if ( context.getX509CertificateBinding() != null) {
                certificateBinding  = context.getX509CertificateBinding();
                context.setX509CertificateBinding(null);
            } else {
                certificateBinding  =(AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
            }
            
            x509TokenId = certificateBinding.getUUID();
            if(x509TokenId == null || x509TokenId.equals("")){
                x509TokenId = secureMsg.generateId();
            }
            if(log.isLoggable(Level.FINEST)){
                log.log(Level.FINEST, "Certificate was "+_x509Cert);
                log.log(Level.FINEST, "BinaryToken ID "+x509TokenId);
            }
            
            HashMap tokenCache = context.getTokenCache();
            HashMap insertedX509Cache = context.getInsertedX509Cache();
            
            SecurityUtil.checkIncludeTokenPolicy(context, certificateBinding, x509TokenId);
            
            _x509Cert = certificateBinding.getX509Certificate();
            referenceType = certificateBinding.getReferenceType();
            if(referenceType.equals("Identifier") && certificateBinding.getValueType().equals(MessageConstants.X509v1_NS)){
                log.log(Level.SEVERE,"WSS1211.unsupported.KeyIdentifierStrategy.X509v1");
                throw new XWSSecurityException("Key Identifier strategy with X509v1 certificate is not allowed");
            }
            keyInfoStrategy = KeyInfoStrategy.getInstance(referenceType);
            _exportCertificate = true;
            keyInfoStrategy.setCertificate(_x509Cert);
            
            if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(referenceType)){
                
                X509SecurityToken token = (X509SecurityToken)tokenCache.get(x509TokenId);
                if(token == null){
                    String valueType = certificateBinding.getValueType();
                    if(valueType==null||valueType.equals("")){
                        //default valueType for X509 as v3
                        valueType = MessageConstants.X509v3_NS;
                    }
                    token = new X509SecurityToken(secureMsg.getSOAPPart(),_x509Cert,x509TokenId, valueType);
                }
                if(insertedX509Cache.get(x509TokenId) == null){
                    secureMsg.findOrCreateSecurityHeader().insertHeaderBlock(token);
                    insertedX509Cache.put(x509TokenId, token);
                    x509TokenElement = secureMsg.findOrCreateSecurityHeader().getNextSiblingOfTimestamp();
                } else{
                    x509TokenElement = secureMsg.getElementByWsuId(x509TokenId);
                }
                
                //x509TokenElement = secureMsg.findOrCreateSecurityHeader().getFirstChildElement();
                
            }
            
            //TODO:Revisit this -Venu
            tmp = null;
            tmp = certificateBinding.getKeyAlgorithm();
            if(tmp != null && !tmp.equals("")){
                keyEncAlgo = tmp;
            }
            _symmetricKey = SecurityUtil.generateSymmetricKey(dataEncAlgo);
        } else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
            SymmetricKeyBinding skb = null;
            if ( context.getSymmetricKeyBinding() != null) {
                skb = context.getSymmetricKeyBinding();
                context.setSymmetricKeyBinding(null);
            } else {
                skb = (SymmetricKeyBinding)keyBinding;
            }
            
            KeyInfoHeaderBlock keyInfoBlock  = null;
            
            if(!skb.getKeyIdentifier().equals(MessageConstants._EMPTY)){
                keyEncAlgo = skb.getKeyAlgorithm();
                if(keyEncAlgo != null && !"".equals(keyEncAlgo)){
                    _symmetricKey = SecurityUtil.generateSymmetricKey(dataEncAlgo);
                }
                keyInfoStrategy = KeyInfoStrategy.getInstance(MessageConstants.KEY_NAME_TYPE);
                keyEncSK = skb.getSecretKey();
                symmetricKeyName = skb.getKeyIdentifier();
                String secKeyAlgo = keyEncSK.getAlgorithm();
                
                if(_symmetricKey == null){
                    ((KeyNameStrategy)keyInfoStrategy).setKeyName(symmetricKeyName);
                    _symmetricKey = keyEncSK;
                    keyEncSK = null;
                }
            } else if (sendEKSHA1) {
                //get the signing key and EKSHA1 reference from the Subject, it was stored from the incoming message
                String ekSha1Ref = getEKSHA1Ref(context);
                _symmetricKey = skb.getSecretKey();
                
                keyInfoBlock = new KeyInfoHeaderBlock(secureMessage.getSOAPPart());
                ekTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                EncryptedKeySHA1Identifier refElem = new EncryptedKeySHA1Identifier(secureMessage.getSOAPPart());
                refElem.setReferenceValue(ekSha1Ref);
                ekTokenRef.setReference(refElem);
                //set the wsse11:TokenType attribute as required by WSS 1.1
                //ekTokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                
                referenceType = MessageConstants.EK_SHA1_TYPE;
                keyInfoStrategy = KeyInfoStrategy.getInstance(referenceType);
                //keyInfoStrategy.insertKey(ekTokenRef, secureMsg);
                
                //TODO: the below cond is always true.
            } else if (wss11Sender || wss10) {
                _symmetricKey = skb.getSecretKey();
                useStandaloneRefList = true;
                
                if(!skb.getCertAlias().equals(MessageConstants._EMPTY)){
                    certificateBinding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    //x509Binding.newPrivateKeyBinding();
                    certificateBinding.setCertificateIdentifier(skb.getCertAlias());
                    _x509Cert = context.getSecurityEnvironment().getCertificate(context.getExtraneousProperties(), certificateBinding.getCertificateIdentifier(), false);
                    certificateBinding.setX509Certificate(_x509Cert);
                    certificateBinding.setReferenceType("Direct");
                }else if ( context.getX509CertificateBinding() != null ) {
                    certificateBinding = context.getX509CertificateBinding();
                    context.setX509CertificateBinding(null);
                }
                
                _x509Cert = certificateBinding.getX509Certificate();
                x509TokenId = certificateBinding.getUUID();
                if(x509TokenId == null || x509TokenId.equals("")){
                    x509TokenId = secureMsg.generateId();
                }
                
                if(log.isLoggable(Level.FINEST)){
                    log.log(Level.FINEST, "Certificate was "+_x509Cert);
                    log.log(Level.FINEST, "BinaryToken ID "+x509TokenId);
                }
                
                HashMap tokenCache = context.getTokenCache();
                HashMap insertedX509Cache = context.getInsertedX509Cache();
                
                SecurityUtil.checkIncludeTokenPolicy(context, certificateBinding, x509TokenId);
                
                X509SecurityToken token = (X509SecurityToken)tokenCache.get(x509TokenId);
                if(token == null){
                    String valueType = certificateBinding.getValueType();
                    if(valueType==null||valueType.equals("")){
                        //default valueType for X509 as v3
                        valueType = MessageConstants.X509v3_NS;
                    }
                    token = new X509SecurityToken(secureMsg.getSOAPPart(),_x509Cert,x509TokenId, valueType);
                    tokenCache.put(x509TokenId, token);
                    context.setCurrentSecret(_symmetricKey);
                } else{
                    skbX509TokenInserted = true;
                    _symmetricKey = context.getCurrentSecret();
                    
                }
                ekTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                DirectReference reference = new DirectReference();
                insertedEkId = (String)ekCache.get(x509TokenId);
                if(insertedEkId == null)
                    insertedEkId = ekId;
                reference.setURI("#"+insertedEkId);
                reference.setValueType(MessageConstants.EncryptedKey_NS);
                ekTokenRef.setReference(reference);
                
                if(!skbX509TokenInserted){
                    
                    referenceType =  certificateBinding.getReferenceType();
                    if(referenceType.equals("Identifier") && certificateBinding.getValueType().equals(MessageConstants.X509v1_NS)){
                        log.log(Level.SEVERE,"WSS1211.unsupported.KeyIdentifierStrategy.X509v1");
                        throw new XWSSecurityException("Key Identifier strategy with X509v1 is not allowed");
                    }
                    keyInfoStrategy = KeyInfoStrategy.getInstance(referenceType);
                    _exportCertificate = true;
                    keyInfoStrategy.setCertificate(_x509Cert);
                    //Store SymmetricKey generated in ProcessingContext
                    context.setExtraneousProperty("SecretKey", _symmetricKey);
                }
                if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(referenceType)){
                    if(insertedX509Cache.get(x509TokenId) == null){
                        secureMsg.findOrCreateSecurityHeader().insertHeaderBlock(token);
                        insertedX509Cache.put(x509TokenId, token);
                        x509TokenElement = secureMsg.findOrCreateSecurityHeader().getNextSiblingOfTimestamp();
                    } else{
                        //x509TokenElement = secureMsg.findOrCreateSecurityHeader().getFirstChildElement();
                        x509TokenElement = secureMsg.getElementByWsuId(x509TokenId);
                    }
                }
                
            }
            
        } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
            //TODO handler saml, it should be a remote SAML Assertion
            // since a message from the sender cannot have the receivers assertion as part of message
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding;
            
            Assertion assertion1 = null;
            Assertion assertion2 = null;
            
            try {
                if (System.getProperty("com.sun.xml.wss.saml.binding.jaxb") == null ) {
                    if (samlBinding.getAssertion().getAttributeNode("ID") != null) {
                        assertion1 = (Assertion)com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion.fromElement(samlBinding.getAssertion());
                    }else{
                        assertion1 = (Assertion)com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion.fromElement(samlBinding.getAssertion());
                    }
                } else {
                    assertion2 = (Assertion)com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion.fromElement(samlBinding.getAssertion());
                }
            } catch (SAMLException ex) {
                log.log(Level.SEVERE, "WSS1212.error.SAMLAssertionException");
                throw new XWSSecurityException(ex);
            }
            
            String assertionID = null;
            if (assertion1 != null) {
                HashMap tokenCache = context.getTokenCache();
                //assuming unique IDs
                assertionID = ((com.sun.xml.wss.saml.Assertion)assertion1).getAssertionID();
                tokenCache.put(assertionID, assertion1);
            } else if (assertion2 != null) {
                HashMap tokenCache = context.getTokenCache();
                //assuming unique IDs
                assertionID = ((com.sun.xml.wss.saml.Assertion)assertion2).getAssertionID();
                tokenCache.put(assertionID, assertion2);
            } else{
                log.log(Level.SEVERE,"WSS1213.null.SAMLAssertion");
                throw new XWSSecurityException("SAML Assertion is NULL");
            }
            
            //Key key = null;
            samlkey = KeyResolver.resolveSamlAssertion(
                    context.getSecurableSoapMessage(), samlBinding.getAssertion(), true, context, assertionID);
            
            /*
            _x509Cert = context.getSecurityEnvironment().getCertificate(
                    context.getExtraneousProperties() ,(PublicKey)key, false);
            if (_x509Cert == null) {
                log.log(Level.SEVERE,"WSS1214.unableto.locate.certificate.SAMLAssertion");
                throw new XWSSecurityException("Could not locate Certificate corresponding to Key in SubjectConfirmation of SAML Assertion");
            }*/
            
            if (!"".equals(samlBinding.getKeyAlgorithm())) {
                keyEncAlgo = samlBinding.getKeyAlgorithm();
            }
            
            _symmetricKey = SecurityUtil.generateSymmetricKey(dataEncAlgo);
            
            referenceType = samlBinding.getReferenceType();
            if (referenceType.equals(MessageConstants.EMBEDDED_REFERENCE_TYPE)) {
                log.log(Level.SEVERE, "WSS1215.unsupported.EmbeddedReference.SAMLAssertion");
                throw new XWSSecurityException("Embedded Reference Type for SAML Assertions not supported yet");
            }
            
            String assertionId = null;
            if ( assertion1 != null) {
                assertionId = ((com.sun.xml.wss.saml.Assertion)assertion1).getAssertionID();
            }else if ( assertion2 != null) {
                assertionId = ((com.sun.xml.wss.saml.Assertion)assertion2).getAssertionID();
            }
            Element binding = samlBinding.getAuthorityBinding();
            samlTokenRef = new SecurityTokenReference(secureMsg.getSOAPPart());
            String strId = samlBinding.getSTRID();
            if(strId == null){
                strId = secureMsg.generateId();
            }
            samlTokenRef.setWsuId(strId);
            
            if (binding != null) {
                samlTokenRef.setSamlAuthorityBinding(binding, secureMsg.getSOAPPart());
            }
            keyInfoStrategy = new KeyIdentifierStrategy(assertionId);
            keyInfoStrategy.insertKey(samlTokenRef, secureMsg);
            
        } else if ( PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
            
            IssuedTokenContext trustContext =  context.getTrustContext();
            
            //get the symmetric key for encryption
            try{
                _symmetricKey = new SecretKeySpec(trustContext.getProofKey(), SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
            } catch(Exception e){
                log.log(Level.SEVERE, "WSS1216.unableto.get.symmetrickey.Encryption");
                throw new XWSSecurityException(e);
            }
            
            //Get the IssuedToken and insert it into the message
            GenericToken issuedToken = (GenericToken)trustContext.getSecurityToken();
            
            // check if the token is already present
            IssuedTokenKeyBinding ikb = (IssuedTokenKeyBinding)keyBinding;
            //String ikbPolicyId = ikb.getPolicyToken().getTokenId();
            String ikbPolicyId = ikb.getUUID();
            
            //Look for TrustToken in TokenCache
            HashMap tokCache = context.getTokenCache();
            Object tok = tokCache.get(ikbPolicyId);
            
            SecurityTokenReference str = null;
            Element strElem = null;
            String tokenVersion = ikb.getIncludeToken();
            includeIST = (IssuedTokenKeyBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals(tokenVersion) ||
                          IssuedTokenKeyBinding.INCLUDE_ALWAYS.equals(tokenVersion) ||
                          IssuedTokenKeyBinding.INCLUDE_ALWAYS_VER2.equals(tokenVersion) ||
                          IssuedTokenKeyBinding.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(tokenVersion)
                          );
            
            if (includeIST && (issuedToken == null)) {
                log.log(Level.SEVERE, "WSS1217.null.IssueToken");
                throw new XWSSecurityException("Issued Token to be inserted into the Message was Null");
            }
            
            //trust token to be inserted into message
            if (issuedToken != null) {
                // treat the token as an Opaque entity and just insert the token into message
                Element elem = (Element)issuedToken.getTokenValue();
                //TODO: remove these expensive conversions DOM Imports
                if (tok == null) {
                    issuedTokenElement = XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), elem);
                    //Temp FIX for Issue#26: We need an Id to cache and MS not sending Id in some cases
                    String tokId = issuedTokenElement.getAttribute("Id");
                    if ("".equals(tokId) &&
                            MessageConstants.ENCRYPTED_DATA_LNAME.equals(issuedTokenElement.getLocalName())) {
                        issuedTokenElement.setAttribute("Id", secureMessage.generateId());
                    }
                    tokCache.put(ikbPolicyId, issuedTokenElement);
                } else {
                    issuedTokenInserted = true;
                    // it will be SOAPElement retrieve its wsuId attr
                    String wsuId = SecurityUtil.getWsuIdOrId((Element)tok);
                    issuedTokenElementFromMsg = secureMessage.getElementById(wsuId);
                    if (issuedTokenElementFromMsg == null) {
                        log.log(Level.SEVERE, "WSS1218.unableto.locate.IssueToken.Message");
                        throw new XWSSecurityException("Could not locate Issued Token in Message");
                    }
                }
            }
            
            if (includeIST) {
                if (trustContext.getAttachedSecurityTokenReference() != null) {
                    strElem = SecurityUtil.convertSTRToElement(trustContext.getAttachedSecurityTokenReference().getTokenValue(), secureMessage.getSOAPPart());
                } else {
                    log.log(Level.SEVERE, "WSS1219.unableto.refer.Attached.IssueToken");
                    throw new XWSSecurityException("Cannot determine how to reference the Attached Issued Token in the Message");
                }
            } else {
                //Trust Issued Token should not be in message at all, so use an external reference
                if (trustContext.getUnAttachedSecurityTokenReference() != null) {
                    strElem = SecurityUtil.convertSTRToElement(trustContext.getUnAttachedSecurityTokenReference().getTokenValue(), secureMessage.getSOAPPart());
                } else {
                    log.log(Level.SEVERE, "WSS1220.unableto.refer.Un-Attached.IssueToken");
                    throw new XWSSecurityException("Cannot determine how to reference the Un-Attached Issued Token in the Message");
                }
            }
            
            //TODO: remove these expensive conversions
            Element imported = (Element)secureMessage.getSOAPPart().importNode(strElem,true);
            issuedTokenRef = new SecurityTokenReference(XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), imported), false);
            SecurityUtil.updateSamlVsKeyCache(issuedTokenRef, context, _symmetricKey);
            
        } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)) {
            
            SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)keyBinding;
            
            //String sctPolicyId = sctBinding.getPolicyToken().getTokenId();
            String sctPolicyId = sctBinding.getUUID();
            //Look for SCT in TokenCache
            HashMap tokCache = context.getTokenCache();
            sct = (SecurityContextTokenImpl)tokCache.get(sctPolicyId);
            
            IssuedTokenContext ictx = context.getSecureConversationContext();
            
            if (sct == null) {
                SecurityContextToken sct1 =(SecurityContextToken)ictx.getSecurityToken();
                if (sct1 == null) {
                    log.log(Level.SEVERE,"WSS1221.null.SecureConversationToken");
                    throw new XWSSecurityException("SecureConversation Token not Found");
                }
                
                sct = new SecurityContextTokenImpl(
                        secureMessage.getSOAPPart(), sct1.getIdentifier().toString(), sct1.getInstance(), sct1.getWsuId(), sct1.getExtElements());
                // put back in token cache
                tokCache.put(sctPolicyId, sct);
            } else {
                sctTokenInserted = true;
                // record the element
                sctElement = secureMessage.getElementByWsuId(sct.getWsuId());
            }
            
            String sctWsuId = sct.getWsuId();
            if (sctWsuId == null) {
                sct.setId(secureMessage.generateId());
            }
            sctWsuId = sct.getWsuId();
            
            secConvRef = new SecurityTokenReference(secureMessage.getSOAPPart());
            DirectReference reference = new DirectReference();
            if (SecureConversationTokenKeyBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals(sctBinding.getIncludeToken()) ||
                    SecureConversationTokenKeyBinding.INCLUDE_ALWAYS.equals(sctBinding.getIncludeToken())) {
                
                reference.setURI("#" + sctWsuId);
            } else {
                includeSCT = false;
                reference.setSCTURI(sct.getIdentifier().toString(), sct.getInstance());
            }
            
            secConvRef.setReference(reference);
            referenceType = MessageConstants.DIRECT_REFERENCE_TYPE;
            keyInfoStrategy = KeyInfoStrategy.getInstance(referenceType);
            
            String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo);
            _symmetricKey = new SecretKeySpec(ictx.getProofKey(), jceAlgo);
            
            
        } else if (PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)){
            DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
            WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
            
            String algorithm = null;
            if(algSuite != null){
                algorithm = algSuite.getEncryptionAlgorithm();
            }
            //The offset and length to be used for DKT
            long offset = 0; // Default 0
            long length = SecurityUtil.getLengthFromAlgorithm(algorithm);
            
            if (PolicyTypeUtil.x509CertificateBinding(originalKeyBinding)) {
                //throw new XWSSecurityException("Asymmetric Binding with DerivedKeys under X509Token Policy Not Yet Supported");
            } else if ( PolicyTypeUtil.symmetricKeyBinding(originalKeyBinding)) {
                SymmetricKeyBinding skb = null;
                if ( context.getSymmetricKeyBinding() != null) {
                    skb = context.getSymmetricKeyBinding();
                    context.setSymmetricKeyBinding(null);
                } else{
                    skb = (SymmetricKeyBinding)originalKeyBinding;
                }
                
                if(sendEKSHA1){
                    String ekSha1Ref = getEKSHA1Ref(context);
                    //Construct a derivedKeyToken to be used
                    originalKey = skb.getSecretKey();
                    byte[] secret = originalKey.getEncoded();
                    DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret);
                    String dktId = secureMessage.generateId();
                    String nonce = Base64.encode(dkt.getNonce());
                    //get the symmetric key for encryption key from derivedkeyToken
                    try{
                        String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(algorithm);
                        _symmetricKey = dkt.generateSymmetricKey(jceAlgo);
                    } catch(Exception e){
                        log.log(Level.SEVERE, "WSS1216.unableto.get.symmetrickey.Encryption");
                        throw new XWSSecurityException(e);
                    }
                    //STR for DerivedKeyToken
                    SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    EncryptedKeySHA1Identifier refElem = new EncryptedKeySHA1Identifier(secureMessage.getSOAPPart());
                    refElem.setReferenceValue(ekSha1Ref);
                    tokenRef.setReference(refElem);
                    
                    //set the wsse11:TokenType attribute as required by WSS 1.1
                    //TODO: uncomment this once MS is ready to accpet this
                    //tokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                    
                    dktHeadrBlock =
                            new DerivedKeyTokenHeaderBlock(_secHeader.getOwnerDocument(), tokenRef, nonce, dkt.getOffset(), dkt.getLength() ,dktId);
                    
                    //Construct the STR for Encryption
                    DirectReference reference = new DirectReference();
                    reference.setURI("#"+dktId);
                    ekTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    ekTokenRef.setReference(reference);
                } else if(wss11Sender || wss10){
                    dktSender = true;
                    originalKey = skb.getSecretKey();
                    if ( context.getX509CertificateBinding() != null ) {
                        certificateBinding = context.getX509CertificateBinding();
                        context.setX509CertificateBinding(null);
                        _x509Cert = certificateBinding.getX509Certificate();
                    }
                    _x509Cert = certificateBinding.getX509Certificate();
                    referenceType =  certificateBinding.getReferenceType();
                    keyInfoStrategy = KeyInfoStrategy.getInstance(referenceType);
                    _exportCertificate = true;
                    keyInfoStrategy.setCertificate(_x509Cert);
                    x509TokenId = certificateBinding.getUUID();
                    if(x509TokenId == null || x509TokenId.equals("")){
                        x509TokenId = secureMsg.generateId();
                    }
                    
                    if(log.isLoggable(Level.FINEST)){
                        log.log(Level.FINEST, "Certificate was "+_x509Cert);
                        log.log(Level.FINEST, "BinaryToken ID "+x509TokenId);
                    }
                    
                    
                    HashMap tokenCache = context.getTokenCache();
                    HashMap insertedX509Cache = context.getInsertedX509Cache();
                    
                    SecurityUtil.checkIncludeTokenPolicy(context, certificateBinding, x509TokenId);
                    
                    // ReferenceType adjustment in checkIncludeTokenPolicy is also currently
                    // causing an insertion of the X509 into the Message
                    X509SecurityToken insertedx509 =
                            (X509SecurityToken)context.getInsertedX509Cache().get(x509TokenId);
                    
                    // this one is used to determine if the whole BST + EK + DKT(opt)
                    // has been inserted by another filter such as Encryption running before
                    X509SecurityToken token = (X509SecurityToken)tokenCache.get(x509TokenId);
                    if(token == null){
                        if (insertedx509 != null) {
                            token = insertedx509;
                            tokenCache.put(x509TokenId, insertedx509);
                        } else {
                            String valueType = certificateBinding.getValueType();
                            if(valueType==null||valueType.equals("")){
                                //default valueType for X509 as v3
                                valueType = MessageConstants.X509v3_NS;
                            }
                            token = new X509SecurityToken(secureMsg.getSOAPPart(),_x509Cert,x509TokenId, valueType);
                            tokenCache.put(x509TokenId, token);
                        }
                        context.setCurrentSecret(originalKey);
                        //Store SymmetricKey generated in ProcessingContext
                        context.setExtraneousProperty("SecretKey", originalKey);
                    } else{
                        skbX509TokenInserted = true;
                        originalKey = context.getCurrentSecret();
                    }
                    //
                    if(insertedx509 == null){
                        if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(referenceType)){
                            secureMsg.findOrCreateSecurityHeader().insertHeaderBlock(token);
                            insertedX509Cache.put(x509TokenId, token);
                            x509TokenElement = secureMsg.findOrCreateSecurityHeader().getNextSiblingOfTimestamp();
                        }
                    } else{
                        //x509TokenElement = secureMsg.getElementByWsuId(x509TokenId);
                        x509TokenElement = insertedx509;
                    }
                    //}
                    
                    //Construct a derivedKeyToken to be used
                    byte[] secret = originalKey.getEncoded();
                    DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret);
                    String dktId = secureMessage.generateId();
                    String nonce = Base64.encode(dkt.getNonce());
                    //get the symmetric key for encryption key from derivedkeyToken
                    try{
                        String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(algorithm);
                        _symmetricKey = dkt.generateSymmetricKey(jceAlgo);
                    } catch(Exception e){
                        log.log(Level.SEVERE, "WSS1216.unableto.get.symmetrickey.Encryption");
                        throw new XWSSecurityException(e);
                    }
                    
                    //STR for DerivedKeyToken
                    SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    DirectReference reference = new DirectReference();
                    //TODO: PLUGFEST Commeting for now as Microsoft setting the EncryptedKey type on reference valueType
                    //tokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                    //set id of encrypted key in STR of DKT
                    insertedEkId = (String)ekCache.get(x509TokenId);
                    if(insertedEkId == null)
                        insertedEkId = ekId;
                    reference.setURI("#"+insertedEkId);
                    reference.setValueType(MessageConstants.EncryptedKey_NS);
                    tokenRef.setReference(reference);
                    dktHeadrBlock =
                            new DerivedKeyTokenHeaderBlock(_secHeader.getOwnerDocument(), tokenRef, nonce, dkt.getOffset(), dkt.getLength(), dktId);
                    
                    //Construct the STR for Encryption
                    DirectReference refEnc = new DirectReference();
                    refEnc.setURI("#"+dktId);
                    ekTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    ekTokenRef.setReference(refEnc);
                }
            } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(originalKeyBinding)) {
                
                sctWithDKT = true;
                
                SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)originalKeyBinding;
                //String sctPolicyId = sctBinding.getPolicyToken().getTokenId();
                String sctPolicyId = sctBinding.getUUID();
                //Look for SCT in TokenCache
                HashMap tokCache = context.getTokenCache();
                sct = (SecurityContextTokenImpl)tokCache.get(sctPolicyId);
                
                IssuedTokenContext ictx = context.getSecureConversationContext();
                
                if (sct == null) {
                    SecurityContextToken sct1 =(SecurityContextToken)ictx.getSecurityToken();
                    if (sct1 == null) {
                        log.log(Level.SEVERE, "WSS1221.null.SecureConversationToken");
                        throw new XWSSecurityException("SecureConversation Token not Found");
                    }
                    
                    sct = new SecurityContextTokenImpl(
                            secureMessage.getSOAPPart(), sct1.getIdentifier().toString(), sct1.getInstance(), sct1.getWsuId(), sct1.getExtElements());
                    // put back in token cache
                    tokCache.put(sctPolicyId, sct);
                } else {
                    sctTokenInserted = true;
                    // record the element
                    sctElement = secureMessage.getElementByWsuId(sct.getWsuId());
                }
                
                String sctWsuId = sct.getWsuId();
                if (sctWsuId == null) {
                    sct.setId(secureMessage.generateId());
                }
                sctWsuId = sct.getWsuId();
                
                byte[] secret =  context.getSecureConversationContext().getProofKey();
                DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret);
                String dktId = secureMessage.generateId();
                String nonce = Base64.encode(dkt.getNonce());
                //get the symmetric key for encryption key from derivedkeyToken
                try{
                    _symmetricKey = dkt.generateSymmetricKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                } catch(Exception e){
                    log.log(Level.SEVERE, "WSS1216.unableto.get.symmetrickey.Encryption");
                    throw new XWSSecurityException(e);
                }
                //STR for DerivedKeyToken
                SecurityTokenReference secRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                DirectReference reference = new DirectReference();
                if (SecureConversationTokenKeyBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals(sctBinding.getIncludeToken()) ||
                        SecureConversationTokenKeyBinding.INCLUDE_ALWAYS.equals(sctBinding.getIncludeToken())) {
                    
                    reference.setURI("#" + sctWsuId);
                } else {
                    includeSCT = false;
                    reference.setSCTURI(sct.getIdentifier().toString(), sct.getInstance());
                }
                secRef.setReference(reference);
                dktHeadrBlock =
                        new DerivedKeyTokenHeaderBlock(_secHeader.getOwnerDocument(), secRef, nonce, dkt.getOffset(), dkt.getLength(),dktId);
                
                //Construct the STR for Encryption
                DirectReference refEnc = new DirectReference();
                refEnc.setURI("#"+dktId);
                dktSctTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                dktSctTokenRef.setReference(refEnc);
                
            } else if (PolicyTypeUtil.issuedTokenKeyBinding(originalKeyBinding)) {
                
                issuedWithDKT = true;
                
                IssuedTokenContext trustContext =  context.getTrustContext();
                DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, trustContext.getProofKey());
                String dktId = secureMessage.generateId();
                String nonce = Base64.encode(dkt.getNonce());
                
                //get the symmetric key for encryption
                Key origKey = null;
                try{
                    origKey = new SecretKeySpec(trustContext.getProofKey(), SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                } catch(Exception e){
                    log.log(Level.SEVERE, "WSS1216.unableto.get.symmetrickey.Encryption");
                    throw new XWSSecurityException(e);
                }
                
                
                //get the symmetric key for encryption key from derivedkeyToken
                try{
                    _symmetricKey = dkt.generateSymmetricKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                } catch(Exception e){
                    log.log(Level.SEVERE, "WSS1216.unableto.get.symmetrickey.Encryption");
                    throw new XWSSecurityException(e);
                }
                
                //Get the IssuedToken and insert it into the message
                GenericToken issuedToken = (GenericToken)trustContext.getSecurityToken();
                
                // check if the token is already present
                IssuedTokenKeyBinding ikb = (IssuedTokenKeyBinding)originalKeyBinding;
                //String ikbPolicyId = ikb.getPolicyToken().getTokenId();
                String ikbPolicyId = ikb.getUUID();
                //Look for TrustToken in TokenCache
                HashMap tokCache = context.getTokenCache();
                Object tok = tokCache.get(ikbPolicyId);
                
                SecurityTokenReference str = null;
                Element strElem = null;
                String tokenVersion = ikb.getIncludeToken();
                includeIST = (IssuedTokenKeyBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals(tokenVersion) ||
                          IssuedTokenKeyBinding.INCLUDE_ALWAYS.equals(tokenVersion) ||
                          IssuedTokenKeyBinding.INCLUDE_ALWAYS_VER2.equals(tokenVersion) ||
                          IssuedTokenKeyBinding.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(tokenVersion)
                          );
                
                if (includeIST && (issuedToken == null)) {
                    log.log(Level.SEVERE, "WSS1217.null.IssueToken");
                    throw new XWSSecurityException("Issued Token to be inserted into the Message was Null");
                }
                
                if (issuedToken != null) {
                    // treat the token as an Opaque entity and just insert the token into message
                    Element elem = (Element)issuedToken.getTokenValue();
                    //TODO: remove these expensive conversions DOM Imports
                    if (tok == null) {
                        issuedTokenElement = XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), elem);
                        //Temp FIX for Issue#26: We need an Id to cache and MS not sending Id in some cases
                        String tokId = issuedTokenElement.getAttribute("Id");
                        if ("".equals(tokId) &&
                                MessageConstants.ENCRYPTED_DATA_LNAME.equals(issuedTokenElement.getLocalName())) {
                            issuedTokenElement.setAttribute("Id", secureMessage.generateId());
                        }
                        tokCache.put(ikbPolicyId, issuedTokenElement);
                    } else {
                        issuedTokenInserted = true;
                        // it will be SOAPElement retrieve its wsuId attr
                        String wsuId = SecurityUtil.getWsuIdOrId((Element)tok);
                        issuedTokenElementFromMsg = secureMessage.getElementById(wsuId);
                        if (issuedTokenElementFromMsg == null) {
                            log.log(Level.SEVERE, "WSS1218.unableto.locate.IssueToken.Message");
                            throw new XWSSecurityException("Could not locate Issued Token in Message");
                        }
                    }
                }
                
                if (includeIST) {
                    if (trustContext.getAttachedSecurityTokenReference() != null) {
                        strElem = (Element)trustContext.getAttachedSecurityTokenReference().getTokenValue();
                    } else {
                        log.log(Level.SEVERE, "WSS1219.unableto.refer.Attached.IssueToken");
                        throw new XWSSecurityException("Cannot determine how to reference the Attached Issued Token in the Message");
                    }
                } else {
                    //Trust Issued Token should not be in message at all, so use an external reference
                    if (trustContext.getUnAttachedSecurityTokenReference() != null) {
                        strElem = (Element)trustContext.getUnAttachedSecurityTokenReference().getTokenValue();
                    } else {
                        log.log(Level.SEVERE, "WSS1220.unableto.refer.Un-Attached.IssueToken");
                        throw new XWSSecurityException("Cannot determine how to reference the Un-Attached Issued Token in the Message");
                    }
                }
                
                //TODO: remove these expensive conversions
                Element imported = (Element)secureMessage.getSOAPPart().importNode(strElem,true);
                str = new SecurityTokenReference(
                        XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), (Element)imported.cloneNode(true)), false);
                
                if (origKey != null) {
                    SecurityUtil.updateSamlVsKeyCache(str, context, origKey);
                }
                
                dktHeadrBlock =
                        new DerivedKeyTokenHeaderBlock(_secHeader.getOwnerDocument(), str, nonce, dkt.getOffset(), dkt.getLength(),dktId);
                //Construct the STR for Encryption
                DirectReference refEnc = new DirectReference();
                refEnc.setURI("#"+dktId);
                dktIssuedTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                dktIssuedTokenRef.setReference(refEnc);
                
            }
        } else {
            log.log(Level.SEVERE, "WSS1222.unsupported.KeyBinding.EncryptionPolicy");
            throw new XWSSecurityException("Unsupported Key Binding for EncryptionPolicy");
        }
        
        
        XMLCipher _keyEncryptor = null;
        XMLCipher _dataEncryptor = null;
        Cipher _attachmentEncryptor = null;
        try {
            // lazy n static instantiation can happen
            //TODO :: Algorithms -- Venu
            if(log.isLoggable(Level.FINEST)){
                log.log(Level.FINEST, "KeyEncryption algorithm is "+keyEncAlgo);
            }
            
            if (_x509Cert != null) {
                //prepare for keytransport
                _keyEncryptor = XMLCipher.getInstance(keyEncAlgo);
                _keyEncryptor.init(XMLCipher.WRAP_MODE, _x509Cert.getPublicKey());
            } else if (samlkey != null) {
                //prepare for keytransport
                _keyEncryptor = XMLCipher.getInstance(keyEncAlgo);
                _keyEncryptor.init(XMLCipher.WRAP_MODE, samlkey);
            }else if( keyEncSK != null){
                //prepare for keywrap
                _keyEncryptor = XMLCipher.getInstance(keyEncAlgo);
                _keyEncryptor.init(XMLCipher.WRAP_MODE, keyEncSK);
            }
            
            if(log.isLoggable(Level.FINEST)){
                log.log(Level.FINEST, "Data encryption algorithm is "+dataEncAlgo);
            }
            
            String dataAlgorithm =  JCEMapper.translateURItoJCEID(dataEncAlgo);
            _dataEncryptor = XMLCipher.getInstance(dataEncAlgo);
            
            _dataEncryptor.init(XMLCipher.ENCRYPT_MODE, _symmetricKey);
            
        } catch (Exception xee) {
            log.log(Level.SEVERE, "WSS1205.unableto.initialize.xml.cipher",xee);
            throw new XWSSecurityException(
                    "Unable to initialize XML Cipher", xee);
        }
        
        ArrayList targets =  featureBinding.getTargetBindings();
        
        ArrayList _aparts = new ArrayList();
        ArrayList _dnodes = new ArrayList();
        
        Iterator i = targets.iterator();
        //TODO : remove all  the three while loops and
        //convert to a 2 loop - Venu
        while (i.hasNext()) {
            EncryptionTarget target = (EncryptionTarget)i.next();
            boolean contentOnly = target.getContentOnly();
            Boolean cOnly = Boolean.valueOf(contentOnly);
            if(MessageConstants.PROCESS_ALL_ATTACHMENTS.equals(target.getValue())){
                Iterator itr = secureMsg.getAttachments();
                while(itr.hasNext()){
                    AttachmentPart ap = (AttachmentPart)itr.next();
                    Object[] s = new Object[2];
                    s[0] = ap;
                    s[1] = cOnly;
                    _aparts.add(s);
                }
                continue;
            }
            Object mgpart = secureMsg.getMessageParts(target);
            //Change this to context.
            //TODO :: Venu
            ArrayList transforms = target.getCipherReferenceTransforms();
            if(mgpart == null){
                continue;
            } else if (mgpart instanceof AttachmentPart) {
                Object[] s = new Object[2];
                s[0] = mgpart;
                s[1] = cOnly;
                _aparts.add(s);
            } else{
                if (mgpart instanceof Node) {
                    Object[] s = new Object[2];
                    s[0] = mgpart;
                    s[1] = cOnly;
                    _dnodes.add(s);
                } else if (mgpart instanceof NodeList) {
                    for (int j=0; j<((NodeList)mgpart).getLength(); j++) {
                        Object[] s = new Object[2];
                        Node n = ((NodeList)mgpart).item(j);
                        s[0] = n;
                        s[1] = cOnly;
                        _dnodes.add(s);
                    }
                }
            }
        }
        
        if (_dnodes.isEmpty() && _aparts.isEmpty()) {
            if(log.isLoggable(Level.WARNING)){
                log.log(Level.WARNING, "None of the specified Encryption Parts found in the Message");
            }
        }
        
        EncryptedKey _encryptedKey = null;
        ReferenceListHeaderBlock _ekReferenceList = null;
        ReferenceListHeaderBlock _standaloneReferenceList = null;
        
        if (_keyEncryptor != null && !skbX509TokenInserted) {
            try {
                if(!dktSender){
                    _encryptedKey = _keyEncryptor.encryptKey(secureMsg.getSOAPPart(), _symmetricKey);
                } else{
                    _encryptedKey = _keyEncryptor.encryptKey(secureMsg.getSOAPPart(), originalKey);
                }
                _encryptedKey.setId(ekId);
                ekCache.put(x509TokenId, ekId);
                KeyInfoHeaderBlock keyInfoBlock = new KeyInfoHeaderBlock(secureMsg.getSOAPPart());
                
                if (samlTokenRef != null) {
                    keyInfoBlock.addSecurityTokenReference(samlTokenRef);
                } else if(_x509Cert != null){
                    keyInfoStrategy.insertKey(keyInfoBlock, secureMsg, x509TokenId);
                }else if(keyEncSK != null){
                    //keyInfoStrategy.insertKey(keyInfoBlock, secureMsg,null);
                    keyInfoBlock.addKeyName(symmetricKeyName);
                }
                KeyInfo keyInfo = keyInfoBlock.getKeyInfo(); /*new KeyInfo(keyInfoBlock.getAsSoapElement(), null); */
                _encryptedKey.setKeyInfo(keyInfo);
                
            } catch (Exception xe) {
                log.log(Level.SEVERE, "WSS1223.unableto.set.KeyInfo.EncryptedKey", xe);
                //xe.printStackTrace();
                throw new XWSSecurityException(xe);
            }
        }
        
        if (_encryptedKey != null && !dktSender && !useStandaloneRefList){
            _ekReferenceList = new ReferenceListHeaderBlock(secureMsg.getSOAPPart());
        }
        // process APs - push only EDs (create EDs), modify AP headers/content
        
        //When encrypting content and attachments with the same key process attachments first.
        //SWA Spec.
        SOAPElement x509Sibling = null;
        
        if(x509TokenElement != null){
            x509Sibling = (SOAPElement)x509TokenElement.getNextSibling();
        }
        Iterator _apartsI = _aparts.iterator();
        if(_apartsI.hasNext()){
            //We have attachments so get the cipher instances.
            try{
                //_attachmentEncryptor = Cipher.getInstance("DESede/CBC/ISO10126Padding");
                //TODO:GETMAP -venu
                _attachmentEncryptor = XMLCipherAdapter.constructCipher(dataEncAlgo);
                _attachmentEncryptor.init(Cipher.ENCRYPT_MODE, _symmetricKey);
            } catch (Exception xee) {
                log.log(Level.SEVERE, "WSS1205.unableto.initialize.xml.cipher", xee);
                throw new XWSSecurityException(
                        "Unable to initialize XML Cipher", xee);
            }
        }
        while (_apartsI.hasNext()) {
            Object[] s = (Object[])_apartsI.next();
            AttachmentPart p = (AttachmentPart)s[0];
            boolean b = ((Boolean)s[1]).booleanValue();
            
            // create n push an ED
            
            EncryptedDataHeaderBlock edhb = new EncryptedDataHeaderBlock();
            
            String id = secureMsg.generateId();
            
            edhb.setId(id);
            edhb.setType( (b ?  MessageConstants.ATTACHMENT_CONTENT_ONLY_URI : MessageConstants.ATTACHMENT_COMPLETE_URI));
            edhb.setMimeType(p.getContentType());
            
            String uri = p.getContentId();
            if (uri != null) {
                if ( uri.charAt(0) == '<' && uri.charAt(uri.length()-1) == '>'){
                    uri = "cid:" + uri.substring(1, uri.length()-1);
                }else{
                    uri = "cid:" + uri;
                }
            } else {
                uri = p.getContentLocation();
            }
            
            edhb.getCipherReference(true, uri);
            edhb.setEncryptionMethod(dataEncAlgo);
            edhb.addTransform(MessageConstants.ATTACHMENT_CONTENT_ONLY_TRANSFORM_URI);
            
            encryptAttachment(p, b, _attachmentEncryptor);
            
            if (_ekReferenceList != null){
                _ekReferenceList.addReference("#"+id);
            }
            if(x509Sibling == null && x509TokenElement == null){
                _secHeader.insertHeaderBlock(edhb);
            }else{
                if(x509Sibling != null){
                    _secHeader.insertBefore(edhb,x509Sibling);
                }else{
                    _secHeader.appendChild(edhb);
                }
            }
        }
        int optType = -1;
        Iterator _dnodeI = _dnodes.iterator();
        while (_dnodeI.hasNext()) {
            Object[] s = (Object[])_dnodeI.next();
            Node     n = (Node)s[0];
            boolean  b = ((Boolean)s[1]).booleanValue();
            //TODO :Add Transforms here.
            Element ed = null;
            boolean _fi = false;
            if(context.getConfigType() == MessageConstants.SIGN_ENCRYPT_BODY ){
                if(_fi){
                    ed = encryptBodyContent(secureMsg,context.getCanonicalizedData(),_dataEncryptor);
                }else{
                    signEncrypt(context, null,_ekReferenceList,_standaloneReferenceList,keyInfoStrategy, dataEncAlgo);
                    continue;
                }
            }else{
                if(n.getNodeType() == Node.TEXT_NODE){
                    ed = encryptElement(secureMsg, (SOAPElement) n.getParentNode(),true, _dataEncryptor);
                }else{
                    ed = encryptElement(secureMsg, (SOAPElement)n, b, _dataEncryptor);
                }
            }
            EncryptedHeaderBlock ehb = null;
            boolean isEhb = false;
            EncryptedDataHeaderBlock xencEncryptedData = new EncryptedDataHeaderBlock(
                    XMLUtil.convertToSoapElement( secureMsg.getSOAPPart(), ed));
            
            String xencEncryptedDataId = secureMsg.generateId();
            String xencEncryptedDataRef = "#" + xencEncryptedDataId;
            if(ed.getParentNode() instanceof SOAPHeader && wss11Sender){
                isEhb = true;
                ehb = new EncryptedHeaderBlock(secureMsg.getSOAPPart());
                ehb.setId(xencEncryptedDataId);
                ehb.copyAttributes(secureMsg, _secHeader);
            }else{
                xencEncryptedData.setId(xencEncryptedDataId);
            }
            
            if (_ekReferenceList != null){
                _ekReferenceList.addReference(xencEncryptedDataRef);
            }else {
                if (_standaloneReferenceList == null){
                    _standaloneReferenceList = new ReferenceListHeaderBlock(secureMsg.getSOAPPart());
                }
                _standaloneReferenceList.addReference(xencEncryptedDataRef);
                
                KeyInfoHeaderBlock keyInfoBlock = new KeyInfoHeaderBlock(secureMsg.getSOAPPart());
                SecurityTokenReference cloned = null;
                if (dktSctTokenRef != null) {
                    cloned = new SecurityTokenReference((SOAPElement)dktSctTokenRef.cloneNode(true));
                    keyInfoBlock.addSecurityTokenReference(cloned);
                } else if (secConvRef != null) {
                    cloned = new SecurityTokenReference((SOAPElement)secConvRef.cloneNode(true));
                    keyInfoBlock.addSecurityTokenReference(cloned);
                } else if(ekTokenRef != null){
                    cloned = new SecurityTokenReference((SOAPElement)ekTokenRef.cloneNode(true));
                    keyInfoBlock.addSecurityTokenReference(cloned);
                } else if (dktIssuedTokenRef != null) {
                    cloned = new SecurityTokenReference((SOAPElement)dktIssuedTokenRef.cloneNode(true));
                    keyInfoBlock.addSecurityTokenReference(cloned);
                } else if (issuedTokenRef != null) {
                    cloned = new SecurityTokenReference((SOAPElement)issuedTokenRef.cloneNode(true));
                    keyInfoBlock.addSecurityTokenReference(cloned);
                } else {
                    
                    if (PolicyTypeUtil.x509CertificateBinding(keyBinding)){
                        //to handle EncryptBeforeSigning we split EK and RefList even in this case
                        DirectReference dRef = new DirectReference();
                        dRef.setURI("#"+ekId);
                        ekDirectRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                        ekDirectRef.setReference(dRef);
                        keyInfoBlock.addSecurityTokenReference(ekDirectRef);
                        
                    }else {
                        // this is the default KeyName case
                        keyInfoStrategy.insertKey(keyInfoBlock, secureMsg, null);
                    }
                    
                }
                xencEncryptedData.setKeyInfo(keyInfoBlock);
            }
            
            if(isEhb){
                try{
                    ed.getParentNode().replaceChild(ehb.getAsSoapElement(), ed);
                    ehb.addChildElement(xencEncryptedData.getAsSoapElement());
                }catch(Exception se){se.printStackTrace();}
            } else{
                ed.getParentNode().replaceChild(xencEncryptedData.getAsSoapElement(), ed);
            }
        }
        
        try {
            x509Sibling = null;
            
            if(x509TokenElement != null){
                x509Sibling = (SOAPElement)x509TokenElement.getNextSibling();
            }
            
            if (_encryptedKey != null) {
                SOAPElement se = (SOAPElement)_keyEncryptor.martial(_encryptedKey);
                se = _secHeader.makeUsable(se);
                if(_ekReferenceList != null)
                    se.appendChild(_ekReferenceList.getAsSoapElement());
                
                //store EKSHA1 of KeyValue contents in context
                Element cipherData = (Element)se.getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                byte[] decodedCipher = Base64.decode(cipherValue);
                byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                String encEkSha1 = Base64.encode(ekSha1);
                context.setExtraneousProperty("EncryptedKeySHA1", encEkSha1);
                
                if(x509Sibling == null ){
                    if(x509TokenElement == null){
                        _secHeader.insertHeaderBlockElement(se);
                    }else{
                        _secHeader.appendChild(se);
                    }
                }else{
                    _secHeader.insertBefore(se,x509Sibling);
                }
                //For SymmetricBinding  with X509 case and for Asym with E before S
                if (_standaloneReferenceList != null){
                    _secHeader.insertBefore(_standaloneReferenceList, se.getNextSibling());
                    context.setCurrentReferenceList(se.getNextSibling());
                }
            }else{
                if (_standaloneReferenceList != null){
                    // if  SCT or IssuedToken is not already in message then do what we did before WSIT
                    if ((sctElement == null) && (issuedTokenElementFromMsg == null)) {
                        if (insertedEkId != null) {
                            //insert the standalone reflist under EK
                            Element ekElem = secureMessage.getElementById(insertedEkId);
                            _secHeader.insertBefore(_standaloneReferenceList, ekElem.getNextSibling());
                            
                        } else {
                            _secHeader.insertHeaderBlock(_standaloneReferenceList);
                            context.setCurrentReferenceList(_standaloneReferenceList.getAsSoapElement());
                        }
                    } else {
                        // insert standalone reflist under the  SCT/Issued Token
                        if (sctElement != null) {
                            _secHeader.insertBefore(_standaloneReferenceList, sctElement.getNextSibling());
                        }else if (issuedTokenElementFromMsg != null) {
                            _secHeader.insertBefore(_standaloneReferenceList, issuedTokenElementFromMsg.getNextSibling());
                        } else {
                            _secHeader.insertHeaderBlock(_standaloneReferenceList);
                            context.setCurrentReferenceList(_standaloneReferenceList.getAsSoapElement());
                        }
                    }
                }
            }
            
            if (sctWithDKT || issuedWithDKT) {
                // SCT or IssuedToken not in message so insert it above the DKT in SecHeader
                if (sctElement == null && (sct != null)) {
                    _secHeader.insertHeaderBlock(dktHeadrBlock);
                    if (includeSCT) {
                        _secHeader.insertHeaderBlock(sct);
                    }
                } else if (issuedTokenElementFromMsg == null && (issuedTokenElement != null)) {
                    _secHeader.insertHeaderBlock(dktHeadrBlock);
                    if (includeIST) {
                        _secHeader.insertHeaderBlockElement(issuedTokenElement);
                    }
                    // also store the token in Packet.invocationProperties to be used by
                    // client side response processing
                    context.setIssuedSAMLToken(issuedTokenElement);
                } else {
                    // if the token is already in Message then insert DKT below it.
                    if (sctElement != null) {
                        _secHeader.insertBefore(dktHeadrBlock, sctElement.getNextSibling());
                    } else if (issuedTokenElementFromMsg != null) {
                        _secHeader.insertBefore(dktHeadrBlock, issuedTokenElementFromMsg.getNextSibling());
                    } else {
                        _secHeader.insertHeaderBlock(dktHeadrBlock);
                    }
                }
            } else {
                //Insert DKT here
                // insert the derivedKey into SecurityHeader
                if(dktHeadrBlock != null) {
                    if(insertedEkId != null) { //If DKT referes to EK
                        Element ekElem = secureMessage.getElementById(insertedEkId);
                        _secHeader.insertBefore(dktHeadrBlock, ekElem.getNextSibling());
                    } else{
                        _secHeader.insertHeaderBlock(dktHeadrBlock);
                    }
                }
                // insert the SecurityContextToken if any in the Non DKT path
                if (!sctTokenInserted && (sct != null) && includeSCT) {
                    _secHeader.insertHeaderBlock(sct);
                }
                
                // insert trust token if any in the Non DKT path
                if (!issuedTokenInserted && (issuedTokenElement != null) && includeIST) {
                    _secHeader.insertHeaderBlockElement(issuedTokenElement);
                    // also store the token in Packet.invocationProperties to be used by
                    // client side response processing
                    context.setIssuedSAMLToken(issuedTokenElement);
                }
            }
            
        } catch (Base64DecodingException e) {
            log.log(Level.SEVERE, "WSS1224.error.insertion.HeaderBlock.SecurityHeader", e);
            throw new XWSSecurityException(e);
        } catch (NoSuchAlgorithmException e) {
            log.log(Level.SEVERE, "WSS1224.error.insertion.HeaderBlock.SecurityHeader", e);
            throw new XWSSecurityException(e);
        }
        
    }
    
    
    //Handle encryption of elememnt and element content
    
    private static Element encryptElement(SecurableSoapMessage secureMsg, SOAPElement encryptElm, boolean contentOnly, XMLCipher xmlCipher) throws XWSSecurityException {
        
        String localName = encryptElm.getLocalName();
        
        // BSP: 5607
        if (!contentOnly
                && (MessageConstants.SOAP_1_1_NS.equalsIgnoreCase(encryptElm.getNamespaceURI())
                || MessageConstants.SOAP_1_2_NS.equalsIgnoreCase(encryptElm.getNamespaceURI()))
                && ("Header".equalsIgnoreCase(localName) ||
                "Envelope".equalsIgnoreCase(localName) ||
                "Body".equalsIgnoreCase(localName)) ) {
            log.log(Level.SEVERE,
                    "WSS1206.illegal.target",
                    encryptElm.getElementName().getQualifiedName());
            throw new XWSSecurityException(
                    "Encryption of SOAP " + localName + " is not allowed"); // BSP 5607
        }
        
        SOAPPart soapPart = secureMsg.getSOAPPart();
        
        // Get the relative location of the element we are working on
        Node refNode = null;
        Node contextNode;
        if (contentOnly){
            contextNode = encryptElm;
        }else {
            contextNode = encryptElm.getParentNode();
            refNode = encryptElm.getNextSibling();
        }
        
        try {
            xmlCipher.doFinal(soapPart, encryptElm, contentOnly);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1207.unableto.encrypt.message");
            throw new XWSSecurityException("Unable to encrypt element", e);
        }
        
        Element xencEncryptedData;
        if (contentOnly){
            xencEncryptedData = (Element) contextNode.getFirstChild();
        }else {
            if (refNode == null){
                xencEncryptedData = (Element) contextNode.getLastChild();
            }else{
                xencEncryptedData = (Element) refNode.getPreviousSibling();
            }
        }
        
        return xencEncryptedData;
    }
    
    
    private static Element encryptBodyContent(SecurableSoapMessage contextNode ,byte[] canonData,XMLCipher xmlCipher)throws XWSSecurityException{
        throw new UnsupportedOperationException("Old optimizations disabled in WSIT");
//        try{
//            EncryptedData ed = xmlCipher.encryptData((Document)contextNode.getSOAPPart(),canonData,true);
//            Element encryptedBodyContent = xmlCipher.martial(ed);
//            SOAPBody body =
//                    ((com.sun.xml.messaging.saaj.soap.ExpressMessage)contextNode.getSOAPMessage()).getEMBody();
//            body.appendChild(encryptedBodyContent);
//            return encryptedBodyContent;
//        }catch(Exception e){
//            log.log(Level.SEVERE, "WSS1207.unableto.encrypt.message");
//            throw new XWSSecurityException("Unable to encrypt element", e);
//        }
    }
    
    private static void signEncrypt(FilterProcessingContext fpc ,Cipher cipher, ReferenceListHeaderBlock _ekReferenceList,
            ReferenceListHeaderBlock _standaloneReferenceList ,KeyInfoStrategy keyInfoStrategy,String encAlgo )throws XWSSecurityException{
        throw new UnsupportedOperationException("Not supported in WSIT");
//        try{
//            byte[] canonData = fpc.getCanonicalizedData();
//            byte[] cipherOutput = cipher.doFinal(canonData);
//            byte[] iv = cipher.getIV();
//            EncryptedDataImpl ed = new EncryptedDataImpl();
//
//            ed.setEncryptedData(cipherOutput);
//            ed.setIv(iv);
//
//            ed.setEncAlgo(encAlgo);
//            String xencEncryptedDataId = fpc.getSecurableSoapMessage().generateId();
//            String xencEncryptedDataRef = "#" + xencEncryptedDataId;
//            ed.setId(xencEncryptedDataId);
//            if (_ekReferenceList != null){
//                _ekReferenceList.addReference(xencEncryptedDataRef);
//            }else {
//                if (_standaloneReferenceList == null){
//                    _standaloneReferenceList = new ReferenceListHeaderBlock(fpc.getSecurableSoapMessage().getSOAPPart());
//                }
//                _standaloneReferenceList.addReference(xencEncryptedDataRef);
//
//                KeyInfoHeaderBlock keyInfoBlock = new KeyInfoHeaderBlock(fpc.getSecurableSoapMessage().getSOAPPart());
//                keyInfoStrategy.insertKey(keyInfoBlock, fpc.getSecurableSoapMessage(), null);
//                ed.setKeyInfo(keyInfoBlock);
//            }
//
//            SOAPMessage msg = fpc.getSOAPMessage();
//            com.sun.xml.jaxws.JAXWSMessage jxm = ((com.sun.xml.messaging.saaj.soap.ExpressMessage)msg).getJAXWSMessage();
//            ed.setXMLSerializer(jxm.getXmlSerializer());
//            jxm.setEncryptedBody(ed);
//        }catch(Exception e){
//            log.log(Level.SEVERE, "WSS1207.unableto.encrypt.message");
//            throw new XWSSecurityException("Unable to encrypt element", e);
//        }
    }
    
    //Start of Attachment code.
    private static void encryptAttachment( AttachmentPart part, boolean contentOnly, Cipher cipher) throws XWSSecurityException {
        try {
            byte[] cipherInput = null;
            
            if (contentOnly) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                part.getDataHandler().writeTo(baos);
                cipherInput = ((ByteArrayOutputStream)baos).toByteArray();
            } else {
                Object [] obj  = AttachmentSignatureInput._getSignatureInput(part);
                
                byte[] headers = serializeHeaders((java.util.Vector) obj[0]);
                byte[] content = (byte[]) obj[1];
                
                cipherInput = new byte[headers.length+content.length];
                
                System.arraycopy(headers, 0, cipherInput, 0, headers.length);
                System.arraycopy(content, 0, cipherInput, headers.length, content.length);
            }
            
            byte[] cipherOutput = cipher.doFinal(cipherInput);
            
            byte[] iv = cipher.getIV();
            byte[] encryptedBytes = new byte[iv.length + cipherOutput.length];
            
            System.arraycopy(iv, 0, encryptedBytes, 0, iv.length);
            System.arraycopy(cipherOutput, 0, encryptedBytes, iv.length, cipherOutput.length);
            
            int cLength  = encryptedBytes.length;
            String cType = MimeConstants.APPLICATION_OCTET_STREAM_TYPE;
            String uri   = part.getContentId();
            //Step 9 and 10.SWA spec.
            if (!contentOnly){
                part.removeAllMimeHeaders();
            }
            
            if (uri != null){
                part.setMimeHeader(MimeConstants.CONTENT_ID, uri);
            }else {
                uri = part.getContentLocation();
                if (uri != null){
                    part.setMimeHeader(MimeConstants.CONTENT_LOCATION, uri);
                }
            }
            part.setContentType(cType);
            part.setMimeHeader(MimeConstants.CONTENT_LENGTH,Integer.toString(cLength));
            part.setMimeHeader("Content-Transfer-Encoding", "base64");
            
            EncryptedAttachmentDataHandler dh = new EncryptedAttachmentDataHandler( new EncryptedAttachmentDataSource(encryptedBytes));
            part.setDataHandler(dh);
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1225.error.encrypting.Attachment", e);
            throw new XWSSecurityException(e);
        }
    }
    
    private static String getEKSHA1Ref(FilterProcessingContext context){
        String ekSha1Ref = null;
        ekSha1Ref = (String) context.getExtraneousProperty(MessageConstants.EK_SHA1_VALUE);
        return ekSha1Ref;
    }
    
    private static byte[] serializeHeaders(java.util.Vector mimeHeaders) throws XWSSecurityException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            for (int i=0; i < mimeHeaders.size(); i++) {
                MimeHeader mh = (MimeHeader) mimeHeaders.elementAt(i);
                
                String name = mh.getName();
                String vlue = mh.getValue();
                
                String line = name + ":" + vlue + "\r\n";
                
                byte[] b = line.getBytes("US-ASCII");
                baos.write(b, 0, b.length);
            }
            
            baos.write(crlf, 0, crlf.length);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1226.error.serialize.headers", e);
            throw new XWSSecurityException(e);
        }
        
        return baos.toByteArray();
    }
    
    private static class EncryptedAttachmentDataSource implements javax.activation.DataSource {
        byte[] datasource;
        
        EncryptedAttachmentDataSource(byte[] ds) {
            datasource = ds;
        }
        
        public String getContentType() {
            return MimeConstants.APPLICATION_OCTET_STREAM_TYPE;
        }
        
        public InputStream getInputStream() throws java.io.IOException {
            return new ByteArrayInputStream(datasource);
        }
        
        public String getName() {
            return "Encrypted Attachment DataSource";
        }
        
        public OutputStream getOutputStream() throws java.io.IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(datasource, 0, datasource.length);
            return baos;
        }
    }
    
    private static class EncryptedAttachmentDataHandler extends javax.activation.DataHandler {
        
        EncryptedAttachmentDataHandler(javax.activation.DataSource ds) {
            super(ds);
        }
        
        @Override
        public void writeTo(OutputStream os) throws java.io.IOException {
            ((ByteArrayOutputStream) getDataSource().getOutputStream()).writeTo(os);
        }
    }
    
    //End of Attachment code.
}
