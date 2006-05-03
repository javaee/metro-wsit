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

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.TransportBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.util.Iterator;
import javax.xml.crypto.dsig.CanonicalizationMethod;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class TransportBindingProcessor extends BindingProcessor {
    private TransportBinding binding = null;
    private TimestampPolicy tp = null;
    /** Creates a new instance of TransportBindingProcessor */
    public TransportBindingProcessor(TransportBinding binding,boolean isServer,boolean isIncoming,XWSSPolicyContainer container){
        this.binding = binding;
        this.container = container;
        this.isIncoming = isIncoming;
        this.isServer = isServer;
        iAP = new IntegrityAssertionProcessor(binding.getAlgorithmSuite(),false);
        eAP = new EncryptionAssertionProcessor(binding.getAlgorithmSuite(),false);
        this.tokenProcessor  = new TokenProcessor(isServer,isIncoming,pid);
    }
    
    public void process() throws PolicyException{
        container.setPolicyContainerMode(binding.getLayout());
        if(binding.isIncludeTimeStamp()){
            tp = new TimestampPolicy();
            tp.setUUID(pid.generateID());
            container.insert(tp);
        }
    }
    
    public void processSupportingTokens(SupportingTokens tokens) throws PolicyException{
        Iterator itr = tokens.getTokens();
        while(itr.hasNext()){
            Token token = (Token) itr.next();
            WSSPolicy policy = tokenProcessor.getWSSToken(token);
            AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
            atp.setFeatureBinding(policy);
            container.insert(atp);
        }
    }
    
    public void processSupportingTokens(SignedSupportingTokens sst) throws PolicyException{
        Iterator itr = sst.getTokens();
        while(itr.hasNext()){
            Token token = (Token) itr.next();
            WSSPolicy policy = tokenProcessor.getWSSToken(token);
            AuthenticationTokenPolicy atp = new AuthenticationTokenPolicy();
            atp.setFeatureBinding(policy);
            container.insert(atp);
        }
    }
    
    public void processSupportingTokens(EndorsingSupportingTokens est) throws PolicyException{
        Iterator itr = est.getTokens();
        while(itr.hasNext()){
            Token token = (Token) itr.next();
            SignaturePolicy sp = new SignaturePolicy();
            SignaturePolicy.FeatureBinding spFB = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
            sp.setUUID(pid.generateID());
            tokenProcessor.addKeyBinding(sp, token);
           // container.insert(sp.getKeyBinding());
            WSSPolicy key = (WSSPolicy) sp.getKeyBinding();
            if(tp != null ){
                SignatureTarget target = iAP.getTargetCreator().newURISignatureTarget(tp.getUUID());
               
                spFB.isEndorsingSignature(true);
                spFB.addTargetBinding(target);
                
                container.insert(sp);
            }
           
        }
    }
    
    public void processSupportingTokens(SignedEndorsingSupportingTokens set) throws PolicyException{
        Iterator itr = set.getTokens();
        while(itr.hasNext()){
            Token token = (Token) itr.next();
            SignaturePolicy sp = new SignaturePolicy();
            sp.setUUID(pid.generateID());
            SignaturePolicy.FeatureBinding spFB = (com.sun.xml.wss.impl.policy.mls.SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
            spFB.setCanonicalizationAlgorithm(CanonicalizationMethod.EXCLUSIVE);
            tokenProcessor.addKeyBinding(sp, token);
            WSSPolicy key = (WSSPolicy) sp.getKeyBinding();
            //protect primary signature
            
            if(tp != null){
                SignatureTarget target = iAP.getTargetCreator().newURISignatureTarget(tp.getUUID());
                //SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
                //spFB.isEndorsingSignature(true);
                spFB.addTargetBinding(target);
                container.insert(sp);
            }
            
            if(binding.getSignatureProtection()){
                SignatureTarget target = iAP.getTargetCreator().newURISignatureTarget(key.getUUID());
              //  SignaturePolicy.FeatureBinding spFB = (SignaturePolicy.FeatureBinding)sp.getFeatureBinding();
                
                spFB.isEndorsingSignature(true);
                spFB.addTargetBinding(target);
                container.insert(sp);
            }
           // container.insert(key);
        }
    }
    
    protected EncryptionPolicy getSecondaryEncryptionPolicy() throws PolicyException {
        throw new UnsupportedOperationException();
    }
    
    protected Binding getBinding() {
        return binding;
    }
    
    protected void close(){
      
    }
}
