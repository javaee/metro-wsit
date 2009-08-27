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
import com.sun.xml.ws.api.security.trust.Status;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.STSAuthorizationProvider;
import com.sun.xml.ws.api.security.trust.STSTokenProvider;
import com.sun.xml.ws.api.security.trust.WSTrustContract;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustFactory;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.elements.ActAs;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.OnBehalfOf;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.SecondaryParameters;
import com.sun.xml.ws.security.trust.elements.UseKey;
import com.sun.xml.ws.security.trust.elements.ValidateTarget;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;

import java.io.IOException;
import java.net.URI;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/**
 *
 * @author Jiandong Guo
 */
public class WSTrustContractImpl implements WSTrustContract<BaseSTSRequest, BaseSTSResponse> {
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    protected static final String SAML_SENDER_VOUCHES_1_0 = "urn:oasis:names:tc:SAML:1.0:cm::sender-vouches";
    protected static final String SAML_SENDER_VOUCHES_2_0 = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";
    
   
    protected STSConfiguration stsConfig;
    protected WSTrustVersion wstVer;
    protected WSTrustElementFactory eleFac;
    
    private static final int DEFAULT_KEY_SIZE = 256;
    
    public void init(final STSConfiguration stsConfig) {
        this.stsConfig = stsConfig;
        this.wstVer = (WSTrustVersion)stsConfig.getOtherOptions().get(WSTrustConstants.WST_VERSION);
        this.eleFac = WSTrustElementFactory.newInstance(wstVer);
    }

    public BaseSTSResponse issue(BaseSTSRequest request, IssuedTokenContext context) throws WSTrustException {
        RequestSecurityToken rst = (RequestSecurityToken)request;
        SecondaryParameters secParas = null;
        context.getOtherProperties().put(IssuedTokenContext.WS_TRUST_VERSION, wstVer);
        if (wstVer.getNamespaceURI().equals(WSTrustVersion.WS_TRUST_13_NS_URI)){
            secParas = rst.getSecondaryParameters();
        }
        
       // Get token scope
        final AppliesTo applies = rst.getAppliesTo();
        String appliesTo = null;
        X509Certificate serCert = null;
        List<Object> at = null;
        if(applies != null){
            at = WSTrustUtil.parseAppliesTo(applies);
            for (int i = 0; i < at.size(); i++){
                Object obj = at.get(i);
                if (obj instanceof String){
                    appliesTo = (String)obj;
                }else if (obj instanceof X509Certificate){
                    serCert = (X509Certificate)obj;
                }
            }
        }
        context.setAppliesTo(appliesTo);
        
        // Get token issuer
        String issuer = stsConfig.getIssuer();
        context.setTokenIssuer(issuer);
        
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
        
        // Get service certificate
        if (serCert == null){
            serCert = this.getServiceCertificate(spMd, appliesTo);
        }
        if (serCert != null){
            context.getOtherProperties().put(IssuedTokenContext.TARGET_SERVICE_CERTIFICATE, serCert);
        }
        
        // Get STS certificate and private key
        Object[] certAndKey = this.getSTSCertAndPrivateKey();
        context.getOtherProperties().put(IssuedTokenContext.STS_CERTIFICATE, (X509Certificate)certAndKey[0]);
        context.getOtherProperties().put(IssuedTokenContext.STS_PRIVATE_KEY, (PrivateKey)certAndKey[1]);
        
        // Get TokenType
        String tokenType = null;
        URI tokenTypeURI = rst.getTokenType();
        if (tokenTypeURI == null && secParas != null){
            tokenTypeURI = secParas.getTokenType();
        }
        if (tokenTypeURI != null){
            tokenType = tokenTypeURI.toString();
        }else{
            tokenType = spMd.getTokenType();
        }
        if (tokenType == null){
            tokenType = WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE;
        }
        context.setTokenType(tokenType);
        
        // Get KeyType
        String keyType = null;
        URI keyTypeURI = rst.getKeyType();
        if (keyTypeURI == null && secParas != null){
            keyTypeURI = secParas.getKeyType();
        }
        if (keyTypeURI != null){
            keyType = keyTypeURI.toString();
        }else{
            keyType = spMd.getKeyType();
        }
        if (keyType == null){
            keyType = wstVer.getSymmetricKeyTypeURI();
        }
        context.setKeyType(keyType);

        // Get crypto algorithms
        String encryptionAlgorithm = null;
        URI encryptionAlgorithmURI = rst.getEncryptionAlgorithm();
        if(encryptionAlgorithmURI == null && secParas != null){
            encryptionAlgorithmURI = secParas.getEncryptionAlgorithm();
        }
        if(encryptionAlgorithmURI != null){
            encryptionAlgorithm = encryptionAlgorithmURI.toString();
        }
        context.setEncryptionAlgorithm(encryptionAlgorithm);
        
        String signatureAlgorithm = null;
        URI signatureAlgorithmURI = rst.getSignatureAlgorithm();
        if(signatureAlgorithmURI == null && secParas != null){
            signatureAlgorithmURI = secParas.getSignatureAlgorithm();
        }
        if(signatureAlgorithmURI != null){
            signatureAlgorithm = signatureAlgorithmURI.toString();
        }
        context.setSignatureAlgorithm(signatureAlgorithm);
        
        String canonicalizationAlgorithm = null;
        URI canonicalizationAlgorithmURI = rst.getCanonicalizationAlgorithm();
        if(canonicalizationAlgorithmURI == null && secParas != null){
            canonicalizationAlgorithmURI = secParas.getCanonicalizationAlgorithm();
        }
        if(canonicalizationAlgorithmURI != null){
            canonicalizationAlgorithm = canonicalizationAlgorithmURI.toString();
        }
        context.setCanonicalizationAlgorithm(canonicalizationAlgorithm);
        
        // Get KeyWrap Algorithm, which is the part of WS-Trust wssx versaion
        URI keyWrapAlgorithmURI = null;        
        if(secParas != null){
            keyWrapAlgorithmURI = secParas.getKeyWrapAlgorithm();            
        }        
        if(keyWrapAlgorithmURI != null){
            context.getOtherProperties().put(IssuedTokenContext.KEY_WRAP_ALGORITHM, keyWrapAlgorithmURI.toString());
        }                
        
        // Get authenticaed client Subject 
        Subject subject = context.getRequestorSubject();
        if (subject == null){
            AccessControlContext acc = AccessController.getContext();
            subject = Subject.getSubject(acc);
            context.setRequestorSubject(subject);
        }
        if(subject == null){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0030_REQUESTOR_NULL());
            throw new WSTrustException(LogStringsMessages.WST_0030_REQUESTOR_NULL());
        }
        
        // Get client authentication context
        String authnCtx = (String)stsConfig.getOtherOptions().get(WSTrustConstants.AUTHN_CONTEXT_CLASS);
        if (authnCtx != null){
             context.getOtherProperties().put(IssuedTokenContext.AUTHN_CONTEXT, authnCtx);
        }

        // Get Claims from the RST
        Claims claims = rst.getClaims();
        if (claims == null && secParas != null){
            claims = secParas.getClaims();
        }
        if (claims != null){
            // Add supporting information
            List<Object> si = rst.getExtensionElements();
            claims.getSupportingProperties().addAll(si);
            if (at != null){
                claims.getSupportingProperties().addAll(at);
            }
        }else{
            claims = eleFac.createClaims();
        }
        
        // Handle OnBehalfOf token
        OnBehalfOf obo = rst.getOnBehalfOf();
        if (obo != null){
            Object oboToken = obo.getAny();
            if (oboToken != null){
                subject.getPublicCredentials().add(eleFac.toElement(oboToken));

                 // set OnBehalfOf attribute
                claims.getOtherAttributes().put(new QName("OnBehalfOf"), "true");

                // Create a Subject with ActAs credential and put it in claims
                Subject oboSubj = new Subject();
                oboSubj.getPublicCredentials().add(eleFac.toElement(oboToken));
                claims.getSupportingProperties().add(oboSubj);
                String confirMethod = null;
                if (tokenType.equals(WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE)||
                    tokenType.equals(WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE)){
                    confirMethod = SAML_SENDER_VOUCHES_1_0;
                } else if (tokenType.equals(WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE)){
                    confirMethod = SAML_SENDER_VOUCHES_2_0;
                }
                if (confirMethod != null){
                    context.getOtherProperties().put(IssuedTokenContext.CONFIRMATION_METHOD, confirMethod);
                }
            }
        }
        
        // Handle ActAs token
        ActAs actAs = rst.getActAs();
        if (actAs != null){
            Object actAsToken = actAs.getAny();
            if (actAsToken != null){
                // set ActAs attribute
                claims.getOtherAttributes().put(new QName("ActAs"), "true");

                // Create a Subject with ActAs credential and put it in claims
                Subject actAsSubj = new Subject();
                actAsSubj.getPublicCredentials().add(eleFac.toElement(actAsToken));
                claims.getSupportingProperties().add(actAsSubj);
            }
        }

        // Check if the client is authorized to be issued the token from the STSAuthorizationProvider
        //ToDo: handling ActAs case
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
        final STSAttributeProvider attrProvider = WSTrustFactory.getSTSAttributeProvider();
        final Map<QName, List<String>> claimedAttrs = attrProvider.getClaimedAttributes(subject, appliesTo, tokenType, claims);
        context.getOtherProperties().put(IssuedTokenContext.CLAIMED_ATTRUBUTES, claimedAttrs);
        
        //==========================================
        // Create proof key and RequestedProofToken 
        //==========================================
           
        RequestedProofToken proofToken = null;
        Entropy serverEntropy = null;
        int keySize = 0;
        if (wstVer.getSymmetricKeyTypeURI().equals(keyType)){
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
            
            // Get KeySize
            keySize = (int)rst.getKeySize();
            if (keySize < 1 && secParas != null){
                keySize = (int) secParas.getKeySize();
            }
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
            
            // compute the secret key
            try {
                if (clientEntr != null && clientEntr.length > 0){
                    proofToken.setComputedKey(URI.create(wstVer.getCKPSHA1algorithmURI()));
                    proofToken.setProofTokenType(RequestedProofToken.COMPUTED_KEY_TYPE);
                    key = SecurityUtil.P_SHA1(clientEntr, key, keySize/8);
                }else{
                    proofToken.setProofTokenType(RequestedProofToken.BINARY_SECRET_TYPE);
                    proofToken.setBinarySecret(serverBS);
                }
            } catch (Exception ex){
                log.log(Level.SEVERE, 
                        LogStringsMessages.WST_0013_ERROR_SECRET_KEY(wstVer.getCKPSHA1algorithmURI(), keySize, appliesTo), ex);
                throw new WSTrustException(LogStringsMessages.WST_0013_ERROR_SECRET_KEY(wstVer.getCKPSHA1algorithmURI(), keySize, appliesTo), ex);
            }
            
            // put the generated secret key into the IssuedTokenContext
            context.setProofKey(key);
        }else if(wstVer.getPublicKeyTypeURI().equals(keyType)){
            // Get UseKey from the RST
            UseKey useKey = rst.getUseKey();
            if (useKey != null){
                Element keyInfo = (Element)eleFac.toElement(useKey.getToken().getTokenValue());
                context.getOtherProperties().put("ConfirmationKeyInfo", keyInfo);
            }
            final Set certs = subject.getPublicCredentials();
            boolean addedClientCert = false;
            for(Object o : certs){
                if(o instanceof X509Certificate){
                    final X509Certificate clientCert = (X509Certificate)o;
                    context.setRequestorCertificate(clientCert);
                    addedClientCert = true;
                }
            }
            if(!addedClientCert && useKey == null){
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
        
        // Create Lifetime
        Lifetime lifetime = rst.getLifetime();
        long currentTime = WSTrustUtil.getCurrentTimeWithOffset();
        long lifespan = -1;
        if (lifetime == null){
            lifespan = stsConfig.getIssuedTokenTimeout();
            lifetime = WSTrustUtil.createLifetime(currentTime, lifespan, wstVer);
           
        }else{
            lifespan = WSTrustUtil.getLifeSpan(lifetime);
        }
        context.setCreationTime(new Date(currentTime));
        context.setExpirationTime(new Date(currentTime + lifespan));
        
        //==============================================
        // Create RequestedSecurityToken and references
        //==============================================
        
        // Get STSTokenProvider
        STSTokenProvider tokenProvider = WSTrustFactory.getSTSTokenProvider();
        tokenProvider.generateToken(context);
        
        // Create RequestedSecurityToken 
        final RequestedSecurityToken reqSecTok = eleFac.createRequestedSecurityToken();
        Token issuedToken = context.getSecurityToken();
        
        // Encrypt the token if required and the service certificate is available
        if (stsConfig.getEncryptIssuedToken()&& serCert != null){
            String keyWrapAlgo = (String) context.getOtherProperties().get(IssuedTokenContext.KEY_WRAP_ALGORITHM);
            Element encTokenEle = this.encryptToken((Element)issuedToken.getTokenValue(), serCert, appliesTo, encryptionAlgorithm, keyWrapAlgo);
            issuedToken = new GenericToken(encTokenEle);
        }
        reqSecTok.setToken(issuedToken);
        
        // Create RequestedAttachedReference and RequestedUnattachedReference
        final SecurityTokenReference raSTR = (SecurityTokenReference)context.getAttachedSecurityTokenReference();
        final SecurityTokenReference ruSTR = (SecurityTokenReference)context.getUnAttachedSecurityTokenReference();
        RequestedAttachedReference raRef =  null;
        if (raSTR != null){
            raRef = eleFac.createRequestedAttachedReference(raSTR);
        }
        RequestedUnattachedReference ruRef = null;
        if (ruSTR != null){
            ruRef = eleFac.createRequestedUnattachedReference(ruSTR);
        }
        
        //======================================
        // Create RequestSecurityTokenResponse
        //======================================
        
        // get Context
        URI ctx = null;
        final String rstCtx = rst.getContext();
        if (rstCtx != null){
            ctx = URI.create(rstCtx);
        }
      
        // Create RequestSecurityTokenResponse
        final RequestSecurityTokenResponse rstr =
                eleFac.createRSTRForIssue(URI.create(tokenType), ctx, reqSecTok, applies, raRef, ruRef, proofToken, serverEntropy, lifetime);
        
        if (keySize > 0){
            rstr.setKeySize(keySize);
        }
        
        this.handleExtension(rst, rstr, context);
        
        if(log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, 
                        LogStringsMessages.WST_1006_CREATED_RST_ISSUE(WSTrustUtil.elemToString(rst, wstVer)));
                log.log(Level.FINE, 
                        LogStringsMessages.WST_1007_CREATED_RSTR_ISSUE(WSTrustUtil.elemToString(rstr, wstVer)));
        }
        
        if (wstVer.getNamespaceURI().equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
            List<RequestSecurityTokenResponse> list = new ArrayList<RequestSecurityTokenResponse>();
            list.add(rstr);
            RequestSecurityTokenResponseCollection rstrc = eleFac.createRSTRC(list);

            return rstrc;
        }
           
        return rstr;
    }
    
    protected void handleExtension(BaseSTSRequest request, BaseSTSResponse response, IssuedTokenContext context) throws WSTrustException{
    
    }

    public BaseSTSResponse renew(BaseSTSRequest rst, IssuedTokenContext context) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: renew");
    }

    public BaseSTSResponse cancel(BaseSTSRequest rst, IssuedTokenContext context, Map map) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: cancel");
    }

    public BaseSTSResponse validate(BaseSTSRequest request, IssuedTokenContext context) throws WSTrustException {
        RequestSecurityToken rst = (RequestSecurityToken)request;
        
        // Get STS certificate and private key
        Object[] certAndKey = this.getSTSCertAndPrivateKey();
        context.getOtherProperties().put(IssuedTokenContext.STS_CERTIFICATE, (X509Certificate)certAndKey[0]);
        context.getOtherProperties().put(IssuedTokenContext.STS_PRIVATE_KEY, (PrivateKey)certAndKey[1]);
        context.getOtherProperties().put(IssuedTokenContext.WS_TRUST_VERSION, wstVer);
         
        // get TokenType
        URI tokenType = rst.getTokenType();
        context.setTokenType(tokenType.toString());
        
        //Get ValidateTarget
        Token token = null;
        if (wstVer.getNamespaceURI().equals(WSTrustVersion.WS_TRUST_10_NS_URI)){
            // It is not well defined how to carry the security token 
            // to be validated. ValidateTarget is obly introduced in the 
            // ws-trust 1.3. Here we assume the token is directly child element 
            // of RST
            List<Object> exts = rst.getExtensionElements();
            if (exts.size() > 0){
                token = new GenericToken((Element)exts.get(0));
            }  
        }else{
            ValidateTarget vt = rst.getValidateTarget();
            token = new GenericToken((Element)vt.getAny());
        }
        context.setTarget(token);
        
        // Get STSTokenProvider and validate the token
        STSTokenProvider tokenProvider = WSTrustFactory.getSTSTokenProvider();
        tokenProvider.isValideToken(context);
        
        // Create RequestedSecurityToken 
        RequestedSecurityToken reqSecTok = null;
        if (!wstVer.getValidateStatuesTokenType().equals(tokenType.toString())){
            reqSecTok = eleFac.createRequestedSecurityToken();
            Token issuedToken = context.getSecurityToken();
            reqSecTok.setToken(issuedToken);
        }
        
        // Create RequestSecurityTokenResponse
        final RequestSecurityTokenResponse rstr = eleFac.createRSTRForValidate(tokenType, reqSecTok, (Status)context.getOtherProperties().get(IssuedTokenContext.STATUS));
        
        if (wstVer.getNamespaceURI().equals(WSTrustVersion.WS_TRUST_13.getNamespaceURI())){
            List<RequestSecurityTokenResponse> list = new ArrayList<RequestSecurityTokenResponse>();
            list.add(rstr);
            RequestSecurityTokenResponseCollection rstrc = eleFac.createRSTRC(list);

            return rstrc;
        }
        
        return rstr;
    }

    public void handleUnsolicited(BaseSTSResponse rstr, IssuedTokenContext context) throws WSTrustException {
        throw new UnsupportedOperationException("Unsupported operation: handleUnsolicited");
    }
    
    private X509Certificate getServiceCertificate(TrustSPMetadata spMd, String appliesTo)throws WSTrustException{
        String certAlias = spMd.getCertAlias();
        X509Certificate cert = null;
        CallbackHandler callbackHandler = stsConfig.getCallbackHandler();
        if (callbackHandler != null){
            // Get the service certificate
            final EncryptionKeyCallback.AliasX509CertificateRequest req = new EncryptionKeyCallback.AliasX509CertificateRequest(spMd.getCertAlias());
            final EncryptionKeyCallback callback = new EncryptionKeyCallback(req);
            final Callback[] callbacks = {callback};
            try{
                callbackHandler.handle(callbacks);
            }catch(IOException ex){
                log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
                throw new WSTrustException(
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);            
            }catch(UnsupportedCallbackException ex){
                log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
                throw new WSTrustException(
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
            }
        
            cert = req.getX509Certificate();
        }else{
            SecurityEnvironment secEnv = (SecurityEnvironment)stsConfig.getOtherOptions().get(WSTrustConstants.SECURITY_ENVIRONMENT);
            try{
                cert = secEnv.getCertificate(stsConfig.getOtherOptions(), certAlias, false);
            }catch( XWSSecurityException ex){
                log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
                throw new WSTrustException(
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
            }
        }
        
        return cert;
    }
    
    private Object[] getSTSCertAndPrivateKey() throws WSTrustException{
        X509Certificate stsCert = null;
        PrivateKey stsPrivKey = null;
        CallbackHandler callbackHandler = stsConfig.getCallbackHandler();
        if (callbackHandler != null){
            final SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            final Callback skc = new SignatureKeyCallback(request);
            final Callback[] callbacks = {skc};
            try{
                callbackHandler.handle(callbacks);
            }catch(IOException ex){
                log.log(Level.SEVERE,
                    LogStringsMessages.WST_0043_UNABLE_GET_STS_KEY(), ex);
                throw new WSTrustException(
                    LogStringsMessages.WST_0043_UNABLE_GET_STS_KEY(), ex);            
            }catch(UnsupportedCallbackException ex){
                log.log(Level.SEVERE,
                    LogStringsMessages.WST_0043_UNABLE_GET_STS_KEY(), ex);
                throw new WSTrustException(
                    LogStringsMessages.WST_0043_UNABLE_GET_STS_KEY(), ex);
            }
                
            stsPrivKey = request.getPrivateKey();
            stsCert = request.getX509Certificate();
        }else{
            SecurityEnvironment secEnv = (SecurityEnvironment)stsConfig.getOtherOptions().get(WSTrustConstants.SECURITY_ENVIRONMENT);
            try{
                stsCert = secEnv.getDefaultCertificate(stsConfig.getOtherOptions());
                stsPrivKey = secEnv.getPrivateKey(stsConfig.getOtherOptions(), stsCert);
            }catch( XWSSecurityException ex){
                log.log(Level.SEVERE,
                    LogStringsMessages.WST_0043_UNABLE_GET_STS_KEY(), ex);
                throw new WSTrustException(
                    LogStringsMessages.WST_0043_UNABLE_GET_STS_KEY(), ex);
            }
        }
        
        Object[] results = new Object[2];
        results[0] = stsCert;
        results[1] = stsPrivKey;
        return results;
    }
    
     private Element encryptToken(final Element assertion,  final X509Certificate serCert, final String appliesTo, final String encryptionAlgorithm, final String keyWrapAlgorithm) throws WSTrustException{
        Element encDataEle = null;
        // Create the encryption key
        try{
            final XMLCipher cipher;
            if(encryptionAlgorithm != null){
                cipher = XMLCipher.getInstance(encryptionAlgorithm);
            }else{
                cipher = XMLCipher.getInstance(XMLCipher.AES_256);
            }
            final int keysizeInBytes = 32;
            final byte[] skey = WSTrustUtil.generateRandomSecret(keysizeInBytes);
            cipher.init(XMLCipher.ENCRYPT_MODE, new SecretKeySpec(skey, "AES"));
                
            // Encrypt the assertion and return the Encrypteddata
            final Document owner = assertion.getOwnerDocument();
            final EncryptedData encData = cipher.encryptData(owner, assertion);
            final String id = "uuid-" + UUID.randomUUID().toString();
            encData.setId(id);
                
            final KeyInfo encKeyInfo = new KeyInfo(owner);
            final EncryptedKey encKey = WSTrustUtil.encryptKey(owner, skey, serCert, keyWrapAlgorithm);
            encKeyInfo.add(encKey);
            encData.setKeyInfo(encKeyInfo);
            
            encDataEle = cipher.martial(encData);
         } catch (XMLEncryptionException ex) {
            log.log(Level.SEVERE,
                            LogStringsMessages.WST_0044_ERROR_ENCRYPT_ISSUED_TOKEN(appliesTo), ex);
            throw new WSTrustException( LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
        } catch (Exception ex) {
            log.log(Level.SEVERE,
                            LogStringsMessages.WST_0044_ERROR_ENCRYPT_ISSUED_TOKEN(appliesTo), ex);
            throw new WSTrustException( LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
        }
        
        return encDataEle;
    }
}
