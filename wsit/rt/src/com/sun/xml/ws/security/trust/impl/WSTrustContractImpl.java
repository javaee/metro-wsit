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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.api.security.trust.IssuedTokenGenerator;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;
import com.sun.xml.ws.api.security.trust.WSTrustContract;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
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
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

/**
 *
 * @author Jiandong Guo
 */
public class WSTrustContractImpl implements WSTrustContract<BaseSTSRequest, BaseSTSResponse> {
   private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    protected STSConfiguration stsConfig;
    
    protected static final WSTrustElementFactory eleFac = WSTrustElementFactory.newInstance();
    protected static final SimpleDateFormat calendarFormatter
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'", Locale.getDefault());
    
    private static final int DEFAULT_KEY_SIZE = 256;
    
    public void init(STSConfiguration config) {
        this.stsConfig = config;
    }

    public BaseSTSResponse issue(BaseSTSRequest request, IssuedTokenContext context) throws WSTrustException {
        WSTrustVersion wstVer = (WSTrustVersion)stsConfig.getOtherOptions().get(WSTrustConstants.WST_VERSION);
        RequestSecurityToken rst = (RequestSecurityToken)request;
        
        // Get AppliesTo
        final AppliesTo applies = rst.getAppliesTo();
        String appliesTo = null;
        if(applies != null){
            appliesTo = WSTrustUtil.getAppliesToURI(applies);
        }
        
        // Get the metadata for the SP as identified by the AppliesTo
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
            // Using symmetric key by default
            keyType = WSTrustConstants.SYMMETRIC_KEY;
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
        
        // Get the OnBehalfOf token and put it in the Subject public credentails
        // ToDo: to identify the OBO token properly
        OnBehalfOf obo = rst.getOnBehalfOf();
        if (obo != null){
            Object oboToken = obo.getAny();
            if (oboToken != null){
                subject.getPublicCredentials().add((Element)oboToken);
            }
        }
        
        // Check if the client is authorized to be issued the token from the STSAuthorizationProvider
        final STSAuthorizationProvider authzProvider = WSTrustFactory.getSTSAuthorizationProvider();
        if (!authzProvider.isAuthorized(subject, appliesTo, tokenType, keyType)){
            String user = subject.getPrincipals().iterator().next().getName();
            log.log(Level.SEVERE, 
                    LogStringsMessages.WST_0015_CLIENT_NOT_AUTHORIZED(
                    user, tokenType, appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0015_CLIENT_NOT_AUTHORIZED(
                    user, tokenType, appliesTo));
        }
        
        // Get claimed attributes from the STSAttributeProvider
        final Claims claims = rst.getClaims();
        final STSAttributeProvider attrProvider = WSTrustFactory.getSTSAttributeProvider();
        final Map<QName, List<String>> claimedAttrs = attrProvider.getClaimedAttributes(subject, appliesTo, tokenType, claims);
        
        //==========================================
        // Create proof key and RequestedProofToken 
        //==========================================
            
        // ToDo
        RequestedProofToken proofToken = null;
        Entropy serverEntropy = null;
        int keySize = 0;
        if (WSTrustConstants.SYMMETRIC_KEY.equals(keyType)){
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
                proofToken.setComputedKey(URI.create(WSTrustConstants.CK_PSHA1));
                key = SecurityUtil.P_SHA1(clientEntr, key, keySize/8);
            } catch (Exception ex){
                log.log(Level.SEVERE, 
                        LogStringsMessages.WST_0013_ERROR_SECRET_KEY(WSTrustConstants.CK_PSHA1, keySize, appliesTo), ex);
                throw new WSTrustException(LogStringsMessages.WST_0013_ERROR_SECRET_KEY(WSTrustConstants.CK_PSHA1, keySize, appliesTo), ex);
            }
            
            context.setProofKey(key);
        }else if(WSTrustConstants.PUBLIC_KEY.equals(keyType)){
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
        }else{
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keyType, appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keyType, appliesTo));
        }
        
        //================================
        // Create RequestedSecurityToken 
        //================================
        
        //ToDo
        
        // Create RequestedSecurityToken 
        final RequestedSecurityToken reqSecTok = null;
        
        // Create RequestedAttachedReference and RequestedUnattachedReference
        final RequestedAttachedReference raRef =  null;
        final RequestedUnattachedReference ruRef = null;
        
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
        
        // Create Lifetime
        final Lifetime lifetime = createLifetime();
        
        // Create RequestSecurityTokenResponse
        final RequestSecurityTokenResponse rstr =
                eleFac.createRSTRForIssue(rst.getTokenType(), ctx, reqSecTok, applies, raRef, ruRef, proofToken, serverEntropy, lifetime);
        
        if (keySize > 0){
            rstr.setKeySize(keySize);
        }
        
        // Populate IssuedTokenContext
        //ToDo
        //context.setSecurityToken(samlToken);
        //context.setAttachedSecurityTokenReference(samlReference);
        //context.setUnAttachedSecurityTokenReference(samlReference);
        //context.setCreationTime(new Date(currentTime));
        //context.setExpirationTime(new Date(currentTime + stsConfig.getIssuedTokenTimeout()));
        
        return rstr;
    }

    public BaseSTSResponse renew(BaseSTSRequest rst, IssuedTokenContext context) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: renew");
    }

    public BaseSTSResponse cancel(BaseSTSRequest rst, IssuedTokenContext context, Map map) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: cancel");
    }

    public BaseSTSResponse validate(BaseSTSRequest request, IssuedTokenContext context) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: validate");
    }

    public void handleUnsolicited(BaseSTSResponse rstr, IssuedTokenContext context) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: handleUnsolicited");
    }
    
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
}
