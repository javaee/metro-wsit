
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
package com.sun.xml.wss.impl.filter;

import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.keyinfo.TokenBuilder;
import com.sun.xml.ws.security.opt.impl.keyinfo.X509TokenBuilder;
import com.sun.xml.ws.security.opt.impl.JAXBFilterProcessingContext;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.message.GSHeaderElement;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.wss.NonceManager;
import java.security.cert.X509Certificate;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.core.SecurityHeader;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.UsernameToken;
import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.TimestampPolicy;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPPart;

import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.impl.HarnessUtil;
import com.sun.xml.wss.impl.keyinfo.KeyIdentifierStrategy;
import org.w3c.dom.NodeList;

import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.impl.policy.mls.IssuedTokenKeyBinding;
import com.sun.xml.wss.logging.impl.filter.LogStringsMessages;
import javax.xml.crypto.Data;
import org.w3c.dom.Element;

/**
 * processes diferent types of tokens like Username,X509,IssuedToken... etc
 * 
 */
public class AuthenticationTokenFilter {
    
    private static final Logger log = Logger.getLogger(
            LogDomainConstants.IMPL_FILTER_DOMAIN,
            LogDomainConstants.IMPL_FILTER_DOMAIN_BUNDLE);
    
    /** if the message is incomming it gets Username Token from the meaage
     *  for outgoing it adds Username Token to the message
     *  @param context FilterProcessingContext
     *  @throws XWSSecurityException
     */
    public static void processUserNameToken(FilterProcessingContext context) throws XWSSecurityException {
        if (context.isInboundMessage()) {
            getUserNameTokenFromMessage(context);
        }else{
            addUserNameTokenToMessage(context);
        }
    }
    /**
     * imports and exports the SAML Assertion
     * @param context FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public static void processSamlToken(FilterProcessingContext context) throws XWSSecurityException {
        if (context.isInboundMessage()) {
            ImportSamlAssertionFilter.process(context);
        }else{
            ExportSamlAssertionFilter.process(context);
        }
    }
    
    /**
     * adds the issued token to the message if the message is not an inbound message
     * @param context FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public static void processIssuedToken(FilterProcessingContext context) throws XWSSecurityException {
        if (!context.isInboundMessage()) {            
            addIssuedTokenToMessage(context);
        }        
    }
    
    /**
     * gets the username token from the message and validates it
     * @param context FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
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
                   log.log(Level.SEVERE, LogStringsMessages.WSS_1427_ERROR_ADHOC(),e);
                    throw new XWSSecurityException(e);
                }
            }
            AuthenticationTokenPolicy policy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
            
            NodeList nodeList = wsseSecurity.getElementsByTagNameNS(MessageConstants.WSSE_NS,
                    MessageConstants.USERNAME_TOKEN_LNAME);
            if(nodeList.getLength() <= 0){
                log.log(Level.SEVERE, LogStringsMessages.WSS_1400_NOUSERNAME_FOUND());
                throw new XWSSecurityException("No Username token found ,Receiver requirement not met");
            } else if (nodeList.getLength() > 1) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1401_MORETHANONE_USERNAME_FOUND());
                throw new XWSSecurityException(
                        "More than one Username token found, Receiver requirement not met");
            }else{
                SOAPElement userNameTokenElement = (SOAPElement)nodeList.item(0);
                token = new UsernameToken(userNameTokenElement, policy.isBSP());
                token.isBSP(policy.isBSP());
            }
        }else{
            
            if (context.getMode() == FilterProcessingContext.POSTHOC) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1402_ERROR_POSTHOC());
                throw new XWSSecurityException(
                        "Internal Error: Called UsernameTokenFilter in POSTHOC Mode");
            }
            
            try{
                token = new UsernameToken(wsseSecurity.getCurrentHeaderElement());
            } catch(XWSSecurityException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1403_IMPORT_USERNAME_TOKEN(),ex);
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
                log.log(Level.SEVERE, LogStringsMessages.WSS_1404_NOTMET_DIGESTED());
                throw new XWSSecurityException(
                        "Receiver Requirement for Digested " +
                        "Password has not been met");
            }
            
            if (!utBinding.getDigestOn() && (passwordDigest != null)) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1405_NOTMET_PLAINTEXT());
                throw new XWSSecurityException(
                        "Receiver Requirement for Plain-Text " +
                        "Password has not been met, Received token has Password-Digest");
            }
            
            if (utBinding.getUseNonce() && (nonce == null)) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1406_NOTMET_NONCE());
                throw new XWSSecurityException(
                        "Receiver Requirement for nonce " +
                        "has not been met");
            }
            
            if (!utBinding.getUseNonce() && (nonce != null)) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_1407_NOTMET_NONONCE());
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
                log.log(Level.SEVERE, LogStringsMessages.WSS_1408_FAILED_SENDER_AUTHENTICATION());
                XWSSecurityException xwse =
                        new XWSSecurityException("Invalid Username Password Pair");
                throw SecurableSoapMessage.newSOAPFaultException(
                        MessageConstants.WSSE_FAILED_AUTHENTICATION,
                        "Authentication of Username Password Token Failed",
                        xwse);
            }
            
            if (log.isLoggable(Level.FINEST)) {
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
                    TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                    maxClockSkew = tPolicy.getMaxClockSkew();
                    freshnessLmt = tPolicy.getTimestampFreshness();
                }
                maxNonceAge = policy.getMaxNonceAge();
            }
            
            if (created != null) {
                context.getSecurityEnvironment().validateCreationTime(
                        context.getExtraneousProperties(), created, maxClockSkew, freshnessLmt);
            }
            
            if (log.isLoggable(Level.FINEST) && created!= null) {
                log.log(Level.FINEST, "CreationTime Validated.....");
            }
            
            if (nonce != null) {
                try {
                    if (!context.getSecurityEnvironment().validateAndCacheNonce(context.getExtraneousProperties(),nonce, created, maxNonceAge)) {
                        XWSSecurityException xwse =
                                new XWSSecurityException(
                                "Invalid/Repeated Nonce value for Username Token");
                        log.log(Level.SEVERE, LogStringsMessages.WSS_1406_NOTMET_NONCE(), xwse);
                        throw SecurableSoapMessage.newSOAPFaultException(
                                MessageConstants.WSSE_FAILED_AUTHENTICATION,
                                "Invalid/Repeated Nonce value for Username Token",
                                xwse);
                    }
                } catch (NonceManager.NonceException ex) {
                    throw SecurableSoapMessage.newSOAPFaultException(
                            MessageConstants.WSSE_FAILED_AUTHENTICATION,
                            "Invalid/Repeated Nonce value for Username Token",
                            ex);
                }
            }
            
        } catch (XWSSecurityException xwsse) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_1408_FAILED_SENDER_AUTHENTICATION(), xwsse);
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_FAILED_AUTHENTICATION,
                    xwsse.getMessage(),
                    xwsse);
        }
        
        context.getSecurityEnvironment().updateOtherPartySubject(
                DefaultSecurityEnvironmentImpl.getSubject(context),username, password);
        
    }
    
    
    /**
     * sets the username and password in the usernametoken
     * @param context FilterProcessingContext
     * @param token UsernameToken
     * @param  unToken com.sun.xml.ws.security.opt.impl.tokens.UsernameToken
     * @param policy AuthenticationTokenPolicy
     * @throws XWSSecurityException
     * @return userNamePolicy UsernameTokenBinding
     */
    public static AuthenticationTokenPolicy.UsernameTokenBinding resolveUserNameTokenData(
            FilterProcessingContext context,
            UsernameToken token, com.sun.xml.ws.security.opt.impl.tokens.UsernameToken unToken,
            AuthenticationTokenPolicy policy)throws XWSSecurityException {
        
       
        if(!context.makeDynamicPolicyCallback()) {
            
         //AuthenticationTokenPolicy.UsernameTokenBinding userNamePolicy =  UsernameTokenDataResolver.setSaltandIterationsforUsernameToken(context, token, unToken, policy);
        
            AuthenticationTokenPolicy.UsernameTokenBinding userNamePolicy =
            (AuthenticationTokenPolicy.UsernameTokenBinding)policy.getFeatureBinding();
            String userName = userNamePolicy.getUsername();
            String password = userNamePolicy.getPassword();
            
            if (userName == null || "".equals(userName)) {
            userName = context.getSecurityEnvironment().getUsername(context.getExtraneousProperties());
            }
            
            if (userName == null || "".equals(userName)) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_1409_INVALID_USERNAME_TOKEN());
            throw new XWSSecurityException("Username has not been set");
            }
            
            if(token != null)
            token.setUsername(userName);
            else
            unToken.setUsernameValue(userName);
            
            if (!userNamePolicy.hasNoPassword() && (password == null || "".equals(password))) {
            password = context.getSecurityEnvironment().getPassword(context.getExtraneousProperties());
            }
            if(!userNamePolicy.hasNoPassword()){
            if (password == null) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_1424_INVALID_USERNAME_TOKEN());
            throw new XWSSecurityException("Password for the username has not been set"); 
            }
            if(token != null)
            token.setPassword(password);
            else
            unToken.setPasswordValue(password);
            }
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
                
                if(token != null){
                    token.setUsername(resolvedPolicy.getUsername());
                    token.setPassword(resolvedPolicy.getPassword());
                } else {
                    unToken.setUsernameValue(resolvedPolicy.getUsername());
                    unToken.setPasswordValue(resolvedPolicy.getPassword());
                }
                return resolvedPolicy;
                
            } catch (Exception e) {
              log.log(Level.SEVERE, LogStringsMessages.WSS_1403_IMPORT_USERNAME_TOKEN(), e);
                throw new XWSSecurityException(e);
            }
        }
    }
    
    
    
    /**
     * sets the parameters nonce,creationtime,...etc to the username token
     * adds this username token to the security header
     * @param context FilterProcessingContext
     * @throws XWSSecurityException
     */
    public static void addUserNameTokenToMessage(FilterProcessingContext context)
    throws XWSSecurityException{
        if(context instanceof JAXBFilterProcessingContext){
            JAXBFilterProcessingContext opContext = (JAXBFilterProcessingContext)context;
            com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader secHeader =
                    opContext.getSecurityHeader();
            AuthenticationTokenPolicy authPolicy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
            com.sun.xml.ws.security.opt.impl.tokens.UsernameToken unToken =
                    new com.sun.xml.ws.security.opt.impl.tokens.UsernameToken(opContext.getSOAPVersion());
            
            AuthenticationTokenPolicy.UsernameTokenBinding policy =
                    resolveUserNameTokenData(opContext, null, unToken, authPolicy);
            
            if(policy.getUseNonce()){
                unToken.setNonce(policy.getNonce());
            }
            if(policy.getDigestOn()){
                unToken.setDigestOn();
            }
            
            if ( policy.getUseNonce() || policy.getDigestOn()) {
                String creationTime = "";
                TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                creationTime = tPolicy.getCreationTime();
                unToken.setCreationTime(creationTime);
            }
            
            
            if(policy.hasNoPassword()){
                String creationTime = "";
                TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                creationTime = tPolicy.getCreationTime();
                unToken.setCreationTime(creationTime);
            }
            
            String wsuId = policy.getUUID();
            if (wsuId != null && !wsuId.equals("")){
                unToken.setId(wsuId);
            }
            secHeader.add(unToken);
        } else {
            SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
            SOAPPart soapPart = secureMessage.getSOAPPart();
            
            AuthenticationTokenPolicy authPolicy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
            UsernameToken token = new UsernameToken(soapPart, "");
            
            AuthenticationTokenPolicy.UsernameTokenBinding policy =
                    resolveUserNameTokenData(context, token, null,authPolicy);
            
            if(policy.getUseNonce()){
                token.setNonce(policy.getNonce());
            }
            if(policy.getDigestOn()){
                token.setDigestOn();
            }
            
            if ( policy.getUseNonce() || policy.getDigestOn()) {
                String creationTime = "";
                TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                creationTime = tPolicy.getCreationTime();
                token.setCreationTime(creationTime);
            }
            
            if(policy.hasNoPassword()){
                String creationTime = "";
                TimestampPolicy tPolicy = (TimestampPolicy) policy.getFeatureBinding();
                creationTime = tPolicy.getCreationTime();
                token.setCreationTime(creationTime);
            }
            SecurityHeader wsseSecurity = secureMessage.findOrCreateSecurityHeader();
            String wsuId = policy.getUUID();
            if (wsuId != null && !wsuId.equals("")){
                XMLUtil.setWsuIdAttr(token.getAsSoapElement(), wsuId);
            }
            wsseSecurity.insertHeaderBlock(token);
        }
    }
    
    
    /**
     * gets the issued token and adds it to the security header
     * @param context FilterProcessingContext
     * @throws XWSSecurityException
     */
    @SuppressWarnings({"unchecked", "static-access" })
    public static void addIssuedTokenToMessage(FilterProcessingContext context)
    throws XWSSecurityException{
        AuthenticationTokenPolicy authPolicy = (AuthenticationTokenPolicy)context.getSecurityPolicy();
        IssuedTokenKeyBinding itkb = (IssuedTokenKeyBinding)authPolicy.getFeatureBinding();
        String itType = itkb.getIncludeToken();
        boolean includeToken  = (itkb.INCLUDE_ALWAYS_TO_RECIPIENT.equals(itType) ||
                          itkb.INCLUDE_ALWAYS.equals(itType) ||
                          itkb.INCLUDE_ALWAYS_VER2.equals(itType) ||
                          itkb.INCLUDE_ALWAYS_TO_RECIPIENT_VER2.equals(itType)
                          );
        if(context instanceof JAXBFilterProcessingContext){
            JAXBFilterProcessingContext opContext = (JAXBFilterProcessingContext)context;
            com.sun.xml.ws.security.opt.impl.outgoing.SecurityHeader secHeader =
                    opContext.getSecurityHeader();
            com.sun.xml.ws.security.opt.api.SecurityHeaderElement issuedTokenElement = null;
            GenericToken issuedToken = null;
            if(opContext.getTrustContext() == null){
                String itPolicyId = itkb.getUUID();                
                IssuedTokenContext ictx = opContext.getIssuedTokenContext(itPolicyId);
                if (ictx != null) {
                    opContext.setTrustContext(ictx);                    
                    issuedToken = (GenericToken)ictx.getSecurityToken();
                }
            }            
            
            if(issuedToken != null){
                issuedTokenElement = issuedToken.getElement();
                if(issuedTokenElement == null){
                    Element element = (Element)issuedToken.getTokenValue();
                    issuedTokenElement = new GSHeaderElement(element);
                    issuedTokenElement.setId(issuedToken.getId());
                }
            }            
            if (issuedToken != null && includeToken) {
                if(opContext.getSecurityHeader().getChildElement(issuedTokenElement.getId()) == null){
                    secHeader.add(issuedTokenElement);
                }
            } 
            
            if (null != itkb.getSTRID()) {
                
                String itId = issuedToken.getId();
                WSSElementFactory elementFactory = new WSSElementFactory(opContext.getSOAPVersion());
                com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier ref = elementFactory.createKeyIdentifier();
                ref.setValue(itId);
                String valueType = null;
                if (issuedTokenElement != null){
                    String issuedTokenNS = issuedTokenElement.getNamespaceURI();
                    if (MessageConstants.SAML_v1_0_NS.equals(issuedTokenNS)){
                        valueType = MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE;
                    }
                    
                    if (MessageConstants.SAML_v2_0_NS.equals(issuedTokenNS)){
                        valueType = MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE;
                    }
                }
                ref.setValueType(valueType);
                com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference secTokRef = elementFactory.createSecurityTokenReference(ref);
                String strId = itkb.getSTRID();
                secTokRef.setId(strId);
                
                Data data = new SSEData((SecurityElement)issuedTokenElement,false,opContext.getNamespaceContext());
                opContext.getElementCache().put(strId,data);
                secHeader.add(secTokRef);
            }
            
        }else{
            SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
            SOAPPart soapPart = secureMessage.getSOAPPart();            
            GenericToken issuedToken = null;
            if(context.getTrustContext() == null){
                String itPolicyId = itkb.getUUID();                
                IssuedTokenContext ictx = context.getIssuedTokenContext(itPolicyId);
                if (ictx != null) {
                    context.setTrustContext(ictx);                    
                    issuedToken = (GenericToken)ictx.getSecurityToken();
                }
            }            
                                        
            Element element = (Element)issuedToken.getTokenValue();
            SOAPElement tokenEle = XMLUtil.convertToSoapElement(soapPart, element);
            if(tokenEle != null && includeToken){
                secureMessage.findOrCreateSecurityHeader().insertHeaderBlockElement(tokenEle);            
            }
            context.setIssuedSAMLToken(tokenEle);
            
            if (null != itkb.getSTRID()) {
                String itId = issuedToken.getId();
                SecurityTokenReference tokenRef = new SecurityTokenReference(secureMessage.getSOAPPart());
                tokenRef.setWsuId(itkb.getSTRID());
                
                KeyIdentifierStrategy strat = new KeyIdentifierStrategy(itId);
                strat.insertKey(tokenRef, context.getSecurableSoapMessage());
                secureMessage.findOrCreateSecurityHeader().insertHeaderBlock(tokenRef);
            }
        }
    }
    
    /**
     * processes the X509 token , if any
     * @param context FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public static void processX509Token(FilterProcessingContext context) throws XWSSecurityException {
        
        if (context.isInboundMessage()) {
            return;
        }
        
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
        
        AuthenticationTokenPolicy.X509CertificateBinding policyClone = (AuthenticationTokenPolicy.X509CertificateBinding)policy.clone();
        policyClone.setX509Certificate(cert);
        
        
        if(context instanceof JAXBFilterProcessingContext){
            JAXBFilterProcessingContext opContext = (JAXBFilterProcessingContext)context;
            ((NamespaceContextEx)opContext.getNamespaceContext()).addWSSNS();
            TokenBuilder x509TokenBuilder = new X509TokenBuilder(opContext, policyClone);
            x509TokenBuilder.process();
        } else{
            SecurableSoapMessage secureMessage = context.getSecurableSoapMessage();
            String wsuId = policy.getUUID();
            if (wsuId == null){
                wsuId = secureMessage.generateId();
            }
            SecurityUtil.checkIncludeTokenPolicy(context, policyClone, wsuId);
        }
        //X509SecurityToken token = new X509SecurityToken(secureMessage.getSOAPPart(), cert, wsuId);
        //wsseSecurity.insertHeaderBlock(token);
    }
    /**
     * processes the RSA token 
     * @param context FilterProcessingContext
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    public static void processRSAToken(FilterProcessingContext context) throws XWSSecurityException {
        
        if (context.isInboundMessage()) {
            return;
        }                
    }
}
