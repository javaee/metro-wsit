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

import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.security.impl.policy.Trust10;
import com.sun.xml.ws.security.impl.policy.Wss10;
import com.sun.xml.ws.security.impl.policy.Wss11;
import com.sun.xml.ws.security.impl.policyconv.IntegrityAssertionProcessor;
import com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.Header;
import com.sun.xml.ws.security.policy.SamlToken;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.policy.Target;
import com.sun.xml.ws.security.policy.Token;
import com.sun.xml.ws.security.policy.TransportBinding;
import com.sun.xml.ws.security.policy.X509Token;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.wss.core.UsernameToken;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy.FeatureBinding;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.KeyBindingBase;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSKeyBindingExtension;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import java.lang.UnsupportedOperationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.namespace.QName;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.ws.security.policy.WSSAssertion;


/**
 * Will convert WS Security Policy to XWSS policy.
 * WS Security Policies are  digested and cached by the
 * first pipe. The same objects are shared across instances
 * of the pipe(refer to Pipe javadoc for more information)
 *
 * @author K.Venugopal@sun.com
 */

public class XWSSPolicyGenerator {
    
    String _protectionOrder = "";
    
    SignaturePolicy _primarySP  = null;
    EncryptionPolicy _primaryEP = null;
    //current secondary encryption policy
    EncryptionPolicy _sEncPolicy = null;
    SignaturePolicy _csSP = null;
    XWSSPolicyContainer _policyContainer = null;
    Binding _binding;
    Policy effectivePolicy = null;
    int id = 1;
    boolean isServer = false;
    boolean isIncoming = false;
    private PolicyAssertion wssAssertion = null;
    //private WSSAssertion wss11 = null;
    private Trust10 trust10 = null;
    private AlgorithmSuite algSuite = null;
    //true if signed by primary signature
    private boolean signBody = false;
    
    //true if encrypted by primary encryption policy
    private boolean encryptBody = false;
    //private HashSet<Header> signParts  = new HashSet<Header>();
    
    private Vector<SignedParts> signedParts = new Vector<SignedParts>();
    private Vector<EncryptedParts> encryptedParts = new Vector<EncryptedParts>();
    private Vector<SignedElements> signedElements = new Vector<SignedElements>();
    private Vector<EncryptedElements> encryptedElements = new Vector<EncryptedElements>();
    private boolean ignoreST = false;
    
    private IntegrityAssertionProcessor iAP = null;
    private EncryptionAssertionProcessor eAP = null;
    private Binding policyBinding = null;
    /** Creates a new instance of WSPolicyProcessorImpl */
    //public XWSSPolicyGenerator(AssertionSet assertionSet,boolean isServer,boolean isIncoming){
    public XWSSPolicyGenerator(Policy effectivePolicy,boolean isServer,boolean isIncoming){
        this.effectivePolicy = effectivePolicy;
        this._policyContainer = new XWSSPolicyContainer(isServer,isIncoming);
        this.isServer = isServer;
        this.isIncoming = isIncoming;
    }
    
    public AlgorithmSuite getBindingLevelAlgSuite(){
        return _binding.getAlgorithmSuite();
    }
    
    public void process(boolean ignoreST) throws PolicyException {
        this.ignoreST = ignoreST;
        process();
    }
    
    
    public void process() throws PolicyException {
        collectPolicies();
        PolicyAssertion binding = (PolicyAssertion)getBinding();
        policyBinding =(Binding) binding;
        if(binding == null){
            //log error.
            throw new PolicyException("Error Effective Security Policy does not have a Binding");
        }
        if(PolicyUtil.isTransportBinding(binding)){
            TransportBindingProcessor tbp= new TransportBindingProcessor((TransportBinding)binding,isServer, isIncoming,_policyContainer);
            //    _policyContainer.getMessagePolicy().setAlgorithmSuite(((TransportBinding)binding).getAlgorithmSuite());
            tbp.process();
            //   _policyContainer.getMessagePolicy().setLayout(((TransportBinding)binding).getLayout());
            processNonBindingAssertions(tbp);
            //TODO
            //else if(PolicyUtil.isTransportBinding(binding.getName()) ){
//            //handle Transport Binding.
//            processTransportBindingPolicy((TransportBinding)binding);
//
//        }
        }else{
            
            iAP = new IntegrityAssertionProcessor(_binding.getAlgorithmSuite(),_binding.isSignContent());
            eAP = new EncryptionAssertionProcessor(_binding.getAlgorithmSuite(),false);
            
            _policyContainer.setPolicyContainerMode(_binding.getLayout());
            //  _policyContainer.getMessagePolicy().setLayout(_binding.getLayout());
            if(PolicyUtil.isSymmetricBinding(binding.getName())) {
                //  _policyContainer.getMessagePolicy().setAlgorithmSuite(((SymmetricBinding) _binding).getAlgorithmSuite());
                SymmetricBindingProcessor sbp =  new SymmetricBindingProcessor((SymmetricBinding) _binding, _policyContainer,
                        isServer, isIncoming,signedParts,encryptedParts,
                        signedElements,encryptedElements);
                if(wssAssertion != null && PolicyUtil.isWSS11(wssAssertion)){
                    sbp.setWSS11((WSSAssertion)wssAssertion);
                }
                sbp.process();
                processNonBindingAssertions(sbp);
                sbp.close();
                
            }else if(PolicyUtil.isAsymmetricBinding(binding.getName()) ){
                AsymmetricBindingProcessor abp = new AsymmetricBindingProcessor((AsymmetricBinding) _binding, _policyContainer,
                        isServer, isIncoming,signedParts,encryptedParts,
                        signedElements,encryptedElements);
                if( wssAssertion != null && PolicyUtil.isWSS11(wssAssertion)){
                    abp.setWSS11((WSSAssertion)wssAssertion);
                }
                abp.process();
                //  _policyContainer.getMessagePolicy().setAlgorithmSuite(((AsymmetricBinding) _binding).getAlgorithmSuite());
                processNonBindingAssertions(abp);
                abp.close();
            }
        }
    }
    
    public MessagePolicy getXWSSPolicy(){
        MessagePolicy mp = _policyContainer.getMessagePolicy();
        try{
            if(wssAssertion != null){
                mp.setWSSAssertion((WSSAssertion)wssAssertion);
            }
            if(policyBinding.getAlgorithmSuite() != null){
                mp.setAlgorithmSuite(policyBinding.getAlgorithmSuite());
            }
            if(policyBinding.getLayout()!= null){
                mp.setLayout(policyBinding.getLayout());
            }
            
        }catch(PolicyGenerationException pe){
            pe.printStackTrace();
        }
        return mp;
    }
    
    private void processNonBindingAssertions(BindingProcessor bindingProcessor) throws PolicyException{
        for(AssertionSet assertionSet: effectivePolicy){
            for(PolicyAssertion assertion:assertionSet){
                if(PolicyUtil.isBinding(assertion)){
                    continue;
                }else if(!ignoreST && shouldAddST() && PolicyUtil.isSupportingToken(assertion)){
                    bindingProcessor.processSupportingTokens((SupportingTokens)assertion);
                } else if(!ignoreST && shouldAddST() && PolicyUtil.isSignedSupportingToken(assertion)){
                    bindingProcessor.processSupportingTokens((SignedSupportingTokens)assertion);
                }else if(!ignoreST && shouldAddST() && PolicyUtil.isEndorsedSupportingToken(assertion)){
                    bindingProcessor.processSupportingTokens((EndorsingSupportingTokens)assertion);
                }else if(!ignoreST && shouldAddST() && PolicyUtil.isSignedEndorsingSupportingToken(assertion)){
                    bindingProcessor.processSupportingTokens((SignedEndorsingSupportingTokens)assertion);
                }else if(PolicyUtil.isWSS10(assertion)){
                    wssAssertion = assertion;
                }else if(PolicyUtil.isWSS11(assertion)){
                    wssAssertion = assertion;
                }else if(PolicyUtil.isTrust10(assertion)){
                    trust10 = (Trust10)assertion;
                }
            }
        }
    }
    
    private Binding getBinding(){
        return _binding;
    }
    
    private void collectPolicies(){
        for(AssertionSet assertionSet: effectivePolicy){
            for(PolicyAssertion assertion:assertionSet){
                if(PolicyUtil.isSignedParts(assertion)){
                    signedParts.add((SignedParts)assertion);
                }else if(PolicyUtil.isEncryptParts(assertion)){
                    encryptedParts.add((EncryptedParts)assertion);
                }else if(PolicyUtil.isSignedElements(assertion)){
                    signedElements.add((SignedElements)assertion);
                }else if(PolicyUtil.isEncryptedElements(assertion)){
                    encryptedElements.add((EncryptedElements)assertion);
                }else if(PolicyUtil.isWSS10(assertion)){
                    wssAssertion = assertion;
                }else if(PolicyUtil.isWSS11(assertion)){
                    wssAssertion = assertion;
                }else if(PolicyUtil.isTrust10(assertion)){
                    trust10 = (Trust10)assertion;
                }else if(PolicyUtil.isBinding(assertion)){
                    _binding =(Binding) assertion;
                }
            }
        }
    }
    
    private boolean shouldAddST(){
        if(isServer && !isIncoming){
            return false;
        }
        if(!isServer && isIncoming){
            return false;
        }
        return true;
    }
}
