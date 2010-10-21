/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.api.security.secconv.client.SCTokenConfiguration;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.secconv.impl.SecurityContextTokenInfoImpl;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import com.sun.xml.ws.security.secconv.logging.LogStringsMessages;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedTokenCancelled;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WSSCClientContract {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    private static final int DEFAULT_KEY_SIZE = 256;
    private WSSCVersion wsscVer = WSSCVersion.WSSC_10;
    private WSTrustVersion wsTrustVer = WSTrustVersion.WS_TRUST_10;
    
    
    /**
     * Handle an RSTR returned by the Issuer and update Token information into the
     * IssuedTokenContext.
     */
    public void handleRSTR(
            final RequestSecurityToken rst, final RequestSecurityTokenResponse rstr, final IssuedTokenContext context) throws WSSecureConversationException {        
        if(!context.getSecurityPolicy().isEmpty()){
            SCTokenConfiguration sctConfig = (SCTokenConfiguration)context.getSecurityPolicy().get(0);
            wsscVer = WSSCVersion.getInstance(sctConfig.getProtocol());        
        }
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13_NS_URI)){
            wsTrustVer = WSTrustVersion.WS_TRUST_13;
        }
        if (rst.getRequestType().toString().equals(wsTrustVer.getIssueRequestTypeURI())){
            // ToDo
            //final AppliesTo requestAppliesTo = rst.getAppliesTo();
            //final AppliesTo responseAppliesTo = rstr.getAppliesTo();
            
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
            
            if(securityToken == null && proofToken == null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0002_NULL_TOKEN());
                throw new WSSecureConversationException(LogStringsMessages.WSSC_0002_NULL_TOKEN());
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
        }if (rst.getRequestType().toString().equals(wsTrustVer.getRenewRequestTypeURI())){
            final RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
            // RequestedProofToken
            final RequestedProofToken proofToken = rstr.getRequestedProofToken();
            
            // Obtain the secret key for the context
            final byte[] key = getKey(rstr, proofToken, rst);
                        
            //get the creation time and expires time and set it in the context
            setLifetime(rstr, context);
            if (securityToken != null){
                context.setSecurityToken(securityToken.getToken());
            }
            SecurityContextTokenInfo sctInfo = null;
            if(context.getSecurityContextTokenInfo() == null){
                sctInfo = new SecurityContextTokenInfoImpl();                
            }else{
                sctInfo = context.getSecurityContextTokenInfo();
            }
            sctInfo.setIdentifier(((SecurityContextToken)context.getSecurityToken()).getIdentifier().toString());
            sctInfo.setInstance(((SecurityContextToken)context.getSecurityToken()).getInstance());
            sctInfo.setExternalId(((SecurityContextToken)context.getSecurityToken()).getWsuId());
            if(key != null){
                sctInfo.addInstance(((SecurityContextToken)context.getSecurityToken()).getInstance(), key);                
            }            
            context.setSecurityContextTokenInfo(sctInfo);
        }else if (rst.getRequestType().toString().equals(wsTrustVer.getCancelRequestTypeURI())){
            
            // Check if the rstr contains the RequestTedTokenCancelled element
            // if yes cleanup the IssuedTokenContext accordingly
            final RequestedTokenCancelled cancelled = rstr.getRequestedTokenCancelled();
            if(cancelled!=null){
                //context.setSecurityToken(null);
                context.setProofKey(null);
            }
        }
        
    }

   /**
     * Handle an RSTRC returned by the Issuer and update Token information into the
     * IssuedTokenContext.
     */
    public void handleRSTRC(
            final RequestSecurityToken rst, final RequestSecurityTokenResponseCollection rstrc, final IssuedTokenContext context) throws WSSecureConversationException {
        List<RequestSecurityTokenResponse> rstrList = rstrc.getRequestSecurityTokenResponses();
        Iterator rstrIterator = rstrList.iterator();
        RequestSecurityTokenResponse rstr;
        while(rstrIterator.hasNext()){
            rstr = (RequestSecurityTokenResponse)rstrIterator.next();
            this.handleRSTR(rst, rstr, context);
        }
    }
    
    private byte[] getKey(final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst) throws UnsupportedOperationException, WSSecureConversationException, WSSecureConversationException, UnsupportedOperationException {
        byte[] key = null;
        if (proofToken != null){
            final String proofTokenType = proofToken.getProofTokenType();
            if (RequestedProofToken.COMPUTED_KEY_TYPE.equals(proofTokenType)){
                key = computeKey(rstr, proofToken, rst);
            } else if (RequestedProofToken.TOKEN_REF_TYPE.equals(proofTokenType)){
                //ToDo
                throw new UnsupportedOperationException("To Do");
            } else if (RequestedProofToken.ENCRYPTED_KEY_TYPE.equals(proofTokenType)){
                //ToDo
                throw new UnsupportedOperationException("To Do");
            } else if (RequestedProofToken.BINARY_SECRET_TYPE.equals(proofTokenType)){
                final BinarySecret binarySecret = proofToken.getBinarySecret();
                key = binarySecret.getRawValue();
            } else{
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0003_INVALID_PROOFTOKEN(proofTokenType));
                throw new WSSecureConversationException(LogStringsMessages.WSSC_0003_INVALID_PROOFTOKEN(proofTokenType));
            }
        }
        return key;
    }
    
    private void setLifetime(final RequestSecurityTokenResponse rstr, final IssuedTokenContext context){
        
        // Get Created and Expires from Lifetime
        final Lifetime lifetime = rstr.getLifetime();
        final AttributedDateTime created = lifetime.getCreated();
        final AttributedDateTime expires = lifetime.getExpires();

        // populate the IssuedTokenContext
        if (created != null){
            context.setCreationTime(WSTrustUtil.parseAttributedDateTime(created));
        }else{
            // set the current time as specified in the spec.
            context.setCreationTime(new Date());
        }
        if (expires != null){
            context.setExpirationTime(WSTrustUtil.parseAttributedDateTime(expires));
        }
    }
    
    private byte[] computeKey(final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst) throws WSSecureConversationException, UnsupportedOperationException {
        // get ComputeKey algorithm URI, client entropy, server entropy and compute
        // the SecretKey
        final URI computedKey = proofToken.getComputedKey();
        final Entropy clientEntropy = rst.getEntropy();
        final Entropy serverEntropy = rstr.getEntropy();
        final BinarySecret clientBS = clientEntropy.getBinarySecret();
        final BinarySecret serverBS = serverEntropy.getBinarySecret();
        byte [] clientEntr = null;
        byte [] serverEntr = null;
        if(clientBS!=null){
            clientEntr = clientBS.getRawValue();
        }
        if(serverBS!=null){
            serverEntr = serverBS.getRawValue();
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
                    LogStringsMessages.WSSC_0005_COMPUTED_KEYSIZE(keySize, DEFAULT_KEY_SIZE));
        }
        if(computedKey.toString().equals(wsTrustVer.getCKPSHA1algorithmURI())){
            try {
                key = SecurityUtil.P_SHA1(clientEntr,serverEntr, keySize/8);
            } catch (Exception ex) {
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0006_UNABLETOEXTRACT_KEY(), ex);
                throw new WSSecureConversationException(LogStringsMessages.WSSC_0006_UNABLETOEXTRACT_KEY(), ex);
            }
        } else {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0026_UNSUPPORTED_COMPUTED_KEY(computedKey));
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0026_UNSUPPORTED_COMPUTED_KEY_E(computedKey));
        }
        return key;
    }
    
    /**
     * Handle an RSTR returned by the Issuer and Respond to the Challenge
     *
     */
    public RequestSecurityTokenResponse handleRSTRForNegotiatedExchange(
            final RequestSecurityToken rst, final RequestSecurityTokenResponse rstr, final IssuedTokenContext context) throws WSSecureConversationException {
        return null;
    }
    
    /**
     * Create an RSTR for a client initiated IssuedTokenContext establishment,
     * for example a Client Initiated WS-SecureConversation context.
     *
     */
    public RequestSecurityTokenResponse createRSTRForClientInitiatedIssuedTokenContext(final AppliesTo scopes,final IssuedTokenContext context) throws WSSecureConversationException {
        final WSSCElementFactory eleFac = WSSCElementFactory.newInstance();
        
        final byte[] secret = WSTrustUtil.generateRandomSecret(DEFAULT_KEY_SIZE);
        final BinarySecret binarySecret = eleFac.createBinarySecret(secret, this.wsTrustVer.getSymmetricKeyTypeURI());
        
        final RequestedProofToken proofToken = eleFac.createRequestedProofToken();
        proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
        proofToken.setBinarySecret(binarySecret);
        
        final SecurityContextToken token = WSTrustUtil.createSecurityContextToken(eleFac);
        final RequestedSecurityToken rst = eleFac.createRequestedSecurityToken(token);
        
        final RequestSecurityTokenResponse rstr = eleFac.createRSTR();
        rstr.setAppliesTo(scopes);
        rstr.setRequestedSecurityToken(rst);
        rstr.setRequestedProofToken(proofToken);
        
        context.setSecurityToken(token);
        context.setProofKey(secret);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_0007_CREATED_RSTR(rstr.toString()));
        }
        return rstr;
    }
    
    /**
     * Contains Challenge
     * @return true if the RSTR contains a SignChallenge/BinaryExchange or
     *  some other custom challenge recognized by this implementation.
     */
    public boolean containsChallenge(final RequestSecurityTokenResponse rstr) {
        return false;
    }
    
    /**
     * Return the &lt;wst:ComputedKey&gt; URI if any inside the RSTR, null otherwise
     */
    public URI getComputedKeyAlgorithmFromProofToken(final RequestSecurityTokenResponse rstr) {
        return null;
    }
}
