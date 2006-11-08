/*
 * $Id: WSTrustElementFactoryImpl.java,v 1.5 2006-11-08 21:06:03 jdg6688 Exp $
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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;

import com.sun.xml.ws.security.trust.elements.AllowPostdating;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.CancelTarget;
import com.sun.xml.ws.security.trust.elements.Claims;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.IssuedTokens;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RenewTarget;
import com.sun.xml.ws.security.trust.elements.Renewing;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestedTokenCancelled;
import com.sun.xml.ws.security.trust.elements.Status;

import com.sun.xml.ws.security.trust.impl.elements.AllowPostdatingImpl;
import com.sun.xml.ws.security.trust.impl.elements.BinarySecretImpl;
import com.sun.xml.ws.security.trust.impl.elements.CancelTargetImpl;
import com.sun.xml.ws.security.trust.impl.elements.ClaimsImpl;
import com.sun.xml.ws.security.trust.impl.elements.EntropyImpl;
import com.sun.xml.ws.security.trust.impl.elements.IssuedTokensImpl;
import com.sun.xml.ws.security.trust.impl.elements.LifetimeImpl;
import com.sun.xml.ws.security.trust.impl.elements.RenewTargetImpl;
import com.sun.xml.ws.security.trust.impl.elements.RenewingImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestSecurityTokenResponseImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestSecurityTokenResponseCollectionImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedProofTokenImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedAttachedReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedUnattachedReferenceImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestSecurityTokenImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedSecurityTokenImpl;
import com.sun.xml.ws.security.trust.impl.elements.RequestedTokenCancelledImpl;
import com.sun.xml.ws.security.trust.impl.elements.StatusImpl;
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
import com.sun.xml.ws.security.wsu10.AttributedDateTime;

import com.sun.xml.ws.security.trust.util.TrustNamespacePrefixMapper;

import javax.xml.bind.PropertyException;


import java.net.URI;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;

import javax.xml.transform.Source;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;

import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.impl.bindings.RequestSecurityTokenResponseCollectionType;
import javax.xml.transform.dom.DOMSource;
import javax.xml.bind.util.JAXBSource;

import javax.xml.bind.Marshaller;

/**
 * A Factory for creating the WS-Trust schema elements,
 * and marshalling/un-marshalling them.
 *
 * @author Manveen Kaur
 */
public class WSTrustElementFactoryImpl extends WSTrustElementFactory {
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private Marshaller marshaller = null;
    
    public WSTrustElementFactoryImpl(){
        try {
            marshaller = getContext().createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new com.sun.xml.ws.security.trust.util.TrustNamespacePrefixMapper());
        } catch( PropertyException e ) {
             throw new RuntimeException(e.getMessage());
        } catch (JAXBException jbe) {
            throw new RuntimeException(jbe.getMessage());
        }        
    }
    
    /**
     * Create an RST for Issue from the given arguments
     * Any of the arguments can be null since they are all optional, but one of tokenType and AppliesTo must be present
     */
    public  RequestSecurityToken createRSTForIssue(URI tokenType, URI requestType, URI context, AppliesTo scopes,
            Claims claims, Entropy entropy, Lifetime lt) throws WSTrustException {
        
        if (tokenType==null && scopes==null) {
            log.log(Level.WARNING, "WST1003.tokentype.appliesto.null");
        }
        RequestSecurityToken rst = new RequestSecurityTokenImpl(tokenType, requestType, context, scopes, claims, entropy, lt, null);
        return rst;
    }
    
    /**
     * Create an RSTR for Issue from the given arguments. TokenType should be Issue.
     * Any of the arguments can be null since they are all optional, but one of RequestedSecurityToken or RequestedProofToken should be returned
     */
    public  RequestSecurityTokenResponse createRSTRForIssue(URI tokenType, URI context, RequestedSecurityToken token, AppliesTo scopes, RequestedAttachedReference attachedReference, RequestedUnattachedReference unattachedReference, RequestedProofToken proofToken, Entropy entropy, Lifetime lt) throws WSTrustException {
        RequestSecurityTokenResponse rstr =
                new RequestSecurityTokenResponseImpl(tokenType, context, token, scopes,
                attachedReference, unattachedReference, proofToken, entropy, lt, null);
        return rstr;
    }
    
    /**
     * Create a collection of RequestSecurityTokenResponse(s)
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionForIssue(URI tokenType, URI context, RequestedSecurityToken token, AppliesTo scopes, RequestedAttachedReference attached, RequestedUnattachedReference unattached, RequestedProofToken proofToken, Entropy entropy, Lifetime lt) throws WSTrustException {
        RequestSecurityTokenResponseCollection rstrCollection =
                new RequestSecurityTokenResponseCollectionImpl(tokenType, context, token, scopes, attached, unattached, proofToken, entropy, lt);
        return rstrCollection;
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
    
    /**
     * Create a Lifetime.
     */
    public Lifetime createLifetime(AttributedDateTime created,  AttributedDateTime expires) {
        return new LifetimeImpl(created, expires);
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
    
    public CancelTarget createCancelTarget(SecurityTokenReference str){
        return new CancelTargetImpl(str);
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
        log.log(Level.INFO,"WST0003.created.rstr.cancel", new Object[]{rstr.toString()});
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
    
    
    /**
     * create an RST from a Source
     */
    public RequestSecurityToken createRSTFrom(Source src) {
        try {
            javax.xml.bind.Unmarshaller u = getContext().createUnmarshaller();
            JAXBElement<RequestSecurityTokenType> rstType = u.unmarshal(src, RequestSecurityTokenType.class);
            RequestSecurityTokenType type = rstType.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    "WST0005.fail.rst.source", new Object[]{src.toString(), ex});
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * create an RST from DOM Element
     */
    public  RequestSecurityToken createRSTFrom(Element elem) {
        try {
            javax.xml.bind.Unmarshaller u = getContext().createUnmarshaller();
            JAXBElement<RequestSecurityTokenType> rstType = u.unmarshal(elem, RequestSecurityTokenType.class);
            RequestSecurityTokenType type = rstType.getValue();
            return new RequestSecurityTokenImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    "WST0006.fail.rst.elem", new Object[]{elem.toString(), ex});
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * create an RSTR from a Source
     */
    public  RequestSecurityTokenResponse createRSTRFrom(Source src) {
        try {
            javax.xml.bind.Unmarshaller u = getContext().createUnmarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstType = u.unmarshal(src, RequestSecurityTokenResponseType.class);
            RequestSecurityTokenResponseType type = rstType.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    "WST0007.fail.rstr.source", new Object[]{src.toString(), ex});
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * create an RSTR from DOM Element
     */
    public  RequestSecurityTokenResponse createRSTRFrom(Element elem) {
        try {
            javax.xml.bind.Unmarshaller u = getContext().createUnmarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstType = u.unmarshal(elem, RequestSecurityTokenResponseType.class);
            RequestSecurityTokenResponseType type = rstType.getValue();
            return new RequestSecurityTokenResponseImpl(type);
        } catch ( Exception ex) {
            log.log(Level.SEVERE,
                    "WST0008.fail.rstr.elem", new Object[]{elem.toString(), ex});
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Create RSTR Collection from Source
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(Source src) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
    
    /**
     * Create RSTR Collection from Element
     */
    public  RequestSecurityTokenResponseCollection createRSTRCollectionFrom(Element elem) {
        throw new UnsupportedOperationException("Not yet implemented!");
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
            log.log(Level.SEVERE,"WST0010.failed.creation.from.jaxbele", new Object[] {"RST", e});
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
            log.log(Level.SEVERE,"WST0010.failed.creation.from.jaxbele", new Object[] {"RSTR", e});
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
            log.log(Level.SEVERE,"WST0010.failed.creation.from.jaxbele", new Object[] {"RSTRCollection", e});
            throw new RuntimeException("There was a problem while creating RSTRCollection from JAXBElement", e);
        }
    }
    
    public SecurityTokenReference createSecurityTokenReference(JAXBElement elem){
        try {
            SecurityTokenReferenceType type = (SecurityTokenReferenceType)elem.getValue();
            return new SecurityTokenReferenceImpl(type);
        } catch (Exception e) {
            log.log(Level.SEVERE,"WST0010.failed.creation.from.jaxbele", new Object[] {"STR", e});
            throw new RuntimeException("There was a problem while creating STR from JAXBElement", e);
        }
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
    
    /**
     * Marshal an RST to a Source.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Source toSource(RequestSecurityToken rst) {
       // return new DOMSource(toElement(rst));
         try{
            return new JAXBSource(marshaller, toJAXBElement(rst));
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Marshal an RSTR to a Source
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public Source toSource(RequestSecurityTokenResponse rstr) {
        //return new DOMSource(toElement(rstr));
        try{
            return new JAXBSource(marshaller, toJAXBElement(rstr));
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Marshal an RSTR Collection to a Source
     * <p>
     * Note: Useful for STS implementations which are JAXWS Providers
     * </p>
     */
    public  Source toSource(RequestSecurityTokenResponseCollection rstrCollection) {
        //return new DOMSource(toElement(rstrCollection));
         try{
            return new JAXBSource(marshaller, toJAXBElement(rstrCollection));
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Marshal an RST to a DOM Element.
     * <p>
     * Note: Useful for Dispatch Client implementations
     * </p>
     */
    public Element toElement(RequestSecurityToken rst) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenType> rstElement =  (new ObjectFactory()).createRequestSecurityToken((RequestSecurityTokenType)rst);
            marshaller.marshal(rstElement, doc);
            return doc.getDocumentElement();
            
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            log.log(Level.SEVERE,"WST0011.parserconfig.ex.toElement", ex);
            throw new RuntimeException(ex.getMessage(), ex);
        } catch (JAXBException e) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", e);
            throw new RuntimeException(e.getMessage(), e);
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
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.marshal(rstrElement, doc);
            return doc.getDocumentElement();
            
        } catch (Exception ex) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    public Element toElement(RequestSecurityTokenResponse rstr, Document doc) {
        try {
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenResponseType> rstrElement =  (new ObjectFactory()).createRequestSecurityTokenResponse((RequestSecurityTokenResponseType)rstr);
            marshaller.marshal(rstrElement, doc);
            return doc.getDocumentElement();
            
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", ex);
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
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<RequestSecurityTokenResponseCollectionType> rstElement =
                    (new ObjectFactory()).createRequestSecurityTokenResponseCollection((RequestSecurityTokenResponseCollectionType)rstrCollection);
            marshaller.marshal(rstElement, doc);
            return doc.getDocumentElement();
        } catch (javax.xml.parsers.ParserConfigurationException pe) {
            log.log(Level.SEVERE,"WST0011.parserconfig.ex.toElement", pe);
            throw new RuntimeException(pe.getMessage(), pe);
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
    
    public Element toElement(BinarySecret bs){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)bs);
            marshaller.marshal(bsElement, doc);
            return doc.getDocumentElement();
        } catch (javax.xml.parsers.ParserConfigurationException pe) {
            log.log(Level.SEVERE,"WST0011.parserconfig.ex.toElement", new Object[] {bs, pe});
            throw new RuntimeException(pe.getMessage(), pe);
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", ex);
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
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.newDocument();
            }
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<SecurityTokenReferenceType> strElement =  (new com.sun.xml.ws.security.secext10.ObjectFactory()).createSecurityTokenReference((SecurityTokenReferenceType)str);
            marshaller.marshal(strElement, doc);
            return doc.getDocumentElement();
        } catch (javax.xml.parsers.ParserConfigurationException pe) {
            log.log(Level.SEVERE,"WST0011.parserconfig.ex.toElement", pe);
            throw new RuntimeException(pe.getMessage(), pe);
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", ex);
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
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.newDocument();
            }
            
            //javax.xml.bind.Marshaller marshaller = getContext().createMarshaller();
            JAXBElement<BinarySecretType> bsElement =
                    (new ObjectFactory()).createBinarySecret((BinarySecretType)bs);
            marshaller.marshal(bsElement, doc);
            return doc.getDocumentElement();
            
        } catch (javax.xml.parsers.ParserConfigurationException pe) {
            // TODOFIXME - print JAXBelement
            log.log(Level.SEVERE,"WST0011.parserconfig.ex.toElement", pe);
            throw new RuntimeException(pe.getMessage(), pe);
        } catch (JAXBException ex) {
            log.log(Level.SEVERE,"WST0012.jaxb.ex.toElement", ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }     
    
}
