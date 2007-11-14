/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.security.secconv;

import com.sun.xml.ws.api.security.trust.WSTrustException;
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
import com.sun.xml.ws.security.impl.policy.Trust13;
import com.sun.xml.ws.security.policy.AlgorithmSuite;
import com.sun.xml.ws.security.policy.Constants;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.security.policy.SymmetricBinding;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.Configuration;
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
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.secconv.logging.LogDomainConstants;
import com.sun.xml.ws.security.secconv.logging.LogStringsMessages;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

public class WSSCContract {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSSC_IMPL_DOMAIN,
            LogDomainConstants.WSSC_IMPL_DOMAIN_BUNDLE);
    
    //private Configuration config;
    
    private long currentTime;
    private WSSCVersion wsscVer = WSSCVersion.WSSC_10;
    private WSTrustVersion wsTrustVer = WSTrustVersion.WS_TRUST_10;
    private static WSSCElementFactory eleFac = WSSCElementFactory.newInstance();
    private static WSSCElementFactory13 eleFac13 = WSSCElementFactory13.newInstance();
    
    private static final int DEFAULT_KEY_SIZE = 128;
    
    // ToDo: Should read from the configuration
    private static final long TIMEOUT = 36000000;
    private static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'",Locale.getDefault());
    
    public WSSCContract(){
        //Empty default constructor
    }
    
    public WSSCContract(Configuration config, final WSSCVersion wsscVer){
        init(config, null);        
    }
    
    public final void init(final Configuration config, final WSSCVersion wsscVer){
        //this.config = config;        
        if(wsscVer instanceof com.sun.xml.ws.security.secconv.impl.wssx.WSSCVersion13){
            this.wsscVer = wsscVer;
            this.wsTrustVer = WSTrustVersion.WS_TRUST_13;            
        }
    }
    
    /** Issue a SecurityContextToken */
    public BaseSTSResponse issue(
            final BaseSTSRequest request, final IssuedTokenContext context, final SecureConversationToken scToken) throws WSSecureConversationException {
        // TokenType and context
        URI tokenType = URI.create(wsscVer.getSCTTokenTypeURI());
        URI con = null;
        URI computeKeyAlgo = URI.create(wsTrustVer.getCKPSHA1algorithmURI());
        
        final String conStr = ((RequestSecurityToken)request).getContext();
        if (conStr != null) {
            try {
                con = new URI(conStr);
            } catch (URISyntaxException ex){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0008_URISYNTAX_EXCEPTION(((RequestSecurityToken)request).getContext()), ex);
                throw new WSSecureConversationException(LogStringsMessages.WSSC_0008_URISYNTAX_EXCEPTION(((RequestSecurityToken)request).getContext()), ex);
            }
        }
        
        
        // AppliesTo
        final AppliesTo scopes = ((RequestSecurityToken)request).getAppliesTo();
        
        final RequestedProofToken proofToken;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
            proofToken = eleFac13.createRequestedProofToken();
        }else{
            proofToken = eleFac.createRequestedProofToken();
        }
        
        // Get client entropy
        byte[] clientEntr = null;
        final Entropy clientEntropy = ((RequestSecurityToken)request).getEntropy();
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
        final BaseSTSResponse response = createRSTR(computeKeyAlgo, scToken, request, scopes, clientEntr, proofToken, tokenType, clientEntropy, context, con);
        
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_0014_RSTR_RESPONSE(elemToString((RequestSecurityTokenResponse)response)));
        }
        return response;
    }

    private BaseSTSResponse createRSTR(final URI computeKeyAlgo, final SecureConversationToken scToken, final BaseSTSRequest request, final AppliesTo scopes, final byte[] clientEntr, final RequestedProofToken proofToken, final URI tokenType, final Entropy clientEntropy, final IssuedTokenContext context, final URI con) throws WSSecureConversationException, WSSecureConversationException {
        
        Trust10 trust10 = null;
        Trust13 trust13 = null;
        SymmetricBinding symBinding = null;
        
        final NestedPolicy wsPolicy = scToken.getBootstrapPolicy();
        final AssertionSet assertionSet = wsPolicy.getAssertionSet();
        for(PolicyAssertion policyAssertion : assertionSet){
            SecurityPolicyVersion spVersion = getSPVersion(policyAssertion);
            if(PolicyUtil.isTrust13(policyAssertion, spVersion)){
                trust13 = (Trust13)policyAssertion;
            }else if(PolicyUtil.isTrust10(policyAssertion, spVersion)){
                trust10 = (Trust10)policyAssertion;
            }else if(PolicyUtil.isSymmetricBinding(policyAssertion, spVersion)){
                symBinding = (SymmetricBinding)policyAssertion;
            }
        }
        boolean reqServerEntr = true;
        boolean reqClientEntr=false;
        if(trust10 != null){
            final Set trustReqdProps = trust10.getRequiredProperties();
            reqServerEntr = trustReqdProps.contains(Constants.REQUIRE_SERVER_ENTROPY);
            reqClientEntr = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);            
        }
        if(trust13 != null){
            final Set trustReqdProps = trust13.getRequiredProperties();
            reqServerEntr = trustReqdProps.contains(Constants.REQUIRE_SERVER_ENTROPY);
            reqClientEntr = trustReqdProps.contains(Constants.REQUIRE_CLIENT_ENTROPY);            
        }
        if(clientEntropy == null){
            if(reqClientEntr){
                log.log(Level.SEVERE,
                        LogStringsMessages.WSSC_0010_CLIENT_ENTROPY_CANNOT_NULL());
                throw new WSSecureConversationException(LogStringsMessages.WSSC_0010_CLIENT_ENTROPY_CANNOT_NULL());
            }else{
                reqServerEntr = true;
            }
        }
               
        int keySize = (int)((RequestSecurityToken)request).getKeySize();
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
        ? wsTrustVer.getSymmetricKeyTypeURI() :wsTrustVer.getNonceBinarySecretTypeURI();
        Entropy serverEntropy = null;
        if(reqServerEntr){
            final BinarySecret serverBS;
            if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
                serverBS = eleFac13.createBinarySecret(secret, proofTokenType);
            }else{
                serverBS = eleFac.createBinarySecret(secret, proofTokenType);
            }
            if (proofTokenType.equals(wsTrustVer.getNonceBinarySecretTypeURI())){
                if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
                    serverEntropy = eleFac13.createEntropy(serverBS);
                }else{
                    serverEntropy = eleFac.createEntropy(serverBS);
                }
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
        
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
            return createResponseFor13NS(serverEntropy, con, scopes, secret, proofToken, context, tokenType);    
        }else{
            return createResponse(serverEntropy, con, scopes, secret, proofToken, context, tokenType);
        }
    }

    private BaseSTSResponse createResponse(final Entropy serverEntropy, final URI con, final AppliesTo scopes, final byte[] secret, final RequestedProofToken proofToken, final IssuedTokenContext context, final URI tokenType) throws WSSecureConversationException {

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
        
        BaseSTSResponse response = null;
        try {
            response =
                    eleFac.createRSTRForIssue(tokenType, con, rst, scopes, rar, rur, proofToken, serverEntropy, lifetime);
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
        }
        
        final Session session =
                SessionManager.getSessionManager().createSession(token.getIdentifier().toString());
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1010_CREATING_SESSION(token.getIdentifier()));
        }
        populateITC(session, secret, token, attachedReference, context, unattachedRef);
        SessionManager.getSessionManager().addSecurityContext(token.getIdentifier().toString(), context);
        return response;
    }
    
    private BaseSTSResponse createResponseFor13NS(final Entropy serverEntropy, final URI con, final AppliesTo scopes, final byte[] secret, final RequestedProofToken proofToken, final IssuedTokenContext context, final URI tokenType) throws WSSecureConversationException {

        // Create Security Context and SecurityContextToken
        final SecurityContextToken token = WSTrustUtil.createSecurityContextToken(eleFac13);
        final RequestedSecurityToken rst = eleFac13.createRequestedSecurityToken(token);
        
        // Create references
        final SecurityTokenReference attachedReference = createSecurityTokenReference(token.getWsuId(),false);
        final RequestedAttachedReference rar = eleFac13.createRequestedAttachedReference(attachedReference);
        final SecurityTokenReference unattachedRef = createSecurityTokenReference(token.getIdentifier().toString(), true);
        final RequestedUnattachedReference rur = eleFac13.createRequestedUnattachedReference(unattachedRef);
        
        // Create Lifetime
        final Lifetime lifetime = createLifetime();
        
        BaseSTSResponse response = null;
        try {
            response =
                    eleFac13.createRSTRCollectionForIssue(tokenType, con, rst, scopes, rar, rur, proofToken, serverEntropy, lifetime);
        } catch (WSTrustException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0020_PROBLEM_CREATING_RSTR(), ex);
        }
        
        final Session session =
                SessionManager.getSessionManager().createSession(token.getIdentifier().toString());
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_1010_CREATING_SESSION(token.getIdentifier()));
        }
        populateITC(session, secret, token, attachedReference, context, unattachedRef);
        SessionManager.getSessionManager().addSecurityContext(token.getIdentifier().toString(), context);
        return response;
    }
    

    private void populateITC(final Session session, final byte[] secret, final SecurityContextToken token, final SecurityTokenReference attachedReference, final IssuedTokenContext context, final SecurityTokenReference unattachedRef) {
        
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
    public BaseSTSResponse cancel(
            final BaseSTSRequest request, final IssuedTokenContext context)
            throws WSSecureConversationException{
        final CancelTarget cancelTgt = ((RequestSecurityToken)request).getCancelTarget();
        final SecurityTokenReference str = cancelTgt.getSecurityTokenReference();
        String id = null;
        final Reference ref = str.getReference();
        if (ref.getType().equals("Reference")){
            id = ((DirectReference)ref).getURIAttr().toString();
        }
                
        final IssuedTokenContext cxt = SessionManager.getSessionManager().getSecurityContext(id);
        if (cxt == null || cxt.getSecurityToken() == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0015_UNKNOWN_CONTEXT(id));
            throw new WSSecureConversationException(LogStringsMessages.WSSC_0015_UNKNOWN_CONTEXT(id));
        }
        
        final BaseSTSResponse rstr;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
            RequestSecurityTokenResponse resp = eleFac13.createRSTRForCancel(); 
            List<RequestSecurityTokenResponse> list = new ArrayList<RequestSecurityTokenResponse>();
            list.add(resp);
            try{
                rstr = eleFac13.createRSTRCollectionForIssue(list);
            }catch(WSTrustException ex){
                throw new WSSecureConversationException(ex);
            }
        }else{        
            rstr = eleFac.createRSTRForCancel();
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,
                    LogStringsMessages.WSSC_0014_RSTR_RESPONSE(elemToString(((RequestSecurityTokenResponse)rstr))));
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
        //final AppliesTo scope = rstr.getAppliesTo();
        final RequestedSecurityToken rqSecToken = rstr.getRequestedSecurityToken();
        final Token token = rqSecToken.getToken();
        final RequestedProofToken rqProofToken = rstr.getRequestedProofToken();
        final String proofTokenType = rqProofToken.getProofTokenType();
        if(proofTokenType.equals(RequestedProofToken.BINARY_SECRET_TYPE)){
            final BinarySecret binarySecret = rqProofToken.getBinarySecret();
            if(binarySecret.getType().equals(this.wsTrustVer.getSymmetricKeyTypeURI())){
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
    
    private SecurityTokenReference createSecurityTokenReference(final String id, final boolean unattached){
        final String uri = (unattached?id:"#"+id);
        final Reference ref;
        if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
            ref = eleFac13.createDirectReference(wsscVer.getSCTTokenTypeURI(), uri);
            return eleFac13.createSecurityTokenReference(ref);
        }else{
            ref = eleFac.createDirectReference(wsscVer.getSCTTokenTypeURI(), uri);
            return eleFac.createSecurityTokenReference(ref);
        }
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
            
            final Lifetime lifetime;
            if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
                lifetime = eleFac13.createLifetime(created, expires);
            }else{
                lifetime = eleFac.createLifetime(created, expires);
            }
            
            return lifetime;
        }
    }
    
    private String elemToString(final RequestSecurityTokenResponse rstr){
        try {
            final javax.xml.bind.Marshaller marshaller;
            if(wsscVer.getNamespaceURI().equals(WSSCVersion.WSSC_13.getNamespaceURI())){
                marshaller = eleFac13.getContext().createMarshaller();
            }else{
                marshaller = eleFac.getContext().createMarshaller();
            }
            final JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final java.io.StringWriter writer = new java.io.StringWriter();
            marshaller.marshal(rstrElement, writer);
            return writer.toString();
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSSC_0001_ERROR_MARSHAL_LOG());
            throw new RuntimeException(LogStringsMessages.WSSC_0001_ERROR_MARSHAL_LOG(), e);
        }
    }
    
    private SecurityPolicyVersion getSPVersion(PolicyAssertion pa){
        String nsUri = pa.getName().getNamespaceURI();
        // Default SPVersion
        SecurityPolicyVersion spVersion = SecurityPolicyVersion.SECURITYPOLICY200507;
        // If spec version, update
        if(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri.equals(nsUri)){
            spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;            
        }        
        return spVersion;
    }
    
}
