/*
 * $Id: NewSecurityRecipient.java,v 1.1 2006-05-03 22:57:36 arungupta Exp $
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

package com.sun.xml.wss.impl;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.impl.dsig.AttachmentData;
import com.sun.xml.wss.impl.policy.mls.EncryptionTarget;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;

import com.sun.xml.wss.impl.policy.SecurityPolicy;

import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.filter.TimestampFilter;
import com.sun.xml.wss.impl.filter.SignatureFilter;
import com.sun.xml.wss.impl.filter.EncryptionFilter;
import com.sun.xml.wss.impl.filter.SignatureConfirmationFilter;
import com.sun.xml.wss.impl.filter.AuthenticationTokenFilter;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SecureConversationTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.impl.policy.mls.DerivedTokenKeyBinding;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.config.DeclarativeSecurityConfiguration;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy.UsernameTokenBinding;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.MLSPolicy;

import org.w3c.dom.Document;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.*;
import com.sun.xml.wss.impl.policy.mls.Target;

/**
 * This class exports a static Security Service for Verifying/Validating Security in an Inbound SOAPMessage.
 * The policy to be applied for Verifying the Message and the SOAPMessage itself are
 * supplied in an instance of a com.sun.xml.wss.ProcessingContext
 * @see ProcessingContext
 */
public class NewSecurityRecipient {
    
    private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    private static SOAPFactory sFactory = null;

    static {
        try { 
            sFactory = SOAPFactory.newInstance();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Validate security in an Inbound SOAPMessage.
     * <P>
     * Calling code should create com.sun.xml.wss.ProcessingContext object with
     * runtime properties. Specifically, it should set SecurityPolicy, application
     * CallbackHandler Or a SecurityEnvironment 
     * The SecurityPolicy instance can be of the following types:
     * <UL>
     *  <LI> A MessagePolicy
     * </UL>
     *
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
        FilterProcessingContext fpContext = new FilterProcessingContext(context);
        fpContext.isInboundMessage(true);

        MessagePolicy msgPolicy = (MessagePolicy) fpContext.getSecurityPolicy();
  
        if ((msgPolicy != null) && (msgPolicy.size() > 0)) {

            if(msgPolicy.enableWSS11Policy()){
                // set a property in context to determine if its WSS11
                fpContext.setExtraneousProperty("EnableWSS11PolicyReceiver","true");
            }

            if (msgPolicy.enableSignatureConfirmation()) {
                //For SignatureConfirmation
                //Set a list in extraneous property which will store all the received SignatureValues
                //If there was no Signature in incoming message this list will be empty
                List scList = new ArrayList();
                fpContext.setExtraneousProperty("receivedSignValues", scList);
            }        

            fpContext.setMode(FilterProcessingContext.ADHOC);
            processMessagePolicy(fpContext);
            checkForExtraSecurity(fpContext);
        } else {
            //unconditionally set these since the policy is unknown
            fpContext.setExtraneousProperty("EnableWSS11PolicyReceiver","true");
            List scList = new ArrayList();
            fpContext.setExtraneousProperty("receivedSignValues", scList);
            fpContext.setMode(FilterProcessingContext.WSDL_POLICY);
            pProcess(fpContext);
        }
        
        try {
            fpContext.getSecurableSoapMessage().deleteSecurityHeader();
            fpContext.getSOAPMessage().saveChanges();
        }catch (Exception ex) {
            throw new XWSSecurityException(ex);
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
        
        /* We insert SignatureConfirmation into the MessagePolicy */
        try {
            if ((policy.size() == 1)  && 
                (PolicyTypeUtil.signatureConfirmationPolicy(policy.get(0)))) {
                fpContext.setMode(FilterProcessingContext.WSDL_POLICY);
                pProcess(fpContext);
                return;
            }
        }catch (Exception e) {
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
        StringBuffer buf = null;
        boolean foundPrimaryPolicy = false;

        while (idx < policy.size()) {
            
            WSSPolicy wssPolicy = null;
            try {
                wssPolicy = (WSSPolicy) policy.get(idx);
            } catch (Exception e) {
                throw new XWSSecurityException(e);
            }
            
            
            if (PolicyTypeUtil.isPrimaryPolicy(wssPolicy)) {
                foundPrimaryPolicy = true;
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
                    
                    if (fpContext.isPrimaryPolicyViolation()) {
                        // log
                        throw new XWSSecurityException(fpContext.getPVE());
                    }
                    
                    if (fpContext.isOptionalPolicyViolation()) {
                        // rollback current security header ptr.
                        // if secondary security header element
                        // is found, proceed to next header element
                        secureMsg.findSecurityHeader().setCurrentHeaderElement(current);
                    }
                    
                    current = HarnessUtil.getNextElement(current);
                }else{
                    //log
                    if ( buf == null)
                        buf = new StringBuffer();
                    buf.append(wssPolicy.getType() + " ");
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
        
        NodeList uList = securityHeader.getElementsByTagNameNS(
            MessageConstants.WSSE_NS, MessageConstants.USERNAME_TOKEN_LNAME);
        if(uList.getLength() >1){
            throw  new XWSSecurityException("More than one wsse:UsernameToken element present in security header");
        }
        
        NodeList tList = securityHeader.getElementsByTagNameNS(MessageConstants.WSU_NS, MessageConstants.TIMESTAMP_LNAME);
        if(tList.getLength() >1){
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
                    throw new XWSSecurityException(e);
                }
                if(PolicyTypeUtil.authenticationTokenPolicy(wssPolicy)){
                    AuthenticationTokenPolicy atp =(AuthenticationTokenPolicy)wssPolicy;
                    WSSPolicy fb = (WSSPolicy)atp.getFeatureBinding();
                    if(PolicyTypeUtil.usernameTokenPolicy(fb)){
                        if(uList.getLength() == 0){
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
            throw  new XWSSecurityException("Message does not conform to configured policy: " +
                    "Additional wsse:UsernameToken element found in security header");
        }
        
        if(tList.getLength() > tspCount){
            //TODO: localize the string
            throw new XWSSecurityException(
            "Message does not conform to configured policy: " +
            "Additional wsu:Timestamp element found in security header");
        }
        
        fpContext.setSecurityPolicy(policy);
        return;
    }
    
    private static void checkForExtraSecurity(FilterProcessingContext context)
    throws XWSSecurityException {
        
        SecurityHeader header = context.getSecurableSoapMessage().findSecurityHeader();
        
        if (header == null || header.getCurrentHeaderElement() == null)
            return;
        
        for (Node nextNode = header.getCurrentHeaderElement().getNextSibling();
        nextNode != null;
        nextNode = nextNode.getNextSibling()) {
            if (nextNode instanceof SOAPElement) {
                SOAPElement current = (SOAPElement) nextNode;
                if (!HarnessUtil.isSecondaryHeaderElement(current)) {
                    throw new XWSSecurityException(
                            "Message does not conform to configured policy (found " + current.getLocalName() +") : " +
                            "Additional security than required found");
                }
            }
        }
        
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
    private static void processCurrentHeader(
        FilterProcessingContext fpContext, SOAPElement current, boolean isSecondary) throws XWSSecurityException {
        
        String elementName = current.getLocalName();

        if (isSecondary) {
            if (MessageConstants.USERNAME_TOKEN_LNAME.equals(elementName)) {
                AuthenticationTokenFilter.processUserNameToken(fpContext);
            } else if (MessageConstants.TIMESTAMP_LNAME.equals(elementName)) {
                TimestampFilter.process(fpContext);
            } else if(MessageConstants.SIGNATURE_CONFIRMATION_LNAME.equals(elementName)) {
               SignatureConfirmationFilter.process(fpContext);
            } else if (MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME.equals(elementName)){
                //ignore
            } else if (MessageConstants.SAML_ASSERTION_LNAME.equals(elementName)){
                AuthenticationTokenFilter.processSamlToken(fpContext);
            } else if (MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME.equals(elementName)){
                //ignore
            } else if (MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME.equals(elementName)) {
                // ignore
            }
        } else {
            if (MessageConstants.DS_SIGNATURE_LNAME.equals(elementName)) {
                SignatureFilter.process(fpContext);
            } else if (MessageConstants.XENC_ENCRYPTED_KEY_LNAME.equals(elementName)) {
                Iterator iter = null;
                try{
                iter = current.getChildElements(
                    sFactory.createName(MessageConstants.XENC_REFERENCE_LIST_LNAME,
                    MessageConstants.XENC_PREFIX, MessageConstants.XENC_NS));
                }catch(Exception e){
                    throw new XWSSecurityException(e);
                }
                if(iter.hasNext()){
                    EncryptionFilter.process(fpContext);
                }
                
            } else if (MessageConstants.XENC_REFERENCE_LIST_LNAME.equals(elementName)) {
                EncryptionFilter.process(fpContext);
                
            } else if (MessageConstants.ENCRYPTED_DATA_LNAME.equals(elementName)) {
                EncryptionFilter.process(fpContext);
            }  else {
                if (!HarnessUtil.isSecondaryHeaderElement(current)) {
                    log.log(Level.SEVERE, "WSS0204.illegal.header.block", elementName);
                    HarnessUtil.throwWssSoapFault("Unrecognized header block: " + elementName);
                }
            }
        }
        
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
        MessagePolicy policy = (MessagePolicy)fpContext.getSecurityPolicy();
        
        if (header == null) {
            if (policy != null) {
                if (PolicyTypeUtil.messagePolicy(policy)) {
                    if (!((MessagePolicy)policy).isEmpty()) {
                        //log
                        throw new XWSSecurityException(
                                "Message does not conform to configured policy: " +
                                "No Security Header found in incoming message");
                        
                    }
                } else {
                    //log
                    throw new XWSSecurityException(
                            "Message does not conform to configured policy: " +
                            "No Security Header found in incoming message");
                }
            }
            
            return;
        }
        
        if ((policy != null) && policy.dumpMessages()) {
            DumpFilter.process(fpContext);
        }
        SOAPElement current = header.getCurrentHeaderBlockElement();
        SOAPElement first = current;
        
        while (current != null) {
            processCurrentHeader(fpContext, current, false);
            current = header.getCurrentHeaderBlockElement();
        }
        
        current = first;
        header.setCurrentHeaderElement(current);
        
        while (current != null) {
            processCurrentHeader(fpContext, current, true);
            current = header.getCurrentHeaderBlockElement();
        }
        
        try{
            printInferredSecurityPolicy(fpContext);
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
    public static void printInferredSecurityPolicy(FilterProcessingContext fpContext) throws Exception{
        MessagePolicy inferredSecurityPolicy = fpContext.getInferredSecurityPolicy();
        StringBuffer buffer = new StringBuffer();
        if(inferredSecurityPolicy == null){
            buffer.append("Inferred Security Policy not set\n");
        } else{
            buffer.append("Size of inferredSecurityPolicy:: " + inferredSecurityPolicy.size() + "\n");
            for(int i = 0; i < inferredSecurityPolicy.size(); i++){
                WSSPolicy pol = (WSSPolicy)inferredSecurityPolicy.get(i);
                buffer.append(pol + "\n");
                if(PolicyTypeUtil.timestampPolicy(pol)){
                    buffer.append("Timestamp Policy\n");
                } else if(PolicyTypeUtil.usernameTokenPolicy(pol)){
                    buffer.append("UsernameToken Policy\n");                    
                } else if(PolicyTypeUtil.signaturePolicy(pol)){
                    buffer.append("Signature Policy\n");
                    SignaturePolicy sigPol = (SignaturePolicy)pol;
                    SignaturePolicy.FeatureBinding featureBinding = 
                            (SignaturePolicy.FeatureBinding)sigPol.getFeatureBinding();
                    ArrayList targets = featureBinding.getTargetBindings();
                    buffer.append("\tCanonicalizationAlgorithm" + featureBinding.getCanonicalizationAlgorithm() + "\n");
                    buffer.append("\t Targets\n");
                    for(int j = 0; j < targets.size(); j++){
                        SignatureTarget target = (SignatureTarget)targets.get(j);
                        buffer.append("\t " + j + ":Type:" + target.getType() + "\n");
                        buffer.append("\t  Value:" + target.getValue() + "\n");
                        buffer.append("\t  DigestAlgorithm:" + target.getDigestAlgorithm() + "\n");
                        ArrayList transforms = target.getTransforms();
                        
                        if(transforms != null){
                            buffer.append("\t  " + "Transforms::\n");
                            for(int k = 0; k < transforms.size(); k++){
                                buffer.append("\t " + "   " + ((SignatureTarget.Transform)transforms.get(k)).getTransform() + "\n");
                            }
                        }
                    }
                    MLSPolicy keyBinding = sigPol.getKeyBinding();
                    if(keyBinding != null){
                        buffer.append("\tKeyBinding\n");
                        printKeyBinding(keyBinding, buffer);
                    }
                } else if(PolicyTypeUtil.encryptionPolicy(pol)){
                    buffer.append("Encryption Policy\n");
                    EncryptionPolicy encPol = (EncryptionPolicy)pol;
                    EncryptionPolicy.FeatureBinding featureBinding = 
                            (EncryptionPolicy.FeatureBinding)encPol.getFeatureBinding();
                    ArrayList targets = featureBinding.getTargetBindings();
                    buffer.append("\t Targets\n");
                    for(int j = 0; j < targets.size(); j++){
                        EncryptionTarget target = (EncryptionTarget)targets.get(j);
                        buffer.append("\t " + j + ":"+ "Type:" + target.getType() + "\n");
                        buffer.append("\t  Value:" + target.getValue() + "\n");
                        buffer.append("\t  ContentOnly:" + target.getContentOnly() + "\n");
                        buffer.append("\t  DataEncryptionAlgorithm:" + target.getDataEncryptionAlgorithm() + "\n");
                    }
                    MLSPolicy keyBinding = encPol.getKeyBinding();
                    if(keyBinding != null){
                        buffer.append("\tKeyBinding\n");
                        printKeyBinding(keyBinding, buffer);
                    }                   
                } else if(PolicyTypeUtil.signatureConfirmationPolicy(pol)){
                    buffer.append("SignatureConfirmation Policy\n");
                }
            }
        }
        System.out.println(buffer.toString());
    }
    
    private static void printKeyBinding(MLSPolicy keyBinding, StringBuffer buffer){
        if(keyBinding != null){        
            if(keyBinding instanceof AuthenticationTokenPolicy.X509CertificateBinding){
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding =
                        (AuthenticationTokenPolicy.X509CertificateBinding)keyBinding;
                buffer.append("\t  X509CertificateBinding\n");
                buffer.append("\t    ValueType:" + x509Binding.getValueType() + "\n");
                buffer.append("\t    ReferenceType:" + x509Binding.getReferenceType() + "\n");
            } else if(keyBinding instanceof SymmetricKeyBinding){
                SymmetricKeyBinding skBinding = (SymmetricKeyBinding)keyBinding;
                buffer.append("\t  SymmetricKeyBinding\n");
                AuthenticationTokenPolicy.X509CertificateBinding x509Binding =
                        (AuthenticationTokenPolicy.X509CertificateBinding)skBinding.getKeyBinding();
                if(x509Binding != null){
                    buffer.append("\t     X509CertificateBinding\n");
                }
            } else if(keyBinding instanceof IssuedTokenKeyBinding){
                buffer.append("\t  IssuedTokenKeyBinding\n");
                            
            } else if(keyBinding instanceof SecureConversationTokenKeyBinding){
                buffer.append("\t  SecureConversationTokenKeyBinding\n");
                            
            }else if(keyBinding instanceof DerivedTokenKeyBinding){
                buffer.append("\t  DerivedTokenKeyBinding\n");
                DerivedTokenKeyBinding dtkBinding = (DerivedTokenKeyBinding)keyBinding;
                buffer.append("\t  OriginalKeyBinding:\n");
                printKeyBinding(dtkBinding.getOriginalKeyBinding(), buffer);
            }
        }        
    }
    
    /*
     * @param context Processing Context
     */
    public static void handleFault(ProcessingContext context) {
    }
    
}
