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


package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustClientContract;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedTokenCancelled;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;

public class WSSCClientContract implements WSTrustClientContract{
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    private Configuration config;
    private static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
    
    private static final int DEFAULT_KEY_SIZE = 256;
    
    public WSSCClientContract(Configuration config) {
        this.config = config;
    }
    
    /**
     * Handle an RSTR returned by the Issuer and update Token information into the
     * IssuedTokenContext.
     */
    public void handleRSTR(
            RequestSecurityToken rst, RequestSecurityTokenResponse rstr, IssuedTokenContext context) throws WSSecureConversationException {
        if (rst.getRequestType().toString().equals(WSTrustConstants.ISSUE_REQUEST)){
            // ToDo
            AppliesTo requestAppliesTo = rst.getAppliesTo();
            AppliesTo responseAppliesTo = rstr.getAppliesTo();
            
            RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
            
            // Requested References
            RequestedAttachedReference attachedRef = rstr.getRequestedAttachedReference();
            RequestedUnattachedReference unattachedRef = rstr.getRequestedUnattachedReference();
            
            // RequestedProofToken
            RequestedProofToken proofToken = rstr.getRequestedProofToken();
            
            // Obtain the secret key for the context
            byte[] key = getKey(rstr, proofToken, rst);
            
            if(key != null){
                context.setProofKey(key);
            }
            
            //get the creation time and expires time and set it in the context
            setLifetime(rstr, context);
            
            if(securityToken == null && proofToken == null){
                log.log(Level.SEVERE,
                        "WSSC0002.null.token");
                throw new WSSecureConversationException(
                        "Invalid (null) Security Token and Proof Token");
            }
            
            if (securityToken != null){
                context.setSecurityToken(securityToken.getToken());
            }
            
            if(attachedRef != null){
                context.setAttachedSecurityTokenReference(attachedRef.getSTR());
            }
            
            if (unattachedRef != null){
                context.setUnAttachedSecurityTokenReference(unattachedRef.getSTR());
            }
            
            
        }else if (rst.getRequestType().toString().equals(WSTrustConstants.CANCEL_REQUEST)){
            
            // Check if the rstr contains the RequestTedTokenCancelled element
            // if yes cleanup the IssuedTokenContext accordingly
            RequestedTokenCancelled cancelled = rstr.getRequestedTokenCancelled();
            if(cancelled!=null){
                context.setSecurityToken(null);
                context.setProofKey(null);
            }
        }
        
    }
    
    private byte[] getKey(final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst) throws UnsupportedOperationException, WSSecureConversationException, WSSecureConversationException, UnsupportedOperationException {
        byte[] key = null;
        if (proofToken != null){
            String proofTokenType = proofToken.getProofTokenType();
            if (RequestedProofToken.COMPUTED_KEY_TYPE.equals(proofTokenType)){
                key = computeKey(rstr, proofToken, rst);
            } else if (RequestedProofToken.TOKEN_REF_TYPE.equals(proofTokenType)){
                //ToDo
                throw new UnsupportedOperationException("To Do");
            } else if (RequestedProofToken.ENCRYPTED_KEY_TYPE.equals(proofTokenType)){
                //ToDo
                throw new UnsupportedOperationException("To Do");
            } else if (RequestedProofToken.BINARY_SECRET_TYPE.equals(proofTokenType)){
                BinarySecret binarySecret = proofToken.getBinarySecret();
                key = binarySecret.getRawValue();
            } else{
                log.log(Level.SEVERE,
                        "WSSC0003.invalid.prooftoken", new Object[] {proofTokenType});
                throw new WSSecureConversationException("Invalid Proof Token Type: " + proofTokenType);
            }
        }
        return key;
    }
    
    private void setLifetime(final RequestSecurityTokenResponse rstr, final IssuedTokenContext context) throws WSSecureConversationException {
        
        // Get Created and Expires from Lifetime
        try{
            Lifetime lifetime = rstr.getLifetime();
            AttributedDateTime created = lifetime.getCreated();
            AttributedDateTime expires = lifetime.getExpires();
            Date dateCreated = calendarFormatter.parse(created.getValue());
            Date dateExpires = calendarFormatter.parse(expires.getValue());
            
            // populate the IssuedTokenContext
            context.setCreationTime(dateCreated);
            context.setExpirationTime(dateExpires);
        }catch(ParseException ex){
            log.log(Level.SEVERE,
                    "WSSC0004.parse.exception");
            throw new WSSecureConversationException(ex.getMessage(), ex);
        }
    }
    
    private byte[] computeKey(final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst) throws WSSecureConversationException, UnsupportedOperationException {
        // get ComputeKey algorithm URI, client entropy, server entropy and compute
        // the SecretKey
        URI computedKey = proofToken.getComputedKey();
        Entropy clientEntropy = rst.getEntropy();
        Entropy serverEntropy = rstr.getEntropy();
        BinarySecret clientBinarySecret = clientEntropy.getBinarySecret();
        BinarySecret serverBinarySecret = serverEntropy.getBinarySecret();
        byte [] clientEntropyBytes = null;
        byte [] serverEntropyBytes = null;
        if(clientBinarySecret!=null){
            clientEntropyBytes = clientBinarySecret.getRawValue();
        }
        if(serverBinarySecret!=null){
            serverEntropyBytes = serverBinarySecret.getRawValue();
        }
        byte[] key = null;
        int keySize = (int)rstr.getKeySize();
        if(keySize == 0){
            keySize = (int)rst.getKeySize();//get it from the request
        }
        if(keySize == 0){
            keySize = DEFAULT_KEY_SIZE;//key size is in bits
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    "WSSC0005.computed.keysize", new Object[] {keySize});
        }
        if(computedKey.toString().equals(WSTrustConstants.CK_PSHA1)){
            try {
                key = SecurityUtil.P_SHA1(clientEntropyBytes,serverEntropyBytes, keySize/8);
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        "WSSC0006.unabletoextract.key");
                throw new WSSecureConversationException(ex.getMessage(), ex);
            }
        } else {
            //ToDo
            throw new UnsupportedOperationException("Unsupported compute key algorithm:" + computedKey);
        }
        return key;
    }
    
    /**
     * Handle an RSTR returned by the Issuer and Respond to the Challenge
     *
     */
    public RequestSecurityTokenResponse handleRSTRForNegotiatedExchange(
            RequestSecurityToken rst, RequestSecurityTokenResponse rstr, IssuedTokenContext context) throws WSSecureConversationException {
        return null;
    }
    
    /**
     * Create an RSTR for a client initiated IssuedTokenContext establishment,
     * for example a Client Initiated WS-SecureConversation context.
     *
     */
    public RequestSecurityTokenResponse createRSTRForClientInitiatedIssuedTokenContext(AppliesTo scopes,IssuedTokenContext context) throws WSSecureConversationException {
        WSSCElementFactory eleFac = WSSCElementFactory.newInstance();
        
        byte[] secret = WSTrustUtil.generateRandomSecret(DEFAULT_KEY_SIZE);
        BinarySecret binarySecret = eleFac.createBinarySecret(secret, BinarySecret.SYMMETRIC_KEY_TYPE);
        
        RequestedProofToken proofToken = eleFac.createRequestedProofToken();
        proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
        proofToken.setBinarySecret(binarySecret);
        
        SecurityContextToken token = WSTrustUtil.createSecurityContextToken(eleFac);
        RequestedSecurityToken rst = eleFac.createRequestedSecurityToken(token);
        
        RequestSecurityTokenResponse rstr = eleFac.createRSTR();
        rstr.setAppliesTo(scopes);
        rstr.setRequestedSecurityToken(rst);
        rstr.setRequestedProofToken(proofToken);
        
        context.setSecurityToken(token);
        context.setProofKey(secret);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    "WSSC0007.created.rstr", new Object[] {rstr});
        }
        return rstr;
    }
    
    /**
     * Contains Challenge
     * @return true if the RSTR contains a SignChallenge/BinaryExchange or
     *  some other custom challenge recognized by this implementation.
     */
    public boolean containsChallenge(RequestSecurityTokenResponse rstr) {
        return false;
    }
    
    /**
     * Return the &lt;wst:ComputedKey&gt; URI if any inside the RSTR, null otherwise
     */
    public URI getComputedKeyAlgorithmFromProofToken(RequestSecurityTokenResponse rstr) {
        return null;
    }
}
