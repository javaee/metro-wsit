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
 * $Id: RequestSecurityTokenResponseImpl.java,v 1.2 2010-10-21 15:37:05 snajper Exp $
 */

package com.sun.xml.ws.security.trust.impl.wssx.elements;

import java.util.List;
import java.util.Map;

import java.net.URI;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.impl.bindings.PolicyReference;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.security.trust.Status;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustVersion;

import com.sun.xml.ws.security.trust.elements.*;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;

import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.impl.wssx.WSTrustVersion13;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.AllowPostdatingType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.AuthenticatorType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.BinaryExchangeType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.DelegateToType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.EncryptionType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.LifetimeType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestedReferenceType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestedProofTokenType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestedSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestedTokenCancelledType;

import com.sun.xml.ws.security.trust.impl.wssx.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.ProofEncryptionType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RenewingType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.SignChallengeType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.StatusType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.UseKeyType;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
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
    private URI keyWrapAlgorithm = null;
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
        if (context != null) setContext(context.toString());
        if (token != null) setRequestedSecurityToken(token);
        if (attached!= null) setRequestedAttachedReference(attached);
        if (unattached!= null) setRequestedUnattachedReference(unattached);
        if (scopes != null) setAppliesTo(scopes);
        if (proofToken != null) setRequestedProofToken(proofToken);
        if (entropy != null) setEntropy(entropy);
        if (lifetime != null) setLifetime(lifetime);
        if (status != null) setStatus(status);
    }
    
    public URI getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(URI tokenType) {
        if (tokenType != null) {
            this.tokenType = tokenType;
            JAXBElement<String> ttElement =
                    (new ObjectFactory()).createTokenType(tokenType.toString());
            getAny().add(ttElement);
        }
    }
    
    public  Map<QName, String> getOtherAttributes() {
        return super.getOtherAttributes();
    }
    
    public Lifetime getLifetime() {
        return lifetime;
    }
    
    public void setLifetime(Lifetime lifetime) {
        this.lifetime = lifetime;
        JAXBElement<LifetimeType> ltElement =
                (new ObjectFactory()).createLifetime((LifetimeType)lifetime);
        getAny().add(ltElement);
    }
    
    public RequestedTokenCancelled getRequestedTokenCancelled() {
        return this.rtc;
    }
    
    public void setRequestedTokenCancelled(RequestedTokenCancelled rtc) {
        this.rtc = rtc;
        JAXBElement<RequestedTokenCancelledType> rtcElement =
                (new ObjectFactory()).createRequestedTokenCancelled((RequestedTokenCancelledType)rtc);
        getAny().add(rtcElement);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        JAXBElement<StatusType> sElement =
                (new ObjectFactory()).createStatus((StatusType)status);
        getAny().add(sElement);
    }
    
    public Entropy getEntropy() {
        return entropy;
    }
    
    public void setEntropy(Entropy entropy) {
        this.entropy = entropy;
        JAXBElement<EntropyType> etElement =
                (new ObjectFactory()).createEntropy((EntropyType)entropy);
        getAny().add(etElement);
    }
    
    public List<Object> getAny() {
        return super.getAny();
    }
    
    public String getContext() {
        return super.getContext();
    }
    
    public void setAppliesTo(AppliesTo appliesTo) {
        getAny().add(appliesTo);
        this.appliesTo = appliesTo;
    }
    
    public AppliesTo getAppliesTo() {
        return appliesTo;
    }
    
    public void setOnBehalfOf(OnBehalfOf onBehalfOf) {
        obo = onBehalfOf;
    }
    
    public OnBehalfOf getOnBehalfOf() {
        return obo;
    }
    
    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
//        JAXBElement<EndpointReferenceImpl> eprType =
//                (new com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory()).createIssuer((EndpointReferenceImpl)issuer);
//        getAny().add(eprType);
    }
    
    public Issuer getIssuer() {
        return issuer;
    }
    
    public void setRenewable(Renewing renew) {
        renewable = renew;
        JAXBElement<RenewingType> renewType =
                (new ObjectFactory()).createRenewing((RenewingType)renew);
        getAny().add(renewType);
    }
    
    public Renewing getRenewable() {
        return renewable;
    }
    
    public void setSignChallenge(SignChallenge challenge) {
        signChallenge = challenge;
        JAXBElement<SignChallengeType> challengeType =
                (new ObjectFactory()).createSignChallenge((SignChallengeType)challenge);
        getAny().add(challengeType);
    }
    
    public SignChallenge getSignChallenge() {
        return signChallenge;
    }
    
    public void setBinaryExchange(BinaryExchange exchange) {
        binaryExchange = exchange;
        JAXBElement<BinaryExchangeType> exchangeType =
                (new ObjectFactory()).createBinaryExchange((BinaryExchangeType)exchange);
        getAny().add(exchangeType);
    }
    
    public BinaryExchange getBinaryExchange() {
        return binaryExchange;
    }
    
    public void setAuthenticationType(URI uri) {
        this.authenticationType = uri;
        JAXBElement<String> atElement =
                (new ObjectFactory()).createAuthenticationType(uri.toString());
        getAny().add(atElement);
    }
    
    public URI getAuthenticationType() {
        return authenticationType;
    }
    
    public void setKeyType(URI keytype) throws WSTrustException {
        WSTrustVersion wstVer = new WSTrustVersion13();
        if (! (keytype.toString().equalsIgnoreCase(wstVer.getSymmetricKeyTypeURI())
               || keytype.toString().equalsIgnoreCase(wstVer.getPublicKeyTypeURI())
               || keytype.toString().equalsIgnoreCase(wstVer.getBearerKeyTypeURI()) )){
             log.log(Level.SEVERE,
                    LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keytype.toString(), null));
            throw new WSTrustException(LogStringsMessages.WST_0025_INVALID_KEY_TYPE(keytype.toString(), null));
        } else {
            this.keyType = keytype;
            JAXBElement<String> ktElement =
                    (new ObjectFactory()).createKeyType(keyType.toString());
            getAny().add(ktElement);
        }
    }
    
    public URI getKeyType() {
        return keyType;
    }
    
    public void setKeySize(long size) {
        keySize = size;
        JAXBElement<Long> ksElement =  (new ObjectFactory()).createKeySize(size);
        getAny().add(ksElement);
    }
    
    public long getKeySize() {
        return keySize;
    }
    
    public void setSignatureAlgorithm(URI algorithm) {
        signatureAlgorithm = algorithm;
        JAXBElement<String> signElement =
                (new ObjectFactory()).createSignatureAlgorithm(algorithm.toString());
        getAny().add(signElement);
    }
    
    public URI getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    public void setEncryptionAlgorithm(URI algorithm) {
        encryptionAlgorithm = algorithm;
        JAXBElement<String> encElement =
                (new ObjectFactory()).createEncryptionAlgorithm(algorithm.toString());
        getAny().add(encElement);
    }
    
    public URI getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }
    
    public void setCanonicalizationAlgorithm(URI algorithm) {
        canonAlgorithm = algorithm;
        JAXBElement<String> canonElement =
                (new ObjectFactory()).createCanonicalizationAlgorithm(algorithm.toString());
        getAny().add(canonElement);
    }
    
    public URI getCanonicalizationAlgorithm() {
        return canonAlgorithm;
    }
    
    public void setUseKey(UseKey useKey) {
        this.useKey = useKey;
        JAXBElement<UseKeyType> ukElement =
                (new ObjectFactory()).createUseKey((UseKeyType)useKey);
        getAny().add(ukElement);
    }
    
    public UseKey getUseKey() {
        return useKey;
    }
    
    public void setProofEncryption(ProofEncryption proofEncryption) {
        this.proofEncryption = proofEncryption;
        JAXBElement<ProofEncryptionType> proofElement =
                (new ObjectFactory()).createProofEncryption((ProofEncryptionType)proofEncryption);
        getAny().add(proofElement);
    }
    
    public ProofEncryption getProofEncryption() {
        return proofEncryption;
    }
    
    public void setComputedKeyAlgorithm(URI algorithm) {
        if (algorithm != null) {
            String ckaString = algorithm.toString();
            if (!ckaString.equalsIgnoreCase(WSTrustVersion.WS_TRUST_13.getCKHASHalgorithmURI())
            && !ckaString.equalsIgnoreCase(WSTrustVersion.WS_TRUST_13.getCKPSHA1algorithmURI())) {
                throw new RuntimeException("Invalid Computed Key Algorithm specified");
            }
            computedKeyAlgorithm = algorithm;
            JAXBElement<String> ckaElement =
                    (new ObjectFactory()).createComputedKeyAlgorithm(ckaString);
            getAny().add(ckaElement);
        }
    }
    
    public URI getComputedKeyAlgorithm() {
        return computedKeyAlgorithm;
    }
    
    public void setEncryption(Encryption enc) {
        this.encryption = enc;
        JAXBElement<EncryptionType> encElement =
                (new ObjectFactory()).createEncryption((EncryptionType)enc);
        getAny().add(encElement);
    }
    
    public Encryption getEncryption() {
        return encryption;
    }
    
    public void setSignWith(URI algorithm) {
        signWith = algorithm;
        JAXBElement<String> sElement =  (new ObjectFactory()).createSignWith(algorithm.toString());
        getAny().add(sElement);
    }
    
    public URI getSignWith() {
        return signWith;
    }
    
    public void setEncryptWith(URI algorithm) {
        encryptWith = algorithm;
        JAXBElement<String> sElement =  (new ObjectFactory()).createEncryptWith(algorithm.toString());
        getAny().add(sElement);
    }
    
    public URI getEncryptWith() {
        return encryptWith;
    }
    
    public void setKeyWrapAlgorithm(URI algorithm) {
        keyWrapAlgorithm = algorithm;
        JAXBElement<String> keyWrapElement =
                (new ObjectFactory()).createKeyWrapAlgorithm(algorithm.toString());
        getAny().add(keyWrapElement);
    }
    
    public URI getKeyWrapAlgorithm() {
        return keyWrapAlgorithm;
    }
    
    public void setDelegateTo(DelegateTo to) {
        this.delegateTo = to;
        JAXBElement<DelegateToType> dtElement =
                (new ObjectFactory()).createDelegateTo((DelegateToType)to);
        getAny().add(dtElement);
    }
    
    public DelegateTo getDelegateTo() {
        return delegateTo;
    }
    
    public void setForwardable(boolean flag) {
        forwardable = flag;
        JAXBElement<Boolean> forward =
                (new ObjectFactory()).createForwardable(flag);
        getAny().add(forward);
    }
    
    public boolean getForwardable() {
        return forwardable;
    }
    
    public void setDelegatable(boolean flag) {
        this.delegatable = flag;
        JAXBElement<Boolean> del =
                (new ObjectFactory()).createDelegatable(flag);
        getAny().add(del);
    }
    
    public boolean getDelegatable() {
        return delegatable;
    }
    
    public void setPolicy(Policy policy) {
        this.policy = policy;
        getAny().add(policy);
    }
    
    public Policy getPolicy() {
        return policy;
    }
    
    public void setPolicyReference(PolicyReference policyRef) {
        this.policyRef = policyRef;
        getAny().add(policyRef);
    }
    
    public PolicyReference getPolicyReference() {
        return policyRef;
    }
    
    public AllowPostdating getAllowPostdating() {
        return apd;
    }
    
    public void setAllowPostdating(AllowPostdating allowPostdating) {
        apd = allowPostdating;
        JAXBElement<AllowPostdatingType> allowPd =
                (new ObjectFactory()).createAllowPostdating((AllowPostdatingType)apd);
        getAny().add(allowPd);
    }
    
    public void setSignChallengeResponse(SignChallengeResponse challenge) {
        signChallengeRes = challenge;
        JAXBElement<SignChallengeType> challengeType =
                (new ObjectFactory()).createSignChallengeResponse((SignChallengeType)challenge);
        getAny().add(challengeType);
    }
    
    public SignChallengeResponse getSignChallengeResponse() {
        return signChallengeRes;
    }
    
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
        JAXBElement<AuthenticatorType> authType =
                (new ObjectFactory()).createAuthenticator((AuthenticatorType)authenticator);
        getAny().add(authType);
    }
    
    public Authenticator getAuthenticator() {
        return authenticator;
    }
    
    public void setRequestedProofToken(RequestedProofToken proofToken) {
        requestedProofToken = proofToken;
        JAXBElement<RequestedProofTokenType> pElement =  (new ObjectFactory()).
                createRequestedProofToken((RequestedProofTokenType)proofToken);
        getAny().add(pElement);
    }
    
    public RequestedProofToken getRequestedProofToken() {
        return requestedProofToken;
    }
    
    public void setRequestedSecurityToken(RequestedSecurityToken securityToken) {
        requestedSecToken = securityToken;
        JAXBElement<RequestedSecurityTokenType> rstElement =  (new ObjectFactory()).
                createRequestedSecurityToken((RequestedSecurityTokenType)securityToken);
        
        getAny().add(rstElement);
    }
    
    public RequestedSecurityToken getRequestedSecurityToken() {
        return requestedSecToken;
    }
    
    public void setRequestedAttachedReference(RequestedAttachedReference reference) {
        requestedAttachedReference = reference;
        JAXBElement<RequestedReferenceType> raElement =  (new ObjectFactory()).
                createRequestedAttachedReference((RequestedReferenceType)reference);
        getAny().add(raElement);
    }
    
    public RequestedAttachedReference getRequestedAttachedReference() {
        return requestedAttachedReference;
    }
    
    public void setRequestedUnattachedReference(RequestedUnattachedReference reference) {
        requestedUnattachedReference = reference;
        JAXBElement<RequestedReferenceType> raElement =  (new ObjectFactory()).
                createRequestedUnattachedReference((RequestedReferenceType)reference);
        getAny().add(raElement);
    }
    
    public RequestedUnattachedReference getRequestedUnattachedReference() {
        return requestedUnattachedReference;
    }
    
    public RequestSecurityTokenResponseImpl(RequestSecurityTokenResponseType rstrType)
    throws Exception {
        
        this.context = rstrType.getContext();
        List<Object> list = rstrType.getAny();
        for (int i = 0; i < list.size(); i++) {
            
            if(list.get(i) instanceof AppliesTo){
                setAppliesTo((AppliesTo)list.get(i));
                continue;
            }
            
            if (list.get(i) instanceof JAXBElement){
                
                JAXBElement obj = (JAXBElement)list.get(i);

                String local = obj.getName().getLocalPart();
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
                    LifetimeType ltType = (LifetimeType)obj.getValue();
                    setLifetime(new LifetimeImpl(ltType));
                } else if (local.equalsIgnoreCase("Entropy")){
                    EntropyType eType = (EntropyType)obj.getValue();
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
                    BinaryExchangeType bcType = (BinaryExchangeType)obj.getValue();
                    setBinaryExchange(new BinaryExchangeImpl(bcType));
                } else if (local.equalsIgnoreCase("Issuer")){
//                    EndpointReferenceImpl isType = (EndpointReferenceImpl)obj.getValue();
//                    setIssuer(new IssuerImpl(isType));
                } else if (local.equalsIgnoreCase("Authenticator")){
                    AuthenticatorType aType = (AuthenticatorType)obj.getValue();
                    setAuthenticator(new AuthenticatorImpl(aType));
                } else if (local.equalsIgnoreCase("Renewing")){
                    setRenewable(new RenewingImpl());
                } else if (local.equalsIgnoreCase("ProofEncryption")){
                    ProofEncryptionType peType = (ProofEncryptionType)obj.getValue();
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
                    EncryptionType encType = (EncryptionType)obj.getValue();
                    setEncryption(new EncryptionImpl(encType));
                } else if (local.equalsIgnoreCase("UseKey")){
                    UseKeyType ukType = (UseKeyType)obj.getValue();
                    setUseKey(new UseKeyImpl(ukType));
                } else if (local.equalsIgnoreCase("Status")){
                    final StatusType sType = (StatusType)obj.getValue();
                    setStatus(new StatusImpl(sType));
                } else if (local.equalsIgnoreCase("DelegateTo")){
                    DelegateToType dtType  = (DelegateToType)obj.getValue();
                    setDelegateTo(new DelegateToImpl(dtType));
                } else if (local.equalsIgnoreCase("RequestedProofToken")){
                    RequestedProofTokenType rptType = (RequestedProofTokenType)obj.getValue();
                    setRequestedProofToken(new RequestedProofTokenImpl(rptType));
                } else if (local.equalsIgnoreCase("RequestedSecurityToken")){
                    RequestedSecurityTokenType rdstType = (RequestedSecurityTokenType)obj.getValue();
                    setRequestedSecurityToken(new RequestedSecurityTokenImpl(rdstType));
                } else if (local.equalsIgnoreCase("RequestedAttachedReference")){
                    RequestedReferenceType rarType = (RequestedReferenceType)obj.getValue();
                    setRequestedAttachedReference(new RequestedAttachedReferenceImpl(rarType));
                } else if (local.equalsIgnoreCase("RequestedUnattachedReference")){
                    RequestedReferenceType rarType = (RequestedReferenceType)obj.getValue();
                    setRequestedUnattachedReference(new RequestedUnattachedReferenceImpl(rarType));
                } else if (local.equalsIgnoreCase("RequestedTokenCancelled")){
                    setRequestedTokenCancelled(new RequestedTokenCancelledImpl());
                } 
            }else{
                getAny().add(list.get(i));
            }
        }
    }
    
}

