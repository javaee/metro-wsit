/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
/**
 * $Id: WssProviderAuthModule.java,v 1.3 2006-10-09 09:18:33 kumarjayanti Exp $
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.provider.wsit;


import com.sun.enterprise.security.jauth.AuthParam;
import java.util.*;
import javax.xml.soap.*;
import javax.security.auth.callback.CallbackHandler;


import com.sun.enterprise.security.jauth.AuthPolicy;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.wss.impl.AlgorithmSuite;

import com.sun.xml.wss.impl.WssProviderSecurityEnvironment;
import com.sun.xml.wss.impl.PolicyViolationException;
import com.sun.xml.wss.impl.WssSoapFaultException;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.ProcessingContextImpl;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.SecurityAnnotator;
import com.sun.xml.wss.impl.SecurityRecipient;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;

import static com.sun.xml.wss.provider.wsit.ModuleOptions.*;

public abstract class WssProviderAuthModule {
    
    // Security Environment reference initialized with a JAAS CallbackHandler
    protected WssProviderSecurityEnvironment secEnv = null;
    
    public static final String REQUESTER_SUBJECT = "REQUESTER_SUBJECT";
    public static final String REQUESTER_KEYID = "REQUESTER_KEYID";
    public static final String REQUESTER_ISSUERNAME = "REQUESTER_ISSUERNAME";
    public static final String REQUESTER_SERIAL = "REQUESTER_SERIAL";
    public static final String SELF_SUBJECT = "SELF_SUBJECT";
    
    public static final String SOAP12="soap12";
    public static final String SOAP11="soap11";
    
    public static final String SOAP_FACTORY="SoapFactory";
    public static final String PACKET = "packet";
    public static final String ISSUED_TOKEN_CONTEXT_MAP = "issued_token_context_map";
    public static final String ALGORITHM_SUITE = "algorithm_suite";
    public static final String MESSAGE_POLICY="message_policy";
    public static final String OPTIMIZED="optimized";
    public static final String SOAP_VERSION="soap_version";
    public static final String BINDING_HAS_ISSUED_TOKENS="BindingHasIssuedTokens";
    
    
    protected boolean debug = false;
    // SOAP version
    protected boolean isSOAP12 = false;
    // SOAP Factory
    protected static SOAPFactory soapFactory = null;
    
    
    public WssProviderAuthModule() {
    }
    
    /**
     * Initialization method for Client and Server Auth Modules
     * @param requestPolicy
     *        used to validate request on server side
     *        and to secure request on client side
     * @param responsePolicy
     *        used to validate response on client side
     *        and to secure response on server side
     * @param handler
     *        CallbackHandler
     * @param options
     *        Map of module options
     * @param isClientAuthModule
     *        indicates if the current instance is client or server module
     * @throws RuntimeException
     */
    public void initialize(AuthPolicy requestPolicy,
            AuthPolicy responsePolicy,
            CallbackHandler handler,
            Map<String, Object> options,
            boolean isClientAuthModule) {
        
        
        String bg = (String)options.get(DEBUG);
        isSOAP12 = SOAP12.equals((String)options.get(SOAP_VERSION));
        soapFactory = (SOAPFactory)options.get(SOAP_FACTORY);
        
        boolean debugON = false;
        if (bg !=null && bg.equals("true")) debug = true;
        
        try {
            secEnv = new WssProviderSecurityEnvironment(handler, options);
        } catch (XWSSecurityException ex){
            throw new RuntimeException(ex);
        }
    }
    
    
    protected SOAPFaultException getSOAPFaultException(WssSoapFaultException sfe) {
        
        SOAPFault fault = null;
        try {
            if (isSOAP12) {
                fault = soapFactory.createFault(sfe.getFaultString(),SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(sfe.getFaultCode());
            } else {
                fault = soapFactory.createFault(sfe.getFaultString(), sfe.getFaultCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Security Pipe: Internal Error while trying to create a SOAPFault");
        }
        return new SOAPFaultException(fault);
        
    }
    
    protected SOAPFaultException getSOAPFaultException(XWSSecurityException xwse) {
        QName qname = null;
        if (xwse.getCause() instanceof PolicyViolationException)
            qname = MessageConstants.WSSE_RECEIVER_POLICY_VIOLATION;
        else
            qname = MessageConstants.WSSE_FAILED_AUTHENTICATION;
        
        com.sun.xml.wss.impl.WssSoapFaultException wsfe =
                SecurableSoapMessage.newSOAPFaultException(
                qname, xwse.getMessage(), xwse);
        
        return getSOAPFaultException(wsfe);
    }
    
  protected ProcessingContext initializeProcessingContext(
            AuthParam param, Map<String, Object> sharedState, Packet packet, boolean optimized, boolean inbound)
            throws XWSSecurityException {
        
        if (optimized) {
            // todo do optimized processing context creation
            return null;
        }
        
        ProcessingContextImpl ctx =
                new ProcessingContextImpl(packet.invocationProperties);
        
        Hashtable issuedTokenContextMap = (Hashtable)sharedState.get(ISSUED_TOKEN_CONTEXT_MAP);
        AlgorithmSuite suite = (AlgorithmSuite)sharedState.get(ALGORITHM_SUITE);
        MessagePolicy policy = (MessagePolicy) sharedState.get(MESSAGE_POLICY);
        String issuedTokensPresent = (String)sharedState.get(BINDING_HAS_ISSUED_TOKENS);
        boolean hasIssuedTokens = false;
        if ("true".equals(issuedTokensPresent)) {
            hasIssuedTokens = true;
        }
        // set the policy, issued-token-map, and extraneous properties
        ctx.setIssuedTokenContextMap(issuedTokenContextMap);
        ctx.setAlgorithmSuite(suite);
        
        if (debug) {
            policy.dumpMessages(true);
        }
        
        if (policy.getAlgorithmSuite() != null) {
            //override the binding level suite
            ctx.setAlgorithmSuite(policy.getAlgorithmSuite());
        }
        ctx.setWSSAssertion(policy.getWSSAssertion());
        ctx.setSecurityPolicy(policy);
        ctx.setSecurityEnvironment(secEnv);
        // setting a flag if issued tokens present
        ctx.hasIssuedToken(hasIssuedTokens);
        
        if (!inbound) {
          ctx.isInboundMessage(false);    
        }else {
          ctx.isInboundMessage(true);  
        }
        
        return ctx;
    }
      
    
    protected Message  secureOutboundMessage(
            Message message, ProcessingContext ctx, boolean optimized){
        try {
            if (optimized) {
                // ctx.setMessage(message);
            }else {
                ctx.setSOAPMessage(message.readAsSOAPMessage());
            }
            SecurityAnnotator.secureMessage(ctx);

            Message secureMessage = null;
            if (optimized) {
                //secureMessage = ctx.getMessage();
            } else {
                secureMessage = Messages.create(ctx.getSOAPMessage());
            }
            return secureMessage;
        } catch (SOAPException ex){
            //log
            XWSSecurityException xwse = new XWSSecurityException(ex);
            throw getSOAPFaultException(xwse);
        } catch (WssSoapFaultException soapFaultException) {
            //log
            throw getSOAPFaultException(soapFaultException);
        } catch (XWSSecurityException xwse) {
            //log
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            throw getSOAPFaultException(wsfe);
        }
    }
    
    
    protected Message  verifyInboundMessage(
            Message message, ProcessingContext ctx, boolean optimized) {
        try {
            if (optimized) {
                //ctx.setMessage(message);
            } else {
                ctx.setSOAPMessage(message.readAsSOAPMessage());
            }

            SecurityRecipient.validateMessage(ctx);
            
            Message validatedMessage = null;
            if (optimized) {
                //validatedMessage = ctx.getMessage();
            } else {
                validatedMessage = Messages.create(ctx.getSOAPMessage());
            }
            return validatedMessage;
            
        } catch (SOAPException ex) {
            //log
            XWSSecurityException xwse = new XWSSecurityException(ex);
            throw getSOAPFaultException(xwse);            
        } catch (WssSoapFaultException soapFaultException) {
            //log
            throw getSOAPFaultException(soapFaultException);
        } catch (XWSSecurityException xwse) {
            //log
            WssSoapFaultException wsfe =
                    SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INTERNAL_SERVER_ERROR,
                    xwse.getMessage(), xwse);
            throw getSOAPFaultException(wsfe);
        }
    }
}
