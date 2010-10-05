/*
 * $Id: SecurityRecipient.java,v 1.1 2010-10-05 11:50:50 m_potociar Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.wss.impl;

import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import javax.xml.soap.AttachmentPart;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.w3c.dom.NodeList;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;

import com.sun.xml.wss.core.SecurityHeader;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;

import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.filter.TimestampFilter;
import com.sun.xml.wss.impl.filter.SignatureFilter;
import com.sun.xml.wss.impl.filter.EncryptionFilter;
import com.sun.xml.wss.impl.filter.SignatureConfirmationFilter;
import com.sun.xml.wss.impl.filter.AuthenticationTokenFilter;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.*;
import com.sun.xml.wss.impl.policy.mls.Target;
import com.sun.xml.wss.logging.LogStringsMessages;

/**
 * This class exports a static Security Service for Verifying/Validating Security in an Inbound SOAPMessage.
 * The policy to be applied for Verifying the Message and the SOAPMessage itself are
 * supplied in an instance of a com.sun.xml.wss.ProcessingContext
 * @see ProcessingContext
 */
public class SecurityRecipient {
    
    private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * Validate security in an Inbound SOAPMessage.
     * <P>
     * Calling code should create com.sun.xml.wss.ProcessingContext object with
     * runtime properties. Specifically, it should set SecurityPolicy, application
     * CallbackHandler Or a SecurityEnvironment and static security policy context.
     * The SecurityPolicy instance can be of the following types:
     * <UL>
     *  <LI> A concrete WSSPolicy
     *  <LI> A MessagePolicy
     *  <LI> A DynamicSecurityPolicy
     * </UL>
     *
     * A DynamicSecurityPolicy can inturn resolve to the following:
     * <UL>
     *  <LI> A concrete WSSPolicy
     *  <LI> A MessagePolicy
     * </UL>
     * <P>
     * For cases when policy resolution does not happen accurately because the
     * identification context was not available - for eg. operation name in
     * the message is encrypted - resolution can happen to the enclosing
     * container configuration.
     * <P>
     * Policy resolution can happen as follows:
     * <P>
     *   (a). Constructed StaticPolicyContext - identifying context associated with
     *       configured security policies - is used for look-up of configured
     *       MessagePolicy/DynamicSecurityPolicy/WSSPolicy instances
     * <P>
     *   (b). NOTE: Relevant only when the SecurityPolicy is an ApplicationSecurityConfiguration, or the
     *        root element of the Security Configuration file that was used to generate the SecurityPolicy is
     *        &lt;xwss:JAXRPCSecurity&gt;
     * <P>
     *       In case an identifying context can not be constructed because such
     *       context has partly to be inferred from the message (for eg. operation
     *       name contained as the element name of SOAPBody), the resolved container
     *       configuration is used as follows:
     * <P>
     *       If C = {p1, p2....pn}
     *          where pi belongs to collection of MessagePolicies held by the container.
     * <P>
     *       an in-bound message is processed without application of a policy, whence
     *       a policy representation of the metadata contained in the security header
     *       is constructed as each header element is processed (iteration).
     * <P>
     *       If P be such policy (MessagePolicy) that gets built upon each iteration, C
     *       is reduced as follows:
     * <P>
     *          redux (P, C, M)
     * <P>
     *       where M is the SOAPMessage and
     * <P>
     *       redux:
     * <P>
     *          (1). eliminate all pi that are not loosely semantically equivalent where
     *               the equivalence is checked as follows:
     * <P>
     *               (a). SignaturePolicy:  equivalence of FeatureBinding and KeyBinding
     *                                      excepting target bindings contained in its
     *                                      FeatureBinding
     * <P>
     *               (b). EncryptionPolicy: equivalence of FeatureBinding and KeyBinding
     *                                      excepting target bindings contained in its
     *                                      FeatureBinding
     * <P>
     *               (c). AuthenticationTokenPolicy: equivalence of Feature and Key Bindings
     *
     * <P>
     *          (2). eliminate all pi whose target bindings contained with the FeatureBindings
     *               are applicable to the message. Note that all required message parts should
     *               be apparent post-processing, therefore, equivalence of target bindings
     *               would be checked by comparison of Node sets of SOAPMessage constructed
     *               using the inferred and those contained in C.
     * <P>
     *       After every such reduction, an attempt is made to resolve the policy identifier
     *       (operation name) that can be used for policy resolution, which should be contained
     *       in C and is used further for message processing.
     * <P>
     * @param context an instance of com.sun.xml.wss.ProcessingContext
     * @exception com.sun.xml.wss.XWSSecurityException if there was an unexpected error
     *     while verifying the message. OR if the security in the incoming
     *     message violates the Security policy that was applied to the message.
     * @exception WssSoapFaultException when security in the incoming message
     *     is in direct violation of the OASIS WSS specification.
     *     When a WssSoapFaultException is thrown the getFaultCode() method on the WssSoapFaultException
     *     will return a <code>QName</code> which would correspond to the WSS defined fault.
     */
    public static void validateMessage(ProcessingContext context)
    throws XWSSecurityException {
        
        HarnessUtil.validateContext(context);
        
        SecurityPolicy policy = context.getSecurityPolicy();
        StaticPolicyContext staticContext = context.getPolicyContext();
        
        FilterProcessingContext fpContext = new FilterProcessingContext(context);
        fpContext.isInboundMessage(true);

        //MessagePolicy msgPolicy = (MessagePolicy) fpContext.getSecurityPolicy();
        if(true /*msgPolicy.enableWSS11Policy()*/){
            // set a property in context to determine if its WSS11
            fpContext.setExtraneousProperty("EnableWSS11PolicyReceiver","true");
        }

        //TODO: enable this after policy is available for incoming msgs
        if (true /*msgPolicy.enableSignatureConfirmation()*/) {
            //For SignatureConfirmation
            //Set a list in extraneous property which will store all the received SignatureValues
            //If there was no Signature in incoming message this list will be empty
            List scList = new ArrayList();
            fpContext.setExtraneousProperty("receivedSignValues", scList);
        }        
        if (policy != null) {
            
            if ( PolicyTypeUtil.messagePolicy(policy) &&
                    !PolicyTypeUtil.applicationSecurityConfiguration(policy) &&
                    ((MessagePolicy)policy).enableDynamicPolicy() &&
                    ((MessagePolicy)policy).size() == 0) {
                policy = new com.sun.xml.wss.impl.policy.mls.DynamicSecurityPolicy();
            }
            
            if (PolicyTypeUtil.dynamicSecurityPolicy(policy)) {
                
                // create dynamic callback context
                DynamicApplicationContext dynamicContext = new DynamicApplicationContext(staticContext);
                dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                dynamicContext.inBoundMessage(true);
                ProcessingContext.copy(dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                
                // make dynamic policy callback
                DynamicPolicyCallback dpCallback = new DynamicPolicyCallback(policy, dynamicContext);
                HarnessUtil.makeDynamicPolicyCallback(dpCallback,
                        context.getSecurityEnvironment().getCallbackHandler());
                
                
                SecurityPolicy result = dpCallback.getSecurityPolicy();
                fpContext.setSecurityPolicy(result);
                fpContext.setMode(FilterProcessingContext.ADHOC);
                
                if (PolicyTypeUtil.messagePolicy(result)) {
                    processMessagePolicy(fpContext);
                } else if (result instanceof WSSPolicy) {
                    HarnessUtil.processWSSPolicy(fpContext);
                } else if ( result != null ) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0260_INVALID_DSP());
                    throw new XWSSecurityException("Invalid dynamic security policy returned by callback handler");
                }
                
            } else if (policy instanceof WSSPolicy) {
                //fpContext.enableDynamicPolicyCallback(((MessagePolicy)policy).enableDynamicPolicy());
                fpContext.setMode(FilterProcessingContext.ADHOC);
                HarnessUtil.processWSSPolicy(fpContext);
            } else if (PolicyTypeUtil.messagePolicy(policy)) {
                fpContext.enableDynamicPolicyCallback(((MessagePolicy)policy).enableDynamicPolicy());
                fpContext.setMode(FilterProcessingContext.ADHOC);
                processMessagePolicy(fpContext);
                checkForExtraSecurity(fpContext);
            } else if (PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                // policy c'd not be resolved accurately
                // resolution can be to the port level
                fpContext.setMode(FilterProcessingContext.POSTHOC);
                processApplicationSecurityConfiguration(fpContext);
                checkForExtraSecurity(fpContext);
            } else {
                log.log(Level.SEVERE,LogStringsMessages.WSS_0251_INVALID_SECURITY_POLICY_INSTANCE());
                throw new XWSSecurityException("SecurityPolicy instance should be of type: " +
                        "WSSPolicy OR MessagePolicy OR DynamicSecurityPolicy " +
                        "OR ApplicationSecurityConfiguration");
            }
            
        } else {
            pProcess(fpContext);
        }
        
        try {
            if (!fpContext.retainSecurityHeader()) {
                fpContext.getSecurableSoapMessage().deleteSecurityHeader();
            } else {
                fpContext.getSecurableSoapMessage().resetMustUnderstandOnSecHeader();
            }
            fpContext.getSOAPMessage().saveChanges();          

        }catch (Exception ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0370_ERROR_DELETING_SECHEADER(),ex);
            throw new XWSSecurityException(ex);
        }
    }
    
    /*
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private static void processApplicationSecurityConfiguration(FilterProcessingContext fpContext)
    throws XWSSecurityException {
        
        ApplicationSecurityConfiguration configuration = (ApplicationSecurityConfiguration) fpContext.getSecurityPolicy();
        
        Collection mConfiguration = configuration.getAllReceiverPolicies();
        
        fpContext.setSecurityPolicy(new MessagePolicy());
        
        SOAPElement current = fpContext.getSecurableSoapMessage().findSecurityHeader().getFirstChildElement();
        MessagePolicy policy = null;
        while (current != null) {
            fpContext.getSecurableSoapMessage().findSecurityHeader().setCurrentHeaderElement(current);
            pProcessOnce(fpContext,current, false);
            if (!mConfiguration.isEmpty())
                try {
                    MessagePolicy mp =  (MessagePolicy) fpContext.getSecurityPolicy();
                    if(!mp.isEmpty()){
                        redux(mp,mConfiguration,fpContext.getSecurableSoapMessage(),
                                false);
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0256_FAILED_CONFIGURE_ASC(), e);
                    throw new XWSSecurityException(e);
                }
            policy = resolveMP(fpContext,configuration);
            if (policy != null) {
                if (!mConfiguration.contains(policy)) {
                    // log
                    StringBuffer buf = null;
                    if ( PolicyTypeUtil.messagePolicy(policy)) {
                        for ( int it=0; it<policy.size(); it++) {
                            if ( buf == null)
                                buf = new StringBuffer();
                            try {
                                buf.append(policy.get(it).getType() + " ");
                            } catch (Exception e) {
                                //ignore
                            }
                        }
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0261_INVALID_MESSAGE_POLICYSET());
                        throw new XWSSecurityException("Message does not conform to configured policy : [ " +
                                buf.toString() + "] policy set is not present in Receiver requirements.");
                    } else {
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0262_INVALID_MESSAGE_POLICYTYPE());
                        throw new XWSSecurityException("Message does not conform to configured policy : " +
                                policy.getType() + " is not present in Receiver requirements.");
                    }
                } else {
                    MessagePolicy policyCopy = new MessagePolicy();
                    int size = ((MessagePolicy) fpContext.getSecurityPolicy()).size();
                    int ppCount = 0;
                    for(int i=0;i<policy.size();i++){
                        try {
                            WSSPolicy wp =(WSSPolicy) policy.get(i);
                            if(PolicyTypeUtil.isSecondaryPolicy(wp)){
                                if(log.isLoggable(Level.FINEST)){
                                    log.log(Level.FINEST, wp.getType());
                                }
                                policyCopy.append(wp);
                            }else{
                                if(ppCount >= size){
                                    if(log.isLoggable(Level.FINEST)){
                                        log.log(Level.FINEST, wp.getType());
                                    }
                                    policyCopy.append(wp);
                                }else{
                                    if(log.isLoggable(Level.FINEST)){
                                        log.log(Level.FINEST, "skipped"+wp.getType());
                                    }
                                }
                                ppCount++;
                            }
                        } catch (Exception e) {
                            log.log(Level.SEVERE,LogStringsMessages.WSS_0257_FAILEDTO_APPEND_SECURITY_POLICY_MESSAGE_POLICY(), e);
                            throw new XWSSecurityException(e);
                        }
                    }
                    fpContext.setMode(FilterProcessingContext.ADHOC);
                    fpContext.setSecurityPolicy(policyCopy);
                    current = HarnessUtil.getNextElement(current);
                    if (policy.dumpMessages()){
                        DumpFilter.process(fpContext);
                    }
                    processMessagePolicy(fpContext,current);
                    break;
                }
            }
            
            current = HarnessUtil.getNextElement(current);
        }
        checkPolicyEquivalence(policy, mConfiguration);
    }
    
    /*
     * @param context FilterProcessingContext
     *
     * @return policy MessagePolicy
     *
     * @throws XWSSecurityException
     */
    private static MessagePolicy resolveMP(FilterProcessingContext fpContext,
            ApplicationSecurityConfiguration configuration )
            throws XWSSecurityException {
        
        String identifier = HarnessUtil.resolvePolicyIdentifier(fpContext.getSOAPMessage());
        
        if (identifier == null)
            return null;
        
        StaticPolicyContext context = fpContext.getPolicyContext();
        
        //if (fpContext.isJAXRPCIntegration ()) {
        // ILs are expected to turn on the FPContext flag
        ((StaticApplicationContext) context).setOperationIdentifier(identifier);
        //} else {}
        
        SecurityPolicy policy = configuration.getSecurityConfiguration((StaticApplicationContext)context);
        
        MessagePolicy mPolicy = null;
        
        if (PolicyTypeUtil.dynamicSecurityPolicy(policy)) {
            
            // create dynamic callback context
            DynamicApplicationContext dynamicContext = new DynamicApplicationContext(context);
            dynamicContext.setMessageIdentifier(fpContext.getMessageIdentifier());
            dynamicContext.inBoundMessage(true);
            ProcessingContext.copy(dynamicContext.getRuntimeProperties(), fpContext.getExtraneousProperties());
            
            // make dynamic policy callback
            DynamicPolicyCallback dpCallback = new DynamicPolicyCallback(policy, dynamicContext);
            HarnessUtil.makeDynamicPolicyCallback(
                    dpCallback, fpContext.getSecurityEnvironment().getCallbackHandler());
            
            if (!(PolicyTypeUtil.messagePolicy(dpCallback.getSecurityPolicy()))) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0271_FAILEDTO_RESOLVE_POLICY());
                throw new XWSSecurityException("Policy has to resolve to MessagePolicy");
            } else {
                mPolicy = (MessagePolicy) dpCallback.getSecurityPolicy();
            }
            
        } else if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
            
            DeclarativeSecurityConfiguration dsc = (DeclarativeSecurityConfiguration) policy;
            mPolicy = dsc.receiverSettings();
        }
        
        return mPolicy;
    }
    
    /*
     * @param mPolicy MessagePolicy
     * @param configuration Collection
     * @param message SecurableSoapMessage
     *
     * @throws XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    private static void redux(
            MessagePolicy mPolicy,
            Collection configuration,
            SecurableSoapMessage message,
            boolean isSecondary)
            throws Exception {
        
        /**
         * Re-visit: Handle BooleanComposer
         */
        
        //TODO: secondary policies don't follow order
        WSSPolicy policy = null;
        
        int _spSize = mPolicy.getSecondaryPolicies().size()-1;
        if(isSecondary && _spSize >= 0){
            policy = (WSSPolicy) mPolicy.getSecondaryPolicies().get(_spSize);
        }else {
            int _pSize = mPolicy.getPrimaryPolicies().size()-1;
            if(_pSize >=0){
                policy =(WSSPolicy) mPolicy.getPrimaryPolicies().get(_pSize);
            }
        }
        if(policy == null){
            return;
        }
        ArrayList reduxx = new ArrayList();
        
        Iterator i = configuration.iterator();
        while (i.hasNext()) {
            try {
                MessagePolicy policyx = (MessagePolicy) i.next();
                int spSize = mPolicy.getSecondaryPolicies().size()-1;
                ArrayList policyxList = policyx.getPrimaryPolicies();
                
                WSSPolicy wssPolicyx  = null;
                if(isSecondary && spSize >= 0){
                    wssPolicyx = (WSSPolicy) policyx.get(spSize);
                }else {
                    int pSize = mPolicy.getPrimaryPolicies().size()-1;
                    if(pSize >=0 && pSize < policyxList.size()){
                        wssPolicyx =(WSSPolicy) policyxList.get(pSize);
                    }else{
                        continue;
                    }
                }
                if (wssPolicyx != null){
                    
                    if(!policy.equalsIgnoreTargets(wssPolicyx)){
                        reduxx.add(policyx);
                    }
                }
                
            } catch (ClassCastException cce) {
                // ignore DynamicSecurityPolicies
                cce.printStackTrace();
                //log
                //throw new RuntimeException(cce);
            }
        }
        
        Iterator j = configuration.iterator();
        while (j.hasNext()) {
            try {
                
                int spSize = mPolicy.getSecondaryPolicies().size()-1;
                MessagePolicy policyy = ((MessagePolicy) j.next());
                ArrayList policyyList = policyy.getPrimaryPolicies();
                
                WSSPolicy wssPolicyy  = null;
                if(isSecondary && spSize >= 0){
                    wssPolicyy = (WSSPolicy) policyy.get(spSize);
                }else {
                    int pSize = mPolicy.getPrimaryPolicies().size()-1;
                    if(pSize >=0 && pSize < policyyList.size()){
                        wssPolicyy =(WSSPolicy) policyyList.get(pSize);
                    }else{
                        continue;
                    }
                }
                
                if (wssPolicyy != null){
                    if(!checkTargetBasedRequirements(policy, wssPolicyy, message)){
                        reduxx.add(policyy);
                    }
                }
            } catch (ClassCastException cce) {
                // ignore DynamicSecurityPolicies
                cce.printStackTrace();
                //log;
            }
        }
        
        
        configuration.removeAll(reduxx);
    }
    
    /*
     * @param policy WSSPolicy
     * @param message SecurableSoapMessage
     *
     * @return boolean if policy is applicable to message
     */
    private static boolean checkTargetBasedRequirements(WSSPolicy inferred,
            WSSPolicy configured,
            SecurableSoapMessage message) {
        ArrayList inferredTargets   = null;
        ArrayList configuredTargets = null;
        if (PolicyTypeUtil.encryptionPolicy(configured) && !PolicyTypeUtil.encryptionPolicy(inferred)) {
            return false;
        }
        
        if (PolicyTypeUtil.signaturePolicy(configured) && !PolicyTypeUtil.signaturePolicy(inferred)) {
            return false;
        }
        
        if (PolicyTypeUtil.signaturePolicy(inferred) && PolicyTypeUtil.signaturePolicy(configured)) {
            return verifySignatureTargets(inferred, configured,message);
        } else if (PolicyTypeUtil.encryptionPolicy(inferred) && PolicyTypeUtil.encryptionPolicy(configured)) {
            return verifyEncryptionTargets(inferred, configured,message);
        }
        return false;
    }
    
    static boolean verifySignatureTargets(WSSPolicy inferred, WSSPolicy configured,  SecurableSoapMessage message) {
        ArrayList inferredTargets   = null;
        ArrayList configuredTargets = null;
        
        inferredTargets = ((SignaturePolicy.FeatureBinding) inferred.getFeatureBinding()).getTargetBindings();
        configuredTargets = ((SignaturePolicy.FeatureBinding) configured.getFeatureBinding()).getTargetBindings();
        
        ArrayList inferredNodeSet   = new ArrayList();
        ArrayList configuredNodeSet = new ArrayList();
        try {
            dereferenceTargets(inferredTargets, inferredNodeSet, message, false);
            dereferenceTargets(configuredTargets, configuredNodeSet, message,false);
        } catch (XWSSecurityException xwsse) {
            //xwsse.printStackTrace();
            // log here
            return false;
        }
        if(inferredNodeSet.size() != configuredNodeSet.size()){
            //throw XWSSecurityException
            return false;
        }
        
        for(int i=0; i< configuredNodeSet.size();i++){
            EncryptedData cn = (EncryptedData)configuredNodeSet.get(i);
            for(int j=0; j< inferredNodeSet.size();j++){
                EncryptedData ci = (EncryptedData)inferredNodeSet.get(j);
                boolean found = false;
                if(cn.isAttachmentData() && ci.isAttachmentData()){
                    found = cn.equals((AttachmentData)ci);
                }else if (cn.isElementData() && ci.isElementData()){
                    found = ((EncryptedElement)cn).equals((EncryptedElement)ci);
                }
                if(found){
                    inferredNodeSet.remove(j);
                    break;
                }
            }
        }
        if(inferredNodeSet.size() != 0){
            //throw XWSSecurityException
            return false;
        }
        return true;
    }
    
    static boolean verifyEncryptionTargets(WSSPolicy inferred, WSSPolicy configured,  SecurableSoapMessage message) {
        ArrayList inferredTargets   = null;
        ArrayList configuredTargets = null;
        inferredTargets = ((EncryptionPolicy.FeatureBinding) inferred.getFeatureBinding()).getTargetBindings();
        configuredTargets = ((EncryptionPolicy.FeatureBinding) configured.getFeatureBinding()).getTargetBindings();
        
        ArrayList inferredNodeSet   = new ArrayList();
        ArrayList configuredNodeSet = new ArrayList();
        try {
            dereferenceTargets(inferredTargets, inferredNodeSet, message, true);
            dereferenceTargets(configuredTargets, configuredNodeSet, message,false);
        } catch (XWSSecurityException xwsse) {
            return false;
        }
        
        
        if(inferredNodeSet.size() != configuredNodeSet.size()){
            //throw XWSSecurityException
            return false;
        }
        for(int i=0; i< configuredNodeSet.size();i++){
            EncryptedData cn = (EncryptedData)configuredNodeSet.get(i);
            for(int j=0; j< inferredNodeSet.size();j++){
                EncryptedData ci = (EncryptedData)inferredNodeSet.get(j);
                boolean found = false;
                if(cn.isAttachmentData() && ci.isAttachmentData()){
                    found = cn.equals((AttachmentData)ci);
                }else if (cn.isElementData() && ci.isElementData()){
                    found = ((EncryptedElement)cn).equals((EncryptedElement)ci);
                }
                if(found){
                    inferredNodeSet.remove(j);
                    break;
                }
            }
        }
        
        if(inferredNodeSet.size() != 0){
            //throw XWSSecurityException
            return false;
        }
        return true;
    }
    
    /*
     * @param targets ArrayList
     * @param nodeSet ArrayList
     *
     * @throws XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    private static void dereferenceTargets(ArrayList targets, ArrayList nodeSet, SecurableSoapMessage message,
            boolean inferred)
            throws XWSSecurityException {
        Iterator i = targets.iterator();
        while (i.hasNext()) {
            Target t = (Target) i.next();
            boolean mandatory = t.getEnforce();
            boolean contentOnly = t.getContentOnly();
            Object object = null;
            EncryptedData data = null;
            try{
                if(!t.isAttachment()){
                    Element el = null;
                    if(inferred && t instanceof EncryptionTarget){
                        el = ((EncryptionTarget)t).getElementData();
                        data = new EncryptedElement(el,contentOnly);
                        nodeSet.add(data);
                    }else{
                        object =  message.getMessageParts(t);
                        if (object instanceof Element){
                            data = new EncryptedElement((Element)object,contentOnly);
                            nodeSet.add(data);
                        }else if (object instanceof NodeList){
                            NodeList nl = (NodeList) object;
                            for (int j=0; j< nl.getLength(); j++){
                                data = new EncryptedElement((Element)nl.item(j),contentOnly);
                                nodeSet.add(data);
                            }
                        }else if(object instanceof Node){
                            data = new EncryptedElement((Element)object,contentOnly);
                            nodeSet.add(data);
                        }
                    }
                    
                }else{
                    if(!inferred){
                        AttachmentPart ap = (AttachmentPart)message.getMessageParts(t);
                        data = new AttachmentData(ap.getContentId(), contentOnly);
                    }else{
                        data = new AttachmentData(t.getValue(),contentOnly);
                    }
                    nodeSet.add(data);
                }
            }catch(XWSSecurityException ex){
                if(!inferred && mandatory){
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0272_FAILEDTO_DEREFER_TARGETS());
                    throw ex;
                }
                continue;
            }
            
            /*System.out.println("Object"+object);
            if (object instanceof NodeList) {
                NodeList nl = (NodeList) object;
                for (int j=0; j< nl.getLength(); j++){
                    System.out.println("NODE "+nl.item(j));
                    nodeSet.add(nl.item(j));
                }
            } else {
                nodeSet.add(object);
            }
              }*/
        }
    }
    
    /*
     * @param policy MessagePolicy
     * @param configuration Collection
     *
     * @throws XWSSecurityException
     */
    private static void checkPolicyEquivalence(MessagePolicy policy, Collection configuration)
    throws XWSSecurityException {
        
        if (policy != null) {
            Iterator i = configuration.iterator();
            
            while (i.hasNext()) {
                MessagePolicy mPolicy = (MessagePolicy) i.next();
                
                if (policy == mPolicy){
                    return;
                }
            }
            log.log(Level.SEVERE, LogStringsMessages.WSS_0263_INVALID_MESSAGE_POLICY());
            throw new XWSSecurityException("Message does not conform to configured policy");
        }
    }
    
    /*
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private static void processMessagePolicy(FilterProcessingContext fpContext)
    throws XWSSecurityException {
        
        MessagePolicy policy = (MessagePolicy) fpContext.getSecurityPolicy();
        
        if (policy.dumpMessages()) {
            DumpFilter.process(fpContext);
        }
        
        if (policy.size() == 0) {
            fpContext.setMode(FilterProcessingContext.DEFAULT);
            pProcess(fpContext);
            return;
        }

        //TODO: hack till we fix this in PolicyTranslator
        //TO be removed before Plugfest
        try {
            if ((policy.size() == 1)  && 
                (PolicyTypeUtil.signatureConfirmationPolicy(policy.get(0)))) {
                fpContext.setMode(FilterProcessingContext.DEFAULT);
                pProcess(fpContext);
                return;
            }
        }catch (Exception e) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0273_FAILEDTO_PROCESS_POLICY(), e);
            throw new RuntimeException(e);
        }

        
        
        SecurityHeader header = fpContext.getSecurableSoapMessage().findSecurityHeader();
        if (header == null) {
            StringBuffer buf = new StringBuffer();
            for(int it=0; it<policy.size(); it++) {
                try {
                    buf.append(policy.get(it).getType() );
                    if ( PolicyTypeUtil.isPrimaryPolicy((WSSPolicy)policy.get(it))) {
                        buf.append("(P) ");
                    } else {
                        buf.append("(S) ");
                    }
                } catch (Exception ex) {
                    //ignore
                }
            }
            log.log(Level.SEVERE,LogStringsMessages.WSS_0253_INVALID_MESSAGE());
            throw new XWSSecurityException("Message does not conform to configured policy [ " + buf.toString()
            + "]:  No Security Header found");
        }
        SOAPElement current = header.getFirstChildElement();
        processMessagePolicy(fpContext,current);
    }
    
    private static void processMessagePolicy(FilterProcessingContext fpContext,
            SOAPElement current)throws XWSSecurityException {
        int idx = 0;
        MessagePolicy policy = (MessagePolicy) fpContext.getSecurityPolicy();
        SecurableSoapMessage secureMsg = fpContext.getSecurableSoapMessage();
        MessagePolicy secPolicy = null;
        ArrayList targets = null;
        StringBuffer buf = null;
        
        boolean foundPrimaryPolicy = false;
        while (idx < policy.size()) {
            
            WSSPolicy wssPolicy = null;
            try {
                wssPolicy = (WSSPolicy) policy.get(idx);
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0270_FAILEDTO_GET_SECURITY_POLICY_MESSAGE_POLICY());
                throw new XWSSecurityException(e);
            }
            
            
            if (PolicyTypeUtil.isPrimaryPolicy(wssPolicy)) {
                if (wssPolicy.getType().equals("EncryptionPolicy")){
                    targets = ((EncryptionPolicy.FeatureBinding)wssPolicy.getFeatureBinding()).getTargetBindings();
                }else{
                    targets = ((SignaturePolicy.FeatureBinding)wssPolicy.getFeatureBinding()).getTargetBindings();
                }                
                foundPrimaryPolicy = true;
                Iterator ite = targets.iterator();
                while(ite.hasNext()){
                    Target t = (Target)ite.next();
                    if (t.getEnforce()){                
                        
                        // roll the pointer down the header till a primary block is hit
                        // if end of header is hit (pointer is null) break out of the loop
                        while (current != null && HarnessUtil.isSecondaryHeaderElement(current))
                            current = HarnessUtil.getNextElement(current);

                        // if pointer is null (hit end of header), reset pointer to begining of header
                        if (current != null) {

                            secureMsg.findSecurityHeader().
                                    setCurrentHeaderElement(current);

                            fpContext.setSecurityPolicy(wssPolicy);
                            HarnessUtil.processDeep(fpContext);

                            boolean keepCurrent = false;
                            if(MessageConstants.ENCRYPTED_DATA_LNAME.equals(current.getLocalName())){
                                keepCurrent = true;
                            }

                            if (fpContext.isPrimaryPolicyViolation()) {
                                log.log(Level.SEVERE, LogStringsMessages.WSS_0265_ERROR_PRIMARY_POLICY());
                                throw new XWSSecurityException(fpContext.getPVE());
                            }

                            if (fpContext.isOptionalPolicyViolation()) {
                                // rollback current security header ptr.
                                // if secondary security header element
                                // is found, proceed to next header element
                                secureMsg.findSecurityHeader().setCurrentHeaderElement(current);
                            }

                            if(!keepCurrent){
                                current = secureMsg.findSecurityHeader().getCurrentHeaderBlockElement();
                            }else{
                                current = HarnessUtil.getNextElement(secureMsg.findSecurityHeader().getCurrentHeaderBlockElement());
                            }
                            //current = HarnessUtil.getNextElement(current);                            
                            break;
                        }else{
                            //log
                            if ( buf == null )
                                buf = new StringBuffer();
                            buf.append(wssPolicy.getType() + " ");
                            //throw new XWSSecurityException("More Receiver requirements specified"+
                            //        " than present in the message");
                        }
                    }else{
                        // roll the pointer down the header till a primary block is hit
                        // if end of header is hit (pointer is null) break out of the loop
                        while (current != null && HarnessUtil.isSecondaryHeaderElement(current))
                            current = HarnessUtil.getNextElement(current);

                        if ((current!=null && wssPolicy.getType().equals("EncryptionPolicy")) && current.getLocalName().equals("Signature")){
                            continue;
                        }
                        if ((current!=null && wssPolicy.getType().equals("SignaturePolicy")) && 
                                (current.getLocalName().equals(MessageConstants.ENCRYPTED_DATA_LNAME) ||
                                  current.getLocalName().equals(MessageConstants.XENC_ENCRYPTED_KEY_LNAME) ||
                                    current.getLocalName().equals(MessageConstants.XENC_REFERENCE_LIST_LNAME))){
                            continue;
                        }
                        
                        // if pointer is null (hit end of header), reset pointer to begining of header
                        if (current != null) {

                            secureMsg.findSecurityHeader().
                                    setCurrentHeaderElement(current);

                            fpContext.setSecurityPolicy(wssPolicy);
                            HarnessUtil.processDeep(fpContext);

                            boolean keepCurrent = false;
                            if(MessageConstants.ENCRYPTED_DATA_LNAME.equals(current.getLocalName())){
                                keepCurrent = true;
                            }

                            if (fpContext.isPrimaryPolicyViolation()) {
                                log.log(Level.SEVERE, LogStringsMessages.WSS_0265_ERROR_PRIMARY_POLICY());
                                throw new XWSSecurityException(fpContext.getPVE());
                            }

                            if (fpContext.isOptionalPolicyViolation()) {
                                // rollback current security header ptr.
                                // if secondary security header element
                                // is found, proceed to next header element
                                secureMsg.findSecurityHeader().setCurrentHeaderElement(current);
                            }

                            if(!keepCurrent){
                                current = secureMsg.findSecurityHeader().getCurrentHeaderBlockElement();
                            }else{
                                current = HarnessUtil.getNextElement(secureMsg.findSecurityHeader().getCurrentHeaderBlockElement());
                            }
                            //current = HarnessUtil.getNextElement(current);                            
                            break;
                        }
                    }
                }
            }else{
                if(secPolicy == null){
                    secPolicy = new MessagePolicy();
                }
                secPolicy.append(wssPolicy);
            }
            
            idx++;
        }
        
        if ( buf != null) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0258_INVALID_REQUIREMENTS());
            throw new XWSSecurityException("More Receiver requirements [ " + buf + " ] specified"+
                    " than present in the message");
        }
        
        if ( !foundPrimaryPolicy) {
            SecurityHeader header = secureMsg.findSecurityHeader();
            if ( header != null && header.getCurrentHeaderElement() == null) {
                header.setCurrentHeaderElement(header.getFirstChildElement());
            }
            checkForExtraSecurity(fpContext);
        }
        
        // now process Secondary policies
        idx = 0;
        SOAPElement securityHeader = secureMsg.findSecurityHeader();
        
        NodeList uList = securityHeader.getElementsByTagNameNS(MessageConstants.WSSE_NS, MessageConstants.USERNAME_TOKEN_LNAME);
        if(uList.getLength() >1){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0259_INVALID_SEC_USERNAME());
            throw  new XWSSecurityException("More than one wsse:UsernameToken element present in security header");
        }
        
        NodeList tList = securityHeader.getElementsByTagNameNS(MessageConstants.WSU_NS, MessageConstants.TIMESTAMP_LNAME);
        if(tList.getLength() >1){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0274_INVALID_SEC_TIMESTAMP());
            throw  new XWSSecurityException("More than one wsu:Timestamp element present in security header");
        }
        
        int unpCount = 0;
        int tspCount = 0;
        if(secPolicy != null){
            
            while (idx < secPolicy.size()) {
                WSSPolicy wssPolicy = null;
                try {
                    wssPolicy = (WSSPolicy) secPolicy.get(idx);
                } catch (Exception e) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0270_FAILEDTO_GET_SECURITY_POLICY_MESSAGE_POLICY());
                    throw new XWSSecurityException(e);
                }
                if(PolicyTypeUtil.authenticationTokenPolicy(wssPolicy)){
                    AuthenticationTokenPolicy atp =(AuthenticationTokenPolicy)wssPolicy;
                    WSSPolicy fb = (WSSPolicy)atp.getFeatureBinding();
                    if(PolicyTypeUtil.usernameTokenPolicy(fb)){
                        if(uList.getLength() == 0){
                            log.log(Level.SEVERE, LogStringsMessages.WSS_0275_INVALID_POLICY_NO_USERNAME_SEC_HEADER());
                            throw new XWSSecurityException(
                                    "Message does not conform to configured policy: " +
                                    "wsse:UsernameToken element not found in security header");
                            
                        }
                        unpCount++;
                    } else if (PolicyTypeUtil.samlTokenPolicy(fb)) {
                        //TODO : there can be more than 1 SAML assertion in a message
                    }
                }else if(PolicyTypeUtil.timestampPolicy(wssPolicy)){
                    if(tList.getLength() == 0){
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0276_INVALID_POLICY_NO_TIMESTAMP_SEC_HEADER());
                        throw new XWSSecurityException(
                                "Message does not conform to configured policy: " +
                                "wsu:Timestamp element not found in security header");
                    }
                    tspCount++;
                }
                
                fpContext.setSecurityPolicy(wssPolicy);
                HarnessUtil.processDeep(fpContext);
                
                idx++;
            }
            
        }
        
        if(uList.getLength() > unpCount){
            log.log(Level.SEVERE, LogStringsMessages.WSS_0259_INVALID_SEC_USERNAME());
            throw  new XWSSecurityException("Message does not conform to configured policy: " +
                    "Additional wsse:UsernameToken element found in security header");
        }
        
        /* For BC reasons we might support an additional Timestamp in the message */
//        if(tList.getLength() > tspCount){
//            //TODO: localize the string
//            if(log.isLoggable(Level.WARNING)){
//            log.log(Level.WARNING, "WSS0274.invalid.SEC.Timestamp");
//            }
//            /*
//            throw new XWSSecurityException(
//            "Message does not conform to configured policy: " +
//            "Additional wsu:Timestamp element found in security header");
//             */
//        }
        
        fpContext.setSecurityPolicy(policy);
        return;
    }
    
    private static void checkForExtraSecurity(FilterProcessingContext context)
    throws XWSSecurityException {
        
        SecurityHeader header = context.getSecurableSoapMessage().findSecurityHeader();
        
        if (header == null || header.getCurrentHeaderElement() == null)
            return;
        
/*
        for (SOAPElement current = (SOAPElement) header.getCurrentHeaderElement().getNextSibling();
        current != null;
        current = (SOAPElement) current.getNextSibling()) {
 */
        
        for (Node nextNode = header.getCurrentHeaderElement().getNextSibling();
        nextNode != null;
        nextNode = nextNode.getNextSibling()) {
            if (nextNode instanceof SOAPElement) {
                SOAPElement current = (SOAPElement) nextNode;
                if (!HarnessUtil.isSecondaryHeaderElement(current)) {
                    //System.out.println("----------" +current.getLocalName());
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0277_INVALID_ADDTIONAL_SEC_MESSAGE_POLICY());
                    throw new XWSSecurityException(
                            "Message does not conform to configured policy (found " + current.getLocalName() +") : " +
                            "Additional security than required found");
                }
            }
        }
        
        // TODO: Revisit this
        // checkForExtraSecondarySecurity (context);
    }
    
    private static void checkForExtraSecondarySecurity(FilterProcessingContext context)
    throws XWSSecurityException {
        
        SecurityHeader header = context.getSecurableSoapMessage().findSecurityHeader();
        MessagePolicy policy  = (MessagePolicy) context.getSecurityPolicy();
        
        boolean _UT = false;
        boolean _TS = false;
        
        for (SOAPElement current = (SOAPElement) header.getFirstChildElement();
        current != null;
        current = (SOAPElement) current.getNextSibling()) {
            try {
                _UT = current.getLocalName().equals(MessageConstants.USERNAME_TOKEN_LNAME);
                _TS = current.getLocalName().equals(MessageConstants.TIMESTAMP_LNAME);
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0278_FAILEDTO_GET_LOCAL_NAME());
                throw new XWSSecurityRuntimeException(e);
            }
        }
        
        boolean throwFault = false;
        StringBuffer buf = null;
        
        if (!_UT)
            for (int i=0; i < policy.size(); i++)
                try {
                    if (PolicyTypeUtil.usernameTokenPolicy(policy.get(i))) {
                        if ( buf == null) {
                            buf = new StringBuffer();
                        }
                        buf.append(policy.get(i).getType() + " ");
                        throwFault = true;
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0279_FAILED_CHECK_SEC_SECURITY(), e);
                    throw new XWSSecurityRuntimeException(e);
                }
        
        if (!_TS)
            for (int j=0; j < policy.size(); j++)
                try {
                    if (PolicyTypeUtil.timestampPolicy(policy.get(j))) {
                        if ( buf == null) {
                            buf = new StringBuffer();
                        }
                        buf.append(policy.get(j).getType() + " ");
                        throwFault = true;
                    }
                } catch (Exception e) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0279_FAILED_CHECK_SEC_SECURITY(), e);
                    throw new XWSSecurityRuntimeException(e);
                }
        
        if (throwFault)
            log.log(Level.SEVERE,LogStringsMessages.WSS_0277_INVALID_ADDTIONAL_SEC_MESSAGE_POLICY());
            throw new XWSSecurityException("Message does not conform to configured policy: " +
                    "Additional security [ " + buf.toString() + "] than required found");
    }
    
    /*
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     * @param isSecondary boolean
     *
     * @return boolean
     *
     * @see pProcess
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private static boolean pProcessOnce(FilterProcessingContext fpContext, SOAPElement current, boolean isSecondary)
    throws XWSSecurityException {
        
        boolean processed = false;
        
        String elementName = current.getLocalName();

        if (isSecondary) {
            if (MessageConstants.USERNAME_TOKEN_LNAME.equals(elementName)) {
                AuthenticationTokenFilter.processUserNameToken(fpContext);
                processed = true;
                
            } else if (MessageConstants.TIMESTAMP_LNAME.equals(elementName)) {
                TimestampFilter.process(fpContext);
                processed = true;
                
            } else if(MessageConstants.SIGNATURE_CONFIRMATION_LNAME.equals(elementName)) {
               SignatureConfirmationFilter.process(fpContext);
               processed = true;
            } else if (MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME.equals(elementName)){
                //ignore
                
            } else if (MessageConstants.SAML_ASSERTION_LNAME.equals(elementName)){
                AuthenticationTokenFilter.processSamlToken(fpContext);
            } else if (MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME.equals(elementName)){
                //ignore
            } else if (MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME.equals(elementName)) {
                //TODO:we can put this into TokenCache ?.
                // Also store this Token into Extraneous Properties for use by RM
            }
        } else {
            if (MessageConstants.DS_SIGNATURE_LNAME.equals(elementName)) {
                SignatureFilter.process(fpContext);
                processed = true;
                
            } else if (MessageConstants.XENC_ENCRYPTED_KEY_LNAME.equals(elementName)) {
                Iterator iter = null;
                try{
                    //TODO: Try to keep a single SOAPFactory instance
                iter = current.getChildElements(
                    SOAPFactory.newInstance().createName(MessageConstants.XENC_REFERENCE_LIST_LNAME,
                    MessageConstants.XENC_PREFIX, MessageConstants.XENC_NS));
                }catch(Exception e){
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0360_ERROR_CREATING_RLHB(e));
                    throw new XWSSecurityException(e);
                }
                if(iter.hasNext()){
                    EncryptionFilter.process(fpContext);
                    processed = true;
                }
                
            } else if (MessageConstants.XENC_REFERENCE_LIST_LNAME.equals(elementName)) {
                EncryptionFilter.process(fpContext);
                processed = true;
                
            } else if (MessageConstants.ENCRYPTED_DATA_LNAME.equals(elementName)) {
                EncryptionFilter.process(fpContext);
                processed = true;
            }  else {
                if (!HarnessUtil.isSecondaryHeaderElement(current)) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0204_ILLEGAL_HEADER_BLOCK(elementName));
                    HarnessUtil.throwWssSoapFault("Unrecognized header block: " + elementName);
                }
            }
        }
        
        return processed;
    }
    
    /*
     * Validation of wsse:UsernameToken/wsu:Timestamp protected by
     * signature/encryption should follow post verification of
     * signature/encryption.
     *
     * A two-pass processing model is implemented, the first pass
     * verifies signature/encryption, while the second, the token/
     * timestamp.
     *
     * Note: Can be specification documented
     *
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private static void pProcess(FilterProcessingContext fpContext)
    throws XWSSecurityException {
        
        SecurityHeader header = fpContext.getSecurableSoapMessage().findSecurityHeader();
        
        if (header == null) {
            SecurityPolicy policy = fpContext.getSecurityPolicy();
            if (policy != null) {
                if (PolicyTypeUtil.messagePolicy(policy)) {
                    if (!((MessagePolicy)policy).isEmpty()) {
                        log.log(Level.SEVERE, LogStringsMessages.WSS_0253_INVALID_MESSAGE());
                        throw new XWSSecurityException(
                                "Message does not conform to configured policy: " +
                                "No Security Header found in incoming message");
                        
                    }
                } else {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0253_INVALID_MESSAGE());
                    throw new XWSSecurityException(
                            "Message does not conform to configured policy: " +
                            "No Security Header found in incoming message");
                }
            }
            
            return;
        }
        
        SOAPElement current = header.getCurrentHeaderBlockElement();
        SOAPElement first = current;
        SOAPElement prev = null;
        while (current != null) {
            
            pProcessOnce(fpContext, current, false);
            if (fpContext.getMode() == FilterProcessingContext.DEFAULT && 
                    "EncryptedData".equals(current.getLocalName()) &&
                     (prev != null)) {
                header.setCurrentHeaderElement(prev);

            } else {
                prev = current;
            }
            current = header.getCurrentHeaderBlockElement();
        }
        
        current = first;
        header.setCurrentHeaderElement(current);
        
        while (current != null) {
            pProcessOnce(fpContext, current, true);
            current = header.getCurrentHeaderBlockElement();
        }
        
    }
    
    /*
     * @param context Processing Context
     */
    public static void handleFault(ProcessingContext context) {
        /**
         * TODO
         */
    }
    
    
    //COPIED FROM DECRYPTION PROCESSOR NOW
    //COMBINE ALL LATER.
    private static interface EncryptedData{
        public boolean isElementData();
        public boolean isAttachmentData();
    }
    
    private static class AttachmentData implements EncryptedData {
        private String cid = null;
        private boolean contentOnly = false;
        public AttachmentData(String cid , boolean co){
            this.cid = cid;
            contentOnly = co;
        }
        public String getCID(){
            return cid;
        }
        public boolean isContentOnly(){
            return contentOnly;
        }
        
        public boolean equals(AttachmentData data){
            if(cid != null && cid.equals(data.getCID()) &&
                    (contentOnly == data.isContentOnly())){
                return true;
            }
            return false;
        }
        
        public boolean isElementData(){
            return false;
        }
        
        public boolean isAttachmentData(){
            return true;
        }
    }
    
    private static class EncryptedElement implements EncryptedData {
        private Element element;
        private boolean contentOnly;
        private EncryptionPolicy policy = null;
        
        public EncryptedElement(Element element, boolean contentOnly) {
            this.element = element;
            this.contentOnly = contentOnly;
        }
        
        public Element getElement() {
            return element;
        }
        
        public boolean getContentOnly() {
            return contentOnly;
        }
        
        public boolean equals(EncryptedElement element) {
            EncryptedElement encryptedElement = (EncryptedElement) element;
            return (encryptedElement.getElement()== this.element &&
                    encryptedElement.getContentOnly() == this.contentOnly);
            //&& this.policy.equals(encryptedElement.getPolicy()));
            
        }
        
        public void setpolicy(EncryptionPolicy policy){
            this.policy = policy;
        }
        
        public EncryptionPolicy getPolicy(){
            return policy;
        }
        
        public boolean isElementData(){
            return true;
        }
        
        public boolean isAttachmentData(){
            return false;
        }
    }
}
