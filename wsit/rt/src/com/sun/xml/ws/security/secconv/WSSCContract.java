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

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.impl.policy.PolicyUtil;
import com.sun.xml.ws.security.impl.policy.Trust10;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Constants;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.secconv.impl.SecurityContextTokenInfoImpl;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import com.sun.xml.ws.security.secconv.logging.LogStringsMessages;
import javax.xml.bind.JAXBElement;

public class WSSCContract implements WSTrustContract   {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    private Configuration config;
    
    private long currentTime;
    
    private static WSSCElementFactory eleFac = WSSCElementFactory.newInstance();
    
    private static final int DEFAULT_KEY_SIZE = 128;
    
    // ToDo: Should read from the configuration
    private static final long TIMEOUT = 36000000;
    private static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'",Locale.getDefault());
    
    public WSSCContract(){
        
    }
    
    public WSSCContract(Configuration config){
        init(config);
    }
    
    public void init(final Configuration config){
        this.config = config;
    }
    
    /** Issue a SecurityContextToken */
    public RequestSecurityTokenResponse issue(
            final RequestSecurityToken request, final IssuedTokenContext context, final SecureConversationToken scToken) throws WSSecureConversationException {
        // TokenType and context
        URI tokenType = null;
        URI con = null;
        URI computeKeyAlgo = null;
        try {
            tokenType = URI.create(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE);
            final String conStr = request.getContext();
            if (conStr != null)
                con = new URI(conStr);
            computeKeyAlgo = URI.create(WSTrustConstants.CK_PSHA1);
        } catch (URISyntaxException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0008_URISYNTAX_EXCEPTION(request.getContext()), ex);
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0008_URISYNTAX_EXCEPTION(request.getContext()), ex);
        }
        
        // AppliesTo
        final AppliesTo scopes = request.getAppliesTo();
        
        Entropy serverEntropy = null;
        final RequestedProofToken proofToken = eleFac.createRequestedProofToken();
        
        // Get client entropy
        byte[] clientEntr = null;
        final Entropy clientEntropy = request.getEntropy();
        if (clientEntropy != null){
            final BinarySecret clientBS = clientEntropy.getBinarySecret();
            if (clientBS == null){
                //ToDo
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE,
                            LogStringsMessages.WSSC_0009_CLIENT_ENTROPY_VALUE("null"));
                }
            }else {
                clientEntr = clientBS.getRawValue();
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE,
                            LogStringsMessages.WSSC_0009_CLIENT_ENTROPY_VALUE(clientEntropy.toString()));
                }
            }
        }
        
        Trust10 trust10 = null;
        SymmetricBinding symBinding = null;
        
        final NestedPolicy wsPolicy = scToken.getBootstrapPolicy();
        final AssertionSet assertionSet = wsPolicy.getAssertionSet();
        for(PolicyAssertion policyAssertion : assertionSet){
            if(PolicyUtil.isTrust10(policyAssertion)){
                trust10 = (Trust10)policyAssertion;
            }else if(PolicyUtil.isSymmetricBinding(policyAssertion)){
                symBinding = (SymmetricBinding)policyAssertion;
            }
        }
        boolean reqServerEntr = true;
        if(trust10 != null){
            final Set trustReqdProps = trust10.getRequiredProperties();
            reqServerEntr = trustReqdProps.contains(Constants.REQUIRE_SERVER_ENTROPY);
        }
        
        if((!reqServerEntr) && (clientEntropy == null)){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0010_CLIENT_ENTROPY_CANNOT_NULL());
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0010_CLIENT_ENTROPY_CANNOT_NULL());
        }
        
        int keySize = (int)request.getKeySize();
        if (keySize < 1 && symBinding!=null ){
            final AlgorithmSuite algoSuite = symBinding.getAlgorithmSuite();
            keySize = algoSuite.getMinSKLAlgorithm();
        }
        if (keySize < 1){
            keySize = DEFAULT_KEY_SIZE;
        }
        if (log.isLoggable(Level.FINE)) {
        log.log(Level.FINE,
                LogStringsMessages.WSSC_0011_KEY_SIZE_VALUE(keySize, this.DEFAULT_KEY_SIZE));
        }
        
        byte[] secret = WSTrustUtil.generateRandomSecret(keySize/8);
        final String proofTokenType = (clientEntr == null ||clientEntr.length ==0)
        ? BinarySecret.SYMMETRIC_KEY_TYPE :BinarySecret.NONCE_KEY_TYPE;
        
        if(reqServerEntr){
            final BinarySecret serverBS = eleFac.createBinarySecret(secret, proofTokenType);
            if (proofTokenType.equals(BinarySecret.NONCE_KEY_TYPE)){
                serverEntropy = eleFac.createEntropy(serverBS);
                proofToken.setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);
                proofToken.setComputedKey(computeKeyAlgo);
                
                // compute the secret key
                try {
                    secret = SecurityUtil.P_SHA1(clientEntr, secret, keySize/8);
                } catch (Exception ex){
                    log.log(Level.SEVERE,
                            LogStringsMessages.WSSC_0012_COMPUTE_SECKEY(), ex);
                    throw new WSSecureConversationException(LogStringsMessages.WSSC_0012_COMPUTE_SECKEY(), ex);
                }
                
            } else {
                proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
                proofToken.setBinarySecret(serverBS);
            }
        }else if (clientEntropy != null){
            secret = clientEntr;
            proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
            proofToken.setBinarySecret(clientEntropy.getBinarySecret());
        }
        
        // Create Security Context and SecurityContextToken
        final SecurityContextToken token = WSTrustUtil.createSecurityContextToken(eleFac);
        final RequestedSecurityToken rst = eleFac.createRequestedSecurityToken(token);
        
        // Create references
        final SecurityTokenReference attachedReference = createSecurityTokenReference(token.getWsuId(),false);
        final RequestedAttachedReference rar = eleFac.createRequestedAttachedReference(attachedReference);
        final SecurityTokenReference unattachedRef = createSecurityTokenReference(token.getIdentifier().toString(), true);
        final RequestedUnattachedReference rur = eleFac.createRequestedUnattachedReference(unattachedRef);
        
        // Create Lifetime
        final Lifetime lifetime = createLifetime();
        
        RequestSecurityTokenResponse response = null;
        try {
            response =
                    eleFac.createRSTRForIssue(tokenType, con, rst, scopes, rar, rur, proofToken, serverEntropy, lifetime);
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0013_CANNOT_CREATE_RSTR_RESPONSE(), ex);
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0013_CANNOT_CREATE_RSTR_RESPONSE(), ex);
        }
        
        final Session session =
                SessionManager.getSessionManager().createSession(token.getIdentifier().toString());
        log.fine("Creating session for : "  + token.getIdentifier());
        
        // Populate the IssuedTokenContext
        context.setSecurityToken(token);
        context.setAttachedSecurityTokenReference(attachedReference);
        context.setUnAttachedSecurityTokenReference(unattachedRef);
        context.setProofKey(secret);
        context.setCreationTime(new Date(currentTime));
        context.setExpirationTime(new Date(currentTime + TIMEOUT));
        
        final SecurityContextTokenInfo sctinfo =
                new SecurityContextTokenInfoImpl();
        sctinfo.setIdentifier(token.getIdentifier().toString());
        sctinfo.setExternalId(token.getWsuId());
        sctinfo.addInstance(null, secret);
        
        sctinfo.setCreationTime(new Date(currentTime));
        sctinfo.setExpirationTime(new Date(currentTime + TIMEOUT));
        
        session.setSecurityInfo(sctinfo);
        
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_0014_RSTR_RESPONSE(elemToString(response)));
        }
        return response;
    }
    
    
    /** Issue a Collection of Token(s) possibly for different scopes */
    public RequestSecurityTokenResponseCollection issueMultiple(
            final RequestSecurityToken request, final IssuedTokenContext context)throws WSSecureConversationException {
        return null;
    }
    
    /** Renew a SecurityContextToken */
    public RequestSecurityTokenResponse renew(
            final RequestSecurityToken request, final IssuedTokenContext context)
            throws WSSecureConversationException {
        return null;
    }
    
    /** Cancel a SecurityContextToken */
    public RequestSecurityTokenResponse cancel(
            final RequestSecurityToken request, final IssuedTokenContext context, final Map issuedTokCtxMap)
            throws WSSecureConversationException {
        final CancelTarget cancelTgt = request.getCancelTarget();
        final SecurityTokenReference str = cancelTgt.getSecurityTokenReference();
        String id = null;
        final Reference ref = str.getReference();
        if (ref.getType().equals("Reference")){
            id = ((DirectReference)ref).getURIAttr().toString();
        }
        
        final IssuedTokenContext cxt = (IssuedTokenContext)issuedTokCtxMap.get(id);
        if (cxt == null || cxt.getSecurityToken() == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0015_UNKNOWN_CONTEXT(id));
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0015_UNKNOWN_CONTEXT(id));
        }
        
        final RequestSecurityTokenResponse rstr = eleFac.createRSTRForCancel();
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_0014_RSTR_RESPONSE(elemToString(rstr)));
        }
        return rstr;
    }
    
    /** Validate a SecurityContextToken */
    public RequestSecurityTokenResponse validate(
            final RequestSecurityToken request, final IssuedTokenContext context)
            throws WSSecureConversationException {
        return null;
    }
    
    /**
     * handle an unsolicited RSTR like in the case of
     * Client Initiated Secure Conversation.
     */
    public void handleUnsolicited(
            final RequestSecurityTokenResponse rstr, final IssuedTokenContext context)
            throws WSSecureConversationException {
        final AppliesTo scope = rstr.getAppliesTo();
        final RequestedSecurityToken rqSecToken = rstr.getRequestedSecurityToken();
        final Token token = rqSecToken.getToken();
        final RequestedProofToken rqProofToken = rstr.getRequestedProofToken();
        final String proofTokenType = rqProofToken.getProofTokenType();
        if(proofTokenType.equals(RequestedProofToken.BINARY_SECRET_TYPE)){
            final BinarySecret binarySecret = rqProofToken.getBinarySecret();
            if(binarySecret.getType().equals(BinarySecret.SYMMETRIC_KEY_TYPE)){
                final byte [] secret = binarySecret.getRawValue();
                context.setProofKey(secret);
            }
        }else if(proofTokenType.equals(RequestedProofToken.ENCRYPTED_KEY_TYPE)){
            //ToDo
        }
        
        context.setSecurityToken(token);
        final long curTime = System.currentTimeMillis();
        final Date creationTime = new Date(curTime);
        final Date expirationTime = new Date(curTime + TIMEOUT);
        context.setCreationTime(creationTime);
        context.setExpirationTime(expirationTime);
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER,
                    LogStringsMessages.WSSC_1003_SETTING_TIMES(creationTime.toString(), expirationTime.toString()));
        }
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
     * Contains Challenge
     * @return true if the RST contains Initial Negotiation/Challenge information
     * for a Multi-Message exchange.
     */
    public boolean containsChallenge(final RequestSecurityToken rst) {
        return false;
    }
    
    private SecurityTokenReference createSecurityTokenReference(final String id, final boolean unattached){
        final String uri = (unattached?id:"#"+id);
        final Reference ref = eleFac.createDirectReference(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE, uri);
        return eleFac.createSecurityTokenReference(ref);
    }
    
    
    private Lifetime createLifetime() {
        final Calendar cal = new GregorianCalendar();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        if (cal.getTimeZone().inDaylightTime(cal.getTime())) {
            offset += cal.getTimeZone().getDSTSavings();
        }
        synchronized (calendarFormatter) {
            calendarFormatter.setTimeZone(cal.getTimeZone());
            
            // always send UTC/GMT time
            final long beforeTime = cal.getTimeInMillis();
            currentTime = beforeTime - offset;
            cal.setTimeInMillis(currentTime);
            
            final AttributedDateTime created = new AttributedDateTime();
            created.setValue(calendarFormatter.format(cal.getTime()));
            
            final AttributedDateTime expires = new AttributedDateTime();
            cal.setTimeInMillis(currentTime + TIMEOUT);
            expires.setValue(calendarFormatter.format(cal.getTime()));
            
            final Lifetime lifetime = eleFac.createLifetime(created, expires);
            
            return lifetime;
        }
    }
    
    private String elemToString(final RequestSecurityTokenResponse rstr){
        try {
            final javax.xml.bind.Marshaller marshaller = eleFac.getContext().createMarshaller();
            final JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final java.io.StringWriter writer = new java.io.StringWriter();
            marshaller.marshal(rstrElement, writer);
            return writer.toString();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0027_ERROR_MARSHAL_LOG());
            throw new RuntimeException(LogStringsMessages.WSSC_0027_ERROR_MARSHAL_LOG(), e);
        }
    }
    
}
