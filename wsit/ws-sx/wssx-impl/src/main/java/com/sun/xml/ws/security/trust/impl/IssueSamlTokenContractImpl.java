/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.security.trust.impl;

import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.NameID;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;


import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.trust.elements.BinarySecret;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.saml.Advice;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.Attribute;
import com.sun.xml.wss.saml.AttributeStatement;
import com.sun.xml.wss.saml.AudienceRestriction;
import com.sun.xml.wss.saml.AudienceRestrictionCondition;
import com.sun.xml.wss.saml.AuthenticationStatement;
import com.sun.xml.wss.saml.AuthnContext;
import com.sun.xml.wss.saml.AuthnStatement;
import com.sun.xml.wss.saml.Conditions;
import com.sun.xml.wss.saml.NameIdentifier;
import com.sun.xml.wss.saml.SAMLAssertionFactory;
import com.sun.xml.wss.saml.SAMLException;
import com.sun.xml.wss.saml.SubjectConfirmation;
import com.sun.xml.wss.saml.KeyInfoConfirmationData;

import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.misc.Base64;

import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;
import java.util.TimeZone;

public  class IssueSamlTokenContractImpl extends IssueSamlTokenContract {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    public Token createSAMLAssertion(final String appliesTo, final String tokenType, final String keyType, final String assertionId, final String issuer, final  Map<QName, List<String>> claimedAttrs, final IssuedTokenContext context) throws WSTrustException {
        Token token = null;
        
        // Get the service certificate
        TrustSPMetadata spMd = stsConfig.getTrustSPMetadata(appliesTo);
        if (spMd == null){
            spMd = stsConfig.getTrustSPMetadata("default");
        }
        X509Certificate serCert = (X509Certificate)context.getOtherProperties().get(IssuedTokenContext.STS_CERTIFICATE);
        if (serCert == null){
            serCert = getServiceCertificate(spMd, appliesTo);
        }
            
        // Create the KeyInfo for SubjectConfirmation
        final KeyInfo keyInfo = createKeyInfo(keyType, serCert, context, appliesTo);
            
        // Create SAML assertion
        Assertion assertion = null;
        if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
            WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
            assertion = createSAML11Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs, keyType);
        } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(tokenType)){
            assertion = createSAML20Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs, keyType);
        } else{
            log.log(Level.SEVERE, LogStringsMessages.WST_0031_UNSUPPORTED_TOKEN_TYPE(tokenType, appliesTo));
            throw new WSTrustException(LogStringsMessages.WST_0031_UNSUPPORTED_TOKEN_TYPE(tokenType, appliesTo));
        }
            
        // Get the STS's certificate and private key
        Object[] stsCertsAndPrikey = getSTSCertAndPrivateKey();
        final X509Certificate stsCert = (X509Certificate)stsCertsAndPrikey[0];
        final PrivateKey stsPrivKey = (PrivateKey)stsCertsAndPrikey[1];
            
        // Sign the assertion with STS's private key
        Element signedAssertion = null;
        try{            
            signedAssertion = assertion.sign(stsCert, stsPrivKey, true, context.getSignatureAlgorithm(), context.getCanonicalizationAlgorithm());            
            //signedAssertion = assertion.sign(stsCert, stsPrivKey, true);            
            //signedAssertion = assertion.sign(stsCert, stsPrivKey);
        }catch (SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }
            
        //javax.xml.bind.Unmarshaller u = eleFac.getContext().createUnmarshaller();
        //JAXBElement<AssertionType> aType = u.unmarshal(signedAssertion, AssertionType.class);
        //assertion =  new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion(aType.getValue());
        //token = new GenericToken(signedAssertion);
            
        if (stsConfig.getEncryptIssuedToken()){
            String keyWrapAlgo = (String) context.getOtherProperties().get(IssuedTokenContext.KEY_WRAP_ALGORITHM);
            Element encData = encryptToken(signedAssertion, serCert, appliesTo, context.getEncryptionAlgorithm(), keyWrapAlgo);
            token = new GenericToken(encData);
                //JAXBElement<EncryptedDataType> eEle = u.unmarshal(cipher.martial(encData), EncryptedDataType.class);
                //return eEle.getValue();
        }else{
                token = new GenericToken(signedAssertion);
        }

        return token;
    }
    
    private EncryptedKey encryptKey(final Document doc, final byte[] encryptedKey, final X509Certificate cert, final String appliesTo, final String keyWrapAlgorithm) throws WSTrustException{
        EncryptedKey encKey = null;
        try{
            final PublicKey pubKey = cert.getPublicKey();
            final XMLCipher cipher;
            if(keyWrapAlgorithm != null){
                cipher = XMLCipher.getInstance(keyWrapAlgorithm);
            }else{
                cipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
            }
            cipher.init(XMLCipher.WRAP_MODE, pubKey);

            encKey = cipher.encryptKey(doc, new SecretKeySpec(encryptedKey, "AES"));
            final KeyInfo keyinfo = new KeyInfo(doc);
            //KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.ThumbPrintIdentifier_NS,null);
            //keyIdentifier.setValue(Base64.encode(X509ThumbPrintIdentifier.getThumbPrintIdentifier(serCert)));
            byte[] skid = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert);
            if (skid != null && skid.length > 0){
                final KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.X509SubjectKeyIdentifier_NS,null);
                keyIdentifier.setValue(Base64.encode(skid));
                final SecurityTokenReference str = new SecurityTokenReferenceImpl(keyIdentifier);
                keyinfo.addUnknownElement((Element)doc.importNode(WSTrustElementFactory.newInstance().toElement(str,null), true));
            }else{
                final X509Data x509data = new X509Data(doc);
                x509data.addCertificate(cert);
                keyinfo.add(x509data);
            }
            encKey.setKeyInfo(keyinfo);
        } catch (XWSSecurityException ex){
            log.log(Level.SEVERE,
                            LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
            throw new WSTrustException( LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
        } catch (XMLEncryptionException ex) {
            log.log(Level.SEVERE,
                            LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
            throw new WSTrustException( LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
        }catch(com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException ex){
            log.log(Level.SEVERE,
                            LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
            throw new WSTrustException( LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
        }
        
        return encKey;
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
            final EncryptedKey encKey = encryptKey(owner, skey, serCert, appliesTo, keyWrapAlgorithm);
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
    
    private KeyInfo createKeyInfo(final String keyType, final X509Certificate serCert, final IssuedTokenContext ctx, String appliesTo)throws WSTrustException{
        Element kiEle = (Element)stsConfig.getOtherOptions().get("ConfirmationKeyInfo");
        if (kiEle != null){
            try{
                return new KeyInfo(kiEle, null);
            }catch(com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException ex){
                log.log(Level.SEVERE, LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT(), ex);
                throw new WSTrustException(LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT(), ex);
            }
        }
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        Document doc = null;
        try{
            doc = docFactory.newDocumentBuilder().newDocument();
        }catch(ParserConfigurationException ex){
            log.log(Level.SEVERE, 
                    LogStringsMessages.WST_0039_ERROR_CREATING_DOCFACTORY(), ex);
            throw new WSTrustException(LogStringsMessages.WST_0039_ERROR_CREATING_DOCFACTORY(), ex);
        }
        
        final KeyInfo keyInfo = new KeyInfo(doc);
        if (wstVer.getSymmetricKeyTypeURI().equals(keyType)){
            final byte[] key = ctx.getProofKey();
            if (stsConfig.getEncryptIssuedKey()){
                final EncryptedKey encKey = encryptKey(doc, key, serCert, appliesTo, null);
                try{
                    keyInfo.add(encKey);
                } catch (XMLEncryptionException ex) {
                    log.log(Level.SEVERE,
                            LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
                    throw new WSTrustException(LogStringsMessages.WST_0040_ERROR_ENCRYPT_PROOFKEY(appliesTo), ex);
                }
            }else{
                final BinarySecret secret = eleFac.createBinarySecret(key, wstVer.getSymmetricKeyTypeURI());
                final Element bsEle= eleFac.toElement(secret,doc);
                keyInfo.addUnknownElement(bsEle);
            }
        }else if(wstVer.getPublicKeyTypeURI().equals(keyType)){
            final X509Data x509data = new X509Data(doc);
            try{
                x509data.addCertificate(ctx.getRequestorCertificate());
            }catch(com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException ex){
                log.log(Level.SEVERE, LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT(), ex);
                throw new WSTrustException(LogStringsMessages.WST_0034_UNABLE_GET_CLIENT_CERT(), ex);
            }
            keyInfo.add(x509data);
        }
        
        return keyInfo;
    }
    
    protected Assertion createSAML11Assertion(final String assertionId, final String issuer, final String appliesTo, final KeyInfo keyInfo, final Map<QName, List<String>> claimedAttrs, String keyType) throws WSTrustException{
        Assertion assertion = null;
        try{
                final SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
            
            final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
            final GregorianCalendar issuerInst = new GregorianCalendar(utcTimeZone);
            final GregorianCalendar notOnOrAfter = new GregorianCalendar(utcTimeZone);
            notOnOrAfter.add(Calendar.MILLISECOND, (int)stsConfig.getIssuedTokenTimeout());
            
            List<AudienceRestrictionCondition> arc = null;
            if (appliesTo != null){
                arc = new ArrayList<AudienceRestrictionCondition>();
                List<String> au = new ArrayList<String>();
                au.add(appliesTo);
                arc.add(samlFac.createAudienceRestrictionCondition(au));
            }
            final List<String> confirmMethods = new ArrayList<String>();
            String confirMethod = (String)stsConfig.getOtherOptions().get(WSTrustConstants.SAML_CONFIRMATION_METHOD);
            if (confirMethod == null){
                if (keyType.equals(wstVer.getBearerKeyTypeURI())){
                     confirMethod = SAML_BEARER_1_0;
                }else{
                    confirMethod = SAML_HOLDER_OF_KEY_1_0;
                }
            }
            
            Element keyInfoEle = null;
            if (keyInfo != null && !wstVer.getBearerKeyTypeURI().equals(keyType)){
                keyInfoEle = keyInfo.getElement();
            }
            confirmMethods.add(confirMethod);
            
            final SubjectConfirmation subjectConfirm = samlFac.createSubjectConfirmation(
                    confirmMethods, null, keyInfoEle);
            final Conditions conditions =
                    samlFac.createConditions(issuerInst, notOnOrAfter, null, arc, null);
            final Advice advice = samlFac.createAdvice(null, null, null);
            
            com.sun.xml.wss.saml.Subject subj = null;
            //final List<Attribute> attrs = new ArrayList<Attribute>();
            QName idName = null;
            final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
            for(Map.Entry<QName, List<String>> entry : entries){
                final QName attrKey = entry.getKey();
                final List<String> values = entry.getValue();
                if (values != null && values.size() > 0){
                    if (STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart()) && subj == null){
                        final NameIdentifier nameId = samlFac.createNameIdentifier(values.get(0), attrKey.getNamespaceURI(), null);
                        subj = samlFac.createSubject(nameId, subjectConfirm);
                        idName = attrKey;
                    }//else{
                       // final Attribute attr = samlFac.createAttribute(attrKey.getLocalPart(), attrKey.getNamespaceURI(), values);
                        //attrs.add(attr);
                    //}
                }
            }
            
            if (idName != null){
                claimedAttrs.remove(idName);
            }
            
            final List<Object> statements = new ArrayList<Object>();
           //if (attrs.isEmpty()){
            if (claimedAttrs.isEmpty()){
                final AuthenticationStatement statement = samlFac.createAuthenticationStatement(null, issuerInst, subj, null, null);
                statements.add(statement); 
            }else{
                final AttributeStatement statement = samlFac.createAttributeStatement(subj, null);
                statements.add(statement);
            }
            assertion =
                    samlFac.createAssertion(assertionId, issuer, issuerInst, conditions, advice, statements);
            if (!claimedAttrs.isEmpty()){
                return WSTrustUtil.addSamlAttributes(assertion, claimedAttrs);
            }
        }catch(SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }catch(XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }
        
        return assertion;
    }
    
    protected Assertion createSAML20Assertion(final String assertionId, final String issuer, final String appliesTo, final KeyInfo keyInfo, final  Map<QName, List<String>> claimedAttrs, String keyType) throws WSTrustException{
        Assertion assertion = null;
        try{
            final SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
            
            // Create Conditions
            final TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
            final GregorianCalendar issueInst = new GregorianCalendar(utcTimeZone);
            final GregorianCalendar notOnOrAfter = new GregorianCalendar(utcTimeZone);
            notOnOrAfter.add(Calendar.MILLISECOND, (int)stsConfig.getIssuedTokenTimeout());
            
            List<AudienceRestriction> arc = null;
            if (appliesTo != null){
                arc = new ArrayList<AudienceRestriction>();
                List<String> au = new ArrayList<String>();
                au.add(appliesTo);
                arc.add(samlFac.createAudienceRestriction(au));
            }

            KeyInfoConfirmationData keyInfoConfData = null;
            String confirMethod = (String)stsConfig.getOtherOptions().get(WSTrustConstants.SAML_CONFIRMATION_METHOD);
            if (confirMethod == null){
                if (keyType.equals(wstVer.getBearerKeyTypeURI())){
                     confirMethod = SAML_BEARER_2_0;
                }else{
                    confirMethod = SAML_HOLDER_OF_KEY_2_0;
                    if (keyInfo != null){
                        keyInfoConfData = samlFac.createKeyInfoConfirmationData(keyInfo.getElement());
                    }
                }
            }
            
            final Conditions conditions = samlFac.createConditions(issueInst, notOnOrAfter, null, arc, null, null);
            
            // Create Subject
            
            // SubjectConfirmationData subjComfData = samlFac.createSubjectConfirmationData(
            // null, null, null, null, appliesTo, keyInfo.getElement());
           
            
            final SubjectConfirmation subjectConfirm = samlFac.createSubjectConfirmation(
                    null, keyInfoConfData, confirMethod);
            
            com.sun.xml.wss.saml.Subject subj = null;
            //final List<Attribute> attrs = new ArrayList<Attribute>();
            QName idName = null;
            final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
            for(Map.Entry<QName, List<String>> entry : entries){
                final QName attrKey = entry.getKey();
                final List<String> values = entry.getValue();
                if (values != null && values.size() > 0){
                    if (STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart()) && subj == null){
                        final NameID nameId = samlFac.createNameID(values.get(0), attrKey.getNamespaceURI(), null);
                        subj = samlFac.createSubject(nameId, subjectConfirm);
                        idName = attrKey;
                    }
                    //else{
                      //  final Attribute attr = samlFac.createAttribute(attrKey.getLocalPart(), attrKey.getNamespaceURI(), values);
                      //  attrs.add(attr);
                    //}
                }
            }
            
            if (idName != null){
                claimedAttrs.remove(idName);
            }
        
            final List<Object> statements = new ArrayList<Object>();
            //if (attrs.isEmpty()){
            if (claimedAttrs.isEmpty()){
                AuthnContext ctx = samlFac.createAuthnContext(this.authnCtxClass, null);
                final AuthnStatement statement = samlFac.createAuthnStatement(issueInst, null, ctx, null, null);
                statements.add(statement); 
            }else{
                final AttributeStatement statement = samlFac.createAttributeStatement(null);
                statements.add(statement);
            }
            
            final NameID issuerID = samlFac.createNameID(issuer, null, null);
            
            // Create Assertion
            assertion =
                    samlFac.createAssertion(assertionId, issuerID, issueInst, conditions, null, null, statements);
            if (!claimedAttrs.isEmpty()){
                assertion = WSTrustUtil.addSamlAttributes(assertion, claimedAttrs);
            }
            ((com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion)assertion).setSubject((com.sun.xml.wss.saml.internal.saml20.jaxb20.SubjectType)subj);
            //return assertion;
        }catch(SAMLException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }catch(XWSSecurityException ex){
            log.log(Level.SEVERE,
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
            throw new WSTrustException(
                    LogStringsMessages.WST_0032_ERROR_CREATING_SAML_ASSERTION(), ex);
        }
        
        return assertion;
    }
}
