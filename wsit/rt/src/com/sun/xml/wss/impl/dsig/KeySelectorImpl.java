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

/**
 * KeySelectorImpl.java
 *
 * Created on February 25, 2005, 4:36 PM
 */

package com.sun.xml.wss.impl.dsig;

import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.saml.AssertionUtil;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.core.ReferenceElement;
import com.sun.xml.wss.core.SecurityToken;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.wss.core.reference.DirectReference;
import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.core.EncryptedKeyToken;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.core.DerivedKeyTokenHeaderBlock;
import com.sun.xml.ws.security.impl.DerivedKeyTokenImpl;
import com.sun.xml.ws.security.DerivedKeyToken;

import com.sun.xml.ws.security.IssuedTokenContext;

import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.Base64;

import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.util.SAMLUtil;

import java.math.BigInteger;

import java.security.Key;
import java.security.KeyException;
import java.security.PublicKey;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Enumeration;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.KeySelector.Purpose;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;

import javax.xml.soap.SOAPElement;
import javax.xml.namespace.QName;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.PolicyTypeUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.crypto.spec.SecretKeySpec;

import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.ws.security.policy.AlgorithmSuite;

import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;

import com.sun.xml.wss.impl.misc.KeyResolver;



/**
 * Implementation of JSR 105 KeySelector interface.
 * Supports resolving Key information from
 * SecurityTokenReference elements, KeyName,
 * X509Data and KeyValue.
 * @author K.Venugopal@sun.com,XWS-Security team
 */
public class KeySelectorImpl extends KeySelector{
    private static KeySelectorImpl keyResolver = null;
    private static Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    
    /** Creates a new instance of KeySelectorImpl */
    static{
        keyResolver = new KeySelectorImpl();
    }
    private KeySelectorImpl() {
        
    }
    
    /**
     *
     * @return
     */
    public static KeySelector getInstance(){
        return keyResolver;
    }
    
    /**
     *
     * @param keyInfo
     * @param purpose
     * @param method
     * @param context
     * @throws KeySelectorException
     * @return
     */
    public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
        if (keyInfo == null) {
            if(logger.getLevel() == Level.SEVERE){
                logger.log(Level.SEVERE,"WSS1317.keyinfo.null");
            }
            throw new KeySelectorException("Null KeyInfo object!");
        }
        
        
        if(MessageConstants.debug){
            logger.log(Level.FINEST, "KeySelectorResult::select Purpose =  "+purpose);
            logger.log(Level.FINEST, "KeySelectorResult::select Algorithm is "+method.getAlgorithm());
            logger.log(Level.FINEST, "KeySelectorResult::select ParameterSpec is "+method.getParameterSpec());
        }
        try{
            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent();
            FilterProcessingContext wssContext = (FilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
            
            SecurityPolicy securityPolicy = wssContext.getSecurityPolicy();
            boolean isBSP = false;
            if(securityPolicy != null) {
                if (PolicyTypeUtil.messagePolicy(securityPolicy)) {
                    isBSP = ((MessagePolicy)securityPolicy).isBSP();
                } else {
                    isBSP = ((WSSPolicy)securityPolicy).isBSP();
                }
            }
            
            if (isBSP && list.size() > 1) {
                logger.log(Level.SEVERE, "BSP Violation of R5402: KeyInfo MUST have exactly one child");
                throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "BSP Violation of R5402: KeyInfo MUST have exactly one child", null);
            }
            
            boolean isStr = false;
            
            for (int i = 0; i < list.size(); i++) {
                XMLStructure xmlStructure = (XMLStructure) list.get(i);
                if (xmlStructure instanceof KeyValue) {
                    PublicKey pk = null;
                    try {
                        pk = ((KeyValue)xmlStructure).getPublicKey();
                    } catch (KeyException ke) {
                        throw new KeySelectorException(ke);
                    }
                    // make sure algorithm is compatible with method
                    if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        return new SimpleKeySelectorResult(pk);
                    }
                } else if(xmlStructure instanceof DOMStructure) {
                    SOAPElement reference = (SOAPElement)((DOMStructure)xmlStructure).getNode();
                    if(isSecurityTokenReference(reference)){
                        isStr = true;
                        final Key key = resolve(reference, context, purpose);
                        return new KeySelectorResult(){
                            public Key getKey(){
                                return key;
                            }
                        };
                    }
                }else if (xmlStructure instanceof KeyName) {
                    KeyName keyName = (KeyName) xmlStructure;
                    Key returnKey = wssContext.getSecurityEnvironment().getSecretKey(
                            wssContext.getExtraneousProperties(),keyName.getName(),false);
                    if(returnKey == null){
                        X509Certificate cert = wssContext.getSecurityEnvironment().getCertificate(
                                wssContext.getExtraneousProperties(),keyName.getName(), false);
                        if (cert != null && algEquals(sm.getAlgorithm(),cert.getPublicKey().getAlgorithm())) {
                            return new SimpleKeySelectorResult(cert.getPublicKey());
                        }
                    }else{
                        return new SimpleKeySelectorResult(returnKey);
                    }
                }else if (xmlStructure instanceof X509Data){
                    Key key = resolveX509Data(wssContext, (X509Data)xmlStructure, purpose);
                    return new SimpleKeySelectorResult(key);
                }
            }
            
            if (isBSP && !isStr) {
                logger.log(Level.SEVERE, "BSP Violation of R5409: Child element of KeyInfo MUST be a STR element");
                throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "BSP Violation of R5409: Child element of KeyInfo MUST be a STR element", null);
            }
            
        }catch(KeySelectorException kse){
            throw kse;
        }catch(Exception ex){
            logger.log(Level.FINEST,"Error occurred while resolving keyinformation" +
                    ex.getMessage());
            throw new KeySelectorException(ex);
        }
        throw new KeySelectorException("No KeyValue element found!");
    }
    
    //@@@FIXME: this should also work for key types other than DSA/RSA
    /**
     *
     * @param algURI
     * @param algName
     * @return
     */
    static boolean algEquals(String algURI, String algName) {
        if (algName.equalsIgnoreCase("DSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
            return true;
        } else if (algName.equalsIgnoreCase("RSA") &&
                algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private Key pk;
        SimpleKeySelectorResult(Key pk) {
            this.pk = pk;
        }
        public Key getKey() { return pk; }
    }
    
    private static Key resolve(SOAPElement securityTokenReference,XMLCryptoContext context, Purpose purpose)throws KeySelectorException {
        try{
            FilterProcessingContext wssContext = (FilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
            SecurableSoapMessage secureMsg = wssContext.getSecurableSoapMessage();
            AlgorithmSuite algSuite = wssContext.getAlgorithmSuite();
            String encAlgo = null;
            boolean isPolicyRecipient = (wssContext.getMode()== FilterProcessingContext.WSDL_POLICY);
            if(algSuite != null){
                encAlgo = algSuite.getEncryptionAlgorithm();
            }
            
            SecurityPolicy securityPolicy = wssContext.getSecurityPolicy();
            boolean isBSP = false;
            if(securityPolicy != null) {
                if (PolicyTypeUtil.messagePolicy(securityPolicy)) {
                    isBSP = ((MessagePolicy)securityPolicy).isBSP();
                } else {
                    isBSP = ((WSSPolicy)securityPolicy).isBSP();
                }
            }
            
            SecurityTokenReference str = new SecurityTokenReference(securityTokenReference, isBSP);
            ReferenceElement refElement = str.getReference();
            HashMap tokenCache = wssContext.getTokenCache();
            HashMap insertedX509Cache = wssContext.getInsertedX509Cache();
            SignaturePolicy policy = (SignaturePolicy)wssContext.getInferredPolicy();
            SignaturePolicy inferredSignaturePolicy = null;
            if(isPolicyRecipient){
                int i = wssContext.getInferredSecurityPolicy().size() - 1;
                inferredSignaturePolicy = (SignaturePolicy)wssContext.getInferredSecurityPolicy().get(i);
            }  
            // will be only during verify.
            AuthenticationTokenPolicy.X509CertificateBinding keyBinding = null;
            
            //TODO: what is this case here
            if(policy != null) {
                keyBinding = (AuthenticationTokenPolicy.X509CertificateBinding) policy.newX509CertificateKeyBinding();
            }
            
            Key returnKey = null;
            if (refElement instanceof KeyIdentifier) {
                KeyIdentifier keyId = (KeyIdentifier)refElement;
                if(keyBinding != null){
                    keyBinding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    keyBinding.setValueType(keyId.getValueType());
                }
                
                
                if (MessageConstants.X509SubjectKeyIdentifier_NS.equals(keyId.getValueType()) ||
                        MessageConstants.X509v3SubjectKeyIdentifier_NS.equals(keyId.getValueType())) {
                    if(isPolicyRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                        x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        if(inferredKB == null){                           
                            inferredSignaturePolicy.setKeyBinding(x509Binding);
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
                    if (purpose == Purpose.VERIFY) {
                        byte[] keyIdBytes = XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue());
                        wssContext.setExtraneousProperty(MessageConstants.REQUESTER_KEYID, new String(keyIdBytes));
                        returnKey = wssContext.getSecurityEnvironment().getPublicKey(
                                wssContext.getExtraneousProperties(),keyIdBytes);
                    } else if(purpose == Purpose.SIGN){
                        returnKey =wssContext.getSecurityEnvironment().getPrivateKey(
                                wssContext.getExtraneousProperties(),
                                XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue()));
                    }
                    
                    
                } else if (MessageConstants.ThumbPrintIdentifier_NS.equals(keyId.getValueType())) {
                    if(isPolicyRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setValueType(MessageConstants.ThumbPrintIdentifier_NS);
                        x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        if(inferredKB == null){                           
                            inferredSignaturePolicy.setKeyBinding(x509Binding);
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
                    if (purpose == Purpose.VERIFY) {
                        byte[] keyIdBytes = XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue());
                        wssContext.setExtraneousProperty(MessageConstants.REQUESTER_KEYID, new String(keyIdBytes));
                        returnKey = wssContext.getSecurityEnvironment().getPublicKey(
                                wssContext.getExtraneousProperties(),keyIdBytes, MessageConstants.THUMB_PRINT_TYPE);
                    } else if(purpose == Purpose.SIGN){
                        returnKey =wssContext.getSecurityEnvironment().getPrivateKey(
                                wssContext.getExtraneousProperties(),
                                XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue()), MessageConstants.THUMB_PRINT_TYPE);
                    }
                    
                }else if (MessageConstants.EncryptedKeyIdentifier_NS.equals(keyId.getValueType())){
                    if(isPolicyRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        skBinding.setKeyBinding(x509Binding);
                        if(inferredKB == null){
                            inferredSignaturePolicy.setKeyBinding(skBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                        }
                    }
                    //Set return key here
                    String ekSha1RefValue = (String)wssContext.getExtraneousProperty("EncryptedKeySHA1");
                    Key secretKey = (Key)wssContext.getExtraneousProperty("SecretKey");
                    String keyRefValue = keyId.getReferenceValue();
                    if(ekSha1RefValue != null && secretKey != null){
                        if(ekSha1RefValue.equals(keyRefValue))
                            returnKey = secretKey;
                    }else{
                        String message = "EncryptedKeySHA1 reference not correct";
                        logger.log(Level.SEVERE,"WSS1306:unsupported.KeyIdentifier.Reference.Type.encountered", new Object[] {message});
                        throw new KeySelectorException(message);
                    }
                    //returnKey = null; 
                } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(keyId.getValueType())) {
                    
                    String assertionID = keyId.getReferenceValue();
                    Element tokenElement = wssContext.getIssuedSAMLToken();
                    if (tokenElement == null) {
                        Assertion samlAssertion = (Assertion)tokenCache.get(assertionID);
                        if (samlAssertion == null) {
                            if (str.getSamlAuthorityBinding() != null) {
                                tokenElement = wssContext.getSecurityEnvironment().
                                        locateSAMLAssertion(
                                        wssContext.getExtraneousProperties(), str.getSamlAuthorityBinding(), assertionID, secureMsg.getSOAPPart());
                            } else {
                                tokenElement = SAMLUtil.locateSamlAssertion(assertionID,secureMsg.getSOAPPart());
                            }
                        } else {
                            try {
                                tokenElement = samlAssertion.toElement(null);
                            } catch (Exception e) {
                                logger.log(Level.FINEST,"Error occurred while trying " +
                                        "to resolve SAML assertion"+e.getMessage());
                                throw new KeySelectorException(e);
                            }
                        }
                   }
                    if(isPolicyRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                        if(inferredKB == null){   
                            inferredSignaturePolicy.setKeyBinding(itkBinding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                        }
                    }                    
                    returnKey = resolveSamlAssertion(context,tokenElement, purpose, assertionID);
                    addAuthorityId(tokenElement,wssContext);
                    SecurityUtil.initInferredIssuedTokenContext(wssContext, str, returnKey);

                } else {
                    
                    // it could be SAML AssertionID without ValueType on KeyIdentifier
                    String assertionID = keyId.getDecodedReferenceValue();
                    Element samlAssertion = null;
                    try {
                        samlAssertion = resolveSAMLToken(str, assertionID, wssContext);
                    } catch (Exception e) {
                        //ignore
                        logger.log(Level.FINEST,"Error occurred while trying " +
                                "to resolve SAML assertion"+e.getMessage());
                    }
                    
                    if (samlAssertion != null) {
                        if(isPolicyRecipient){
                            MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                            IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                            if(inferredKB == null){   
                                inferredSignaturePolicy.setKeyBinding(itkBinding);
                            } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                                if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                                    ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                            }
                        }                        
                        returnKey = resolveSamlAssertion(context,samlAssertion, purpose, assertionID);
                        addAuthorityId(samlAssertion,wssContext);

                        //whenever we have SAML we want to record the proofkey and str
                        SecurityUtil.initInferredIssuedTokenContext(wssContext, str, returnKey);                        
                    } else {
                        // now assume its an X509Token
                        // Note: the code below assumes base64 EncodingType for X509 SKI
                        if(isPolicyRecipient){
                            MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                            AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                            x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                            x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                            if(inferredKB == null){                           
                                inferredSignaturePolicy.setKeyBinding(x509Binding);
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
                        if (purpose == Purpose.VERIFY) {
                            byte[] keyIdBytes = XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue());
                            wssContext.setExtraneousProperty(MessageConstants.REQUESTER_KEYID, new String(keyIdBytes));
                            returnKey = wssContext.getSecurityEnvironment().getPublicKey(
                                    wssContext.getExtraneousProperties(),
                                    XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue()));
                        } else if(purpose == Purpose.SIGN){
                            returnKey =wssContext.getSecurityEnvironment().getPrivateKey(
                                    wssContext.getExtraneousProperties(),
                                    XMLUtil.getDecodedBase64EncodedData(keyId.getReferenceValue()));
                        }
                    }

                }
                
            } else if (refElement instanceof DirectReference) {
                if(keyBinding != null){
                    keyBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                    
                }
                String uri = ((DirectReference) refElement).getURI();
                if (isBSP && !uri.startsWith("#")) {
                    throw new XWSSecurityException("Violation of BSP R5204 "
                            + ": When a SECURITY_TOKEN_REFERENCE uses a Direct Reference to an INTERNAL_SECURITY_TOKEN, it MUST use a Shorthand XPointer Reference");
                }
                
                String valueType = ((DirectReference) refElement).getValueType();
                if (MessageConstants.DKT_VALUETYPE.equals(valueType)){
                    //TODO: this will work for now but need to handle this case here later
                    valueType = null;
                }

                if (MessageConstants.X509v3_NS.equals(valueType)) {
                    // its an X509 Token
                    if(keyBinding != null){
                        keyBinding.setValueType(MessageConstants.X509v3_NS);
                    }
                    String wsuId = secureMsg.getIdFromFragmentRef(uri);
                    X509SecurityToken token = (X509SecurityToken) insertedX509Cache.get(wsuId);               
                    //if(token == null)
                    //    token =(X509SecurityToken) tokenCache.get(wsuId);
                    
                    if(token == null){
                        token = (X509SecurityToken)resolveToken(wsuId,context);
                        if(token == null){
                            throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                        }else{
                            tokenCache.put(wsuId, token);
                        }
                    }

                    if(isPolicyRecipient){
                        MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                        AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                        x509Binding.setValueType(MessageConstants.X509v3_NS);
                        x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                        if(inferredKB == null){                           
                            inferredSignaturePolicy.setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                            ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                        } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                            DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                            if(dktBind.getOriginalKeyBinding() == null)
                                dktBind.setOriginalKeyBinding(x509Binding);
                            else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                                dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                            }
                        }
                    }                    
                    
                    returnKey = resolveX509Token(wssContext,  token, purpose);
                    
                } else if(MessageConstants.EncryptedKey_NS.equals(valueType)) {
                    String wsuId = secureMsg.getIdFromFragmentRef(uri);
                    SecurityToken token = (SecurityToken)tokenCache.get(wsuId);
                    if(token == null){
                        token = resolveToken(wsuId, context);
                        if(token == null){
                            throw new KeySelectorException("Token with Id "+wsuId+ "not found");//TODO LOG ::Venu
                        }else{
                            tokenCache.put(wsuId, token);
                        }
                    }
                    
                        if(isPolicyRecipient){
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
                    
                        KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                        SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                        SOAPElement se = sectr.getAsSoapElement();
                        ReferenceElement refElem = sectr.getReference();
                        Key privKey  = resolve(se, context, Purpose.SIGN);
                    
                        Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                            
                        String encEkSha1 = Base64.encode(ekSha1);
                        wssContext.getSecurityEnvironment().updateOtherPartySubject(
                                              DefaultSecurityEnvironmentImpl.getSubject(wssContext), encEkSha1); 
                        
                        returnKey = ((EncryptedKeyToken)token).getSecretKey(privKey, encAlgo);
                        wssContext.getSecurityEnvironment().updateOtherPartySubject(
                                                  DefaultSecurityEnvironmentImpl.getSubject(wssContext), returnKey);                
                } else if (MessageConstants.SCT_VALUETYPE.equals(valueType)) {
                    // could be wsuId or SCT Session Id
                    String sctId = secureMsg.getIdFromFragmentRef(uri);
                    SecurityToken token = (SecurityToken)tokenCache.get(sctId);
                    
                    if(token == null){
                        token = SecurityUtil.locateBySCTId(wssContext, uri);
                        if (token == null) {
                            token = resolveToken(sctId, context);
                        }

                        if(token == null){
                            throw new KeySelectorException("SCT Token with Id "+sctId+ "not found");
                        }else{
                            tokenCache.put(sctId, token);
                        }
                    }

                    if (token instanceof SecurityContextToken) {
                        //handling for SecurityContext Token
                        if(isPolicyRecipient){
                            MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                            SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                            if(inferredKB == null){
                                inferredSignaturePolicy.setKeyBinding(sctBinding);
                            } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                            }
                        }                       
                        returnKey = resolveSCT(wssContext, (SecurityContextTokenImpl)token, purpose);
                        
                    } else {
                        throw new KeySelectorException("Incorrect ValueType: " + MessageConstants.SCT_VALUETYPE + ", specified for a Non SCT Token");
                    }

                } else if (null == valueType) {
                    // Log fails BSP:R3059 and R3058
                    logger.log(Level.WARNING, "Fails BSP requirements R3058 and 3059");
                    
                    // Do default processing
                    String wsuId = secureMsg.getIdFromFragmentRef(uri);
                    SecurityToken token = (SecurityToken)tokenCache.get(wsuId);
                    
                    if(token == null){
                        token = resolveToken(wsuId, context);
                        if (token == null) {
                            token = SecurityUtil.locateBySCTId(wssContext, uri);
                        }

                        if(token == null){
                            throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                        }else{
                            tokenCache.put(wsuId, token);
                        }
                    }
                    
                    if (token instanceof X509SecurityToken) {
                        if(isPolicyRecipient){
                            MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                            AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                            x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                            if(inferredKB == null){                           
                                inferredSignaturePolicy.setKeyBinding(x509Binding);
                            } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                                ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                            } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                                DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                                if(dktBind.getOriginalKeyBinding() == null)
                                    dktBind.setOriginalKeyBinding(x509Binding);
                                else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                                    dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                                }
                            }
                        }                        
                        returnKey =  resolveX509Token(wssContext,(X509SecurityToken)token, purpose);
                        
                    } else if (token instanceof EncryptedKeyToken) {
                        if(isPolicyRecipient){
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
                        KeyInfoHeaderBlock kiHB = ((EncryptedKeyToken)token).getKeyInfo();
                        SecurityTokenReference sectr = kiHB.getSecurityTokenReference(0);
                        SOAPElement se = sectr.getAsSoapElement();
                        ReferenceElement refElem = sectr.getReference();
                        Key privKey  = resolve(se, context, Purpose.SIGN);
                    
                        Element cipherData = (Element)((EncryptedKeyToken)token).getAsSoapElement().getChildElements(new QName(MessageConstants.XENC_NS, "CipherData", MessageConstants.XENC_PREFIX)).next();
                        String cipherValue = cipherData.getElementsByTagNameNS(MessageConstants.XENC_NS, "CipherValue").item(0).getTextContent();
                        byte[] decodedCipher = Base64.decode(cipherValue);
                        byte[] ekSha1 = MessageDigest.getInstance("SHA-1").digest(decodedCipher);
                            
                        String encEkSha1 = Base64.encode(ekSha1);
                        wssContext.getSecurityEnvironment().updateOtherPartySubject(
                                              DefaultSecurityEnvironmentImpl.getSubject(wssContext), encEkSha1);
                        
                        returnKey = ((EncryptedKeyToken)token).getSecretKey(privKey, encAlgo);
                        wssContext.getSecurityEnvironment().updateOtherPartySubject(
                                                  DefaultSecurityEnvironmentImpl.getSubject(wssContext), returnKey);
                    
                    } else if (token instanceof SecurityContextToken) {
                        //handling for SecurityContext Token
                        if(isPolicyRecipient){
                            MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                            SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                            if(inferredKB == null){
                                inferredSignaturePolicy.setKeyBinding(sctBinding);
                            } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                                ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                            }
                        }                
                        returnKey = resolveSCT(wssContext, (SecurityContextTokenImpl)token, purpose);

                    } else if (token instanceof DerivedKeyTokenHeaderBlock){
                        if(isPolicyRecipient){
                            MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                            DerivedTokenKeyBinding dtkBinding = new DerivedTokenKeyBinding();
                            if(inferredKB == null){
                                inferredSignaturePolicy.setKeyBinding(dtkBinding);
                            } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)) {
                                //already set - do nothing
                            } else{
                                throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                            }
                        }                        
                        returnKey = resolveDKT(context, (DerivedKeyTokenHeaderBlock)token);
                    }
                    else {
                        String message = " Cannot Resolve URI " + uri;
                        logger.log(Level.SEVERE,"WSS1307.unsupported.directref.mechanism", new Object[] {message});
                        KeySelectorException xwsse =  new KeySelectorException(message);
                        //throw xwsse;
                        throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,xwsse.getMessage(),xwsse);
                    }
                    
                } else {
                    logger.log(Level.SEVERE,"WSS1307.unsupported.directref.mechanism",
                            new Object[] {((DirectReference)refElement).getValueType()});
                            
                            throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                                    "unsupported directreference ValueType "+ ((DirectReference) refElement).getValueType(),null);
                }
            } else if (refElement instanceof com.sun.xml.wss.core.reference.X509IssuerSerial) {
                if(keyBinding != null){
                    keyBinding.setReferenceType(MessageConstants.X509_ISSUER_TYPE);
                }
                com.sun.xml.wss.core.reference.X509IssuerSerial xisElement=
                        (com.sun.xml.wss.core.reference.X509IssuerSerial)refElement;
                BigInteger serialNumber =  xisElement.getSerialNumber();
                String issuerName =  xisElement.getIssuerName();
                if(isPolicyRecipient){
                    MLSPolicy inferredKB = inferredSignaturePolicy.getKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setReferenceType(MessageConstants.X509_ISSUER_TYPE);
                    if(inferredKB == null){ 
                        inferredSignaturePolicy.setKeyBinding(x509Binding);
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
                if (purpose ==  Purpose.VERIFY) {
                    wssContext.setExtraneousProperty(MessageConstants.REQUESTER_SERIAL, serialNumber);
                    wssContext.setExtraneousProperty(MessageConstants.REQUESTER_ISSUERNAME, issuerName);
                    returnKey = wssContext.getSecurityEnvironment().getPublicKey(
                            wssContext.getExtraneousProperties(),serialNumber, issuerName);
                } else if(purpose== Purpose.SIGN){
                    returnKey = wssContext.getSecurityEnvironment().getPrivateKey(
                            wssContext.getExtraneousProperties(),serialNumber, issuerName);
                }
                
            } else {
                logger.log(Level.SEVERE, "WSS1308.unsupported.reference.mechanism");
                KeySelectorException xwsse = new KeySelectorException(
                        "Key reference mechanism not supported");
                //throw xwsse;
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_UNSUPPORTED_SECURITY_TOKEN,xwsse.getMessage(),xwsse);
            }
            return returnKey;
        }catch(XWSSecurityException xwsExp){
            //log
            if(logger.getLevel() == Level.FINEST){
                logger.log(Level.FINEST,"Error occurred while resolving" +
                        "key information",xwsExp);
            }
            throw new KeySelectorException(xwsExp);
        }catch(MarshalException me){
            //log
            if(logger.getLevel() == Level.FINEST){
                logger.log(Level.FINEST,"Error occurred while resolving" +
                        "key information",me);
            }
            throw new KeySelectorException(me);
        }catch(Exception ex){
            //log
            logger.log(Level.FINEST,"Error occurred while resolving" +
                    "key information",ex);
            throw new KeySelectorException(ex);
        }
    }
    
    
    private static Key resolveSamlAssertion(XMLCryptoContext dsigContext, Element samlAssertion, Purpose purpose, String assertionID)
    throws MarshalException, KeySelectorException, XWSSecurityException{
        
        FilterProcessingContext context = (FilterProcessingContext)dsigContext.get(MessageConstants.WSS_PROCESSING_CONTEXT);
        Key key = (Key) context.getSamlIdVSKeyCache().get(assertionID);
        if (key != null)
            return key;
        

        if (purpose == Purpose.VERIFY) {
            NodeList nl = samlAssertion.getElementsByTagNameNS(MessageConstants.DSIG_NS, "Signature");
            //verify the signature inside the SAML assertion
            if ( nl.getLength() == 0 ) {
                XWSSecurityException e = new XWSSecurityException("Unsigned SAML Assertion encountered");
                logger.log(Level.SEVERE, "WSS1309.saml.signature.verify.failed", e);
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY,
                        "Exception during Signature verfication in SAML Assertion",
                        e);
            }
            SignaturePolicy policy = (SignaturePolicy)context.getInferredPolicy();
            // will be only during verify.
            AuthenticationTokenPolicy.SAMLAssertionBinding keyBinding = null;
            
            if(policy != null){
                keyBinding = (AuthenticationTokenPolicy.SAMLAssertionBinding) policy.newSAMLAssertionKeyBinding();
            }
            Element elem = (Element)nl.item(0);
            
            try {
                if ( !SignatureProcessor.verifySignature(elem, context)) {
                    logger.log(Level.SEVERE, "WSS1310.saml.signature.invalid");
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_FAILED_AUTHENTICATION,
                            "SAML Assertion has invalid Signature",
                            new Exception(
                            "SAML Assertion has invalid Signature"));
                }
            } catch (XWSSecurityException ex) {
                logger.log(Level.SEVERE, "WSS1310.saml.signature.invalid");
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_FAILED_AUTHENTICATION,
                        "SAML Assertion has invalid Signature",
                        ex);
            }
        }
        
        Element keyInfoElem = AssertionUtil.getSubjectConfirmationKeyInfo(samlAssertion);
        
        KeyInfo keyInfo = KeyInfoFactory.getInstance().unmarshalKeyInfo(new DOMStructure(keyInfoElem));
        List keyInfoList = keyInfo.getContent();
        Iterator content = keyInfoList.iterator();
        while(content.hasNext()){
            Object data = content.next();
            if(data instanceof KeyName){
                throw new XWSSecurityException("Unsupported KeyName under SAML SubjectConfirmation");
            }else if(data instanceof KeyValue){
                key = resolveKeyValue(context, (KeyValue)data, purpose);
                break;
            }else if (data instanceof X509Data){
                key = resolveX509Data(context, (X509Data)data, purpose);
                break;
            } else if (data instanceof DOMStructure) {
               // we will support STR,  EK and wst:BinarySecret
               SOAPElement reference = (SOAPElement)((DOMStructure)data).getNode();
               if (isSecurityTokenReference(reference)){
                   key = resolve(reference,dsigContext, purpose);
                   break;
               } else if (SecurityUtil.isBinarySecret(reference)) {
                   BinarySecret bs = null;
                   try {
                       bs = WSTrustElementFactory.newInstance().createBinarySecret(reference);
                   }catch (WSTrustException ex) {
                       throw new XWSSecurityException(ex);
                   } 
                   // assuming the Binary Secret is of Type 
                   if ((bs.getType() == null) || bs.getType().equals(BinarySecret.SYMMETRIC_KEY_TYPE)) {
                        String algo = "AES"; // hardcoding for now
                        if (context.getAlgorithmSuite() != null) {
                            algo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                        }
                        key = new SecretKeySpec(bs.getRawValue(), algo);
                        break;
                   } else {
                       logger.log(Level.SEVERE, "WSS1312.unsupported.keyinfo");
                       throw new KeySelectorException("Unsupported wst:BinarySecret Type");
                   }
               } else if (SecurityUtil.isEncryptedKey(reference)) {
                    EncryptedKeyToken ekToken = new EncryptedKeyToken(reference);
                    KeyInfoHeaderBlock kiHB = ekToken.getKeyInfo();
                    // assume it contains STR 
                    if (kiHB.containsSecurityTokenReference()) {
                        SecurityTokenReference str = kiHB.getSecurityTokenReference(0);
                        Key privKey = KeyResolver.processSTR(kiHB, false, context);
                        //Default algo
                       String dataEncAlgo = MessageConstants.AES_BLOCK_ENCRYPTION_128;
                       if (context.getAlgorithmSuite() != null) {
                           dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                       }
                       key = ekToken.getSecretKey(privKey, dataEncAlgo);
                       break;
                    } else {
                       logger.log(Level.SEVERE, "WSS1312.unsupported.keyinfo");
                       throw new KeySelectorException("Unsupported Key Information Inside EncryptedKey");
                    }

               } else {
                   logger.log(Level.SEVERE, "WSS1312.unsupported.keyinfo");
                   throw new KeySelectorException("Unsupported Key Information");
               }

            }else {
                logger.log(Level.SEVERE, "WSS1312.unsupported.keyinfo");
                throw new KeySelectorException("Unsupported Key Information");
            }
        }
        context.getSamlIdVSKeyCache().put(assertionID, key);
        
        //set the SAML Assertion as Public Credential inside the requester Subject
        try {
            context.getSecurityEnvironment().updateOtherPartySubject(
                    DefaultSecurityEnvironmentImpl.getSubject(context), AssertionUtil.fromElement(samlAssertion));
        } catch (SAMLException ex) {
            //ignore
        }
        
        return key;
    }
    
    
    
    private static Key resolveKeyValue( FilterProcessingContext context, KeyValue  keyValue, Purpose purpose) throws KeySelectorException {
        try {
            if (purpose == Purpose.VERIFY) {
                return keyValue.getPublicKey();
            } else if(purpose == Purpose.SIGN){
                return context.getSecurityEnvironment().getPrivateKey(
                        context.getExtraneousProperties(), keyValue.getPublicKey(), true);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"WSS1313.illegal.key.value",e.getMessage());
            throw new KeySelectorException(e);
        }
        return null;
    }
    
    
    
    private static Key resolveX509Data(FilterProcessingContext context,X509Data  x509Data,  Purpose purpose) throws KeySelectorException {
        
        
        X509Certificate cert =  null;
        
        try {
            List data = x509Data.getContent();
            Iterator iterator = data.iterator();
            while(iterator.hasNext()){//will break for in single loop;
                Object content = iterator.next();
                if (content instanceof X509Certificate) {
                    cert = (X509Certificate)content;
                } else if(content instanceof byte[]) {
                    byte[] ski = (byte[]) content;
                    if (purpose == Purpose.VERIFY) {
                        return context.getSecurityEnvironment().getPublicKey(
                                context.getExtraneousProperties(),ski);
                    } else if(purpose == Purpose.SIGN){
                        return context.getSecurityEnvironment().getPrivateKey(
                                context.getExtraneousProperties(), ski);
                    }
                } else if (content instanceof String) {
                    logger.log(Level.SEVERE, "WSS1312.unsupported.keyinfo");
                    throw new KeySelectorException(
                            "X509SubjectName child element of X509Data is not yet supported by our implementation");
                } else if (content instanceof X509IssuerSerial) {
                    X509IssuerSerial xis = (X509IssuerSerial) content;
                    if (purpose == Purpose.VERIFY) {
                        return context.getSecurityEnvironment().getPublicKey(
                                context.getExtraneousProperties(), xis.getSerialNumber(), xis.getIssuerName());
                    } else if(purpose == Purpose.SIGN){
                        return context.getSecurityEnvironment().getPrivateKey(
                                context.getExtraneousProperties(), xis.getSerialNumber(), xis.getIssuerName());
                    }
                    
                } else {
                    logger.log(Level.SEVERE, "WSS1312.unsupported.keyinfo");
                    throw new KeySelectorException(
                            "Unsupported child element of X509Data encountered");
                }
                
                if (purpose == Purpose.VERIFY) {
                    return cert.getPublicKey();
                } else if(purpose == Purpose.SIGN){
                    return context.getSecurityEnvironment().getPrivateKey(
                            context.getExtraneousProperties(), cert);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"WSS1314.illegal.x509.data", e.getMessage());
            throw new KeySelectorException(e);
        }
        return null;//Should never come here.
    }
    
    
    private static Key resolveX509Token(FilterProcessingContext context, X509SecurityToken token, Purpose purpose) throws XWSSecurityException {
        if (purpose == Purpose.VERIFY) {
            // Update the Subject of the sender
            SecurableSoapMessage secureMsg = context.getSecurableSoapMessage();
            X509Certificate cert = token.getCertificate();
            context.getSecurityEnvironment().updateOtherPartySubject(
                    DefaultSecurityEnvironmentImpl.getSubject(context), cert);
            return cert.getPublicKey();
        } else if(purpose == Purpose.SIGN || purpose == Purpose.DECRYPT) {
            return context.getSecurityEnvironment().getPrivateKey(
                    context.getExtraneousProperties(), token.getCertificate());
        }
        return null;
    }
    

    private static  boolean isSecurityTokenReference(Element reference){
        if(MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME.equals(reference.getLocalName()))
            return true;
        return false;
    }
    
    
    
    /**
     * BinaryTokens if found would be cached into {@link FilterProcessingContext}.
     * @param uri
     * @param context
     * @throws URIReferenceException
     * @throws XWSSecurityException
     * @return
     */
    protected static SecurityToken resolveToken(final String uri, XMLCryptoContext context) throws URIReferenceException, XWSSecurityException{
        
        URIDereferencer resolver = context.getURIDereferencer();
        URIReference uriRef = new URIReference(){
            
            public String getURI(){
                return uri;
            }
            
            public String getType(){
                return null;
            }
        };
        
        FilterProcessingContext wssContext = (FilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);


        SecurityPolicy securityPolicy = wssContext.getSecurityPolicy();
        
        boolean isBSP = false;
        
        if( securityPolicy != null) {
            if (PolicyTypeUtil.messagePolicy(securityPolicy)) {
                isBSP = ((MessagePolicy)securityPolicy).isBSP();
            } else {
                isBSP = ((WSSPolicy)securityPolicy).isBSP();
            }
        }

        
        try{
            NodeSetData set =(NodeSetData) resolver.dereference(uriRef,context);
            Iterator itr = set.iterator();
            while(itr.hasNext()){
                Node node = (Node)itr.next();
                if(MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME.equals(node.getLocalName())){
                    X509SecurityToken token = new X509SecurityToken((SOAPElement)node, isBSP);
                    X509Certificate cert = null;
                    try{
                        cert = token.getCertificate();
                    }catch(XWSSecurityException xwe){
                        throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                                "A Invalid security token was provided ", xwe);
                    }
                    if(!wssContext.getSecurityEnvironment().validateCertificate(cert)){
                        throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                                "Certificate validation failed", null);
                    }
                    return token;
                }else if(MessageConstants.ENCRYPTEDKEY_LNAME.equals(node.getLocalName())){
                    return new  EncryptedKeyToken((SOAPElement)node);
                } else if (MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME.equals(node.getLocalName())){
                    return new SecurityContextTokenImpl((SOAPElement)node);
                } else if (MessageConstants.DERIVEDKEY_TOKEN_LNAME.equals(node.getLocalName())){
                    return new DerivedKeyTokenHeaderBlock((SOAPElement)node);
                }
                
            }
        }catch(URIReferenceException ure){
            logger.log(Level.SEVERE,"WSS1304.FC_SECURITY_TOKEN_UNAVAILABLE",ure);
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                    "Referenced Security Token could not be retrieved",
                    ure);
        }
        
        if(logger.isLoggable(Level.SEVERE)){
            logger.log(Level.SEVERE,"WSS1305.UnSupported.security.token");
        }
        throw SecurableSoapMessage.newSOAPFaultException(MessageConstants.WSSE_UNSUPPORTED_SECURITY_TOKEN, "A Unsupported token was provided ", null);
    }
    
    private static Element resolveSAMLToken(SecurityTokenReference tokenRef, String assertionId,
            FilterProcessingContext context)throws XWSSecurityException {
        
        /*
        Assertion ret = (Assertion)context.getTokenCache().get(assertionId);
        if (ret != null)
            return ret;
         */
        Assertion ret = null;
        
        // first check if this is a Trust Issued Token
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
        //TODO: expensive conversion happening here 
        try {
            ret = AssertionUtil.fromElement(tokenElement);
        } catch (Exception e) {
            if(logger.getLevel() == Level.FINEST){
                logger.log(Level.FINEST,"Error occured while resolving" +
                        "SAML assertion",e);
            }
            throw new XWSSecurityException(e);
        }
        context.getTokenCache().put(assertionId, ret);
        return tokenElement;
    }
    
    private static void addAuthorityId(Element assertion , FilterProcessingContext fp){
        SignaturePolicy ep = (SignaturePolicy)fp.getInferredPolicy();
        if( ep != null){
            AuthenticationTokenPolicy.SAMLAssertionBinding kb = 
                (AuthenticationTokenPolicy.SAMLAssertionBinding ) ep.newSAMLAssertionKeyBinding();
            String issuer = assertion.getAttribute("Issuer");
            kb.setAuthorityIdentifier(issuer);
        }
    }

    // this method would be called on incoming client/server messages  
    private static Key resolveSCT(
        FilterProcessingContext context, SecurityContextTokenImpl token, Purpose purpose) 
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
        // this is because the key would be used for Signatures
        //TODO: PLUGFEST : change this to globally available encryption algo
        if (proofKey == null) {
            throw new XWSSecurityException("Could not locate SecureConversation session for Id:" + scId);
        }

        String algo = "AES"; // hardcoding for now
        if (context.getAlgorithmSuite() != null) {
            algo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
        }
        SecretKeySpec key = new SecretKeySpec(proofKey, algo);        
        return key;
    }
    
    private static Key resolveDKT(XMLCryptoContext context, 
            DerivedKeyTokenHeaderBlock token) throws XWSSecurityException{
        
        FilterProcessingContext wssContext = (FilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
        AlgorithmSuite algSuite = wssContext.getAlgorithmSuite();
        //TODO: hardcoded for testing -- need to obtain this from somewhere
        String algorithm = MessageConstants.AES_BLOCK_ENCRYPTION_128;
        if(algSuite != null)
            algorithm = algSuite.getEncryptionAlgorithm();
        
        try{
        SecurityTokenReference sectr = token.getDerivedKeyElement();
        SOAPElement se = sectr.getAsSoapElement();
        ReferenceElement refElem = sectr.getReference();
        Key encKey  = resolve(se, context, Purpose.SIGN);
        byte[] secret = encKey.getEncoded();
        
        byte[] nonce = token.getNonce();
        long length = token.getLength();
        long offset = token.getOffset();
        DerivedKeyToken dkt = new DerivedKeyTokenImpl(offset, length, secret, nonce);
        String jceAlgo = SecurityUtil.getSecretKeyAlgorithm(algorithm);
        Key returnKey = dkt.generateSymmetricKey(jceAlgo);
        return returnKey;
        } catch (Exception e){
            throw new XWSSecurityException(e);
        }
    }
}
