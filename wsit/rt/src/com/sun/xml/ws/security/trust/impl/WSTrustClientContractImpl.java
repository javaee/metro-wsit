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

import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.client.STSIssuedTokenConfiguration;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.WSTrustClientContract;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.api.security.trust.Status;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    
    private static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'",Locale.getDefault());
    
    /**
     * Creates a new instance of WSTrustClientContractImpl
     */
    public WSTrustClientContractImpl() {
        //this.config = config;
    }
    
    /**
     * Handle an RSTR returned by the Issuer and update Token information into the
     * IssuedTokenContext.
     */
    public void handleRSTR(
            final BaseSTSRequest request, final BaseSTSResponse response, final IssuedTokenContext context) throws WSTrustException{
        WSTrustVersion wstVer = WSTrustVersion.getInstance(((STSIssuedTokenConfiguration)context.getSecurityPolicy().get(0)).getProtocol());
        RequestSecurityToken rst = (RequestSecurityToken)request;  
        RequestSecurityTokenResponse rstr = null;
        if (response instanceof RequestSecurityTokenResponse){
            rstr = (RequestSecurityTokenResponse)response;
        }else if (response instanceof RequestSecurityTokenResponseCollection){
            rstr = ((RequestSecurityTokenResponseCollection)response).getRequestSecurityTokenResponses().get(0);
        }
        if (rst.getRequestType().toString().equals(wstVer.getIssueRequestTypeURI())){
            
            String appliesTo = null;
            AppliesTo requestAppliesTo = rst.getAppliesTo();
            if (requestAppliesTo != null){
                appliesTo = WSTrustUtil.getAppliesToURI(requestAppliesTo);
            }
            //AppliesTo responseAppliesTo = rstr.getAppliesTo();
            
            final RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
            
            // Requested References
            final RequestedAttachedReference attachedRef = rstr.getRequestedAttachedReference();
            final RequestedUnattachedReference unattachedRef = rstr.getRequestedUnattachedReference();
            
            // RequestedProofToken
            final RequestedProofToken proofToken = rstr.getRequestedProofToken();
            
            // Obtain the secret key for the context
            final byte[] key = getKey(wstVer, rstr, proofToken, rst, appliesTo);
            
            if(key != null){
                context.setProofKey(key);
            }
            
            //get the creation time and expires time and set it in the context
            setLifetime(rstr, context);
            
            // if securityToken == null and proofToken == null
            // throw exception
            if(securityToken == null && proofToken == null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0018_TOKENS_NULL(appliesTo));
                throw new WSTrustException(
                        LogStringsMessages.WST_0018_TOKENS_NULL(appliesTo));
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
        }else if (rst.getRequestType().toString().equals(wstVer.getValidateRequestTypeURI())){
            Status status = rstr.getStatus();
            context.getOtherProperties().put(IssuedTokenContext.STATUS, status);
            final RequestedSecurityToken securityToken = rstr.getRequestedSecurityToken();
            if (securityToken != null){
                 context.setSecurityToken(securityToken.getToken());
            }
        }
    }
    
    /**
     * Handle an RSTR returned by the Issuer and Respond to the Challenge
     *
     */
    public BaseSTSResponse handleRSTRForNegotiatedExchange(
            final BaseSTSRequest request, final BaseSTSResponse response, final IssuedTokenContext context) throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: handleRSTRForNegotiatedExchange");
    }
    
    /**
     * Create an RSTR for a client initiated IssuedTokenContext establishment,
     * for example a Client Initiated WS-SecureConversation context.
     *
     */
    public BaseSTSResponse createRSTRForClientInitiatedIssuedTokenContext(final AppliesTo scopes,final IssuedTokenContext context) throws WSTrustException {
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
            if (lifetime != null){
                final AttributedDateTime created = lifetime.getCreated();
                final AttributedDateTime expires = lifetime.getExpires();
                synchronized (calendarFormatter){
                    final Date dateCreated = calendarFormatter.parse(created.getValue());
                    final Date dateExpires = calendarFormatter.parse(expires.getValue());
                
                    // populate the IssuedTokenContext
                    context.setCreationTime(dateCreated);
                    context.setExpirationTime(dateExpires);
                }
            }
        }catch(ParseException ex){
            throw new WSTrustException(ex.getMessage(), ex);
        }
    }
    
    private byte[] getKey(final WSTrustVersion wstVer, final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst, final String appliesTo)
    throws WSTrustException {
        byte[] key = null;
        if (proofToken != null){
            final String proofTokenType = proofToken.getProofTokenType();
            if (RequestedProofToken.COMPUTED_KEY_TYPE.equals(proofTokenType)){
                key = computeKey(wstVer, rstr, proofToken, rst);
            } else if (RequestedProofToken.TOKEN_REF_TYPE.equals(proofTokenType)){
                //ToDo
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType, appliesTo));
                throw new WSTrustException( LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType, appliesTo));
            } else if (RequestedProofToken.ENCRYPTED_KEY_TYPE.equals(proofTokenType)){
                // ToDo
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType, appliesTo));
                throw new WSTrustException( LogStringsMessages.WST_0001_UNSUPPORTED_PROOF_TOKEN_TYPE(proofTokenType, appliesTo));
            } else if (RequestedProofToken.BINARY_SECRET_TYPE.equals(proofTokenType)){
                final BinarySecret binarySecret = proofToken.getBinarySecret();
                key = binarySecret.getRawValue();
            } else{
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0019_INVALID_PROOF_TOKEN_TYPE(proofTokenType, appliesTo));
                throw new WSTrustException( LogStringsMessages.WST_0019_INVALID_PROOF_TOKEN_TYPE(proofTokenType, appliesTo));
            }
        }else{
            Entropy clientEntropy = rst.getEntropy();
            if (clientEntropy != null){
                BinarySecret bs = clientEntropy.getBinarySecret();
                if (bs != null){
                    key = bs.getRawValue();
                }
            }
        }
        return key;
    }
    
    private byte[] computeKey(WSTrustVersion wstVer, final RequestSecurityTokenResponse rstr, final RequestedProofToken proofToken, final RequestSecurityToken rst) throws WSTrustException, UnsupportedOperationException {
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
        
        int keySize = (int)rstr.getKeySize()/8;
        if (keySize == 0){
            keySize = (int)rst.getKeySize()/8;
        }
        byte[] key = null;
        if(computedKey.toString().equals(wstVer.getCKPSHA1algorithmURI())){
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
            throw new WSTrustException(LogStringsMessages.WST_0026_INVALID_CK_ALGORITHM_E(computedKey));
        }
        return key;
    }
}
