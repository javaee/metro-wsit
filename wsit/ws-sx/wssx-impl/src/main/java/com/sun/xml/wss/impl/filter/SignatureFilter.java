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
 * $Id: SignatureFilter.java,v 1.2 2010-10-21 15:37:28 snajper Exp $
 */

package com.sun.xml.wss.impl.filter;

import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;

import com.sun.xml.wss.impl.dsig.SignatureProcessor;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.HarnessUtil;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.ws.security.opt.impl.tokens.UsernameToken ;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.UsernameTokenBinding;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.logging.impl.filter.LogStringsMessages;

/**
 * Performs signature or verifies signature
 *
 * Message ANNOTATION is performed as follows:
 *
 *   if (complete policy resolution should happen)
 *       make DynamicPolicyCallback
 *   else
 *       // assumes feature binding component is statically specified -
 *       // including targets and canonicalization algorithm
 *       if (X509CertificateBinding)
 *           resolve certificate - make SignatureKeyCallback
 *           if (X509CertificateBinding has associated SymmetricKeyBinding)
 *               resolve symmetric key - make SymmetricKeyCallback
 *           else
 *           if (X509CertificateBinding has no associated bindings ||
 *               X509CertificateBinding has associated PrivateKeyBinding)
 *               create PrivateKeyBinding if req'd with private key associated with the certificate
 *           else
 *               throw Exception
 *       else
 *           throw Exception
 *
 *    call SignatureProcessor
 *
 * Message (signature verification) VALIDATION is performed as follows:
 *
 *   if (ADHOC processing mode)
 *       if (complete policy resolution should happen)
 *           make DynamicPolicyCallback
 *       call VerificationProcessor
 *   else
 *   if (POSTHOC or DEFAULT mode)
 *       call VerificationProcessor
 */
public class SignatureFilter {
    
    private static Logger log =  Logger.getLogger(
            LogDomainConstants.IMPL_FILTER_DOMAIN,
            LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
    
   /**
    * sets the username token in UsernameToken Binding,
    * creates secret key for signature and sets it in username token binding
    * @param context FilterProcessingContext
    * @param untBinding UsernameTokenBinding
    * @param firstByte int
    * @return binding UsernameTokenBinding
    * @throws com.sun.xml.wss.XWSSecurityException
    */
    public static UsernameTokenBinding createUntBinding(FilterProcessingContext context,UsernameTokenBinding untBinding, int firstByte) throws XWSSecurityException{
        UsernameTokenBinding binding = (UsernameTokenBinding)untBinding.clone();
        JAXBFilterProcessingContext opContext = (JAXBFilterProcessingContext) context;
        SignaturePolicy authPolicy = (SignaturePolicy) context.getSecurityPolicy();
        UsernameToken unToken = null;
        if (context.getusernameTokenBinding() == null) {
            unToken = new UsernameToken(opContext.getSOAPVersion());
        } else {
            if (untBinding.getUUID().equals(context.getusernameTokenBinding().getUUID())) {
                unToken = context.getusernameTokenBinding().getUsernameToken();
            } else {
                unToken = new UsernameToken(opContext.getSOAPVersion());
            }
        }
        try {
            binding = UsernameTokenDataResolver.setSaltandIterationsforUsernameToken(opContext, unToken, authPolicy, binding, firstByte);
        } catch (UnsupportedEncodingException ex) {
             throw new XWSSecurityException("error occurred while decoding the salt in username token",ex);
        } catch(XWSSecurityException ex){
            throw  ex;
        }
        if (binding.getUseNonce()&& unToken.getNonceValue() == null ) {
            unToken.setNonce(binding.getNonce());
            String creationTime = "";
            TimestampPolicy tPolicy = (TimestampPolicy) binding.getFeatureBinding();
            if(tPolicy == null)tPolicy = (TimestampPolicy) binding.newTimestampFeatureBinding();
            creationTime = tPolicy.getCreationTime();
            unToken.setCreationTime(creationTime);
        }
        if (binding.getUseCreated() && unToken.getCreatedValue() == null) {
            String creationTime = "";
            TimestampPolicy tPolicy = (TimestampPolicy) binding.getFeatureBinding();
            if(tPolicy == null)tPolicy = (TimestampPolicy) binding.newTimestampFeatureBinding();
            creationTime = tPolicy.getCreationTime();
            unToken.setCreationTime(creationTime);
        }
        binding.setUsernameToken(unToken);
        String dataEncAlgo = null;
        if (context.getAlgorithmSuite() != null) {
            dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
        } else {
            dataEncAlgo = MessageConstants.HMAC_SHA1_SIGMETHOD;
        }
        SecretKey sKey = binding.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
        binding.setSecretKey(sKey);
        return binding;
    }
    /**
     * creates the suitable key for each binding type and sets the binding in the context
     * @param context FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
     @SuppressWarnings("unchecked")
    public static void process(FilterProcessingContext context) throws XWSSecurityException {
        
        if (!context.isInboundMessage()) {
            
            WSSPolicy policy =(WSSPolicy) context.getSecurityPolicy();
            SignaturePolicy resolvedPolicy = (SignaturePolicy) policy;
            
            if (!context.makeDynamicPolicyCallback()) {

                WSSPolicy keyBinding = (WSSPolicy) ((SignaturePolicy) policy).getKeyBinding();
                 if(PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)){
                    DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
                    WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();

                     if(PolicyTypeUtil.usernameTokenBinding(originalKeyBinding)){
                        AuthenticationTokenPolicy.UsernameTokenBinding ckBindingClone =
                                (AuthenticationTokenPolicy.UsernameTokenBinding)originalKeyBinding.clone();
                        SymmetricKeyBinding skb = new SymmetricKeyBinding();
                        skb.setKeyBinding(ckBindingClone);
                        dtk.setOriginalKeyBinding(skb);
                        keyBinding = dtk;
                    }
                }
                //SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding) policy.getFeatureBinding();
                if (PolicyTypeUtil.usernameTokenBinding(keyBinding)) {
                    UsernameTokenBinding binding = createUntBinding(context,(UsernameTokenBinding)keyBinding,MessageConstants.VALUE_FOR_SIGNATURE);
                    context.setUsernameTokenBinding(binding);                    
                }else if (PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
                    try {
                        AuthenticationTokenPolicy.X509CertificateBinding binding = (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding.clone();
                        String certIdentifier = binding.getCertificateIdentifier();
                        String algorithm = binding.getKeyAlgorithm();
                        if(MessageConstants.HMAC_SHA1_SIGMETHOD.equals(algorithm)){
                            X509Certificate cert = context.getSecurityEnvironment().getCertificate(context.getExtraneousProperties(), certIdentifier, false);
                            binding.setX509Certificate(cert);
                        }else {
                            
                            if(certIdentifier == null || "".equals(certIdentifier)) {
                                
                                WSSPolicy ckBinding = (WSSPolicy) binding.getKeyBinding();
                                
                                if (ckBinding == null) {
                                    ckBinding = (WSSPolicy)binding.newPrivateKeyBinding();
                                }
                                
                                if (context.getSecurityEnvironment().getClass().getName().equals(
                                        "com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl")) {
                                    SignatureKeyCallback.PrivKeyCertRequest request =
                                            ((DefaultSecurityEnvironmentImpl)context.getSecurityEnvironment()).
                                            getDefaultPrivKeyCertRequest(context.getExtraneousProperties());
                                    
                                    binding.setX509Certificate(request.getX509Certificate());
                                    if(request.getX509Certificate() == null){
                                        log.log(Level.SEVERE, LogStringsMessages.WSS_1421_NO_DEFAULT_X_509_CERTIFICATE_PROVIDED());
                                        throw new XWSSecurityException("No default X509Certificate was provided");
                                    }
                                    ((PrivateKeyBinding) ckBinding).setPrivateKey(request.getPrivateKey());
                                }else {
                                    X509Certificate cert = context.getSecurityEnvironment().
                                            getDefaultCertificate(context.getExtraneousProperties());
                                    if(cert == null){
                                        log.log(Level.SEVERE, LogStringsMessages.WSS_1421_NO_DEFAULT_X_509_CERTIFICATE_PROVIDED());
                                        throw new XWSSecurityException("No default X509Certificate was provided");
                                    }
                                    binding.setX509Certificate(cert);
                                    PrivateKey pk = context.getSecurityEnvironment().getPrivateKey(
                                            context.getExtraneousProperties(), cert);
                                    ((PrivateKeyBinding) ckBinding).setPrivateKey(pk);
                                }
                                
                            } else {
                                
                                if (context.getSecurityEnvironment().getClass().getName().equals(
                                        "com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl")) {
                                    SignatureKeyCallback.AliasPrivKeyCertRequest request =
                                            ((DefaultSecurityEnvironmentImpl)context.getSecurityEnvironment()).
                                            getAliasPrivKeyCertRequest(certIdentifier);
                                    
                                    binding.setX509Certificate(request.getX509Certificate());
                                    if(request.getX509Certificate() == null){
                                        log.log(Level.SEVERE,LogStringsMessages.WSS_1421_NO_DEFAULT_X_509_CERTIFICATE_PROVIDED());
                                        throw new XWSSecurityException("No X509Certificate was provided");
                                    }
                                    
                                    WSSPolicy ckBinding = (WSSPolicy) binding.getKeyBinding();
                                    
                                    if (PolicyTypeUtil.privateKeyBinding(ckBinding)) {
                                        ((PrivateKeyBinding) ckBinding).setPrivateKey(request.getPrivateKey());
                                    } else {
                                        if (ckBinding == null) {
                                            // keyBinding un-defined
                                            
                                            ((PrivateKeyBinding) binding.newPrivateKeyBinding()).
                                                    setPrivateKey(request.getPrivateKey());
                                        } else {
                                            log.log(Level.SEVERE, LogStringsMessages.WSS_1416_UNSUPPORTED_KEYBINDING());
                                            throw new XWSSecurityException(
                                                    "Unsupported KeyBinding for X509CertificateBinding");
                                        }
                                    }
                                } else {
                                    // not handling symmetric key for provider
                                    X509Certificate cert = context.getSecurityEnvironment().
                                            getCertificate(
                                            context.getExtraneousProperties(), certIdentifier,true);
                                    binding.setX509Certificate(cert);
                                    WSSPolicy ckBinding = (WSSPolicy) binding.getKeyBinding();
                                    PrivateKey key = context.getSecurityEnvironment().getPrivateKey(
                                            context.getExtraneousProperties(), certIdentifier);
                                    
                                    if (PolicyTypeUtil.privateKeyBinding(ckBinding)) {
                                        ((PrivateKeyBinding) ckBinding).setPrivateKey(key);
                                    } else {
                                        if (ckBinding == null) {
                                            // keyBinding un-defined
                                            ((PrivateKeyBinding) binding.newPrivateKeyBinding()).
                                                    setPrivateKey(key);
                                        } else {
                                            log.log(Level.SEVERE, LogStringsMessages.WSS_1416_UNSUPPORTED_KEYBINDING());
                                            throw new XWSSecurityException(
                                                    "Unsupported KeyBinding for X509CertificateBinding");
                                        }
                                    }
                                    
                                }
                            }
                            
                        }
                        
                        context.setX509CertificateBinding(binding);
                        
                    } catch (Exception e) {
                        log.log(Level.SEVERE, LogStringsMessages.WSS_1417_EXCEPTION_PROCESSING_SIGNATURE(new Object[] {e.getMessage()}));
                        throw new XWSSecurityException(e);
                    }
                } else if(PolicyTypeUtil.kerberosTokenBinding(keyBinding)) {
                    AuthenticationTokenPolicy.KerberosTokenBinding binding = (AuthenticationTokenPolicy.KerberosTokenBinding)keyBinding.clone();
                    String algorithm = binding.getKeyAlgorithm();
                    
                    //String ktPolicyId = binding.getUUID();
                    String encodedRef = (String)context.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
                    KerberosContext krbContext = null;
                    if(encodedRef != null){
                        krbContext = context.getKerberosContext();
                    }
                    String dataEncAlgo = null;
                    if (context.getAlgorithmSuite() != null) {
                        dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                    } else {
                        dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                        // warn about using default
                    }
                    if(krbContext != null){
                        byte[] kerberosToken = krbContext.getKerberosToken();
                        binding.setTokenValue(kerberosToken);
                        
                        SecretKey sKey = krbContext.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                        binding.setSecretKey(sKey);
                    }else{
                        log.log(Level.SEVERE, LogStringsMessages.WSS_1423_KERBEROS_CONTEXT_NOTSET());
                        throw new XWSSecurityException("WSS1423.kerberos.context.notset");
                    }
                    
                    context.setKerberosTokenBinding(binding);
                } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
                    //resolvedPolicy = (SignaturePolicy)policy.clone();
                    keyBinding =(WSSPolicy) ((SignaturePolicy) policy).getKeyBinding();                    
                    AuthenticationTokenPolicy.SAMLAssertionBinding binding =
                            (AuthenticationTokenPolicy.SAMLAssertionBinding) keyBinding;
                    if(binding.getAssertion() != null || binding.getAssertionReader() != null || 
                            binding.getAuthorityBinding() != null){
                        binding.setAssertion((org.w3c.dom.Element)null);
                        binding.setAuthorityBinding(null);
                        binding.setAssertion((javax.xml.stream.XMLStreamReader)null);
                    }
                    
                    binding.isReadOnly(true);
                    
                    
                    DynamicApplicationContext dynamicContext =
                            new DynamicApplicationContext(context.getPolicyContext());
                    
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(false);
                    AuthenticationTokenPolicy.SAMLAssertionBinding resolvedSAMLBinding =
                           (AuthenticationTokenPolicy.SAMLAssertionBinding)
                    context.getExtraneousProperties().get(MessageConstants.SAML_ASSERTION_CLIENT_CACHE);
                    if (resolvedSAMLBinding == null) {
                        //try to obtain the HOK assertion
                        resolvedSAMLBinding =
                                context.getSecurityEnvironment().populateSAMLPolicy(context.getExtraneousProperties(), binding, dynamicContext);
                        context.getExtraneousProperties().put(MessageConstants.SAML_ASSERTION_CLIENT_CACHE, resolvedSAMLBinding);
                    }
                    if ((resolvedSAMLBinding.getAssertion() == null) &&
                            (resolvedSAMLBinding.getAuthorityBinding() == null) && (resolvedSAMLBinding.getAssertionReader() == null) ) {
                        log.log(Level.SEVERE, LogStringsMessages.WSS_1418_SAML_INFO_NOTSET());
                        throw new XWSSecurityException(
                                "None of SAML Assertion, SAML AuthorityBinding information was set into " +
                                " the Policy by the CallbackHandler");
                    }
                    
                    policy.setKeyBinding(resolvedSAMLBinding);
                    resolvedPolicy = (SignaturePolicy)policy;                  
                    
                }else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
                    try {
                        
                        String dataEncAlgo = null;
                        if (context.getAlgorithmSuite() != null) {
                            dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                        } else {
                            dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                            // warn about using default
                        }
                        
                        SymmetricKeyBinding binding = (SymmetricKeyBinding)keyBinding.clone();
                        
                        String keyIdentifier = binding.getKeyIdentifier();
                        SecretKey sKey = null;
                        
                        WSSPolicy ckBinding = (WSSPolicy) binding.getKeyBinding();
                        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
                        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
                        boolean wss10 = !wss11Sender;
                        boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getReceivedSecret(context) != null);
                        if (PolicyTypeUtil.usernameTokenBinding(ckBinding)) {
                            try {
                                if (!sendEKSHA1) {
                                    AuthenticationTokenPolicy.UsernameTokenBinding untbinding = createUntBinding(context, (UsernameTokenBinding) ckBinding, MessageConstants.VALUE_FOR_ENCRYPTION);
                                    context.setUsernameTokenBinding(untbinding);
                                    sKey = untbinding.getSecretKey();
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE, LogStringsMessages.WSS_1433_ERROR_EXTRACTING_USERNAMETOKEN(), e);
                                throw new XWSSecurityException(e);
                            }
                        }else if (PolicyTypeUtil.x509CertificateBinding(ckBinding)) {
                            try {
                                if (!sendEKSHA1) {
                                    AuthenticationTokenPolicy.X509CertificateBinding ckBindingClone =
                                            (AuthenticationTokenPolicy.X509CertificateBinding)ckBinding.clone();
                                    String certIdentifier = ckBindingClone.getCertificateIdentifier();
                                    X509Certificate cert = context.getSecurityEnvironment().
                                            getCertificate(context.getExtraneousProperties(), certIdentifier, false);
                                    ckBindingClone.setX509Certificate(cert);
                                    context.setX509CertificateBinding(ckBindingClone);
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE, LogStringsMessages.WSS_1413_ERROR_EXTRACTING_CERTIFICATE(), e);
                                throw new XWSSecurityException(e);
                            }
                            
                        } else if(PolicyTypeUtil.kerberosTokenBinding(ckBinding)){
                            AuthenticationTokenPolicy.KerberosTokenBinding ckBindingClone =
                                    (AuthenticationTokenPolicy.KerberosTokenBinding)ckBinding;
                            //String ktPolicyId = ckBindingClone.getUUID();
                            String encodedRef = (String)context.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
                            KerberosContext krbContext = null;
                            if(encodedRef != null){
                                krbContext = context.getKerberosContext();
                            }
                            if(krbContext != null){
                                byte[] kerberosToken = krbContext.getKerberosToken();
                                ckBindingClone.setTokenValue(kerberosToken);
                                
                                sKey = krbContext.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                                ckBindingClone.setSecretKey(sKey);
                            }else{
                                log.log(Level.SEVERE, LogStringsMessages.WSS_1423_KERBEROS_CONTEXT_NOTSET());
                                throw new XWSSecurityException("WSS1423.kerberos.context.notset");
                            }
                            context.setKerberosTokenBinding(ckBindingClone);
                        }
                        if((!PolicyTypeUtil.kerberosTokenBinding(ckBinding))){
                            if(!binding.getKeyIdentifier().equals(MessageConstants._EMPTY)){
                                sKey = context.getSecurityEnvironment().getSecretKey(
                                        context.getExtraneousProperties(),
                                        keyIdentifier, true);
                            } else if(sendEKSHA1){
                                sKey = getReceivedSecret(context);
                            }else if(wss11Sender || wss10){
                                
                                sKey =  SecurityUtil.generateSymmetricKey(dataEncAlgo);
                            }
                        }
                        
                        binding.setSecretKey(sKey);
                        context.setSymmetricKeyBinding(binding);
                    } catch (Exception e) {
                        //TODO: this error message should come only in Symm Keystore case
                        log.log(Level.SEVERE, LogStringsMessages.WSS_1414_ERROR_EXTRACTING_SYMMETRICKEY(new Object[] { e.getMessage()}));
                        throw new XWSSecurityException(e);
                    }
                } else if (PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
                    IssuedTokenKeyBinding itkb = (IssuedTokenKeyBinding)keyBinding;
                    SecurityUtil.resolveIssuedToken(context, itkb);
                    
                } else if (PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)) {
                    
                    DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
                    WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
                    
                    if ( PolicyTypeUtil.symmetricKeyBinding(originalKeyBinding)) {
                        
                        String dataEncAlgo = null;
                        if (context.getAlgorithmSuite() != null) {
                            dataEncAlgo = context.getAlgorithmSuite().getEncryptionAlgorithm();
                        } else {
                            dataEncAlgo = MessageConstants.DEFAULT_DATA_ENC_ALGO;
                            // warn about using default
                        }
                        
                        SymmetricKeyBinding symmBinding = (SymmetricKeyBinding)originalKeyBinding.clone();
                        SecretKey sKey = null;
                        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
                        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
                        boolean wss10 = !wss11Sender;
                        boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getReceivedSecret(context) != null);
                        
                        WSSPolicy ckBinding = (WSSPolicy) originalKeyBinding.getKeyBinding();
                        if (PolicyTypeUtil.usernameTokenBinding(ckBinding)) {
                            try {
                                if (!sendEKSHA1) {
                                    AuthenticationTokenPolicy.UsernameTokenBinding untbinding = createUntBinding(context, (UsernameTokenBinding) ckBinding, MessageConstants.VALUE_FOR_ENCRYPTION);
                                    context.setUsernameTokenBinding(untbinding);
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE,LogStringsMessages.WSS_1433_ERROR_EXTRACTING_USERNAMETOKEN(), e);
                                throw new XWSSecurityException(e);
                            }

                        } else if (PolicyTypeUtil.x509CertificateBinding(ckBinding)) {
                            try {
                                if (!sendEKSHA1) {
                                    AuthenticationTokenPolicy.X509CertificateBinding ckBindingClone =
                                            (AuthenticationTokenPolicy.X509CertificateBinding)ckBinding.clone();
                                    String certIdentifier = ckBindingClone.getCertificateIdentifier();
                                    X509Certificate cert = context.getSecurityEnvironment().
                                            getCertificate(context.getExtraneousProperties(), certIdentifier, false);
                                    ckBindingClone.setX509Certificate(cert);
                                    context.setX509CertificateBinding(ckBindingClone);
                                }
                            } catch (Exception e) {
                                log.log(Level.SEVERE, LogStringsMessages.WSS_1413_ERROR_EXTRACTING_CERTIFICATE(), e);
                                throw new XWSSecurityException(e);
                            }
                        } else if(PolicyTypeUtil.kerberosTokenBinding(ckBinding)){
                            AuthenticationTokenPolicy.KerberosTokenBinding ckBindingClone =
                                    (AuthenticationTokenPolicy.KerberosTokenBinding)ckBinding;
                            String encodedRef = (String)context.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
                            KerberosContext krbContext = null;
                            if(encodedRef != null){
                                krbContext = context.getKerberosContext();
                            }
                            if(krbContext != null){
                                byte[] kerberosToken = krbContext.getKerberosToken();
                                ckBindingClone.setTokenValue(kerberosToken);
                                sKey = krbContext.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                                ckBindingClone.setSecretKey(sKey);
                            } else{
                                log.log(Level.SEVERE, LogStringsMessages.WSS_1423_KERBEROS_CONTEXT_NOTSET());
                                throw new XWSSecurityException("WSS1423.kerberos.context.notset");
                            }
                            context.setKerberosTokenBinding(ckBindingClone);
                        }
                        if(!PolicyTypeUtil.kerberosTokenBinding(ckBinding)){
                            if(sendEKSHA1){
                                sKey = getReceivedSecret(context);
                            }else if(wss11Sender || wss10){
                                sKey =  SecurityUtil.generateSymmetricKey(dataEncAlgo);
                            }
                        }
                        symmBinding.setSecretKey(sKey);
                        context.setSymmetricKeyBinding(symmBinding);
                    } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(originalKeyBinding)) {
                        // resolve the ProofKey here and set it into ProcessingContext
                        SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)originalKeyBinding;
                        SecurityUtil.resolveSCT(context, sctBinding);
                    } else if (PolicyTypeUtil.issuedTokenKeyBinding(originalKeyBinding)) {
                        IssuedTokenKeyBinding itkb = (IssuedTokenKeyBinding)originalKeyBinding;
                        SecurityUtil.resolveIssuedToken(context, itkb);
                    }
                    
                } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)) {
                    // resolve the ProofKey here and set it into ProcessingContext
                    SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)keyBinding;
                    SecurityUtil.resolveSCT(context, sctBinding);
                } else if (PolicyTypeUtil.keyValueTokenBinding(keyBinding)) {
                    // resolve the ProofKey here and set it into ProcessingContext
                    AuthenticationTokenPolicy.KeyValueTokenBinding binding = (AuthenticationTokenPolicy.KeyValueTokenBinding)keyBinding.clone();
                }  else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_1419_UNSUPPORTED_KEYBINDING_SIGNATURE());
                    throw new XWSSecurityException("Unsupported KeyBinding for SignaturePolicy");
                }
            } else {
                //resolvedPolicy = (SignaturePolicy)policy.clone();
                ((SignaturePolicy)policy).isReadOnly(true);
                
                try {
                    DynamicApplicationContext dynamicContext =
                            new DynamicApplicationContext(context.getPolicyContext());
                    
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(false);
                    // TODO: set runtime context for making callback
                    DynamicPolicyCallback dynamicCallback = new DynamicPolicyCallback(
                            policy, dynamicContext);
                    ProcessingContext.copy(dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                    HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                            context.getSecurityEnvironment().getCallbackHandler());
                    resolvedPolicy = (SignaturePolicy)dynamicCallback.getSecurityPolicy();
                    
                } catch (Exception e) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_1420_DYNAMIC_POLICY_SIGNATURE(new Object[] {e.getMessage()}));
                    throw new XWSSecurityException(e);
                }
            }
            
            context.setSecurityPolicy(resolvedPolicy);
            
            sign(context);
            
        } else {
            
            if ( context.makeDynamicPolicyCallback()) {
                WSSPolicy policy =(WSSPolicy) context.getSecurityPolicy();
                SignaturePolicy resolvedPolicy = null;
                ((SignaturePolicy)policy).isReadOnly(true);
                
                try {
                    DynamicApplicationContext dynamicContext =
                            new DynamicApplicationContext(context.getPolicyContext());
                    
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(true);
                    // TODO: set runtime context for making callback
                    DynamicPolicyCallback dynamicCallback = new DynamicPolicyCallback(
                            policy, dynamicContext);
                    ProcessingContext.copy(dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                    HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                            context.getSecurityEnvironment().getCallbackHandler());
                    
                    resolvedPolicy = (SignaturePolicy)dynamicCallback.getSecurityPolicy();
                } catch (Exception e) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_1420_DYNAMIC_POLICY_SIGNATURE(new Object[] {e.getMessage()}));
                    throw new XWSSecurityException(e);
                }
                context.setSecurityPolicy(resolvedPolicy);
            }
            
            SignatureProcessor.verify(context);
        }
    }
    /**
     * performs signature processing 
     * @param context com.sun.xml.wss.impl.FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private static void sign(com.sun.xml.wss.impl.FilterProcessingContext context)
    throws XWSSecurityException{
        if(context instanceof JAXBFilterProcessingContext)
            com.sun.xml.ws.security.opt.impl.dsig.SignatureProcessor.sign((JAXBFilterProcessingContext)context);
        else
            SignatureProcessor.sign(context);
        
    }
    /**
     * gets the secret key from the context which will be used for handling
     * EKSHA1 value for sending response
     * @param context com.sun.xml.wss.impl.FilterProcessingContext
     * @return sKey SecretKey
     */
    private static SecretKey getReceivedSecret(com.sun.xml.wss.impl.FilterProcessingContext context) {
        SecretKey sKey = null;
        sKey = (javax.crypto.SecretKey)context.getExtraneousProperty(MessageConstants.SECRET_KEY_VALUE);
        return sKey;
    }
    
}
