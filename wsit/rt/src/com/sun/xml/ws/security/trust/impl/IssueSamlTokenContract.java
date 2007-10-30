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

package com.sun.xml.ws.security.trust.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;

import javax.security.auth.Subject;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;
import com.sun.xml.ws.api.security.trust.WSTrustContract;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.policy.SecureConversationToken;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.OnBehalfOf;
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
import java.util.ArrayList;
import org.w3c.dom.Element;

public abstract class IssueSamlTokenContract implements com.sun.xml.ws.api.security.trust.IssueSamlTokenContract<BaseSTSRequest, BaseSTSResponse> {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    protected STSConfiguration stsConfig;
    protected WSTrustVersion wstVer;
    protected WSTrustElementFactory eleFac = 
            WSTrustElementFactory.newInstance(WSTrustVersion.WS_TRUST_10);
    protected static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'", Locale.getDefault());
    
    private static final int DEFAULT_KEY_SIZE = 256;

    
    public void init(final STSConfiguration stsConfig) {
        this.stsConfig = stsConfig;
        this.wstVer = (WSTrustVersion)stsConfig.getOtherOptions().get(WSTrustConstants.WST_VERSION);
        eleFac = WSTrustElementFactory.newInstance(wstVer);
    }
    
    /** Issue a Token */
    public BaseSTSResponse issue(final BaseSTSRequest request, final IssuedTokenContext context)throws WSTrustException {
        
        RequestSecurityToken rst = (RequestSecurityToken)request;
        //WSTrustVersion wstVer = (WSTrustVersion)stsConfig.getOtherOptions().get(WSTrustConstants.WST_VERSION);

        // Get AppliesTo
        final AppliesTo applies = rst.getAppliesTo();
        String appliesTo = null;
        if(applies != null){
            appliesTo = WSTrustUtil.getAppliesToURI(applies);
        }
        
        TrustSPMetadata spMd = stsConfig.getTrustSPMetadata(appliesTo);
        if (spMd == null){
            // Only used for testing purpose; default should not documented
            spMd = stsConfig.getTrustSPMetadata("default");
        }
        if (spMd == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0004_UNKNOWN_SERVICEPROVIDER(appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0004_UNKNOWN_SERVICEPROVIDER(appliesTo));
        }

        // Get TokenType
        String tokenType = null;
        final URI tokenTypeURI = rst.getTokenType();
        if (tokenTypeURI != null){
            tokenType = tokenTypeURI.toString();
        }else{
            tokenType = spMd.getTokenType();
        }
        if (tokenType == null){
            tokenType = WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE;
        }
        
        // Get KeyType
        String keyType = null;
        final URI keyTypeURI = rst.getKeyType();
        if (keyTypeURI != null){
            keyType = keyTypeURI.toString();
        }else{
            keyType = spMd.getKeyType();
        }
        if (keyType == null){
            keyType = wstVer.getSymmetricKeyTypeURI();
        }
        
        
        // Get authenticaed client Subject 
        Subject subject = context.getRequestorSubject();
        if (subject == null){
            AccessControlContext acc = AccessController.getContext();
            subject = Subject.getSubject(acc);
        }
        if(subject == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0030_REQUESTOR_NULL());
            throw new WSTrustException(LogStringsMessages.WST_0030_REQUESTOR_NULL());
        }
        
        OnBehalfOf obo = rst.getOnBehalfOf();
        if (obo != null){
            Object oboToken = obo.getAny();
            if (oboToken != null){
                subject.getPublicCredentials().add((Element)oboToken);
            }
        }
        
        // Check if the client is authorized to be issued the token
        final STSAuthorizationProvider authzProvider = WSTrustFactory.getSTSAuthorizationProvider();
        if (!authzProvider.isAuthorized(subject, appliesTo, tokenType, keyType)){
            String user = subject.getPrincipals().iterator().next().getName();
            log.log(Level.SEVERE, 
                    LogStringsMessages.WST_0015_CLIENT_NOT_AUTHORIZED(
                    user, tokenType, appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0015_CLIENT_NOT_AUTHORIZED(
                    user, tokenType, appliesTo));
        }
        
        // Get claimed attributes
        final Claims claims = rst.getClaims();
        final STSAttributeProvider attrProvider = WSTrustFactory.getSTSAttributeProvider();
        final Map<QName, List<String>> claimedAttrs = attrProvider.getClaimedAttributes(subject, appliesTo, tokenType, claims);
        
        RequestedProofToken proofToken = null;
        Entropy serverEntropy = null;
        int keySize = 0;
        if (wstVer.getSymmetricKeyTypeURI().equals(keyType)){
            //============================
            // Create required secret key
            //============================
            
            proofToken = eleFac.createRequestedProofToken();
            
            // Get client entropy
            byte[] clientEntr = null;
            final Entropy clientEntropy = rst.getEntropy();
            if (clientEntropy != null){
                final BinarySecret clientBS = clientEntropy.getBinarySecret();
                if (clientBS == null){
                    if(log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, 
                                LogStringsMessages.WST_1009_NULL_BINARY_SECRET());
                    }
                }else {
                    clientEntr = clientBS.getRawValue();
                }
            }
            
            keySize = (int)rst.getKeySize();
            if (keySize < 1){
                keySize = DEFAULT_KEY_SIZE;
            }
            if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, 
                        LogStringsMessages.WST_1010_KEY_SIZE(keySize, DEFAULT_KEY_SIZE));
            }
            
            byte[] key = WSTrustUtil.generateRandomSecret(keySize/8);
            final BinarySecret serverBS = eleFac.createBinarySecret(key, wstVer.getNonceBinarySecretTypeURI());
            serverEntropy = eleFac.createEntropy(serverBS);
            proofToken.setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);
            
            // compute the secret key
            try {
                proofToken.setComputedKey(URI.create(wstVer.getCKPSHA1algorithmURI()));
                key = SecurityUtil.P_SHA1(clientEntr, key, keySize/8);
            } catch (Exception ex){
                log.log(Level.SEVERE, 
                        LogStringsMessages.WST_0013_ERROR_SECRET_KEY(wstVer.getCKPSHA1algorithmURI(), keySize, appliesTo), ex);
                throw new WSTrustException(LogStringsMessages.WST_0013_ERROR_SECRET_KEY(wstVer.getCKPSHA1algorithmURI(), keySize, appliesTo), ex);
            }
            
            context.setProofKey(key);
        }else if(wstVer.getPublicKeyTypeURI().equals(keyType)){
            final Set certs = context.getRequestorSubject().getPublicCredentials();
            if(certs == null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
                throw new WSTrustException(
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
            }
            boolean addedClientCert = false;
            for(Object o : certs){
                if(o instanceof X509Certificate){
                    final X509Certificate clientCert = (X509Certificate)o;
                    context.setRequestorCertificate(clientCert);
                    addedClientCert = true;
                }
            }
            if(!addedClientCert){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
                throw new WSTrustException(LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
            }
        }else if(wstVer.getBearerKeyTypeURI().equals(keyType)){
            //No proof key required 
        }else{
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keyType, appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keyType, appliesTo));
        }
        
        //==================
        // Create the RSTR
        //==================
        
        // get Context
        URI ctx = null;
        try {
            final String rstCtx = rst.getContext();
            if (rstCtx != null){
                ctx = new URI(rst.getContext());
            }
        } catch (URISyntaxException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0014_URI_SYNTAX(), ex);
            throw new WSTrustException(
                    LogStringsMessages.WST_0014_URI_SYNTAX() ,ex);
        }
        
        // Create RequestedSecurityToken with SAML assertion
        final String assertionId = "uuid-" + UUID.randomUUID().toString();
        final RequestedSecurityToken reqSecTok = eleFac.createRequestedSecurityToken();
        final Token samlToken = createSAMLAssertion(appliesTo, tokenType, keyType, assertionId, stsConfig.getIssuer(), claimedAttrs, context);
        reqSecTok.setToken(samlToken);
        
        // Create RequestedAttachedReference and RequestedUnattachedReference
        final SecurityTokenReference samlReference = createSecurityTokenReference(assertionId, tokenType);
        final RequestedAttachedReference raRef =  eleFac.createRequestedAttachedReference(samlReference);
        final RequestedUnattachedReference ruRef =  eleFac.createRequestedUnattachedReference(samlReference);
        
        // Create Lifetime
        final Lifetime lifetime = createLifetime();
        
        final RequestSecurityTokenResponse rstr =
                eleFac.createRSTRForIssue(rst.getTokenType(), ctx, reqSecTok, applies, raRef, ruRef, proofToken, serverEntropy, lifetime);
        
        if (keySize > 0){
            rstr.setKeySize(keySize);
        }
        
       //String issuer = config.getIssuer();
        
      // Token samlToken = createSAMLAssertion(appliesTo, tokenType, keyType, assertionId, issuer, claimedAttrs, context);
       //rstr.getRequestedSecurityToken().setToken(samlToken);
        
        // Populate IssuedTokenContext
        context.setSecurityToken(samlToken);
        context.setAttachedSecurityTokenReference(samlReference);
        context.setUnAttachedSecurityTokenReference(samlReference);
        context.setCreationTime(new Date(currentTime));
        context.setExpirationTime(new Date(currentTime + stsConfig.getIssuedTokenTimeout()));
        
        if (wstVer.getNamespaceURI().equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
            List<RequestSecurityTokenResponse> list = new ArrayList<RequestSecurityTokenResponse>();
            list.add(rstr);
            RequestSecurityTokenResponseCollection rstrc = eleFac.createRSTRC(list);

            return rstrc;
        }
        return rstr;
    }
    
    /** Issue a Collection of Token(s) possibly for different scopes */
    public BaseSTSResponse issueMultiple(
            final BaseSTSRequest request, final IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: issueMultiple");
    }
    
    /** Renew a Token */
    public BaseSTSResponse renew(
            final BaseSTSRequest request, final IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: renew");
    }
    
    /** Cancel a Token */
    public BaseSTSResponse cancel(
            final BaseSTSRequest request, final IssuedTokenContext context, final Map issuedTokenCtxMap)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: cancel");
    }
    
    /** Validate a Token */
    public BaseSTSResponse validate(
            final BaseSTSRequest request, final IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: validate");
    }
    
    /**
     * handle an unsolicited RSTR like in the case of
     * Client Initiated Secure Conversation.
     */
    public void handleUnsolicited(
            final BaseSTSResponse rstr, final IssuedTokenContext context)
            throws WSTrustException{
        throw new UnsupportedOperationException("Unsupported operation: handleUnsolicited");
    }
    
    public abstract Token createSAMLAssertion(String appliesTo, String tokenType, String keyType, String assertionId, String issuer, Map<QName, List<String>> claimedAttrs, IssuedTokenContext context) throws WSTrustException;
    
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
            cal.setTimeInMillis(currentTime + stsConfig.getIssuedTokenTimeout());
            expires.setValue(calendarFormatter.format(cal.getTime()));
            
            final Lifetime lifetime = eleFac.createLifetime(created, expires);
            
            return lifetime;
        }
    }
    
    private SecurityTokenReference createSecurityTokenReference(final String id, final String tokenType){
        String valueType = null;
         if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
                WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                valueType = MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE;
            } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                valueType = MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE;
            }
        final KeyIdentifier ref = eleFac.createKeyIdentifier(valueType, null);
        ref.setValue(id);
        return eleFac.createSecurityTokenReference(ref);
    }
}
