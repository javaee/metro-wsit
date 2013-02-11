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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.security.addressing.policy.Address;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.impl.policy.UsernameToken;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.KerberosToken;
import com.sun.xml.ws.security.policy.SamlToken;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.X509Token;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.Vector;
import com.sun.xml.ws.security.policy.IssuedToken;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class SymmetricBindingProcessor extends BindingProcessor{
    private SymmetricBinding binding = null;
   
    /** Creates a new instance of SymmetricBindingProcessor */
    public SymmetricBindingProcessor(SymmetricBinding binding,XWSSPolicyContainer container,
            boolean isServer,boolean isIncoming,Vector<SignedParts> signedParts,Vector<EncryptedParts> encryptedParts,
            Vector<SignedElements> signedElements,Vector<EncryptedElements> encryptedElements) {
        this.binding = binding;
        this.container = container;
        this.isServer = isServer;
        this.isIncoming = isIncoming;
        protectionOrder = binding.getProtectionOrder();
        tokenProcessor = new TokenProcessor(isServer,isIncoming,pid);
        iAP = new IntegrityAssertionProcessor(binding.getAlgorithmSuite(),binding.isSignContent());
        eAP = new EncryptionAssertionProcessor(binding.getAlgorithmSuite(),false);
        this.signedParts = signedParts;
        this.signedElements = signedElements;
        this.encryptedElements = encryptedElements;
        this.encryptedParts = encryptedParts;
        
    }
    
    
    public void process()throws PolicyException{
        
        Token pt = binding.getProtectionToken();
        Token st = null;
        Token et = null;
        
        if(pt == null ){
            st = binding.getSignatureToken();
            et = binding.getEncryptionToken();
            
            if(et != null){
                primaryEP = new EncryptionPolicy();
                primaryEP.setUUID(pid.generateID());
                addSymmetricKeyBinding(primaryEP,et);
            }
            
            if(st != null){
                primarySP = new SignaturePolicy();
                primarySP.setUUID(pid.generateID());
                
                SignaturePolicy.FeatureBinding spFB = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding)
                primarySP.getFeatureBinding();
                //spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
                SecurityPolicyUtil.setCanonicalizationMethod(spFB, binding.getAlgorithmSuite());
                spFB.isPrimarySignature(true);
                addSymmetricKeyBinding(primarySP,st);
            }
        }else{
            primarySP = new SignaturePolicy();
            primarySP.setUUID(pid.generateID());
            primaryEP = new EncryptionPolicy();
            primaryEP.setUUID(pid.generateID());
            PolicyAssertion tokenAssertion = (PolicyAssertion)pt;
            SecurityPolicyVersion spVersion = SecurityPolicyUtil.getSPVersion(tokenAssertion);
            addSymmetricKeyBinding(primarySP,pt);
            addSymmetricKeyBinding(primaryEP,pt);
            //share the keybinding
            if (PolicyUtil.isUsernameToken(tokenAssertion,spVersion)&& (!PolicyTypeUtil.derivedTokenKeyBinding(primarySP.getKeyBinding())||(!PolicyTypeUtil.derivedTokenKeyBinding(primaryEP.getKeyBinding())))) {
                ((WSSPolicy)primaryEP).setKeyBinding((WSSPolicy)primarySP.getKeyBinding());
            }
            SignaturePolicy.FeatureBinding spFB = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding)
            primarySP.getFeatureBinding();
            //spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
            SecurityPolicyUtil.setCanonicalizationMethod(spFB, binding.getAlgorithmSuite());
            spFB.isPrimarySignature(true);
        }
        
        if(protectionOrder == Binding.SIGN_ENCRYPT){
            container.insert(primarySP);
            // container.insert(primaryEP);
        }else{
            container.insert(primaryEP);
            container.insert(primarySP);
            if(primaryEP != null){
                EncryptionPolicy.FeatureBinding efp = (EncryptionPolicy.FeatureBinding) primaryEP.getFeatureBinding();
                efp.setUseStandAloneRefList(true);
            }
            
        }
        addPrimaryTargets();
        
        
        if(foundEncryptTargets && binding.getSignatureProtection()){
            protectPrimarySignature();
        }
        if(binding.isIncludeTimeStamp()){
            TimestampPolicy tp = new TimestampPolicy();
            tp.setUUID(pid.generateID());
            container.insert(tp);
            if(!binding.isDisableTimestampSigning()){
                protectTimestamp(tp);
            }
        }
        if (binding.getTokenProtection()) {
            if ((isServer && isIncoming) || (!isServer && !isIncoming)) { //token protection is from client to service only
                WSSPolicy policy = (WSSPolicy) primarySP.getKeyBinding();
                if (PolicyTypeUtil.derivedTokenKeyBinding(policy)) {
                    protectToken(policy, true);
                } else {
                    protectToken((WSSPolicy) policy.getKeyBinding(), true);
                }
            }
        }
    }
    
    protected void addSymmetricKeyBinding(WSSPolicy policy, Token token) throws PolicyException{
        com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding skb =
                new com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding();
        //skb.setKeyAlgorithm(_binding.getAlgorithmSuite().getSymmetricKeyAlgorithm());
        // policy.setKeyBinding(skb);
        PolicyAssertion tokenAssertion = (PolicyAssertion)token;
        SecurityPolicyVersion spVersion = SecurityPolicyUtil.getSPVersion(tokenAssertion);
        if(PolicyUtil.isX509Token(tokenAssertion, spVersion)){
            AuthenticationTokenPolicy.X509CertificateBinding x509CB =new AuthenticationTokenPolicy.X509CertificateBinding();
            //        (AuthenticationTokenPolicy.X509CertificateBinding)policy.newX509CertificateKeyBinding();
            X509Token x509Token = (X509Token)tokenAssertion;
            x509CB.setUUID(token.getTokenId());
            tokenProcessor.setTokenValueType(x509CB, tokenAssertion);
            tokenProcessor.setTokenInclusion(x509CB,(Token) tokenAssertion);
            //x509CB.setPolicyToken((Token) tokenAssertion);
            tokenProcessor.setX509TokenRefType(x509CB, x509Token);
            
             if(x509Token.getIssuer() != null){
                Address addr = x509Token.getIssuer().getAddress();
                if(addr != null)
                    x509CB.setIssuer(addr.getURI().toString());
            } else if(x509Token.getIssuerName() != null){
                x509CB.setIssuer(x509Token.getIssuerName().getIssuerName());
            }
            
            if(x509Token.getClaims() != null){
                x509CB.setClaims(x509Token.getClaims().getClaimsAsBytes());
            }
            
            if(x509Token.isRequireDerivedKeys()){
                DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
                skb.setKeyBinding(x509CB);
                policy.setKeyBinding(dtKB);
                dtKB.setOriginalKeyBinding(skb);
                dtKB.setUUID(pid.generateID());
            }else{
                skb.setKeyBinding(x509CB);
                policy.setKeyBinding(skb);
            }
        } else if(PolicyUtil.isKerberosToken(tokenAssertion, spVersion)){
            AuthenticationTokenPolicy.KerberosTokenBinding kerberosBinding =
                    new AuthenticationTokenPolicy.KerberosTokenBinding();
            kerberosBinding.setUUID(token.getTokenId());
            KerberosToken kerberosToken = (KerberosToken)tokenAssertion;
            tokenProcessor.setTokenValueType(kerberosBinding, tokenAssertion);
            tokenProcessor.setTokenInclusion(kerberosBinding,(Token) tokenAssertion);
            tokenProcessor.setKerberosTokenRefType(kerberosBinding, kerberosToken);
            
            if(kerberosToken.getIssuer() != null){
                Address addr = kerberosToken.getIssuer().getAddress();
                if(addr != null)
                    kerberosBinding.setIssuer(addr.getURI().toString());
            } else if(kerberosToken.getIssuerName() != null){
                kerberosBinding.setIssuer(kerberosToken.getIssuerName().getIssuerName());
            }
            
            if(kerberosToken.getClaims() != null){
                kerberosBinding.setClaims(kerberosToken.getClaims().getClaimsAsBytes());
            }
            
            if(kerberosToken.isRequireDerivedKeys()){
                DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
                skb.setKeyBinding(kerberosBinding);
                policy.setKeyBinding(dtKB);
                dtKB.setOriginalKeyBinding(skb);
                dtKB.setUUID(pid.generateID());
            }else{
                skb.setKeyBinding(kerberosBinding);
                policy.setKeyBinding(skb);
            }
        }else if(PolicyUtil.isSamlToken(tokenAssertion, spVersion)){
            AuthenticationTokenPolicy.SAMLAssertionBinding sab = new AuthenticationTokenPolicy.SAMLAssertionBinding();
            SamlToken samlToken = (SamlToken)tokenAssertion;
            sab.setUUID(token.getTokenId());
            sab.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
            tokenProcessor.setTokenInclusion(sab,(Token) tokenAssertion);
            //sab.setPolicyToken((Token) tokenAssertion);
            
             if(samlToken.getIssuer() != null){
                Address addr = samlToken.getIssuer().getAddress();
                if(addr != null)
                    sab.setIssuer(addr.getURI().toString());
            } else if(samlToken.getIssuerName() != null){
                sab.setIssuer(samlToken.getIssuerName().getIssuerName());
            }
            
            if(samlToken.getClaims() != null){
                sab.setClaims(samlToken.getClaims().getClaimsAsBytes());
            }
            
            if(samlToken.isRequireDerivedKeys()){
                DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
                dtKB.setOriginalKeyBinding(sab);
                policy.setKeyBinding(dtKB);
                dtKB.setUUID(pid.generateID());
            }else{
                policy.setKeyBinding(sab);
            }
        }else if(PolicyUtil.isIssuedToken(tokenAssertion, spVersion)){
            IssuedTokenKeyBinding itkb = new IssuedTokenKeyBinding();
            tokenProcessor.setTokenInclusion(itkb,(Token) tokenAssertion);
            //itkb.setPolicyToken((Token) tokenAssertion);
            itkb.setUUID(((Token)tokenAssertion).getTokenId());
            IssuedToken it = (IssuedToken)tokenAssertion;
            
            if(it.getIssuer() != null){
                Address addr = it.getIssuer().getAddress();
                if(addr != null)
                    itkb.setIssuer(addr.getURI().toString());
            } else if(it.getIssuerName() != null){
                itkb.setIssuer(it.getIssuerName().getIssuerName());
            }
            
            if(it.getClaims() != null){
                itkb.setClaims(it.getClaims().getClaimsAsBytes());
            }
            
            if(it.isRequireDerivedKeys()){
                DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
                dtKB.setOriginalKeyBinding(itkb);
                policy.setKeyBinding(dtKB);
                dtKB.setUUID(pid.generateID());
            }else{
                policy.setKeyBinding(itkb);
            }
        }else if(PolicyUtil.isSecureConversationToken(tokenAssertion, spVersion)){
            SecureConversationTokenKeyBinding sct = new SecureConversationTokenKeyBinding();
            SecureConversationToken sctPolicy = (SecureConversationToken)tokenAssertion;
            
            if(sctPolicy.getIssuer() != null){
                Address addr = sctPolicy.getIssuer().getAddress();
                if(addr != null)
                    sct.setIssuer(addr.getURI().toString());
            } else if(sctPolicy.getIssuerName() != null){
                sct.setIssuer(sctPolicy.getIssuerName().getIssuerName());
            }
            
            if(sctPolicy.getClaims() != null){
                sct.setClaims(sctPolicy.getClaims().getClaimsAsBytes());
            }
            
            if(sctPolicy.isRequireDerivedKeys()){
                DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
                dtKB.setOriginalKeyBinding(sct);
                policy.setKeyBinding(dtKB);
                dtKB.setUUID(pid.generateID());
            }else{
                policy.setKeyBinding(sct);
            }
            tokenProcessor.setTokenInclusion(sct,(Token) tokenAssertion);
            //sct.setPolicyToken((Token) tokenAssertion);
            sct.setUUID(((Token)tokenAssertion).getTokenId());
        }else if(PolicyUtil.isUsernameToken(tokenAssertion, spVersion)){
            AuthenticationTokenPolicy.UsernameTokenBinding utb=new AuthenticationTokenPolicy.UsernameTokenBinding();
            UsernameToken unt = (UsernameToken)tokenAssertion;
            utb.setUUID(token.getTokenId());
            utb.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
            tokenProcessor.setTokenValueType(utb, tokenAssertion);
            tokenProcessor.setTokenInclusion(utb,(Token) tokenAssertion);
            tokenProcessor.setUsernameTokenRefType(utb, unt);
            if(unt.getIssuer() != null){
                Address addr = unt.getIssuer().getAddress();
                if(addr != null)
                    utb.setIssuer(addr.getURI().toString());
            } else if(unt.getIssuerName() != null){
                utb.setIssuer(unt.getIssuerName().getIssuerName());
            }
            
            if(unt.getClaims() != null){
                utb.setClaims(unt.getClaims().getClaimsAsBytes());
            }
            utb.setUseCreated(unt.useCreated());
            utb.setUseNonce(unt.useNonce());
            //utb.setNoPassword(true);
            if(unt.isRequireDerivedKeys()){
            DerivedTokenKeyBinding dtKB =  new DerivedTokenKeyBinding();
            skb.setKeyBinding(utb);
            policy.setKeyBinding(dtKB);
            dtKB.setOriginalKeyBinding(skb);
            dtKB.setUUID(pid.generateID());
            } else{
                skb.setKeyBinding(utb);
                policy.setKeyBinding(skb);
            }            
        }
        else{
            throw new UnsupportedOperationException("addKeyBinding for "+ token + "is not supported");
        }
    }
    
    protected Binding getBinding(){
        return binding;
    }
    
    protected EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException {
        if(sEncPolicy == null){
            sEncPolicy  = new EncryptionPolicy();
            sEncPolicy.setUUID(pid.generateID());
            Token token = null;
            token = binding.getProtectionToken();
            if( token== null){
                token = binding.getEncryptionToken();
            }
            addSymmetricKeyBinding(sEncPolicy,token);
            container.insert(sEncPolicy);
        }
        return sEncPolicy;
    }
    
    protected void close(){
        if(protectionOrder == Binding.SIGN_ENCRYPT){
            container.insert(primaryEP);
        }
    }
    
    
}
