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


import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;
import com.sun.xml.security.core.dsig.ObjectFactory;
import com.sun.xml.security.core.xenc.CipherDataType;
import com.sun.xml.security.core.xenc.EncryptedDataType;
import com.sun.xml.security.core.xenc.EncryptionMethodType;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.SecurityTokenReference;
import com.sun.xml.security.core.dsig.ObjectFactory;
import com.sun.xml.ws.security.opt.crypto.JAXBData;
import com.sun.xml.ws.security.opt.crypto.dsig.Signature;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.DSAKeyValue;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.RSAKeyValue;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.X509Data;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignContext;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignatureFactory;
import com.sun.xml.ws.security.opt.impl.crypto.JAXBDataImpl;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.dsig.DSigResolver;
import com.sun.xml.ws.security.opt.impl.dsig.EnvelopedSignedMessageHeader;
import com.sun.xml.ws.security.opt.impl.dsig.JAXBSignatureHeaderElement;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedData;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedKey;
import com.sun.xml.ws.security.opt.impl.keyinfo.SAMLToken;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.NameID;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import com.sun.xml.ws.security.opt.api.EncryptedKey;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.crypto.Data;
import javax.xml.crypto.URIDereferencer;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.URIReferenceException;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.transform.Source;

import com.sun.xml.wss.SubjectAccessor;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.Configuration;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.WSTrustException;
import com.sun.xml.ws.security.trust.WSTrustContract;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.trust.elements.BinarySecret;
import com.sun.xml.ws.security.trust.elements.Entropy;
import com.sun.xml.ws.security.trust.elements.Lifetime;
import com.sun.xml.ws.security.trust.elements.RequestedProofToken;
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;
import com.sun.xml.ws.security.trust.elements.RequestSecurityToken;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponse;
import com.sun.xml.ws.security.trust.elements.RequestSecurityTokenResponseCollection;
import com.sun.xml.ws.security.trust.elements.RequestedSecurityToken;
import com.sun.xml.ws.security.wsu10.AttributedDateTime;

import com.sun.xml.wss.core.reference.X509ThumbPrintIdentifier;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.saml.Advice;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.Attribute;
import com.sun.xml.wss.saml.AttributeStatement;
import com.sun.xml.wss.saml.Conditions;
import com.sun.xml.wss.saml.NameIdentifier;
import com.sun.xml.wss.saml.SAMLAssertionFactory;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.saml.SubjectConfirmation;
import com.sun.xml.wss.saml.SubjectConfirmationData;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType;

import javax.security.auth.Subject;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.misc.Base64;
import com.sun.xml.wss.impl.misc.SecurityUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

/**
 *
 * @author
 */
public class SBIssuedSamlTokenContractImpl extends IssueSamlTokenContract{
    
    //move to base class
    private static final String SAML_HOLDER_OF_KEY = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    protected static final String PRINCIPAL = "principal";
    private SOAPVersion soapVersion = SOAPVersion.SOAP_11;
    WSSElementFactory wef = new WSSElementFactory(SOAPVersion.SOAP_11);//TODO:: Pick up proper SOAPVersion.
    
    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    /** Creates a new instance of SBIssuedSamlTokenContractImpl */
    public SBIssuedSamlTokenContractImpl(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
    }
    
    public SBIssuedSamlTokenContractImpl() {
    }
    
    protected Token createSAMLAssertion(String appliesTo, String tokenType, String keyType, String assertionId, String issuer, Map claimedAttrs, IssuedTokenContext context) throws WSTrustException {
        Token token = null;
        
        CallbackHandler callbackHandler = config.getCallbackHandler();
        
        try{
            NamespaceContextEx nsContext = null;
            if(soapVersion == soapVersion.SOAP_11){
                nsContext = new NamespaceContextEx();
            }else{
                nsContext  = new NamespaceContextEx(true);
            }
            nsContext.addEncryptionNS();
            nsContext.addExc14NS();
            nsContext.addSAMLNS();
            nsContext.addSignatureNS();
            nsContext.addWSSNS();
            // Get the service certificate
            X509Certificate serCert = getServiceCertificate(callbackHandler);
            
            // Create the KeyInfo for SubjectConfirmation
            KeyInfo keyInfo = createKeyInfo(keyType, serCert, context);
            
            // Create SAML assertion
            Assertion assertion = null;
            SAMLToken st = null;
            if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
                    WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML11Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs);
                st = new SAMLToken(assertion,SAMLJAXBUtil.getJAXBContext(),soapVersion);
                
            } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML20Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs);
                st = new SAMLToken(assertion,SAMLJAXBUtil.getJAXBContext(),soapVersion);
            } else{
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0031_UNSUPPORTED_TOKEN_TYPE(tokenType));
                throw new WSTrustException("Unsupported token type: " + tokenType);
            }
            
            // Get the STS's public and private key
            SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            Callback skc = new SignatureKeyCallback(request);
            Callback[] callbacks = {skc};
            callbackHandler.handle(callbacks);
            PrivateKey stsPrivKey = request.getPrivateKey();
            
            // Sign the assertion with STS's private key
            //Element signedAssertion = assertion.sign(request.getX509Certificate(), stsPrivKey);
            SecurityHeaderElement signedAssertion = createSignature(request.getX509Certificate().getPublicKey(),stsPrivKey,st,nsContext);
            
            //javax.xml.bind.Unmarshaller u = eleFac.getContext().createUnmarshaller();
            //JAXBElement<AssertionType> aType = u.unmarshal(signedAssertion, AssertionType.class);
            //assertion =  new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion(aType.getValue());
            token = new GenericToken(signedAssertion);
            
            if (config.getEncryptIssuedToken()){
                String id = "uuid-" + UUID.randomUUID().toString();
                int keysizeInBytes = 32;
                byte[] skey = WSTrustUtil.generateRandomSecret(keysizeInBytes);
                Key key = new SecretKeySpec(skey, "AES");
                
                KeyInfo encKeyInfo = new KeyInfo();
                EncryptedKey encKey = encryptKey(key, serCert);
                encKeyInfo.getContent().add(encKey);
                EncryptedDataType edt = createEncryptedData(id,MessageConstants.AES_BLOCK_ENCRYPTION_256,encKeyInfo,false);
                
                
                JAXBEncryptedData jed = new JAXBEncryptedData(edt,new SSEData((SecurityElement)signedAssertion,false,nsContext),soapVersion);
                token = new GenericToken(jed);
            }else{
                token = new GenericToken(signedAssertion);
            }
        } catch (XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(ex));
            throw new WSTrustException("Error creating SAML Assertion", ex);
        }catch (Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(ex));
            throw new WSTrustException("Error creating SAML Assertion", ex);
        }
        return token;
    }
    
    
    
    private Assertion createSAML11Assertion(String assertionId, String issuer, String appliesTo, KeyInfo keyInfo, Map claimedAttrs) throws WSTrustException{
        Assertion assertion = null;
        try{
            SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
            
            GregorianCalendar issuerInst = new GregorianCalendar();
            GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)config.getIssuedTokenTimeout());
            
            Conditions conditions =
                    samlFac.createConditions(issuerInst, notOnOrAfter, null, null, null);
            Advice advice = samlFac.createAdvice(null, null, null);
            
            List<String>  confirmationMethods = new ArrayList<String>();
            confirmationMethods.add(SAML_HOLDER_OF_KEY);
            
            SubjectConfirmation subjectConfirmation = samlFac.createSubjectConfirmation(confirmationMethods,null, keyInfo);
            
            com.sun.xml.wss.saml.Subject subj = null;
            QName principal = (QName)claimedAttrs.get(PRINCIPAL);
            if (principal != null){
                NameIdentifier nameId = samlFac.createNameIdentifier(principal.getLocalPart(), null, null);
                subj = samlFac.createSubject(nameId, subjectConfirmation);
                claimedAttrs.remove(PRINCIPAL);
            }
            
            List<Attribute> attrs = new ArrayList<Attribute>();
            Set keys = claimedAttrs.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()){
                String attrKey = (String)iterator.next();
                QName value = (QName)claimedAttrs.get(attrKey);
                List<String> values = new ArrayList<String>();
                values.add(value.getLocalPart());
                Attribute attr = samlFac.createAttribute(attrKey, value.getNamespaceURI(), values);
                attrs.add(attr);
            }
            AttributeStatement statement = samlFac.createAttributeStatement(subj, attrs);
            List<AttributeStatement> statements = new ArrayList<AttributeStatement>();
            statements.add(statement);
            assertion =
                    samlFac.createAssertion(assertionId, issuer, issuerInst, conditions, advice, statements);
        }catch(SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(ex));
            throw new WSTrustException("Unable to create SAML assertion", ex);
        }catch(XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(ex));
            throw new WSTrustException("Unable to create the SAML assertion", ex);
        }
        
        return assertion;
    }
    
    
    
    private Assertion createSAML20Assertion(String assertionId, String issuer, String appliesTo, KeyInfo keyInfo, Map claimedAttrs) throws WSTrustException{
        Assertion assertion = null;
        try{
            SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
            
            // Create Conditions
            GregorianCalendar issueInst = new GregorianCalendar();
            GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)config.getIssuedTokenTimeout());
            
            Conditions conditions = samlFac.createConditions(issueInst, notOnOrAfter, null, null, null, null);
            
            // Create Subject
            
            SubjectConfirmationData subjComfData = samlFac.createSubjectConfirmationData(
                    null, null, issueInst, notOnOrAfter, appliesTo, keyInfo);
            
            SubjectConfirmation subjectConfirmation = samlFac.createSubjectConfirmation(
                    null, subjComfData, SAML_HOLDER_OF_KEY);
            
            com.sun.xml.wss.saml.Subject subj = null;
            QName principal = (QName)claimedAttrs.get(PRINCIPAL);
            
            if (principal != null){
                NameID nameId = samlFac.createNameID(principal.getLocalPart(), principal.getNamespaceURI(), null);
                subj = samlFac.createSubject(nameId, subjectConfirmation);
                claimedAttrs.remove(PRINCIPAL);
            }
            
            // Create AttributeStatement
            List<Attribute> attrs = new ArrayList<Attribute>();
            Set keys = claimedAttrs.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()){
                String attrKey = (String)iterator.next();
                QName value = (QName)claimedAttrs.get(attrKey);
                List<String> values = new ArrayList<String>();
                values.add(value.getLocalPart());
                Attribute attr = samlFac.createAttribute(attrKey, values);
                attrs.add(attr);
            }
            AttributeStatement statement = samlFac.createAttributeStatement(attrs);
            List<AttributeStatement> statements = new ArrayList<AttributeStatement>();
            statements.add(statement);
            
            NameID issuerID = samlFac.createNameID(issuer, null, null);
            
            // Create Assertion
            assertion =
                    samlFac.createAssertion(assertionId, issuerID, issueInst, conditions, null, subj, statements);
        }catch(SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(ex));
            throw new WSTrustException("Unable to create SAML assertion", ex);
        }catch(XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(ex));
            throw new WSTrustException("Unable to create the SAML assertion", ex);
        }
        
        return assertion;
    }
    
    private KeyInfo createKeyInfo(String keyType, X509Certificate serCert, IssuedTokenContext ctx)throws WSTrustException{
        KeyInfo keyInfo = new KeyInfo();
        if (WSTrustConstants.SYMMETRIC_KEY.equals(keyType)){
            byte[] key = ctx.getProofKey();
            if (!config.getEncryptIssuedToken() && config.getEncryptIssuedKey()){
                try{
                    Key secKey = new SecretKeySpec(key, "AES");
                    EncryptedKey encKey = encryptKey(secKey, serCert);
                    keyInfo.getContent().add(encKey);
                }catch(Exception ex){
                    throw new WSTrustException(ex.getMessage(), ex);
                }
            }else{
                BinarySecret bs = eleFac.createBinarySecret(key, BinarySecret.SYMMETRIC_KEY_TYPE);
                keyInfo.getContent().add(bs);
            }
        }else if(WSTrustConstants.PUBLIC_KEY.equals(keyType)){
            
            X509Data x509Data = new X509Data();
            Set certs = ctx.getRequestorSubject().getPublicCredentials();
            if(certs == null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
                throw new WSTrustException("Unable to obtain client certificate");
            }
            boolean addedClientCert = false;
            for(Object o : certs){
                if(o instanceof X509Certificate){
                    X509Certificate clientCert = (X509Certificate)o;
                    
                    ObjectFactory dsigOF = new ObjectFactory();
                    JAXBElement<byte[]> certElement;
                    try {
                        certElement = dsigOF.createX509DataTypeX509Certificate(clientCert.getEncoded());
                    } catch (CertificateEncodingException ex) {
                        ex.printStackTrace();
                        throw new WSTrustException("Unable to create KeyInfo",ex);
                    }
                    x509Data.getContent().add(certElement);
                    addedClientCert = true;
                    
                }
            }
            if(!addedClientCert){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
                throw new WSTrustException("Unable to obtain client certificate");
            }
            keyInfo.getContent().add(x509Data);
        }
        
        return keyInfo;
    }
    
    private EncryptedKey encryptKey(Key key, X509Certificate cert) throws XWSSecurityException{
        KeyInfo ki = null;
        KeyIdentifier keyIdentifier = wef.createKeyIdentifier();
        keyIdentifier.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
        keyIdentifier.updateReferenceValue(cert);
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        SecurityTokenReference str = wef.createSecurityTokenReference(keyIdentifier);
        ki = wef.createKeyInfo((com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference) str);
        
        EncryptedKey jEK = wef.createEncryptedKey(null,MessageConstants.RSA_OAEP_KEY_TRANSPORT,ki,cert.getPublicKey(),key);
        return jEK;
    }
    
    
    //common method to be moved to Base Class
    private X509Certificate getServiceCertificate(CallbackHandler callbackHandler)throws WSTrustException{
        // Get the service certificate
        EncryptionKeyCallback.AliasX509CertificateRequest req = new EncryptionKeyCallback.AliasX509CertificateRequest(config.getCertAlias());
        EncryptionKeyCallback ec = new EncryptionKeyCallback(req);
        Callback[] callbacks = {ec};
        try{
            callbackHandler.handle(callbacks);
        }catch(IOException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(ex));
            throw new WSTrustException("Unable to get the service certificate", ex);
        }catch(UnsupportedCallbackException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(ex));
            throw new WSTrustException("Unable to get the service certificate", ex);
        }
        
        return req.getX509Certificate();
    }
    
    
    public EncryptedDataType createEncryptedData(String id,String dataEncAlgo,KeyInfo keyInfo,boolean contentOnly){
        EncryptedDataType edt = new EncryptedDataType();
        if(contentOnly){
            edt.setType(MessageConstants.ENCRYPT_ELEMENT_CONTENT);
        }else{
            edt.setType(MessageConstants.ENCRYPT_ELEMENT);
        }
        EncryptionMethodType emt = new EncryptionMethodType();
        emt.setAlgorithm(dataEncAlgo);
        edt.setEncryptionMethod(emt);
        CipherDataType ct = new CipherDataType();
        ct.setCipherValue("ed".getBytes());
        edt.setCipherData(ct);
        edt.setId(id);
        if(keyInfo != null){
            edt.setKeyInfo(keyInfo);
        }
        return edt;
    }
    
    protected boolean isAuthorized(Subject subject, String appliesTo, String tokenType, String keyType){
        return true;
    }
    
    protected Map getClaimedAttributes(Subject subject, String appliesTo, String tokenType){
        Set<Principal> principals = subject.getPrincipals();
        Map<String, QName> attrs = new HashMap<String, QName>();
        if (principals != null){
            Iterator iterator = principals.iterator();
            while (iterator.hasNext()){
                String name = principals.iterator().next().getName();
                if (name != null){
                    //attrs.add(name);
                    attrs.put(PRINCIPAL, new QName("http://sun.com", name));
                    break;
                }
            }
        }
        
        if (attrs.get(PRINCIPAL) == null){
            attrs.put(PRINCIPAL, new QName("http://sun.com", "principal"));
        }
        
        // Set up a dumy attribute value
        String key = "name";
        QName value = new QName("http://sun.com", "value");
        attrs.put(key, value);
        
        return attrs;
    }
    
    
    private SecurityHeaderElement createSignature(PublicKey pubKey,Key signingKey,SAMLToken samlToken,NamespaceContextEx nsContext)throws WSTrustException{
        try{
            JAXBSignatureFactory signatureFactory = JAXBSignatureFactory.newInstance();
            C14NMethodParameterSpec spec = null;
            CanonicalizationMethod canonicalMethod =
                    signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,spec);
            DigestMethod dm;
            dm = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
            SignatureMethod signatureMethod;
            signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
            
            //Note : Signature algorithm parameters null for now , fix me.
            
            ArrayList<Transform> transformList = new ArrayList<Transform>();
            Transform tr1;
            
            tr1 = signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            
            Transform tr2;
            
            tr2 = signatureFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
            
            transformList.add(tr1);
            transformList.add(tr2);
            
            String uri = "#" + "uuid-" + UUID.randomUUID().toString();
            Reference ref = signatureFactory.newReference(uri,dm,transformList, null, null);
            
            // Create the SignedInfo
            SignedInfo si = signatureFactory.newSignedInfo(canonicalMethod,signatureMethod,Collections.singletonList(ref));
            
            KeyValue kv;
            
            //kv = kif.newKeyValue(pubKey);
            if (pubKey instanceof java.security.interfaces.DSAPublicKey) {
                DSAKeyValue dsa = null;
                DSAPublicKey key = (DSAPublicKey)pubKey;
                
                byte[] p = key.getParams().getP().toByteArray();
                byte[] q = key.getParams().getQ().toByteArray();
                byte[] g = key.getParams().getG().toByteArray();
                byte[] y = key.getY().toByteArray();
                dsa = signatureFactory.newDSAKeyValue(p,q,g,y,null,null,null);
                kv = signatureFactory.newKeyValue(Collections.singletonList(dsa));
                
            } else if (pubKey instanceof java.security.interfaces.RSAPublicKey) {
                RSAKeyValue rsa = null;
                RSAPublicKey key = (RSAPublicKey)pubKey;
                rsa = signatureFactory.newRSAKeyValue(key.getModulus().toByteArray(),key.getPublicExponent().toByteArray());
                kv = signatureFactory.newKeyValue(Collections.singletonList(rsa));
            }else{
                throw new WSTrustException("Unsupported PublicKey");
            }
            
            // Create a KeyInfo and add the KeyValue to it
            javax.xml.crypto.dsig.keyinfo.KeyInfo ki = signatureFactory.newKeyInfo(Collections.singletonList(kv));
            JAXBSignContext signContext = new JAXBSignContext(signingKey);
            
            SSEData data = null;
            signContext.setURIDereferencer(new DSigResolver(data));
            com.sun.xml.ws.security.opt.crypto.dsig.Signature signature = (Signature) signatureFactory.newXMLSignature(si,ki);
            JAXBSignatureHeaderElement jhe =  new JAXBSignatureHeaderElement(signature,soapVersion,(XMLSignContext)signContext);
            return new EnvelopedSignedMessageHeader(samlToken,(com.sun.xml.ws.security.opt.crypto.dsig.Reference) ref, jhe,nsContext);
//        } catch (KeyException ex) {
//            ex.printStackTrace();
//            throw new WSTrustException("Unable to create sign SAML Assertion",ex);
//        }
        } catch (NoSuchAlgorithmException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0035_UNABLE_CREATE_SIGN_SAML_ASSERTION(ex));
            throw new WSTrustException("Unable to create sign SAML Assertion",ex);
        } catch (InvalidAlgorithmParameterException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0035_UNABLE_CREATE_SIGN_SAML_ASSERTION(ex));
            throw new WSTrustException("Unable to create sign SAML Assertion",ex);
        }
    }
    
    private class DSigResolver implements URIDereferencer{
        public Data data = null;
        DSigResolver(Data data){
            this.data = data;
        }
        public Data dereference(URIReference uRIReference, XMLCryptoContext xMLCryptoContext) throws URIReferenceException {
            return data;
        }
        
    }
}
