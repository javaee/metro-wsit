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


import com.sun.xml.ws.api.security.trust.STSAttributeProvider;

import com.sun.xml.security.core.dsig.ObjectFactory;
import com.sun.xml.security.core.xenc.CipherDataType;
import com.sun.xml.security.core.xenc.EncryptedDataType;
import com.sun.xml.security.core.xenc.EncryptionMethodType;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.opt.api.SecurityElement;
import com.sun.xml.ws.security.opt.api.SecurityHeaderElement;
import com.sun.xml.ws.security.opt.api.keyinfo.SecurityTokenReference;
import com.sun.xml.ws.security.opt.crypto.dsig.Signature;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.DSAKeyValue;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.KeyInfo;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.RSAKeyValue;
import com.sun.xml.ws.security.opt.crypto.dsig.keyinfo.X509Data;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignContext;
import com.sun.xml.ws.security.opt.crypto.jaxb.JAXBSignatureFactory;
import com.sun.xml.ws.security.opt.impl.crypto.SSEData;
import com.sun.xml.ws.security.opt.impl.dsig.EnvelopedSignedMessageHeader;
import com.sun.xml.ws.security.opt.impl.dsig.JAXBSignatureHeaderElement;
import com.sun.xml.ws.security.opt.impl.enc.JAXBEncryptedData;
import com.sun.xml.ws.security.opt.impl.keyinfo.SAMLToken;
import com.sun.xml.ws.security.opt.impl.reference.KeyIdentifier;
import com.sun.xml.ws.security.opt.impl.util.NamespaceContextEx;
import com.sun.xml.ws.security.opt.impl.util.WSSElementFactory;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.NameID;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.sun.xml.ws.security.opt.api.EncryptedKey;

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
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;


import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.trust.elements.BinarySecret;

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

import javax.security.auth.callback.UnsupportedCallbackException;


import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;



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
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    /** Creates a new instance of SBIssuedSamlTokenContractImpl */
    public SBIssuedSamlTokenContractImpl(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
    }
    
    public SBIssuedSamlTokenContractImpl() {
        //constructor
    }
    
    public Token createSAMLAssertion(final String appliesTo, final String tokenType, final String keyType, final String assertionId, final String issuer, final Map<QName, List<String>> claimedAttrs, final IssuedTokenContext context) throws WSTrustException {
        Token token = null;
        
        final CallbackHandler callbackHandler = stsConfig.getCallbackHandler();
        
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
            final X509Certificate serCert = getServiceCertificate(callbackHandler, stsConfig.getTrustSPMetadata(appliesTo), appliesTo);
            
            // Create the KeyInfo for SubjectConfirmation
            final KeyInfo keyInfo = createKeyInfo(keyType, serCert, context);
            
            // Create SAML assertion
            Assertion assertion = null;
            SAMLToken samlToken = null;
            if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
                    WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML11Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs);
                samlToken = new SAMLToken(assertion,SAMLJAXBUtil.getJAXBContext(),soapVersion);
                
            } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML20Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs);
                samlToken = new SAMLToken(assertion,SAMLJAXBUtil.getJAXBContext(),soapVersion);
            } else{
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0031_UNSUPPORTED_TOKEN_TYPE(tokenType, appliesTo));
                throw new WSTrustException(LogStringsMessages.WST_0031_UNSUPPORTED_TOKEN_TYPE(tokenType, appliesTo));
            }
            
            // Get the STS's public and private key
            final SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            final Callback skc = new SignatureKeyCallback(request);
            final Callback[] callbacks = {skc};
            callbackHandler.handle(callbacks);
            final PrivateKey stsPrivKey = request.getPrivateKey();
            
            // Sign the assertion with STS's private key
            //Element signedAssertion = assertion.sign(request.getX509Certificate(), stsPrivKey);
            final SecurityHeaderElement signedAssertion = createSignature(request.getX509Certificate().getPublicKey(),stsPrivKey,samlToken,nsContext);
            
            //javax.xml.bind.Unmarshaller u = eleFac.getContext().createUnmarshaller();
            //JAXBElement<AssertionType> aType = u.unmarshal(signedAssertion, AssertionType.class);
            //assertion =  new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion(aType.getValue());
            token = new GenericToken(signedAssertion);
            
            if (stsConfig.getEncryptIssuedToken()){
                final String id = "uuid-" + UUID.randomUUID().toString();
                final int keysizeInBytes = 32;
                final byte[] skey = WSTrustUtil.generateRandomSecret(keysizeInBytes);
                final Key key = new SecretKeySpec(skey, "AES");
                
                final KeyInfo encKeyInfo = new KeyInfo();
                final EncryptedKey encKey = encryptKey(key, serCert);
                encKeyInfo.getContent().add(encKey);
                final EncryptedDataType edt = createEncryptedData(id,MessageConstants.AES_BLOCK_ENCRYPTION_256,encKeyInfo,false);
                
                
                final JAXBEncryptedData jed = new JAXBEncryptedData(edt,new SSEData((SecurityElement)signedAssertion,false,nsContext),soapVersion);
                token = new GenericToken(jed);
            }else{
                token = new GenericToken(signedAssertion);
            }
        } catch (XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }catch (Exception ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }
        return token;
    }
    
    
    
    private Assertion createSAML11Assertion(final String assertionId, final String issuer, final String appliesTo, final KeyInfo keyInfo, final Map<QName, List<String>> claimedAttrs) throws WSTrustException{
        Assertion assertion = null;
        try{
            final SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
            
            final GregorianCalendar issuerInst = new GregorianCalendar();
            final GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)stsConfig.getIssuedTokenTimeout());
            
            final Conditions conditions =
                    samlFac.createConditions(issuerInst, notOnOrAfter, null, null, null);
            final Advice advice = samlFac.createAdvice(null, null, null);
            
            final List<String>  confirmMethods = new ArrayList<String>();
            confirmMethods.add(SAML_HOLDER_OF_KEY);
            
            final SubjectConfirmation subjectConfirm = samlFac.createSubjectConfirmation(confirmMethods,null, keyInfo);
            
            com.sun.xml.wss.saml.Subject subj = null;
            final List<Attribute> attrs = new ArrayList<Attribute>();
            final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
            for(Map.Entry<QName, List<String>> entry : entries){
                final QName attrKey = (QName)entry.getKey();
                final List<String> values = (List<String>)entry.getValue();
                if (values != null && values.size() > 0){
                    if (STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart()) && subj == null){
                        final NameIdentifier nameId = samlFac.createNameIdentifier(values.get(0), attrKey.getNamespaceURI(), null);
                        subj = samlFac.createSubject(nameId, subjectConfirm);
                    }
                    else{
                        final Attribute attr = samlFac.createAttribute(attrKey.getLocalPart(), attrKey.getNamespaceURI(), values);
                        attrs.add(attr);
                    }
                }
            }
            final AttributeStatement statement = samlFac.createAttributeStatement(subj, attrs);
            final List<AttributeStatement> statements = new ArrayList<AttributeStatement>();
            statements.add(statement);
            assertion =
                    samlFac.createAssertion(assertionId, issuer, issuerInst, conditions, advice, statements);
        }catch(SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }catch(XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }
        
        return assertion;
    }
    
    
    
    private Assertion createSAML20Assertion(final String assertionId, final String issuer, final String appliesTo, final KeyInfo keyInfo, final Map<QName, List<String>> claimedAttrs) throws WSTrustException{
        Assertion assertion = null;
        try{
            final SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
            
            // Create Conditions
            final GregorianCalendar issueInst = new GregorianCalendar();
            final GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)stsConfig.getIssuedTokenTimeout());
            
            final Conditions conditions = samlFac.createConditions(issueInst, notOnOrAfter, null, null, null, null);
            
            // Create Subject
            
            final SubjectConfirmationData subjComfData = samlFac.createSubjectConfirmationData(
                    null, null, issueInst, notOnOrAfter, appliesTo, keyInfo);
            
            final SubjectConfirmation subConfirmation = samlFac.createSubjectConfirmation(
                    null, subjComfData, SAML_HOLDER_OF_KEY);
            
            com.sun.xml.wss.saml.Subject subj = null;
            final List<Attribute> attrs = new ArrayList<Attribute>();
            final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
            for(Map.Entry<QName, List<String>> entry : entries){
                final QName attrKey = (QName)entry.getKey();
                final List<String> values = (List<String>)entry.getValue();
                if (values != null && values.size() > 0){
                    if (STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart()) && subj == null){
                        final NameIdentifier nameId = samlFac.createNameIdentifier(values.get(0), attrKey.getNamespaceURI(), null);
                        subj = samlFac.createSubject(nameId, subConfirmation);
                    }
                    else{
                        final Attribute attr = samlFac.createAttribute(attrKey.getLocalPart(), values);
                        attrs.add(attr);
                    }
                }
            }
            final AttributeStatement statement = samlFac.createAttributeStatement(attrs);
            final List<AttributeStatement> statements = new ArrayList<AttributeStatement>();
            statements.add(statement);
            
            final NameID issuerID = samlFac.createNameID(issuer, null, null);
            
            // Create Assertion
            assertion =
                    samlFac.createAssertion(assertionId, issuerID, issueInst, conditions, null, subj, statements);
        }catch(SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }catch(XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }
        
        return assertion;
    }
    
    private KeyInfo createKeyInfo(final String keyType, final X509Certificate serCert, final IssuedTokenContext ctx)throws WSTrustException{
        final KeyInfo keyInfo = new KeyInfo();
        if (WSTrustConstants.SYMMETRIC_KEY.equals(keyType)){
            final byte[] key = ctx.getProofKey();
            if (!stsConfig.getEncryptIssuedToken() && stsConfig.getEncryptIssuedKey()){
                try{
                    final Key secKey = new SecretKeySpec(key, "AES");
                    final EncryptedKey encKey = encryptKey(secKey, serCert);
                    keyInfo.getContent().add(encKey);
                }catch(Exception ex){
                    throw new WSTrustException(ex.getMessage(), ex);
                }
            }else{
                final BinarySecret secret = eleFac.createBinarySecret(key, wstVer.getSymmetricKeyTypeURI());
                keyInfo.getContent().add(secret);
            }
        }else if(WSTrustConstants.PUBLIC_KEY.equals(keyType)){
            
            final X509Data x509Data = new X509Data();
            final Set certs = ctx.getRequestorSubject().getPublicCredentials();
            if(certs == null){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
                throw new WSTrustException(LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
            }
            boolean addedClientCert = false;
            final ObjectFactory dsigOF = new ObjectFactory();
            for(Object o : certs){
                if(o instanceof X509Certificate){
                    final X509Certificate clientCert = (X509Certificate)o;
                    
                    JAXBElement<byte[]> certElement;
                    try {
                        certElement = dsigOF.createX509DataTypeX509Certificate(clientCert.getEncoded());
                    } catch (CertificateEncodingException ex) {
                        //ex.printStackTrace();
                        throw new WSTrustException("Unable to create KeyInfo",ex);
                    }
                    @SuppressWarnings("unchecked") final List<Object> x509DataContent = x509Data.getContent();
                    x509DataContent.add(certElement);
                    addedClientCert = true;
                    
                }
            }
            if(!addedClientCert){
                log.log(Level.SEVERE,
                        LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
                throw new WSTrustException(LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT());
            }
            keyInfo.getContent().add(x509Data);
        }
        
        return keyInfo;
    }
    
    private EncryptedKey encryptKey(final Key key, final X509Certificate cert) throws XWSSecurityException{
        KeyInfo keyInfo = null;
        final KeyIdentifier keyIdentifier = wef.createKeyIdentifier();
        keyIdentifier.setValueType(MessageConstants.X509SubjectKeyIdentifier_NS);
        keyIdentifier.updateReferenceValue(cert);
        keyIdentifier.setEncodingType(MessageConstants.BASE64_ENCODING_NS);
        final SecurityTokenReference str = wef.createSecurityTokenReference(keyIdentifier);
        keyInfo = wef.createKeyInfo((com.sun.xml.ws.security.opt.impl.keyinfo.SecurityTokenReference) str);
        
        return wef.createEncryptedKey(null,MessageConstants.RSA_OAEP_KEY_TRANSPORT,keyInfo,cert.getPublicKey(),key);
    }
    
    
    //common method to be moved to Base Class
    private X509Certificate getServiceCertificate(final CallbackHandler callbackHandler, TrustSPMetadata spMd, String appliesTo)throws WSTrustException{
        // Get the service certificate
        final EncryptionKeyCallback.AliasX509CertificateRequest req = new EncryptionKeyCallback.AliasX509CertificateRequest(spMd.getCertAlias());
        final EncryptionKeyCallback callback = new EncryptionKeyCallback(req);
        final Callback[] callbacks = {callback};
        try{
            callbackHandler.handle(callbacks);
        }catch(IOException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
            throw new WSTrustException(LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
        }catch(UnsupportedCallbackException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
            throw new WSTrustException(LogStringsMessages.WST_0033_UNABLE_GET_SERVICE_CERT(appliesTo), ex);
        }
        
        return req.getX509Certificate();
    }
    
    
    public EncryptedDataType createEncryptedData(final String id,final String dataEncAlgo,final KeyInfo keyInfo,final boolean contentOnly){
        final EncryptedDataType edt = new EncryptedDataType();
        if(contentOnly){
            edt.setType(MessageConstants.ENCRYPT_ELEMENT_CONTENT);
        }else{
            edt.setType(MessageConstants.ENCRYPT_ELEMENT);
        }
        final EncryptionMethodType emt = new EncryptionMethodType();
        emt.setAlgorithm(dataEncAlgo);
        edt.setEncryptionMethod(emt);
        final CipherDataType cipherType = new CipherDataType();
        cipherType.setCipherValue("ed".getBytes());
        edt.setCipherData(cipherType);
        edt.setId(id);
        if(keyInfo != null){
            edt.setKeyInfo(keyInfo);
        }
        return edt;
    }
    
    private SecurityHeaderElement createSignature(final PublicKey pubKey,final Key signingKey,final SAMLToken samlToken,final NamespaceContextEx nsContext)throws WSTrustException{
        try{
            final JAXBSignatureFactory signatureFactory = JAXBSignatureFactory.newInstance();
            final C14NMethodParameterSpec spec = null;
            final CanonicalizationMethod canonicalMethod =
                    signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,spec);
            DigestMethod digestMethod;
            digestMethod = signatureFactory.newDigestMethod(DigestMethod.SHA1, null);
            SignatureMethod signatureMethod;
            signatureMethod = signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
            
            //Note : Signature algorithm parameters null for now , fix me.
            
            final ArrayList<Transform> transformList = new ArrayList<Transform>();
            Transform tr1;
            
            tr1 = signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            
            Transform tr2;
            
            tr2 = signatureFactory.newTransform(CanonicalizationMethod.EXCLUSIVE, (TransformParameterSpec) null);
            
            transformList.add(tr1);
            transformList.add(tr2);
            
            final String uri = "#" + "uuid-" + UUID.randomUUID().toString();
            final Reference ref = signatureFactory.newReference(uri,digestMethod,transformList, null, null);
            
            // Create the SignedInfo
            final SignedInfo signedInfo = signatureFactory.newSignedInfo(canonicalMethod,signatureMethod,Collections.singletonList(ref));
            
            KeyValue keyValue;
            
            //kv = kif.newKeyValue(pubKey);
            if (pubKey instanceof java.security.interfaces.DSAPublicKey) {
                DSAKeyValue dsa = null;
                final DSAPublicKey key = (DSAPublicKey)pubKey;
                
                final byte[] paramP = key.getParams().getP().toByteArray();
                final byte[] paramQ = key.getParams().getQ().toByteArray();
                final byte[] paramG = key.getParams().getG().toByteArray();
                final byte[] paramY = key.getY().toByteArray();
                dsa = signatureFactory.newDSAKeyValue(paramP,paramQ,paramG,paramY,null,null,null);
                keyValue = signatureFactory.newKeyValue(Collections.singletonList(dsa));
                
            } else if (pubKey instanceof java.security.interfaces.RSAPublicKey) {
                RSAKeyValue rsa = null;
                final RSAPublicKey key = (RSAPublicKey)pubKey;
                rsa = signatureFactory.newRSAKeyValue(key.getModulus().toByteArray(),key.getPublicExponent().toByteArray());
                keyValue = signatureFactory.newKeyValue(Collections.singletonList(rsa));
            }else{
                throw new WSTrustException("Unsupported PublicKey");
            }
            
            // Create a KeyInfo and add the KeyValue to it
            final javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo = signatureFactory.newKeyInfo(Collections.singletonList(keyValue));
            final JAXBSignContext signContext = new JAXBSignContext(signingKey);
            
            final SSEData data = null;
            signContext.setURIDereferencer(new DSigResolver(data));
            final com.sun.xml.ws.security.opt.crypto.dsig.Signature signature = (Signature) signatureFactory.newXMLSignature(signedInfo,keyInfo);
            final JAXBSignatureHeaderElement jhe =  new JAXBSignatureHeaderElement(signature,soapVersion,(XMLSignContext)signContext);
            return new EnvelopedSignedMessageHeader(samlToken,(com.sun.xml.ws.security.opt.crypto.dsig.Reference) ref, jhe,nsContext);
//        } catch (KeyException ex) {
//            ex.printStackTrace();
//            throw new WSTrustException("Unable to create sign SAML Assertion",ex);
//        }
        } catch (NoSuchAlgorithmException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0035_UNABLE_CREATE_SIGN_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0035_UNABLE_CREATE_SIGN_SAML_ASSERTION(),ex);
        } catch (InvalidAlgorithmParameterException ex) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0035_UNABLE_CREATE_SIGN_SAML_ASSERTION(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0035_UNABLE_CREATE_SIGN_SAML_ASSERTION(),ex);
        }
    }
    
    private static class DSigResolver implements URIDereferencer{
        public Data data = null;
        DSigResolver(Data data){
            this.data = data;
        }
        public Data dereference(final URIReference uRIReference, final XMLCryptoContext xMLCryptoContext) throws URIReferenceException {
            return data;
        }
        
    }
}
