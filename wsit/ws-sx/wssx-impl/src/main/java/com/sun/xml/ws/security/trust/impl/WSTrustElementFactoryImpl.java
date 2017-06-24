/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import com.sun.xml.ws.api.security.trust.Claims;
import com.sun.xml.ws.security.trust.elements.AllowPostdating;
import com.sun.xml.ws.security.trust.elements.ActAs;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.IssuedTokens;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.OnBehalfOf;
import com.sun.xml.ws.security.trust.elements.RenewTarget;
import com.sun.xml.ws.security.trust.elements.Renewing;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.SecondaryParameters;
import com.sun.xml.ws.api.security.trust.Status;
import com.sun.xml.ws.security.trust.elements.UseKey;
import com.sun.xml.ws.security.trust.elements.ValidateTarget;

import com.sun.xml.ws.security.trust.impl.elements.BinarySecretImpl;
import com.sun.xml.ws.security.trust.impl.elements.CancelTargetImpl;
import com.sun.xml.ws.security.trust.impl.elements.ClaimsImpl;
import com.sun.xml.ws.security.trust.impl.elements.EntropyImpl;
import com.sun.xml.ws.security.trust.impl.elements.IssuedTokensImpl;
import com.sun.xml.ws.security.trust.impl.elements.LifetimeImpl;
import com.sun.xml.ws.security.trust.impl.elements.OnBehalfOfImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestSecurityTokenResponseImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestSecurityTokenResponseCollectionImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedProofTokenImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedAttachedReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedUnattachedReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestSecurityTokenImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedSecurityTokenImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedTokenCancelledImpl;
import com.sun.xml.ws.security.trust.impl.elements.StatusImpl;
import com.sun.xml.ws.security.trust.impl.elements.UseKeyImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.DirectReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;
import com.sun.xml.ws.security.trust.impl.bindings.BinarySecretType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.bindings.ObjectFactory;
import com.sun.xml.ws.security.trust.impl.bindings.EntropyType;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;

import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.EncryptedKey;
import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;


import javax.xml.bind.PropertyException;


import java.net.URI;


import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import javax.xml.transform.Source;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseCollectionType;
import com.sun.xml.ws.security.trust.impl.elements.RenewTargetImpl;
import javax.xml.bind.util.JAXBSource;

import javax.xml.bind.Marshaller;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import java.util.List;

/**
 * A Factory for creating the WS-Trust schema elements,
 * and marshalling/un-marshalling them.
 *
 * @author Manveen Kaur
 */
public class WSTrustElementFactoryImpl extends WSTrustElementFactory {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private static final String RST = "RST";
    private static final String RSTRCollection = "RSTRCollection";
    
    public WSTrustElementFactoryImpl(){
    }
    
    /**
     * Create an RST for Issue from the given arguments
     * Any of the arguments can be null since they are all optional, but one of tokenType and AppliesTo must be present
     */
    public  RequestSecurityToken createRSTForIssue(final URI tokenType,
            final URI requestType,
            final URI context,
            final AppliesTo scopes,
            final Claims claims, final Entropy entropy, final Lifetime lifetime) throws WSTrustException {
        
        if (tokenType==null && scopes==null) {
            log.log(Level.WARNING,
                    LogStringsMessages.WST_1003_TOKENTYPE_APPLIESTO_NULL());
        }
        return new RequestSecurityTokenImpl(tokenType, requestType, context, scopes, claims, entropy, lifetime, null);
    }
    
    /**
     * Create an RSTR for Issue from the given arguments. TokenType should be Issue.
     * Any of the arguments can be null since they are all optional, but one of RequestedSecurityToken or RequestedProofToken should be returned
     */
    public  RequestSecurityTokenResponse createRSTRForIssue(final URI tokenType, final URI context, final RequestedSecurityToken token, final AppliesTo scopes, final RequestedAttachedReference attachedReference, final RequestedUnattachedReference unattachedRef, final RequestedProofToken proofToken, final Entropy entropy, final Lifetime lifetime) throws WSTrustException {
        final RequestSecurityTokenResponse rstr =
                new RequestSecurityTokenResponseImpl(tokenType, context, token, scopes,
                attachedReference, unattachedRef, proofToken, entropy, lifetime, null);
        return rstr;
    }
    
    /**
     * Create a collection of RequestSecurityTokenResponse(s)
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionForIssue(final URI tokenType, final URI context, final RequestedSecurityToken token, final AppliesTo scopes, final RequestedAttachedReference attached, final RequestedUnattachedReference unattached, final RequestedProofToken proofToken, final Entropy entropy, final Lifetime lifetime) throws WSTrustException {
        final RequestSecurityTokenResponseCollection rstrCollection =
                new RequestSecurityTokenResponseCollectionImpl(tokenType, context, token, scopes, attached, unattached, proofToken, entropy, lifetime);
        return rstrCollection;
    }
    
    /**
     * Create an RSTR for Renew from the given arguments. TokenType should be Issue.
     * Any of the arguments can be null since they are all optional, but one of RequestedSecurityToken or RequestedProofToken should be returned
     */
    public  RequestSecurityTokenResponse createRSTRForRenew(URI tokenType, final URI context, RequestedSecurityToken token, final RequestedAttachedReference attachedReference, final RequestedUnattachedReference unattachedRef, final RequestedProofToken proofToken, final Entropy entropy, final Lifetime lifetime) throws WSTrustException {
        final RequestSecurityTokenResponse rstr =
                new RequestSecurityTokenResponseImpl(tokenType, context, token, null, attachedReference, unattachedRef, proofToken, entropy, lifetime, null);
        return rstr;
    }
    
    /**
     * Create a wst:IssuedTokens object
     */
    public  IssuedTokens createIssuedTokens(final RequestSecurityTokenResponseCollection issuedTokens) {
        return new IssuedTokensImpl(issuedTokens);
    }
    
    public OnBehalfOf createOnBehalfOf(Token oboToken){
        return new OnBehalfOfImpl(oboToken);
    }
    
    public ValidateTarget createValidateTarget(Token token){
         throw new UnsupportedOperationException("Unsupported operation: ValidateTarget");
    }

    public ActAs createActAs(Token token){
        throw new UnsupportedOperationException("Unsupported operation: createActAs");
    }
    
    /**
     * Create an Entropy with a BinarySecret
     */
    public Entropy createEntropy(final BinarySecret secret) {
        return new EntropyImpl(secret);
    }
    
    /**
     * Create an Entropy with an xenc:EncryptedKey
     */
    public  Entropy createEntropy(final EncryptedKey key) {
        return new EntropyImpl(key);
    }
    
    public BinarySecret createBinarySecret(final byte[] rawValue, final String type) {
        return new BinarySecretImpl(rawValue, type);
    }
    
    public BinarySecret createBinarySecret(final Element elem) throws WSTrustException {
        return new BinarySecretImpl(BinarySecretImpl.fromElement(elem));
    }
    
    /**
     * Create a Lifetime.
     */
    public Lifetime createLifetime(final AttributedDateTime created,  final AttributedDateTime expires) {
        return new LifetimeImpl(created, expires);
    }
    
    public Claims createClaims(Element elem) throws WSTrustException {
        return new ClaimsImpl(ClaimsImpl.fromElement(elem));
    }

    public Claims createClaims(Claims claims) throws WSTrustException {
        ClaimsImpl newClaims = new ClaimsImpl();
        if (claims != null){
            newClaims.setDialect(claims.getDialect());
            newClaims.getAny().addAll(claims.getAny());
            newClaims.getOtherAttributes().putAll(claims.getOtherAttributes());
        }

        return newClaims;
    }

    public Claims createClaims() throws WSTrustException {
        return new ClaimsImpl();
    }
    
    public Status createStatus(String code, String reason){
        return new StatusImpl(code, reason);
    }
    
    /**
     * Create a RequestedSecurityToken.
     */
    public RequestedSecurityToken createRequestedSecurityToken(final Token token) {
        return new RequestedSecurityTokenImpl(token);
    }
    
    /**
     * Create a RequestedSecurityToken.
     */
    public RequestedSecurityToken createRequestedSecurityToken() {
        return new RequestedSecurityTokenImpl();
    }
    
    public DirectReference createDirectReference(final String valueType, final String uri){
        return new DirectReferenceImpl(valueType, uri);
    }
    
    public DirectReference createDirectReference(final String valueType, final String uri, final String instanceId){
        return new DirectReferenceImpl(valueType, uri, instanceId);
    }
    
    public KeyIdentifier createKeyIdentifier(final String valueType, final String encodingType){
        return new KeyIdentifierImpl(valueType, encodingType);
    }
    
    public SecurityTokenReference createSecurityTokenReference(final Reference ref){
        return new SecurityTokenReferenceImpl(ref);
    }

    /**
     * Create a RequestedAttachedReference.
     */
    public RequestedAttachedReference createRequestedAttachedReference(final SecurityTokenReference str) {
        return new RequestedAttachedReferenceImpl(str);
    }
    
    /**
     * Create a RequestedUnattachedReference.
     */
    public RequestedUnattachedReference createRequestedUnattachedReference(final SecurityTokenReference str) {
        return new RequestedUnattachedReferenceImpl(str);
    }
    
    /**
     * Create a RequestedProofToken.
     */
    public RequestedProofToken createRequestedProofToken() {
        return new RequestedProofTokenImpl();
    }
    
    /**
     * Create SecondaryParameters
     */
    public SecondaryParameters createSecondaryParameters(){
        throw new UnsupportedOperationException("Unsupported operation: createSecondaryParameters");
    }
    
    public UseKey createUseKey(Token token, String sig){
        UseKey useKey = new UseKeyImpl(token);
        if (sig != null){
            useKey.setSignatureID(URI.create(sig));
        }
        
        return useKey;
    }

    /**
     *Create an RST for a Renewal Request
     */
    public  RequestSecurityToken createRSTForRenew(final URI tokenType, final URI requestType, final URI context, final RenewTarget target, final AllowPostdating apd, final Renewing renewingInfo) {
        return new RequestSecurityTokenImpl(tokenType, requestType, context, target, apd, renewingInfo);
    }
    
    public RenewTarget createRenewTarget(final SecurityTokenReference str){
        return new RenewTargetImpl(str);
    }
    
    public CancelTarget createCancelTarget(final SecurityTokenReference str){
        return new CancelTargetImpl(str);
    }
    
    /**
     *Create an RST for Token Cancellation
     */
    public  RequestSecurityToken createRSTForCancel(final URI requestType, final CancelTarget target) {
        return new RequestSecurityTokenImpl(null, requestType, target);
    }
    
    /**
     *Create an RSTR for a Successful Token Cancellation
     */
    public  RequestSecurityTokenResponse createRSTRForCancel() {
        final RequestSecurityTokenResponse rstr =  new RequestSecurityTokenResponseImpl();
        rstr.setRequestedTokenCancelled(new RequestedTokenCancelledImpl());
        if(log.isLoggable(Level.FINE)) {            
            log.log(Level.FINE,
                    LogStringsMessages.WST_1008_CREATED_RSTR_CANCEL(rstr.toString()));
        }
        return rstr;
    }
    
    /**
     *Create an RST for Token Validation
     *<p>
     *TODO: Not clear from Spec whether the Token to be validated is ever sent ?
     *TODO: There is a mention of special case where a SOAPEnvelope may be specified as
     * a security token if the requestor desires the envelope to be validated.
     *</p>
     */
    public  RequestSecurityToken createRSTForValidate(final URI tokenType, final URI requestType) {
        return new RequestSecurityTokenImpl(tokenType, requestType);
    }
    
    /**
     * create an RSTR for validate request.
     */
    public  RequestSecurityTokenResponse createRSTRForValidate(final URI tokenType, final RequestedSecurityToken token, final Status status) {
        return new RequestSecurityTokenResponseImpl(tokenType, null, token, null, null, null, null, null, null, status);
    }
    
    /**
     * Create an Empty RST
     */
    public RequestSecurityToken createRST() {
        return new RequestSecurityTokenImpl();
    }
    
    /**
     * Create an Empty RSTR
     */
    public RequestSecurityTokenResponse createRSTR() {
        return new RequestSecurityTokenResponseImpl();
    }

    public RequestSecurityTokenResponseCollection createRSTRC(List<RequestSecurityTokenResponse> rstrs){
        RequestSecurityTokenResponseCollection rstrc = new RequestSecurityTokenResponseCollectionImpl();
        rstrc.getRequestSecurityTokenResponses().addAll(rstrs);

        return rstrc;
    }
    
    
    /**
     * create an RST from a Source
     */
    public RequestSecurityToken createRSTFrom(final Source src) {
        try {
            final javax.xml.bind.Unmarshaller unmarshaller = getContext().createUnmarshaller();
            final JAXBElement<RequestSecurityTokenType> rstType = unmarshaller.unmarshal(src, RequestSecurityTokenType.class);
            final RequestSecurityTokenType type = rstType.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0006_FAIL_RST_SOURCE(src.toString()), ex);
            throw new RuntimeException(LogStringsMessages.WST_0006_FAIL_RST_SOURCE(src.toString()), ex);
        }
    }
    
    /**
     * create an RST from DOM Element
     */
    public  RequestSecurityToken createRSTFrom(final Element elem) {
        try {
            final javax.xml.bind.Unmarshaller unmarshaller = getContext().createUnmarshaller();
            final JAXBElement<RequestSecurityTokenType> rstType = unmarshaller.unmarshal(elem, RequestSecurityTokenType.class);
            final RequestSecurityTokenType type = rstType.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0007_FAIL_RST_ELEM(elem.toString()), ex);
            throw new RuntimeException(LogStringsMessages.WST_0007_FAIL_RST_ELEM(elem.toString()), ex);
        }
    }
    
    /**
     * create an RSTR from a Source
     */
    public  RequestSecurityTokenResponse createRSTRFrom(final Source src) {
        try {
            final javax.xml.bind.Unmarshaller unmarshaller = getContext().createUnmarshaller();
            final JAXBElement<RequestSecurityTokenResponseType> rstType = unmarshaller.unmarshal(src, RequestSecurityTokenResponseType.class);
            final RequestSecurityTokenResponseType type = rstType.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0008_FAIL_RSTR_SOURCE(src.toString()), ex);
            throw new RuntimeException(LogStringsMessages.WST_0008_FAIL_RSTR_SOURCE(src.toString()), ex);
        }
    }
    
    /**
     * create an RSTR from DOM Element
     */
    public  RequestSecurityTokenResponse createRSTRFrom(final Element elem) {
        try {
            final javax.xml.bind.Unmarshaller unmarshaller = getContext().createUnmarshaller();
            final JAXBElement<RequestSecurityTokenResponseType> rstType = unmarshaller.unmarshal(elem, RequestSecurityTokenResponseType.class);
            final RequestSecurityTokenResponseType type = rstType.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0008_FAIL_RSTR_SOURCE(elem.toString()), ex);
            throw new RuntimeException(LogStringsMessages.WST_0008_FAIL_RSTR_SOURCE(elem.toString()), ex);
        }
    }
    
    /**
     * Create RSTR Collection from Source
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(final Source src) {
        try {
            javax.xml.bind.Unmarshaller u = getContext().createUnmarshaller();
            JAXBElement<RequestSecurityTokenResponseCollectionType> rstrcType = u.unmarshal(src, RequestSecurityTokenResponseCollectionType.class);
            RequestSecurityTokenResponseCollectionType type = rstrcType.getValue();
            return new RequestSecurityTokenResponseCollectionImpl(type);
        } catch ( Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Create RSTR Collection from Element
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(final Element elem) {
        try {
            javax.xml.bind.Unmarshaller u = getContext().createUnmarshaller();
            JAXBElement<RequestSecurityTokenResponseCollectionType> rstrcType = u.unmarshal(elem, RequestSecurityTokenResponseCollectionType.class);
            RequestSecurityTokenResponseCollectionType type = rstrcType.getValue();
            return new RequestSecurityTokenResponseCollectionImpl(type);
        } catch ( Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    
    /**
     * create an RST from JAXBElement
     * <p>
     * NOTE: an STS Implementor can call
     * <PRE>
     * JAXBElement&lt;RequestSecurityTokenType&gt; elem=
     * ObjectFactory.createRequestSecurityToken(&lt;JAXBBean for RST&gt;)
     * </PRE>
     * The JAXBBean for RST is the one generated from the ws-trust.xsd schema
     * The default implementation expects the packagename of the generated JAXB Beans to be fixed.
     * </p>
     */
    public RequestSecurityToken createRSTFrom(final JAXBElement elem) {
        try {
            final RequestSecurityTokenType type = (RequestSecurityTokenType)elem.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0010_FAILED_CREATION_FROM_JAXBELE(RST), e);
            throw new RuntimeException(LogStringsMessages.WST_0010_FAILED_CREATION_FROM_JAXBELE(RST), e);
        }
    }
    
    /**
     * create an RSTR from JAXBElement
     * <p>
     * NOTE: an STS Implementor can call
     * <PRE>
     * JAXBElement&lt;RequestSecurityTokenResponseType&gt; elem=
     * ObjectFactory.createRequestSecurityTokenResponse(&lt;JAXBBean for RSTR&gt;);
     * </PRE>
     * The &lt;JAXBBean for RSTR&gt; is the one generated from the ws-trust.xsd schema
     * The default implementation expects the packagename of the generated JAXB Beans to be fixed.
     * </p>
     */
    public  RequestSecurityTokenResponse createRSTRFrom(final JAXBElement elem) {
        try {
            final RequestSecurityTokenResponseType type = (RequestSecurityTokenResponseType)elem.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0009_FAIL_RSTR_ELEM(elem.toString()), e);
            throw new RuntimeException(LogStringsMessages.WST_0009_FAIL_RSTR_ELEM(elem.toString()), e);
        }
    }
    /**
     * create an RSTR Collection from JAXBElement
     * <p>
     * NOTE: an STS Implementor can call
     * <PRE>
     * JAXBElement&lt;RequestSecurityTokenResponseCollectionType&gt; elem=
     * ObjectFactory.createRequestSecurityTokenResponseCollection(&lt;JAXBBean for RSTR Collection&gt;
     * </PRE>
     * The &lt;JAXBBean for RSTR Collection&gt; is the one generated from the ws-trust.xsd schema
     * The default implementation expects the packagename of the generated JAXB Beans to be fixed.
     * </p>
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(final JAXBElement elem) {
        try {
            final RequestSecurityTokenResponseCollectionType type = (RequestSecurityTokenResponseCollectionType)elem.getValue();
            return new RequestSecurityTokenResponseCollectionImpl(type);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0010_FAILED_CREATION_FROM_JAXBELE(RSTRCollection), e);
            throw new RuntimeException(LogStringsMessages.WST_0010_FAILED_CREATION_FROM_JAXBELE(RSTRCollection), e);
        }
    }
    
    public SecurityTokenReference createSecurityTokenReference(final JAXBElement elem){
        try {
            final SecurityTokenReferenceType type = (SecurityTokenReferenceType)elem.getValue();
            return new SecurityTokenReferenceImpl(type);
        } catch (Exception e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0010_FAILED_CREATION_FROM_JAXBELE("STR"), e);
            throw new RuntimeException(LogStringsMessages.WST_0010_FAILED_CREATION_FROM_JAXBELE("STR"), e);
        }
    }

    public SecurityContextToken createSecurityContextToken(final URI identifier, final String instance, final String wsuId){
        throw new UnsupportedOperationException("this operation is not supported");
    }
    
    public JAXBElement toJAXBElement(final BaseSTSRequest request) {
        if (request instanceof RequestSecurityToken){
            return toJAXBElement((RequestSecurityToken)request);
        }

        return null;
    }

    public JAXBElement toJAXBElement(final BaseSTSResponse response) {
        if (response instanceof RequestSecurityTokenResponse){
            return toJAXBElement((RequestSecurityTokenResponse)response);
        }
        
        if (response instanceof RequestSecurityTokenResponseCollection){
            return toJAXBElement((RequestSecurityTokenResponseCollection)response);
        }

        return null;
    }

    /**
     * convert an SecurityTokenReference to a JAXBElement
     */
    public JAXBElement toJAXBElement(final SecurityTokenReference str){
        final JAXBElement<SecurityTokenReferenceType> strElement =
                (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)str);
        return strElement;
    }
    
    /**
     * convert an RST to a JAXBElement
     */
    public JAXBElement toJAXBElement(final RequestSecurityToken rst) {
        final JAXBElement<RequestSecurityTokenType> rstElement=
                (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
        return rstElement;
    }
    
    /**
     * convert an RSTR to a JAXBElement
     */
    public  JAXBElement toJAXBElement(final RequestSecurityTokenResponse rstr) {
        final JAXBElement<RequestSecurityTokenResponseType> rstElement=
                (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
        return rstElement;
    }
    
    /**
     * convert a Entropy to a JAXBElement
     */
    public  JAXBElement toJAXBElement(final Entropy entropy) {
        final JAXBElement<EntropyType> etElement=
                (new ObjectFactory()).createEntropy((EntropyType)entropy);
        return etElement;
    }
    
    /**
     * convert an RSTR Collection to a JAXBElement
     */
    public  JAXBElement toJAXBElement(final RequestSecurityTokenResponseCollection rstrCollection) {
        final JAXBElement<RequestSecurityTokenResponseCollectionType> rstElement=
                (new ObjectFactory()).createRequestSecurityTokenResponseCollection((RequestSecurityTokenResponseCollectionType)rstrCollection);
        return rstElement;
    }
    
    public Source toSource(final BaseSTSRequest request) {
        if (request instanceof RequestSecurityToken){
            return toSource((RequestSecurityToken)request);
        }

        return null;
    }

    public Source toSource(final BaseSTSResponse response) {
        if (response instanceof RequestSecurityTokenResponse){
            return toSource((RequestSecurityTokenResponse)response);
        }
        
        if (response instanceof RequestSecurityTokenResponseCollection){
            return toSource((RequestSecurityTokenResponseCollection)response);
        }

        return null;
    }
    /**
     * Marshal an RST to a Source.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Source toSource(final RequestSecurityToken rst) {
        try{
            return new JAXBSource(getMarshaller(), toJAXBElement(rst));
        }catch(JAXBException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0002_FAIL_MARSHAL_TOSOURCE(RST), ex);
            throw new RuntimeException(LogStringsMessages.WST_0002_FAIL_MARSHAL_TOSOURCE(RST), ex);
        }
    }
    
    /**
     * Marshal an RSTR to a Source
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public Source toSource(final RequestSecurityTokenResponse rstr) {
        //return new DOMSource(toElement(rstr));
        try{
            return new JAXBSource(getMarshaller(), toJAXBElement(rstr));
        }catch(JAXBException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0002_FAIL_MARSHAL_TOSOURCE("RSTR"), ex);
            throw new RuntimeException(LogStringsMessages.WST_0002_FAIL_MARSHAL_TOSOURCE("RSTR"), ex);
        }
    }
    
    /**
     * Marshal an RSTR Collection to a Source
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public  Source toSource(final RequestSecurityTokenResponseCollection rstrCollection) {
        try{
            return new JAXBSource(getMarshaller(), toJAXBElement(rstrCollection));
        }catch(JAXBException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0002_FAIL_MARSHAL_TOSOURCE(RSTRCollection), ex);
            throw new RuntimeException(LogStringsMessages.WST_0002_FAIL_MARSHAL_TOSOURCE(RSTRCollection), ex);
        }
    }

    public Element toElement(final BaseSTSRequest request) {
        if (request instanceof RequestSecurityToken){
            return toElement((RequestSecurityToken)request);
        }

        return null;
    }

    public Element toElement(final BaseSTSResponse response) {
        if (response instanceof RequestSecurityTokenResponse){
            return toElement((RequestSecurityTokenResponse)response);
        }
        
        if (response instanceof RequestSecurityTokenResponseCollection){
            return toElement((RequestSecurityTokenResponseCollection)response);
        }

        return null;
    }
    
    /**
     * Marshal an RST to a DOM Element.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Element toElement(final RequestSecurityToken rst) {
        try {
            
            final Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            final JAXBElement<RequestSecurityTokenType> rstElement =  (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
            getMarshaller().marshal(rstElement, doc);
            return doc.getDocumentElement();
        } catch (JAXBException e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), e);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), e);
        }
    }
    
    /**
     * Marshal an RSTR to DOM Element
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public Element toElement(final RequestSecurityTokenResponse rstr) {
        try {
            final Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            final JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            getMarshaller().marshal(rstrElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
        }
    }
    
    public Element toElement(final RequestSecurityTokenResponse rstr, final Document doc) {
        try {
            final JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            getMarshaller().marshal(rstrElement, doc);
            return doc.getDocumentElement();
            
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
        }
    }
    
    /**
     * Marshal an RSTR Collection to a DOM Element
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public  Element toElement(final RequestSecurityTokenResponseCollection rstrCollection) {
        try {
            final Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            final JAXBElement<RequestSecurityTokenResponseCollectionType> rstElement =
                    (new ObjectFactory()).createRequestSecurityTokenResponseCollection((RequestSecurityTokenResponseCollectionType)rstrCollection);
            getMarshaller().marshal(rstElement, doc);
            return doc.getDocumentElement();
        }  catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
        }
    }
    
    public Element toElement(final BinarySecret secret){
        try {
            final Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            final JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)secret);
            getMarshaller().marshal(bsElement, doc);
            return doc.getDocumentElement();
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
        }
    }
    
    /**
     * Marshal an STR to a DOM Element.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Element toElement(final SecurityTokenReference str, Document doc) {
        try {
            if(doc == null){
                doc = WSTrustUtil.newDocument();
            }
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            final JAXBElement<SecurityTokenReferenceType> strElement =  (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)str);
            getMarshaller().marshal(strElement, doc);
            return doc.getDocumentElement();
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
        }
    }
    
    /**
     * Marshal an BinarySecret to a DOM Element.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Element toElement(final BinarySecret secret, Document doc) {
        try {
            if(doc == null){
                doc = WSTrustUtil.newDocument();
            }
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            final JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)secret);
            getMarshaller().marshal(bsElement, doc);
            return doc.getDocumentElement();
            
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
            throw new RuntimeException(LogStringsMessages.WST_0012_JAXB_EX_TO_ELEMENT(), ex);
        }
    }
    
    public Marshaller getMarshaller(){
         try {
            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new com.sun.xml.ws.security.trust.util.TrustNamespacePrefixMapper());
        
            return marshaller;
         } catch( PropertyException e ) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0003_ERROR_CREATING_WSTRUSTFACT(), e);
            throw new RuntimeException(
                    LogStringsMessages.WST_0003_ERROR_CREATING_WSTRUSTFACT(), e);
        } catch (JAXBException jbe) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0003_ERROR_CREATING_WSTRUSTFACT(), jbe);
            throw new RuntimeException(
                    LogStringsMessages.WST_0003_ERROR_CREATING_WSTRUSTFACT(), jbe);
        }
    }
}
