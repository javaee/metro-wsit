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
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.X509Token;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.Set;
import java.util.Vector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import static com.sun.xml.wss.impl.policy.mls.Target.SIGNATURE_CONFIRMATION;
/**
 *
 * @author K.Venugopal@sun.com
 */
public abstract class BindingProcessor {
    
    protected String protectionOrder = Binding.SIGN_ENCRYPT;
    protected boolean isServer = false;
    protected boolean isIncoming = false;
    protected SignaturePolicy primarySP  = null;
    protected EncryptionPolicy primaryEP = null;
    //current secondary encryption policy
    
    protected EncryptionPolicy sEncPolicy = null;
    protected SignaturePolicy sSigPolicy = null;
    protected XWSSPolicyContainer container = null;
    
    protected Vector<SignedParts> signedParts = null;
    protected Vector<EncryptedParts> encryptedParts = null;
    
    protected Vector<SignedElements> signedElements = null;
    protected Vector<EncryptedElements> encryptedElements =null;
    protected PolicyID pid = null;
    
    protected TokenProcessor tokenProcessor= null;
    protected IntegrityAssertionProcessor iAP = null;
    protected EncryptionAssertionProcessor eAP = null;
    private boolean WSS11  = false;
    /** Creates a new instance of BindingProcessor */
    public BindingProcessor() {
        this.pid = new PolicyID();
    }
    
    /*
     WSIT Configuration should not allow protect primary signature
     property to be set if we determine there will be no signature.
     */
    
    protected void protectPrimarySignature()throws PolicyException{
        if(protectionOrder == Binding.ENCRYPT_SIGN){
            EncryptionPolicy ep = getSecondaryEncryptionPolicy();
            EncryptionPolicy.FeatureBinding epFB  = (EncryptionPolicy.FeatureBinding) ep.getFeatureBinding();
            EncryptionTarget et = eAP.getTargetCreator().newURIEncryptionTarget(primarySP.getUUID());
            epFB.addTargetBinding(et);
        }else{
            EncryptionPolicy.FeatureBinding epFB  = (EncryptionPolicy.FeatureBinding) primaryEP.getFeatureBinding();
            EncryptionTarget et = eAP.getTargetCreator().newURIEncryptionTarget(primarySP.getUUID());
            epFB.addTargetBinding(et);
        }
    }
    
    protected void protectTimestamp(){
        TimestampPolicy tp = new TimestampPolicy();
        tp.setUUID(pid.generateID());
        SignatureTarget target = iAP.getTargetCreator().newURISignatureTarget(tp.getUUID());
        container.insert(tp);
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)primarySP.getFeatureBinding();
        spFB.addTargetBinding(target);
    }
    
    //TODO:WS-SX Spec:If we have a secondary signature should it protect the token too ?
    protected void protectToken(WSSPolicy token){
        String uid = token.getUUID();
        if(PolicyTypeUtil.x509CertificateBinding(token)){
            uid = ((AuthenticationTokenPolicy.X509CertificateBinding)token).getSTRID();
            if(uid == null){
                uid = pid.generateID();
                ((AuthenticationTokenPolicy.X509CertificateBinding)token).setSTRID(uid);
            }
        }else if(PolicyTypeUtil.samlTokenPolicy(token)){
            uid = ((AuthenticationTokenPolicy.SAMLAssertionBinding)token).getSTRID();
            if(uid == null){
                uid = pid.generateID();
                ((AuthenticationTokenPolicy.SAMLAssertionBinding)token).setSTRID(uid);
            }
        }
        //TODO:: Handle DTK and IssuedToken.
        if ( uid != null ) {
            SignatureTargetCreator stc = iAP.getTargetCreator();
            SignatureTarget st = stc.newURISignatureTarget(uid);
            stc.addSTRTransform(st);
            SignaturePolicy.FeatureBinding fb = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding) primarySP.getFeatureBinding();
            fb.addTargetBinding(st);
        }
    }
    
    protected abstract EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException;
    
    
    protected void addPrimaryTargets()throws PolicyException{
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)primarySP.getFeatureBinding();
        EncryptionPolicy.FeatureBinding epFB = (EncryptionPolicy.FeatureBinding)primaryEP.getFeatureBinding();
        spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        
        //TODO:: Merge SignedElements.
        
        for(SignedElements se : signedElements){
            iAP.process(se,spFB);
        }
        /*
            If Empty SignParts is present then remove rest of the SignParts
            as we will be signing all HEADERS and Body. Question to WS-SX:
            Are SignedParts headers targeted to ultimate reciever role.
         */
        for(SignedParts sp : signedParts){
            if(SecurityPolicyUtil.isSignedPartsEmpty(sp)){
                signedParts.removeAllElements();
                signedParts.add(sp);
                break;
            }
        }
        for(SignedParts sp : signedParts){
            iAP.process(sp,spFB);
        }
        for(EncryptedParts ep :encryptedParts){
            eAP.process(ep,epFB);
        }
        
        for(EncryptedElements encEl : encryptedElements){
            eAP.process(encEl,epFB);
        }
        if(isWSS11()){
            iAP.process(SIGNATURE_CONFIRMATION,spFB);
        }
        if(isServer && !isIncoming && getBinding().getSignatureProtection()){
            eAP.process(SIGNATURE_CONFIRMATION,epFB);
        }
    }
    
    
    protected abstract Binding getBinding();
    
    
    public void processSupportingTokens(SupportingTokens st) throws PolicyException{
        
        SupportingTokensProcessor stp =  new SupportingTokensProcessor((SupportingTokens)st,
                tokenProcessor,getBinding(),container,primarySP,getEncryptionPolicy(),pid);
        stp.process();
    }
    
    public void processSupportingTokens(SignedSupportingTokens st) throws PolicyException{
        
        SignedSupportingTokensProcessor stp = new SignedSupportingTokensProcessor(st,
                tokenProcessor,getBinding(),container,primarySP,getEncryptionPolicy(),pid);
        stp.process();
    }
    public void processSupportingTokens(EndorsingSupportingTokens est) throws PolicyException{
        
        EndorsingSupportingTokensProcessor stp = new EndorsingSupportingTokensProcessor(est,
                tokenProcessor,getBinding(),container,primarySP,getEncryptionPolicy(),pid);
        stp.process();
    }
    
    public void processSupportingTokens(SignedEndorsingSupportingTokens est) throws PolicyException{
        SignedEndorsingSupportingTokensProcessor stp = new SignedEndorsingSupportingTokensProcessor(est,
                tokenProcessor,getBinding(),container,primarySP,getEncryptionPolicy(),pid);
        stp.process();
        
    }
    
    protected SignaturePolicy getSignaturePolicy(){
        if(getBinding().getProtectionOrder() == Binding.SIGN_ENCRYPT){
            return primarySP;
        }else{
            return sSigPolicy;
        }
    }
    
    private EncryptionPolicy getEncryptionPolicy() throws PolicyException{
        if(getBinding().getProtectionOrder() == Binding.SIGN_ENCRYPT){
            return primaryEP;
        }else{
            return getSecondaryEncryptionPolicy();
        }
    }
    
    protected abstract void close();
    
    public boolean isWSS11() {
        return WSS11;
    }
    
    public void setWSS11(boolean WSS11) {
        this.WSS11 = WSS11;
    }
}
