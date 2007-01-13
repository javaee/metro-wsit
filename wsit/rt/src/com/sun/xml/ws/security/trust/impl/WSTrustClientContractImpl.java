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

/*
 * WSTrustClientContractImpl.java
 *
 * Created on February 19, 2006, 8:14 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustClientContract;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

/**
 *
 * @author WS-Trust-Implementation team
 */
public class WSTrustClientContractImpl implements WSTrustClientContract {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    //private static final int DEFAULT_KEY_SIZE = 256;
    
    private final Configuration config;
    
    private static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
    
    /**
     * Creates a new instance of WSTrustClientContractImpl
     */
    public WSTrustClientContractImpl(Configuration config) {
        this.config = config;
    }
    
    /**
     * Handle an RSTR returned by the Issuer and update Token information into the
     * IssuedTokenContext.
     */
    public void handleRSTR(
            final RequestSecurityToken rst, final RequestSecurityTokenResponse rstr, final IssuedTokenContext context) throws WSTrustException{
        if (rst.getRequestType().toString().equals(WSTrustConstants.ISSUE_REQUEST)){
            
            //AppliesTo requestAppliesTo = rst.getAppliesTo();
            //AppliesTo responseAppliesTo = rstr.getAppliesTo();
            
            final RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
            
            // Requested References
            final RequestedAttachedReference attachedRef = rstr.getRequestedAttachedReference();
            final RequestedUnattachedReference unattachedRef = rstr.getRequestedUnattachedReference();
            
            // RequestedProofToken
            final RequestedProofToken proofToken = rstr.getRequestedProofToken();
            
            // Obtain the secret key for the context
            final byte[] key = getKey(rstr, proofToken, rst);
            
            if(key != null){
                context.setProofKey(key);
            }
            
            //get the creation time and expires time and set it in the context
            setLifetime(rstr, context);
            
            // if securityToken == null and proofToken == null
            // throw exception
            if(securityToken == null && proofToken == null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0018_TOKENS_NULL());
                throw new WSTrustException(
                        LogStringsMessages.WST_0018_TOKENS_NULL());
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
        }
    }
    
    /**
     * Handle an RSTR returned by the Issuer and Respond to the Challenge
     *
     */
    public RequestSecurityTokenResponse handleRSTRForNegotiatedExchange(
            final RequestSecurityToken rst, final RequestSecurityTokenResponse rstr, final IssuedTokenContext context) throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: handleRSTRForNegotiatedExchange");
    }
    
    /**
     * Create an RSTR for a client initiated IssuedTokenContext establishment,
     * for example a Client Initiated WS-SecureConversation context.
     *
     */
    public RequestSecurityTokenResponse createRSTRForClientInitiatedIssuedTokenContext(final AppliesTo scopes,final IssuedTokenContext context) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: createRSTRForClientInitiatedIssuedTokenContext");
    }
    
    /**
     * Contains Challenge
     * @return true if the RSTR contains a SignChallenge/BinaryExchange or
     *  some other custom challenge recognized by this implementation.
     */
    public boolean containsChallenge(final RequestSecurityTokenResponse rstr){
        throw new UnsupportedOperationException("Unsupported operation: containsChallenge");
    }
    
    /**
     * Return the &lt;wst:ComputedKey&gt; URI if any inside the RSTR, null otherwise
     */
    public URI getComputedKeyAlgorithmFromProofToken(final RequestSecurityTokenResponse rstr){
        throw new UnsupportedOperationException("Unsupported operation: getComputedKeyAlgorithmFromProofToken");
    }
    
    private void setLifetime(final RequestSecurityTokenResponse rstr, final IssuedTokenContext context) throws WSTrustException {
        
        // Get Created and Expires from Lifetime
        try{
            final Lifetime lifetime = rstr.getLifetime();
            final AttributedDateTime created = lifetime.getCreated();
            final AttributedDateTime expires = lifetime.getExpires();
            final Date dateCreated = calendarFormatter.parse(created.getValue());
            final Date dateExpires = calendarFormatter.parse(expires.getValue());
            
            // populate the IssuedTokenContext
            context.setCreationTime(dateCreated);
            context.setExpirationTime(dateExpires);
        }catch(ParseException ex){
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }
    
    private byte[] getKey(final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst)
    throws WSTrustException {
        byte[] key = null;
        if (proofToken != null){
            final String proofTokenType = proofToken.getProofTokenType();
            if (RequestedProofToken.COMPUTED_KEY_TYPE.equals(proofTokenType)){
                key = computeKey(rstr, proofToken, rst);
            } else if (RequestedProofToken.TOKEN_REF_TYPE.equals(proofTokenType)){
                //ToDo
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType));
                throw new WSTrustException( LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType));
            } else if (RequestedProofToken.ENCRYPTED_KEY_TYPE.equals(proofTokenType)){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType));
                throw new WSTrustException( LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType));
            } else if (RequestedProofToken.BINARY_SECRET_TYPE.equals(proofTokenType)){
                final BinarySecret binarySecret = proofToken.getBinarySecret();
                key = binarySecret.getRawValue();
            } else{
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0019_INVALID_PROOF_TOKEN_TYPE(proofTokenType));
                throw new WSTrustException( LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType));
            }
        }
        return key;
    }
    
    private byte[] computeKey(final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst) throws WSTrustException, UnsupportedOperationException {
        // get ComputeKey algorithm URI, client entropy, server entropy and compute
        // the SecretKey
        final URI computedKey = proofToken.getComputedKey();
        final Entropy clientEntropy = rst.getEntropy();
        final Entropy serverEntropy = rstr.getEntropy();
        final BinarySecret clientBinarySecret = clientEntropy.getBinarySecret();
        final BinarySecret serverBinarySecret = serverEntropy.getBinarySecret();
        byte [] clientEntropyBytes = null;
        byte [] serverEntropyBytes = null;
        if(clientBinarySecret!=null){
            clientEntropyBytes = clientBinarySecret.getRawValue();
        }
        if(serverBinarySecret!=null){
            serverEntropyBytes = serverBinarySecret.getRawValue();
        }
        
        final int keySize = (int)rstr.getKeySize()/8;
        byte[] key = null;
        if(computedKey.toString().equals(WSTrustConstants.CK_PSHA1)){
            try {
                key = SecurityUtil.P_SHA1(clientEntropyBytes,serverEntropyBytes, keySize);
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0037_ERROR_COMPUTING_KEY(), ex);
                throw new WSTrustException(LogStringsMessages.WST_0037_ERROR_COMPUTING_KEY(), ex);
            }
        } else {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0026_INVALID_CK_ALGORITHM(computedKey));
            throw new WSTrustException(LogStringsMessages.WST_0026_INVALID_CK_ALGORITHM(computedKey));
        }
        return key;
    }
}
