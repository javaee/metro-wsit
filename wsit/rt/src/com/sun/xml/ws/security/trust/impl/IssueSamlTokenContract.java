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

package com.sun.xml.ws.security.trust.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import javax.security.auth.Subject;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

public abstract class IssueSamlTokenContract implements WSTrustContract {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    protected TrustSPMetadata config;
    
    protected static final WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    protected static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
    
    private static final int DEFAULT_KEY_SIZE = 256;

    
    public void init(Configuration config) {
        this.config = (TrustSPMetadata)config;
    }
    
    /** Issue a Token */
    public RequestSecurityTokenResponse issue(RequestSecurityToken rst, IssuedTokenContext context, SecureConversationToken policy)throws WSTrustException {
        // Get TokenType
        String tokenType = null;
        URI tokenTypeURI = rst.getTokenType();
        if (tokenTypeURI != null){
            tokenType = tokenTypeURI.toString();
        }else{
            tokenType = config.getTokenType();
        }
        if (tokenType == null){
            tokenType = WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE;
        }
        
        // Get KeyType
        String keyType = null;
        URI keyTypeURI = rst.getKeyType();
        if (keyTypeURI != null){
            keyType = keyTypeURI.toString();
        }else{
            keyType = config.getKeyType();
        }
        if (keyType == null){
            keyType = WSTrustConstants.SYMMETRIC_KEY;
        }
        
        // Get AppliesTo
        AppliesTo ap = rst.getAppliesTo();
        String appliesTo = null;
        if(ap != null){
            appliesTo = WSTrustUtil.getAppliesToURI(ap);
        }
        
        // Get authenticaed client Subject
        Subject subject = context.getRequestorSubject();
        if(subject == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0030_REQUESTOR_NULL());
            throw new WSTrustException(LogStringsMessages.WST_0030_REQUESTOR_NULL());
        }
        
        // Check if the client is authorized to be issued the token
        STSAuthorizationProvider authzProvider = WSTrustFactory.getSTSAuthorizationProvider();
        if (!authzProvider.isAuthorized(subject, appliesTo, tokenType, keyType)){
            log.log(Level.SEVERE, 
                    LogStringsMessages.WST_0015_CLIENT_NOT_AUTHORIZED(
                    subject.toString(), tokenType, appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0015_CLIENT_NOT_AUTHORIZED(
                    subject.toString(), tokenType, appliesTo));
        }
        
        // Get claimed attributes
        Claims claims = rst.getClaims();
        STSAttributeProvider attrProvider = WSTrustFactory.getSTSAttributeProvider();
        Map claimedAttrs = attrProvider.getClaimedAttributes(subject, appliesTo, tokenType, claims);
        
        RequestedProofToken proofToken = null;
        Entropy serverEntropy = null;
        int keySize = 0;
        if (WSTrustConstants.SYMMETRIC_KEY.equals(keyType)){
            //============================
            // Create required secret key
            //============================
            
            proofToken = eleFac.createRequestedProofToken();
            
            // Get client entropy
            byte[] clientEntropyValue = null;
            Entropy clientEntropy = rst.getEntropy();
            if (clientEntropy != null){
                BinarySecret clientBS = clientEntropy.getBinarySecret();
                if (clientBS == null){
                    if(log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, 
                                LogStringsMessages.WST_1009_NULL_BINARY_SECRET());
                    }
                }else {
                    clientEntropyValue = clientBS.getRawValue();
                }
            }
            
            keySize = (int)rst.getKeySize();
            if (keySize < 1){
                keySize = DEFAULT_KEY_SIZE;
            }
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, 
                        LogStringsMessages.WST_1010_KEY_SIZE(keySize));
            }
            
            byte[] key = WSTrustUtil.generateRandomSecret(keySize/8);
            BinarySecret serverBS = eleFac.createBinarySecret(key, BinarySecret.NONCE_KEY_TYPE);
            serverEntropy = eleFac.createEntropy(serverBS);
            proofToken.setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);
            
            // compute the secret key
            try {
                proofToken.setComputedKey(URI.create(WSTrustConstants.CK_PSHA1));
                key = SecurityUtil.P_SHA1(clientEntropyValue, key, keySize/8);
            } catch (Exception ex){
                log.log(Level.SEVERE, 
                        LogStringsMessages.WST_0013_ERROR_SECRET_KEY(), ex);
                throw new WSTrustException(LogStringsMessages.WST_0013_ERROR_SECRET_KEY(), ex);
            }
            
            context.setProofKey(key);
        }else if(WSTrustConstants.PUBLIC_KEY.equals(keyType)){
            // Get client certificate and put it in the IssuedTokenContext
        }else{
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keyType));
            throw new WSTrustException(LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keyType));
        }
        
        //==================
        // Create the RSTR
        //==================
        
        // get Context
        URI ctx = null;
        try {
            String rstCtx = rst.getContext();
            if (rstCtx != null)
                ctx = new URI(rst.getContext());
        } catch (URISyntaxException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0014_URI_SYNTAX(rst.getContext()));
            throw new WSTrustException(
                    LogStringsMessages.WST_0014_URI_SYNTAX(rst.getContext()) ,ex);
        }
        
        // Create RequestedSecurityToken with SAML assertion
        String assertionId = "uuid-" + UUID.randomUUID().toString();
        RequestedSecurityToken st = eleFac.createRequestedSecurityToken();
        Token samlToken = createSAMLAssertion(appliesTo, tokenType, keyType, assertionId, config.getIssuer(), claimedAttrs, context);
        st.setToken(samlToken);
        
        // Create RequestedAttachedReference and RequestedUnattachedReference
        SecurityTokenReference samlReference = createSecurityTokenReference(assertionId, tokenType);
        RequestedAttachedReference raRef =  eleFac.createRequestedAttachedReference(samlReference);
        RequestedUnattachedReference ruRef =  eleFac.createRequestedUnattachedReference(samlReference);
        
        // Create Lifetime
        Lifetime lifetime = createLifetime();
        
        RequestSecurityTokenResponse rstr =
                eleFac.createRSTRForIssue(rst.getTokenType(), ctx, st, ap, raRef, ruRef, proofToken, serverEntropy, lifetime);
        
        if (keySize > 0){
            rstr.setKeySize(keySize);
        }
        
       String issuer = config.getIssuer();
        
      // Token samlToken = createSAMLAssertion(appliesTo, tokenType, keyType, assertionId, issuer, claimedAttrs, context);
       //rstr.getRequestedSecurityToken().setToken(samlToken);
        
        // Populate IssuedTokenContext
        context.setSecurityToken(samlToken);
        context.setAttachedSecurityTokenReference(samlReference);
        context.setUnAttachedSecurityTokenReference(samlReference);
        context.setCreationTime(new Date(currentTime));
        context.setExpirationTime(new Date(currentTime + config.getIssuedTokenTimeout()));
        
        return rstr;
    }
    
    /** Issue a Collection of Token(s) possibly for different scopes */
    public RequestSecurityTokenResponseCollection issueMultiple(
            RequestSecurityToken request, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: issueMultiple");
    }
    
    /** Renew a Token */
    public RequestSecurityTokenResponse renew(
            RequestSecurityToken request, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: renew");
    }
    
    /** Cancel a Token */
    public RequestSecurityTokenResponse cancel(
            RequestSecurityToken request, IssuedTokenContext context, Map issuedTokenContextMap)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: cancel");
    }
    
    /** Validate a Token */
    public RequestSecurityTokenResponse validate(
            RequestSecurityToken request, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: validate");
    }
    
    /**
     * handle an unsolicited RSTR like in the case of
     * Client Initiated Secure Conversation.
     */
    public void handleUnsolicited(
            RequestSecurityTokenResponse rstr, IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: handleUnsolicited");
    }
    
    /**
     * Contains Challenge
     * @return true if the RSTR contains a SignChallenge/BinaryExchange or
     *  some other custom challenge recognized by this implementation.
     */
    public boolean containsChallenge(RequestSecurityTokenResponse rstr){
        throw new UnsupportedOperationException("Unsupported operation: containsChallenge");
    }
    
    /**
     * Contains Challenge
     * @return true if the RST contains Initial Negotiation/Challenge information
     * for a Multi-Message exchange.
     */
    public boolean containsChallenge(RequestSecurityToken rst){
        throw new UnsupportedOperationException("Unsupported operation: containsChallenge");
    }
    
    protected abstract Token createSAMLAssertion(String appliesTo, String tokenType, String keyType, String assertionId, String issuer, Map claimedAttrs, IssuedTokenContext context) throws WSTrustException;
    
  /*protected abstract boolean isAuthorized(Subject subject, String appliesTo, String tokenType, String keyType);
    
    protected abstract Map getClaimedAttributes(Subject subject, String appliesTo, String tokenType);
    
    protected byte[] createSecretKey(RequestSecurityToken rst)throws WSTrustException
    {
        // get key information
        int keySize = (int)rst.getKeySize();
        if (keySize < 1){
            keySize = DEFAULT_KEY_SIZE;
        }
        URI keyType = rst.getKeyType();
        URI alg = rst.getComputedKeyAlgorithm();
  
        Entropy entropy = rst.getEntropy();
        BinarySecret bs = entropy.getBinarySecret();
        byte[] nonce = bs.getRawValue();
  
        byte[] key = null;
  
        if (alg == null){
            key = nonce;
        } else if(alg.toString().equals(WSTrustConstants.CK_PSHA1)){
            try {
                  key = SecurityUtil.P_SHA1(nonce,null, keySize/8 );
                } catch (Exception ex) {
                      throw new WSTrustException(ex.getMessage(), ex);
                }
        } else {
            throw new WSTrustException("Unsupported key computation algorithm: " + alg.toString());
        }
  
        return key;
    }*/
    
    private long currentTime;
    private Lifetime createLifetime() {
        Calendar c = new GregorianCalendar();
        int offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        synchronized (calendarFormatter) {
            calendarFormatter.setTimeZone(c.getTimeZone());
            
            // always send UTC/GMT time
            long beforeTime = c.getTimeInMillis();
            currentTime = beforeTime - offset;
            c.setTimeInMillis(currentTime);
            
            AttributedDateTime created = new AttributedDateTime();
            created.setValue(calendarFormatter.format(c.getTime()));
            
            AttributedDateTime expires = new AttributedDateTime();
            c.setTimeInMillis(currentTime + config.getIssuedTokenTimeout());
            expires.setValue(calendarFormatter.format(c.getTime()));
            
            Lifetime lifetime = eleFac.createLifetime(created, expires);
            
            return lifetime;
        }
    }
    
    private SecurityTokenReference createSecurityTokenReference(String id, String tokenType){
        String valueType = null;
         if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
                WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                valueType = MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE;
            } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                valueType = MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE;
            }
        KeyIdentifier ref = eleFac.createKeyIdentifier(valueType, null);
        ref.setValue(id);
        return eleFac.createSecurityTokenReference(ref);
    }
}
