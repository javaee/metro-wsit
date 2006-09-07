/**
 * $Id: SignatureFilter.java,v 1.1.8.1 2006-09-07 16:37:35 kumarjayanti Exp $
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
package com.sun.xml.wss.impl.filter;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.policy.mls.WSSKeyBindingExtension;

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

import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;


import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;

import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.impl.filter.TimestampFilter;
import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.impl.MessageConstants;

import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.SubjectAccessor;

import org.w3c.dom.Element;

import javax.security.auth.Subject;
import java.util.Set;
import java.util.Iterator;

import com.sun.xml.wss.core.SecurityContextTokenImpl;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;

import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

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
     * @param context FilterProcessingContext
     *
     * @throws XWSSecurityException
     */
    public static void process(FilterProcessingContext context) throws XWSSecurityException {
        
        if (!context.isInboundMessage()) {
            
            WSSPolicy policy =(WSSPolicy) context.getSecurityPolicy();
            SignaturePolicy resolvedPolicy = (SignaturePolicy) policy;
            
            if (!context.makeDynamicPolicyCallback()) {

                WSSPolicy keyBinding =(WSSPolicy) ((SignaturePolicy) policy).getKeyBinding();
                SignaturePolicy.FeatureBinding featureBinding = (SignaturePolicy.FeatureBinding)policy.getFeatureBinding();


                if (PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
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
                                        throw new XWSSecurityException("No default X509Certificate was provided");
                                    }
                                    ((PrivateKeyBinding) ckBinding).setPrivateKey(request.getPrivateKey());
                                }else {
                                    X509Certificate cert = context.getSecurityEnvironment().
                                            getDefaultCertificate(context.getExtraneousProperties());
                                    if(cert == null){
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
                                            log.log(Level.SEVERE, "WSS1416.unsupported.keybinding");
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
                                            log.log(Level.SEVERE, "WSS1416.unsupported.keybinding");
                                            throw new XWSSecurityException(
                                                    "Unsupported KeyBinding for X509CertificateBinding");
                                        }
                                    }
                                    
                                }
                            }
                            
                        }
                        
                        context.setX509CertificateBinding(binding);
                        
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "WSS1417.exception.processing.signature",
                                new Object[] {e.getMessage()} );                                                                                 
                        throw new XWSSecurityException(e);
                    }
                } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
                    //resolvedPolicy = (SignaturePolicy)policy.clone();
                    keyBinding =(WSSPolicy) ((SignaturePolicy) policy).getKeyBinding();
                    
                    AuthenticationTokenPolicy.SAMLAssertionBinding binding =
                        (AuthenticationTokenPolicy.SAMLAssertionBinding) keyBinding;
                    binding.isReadOnly(true);
                    
                    
                    DynamicApplicationContext dynamicContext =
                    new DynamicApplicationContext(context.getPolicyContext());
                    
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(false);

                    //try to obtain the HOK assertion
                    AuthenticationTokenPolicy.SAMLAssertionBinding resolvedSAMLBinding = 
                        context.getSecurityEnvironment().populateSAMLPolicy(context.getExtraneousProperties(), binding, dynamicContext);

                    if ((resolvedSAMLBinding.getAssertion() == null) && 
                        (resolvedSAMLBinding.getAuthorityBinding() == null)) {
                        log.log(Level.SEVERE, "WSS1418.saml.info.notset");                     
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
                        if (PolicyTypeUtil.x509CertificateBinding(ckBinding)) {
                            try {
                                AuthenticationTokenPolicy.X509CertificateBinding ckBindingClone = 
                                        (AuthenticationTokenPolicy.X509CertificateBinding)ckBinding.clone();
                                String certIdentifier = ckBindingClone.getCertificateIdentifier();
                                X509Certificate cert = context.getSecurityEnvironment().
                                        getCertificate(context.getExtraneousProperties(), certIdentifier, false);
                                ckBindingClone.setX509Certificate(cert);
                                context.setX509CertificateBinding(ckBindingClone);
                            } catch (Exception e) {
                                log.log(Level.SEVERE, "WSS1413.error.extracting.certificate", e);
                                throw new XWSSecurityException(e);
                            }
                        }
                        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
                        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
                        boolean wss10 = !wss11Sender;
                        boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getReceivedSecret(context) != null);
                        if(!binding.getKeyIdentifier().equals(MessageConstants._EMPTY)){
                            sKey = context.getSecurityEnvironment().getSecretKey(
                            context.getExtraneousProperties(),
                            keyIdentifier, true);
                        } else if(sendEKSHA1){
                           sKey = getReceivedSecret(context);
                        }else if(wss11Sender || wss10){

                            sKey =  SecurityUtil.generateSymmetricKey(dataEncAlgo);
                        }
                        
                        binding.setSecretKey(sKey);
                        context.setSymmetricKeyBinding(binding);
                    } catch (Exception e) {
                        //TODO: this error message should come only in Symm Keystore case
                        //log.log(Level.SEVERE, "WSS1414.error.extracting.symmetrickey", 
                         //   new Object[] { e.getMessage()});
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
                        
                        WSSPolicy ckBinding = (WSSPolicy) originalKeyBinding.getKeyBinding();
                        if (PolicyTypeUtil.x509CertificateBinding(ckBinding)) {
                            try {
                                AuthenticationTokenPolicy.X509CertificateBinding ckBindingClone = 
                                        (AuthenticationTokenPolicy.X509CertificateBinding)ckBinding.clone();
                                String certIdentifier = ckBindingClone.getCertificateIdentifier();
                                X509Certificate cert = context.getSecurityEnvironment().
                                        getCertificate(context.getExtraneousProperties(), certIdentifier, false);
                                ckBindingClone.setX509Certificate(cert);
                                context.setX509CertificateBinding(ckBindingClone);
                            } catch (Exception e) {
                                log.log(Level.SEVERE, "WSS1413.error.extracting.certificate", e);
                                throw new XWSSecurityException(e);
                            }
                        }
                        
                        boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
                        boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
                        boolean wss10 = !wss11Sender;
                        boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getReceivedSecret(context) != null);
                        if(sendEKSHA1){
                           sKey = getReceivedSecret(context);
                        }else if(wss11Sender || wss10){
                            sKey =  SecurityUtil.generateSymmetricKey(dataEncAlgo);
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
                } else {
                        log.log(Level.SEVERE, "WSS1419.unsupported.keybinding.signature");                     
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
                    ProcessingContext.copy (dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                    HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                          context.getSecurityEnvironment().getCallbackHandler());
                    resolvedPolicy = (SignaturePolicy)dynamicCallback.getSecurityPolicy();

                } catch (Exception e) {
                    log.log(Level.SEVERE, "WSS1420.dynamic.policy.signature",
                            new Object[] {e.getMessage()});                     
                    throw new XWSSecurityException(e);
                }
            }

            context.setSecurityPolicy(resolvedPolicy);
            
            SignatureProcessor.sign(context);
            
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
                    ProcessingContext.copy (dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                    HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                          context.getSecurityEnvironment().getCallbackHandler());

                    resolvedPolicy = (SignaturePolicy)dynamicCallback.getSecurityPolicy();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "WSS1420.dynamic.policy.signature",
                            new Object[] {e.getMessage()});                     
                    throw new XWSSecurityException(e);
                }
                context.setSecurityPolicy(resolvedPolicy);
            }                    

            SignatureProcessor.verify(context);
        }
    }
    
    private static SecretKey getReceivedSecret(FilterProcessingContext context){
        SecretKey sKey = null;
        sKey = (javax.crypto.SecretKey)context.getExtraneousProperty(MessageConstants.SECRET_KEY_VALUE);
        return sKey;
    }
    
 }
