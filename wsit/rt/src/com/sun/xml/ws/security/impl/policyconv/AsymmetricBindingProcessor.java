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

import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.Vector;
import javax.xml.crypto.dsig.CanonicalizationMethod;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class AsymmetricBindingProcessor extends BindingProcessor {
    private AsymmetricBinding binding = null;
    
    /** Creates a new instance of AsymmetricBindingProcessor */
    public AsymmetricBindingProcessor(AsymmetricBinding asBinding,XWSSPolicyContainer container,
            boolean isServer,boolean isIncoming,Vector<SignedParts> signedParts,Vector<EncryptedParts> encryptedParts,
            Vector<SignedElements> signedElements,Vector<EncryptedElements> encryptedElements) {
        this.binding = asBinding;
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
        Token st = getSignatureToken();
        Token et = getEncryptionToken();
        if(st != null){
            primarySP = new SignaturePolicy();
            primarySP.setUUID(pid.generateID());
            tokenProcessor.addKeyBinding(primarySP,st,true);
            SignaturePolicy.FeatureBinding spFB = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding)
            primarySP.getFeatureBinding();
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
        }
        if(et != null){
            primaryEP = new EncryptionPolicy();
            primaryEP.setUUID(pid.generateID());
            tokenProcessor.addKeyBinding(primaryEP,et,false);
        }
        if(protectionOrder == Binding.SIGN_ENCRYPT){
            container.insert(primarySP);
        }else{
            container.insert(primaryEP);
            container.insert(primarySP);
            
        }
        addPrimaryTargets();
        if(binding.getSignatureProtection()){
            protectPrimarySignature();
        }
        if(binding.isIncludeTimeStamp()){
            protectTimestamp();
        }
        if(binding.getTokenProtection()){
            protectToken((WSSPolicy) primarySP.getKeyBinding());
        }
     
    }
    
    protected Token getEncryptionToken(){
        if((isServer && !isIncoming) || (!isServer && isIncoming)){
            return binding.getInitiatorToken();
        }else{
            return binding.getRecipientToken();
        }
    }
    
    protected Token getSignatureToken(){
        if((isServer && !isIncoming) || (!isServer && isIncoming)){
            return binding.getRecipientToken();
        }else{
            return binding.getInitiatorToken();
        }
    }
    
    protected Binding getBinding(){
        return binding;
    }
    
    protected EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException{
        if(sEncPolicy == null){
            sEncPolicy  = new EncryptionPolicy();
            sEncPolicy.setUUID(pid.generateID());
            Token token = null;
            token = getEncryptionToken();
            tokenProcessor.addKeyBinding(sEncPolicy,token,false);
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
