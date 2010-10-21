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

package wstrust.scenario5n4.sts;

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


import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.Token;
import com.sun.xml.ws.api.security.trust.STSAttributeProvider;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.trust.GenericToken;
import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.trust.util.WSTrustUtil;
import com.sun.xml.ws.security.trust.elements.BinarySecret;

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
import com.sun.xml.wss.saml.KeyInfoConfirmationData;

import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.misc.Base64;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.security.trust.logging.LogDomainConstants;
import com.sun.xml.ws.security.trust.logging.LogStringsMessages;

public  class IssueSamlTokenContractImpl extends MyIssueSamlTokenContract {
    
    private static final Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);
    
    private static final String SAML_HOLDER_OF_KEY = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    
    public Token createSAMLAssertion(final String appliesTo, final String tokenType, final String keyType, final String assertionId, final String issuer, final  Map<QName, List<String>> claimedAttrs, final IssuedTokenContext context) throws WSTrustException {
        Token token = null;
        
        final CallbackHandler callbackHandler = stsConfig.getCallbackHandler();
        
        try{
            // Get the service certificate
            final X509Certificate serCert = getServiceCertificate(callbackHandler, stsConfig.getTrustSPMetadata(appliesTo));
            
            // Create the KeyInfo for SubjectConfirmation
            final KeyInfo keyInfo = createKeyInfo(keyType, serCert, context);
            
            // Create SAML assertion
            Assertion assertion = null;
            if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
                    WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML11Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs);
            } else if (WSTrustConstants.SAML20_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML20Assertion(assertionId, issuer, appliesTo, keyInfo, claimedAttrs);
            } else{
           
            }
            
            // Get the STS's public and private key
            final SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            final Callback skc = new SignatureKeyCallback(request);
            final Callback[] callbacks = {skc};
            callbackHandler.handle(callbacks);
            final PrivateKey stsPrivKey = request.getPrivateKey();
            
            // Sign the assertion with STS's private key
            final Element signedAssertion = assertion.sign(request.getX509Certificate(), stsPrivKey);
            
            //javax.xml.bind.Unmarshaller u = eleFac.getContext().createUnmarshaller();
            //JAXBElement<AssertionType> aType = u.unmarshal(signedAssertion, AssertionType.class);
            //assertion =  new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion(aType.getValue());
            //token = new GenericToken(signedAssertion);
            
            if (stsConfig.getEncryptIssuedToken()){
                // Create the encryption key
                final XMLCipher cipher = XMLCipher.getInstance(XMLCipher.AES_128);
                final int keysizeInBytes = 32;
                final byte[] skey = WSTrustUtil.generateRandomSecret(keysizeInBytes);
                cipher.init(XMLCipher.ENCRYPT_MODE, new SecretKeySpec(skey, "AES"));
                
                // Encrypt the assertion and return the Encrypteddata
                final Document owner = signedAssertion.getOwnerDocument();
                final EncryptedData encData = cipher.encryptData(owner, signedAssertion);
                final String id = "uuid-" + UUID.randomUUID().toString();
                encData.setId(id);
                
                final KeyInfo encKeyInfo = new KeyInfo(owner);
                final EncryptedKey encKey = encryptKey(owner, skey, serCert);
                encKeyInfo.add(encKey);
                encData.setKeyInfo(encKeyInfo);
                
                token = new GenericToken(cipher.martial(encData));
                //JAXBElement<EncryptedDataType> eEle = u.unmarshal(cipher.martial(encData), EncryptedDataType.class);
                //return eEle.getValue();
            }else{
                token = new GenericToken(signedAssertion);
            }
        } catch (XWSSecurityException ex){
           
        }catch (XMLEncryptionException ex) {
           
        }catch (JAXBException ex) {
            
        }catch (Exception ex) {
            
        }
        return token;
    }
    
 /*  protected boolean isAuthorized(Subject subject, String appliesTo, String tokenType, String keyType){
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
   } */
    
    private EncryptedKey encryptKey(final Document doc, final byte[] encryptedKey, final X509Certificate cert) throws XMLEncryptionException, XWSSecurityException{
        final PublicKey pubKey = cert.getPublicKey();
        final XMLCipher cipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
        cipher.init(XMLCipher.WRAP_MODE, pubKey);
        
        final EncryptedKey encKey = cipher.encryptKey(doc, new SecretKeySpec(encryptedKey, "AES"));
        final KeyInfo keyinfo = new KeyInfo(doc);
        //KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.ThumbPrintIdentifier_NS,null);
        //keyIdentifier.setValue(Base64.encode(X509ThumbPrintIdentifier.getThumbPrintIdentifier(serCert)));
        final KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.X509SubjectKeyIdentifier_NS,null);
        keyIdentifier.setValue(Base64.encode(X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert)));
        final SecurityTokenReference str = new SecurityTokenReferenceImpl(keyIdentifier);
        keyinfo.addUnknownElement((Element)doc.importNode(WSTrustElementFactory.newInstance().toElement(str,null), true));
        encKey.setKeyInfo(keyinfo);
        
        return encKey;
    }
    
    private X509Certificate getServiceCertificate(final CallbackHandler callbackHandler, TrustSPMetadata spMd)throws WSTrustException{
        // Get the service certificate
        final EncryptionKeyCallback.AliasX509CertificateRequest req = new EncryptionKeyCallback.AliasX509CertificateRequest(spMd.getCertAlias());
        final EncryptionKeyCallback callback = new EncryptionKeyCallback(req);
        final Callback[] callbacks = {callback};
        try{
            callbackHandler.handle(callbacks);
        }catch(IOException ex){
                 
        }catch(UnsupportedCallbackException ex){
            
        }
        
        return req.getX509Certificate();
    }
    
    private KeyInfo createKeyInfo(final String keyType, final X509Certificate serCert, final IssuedTokenContext ctx)throws WSTrustException{
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        
        Document doc = null;
        try{
            doc = docFactory.newDocumentBuilder().newDocument();
        }catch(ParserConfigurationException ex){
           
        }
        
        final KeyInfo keyInfo = new KeyInfo(doc);
        if (WSTrustConstants.SYMMETRIC_KEY.equals(keyType)){
            final byte[] key = ctx.getProofKey();
            if (!stsConfig.getEncryptIssuedToken() && stsConfig.getEncryptIssuedKey()){
                try{
                    final EncryptedKey encKey = encryptKey(doc, key, serCert);
                    keyInfo.add(encKey);
                }catch(Exception ex){
                    
                }
            }else{
                final BinarySecret secret = eleFac.createBinarySecret(key, BinarySecret.SYMMETRIC_KEY_TYPE);
                final Element bsEle= eleFac.toElement(secret,doc);
                keyInfo.addUnknownElement(bsEle);
            }
        }else if(WSTrustConstants.PUBLIC_KEY.equals(keyType)){
            final X509Data x509data = new X509Data(doc);
            final Set certs = ctx.getRequestorSubject().getPublicCredentials();
            if(certs == null){
               
            }
            boolean addedClientCert = false;
            for(Object o : certs){
                if(o instanceof X509Certificate){
                    final X509Certificate clientCert = (X509Certificate)o;
                    try{
                        x509data.addCertificate(clientCert);
                        addedClientCert = true;
                    }catch(com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException ex){
                       
                    }
                }
            }
            if(!addedClientCert){
                
            }
            keyInfo.add(x509data);
        }
        
        return keyInfo;
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
            
            final List<String> confirmMethods = new ArrayList<String>();
            confirmMethods.add(SAML_HOLDER_OF_KEY);
            
            final SubjectConfirmation subjectConfirm = samlFac.createSubjectConfirmation(
                    confirmMethods, null, keyInfo.getElement());
            
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
            
        }catch(XWSSecurityException ex){
            
        }
        
        return assertion;
    }
    
    private Assertion createSAML20Assertion(final String assertionId, final String issuer, final String appliesTo, final KeyInfo keyInfo, final  Map<QName, List<String>> claimedAttrs) throws WSTrustException{
        Assertion assertion = null;
        try{
            final SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
            
            // Create Conditions
            final GregorianCalendar issueInst = new GregorianCalendar();
            final GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)stsConfig.getIssuedTokenTimeout());
            
            final Conditions conditions = samlFac.createConditions(issueInst, notOnOrAfter, null, null, null, null);
            
            // Create Subject
            
            // SubjectConfirmationData subjComfData = samlFac.createSubjectConfirmationData(
            // null, null, null, null, appliesTo, keyInfo.getElement());
            
            final KeyInfoConfirmationData keyInfoConfData = samlFac.createKeyInfoConfirmationData(keyInfo.getElement());
            
            final SubjectConfirmation subjectConfirm = samlFac.createSubjectConfirmation(
                    null, keyInfoConfData, SAML_HOLDER_OF_KEY);
            
            com.sun.xml.wss.saml.Subject subj = null;
            final List<Attribute> attrs = new ArrayList<Attribute>();
            final Set<Map.Entry<QName, List<String>>> entries = claimedAttrs.entrySet();
            for(Map.Entry<QName, List<String>> entry : entries){
                final QName attrKey = (QName)entry.getKey();
                final List<String> values = (List<String>)entry.getValue();
                if (values != null && values.size() > 0){
                    if (STSAttributeProvider.NAME_IDENTIFIER.equals(attrKey.getLocalPart()) && subj == null){
                        final NameID nameId = samlFac.createNameID(values.get(0), attrKey.getNamespaceURI(), null);
                        subj = samlFac.createSubject(nameId, subjectConfirm);
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
            
        }catch(XWSSecurityException ex){
           
        }
        
        return assertion;
    }
}
