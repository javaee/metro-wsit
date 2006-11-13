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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.Target;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.crypto.dsig.CanonicalizationMethod;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class SupportingTokensProcessor {
    protected TokenProcessor tokenProcessor = null;
    protected SignatureTargetCreator stc = null;
    protected EncryptionTargetCreator etc = null;
    protected Binding binding = null;
    protected XWSSPolicyContainer policyContainer = null;
    
    protected SignaturePolicy signaturePolicy = null;
    protected EncryptionPolicy encryptionPolicy = null;
    protected SupportingTokens st = null;
    protected IntegrityAssertionProcessor iAP = null;
    protected EncryptionAssertionProcessor eAP = null;
    
    protected ArrayList<SignaturePolicy> spList =null;
    protected ArrayList<EncryptionPolicy> epList =null;
    protected SignedParts emptySP  = null;
    protected boolean buildSP = false;
    protected boolean buildEP = false;
    protected PolicyID pid = null;
    
    protected SupportingTokensProcessor(){
        
    }
    /** Creates a new instance of SupportingTokensProcessor */
    public SupportingTokensProcessor(SupportingTokens st,TokenProcessor tokenProcessor,Binding binding,XWSSPolicyContainer container,SignaturePolicy sp,
            EncryptionPolicy ep,PolicyID pid) {
        this.st = st;
        this.tokenProcessor = tokenProcessor;
        this.binding = binding;
        this.pid =pid;
        this.policyContainer = container;
        this.encryptionPolicy  = ep;
        this.signaturePolicy = sp;
        AlgorithmSuite as = null;
        as = st.getAlgorithmSuite();
        if( as == null){
            as = binding.getAlgorithmSuite();
        }
        this.iAP = new IntegrityAssertionProcessor(as,binding.isSignContent());
        this.eAP = new EncryptionAssertionProcessor(as,false);
        this.stc = iAP.getTargetCreator();
        this.etc = eAP.getTargetCreator();
        this.emptySP = getEmptySignedParts(st.getSignedParts());
    }
    
    public void process() throws PolicyException{
        Iterator tokens = st.getTokens();
        
        if(st.getEncryptedParts().hasNext() || st.getEncryptedElements().hasNext()){
            buildEP = true;
        }
        if(st.getSignedElements().hasNext() || st.getSignedParts().hasNext()){
            buildSP = true;
        }
        
        while(tokens.hasNext()){
            Token token = (Token) tokens.next();
            WSSPolicy policy = tokenProcessor.getWSSToken(token);
            if ( policy.getUUID() != null ) {
                
                addToPrimarySignature(policy,token);
                if(PolicyUtil.isUsernameToken((PolicyAssertion) token)){
                    encryptToken(token);
                }
                
                collectSignaturePolicies(token);
                if(buildEP){
                    EncryptionPolicy ep = new EncryptionPolicy();
                    ep.setKeyBinding(policy);
                    getEPList().add(ep);
                }
            }
            
            //TODO:: Add token to MessagePolicy;
            AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
            atp.setFeatureBinding(policy);
            policyContainer.insert(atp);
            //TODO: Take care of targets.
            addTargets();
        }
    }
    
    protected void collectSignaturePolicies(Token token) throws PolicyException{
        if(buildSP){
            createSupportingSignature(token);
        }
    }
    
    protected void createSupportingSignature(Token token) throws PolicyException{
        SignaturePolicy sp = new SignaturePolicy();
        sp.setUUID(pid.generateID());
        tokenProcessor.addKeyBinding(sp, token,true);
        if(binding.getTokenProtection()){
            protectToken((WSSPolicy) sp.getKeyBinding(), sp);
        }
        SignaturePolicy.FeatureBinding spFB = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
        spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        //   sp.setKeyBinding(policy);
        getSPList().add(sp);
        endorseSignature(sp);
    }
    protected void addToPrimarySignature(WSSPolicy policy,Token token) throws PolicyException{
        //no-op
    }
    
    protected void endorseSignature(SignaturePolicy sp){
        //no-op
    }
    
    protected ArrayList<SignaturePolicy> getSPList(){
        if(spList == null){
            spList = new ArrayList<SignaturePolicy>();
        }
        return spList;
    }
    
    protected ArrayList<EncryptionPolicy> getEPList(){
        if(epList == null){
            epList = new ArrayList<EncryptionPolicy>();
        }
        return epList;
    }
    
    protected void encryptToken(Token token)throws PolicyException{
        if ( token.getTokenId()!= null ) {
            EncryptionPolicy.FeatureBinding fb =(EncryptionPolicy.FeatureBinding) encryptionPolicy.getFeatureBinding();
            EncryptionTarget et = etc.newURIEncryptionTarget(token.getTokenId());
            fb.addTargetBinding(et);
        }
    }
    
        
    protected SignedParts getEmptySignedParts(Iterator itr){
        while(itr.hasNext()){
            Target target = (Target)itr.next();
            if(PolicyUtil.isSignedParts((PolicyAssertion)target)){
                if(SecurityPolicyUtil.isSignedPartsEmpty((SignedParts) target)){
                    return (SignedParts) target;
                }
            }
        }
        return null;
    }
    
    protected void addTargets(){
        if(binding.getProtectionOrder() == Binding.SIGN_ENCRYPT){
            if(spList != null){
                populateSignaturePolicy();
            }
            if(epList != null){
                populateEncryptionPolicy();
            }
        }else{
            if(epList != null){
                populateEncryptionPolicy();
            }
            if(spList != null){
                populateSignaturePolicy();
            }
        }
    }
    
    protected void populateSignaturePolicy(){
        for(SignaturePolicy sp : spList){
            SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
            if(emptySP != null){
                iAP.process(emptySP,spFB);
            }else{
                Iterator<SignedParts>itr = st.getSignedParts();
                while(itr.hasNext()){
                    SignedParts target = itr.next();
                    iAP.process(target,spFB);
                }
            }
            Iterator<SignedElements> itr = st.getSignedElements();
            while(itr.hasNext()){
                SignedElements target = itr.next();
                iAP.process(target,spFB);
            }
            policyContainer.insert(sp);
        }
    }
    
    protected void populateEncryptionPolicy(){
        for(EncryptionPolicy ep :epList){
            EncryptionPolicy.FeatureBinding epFB = (EncryptionPolicy.FeatureBinding)ep.getFeatureBinding();
            Iterator<EncryptedElements> itr = st.getEncryptedElements();
            while(itr.hasNext()){
                EncryptedElements target = itr.next();
                eAP.process(target,epFB);
            }
            Iterator<EncryptedParts> epr = st.getEncryptedParts();
            while(epr.hasNext()){
                EncryptedParts target = epr.next();
                eAP.process(target,epFB);
            }
            policyContainer.insert(ep);
        }
    }
    
    protected void protectToken(WSSPolicy token,SignaturePolicy sp){
        String uid = token.getUUID();
        if ( uid != null ) {
            SignatureTargetCreator stc = iAP.getTargetCreator();
            SignatureTarget st = stc.newURISignatureTarget(uid);
            SecurityPolicyUtil.setName(st, token);
            stc.addSTRTransform(st);
            SignaturePolicy.FeatureBinding fb = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding) sp.getFeatureBinding();
            fb.addTargetBinding(st);
        }
    }
}
