/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: RequestSecurityTokenResponseImpl.java,v 1.2 2010-10-21 15:36:55 snajper Exp $
 */

package com.sun.xml.ws.security.trust.impl.elements;

import java.net.URISyntaxException;
import java.util.List;

import java.net.URI;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.impl.bindings.PolicyReference;
import javax.xml.bind.JAXBElement;

import com.sun.xml.ws.api.security.trust.WSTrustException;

import com.sun.xml.ws.security.trust.elements.*;

import com.sun.xml.ws.api.security.trust.Status;

import com.sun.xml.ws.security.trust.impl.bindings.AllowPostdatingType;
import com.sun.xml.ws.security.trust.impl.bindings.AuthenticatorType;
import com.sun.xml.ws.security.trust.impl.bindings.BinaryExchangeType;
import com.sun.xml.ws.security.trust.impl.bindings.DelegateToType;
import com.sun.xml.ws.security.trust.impl.bindings.EncryptionType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.bindings.LifetimeType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedReferenceType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedProofTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedTokenCancelledType;

import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.bindings.ProofEncryptionType;
import com.sun.xml.ws.security.trust.impl.bindings.RenewingType;
import com.sun.xml.ws.security.trust.impl.bindings.SignChallengeType;
import com.sun.xml.ws.security.trust.impl.bindings.StatusType;
import com.sun.xml.ws.security.trust.impl.bindings.UseKeyType;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import com.sun.istack.NotNull;
import com.sun.xml.ws.security.trust.WSTrustVersion;

import com.sun.xml.ws.security.trust.impl.WSTrustVersion10;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 * Implementation of a RequestSecurityTokenResponse.
 *
 * @author Manveen Kaur
 */
public class RequestSecurityTokenResponseImpl extends RequestSecurityTokenResponseType implements RequestSecurityTokenResponse {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);

    private URI tokenType = null;
    
    private long keySize = 0;
    
    private URI keyType = null;
    private URI computedKeyAlgorithm = null;
    private URI signatureAlgorithm = null;
    private URI encryptionAlgorithm = null;
    private URI canonAlgorithm = null;
    
    private Lifetime lifetime = null;
    private Entropy entropy = null;
    private AppliesTo appliesTo = null;
    private Authenticator authenticator = null;
    private UseKey useKey = null;
    private ProofEncryption proofEncryption = null;
    private Encryption encryption = null;
    private DelegateTo delegateTo = null;
    
    private OnBehalfOf obo = null;
    private RequestedSecurityToken requestedSecToken = null;
    private RequestedProofToken requestedProofToken = null;
    private RequestedAttachedReference requestedAttachedReference = null;
    private RequestedUnattachedReference requestedUnattachedReference = null;
    
    private URI signWith = null;
    private URI encryptWith = null;
    private URI authenticationType = null;
    
    private SignChallenge signChallenge = null;
    private SignChallengeResponse signChallengeRes = null;
    
    private boolean forwardable = true;
    private boolean delegatable = false;
    
    private Issuer issuer = null;
    private Renewing renewable = null;
    
    private BinaryExchange binaryExchange = null;
    private AllowPostdating apd = null;
    private Status status = null;
    
    private Policy policy = null;
    private PolicyReference policyRef = null;
    
    private RequestedTokenCancelled rtc = null;
    
    public RequestSecurityTokenResponseImpl() {
        // default empty constructor
    }
    
    public RequestSecurityTokenResponseImpl(URI tokenType,
            URI context,
            RequestedSecurityToken token,
            AppliesTo scopes,
            RequestedAttachedReference attached,
            RequestedUnattachedReference unattached,
            RequestedProofToken proofToken,
            Entropy entropy,
            Lifetime lifetime,
            Status status) {
        
        setTokenType(tokenType);
        if (context != null) { setContext(context.toString()); }
        if (token != null) { setRequestedSecurityToken(token); }
        if (attached!= null) { setRequestedAttachedReference(attached); }
        if (unattached!= null) { setRequestedUnattachedReference(unattached); }
        if (scopes != null) { setAppliesTo(scopes); }
        if (proofToken != null) { setRequestedProofToken(proofToken); }
        if (entropy != null) { setEntropy(entropy); }
        if (lifetime != null) { setLifetime(lifetime); }
        if (status != null) { setStatus(status); }
    }
    
    public URI getTokenType() {
        return tokenType;
    }
    
    public final void setTokenType(final URI tokenType) {
        if (tokenType != null) {
            this.tokenType = tokenType;
            final JAXBElement<String> ttElement =
                    (new ObjectFactory()).createTokenType(tokenType.toString());
            getAny().add(ttElement);
        }
    }
    
    public Lifetime getLifetime() {
        return lifetime;
    }
    
    public final void setLifetime(final Lifetime lifetime) {
        this.lifetime = lifetime;
        final JAXBElement<LifetimeType> ltElement =
                (new ObjectFactory()).createLifetime((LifetimeType)lifetime);
        getAny().add(ltElement);
    }
    
    public RequestedTokenCancelled getRequestedTokenCancelled() {
        return this.rtc;
    }
    
    public final void setRequestedTokenCancelled(final RequestedTokenCancelled rtc) {
        this.rtc = rtc;
        final JAXBElement<RequestedTokenCancelledType> rtcElement =
                (new ObjectFactory()).createRequestedTokenCancelled((RequestedTokenCancelledType)rtc);
        getAny().add(rtcElement);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public final void setStatus(final Status status) {
        this.status = status;
        final JAXBElement<StatusType> sElement =
                (new ObjectFactory()).createStatus((StatusType)status);
        getAny().add(sElement);
    }
    
    public Entropy getEntropy() {
        return entropy;
    }
    
    public final void setEntropy(final Entropy entropy) {
        this.entropy = entropy;
        final JAXBElement<EntropyType> etElement =
                (new ObjectFactory()).createEntropy((EntropyType)entropy);
        getAny().add(etElement);
    }
    
    public final void setAppliesTo(final AppliesTo appliesTo) {
        getAny().add(appliesTo);
        this.appliesTo = appliesTo;
    }
    
    public AppliesTo getAppliesTo() {
        return appliesTo;
    }
    
    public final void setOnBehalfOf(final OnBehalfOf onBehalfOf) {
        obo = onBehalfOf;
    }
    
    public OnBehalfOf getOnBehalfOf() {
        return obo;
    }
    
    public final void setIssuer(final Issuer issuer) {
        this.issuer = issuer;
       /* JAXBElement<EndpointReferenceImpl> eprType =
                (new com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory()).createIssuer((EndpointReferenceImpl)issuer);
        getAny().add(eprType);*/
    }
    
    public Issuer getIssuer() {
        return issuer;
    }
    
    public final void setRenewable(final Renewing renew) {
        renewable = renew;
        final JAXBElement<RenewingType> renewType =
                (new ObjectFactory()).createRenewing((RenewingType)renew);
        getAny().add(renewType);
    }
    
    public Renewing getRenewable() {
        return renewable;
    }
    
    public final void setSignChallenge(final SignChallenge challenge) {
        signChallenge = challenge;
        final JAXBElement<SignChallengeType> challengeType =
                (new ObjectFactory()).createSignChallenge((SignChallengeType)challenge);
        getAny().add(challengeType);
    }
    
    public SignChallenge getSignChallenge() {
        return signChallenge;
    }
    
    public final void setBinaryExchange(final BinaryExchange exchange) {
        binaryExchange = exchange;
        final JAXBElement<BinaryExchangeType> exchangeType =
                (new ObjectFactory()).createBinaryExchange((BinaryExchangeType)exchange);
        getAny().add(exchangeType);
    }
    
    public BinaryExchange getBinaryExchange() {
        return binaryExchange;
    }
    
    public final void setAuthenticationType(final URI uri) {
        this.authenticationType = uri;
        final JAXBElement<String> atElement =
                (new ObjectFactory()).createAuthenticationType(uri.toString());
        getAny().add(atElement);
    }
    
    public URI getAuthenticationType() {
        return authenticationType;
    }
    
    public final void setKeyType(@NotNull final URI keytype) throws WSTrustException {
        
        WSTrustVersion wstVer = new WSTrustVersion10();
        if (! (keytype.toString().equalsIgnoreCase(wstVer.getSymmetricKeyTypeURI())
               || keytype.toString().equalsIgnoreCase(wstVer.getPublicKeyTypeURI())
               || keytype.toString().equalsIgnoreCase(wstVer.getBearerKeyTypeURI()) )){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keytype.toString(), null));
            throw new WSTrustException(LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keytype.toString(), null));
        } else {
            this.keyType = keytype;
            final JAXBElement<String> ktElement =
                    (new ObjectFactory()).createKeyType(keyType.toString());
            getAny().add(ktElement);
        }
    }
    
    public URI getKeyType() {
        return keyType;
    }
    
    public final void setKeySize(@NotNull final long size) {
        keySize = size;
        final JAXBElement<Long> ksElement =  (new ObjectFactory()).createKeySize(size);
        getAny().add(ksElement);
    }
    
    public long getKeySize() {
        return keySize;
    }
    
    public final void setSignatureAlgorithm(@NotNull final URI algorithm) {
        signatureAlgorithm = algorithm;
        final JAXBElement<String> signElement =
                (new ObjectFactory()).createSignatureAlgorithm(algorithm.toString());
        getAny().add(signElement);
    }
    
    public URI getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    public final void setEncryptionAlgorithm(@NotNull final URI algorithm) {
        encryptionAlgorithm = algorithm;
        final JAXBElement<String> encElement =
                (new ObjectFactory()).createEncryptionAlgorithm(algorithm.toString());
        getAny().add(encElement);
    }
    
    public URI getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public final void setCanonicalizationAlgorithm(@NotNull final URI algorithm) {
        canonAlgorithm = algorithm;
        final JAXBElement<String> canonElement =
                (new ObjectFactory()).createCanonicalizationAlgorithm(algorithm.toString());
        getAny().add(canonElement);
    }
    
    public URI getCanonicalizationAlgorithm() {
        return canonAlgorithm;
    }
    
    public final void setUseKey(final UseKey useKey) {
        this.useKey = useKey;
        final JAXBElement<UseKeyType> ukElement =
                (new ObjectFactory()).createUseKey((UseKeyType)useKey);
        getAny().add(ukElement);
    }
    
    public UseKey getUseKey() {
        return useKey;
    }
    
    public final void setProofEncryption(final ProofEncryption proofEncryption) {
        this.proofEncryption = proofEncryption;
        final JAXBElement<ProofEncryptionType> proofElement =
                (new ObjectFactory()).createProofEncryption((ProofEncryptionType)proofEncryption);
        getAny().add(proofElement);
    }
    
    public ProofEncryption getProofEncryption() {
        return proofEncryption;
    }
    
    public final void setComputedKeyAlgorithm(@NotNull final URI algorithm) {
        if (algorithm != null) {
            final String ckaString = algorithm.toString();
            if (!ckaString.equalsIgnoreCase(WSTrustVersion.WS_TRUST_10.getCKHASHalgorithmURI())
            && !ckaString.equalsIgnoreCase(WSTrustVersion.WS_TRUST_10.getCKPSHA1algorithmURI())) {
                throw new RuntimeException("Invalid Computed Key Algorithm specified");
            }
            computedKeyAlgorithm = algorithm;
            final JAXBElement<String> ckaElement =
                    (new ObjectFactory()).createComputedKeyAlgorithm(ckaString);
            getAny().add(ckaElement);
        }
    }
    
    public URI getComputedKeyAlgorithm() {
        return computedKeyAlgorithm;
    }
    
    public final void setEncryption(final Encryption enc) {
        this.encryption = enc;
        final JAXBElement<EncryptionType> encElement =
                (new ObjectFactory()).createEncryption((EncryptionType)enc);
        getAny().add(encElement);
    }
    
    public Encryption getEncryption() {
        return encryption;
    }
    
    public final void setSignWith(final URI algorithm) {
        signWith = algorithm;
        final JAXBElement<String> sElement =  (new ObjectFactory()).createSignWith(algorithm.toString());
        getAny().add(sElement);
    }
    
    public URI getSignWith() {
        return signWith;
    }
    
    public final void setEncryptWith(@NotNull final URI algorithm) {
        encryptWith = algorithm;
        final JAXBElement<String> sElement =  (new ObjectFactory()).createEncryptWith(algorithm.toString());
        getAny().add(sElement);
    }
    
    public URI getEncryptWith() {
        return encryptWith;
    }
    
    public void setKeyWrapAlgorithm(URI algorithm) {
        throw new UnsupportedOperationException("KeyWrapAlgorithm element in WS-Trust Standard version(1.0) is not supported");
    }
    
    public URI getKeyWrapAlgorithm() {
        throw new UnsupportedOperationException("KeyWrapAlgorithm element in WS-Trust Standard version(1.0) is not supported");
    }
    
    public final void setDelegateTo(final DelegateTo to) {
        this.delegateTo = to;
        final JAXBElement<DelegateToType> dtElement =
                (new ObjectFactory()).createDelegateTo((DelegateToType)to);
        getAny().add(dtElement);
    }
    
    public DelegateTo getDelegateTo() {
        return delegateTo;
    }
    
    public final void setForwardable(final boolean flag) {
        forwardable = flag;
        final JAXBElement<Boolean> forward =
                (new ObjectFactory()).createForwardable(flag);
        getAny().add(forward);
    }
    
    public boolean getForwardable() {
        return forwardable;
    }
    
    public final void setDelegatable(final boolean flag) {
        this.delegatable = flag;
        final JAXBElement<Boolean> del =
                (new ObjectFactory()).createDelegatable(flag);
        getAny().add(del);
    }
    
    public boolean getDelegatable() {
        return delegatable;
    }
    
    public final void setPolicy(final Policy policy) {
        this.policy = policy;
        getAny().add(policy);
    }
    
    public Policy getPolicy() {
        return policy;
    }
    
    public final void setPolicyReference(final PolicyReference policyRef) {
        this.policyRef = policyRef;
        getAny().add(policyRef);
    }
    
    public PolicyReference getPolicyReference() {
        return policyRef;
    }
    
    public AllowPostdating getAllowPostdating() {
        return apd;
    }
    
    public final void setAllowPostdating(final AllowPostdating allowPostdating) {
        apd = allowPostdating;
        final JAXBElement<AllowPostdatingType> allowPd =
                (new ObjectFactory()).createAllowPostdating((AllowPostdatingType)apd);
        getAny().add(allowPd);
    }
    
    public final void setSignChallengeResponse(final SignChallengeResponse challenge) {
        signChallengeRes = challenge;
        final JAXBElement<SignChallengeType> challengeType =
                (new ObjectFactory()).createSignChallengeResponse((SignChallengeType)challenge);
        getAny().add(challengeType);
    }
    
    public SignChallengeResponse getSignChallengeResponse() {
        return signChallengeRes;
    }
    
    public final void setAuthenticator(final Authenticator authenticator) {
        this.authenticator = authenticator;
        final JAXBElement<AuthenticatorType> authType =
                (new ObjectFactory()).createAuthenticator((AuthenticatorType)authenticator);
        getAny().add(authType);
    }
    
    public Authenticator getAuthenticator() {
        return authenticator;
    }
    
    public final void setRequestedProofToken(final RequestedProofToken proofToken) {
        requestedProofToken = proofToken;
        final JAXBElement<RequestedProofTokenType> pElement =  (new ObjectFactory()).
                createRequestedProofToken((RequestedProofTokenType)proofToken);
        getAny().add(pElement);
    }
    
    public RequestedProofToken getRequestedProofToken() {
        return requestedProofToken;
    }
    
    public final void setRequestedSecurityToken(final RequestedSecurityToken securityToken) {
        requestedSecToken = securityToken;
        final JAXBElement<RequestedSecurityTokenType> rstElement =  (new ObjectFactory()).
                createRequestedSecurityToken((RequestedSecurityTokenType)securityToken);
        
        getAny().add(rstElement);
    }
    
    public RequestedSecurityToken getRequestedSecurityToken() {
        return requestedSecToken;
    }
    
    public final void setRequestedAttachedReference(final RequestedAttachedReference reference) {
        requestedAttachedReference = reference;
        final JAXBElement<RequestedReferenceType> raElement =  (new ObjectFactory()).
                createRequestedAttachedReference((RequestedReferenceType)reference);
        getAny().add(raElement);
    }
    
    public RequestedAttachedReference getRequestedAttachedReference() {
        return requestedAttachedReference;
    }
    
    public final void setRequestedUnattachedReference(final RequestedUnattachedReference reference) {
        requestedUnattachedReference = reference;
        final JAXBElement<RequestedReferenceType> raElement =  (new ObjectFactory()).
                createRequestedUnattachedReference((RequestedReferenceType)reference);
        getAny().add(raElement);
    }
    
    public RequestedUnattachedReference getRequestedUnattachedReference() {
        return requestedUnattachedReference;
    }
    
    public RequestSecurityTokenResponseImpl(RequestSecurityTokenResponseType rstrType)
    throws URISyntaxException,WSTrustException {
        
        this.context = rstrType.getContext();
        final List<Object> list = rstrType.getAny();
        for (int i = 0; i < list.size(); i++) {
            
            if(list.get(i) instanceof AppliesTo){
                setAppliesTo((AppliesTo)list.get(i));
                continue;
            }
            
            Object object = list.get(i);
            if (!(object instanceof JAXBElement)){
                getAny().add(object);
            } else {
                JAXBElement obj = (JAXBElement)list.get(i);
                final String local = obj.getName().getLocalPart();
                if (local.equalsIgnoreCase("KeySize")) {
                    setKeySize((Long)obj.getValue());
                } else if (local.equalsIgnoreCase("KeyType")){
                    setKeyType(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("ComputedKeyAlgorithm")){
                    setComputedKeyAlgorithm(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("TokenType")){
                    setTokenType(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("AuthenticationType")){
                    setAuthenticationType(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("Lifetime")){
                    final LifetimeType ltType = (LifetimeType)obj.getValue();
                    setLifetime(new LifetimeImpl(ltType));
                } else if (local.equalsIgnoreCase("Entropy")){
                    final EntropyType eType = (EntropyType)obj.getValue();
                    setEntropy(new EntropyImpl(eType));
                } else if (local.equalsIgnoreCase("Forwardable")){
                    setForwardable((Boolean)obj.getValue());
                } else if (local.equalsIgnoreCase("Delegatable")){
                    setDelegatable((Boolean)obj.getValue());
                } else if (local.equalsIgnoreCase("SignWith")){
                    setSignWith(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("EncryptWith")){
                    setEncryptWith(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("SignatureAlgorithm")){
                    setSignatureAlgorithm(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("EncryptionAlgorithm")){
                    setEncryptionAlgorithm(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("CanonicalizationAlgorithm")){
                    setCanonicalizationAlgorithm(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("AllowPostdating")){
                    setAllowPostdating(new AllowPostdatingImpl());
                } else if (local.equalsIgnoreCase("SignChallenge")){
                    setSignChallenge(new SignChallengeImpl());
                } else if (local.equalsIgnoreCase("SignChallengeResponse")){
                     setSignChallengeResponse(new SignChallengeResponseImpl());
                } else if (local.equalsIgnoreCase("BinaryExchange")){
                    final BinaryExchangeType bcType = (BinaryExchangeType)obj.getValue();
                    setBinaryExchange(new BinaryExchangeImpl(bcType));
                } else if (local.equalsIgnoreCase("Issuer")){
                    /* EndpointReferenceImpl isType = (EndpointReferenceImpl)obj.getValue();
                    setIssuer(new IssuerImpl(isType));*/
                } else if (local.equalsIgnoreCase("Authenticator")){
                    final AuthenticatorType aType = (AuthenticatorType)obj.getValue();
                    setAuthenticator(new AuthenticatorImpl(aType));
                } else if (local.equalsIgnoreCase("Renewing")){
                    setRenewable(new RenewingImpl());
                } else if (local.equalsIgnoreCase("ProofEncryption")){
                    final ProofEncryptionType peType = (ProofEncryptionType)obj.getValue();
                    setProofEncryption(new ProofEncryptionImpl(peType));
                } else if (local.equalsIgnoreCase("Policy")){
                    setPolicy((Policy)obj.getValue());
                } else if (local.equalsIgnoreCase("PolicyReference")){
                    setPolicyReference((PolicyReference)obj.getValue());
                } else if (local.equalsIgnoreCase("AppliesTo")){
                    setAppliesTo((AppliesTo)obj.getValue());
                } else if (local.equalsIgnoreCase("OnBehalfOf")){
                    this.obo = (OnBehalfOf)obj.getValue();
                } else if (local.equalsIgnoreCase("Encryption")){
                    final EncryptionType encType = (EncryptionType)obj.getValue();
                    setEncryption(new EncryptionImpl(encType));
                } else if (local.equalsIgnoreCase("UseKey")){
                    final UseKeyType ukType = (UseKeyType)obj.getValue();
                    setUseKey(new UseKeyImpl(ukType));
                } else if (local.equalsIgnoreCase("Status")){
                    final StatusType sType = (StatusType)obj.getValue();
                    setStatus(new StatusImpl(sType));
                } else if (local.equalsIgnoreCase("DelegateTo")){
                    final DelegateToType dtType  = (DelegateToType)obj.getValue();
                    setDelegateTo(new DelegateToImpl(dtType));
                } else if (local.equalsIgnoreCase("RequestedProofToken")){
                    final RequestedProofTokenType rptType = (RequestedProofTokenType)obj.getValue();
                    setRequestedProofToken(new RequestedProofTokenImpl(rptType));
                } else if (local.equalsIgnoreCase("RequestedSecurityToken")){
                    final RequestedSecurityTokenType rdstType = (RequestedSecurityTokenType)obj.getValue();
                    setRequestedSecurityToken(new RequestedSecurityTokenImpl(rdstType));
                } else if (local.equalsIgnoreCase("RequestedAttachedReference")){
                    final RequestedReferenceType rarType = (RequestedReferenceType)obj.getValue();
                    setRequestedAttachedReference(new RequestedAttachedReferenceImpl(rarType));
                } else if (local.equalsIgnoreCase("RequestedUnattachedReference")){
                    final RequestedReferenceType rarType = (RequestedReferenceType)obj.getValue();
                    setRequestedUnattachedReference(new RequestedUnattachedReferenceImpl(rarType));
                } else if (local.equalsIgnoreCase("RequestedTokenCancelled")){
                    setRequestedTokenCancelled(new RequestedTokenCancelledImpl());
                } else {
                    getAny().add(obj.getValue());
                }
            }
        }
    }
    
}

