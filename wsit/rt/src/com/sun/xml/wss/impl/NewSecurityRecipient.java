/*
 * $Id: NewSecurityRecipient.java,v 1.1.2.1 2006-06-28 14:11:14 ashutoshshahi Exp $
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
import javax.xml.soap.SOAPException;

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
import com.sun.xml.ws.security.policy.WSSAssertion;
import com.sun.xml.wss.impl.ProcessingContextImpl;

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
import com.sun.xml.ws.security.policy.Token;

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
        if ((msgPolicy != null) && (msgPolicy.dumpMessages())) {
            DumpFilter.process(fpContext);
        }
        
        //unconditionally set these since the policy is unknown
        fpContext.setExtraneousProperty("EnableWSS11PolicyReceiver","true");
        List scList = new ArrayList();
        fpContext.setExtraneousProperty("receivedSignValues", scList);
        fpContext.setMode(FilterProcessingContext.WSDL_POLICY);

        pProcess(fpContext);

        boolean isTrust = fpContext.isTrustMessage();
        
        if(msgPolicy == null || msgPolicy.size() <= 0){
            OperationResolver opResolver = fpContext.getOperationResolver();
            if(opResolver != null && !isTrust)
                msgPolicy = opResolver.resolveOperationPolicy(fpContext.getSOAPMessage());
        }
        //TODO: this is a workaround for PROTOCOL Messages
        if (msgPolicy == null) {
            return;
        }
        
        /*
        try{
            System.out.println("Inferred Security Policy");
            printInferredSecurityPolicy(fpContext.getInferredSecurityPolicy());
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        System.out.println("==================================");
       
        
        try{
            System.out.println("Actual SecurityPolicy");
            printInferredSecurityPolicy(msgPolicy);
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        */

        WSSAssertion wssAssertion = ((ProcessingContextImpl)context).getWSSAssertion();
        if(!isTrust){
            verifyPolicy(fpContext.getInferredSecurityPolicy(), msgPolicy, 
                fpContext.getSecurableSoapMessage(), wssAssertion);
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
        
    }
    
    private static void verifyPolicy(MessagePolicy inferredSecurityPolicy, 
            MessagePolicy actualPolicy, SecurableSoapMessage soapMsg, 
            WSSAssertion wssAssertion) throws XWSSecurityException{
        try{
            Node firstChild = soapMsg.getSOAPBody().getFirstChild();
            if(firstChild != null){
                String uri = firstChild.getNamespaceURI();
                String localName = firstChild.getLocalName();
                if(localName.equals("Fault") && 
                        (uri.equals(MessageConstants.SOAP_1_1_NS) || uri.equals(MessageConstants.SOAP_1_2_NS)))
                    return;
            } 
        } catch(SOAPException se){
            log.log(Level.WARNING, "WSS0807.no.body.element");
        }
        if(actualPolicy == null || actualPolicy.size() <= 0){
            log.log(Level.SEVERE, "WSS0805.policy.null");
            throw new XWSSecurityException("ERROR: Policy for the service could not be obtained");
        } else if(inferredSecurityPolicy == null || inferredSecurityPolicy.size() <= 0){
            throw new XWSSecurityException("ERROR: No security header found in the message");
        } else{ // verify policy now
            try{
                for(int i = 0; i < actualPolicy.size(); i++) {
                    WSSPolicy actualPol = (WSSPolicy)actualPolicy.get(i);
                    if(PolicyTypeUtil.isSecondaryPolicy(actualPol)){
                        processSecondaryPolicy(actualPol, inferredSecurityPolicy);
                    } else if(PolicyTypeUtil.isPrimaryPolicy(actualPol)){
                        processPrimaryPolicy(actualPol, inferredSecurityPolicy, soapMsg, wssAssertion);
                    }
                }
                
            } catch(Exception e){
                throw new XWSSecurityException(e);
            }
        }
    }
    
    private static void processSecondaryPolicy(WSSPolicy actualPol,
            MessagePolicy inferredSecurityPolicy) throws XWSSecurityException{
        try{
            if(PolicyTypeUtil.timestampPolicy(actualPol)){
                boolean found = false;
                for(int j = 0; j < inferredSecurityPolicy.size(); j++) {
                    WSSPolicy pol = (WSSPolicy)inferredSecurityPolicy.get(j);
                    if(PolicyTypeUtil.timestampPolicy(pol)){
                        inferredSecurityPolicy.remove(pol);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    log.log(Level.WARNING, "Timestamp not found in configured policy but occurs in message");
                    // commenting for now, uncomment once this is corrected in SecurityPolicy
                    /*throw new XWSSecurityException("Policy Verification error:" 
                            + "Timestamp not found in configured policy but occurs in message");*/
                }
            } else if(PolicyTypeUtil.usernameTokenPolicy(actualPol)){
                boolean found = false;
                for(int j = 0; j < inferredSecurityPolicy.size(); j++) {
                    WSSPolicy pol = (WSSPolicy)inferredSecurityPolicy.get(j);
                    if(PolicyTypeUtil.usernameTokenPolicy(pol)){
                        inferredSecurityPolicy.remove(pol);
                        found = true;
                        break;
                    }
                }
                if(!found){
                    throw new XWSSecurityException("Policy Verification error:" 
                            + "UsernameToken not found in configured policy but occurs in message");
                }
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
    private static void processPrimaryPolicy(WSSPolicy actualPol,
            MessagePolicy inferredSecurityPolicy, 
            SecurableSoapMessage soapMsg, WSSAssertion wssAssertion) throws XWSSecurityException{
        try{
            if(PolicyTypeUtil.signaturePolicy(actualPol)){
                SignaturePolicy actualSignPolicy = (SignaturePolicy)actualPol;
                boolean isEndorsing = ((SignaturePolicy.FeatureBinding)actualSignPolicy.getFeatureBinding()).isEndorsingSignature();
                WSSPolicy pol = getFirstPrimaryPolicy(inferredSecurityPolicy, isEndorsing); 
                if(pol == null && checkTargets(actualPol, soapMsg))
                    throw new XWSSecurityException("Policy verification error:" +
                            "Missing signature");
                if(PolicyTypeUtil.signaturePolicy(pol)){
                    SignaturePolicy inferredPol = (SignaturePolicy)pol;
                    // verify key binding
                    boolean isKBTrue = verifyKeyBinding(actualSignPolicy.getKeyBinding(), inferredPol.getKeyBinding(),
                            wssAssertion);
                    // verify target binding
                    boolean isTBTrue = verifySignTargetBinding((SignaturePolicy.FeatureBinding)actualSignPolicy.getFeatureBinding(), 
                            (SignaturePolicy.FeatureBinding)inferredPol.getFeatureBinding());

                    inferredSecurityPolicy.remove(pol);
                    if(!isKBTrue){
                        log.log(Level.SEVERE, "WSS0206.policy.violation.exception");
                        throw new XWSSecurityException("Policy verification error:" +
                                "Incorrect Key types or references were used in signature");
                    }
                    if(!isTBTrue){
                        log.log(Level.SEVERE, "WSS0206.policy.violation.exception");
                        throw new XWSSecurityException("Policy verification error:" +
                                "One or more signed parts could not be verified");
                    }
                } else{
                    log.log(Level.SEVERE, "WSS0206.policy.violation.exception");
                    throw new XWSSecurityException("Policy verification error: Incorrect policy encountered");
                }
            } else if(PolicyTypeUtil.encryptionPolicy(actualPol)){
                EncryptionPolicy actualEncryptionPolicy = (EncryptionPolicy)actualPol;
                WSSPolicy pol = getFirstPrimaryPolicy(inferredSecurityPolicy, false); 
                if(pol == null && checkTargets(actualPol, soapMsg))
                    throw new XWSSecurityException("Policy verification error:" +
                            "Missing encryption element");
                if(PolicyTypeUtil.encryptionPolicy(pol)){
                   EncryptionPolicy inferredPol = (EncryptionPolicy)pol;
                   //verify key binding
                   boolean isKBTrue = verifyKeyBinding(actualEncryptionPolicy.getKeyBinding(), 
                           inferredPol.getKeyBinding(), wssAssertion);
                   // verify target binding
                   boolean isTBTrue = verifyEncTargetBinding((EncryptionPolicy.FeatureBinding)actualEncryptionPolicy.getFeatureBinding(), 
                           (EncryptionPolicy.FeatureBinding)inferredPol.getFeatureBinding());

                   inferredSecurityPolicy.remove(pol);
                   if(!isKBTrue){
                       log.log(Level.SEVERE, "WSS0206.policy.violation.exception");
                       throw new XWSSecurityException("Policy verification error" +
                               "Incorrect Key types or references were used in encryption");
                   }
                   if(!isTBTrue){
                       log.log(Level.SEVERE, "WSS0206.policy.violation.exception");
                       throw new XWSSecurityException("Policy verification error" +
                              "One or more encrypted parts could not be verified");
                   }
                } else{
                    log.log(Level.SEVERE, "WSS0206.policy.violation.exception");
                    throw new XWSSecurityException("Policy verification error: Incorrect policy encountered");
                }
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
    }
    
    private static boolean checkTargets(WSSPolicy actualPol, SecurableSoapMessage message){
        
        return false;
    }
    
    private static boolean verifyKeyBinding(MLSPolicy actualKeyBinding, 
            MLSPolicy inferredKeyBinding, WSSAssertion wssAssertion) throws XWSSecurityException{
        boolean verified = false;
        try{
            if(actualKeyBinding != null && inferredKeyBinding != null){
                if(PolicyTypeUtil.x509CertificateBinding(actualKeyBinding) &&
                        PolicyTypeUtil.x509CertificateBinding(inferredKeyBinding)){
                    /* TODO: cannot change actual policy, there seems to be a bug in
                     * security policy
                    AuthenticationTokenPolicy.X509CertificateBinding actualX509Bind = 
                            (AuthenticationTokenPolicy.X509CertificateBinding)actualKeyBinding;
                    AuthenticationTokenPolicy.X509CertificateBinding inferredX509Bind = 
                            (AuthenticationTokenPolicy.X509CertificateBinding)inferredKeyBinding;
                    // workaround - policy sets reference type as Thumprint
                    if(actualX509Bind.getReferenceType().equals(MessageConstants.THUMB_PRINT_TYPE))
                        actualX509Bind.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    correctIncludeTokenPolicy(actualX509Bind, wssAssertion);
                    if(actualX509Bind.getReferenceType().equals(inferredX509Bind.getReferenceType()))*/
                    verified =  true;
                } else if(PolicyTypeUtil.symmetricKeyBinding(actualKeyBinding) &&
                       PolicyTypeUtil.symmetricKeyBinding(inferredKeyBinding)){
                    verified = verifyKeyBinding(actualKeyBinding.getKeyBinding(), inferredKeyBinding.getKeyBinding(), 
                            wssAssertion);
                } else if(PolicyTypeUtil.issuedTokenKeyBinding(actualKeyBinding) &&
                        PolicyTypeUtil.issuedTokenKeyBinding(inferredKeyBinding)){
                    
                    verified = true;
                } else if(PolicyTypeUtil.secureConversationTokenKeyBinding(actualKeyBinding) &&
                        PolicyTypeUtil.secureConversationTokenKeyBinding(inferredKeyBinding)){
                    
                    verified = true;
                } else if(PolicyTypeUtil.derivedTokenKeyBinding(actualKeyBinding) &&
                       PolicyTypeUtil.derivedTokenKeyBinding(inferredKeyBinding)){
                    
                    verified = verifyKeyBinding(((DerivedTokenKeyBinding)actualKeyBinding).getOriginalKeyBinding(), 
                            ((DerivedTokenKeyBinding)inferredKeyBinding).getOriginalKeyBinding(), wssAssertion);
                }
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        return verified;
    }
    
    private static boolean verifySignTargetBinding(SignaturePolicy.FeatureBinding actualFeatureBinding,
            SignaturePolicy.FeatureBinding inferredFeatureBinding){
        
        return true;
    }
    
    private static boolean verifyEncTargetBinding(EncryptionPolicy.FeatureBinding actualFeatureBinding,
            EncryptionPolicy.FeatureBinding inferredFeatureBinding){
        
        return true;
    }
    
    private static WSSPolicy getFirstPrimaryPolicy(MessagePolicy securityPolicy, boolean isEndorsingSign) throws 
            XWSSecurityException{
        try{
            if(!isEndorsingSign){
                for(int i = 0; i < securityPolicy.size(); i++){
                    WSSPolicy pol = (WSSPolicy)securityPolicy.get(i);
                    if(PolicyTypeUtil.isPrimaryPolicy(pol)){
                        // accounts for encrypted SAML tokens issued by STS 
                        if(PolicyTypeUtil.encryptionPolicy(pol) && 
                               ((EncryptionPolicy.FeatureBinding)pol.getFeatureBinding()).encryptsIssuedToken() ){
                            continue;
                        } else
                            return pol;
                    }
                }
            } else{
                // endorsingSign policy is not placed correctly in actual policy
                for(int i = 0; i < securityPolicy.size(); i++){
                    WSSPolicy pol = (WSSPolicy)securityPolicy.get(i);
                    if(PolicyTypeUtil.isPrimaryPolicy(pol) && PolicyTypeUtil.signaturePolicy(pol)){
                        SignaturePolicy signPol = (SignaturePolicy)pol;
                        SignaturePolicy.FeatureBinding fb = (SignaturePolicy.FeatureBinding)signPol.getFeatureBinding();
                        if(fb.getTargetBindings().size() == 1){
                            SignatureTarget target = (SignatureTarget)fb.getTargetBindings().get(0);
                            if("{http://www.w3.org/2000/09/xmldsig#}Signature".equals(target.getValue()))
                                return pol;
                        }
                    }
                }
            }
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        return null;
    }
    
    private static void correctIncludeTokenPolicy(AuthenticationTokenPolicy.X509CertificateBinding x509Bind, 
            WSSAssertion wssAssertion){
        if(Token.INCLUDE_NEVER.equals(x509Bind.getIncludeToken())){
            if(MessageConstants.DIRECT_REFERENCE_TYPE.equals(x509Bind.getReferenceType())){
                if(wssAssertion != null){
                    if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUST_SUPPORT_REF_KEYIDENTIFIER))
                        x509Bind.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                    else if(wssAssertion.getRequiredProperties().contains(WSSAssertion.MUSTSUPPORT_REF_THUMBPRINT))
                        x509Bind.setReferenceType(MessageConstants.THUMB_PRINT_TYPE);                    
                } else {
                    // when wssAssertion is not set use KeyIdentifier
                    x509Bind.setReferenceType(MessageConstants.KEY_INDETIFIER_TYPE);
                }
            }
        } else if(Token.INCLUDE_ALWAYS_TO_RECIPIENT.equals(x509Bind.getIncludeToken()) || 
                Token.INCLUDE_ALWAYS.equals(x509Bind.getIncludeToken())){
            x509Bind.setReferenceType(MessageConstants.DIRECT_REFERENCE_TYPE);
        }
    }
    
    private static void printInferredSecurityPolicy(MessagePolicy inferredSecurityPolicy) throws Exception{
        StringBuffer buffer = new StringBuffer();
        if(inferredSecurityPolicy == null){
            buffer.append("Security Policy not set\n");
        } else{
            buffer.append("Size of Policy:: " + inferredSecurityPolicy.size() + "\n");
            for(int i = 0; i < inferredSecurityPolicy.size(); i++){
                WSSPolicy pol = (WSSPolicy)inferredSecurityPolicy.get(i);
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
                } else{
                    buffer.append(pol + "\n");
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
                    buffer.append("\t       ValueType:" + x509Binding.getValueType() + "\n");
                    buffer.append("\t       ReferenceType:" + x509Binding.getReferenceType() + "\n");
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