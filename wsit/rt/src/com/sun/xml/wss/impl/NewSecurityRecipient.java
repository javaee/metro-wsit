/*
 * $Id: NewSecurityRecipient.java,v 1.5 2010-08-11 06:30:25 kumarjayanti Exp $
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

import com.sun.xml.wss.impl.policy.verifier.MessagePolicyVerifier;
import com.sun.xml.wss.impl.policy.verifier.TargetResolver;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;

import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.impl.filter.DumpFilter;
import com.sun.xml.wss.impl.filter.TimestampFilter;
import com.sun.xml.wss.impl.filter.SignatureFilter;
import com.sun.xml.wss.impl.filter.EncryptionFilter;
import com.sun.xml.wss.impl.filter.SignatureConfirmationFilter;
import com.sun.xml.wss.impl.filter.AuthenticationTokenFilter;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.*;
import com.sun.xml.wss.impl.policy.PolicyAlternatives;
import com.sun.xml.wss.impl.policy.SecurityPolicy;

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
            log.log(Level.SEVERE, "WSS0397.soap.factory.exception", ex);
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
    @SuppressWarnings("static-access")
    public static void validateMessage(ProcessingContext context)
    throws XWSSecurityException {
        
        HarnessUtil.validateContext(context);
        FilterProcessingContext fpContext = new FilterProcessingContext(context);
        fpContext.isInboundMessage(true);
        SecurityPolicy pol = fpContext.getSecurityPolicy();
        MessagePolicy msgPolicy = null;
        List<MessagePolicy> messagePolicies = null;

        //we have to retain this stuff for old Metro 2.0 style backward compatibility
        if (pol instanceof MessagePolicy) {
            msgPolicy = (MessagePolicy) pol;
        } else if (pol instanceof PolicyAlternatives) {
            messagePolicies = ((PolicyAlternatives) pol).getSecurityPolicy();
        }

        if ((msgPolicy != null) && (msgPolicy.dumpMessages())) {
            DumpFilter.process(fpContext);
        }
        fpContext.setSecurityPolicyVersion( ((ProcessingContextImpl)context).getSecurityPolicyVersion());
        //unconditionally set these since the policy is unknown
        fpContext.setExtraneousProperty("EnableWSS11PolicyReceiver","true");
        List scList = new ArrayList();
        fpContext.setExtraneousProperty("receivedSignValues", scList);
        fpContext.setMode(FilterProcessingContext.WSDL_POLICY);

        pProcess(fpContext);
        
        if((msgPolicy == null || msgPolicy.size() <= 0) && (messagePolicies == null && messagePolicies.size() <=0) ){
            PolicyResolver opResolver = 
                    (PolicyResolver)fpContext.getExtraneousProperty(fpContext.OPERATION_RESOLVER);
            if(opResolver != null){
                pol = opResolver.resolvePolicy(fpContext);
            }
        }
        //we have to retain this stuff for old Metro 2.0 style backward compatibility
        if (pol instanceof MessagePolicy) {
            msgPolicy = (MessagePolicy) pol;
        } else if (pol instanceof PolicyAlternatives) {
            messagePolicies = ((PolicyAlternatives) pol).getSecurityPolicy();
            //temporary workaround for this legacy code
            msgPolicy = (messagePolicies != null) ? messagePolicies.get(0) : null;
        }


        //TODO: this is a workaround for PROTOCOL Message
        try {
            if (msgPolicy == null ||
                    (msgPolicy.size() == 0 && fpContext.getSOAPMessage().getSOAPBody().hasFault())) {
                
                fpContext.getSecurableSoapMessage().deleteSecurityHeader();
                fpContext.getSOAPMessage().saveChanges();
                return;
            }
        }catch (Exception ex) {
            log.log(Level.SEVERE, "WSS0370.error.deleting.secheader", ex);
            throw new XWSSecurityException(ex);
        }

        // for Policy verfication
        TargetResolver targetResolver = new TargetResolverImpl(context);
        MessagePolicyVerifier mpv = new MessagePolicyVerifier(context, targetResolver);   
        /*
        try{
            System.out.println("Inferred Security Policy");
            mpv.printInferredSecurityPolicy(fpContext.getInferredSecurityPolicy());
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        System.out.println("==================================");
       
        
        try{
            System.out.println("Actual SecurityPolicy");
            mpv.printInferredSecurityPolicy(msgPolicy);
        } catch(Exception e){
            throw new XWSSecurityException(e);
        }
        */

        //if(!isTrust){
            mpv.verifyPolicy(fpContext.getInferredSecurityPolicy(), msgPolicy);
        //}
        

        try {
            fpContext.getSecurableSoapMessage().deleteSecurityHeader();
            fpContext.getSOAPMessage().saveChanges();
        }catch (Exception ex) {
            log.log(Level.SEVERE, "WSS0370.error.deleting.secheader", ex);
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
                    log.log(Level.SEVERE, "WSS0252.failedto.getChildElement", e);
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
                        log.log(Level.SEVERE, "WSS0253.invalid.Message");
                        throw new XWSSecurityException(
                                "Message does not conform to configured policy: " +
                                "No Security Header found in incoming message");
                        
                    }
                } else {
                    log.log(Level.SEVERE, "WSS0253.invalid.Message");
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
    
     
    /*
     * @param context Processing Context
     */
    public static void handleFault(ProcessingContext context) {
    }
    
}
