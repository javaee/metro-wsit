/*
 * EncryptionPolicyVerifier.java
 *
 * Created on August 7, 2005, 9:08 PM
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


package com.sun.xml.wss.impl.policy.verifier;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.policy.spi.PolicyVerifier;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.SAMLAssertionBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.X509CertificateBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.WSSAssertion;
import com.sun.xml.wss.impl.MessageConstants;

/**
 *
 * @author  K.Venugopal@sun.com
 */
public class EncryptionPolicyVerifier implements PolicyVerifier{
    
    FilterProcessingContext context;
    
    /** Creates a new instance of EncryptionPolicyVerifier */
    public EncryptionPolicyVerifier (FilterProcessingContext context) {
        this.context = context;
    }
    
    /**
     *
     * @param configPolicy Policy configured for the incoming message.
     * @param recvdPolicy policy infered from the incoming message.
     * @throws com.sun.xml.wss.PolicyViolationException when policy infered from incoming message does not match with what
     * is configured.
     *
     */
    public void verifyPolicy (SecurityPolicy configPolicy, SecurityPolicy recvdPolicy) throws PolicyViolationException {
        if(PolicyTypeUtil.encryptionPolicy (configPolicy) && PolicyTypeUtil.encryptionPolicy (recvdPolicy)){
            EncryptionPolicy rEP = (EncryptionPolicy)recvdPolicy;
            EncryptionPolicy cEP = (EncryptionPolicy)configPolicy;
            
            EncryptionPolicy.FeatureBinding rfBinding = (EncryptionPolicy.FeatureBinding)rEP.getFeatureBinding ();
            EncryptionPolicy.FeatureBinding cfBinding = (EncryptionPolicy.FeatureBinding)cEP.getFeatureBinding ();
            String rDA = rfBinding.getDataEncryptionAlgorithm ();
            String cDA = cfBinding.getDataEncryptionAlgorithm ();
            if(cDA != null && cDA.length () > 0 ){
                if(!cDA.equals (rDA)){
                    throw new PolicyViolationException ("Receiver side requirement verification failed, "+
                            "DataEncryptionAlgorithm specified in the receiver requirements did match with"
                            +" DataEncryptionAlgorithm used to encrypt the message."+
                            "Configured DataEncryptionAlgorithm is "+cDA+"  DataEncryptionAlgorithm used in the" +
                            "message is "+rDA);
                }
            }
            /*
            WSSPolicy ckeyBinding =  (WSSPolicy) cEP.getKeyBinding ();
            if(ckeyBinding != null){
                String cKeyType = ckeyBinding.getType ();
                WSSPolicy rkeyBinding =  (WSSPolicy) rEP.getKeyBinding ();
                if(rkeyBinding == null){
                    throw new PolicyViolationException ("KeyType used to Encrypt the message doesnot match with " +
                            " the receiver side requirements. Configured KeyType is "+ckeyBinding+
                            " KeyType inferred from the message is  "+ rkeyBinding);
                }
                String rKeyType = rkeyBinding.getType ();
                //TODO: Verification of KeyBinding later
                if(rKeyType != cKeyType){
                    throw new PolicyViolationException ("KeyType used to Encrypt the message doesnot match with " +
                            " the receiver side requirements. Configured KeyType is "+ckeyBinding+
                            " KeyType inferred from the message is  "+ rkeyBinding);
                    //log propert error message.
                }
                if(cKeyType == PolicyTypeUtil.SAMLASSERTION_TYPE){
                    checkSAMLAssertionBinding ((SAMLAssertionBinding)ckeyBinding,(SAMLAssertionBinding)rkeyBinding);
                }else if(cKeyType == PolicyTypeUtil.X509CERTIFICATE_TYPE ){
                    checkX509CertificateBinding ((X509CertificateBinding)ckeyBinding,(X509CertificateBinding)rkeyBinding);
                }
                
            } */
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
        
        if(cVT != null && cVT.length () > 0 ){
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
         
        if(!matched){
            throw new PolicyViolationException ("KeyType used to sign the message doesnot match with " +
                    " the receiver side requirements. Configured KeyType is "+configPolicy+
                    " KeyType inferred from the message is  "+ recvdPolicy);
        }*/
    }
    
    private final void _throwError (SecurityPolicy configPolicy, SecurityPolicy recvdPolicy, boolean matched)throws PolicyViolationException {
        if(!matched){
            throw new PolicyViolationException ("KeyType used to Encrypt the message doesnot match with " +
                    " the receiver side requirements. Configured KeyType is "+configPolicy+
                    " KeyType inferred from the message is  "+ recvdPolicy);
        }
    }
    
    private X509CertificateBinding setReferenceType(X509CertificateBinding configPolicy){
        
            //Token policyToken = configPolicy.getPolicyToken();
            //if (policyToken != null) {
            if (configPolicy.policyTokenWasSet()) {
                if(configPolicy.INCLUDE_NEVER.equals(configPolicy.getIncludeToken())){
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
