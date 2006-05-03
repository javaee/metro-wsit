/*
 * SignaturePolicyVerifier.java
 *
 * Created on August 7, 2005, 9:06 PM
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


package com.sun.xml.wss.impl.policy.verifier;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.policy.spi.PolicyVerifier;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.SAMLAssertionBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.X509CertificateBinding;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.core.X509SecurityToken;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.WSSAssertion;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;

import java.util.HashMap;


/**
 *
 * @author K.Venugopal@sun.com
 */
public class SignaturePolicyVerifier implements PolicyVerifier{
    
    FilterProcessingContext context;
    
    /** Creates a new instance of SignaturePolicyVerifier */
    public SignaturePolicyVerifier (FilterProcessingContext context) {
        this.context = context;
    }
    
    /**
     *
     * @param configPolicy Policy configured for the incoming message.
     * @param recvdPolicy policy inferred from the incoming message.
     * @throws com.sun.xml.wss.PolicyViolationException when policy infered from incoming message does not match with what
     * is configured.
     *
     */
    
    /*
     * Note : Right now we dont check Signature Target requirements here. We should come up
     * with a better and yet performant design as policy requirements get clear.
     *
     */
    public void verifyPolicy (SecurityPolicy configPolicy, SecurityPolicy recvdPolicy) throws PolicyViolationException {
        
        if(PolicyTypeUtil.signaturePolicy (configPolicy) && PolicyTypeUtil.signaturePolicy (recvdPolicy)){
            SignaturePolicy rsignPolicy = (SignaturePolicy)recvdPolicy;
            SignaturePolicy csignPolicy = (SignaturePolicy)configPolicy;
            
            SignaturePolicy.FeatureBinding rfBinding = (SignaturePolicy.FeatureBinding)rsignPolicy.getFeatureBinding ();
            SignaturePolicy.FeatureBinding cfBinding = (SignaturePolicy.FeatureBinding)csignPolicy.getFeatureBinding ();
            
            String cCanonAlgo = cfBinding.getCanonicalizationAlgorithm ();
            String rCanonAlgo = rfBinding.getCanonicalizationAlgorithm ();
            if((cCanonAlgo != null && cCanonAlgo.length () >0) && (rCanonAlgo != null || rCanonAlgo.length () >0 )){
                if(!rCanonAlgo.equals (cCanonAlgo)){
                    throw new PolicyViolationException ("Receiver side requirement verification failed,"+
                            " canonicalization algorithm received in the message is "+ rfBinding.getCanonicalizationAlgorithm ()
                            + " policy requires "+cfBinding.getCanonicalizationAlgorithm ());
                }
            }
            /*
            WSSPolicy ckeyBinding =  (WSSPolicy) csignPolicy.getKeyBinding ();
            if(ckeyBinding != null){
                String cKeyType = ckeyBinding.getType ();
                WSSPolicy rkeyBinding =  (WSSPolicy) rsignPolicy.getKeyBinding ();
                if(rkeyBinding == null){
                    throw new PolicyViolationException ("KeyType used to sign the message doesnot match with " +
                            " the receiver side requirements. Configured KeyType is "+ckeyBinding+
                            " KeyType inferred from the message is  "+ rkeyBinding);
                }
                String rKeyType = rkeyBinding.getType ();
                //TODO: Verification of KeyBinding later
                if(rKeyType != cKeyType){
                    throw new PolicyViolationException ("KeyType used to sign the message doesnot match with " +
                            " the receiver side requirements. Configured KeyType is "+ckeyBinding+
                            " KeyType inferred from the message is  "+ rkeyBinding);
                    //log propert error message.
                }
                if(cKeyType == PolicyTypeUtil.SAMLASSERTION_TYPE){
                    // checkSAMLAssertionBinding ((SAMLAssertionBinding)ckeyBinding,(SAMLAssertionBinding)rkeyBinding);
                }else if(cKeyType == PolicyTypeUtil.X509CERTIFICATE_TYPE ){
                    checkX509CertificateBinding ((X509CertificateBinding)ckeyBinding,(X509CertificateBinding)rkeyBinding);
                }
                
            }*/
        }
    }
    
    private void checkSAMLAssertionBinding (SAMLAssertionBinding configPolicy , SAMLAssertionBinding recvdPolicy)throws PolicyViolationException {
        
        boolean matched = true;
        
        String _cAI = configPolicy.getAuthorityIdentifier ();
        String _rAI = recvdPolicy.getAuthorityIdentifier ();
        if((_cAI != null && _cAI.length () > 0 ) && _rAI != null){
            matched = _cAI.equals (_rAI);
            _throwError (configPolicy,recvdPolicy,matched);
        }        
       
    }
    
    
    private void checkX509CertificateBinding (X509CertificateBinding configPolicy , X509CertificateBinding recvdPolicy)throws PolicyViolationException {
        
        boolean matched = true;
        
        configPolicy = setReferenceType(configPolicy);
        String ckeyAlg = configPolicy.getKeyAlgorithm ();
        String rkeyAlg = recvdPolicy.getKeyAlgorithm ();
        if(ckeyAlg != null && ckeyAlg.length () > 0 && rkeyAlg.length () > 0){
            matched = ckeyAlg.equals (rkeyAlg);
        }
        _throwError (configPolicy,recvdPolicy,matched);
        
        /*String cRT = configPolicy.getReferenceType ();
        String rRT = recvdPolicy.getReferenceType ();
        
        if(cRT != null && cRT.length () > 0 ){
            matched = cRT.equals (rRT);
        }
        _throwError (configPolicy,recvdPolicy,matched);*/
        
        String cVT = configPolicy.getValueType ();
        String rVT = recvdPolicy.getValueType ();
        
        if(cVT != null && cVT.length () > 0 && rVT.length () >0){
            matched = cVT.equals (rVT);
        }
        _throwError (configPolicy,recvdPolicy,matched);
        /*
        String cCI = configPolicy.getCertificateIdentifier ();
        String rCI = recvdPolicy.getCertificateIdentifier ();
         
        if(cCI != null && cCI.length () > 0 ){
            matched = cCI.equals (rCI);
        }
        _throwError (configPolicy,recvdPolicy,matched);
         */
        /*if(!matched){
            throw new PolicyViolationException ("KeyType used to sign the message doesnot match with " +
                    " the receiver side requirements. Configured KeyType is "+configPolicy+
                    " KeyType inferred from the message is  "+ recvdPolicy);
        }*/
    }
    
    private final void _throwError (SecurityPolicy configPolicy, SecurityPolicy recvdPolicy, boolean matched)throws PolicyViolationException {
        if(!matched){
            throw new PolicyViolationException ("KeyType used to sign the message doesnot match with " +
                    " the receiver side requirements. Configured KeyType is "+configPolicy+
                    " KeyType inferred from the message is  "+ recvdPolicy);
        }
    }
    
    private X509CertificateBinding setReferenceType(X509CertificateBinding configPolicy){
        
            Token policyToken = configPolicy.getPolicyToken();
            if (policyToken != null) {
                if(Token.INCLUDE_NEVER.equals(configPolicy.getIncludeToken())){
                    WSSAssertion wssAssertion = context.getWSSAssertion();
                    if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(configPolicy.getReferenceType())){
                        if(wssAssertion != null){
                            if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUST_SUPPORT_REF_KEYIDENTIFIER))
                                configPolicy.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                            else if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUSTSUPPORT_REF_THUMBPRINT))
                                configPolicy.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);
                        } else{
                            // when wssAssertion is not set use KeyIdentifier
                            configPolicy.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                        }
                    }
                }
             }
        
        return configPolicy;
    }
}
