/*
 * $Id: HarnessUtil.java,v 1.5 2010-03-20 12:33:41 kumarjayanti Exp $
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

package com.sun.xml.wss.impl;

import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

import com.sun.xml.wss.impl.filter.TimestampFilter;
import com.sun.xml.wss.impl.filter.SignatureFilter;
import com.sun.xml.wss.impl.filter.EncryptionFilter;
import com.sun.xml.wss.impl.filter.AuthenticationTokenFilter;
import com.sun.xml.wss.impl.filter.SignatureConfirmationFilter;

import com.sun.xml.ws.api.message.Message;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.*;
import org.w3c.dom.NodeList;

public abstract class HarnessUtil {
    
    private static Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /**
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    @SuppressWarnings("unchecked")
    static void processWSSPolicy(FilterProcessingContext fpContext)
    throws XWSSecurityException {
        
        WSSPolicy policy = (WSSPolicy) fpContext.getSecurityPolicy();
        if (PolicyTypeUtil.signaturePolicy(policy)) {
            SignatureFilter.process(fpContext);
        } else if (PolicyTypeUtil.encryptionPolicy(policy)) {
            EncryptionFilter.process(fpContext);
        } else if (PolicyTypeUtil.timestampPolicy(policy)) {
            TimestampFilter.process(fpContext);
        } else if(PolicyTypeUtil.signatureConfirmationPolicy(policy)) {
            SignatureConfirmationFilter.process(fpContext);
        }
        else if (PolicyTypeUtil.authenticationTokenPolicy(policy)) {
            fpContext.getExtraneousProperties().put(TokenPolicyMetaData.TOKEN_POLICY, policy);
            WSSPolicy authPolicy = (WSSPolicy)policy.getFeatureBinding();
            if(PolicyTypeUtil.usernameTokenPolicy(authPolicy)) {
                AuthenticationTokenPolicy.UsernameTokenBinding utb=
                        (AuthenticationTokenPolicy.UsernameTokenBinding)authPolicy;
                if (!utb.isEndorsing()) {
                    AuthenticationTokenFilter.processUserNameToken(fpContext);
                }
            } else if (PolicyTypeUtil.samlTokenPolicy(authPolicy)) {                
                AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                    (AuthenticationTokenPolicy.SAMLAssertionBinding)authPolicy;
                try{
                    if (samlPolicy.getAssertionType() == 
                        AuthenticationTokenPolicy.SAMLAssertionBinding.SV_ASSERTION) {
                        AuthenticationTokenFilter.processSamlToken(fpContext);
                    }
                }catch(Exception ex){
                    //ignore it
                }
            }else if (PolicyTypeUtil.x509CertificateBinding(authPolicy)) {
                AuthenticationTokenFilter.processX509Token(fpContext);
            }else if (PolicyTypeUtil.issuedTokenKeyBinding(authPolicy)) {                
                AuthenticationTokenFilter.processIssuedToken(fpContext);
            }else if (PolicyTypeUtil.keyValueTokenBinding(authPolicy)) {                
                AuthenticationTokenFilter.processRSAToken(fpContext);
            }
        }else if(PolicyTypeUtil.isMandatoryTargetPolicy(policy)) {
            return;
        } else {
            log.log(Level.SEVERE, "WSS0801.illegal.securitypolicy");
            throw new XWSSecurityException("Invalid WSSPolicy Type");
        }
    }
    
    /**
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    /*static void processBooleanComposer(FilterProcessingContext fpContext)
    throws XWSSecurityException {
     
        BooleanComposer bComposer = (BooleanComposer) fpContext.getSecurityPolicy();
     
        int type = bComposer.getComposerType();
     
        SecurityPolicy policyA = (SecurityPolicy) bComposer.getPolicyA();
        SecurityPolicy policyB = (SecurityPolicy) bComposer.getPolicyB();
     
        if (type == BooleanComposer.OR) {
            fpContext.setSecurityPolicy(policyA);
            processDeep(fpContext);
     
            if (fpContext.isPrimaryPolicyViolation()) {
                fpContext.setSecurityPolicy(policyB);
                processDeep(fpContext);
     
                if (fpContext.isPrimaryPolicyViolation()) {
                    String message =
                            "Policy: " + policyA.getClass().getName() + " OR " +
                            "Policy: " + policyB.getClass().getName() +
                            " not satisfied";
                    log.log(Level.SEVERE,
                            "WSS0802.securitypolicy.notsatisfied",
                            new Object[] {message});
     
                            throw new XWSSecurityException(message, fpContext.getPVE());
                }
            }
        } else
            if (type == BooleanComposer.AND_STRICT) {
            fpContext.setSecurityPolicy(policyA);
            processDeep(fpContext);
     
            if (fpContext.isPrimaryPolicyViolation()) {
                String message =
                        "Policy: " + policyA.getClass().getName() + " AND_STRICT " +
                        "Policy: " + policyB.getClass().getName() +
                        " not satisfied";
                log.log(Level.SEVERE,
                        "WSS0802.securitypolicy.notsatisfied",
                        new Object[] {message});
     
                        throw new XWSSecurityException(message, fpContext.getPVE());
            }
     
            fpContext.setSecurityPolicy(policyB);
            processDeep(fpContext);
     
            if (fpContext.isPrimaryPolicyViolation()) {
                String message =
                        "Policy: " + policyA.getClass().getName() + " AND_STRICT " +
                        "Policy: " + policyB.getClass().getName() +
                        " not satisfied";
                log.log(Level.SEVERE,
                        "WSS0802.securitypolicy.notsatisfied",
                        new Object[] {message});
                        throw new XWSSecurityException(message, fpContext.getPVE());
            }
            } else
                if (type == BooleanComposer.AND_FLEXIBLE) {
            fpContext.setSecurityPolicy(policyA);
            processDeep(fpContext);
     
            if (fpContext.isPrimaryPolicyViolation()) {
                fpContext.setSecurityPolicy(policyB);
                processDeep(fpContext);
     
                if (fpContext.isPrimaryPolicyViolation()) {
                    String message =
                            "Policy: " + policyA.getClass().getName() +
                            " AND_FLEXIBLE " +
                            "Policy: " + policyB.getClass().getName() +
                            " not satisfied";
                    log.log(Level.SEVERE,
                            "WSS0802.securitypolicy.notsatisfied",
                            new Object[] {message});
                            throw new XWSSecurityException(
                                    message, fpContext.getPVE());
                }
     
                fpContext.setSecurityPolicy(policyA);
                processDeep(fpContext);
     
                if (fpContext.isPrimaryPolicyViolation()) {
                    String message =
                            "Policy: " + policyA.getClass().getName() +
                            " AND_FLEXIBLE " +
                            "Policy: " + policyB.getClass().getName() +
                            " not satisfied";
                    log.log(Level.SEVERE,
                            "WSS0802.securitypolicy.notsatisfied",
                            new Object[] {message});
     
                            throw new XWSSecurityException(
                                    message, fpContext.getPVE());
                }
     
                return;
            }
     
            fpContext.setSecurityPolicy(policyB);
            processDeep(fpContext);
     
            if (fpContext.isPrimaryPolicyViolation()) {
                String message =
                        "Policy: " + policyA.getClass().getName() +
                        " AND_FLEXIBLE " +
                        "Policy: " + policyB.getClass().getName() +
                        " not satisfied";
                log.log(Level.SEVERE,
                        "WSS0802.securitypolicy.notsatisfied",
                        new Object[] {message});
     
                        throw new XWSSecurityException(message, fpContext.getPVE());
            }
                }
    }*/
    
    /**
     * @param fpContext com.sun.xml.wss.FilterProcessingContext
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    static void processDeep(FilterProcessingContext fpContext)
    throws XWSSecurityException {
        /*if (PolicyTypeUtil.booleanComposerPolicy(fpContext.getSecurityPolicy())) {
            processBooleanComposer(fpContext);
        } else*/
        if (fpContext.getSecurityPolicy() instanceof WSSPolicy) {
            processWSSPolicy(fpContext);
        } else {
            log.log(Level.SEVERE, "WSS0801.illegal.securitypolicy");
            throw new XWSSecurityException("Invalid SecurityPolicy Type");
        }
    }
    
    /**
     * Checks for required context parameters
     *
     * @param context ProcessingContext
     *
     * @throws XWSSecurityException
     */
    static void validateContext(ProcessingContext context)
    throws XWSSecurityException {
        SOAPMessage message = null;
        Message jaxWsMessage = null;
        if(context instanceof JAXBFilterProcessingContext)
            jaxWsMessage = ((JAXBFilterProcessingContext)context).getJAXWSMessage();
        else
            message = context.getSOAPMessage();
        SecurityEnvironment handler = context.getSecurityEnvironment();
        SecurityPolicy policy = context.getSecurityPolicy();
        boolean isInboundMessage = context.isInboundMessage();
        StaticPolicyContext staticContext = context.getPolicyContext();
        
        if (message == null && jaxWsMessage == null) {
            log.log(Level.SEVERE, "WSS0803.soapmessage.notset");
            throw new XWSSecurityException(
                    "javax.xml.soap.SOAPMessage parameter not set in the ProcessingContext");
        }
        
        if (handler == null) {
            log.log(Level.SEVERE, "WSS0804.callback.handler.notset");
            throw new XWSSecurityException(
                    "SecurityEnvironment/javax.security.auth.callback.CallbackHandler implementation not set in the ProcessingContext");
        }
        
        if (policy == null && !isInboundMessage) {
            if(log.isLoggable(Level.WARNING)){
            log.log(Level.WARNING, "WSS0805.policy.null");
            }
        }
        
        if (staticContext == null) {
            // log.log(Level.WARNING, "WSS0806.static.context.null");
        }
    }
    
    /**
     * TODO: Handle doc-lit/non-wrap JAXRPC Mode
     * @param message SOAPMessage
     */
    static String resolvePolicyIdentifier(SOAPMessage message)
    throws XWSSecurityException {
        
        Node firstChild = null;
        SOAPBody body = null;
        
        try {
            body = message.getSOAPBody();
        } catch (SOAPException se) {
            log.log(Level.SEVERE, "WSS0807.no.body.element", se);
            throw new XWSSecurityException(se);
        }
        
        if (body != null) {
            firstChild = body.getFirstChild();
        } else {
            log.log(Level.SEVERE, "WSS0808.no.body.element.operation");
            throw new XWSSecurityException(
                    "No body element identifying an operation is found");
        }
        
        String id =
                firstChild != null ?
                    "{"+firstChild.getNamespaceURI()+"}"+firstChild.getLocalName() :
                    null;
        
        return id;
    }
    
    /*
     * @param current
     *
     * @return boolean
     */
    static boolean isSecondaryHeaderElement(SOAPElement element) {
        if ( element.getLocalName().equals(MessageConstants.ENCRYPTEDKEY_LNAME)) {
            NodeList nl = element.getElementsByTagNameNS(MessageConstants.XENC_NS, MessageConstants.XENC_REFERENCE_LIST_LNAME);
            if ( nl.getLength() == 0 ) {
                return true;
            }
        }
        return (element.getLocalName().equals(MessageConstants.WSSE_BINARY_SECURITY_TOKEN_LNAME) ||
                element.getLocalName().equals(MessageConstants.USERNAME_TOKEN_LNAME) ||
                element.getLocalName().equals(MessageConstants.SAML_ASSERTION_LNAME) ||
                element.getLocalName().equals(MessageConstants.TIMESTAMP_LNAME) ||
                element.getLocalName().equals(MessageConstants.SIGNATURE_CONFIRMATION_LNAME) ||
                element.getLocalName().equals(MessageConstants.WSSE_SECURITY_TOKEN_REFERENCE_LNAME) ||
                element.getLocalName().equals(MessageConstants.DERIVEDKEY_TOKEN_LNAME) ||
                element.getLocalName().equals(MessageConstants.SECURITY_CONTEXT_TOKEN_LNAME));
    }
    
    /*
     * @param current SOAPElement
     */
    static SOAPElement getNextElement(SOAPElement current) {
        if(current == null){
            return null;
        }
        Node node = current.getNextSibling();
        while ((null != node) && (node.getNodeType() != Node.ELEMENT_NODE))
            node = node.getNextSibling();
        return (SOAPElement) node;
    }
    
    /*
     * @param current SOAPElement
     */
    static SOAPElement getPreviousElement(SOAPElement current) {
        Node node = current.getPreviousSibling();
        while ((node !=null) && !(node.getNodeType() == Node.ELEMENT_NODE))
            node = node.getPreviousSibling();
        return (SOAPElement) node;
    }
    
    /*
     * @param message
     *
     * @throws com.sun.xml.wss.WssSoapFaultException
     */
    static void throwWssSoapFault(String message) throws WssSoapFaultException {
        XWSSecurityException xwsse = new XWSSecurityException(message);
        log.log(Level.SEVERE, "WSS0809.fault.WSSSOAP", xwsse);
        throw SecurableSoapMessage.newSOAPFaultException(
                MessageConstants.WSSE_INVALID_SECURITY,
                message,
                xwsse);
    }
    
   /*
    * make a DynamicPolicyCallback
    * @param callback the DynamicPolicyCallback object
    */
    public static void makeDynamicPolicyCallback(DynamicPolicyCallback callback,
            CallbackHandler callbackHandler)
            throws XWSSecurityException {
        
        if (callbackHandler == null)
            return;
        
        try {
            Callback[] callbacks = new Callback[] { callback };
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException ex) {
            //ok to ignore this since not all callback-handlers will implement this
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0237.failed.DynamicPolicyCallback", e);
            throw new XWSSecurityException(e);
        }
    }
    
}
