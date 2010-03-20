package com.sun.xml.ws.security.trust.impl.bindings;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.trust.impl.bindings.AllowPostdatingType;
import com.sun.xml.ws.security.trust.impl.bindings.AuthenticatorType;
import com.sun.xml.ws.security.trust.impl.bindings.BinaryExchangeType;
import com.sun.xml.ws.security.trust.impl.bindings.BinarySecretType;
import com.sun.xml.ws.security.trust.impl.bindings.CancelTargetType;
import com.sun.xml.ws.security.trust.impl.bindings.ClaimsType;
import com.sun.xml.ws.security.trust.impl.bindings.DelegateToType;
import com.sun.xml.ws.security.trust.impl.bindings.EncryptionType;
import com.sun.xml.ws.security.trust.impl.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.bindings.KeyExchangeTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.LifetimeType;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.bindings.OnBehalfOfType;
import com.sun.xml.ws.security.trust.impl.bindings.ParticipantType;
import com.sun.xml.ws.security.trust.impl.bindings.ParticipantsType;
import com.sun.xml.ws.security.trust.impl.bindings.ProofEncryptionType;
import com.sun.xml.ws.security.trust.impl.bindings.RenewTargetType;
import com.sun.xml.ws.security.trust.impl.bindings.RenewingType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestKETType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseCollectionType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedProofTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedReferenceType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestedTokenCancelledType;
import com.sun.xml.ws.security.trust.impl.bindings.SignChallengeType;
import com.sun.xml.ws.security.trust.impl.bindings.StatusType;
import com.sun.xml.ws.security.trust.impl.bindings.UseKeyType;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sun.xml.ws.security.trust.impl.bindings package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Issuer_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Issuer");
    private final static QName _Claims_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Claims");
    private final static QName _SignWith_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "SignWith");
    private final static QName _CanonicalizationAlgorithm_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "CanonicalizationAlgorithm");
    private final static QName _Participants_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Participants");
    private final static QName _IssuedTokens_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "IssuedTokens");
    private final static QName _Lifetime_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Lifetime");
    private final static QName _KeyType_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "KeyType");
    private final static QName _SignChallenge_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "SignChallenge");
    private final static QName _DelegateTo_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "DelegateTo");
    private final static QName _Renewing_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Renewing");
    private final static QName _RenewTarget_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RenewTarget");
    private final static QName _UseKey_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "UseKey");
    private final static QName _AllowPostdating_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "AllowPostdating");
    private final static QName _Authenticator_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Authenticator");
    private final static QName _TokenType_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "TokenType");
    private final static QName _Challenge_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Challenge");
    private final static QName _RequestedAttachedReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestedAttachedReference");
    private final static QName _AuthenticationType_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "AuthenticationType");
    private final static QName _Forwardable_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Forwardable");
    private final static QName _KeySize_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "KeySize");
    private final static QName _SignChallengeResponse_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "SignChallengeResponse");
    private final static QName _RequestSecurityTokenResponseCollection_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestSecurityTokenResponseCollection");
    private final static QName _Encryption_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Encryption");
    private final static QName _ProofEncryption_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "ProofEncryption");
    private final static QName _RequestedTokenCancelled_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestedTokenCancelled");
    private final static QName _EncryptionAlgorithm_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "EncryptionAlgorithm");
    private final static QName _RequestedSecurityToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestedSecurityToken");
    private final static QName _Entropy_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Entropy");
    private final static QName _RequestedProofToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestedProofToken");
    private final static QName _RequestSecurityTokenResponse_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestSecurityTokenResponse");
    private final static QName _RequestType_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestType");
    private final static QName _ComputedKeyAlgorithm_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "ComputedKeyAlgorithm");
    private final static QName _OnBehalfOf_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "OnBehalfOf");
    private final static QName _CombinedHash_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "CombinedHash");
    private final static QName _KeyExchangeToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "KeyExchangeToken");
    private final static QName _EncryptWith_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "EncryptWith");
    private final static QName _RequestSecurityToken_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestSecurityToken");
    private final static QName _BinarySecret_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "BinarySecret");
    private final static QName _Status_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Status");
    private final static QName _ComputedKey_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "ComputedKey");
    private final static QName _BinaryExchange_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "BinaryExchange");
    private final static QName _RequestedUnattachedReference_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestedUnattachedReference");
    private final static QName _RequestKET_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestKET");
    private final static QName _CancelTarget_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "CancelTarget");
    private final static QName _Delegatable_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "Delegatable");
    private final static QName _SignatureAlgorithm_QNAME = new QName("http://schemas.xmlsoap.org/ws/2005/02/trust", "SignatureAlgorithm");
    private static final QName _EndpointReference_QNAME = new QName("http://www.w3.org/2005/08/addressing", "EndpointReference");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sun.xml.ws.security.trust.impl.bindings
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProofEncryptionType }
     * 
     */
    public ProofEncryptionType createProofEncryptionType() {
        return new ProofEncryptionType();
    }

    /**
     * Create an instance of {@link RequestSecurityTokenResponseCollectionType }
     * 
     */
    public RequestSecurityTokenResponseCollectionType createRequestSecurityTokenResponseCollectionType() {
        return new RequestSecurityTokenResponseCollectionType();
    }

    /**
     * Create an instance of {@link RequestSecurityTokenResponseType }
     * 
     */
    public RequestSecurityTokenResponseType createRequestSecurityTokenResponseType() {
        return new RequestSecurityTokenResponseType();
    }

    /**
     * Create an instance of {@link OnBehalfOfType }
     * 
     */
    public OnBehalfOfType createOnBehalfOfType() {
        return new OnBehalfOfType();
    }

    /**
     * Create an instance of {@link ParticipantType }
     * 
     */
    public ParticipantType createParticipantType() {
        return new ParticipantType();
    }

    /**
     * Create an instance of {@link BinaryExchangeType }
     * 
     */
    public BinaryExchangeType createBinaryExchangeType() {
        return new BinaryExchangeType();
    }

    /**
     * Create an instance of {@link KeyExchangeTokenType }
     * 
     */
    public KeyExchangeTokenType createKeyExchangeTokenType() {
        return new KeyExchangeTokenType();
    }

    /**
     * Create an instance of {@link CancelTargetType }
     * 
     */
    public CancelTargetType createCancelTargetType() {
        return new CancelTargetType();
    }

    /**
     * Create an instance of {@link RequestedSecurityTokenType }
     * 
     */
    public RequestedSecurityTokenType createRequestedSecurityTokenType() {
        return new RequestedSecurityTokenType();
    }

    /**
     * Create an instance of {@link RequestedReferenceType }
     * 
     */
    public RequestedReferenceType createRequestedReferenceType() {
        return new RequestedReferenceType();
    }

    /**
     * Create an instance of {@link SignChallengeType }
     * 
     */
    public SignChallengeType createSignChallengeType() {
        return new SignChallengeType();
    }

    /**
     * Create an instance of {@link LifetimeType }
     * 
     */
    public LifetimeType createLifetimeType() {
        return new LifetimeType();
    }

    /**
     * Create an instance of {@link RequestedProofTokenType }
     * 
     */
    public RequestedProofTokenType createRequestedProofTokenType() {
        return new RequestedProofTokenType();
    }

    /**
     * Create an instance of {@link RenewingType }
     * 
     */
    public RenewingType createRenewingType() {
        return new RenewingType();
    }

    /**
     * Create an instance of {@link RenewTargetType }
     * 
     */
    public RenewTargetType createRenewTargetType() {
        return new RenewTargetType();
    }

    /**
     * Create an instance of {@link ClaimsType }
     * 
     */
    public ClaimsType createClaimsType() {
        return new ClaimsType();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link EntropyType }
     * 
     */
    public EntropyType createEntropyType() {
        return new EntropyType();
    }

    /**
     * Create an instance of {@link DelegateToType }
     * 
     */
    public DelegateToType createDelegateToType() {
        return new DelegateToType();
    }

    /**
     * Create an instance of {@link BinarySecretType }
     * 
     */
    public BinarySecretType createBinarySecretType() {
        return new BinarySecretType();
    }

    /**
     * Create an instance of {@link RequestedTokenCancelledType }
     * 
     */
    public RequestedTokenCancelledType createRequestedTokenCancelledType() {
        return new RequestedTokenCancelledType();
    }

    /**
     * Create an instance of {@link EncryptionType }
     * 
     */
    public EncryptionType createEncryptionType() {
        return new EncryptionType();
    }

    /**
     * Create an instance of {@link AuthenticatorType }
     * 
     */
    public AuthenticatorType createAuthenticatorType() {
        return new AuthenticatorType();
    }

    /**
     * Create an instance of {@link RequestKETType }
     * 
     */
    public RequestKETType createRequestKETType() {
        return new RequestKETType();
    }

    /**
     * Create an instance of {@link RequestSecurityTokenType }
     * 
     */
    public RequestSecurityTokenType createRequestSecurityTokenType() {
        return new RequestSecurityTokenType();
    }

    /**
     * Create an instance of {@link ParticipantsType }
     * 
     */
    public ParticipantsType createParticipantsType() {
        return new ParticipantsType();
    }

    /**
     * Create an instance of {@link UseKeyType }
     * 
     */
    public UseKeyType createUseKeyType() {
        return new UseKeyType();
    }

    /**
     * Create an instance of {@link AllowPostdatingType }
     * 
     */
    public AllowPostdatingType createAllowPostdatingType() {
        return new AllowPostdatingType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EndpointReferenceType }{@code >}}
     * 
     */
    @SuppressWarnings("unchecked")
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Issuer")
     public JAXBElement createIssuer(EndpointReference value)
    {
        return new JAXBElement(_Issuer_QNAME, EndpointReference.class, null, value);
    }
    @SuppressWarnings("unchecked")
    @XmlElementDecl(namespace="http://www.w3.org/2005/08/addressing", name="EndpointReference")
     public JAXBElement createEndpointReference(EndpointReference value)
    {
        return new JAXBElement(_EndpointReference_QNAME, EndpointReference.class, null, value);
    }



    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClaimsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Claims")
    public JAXBElement<ClaimsType> createClaims(ClaimsType value) {
        return new JAXBElement<ClaimsType>(_Claims_QNAME, ClaimsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "SignWith")
    public JAXBElement<String> createSignWith(String value) {
        return new JAXBElement<String>(_SignWith_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "CanonicalizationAlgorithm")
    public JAXBElement<String> createCanonicalizationAlgorithm(String value) {
        return new JAXBElement<String>(_CanonicalizationAlgorithm_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParticipantsType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Participants")
    public JAXBElement<ParticipantsType> createParticipants(ParticipantsType value) {
        return new JAXBElement<ParticipantsType>(_Participants_QNAME, ParticipantsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestSecurityTokenResponseCollectionType }{@code >}}
     * 
     */
    @SuppressWarnings("unchecked")
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "IssuedTokens")
    public JAXBElement<RequestSecurityTokenResponseCollectionType> createIssuedTokens(RequestSecurityTokenResponseCollectionType value) {
        return new JAXBElement<RequestSecurityTokenResponseCollectionType>(_IssuedTokens_QNAME, RequestSecurityTokenResponseCollectionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LifetimeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Lifetime")
    public JAXBElement<LifetimeType> createLifetime(LifetimeType value) {
        return new JAXBElement<LifetimeType>(_Lifetime_QNAME, LifetimeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "KeyType")
    public JAXBElement<String> createKeyType(String value) {
        return new JAXBElement<String>(_KeyType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignChallengeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "SignChallenge")
    public JAXBElement<SignChallengeType> createSignChallenge(SignChallengeType value) {
        return new JAXBElement<SignChallengeType>(_SignChallenge_QNAME, SignChallengeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DelegateToType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "DelegateTo")
    public JAXBElement<DelegateToType> createDelegateTo(DelegateToType value) {
        return new JAXBElement<DelegateToType>(_DelegateTo_QNAME, DelegateToType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RenewingType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Renewing")
    public JAXBElement<RenewingType> createRenewing(RenewingType value) {
        return new JAXBElement<RenewingType>(_Renewing_QNAME, RenewingType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RenewTargetType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RenewTarget")
    public JAXBElement<RenewTargetType> createRenewTarget(RenewTargetType value) {
        return new JAXBElement<RenewTargetType>(_RenewTarget_QNAME, RenewTargetType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UseKeyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "UseKey")
    public JAXBElement<UseKeyType> createUseKey(UseKeyType value) {
        return new JAXBElement<UseKeyType>(_UseKey_QNAME, UseKeyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AllowPostdatingType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "AllowPostdating")
    public JAXBElement<AllowPostdatingType> createAllowPostdating(AllowPostdatingType value) {
        return new JAXBElement<AllowPostdatingType>(_AllowPostdating_QNAME, AllowPostdatingType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthenticatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Authenticator")
    public JAXBElement<AuthenticatorType> createAuthenticator(AuthenticatorType value) {
        return new JAXBElement<AuthenticatorType>(_Authenticator_QNAME, AuthenticatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "TokenType")
    public JAXBElement<String> createTokenType(String value) {
        return new JAXBElement<String>(_TokenType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Challenge")
    public JAXBElement<String> createChallenge(String value) {
        return new JAXBElement<String>(_Challenge_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestedReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestedAttachedReference")
    public JAXBElement<RequestedReferenceType> createRequestedAttachedReference(RequestedReferenceType value) {
        return new JAXBElement<RequestedReferenceType>(_RequestedAttachedReference_QNAME, RequestedReferenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "AuthenticationType")
    public JAXBElement<String> createAuthenticationType(String value) {
        return new JAXBElement<String>(_AuthenticationType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Forwardable")
    public JAXBElement<Boolean> createForwardable(Boolean value) {
        return new JAXBElement<Boolean>(_Forwardable_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "KeySize")
    public JAXBElement<Long> createKeySize(Long value) {
        return new JAXBElement<Long>(_KeySize_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SignChallengeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "SignChallengeResponse")
    public JAXBElement<SignChallengeType> createSignChallengeResponse(SignChallengeType value) {
        return new JAXBElement<SignChallengeType>(_SignChallengeResponse_QNAME, SignChallengeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestSecurityTokenResponseCollectionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestSecurityTokenResponseCollection")
    public JAXBElement<RequestSecurityTokenResponseCollectionType> createRequestSecurityTokenResponseCollection(RequestSecurityTokenResponseCollectionType value) {
        return new JAXBElement<RequestSecurityTokenResponseCollectionType>(_RequestSecurityTokenResponseCollection_QNAME, RequestSecurityTokenResponseCollectionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EncryptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Encryption")
    public JAXBElement<EncryptionType> createEncryption(EncryptionType value) {
        return new JAXBElement<EncryptionType>(_Encryption_QNAME, EncryptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProofEncryptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "ProofEncryption")
    public JAXBElement<ProofEncryptionType> createProofEncryption(ProofEncryptionType value) {
        return new JAXBElement<ProofEncryptionType>(_ProofEncryption_QNAME, ProofEncryptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestedTokenCancelledType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestedTokenCancelled")
    public JAXBElement<RequestedTokenCancelledType> createRequestedTokenCancelled(RequestedTokenCancelledType value) {
        return new JAXBElement<RequestedTokenCancelledType>(_RequestedTokenCancelled_QNAME, RequestedTokenCancelledType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "EncryptionAlgorithm")
    public JAXBElement<String> createEncryptionAlgorithm(String value) {
        return new JAXBElement<String>(_EncryptionAlgorithm_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestedSecurityTokenType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestedSecurityToken")
    public JAXBElement<RequestedSecurityTokenType> createRequestedSecurityToken(RequestedSecurityTokenType value) {
        return new JAXBElement<RequestedSecurityTokenType>(_RequestedSecurityToken_QNAME, RequestedSecurityTokenType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EntropyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Entropy")
    public JAXBElement<EntropyType> createEntropy(EntropyType value) {
        return new JAXBElement<EntropyType>(_Entropy_QNAME, EntropyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestedProofTokenType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestedProofToken")
    public JAXBElement<RequestedProofTokenType> createRequestedProofToken(RequestedProofTokenType value) {
        return new JAXBElement<RequestedProofTokenType>(_RequestedProofToken_QNAME, RequestedProofTokenType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestSecurityTokenResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestSecurityTokenResponse")
    public JAXBElement<RequestSecurityTokenResponseType> createRequestSecurityTokenResponse(RequestSecurityTokenResponseType value) {
        return new JAXBElement<RequestSecurityTokenResponseType>(_RequestSecurityTokenResponse_QNAME, RequestSecurityTokenResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestType")
    public JAXBElement<String> createRequestType(String value) {
        return new JAXBElement<String>(_RequestType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "ComputedKeyAlgorithm")
    public JAXBElement<String> createComputedKeyAlgorithm(String value) {
        return new JAXBElement<String>(_ComputedKeyAlgorithm_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link OnBehalfOfType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "OnBehalfOf")
    public JAXBElement<OnBehalfOfType> createOnBehalfOf(OnBehalfOfType value) {
        return new JAXBElement<OnBehalfOfType>(_OnBehalfOf_QNAME, OnBehalfOfType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "CombinedHash")
    public JAXBElement<byte[]> createCombinedHash(byte[] value) {
        return new JAXBElement<byte[]>(_CombinedHash_QNAME, byte[].class, null, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link KeyExchangeTokenType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "KeyExchangeToken")
    public JAXBElement<KeyExchangeTokenType> createKeyExchangeToken(KeyExchangeTokenType value) {
        return new JAXBElement<KeyExchangeTokenType>(_KeyExchangeToken_QNAME, KeyExchangeTokenType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "EncryptWith")
    public JAXBElement<String> createEncryptWith(String value) {
        return new JAXBElement<String>(_EncryptWith_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestSecurityTokenType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestSecurityToken")
    public JAXBElement<RequestSecurityTokenType> createRequestSecurityToken(RequestSecurityTokenType value) {
        return new JAXBElement<RequestSecurityTokenType>(_RequestSecurityToken_QNAME, RequestSecurityTokenType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BinarySecretType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "BinarySecret")
    public JAXBElement<BinarySecretType> createBinarySecret(BinarySecretType value) {
        return new JAXBElement<BinarySecretType>(_BinarySecret_QNAME, BinarySecretType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatusType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Status")
    public JAXBElement<StatusType> createStatus(StatusType value) {
        return new JAXBElement<StatusType>(_Status_QNAME, StatusType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "ComputedKey")
    public JAXBElement<String> createComputedKey(String value) {
        return new JAXBElement<String>(_ComputedKey_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BinaryExchangeType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "BinaryExchange")
    public JAXBElement<BinaryExchangeType> createBinaryExchange(BinaryExchangeType value) {
        return new JAXBElement<BinaryExchangeType>(_BinaryExchange_QNAME, BinaryExchangeType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestedReferenceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestedUnattachedReference")
    public JAXBElement<RequestedReferenceType> createRequestedUnattachedReference(RequestedReferenceType value) {
        return new JAXBElement<RequestedReferenceType>(_RequestedUnattachedReference_QNAME, RequestedReferenceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestKETType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "RequestKET")
    public JAXBElement<RequestKETType> createRequestKET(RequestKETType value) {
        return new JAXBElement<RequestKETType>(_RequestKET_QNAME, RequestKETType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelTargetType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "CancelTarget")
    public JAXBElement<CancelTargetType> createCancelTarget(CancelTargetType value) {
        return new JAXBElement<CancelTargetType>(_CancelTarget_QNAME, CancelTargetType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "Delegatable")
    public JAXBElement<Boolean> createDelegatable(Boolean value) {
        return new JAXBElement<Boolean>(_Delegatable_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.xmlsoap.org/ws/2005/02/trust", name = "SignatureAlgorithm")
    public JAXBElement<String> createSignatureAlgorithm(String value) {
        return new JAXBElement<String>(_SignatureAlgorithm_QNAME, String.class, null, value);
    }

}
