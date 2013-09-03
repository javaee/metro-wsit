/**
 * $Id: EncryptionFilter.java,v 1.15.4.1 2010/06/01 10:30:00 sm228678 Exp $
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
package com.sun.xml.wss.impl.filter;

import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import java.security.cert.X509Certificate;
import javax.crypto.SecretKey;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.HarnessUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.apachecrypto.DecryptionProcessor;
import com.sun.xml.wss.impl.apachecrypto.EncryptionProcessor;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;

import org.w3c.dom.Element;

/**
 * Performs encryption or decryption
 *
 * Message ANNOTATION is performed as follows:
 *
 *   if (complete policy resolution should happen)
 *       make DynamicPolicyCallback
 *   else
 *       // assumes feature binding component is statically specified -
 *       // including targets and canonicalization algorithm
 *       if (X509CertificateBinding)
 *           resolve certificate - make EncryptionKeyCallback
 *       else
 *       if (SymmetricKeyBinding)
 *           resolve symmetrick key - make SymmetricKeyCallback
 *       else
 *           throw Exception
 *   call EncryptionProcessor
 *
 * Message (decryption) VALIDATION is performed as follows:
 *
 *   if (ADHOC processing mode)
 *       if (complete policy resolution should happen)
 *           make DynamicPolicyCallback
 *       call DecryptionProcessor
 *   else
 *   if (POSTHOC or DEFAULT mode)
 *       call DecryptionProcessor
 */
public class EncryptionFilter {
    
    
    protected static final Logger log =  Logger.getLogger(
            LogDomainConstants.IMPL_FILTER_DOMAIN,
            LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
    
    /**
     * @param context FilterProcessingContext
     *
     * @throws XWSSecurityException
     */
    public static void process(FilterProcessingContext context) throws XWSSecurityException {
        
        if (!context.isInboundMessage()) {
            
            EncryptionPolicy policy = (EncryptionPolicy)context.getSecurityPolicy();
            EncryptionPolicy resolvedPolicy = (EncryptionPolicy)policy;
            
            boolean wss11Receiver = "true".equals(context.getExtraneousProperty("EnableWSS11PolicyReceiver"));
            boolean wss11Sender = "true".equals(context.getExtraneousProperty("EnableWSS11PolicySender"));
            boolean sendEKSHA1 =  wss11Receiver && wss11Sender && (getReceivedSecret(context) != null);
            boolean wss10 = !wss11Sender;
            
            if (!context.makeDynamicPolicyCallback()) {
                WSSPolicy keyBinding = (WSSPolicy) policy.getKeyBinding();
                String dataEncAlgo = MessageConstants.TRIPLE_DES_BLOCK_ENCRYPTION;
                
                EncryptionPolicy.FeatureBinding featureBinding =
                        (EncryptionPolicy.FeatureBinding) policy.getFeatureBinding();
                String tmp = featureBinding.getDataEncryptionAlgorithm();
                if (tmp == null || "".equals(tmp)) {
                    if (context.getAlgorithmSuite() != null) {
                        tmp = context.getAlgorithmSuite().getEncryptionAlgorithm();
                    } else {
                        // warn that no dataEncAlgo was set
                    }
                }
                
                if(tmp != null && !"".equals(tmp)){
                    dataEncAlgo = tmp;
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
                
                if (PolicyTypeUtil.x509CertificateBinding(keyBinding)) {
                    try {
                        AuthenticationTokenPolicy.X509CertificateBinding binding =
                                (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding.clone();
                        
                        String certIdentifier = binding.getCertificateIdentifier();
                        
                        X509Certificate cert = context.getSecurityEnvironment().
                                getCertificate(context.getExtraneousProperties(), certIdentifier, false);
                        binding.setX509Certificate(cert);
                        
                        context.setX509CertificateBinding(binding);
                        
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "WSS1413.error.extracting.certificate", e);
                        throw new XWSSecurityException(e);
                    }
                } else if(PolicyTypeUtil.kerberosTokenBinding(keyBinding)) {
                    AuthenticationTokenPolicy.KerberosTokenBinding binding = (AuthenticationTokenPolicy.KerberosTokenBinding)keyBinding.clone();
                    //String ktPolicyId = binding.getUUID();
                    String encodedRef = (String)context.getExtraneousProperty(MessageConstants.KERBEROS_SHA1_VALUE);
                    KerberosContext krbContext = null;
                    if(encodedRef != null){
                        krbContext = context.getKerberosContext();
                    }
                    if(krbContext != null){
                        byte[] kerberosToken = krbContext.getKerberosToken();
                        binding.setTokenValue(kerberosToken);
                        
                        SecretKey sKey = krbContext.getSecretKey(SecurityUtil.getSecretKeyAlgorithm(dataEncAlgo));
                        binding.setSecretKey(sKey);
                    }else{
                        log.log(Level.SEVERE, "WSS1423.kerberos.context.notset");
                        throw new XWSSecurityException("WSS1423.kerberos.context.notset");
                    }
                    context.setKerberosTokenBinding(binding);
                } else if (PolicyTypeUtil.symmetricKeyBinding(keyBinding)) {
                    try {
                        SymmetricKeyBinding binding = (SymmetricKeyBinding)keyBinding.clone();
                        
                        String keyIdentifier = binding.getKeyIdentifier();
                        SecretKey sKey = null;
                        
                        WSSPolicy ckBinding = (WSSPolicy) binding.getKeyBinding();
                        if (PolicyTypeUtil.x509CertificateBinding(ckBinding)) {
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
                                log.log(Level.SEVERE, "WSS1413.error.extracting.certificate", e);
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
                            } else{
                                log.log(Level.SEVERE, "WSS1423.kerberos.context.notset");
                                throw new XWSSecurityException("WSS1423.kerberos.context.notset");
                            }
                            context.setKerberosTokenBinding(ckBindingClone);
                        }
                        
                        if(!PolicyTypeUtil.kerberosTokenBinding(ckBinding)){
                            if(!keyIdentifier.equals(MessageConstants._EMPTY)){
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
                        log.log(Level.SEVERE, "WSS1414.error.extracting.symmetrickey",
                                new Object[] { e.getMessage()});
                        throw new XWSSecurityException(e);
                    }
                } else if (PolicyTypeUtil.samlTokenPolicy(keyBinding)) {
                    
                     //resolvedPolicy = (EncryptionPolicy)policy.clone();
                    keyBinding =(WSSPolicy) ((EncryptionPolicy) policy).getKeyBinding();

                    DynamicApplicationContext dynamicContext =
                            new DynamicApplicationContext(context.getPolicyContext());
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(false);

                    AuthenticationTokenPolicy.SAMLAssertionBinding binding =
                            (AuthenticationTokenPolicy.SAMLAssertionBinding)keyBinding;
                    binding.isReadOnly(true);

                    AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                            new AuthenticationTokenPolicy.SAMLAssertionBinding();

                    if (context.getExtraneousProperty(MessageConstants.INCOMING_SAML_ASSERTION) == null) {
                        AuthenticationTokenPolicy.SAMLAssertionBinding resolvedSAMLBinding =
                           (AuthenticationTokenPolicy.SAMLAssertionBinding)
                           context.getExtraneousProperties().get(MessageConstants.SAML_ASSERTION_CLIENT_CACHE);
                        if (resolvedSAMLBinding == null) {
                            //try to obtain the HOK assertion
                            resolvedSAMLBinding =
                                    context.getSecurityEnvironment().populateSAMLPolicy(context.getExtraneousProperties(), binding, dynamicContext);
                            context.getExtraneousProperties().put(MessageConstants.SAML_ASSERTION_CLIENT_CACHE, resolvedSAMLBinding);
                            samlBinding = resolvedSAMLBinding;
                        }
                    }else{
                        Object assertion = context.getExtraneousProperty(MessageConstants.INCOMING_SAML_ASSERTION);
                        if(assertion instanceof Element){
                            samlBinding.setAssertion((Element)assertion);
                        }
                    }

                    policy.setKeyBinding(samlBinding);
                    resolvedPolicy = (EncryptionPolicy)policy;
                } else if (PolicyTypeUtil.secureConversationTokenKeyBinding(keyBinding)) {
                    // resolve the ProofKey here and set it into ProcessingContext
                    SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)keyBinding;
                    SecurityUtil.resolveSCT(context, sctBinding);
                    
                } else if (PolicyTypeUtil.issuedTokenKeyBinding(keyBinding)) {
                    IssuedTokenKeyBinding itkb = (IssuedTokenKeyBinding)keyBinding;
                    SecurityUtil.resolveIssuedToken(context, itkb);
                } else if (PolicyTypeUtil.derivedTokenKeyBinding(keyBinding)) {
                    DerivedTokenKeyBinding dtk = (DerivedTokenKeyBinding)keyBinding.clone();
                    WSSPolicy originalKeyBinding = dtk.getOriginalKeyBinding();
                    
                    if ( PolicyTypeUtil.symmetricKeyBinding(originalKeyBinding)) {
                        SymmetricKeyBinding symmBinding = (SymmetricKeyBinding)originalKeyBinding.clone();
                        SecretKey sKey = null;
                        
                        WSSPolicy ckBinding = (WSSPolicy) originalKeyBinding.getKeyBinding();
                        if (PolicyTypeUtil.x509CertificateBinding(ckBinding)) {
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
                                log.log(Level.SEVERE, "WSS1413.error.extracting.certificate", e);
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
                                log.log(Level.SEVERE, "WSS1423.kerberos.context.notset");
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
                    } else if ( PolicyTypeUtil.secureConversationTokenKeyBinding(originalKeyBinding)) {
                        // resolve the ProofKey here and set it into ProcessingContext
                        SecureConversationTokenKeyBinding sctBinding = (SecureConversationTokenKeyBinding)originalKeyBinding;
                        SecurityUtil.resolveSCT(context, sctBinding);
                    } else if ( PolicyTypeUtil.issuedTokenKeyBinding(originalKeyBinding)) {
                        IssuedTokenKeyBinding itkb = (IssuedTokenKeyBinding)originalKeyBinding;
                        SecurityUtil.resolveIssuedToken(context, itkb);
                    }
                } else {
                    log.log(Level.SEVERE,"WSS1422.unsupported.keybinding.EncryptionPolicy");
                    throw new XWSSecurityException("Unsupported KeyBinding for EncryptionPolicy");
                }
                
            } else {
                try {
                    //resolvedPolicy = (EncryptionPolicy)policy.clone();
                    ((EncryptionPolicy)policy).isReadOnly(true);
                    
                    
                    DynamicApplicationContext dynamicContext =
                            new DynamicApplicationContext(context.getPolicyContext());
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(false);
                    // TODO: copy runtime context for making dynamic callback
                    DynamicPolicyCallback dynamicCallback = new DynamicPolicyCallback(
                            policy, dynamicContext);
                    ProcessingContext.copy(dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                    HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                            context.getSecurityEnvironment().getCallbackHandler());
                    
                    resolvedPolicy = (EncryptionPolicy)dynamicCallback.getSecurityPolicy();
                    
                } catch (Exception e) {
                    log.log(Level.SEVERE, "WSS1412.error.processing.dynamicpolicy",
                            new Object[] { e.getMessage()});
                    throw new XWSSecurityException(e);
                }
            }
            
            context.setSecurityPolicy(resolvedPolicy);
            encrypt(context);
            
        } else {
            
            if ( context.makeDynamicPolicyCallback()) {
                WSSPolicy policy =(WSSPolicy) context.getSecurityPolicy();
                EncryptionPolicy resolvedPolicy = null;
                
                try {
                    ((EncryptionPolicy)policy).isReadOnly(true);
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
                    
                    resolvedPolicy = (EncryptionPolicy)dynamicCallback.getSecurityPolicy();
                    
                } catch (Exception e) {
                    log.log(Level.SEVERE, "WSS1420.dynamic.policy.signature",
                            new Object[] {e.getMessage()});
                    throw new XWSSecurityException(e);
                }
                context.setSecurityPolicy(resolvedPolicy);
            }
            
            DecryptionProcessor.decrypt(context);
        }
    }
    
    private static void encrypt(com.sun.xml.wss.impl.FilterProcessingContext context)
    throws XWSSecurityException{
        if(context instanceof JAXBFilterProcessingContext)
            new com.sun.xml.ws.security.opt.impl.enc.EncryptionProcessor().process((JAXBFilterProcessingContext)context);
        else
            EncryptionProcessor.encrypt(context);
    }
    
    private static SecretKey getReceivedSecret(com.sun.xml.wss.impl.FilterProcessingContext context) {
        SecretKey sKey = null;
        sKey = (javax.crypto.SecretKey)context.getExtraneousProperty(MessageConstants.SECRET_KEY_VALUE);
        return sKey;
    }
    
}
