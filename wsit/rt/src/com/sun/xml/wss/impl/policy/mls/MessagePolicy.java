/*
 * $Id: MessagePolicy.java,v 1.4 2010-03-20 12:32:28 kumarjayanti Exp $
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

package com.sun.xml.wss.impl.policy.mls;

import com.sun.xml.wss.impl.MessageConstants;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import com.sun.xml.wss.XWSSecurityException;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.PolicyGenerationException;
import com.sun.xml.wss.impl.PolicyTypeUtil;
import java.util.HashMap;
import com.sun.xml.wss.impl.configuration.*;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import com.sun.xml.wss.impl.AlgorithmSuite;
import com.sun.xml.wss.impl.WSSAssertion;

import com.sun.xml.wss.impl.MessageLayout;
/**
 * Represents an ordered collection of Security Policies
 */
public class MessagePolicy implements SecurityPolicy {
    
    protected static final Logger log =  Logger.getLogger(
            LogDomainConstants.IMPL_CONFIG_DOMAIN,
            LogDomainConstants.IMPL_CONFIG_DOMAIN_BUNDLE);
    
    private ArrayList info;
    private ArrayList optionals;
    
    private boolean dumpMessages = false;
    private boolean enableDynamicPolicyFlag = false;
    private boolean bsp = false;
    private boolean enableWSS11PolicyFlag = false;
    private boolean enableSignatureConfirmation = false;
    private WSSAssertion wssAssertion;
    private MessageLayout layout = MessageLayout.Lax;
    private boolean onSSL = false;
    private int optimizedType = -1;

    //TODO: temporary workaround for obtain the algosuite
    // need to remove this once we have the SC Layer taking care of it
    private AlgorithmSuite algoSuite;
    
    
    /**
     * Construct an Empty MessagePolicy
     */
    public MessagePolicy() {
        info = new ArrayList();
        optionals = new ArrayList();
    }
    
    @SuppressWarnings("unchecked")
     public int getOptimizedType() throws XWSSecurityException {
        
        if ( optimizedType != -1 )
            return optimizedType;
        
        if ( enableDynamicPolicy() ) {
            optimizedType = MessageConstants.NOT_OPTIMIZED;
            return optimizedType;
        }
        
        StringBuffer securityOperation = new StringBuffer();
        securityOperation.append("_BODY");
        
        StringBuffer tmpBuffer = new StringBuffer("");
        
        SignatureTarget sigTarget = null;
        EncryptionTarget encTarget = null;
        
        WSSPolicy policy = null;
        String targetValue = null;
        int secureHeaders = -1;
        int secureAttachments = -1;
        
        HashMap map = new HashMap();
        
        ArrayList primaryPolicies = getPrimaryPolicies() ;
        ArrayList secondaryPolicies = getSecondaryPolicies();
        
        int size = primaryPolicies.size();
        int secondaryPoliciesSize = secondaryPolicies.size();
        
        
        if ( size == 0 && secondaryPoliciesSize > 0 ) {
            optimizedType = MessageConstants.SECURITY_HEADERS;
            return optimizedType;
        }
        
        
        
        int iterator = 0;
        
        for ( iterator =0 ; iterator < secondaryPoliciesSize; iterator++) {
            policy = (WSSPolicy)secondaryPolicies.get(iterator);
            if ( policy.getType().intern() == "uri" ) {
                if ( PolicyTypeUtil.usernameTokenPolicy(policy)) {
                    map.put("UsernameToken", policy.getUUID() );
                } else if ( PolicyTypeUtil.timestampPolicy(policy)) {
                    map.put("Timestamp", policy.getUUID());
                } else if ( PolicyTypeUtil.samlTokenPolicy(policy)) {
                    map.put("Assertion", policy.getUUID());
                }
            }
        }
        
        
        
        for ( iterator=0; iterator<size; iterator++ ) {
            policy = (WSSPolicy)primaryPolicies.get(iterator);
            
            if ( PolicyTypeUtil.signaturePolicy(policy) ) {
                tmpBuffer.delete(0, tmpBuffer.length());
                SignaturePolicy.FeatureBinding featureBinding =
                        (SignaturePolicy.FeatureBinding)policy.getFeatureBinding();
                
                int targetBindingSize = featureBinding.getTargetBindings().size();
                for ( int targetIterator = 0; targetIterator<targetBindingSize; targetIterator++) {
                    sigTarget = (SignatureTarget)featureBinding.getTargetBindings().get(targetIterator);
                    
                    if (sigTarget == null){
                        throw new XWSSecurityException("Signature Target is null.");
                    }
                    
                    if ( sigTarget != null &&
                            sigTarget.getTransforms().size() > 1 ) {
                        optimizedType = MessageConstants.NOT_OPTIMIZED;
                        return optimizedType;
                    }
                    
                    if ( sigTarget.getTransforms().size() == 1) {
                        SignatureTarget.Transform transform = (SignatureTarget.Transform)sigTarget.getTransforms().get(0);
                        if ( transform != null ) {
                            if ( transform.getTransform().intern() !=
                                    MessageConstants.TRANSFORM_C14N_EXCL_OMIT_COMMENTS) {
                                optimizedType = MessageConstants.NOT_OPTIMIZED;
                                return optimizedType;
                            }
                        }
                    }
                    
                    
                    if ( sigTarget.getType().intern() == "qname") {
                        targetValue = sigTarget.getQName().getLocalPart().intern();
                    } else if ( sigTarget.getType().intern() == "uri") {
                        if ( map.containsKey(sigTarget.getValue() )) {
                            targetValue = map.get(sigTarget.getValue()).toString();
                        } else if ( sigTarget.getValue().intern() == "attachmentRef:attachment" || 
                                    sigTarget.getValue().startsWith("cid:")) {
                            targetValue = "Attachment";
                        }
                    } else if ( sigTarget.getType().intern() == "xpath") {
                        optimizedType = MessageConstants.NOT_OPTIMIZED;
                        return optimizedType;
                    }
                    
                    if ( targetValue == "Body" ) {
                        if ( tmpBuffer.indexOf("_SIGN") == -1) {
                            tmpBuffer.append("_SIGN");
                            if ( secureHeaders == 1 || secureHeaders == -1)
                                secureHeaders = 0;
                            if ( secureAttachments == 1 || secureAttachments == -1)
                                secureAttachments = 0;
                        }
                    } else if ( targetValue == "Timestamp" || 
                                targetValue == "UsernameToken" || 
                                targetValue == "Assertion" ) {
                        if ( secureHeaders == -1)
                            secureHeaders = 1;
                    } else if ( targetValue == "Attachment") {
                        if ( secureAttachments == -1 )
                            secureAttachments = 1;
                    } else {
                        return MessageConstants.NOT_OPTIMIZED;
                    }
                }
                securityOperation.insert(securityOperation.indexOf("_BODY"), tmpBuffer.toString());
            } else if ( PolicyTypeUtil.encryptionPolicy(policy) ) {
                tmpBuffer.delete(0, tmpBuffer.length());
                EncryptionPolicy.FeatureBinding featureBinding =
                        (EncryptionPolicy.FeatureBinding)policy.getFeatureBinding();
                
                int targetBindingSize = featureBinding.getTargetBindings().size();
                for ( int targetIterator = 0; targetIterator<targetBindingSize; targetIterator++) {
                    encTarget = (EncryptionTarget)featureBinding.getTargetBindings().get(targetIterator);
                    
                    
                    
                    if ( encTarget.getType().intern() == "qname") {
                        targetValue = encTarget.getQName().getLocalPart().intern();
                    } else if ( encTarget.getType().intern() == "uri") {
                        if ( map.containsKey(encTarget.getValue() )) {
                            targetValue = map.get(encTarget.getValue()).toString();
                        } else if ( encTarget.getValue().intern() == "attachmentRef:attachment" || 
                                    encTarget.getValue().startsWith("cid:")) {
                            targetValue = "Attachment";
                        }
                    } else if ( encTarget.getType().intern() == "xpath") {
                        optimizedType = MessageConstants.NOT_OPTIMIZED;
                        return optimizedType;
                    }
                    
                    if ( targetValue == "Body" ) {
                        if ( tmpBuffer.indexOf("_ENCRYPT") == -1) {
                            tmpBuffer.append("_ENCRYPT");
                            if ( secureHeaders == 1 || secureHeaders == -1)
                                secureHeaders = 0;
                            if ( secureAttachments == 1 || secureAttachments == -1)
                                secureAttachments = 0;
                        }
                    } else if ( targetValue == "Timestamp" || 
                                targetValue == "UsernameToken" || 
                                targetValue == "Assertion" ) {
                        if ( secureHeaders == -1)
                            secureHeaders = 1;
                    } else if ( targetValue == "Attachment") {
                        if ( secureAttachments == -1 )
                            secureAttachments = 1;
                    } else {
                        return MessageConstants.NOT_OPTIMIZED;
                    }
                }
                securityOperation.insert(securityOperation.indexOf("_BODY"), tmpBuffer.toString());
            }
        }
        
        
        
        if ( secureHeaders == 1 && secureAttachments != 1) {
            optimizedType = MessageConstants.SECURITY_HEADERS;
            return optimizedType;
        } else if ( secureAttachments == 1 && secureAttachments != 1) {
            optimizedType = MessageConstants.SECURE_ATTACHMENTS;
            return optimizedType;
        } else if ( secureHeaders == 1 && secureAttachments == 1) {
            optimizedType = MessageConstants.SECURITY_HEADERS_AND_ATTACHMENTS;
            return optimizedType;
        }
        
        String type = securityOperation.toString().intern();
        
        if ( type == "_SIGN_BODY") {
            optimizedType = MessageConstants.SIGN_BODY;
        } else if ( type == "_SIGN_ENCRYPT_BODY") {
            optimizedType =  MessageConstants.SIGN_ENCRYPT_BODY;
        } else if (type == "_ENCRYPT_SIGN_BODY") {
            optimizedType = MessageConstants.NOT_OPTIMIZED;//MessageConstants.ENCRYPT_SIGN_BODY;
        } else if ( type == "_ENCRYPT_BODY") {
            optimizedType = MessageConstants.NOT_OPTIMIZED;// MessageConstants.ENCRYPT_BODY;
        }
        
        return optimizedType;
    }
    
    
    /**
     * Append a SecurityPolicy
     * @param item SecurityPolicy instance to be appended
     * @throws PolicyGenerationException if the policy being appended is
     * not an instance of <code>WSSPolicy</code>
     */
    @SuppressWarnings("unchecked")
    public void append(SecurityPolicy item)
    throws PolicyGenerationException {
        //BooleanComposer.checkType(item);
        info.add(item);
    }
    
    /**
     * Prepend a SecurityPolicy
     * @param item SecurityPolicy instance to be prepended
     * @throws PolicyGenerationException if the policy being prepended is
     * not an instance of <code>WSSPolicy</code>
     */
    @SuppressWarnings("unchecked")
    public void prepend(SecurityPolicy item)
    throws PolicyGenerationException {
        //BooleanComposer.checkType(item);
        int i = 0;
        for(i = 0; i < info.size(); i++ ){
            SecurityPolicy sp = (SecurityPolicy)info.get(i);
            if(!sp.getType().equals(PolicyTypeUtil.SIGNATURE_CONFIRMATION_POLICY_TYPE)){
                break;
            }
        }
        info.add(i, item);
    }
    
    /**
     * Append a policy collection
     * @param items Collection of SecurityPolicy instances to be appended
     * @throws PolicyGenerationException
     */
    @SuppressWarnings("unchecked")
    public void appendAll(Collection items)
    throws PolicyGenerationException {
        Iterator i = items.iterator();
        while (i.hasNext()) {
            SecurityPolicy item = (SecurityPolicy) i.next();
            //BooleanComposer.checkType(item);
        }
        info.addAll(items);
    }
    
    /**
     * clear this policy collection
     */
    public void removeAll() {
        info.clear();
    }
    
    /**
     * @return size of policy collection
     */
    public int size() {
        return info.size();
    }
    
    /**
     * Get the Security policy at the specified index
     * @param index index to the policy collection
     * @return SecurityPolicy instance at the specified index
     * @throws Exception if a policy could not be retrieved
     */
    public SecurityPolicy get(int index) throws Exception {
        
        if (!optionals.isEmpty()) addOptionals();
        
        return (SecurityPolicy) info.get(index);
    }
    
    /**
     * @return <code>Iterator</code> iterator on policy collection
     */
    public Iterator iterator() {
        
        if (!optionals.isEmpty()) addOptionals();
        
        return info.iterator();
    }
    
    /**
     * @return true if collection is empty
     */
    public boolean isEmpty() {
        return info.isEmpty();
    }
    
    /**
     * remove the specified SecurityPolicy
     * @param item the SecurityPolicy instance to be removed
     */
    public void remove(SecurityPolicy item) {
        int i = info.indexOf(item);
        if (i == -1) {
            return;
        }
        info.remove(i);
    }
    
    /**
     * Insert the additional policy before the existing policy
     * @param existing SecurityPolicy instance before which the additional policy needs to be inserted
     * @param additional SecurityPolicy instance to be inserted
     * @throws PolicyGenerationException if the policy to be inserted is not an instance of <code>WSSPolicy</code>,
     * or there is an error in inserting the policy
     */
    @SuppressWarnings("unchecked")
    public void insertBefore(SecurityPolicy existing, SecurityPolicy additional)
    throws PolicyGenerationException {
        //BooleanComposer.checkType(existing);
        //BooleanComposer.checkType(additional);
        
        int i = info.indexOf(existing);
        if (i == -1) {
            return;
        }
        info.add(i, additional);
    }
    
    /**
     * @param dump set it to true if messages should be Logged
     */
    public void dumpMessages(boolean dump) {
        dumpMessages = dump;
    }
    
    /**
     * @return true if logging of messages is enabled
     */
    public boolean dumpMessages() {
        return dumpMessages;
    }
    
    /*
     * @param flag boolean that indicates if dynamic policy is enabled
     */
    public void enableDynamicPolicy(boolean flag) {
        enableDynamicPolicyFlag = flag;
    }
    
    /*
     * @return true if dynamic policy is enabled
     */
    public boolean enableDynamicPolicy() {
        return enableDynamicPolicyFlag;
    }
    
    public void setWSSAssertion(WSSAssertion wssAssertion) 
        throws PolicyGenerationException{
        this.wssAssertion = wssAssertion;
        if("1.1".equals(this.wssAssertion.getType())){
            enableWSS11PolicyFlag = true;
        }
        if(this.wssAssertion.getRequiredProperties().contains("RequireSignatureConfirmation")){
            enableSignatureConfirmation = true;
        }
        if(enableSignatureConfirmation){
            SignatureConfirmationPolicy signConfirmPolicy = new SignatureConfirmationPolicy();
            String id = SecurityUtil.generateUUID();
            signConfirmPolicy.setUUID(id);
            prepend(signConfirmPolicy);
        }        
    }

    public WSSAssertion getWSSAssertion() {
        return this.wssAssertion;
    }
    
    public void enableSignatureConfirmation(boolean flag) throws PolicyGenerationException{
        enableSignatureConfirmation = flag;
        if(enableSignatureConfirmation){
            SignatureConfirmationPolicy signConfirmPolicy = new SignatureConfirmationPolicy();
            String id = SecurityUtil.generateUUID();
            signConfirmPolicy.setUUID(id);
            append(signConfirmPolicy);
        }
    }

   public boolean enableSignatureConfirmation() {
       return enableSignatureConfirmation;
   }

   public void enableWSS11Policy(boolean flag){
       enableWSS11PolicyFlag = flag;
   }
    
    public boolean enableWSS11Policy() {
        return enableWSS11PolicyFlag;
    }
    
    /*
     */
    public void isBSP(boolean flag) {
        bsp = flag;
    }
    
    /*
     */
    public boolean isBSP() {
        return bsp;
    }
    
    /*
     */
    public void removeOptionalTargets() {
        optionals.clear();
    }
    
    /*
     * @param optionals specify optional targets that can be signed/encrypted
     */
    @SuppressWarnings("unchecked")
    public void addOptionalTargets(ArrayList optionls) throws XWSSecurityException {
        Iterator i = optionls.iterator();
        
        while (i.hasNext()) {
            try {
                Target target = (Target) i.next();
                target.setEnforce(false);
            } catch (ClassCastException cce) {
                String message = "Target should be of types: " +
                        "com.sun.xml.wss.impl.policy.mls.SignatureTarget OR " +
                        "com.sun.xml.wss.impl.policy.mls.EncryptionTarget";
                log.log(Level.SEVERE, "WSS1100.classcast.target",
                        new Object[] {message});
                        throw new XWSSecurityException(message, cce);
            }
        }
        
        optionals.addAll(optionls);
    }
    
    /*
     * @param target add an optional target for signature/encryption
     */
    @SuppressWarnings("unchecked")
    public void addOptionalTarget(Target target) {
        target.setEnforce(false);
        optionals.add(target);
    }
    
    /**
     * Equals operator
     * @param policy <code>MessagePolicy</code> to be compared for equality
     * @return true if the policy is equal to this policy
     */
    public boolean equals(MessagePolicy policy) {
        
        boolean assrt = policy.dumpMessages() && policy.enableDynamicPolicy();
        
        if (assrt) {
            ArrayList primary0 = getPrimaryPolicies();
            ArrayList secdary0 = getSecondaryPolicies();
            
            ArrayList primary1 = policy.getPrimaryPolicies();
            ArrayList secdary1 = policy.getSecondaryPolicies();
            
            if (primary0.equals(primary1) && secdary0.equals(secdary1)) assrt = true;
        }
        
        return assrt;
    }
    
    /*
     * @return primary policy list
     */
    @SuppressWarnings("unchecked")
    public ArrayList getPrimaryPolicies() {
        ArrayList list = new ArrayList();
        
        Iterator i = iterator();
        while (i.hasNext()) {
            SecurityPolicy policy = (SecurityPolicy) i.next();
            if(PolicyTypeUtil.encryptionPolicy(policy) || PolicyTypeUtil.signaturePolicy(policy)){
                list.add(policy);
            }
        }
        
        return list;
    }
    
    /*
     * @return secondary policy list
     */
    @SuppressWarnings("unchecked")
    public ArrayList getSecondaryPolicies() {
        ArrayList list = new ArrayList();
        
        Iterator i = iterator();
        while (i.hasNext()) {
            SecurityPolicy policy = (SecurityPolicy) i.next();
            
            if(PolicyTypeUtil.authenticationTokenPolicy(policy) || PolicyTypeUtil.timestampPolicy(policy)){
                list.add(policy);
            }
        }
        
        return list;
    }
    
    private void addOptionals() {
        
        Iterator j = info.iterator();
        
        while (j.hasNext()) {
            
            SecurityPolicy policy = (SecurityPolicy) j.next();
            
            if (policy instanceof WSSPolicy) {
                processWSSPolicy((WSSPolicy) policy);
            } /*else
                if (PolicyTypeUtil.booleanComposerPolicy(policy)) {
                processBooleanComposer((BooleanComposer)policy);
                }*/
            
        }
        
        optionals.clear();
    }
    
    /*
     * @param policy WSSPolicy
     */
    private void processWSSPolicy(WSSPolicy policy) {
        if (PolicyTypeUtil.signaturePolicy(policy)) {
            SignaturePolicy sPolicy = (SignaturePolicy) policy;
            SignaturePolicy.FeatureBinding fBinding =
                    (SignaturePolicy.FeatureBinding) sPolicy.getFeatureBinding();
            
            Iterator it = optionals.iterator();
            for (; it.hasNext(); ) {
                fBinding.addTargetBinding((Target)it.next());
            }
        } else
            if (PolicyTypeUtil.encryptionPolicy(policy)) {
            EncryptionPolicy ePolicy = (EncryptionPolicy) policy;
            EncryptionPolicy.FeatureBinding fBinding =
                    (EncryptionPolicy.FeatureBinding) ePolicy.getFeatureBinding();
            
            Iterator it = optionals.iterator();
            for (; it.hasNext(); ) {
                fBinding.addTargetBinding((Target)it.next());
            }
            }
    }
    
    /*
     * @param composer BooleanComposer
     */
    /*private void processBooleanComposer(BooleanComposer composer) {
        if (PolicyTypeUtil.booleanComposerPolicy(composer.getPolicyA())) {
               processBooleanComposer((BooleanComposer) composer.getPolicyA());
        } else {
            processWSSPolicy((WSSPolicy) composer.getPolicyA());
        }
        
        if (PolicyTypeUtil.booleanComposerPolicy(composer.getPolicyB())) {
            processBooleanComposer((BooleanComposer) composer.getPolicyB());
        } else {
            processWSSPolicy((WSSPolicy) composer.getPolicyB());
        }
    }*/
    
    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.MESSAGEPOLICY_CONFIG_TYPE;
    }
    
    public void setAlgorithmSuite(AlgorithmSuite algSuite){
        this.algoSuite = algSuite;
    }
    
    public AlgorithmSuite getAlgorithmSuite(){
        return this.algoSuite;
    }
    
    public MessageLayout getLayout(){
        return layout;
    }
    
    public void setLayout(MessageLayout layout){
        this.layout = layout;
    }
    
    public void setSSL(boolean value){
        this.onSSL = value;
    }
    public boolean isSSL(){
        return onSSL;
    }
    
}
