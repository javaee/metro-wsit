
/*
 * $Id: SignatureProcessor.java,v 1.14 2010-04-20 17:32:45 m_potociar Exp $
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

package com.sun.xml.wss.impl.dsig;


import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.ws.api.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.api.security.DerivedKeyToken;
import com.sun.xml.wss.core.DerivedKeyTokenHeaderBlock;
import com.sun.xml.wss.core.reference.X509ThumbPrintIdentifier;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.verifier.SignaturePolicyVerifier;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.SamlAssertionHeaderBlock;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.wss.core.reference.DirectReference;
import com.sun.xml.wss.core.reference.X509IssuerSerial;
import com.sun.xml.wss.core.reference.EncryptedKeySHA1Identifier;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.keyinfo.KeyIdentifierStrategy;
import com.sun.xml.wss.impl.keyinfo.KeyInfoStrategy;
import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.ws.api.security.SecurityContextToken;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.AlgorithmSuite;

import java.io.IOException;
import java.io.InputStream;

import java.security.Key;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.crypto.dsig.spec.ExcC14NParameterSpec;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.crypto.spec.SecretKeySpec;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.xml.ws.security.trust.GenericToken;

import com.sun.xml.wss.impl.XMLUtil;
import java.security.NoSuchAlgorithmException;
import javax.xml.soap.SOAPException;

/*
 *
 * This class provides support for WSS 1.0 signature generation and verification.
 * This class depends on JSR105 Digital signature implementation.
 * @author K.Venugopal@sun.com
 *
 */
public class SignatureProcessor{
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    /**
     *
     * @param context FilterProcessingContext
     * @throws XWSSecurityException
     * @return errorCode
     */
    @SuppressWarnings("unchecked")
    public static int sign(FilterProcessingContext context) throws XWSSecurityException {
        
        try{
            SignaturePolicy signaturePolicy  = (SignaturePolicy)context.getSecurityPolicy();
            SOAPMessage soapMessage = context.getSOAPMessage();
            //Dependant on secure soap meesage.
            //discuss and refactor.
            SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
            WSSPolicy keyBinding = (WSSPolicy)signaturePolicy.getKeyBinding();
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST, "KeyBinding is "+keyBinding);
            }
            
            Key signingKey = null;
            Node nextSibling =  null;
            //TODO :: Creation of WSSPolicyConsumerImpl every time.
            WSSPolicyConsumerImpl dsigHelper = WSSPolicyConsumerImpl.getInstance();
            KeyInfo keyInfo = null;
            SecurityHeader securityHeader = secureMessage.findOrCreateSecurityHeader();
            
            SignaturePolicy.FeatureBinding featureBinding =
                    (SignaturePolicy.FeatureBinding)signaturePolicy.getFeatureBinding();
            AlgorithmSuite algSuite = context.getAlgorithmSuite();
            
            boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
            boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
            boolean wss10 = !wss11Sender;
            boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getEKSHA1Ref(context) != null);
            
            if (PolicyTypeUtil.usernameTokenPolicy(keyBinding)) {
                logger.log(Level.SEVERE, "WSS1326.unsupported.usernametoken.keybinding");
                throw new XWSSecurityException("UsernameToken as KeyBinding for SignaturePolicy is Not Yet Supported");
            } else if ( PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)) {
                DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
                
                WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
                
                String algorithm = null;
                if(algSuite != null){
                    algorithm = algSuite.getEncryptionAlgorithm();
                }
                String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(algorithm);
                //The offset and length to be used for DKT
                //TODO: PLUGFEST the length here should be set correctly
                long offset = 0; // Default 0
                long length = SecurityUtil.getLengthFromAlgorithm(algorithm);
                if(length == 32) length = 24;
                
                if (PolicyTypeUtil.x509CertificateBinding(originalKeyBinding)) {
                    // this is becuase SecurityPolicy Converter never produces this combination
                    logger.log(Level.SEVERE, "WSS1327.unsupported.asymmetricbinding.derivedkey.x509token");
                    throw new XWSSecurityException("Asymmetric Binding with DerivedKeys under X509Token Policy Not Yet Supported");
                } else if ( PolicyTypeUtil.symmetricKeyBinding(originalKeyBinding)) {
                    
                    SymmetricKeyBinding skb = null;
                    if ( context.getSymmetricKeyBinding() != null) {
                        skb = context.getSymmetricKeyBinding();
                        context.setSymmetricKeyBinding(null);
                    }
                    //Construct a derivedKeyToken to be used
                    Key originalKey = null;
                    if(context.getCurrentSecret() != null){
                        originalKey = context.getCurrentSecret();
                    }else{
                        originalKey = skb.getSecretKey();
                        context.setCurrentSecret(originalKey);
                    }
                    byte[] secret = originalKey.getEncoded();
                    DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret);
                    //get the signing key for signature from derivedkeyToken
                    signingKey = dkt.generateSymmetricKey(jceAlgo);
                    
                    Node[] nxtSiblingContainer = new Node[1];
                    keyInfo = prepareForSymmetricKeySignature(context, keyBinding, originalKey, signaturePolicy, nxtSiblingContainer, null, dkt);
                    nextSibling = nxtSiblingContainer[0];
                    
                } else if ( PolicyTypeUtil.issuedTokenKeyBinding(originalKeyBinding)) {
                    byte[] prfKey = context.getTrustContext().getProofKey();
                    if (prfKey == null) {
                        //handle Asymmetric Issued Token
                        X509Certificate cert =
                                context.getTrustContext().getRequestorCertificate();
                        if (cert == null){
                            logger.log(Level.SEVERE, "WSS1328.illegal.Certificate.key.null");
                            throw new XWSSecurityException(
                                    "Requestor Certificate and Proof Key are both null for Issued Token");
                        }
                        signingKey = context.getSecurityEnvironment().
                                getPrivateKey(context.getExtraneousProperties(), cert);
                        
                        //Get the IssuedToken and insert it into the message
                        GenericToken issuedToken =
                                (GenericToken)context.getTrustContext().getSecurityToken();
                        Element elem = (Element)issuedToken.getTokenValue();
                        SOAPElement tokenElem =
                                XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), elem);
                        //FIX for Issue 26: We need an Id to cache and MS is not setting in
                        //some cases
                        String tokId = tokenElem.getAttribute("Id");
                        if ("".equals(tokId) &&
                                MessageConstants.ENCRYPTED_DATA_LNAME.equals(
                                tokenElem.getLocalName())) {
                            tokenElem.setAttribute("Id", secureMessage.generateId());
                        }
                        context.getTokenCache().put(keyBinding.getUUID(), tokenElem);
                        IssuedTokenKeyBinding ikb = (IssuedTokenKeyBinding)originalKeyBinding;
                        String iTokenType = ikb.getIncludeToken();
                        boolean includeToken =  (ikb.INCLUDE_ALWAYS_TO_RECIPIENT.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS_VER2.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(iTokenType)
                                                );
                        Element strElem = null;
                        
                        if (includeToken) {
                            strElem =(Element)context.getTrustContext().
                                    getAttachedSecurityTokenReference().getTokenValue();
                        }else  {
                            strElem = (Element)context.getTrustContext().
                                    getUnAttachedSecurityTokenReference().getTokenValue();
                        }
                        //TODO: remove these expensive conversions
                        Element imported = (Element)
                        secureMessage.getSOAPPart().importNode(strElem,true);
                        SecurityTokenReference str = new SecurityTokenReference(
                                XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(),
                                (Element)imported.cloneNode(true)), false);
                        
                        if (tokenElem != null) {
                            if(includeToken) {
                                secureMessage.findOrCreateSecurityHeader().
                                        insertHeaderBlockElement(tokenElem);
                                nextSibling = tokenElem.getNextSibling();
                                
                            } else {
                                nextSibling =  null;
                            }
                            context.setIssuedSAMLToken(tokenElem);
                        }
                        keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,str);
                        SecurityUtil.updateSamlVsKeyCache(str, context, cert.getPublicKey());
                    } else {
                        DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, prfKey);
                        signingKey = dkt.generateSymmetricKey(jceAlgo);
                        Node[] nxtSiblingContainer = new Node[1];
                        //NOTE: passing the proofKey here as original key
                        String secretKeyAlg = "AES";
                        if (algSuite != null) {
                            secretKeyAlg = SecurityUtil.getSecretKeyAlgorithm(algSuite.getEncryptionAlgorithm());
                        }
                        Key originalKey = new SecretKeySpec(prfKey, secretKeyAlg);
                        keyInfo = prepareForSymmetricKeySignature(
                                context, keyBinding, originalKey, signaturePolicy, nxtSiblingContainer, null, dkt);
                        nextSibling = nxtSiblingContainer[0];
                    }
                } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(originalKeyBinding)) {
                    
                    DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, context.getSecureConversationContext().getProofKey());
                    //get the signing key for signature from derivedkeyToken
                    signingKey = dkt.generateSymmetricKey(jceAlgo);
                    Node[] nxtSiblingContainer = new Node[1];
                    keyInfo = prepareForSymmetricKeySignature(context, keyBinding, null, signaturePolicy, nxtSiblingContainer, null, dkt);
                    nextSibling = nxtSiblingContainer[0];
                }
                
            } else if ( PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
                Node[] nxtSiblingContainer = new Node[1];
                // look for the proof token inside the IssuedToken
                byte[] proofKey = context.getTrustContext().getProofKey();
                if (proofKey == null) {
                    //handle Asymmetric Issued Token
                    X509Certificate cert =
                            context.getTrustContext().getRequestorCertificate();
                    if (cert == null){
                        logger.log(Level.SEVERE, "WSS1328.illegal.Certificate.key.null");
                        throw new XWSSecurityException(
                                "Requestor Certificate and Proof Key are both null for Issued Token");
                    }
                    signingKey = context.getSecurityEnvironment().
                            getPrivateKey(context.getExtraneousProperties(), cert);
                    
                    //Get the IssuedToken and insert it into the message
                    GenericToken issuedToken =
                            (GenericToken)context.getTrustContext().getSecurityToken();
                    Element elem = (Element)issuedToken.getTokenValue();
                    SOAPElement tokenElem =
                            XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), elem);
                    //FIX for Issue 26: We need an Id to cache and MS is not setting in
                    //some cases
                    String tokId = tokenElem.getAttribute("Id");
                    if ("".equals(tokId) &&
                            MessageConstants.ENCRYPTED_DATA_LNAME.equals(
                            tokenElem.getLocalName())) {
                        tokenElem.setAttribute("Id", secureMessage.generateId());
                    }
                    context.getTokenCache().put(keyBinding.getUUID(), tokenElem);
                    IssuedTokenKeyBinding ikb = (IssuedTokenKeyBinding)keyBinding;
                    String iTokenType = ikb.getIncludeToken();
                    boolean includeToken =  (ikb.INCLUDE_ALWAYS_TO_RECIPIENT.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS_VER2.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(iTokenType)
                                                );
                    Element strElem = null;
                    
                    if (includeToken) {
                        strElem =(Element)context.getTrustContext().
                                getAttachedSecurityTokenReference().getTokenValue();
                    }else  {
                        strElem = (Element)context.getTrustContext().
                                getUnAttachedSecurityTokenReference().getTokenValue();
                    }
                    //TODO: remove these expensive conversions
                    Element imported = (Element)
                    secureMessage.getSOAPPart().importNode(strElem,true);
                    SecurityTokenReference str = new SecurityTokenReference(
                            XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(),
                            (Element)imported.cloneNode(true)), false);
                    
                    if (tokenElem != null) {
                        if(includeToken) {
                            secureMessage.findOrCreateSecurityHeader().
                                    insertHeaderBlockElement(tokenElem);
                            nextSibling = tokenElem.getNextSibling();
                            
                        } else {
                            nextSibling =  null;
                        }
                        context.setIssuedSAMLToken(tokenElem);
                    }
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,str);
                    SecurityUtil.updateSamlVsKeyCache(str, context, cert.getPublicKey());
                } else {
                    // symmetric issued
                    String secretKeyAlg = "AES"; // hardcoding to AES for now
                    if (algSuite != null) {
                        secretKeyAlg = SecurityUtil.getSecretKeyAlgorithm(algSuite.getEncryptionAlgorithm());
                    }
                    //TODO: assuming proofkey is a byte array in case of Trust as well
                    signingKey = new SecretKeySpec(proofKey, secretKeyAlg);
                    keyInfo = prepareForSymmetricKeySignature(
                            context, keyBinding, signingKey, signaturePolicy, nxtSiblingContainer, null, null);
                    nextSibling = nxtSiblingContainer[0];
                }
                
            } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)) {
                
                //Hack to get the nextSibling node from prepareForSymmetricKeySignature
                Node[] nxtSiblingContainer = new Node[1];
                keyInfo = prepareForSymmetricKeySignature(
                        context, keyBinding, null, signaturePolicy, nxtSiblingContainer, null, null);
                
                // look for the proof token inside the secureConversationToken
                String secretKeyAlg = "AES"; // hardcoding to AES for now
                if (algSuite != null) {
                    secretKeyAlg = SecurityUtil.getSecretKeyAlgorithm(algSuite.getEncryptionAlgorithm());
                }
                signingKey = new SecretKeySpec(context.getSecureConversationContext().getProofKey(), secretKeyAlg);
                nextSibling = nxtSiblingContainer[0];
                
            } else if(PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
                
                AuthenticationTokenPolicy.X509CertificateBinding certInfo = null;
                if ( context.getX509CertificateBinding() != null ) {
                    certInfo = context.getX509CertificateBinding();
                    context.setX509CertificateBinding(null);
                } else {
                    certInfo = (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
                }
                
                PrivateKeyBinding privKBinding  = (PrivateKeyBinding)certInfo.getKeyBinding();
                signingKey = privKBinding.getPrivateKey();
                
                Node[] nxtSiblingContainer = new Node[1];
                keyInfo = handleX509Binding(context, signaturePolicy, certInfo, nxtSiblingContainer);
                nextSibling = nxtSiblingContainer[0];
                
            } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
                // populate the policy, the handler should also add a privateKey binding for HOK
                
                AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                        (AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding;
                PrivateKeyBinding privKBinding  = (PrivateKeyBinding)samlBinding.getKeyBinding();
                if (privKBinding == null) {
                    logger.log(Level.SEVERE, "WSS1329.null.privatekeybinding.SAMLPolicy");
                    throw new XWSSecurityException("PrivateKey binding not set for SAML Policy by CallbackHandler");
                }
                
                signingKey = privKBinding.getPrivateKey();
                
                if (signingKey == null) {
                    logger.log(Level.SEVERE, "WSS1330.null.privatekey.SAMLPolicy");
                    throw new XWSSecurityException("PrivateKey null inside PrivateKeyBinding set for SAML Policy ");
                }
                
                String referenceType = samlBinding.getReferenceType();
                if (referenceType.equals(MessageConstants.EMBEDDED_REFERENCE_TYPE)) {
                    logger.log(Level.SEVERE, "WSS1331.unsupported.EmbeddedReference.SAML");
                    throw new XWSSecurityException("Embedded Reference Type for SAML Assertions not supported yet");
                }
                
                String assertionId = samlBinding.getAssertionId();
                Element _assertion = samlBinding.getAssertion();
                Element _authorityBinding = samlBinding.getAuthorityBinding();
                
                if (assertionId == null) {
                    if (_assertion == null) {
                        logger.log(Level.SEVERE, "WSS1332.null.SAMLAssertion.SAMLAssertionId");
                        throw new XWSSecurityException(
                                "None of SAML Assertion, SAML Assertion Id information was set into " +
                                " the Policy by the CallbackHandler");
                    }
                    if(_assertion.getAttributeNode("ID") != null){
                        assertionId = _assertion.getAttribute("ID");
                    }else{
                        assertionId = _assertion.getAttribute("AssertionID");
                    }
                }
                
                SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                String strId = samlBinding.getSTRID();
                if(strId == null){
                    strId = secureMessage.generateId();
                }
                tokenRef.setWsuId(strId);
                // set wsse11:TokenType to SAML1.1 or SAML2.0
                if(_assertion.getAttributeNode("ID") != null){
                    tokenRef.setTokenType(MessageConstants.WSSE_SAML_v2_0_TOKEN_TYPE);
                }else{
                    tokenRef.setTokenType(MessageConstants.WSSE_SAML_v1_1_TOKEN_TYPE);
                }
                
                if (_authorityBinding != null) {
                    tokenRef.setSamlAuthorityBinding(_authorityBinding,
                            secureMessage.getSOAPPart());
                }
                
                if ((_assertion != null) && (_authorityBinding == null)) {
                    //insert the SAML Assertion
                    SamlAssertionHeaderBlock samlHeaderblock =
                            new SamlAssertionHeaderBlock(_assertion, secureMessage.getSOAPPart());
                    secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(samlHeaderblock);
                    // setting ValueType of Keydentifier to SAML1.1 0r SAML2.0
                    KeyIdentifierStrategy strat = new KeyIdentifierStrategy(assertionId);
                    strat.insertKey(tokenRef, secureMessage);
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy, tokenRef);
                    
                    nextSibling = samlHeaderblock.getAsSoapElement().getNextSibling();
                } else {
                    nextSibling = securityHeader.getNextSiblingOfTimestamp();
                }
            }else if(PolicyTypeUtil.symmetricKeyBinding(keyBinding)){
                SymmetricKeyBinding skb = null;
                if ( context.getSymmetricKeyBinding() != null) {
                    skb = context.getSymmetricKeyBinding();
                    context.setSymmetricKeyBinding(null);
                } else {
                    skb = (SymmetricKeyBinding)keyBinding;
                }
                
                // sign method is HMACSHA-1 for symmetric keys
                if(!skb.getKeyIdentifier().equals(MessageConstants._EMPTY)){
                    signingKey = skb.getSecretKey();
                    String symmetricKeyName = skb.getKeyIdentifier();
                    
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy, symmetricKeyName);
                    nextSibling = securityHeader.getNextSiblingOfTimestamp();
                } else if(sendEKSHA1){
                    //get the signing key and EKSHA1 reference from the Subject, it was stored from the incoming message
                    String ekSha1Ref = getEKSHA1Ref(context);
                    signingKey = skb.getSecretKey();
                    
                    SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    
                    EncryptedKeySHA1Identifier refElem = new EncryptedKeySHA1Identifier(secureMessage.getSOAPPart());
                    refElem.setReferenceValue(ekSha1Ref);
                    secTokenRef.setReference(refElem);
                    
                    //set the wsse11:TokenType attribute as required by WSS 1.1
                    //secTokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                    
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy, secTokenRef);
                    nextSibling = securityHeader.getNextSiblingOfTimestamp();
                    
                    //TODO: the below condition is always true
                }else if(wss11Sender || wss10){
                    signingKey = skb.getSecretKey();
                    
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = null;
                    X509Certificate cert = null;
                    if(!skb.getCertAlias().equals(MessageConstants._EMPTY)){
                        x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.newPrivateKeyBinding();
                        x509Binding.setCertificateIdentifier(skb.getCertAlias());
                        cert = context.getSecurityEnvironment().getCertificate(context.getExtraneousProperties(), x509Binding.getCertificateIdentifier(), false);
                        x509Binding.setX509Certificate(cert);
                        
                        x509Binding.setReferenceType("Direct");
                    }else if ( context.getX509CertificateBinding() != null ) {
                        x509Binding = context.getX509CertificateBinding();
                        context.setX509CertificateBinding(null);
                        cert = x509Binding.getX509Certificate();
                    }
                    
                    HashMap tokenCache = context.getTokenCache();
                    HashMap insertedX509Cache = context.getInsertedX509Cache();
                    String x509id = x509Binding.getUUID();
                    if(x509id == null || x509id.equals("")){
                        x509id = secureMessage.generateId();
                    }
                    
                    SecurityUtil.checkIncludeTokenPolicy(context, x509Binding, x509id);
                    
                    String keyEncAlgo = XMLCipher.RSA_v1dot5;  //<--Harcoding of Algo
                    String tmp = null;
                    if(algSuite != null){
                        tmp = algSuite.getAsymmetricKeyAlgorithm();
                    }
                    if(tmp != null && !"".equals(tmp)){
                        keyEncAlgo = tmp;
                    }
                    String referenceType =  x509Binding.getReferenceType();
                    if(referenceType.equals("Identifier") && x509Binding.getValueType().equals(MessageConstants.X509v1_NS)){
                        logger.log(Level.SEVERE, "WSS1333.unsupported.keyidentifer.X509v1");
                        throw new XWSSecurityException("Key Identifier strategy in X509v1 is not supported");
                    }
                    KeyInfoStrategy  strategy = KeyInfoStrategy.getInstance(referenceType);
                    KeyInfoHeaderBlock keyInfoBlock  = null;
                    secureMessage = context.getSecurableSoapMessage();
                    dsigHelper = WSSPolicyConsumerImpl.getInstance();
                    
                    //Check to see if same x509 token used for Signature and Encryption
                    X509SecurityToken token = null;
                    cert = x509Binding.getX509Certificate();
                    String x509TokenId = x509Binding.getUUID();
                    //String x509TokenId = x509Binding.getPolicyToken().getTokenId();
                    boolean tokenInserted = false;
                    
                    //insert x509 token in tokencache always irrespective of reference type
                    if(x509TokenId == null || x509TokenId.equals("")){
                        x509TokenId = secureMessage.generateId();
                    }
                    
                    token = (X509SecurityToken)tokenCache.get(x509TokenId);
                    
                    //reference type adjustment in checkIncludePolicy might
                    // have inserted x509
                    X509SecurityToken insertedx509 =
                            (X509SecurityToken)context.getInsertedX509Cache().get(x509TokenId);
                    
                    if (token == null) {
                        String valueType = x509Binding.getValueType();
                        if(valueType==null||valueType.equals("")){
                            //default valueType for X509 as v3
                            valueType = MessageConstants.X509v3_NS;
                        }
                        token = new X509SecurityToken(secureMessage.getSOAPPart(), cert, x509TokenId, valueType);
                        tokenCache.put(x509TokenId, token);
                    } else{
                        tokenInserted = true;
                    }
                    String id = null;
                    HashMap ekCache = context.getEncryptedKeyCache();
                    if(!tokenInserted){
                        context.setCurrentSecret(signingKey);
                        //Store SymmetricKey generated in ProcessingContext
                        context.setExtraneousProperty("SecretKey", signingKey);
                        keyInfoBlock = new KeyInfoHeaderBlock(secureMessage.getSOAPPart());
                        strategy.setCertificate(cert);
                        strategy.insertKey(keyInfoBlock, secureMessage, x509TokenId);
                        com.sun.org.apache.xml.internal.security.keys.KeyInfo apacheKeyInfo = keyInfoBlock.getKeyInfo();
                        //create an encrypted Key
                        EncryptedKey encryptedKey = null;
                        XMLCipher keyEncryptor = null;
                        try{
                            keyEncryptor = XMLCipher.getInstance(keyEncAlgo);
                            keyEncryptor.init(XMLCipher.WRAP_MODE, cert.getPublicKey());
                            if (keyEncryptor != null) {
                                encryptedKey = keyEncryptor.encryptKey(secureMessage.getSOAPPart(), signingKey);
                            }
                        }catch(Exception e){
                            logger.log(Level.SEVERE, "WSS1334.error.creating.encryptedkey");
                            throw new XWSSecurityException(e);
                        }
                        id = secureMessage.generateId();
                        encryptedKey.setId(id);
                        ekCache.put(x509TokenId, id);
                        // set its KeyInfo
                        encryptedKey.setKeyInfo(apacheKeyInfo);
                        
                        // insert the EK into the SOAPMessage
                        SOAPElement se = (SOAPElement)keyEncryptor.martial(encryptedKey);
                        if (insertedx509 == null) {
                            secureMessage.findOrCreateSecurityHeader().insertHeaderBlockElement(se);
                        } else {
                            secureMessage.findOrCreateSecurityHeader().insertBefore(se,insertedx509.getNextSibling());
                        }
                        
                        //store EKSHA1 of KeyValue contents in context
                        Element cipherData = (Element)se.getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.setExtraneousProperty("EncryptedKeySHA1", encEkSha1);
                        nextSibling = se.getNextSibling();
                    } else{
                        
                        id = (String)ekCache.get(x509TokenId);
                        signingKey = context.getCurrentSecret();
                        nextSibling = secureMessage.getElementById(id).getNextSibling();
                    }
                    //insert the token as the first child in SecurityHeader -- if same token was not already
                    // inserted by Encryption
                    if (MessageConstants.DIRECT_REFERENCE_TYPE.equals(referenceType) && insertedx509 == null){
                        secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(token);
                        insertedX509Cache.put(x509TokenId, token);
                    }
                    
                    //STR for the KeyInfo of signature
                    SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    DirectReference reference = new DirectReference();
                    String strId = x509Binding.getSTRID();
                    if(strId == null){
                        strId = secureMessage.generateId();
                    }
                    secTokenRef.setWsuId(strId);
                    //TODO: PLUGFEST Microsoft setting EK on reference inseatd of STR
                    //secTokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                    //set id of encrypted key
                    reference.setURI("#"+id);
                    reference.setValueType(MessageConstants.EncryptedKey_NS);
                    secTokenRef.setReference(reference);
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,secTokenRef);
                    
                }
            }else {
                logger.log(Level.SEVERE,"WSS1335.unsupported.keybinding.signaturepolicy");
                throw new XWSSecurityException("Unsupported Key Binding for SignaturePolicy");
            }
            
            // Put UsernameToken above signature
            NodeList nodeList = securityHeader.getElementsByTagNameNS(MessageConstants.WSSE_NS, MessageConstants.USERNAME_TOKEN_LNAME);
            if(nodeList != null && nodeList.getLength() > 0){
                nextSibling = nodeList.item(0).getNextSibling();
            }
            
            // if currentReflist is non-null it means we are doing E before S
            Node refList = context.getCurrentRefList();
            if (refList != null) {
                nextSibling = refList;
                //reset it after using once to null.
                context.setCurrentReferenceList(null);
            }
            
            if(featureBinding.isEndorsingSignature()){
                nextSibling = securityHeader.getLastChild().getNextSibling();
            }
            
            SignedInfo signedInfo = WSSPolicyConsumerImpl.getInstance().constructSignedInfo(context);
            DOMSignContext signContext = null;
            if(nextSibling == null){
                signContext = new DOMSignContext(signingKey,securityHeader.getAsSoapElement());//firstChildElement);
            }else{
                signContext = new DOMSignContext(signingKey,securityHeader.getAsSoapElement(),nextSibling);
            }
            signContext.setURIDereferencer(DSigResolver.getInstance());
            XMLSignature signature = dsigHelper.constructSignature(signedInfo, keyInfo, signaturePolicy.getUUID());
            signContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, context);
            signContext.putNamespacePrefix(MessageConstants.DSIG_NS, MessageConstants.DSIG_PREFIX);
//            XMLUtils.circumventBug2650(context.getSecurableSoapMessage().getSOAPPart());
            signature.sign(signContext);
            
            //For SignatureConfirmation
            List scList = (ArrayList)context.getExtraneousProperty("SignatureConfirmation");
            if(scList != null){
                scList.add(Base64.encode(signature.getSignatureValue().getValue()));
            }
            //End SignatureConfirmation specific code
            
        }catch(XWSSecurityException xe){          
                logger.log(Level.SEVERE,"WSS1316.sign.failed",xe);           
            throw xe;
        }catch(Exception ex){
                logger.log(Level.SEVERE,"WSS1316.sign.failed",ex);       
            throw new XWSSecurityException(ex);
        }
        return 0;
    }
    
    /**
     *
     * @param context FilterProcessingContext
     * @throws XWSSecurityException
     * @return errorCode.
     */
    @SuppressWarnings("unchecked")
    public static int verify(FilterProcessingContext context) throws XWSSecurityException {
        try{
            WSSPolicyConsumerImpl dsigUtil = WSSPolicyConsumerImpl.getInstance();;
            SOAPElement signElement = context.getSecurableSoapMessage().findSecurityHeader().getCurrentHeaderElement();
            if(signElement == null || signElement.getLocalName()== null || !"Signature".equals(signElement.getLocalName()) ){
                //throw new XWSSecurityException("No Signature Element found");
                String localName = signElement != null ? signElement.getLocalName() : "";
                context.setPVE(new PolicyViolationException(
                        "Expected Signature Element as per receiver requirements, found  "+
                        localName));
                context.isPrimaryPolicyViolation(true);
                return 0;
            }
            DOMValidateContext validationContext = new DOMValidateContext(KeySelectorImpl.getInstance(), signElement);
            XMLSignatureFactory signatureFactory = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();
            // unmarshal the XMLSignature
            XMLSignature signature = signatureFactory.unmarshalXMLSignature(validationContext);
            
            //For SignatureConfirmation
            List scList = (ArrayList)context.getExtraneousProperty("receivedSignValues");
            if(scList != null){
                scList.add(Base64.encode(signature.getSignatureValue().getValue()));
            }
            //End SignatureConfirmation specific code
            
            validationContext.setURIDereferencer(DSigResolver.getInstance());
            // Validate the XMLSignature (generated above)
            validationContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, context);
            SignaturePolicy currentMessagePolicy = null;
            if(context.getMode() == FilterProcessingContext.ADHOC ||
                    context.getMode() == FilterProcessingContext.POSTHOC){
                currentMessagePolicy = new SignaturePolicy();
                context.setInferredPolicy(currentMessagePolicy);
            } else if (context.getMode() == FilterProcessingContext.WSDL_POLICY) {
                currentMessagePolicy = new SignaturePolicy();
                context.getInferredSecurityPolicy().append(currentMessagePolicy);
            }
            
//            XMLUtils.circumventBug2650(context.getSecurableSoapMessage().getSOAPPart());
            boolean coreValidity = signature.validate(validationContext);
            SecurityPolicy securityPolicy = context.getSecurityPolicy();
            
            boolean isBSP = false;
            if(securityPolicy != null) {
                if (PolicyTypeUtil.messagePolicy(securityPolicy)) {
                    isBSP = ((MessagePolicy)securityPolicy).isBSP();
                } else {
                    isBSP = ((WSSPolicy)securityPolicy).isBSP();
                }
            }
            
            
            // Check core validation status
            if (coreValidity == false) {
                
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST,"Signature failed core validation");
                    boolean sv = signature.getSignatureValue().validate(validationContext);
                    logger.log(Level.FINEST,"Signature validation status: " + sv);
                    // check the validation status of each Reference
                    Iterator i = signature.getSignedInfo().getReferences().iterator();
                    for (int j=0; i.hasNext(); j++) {
                        Reference ref = (Reference) i.next();
                        logger.log(Level.FINEST,"Reference ID "+ref.getId());
                        logger.log(Level.FINEST,"Reference URI "+ref.getURI());
                        boolean refValid =
                                ref.validate(validationContext);
                        logger.log(Level.FINEST,"Reference["+j+"] validity status: " + refValid);
                    }
                }
                    logger.log(Level.SEVERE, "WSS1315.signature.verification.failed");
                XWSSecurityException xwsse =   new XWSSecurityException("Signature verification failed");
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_FAILED_CHECK,"Signature verification failed ",xwsse);
            } else {
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINE,"Signature Passed Core Validation");
                }
                SignedInfo signInfo = signature.getSignedInfo();
                if (isBSP) {
                    Iterator i = signInfo.getReferences().iterator();
                    for (int j=0; i.hasNext(); j++) {
                        Reference reference = (Reference) i.next();
                        
                        Iterator t = reference.getTransforms().iterator();
                        for (int index=0; t.hasNext(); index++) {
                            Transform transform = (Transform) t.next();
                            if (Transform.ENVELOPED.equals(transform.getAlgorithm())) {
                                logger.log(Level.SEVERE, "WSS1336.illegal.envelopedsignature");
                                throw new XWSSecurityException("Enveloped signatures not permitted by BSP");
                            }
                            if (MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS.equals(transform.getAlgorithm())) {
                                //check the inclusiveprefix list is not empty
                                if (transform.getParameterSpec()!=null) {
                                    ExcC14NParameterSpec spec = (ExcC14NParameterSpec)transform.getParameterSpec();
                                    if (spec.getPrefixList().isEmpty())
                                        logger.log(Level.SEVERE, "WSS1337.invalid.Emptyprefixlist");
                                        throw new XWSSecurityException("Prefix List cannot be empty: violation of BSP 5407");
                                }
                            }
                        }
                    }
                }
                if(context.getMode() == FilterProcessingContext.POSTHOC){
                    //TODO: handle SAML KeyBinding here
                    MessagePolicy policy = (MessagePolicy) context.getSecurityPolicy();
                    dsigUtil.constructSignaturePolicy(signInfo, policy.isBSP(),currentMessagePolicy);
                    policy.append(currentMessagePolicy);
                }
                
                if(context.getMode() == FilterProcessingContext.ADHOC){
                    //throws Exception for now , need to throw only
                    //appropriate errors.
                    //Next step do it more efficiently.
                    verifyRequirements(context,signature,validationContext);
                    SignaturePolicy policy =(SignaturePolicy) context.getSecurityPolicy();
                    dsigUtil.constructSignaturePolicy(signInfo, policy.isBSP(),currentMessagePolicy);
                    SignaturePolicyVerifier spv = new SignaturePolicyVerifier(context);
                    spv.verifyPolicy(policy,currentMessagePolicy);
                    
                    if(logger.isLoggable(Level.FINEST)){
                        logger.log(Level.FINE,"Reciever Requirements  are met");
                    }
                }
                
                if(context.getMode() == FilterProcessingContext.WSDL_POLICY){
                    dsigUtil.constructSignaturePolicy(signInfo, currentMessagePolicy, context.getSecurableSoapMessage());
                }
            }
        }catch(XWSSecurityException xwe){
            logger.log(Level.SEVERE, "WSS1338.error.verify");
            throw xwe;
        }catch(XMLSignatureException xse){
            
            Throwable t1 = xse.getCause();
            
            if(t1== null){
                logger.log(Level.SEVERE, "WSS1338.error.verify");
                throw new XWSSecurityException(xse);
            }
            if(t1 instanceof KeySelectorException || t1 instanceof URIReferenceException ){
                
                Throwable t2 = t1.getCause();
                
                if(t2 != null && t2 instanceof WssSoapFaultException){
                    logger.log(Level.SEVERE, "WSS1338.error.verify");
                    throw (WssSoapFaultException)t2;
                }else{
                    logger.log(Level.SEVERE, "WSS1338.error.verify");
                    throw new XWSSecurityException((Exception)t1);
                }
            }
            logger.log(Level.SEVERE, "WSS1338.error.verify");
            throw new XWSSecurityException(xse);
            
        }catch(Exception ex){
            logger.log(Level.SEVERE, "WSS1338.error.verify");
            throw new XWSSecurityException(ex);
        }finally{
            context.setInferredPolicy(null);
        }
        return 0;
    }
    
    /**
     *
     * @param context
     * @param signature
     * @param validationContext
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static void verifyRequirements(FilterProcessingContext context ,
            XMLSignature signature,DOMValidateContext validationContext )throws Exception{
        
        SignaturePolicy policy =(SignaturePolicy) context.getSecurityPolicy();
        SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)policy.getFeatureBinding();
        WSSPolicyConsumerImpl dsigUtil = WSSPolicyConsumerImpl.getInstance();
        ArrayList targets = featureBinding.getTargetBindings();
        if(targets == null || targets.size() == 0){
            return;
        }
        SignedInfo signedInfo = signature.getSignedInfo();
        List signedReferences = signedInfo.getReferences();
        Iterator sr = signedReferences.listIterator();
        ArrayList signedDataList = new ArrayList();
        ArrayList signedReferenceList = new ArrayList();
        while(sr.hasNext()){
            Reference reference = (Reference)sr.next();
            Data tmpObj = getData(reference,validationContext);
            signedDataList.add(new DataWrapper(tmpObj));
            //TODO:Should use cached data from References of already validated
            //messages when sean provides on . For now get the Data again.
            signedReferenceList.add(reference);
        }
        
        ArrayList optionalReqList = new ArrayList();
        ArrayList requiredDataList = new ArrayList();
        ArrayList requiredReferenceList = new ArrayList();
        ArrayList optionalDataList = new ArrayList();
        ArrayList optionalReferenceList = new ArrayList();
        //It would have been better If I had optional list
        //seperated
        
        Iterator targetItr = targets.iterator();
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        while(targetItr.hasNext()){
            SignatureTarget signatureTarget = (SignatureTarget) targetItr.next();
            boolean requiredTarget = signatureTarget.getEnforce();
            List referenceList = null;
            try{
                if(requiredTarget){
                    referenceList = dsigUtil.generateReferenceList(Collections.singletonList(signatureTarget),secureMessage,context,true, featureBinding.isEndorsingSignature());
                }else{
                    //dont resolve it now.
                    optionalReqList.add(signatureTarget);
                }
            }catch(Exception ex){
                logger.log(Level.SEVERE,"WSS1302.reflist_error",ex);
                if(requiredTarget){
                    logger.log(Level.SEVERE, "WSS1339.invalid.ReceiverRequirements");
                    throw new XWSSecurityException("Receiver requirement for SignatureTarget "+
                            signatureTarget.getValue()+" is not met");
                }
                //log
            }
            if(!requiredTarget){
                continue;
            }
            if( referenceList.size() <= 0){
                logger.log(Level.SEVERE, "WSS1339.invalid.ReceiverRequirements");
                throw new XWSSecurityException("Receiver requirement for SignatureTarget "+
                        signatureTarget.getValue()+" is not met");
            }
            boolean allRef = false;
            //Verify all attachments are signed.              || all header elements are signed
           /* if(signatureTarget.getValue().startsWith("cid:*") || signatureTarget.getValue().equals(SignatureTarget.ALL_MESSAGE_HEADERS)){
                allRef = true;
            }*/
            for(int i =0; i<referenceList.size(); i++){
                Reference reference = (Reference)referenceList.get(i);
                Data data = null;
                try{
                    data = getData(reference,validationContext);
                    if(requiredTarget && data != null){
                        DataWrapper tmpObj = new DataWrapper(data);
                        tmpObj.setTarget(signatureTarget);
                        //It would still have cid:*
                        requiredDataList.add(tmpObj);
                        requiredReferenceList.add(reference);
                    }
                }catch(Exception ex){
                    if(requiredTarget){
                        logger.log(Level.SEVERE, "WSS1339.invalid.ReceiverRequirements");
                        throw new XWSSecurityException("Receiver requirement for SignatureTarget "+
                                signatureTarget.getValue()+" is not met");
                    }
                }
                /*if(!allRef){
                    break;
                }*/
            }
        }
        
        if(optionalReqList.size() ==0 && requiredReferenceList.size() != signedReferenceList.size()){
            logger.log(Level.SEVERE, "WSS1340.illegal.unmatched.NoofTargets");
            throw new XWSSecurityException("Number of Targets in the message"+
                    " dont match number of Targets in receiver requirements");
        }
        
        if(requiredDataList.size() == 0){
            if(logger.isLoggable(Level.FINER)){
                logger.log(Level.FINER,"No mandatory receiver requirements were provided");
            }
            return;
        }
        
        for(int i=0;i<requiredDataList.size();i++){
            DataWrapper rData = (DataWrapper)requiredDataList.get(i);
            boolean found = false;
            for(int j=0;j< signedDataList.size();j++){
                DataWrapper sData = null;
                sData = (DataWrapper)signedDataList.get(j);
                if(isEqual(rData,sData,(Reference)requiredReferenceList.get(i),(Reference)signedReferenceList.get(j))){
                    signedDataList.remove(j);
                    signedReferenceList.remove(j);
                    found = true;
                    break;
                }
            }
            if(!found){
                //Reference st = (Reference)requiredReferenceList.get(i);
                String uri = rData.getTarget().getValue();
                String type = rData.getTarget().getType();
                logger.log(Level.SEVERE, "WSS1341.illegal.unmatched.Type.Uri");
                throw new XWSSecurityException("Receiver requirement for SignatureTarget "+
                        "having " + type+" type and value " +uri+" is not met");
            }
        }
        
        if(signedDataList.size() == 0){
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"All receiver requirements are met");
            }
            return;
        }else{
            List referenceList = null;
            
            //Resolve All optional references if any
            for(int i=0;i<optionalReqList.size();i++){
                SignatureTarget signatureTarget = (SignatureTarget)optionalReqList.get(i);
                try{
                    referenceList = null;
                    referenceList = dsigUtil.generateReferenceList(Collections.singletonList(signatureTarget),secureMessage,context,true, featureBinding.isEndorsingSignature());
                }catch(Exception ex){
                    if(logger.isLoggable(Level.FINE)){
                        logger.log(Level.FINE,"Optional Target not found in the message ",ex);
                    }
                }
                if(referenceList == null || referenceList.size() <= 0){
                    continue;
                }
                Reference reference = (Reference)referenceList.get(0);
                Data data = null;
                try{
                    data = getData(reference,validationContext);
                }catch(Exception ex){
                    //log
                }
                if(data != null){
                    DataWrapper tmpObj  = new DataWrapper(data);
                    tmpObj.setTarget(signatureTarget);
                    optionalDataList.add(tmpObj);
                    optionalReferenceList.add(reference);
                }
            }
            
            for(int i=0;i<signedDataList.size();i++){
                DataWrapper sData = (DataWrapper)signedDataList.get(i);
                DataWrapper oData = null;
                boolean found = false;
                
                for(int j=0;j< optionalDataList.size();j++){
                    oData = (DataWrapper)optionalDataList.get(j);
                    
                    if(isEqual(oData,sData,(Reference)optionalReferenceList.get(j),(Reference)signedReferenceList.get(i))){
                        optionalDataList.remove(j);
                        optionalReferenceList.remove(j);
                        found = true;
                        break;
                    }
                }
                
                if(!found){
                    Reference st = (Reference)signedReferenceList.get(i);
                    logger.log(Level.SEVERE, "WSS1341.illegal.unmatched.Type.Uri");
                    throw new XWSSecurityException("SignatureTarget in the message "+
                            "with URI " +st.getURI()+ " has not met receiver requirements");
                }
            }
            
            if(logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"All receiver requirements are met");
            }
        }
    }
    
    private static boolean isEqual(DataWrapper data1 ,DataWrapper data2,Reference ref1,Reference ref2) throws XWSSecurityException {
        if(data1.isNodesetData()&& data2.isNodesetData()){
            NodeSetData  ns1 = (NodeSetData)(data1.getData());
            NodeSetData  ns2 = (NodeSetData)(data2.getData());
            //Fix for Issue#5 back porting from XWSS_2_0
            Node nsd1Root = null;
            Node nsd2Root = null;
            
            if (ns1 instanceof org.jcp.xml.dsig.internal.dom.DOMSubTreeData) {
                nsd1Root = ((org.jcp.xml.dsig.internal.dom.DOMSubTreeData)ns1).getRoot();
            }
            
            if (ns2 instanceof org.jcp.xml.dsig.internal.dom.DOMSubTreeData) {
                nsd2Root = ((org.jcp.xml.dsig.internal.dom.DOMSubTreeData)ns2).getRoot();
            }
            
            if (nsd1Root != null && nsd2Root != null) {
                if(nsd1Root.isSameNode(nsd2Root) || nsd1Root.isEqualNode(nsd2Root)){
                    return true;
                }
            }
            return false;
                        /*
            Iterator itr1 = ns1.iterator();
            Iterator itr2 = ns2.iterator();
            //based of property set we can reduce checking all the
            //nodes we can check first and last node and if
            //possible parent and sibling
            while(itr1.hasNext() && itr2.hasNext()){
                Node node1 = (Node)itr1.next();
                Node node2 = (Node)itr2.next();
                if(!node1.isSameNode(node2)){
                    if(MessageConstants.debug){
                        logger.log(Level.FINEST, "Debug is Same Node returned false");
                    }
                    if(!node1.isEqualNode(node2)){
                        if(MessageConstants.debug){
                            logger.log(Level.FINEST, "Bail out");
                        }
                        return false;
                    }
                }
            }
                         
            if(itr1.hasNext() || itr2.hasNext()){
                return false;
            }
            return true;
                         */
        }else if(data1.isOctectData() && data2.isOctectData()){
            OctetStreamData osd1 = (OctetStreamData)data1.getData();
            OctetStreamData osd2 = (OctetStreamData)data2.getData();
            InputStream stream1 = (InputStream)osd1.getOctetStream();
            InputStream stream2 = (InputStream)osd2.getOctetStream();
            byte [] b1= new byte[128];
            byte [] b2= new byte[128];
            while(true){
                int len1 =0;
                int len2 =0;
                try{
                    len1 = stream1.read(b1);
                    len2 = stream2.read(b2);
                }catch(IOException ioEx){
                    if(logger.isLoggable(Level.FINEST)){
                        logger.log(Level.FINEST,"Error occurred while" +
                                "comparing OctetStreamData objects "+ioEx.getMessage());
                    }
                    return false;
                }
                if(len1 == -1 && len2 == -1){
                    break;
                }
                if(len1 != len2){
                    return false;
                }else{
                    for(int i=0;i<len1;i++){
                        if(b1[i] != b2[i])
                            return false;
                    }
                }
            }
            return true;
        }else if(data1.isAttachmentData() && data2.isAttachmentData()){
            AttachmentData ad1 = (AttachmentData)data1.getData();
            AttachmentData ad2 = (AttachmentData)data2.getData();
            String uriOne = ad1.getAttachmentPart().getContentId();
            String uriTwo = ad2.getAttachmentPart().getContentId();
            if(uriOne != null && uriOne.equals(uriTwo)){
                return isTransformsEqual(ref1,ref2);
            }else{
                return false;
            }
        }
        return false;
    }
    
    private static boolean isTransformsEqual(Reference ref1, Reference ref2) throws XWSSecurityException {
        List tList1 = ref1.getTransforms();
        
        List tList2 = ref2.getTransforms();
        if(tList1.size() != tList2.size()){
            logger.log(Level.SEVERE, "WSS1342.illegal.unmatched.transforms");
            throw new XWSSecurityException("Receiver Requirements for the transforms are not met");
            //return false;
        }else{
            int i=0;
            while(i< tList1.size()){
                Transform tr1 = (Transform)tList1.get(i);
                Transform tr2 = (Transform)tList2.get(i);
                
                String alg1 = tr1.getAlgorithm();
                String alg2 = tr2.getAlgorithm();
                i++;
                if(alg1 == alg2 || (alg1 != null && alg1.equals(alg2))){
                    continue;
                }else{
                    logger.log(Level.SEVERE, "WSS1342.illegal.unmatched.transforms");
                    throw new XWSSecurityException("Receiver Requirements for the transforms are not met");
                    //return false;
                }
                
            }
        }
        return true;
    }
    
    private static Data getData(Reference reference,DOMValidateContext context) throws Exception{
        
        final String uri = reference.getURI();
        URIReference uriRef = new URIReference(){
            public String getURI(){
                return uri;
            }
            
            public String getType(){
                return null;
            }
        };
        Data inputData = DSigResolver.getInstance().dereference(uriRef, context);
        if(inputData instanceof AttachmentData){
            return inputData;
        }
        List transformList = reference.getTransforms();
        Iterator itr = transformList.iterator();
        while(itr.hasNext()){
            Transform transform = (Transform)itr.next();
            inputData = getData(transform,inputData,context);
        }
        return inputData;
    }
    
    
    private static Data getData(Transform transform,Data inputData,DOMValidateContext context)throws Exception{
        String transformAlgo = transform.getAlgorithm();
        if( transformAlgo == Transform.XPATH || transformAlgo == Transform.XPATH2 || transformAlgo == Transform.XSLT ){
            TransformService transformImpl = TransformService.getInstance(transformAlgo,"DOM");
            TransformParameterSpec transformParams = null;
            //transformParams = transform.getParamter();
            transformParams = (TransformParameterSpec)transform.getParameterSpec();
            transformImpl.init(transformParams);
            return transformImpl.transform(inputData,context);
        }else {
            //handle all other transforms based on flag set on processing context.
            //flag =STRICT_VERIFICATION {true,false}
        }
        return inputData;
    }
    
    /**
     *
     * @param signElement
     * @param context
     * @throws XWSSecurityException
     * @return
     */
    public static boolean verifySignature(Element signElement, FilterProcessingContext context)
    throws XWSSecurityException {
        try {
            
            
            DOMValidateContext validationContext =
                    new DOMValidateContext(KeySelectorImpl.getInstance(), signElement);
            XMLSignatureFactory signatureFactory = WSSPolicyConsumerImpl.getInstance().getSignatureFactory();
            // unmarshal the XMLSignature
            XMLSignature signature = signatureFactory.unmarshalXMLSignature(validationContext);
            validationContext.setURIDereferencer(DSigResolver.getInstance());
            // Validate the XMLSignature (generated above)
            validationContext.put(MessageConstants.WSS_PROCESSING_CONTEXT, context);
            boolean coreValidity = signature.validate(validationContext);
            if (coreValidity == false){
                
                if(logger.isLoggable(Level.FINEST)){
                    logger.log(Level.FINEST,"Signature failed core validation");
                    boolean sv = signature.getSignatureValue().validate(validationContext);
                    logger.log(Level.FINEST,"Signature validation status: " + sv);
                    // check the validation status of each Reference
                    Iterator i = signature.getSignedInfo().getReferences().iterator();
                    for (int j=0; i.hasNext(); j++) {
                        Reference ref = (Reference) i.next();
                        logger.log(Level.FINEST,"Reference ID "+ref.getId());
                        logger.log(Level.FINEST,"Reference URI "+ref.getURI());
                        boolean refValid =
                                ref.validate(validationContext);
                        logger.log(Level.FINEST,"Reference["+j+"] validity status: " + refValid);
                    }
                }
            }
            return coreValidity;
            
        }catch (Exception e) {
            //log here
            logger.log(Level.SEVERE,"Exception occurred during signature verification"+e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    @SuppressWarnings("unchecked")
    private static KeyInfo prepareForSymmetricKeySignature(FilterProcessingContext context,
            WSSPolicy keyBinding, Key originalKey, SignaturePolicy signaturePolicy, Node[] nxtSiblingContainer, AuthenticationTokenPolicy.X509CertificateBinding certInfo, DerivedKeyToken dkt)
            throws XWSSecurityException {
        
        Node nextSibling = null;
        KeyInfo keyInfo = null;
        
        //depending on the ref type handle an X509Token
        //create an EK with keyInfo pointing to the X509
        String keyEncAlgo = XMLCipher.RSA_v1dot5;  //<--Harcoding of Algo
        if (context.getAlgorithmSuite() != null) {
            keyEncAlgo = context.getAlgorithmSuite().getAsymmetricKeyAlgorithm();
        }
        
        String referenceType =  null;
        KeyInfoStrategy  strategy = null;
        KeyInfoHeaderBlock keyInfoBlock  = null;
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        SecurityHeader securityHeader = secureMessage.findOrCreateSecurityHeader();
        
        WSSPolicyConsumerImpl dsigHelper = WSSPolicyConsumerImpl.getInstance();
        
        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
        boolean sendEKSHA1 =  wss11Receiver && wss11Sender&& (getEKSHA1Ref(context) != null);
        boolean wss10 = !wss11Sender;
        
        try {
            if ( PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)) {
                DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
                
                WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
                
                if (PolicyTypeUtil.x509CertificateBinding(originalKeyBinding)) {
                    logger.log(Level.SEVERE, "WSS1327.unsupported.asymmetricbinding.derivedkey.x509token");
                    throw new XWSSecurityException("Asymmetric Binding with DerivedKeys under X509Token Policy Not Yet Supported");
                } else  if ( PolicyTypeUtil.symmetricKeyBinding(originalKeyBinding)) {
                    
                    if(sendEKSHA1){
                        String ekSha1Ref = getEKSHA1Ref(context);
                        
                        //STR for DerivedKeyToken
                        SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                        EncryptedKeySHA1Identifier refElem = new EncryptedKeySHA1Identifier(secureMessage.getSOAPPart());
                        refElem.setReferenceValue(ekSha1Ref);
                        tokenRef.setReference(refElem);
                        //set the wsse11:TokenType attribute as required by WSS 1.1
                        //tokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                        //TODO: Temporary fix for signing
                        String dktId = keyBinding.getUUID();
                        if (dktId == null) {
                            dktId = secureMessage.generateId();
                        }
                        String nonce = Base64.encode(dkt.getNonce());
                        DerivedKeyTokenHeaderBlock dktHeadrBlock =
                                new DerivedKeyTokenHeaderBlock(securityHeader.getOwnerDocument(), tokenRef, nonce, dkt.getOffset(), dkt.getLength() ,dktId);
                        // insert the derivedKey into SecurityHeader
                        secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(dktHeadrBlock);
                        // set the next sibling to next sibling of derived key token
                        nextSibling = dktHeadrBlock.getAsSoapElement().getNextSibling();
                        nxtSiblingContainer[0] = nextSibling;
                        
                        //Construct the STR for signature
                        DirectReference reference = new DirectReference();
                        reference.setURI("#"+dktId);
                        SecurityTokenReference sigTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                        sigTokenRef.setReference(reference);
                        keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,sigTokenRef);
                        
                        return keyInfo;
                        
                    } else if(wss11Sender || wss10){
                        
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = null;
                        X509Certificate cert = null;
                        if ( context.getX509CertificateBinding() != null ) {
                            x509Binding = context.getX509CertificateBinding();
                            context.setX509CertificateBinding(null);
                        }
                        
                        HashMap tokenCache = context.getTokenCache();
                        HashMap insertedX509Cache = context.getInsertedX509Cache();
                        String x509id = x509Binding.getUUID();
                        if(x509id == null || x509id.equals("")){
                            x509id = secureMessage.generateId();
                        }
                        
                        SecurityUtil.checkIncludeTokenPolicy(context, x509Binding, x509id);
                        
                        referenceType =  x509Binding.getReferenceType();
                        strategy = KeyInfoStrategy.getInstance(referenceType);
                        X509SecurityToken token = null;
                        cert = x509Binding.getX509Certificate();
                        String x509TokenId = x509Binding.getUUID();
                        //Check to see if same x509 token used for Signature and Encryption
                        boolean tokenInserted = false;
                        
                        if(x509TokenId == null || x509TokenId.equals("")){
                            x509TokenId = secureMessage.generateId();
                        }
                        // ReferenceType adjustment in checkIncludeTokenPolicy is also currently
                        // causing an insertion of the X509 into the Message
                        X509SecurityToken insertedx509 =
                                (X509SecurityToken)context.getInsertedX509Cache().get(x509TokenId);
                        
                        // this one is used to determine if the whole BST + EK + DKT(opt)
                        // has been inserted by another filter such as Encryption running before
                        token = (X509SecurityToken)tokenCache.get(x509TokenId);
                        if (token == null) {
                            if (insertedx509 != null) {
                                token = insertedx509;
                                tokenCache.put(x509TokenId, insertedx509);
                            } else {
                                String valueType = x509Binding.getValueType();
                                if(valueType==null||valueType.equals("")){
                                    //default valueType for X509 as v3
                                    valueType = MessageConstants.X509v3_NS;
                                }
                                token = new X509SecurityToken(secureMessage.getSOAPPart(), cert, x509TokenId, valueType);
                                tokenCache.put(x509TokenId, token);
                            }
                            context.setCurrentSecret(originalKey);
                        } else{
                            tokenInserted = true;
                        }
                        
                        String dktId = keyBinding.getUUID();
                        if (dktId == null) {
                            dktId = secureMessage.generateId();
                        }
                        String nonce = Base64.encode(dkt.getNonce());
                        HashMap ekCache = context.getEncryptedKeyCache();
                        String ekId = (String)ekCache.get(x509TokenId);
                        EncryptedKey encryptedKey = null;
                        XMLCipher keyEncryptor = null;
                        if(!tokenInserted){
                            //Store SymmetricKey generated in ProcessingContext
                            context.setExtraneousProperty("SecretKey", originalKey); //this is the originalKey
                            //keyinfo for encryptedKey
                            keyInfoBlock = new KeyInfoHeaderBlock(secureMessage.getSOAPPart());
                            strategy.setCertificate(cert);
                            strategy.insertKey(keyInfoBlock, secureMessage, x509TokenId);
                            com.sun.org.apache.xml.internal.security.keys.KeyInfo apacheKeyInfo = keyInfoBlock.getKeyInfo();
                            
                            
                            //create an encrypted Key --- it encrypts the original key
                            try{
                                keyEncryptor = XMLCipher.getInstance(keyEncAlgo);
                                keyEncryptor.init(XMLCipher.WRAP_MODE, cert.getPublicKey());
                                if (keyEncryptor != null) {
                                    encryptedKey = keyEncryptor.encryptKey(secureMessage.getSOAPPart(), originalKey);
                                }
                            }catch(Exception e){
                                logger.log(Level.SEVERE, "WSS1334.error.creating.encryptedkey");
                                throw new XWSSecurityException(e);
                            }
                            ekId = secureMessage.generateId();
                            ekCache.put(x509TokenId, ekId);
                            encryptedKey.setId(ekId);
                            // set its KeyInfo
                            encryptedKey.setKeyInfo(apacheKeyInfo);
                        }
                        
                        //STR for DerivedKeyToken
                        SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                        DirectReference reference = new DirectReference();
                        //TODO: PLUGFEST commenting this as Microsoft puts Value type on reference itself
                        //tokenRef.setTokenType(MessageConstants.EncryptedKey_NS);
                        //set id of encrypted key in STR of DKT
                        reference.setValueType(MessageConstants.EncryptedKey_NS);
                        reference.setURI("#"+ekId);
                        tokenRef.setReference(reference);
                        DerivedKeyTokenHeaderBlock dktHeadrBlock =
                                new DerivedKeyTokenHeaderBlock(securityHeader.getOwnerDocument(), tokenRef, nonce, dkt.getOffset(), dkt.getLength() ,dktId);
                        
                        if(!tokenInserted){
                            Node nsX509 = null;
                            if (insertedx509 != null) {
                                nsX509 = insertedx509.getNextSibling();
                            }
                            // move DKT below X509 if present
                            if (nsX509 == null) {
                                secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(dktHeadrBlock);
                            } else {
                                secureMessage.findOrCreateSecurityHeader().insertBefore(dktHeadrBlock, nsX509);
                            }
                            // move EK above DKT but below X509
                            if (insertedx509 != null) {
                                nsX509 = insertedx509.getNextSibling();
                            }
                            
                            // insert the EK into the SOAPMessage -  this goes on top of DKT Header block
                            SOAPElement se = (SOAPElement)keyEncryptor.martial(encryptedKey);
                            if (nsX509 == null) {
                                secureMessage.findOrCreateSecurityHeader().insertHeaderBlockElement(se);
                            }else {
                                secureMessage.findOrCreateSecurityHeader().insertBefore(se, nsX509);
                            }
                            //insert the token as the first child in SecurityHeader
                            if (MessageConstants.DIRECT_REFERENCE_TYPE.equals(referenceType) && insertedX509Cache.get(x509TokenId) == null){
                                secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(token);
                                insertedX509Cache.put(x509TokenId, token);
                            }
                            //store EKSHA1 of KeyValue contents in context
                            Element cipherData = (Element)se.getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                            String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                            byte[] decodedCipher = Base64.decode(cipherValue);
                            byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                            String encEkSha1 = Base64.encode(ekSha1);
                            context.setExtraneousProperty("EncryptedKeySHA1", encEkSha1);
                        } else{
                            //insert derived key after the existing EK
                            Element ekElem = secureMessage.getElementById(ekId);
                            secureMessage.findOrCreateSecurityHeader().insertBefore(dktHeadrBlock, ekElem.getNextSibling());
                        }
                        
                        //Construct the STR for signature
                        DirectReference refSig = new DirectReference();
                        refSig.setURI("#"+dktId);
                        SecurityTokenReference sigTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                        sigTokenRef.setReference(refSig);
                        keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,sigTokenRef);
                        
                        // set the next sibling to next sibling of derived key token
                        nextSibling = dktHeadrBlock.getAsSoapElement().getNextSibling();
                        nxtSiblingContainer[0] = nextSibling;
                        
                        return keyInfo;
                        
                    }
                } else if ( PolicyTypeUtil.issuedTokenKeyBinding(originalKeyBinding)) {
                    
                    IssuedTokenKeyBinding itk = (IssuedTokenKeyBinding)originalKeyBinding;
                    
                    IssuedTokenContext issuedTokenContext =  context.getTrustContext();
                    
                    //Get the IssuedToken and insert it into the message
                    GenericToken issuedToken = (GenericToken)issuedTokenContext.getSecurityToken();
                    SOAPElement tokenElem =  null;
                    SecurityTokenReference str = null;
                    Element strElem = null;
                    
                    // check if the token is already present
                    IssuedTokenKeyBinding ikb = (IssuedTokenKeyBinding)originalKeyBinding;
                    //String ikbPolicyId = ikb.getPolicyToken().getTokenId();
                    String ikbPolicyId = ikb.getUUID();
                    
                    //Look for TrustToken in TokenCache
                    HashMap tokCache = context.getTokenCache();
                    Object tok = tokCache.get(ikbPolicyId);
                    SOAPElement issuedTokenElementFromMsg = null;
                    String iTokenType = ikb.getIncludeToken();
                    boolean includeIST =  (ikb.INCLUDE_ALWAYS_TO_RECIPIENT.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS_VER2.equals(iTokenType) ||
                                                 ikb.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(iTokenType)
                                                );
                    
                    if (includeIST && (issuedToken == null)) {
                        logger.log(Level.SEVERE, "WSS1343.null.IssuedToken");
                        throw new XWSSecurityException("Issued Token to be inserted into the Message was Null");
                    }
                    
                    if (issuedToken != null) {
                        // treat the token as an Opaque entity and just insert the token into message
                        Element elem = (Element)issuedToken.getTokenValue();
                        if (tok == null) {
                            //TODO: remove these expensive conversions DOM Imports
                            tokenElem = XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), elem);
                            //FIX for Issue 26: We need an Id to cache and MS is not setting in some cases
                            String tokId = tokenElem.getAttribute("Id");
                            if ("".equals(tokId) &&
                                    MessageConstants.ENCRYPTED_DATA_LNAME.equals(tokenElem.getLocalName())) {
                                tokenElem.setAttribute("Id", secureMessage.generateId());
                            }
                            tokCache.put(ikbPolicyId, tokenElem);
                        } else {
                            // it will be SOAPElement retrieve its wsuId attr
                            String wsuId = SecurityUtil.getWsuIdOrId((Element)tok);
                            issuedTokenElementFromMsg = (SOAPElement)secureMessage.getElementById(wsuId);
                            if (issuedTokenElementFromMsg == null) {
                                logger.log(Level.SEVERE, "WSS1344.error.locateIssueToken.Message");
                                throw new XWSSecurityException("Could not locate Issued Token in Message");
                            }
                        }
                    }
                    
                    if (includeIST) {
                        strElem = (Element)issuedTokenContext.getAttachedSecurityTokenReference().getTokenValue();
                    } else {
                        strElem = (Element)issuedTokenContext.getUnAttachedSecurityTokenReference().getTokenValue();
                    }
                    
                    //TODO: remove these expensive conversions
                    Element imported = (Element)secureMessage.getSOAPPart().importNode(strElem,true);
                    str = new SecurityTokenReference(XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), imported), false);
                    
                    if (originalKey != null) {
                        SecurityUtil.updateSamlVsKeyCache(str, context, originalKey);
                    }
                    
                    String dktId = keyBinding.getUUID();
                    if (dktId == null) {
                        dktId = secureMessage.generateId();
                    }
                    
                    DerivedKeyTokenHeaderBlock derivedKeyTokenHeaderBlock =
                            new DerivedKeyTokenHeaderBlock(
                            secureMessage.getSOAPPart(),
                            str,
                            Base64.encode(dkt.getNonce()),
                            dkt.getOffset(),
                            dkt.getLength(),
                            dktId);
                    
                    
                    if (issuedTokenElementFromMsg != null) {
                        SecurityHeader _secHeader = secureMessage.findOrCreateSecurityHeader();
                        _secHeader.insertBefore(derivedKeyTokenHeaderBlock, issuedTokenElementFromMsg.getNextSibling());
                    } else {
                        Node reflist = context.getCurrentRefList();
                        if (reflist != null) {
                            secureMessage.findOrCreateSecurityHeader().insertBefore(derivedKeyTokenHeaderBlock, reflist);
                            context.setCurrentReferenceList(null);
                        } else {
                            secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(derivedKeyTokenHeaderBlock);
                        }
                    }
                    
                    // insert the Issued Token after the DKT
                    if (tokenElem != null) {
                        if (includeIST) {
                            secureMessage.findOrCreateSecurityHeader().insertHeaderBlockElement(tokenElem);
                        }
                        // also store the token in Packet.invocationProperties to be used by
                        // client side response processing
                        context.setIssuedSAMLToken(tokenElem);
                    }
                    
                    //Construct the STR for signature
                    DirectReference refSig = new DirectReference();
                    refSig.setURI("#"+dktId);
                    SecurityTokenReference sigTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    sigTokenRef.setReference(refSig);
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,sigTokenRef);
                    
                    // set the next sibling to next sibling of derived key token
                    nextSibling = derivedKeyTokenHeaderBlock.getAsSoapElement().getNextSibling();
                    nxtSiblingContainer[0] = nextSibling;
                    return keyInfo;
                    
                } else if ( PolicyTypeUtil.samlTokenPolicy(originalKeyBinding)) {
                    logger.log(Level.SEVERE, "WSS1345.unsupported.derivedkeys.SAMLToken");
                    throw new UnsupportedOperationException("DerivedKeys with SAMLToken not yet supported");
                    
                } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(originalKeyBinding)) {
                    SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)originalKeyBinding;
                    //STR for DerivedKeyToken
                    SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    SOAPElement sctElement = insertSCT(context, sctBinding, tokenRef);
                    String dktId = keyBinding.getUUID();
                    if (dktId == null) {
                        dktId = secureMessage.generateId();
                    }
                    String nonce = Base64.encode(dkt.getNonce());
                    DerivedKeyTokenHeaderBlock dktHeaderBlock =
                            new DerivedKeyTokenHeaderBlock(
                            securityHeader.getOwnerDocument(), tokenRef, nonce, dkt.getOffset(), dkt.getLength() ,dktId);
                    
                    Node next = (sctElement != null) ? sctElement.getNextSibling() : null;
                    
                    if (next == null) {
                        Node reflist = context.getCurrentRefList();
                        if (reflist != null) {
                            next = reflist;
                            context.setCurrentReferenceList(null);
                        }
                    }
                    
                    SOAPElement dktElem = (SOAPElement)securityHeader.insertBefore(
                            dktHeaderBlock.getAsSoapElement(), next);
                    //Construct the STR for signature
                    DirectReference refSig = new DirectReference();
                    refSig.setURI("#"+dktId);
                    SecurityTokenReference sigTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                    sigTokenRef.setReference(refSig);
                    
                    // signature should be below DKT
                    nextSibling = dktElem.getNextSibling();
                    nxtSiblingContainer[0] = nextSibling;
                    
                    keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,sigTokenRef);
                    return keyInfo;
                }
                
            } else if ( PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
                //Get the IssuedToken and insert it into the message
                IssuedTokenContext issuedTokenContext =  context.getTrustContext();
                GenericToken issuedToken = (GenericToken)issuedTokenContext.getSecurityToken();
                SOAPElement tokenElem =  null;
                SecurityTokenReference str = null;
                Element strElem = null;
                SOAPElement issuedTokenElementFromMsg = null;
                
                // check if the token is already present
                IssuedTokenKeyBinding ikb = (IssuedTokenKeyBinding)keyBinding;
                //String ikbPolicyId = ikb.getPolicyToken().getTokenId();
                String ikbPolicyId = ikb.getUUID();
                
                //Look for TrustToken in TokenCache
                HashMap tokCache = context.getTokenCache();
                Object tok = tokCache.get(ikbPolicyId);
                String iTokenType = ikb.getIncludeToken();
                boolean includeIST = (ikb.INCLUDE_ALWAYS_TO_RECIPIENT.equals(iTokenType) ||
                          ikb.INCLUDE_ALWAYS.equals(iTokenType) ||
                          ikb.INCLUDE_ALWAYS_VER2.equals(iTokenType) ||
                          ikb.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(iTokenType)
                          );
                if (includeIST && (issuedToken == null)) {
                    logger.log(Level.SEVERE, "WSS1343.null.IssuedToken");
                    throw new XWSSecurityException("Issued Token to be inserted into the Message was Null");
                }
                
                if (issuedToken != null) {
                    // treat the token as an Opaque entity and just insert the token into message
                    Element elem = (Element)issuedToken.getTokenValue();
                    if (tok == null) {
                        //TODO: remove these expensive conversions DOM Imports
                        tokenElem = XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), elem);
                        //FIX for Issue 26: We need an Id to cache and MS is not setting in some cases
                        String tokId = tokenElem.getAttribute("Id");
                        if ("".equals(tokId) &&
                                MessageConstants.ENCRYPTED_DATA_LNAME.equals(tokenElem.getLocalName())) {
                            tokenElem.setAttribute("Id", secureMessage.generateId());
                        }
                        tokCache.put(ikbPolicyId, tokenElem);
                    } else {
                        // it will be SOAPElement retrieve its wsuId attr
                        String wsuId = SecurityUtil.getWsuIdOrId((Element)tok);
                        issuedTokenElementFromMsg = (SOAPElement)secureMessage.getElementById(wsuId);
                        if (issuedTokenElementFromMsg == null) {
                            logger.log(Level.SEVERE, "WSS1344.error.locateIssueToken.Message");
                            throw new XWSSecurityException("Could not locate Issued Token in Message");
                        }
                    }
                }
                
                if (includeIST) {
                    strElem = SecurityUtil.convertSTRToElement(issuedTokenContext.getAttachedSecurityTokenReference().getTokenValue(), secureMessage.getSOAPPart());
                } else {
                    strElem = SecurityUtil.convertSTRToElement(issuedTokenContext.getUnAttachedSecurityTokenReference().getTokenValue(), secureMessage.getSOAPPart());
                }
                
                if(strElem == null){
                    logger.log(Level.SEVERE, "WSS1378.unableto.refer.IssueToken");
                    throw new XWSSecurityException("Cannot determine how to reference the Issued Token in the Message");
                }
                
                //TODO: remove these expensive conversions
                Element imported = (Element)secureMessage.getSOAPPart().importNode(strElem,true);
                str = new SecurityTokenReference(
                        XMLUtil.convertToSoapElement(secureMessage.getSOAPPart(), (Element)imported.cloneNode(true)), false);
                
                if (originalKey != null) {
                    SecurityUtil.updateSamlVsKeyCache(str, context, originalKey);
                }
                
                if (tokenElem != null) {
                    if(includeIST) {

                        secureMessage.findOrCreateSecurityHeader().insertHeaderBlockElement(tokenElem);
                        nxtSiblingContainer[0] = tokenElem.getNextSibling();
                    } else {
                        nxtSiblingContainer[0] =  null;
                    }
                    // also store the token in Packet.invocationProperties to be used by
                    // client side response processing
                    context.setIssuedSAMLToken(tokenElem);
                } else if (issuedTokenElementFromMsg != null) {
                    nxtSiblingContainer[0] = issuedTokenElementFromMsg.getNextSibling();
                }
                
                keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,str);
                return keyInfo;
                
            } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)){
                
                SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)keyBinding;
                SOAPElement sctElement = insertSCT(context, sctBinding, secTokenRef);
                
                // signature should be below SCT
                nextSibling = (sctElement != null) ? sctElement.getNextSibling() : null;
                nxtSiblingContainer[0] =  nextSibling;
                
                keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,secTokenRef);
                return keyInfo;
            }
            
        } catch (SOAPException ex ) {
            logger.log(Level.SEVERE, "WSS1346.error.preparing.symmetrickey.signature", ex);
            throw new XWSSecurityException(ex);
        } catch (Base64DecodingException ex) {
            logger.log(Level.SEVERE, "WSS1346.error.preparing.symmetrickey.signature", ex);
            throw new XWSSecurityException(ex);
        } catch (NoSuchAlgorithmException ex) {
            logger.log(Level.SEVERE, "WSS1346.error.preparing.symmetrickey.signature", ex);
            throw new XWSSecurityException(ex);
        }
        
        return null;
    }
    @SuppressWarnings("unchecked")
    public static SOAPElement insertSCT(FilterProcessingContext context, SecureConversationTokenKeyBinding sctBinding, SecurityTokenReference secTokenRef)
    throws XWSSecurityException {
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        //String sctPolicyId = sctBinding.getPolicyToken().getTokenId();
        String sctPolicyId = sctBinding.getUUID();
        
        //Look for SCT in TokenCache
        HashMap tokCache = context.getTokenCache();
        SecurityContextTokenImpl sct = null;
        sct = (SecurityContextTokenImpl)tokCache.get(sctPolicyId);
        boolean tokenInserted = false;
        SOAPElement sctElement = null;
        
        IssuedTokenContext ictx = context.getSecureConversationContext();
        
        if (sct == null) {
            SecurityContextToken sct1 =(SecurityContextToken)ictx.getSecurityToken();
            if (sct1 == null) {
                logger.log(Level.SEVERE, "WSS1347.null.SecureConversationToken");
                throw new XWSSecurityException("SecureConversation Token not Found");
            }
            
            sct = new SecurityContextTokenImpl(
                    secureMessage.getSOAPPart(), sct1.getIdentifier().toString(), sct1.getInstance(), sct1.getWsuId(), sct1.getExtElements());
            // put back in token cache
            tokCache.put(sctPolicyId, sct);
        } else {
            tokenInserted = true;
            // record the element
            sctElement = secureMessage.getElementByWsuId(sct.getWsuId());
        }
        
        String sctWsuId = sct.getWsuId();
        if (sctWsuId == null) {
            sct.setId(secureMessage.generateId());
        }
        sctWsuId = sct.getWsuId();
        String iTokenType = sctBinding.getIncludeToken();
        if(sctBinding.INCLUDE_ALWAYS_TO_RECIPIENT.equals(iTokenType) ||
           sctBinding.INCLUDE_ALWAYS.equals(iTokenType) ||
           sctBinding.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(iTokenType) ||
           sctBinding.INCLUDE_ALWAYS_VER2.equals(iTokenType)) {
            
            if (!tokenInserted) {
                secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(sct);
                // record the element
                sctElement = secureMessage.getElementByWsuId(sct.getWsuId());
            }
            
            DirectReference reference = new DirectReference();
            
            reference.setURI("#" + sctWsuId);
            secTokenRef.setReference(reference);
        } else {
            // we have to insert SCT Session Id instead of wsuId
            DirectReference reference = new DirectReference();
            reference.setSCTURI(sct.getIdentifier().toString(), sct.getInstance());
            secTokenRef.setReference(reference);
        }
        
        return sctElement;
    }
    @SuppressWarnings("unchecked")
    private static KeyInfo handleX509Binding(FilterProcessingContext context,
            SignaturePolicy signaturePolicy,
            AuthenticationTokenPolicy.X509CertificateBinding certInfo,
            Node[] nxtSiblingContainer) throws XWSSecurityException{
        
        Node nextSibling = null;
        KeyInfo keyInfo = null;
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        SecurityHeader securityHeader = secureMessage.findOrCreateSecurityHeader();
        WSSPolicyConsumerImpl dsigHelper = WSSPolicyConsumerImpl.getInstance();
        
        HashMap tokenCache = context.getTokenCache();
        HashMap insertedX509Cache = context.getInsertedX509Cache();
        String x509id = certInfo.getUUID();
        if(x509id == null || x509id.equals("")){
            x509id = secureMessage.generateId();
        }
        
        SecurityUtil.checkIncludeTokenPolicy(context, certInfo, x509id);
        
        String referenceType = certInfo.getReferenceType();
        String strId = certInfo.getSTRID();
        if(strId == null){
            strId = secureMessage.generateId();
        }
        
        try{
            if(referenceType.equals("Direct")){
                DirectReference reference = new DirectReference();
                // this is an X509 certificate binding
                String valueType= certInfo.getValueType();
                if(valueType==null||valueType.equals("")){
                    valueType=MessageConstants.X509v3_NS;
                    
                }
                reference.setValueType(valueType);
                //Use DirectReferenceStrategy -
                //Revisit :: Move is generation to filters.
                String id = certInfo.getUUID();
                if(id == null || id.equals("")){
                    id = secureMessage.generateId();
                }
                reference.setURI("#"+id);
                SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                secTokenRef.setReference(reference);
                secTokenRef.setWsuId(strId);
                keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,secTokenRef);
                X509SecurityToken token =  null;
                token = (X509SecurityToken)tokenCache.get(id);
                if(token == null){
                    valueType = certInfo.getValueType();
                    if(valueType==null||valueType.equals("")){
                        //default valueType for X509 as v3
                        valueType = MessageConstants.X509v3_NS;
                    }
                    token = new X509SecurityToken(secureMessage.getSOAPPart(),certInfo.getX509Certificate(),id, valueType);
                    tokenCache.put(id, token);
                }
                if(insertedX509Cache.get(id) == null){
                    secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(token);
                    insertedX509Cache.put(id, token);
                }
                nextSibling = token.getAsSoapElement().getNextSibling();
                nxtSiblingContainer[0] = nextSibling;
                return keyInfo;
            }else if(referenceType.equals("Identifier")){
                String valueType = certInfo.getValueType();
                if(valueType==MessageConstants.X509v1_NS||valueType.equals(MessageConstants.X509v1_NS)) {
                    logger.log(Level.SEVERE, "WSS1333.unsupported.keyidentifer.X509v1");
                    throw new XWSSecurityException("Key Identifier reference Type is not allowed for X509v1 Certificates");
                }
                KeyIdentifierStrategy keyIdentifier =
                        new KeyIdentifierStrategy(certInfo.getCertificateIdentifier(),true);
                keyIdentifier.setCertificate(certInfo.getX509Certificate());
                SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                keyIdentifier.insertKey(secTokenRef, secureMessage);
                secTokenRef.setWsuId(strId);
                X509SubjectKeyIdentifier re = (X509SubjectKeyIdentifier)secTokenRef.getReference();
                String id = re.getReferenceValue();
                tokenCache.put(id, re);
                re.setCertificate(certInfo.getX509Certificate());
                keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,secTokenRef);
                nextSibling = securityHeader.getNextSiblingOfTimestamp();
                nxtSiblingContainer[0] = nextSibling;
                return keyInfo;
            }else if(referenceType.equals(MessageConstants.THUMB_PRINT_TYPE)){
                String valueType = certInfo.getValueType();
                if(valueType==MessageConstants.X509v1_NS||valueType.equals(MessageConstants.X509v1_NS)) {
                    logger.log(Level.SEVERE,"WSS1348.illegal.thumbprint.x509v1");
                    throw new XWSSecurityException("Thumb reference Type is not allowed for X509v1 Certificates");
                }
                KeyIdentifierStrategy keyIdentifier = new KeyIdentifierStrategy(certInfo.getCertificateIdentifier(),true, true);
                keyIdentifier.setCertificate(certInfo.getX509Certificate());
                SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                keyIdentifier.insertKey(secTokenRef, secureMessage);
                secTokenRef.setWsuId(strId);
                X509ThumbPrintIdentifier re = (X509ThumbPrintIdentifier)secTokenRef.getReference();
                String id = re.getReferenceValue();
                tokenCache.put(id, re);
                re.setCertificate(certInfo.getX509Certificate());
                keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,secTokenRef);
                nextSibling = securityHeader.getNextSiblingOfTimestamp();
                nxtSiblingContainer[0] = nextSibling;
                return keyInfo;
            }else if(referenceType.equals(MessageConstants.X509_ISSUER_TYPE)){
                X509Certificate xCert = certInfo.getX509Certificate();
                X509IssuerSerial xis = new X509IssuerSerial(secureMessage.getSOAPPart(),
                        xCert.getIssuerDN().getName(),xCert.getSerialNumber());
                SecurityTokenReference secTokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                secTokenRef.setReference(xis);
                secTokenRef.setWsuId(strId);
                xis.setCertificate(xCert);
                tokenCache.put(xis.getIssuerName()+xis.getSerialNumber(),xis);
                keyInfo = dsigHelper.constructKeyInfo(signaturePolicy,secTokenRef);
                nextSibling = securityHeader.getNextSiblingOfTimestamp();
                nxtSiblingContainer[0] = nextSibling;
                return keyInfo;
            }else{
                logger.log(Level.SEVERE, "WSS1308.unsupported.reference.mechanism");
                throw new XWSSecurityException("Reference type "+referenceType+"not supported");
            }
        } catch(Exception e){
            logger.log(Level.SEVERE, "WSS1349.error.handlingX509Binding", e);
            throw new XWSSecurityException(e);
        }
    }
    
    private static String getEKSHA1Ref(FilterProcessingContext context) {
        String ekSha1Ref = null;
        ekSha1Ref = (String) context.getExtraneousProperty(MessageConstants.EK_SHA1_VALUE);
        return ekSha1Ref;
    }
    
}
