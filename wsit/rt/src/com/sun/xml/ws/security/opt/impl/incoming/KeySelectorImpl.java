
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

package com.sun.xml.ws.security.opt.impl.incoming;


import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.BinarySecurityToken;
import com.sun.xml.ws.security.opt.impl.crypto.OctectStreamData;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.wss.impl.misc.Base64;


import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.MLSPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.crypto.Data;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;

import javax.xml.bind.JAXBElement;

import java.security.PublicKey;
import java.security.KeyException;
import java.security.Key;
import java.security.cert.X509Certificate;

import java.math.BigInteger;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference;
import com.sun.xml.ws.security.opt.api.reference.Reference;
import com.sun.xml.ws.security.opt.api.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.api.reference.DirectReference;
import com.sun.xml.ws.runtime.dev.SessionManager;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBStructure;
import javax.xml.crypto.KeySelector.Purpose;
import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;
import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.IssuedTokenManager;
import com.sun.xml.ws.security.impl.PasswordDerivedKey;
import com.sun.xml.ws.security.opt.impl.tokens.UsernameToken;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.secconv.impl.client.DefaultSCTokenConfiguration;
import com.sun.xml.ws.security.secext10.AttributedString;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.UsernameTokenBinding;
import java.lang.NullPointerException;
import java.security.cert.CertificateEncodingException;
import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSException;



/**
 *
 * @author Ashutosh.Shahi@Sun.Com
 */
public class KeySelectorImpl extends KeySelector {
    
    private static KeySelectorImpl keyResolver = null;
    private static final Logger logger = Logger.getLogger(LogDomainConstants.IMPL_SIGNATURE_DOMAIN,
            LogDomainConstants.IMPL_SIGNATURE_DOMAIN_BUNDLE);
    
    
    /** Creates a new instance of KeySelectorImpl */
    static{
        keyResolver = new KeySelectorImpl();
    }
    
    /** Creates a new instance of KeySelectorImpl */
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
        try {
            
            SignatureMethod sm = (SignatureMethod) method;
            List list = keyInfo.getContent();
            JAXBFilterProcessingContext wssContext = (JAXBFilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
            
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
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
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
                    //if the purpose is signature verification, we need to make sure we
                    //trust the certificate. in case of HOK SAML this can be the cert of the IP
                    if (purpose == Purpose.VERIFY) {
                        X509Certificate cert = wssContext.getSecurityEnvironment().getCertificate(wssContext.getExtraneousProperties(),pk,false);
                        wssContext.getSecurityEnvironment().validateCertificate(cert,wssContext.getExtraneousProperties());
                    }
                    // make sure algorithm is compatible with method
                    if (algEquals(sm.getAlgorithm(), pk.getAlgorithm())) {
                        return new SimpleKeySelectorResult(pk);
                    }
                } else if(xmlStructure instanceof JAXBStructure){
                    JAXBElement reference = ((JAXBStructure)xmlStructure).getJAXBElement();
                    if(isSecurityTokenReference(reference)){
                        isStr = true;
                        final Key key = resolve(reference, context, purpose);
                        return new KeySelectorResult(){
                            public Key getKey(){
                                return key;
                            }
                        };
                    }
                } else if (xmlStructure instanceof KeyName) {
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
                } else if (xmlStructure instanceof X509Data){
                    Key key = resolveX509Data(wssContext, (X509Data)xmlStructure, purpose);
                    return new SimpleKeySelectorResult(key);
                }
            }
            
        } catch(KeySelectorException kse){
            throw kse;
        } catch(Exception ex){
            logger.log(Level.FINEST,"Error occurred while resolving keyinformation" +
                    ex.getMessage());
            throw new KeySelectorException(ex);
        }
        throw new KeySelectorException("No KeyValue element found!");
    }
    
    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private Key pk;
        SimpleKeySelectorResult(Key pk) {
            this.pk = pk;
        }
        public Key getKey() { return pk; }
    }
    
    private static Key resolve(JAXBElement securityTokenReference,XMLCryptoContext context, Purpose purpose)throws KeySelectorException {
        try{
            JAXBFilterProcessingContext wssContext = (JAXBFilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
            boolean isPolicyRecipient = (wssContext.getMode()== JAXBFilterProcessingContext.WSDL_POLICY);
            
            SecurityPolicy securityPolicy = wssContext.getSecurityPolicy();
            boolean isBSP = false;
            if(securityPolicy != null) {
                if (PolicyTypeUtil.messagePolicy(securityPolicy)) {
                    isBSP = ((MessagePolicy)securityPolicy).isBSP();
                } else {
                    isBSP = ((WSSPolicy)securityPolicy).isBSP();
                }
            }
            
            //SecurityTokenReference str = new com.sun.xml.ws.opt.security.impl.keyinfo.SecurityTokenReference(
            //        (SecurityTokenReferenceType)securityTokenReference.getValue());
            SecurityTokenReference str = (SecurityTokenReference)securityTokenReference.getValue();
            Reference reference = str.getReference();
            //HashMap tokenCache = wssContext.getTokenCache();
            //HashMap insertedX509Cache = wssContext.getInsertedX509Cache();
            
            Key returnKey = null;
            if(reference instanceof KeyIdentifier){
                KeyIdentifier keyId = (KeyIdentifier)reference;
                
                returnKey = resolveKeyIdentifier(context, keyId.getValueType(),
                        keyId.getReferenceValue(),null, purpose);
                
                
            } else if (reference instanceof DirectReference) {
                //WSSElementFactory elementFactory = new WSSElementFactory(wssContext.getSOAPVersion());
                //DirectReference directRef = elementFactory.createDirectReference();
                //DirectReference dReference = (DirectReference) reference;
                DirectReference dReference = (DirectReference)reference;
                String wscInstance = ((com.sun.xml.ws.security.opt.impl.reference.DirectReference)dReference).getAttribute(
                        wssContext.getWSSCVersion(wssContext.getSecurityPolicyVersion()), "Instance");
                String uri = dReference.getURI();
                if (isBSP && !uri.startsWith("#")) {
                    throw new XWSSecurityException("Violation of BSP R5204 "
                            + ": When a SECURITY_TOKEN_REFERENCE uses a Direct Reference to an INTERNAL_SECURITY_TOKEN, it MUST use a Shorthand XPointer Reference");
                }
                
                String valueType = dReference.getValueType();
                if (MessageConstants.DKT_VALUETYPE.equals(valueType) ||
                        MessageConstants.DKT_13_VALUETYPE.equals(valueType)){
                    //TODO: this will work for now but need to handle this case here later
                    valueType = null;
                }
                
                returnKey = resolveDirectReference(context, valueType, uri, purpose);
                
                
            } else if(reference instanceof com.sun.xml.ws.security.opt.impl.reference.X509IssuerSerial){
                com.sun.xml.ws.security.opt.impl.reference.X509IssuerSerial xis =
                        (com.sun.xml.ws.security.opt.impl.reference.X509IssuerSerial)reference;
                BigInteger serialNumber = xis.getX509SerialNumber();
                String issuerName = xis.getX509IssuerName();
                
                resolveIssuerSerial(context, issuerName, serialNumber, xis.getId(), purpose);
            } else {
                logger.log(Level.SEVERE, "WSS1308.unsupported.reference.mechanism");
                KeySelectorException xwsse = new KeySelectorException(
                        "Key reference mechanism not supported");
                //throw xwsse;
                throw SOAPUtil.newSOAPFaultException(
                        MessageConstants.WSSE_UNSUPPORTED_SECURITY_TOKEN,xwsse.getMessage(),xwsse);
            }
            return returnKey;
        } catch(XWSSecurityException xwsExp){
            logger.log(Level.FINEST,"Error occurred while resolving" +
                    "key information",xwsExp);
            throw new KeySelectorException(xwsExp);
        } catch(Exception ex){
            logger.log(Level.FINEST,"Error occurred while resolving" +
                    "key information",ex);
            throw new KeySelectorException(ex);
        }
        
    }
    @SuppressWarnings("unchecked")
    public static Key resolveIssuerSerial(XMLCryptoContext context, String issuerName,
            BigInteger serialNumber, String strId, Purpose purpose) throws KeySelectorException {
        Key returnKey = null;
        String normalizedIssuerName = RFC2253Parser.normalize(issuerName);
        try{
            JAXBFilterProcessingContext wssContext = (JAXBFilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
            MLSPolicy inferredKB = wssContext.getSecurityContext().getInferredKB();
            
            // for policy verification
            AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
            x509Binding.setReferenceType(MessageConstants.X509_ISSUER_TYPE);
            if(inferredKB == null){
                wssContext.getSecurityContext().setInferredKB(x509Binding);
            } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
            } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                if(dktBind.getOriginalKeyBinding() == null) {
                    dktBind.setOriginalKeyBinding(x509Binding);
                } else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                    dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                }
            }
            
            if (purpose ==  Purpose.VERIFY) {
                wssContext.setExtraneousProperty(MessageConstants.REQUESTER_SERIAL, serialNumber);
                wssContext.setExtraneousProperty(MessageConstants.REQUESTER_ISSUERNAME, normalizedIssuerName);
                
//                returnKey = wssContext.getSecurityEnvironment().getPublicKey(
//                        wssContext.getExtraneousProperties(),serialNumber, normalizedIssuerName);
                X509Certificate cert = wssContext.getSecurityEnvironment().getCertificate(
                        wssContext.getExtraneousProperties(),serialNumber, normalizedIssuerName);
                returnKey = cert.getPublicKey();
                if(strId != null){
                    byte [] encodedCert = cert.getEncoded();
                    wssContext.getElementCache().put(strId,new OctectStreamData(new String(encodedCert)));
                }
                
            } else if(purpose == Purpose.SIGN || purpose == Purpose.DECRYPT){
                returnKey = wssContext.getSecurityEnvironment().getPrivateKey(
                        wssContext.getExtraneousProperties(),serialNumber, normalizedIssuerName);
            }
        } catch(Exception ex){
            logger.log(Level.FINEST,"Error occurred while resolving" +
                    "key information",ex);
            throw new KeySelectorException(ex);
        }
        return returnKey;
    }
    
    public static Key resolveDirectReference(XMLCryptoContext context , String valueType,
            String uri, Purpose purpose) throws KeySelectorException {
        
        Key returnKey = null;
        try{
            JAXBFilterProcessingContext wssContext = (JAXBFilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);
            MLSPolicy inferredKB = wssContext.getSecurityContext().getInferredKB();
            String wsuId = SOAPUtil.getIdFromFragmentRef(uri);
            boolean isSymmetric = false;            
            if (MessageConstants.USERNAME_TOKEN_NS.equals(valueType) || MessageConstants.USERNAME_STR_REFERENCE_NS.equals(valueType)) {
                UsernameTokenHeader token = null;
                token = (UsernameTokenHeader) resolveToken(wsuId, context);
                if (token == null) {
                    throw new KeySelectorException("Token with Id " + wsuId + " not found");
                }
                AuthenticationTokenPolicy.UsernameTokenBinding untBinding = new AuthenticationTokenPolicy.UsernameTokenBinding();
                untBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                untBinding.setValueType(valueType);
                if (inferredKB == null) {
                    wssContext.getSecurityContext().setInferredKB(untBinding);
                    if (wssContext.getExtraneousProperty("EncryptedKey") != null) {
                      isSymmetric = true;
                    } 
                } else if (PolicyTypeUtil.symmetricKeyBinding(inferredKB)) {
                    ((SymmetricKeyBinding) inferredKB).setKeyBinding(untBinding);
                    isSymmetric = true;
                } else if (PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)) {
                    DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding) inferredKB;
                    if (dktBind.getOriginalKeyBinding() == null) {
                        dktBind.setOriginalKeyBinding(untBinding);
                    } else if (PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())) {
                        dktBind.getOriginalKeyBinding().setKeyBinding(untBinding);
                        isSymmetric = true;
                    }
                }
                returnKey = resolveUsernameToken(wssContext, token, purpose, isSymmetric);

            } else if (MessageConstants.X509v3_NS.equals(valueType) || MessageConstants.X509v1_NS.equals(valueType)) {
                // its an X509 Token
                X509BinarySecurityToken token = null;
                token = (X509BinarySecurityToken)resolveToken(wsuId,context);
                if(token == null){
                    throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                }
                // for policy verification
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                x509Binding.setValueType(valueType);
                if(inferredKB == null){
                    wssContext.getSecurityContext().setInferredKB(x509Binding);
                } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                    ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    isSymmetric = true;
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                    if(dktBind.getOriginalKeyBinding() == null) {
                        dktBind.setOriginalKeyBinding(x509Binding);
                    } else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                        dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                        isSymmetric = true;
                    }
                }
                
                returnKey = resolveX509Token(wssContext,  token, purpose, isSymmetric);
            } else if(MessageConstants.KERBEROS_V5_GSS_APREQ_1510.equals(valueType) ||
                    MessageConstants.KERBEROS_V5_GSS_APREQ.equals(valueType)){
                KerberosBinarySecurityToken token = (KerberosBinarySecurityToken)resolveToken(wsuId,context);
                if(token == null){
                    throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                }
                // for policy verification
                SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                AuthenticationTokenPolicy.KerberosTokenBinding ktBinding = new AuthenticationTokenPolicy.KerberosTokenBinding();
                ktBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                ktBinding.setValueType(valueType);
                skBinding.setKeyBinding(ktBinding);
                if(inferredKB == null){
                    wssContext.getSecurityContext().setInferredKB(skBinding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                    if(dktBind.getOriginalKeyBinding() == null) {
                        dktBind.setOriginalKeyBinding(skBinding);
                    } else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                        dktBind.getOriginalKeyBinding().setKeyBinding(ktBinding);
                        isSymmetric = true;
                    }
                }
                
                returnKey = resolveKerberosToken(wssContext, token);
            } else if(MessageConstants.EncryptedKey_NS.equals(valueType)) {
                EncryptedKey token = (EncryptedKey)resolveToken(wsuId, context);
                if(token == null){
                    throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                }
                // for policy verification
                WSSPolicy skBinding = null;
                boolean saml = wssContext.getSecurityContext().getIsSAMLKeyBinding();
                if (saml) {
                    skBinding = new AuthenticationTokenPolicy.SAMLAssertionBinding();
                    //reset the property, but why ?. Currently Policy is being inferred for
                    // every ED, so reset here will screw up again
                    //wssContext.getSecurityContext().setIsSAMLKeyBinding(false);
                } else {
                    // for policy verification
                    SymmetricKeyBinding symkBinding = new SymmetricKeyBinding();
                    //AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    //symkBinding.setKeyBinding(x509Binding);
                    skBinding = symkBinding;
                }
                //TODO: ReferenceType and ValueType not set on X509Binding
                if(inferredKB == null){
                    wssContext.getSecurityContext().setInferredKB(skBinding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    
                }
                // TODO: where are EKSHA1 and and SECRET_KEY values being set
                String algo = wssContext.getAlgorithmSuite().getEncryptionAlgorithm();
                returnKey = token.getKey(algo);
                skBinding.setKeyBinding(token.getInferredKB());
            } else if (MessageConstants.SCT_VALUETYPE.equals(valueType) || MessageConstants.SCT_13_VALUETYPE.equals(valueType)) {                                
                // wsuId here could be wsuId or SCT Session Id
                if(wssContext.isClient()){
                    returnKey = resolveSCT(wssContext, wsuId, purpose);
                }
                if(returnKey == null){
                    SecurityContextToken scToken = (SecurityContextToken)resolveToken(wsuId, context);
                    //wssContext.setExtraneousProperty(MessageConstants.INCOMING_SCT, scToken);
                    if(scToken == null){
                        if(!wssContext.isClient()){
                            // It will be executed on server-side when IncludeToken=Never
                            returnKey = resolveSCT(wssContext, wsuId, purpose);
                        }else{
                            throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                        }
                    }else{
                        returnKey = resolveSCT(wssContext, scToken.getSCId(), purpose);
                    }
                }
                
                SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                if(inferredKB == null){
                    wssContext.getSecurityContext().setInferredKB(sctBinding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                }
                return returnKey;
            } else if(MessageConstants.DKT_VALUETYPE.equals(valueType) || 
                        MessageConstants.DKT_13_VALUETYPE.equals(valueType)){
                DerivedKeyToken token = (DerivedKeyToken)resolveToken(wsuId, context);
                if(token == null){
                    throw new KeySelectorException("Token with Id "+wsuId+ "not found");
                }
                returnKey = ((DerivedKeyToken)token).getKey();
                DerivedTokenKeyBinding dtkBinding = new DerivedTokenKeyBinding();
                dtkBinding.setOriginalKeyBinding(token.getInferredKB());
                if(inferredKB == null){
                    wssContext.getSecurityContext().setInferredKB(dtkBinding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)) {
                    //already set - do nothing
                } else{
                    //throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                }
                
                //returnKey = ((DerivedKeyToken)token).getKey();
            } else if (null == valueType) {
                
                SecurityHeaderElement token = resolveToken(wsuId, context);
                if(token == null){
                    throw new KeySelectorException("Token with Id "+wsuId+ " not found");
                }
                if(token instanceof X509BinarySecurityToken){
                    // for policy verification
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    x509Binding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                    if(inferredKB == null){
                        wssContext.getSecurityContext().setInferredKB(x509Binding);
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
                    //
                    
                    returnKey = resolveX509Token(wssContext, (X509BinarySecurityToken)token, purpose, isSymmetric);
                } else if(token instanceof EncryptedKey){
                    // for policy verification
                    SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                    AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                    skBinding.setKeyBinding(x509Binding);
                    //TODO: ReferenceType and ValueType not set on X509Binding
                    if(inferredKB == null){
                        wssContext.getSecurityContext().setInferredKB(skBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                            ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                    }
                    //
                    
                    String algo = wssContext.getAlgorithmSuite().getEncryptionAlgorithm();
                    returnKey = ((EncryptedKey)token).getKey(algo);
                } else if(token instanceof DerivedKeyToken){
                    // for policy verification
                    returnKey = ((DerivedKeyToken)token).getKey();
                    inferredKB = wssContext.getSecurityContext().getInferredKB();
                    DerivedTokenKeyBinding dtkBinding = new DerivedTokenKeyBinding();
                    dtkBinding.setOriginalKeyBinding(((DerivedKeyToken)token).getInferredKB());
                    if(inferredKB == null){
                        wssContext.getSecurityContext().setInferredKB(dtkBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)) {
                        //already set - do nothing
                    } else{
                        //throw new XWSSecurityException("A derived Key Token should be a top level key binding");
                    }
                    //                    
                    //returnKey = ((DerivedKeyToken)token).getKey();
                } else if(token instanceof SecurityContextToken){
                    // for policy verification
                    SecureConversationTokenKeyBinding sctBinding = new SecureConversationTokenKeyBinding();
                    if(inferredKB == null){
                        wssContext.getSecurityContext().setInferredKB(sctBinding);
                    } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(sctBinding);
                    }
                    //wssContext.setExtraneousProperty(MessageConstants.INCOMING_SCT, token);
                    returnKey = resolveSCT(wssContext, ((SecurityContextToken)token).getSCId(), purpose);
                } else if (token instanceof UsernameToken){
                    AuthenticationTokenPolicy.UsernameTokenBinding untBinding = new AuthenticationTokenPolicy.UsernameTokenBinding();
                    untBinding.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
                    if(inferredKB == null){
                        wssContext.getSecurityContext().setInferredKB(untBinding);
                    }else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                        if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null){
                           ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(untBinding);
                        }
                    }
                     //TODO:suresh fix this
                   returnKey = resolveUsernameToken(wssContext, (UsernameTokenHeader)token, purpose, isSymmetric);
                    
                }
                
            } else {
                logger.log(Level.SEVERE,"WSS1307.unsupported.directref.mechanism",
                        new Object[] {valueType});
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "unsupported directreference ValueType "+ valueType,null);
            }
        } catch (XWSSecurityException ex) {
            logger.log(Level.SEVERE,"WSS1377.error.in.resolving.keyinfo",ex);
            throw new KeySelectorException(ex);
        } catch (URIReferenceException ex) {
            logger.log(Level.SEVERE,"WSS1377.error.in.resolving.keyinfo" ,ex);
            throw new KeySelectorException(ex);
        }
        
        return returnKey;
    }
    @SuppressWarnings("unchecked")
    public static Key resolveKeyIdentifier(XMLCryptoContext xc, String valueType,
            String referenceValue,String strId, Purpose purpose) throws KeySelectorException {
        JAXBFilterProcessingContext context = (JAXBFilterProcessingContext)xc.get(MessageConstants.WSS_PROCESSING_CONTEXT);
        Key returnKey = null;
        MLSPolicy inferredKB = context.getSecurityContext().getInferredKB();
        boolean isSymmetric = false;
        try{
            if (MessageConstants.X509SubjectKeyIdentifier_NS.equals(valueType) ||
                    MessageConstants.X509v3SubjectKeyIdentifier_NS.equals(valueType)) {
                //for policy verification
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                x509Binding.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
                x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                if(inferredKB == null){
                    context.getSecurityContext().setInferredKB(x509Binding);
                } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                    ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    isSymmetric = true;
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                    if(dktBind.getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                    else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                        dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                        isSymmetric = true;
                    }
                }
                // get the key
                if (purpose == Purpose.VERIFY || purpose == Purpose.ENCRYPT) {
                    byte[] keyIdBytes = XMLUtil.getDecodedBase64EncodedData(referenceValue);
                    context.setExtraneousProperty(MessageConstants.REQUESTER_KEYID, new String(keyIdBytes));
                    //returnKey = context.getSecurityEnvironment().getPublicKey(
                    //      context.getExtraneousProperties(),keyIdBytes);
                    X509Certificate cert = context.getSecurityEnvironment().getCertificate(
                            context.getExtraneousProperties(),keyIdBytes);
                    if(strId != null){
                        byte [] encodedCert = cert.getEncoded();
                        context.getElementCache().put(strId,new OctectStreamData(new String(encodedCert)));
                    }
                    if (!isSymmetric && !context.isSamlSignatureKey()) {
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context), cert);
                    }
                    returnKey = cert.getPublicKey();
                } else if(purpose == Purpose.SIGN || purpose == Purpose.DECRYPT){
                    returnKey = context.getSecurityEnvironment().getPrivateKey(
                            context.getExtraneousProperties(),
                            XMLUtil.getDecodedBase64EncodedData(referenceValue));
                }
            }  else if (MessageConstants.ThumbPrintIdentifier_NS.equals(valueType)) {
                //for policy verification
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                x509Binding.setValueType(MessageConstants.ThumbPrintIdentifier_NS);
                x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                if(inferredKB == null){
                    context.getSecurityContext().setInferredKB(x509Binding);
                } else if(PolicyTypeUtil.symmetricKeyBinding(inferredKB)){
                    ((SymmetricKeyBinding)inferredKB).setKeyBinding(x509Binding);
                    isSymmetric = true;
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    DerivedTokenKeyBinding dktBind = (DerivedTokenKeyBinding)inferredKB;
                    if(dktBind.getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(x509Binding);
                    else if(PolicyTypeUtil.symmetricKeyBinding(dktBind.getOriginalKeyBinding())){
                        dktBind.getOriginalKeyBinding().setKeyBinding(x509Binding);
                        isSymmetric = true;
                    }
                }
                // get the key
                if (purpose == Purpose.VERIFY || purpose == Purpose.ENCRYPT) {
                    byte[] keyIdBytes = XMLUtil.getDecodedBase64EncodedData(referenceValue);
                    context.setExtraneousProperty(MessageConstants.REQUESTER_KEYID, new String(keyIdBytes));
                    
                    X509Certificate cert = context.getSecurityEnvironment().getCertificate(
                            context.getExtraneousProperties(),keyIdBytes, MessageConstants.THUMB_PRINT_TYPE);
                    if (!isSymmetric) {
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context), cert);
                    }
                    returnKey = cert.getPublicKey();
                    
                    if(strId != null){
                        byte [] encodedCert = cert.getEncoded();
                        context.getElementCache().put(strId,new OctectStreamData(new String(encodedCert)));
                    }
                    
                } else if(purpose == Purpose.SIGN || purpose == Purpose.DECRYPT){
                    returnKey =context.getSecurityEnvironment().getPrivateKey(
                            context.getExtraneousProperties(),
                            XMLUtil.getDecodedBase64EncodedData(referenceValue), MessageConstants.THUMB_PRINT_TYPE);
                }
            } else if(MessageConstants.KERBEROS_v5_APREQ_IDENTIFIER.equals(valueType)){
                //for policy verification
                SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                AuthenticationTokenPolicy.KerberosTokenBinding ktBinding = new AuthenticationTokenPolicy.KerberosTokenBinding();
                ktBinding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                skBinding.setKeyBinding(ktBinding);
                if(inferredKB == null){
                    context.getSecurityContext().setInferredKB(skBinding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                }
                // now get the key
                String algo = SecurityUtil.getSecretKeyAlgorithm(context.getAlgorithmSuite().getEncryptionAlgorithm());
                KerberosContext krbContext = context.getKerberosContext();
                if(krbContext != null){
                    String encodedRef = (String)context.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
                    if(!referenceValue.equals(encodedRef)){
                        throw new XWSSecurityException("SecretKey could not be obtained, Incorrect Kerberos Context found");
                    }
                    returnKey = krbContext.getSecretKey(algo);
                } else{
                    throw new XWSSecurityException("SecretKey could not be obtained, Kerberos Context not set");
                }
            } else if (MessageConstants.EncryptedKeyIdentifier_NS.equals(valueType)){
                //for policy verification
                SymmetricKeyBinding skBinding = new SymmetricKeyBinding();
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding = new AuthenticationTokenPolicy.X509CertificateBinding();
                x509Binding.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                skBinding.setKeyBinding(x509Binding);
                //TODO: ValueType not set on X509Binding
                if(inferredKB == null){
                    context.getSecurityContext().setInferredKB(skBinding);
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(skBinding);
                }
                // get the key
                String ekSha1RefValue = (String)context.getExtraneousProperty("EncryptedKeySHA1");
                Key secretKey = (Key)context.getExtraneousProperty("SecretKey");
                String keyRefValue = referenceValue;
                if(ekSha1RefValue != null && secretKey != null){
                    if (ekSha1RefValue.equals(keyRefValue)) {
                        returnKey = secretKey;
                        //Cannot determine whether the original key was X509 or PasswordDerivedKey
                        skBinding.usesEKSHA1KeyBinding(true);
                    }
                }else{
                    String message = "EncryptedKeySHA1 reference not correct";
                    logger.log(Level.SEVERE,"WSS1306:unsupported.KeyIdentifier.Reference.Type.encountered", new Object[] {message});
                    throw new KeySelectorException(message);
                }
            } else if (MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE.equals(valueType) ||
                    MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE.equals(valueType)) {
                //for policy verification
                IssuedTokenKeyBinding itkBinding = new IssuedTokenKeyBinding();
                if(inferredKB == null){
                    if (context.hasIssuedToken()){
                        context.getSecurityContext().setInferredKB(itkBinding);
                    } else{
                        context.getSecurityContext().setInferredKB(new AuthenticationTokenPolicy.SAMLAssertionBinding());
                    }
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(inferredKB)){
                    if(((DerivedTokenKeyBinding)inferredKB).getOriginalKeyBinding() == null)
                        ((DerivedTokenKeyBinding)inferredKB).setOriginalKeyBinding(itkBinding);
                    
                }
                // TODO:
                SecurityHeaderElement she = resolveToken(referenceValue,xc);
                if(she != null && she instanceof SAMLAssertion){
                    SAMLAssertion samlAssertion = (SAMLAssertion)she;
                    returnKey = samlAssertion.getKey();
                    if(strId != null && strId.length() >0){
                        Data data = new SSEData((SecurityElement)samlAssertion,false,context.getNamespaceContext());
                        context.getElementCache().put(strId,data);
                    }
                } else{
                    HashMap sentSamlKeys = (HashMap) context.getExtraneousProperty(MessageConstants.STORED_SAML_KEYS);
                    if(sentSamlKeys != null){
                        // for policy verification
                        context.getSecurityContext().setIsSAMLKeyBinding(true);
                        returnKey = (Key) sentSamlKeys.get(referenceValue);
                    }
                }
                
                if (context.hasIssuedToken() && returnKey != null){
                    SecurityTokenReference str = new SecurityTokenReference(context.getSOAPVersion());
                    com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier ki = new com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier(context.getSOAPVersion());
                    ki.setValueType(valueType);
                    ki.setReferenceValue(referenceValue);
                    str.setReference(ki);
                    SecurityUtil.initInferredIssuedTokenContext(context, str, returnKey);
                }
            } else {
                // assume SAML AssertionID without ValueType on KeyIdentifier
                // now assume its an X509Token
                returnKey = null;
            }
        } catch (XWSSecurityException ex) {
            logger.log(Level.SEVERE,"WSS1377.error.in.resolving.keyinfo",ex);
            throw new KeySelectorException(ex);
        } catch (URIReferenceException ex) {
            logger.log(Level.SEVERE,"WSS1377.error.in.resolving.keyinfo" ,ex);
            throw new KeySelectorException(ex);
        } catch (CertificateEncodingException ex) {
            logger.log(Level.SEVERE,"WSS1377.error.in.resolving.keyinfo" ,ex);
            throw new KeySelectorException(ex);
        }
        return returnKey;
        
    }
    
    //@@@FIXME: this should also work for key types other than DSA/RSA
    /**
     *
     * @param algURI
     * @param algName
     * @return
     */
    private static boolean algEquals(String algURI, String algName) {
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
    private static Key resolveUsernameToken(JAXBFilterProcessingContext wssContext, UsernameTokenHeader token, Purpose purpose, boolean isSymmetric)
        throws XWSSecurityException {
        String algo = wssContext.getAlgorithmSuite().getSymmetricKeyAlgorithm();
        AuthenticationTokenPolicy.UsernameTokenBinding untBinding = new AuthenticationTokenPolicy.UsernameTokenBinding();
        String decodedSalt = token.getSalt();
        if(decodedSalt == null){
            throw new XWSSecurityException("Salt retrieved from UsernameToken is null");
        }
        byte[] salt = null;
        try {
            salt = Base64.decode(decodedSalt);
        } catch (Base64DecodingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }        
        String password = null;
        try {
            password = wssContext.getSecurityEnvironment().authenticateUser(wssContext.getExtraneousProperties(), token.getUsernameValue());
        } catch (XWSSecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        if(password == null){
            throw new XWSSecurityException("Password retrieved from UsernameToken is null");
        }
        String iterate = token.getIterations();
        if(iterate == null){
            throw new XWSSecurityException("Value of Iterations  retrieved from UsernameToken is null");
        }
        int iterations = Integer.parseInt(iterate);
        PasswordDerivedKey pdk = new PasswordDerivedKey();
        SecretKey sKey = null;
        byte[] verifySignature = null;
        if ((purpose == Purpose.DECRYPT)) {
            salt[0] = MessageConstants.VALUE_FOR_ENCRYPTION;
            if (isSymmetric) {
                try {
                    verifySignature = pdk.generate160BitKey(password, iterations, salt);
                } catch (UnsupportedEncodingException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                untBinding.setSecretKey(verifySignature);
                sKey = untBinding.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(algo));
                untBinding.setSecretKey(sKey);
                wssContext.setUsernameTokenBinding(untBinding);
                byte[] secretKey = untBinding.getSecretKey().getEncoded();
                SecretKey key = pdk.generate16ByteKeyforEncryption(secretKey);
                sKey = key;
            } else {
                byte[] decSignature = null;
                try {
                    decSignature = pdk.generate160BitKey(password, iterations, salt);
                } catch (UnsupportedEncodingException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                byte[] keyof128Bits = new byte[16];
                for (int i = 0; i < 16; i++) {
                    keyof128Bits[i] = decSignature[i];
                }
                untBinding.setSecretKey(keyof128Bits);
                sKey = untBinding.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(algo));
                untBinding.setSecretKey(sKey);
            }            
        } else if (purpose == Purpose.VERIFY) {
            salt[0] = MessageConstants.VALUE_FOR_SIGNATURE;
            try {
                verifySignature = pdk.generate160BitKey(password, iterations, salt);
            } catch (UnsupportedEncodingException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            untBinding.setSecretKey(verifySignature);
            sKey = untBinding.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(algo));
            untBinding.setSecretKey(sKey);
            //return sKey;
           } else {
            //handles RequiredDerivedKeys case
              salt[0] = MessageConstants.VALUE_FOR_ENCRYPTION;
              byte[] key = null;
            try {
                key = pdk.generate160BitKey(password, iterations, salt);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(KeySelectorImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
              byte[] sKeyof16ByteLength = new byte[16];
              for(int i=0;i<16;i++){
                  sKeyof16ByteLength[i] = key[i];
              }
              untBinding.setSecretKey(sKeyof16ByteLength);
              sKey = untBinding.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(algo));    
          }
        return sKey;
}
    private static Key resolveX509Token(JAXBFilterProcessingContext context,
            X509BinarySecurityToken token, Purpose purpose, boolean isSymmetric) throws XWSSecurityException {
        X509Certificate cert = token.getCertificate();
        if(cert == null)
            cert = SOAPUtil.getCertificateFromToken(token);
        if (purpose == Purpose.VERIFY) {
            if (!isSymmetric) {
                context.getSecurityEnvironment().updateOtherPartySubject(
                        DefaultSecurityEnvironmentImpl.getSubject(context), cert);
            }
            return cert.getPublicKey();
        } else if(purpose == Purpose.SIGN || purpose == Purpose.DECRYPT) {
            return context.getSecurityEnvironment().getPrivateKey(
                    context.getExtraneousProperties(), cert);
        }
        return null;
    }
    
    private static Key resolveX509Data(JAXBFilterProcessingContext context,X509Data  x509Data,  Purpose purpose) throws KeySelectorException {
        
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
                        //return context.getSecurityEnvironment().getPublicKey(
                        //      context.getExtraneousProperties(),ski);
                        cert =
                                context.getSecurityEnvironment().getCertificate(
                                context.getExtraneousProperties(), ski);
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context), cert);
                        return cert.getPublicKey();
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
                        //return context.getSecurityEnvironment().getPublicKey(
                        //        context.getExtraneousProperties(), xis.getSerialNumber(), xis.getIssuerName());
                        cert = context.getSecurityEnvironment().getCertificate(
                                context.getExtraneousProperties(), xis.getSerialNumber(), xis.getIssuerName());
                        context.getSecurityEnvironment().updateOtherPartySubject(
                                DefaultSecurityEnvironmentImpl.getSubject(context),cert);
                        return cert.getPublicKey();
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
                    context.getSecurityEnvironment().updateOtherPartySubject(
                            DefaultSecurityEnvironmentImpl.getSubject(context),cert);
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
    
    protected static SecurityHeaderElement resolveToken(final String uri, XMLCryptoContext context) throws
            URIReferenceException, XWSSecurityException{
        URIDereferencer resolver = context.getURIDereferencer();
        
        URIReference uriRef = new URIReference(){
            public String getURI(){
                return uri;
            }
            
            public String getType(){
                return null;
            }
        };
        
        JAXBFilterProcessingContext wssContext = (JAXBFilterProcessingContext)context.get(MessageConstants.WSS_PROCESSING_CONTEXT);

        try{
            StreamWriterData data = (StreamWriterData)resolver.dereference(uriRef,context);
            //JAXBElement element = data.getJAXBElement();
            if(data == null){
                return null;
            }
            Object derefData = data.getDereferencedObject();
            SecurityHeaderElement she = null;
            if(derefData instanceof SecurityHeaderElement){
                she = (SecurityHeaderElement)derefData;
            }
            
            if(she == null){
                logger.log(Level.SEVERE,"WSS1304.FC_SECURITY_TOKEN_UNAVAILABLE");
                throw SOAPUtil.newSOAPFaultException(
                        MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                        "Referenced Security Token could not be retrieved",
                        null);
            }
            
            if(MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME.equals(she.getLocalPart())){
                BinarySecurityToken token = (BinarySecurityToken)she;
                if(MessageConstants.KERBEROS_V5_GSS_APREQ_1510.equals(token.getValueType()) ||
                        MessageConstants.KERBEROS_V5_GSS_APREQ.equals(token.getValueType())){
                    return (KerberosBinarySecurityToken)token;
                } else{
                    X509BinarySecurityToken x509bst = (X509BinarySecurityToken)token;
                    X509Certificate cert = null;
                    cert = x509bst.getCertificate();
                     // this one is directly from Message, since we  validate the X509BinarySecurityToken separately
                     // so this should not be done here
//                    if(!wssContext.getSecurityEnvironment().validateCertificate(cert, wssContext.getExtraneousProperties())){
//                        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
//                                "Certificate validation failed", null);
//                    }
                    return x509bst;
                }
            } else if(MessageConstants.ENCRYPTEDKEY_LNAME.equals(she.getLocalPart())){
                return (EncryptedKey)she;
            } else if (MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME.equals(she.getLocalPart())){
                return (SecurityContextToken)she;
            }  else if (MessageConstants.DERIVEDKEY_TOKEN_LNAME.equals(she.getLocalPart())){
                return (DerivedKeyToken)she;
            }else if(MessageConstants.SAML_ASSERTION_LNAME.equals(she.getLocalPart())){
                //TODO : update other party subject
                return she;
            }else if(MessageConstants.USERNAME_TOKEN_LNAME.equals(she.getLocalPart())){
                return she;
            }
        } catch(URIReferenceException ure){
            logger.log(Level.SEVERE,"WSS1304.FC_SECURITY_TOKEN_UNAVAILABLE",ure);
            throw SOAPUtil.newSOAPFaultException(
                    MessageConstants.WSSE_SECURITY_TOKEN_UNAVAILABLE,
                    "Referenced Security Token could not be retrieved",
                    ure);
        }
        
        if(logger.isLoggable(Level.SEVERE)){
            logger.log(Level.SEVERE,"WSS1305.UnSupported.security.token");
        }
        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_UNSUPPORTED_SECURITY_TOKEN, "A Unsupported token was provided ", null);
    }
    
    private static  boolean isSecurityTokenReference(JAXBElement reference){
        String local = reference.getName().getLocalPart();
        String uri = reference.getName().getNamespaceURI();
        if(MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME.equals(local) &&
                MessageConstants.WSSE_NS.equals(uri))
            return true;
        return false;
    }
    
   
    @SuppressWarnings("unchecked")
    private static Key resolveSCT(JAXBFilterProcessingContext wssContext, String scId, KeySelector.Purpose purpose) throws XWSSecurityException {
        
        IssuedTokenContext ctx = null;
        //String protocol = null;
        String protocol = wssContext.getWSSCVersion(wssContext.getSecurityPolicyVersion());
        if(wssContext.isClient()){
            SCTokenConfiguration config = new DefaultSCTokenConfiguration(protocol, scId, !wssContext.isExpired(), !wssContext.isInboundMessage());
            ctx =IssuedTokenManager.getInstance().createIssuedTokenContext(config, null);
            try{
                IssuedTokenManager.getInstance().getIssuedToken(ctx);
            }catch(WSTrustException e){
                throw new XWSSecurityException(e);
            }
            
            //Retrive the context from issuedTokenContextMap
//            Enumeration elements = wssContext.getIssuedTokenContextMap().elements();
//            while (elements.hasMoreElements()) {
//                IssuedTokenContext ictx = (IssuedTokenContext)elements.nextElement();
//                Object tok = ictx.getSecurityToken();
//                String ctxid = null;
//                
//                if (tok instanceof com.sun.xml.ws.security.SecurityContextToken) {
//                    ctxid = ((com.sun.xml.ws.security.SecurityContextToken)tok).getIdentifier().toString();
//                    if (ctxid.equals(scId)) {
//                        ctx = ictx;
//                        break;
//                    }
//                }
//            }
            if (ctx == null || ctx.getSecurityPolicy().isEmpty()) {
                // Return null as scId still needs to be resolved
                return null;
            }
        }else{
            //Retrive the context from Session Manager's cache
            ctx = ((SessionManager)wssContext.getExtraneousProperty("SessionManager")).getSecurityContext(scId, !wssContext.isExpired());
            com.sun.xml.ws.security.SecurityContextToken sct = (com.sun.xml.ws.security.SecurityContextToken)ctx.getSecurityToken();
            ctx.setSecurityToken(WSTrustElementFactory.newInstance(protocol).createSecurityContextToken(sct.getIdentifier(), sct.getInstance(), sct.getWsuId()));
        }                                
        
        //update otherparty subject with bootstrap credentials.
        Subject subj = ctx.getRequestorSubject();
        if (subj != null) {
            // subj will be null if this is the client side execution
            if (wssContext.getExtraneousProperty(MessageConstants.SCBOOTSTRAP_CRED_IN_SUBJ) == null) {
                //do it only once
                wssContext.getSecurityEnvironment().updateOtherPartySubject(
                        SecurityUtil.getSubject(wssContext.getExtraneousProperties()),subj);
                wssContext.getExtraneousProperties().put(MessageConstants.SCBOOTSTRAP_CRED_IN_SUBJ, "true");
            }
        }
        
        
        byte[] proofKey = null;
        //com.sun.xml.ws.security.SecurityContextToken scToken = (com.sun.xml.ws.security.SecurityContextToken)ctx.getSecurityToken();
        if(wssContext.getWSCInstance() != null){
            if(wssContext.isExpired()){
                proofKey = ctx.getProofKey();                
            }else{
                SecurityContextTokenInfo sctInstanceInfo = ctx.getSecurityContextTokenInfo();
                proofKey = sctInstanceInfo.getInstanceSecret(wssContext.getWSCInstance());
            }
        }else{
            proofKey = ctx.getProofKey();
        }
        wssContext.setExtraneousProperty(MessageConstants.INCOMING_SCT, ctx.getSecurityToken());
        
        
        if (proofKey == null) {
            throw new XWSSecurityException("Could not locate SecureConversation session for Id:" + scId);
        }
        
        String algo = "AES"; // hardcoding for now
        if (wssContext.getAlgorithmSuite() != null) {
            algo = SecurityUtil.getSecretKeyAlgorithm(wssContext.getAlgorithmSuite().getEncryptionAlgorithm());
        }
        SecretKeySpec key = new SecretKeySpec(proofKey, algo);
        return key;
    }
    
    private static Key resolveKerberosToken(JAXBFilterProcessingContext wssContext, KerberosBinarySecurityToken token) throws XWSSecurityException {
        
        String encodedRef = (String)wssContext.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
        
        if(encodedRef == null){
            try {
                byte[] krbSha1 = MessageDigest.getInstance("SHA-1").digest(token.getTokenValue());
                encodedRef = Base64.encode(krbSha1);
            } catch (NoSuchAlgorithmException nsae) {
                throw new XWSSecurityException(nsae);
            }
        }
        String algo = SecurityUtil.getSecretKeyAlgorithm(wssContext.getAlgorithmSuite().getEncryptionAlgorithm());
        KerberosContext krbContext = wssContext.getKerberosContext();
        
        if(krbContext == null){
            krbContext = wssContext.getSecurityEnvironment().doKerberosLogin(token.getTokenValue());
            wssContext.setKerberosContext(krbContext);
            try{
            wssContext.getSecurityEnvironment().updateOtherPartySubject(DefaultSecurityEnvironmentImpl.getSubject(wssContext), 
                    krbContext.getGSSContext().getSrcName(), krbContext.getDelegatedCredentials());
            } catch(GSSException gsse){
                throw new XWSSecurityException(gsse);
            }
        }
        wssContext.setExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE, encodedRef);
        return krbContext.getSecretKey(algo);
    }
}

