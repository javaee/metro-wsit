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
import java.util.Map;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import javax.xml.bind.JAXBElement;

public class WSSCContract implements WSTrustContract   {
    
    private static Logger log =
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
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
    
    public WSSCContract(){
        
    }
    
    public WSSCContract(Configuration config){
        init(config);
    }
    
    public void init(Configuration config){
        this.config = config;
    }
    
    /** Issue a SecurityContextToken */
    public RequestSecurityTokenResponse issue(
            RequestSecurityToken request, IssuedTokenContext context, SecureConversationToken scToken) throws WSSecureConversationException {
        // TokenType and context
        URI tokenType = null;
        URI con = null;
        URI computeKeyAlgo = null;
        try {
            tokenType = URI.create(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE);
            String conStr = request.getContext();
            if (conStr != null)
                con = new URI(conStr);
            computeKeyAlgo = URI.create(WSTrustConstants.CK_PSHA1);
        } catch (URISyntaxException ex){
            log.log(Level.SEVERE,
                    "WSSC0008.urisyntax.exception",
                    new Object[] {request.getContext()});
            throw new WSSecureConversationException(ex.getMessage(), ex);
        }
        
        // AppliesTo
        AppliesTo scopes = request.getAppliesTo();
        
        Entropy serverEntropy = null;
        RequestedProofToken proofToken = eleFac.createRequestedProofToken();
        
        // Get client entropy
        byte[] clientEntropyValue = null;
        Entropy clientEntropy = request.getEntropy();
        if (clientEntropy != null){
            BinarySecret clientBS = clientEntropy.getBinarySecret();
            if (clientBS == null){
                //ToDo
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE,
                            "WSSC0009.clientEntropy.value",
                            new Object[] {"null"});
                }
            }else {
                clientEntropyValue = clientBS.getRawValue();
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE,
                            "WSSC0009.clientEntropy.value",
                            new Object[] {clientEntropy.toString()});
                }
            }
        }
        
        Trust10 trust10 = null;
        SymmetricBinding symBinding = null;
        
        NestedPolicy wsPolicy = scToken.getBootstrapPolicy();
        AssertionSet assertionSet = wsPolicy.getAssertionSet();
        for(PolicyAssertion policyAssertion : assertionSet){
            if(PolicyUtil.isTrust10(policyAssertion)){
                trust10 = (Trust10)policyAssertion;
            }else if(PolicyUtil.isSymmetricBinding(policyAssertion)){
                symBinding = (SymmetricBinding)policyAssertion;
            }
        }
        boolean requireServerEntropy = true;
        if(trust10 != null){
            Set trustReqdProps = trust10.getRequiredProperties();
            requireServerEntropy = trustReqdProps.contains(Constants.REQUIRE_SERVER_ENTROPY);
        }
        
        if((!requireServerEntropy) && (clientEntropy == null)){
            log.log(Level.SEVERE,
                    "WSSC0010.clientEntropy.cannot.null");
            throw new WSSecureConversationException("client entropy cannot be null when RequireServerEntropy is not enabled");
        }
        
        int keySize = (int)request.getKeySize();
        if (keySize < 1 && symBinding!=null ){
            AlgorithmSuite algoSuite = symBinding.getAlgorithmSuite();
            keySize = algoSuite.getMinSKLAlgorithm();
        }
        if (keySize < 1){
            keySize = DEFAULT_KEY_SIZE;
        }
        if (log.isLoggable(Level.FINE)) {
        log.log(Level.FINE,
                "WSSC0011.keySize.value", new Object[] {keySize});
        }
        
        byte[] secret = WSTrustUtil.generateRandomSecret(keySize/8);
        String proofTokenType = (clientEntropyValue == null ||clientEntropyValue.length ==0)
        ? BinarySecret.SYMMETRIC_KEY_TYPE :BinarySecret.NONCE_KEY_TYPE;
        
        if(requireServerEntropy){
            BinarySecret serverBS = eleFac.createBinarySecret(secret, proofTokenType);
            if (proofTokenType.equals(BinarySecret.NONCE_KEY_TYPE)){
                serverEntropy = eleFac.createEntropy(serverBS);
                proofToken.setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);
                proofToken.setComputedKey(computeKeyAlgo);
                
                // compute the secret key
                try {
                    secret = SecurityUtil.P_SHA1(clientEntropyValue, secret, keySize/8);
                } catch (Exception ex){
                    log.log(Level.SEVERE,
                            "WSSC0012.compute.seckey", new Object[] {ex});
                    throw new WSSecureConversationException(ex.getMessage(), ex);
                }
                
            } else {
                proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
                proofToken.setBinarySecret(serverBS);
            }
        }else if (clientEntropy != null){
            secret = clientEntropyValue;
            proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
            proofToken.setBinarySecret(clientEntropy.getBinarySecret());
        }
        
        // Create Security Context and SecurityContextToken
        SecurityContextToken token = WSTrustUtil.createSecurityContextToken(eleFac);
        RequestedSecurityToken rst = eleFac.createRequestedSecurityToken(token);
        
        // Create references
        SecurityTokenReference attachedReference = createSecurityTokenReference(token.getWsuId(),false);
        RequestedAttachedReference rar = eleFac.createRequestedAttachedReference(attachedReference);
        SecurityTokenReference unattachedReference = createSecurityTokenReference(token.getIdentifier().toString(), true);
        RequestedUnattachedReference rur = eleFac.createRequestedUnattachedReference(unattachedReference);
        
        // Create Lifetime
        Lifetime lifetime = createLifetime();
        
        RequestSecurityTokenResponse response = null;
        try {
            response =
                    eleFac.createRSTRForIssue(tokenType, con, rst, scopes, rar, rur, proofToken, serverEntropy, lifetime);
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    "WSSC0013.cannot.create.rstr.response", new Object[] {ex.getMessage()});
            throw new WSSecureConversationException(ex);
        }
        
        Session session =
                SessionManager.getSessionManager().createSession(token.getIdentifier().toString());
        log.fine("Creating session for : "  + token.getIdentifier());
        
        // Populate the IssuedTokenContext
        context.setSecurityToken(token);
        context.setAttachedSecurityTokenReference(attachedReference);
        context.setUnAttachedSecurityTokenReference(unattachedReference);
        context.setProofKey(secret);
        context.setCreationTime(new Date(currentTime));
        context.setExpirationTime(new Date(currentTime + TIMEOUT));
        
        SecurityContextTokenInfo sctinfo =
                new SecurityContextTokenInfoImpl();
        sctinfo.setIdentifier(token.getIdentifier().toString());
        sctinfo.setExternalId(token.getWsuId());
        sctinfo.addInstance(null, secret);
        
        sctinfo.setCreationTime(new Date(currentTime));
        sctinfo.setExpirationTime(new Date(currentTime + TIMEOUT));
        
        session.setSecurityInfo(sctinfo);
        
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    "WSSC0014.rstr.response", new Object[] {elemToString(response)});
        }
        return response;
    }
    
    
    /** Issue a Collection of Token(s) possibly for different scopes */
    public RequestSecurityTokenResponseCollection issueMultiple(
            RequestSecurityToken request, IssuedTokenContext context)throws WSSecureConversationException {
        return null;
    }
    
    /** Renew a SecurityContextToken */
    public RequestSecurityTokenResponse renew(
            RequestSecurityToken request, IssuedTokenContext context)
            throws WSSecureConversationException {
        return null;
    }
    
    /** Cancel a SecurityContextToken */
    public RequestSecurityTokenResponse cancel(
            RequestSecurityToken request, IssuedTokenContext context, Map issuedTokenContextMap)
            throws WSSecureConversationException {
        CancelTarget ct = request.getCancelTarget();
        SecurityTokenReference str = ct.getSecurityTokenReference();
        String id = null;
        Reference ref = str.getReference();
        if (ref.getType().equals("Reference")){
            id = ((DirectReference)ref).getURIAttr().toString();
        }
        
        IssuedTokenContext cxt = (IssuedTokenContext)issuedTokenContextMap.get(id);
        if (cxt == null || cxt.getSecurityToken() == null){
            log.log(Level.SEVERE,
                    "WSSC0015.unknown.context", new Object[] {id});
            throw new WSSecureConversationException("Unknown security context token to cancel: "+id);
        }
        
        RequestSecurityTokenResponse rstr = eleFac.createRSTRForCancel();
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    "WSSC0014.rstr.response", new Object[] {elemToString(rstr)});
        }
        return rstr;
    }
    
    /** Validate a SecurityContextToken */
    public RequestSecurityTokenResponse validate(
            RequestSecurityToken request, IssuedTokenContext context)
            throws WSSecureConversationException {
        return null;
    }
    
    /**
     * handle an unsolicited RSTR like in the case of
     * Client Initiated Secure Conversation.
     */
    public void handleUnsolicited(
            RequestSecurityTokenResponse rstr, IssuedTokenContext context)
            throws WSSecureConversationException {
        AppliesTo scope = rstr.getAppliesTo();
        RequestedSecurityToken rqSecToken = rstr.getRequestedSecurityToken();
        Token token = rqSecToken.getToken();
        RequestedProofToken rqProofToken = rstr.getRequestedProofToken();
        String proofTokenType = rqProofToken.getProofTokenType();
        if(proofTokenType.equals(RequestedProofToken.BINARY_SECRET_TYPE)){
            BinarySecret binarySecret = rqProofToken.getBinarySecret();
            if(binarySecret.getType().equals(BinarySecret.SYMMETRIC_KEY_TYPE)){
                byte [] secret = binarySecret.getRawValue();
                context.setProofKey(secret);
            }
        }else if(proofTokenType.equals(RequestedProofToken.ENCRYPTED_KEY_TYPE)){
            //ToDo
        }
        
        context.setSecurityToken(token);
        long curTime = System.currentTimeMillis();
        Date creationTime = new Date(curTime);
        Date expirationTime = new Date(curTime + TIMEOUT);
        context.setCreationTime(creationTime);
        context.setExpirationTime(expirationTime);
        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER,
                    "WSSC1003.setting.times",
                    new Object[] {creationTime.toString(), expirationTime.toString()});
        }
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
     * Contains Challenge
     * @return true if the RST contains Initial Negotiation/Challenge information
     * for a Multi-Message exchange.
     */
    public boolean containsChallenge(RequestSecurityToken rst) {
        return false;
    }
    
    private SecurityTokenReference createSecurityTokenReference(String id, boolean unattached){
        String uri = (unattached?id:"#"+id);
        Reference ref = eleFac.createDirectReference(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE, uri);
        return eleFac.createSecurityTokenReference(ref);
    }
    
    
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
            c.setTimeInMillis(currentTime + TIMEOUT);
            expires.setValue(calendarFormatter.format(c.getTime()));
            
            Lifetime lifetime = eleFac.createLifetime(created, expires);
            
            return lifetime;
        }
    }
    
    private String elemToString(RequestSecurityTokenResponse rstr){
        try {
            javax.xml.bind.Marshaller marshaller = eleFac.getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            java.io.StringWriter sw = new java.io.StringWriter();
            marshaller.marshal(rstrElement, sw);
            return sw.toString();
        } catch (Exception e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "WST1004.error.marshal.toString", e);
            }
            throw new RuntimeException("Error in Marshalling RSTR to string for logging ", e);
        }
    }
    
}
