/*
 * $Id: RequestSecurityTokenImpl.java,v 1.5 2006-10-05 00:19:10 jdg6688 Exp $
 */

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

package com.sun.xml.ws.security.trust.impl.elements;

import java.util.List;

import java.net.URI;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.impl.bindings.PolicyReference;
import javax.xml.bind.JAXBElement;

import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustException;

import com.sun.xml.ws.security.trust.elements.*;
import com.sun.xml.ws.security.trust.elements.Claims;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.Participants;

import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.impl.bindings.AllowPostdatingType;
import com.sun.xml.ws.security.trust.impl.bindings.BinaryExchangeType;
import com.sun.xml.ws.security.trust.impl.bindings.CancelTargetType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.LifetimeType;
import com.sun.xml.ws.security.trust.impl.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.bindings.ClaimsType;
import com.sun.xml.ws.security.trust.impl.bindings.DelegateToType;
import com.sun.xml.ws.security.trust.impl.bindings.EncryptionType;
import com.sun.xml.ws.security.trust.impl.bindings.OnBehalfOfType;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.bindings.ParticipantsType;
import com.sun.xml.ws.security.trust.impl.bindings.ProofEncryptionType;
import com.sun.xml.ws.security.trust.impl.bindings.RenewTargetType;
import com.sun.xml.ws.security.trust.impl.bindings.RenewingType;
import com.sun.xml.ws.security.trust.impl.bindings.SignChallengeType;
import com.sun.xml.ws.security.trust.impl.bindings.UseKeyType;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

/**
 * Implementation of the RequestSecurityToken interface.
 *
 * @author Manveen Kaur
 */
public class RequestSecurityTokenImpl  extends RequestSecurityTokenType
        implements RequestSecurityToken {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private Claims claims = null;
    private Participants participants = null;
    private URI tokenType = null;
    
    private URI requestType = null;
    
    private long keySize = 0;
    private URI keyType = null;
    private URI computedKeyAlgorithm = null;
    
    private URI signWith = null;
    private URI encryptWith = null;
    private URI authenticationType = null;
    private URI signatureAlgorithm = null;
    private URI encryptionAlgorithm = null;
    private URI canonAlgorithm = null;
    
    private Lifetime lifetime = null;
    private Entropy entropy = null;
    private AppliesTo appliesTo = null;
    private OnBehalfOf obo = null;
    private SignChallenge signChallenge = null;
    private Encryption encryption = null;
    private UseKey useKey = null;
    private DelegateTo delegateTo = null;
    private RenewTarget renewTarget = null;
    private CancelTarget cancelTarget = null;
    
    private AllowPostdating apd = null;
    private BinaryExchange binaryExchange = null;
    private Issuer issuer = null;
    private Renewing renewable = null;
    private ProofEncryption proofEncryption = null;
    
    private boolean forwardable = true;
    private boolean delegatable = false;
    
    private Policy policy = null;
    private PolicyReference policyRef = null;
    
    public RequestSecurityTokenImpl() {
        setRequestType(URI.create(WSTrustConstants.ISSUE_REQUEST));
    }
    
    public RequestSecurityTokenImpl(URI tokenType, URI requestType) {
        setTokenType(tokenType);
        setRequestType(requestType);
    }
    
    public RequestSecurityTokenImpl(URI tokenType, URI requestType,
            URI context, AppliesTo scopes,
            Claims claims, Entropy entropy,
            Lifetime lt, URI algorithm) {
        setTokenType(tokenType);
        setRequestType(requestType);
        if (context != null) {
            setContext(context.toString());
        }
        if (scopes != null) {
            setAppliesTo(scopes);
        }
        if (claims != null) {
            setClaims(claims);
        }
        if (entropy !=null)
            setEntropy(entropy);
        if (lt!=null)
            setLifetime(lt);
        if (algorithm !=null)
            setComputedKeyAlgorithm(algorithm);
    }
    
    public RequestSecurityTokenImpl(URI tokenType, URI requestType, URI context,
            RenewTarget target, AllowPostdating apd, Renewing renewingInfo) {
        setTokenType(tokenType);
        setRequestType(requestType);
        if (context != null) {
            setContext(context.toString());
        }
        if (context != null) {
            setContext(context.toString());
        }
        if (target != null) {
            setRenewTarget(target);
        }
        if (apd != null) {
            setAllowPostdating(apd);
        }
        if (renewingInfo != null) {
            setRenewable(renewingInfo);
        }
    }
    
    public RequestSecurityTokenImpl(URI tokenType, URI requestType, CancelTarget cancel) {
        setTokenType(tokenType);
        setRequestType(requestType);
        setCancelTarget(cancel);
    }
    
    public String getContext() {
        return super.getContext();
    }
    
    public void setClaims(Claims claims) {
        this.claims = claims;
        JAXBElement<ClaimsType> cElement =
                (new ObjectFactory()).createClaims((ClaimsType)claims);
        getAny().add(cElement);
    }
    
    public Claims getClaims() {
        return claims;
    }
    
    public void setCancelTarget(CancelTarget cTarget) {
        this.cancelTarget = cTarget;
        JAXBElement<CancelTargetType> ctElement =
                (new ObjectFactory()).createCancelTarget((CancelTargetType)cTarget);
        getAny().add(ctElement);
    }
    
    public CancelTarget getCancelTarget() {
        return cancelTarget;
    }
    
    public void setRenewTarget(RenewTarget target) {
        this.renewTarget = target;
        JAXBElement<RenewTargetType> rElement =
                (new ObjectFactory()).createRenewTarget((RenewTargetType)target);
        getAny().add(rElement);
    }
    
    public RenewTarget getRenewTarget() {
        return renewTarget;
    }
    
    public void setParticipants(Participants participants) {
        this.participants = participants;
        JAXBElement<ParticipantsType> rElement =
                (new ObjectFactory()).createParticipants((ParticipantsType)participants);
        getAny().add(rElement);
    }
    
    public Participants getParticipants() {
        return participants;
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
    
    public URI getRequestType() {
        return requestType;
    }
    
    public void setRequestType(URI requestType) {
        if (requestType == null) {
            log.log(Level.SEVERE,"WST0024.invalid.request.type", "null");
            throw new RuntimeException("RequestType cannot be null");
        }
        String rtString = requestType.toString();
        if (!rtString.equalsIgnoreCase(WSTrustConstants.ISSUE_REQUEST)
        && !rtString.equalsIgnoreCase(WSTrustConstants.CANCEL_REQUEST)
        && !rtString.equalsIgnoreCase(WSTrustConstants.KEY_EXCHANGE_REQUEST)
        && !rtString.equalsIgnoreCase(WSTrustConstants.RENEW_REQUEST)
        && !rtString.equalsIgnoreCase(WSTrustConstants.VALIDATE_REQUEST)) {
            log.log(Level.SEVERE,"WST0024.invalid.request.type", rtString);
            throw new RuntimeException("Invalid Request Type specified");
        }
        this.requestType = requestType;
        JAXBElement<String> rtElement =
                (new ObjectFactory()).createRequestType(rtString);
        getAny().add(rtElement);
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
    
    public Entropy getEntropy() {
        return entropy;
    }
    
    public void setEntropy(Entropy entropy) {
        this.entropy = entropy;
        JAXBElement<EntropyType> etElement =
                (new ObjectFactory()).createEntropy((EntropyType)entropy);
        getAny().add(etElement);
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
        /*JAXBElement<EndpointReferenceImpl> eprType =
                (new com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory()).createIssuer((EndpointReferenceImpl)issuer);
        getAny().add(eprType);*/
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
        
        if (keytype == null) {
            log.log(Level.SEVERE,"WST0025.invalid.key.type", "null");
            throw new WSTrustException("Invalid KeyType: null");
        }
        
        if (! (keytype.toString().equalsIgnoreCase(RequestSecurityToken.PUBLIC_KEY_TYPE)
        || keytype.toString().equalsIgnoreCase(RequestSecurityToken.SYMMETRIC_KEY_TYPE) )){
            log.log(Level.SEVERE,"WST0025.invalid.key.type", keytype.toString());
            throw new WSTrustException("Invalid KeyType " + keytype.toString());
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
        if (algorithm == null) {
            log.log(Level.SEVERE,"WST0026.invalid.ck.algorithm", "null");
            throw new RuntimeException("Null Computed Key Algorithm specified");
        }
        
        if (algorithm != null) {
            String ckaString = algorithm.toString();
            if (!ckaString.equalsIgnoreCase(WSTrustConstants.CK_HASH)
            && !ckaString.equalsIgnoreCase(WSTrustConstants.CK_PSHA1)) {
                log.log(Level.SEVERE,"WST0026.invalid.ck.algorithm", ckaString);
                throw new RuntimeException("Invalid Computed Key Algorithm specified :" + ckaString);
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
        delegatable = flag;
        JAXBElement<Boolean> del =
                (new ObjectFactory()).createDelegatable(flag);
        getAny().add(del);
    }
    
    public  boolean getDelegatable() {
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
    
    public RequestSecurityTokenImpl(RequestSecurityTokenType rstType)
    throws Exception {
        this.context = rstType.getContext();
        List<Object> list = rstType.getAny();
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) instanceof AppliesTo){
                setAppliesTo((AppliesTo)list.get(i));
                continue;
            }
            if (list.get(i) instanceof JAXBElement){
                JAXBElement obj = (JAXBElement)list.get(i);
                
                String local = obj.getName().getLocalPart();
                if (local.equalsIgnoreCase("RequestType")) {
                    setRequestType(new URI((String)obj.getValue()));
                } else if (local.equalsIgnoreCase("KeySize")) {
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
                    setLifetime(new LifetimeImpl(ltType.getCreated(), ltType.getExpires()));
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
                }  else if (local.equalsIgnoreCase("SignChallenge")){
                    setSignChallenge(new SignChallengeImpl());
                }else if (local.equalsIgnoreCase("BinaryExchange")){
                    BinaryExchangeType bcType = (BinaryExchangeType)obj.getValue();
                    setBinaryExchange(new BinaryExchangeImpl(bcType));
                } else if (local.equalsIgnoreCase("Issuer")){
                   // EndpointReferenceImpl isType = (EndpointReferenceImpl)obj.getValue();
                   // setIssuer(new IssuerImpl(isType));
                } else if (local.equalsIgnoreCase("Claims")){
                    ClaimsType cType = (ClaimsType)obj.getValue();
                    setClaims(new ClaimsImpl(cType));
                } else if (local.equalsIgnoreCase("Participants")){
                    ParticipantsType psType = (ParticipantsType)obj.getValue();
                    setParticipants(new ParticipantsImpl(psType));
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
                    OnBehalfOfType oboType = (OnBehalfOfType)obj.getValue();
                    setOnBehalfOf(new OnBehalfOfImpl(oboType));
                } else if (local.equalsIgnoreCase("Encryption")){
                    EncryptionType encType = (EncryptionType)obj.getValue();
                    setEncryption(new EncryptionImpl(encType));
                } else if (local.equalsIgnoreCase("UseKey")){
                    UseKeyType ukType = (UseKeyType)obj.getValue();
                    setUseKey(new UseKeyImpl(ukType));
                } else if (local.equalsIgnoreCase("DelegateTo")){
                    DelegateToType dtType  = (DelegateToType)obj.getValue();
                    setDelegateTo(new DelegateToImpl(dtType));
                } else if (local.equalsIgnoreCase("RenewTarget")){
                    RenewTargetType rtType = (RenewTargetType)obj.getValue();
                    setRenewTarget(new RenewTargetImpl(rtType));
                } else if (local.equalsIgnoreCase("CancelTarget")){
                    CancelTargetType ctType = (CancelTargetType)obj.getValue();
                    setCancelTarget(new CancelTargetImpl(ctType));
                } else if (local.equalsIgnoreCase("AppliesTo")) {
                    setAppliesTo((AppliesTo)obj.getValue());
                }
            }
        }
    }
}
