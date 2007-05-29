/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.security.impl.policyconv;

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.WSSAssertion;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.KeyBindingBase;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
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
    private WSSAssertion wss11  = null;
    
    protected boolean foundEncryptTargets = false;
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
            SecurityPolicyUtil.setName(et, primarySP);
            epFB.addTargetBinding(et);
        }else{
            EncryptionPolicy.FeatureBinding epFB  = (EncryptionPolicy.FeatureBinding) primaryEP.getFeatureBinding();
            EncryptionTarget et = eAP.getTargetCreator().newURIEncryptionTarget(primarySP.getUUID());
            SecurityPolicyUtil.setName(et, primarySP);
            epFB.addTargetBinding(et);
        }
    }
    
    protected void protectTimestamp(){
        TimestampPolicy tp = new TimestampPolicy();
        tp.setUUID(pid.generateID());
        SignatureTarget target = iAP.getTargetCreator().newURISignatureTarget(tp.getUUID());
        SecurityPolicyUtil.setName(target, tp);
        container.insert(tp);
        SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)primarySP.getFeatureBinding();
        spFB.addTargetBinding(target);
    }
    
    //TODO:WS-SX Spec:If we have a secondary signature should it protect the token too ?
    protected void protectToken(WSSPolicy token){
        protectToken(token,false);
    }
    
    protected void protectToken(WSSPolicy token,boolean ignoreSTR){
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
        if(!ignoreSTR){
            if ( uid != null ) {
                SignatureTargetCreator stc = iAP.getTargetCreator();
                SignatureTarget st = stc.newURISignatureTarget(uid);
                stc.addSTRTransform(st);
                SignaturePolicy.FeatureBinding fb = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding) primarySP.getFeatureBinding();
                fb.addTargetBinding(st);
            }
        }else{
            SignatureTargetCreator stc = iAP.getTargetCreator();
            SignatureTarget st = null;
            if (PolicyTypeUtil.derivedTokenKeyBinding(token)) {
                WSSPolicy kbd = ((DerivedTokenKeyBinding)token).getOriginalKeyBinding();
                if (PolicyTypeUtil.symmetricKeyBinding(kbd)) {
                    WSSPolicy sbd = (KeyBindingBase)kbd.getKeyBinding();
                    st = stc.newURISignatureTarget(sbd.getUUID());
                } else {
                    st = stc.newURISignatureTarget(kbd.getUUID());
                }
            } else {
                st = stc.newURISignatureTarget(token.getUUID());
            }
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
            foundEncryptTargets = true;
            eAP.process(ep,epFB);
        }
        
        for(EncryptedElements encEl : encryptedElements){
            foundEncryptTargets = true;
            eAP.process(encEl,epFB);
        }
        if(isWSS11() && requireSC()){
            iAP.process(SIGNATURE_CONFIRMATION,spFB);
        }
        if(foundEncryptTargets && (isWSS11() && requireSC() ) && isServer && !isIncoming && getBinding().getSignatureProtection()){
            eAP.process(SIGNATURE_CONFIRMATION,epFB);
        }
    }
    
    protected boolean requireSC(){
        if(wss11 != null){
            if(wss11.getRequiredProperties().contains(WSSAssertion.REQUIRE_SIGNATURE_CONFIRMATION)){
                return true;
            }
        }
        return false;
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
        if(wss11 != null){
            return true;
        }
        return false;
    }
    
    public void setWSS11(WSSAssertion wss11) {
        this.wss11 = wss11;
    }
}
