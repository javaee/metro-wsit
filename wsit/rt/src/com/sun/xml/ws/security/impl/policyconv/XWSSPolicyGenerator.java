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
import com.sun.xml.ws.security.impl.policy.LogStringsMessages;
import com.sun.xml.ws.security.impl.policy.Trust10;
import com.sun.xml.ws.security.impl.policyconv.IntegrityAssertionProcessor;
import com.sun.xml.ws.security.impl.policyconv.XWSSPolicyContainer;
import com.sun.xml.ws.security.policy.AsymmetricBinding;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Binding;
import com.sun.xml.ws.security.policy.EncryptedElements;
import com.sun.xml.ws.security.policy.EncryptedParts;
import com.sun.xml.ws.security.policy.EndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.RequiredElements;
import com.sun.xml.ws.security.policy.SignedElements;
import com.sun.xml.ws.security.policy.SignedEndorsingSupportingTokens;
import com.sun.xml.ws.security.policy.SignedParts;
import com.sun.xml.ws.security.policy.SignedSupportingTokens;
import com.sun.xml.ws.security.policy.SupportingTokens;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.policy.TransportBinding;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import com.sun.xml.ws.security.policy.WSSAssertion;
import java.util.logging.Level;


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
    private WSSAssertion wss11 = null;
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
    private boolean transportBinding = false;
    private IntegrityAssertionProcessor iAP = null;
    private EncryptionAssertionProcessor eAP = null;
    private Binding policyBinding = null;
    private List<RequiredElements> reqElements = new ArrayList<RequiredElements>();
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
            logger.log(Level.SEVERE,LogStringsMessages.SP_0105_ERROR_BINDING_ASSR_NOT_PRESENT());
            throw new PolicyException("Effective SecurityPolicy does not have a Binding");
        }
        if(PolicyUtil.isTransportBinding(binding)){
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE, "TransportBinding was configured in the policy");
            }
            TransportBindingProcessor tbp= new TransportBindingProcessor((TransportBinding)binding,isServer, isIncoming,_policyContainer);
            tbp.process();
            processNonBindingAssertions(tbp);
            transportBinding = true;
        }else{
            
            iAP = new IntegrityAssertionProcessor(_binding.getAlgorithmSuite(),_binding.isSignContent());
            eAP = new EncryptionAssertionProcessor(_binding.getAlgorithmSuite(),false);
            
            _policyContainer.setPolicyContainerMode(_binding.getLayout());
            if(PolicyUtil.isSymmetricBinding(binding.getName())) {
                
                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE, "SymmetricBinding was configured in the policy");
                }
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
                
                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE, "AsymmetricBinding was configured in the policy");
                }
                AsymmetricBindingProcessor abp = new AsymmetricBindingProcessor((AsymmetricBinding) _binding, _policyContainer,
                        isServer, isIncoming,signedParts,encryptedParts,
                        signedElements,encryptedElements);
                if( wssAssertion != null && PolicyUtil.isWSS11(wssAssertion)){
                    abp.setWSS11((WSSAssertion)wssAssertion);
                }
                abp.process();
                processNonBindingAssertions(abp);
                abp.close();
            }
        }
    }
    
    public MessagePolicy getXWSSPolicy()throws PolicyException{
        MessagePolicy mp = null;
        try{
            mp = _policyContainer.getMessagePolicy();
        }catch(PolicyGenerationException ex){
            logger.log(Level.SEVERE,""+effectivePolicy,ex);
            throw new PolicyException("Unable to digest SecurityPolicy ");
        }
        //try{
        if(wssAssertion != null){
            try{
                mp.setWSSAssertion(getWssAssertion((com.sun.xml.ws.security.policy.WSSAssertion) wssAssertion));
            } catch (PolicyGenerationException ex) {
                logger.log(Level.SEVERE,LogStringsMessages.SP_0104_ERROR_SIGNATURE_CONFIRMATION_ELEMENT(ex.getMessage()),ex);
                 throw new PolicyException("Unable to process WSSAssertion");
            }
        }
        if(policyBinding.getAlgorithmSuite() != null){
            mp.setAlgorithmSuite(getAlgoSuite(policyBinding.getAlgorithmSuite()));
        }
        if(policyBinding.getLayout()!= null){
            mp.setLayout(getLayout(policyBinding.getLayout()));
        }
        if(isIncoming && reqElements.size() > 0){
            try {
                com.sun.xml.ws.security.impl.policyconv.RequiredElementsProcessor rep =
                        new com.sun.xml.ws.security.impl.policyconv.RequiredElementsProcessor(reqElements, mp);
                rep.process();
            } catch (PolicyGenerationException ex) {
                logger.log(Level.SEVERE,LogStringsMessages.SP_0103_ERROR_REQUIRED_ELEMENTS(ex.getMessage()),ex);
                throw new PolicyException("Unable to process RequiredElements");
            }
        }
        if(transportBinding){
            mp.setSSL(transportBinding);
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
                }else if(PolicyUtil.isRequiredElements(assertion)){
                    reqElements.add((RequiredElements)assertion);
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
    
    protected com.sun.xml.wss.impl.AlgorithmSuite getAlgoSuite(AlgorithmSuite suite) {
        com.sun.xml.wss.impl.AlgorithmSuite als = new com.sun.xml.wss.impl.AlgorithmSuite(
                suite.getDigestAlgorithm(),
                suite.getEncryptionAlgorithm(),
                suite.getSymmetricKeyAlgorithm(),
                suite.getAsymmetricKeyAlgorithm());
        
        return als;
    }
    
    protected com.sun.xml.wss.impl.WSSAssertion getWssAssertion(WSSAssertion asser) {
        com.sun.xml.wss.impl.WSSAssertion assertion = new com.sun.xml.wss.impl.WSSAssertion(
                asser.getRequiredProperties(),
                asser.getType());
        return assertion;
    }
    
    protected com.sun.xml.wss.impl.MessageLayout getLayout(
            com.sun.xml.ws.security.policy.MessageLayout layout) {
        
        switch(layout) {
        case Strict :{
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,"MessageLayout has been configured to be  STRICT ");
            }
            return com.sun.xml.wss.impl.MessageLayout.Strict;
        }
        case Lax :{
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,"MessageLayout has been configured to be LAX ");
            }
            return com.sun.xml.wss.impl.MessageLayout.Lax;
        }
        case LaxTsFirst :{
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,"MessageLayout has been configured to be LaxTimestampFirst ");
            }
            return com.sun.xml.wss.impl.MessageLayout.LaxTsFirst;
        }
        case LaxTsLast :{
            if(logger.isLoggable(Level.FINE)){
                logger.log(Level.FINE,"MessageLayout has been configured tp be LaxTimestampLast ");
            }
            return com.sun.xml.wss.impl.MessageLayout.LaxTsLast;
        }default :{
            if(logger.isLoggable(Level.SEVERE)){
                logger.log(Level.SEVERE,LogStringsMessages.SP_0106_UNKNOWN_MESSAGE_LAYOUT(layout));
            }
            throw new RuntimeException("Unkown MessageLayout Enum Value Encountered");
        }
        }        
    }
    
}
