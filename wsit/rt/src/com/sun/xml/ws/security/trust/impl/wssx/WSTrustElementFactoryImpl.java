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

package com.sun.xml.ws.security.trust.impl.wssx;

import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import com.sun.xml.ws.security.trust.elements.AllowPostdating;
import com.sun.xml.ws.security.trust.elements.ActAs;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.BaseSTSRequest;
import com.sun.xml.ws.security.trust.elements.BaseSTSResponse;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.api.security.trust.Claims;
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

import com.sun.xml.ws.security.trust.impl.elements.str.DirectReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;

import com.sun.xml.ws.security.trust.impl.wssx.elements.ActAsImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.BinarySecretImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.CancelTargetImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.ClaimsImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.EntropyImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.IssuedTokensImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.LifetimeImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.OnBehalfOfImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestSecurityTokenResponseImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestSecurityTokenResponseCollectionImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestedProofTokenImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestedAttachedReferenceImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestedUnattachedReferenceImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestSecurityTokenImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestedSecurityTokenImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RequestedTokenCancelledImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.SecondaryParametersImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.StatusImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.UseKeyImpl;
import com.sun.xml.ws.security.trust.impl.wssx.elements.ValidateTargetImpl;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.BinarySecretType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.EntropyType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestSecurityTokenType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestSecurityTokenResponseType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.RequestSecurityTokenResponseCollectionType;
import com.sun.xml.ws.security.trust.impl.wssx.bindings.ObjectFactory;
import com.sun.xml.ws.policy.impl.bindings.AppliesTo;

import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.api.security.EncryptedKey;
import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.api.security.Token;
import com.sun.xml.ws.api.security.SecurityContextToken;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;

import java.net.URI;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBElement;

import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustVersion;
import com.sun.xml.ws.security.trust.impl.wssx.elements.RenewTargetImpl;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import java.util.logging.Level;

import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;

import javax.xml.bind.PropertyException;

import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import java.util.logging.Logger;



public class WSTrustElementFactoryImpl extends WSTrustElementFactory {
     private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
     
    public WSTrustElementFactoryImpl(){
        
    }
    
    /**
     * Create an RST for Issue from the given arguments
     * Any of the arguments can be null since they are all optional, but one of tokenType and AppliesTo must be present
     */
    public  RequestSecurityToken createRSTForIssue(URI tokenType, URI requestType, URI context, AppliesTo scopes,
            Claims claims, Entropy entropy, Lifetime lt) throws WSTrustException {
       // if (tokenType==null || scopes==null)
         //   throw new WSTrustException("TokenType and AppliesTo cannot be both null");
        RequestSecurityToken rst = new RequestSecurityTokenImpl(tokenType, requestType, context, scopes, claims, entropy, lt, null);
        return rst;
    }
    
    /**
     * Create an RSTR for Issue from the given arguments. TokenType should be Issue.
     * Any of the arguments can be null since they are all optional, but one of RequestedSecurityToken or RequestedProofToken should be returned
     */
    public  RequestSecurityTokenResponse createRSTRForIssue(URI tokenType, URI context, RequestedSecurityToken token, AppliesTo scopes, RequestedAttachedReference attachedReference, RequestedUnattachedReference unattachedReference, RequestedProofToken proofToken, Entropy entropy, Lifetime lt) throws WSTrustException {
        return new RequestSecurityTokenResponseImpl(tokenType, context, token, scopes,
                attachedReference, unattachedReference, proofToken, entropy, lt, null);
    }
    
    /**
     * Create  a collection of RequestSecurityTokenResponse(s)
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionForIssue(URI tokenType, URI context, RequestedSecurityToken token, AppliesTo scopes, RequestedAttachedReference attached, RequestedUnattachedReference unattached, RequestedProofToken proofToken, Entropy entropy, Lifetime lt) throws WSTrustException {
        return new RequestSecurityTokenResponseCollectionImpl(tokenType, context, token, scopes, attached, unattached, proofToken, entropy, lt);
    }
    
    public RequestSecurityTokenResponseCollection createRSTRCollectionForIssue(List rstrs) throws WSTrustException {
         //RequestSecurityTokenResponseCollection rstrc = new RequestSecurityTokenResponseCollectionImpl();
        RequestSecurityTokenResponseCollectionImpl rstrc = new RequestSecurityTokenResponseCollectionImpl();
         for (int i = 0; i < rstrs.size(); i++) {
            rstrc.addRequestSecurityTokenResponse((RequestSecurityTokenResponse)rstrs.get(i));
        }
         return rstrc;
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
    public  IssuedTokens createIssuedTokens(RequestSecurityTokenResponseCollection issuedTokens) {
        return new IssuedTokensImpl(issuedTokens);
    }
    
    /**
     * Create an Entropy with a BinarySecret
     */
    public Entropy createEntropy(BinarySecret secret) {
        return new EntropyImpl(secret);
    }
    
    /**
     * Create an Entropy with an xenc:EncryptedKey
     */
    public  Entropy createEntropy(EncryptedKey key) {
        return new EntropyImpl(key);
    }
    
    public BinarySecret createBinarySecret(byte[] rawValue, String type) {
        return new BinarySecretImpl(rawValue, type);
    }

    public BinarySecret createBinarySecret(Element elem) throws WSTrustException {
        return new BinarySecretImpl(BinarySecretImpl.fromElement(elem));
    }
    
    public Claims createClaims(Element elem)throws WSTrustException {
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
     * Create a Lifetime.
     */
    public Lifetime createLifetime(AttributedDateTime created,  AttributedDateTime expires) {
        return new LifetimeImpl(created, expires);
    }
    
    public OnBehalfOf createOnBehalfOf(Token oboToken){
         return new OnBehalfOfImpl(oboToken);
    }

    public ActAs createActAs(Token actAsToken){
        return new ActAsImpl(actAsToken);
    }
    
    /**
     * Create a RequestedSecurityToken.
     */
    public RequestedSecurityToken createRequestedSecurityToken(Token token) {
        return new RequestedSecurityTokenImpl(token);
    }
    
     /**
     * Create a RequestedSecurityToken.
     */
    public RequestedSecurityToken createRequestedSecurityToken() {
        return new RequestedSecurityTokenImpl();
    }
    
    public DirectReference createDirectReference(String valueType, String uri){
        return new DirectReferenceImpl(valueType, uri);
    }
   
   public KeyIdentifier createKeyIdentifier(String valueType, String encodingType){
       return new KeyIdentifierImpl(valueType, encodingType);
   }
   
    public SecurityTokenReference createSecurityTokenReference(Reference ref){
        return new SecurityTokenReferenceImpl(ref);
    }
    /**
     * Create a RequestedAttachedReference.
     */
    public RequestedAttachedReference createRequestedAttachedReference(SecurityTokenReference str) {
        return new RequestedAttachedReferenceImpl(str);
    }
    
    /**
     * Create a RequestedUnattachedReference.
     */
    public RequestedUnattachedReference createRequestedUnattachedReference(SecurityTokenReference str) {
        return new RequestedUnattachedReferenceImpl(str);
    }
    
    /**
     * Create a RequestedProofToken.
     */
    public RequestedProofToken createRequestedProofToken() {
        return new RequestedProofTokenImpl();
    }
    
    
    /**
     *Create an RST for a Renewal Request
     */
    public  RequestSecurityToken createRSTForRenew(URI tokenType, URI requestType, URI context, RenewTarget target, AllowPostdating apd, Renewing renewingInfo) {
        return new RequestSecurityTokenImpl(tokenType, requestType, context, target, apd, renewingInfo);
    }
    
    public RenewTarget createRenewTarget(final SecurityTokenReference str){
        return new RenewTargetImpl(str);
    }
    
    public CancelTarget createCancelTarget(SecurityTokenReference str){
        return new CancelTargetImpl(str);
    }
    
    public ValidateTarget createValidateTarget(Token token){
         return new ValidateTargetImpl(token);
    }
    
    public SecondaryParameters createSecondaryParameters(){
        return new SecondaryParametersImpl();
    }
    
    public UseKey createUseKey(Token token, String sig){
        UseKey useKey = new UseKeyImpl(token);
        if (sig != null){
            useKey.setSignatureID(URI.create(sig));
        }
        
        return useKey;
    }
    
    /**
     *Create an RST for Token Cancellation
     */
    public  RequestSecurityToken createRSTForCancel(URI requestType, CancelTarget target) {
        return new RequestSecurityTokenImpl(null, requestType, target);
    }
    
    /**
     *Create an RSTR for a Successful Token Cancellation
     */
    public  RequestSecurityTokenResponse createRSTRForCancel() {
        RequestSecurityTokenResponse rstr =  new RequestSecurityTokenResponseImpl();
        rstr.setRequestedTokenCancelled(new RequestedTokenCancelledImpl());
        
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
    public  RequestSecurityToken createRSTForValidate(URI tokenType, URI requestType) {
        return new RequestSecurityTokenImpl(tokenType, requestType);
    }
    
    /**
     * create an RSTR for validate request.
     */
    public  RequestSecurityTokenResponse createRSTRForValidate(URI tokenType, RequestedSecurityToken token, Status status) {
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
        //rstrc.getRequestSecurityTokenResponses().addAll(rstrs);        
        
        for (int i = 0; i < rstrs.size(); i++) {
            ((RequestSecurityTokenResponseCollectionImpl)rstrc).addRequestSecurityTokenResponse(rstrs.get(i));
        }
        return rstrc;
    }
    
    
    /**
     * create an RST from a Source
     */
    public RequestSecurityToken createRSTFrom(Source src) {
        try {           
            javax.xml.bind.Unmarshaller u = getContext(WSTrustVersion.WS_TRUST_13).createUnmarshaller();
            JAXBElement<RequestSecurityTokenType> rstType = u.unmarshal(src, RequestSecurityTokenType.class);
            RequestSecurityTokenType type = rstType.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch ( Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * create an RST from DOM Element
     */
    public  RequestSecurityToken createRSTFrom(Element elem) {
        try {
            javax.xml.bind.Unmarshaller u = getContext(WSTrustVersion.WS_TRUST_13).createUnmarshaller();
            JAXBElement<RequestSecurityTokenType> rstType = u.unmarshal(elem, RequestSecurityTokenType.class);
            RequestSecurityTokenType type = rstType.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch ( Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * create an RSTR from a Source
     */
    public  RequestSecurityTokenResponse createRSTRFrom(Source src) {
        try {
            javax.xml.bind.Unmarshaller u = getContext(WSTrustVersion.WS_TRUST_13).createUnmarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstType = u.unmarshal(src, RequestSecurityTokenResponseType.class);
            RequestSecurityTokenResponseType type = rstType.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch ( Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * create an RSTR from DOM Element
     */
    public  RequestSecurityTokenResponse createRSTRFrom(Element elem) {
        try {
            javax.xml.bind.Unmarshaller u = getContext(WSTrustVersion.WS_TRUST_13).createUnmarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstType = u.unmarshal(elem, RequestSecurityTokenResponseType.class);
            RequestSecurityTokenResponseType type = rstType.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch ( Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Create RSTR Collection from Source
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(Source src) {
         try {
            javax.xml.bind.Unmarshaller u = getContext(WSTrustVersion.WS_TRUST_13).createUnmarshaller();
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
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(Element elem) {
        try {
            javax.xml.bind.Unmarshaller u = getContext(WSTrustVersion.WS_TRUST_13).createUnmarshaller();
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
    public RequestSecurityToken createRSTFrom(JAXBElement elem) {
        try {
            RequestSecurityTokenType type = (RequestSecurityTokenType)elem.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch (Exception e) {
            throw new RuntimeException("There was a problem while creating RST from JAXBElement", e);
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
    public  RequestSecurityTokenResponse createRSTRFrom(JAXBElement elem) {
        try {
            RequestSecurityTokenResponseType type = (RequestSecurityTokenResponseType)elem.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch (Exception e) {
            throw new RuntimeException("There was a problem while creating RSTR from JAXBElement", e);
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
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(JAXBElement elem) {
        try {
            RequestSecurityTokenResponseCollectionType type = (RequestSecurityTokenResponseCollectionType)elem.getValue();
            return new RequestSecurityTokenResponseCollectionImpl(type);
        } catch (Exception e) {
            throw new RuntimeException("There was a problem while creating RSTRCollection from JAXBElement", e);
        }
    }
    
     public Object createResponseFrom(JAXBElement elem){
        String local = elem.getName().getLocalPart();
        if (local.equalsIgnoreCase("RequestSecurityTokenResponseType")) {
           return createRSTRFrom(elem);
        }else{
            return createRSTRCollectionFrom(elem);
        }
    }
    
     public SecurityTokenReference createSecurityTokenReference(JAXBElement elem){
          try {
            SecurityTokenReferenceType type = (SecurityTokenReferenceType)elem.getValue();
            return new SecurityTokenReferenceImpl(type);
        } catch (Exception e) {
            throw new RuntimeException("There was a problem while creating STR from JAXBElement", e);
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
     public JAXBElement toJAXBElement(SecurityTokenReference str){
         JAXBElement<SecurityTokenReferenceType> strElement =
            (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)str);
        return strElement;
     }
    
    /**
     * convert an RST to a JAXBElement
     */
    public JAXBElement toJAXBElement(RequestSecurityToken rst) {
        JAXBElement<RequestSecurityTokenType> rstElement=
                (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
        return rstElement;
    }
    
    /**
     * convert an RSTR to a JAXBElement
     */
    public  JAXBElement toJAXBElement(RequestSecurityTokenResponse rstr) {
        JAXBElement<RequestSecurityTokenResponseType> rstElement=
                (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
        return rstElement;
    }
    
    
    /**
     * convert a Entropy to a JAXBElement
     */
    public  JAXBElement toJAXBElement(Entropy entropy) {
        JAXBElement<EntropyType> etElement=
                (new ObjectFactory()).createEntropy((EntropyType)entropy);
        return etElement;
    }
    
    /**
     * convert an RSTR Collection to a JAXBElement
     */
    public  JAXBElement toJAXBElement(RequestSecurityTokenResponseCollection rstrCollection) {
        JAXBElement<RequestSecurityTokenResponseCollectionType> rstElement=
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
    public Source toSource(RequestSecurityToken rst) {
        return new DOMSource(toElement(rst));
    }
    
    /**
     * Marshal an RSTR to a Source
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public Source toSource(RequestSecurityTokenResponse rstr) {
        return new DOMSource(toElement(rstr));
    }
    
    /**
     * Marshal an RSTR Collection to a Source
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public  Source toSource(RequestSecurityTokenResponseCollection rstrCollection) {
         return new DOMSource(toElement(rstrCollection));
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
    public Element toElement(RequestSecurityToken rst) {
        try {
            Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
            JAXBElement<RequestSecurityTokenType> rstElement =  (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
            getMarshaller().marshal(rstElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    
    
    /**
     * Marshal an RSTR to DOM Element
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public Element toElement(RequestSecurityTokenResponse rstr) {
        try {
            Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            getMarshaller().marshal(rstrElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
     public Element toElement(RequestSecurityTokenResponse rstr, Document doc) {
        try { 
           // javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
           JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
           getMarshaller().marshal(rstrElement, doc);
           return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Marshal an RSTR Collection to a DOM Element
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public  Element toElement(RequestSecurityTokenResponseCollection rstrCollection) {
        try {
            Document doc = WSTrustUtil.newDocument();
           // javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();            
            JAXBElement<RequestSecurityTokenResponseCollectionType> rstrElement =
                    (new ObjectFactory()).createRequestSecurityTokenResponseCollection((RequestSecurityTokenResponseCollectionType)rstrCollection);                                    
            getMarshaller().marshal(rstrElement, doc);   

            return doc.getDocumentElement();                        
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    public Element toElement(BinarySecret bs){
        try {
            Document doc = WSTrustUtil.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
            JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)bs);
            getMarshaller().marshal(bsElement, doc);
            return doc.getDocumentElement();            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Marshal an STR to a DOM Element.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Element toElement(SecurityTokenReference str, Document doc) {
        try {
            if(doc == null){
                doc = WSTrustUtil.newDocument();
            }
            
            //javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
            JAXBElement<SecurityTokenReferenceType> strElement =  (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)str);
            getMarshaller().marshal(strElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Marshal an BinarySecret to a DOM Element.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Element toElement(BinarySecret bs, Document doc) {
        try {
            if(doc == null){
                doc = WSTrustUtil.newDocument();
            }
            
            //javax.xml.bind.Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
            JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)bs);
            getMarshaller().marshal(bsElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
     
    public Marshaller getMarshaller(){
         try {
            Marshaller marshaller = getContext(WSTrustVersion.WS_TRUST_13).createMarshaller();
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
