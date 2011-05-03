/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.wss.impl.misc;


import com.sun.xml.wss.core.EncryptedKeyToken;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.saml.AssertionUtil;
import com.sun.xml.wss.impl.dsig.SignatureProcessor;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import java.security.Key;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;

import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurableSoapMessage;

import com.sun.xml.wss.core.SecurityToken;
import com.sun.xml.wss.core.ReferenceElement;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.DerivedKeyTokenHeaderBlock;

import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.core.reference.DirectReference;
import com.sun.xml.wss.core.reference.X509IssuerSerial;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.saml.util.SAMLUtil;
import com.sun.xml.wss.impl.XMLUtil;

import org.w3c.dom.NodeList;

import java.util.HashMap;
import javax.xml.soap.SOAPElement;
import javax.crypto.spec.SecretKeySpec;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;

import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.security.DerivedKeyToken;

import com.sun.xml.ws.security.trust.elements.BinarySecret;
import javax.security.auth.Subject;
import com.sun.xml.ws.runtime.dev.SessionManager;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.secconv.impl.client.DefaultSCTokenConfiguration;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.wss.logging.LogStringsMessages;


public class KeyResolver {
    
    private static Logger log = Logger.getLogger(LogDomainConstants.WSS_API_DOMAIN, LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * If a SecurityTokenReference is present inside the KeyInfo,
     * the return value is an instance of PrivateKey (if sig is false) or
     * PublicKey (if sig is true).
     * Else If a KeyName is present inside the KeyInfo, then the return
     * value is an instance of SecretKey.
     * Else, an XWSSecurityException is thrown.
     *
     * @param keyInfo
     * @param sig
     *     true if this method is called by a signature verifier, false if
     *     called by a decrypter
     * @param secureMsg
     */
    
    /*
     *
     * TODO:: SIG flag to be removed once JSR105 has been tested completly.-Venu
     */
    public static Key getKey( KeyInfoHeaderBlock keyInfo,  boolean sig,
            FilterProcessingContext context)  throws XWSSecurityException {
        
        Key returnKey;
        //HashMap tokenCache = context.getTokenCache();
        try {
            SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
            if (keyInfo.containsSecurityTokenReference()) {
                return processSecurityTokenReference(keyInfo,sig, context);
            }else if (keyInfo.containsKeyName()) {
                EncryptionPolicy policy = (EncryptionPolicy)context.getInferredPolicy();
                
                String keynameString = keyInfo.getKeyNameString(0);
                if(policy != null){
                    SymmetricKeyBinding keyBinding = null;
                    keyBinding = (SymmetricKeyBinding)policy.newSymmetricKeyBinding();
                    keyBinding.setKeyIdentifier(keynameString);
                    
                }
                returnKey =
                        context.getSecurityEnvironment().getSecretKey(context.getExtraneousProperties(),
                        keynameString,
                        false);
            } else if (keyInfo.containsKeyValue()) {
                // resolve KeyValue
                returnKey =
                        resolveKeyValue(secureMsg, keyInfo.getKeyValue(0), sig,context);
            } else if (keyInfo.containsX509Data()) {
                // resolve X509Data
                returnKey =  resolveX509Data(secureMsg, keyInfo.getX509Data(0), sig,context);
            } else if(keyInfo.containsEncryptedKeyToken()){
                EncryptedKeyToken token = keyInfo.getEncryptedKey(0);
                KeyInfoHeaderBlock kiHB = token.getKeyInfo();
                if(kiHB.containsSecurityTokenReference()){
                    SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0335_UNSUPPORTED_REFERENCETYPE());
                    throw new XWSSecurityException("Unsupported reference type under EncryptedKey");
                }
                //Default algo
                //String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                //restore backward compatibility
                String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                if (context.getAlgorithmSuite() != null) {
                    dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                }else{
                    if (context.getDataEncryptionAlgorithm() != null){
                        dataEncAlgo = context.getDataEncryptionAlgorithm();
                    }
                }
                returnKey = token.getSecretKey(getKey(kiHB, false, context), dataEncAlgo);
            } else if (keyInfo.containsBinarySecret()) {
                BinarySecret bs = keyInfo.getBinarySecret(0);
                // assuming the Binary Secret is of Type
                if ((bs.getType() == null) || bs.getType().equals(BinarySecret.SYMMETRIC_KEY_TYPE)) {
                    String algo = "AES"; // hardcoding for now
                    if (context.getAlgorithmSuite() != null) {
                        algo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                    }
                    returnKey = new SecretKeySpec(bs.getRawValue(), algo);
                } else {
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0339_UNSUPPORTED_KEYINFO());
                    throw new XWSSecurityException("Unsupported wst:BinarySecret Type");
                }
                
            } else {               
                XWSSecurityException xwsse =
                        new XWSSecurityException(
                        "Support for processing information in the given ds:KeyInfo is not present");
                log.log(Level.SEVERE, LogStringsMessages.WSS_0339_UNSUPPORTED_KEYINFO(),xwsse);
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY,
                        xwsse.getMessage(),
                        xwsse);
            }
        }catch(WssSoapFaultException wsse){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0284_WSS_SOAP_FAULT_EXCEPTION(), wsse);
            throw wsse;
        }catch (XWSSecurityException xwsse) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0284_WSS_SOAP_FAULT_EXCEPTION(), xwsse);
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                    xwsse.getMessage(),
                    xwsse);
        }
        
        if (returnKey == null) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSS_0600_ILLEGAL_TOKEN_REFERENCE());
            XWSSecurityException xwsse =
                    new XWSSecurityException(
                    "Referenced security token could not be retrieved");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                    xwsse.getMessage(),
                    xwsse);
        }
        
        return returnKey;
    }
    
    @SuppressWarnings("unchecked")
    public static Key resolveSamlAssertion(SecurableSoapMessage secureMsg, Element samlAssertion,
            boolean sig,FilterProcessingContext context, String assertionID) throws XWSSecurityException {
        
        try {
            Key key = (Key) context.getSamlIdVSKeyCache().get(assertionID);
            String samlSignatureResolved = (String)context.getExtraneousProperty(MessageConstants.SAML_SIG_RESOLVED);
            if (key != null){
                return key;
            }
            
            //TODO: expensive conversion happening
            if (samlAssertion == null) {
                log.log(Level.SEVERE,LogStringsMessages.WSS_0235_FAILED_LOCATE_SAML_ASSERTION());
                throw new XWSSecurityException("Cannot Locate SAML Assertion");
            }
            
            if ("false".equals(samlSignatureResolved)) {
                NodeList nl = samlAssertion.getElementsByTagNameNS(MessageConstants.DSIG_NS, "Signature");
                //verify the signature inside the SAML assertion
                if ( nl.getLength() == 0 ) {
                    XWSSecurityException e = new XWSSecurityException("Unsigned SAML Assertion encountered");
                    log.log(Level.SEVERE, com.sun.xml.wss.logging.impl.dsig.LogStringsMessages.WSS_1309_SAML_SIGNATURE_VERIFY_FAILED(), e);
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_INVALID_SECURITY,
                            "Exception during Signature verfication in SAML Assertion",
                            e);
                }
                SignaturePolicy policy = (SignaturePolicy)context.getInferredPolicy();
                // will be only during verify.
//                AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
//
//                if(policy != null){
//                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
//                }
                
                Element elem = (Element)nl.item(0);
                
                try {
                    if ( !SignatureProcessor.verifySignature(elem, context)) {
                        log.log(Level.SEVERE, com.sun.xml.wss.logging.impl.dsig.LogStringsMessages.WSS_1310_SAML_SIGNATURE_INVALID());
                        throw SecurableSoapMessage.newSOAPFaultException(
                                MessageConstants.WSSE_FAILED_AUTHENTICATION,
                                "SAML Assertion has invalid Signature",
                                new Exception(
                                "SAML Assertion has invalid Signature"));
                    }
                } catch (XWSSecurityException ex) {
                    log.log(Level.SEVERE, com.sun.xml.wss.logging.impl.dsig.LogStringsMessages.WSS_1310_SAML_SIGNATURE_INVALID());
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_FAILED_AUTHENTICATION,
                            "SAML Assertion has invalid Signature",
                            ex);
                }
            }
            
            if ( "false".equals(samlSignatureResolved) ){
                context.setExtraneousProperty(MessageConstants.SAML_SIG_RESOLVED,"true");
            }
            
            Element keyInfoElem =
                    AssertionUtil.getSubjectConfirmationKeyInfo(samlAssertion);
            KeyInfoHeaderBlock keyInfo = new KeyInfoHeaderBlock(
                    XMLUtil.convertToSoapElement(secureMsg.getSOAPPart(),keyInfoElem));
            key = getKey(keyInfo, sig, context);
            context.getSamlIdVSKeyCache().put(assertionID, key);
            return key;
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0238_FAILED_RESOLVE_SAML_ASSERTION());
            throw new XWSSecurityException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Key processSecurityTokenReference(KeyInfoHeaderBlock keyInfo,
            boolean sig, FilterProcessingContext context)throws XWSSecurityException{
        Key returnKey = null;
        HashMap tokenCache = context.getTokenCache();
        SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
        SecurityTokenReference str = keyInfo.getSecurityTokenReference(0);
        ReferenceElement refElement = str.getReference();
        EncryptionPolicy policy = (EncryptionPolicy)context.getInferredPolicy();
        EncryptionPolicy inferredEncryptionPolicy = null;
        boolean isWSITRecipient = (context.getMode()== FilterProcessingContext.WSDL_POLICY);
        try{
            if(isWSITRecipient){
                int i = context.getInferredSecurityPolicy().size() - 1;
                inferredEncryptionPolicy = (EncryptionPolicy)context.getInferredSecurityPolicy().get(i);
            }
        } catch(Exception e){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0239_FAILED_PROCESS_SECURITY_TOKEN_REFERENCE(), e);
            throw new XWSSecurityException(e);
        }
        // Do a case analysis based on the type of refElement.
        // X509 Token Profile supports 3 kinds of reference mechanisms.
        // Embedded Reference not considered.
        if (refElement instanceof KeyIdentifier) {
            KeyIdentifier keyId = (KeyIdentifier)refElement;
            
            if (MessageConstants.X509SubjectKeyIdentifier_NS.equals(keyId.getValueType()) ||
                    MessageConstants.X509v3SubjectKeyIdentifier_NS.equals(keyId.getValueType())) {
                if(policy != null){
                    AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                    keyBinding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                }
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                    x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    if(inferredKB == null){
                        inferredEncryptionPolicy.setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                        ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                        if(dktBind.getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                        else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                            dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                        }
                    }
                    
                }
                if (sig) {
                    returnKey =
                            context.getSecurityEnvironment().getPublicKey(context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()));
                } else {
                    returnKey =
                            context.getSecurityEnvironment().getPrivateKey(context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()));
                }
                
            } else if (MessageConstants.ThumbPrintIdentifier_NS.equals(keyId.getValueType())) {
                if(policy != null){
                    AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                    keyBinding.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
                }
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setValueType(MessageConstants.ThumbPrintIdentifier_NS);
                    x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    if(inferredKB == null){
                        inferredEncryptionPolicy.setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                        ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                        if(dktBind.getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                        else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                            dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                        }
                    }
                    
                }
                if (sig) {
                    returnKey =
                            context.getSecurityEnvironment().getPublicKey(
                            context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()),
                            MessageConstants.THUMB_PRINT_TYPE
                            );
                } else {
                    returnKey =
                            context.getSecurityEnvironment().getPrivateKey(
                            context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()),
                            MessageConstants.THUMB_PRINT_TYPE
                            );
                }
                
            } else if(MessageConstants.EncryptedKeyIdentifier_NS.equals(keyId.getValueType())){
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(inferredKB == null){
                        inferredEncryptionPolicy.setKeyBinding(skBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    }
                }
                String ekSha1RefValue = (String)context.getExtraneousProperty("EncryptedKeySHA1");
                Key secretKey = (Key)context.getExtraneousProperty("SecretKey");
                String keyRefValue = keyId.getReferenceValue();
                if(ekSha1RefValue != null && secretKey != null){
                    if(ekSha1RefValue.equals(keyRefValue))
                        returnKey = secretKey;
                } else{
                    String message = "EncryptedKeySHA1 reference not correct";
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE());
                    throw new XWSSecurityException(message);
                }
                
            } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())
            || MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                // Its a SAML Assertion, retrieve the assertion
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                    keyBinding.setReferenceType(keyId.getValueType());
                }
                String assertionID = keyId.getDecodedReferenceValue();
                Element samlAssertion = resolveSAMLToken(str, assertionID,context);
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                    if(inferredKB == null){
                        if (context.hasIssuedToken()){
                            inferredEncryptionPolicy.setKeyBinding(itkBinding);
                        }else{
                            inferredEncryptionPolicy.setKeyBinding(new AuthenticationTokenPolicy.SAMLAssertionBinding());
                        }
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                    }
                }
                returnKey = resolveSamlAssertion(secureMsg,samlAssertion, sig,context, assertionID);
                if (context.hasIssuedToken() && returnKey != null){
                    SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
                }
                
            } else {
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                }
                Element samlAssertion = null;
                String assertionID = keyId.getDecodedReferenceValue();
                try{
                    samlAssertion = resolveSAMLToken(str, assertionID,context);
                }catch(Exception ex){
                    //ignore
                }
                if (samlAssertion != null) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(itkBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                        }
                    }
                    returnKey = resolveSamlAssertion(secureMsg,samlAssertion, sig,context, assertionID);
                    if (context.hasIssuedToken() && returnKey != null){
                        SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
                    }
                } else {
                    // now assume its an X509Token
                    // Note: the code below assumes base64 EncodingType for X509 SKI
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                        x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                            ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                            if(dktBind.getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                            else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                                dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                            }
                        }
                        
                    }
                    if (sig) {
                        returnKey =
                                context.getSecurityEnvironment().getPublicKey(context.getExtraneousProperties(),
                                getDecodedBase64EncodedData(keyId.getReferenceValue()));
                    } else {
                        returnKey =
                                context.getSecurityEnvironment().getPrivateKey(context.getExtraneousProperties(),
                                getDecodedBase64EncodedData(keyId.getReferenceValue()));
                    }
                }
            }
        } else if (refElement instanceof DirectReference) {
            String uri = ((DirectReference) refElement).getURI();
            
            // will be only during verify.
            AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
            String valueType = ((DirectReference) refElement).getValueType();
            if (MessageConstants.DKT_VALUETYPE.equals(valueType) ||
                    MessageConstants.DKT_13_VALUETYPE.equals(valueType)){
                //TODO: this will work for now but need to handle this case here later
                valueType = null;
            }
            
            if(policy != null){
                keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                keyBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                keyBinding.setValueType(valueType);
            }
            
            if (MessageConstants.X509v3_NS.equals(valueType)||MessageConstants.X509v1_NS.equals(valueType)) {
                // its an X509 Token
                HashMap insertedX509Cache = context.getInsertedX509Cache();
                String wsuId = secureMsg.getIdFromFragmentRef(uri);
                X509SecurityToken token = (X509SecurityToken)insertedX509Cache.get(wsuId);
                if(token == null)
                    token =(X509SecurityToken)resolveToken(wsuId,context,secureMsg);
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                    x509Binding.setValueType(valueType);
                    if(inferredKB == null){
                        inferredEncryptionPolicy.setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                        ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    } else  if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                        if(dktBind.getOriginalKeyBinding() == null)
                            dktBind.setOriginalKeyBinding(x509Binding);
                        else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding()))
                            dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                    }
                    
                }
                returnKey = resolveX509Token(secureMsg, token, sig,context);
                
            } else if(MessageConstants.EncryptedKey_NS.equals(valueType)){
                // Do default processing
                String wsuId = secureMsg.getIdFromFragmentRef(uri);
                SecurityToken token =resolveToken(wsuId,context,secureMsg);
                //TODO: STR is referring to EncryptedKey
                KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                
                //String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                // now that context will have AlgoSuite under WSIT, this should not be an issue
                // so restoring old value since it breaks Backward Compat otherwise
                String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                if (context.getAlgorithmSuite() != null) {
                    dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                }else{
                    if (context.getDataEncryptionAlgorithm() != null){
                        dataEncAlgo = context.getDataEncryptionAlgorithm();
                    }
                }
                try{
                    Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                    String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                    byte[] decodedCipher = Base64.decode(cipherValue);
                    byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                    String encEkSha1 = Base64.encode(ekSha1);
                    context.setExtraneousProperty(MessageConstants.EK_SHA1_VALUE, encEkSha1);
                    
                } catch(Exception e){
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT(), e);
                    throw new XWSSecurityException(e);
                }
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(inferredKB == null){
                        inferredEncryptionPolicy.setKeyBinding(skBinding);
                    } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                        ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                        if(dktBind.getOriginalKeyBinding() == null)
                            dktBind.setOriginalKeyBinding(x509Binding);
                        else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding()))
                            dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                    }
                    
                }
                returnKey = ((EncryptedKeyToken)token).getSecretKey(getKey(kiHB, sig, context), dataEncAlgo);
                context.setExtraneousProperty(MessageConstants.SECRET_KEY_VALUE, returnKey);
                
            } else if (MessageConstants.SCT_VALUETYPE.equals(valueType) || MessageConstants.SCT_13_VALUETYPE.equals(valueType)) {
                // could be wsuId or SCT Session Id
                String sctId = secureMsg.getIdFromFragmentRef(uri);
                SecurityToken token = (SecurityToken)tokenCache.get(sctId);
                
                if(token == null){
                    token = SecurityUtil.locateBySCTId(context, uri);
                    if (token == null) {
                        token = resolveToken(sctId, context, secureMsg);
                    }
                    if(token == null){
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0242_UNABLETO_LOCATE_SCT());
                        throw new XWSSecurityException("SCT Token with Id "+sctId+ "not found");
                    }else{
                        tokenCache.put(sctId, token);
                    }
                }
                
                if (token instanceof SecurityContextToken) {
                    //handling for SecurityContext Token
                    byte[] proofKey = resolveSCT(context, (SecurityContextTokenImpl)token, sig);
                    String encAlgo = "AES"; //hardcoding for now
                    if (context.getAlgorithmSuite() != null) {
                        encAlgo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                    }
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(sctBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                        }
                    }
                    returnKey = new SecretKeySpec(proofKey, encAlgo);
                    
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0243_INVALID_VALUE_TYPE_NON_SCT_TOKEN());
                    throw new XWSSecurityException("Incorrect ValueType: " + MessageConstants.SCT_VALUETYPE + ", specified for a Non SCT Token");
                }
                
            } else if (null == valueType) {
                // Do default processing
                String wsuId = secureMsg.getIdFromFragmentRef(uri);
                SecurityToken token = SecurityUtil.locateBySCTId(context, wsuId);
                if (token == null) {
                    token =resolveToken(wsuId,context,secureMsg);
                }
                if (token instanceof X509SecurityToken) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                        }
                    }
                    returnKey = resolveX509Token(secureMsg, (X509SecurityToken)token, sig,context);
                } else if (token instanceof EncryptedKeyToken) {
                    //TODO: STR is referring to EncryptedKey
                    KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                    SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                    ReferenceElement refElem = sectr.getReference();
                    String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                    // now that context will have AlgoSuite under WSIT, this should not be an issue
                    // so restoring old value since it breaks Backward Compat otherwise
                    //String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                    if (context.getAlgorithmSuite() != null) {
                        dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                    }else{
                        if (context.getDataEncryptionAlgorithm() != null){
                            dataEncAlgo = context.getDataEncryptionAlgorithm();
                        }
                    }
                    try{
                        Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.setExtraneousProperty(MessageConstants.EK_SHA1_VALUE, encEkSha1);
                    } catch(Exception e){
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT(), e);
                        throw new XWSSecurityException(e);
                    }
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        skBinding.setKeyBinding(x509Binding);
                        //TODO: ReferenceType and ValueType not set on X509Binding
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(skBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                        }
                    }
                    returnKey = ((EncryptedKeyToken)token).getSecretKey(getKey(kiHB, sig, context), dataEncAlgo);
                    context.setExtraneousProperty(MessageConstants.SECRET_KEY_VALUE, returnKey);
                    
                } else if (token instanceof SecurityContextToken) {
                    //handling for SecurityContext Token
                    byte[] proofKey = resolveSCT(context, (SecurityContextTokenImpl)token, sig);
                    String encAlgo = "AES"; //default algo
                    if (context.getAlgorithmSuite() != null) {
                        encAlgo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                    }
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(sctBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                        }
                    }
                    returnKey = new SecretKeySpec(proofKey, encAlgo);
                    
                } else if (token instanceof DerivedKeyTokenHeaderBlock){
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        DerivedTokenKeyBinding dtkBinding = new DerivedTokenKeyBinding();
                        if(inferredKB == null){
                            inferredEncryptionPolicy.setKeyBinding(dtkBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)) {
                            //already set - do nothing
                        } else{
                            log.log(Level.SEVERE, LogStringsMessages.WSS_0244_INVALID_LEVEL_DKT());
                            throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                        }
                    }
                    returnKey = resolveDKT(context, (DerivedKeyTokenHeaderBlock)token);
                } else {
                    String message = " Cannot Resolve URI " + uri;
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(message),
                            new Object[] {message});
                    XWSSecurityException xwsse = new XWSSecurityException(message);
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                            xwsse.getMessage(), xwsse);
                }
            } else {
                log.log(
                        Level.SEVERE,
                        LogStringsMessages.WSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(((DirectReference)refElement).getValueType()),
                        new Object[] {((DirectReference)refElement).getValueType()});
                XWSSecurityException xwsse =
                        new XWSSecurityException(
                        "unsupported directreference ValueType "
                        + ((DirectReference) refElement).getValueType());
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        xwsse.getMessage(), xwsse);
            }
        } else if (refElement instanceof X509IssuerSerial) {
            BigInteger serialNumber = ((X509IssuerSerial) refElement).getSerialNumber();
            String issuerName = ((X509IssuerSerial) refElement).getIssuerName();
            
            if(isWSITRecipient){
                MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                x509Binding.setReferenceType(MessageConstants.X509_ISSUER_TYPE);
                if(inferredKB == null){
                    inferredEncryptionPolicy.setKeyBinding(x509Binding);
                } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                    ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                    if(dktBind.getOriginalKeyBinding() == null)
                        dktBind.setOriginalKeyBinding(x509Binding);
                    else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding()))
                        dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                }
                
            }
            if (sig) {
                returnKey = context.getSecurityEnvironment().getPublicKey(
                        context.getExtraneousProperties(), serialNumber, issuerName);
            } else {
                returnKey = context.getSecurityEnvironment().getPrivateKey(
                        context.getExtraneousProperties(), serialNumber, issuerName);
            }
        } else {
            log.log(
                    Level.SEVERE, LogStringsMessages.WSS_0338_UNSUPPORTED_REFERENCE_MECHANISM());
            XWSSecurityException xwsse =
                    new XWSSecurityException(
                    "Key reference mechanism not supported");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_UNSUPPORTED_SECURITY_TOKEN,
                    xwsse.getMessage(),
                    xwsse);
        }
        return returnKey;
    }
    
    public static Key resolveX509Token( SecurableSoapMessage secureMsg, X509SecurityToken token,
            boolean sig,FilterProcessingContext context) throws XWSSecurityException {
        
        if (sig) {
            // Update the Subject of the sender
            X509Certificate cert = token.getCertificate();
            context.getSecurityEnvironment().updateOtherPartySubject(DefaultSecurityEnvironmentImpl.getSubject(context), cert);
            // updating other party credentials
            if (context.getTrustCredentialHolder() != null) {
                context.getTrustCredentialHolder().setRequestorCertificate(cert);
            }
            return cert.getPublicKey();
        } else {
            return context.getSecurityEnvironment().getPrivateKey(context.getExtraneousProperties(),
                    token.getCertificate());
        }
    }
    
    public static Key resolveKeyValue(SecurableSoapMessage secureMsg,KeyValue  keyValue,
            boolean sig,FilterProcessingContext context)  throws XWSSecurityException {
        keyValue.getElement().normalize();
        try {
            if (sig) {
                return keyValue.getPublicKey();
            } else {
                return context.getSecurityEnvironment().
                        getPrivateKey(context.getExtraneousProperties(),keyValue.getPublicKey(), false);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSS_0601_UNSUPPORTED_KEYINFO_WSS_0601_ILLEGAL_KEY_VALUE(e.getMessage()),
                    e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    
    // not supporting CRL and SubjectName yet
    // TODO: the support is incomplete here
    public static Key resolveX509Data(SecurableSoapMessage secureMsg, X509Data x509Data,
            boolean sig,FilterProcessingContext context) throws XWSSecurityException {
        
        // workaround for what seems to be a bug in XMLSecurity
        // text node getting split
        x509Data.getElement().normalize();
        X509Certificate cert =  null;
        
        try {
            if (x509Data.containsCertificate()) {
                cert = (x509Data.itemCertificate(0)).getX509Certificate();
            } else if (x509Data.containsSKI()) {
                if (sig) {
                    return context.getSecurityEnvironment().getPublicKey(
                            context.getExtraneousProperties(), x509Data.itemSKI(0).getSKIBytes());
                } else {
                    return
                            context.getSecurityEnvironment().getPrivateKey(
                            context.getExtraneousProperties(), x509Data.itemSKI(0).getSKIBytes());
                }
            } else if (x509Data.containsSubjectName()) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0339_UNSUPPORTED_KEYINFO());
                throw new XWSSecurityException(
                        "X509SubjectName child element of X509Data is not yet supported by our implementation");
            } else if (x509Data.containsIssuerSerial()) {
                if (sig) {
                    return context.getSecurityEnvironment().
                            getPublicKey(
                            context.getExtraneousProperties(),
                            x509Data.itemIssuerSerial(0).getSerialNumber(),
                            x509Data.itemIssuerSerial(0).getIssuerName());
                } else {
                    return context.getSecurityEnvironment().
                            getPrivateKey(
                            context.getExtraneousProperties(),
                            x509Data.itemIssuerSerial(0).getSerialNumber(),
                            x509Data.itemIssuerSerial(0).getIssuerName());
                }
                
            } else {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0339_UNSUPPORTED_KEYINFO());
                throw new XWSSecurityException(
                        "Unsupported child element of X509Data encountered");
            }
            
            if (sig) {
                return cert.getPublicKey();
            } else {
                return context.getSecurityEnvironment().getPrivateKey(
                        context.getExtraneousProperties(), cert);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSS_0602_ILLEGAL_X_509_DATA(e.getMessage()),
                    e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    
    private static byte[] getDecodedBase64EncodedData(String encodedData)
    throws XWSSecurityException {
        try {
            return Base64.decode(encodedData);
        } catch (Base64DecodingException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0144_UNABLETO_DECODE_BASE_64_DATA(e.getMessage()) ,e);
            throw new XWSSecurityException(
                    "Unable to decode Base64 encoded data",
                    e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static SecurityToken resolveToken(String uri, FilterProcessingContext context, SecurableSoapMessage secureMsg)
    throws XWSSecurityException{
        try{
            HashMap cache = context.getTokenCache();
            SecurityToken token = (SecurityToken)cache.get(uri);
            if(token != null){
                return token;
            }
            if (token == null) {
                Node tokenNode =  secureMsg.getElementById(uri);
                tokenNode.normalize();
                if(MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME.equals(tokenNode.getLocalName())){
                    token = new X509SecurityToken((SOAPElement)tokenNode);
                } else if ( MessageConstants.ENCRYPTEDKEY_LNAME.equals(tokenNode.getLocalName())) {
                    token = new EncryptedKeyToken((SOAPElement)tokenNode);
                } else if (MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME.equals(tokenNode.getLocalName())) {
                    token = new SecurityContextTokenImpl((SOAPElement)tokenNode);
                } else if (MessageConstants.DERIVEDKEY_TOKEN_LNAME.equals(tokenNode.getLocalName())){
                    token = new DerivedKeyTokenHeaderBlock((SOAPElement)tokenNode);
                }
            }
            cache.put(uri, token);
            return token;
        }catch(Exception ex){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0245_FAILED_RESOLVE_SECURITY_TOKEN(), ex);
            throw new XWSSecurityException(ex);
        }
    }
    
    private static Element resolveSAMLToken(SecurityTokenReference tokenRef, String assertionId,
            FilterProcessingContext context)throws XWSSecurityException {
        
        Element clientSAMLAssertionCache = (Element)context.getExtraneousProperties().get(MessageConstants.SAML_ASSERTION_CLIENT_CACHE);
        
        if (clientSAMLAssertionCache != null){
            return clientSAMLAssertionCache;
        }
        
        Element tokenElement = context.getIssuedSAMLToken();
        if (tokenElement != null){
            context.setExtraneousProperty(MessageConstants.SAML_SIG_RESOLVED,"false");
        }
        if (tokenElement == null) {
            if (tokenRef.getSamlAuthorityBinding() != null) {
                tokenElement = context.getSecurityEnvironment().
                        locateSAMLAssertion(
                        context.getExtraneousProperties(),
                        tokenRef.getSamlAuthorityBinding(),
                        assertionId,
                        context.getSOAPMessage().getSOAPPart());
            } else {
                tokenElement = SAMLUtil.locateSamlAssertion(
                        assertionId, context.getSOAPMessage().getSOAPPart());
                if (!("true".equals((String)context.getExtraneousProperty(MessageConstants.SAML_SIG_RESOLVED))) ||
                        "false".equals((String)context.getExtraneousProperty(MessageConstants.SAML_SIG_RESOLVED))){
                    context.setExtraneousProperty(MessageConstants.SAML_SIG_RESOLVED,"false");
                }
            }
        }
        
        addAuthorityId(tokenElement , context);
        
        //TODO: expensive conversion happening here
        try {
            // if it is an Encrypted SAML Assertion we cannot decrypt it
            // on the client side since we don't have the Private Key
            if ("EncryptedData".equals(tokenElement.getLocalName())) {
                return null;
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0238_FAILED_RESOLVE_SAML_ASSERTION(), e);
            throw new XWSSecurityException(e);
        }
        
        return tokenElement;
    }
    
    private static void addAuthorityId(Element assertion , FilterProcessingContext fp){
        EncryptionPolicy ep = (EncryptionPolicy)fp.getInferredPolicy();
        if( ep != null){
            AuthenticationTokenPolicy.SAMLAssertionBinding kb = (AuthenticationTokenPolicy.SAMLAssertionBinding ) ep.newSAMLAssertionKeyBinding();
            String issuer = assertion.getAttribute("Issuer");
            kb.setAuthorityIdentifier(issuer);
        }
    }

  
    
    // this method would be called on incoming client/server messages
    @SuppressWarnings("unchecked")
    private static byte[] resolveSCT(
            FilterProcessingContext context, SecurityContextTokenImpl token, boolean sig)
            throws XWSSecurityException{
        
        // first set it into Extraneous Properties
        context.setExtraneousProperty(MessageConstants.INCOMING_SCT, token);
        // now get the SC ID
        String scId = token.getSCId();
        IssuedTokenContext ctx = null;        
        String protocol = context.getWSSCVersion(context.getSecurityPolicyVersion());
        if(context.isClient()){       
            SCTokenConfiguration config = new DefaultSCTokenConfiguration(protocol, scId, !context.isExpired(), !context.isInboundMessage());
            ctx =IssuedTokenManager.getInstance().createIssuedTokenContext(config, null);
            try{
                IssuedTokenManager.getInstance().getIssuedToken(ctx);
            }catch(WSTrustException e){
                throw new XWSSecurityException(e);
            }
            
            //Retrive the context from issuedTokenContextMap
//            Enumeration elements = context.getIssuedTokenContextMap().elements();
//            while (elements.hasMoreElements()) {
//                IssuedTokenContext ictx = (IssuedTokenContext)elements.nextElement();
//                Object tok = ictx.getSecurityToken();
//                String ctxid = null;
//                if (tok instanceof SecurityContextToken) {
//                    ctxid = ((SecurityContextToken)tok).getIdentifier().toString();
//                    if (ctxid.equals(scId)) {
//                        ctx = ictx;
//                        break;
//                    }
//                }
//                
//            }
        }else{
            //Retrive the context from Session Manager's cache
            ctx = ((SessionManager)context.getExtraneousProperty("SessionManager")).getSecurityContext(scId, !context.isExpired());
            java.net.URI sctId = null;
            String sctIns = null;
            String wsuId = null;
            SecurityContextToken sct = (SecurityContextToken)ctx.getSecurityToken();
            if (sct != null){
                sctId = sct.getIdentifier();
                sctIns = sct.getInstance();
                wsuId = sct.getWsuId();
            }else {
                SecurityContextTokenInfo sctInfo = ctx.getSecurityContextTokenInfo();
                sctId = java.net.URI.create(sctInfo.getIdentifier());
                sctIns = sctInfo.getInstance();
                wsuId = sctInfo.getExternalId();  
            }
            ctx.setSecurityToken(WSTrustElementFactory.newInstance(protocol).createSecurityContextToken(sctId, sctIns, wsuId));
        }       
                
        if (ctx == null) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0246_UNABLETO_LOCATE_SECURE_CONVERSATION_SESSION());
            throw new XWSSecurityException("Could not locate SecureConversation session for Id:" + scId);
        }                
        
        Subject subj = ctx.getRequestorSubject();
        if (subj != null) {
            // subj will be null if this is the client side execution
            if (context.getExtraneousProperty(MessageConstants.SCBOOTSTRAP_CRED_IN_SUBJ) == null) {
                //do it only once
                context.getSecurityEnvironment().updateOtherPartySubject(
                        SecurityUtil.getSubject(context.getExtraneousProperties()),subj);
                context.getExtraneousProperties().put(MessageConstants.SCBOOTSTRAP_CRED_IN_SUBJ, "true");
            }
        }
        
        byte[] proofKey = null;
        String instance = null;
        com.sun.xml.ws.security.SecurityContextToken scToken = (com.sun.xml.ws.security.SecurityContextToken)ctx.getSecurityToken();
        if (scToken != null){
            instance = scToken.getInstance();
        }else{
            instance = ctx.getSecurityContextTokenInfo().getInstance();
        }
        if(instance != null){
            if(context.isExpired()){
                proofKey = ctx.getProofKey();
            }else{
                SecurityContextTokenInfo sctInstanceInfo = ctx.getSecurityContextTokenInfo();
                proofKey = sctInstanceInfo.getInstanceSecret(scToken.getInstance());
            }
        }else{
            proofKey = ctx.getProofKey();
        }
        return proofKey;
    }
    
    private static Key resolveDKT(FilterProcessingContext context,
            DerivedKeyTokenHeaderBlock token) throws XWSSecurityException{
        
        //TODO: hardcoded for testing -- need to obtain this from somewhere
        String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
        if (context.getAlgorithmSuite() != null) {
            dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
        }
        //HashMap tokenCache = context.getTokenCache();
        SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
        
        EncryptionPolicy inferredEncryptionPolicy = null;
        boolean isWSITRecipient = (context.getMode()== FilterProcessingContext.WSDL_POLICY);
        try{
            if(isWSITRecipient){
                int i = context.getInferredSecurityPolicy().size() - 1;
                inferredEncryptionPolicy = (EncryptionPolicy)context.getInferredSecurityPolicy().get(i);
            }
        } catch(Exception e){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN());
            throw new XWSSecurityException(e);
        }
        
        
        SecurityTokenReference sectr = token.getDerivedKeyElement();
        if (sectr == null) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0248_NULL_STR());
            throw new XWSSecurityException("Invalid DerivedKey Token encountered, no STR found");
        }
        ReferenceElement refElement = sectr.getReference();
        Key encKey = null;
        byte[] secret = null;
        if (refElement instanceof DirectReference) {
            String uri = ((DirectReference) refElement).getURI();
            String valueType = ((DirectReference) refElement).getValueType();
            String wsuId = secureMsg.getIdFromFragmentRef(uri);
            SecurityToken secToken = SecurityUtil.locateBySCTId(context, wsuId);
            if (secToken == null) {
                secToken =resolveToken(wsuId,context,secureMsg);
                //workaround for case where Reference does not have ValueType
                if ((valueType == null) && (secToken instanceof EncryptedKeyToken)){
                    valueType = MessageConstants.EncryptedKey_NS;
                }
            }
            
            if(MessageConstants.EncryptedKey_NS.equals(valueType)){
                try{
                    Element cipherData = (Element)((EncryptedKeyToken)secToken).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                    String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                    byte[] decodedCipher = Base64.decode(cipherValue);
                    byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                    String encEkSha1 = Base64.encode(ekSha1);
                    context.setExtraneousProperty(MessageConstants.EK_SHA1_VALUE, encEkSha1);
                } catch(Exception e){
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT(), e);
                    throw new XWSSecurityException(e);
                }
                
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    }
                }
                
                KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)secToken).getKeyInfo();
                encKey = ((EncryptedKeyToken)secToken).getSecretKey(getKey(kiHB, false, context), dataEncAlgo);
                secret = encKey.getEncoded();
                context.setExtraneousProperty(MessageConstants.SECRET_KEY_VALUE, encKey);
            } else if (MessageConstants.SCT_VALUETYPE.equals(valueType) || MessageConstants.SCT_13_VALUETYPE.equals(valueType)) {
                if (secToken instanceof SecurityContextToken) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                        if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                        }
                    }
                    //handling for SecurityContext Token
                    secret = resolveSCT(context, (SecurityContextTokenImpl)secToken, false);
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0243_INVALID_VALUE_TYPE_NON_SCT_TOKEN());
                    throw new XWSSecurityException("Incorrect ValueType: " + MessageConstants.SCT_VALUETYPE + ", specified for a Non SCT Token");
                }
                
            } else if (null == valueType) {
                if (secToken instanceof SecurityContextToken) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                        if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                        }
                    }
                    //handling for SecurityContext Token
                    secret = resolveSCT(context, (SecurityContextTokenImpl)secToken, false);
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0249_UNSUPPORTED_TOKEN_TYPE_DKT());
                    throw new XWSSecurityException("Unsupported TokenType " + secToken + " under DerivedKeyToken");
                }
            } else{
                log.log(Level.SEVERE, LogStringsMessages.WSS_0249_UNSUPPORTED_TOKEN_TYPE_DKT());
                throw new XWSSecurityException("Unsupported TokenType " + secToken + " under DerivedKeyToken");
            }
        } else if (refElement instanceof KeyIdentifier) {
            KeyIdentifier keyId = (KeyIdentifier)refElement;
            if(MessageConstants.EncryptedKeyIdentifier_NS.equals(keyId.getValueType())){
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    }
                }
                
                String ekSha1RefValue = (String)context.getExtraneousProperty("EncryptedKeySHA1");
                Key secretKey = (Key)context.getExtraneousProperty("SecretKey");
                String keyRefValue = keyId.getReferenceValue();
                if(ekSha1RefValue != null && secretKey != null){
                    if(ekSha1RefValue.equals(keyRefValue)){
                        encKey = secretKey;
                        secret = encKey.getEncoded();
                    } else{
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE());
                        throw new XWSSecurityException("EncryptedKeySHA1 reference not correct");
                    }
                } else{
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE());
                    String message = "EncryptedKeySHA1 reference not correct";
                    throw new XWSSecurityException(message);
                }
            } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())
            || MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                    IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                    if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                    }
                }
                
                String asId = keyId.getReferenceValue();
                Element assertion = resolveSAMLToken(sectr, asId, context);
                encKey = resolveSamlAssertion(secureMsg,assertion, true,context, asId);
                if (context.hasIssuedToken() && encKey != null){
                    SecurityUtil.initInferredIssuedTokenContext(context, sectr, encKey);
                }
                secret = encKey.getEncoded();
            } else{
                log.log(Level.SEVERE, LogStringsMessages.WSS_0282_UNSUPPORTED_KEY_IDENTIFIER_REFERENCE_DKT());
                throw new XWSSecurityException("Unsupported KeyIdentifier Reference " + keyId + " under DerivedKeyToken");
            }
        } else{
            log.log(Level.SEVERE, LogStringsMessages.WSS_0283_UNSUPPORTED_REFERENCE_TYPE_DKT());
            throw new XWSSecurityException("Unsupported ReferenceType " + refElement + " under DerivedKeyToken");
        }
        long length = token.getLength();
        long offset = token.getOffset();
        byte[] nonce = token.getNonce();
        String label = token.getLabel();
        DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret, nonce, label);
        String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo);
        
        Key returnKey = null;
        try {
            returnKey = dkt.generateSymmetricKey(jceAlgo);
        } catch (InvalidKeyException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN());
            throw new XWSSecurityException(ex);
        } catch (UnsupportedEncodingException ex) {
            log.log(Level.SEVERE,  LogStringsMessages.WSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN());
            throw new XWSSecurityException(ex);
        } catch (NoSuchAlgorithmException ex) {
            log.log(Level.SEVERE,  LogStringsMessages.WSS_0247_FAILED_RESOLVE_DERIVED_KEY_TOKEN());
            throw new XWSSecurityException(ex);
        }
        return returnKey;
        
    }
    
    //TODO: called from KeySelector for Signature Related EncryptedKey Processing
    // clean this up later use a restructured processSecurityTokenReference instead
    @SuppressWarnings("unchecked")
    public static Key processSTR(KeyInfoHeaderBlock keyInfo,
            boolean sig, FilterProcessingContext context)throws XWSSecurityException{
        Key returnKey = null;
        HashMap tokenCache = context.getTokenCache();
        SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
        SecurityTokenReference str = keyInfo.getSecurityTokenReference(0);
        ReferenceElement refElement = str.getReference();
        SignaturePolicy policy = (SignaturePolicy)context.getInferredPolicy();
        SignaturePolicy inferredSignaturePolicy = null;
        boolean isWSITRecipient = (context.getMode()== FilterProcessingContext.WSDL_POLICY);
        try{
            if(isWSITRecipient){
                int i = context.getInferredSecurityPolicy().size() - 1;
                inferredSignaturePolicy = (SignaturePolicy)context.getInferredSecurityPolicy().get(i);
            }
        } catch(Exception e){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0250_FAILED_PROCESS_STR(), e);
            throw new XWSSecurityException(e);
        }
        // Do a case analysis based on the type of refElement.
        // X509 Token Profile supports 3 kinds of reference mechanisms.
        // Embedded Reference not considered.
        if (refElement instanceof KeyIdentifier) {
            KeyIdentifier keyId = (KeyIdentifier)refElement;
            
            if (MessageConstants.X509SubjectKeyIdentifier_NS.equals(keyId.getValueType()) ||
                    MessageConstants.X509v3SubjectKeyIdentifier_NS.equals(keyId.getValueType())) {
                if(policy != null){
                    AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                    keyBinding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                }
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                    x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    if(inferredKB == null){
                        inferredSignaturePolicy.setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                    }
                }
                if (sig) {
                    returnKey =
                            context.getSecurityEnvironment().getPublicKey(context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()));
                } else {
                    returnKey =
                            context.getSecurityEnvironment().getPrivateKey(context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()));
                }
                
            } else if (MessageConstants.ThumbPrintIdentifier_NS.equals(keyId.getValueType())) {
                if(policy != null){
                    AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                    keyBinding.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
                }
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setValueType(MessageConstants.ThumbPrintIdentifier_NS);
                    x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    if(inferredKB == null){
                        inferredSignaturePolicy.setKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                    }
                }
                if (sig) {
                    returnKey =
                            context.getSecurityEnvironment().getPublicKey(
                            context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()),
                            MessageConstants.THUMB_PRINT_TYPE
                            );
                } else {
                    returnKey =
                            context.getSecurityEnvironment().getPrivateKey(
                            context.getExtraneousProperties(),
                            getDecodedBase64EncodedData(keyId.getReferenceValue()),
                            MessageConstants.THUMB_PRINT_TYPE
                            );
                }
                
            } else if(MessageConstants.EncryptedKeyIdentifier_NS.equals(keyId.getValueType())){
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(inferredKB == null){
                        inferredSignaturePolicy.setKeyBinding(skBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    }
                }
                String ekSha1RefValue = (String)context.getExtraneousProperty("EncryptedKeySHA1");
                Key secretKey = (Key)context.getExtraneousProperty("SecretKey");
                String keyRefValue = keyId.getReferenceValue();
                if(ekSha1RefValue != null && secretKey != null){
                    if(ekSha1RefValue.equals(keyRefValue))
                        returnKey = secretKey;
                } else{
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0240_INVALID_ENCRYPTED_KEY_SHA_1_REFERENCE());
                    String message = "EncryptedKeySHA1 reference not correct";
                    throw new XWSSecurityException(message);
                }
                
            } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType()) ||
                    MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                // Its a SAML Assertion, retrieve the assertion
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                    keyBinding.setReferenceType(keyId.getValueType());
                }
                
                String assertionID = keyId.getDecodedReferenceValue();
                Element samlAssertion = resolveSAMLToken(str, assertionID,context);
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                    if(inferredKB == null){
                        if (context.hasIssuedToken()){
                            inferredSignaturePolicy.setKeyBinding(itkBinding);
                        }else{
                            inferredSignaturePolicy.setKeyBinding(new AuthenticationTokenPolicy.SAMLAssertionBinding());
                        }
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                    }
                }
                returnKey = resolveSamlAssertion(secureMsg,samlAssertion, sig,context, assertionID);
                if (context.hasIssuedToken() && returnKey != null){
                    SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
                }
                
            } else {
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                }
                Element samlAssertion = null;
                String assertionID = keyId.getDecodedReferenceValue();
                try{
                    samlAssertion = resolveSAMLToken(str, assertionID,context);
                }catch(Exception ex){
                    //ignore
                }
                if (samlAssertion != null) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                        if(inferredKB == null){
                            if (context.hasIssuedToken()){
                                inferredSignaturePolicy.setKeyBinding(itkBinding);
                            }else{
                                inferredSignaturePolicy.setKeyBinding(new AuthenticationTokenPolicy.SAMLAssertionBinding());
                            }
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                        }
                    }
                    returnKey = resolveSamlAssertion(secureMsg,samlAssertion, sig,context, assertionID);
                    if (context.hasIssuedToken() && returnKey != null){
                        SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
                    }
                } else {
                    // now assume its an X509Token
                    // Note: the code below assumes base64 EncodingType for X509 SKI
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                        x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                        }
                    }
                    if (sig) {
                        returnKey =
                                context.getSecurityEnvironment().getPublicKey(context.getExtraneousProperties(),
                                getDecodedBase64EncodedData(keyId.getReferenceValue()));
                    } else {
                        returnKey =
                                context.getSecurityEnvironment().getPrivateKey(context.getExtraneousProperties(),
                                getDecodedBase64EncodedData(keyId.getReferenceValue()));
                    }
                }
            }
        } else if (refElement instanceof DirectReference) {
            String uri = ((DirectReference) refElement).getURI();
            
            // will be only during verify.
            AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
            String valueType = ((DirectReference) refElement).getValueType();
            if (MessageConstants.DKT_VALUETYPE.equals(valueType) ||
                    MessageConstants.DKT_13_VALUETYPE.equals(valueType)){
                //TODO: this will work for now but need to handle this case here later
                valueType = null;
            }
            
            if(policy != null){
                keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                keyBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                keyBinding.setValueType(valueType);
            }
            
            if (MessageConstants.X509v3_NS.equals(valueType)||MessageConstants.X509v1_NS.equals(valueType)) {
                // its an X509 Token
                HashMap insertedX509Cache = context.getInsertedX509Cache();
                String wsuId = secureMsg.getIdFromFragmentRef(uri);
                X509SecurityToken token = (X509SecurityToken)insertedX509Cache.get(wsuId);
                if(token == null)
                    token =(X509SecurityToken)resolveToken(wsuId,context,secureMsg);
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                    x509Binding.setValueType(valueType);
                    if(inferredKB == null){
                        inferredSignaturePolicy.setKeyBinding(x509Binding);
                    } else  if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                    }
                }
                returnKey = resolveX509Token(secureMsg, token, sig,context);
                
            } else if(MessageConstants.EncryptedKey_NS.equals(valueType)){
                // Do default processing
                String wsuId = secureMsg.getIdFromFragmentRef(uri);
                SecurityToken token =resolveToken(wsuId,context,secureMsg);
                //TODO: STR is referring to EncryptedKey
                KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                
                //TODO: PLUGFEST Algorithm hardcoded for now
                //String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                // restore Backward compatibility
                String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                if (context.getAlgorithmSuite() != null) {
                    dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                }else{
                    if (context.getDataEncryptionAlgorithm() != null){
                        dataEncAlgo = context.getDataEncryptionAlgorithm();
                    }
                }
                try{
                    Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                    String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                    byte[] decodedCipher = Base64.decode(cipherValue);
                    byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                    String encEkSha1 = Base64.encode(ekSha1);
                    context.setExtraneousProperty(MessageConstants.EK_SHA1_VALUE, encEkSha1);
                } catch(Exception e){
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT(), e);
                    throw new XWSSecurityException(e);
                }
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(inferredKB == null){
                        inferredSignaturePolicy.setKeyBinding(skBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    }
                }
                returnKey = ((EncryptedKeyToken)token).getSecretKey(getKey(kiHB, sig, context), dataEncAlgo);
                context.setExtraneousProperty(MessageConstants.SECRET_KEY_VALUE, returnKey);
                
            } else if (MessageConstants.SCT_VALUETYPE.equals(valueType) || MessageConstants.SCT_13_VALUETYPE.equals(valueType)) {
                // could be wsuId or SCT Session Id
                String sctId = secureMsg.getIdFromFragmentRef(uri);
                SecurityToken token = (SecurityToken)tokenCache.get(sctId);
                
                if(token == null){
                    token = SecurityUtil.locateBySCTId(context, uri);
                    if (token == null) {
                        token = resolveToken(sctId, context, secureMsg);
                    }
                    if(token == null){
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0242_UNABLETO_LOCATE_SCT());
                        throw new XWSSecurityException("SCT Token with Id "+sctId+ "not found");
                    }else{
                        tokenCache.put(sctId, token);
                    }
                }
                
                if (token instanceof SecurityContextToken) {
                    //handling for SecurityContext Token
                    byte[] proofKey = resolveSCT(context, (SecurityContextTokenImpl)token, sig);
                    String encAlgo = "AES"; //hardcoding for now
                    if (context.getAlgorithmSuite() != null) {
                        encAlgo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                    }
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(sctBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                        }
                    }
                    returnKey = new SecretKeySpec(proofKey, encAlgo);
                    
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0243_INVALID_VALUE_TYPE_NON_SCT_TOKEN());
                    throw new XWSSecurityException("Incorrect ValueType: " + MessageConstants.SCT_VALUETYPE + ", specified for a Non SCT Token");
                }
                
            } else if (null == valueType) {
                // Do default processing
                String wsuId = secureMsg.getIdFromFragmentRef(uri);
                SecurityToken token = SecurityUtil.locateBySCTId(context, wsuId);
                if (token == null) {
                    token =resolveToken(wsuId,context,secureMsg);
                }
                if (token instanceof X509SecurityToken) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                        }
                    }
                    returnKey = resolveX509Token(secureMsg, (X509SecurityToken)token, sig,context);
                } else if (token instanceof EncryptedKeyToken) {
                    //TODO: STR is referring to EncryptedKey
                    KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                    SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                    ReferenceElement refElem = sectr.getReference();
                    String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                    //restoring BC
                    //String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                    if (context.getAlgorithmSuite() != null) {
                        dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                    }else{
                        if (context.getDataEncryptionAlgorithm() != null){
                            dataEncAlgo = context.getDataEncryptionAlgorithm();
                        }
                    }
                    try{
                        Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.setExtraneousProperty(MessageConstants.EK_SHA1_VALUE, encEkSha1);
                    } catch(Exception e){
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0241_UNABLETO_SET_EKSHA_1_ON_CONTEXT(), e);
                        throw new XWSSecurityException(e);
                    }
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        skBinding.setKeyBinding(x509Binding);
                        //TODO: ReferenceType and ValueType not set on X509Binding
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(skBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                        }
                    }
                    returnKey = ((EncryptedKeyToken)token).getSecretKey(getKey(kiHB, sig, context), dataEncAlgo);
                    context.setExtraneousProperty(MessageConstants.SECRET_KEY_VALUE, returnKey);
                    
                } else if (token instanceof SecurityContextToken) {
                    //handling for SecurityContext Token
                    byte[] proofKey = resolveSCT(context, (SecurityContextTokenImpl)token, sig);
                    String encAlgo = "AES"; //default algo
                    if (context.getAlgorithmSuite() != null) {
                        encAlgo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                    }
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(sctBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                        }
                    }
                    returnKey = new SecretKeySpec(proofKey, encAlgo);
                    
                } else if (token instanceof DerivedKeyTokenHeaderBlock){
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        DerivedTokenKeyBinding dtkBinding = new DerivedTokenKeyBinding();
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(dtkBinding);
                        } else{
                            log.log(Level.SEVERE, LogStringsMessages.WSS_0244_INVALID_LEVEL_DKT());
                            throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                        }
                    }
                    returnKey = resolveDKT(context, (DerivedKeyTokenHeaderBlock)token);
                } else {
                    String message = " Cannot Resolve URI " + uri;
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(message),
                            new Object[] {message});
                    XWSSecurityException xwsse = new XWSSecurityException(message);
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                            xwsse.getMessage(), xwsse);
                }
            } else {
                log.log(
                        Level.SEVERE,
                        LogStringsMessages.WSS_0337_UNSUPPORTED_DIRECTREF_MECHANISM(((DirectReference)refElement).getValueType()),
                        new Object[] {((DirectReference)refElement).getValueType()});
                XWSSecurityException xwsse =
                        new XWSSecurityException(
                        "unsupported directreference ValueType "
                        + ((DirectReference) refElement).getValueType());
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        xwsse.getMessage(), xwsse);
            }
        } else if (refElement instanceof X509IssuerSerial) {
            BigInteger serialNumber = ((X509IssuerSerial) refElement).getSerialNumber();
            String issuerName = ((X509IssuerSerial) refElement).getIssuerName();
            
            if(isWSITRecipient){
                MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                x509Binding.setReferenceType(MessageConstants.X509_ISSUER_TYPE);
                if(inferredKB == null){
                    inferredSignaturePolicy.setKeyBinding(x509Binding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                }
            }
            if (sig) {
                returnKey = context.getSecurityEnvironment().getPublicKey(
                        context.getExtraneousProperties(), serialNumber, issuerName);
            } else {
                returnKey = context.getSecurityEnvironment().getPrivateKey(
                        context.getExtraneousProperties(), serialNumber, issuerName);
            }
        } else {
            log.log(
                    Level.SEVERE, LogStringsMessages.WSS_0338_UNSUPPORTED_REFERENCE_MECHANISM());
            XWSSecurityException xwsse =
                    new XWSSecurityException(
                    "Key reference mechanism not supported");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_UNSUPPORTED_SECURITY_TOKEN,
                    xwsse.getMessage(),
                    xwsse);
        }
        return returnKey;
    }
}
