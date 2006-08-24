/*
 * $Id: HarnessUtil.java,v 1.3 2006-08-24 03:35:20 venu Exp $
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
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
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
import com.sun.xml.wss.impl.policy.mls.SignaturePolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
//import com.sun.xml.wss.impl.policy.mls.BooleanComposer;
import com.sun.xml.wss.impl.policy.mls.EncryptionPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;

import com.sun.xml.wss.impl.filter.TimestampFilter;
import com.sun.xml.wss.impl.filter.SignatureFilter;
import com.sun.xml.wss.impl.filter.EncryptionFilter;
import com.sun.xml.wss.impl.filter.AuthenticationTokenFilter;
import com.sun.xml.wss.impl.filter.SignatureConfirmationFilter;

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
        } else if (PolicyTypeUtil.authenticationTokenPolicy(policy)) {
            WSSPolicy authPolicy = (WSSPolicy)policy.getFeatureBinding();
            if(PolicyTypeUtil.usernameTokenPolicy(authPolicy)) {
                AuthenticationTokenFilter.processUserNameToken(fpContext);
            } else if (PolicyTypeUtil.samlTokenPolicy(authPolicy)) {
                AuthenticationTokenFilter.processSamlToken(fpContext);
            }else if (PolicyTypeUtil.x509CertificateBinding(authPolicy)) {
                AuthenticationTokenFilter.processX509Token(fpContext);
            }
        }else if(PolicyTypeUtil.isMandatoryTargetPolicy(policy)) {
            return;
        }else {
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
        
        SOAPMessage message = context.getSOAPMessage();
        SecurityEnvironment handler = context.getSecurityEnvironment();
        SecurityPolicy policy = context.getSecurityPolicy();
        boolean isInboundMessage = context.isInboundMessage();
        StaticPolicyContext staticContext = context.getPolicyContext();
        
        if (message == null) {
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
            log.log(Level.WARNING, "WSS0805.policy.null");
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
            throw new XWSSecurityException(e);
        }
    }
    
}
