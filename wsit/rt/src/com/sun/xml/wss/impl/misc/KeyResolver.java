/*
 * $Id: KeyResolver.java,v 1.2.2.1 2006-06-28 14:11:14 ashutoshshahi Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.wss.impl.misc;


import com.sun.xml.wss.core.EncryptedKeyToken;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.impl.dsig.SignatureProcessor;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.saml.AssertionUtil;

import java.security.Key;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;

import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.SecurityEnvironment;
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

import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.util.SAMLUtil;
import com.sun.xml.wss.impl.XMLUtil;

import com.sun.xml.wss.impl.resolver.URIResolver;

import java.util.HashMap;
import java.util.Enumeration;
import javax.xml.soap.SOAPElement;
import javax.crypto.spec.SecretKeySpec;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.namespace.QName;

import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.security.DerivedKeyToken;

import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;

import com.sun.xml.wss.impl.misc.SecurityUtil;

import com.sun.xml.ws.security.trust.elements.BinarySecret;


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
        HashMap tokenCache = context.getTokenCache();
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
                    throw new XWSSecurityException("Unsupported reference type under EncryptedKey");
                }
                //Default algo
                String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                if (context.getAlgorithmSuite() != null) {
                    dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
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
                    log.log(Level.SEVERE, "WSS0339.unsupported.keyinfo");
                    throw new XWSSecurityException("Unsupported wst:BinarySecret Type");
                }

            } else {
                log.log(Level.SEVERE, "WSS0339.unsupported.keyinfo");
                XWSSecurityException xwsse =
                        new XWSSecurityException(
                        "Support for processing information in the given ds:KeyInfo is not present");
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY,
                        xwsse.getMessage(),
                        xwsse);
            }
        }catch(WssSoapFaultException wsse){
            throw wsse;
        }catch (XWSSecurityException xwsse) {
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                    xwsse.getMessage(),
                    xwsse);
        }
        
        if (returnKey == null) {
            log.log(Level.SEVERE,
                    "WSS0600.illegal.token.reference");
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
    
    
    public static Key resolveSamlAssertion(SecurableSoapMessage secureMsg, Assertion samlAssertion,
            boolean sig,FilterProcessingContext context, String assertionID) throws XWSSecurityException {
        
        try {
             Key key = (Key) context.getSamlIdVSKeyCache().get(assertionID);
             if (key != null)
                 return key;
            //TODO: expensive conversion happening
            Element keyInfoElem =
                    AssertionUtil.getSubjectConfirmationKeyInfo(samlAssertion.toElement(null));
            KeyInfoHeaderBlock keyInfo = new KeyInfoHeaderBlock(
                XMLUtil.convertToSoapElement(secureMsg.getSOAPPart(),keyInfoElem));
            key = getKey(keyInfo, sig, context);
            context.getSamlIdVSKeyCache().put(assertionID, key);
            return key;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }
    
    
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
                    throw new XWSSecurityException(message);
                 }
                
            } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                // Its a SAML Assertion, retrieve the assertion
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                    keyBinding.setReferenceType(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE);
                }
                String assertionID = keyId.getDecodedReferenceValue();
                Assertion samlAssertion = resolveSAMLToken(str, assertionID,context);
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
                SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
                
            } else {
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                }
                Assertion samlAssertion = null;
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
                    SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
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
            if (MessageConstants.DKT_VALUETYPE.equals(valueType)){
                //TODO: this will work for now but need to handle this case here later
                valueType = null;
            }
            
            if(policy != null){
                keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                keyBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                keyBinding.setValueType(valueType);
            }
            
            if (MessageConstants.X509v3_NS.equals(valueType)) {
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
                    x509Binding.setValueType(MessageConstants.X509v3_NS);
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
                
                //TODO: PLUGFEST Algorithm hardcoded for now
                String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                if (context.getAlgorithmSuite() != null) {
                    dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                }
                try{
                    Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                    String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                    byte[] decodedCipher = Base64.decode(cipherValue);
                    byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                    String encEkSha1 = Base64.encode(ekSha1);
                    context.getSecurityEnvironment().updateOtherPartySubject(
                            DefaultSecurityEnvironmentImpl.getSubject(context), encEkSha1); 
                } catch(Exception e){
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
                context.getSecurityEnvironment().updateOtherPartySubject(
                        DefaultSecurityEnvironmentImpl.getSubject(context), returnKey);
                
            } else if (MessageConstants.SCT_VALUETYPE.equals(valueType)) {
                    // could be wsuId or SCT Session Id
                    String sctId = secureMsg.getIdFromFragmentRef(uri);
                    SecurityToken token = (SecurityToken)tokenCache.get(sctId);
                                                                                                                                                     
                    if(token == null){
                        token = SecurityUtil.locateBySCTId(context, uri);
                        if (token == null) {
                            token = resolveToken(sctId, context, secureMsg);
                        }
                        if(token == null){
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
                    returnKey = resolveX509Token(secureMsg, (X509SecurityToken)token, sig,context);
                } else if (token instanceof EncryptedKeyToken) {
                    //TODO: STR is referring to EncryptedKey
                    KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                    SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                    ReferenceElement refElem = sectr.getReference();
                    //String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                    //TODO: PLUGFEST Algorithm hardcoded for now
                    String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                    if (context.getAlgorithmSuite() != null) {
                        dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                    }
                    try{
                        Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context), encEkSha1); 
                    } catch(Exception e){
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
                    context.getSecurityEnvironment().updateOtherPartySubject(
                            DefaultSecurityEnvironmentImpl.getSubject(context), returnKey);  

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
                              throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                          }
                      }                      
                    returnKey = resolveDKT(context, (DerivedKeyTokenHeaderBlock)token);
                } else {
                    String message = " Cannot Resolve URI " + uri;
                    log.log(Level.SEVERE,"WSS0337.unsupported.directref.mechanism",
                            new Object[] {message});
                            XWSSecurityException xwsse = new XWSSecurityException(message);
                            throw SecurableSoapMessage.newSOAPFaultException(
                                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                                    xwsse.getMessage(), xwsse);
                }
            } else {
                log.log(
                        Level.SEVERE,
                        "WSS0337.unsupported.directref.mechanism",
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
                    Level.SEVERE, "WSS0338.unsupported.reference.mechanism");
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
                    "WSS0601.illegal.key.value",
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
                log.log(Level.SEVERE, "WSS0339.unsupported.keyinfo");
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
                log.log(Level.SEVERE, "WSS0339.unsupported.keyinfo");
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
                    "WSS0602.illegal.x509.data",
                    e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    
    private static byte[] getDecodedBase64EncodedData(String encodedData)
    throws XWSSecurityException {
        try {
            return Base64.decode(encodedData);
        } catch (Base64DecodingException e) {
            throw new XWSSecurityException(
                    "Unable to decode Base64 encoded data",
                    e);
        }
    }
    
    
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
            throw new XWSSecurityException(ex);
        }
    }
    
    private static Assertion resolveSAMLToken(SecurityTokenReference tokenRef, String assertionId,
            FilterProcessingContext context)throws XWSSecurityException {
        
        Assertion ret = (Assertion)context.getTokenCache().get(assertionId);
        if (ret != null)
            return ret;
        
        Element tokenElement = context.getIssuedSAMLToken();
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
            }
        }
        
        addAuthorityId(tokenElement , context);
        
        //TODO: expensive conversion happening here
        try {
            ret = AssertionUtil.fromElement(tokenElement);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        context.getTokenCache().put(assertionId, ret);
        return ret;
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
    private static byte[] resolveSCT(
        FilterProcessingContext context, SecurityContextTokenImpl token, boolean sig)
        throws XWSSecurityException {
                                                                                                              
        // first set it into Extraneous Properties
        context.setExtraneousProperty(MessageConstants.INCOMING_SCT, token);
        // now get the SC ID
        String scId = token.getSCId();
                                                                                                              
        IssuedTokenContext ctx = context.getIssuedTokenContext(scId);
        if (ctx == null) {
            // this can happen on client side where the map is based on tokenId
            Enumeration elements = context.getIssuedTokenContextMap().elements();
            while (elements.hasMoreElements()) {
                IssuedTokenContext ictx = (IssuedTokenContext)elements.nextElement();
                Object tok = ictx.getSecurityToken();
                String ctxid = null;
                if (tok instanceof SecurityContextToken) {
                    ctxid = ((SecurityContextToken)tok).getIdentifier().toString();
                    if (ctxid.equals(scId)) {
                        ctx = ictx;
                        break;
                    }
                }
                
            }
        }
                                                                                                              
        if (ctx == null) {
            throw new XWSSecurityException("Could not locate SecureConversation session for Id:" + scId);
        }
                                                                                                              
        byte[] proofKey = ctx.getProofKey();
        return proofKey;
    }
    
    private static Key resolveDKT(FilterProcessingContext context, 
            DerivedKeyTokenHeaderBlock token) throws XWSSecurityException{
        
        //TODO: hardcoded for testing -- need to obtain this from somewhere
        String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
        if (context.getAlgorithmSuite() != null) {
            dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
        }
        HashMap tokenCache = context.getTokenCache();
        SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
        
        EncryptionPolicy inferredEncryptionPolicy = null;
        boolean isWSITRecipient = (context.getMode()== FilterProcessingContext.WSDL_POLICY);
        try{
            if(isWSITRecipient){
                int i = context.getInferredSecurityPolicy().size() - 1;
                inferredEncryptionPolicy = (EncryptionPolicy)context.getInferredSecurityPolicy().get(i);
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        
        try{
            SecurityTokenReference sectr = token.getDerivedKeyElement();
            if (sectr == null) {
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
                }
                if(MessageConstants.EncryptedKey_NS.equals(valueType)){
                    try{
                        Element cipherData = (Element)((EncryptedKeyToken)secToken).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context), encEkSha1); 
                    } catch(Exception e){
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
                    context.getSecurityEnvironment().updateOtherPartySubject(
                            DefaultSecurityEnvironmentImpl.getSubject(context), encKey);
                } else if (MessageConstants.SCT_VALUETYPE.equals(valueType)) {
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
                        throw new XWSSecurityException("Incorrect ValueType: " + MessageConstants.SCT_VALUETYPE + ", specified for a Non SCT Token");                    }

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
                        throw new XWSSecurityException("Unsupported TokenType " + token + " under DerivedKeyToken");
                    }
                } else{
                    throw new XWSSecurityException("Unsupported TokenType " + token + " under DerivedKeyToken");
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
                            throw new XWSSecurityException("EncryptedKeySHA1 reference not correct");
                        }
                     } else{
                        String message = "EncryptedKeySHA1 reference not correct";                    
                        throw new XWSSecurityException(message);
                     }                    
                } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                    if(isWSITRecipient){
                        MLSPolicy inferredKB = inferredEncryptionPolicy.getKeyBinding();
                        IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                        if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                        }
                    }
                    
                    String asId = keyId.getReferenceValue();
                    Assertion assertion = resolveSAMLToken(sectr, asId, context);
                    encKey = resolveSamlAssertion(secureMsg,assertion, true,context, asId);
                    SecurityUtil.initInferredIssuedTokenContext(context, sectr, encKey);
                    secret = encKey.getEncoded();
                } else{
                    throw new XWSSecurityException("Unsupported TokenType " + token + " under DerivedKeyToken"); 
                }
           } else{
               throw new XWSSecurityException("Unsupported TokenType " + token + " under DerivedKeyToken"); 
           }
           long length = token.getLength();
           long offset = token.getOffset();
           byte[] nonce = token.getNonce();
           DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret, nonce);
           String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo);
           Key returnKey = dkt.generateSymmetricKey(jceAlgo);
           return returnKey;
        } catch (Exception e){
            throw new XWSSecurityException(e);
        }
    }

    //TODO: called from KeySelector for Signature Related EncryptedKey Processing
    // clean this up later use a restructured processSecurityTokenReference instead
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
                    String message = "EncryptedKeySHA1 reference not correct";                    
                    throw new XWSSecurityException(message);
                 }
                
            } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                // Its a SAML Assertion, retrieve the assertion
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                    keyBinding.setReferenceType(MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE);
                }
                String assertionID = keyId.getDecodedReferenceValue();
                Assertion samlAssertion = resolveSAMLToken(str, assertionID,context);
                if(isWSITRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                    if(inferredKB == null){  
                        inferredSignaturePolicy.setKeyBinding(itkBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                    }
                }                
                returnKey = resolveSamlAssertion(secureMsg,samlAssertion, sig,context, assertionID);
                SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
                
            } else {
                if(policy != null){
                    AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
                    keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
                }
                Assertion samlAssertion = null;
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
                            inferredSignaturePolicy.setKeyBinding(itkBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                        }
                    }                    
                    returnKey = resolveSamlAssertion(secureMsg,samlAssertion, sig,context, assertionID);
                    SecurityUtil.initInferredIssuedTokenContext(context,str, returnKey);
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
            if (MessageConstants.DKT_VALUETYPE.equals(valueType)){
                //TODO: this will work for now but need to handle this case here later
                valueType = null;
            }
            
            if(policy != null){
                keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
                keyBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                keyBinding.setValueType(valueType);
            }
            
            if (MessageConstants.X509v3_NS.equals(valueType)) {
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
                    x509Binding.setValueType(MessageConstants.X509v3_NS);
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
                String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                if (context.getAlgorithmSuite() != null) {
                    dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                }
                try{
                    Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                    String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                    byte[] decodedCipher = Base64.decode(cipherValue);
                    byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                    String encEkSha1 = Base64.encode(ekSha1);
                    context.getSecurityEnvironment().updateOtherPartySubject(
                            DefaultSecurityEnvironmentImpl.getSubject(context), encEkSha1); 
                } catch(Exception e){
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
                context.getSecurityEnvironment().updateOtherPartySubject(
                        DefaultSecurityEnvironmentImpl.getSubject(context), returnKey);
                
            } else if (MessageConstants.SCT_VALUETYPE.equals(valueType)) {
                    // could be wsuId or SCT Session Id
                    String sctId = secureMsg.getIdFromFragmentRef(uri);
                    SecurityToken token = (SecurityToken)tokenCache.get(sctId);
                                                                                                                                                     
                    if(token == null){
                        token = SecurityUtil.locateBySCTId(context, uri);
                        if (token == null) {
                            token = resolveToken(sctId, context, secureMsg);
                        }
                        if(token == null){
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
                    //String dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                    //TODO: PLUGFEST Algorithm hardcoded for now
                    String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                    if (context.getAlgorithmSuite() != null) {
                        dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                    }
                    try{
                        Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                        String encEkSha1 = Base64.encode(ekSha1);
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context), encEkSha1); 
                    } catch(Exception e){
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
                    context.getSecurityEnvironment().updateOtherPartySubject(
                            DefaultSecurityEnvironmentImpl.getSubject(context), returnKey);  

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
                              throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                          }
                      }                      
                    returnKey = resolveDKT(context, (DerivedKeyTokenHeaderBlock)token);
                } else {
                    String message = " Cannot Resolve URI " + uri;
                    log.log(Level.SEVERE,"WSS0337.unsupported.directref.mechanism",
                            new Object[] {message});
                            XWSSecurityException xwsse = new XWSSecurityException(message);
                            throw SecurableSoapMessage.newSOAPFaultException(
                                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                                    xwsse.getMessage(), xwsse);
                }
            } else {
                log.log(
                        Level.SEVERE,
                        "WSS0337.unsupported.directref.mechanism",
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
                    Level.SEVERE, "WSS0338.unsupported.reference.mechanism");
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
