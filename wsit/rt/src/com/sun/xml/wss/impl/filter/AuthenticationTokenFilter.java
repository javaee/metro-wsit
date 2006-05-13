/*
 * $Id: AuthenticationTokenFilter.java,v 1.2 2006-05-13 08:17:27 kumarjayanti Exp $
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
package com.sun.xml.wss.impl.filter;

import java.security.cert.X509Certificate;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.core.BinarySecurityToken;
import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.UsernameToken;
import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.core.X509SecurityToken;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;
import com.sun.xml.wss.impl.policy.mls.WSSPolicy;

import java.util.logging.Level;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPPart;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;

import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;

import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.misc.NonceContainer;

import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;

import com.sun.xml.wss.impl.HarnessUtil;

import org.w3c.dom.NodeList;

import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.policy.SecurityPolicy;

public class AuthenticationTokenFilter {
    
    private static Logger log = Logger.getLogger(
            LogDomainConstants.IMPL_FILTER_DOMAIN,
            LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
    
    /**
     * @param context
     * @throws XWSSecurityException
     */
    public static void processUserNameToken(FilterProcessingContext context) throws XWSSecurityException {
        if (context.isInboundMessage()) {
            getUserNameTokenFromMessage(context);
        }else{
            addUserNameTokenToMessage(context);
        }
    }
    
    public static void processSamlToken(FilterProcessingContext context) throws XWSSecurityException {
        if (context.isInboundMessage()) {
            ImportSamlAssertionFilter.process(context);
        }else{
            ExportSamlAssertionFilter.process(context);
        }
    }
    
    
    private static void getUserNameTokenFromMessage(FilterProcessingContext context)
    throws XWSSecurityException{
        
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        SecurityHeader wsseSecurity = secureMessage.findSecurityHeader();
        UsernameToken token = null;
        
        if(context.getMode() == FilterProcessingContext.ADHOC) {
            //AuthenticationTokenPolicy policy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
            if ( context.makeDynamicPolicyCallback() ) {
                try {
                    
                    AuthenticationTokenPolicy policy =
                            ((AuthenticationTokenPolicy)context.getSecurityPolicy());
                    
                    
                    AuthenticationTokenPolicy.UsernameTokenBinding userNamePolicy =
                            (AuthenticationTokenPolicy.UsernameTokenBinding)policy.getFeatureBinding();
                    userNamePolicy.isReadOnly(true);
                    
                    DynamicApplicationContext dynamicContext =
                            new DynamicApplicationContext(context.getPolicyContext());
                    
                    dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                    dynamicContext.inBoundMessage(true);
                    DynamicPolicyCallback dynamicCallback =
                            new DynamicPolicyCallback(userNamePolicy, dynamicContext);
                    ProcessingContext.copy(dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                    HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                            context.getSecurityEnvironment().getCallbackHandler());
                    
                    policy.setFeatureBinding((AuthenticationTokenPolicy.UsernameTokenBinding)dynamicCallback.getSecurityPolicy());
                    //context.setSecurityPolicy(policy);
                } catch (Exception e) {
                    // log
                    throw new XWSSecurityException(e);
                }
            }
            AuthenticationTokenPolicy policy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
            
            NodeList nodeList = wsseSecurity.getElementsByTagNameNS(MessageConstants.WSSE_NS,
                    MessageConstants.USERNAME_TOKEN_LNAME);
            if(nodeList.getLength() <= 0){
                log.log(Level.SEVERE, "WSS1400.nousername.found");
                throw new XWSSecurityException("No Username token found ,Receiver requirement not met");
            } else if (nodeList.getLength() > 1) {
                log.log(Level.SEVERE, "WSS1401.morethanone.username.found");
                throw new XWSSecurityException(
                        "More than one Username token found, Receiver requirement not met");
            }else{
                SOAPElement userNameTokenElement = (SOAPElement)nodeList.item(0);
                token = new UsernameToken(userNameTokenElement, policy.isBSP());
                token.isBSP(policy.isBSP());
            }
        }else{
            
            if (context.getMode() == FilterProcessingContext.POSTHOC) {
                log.log(Level.SEVERE, "WSS1402.error.posthoc");
                throw new XWSSecurityException(
                        "Internal Error: Called UsernameTokenFilter in POSTHOC Mode");
            }
            
            try{
                token = new UsernameToken(wsseSecurity.getCurrentHeaderElement());
            } catch(XWSSecurityException ex) {
                log.log(Level.SEVERE, "WSS1403.import.username.token");
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "Exception while importing Username Password Token",
                        ex);
            }
        }
        
        String username = token.getUsername();
        String password = token.getPassword();
        String passwordDigest = token.getPasswordDigest();
        String passwordType = token.getPasswordType();
        String nonce = token.getNonce();
        String created = token.getCreated();
        boolean authenticated = false;
        
        if (context.getMode() == FilterProcessingContext.ADHOC) {
            
            AuthenticationTokenPolicy policy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
            AuthenticationTokenPolicy.UsernameTokenBinding utBinding =
                    (AuthenticationTokenPolicy.UsernameTokenBinding)policy.getFeatureBinding();
            
            // do policy checks
            if (utBinding.getDigestOn() && (passwordDigest == null)) {
                log.log(Level.SEVERE, "WSS1404.notmet.digested");
                throw new XWSSecurityException(
                        "Receiver Requirement for Digested " +
                        "Password has not been met");
            }
            
            if (!utBinding.getDigestOn() && (passwordDigest != null)) {
                log.log(Level.SEVERE, "WSS1405.notmet.plaintext");
                throw new XWSSecurityException(
                        "Receiver Requirement for Plain-Text " +
                        "Password has not been met, Received token has Password-Digest");
            }
            
            if (utBinding.getUseNonce() && (nonce == null)) {
                log.log(Level.SEVERE, "WSS1406.notmet.nonce");
                throw new XWSSecurityException(
                        "Receiver Requirement for nonce " +
                        "has not been met");
            }
            
            if (!utBinding.getUseNonce() && (nonce != null)) {
                log.log(Level.SEVERE, "WSS1407.notmet.nononce");
                throw new XWSSecurityException(
                        "Receiver Requirement for no nonce " +
                        "has not been met, Received token has a nonce specified");
            }
        } else if (context.getMode() == FilterProcessingContext.WSDL_POLICY) {
            //try to infer a Policy here
            AuthenticationTokenPolicy.UsernameTokenBinding sp = new AuthenticationTokenPolicy.UsernameTokenBinding();
            if (passwordDigest != null) {
                sp.setDigestOn(true);
            }
            if (nonce != null) {
                sp.setUseNonce(true);
            }
            ((MessagePolicy)context.getInferredSecurityPolicy()).append(sp);
        }
        
        try {
            if (MessageConstants.PASSWORD_TEXT_NS == passwordType) {
                authenticated = context.getSecurityEnvironment().authenticateUser(context.getExtraneousProperties(), username, password);
            } else{
                authenticated = context.getSecurityEnvironment().authenticateUser(
                        context.getExtraneousProperties(), username, passwordDigest, nonce, created);
            }
            
            if (!authenticated) {
                log.log(Level.SEVERE, "WSS1408.failed.sender.authentication");
                XWSSecurityException xwse =
                        new XWSSecurityException("Invalid Username Password Pair");
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_FAILED_AUTHENTICATION,
                        "Authentication of Username Password Token Failed",
                        xwse);
            }
            
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "Password Validated.....");
            }
            
            long maxClockSkew = Timestamp.MAX_CLOCK_SKEW;
            long freshnessLmt = Timestamp.TIMESTAMP_FRESHNESS_LIMIT;
            long maxNonceAge =  UsernameToken.MAX_NONCE_AGE;
            
            if (context.getMode() == FilterProcessingContext.ADHOC) {
                
                AuthenticationTokenPolicy authPolicy =
                        (AuthenticationTokenPolicy)context.getSecurityPolicy();
                
                AuthenticationTokenPolicy.UsernameTokenBinding policy =
                        (AuthenticationTokenPolicy.UsernameTokenBinding)
                        authPolicy.getFeatureBinding();
                
                if (created != null) {
                    try {
                        TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                        maxClockSkew = tPolicy.getMaxClockSkew();
                        freshnessLmt = tPolicy.getTimestampFreshness();
                    } catch (Exception e) {}
                }
                maxNonceAge = policy.getMaxNonceAge();
            }
            
            if (created != null) {
                context.getSecurityEnvironment().validateCreationTime(
                        context.getExtraneousProperties(), created, maxClockSkew, freshnessLmt);
            }
            
            if (MessageConstants.debug && created!= null) {
                log.log(Level.FINEST, "CreationTime Validated.....");
            }
            
            if (nonce != null ) {
                
                if (context.getHandler() != null) {
                    //need to use NonceContainer here
                    StaticPolicyContext pc = context.getPolicyContext();
                    String applicationId = null;
                    if ((pc != null) && (pc instanceof StaticApplicationContext)) {
                        applicationId =
                                ((StaticApplicationContext)pc).getApplicationContextRoot();
                    }
                    if (applicationId != null) {
                        if (!validateAndCacheNonce(applicationId, nonce, created, maxNonceAge)) {
                            XWSSecurityException xwse =
                                    new XWSSecurityException(
                                    "Invalid/Repeated Nonce value for Username Token");
                            throw SecurableSoapMessage.newSOAPFaultException(
                                    MessageConstants.WSSE_FAILED_AUTHENTICATION,
                                    "Invalid/Repeated Nonce value for Username Token",
                                    xwse);
                        }
                    } else {
                        //log a WARNING that we are unable to validate nonce since no ApplicationId was provided
                        if (MessageConstants.debug) {
                            log.log(Level.WARNING,"Unable to validate nonce since no ApplicationId was provided");
                        }
                    }
                    
                } else {
                    if (!context.getSecurityEnvironment().validateAndCacheNonce(nonce, created, maxNonceAge)) {
                        XWSSecurityException xwse =
                                new XWSSecurityException(
                                "Invalid/Repeated Nonce value for Username Token");
                        throw SecurableSoapMessage.newSOAPFaultException(
                                MessageConstants.WSSE_FAILED_AUTHENTICATION,
                                "Invalid/Repeated Nonce value for Username Token",
                                xwse);
                    }
                }
            }
            
        } catch (XWSSecurityException xwsse) {
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_FAILED_AUTHENTICATION,
                    xwsse.getMessage(),
                    xwsse);
        }
        
        context.getSecurityEnvironment().updateOtherPartySubject(
                DefaultSecurityEnvironmentImpl.getSubject(context),username, password);
        
    }
    
    
    /**
     * @param context
     * @throws XWSSecurityException
     * @return
     */
    public static AuthenticationTokenPolicy.UsernameTokenBinding resolveUserNameTokenData(
            FilterProcessingContext context,
            UsernameToken token,
            AuthenticationTokenPolicy policy)throws XWSSecurityException {
        
        
        if(!context.makeDynamicPolicyCallback()) {
            
            AuthenticationTokenPolicy.UsernameTokenBinding userNamePolicy =
                    (AuthenticationTokenPolicy.UsernameTokenBinding)policy.getFeatureBinding();
            String userName = userNamePolicy.getUsername();
            String password = userNamePolicy.getPassword();
            
            if (userName == null || "".equals(userName)) {
                userName = context.getSecurityEnvironment().getUsername(context.getExtraneousProperties());
            }
            
            if (userName == null || "".equals(userName)) {
                log.log(Level.SEVERE, "WSS1409.error.creating.usernametoken");
                throw new XWSSecurityException("Username has not been set");
            }
            token.setUsername(userName);
            
            if (password == null || "".equals(password)) {
                password = context.getSecurityEnvironment().getPassword(context.getExtraneousProperties());
            }
            token.setPassword(password);
            
            return userNamePolicy;
            
        }else{
            try {
                //((AuthenticationTokenPolicy)policy).isReadOnly(true);
                AuthenticationTokenPolicy.UsernameTokenBinding userNamePolicy =
                        (AuthenticationTokenPolicy.UsernameTokenBinding)policy.getFeatureBinding();
                userNamePolicy.isReadOnly(true);
                
                DynamicApplicationContext dynamicContext =
                        new DynamicApplicationContext(context.getPolicyContext());
                
                dynamicContext.setMessageIdentifier(context.getMessageIdentifier());
                dynamicContext.inBoundMessage(false);
                DynamicPolicyCallback dynamicCallback =
                        new DynamicPolicyCallback(userNamePolicy, dynamicContext);
                ProcessingContext.copy(dynamicContext.getRuntimeProperties(), context.getExtraneousProperties());
                HarnessUtil.makeDynamicPolicyCallback(dynamicCallback,
                        context.getSecurityEnvironment().getCallbackHandler());
                
                
                AuthenticationTokenPolicy.UsernameTokenBinding resolvedPolicy =
                        (AuthenticationTokenPolicy.UsernameTokenBinding)dynamicCallback.getSecurityPolicy();
                
                token.setUsername(resolvedPolicy.getUsername());
                token.setPassword(resolvedPolicy.getPassword());
                return resolvedPolicy;
                
            } catch (Exception e) {
                // log
                throw new XWSSecurityException(e);
            }
        }
    }
    
    
    
    /**
     *
     * @param context
     * @throws XWSSecurityException
     */
    public static void addUserNameTokenToMessage(FilterProcessingContext context)
    throws XWSSecurityException{
        
        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        SOAPPart soapPart = secureMessage.getSOAPPart();
        
        AuthenticationTokenPolicy authPolicy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
        UsernameToken token = new UsernameToken(soapPart, "");
        
        AuthenticationTokenPolicy.UsernameTokenBinding policy =
                resolveUserNameTokenData(context, token, authPolicy);
        
        if(policy.getUseNonce()){
            token.setNonce(policy.getNonce());
        }
        if(policy.getDigestOn()){
            token.setDigestOn();
        }
        
        if ( policy.getUseNonce() || policy.getDigestOn()) {
            String creationTime = "";
            try {
                TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                creationTime = tPolicy.getCreationTime();
            } catch (Exception e) {}
            token.setCreationTime(creationTime);
        }
        
        SecurityHeader wsseSecurity = secureMessage.findOrCreateSecurityHeader();
        String wsuId = policy.getUUID();
        if (wsuId != null && !wsuId.equals("")){
            XMLUtil.setWsuIdAttr(token.getAsSoapElement(), wsuId);
        }
        
        wsseSecurity.insertHeaderBlock(token);
    }
    
    /**
     * Validate the nonce in the UsernameToken and cache the nonce
     * @param nonce
     * @param created
     * @return
     */
    public static boolean validateAndCacheNonce(
            String applicationId, String nonce, String created, long maxNonceAge) {
        return NonceContainer.validateAndCacheNonce(
                applicationId, nonce, created, maxNonceAge);
    }
    
    public static void processX509Token(FilterProcessingContext context) throws XWSSecurityException {
    
        if (context.isInboundMessage()) {
            return;
        }

        SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
        AuthenticationTokenPolicy authPolicy = 
            (AuthenticationTokenPolicy)context.getSecurityPolicy();
        AuthenticationTokenPolicy.X509CertificateBinding policy =
            (AuthenticationTokenPolicy.X509CertificateBinding)
                authPolicy.getFeatureBinding();

        X509Certificate cert = context.getSecurityEnvironment().
            getDefaultCertificate(context.getExtraneousProperties());
        if (cert == null) {
            throw new XWSSecurityException("No default X509 certificate was provided");
        }
        SecurityHeader wsseSecurity = secureMessage.findOrCreateSecurityHeader();
        String wsuId = policy.getUUID();
        if (wsuId == null){
            wsuId = secureMessage.generateId();
        }
        X509SecurityToken token = new X509SecurityToken(secureMessage.getSOAPPart(), cert, wsuId);
        wsseSecurity.insertHeaderBlock(token);

    }
    
}
