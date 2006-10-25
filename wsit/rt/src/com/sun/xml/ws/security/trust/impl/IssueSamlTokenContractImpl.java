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

import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.trust.impl.elements.str.KeyIdentifierImpl;
import com.sun.xml.ws.security.trust.impl.elements.str.SecurityTokenReferenceImpl;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.saml.NameID;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap; 
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.transform.Source;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedData;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;

//import com.sun.xml.security.core.xenc.EncryptedDataType;
import com.sun.xml.wss.SubjectAccessor;

import com.sun.xml.ws.policy.impl.bindings.AppliesTo;
import com.sun.xml.ws.security.trust.elements.str.DirectReference;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.trust.elements.str.KeyIdentifier;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
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

public  class IssueSamlTokenContractImpl extends IssueSamlTokenContract {

    private static Logger log =
            Logger.getLogger(
            LogDomainConstants.TRUST_IMPL_DOMAIN,
            LogDomainConstants.TRUST_IMPL_DOMAIN_BUNDLE);

    private static final String SAML_HOLDER_OF_KEY = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    protected static final String PRINCIPAL = "principal";
   
    protected Token createSAMLAssertion(String tokenType, String keyType, String assertionId, String issuer, Map claimedAttrs, IssuedTokenContext context) throws WSTrustException
    {       
        Token token = null;
        
        CallbackHandler callbackHandler = config.getCallbackHandler();
        
        try{
            // Get the service certificate
            X509Certificate serCert = getServiceCertificate(callbackHandler);

            // Create the KeyInfo for SubjectConfirmation
            KeyInfo keyInfo = createKeyInfo(keyType, serCert, context);

            // Create SAML assertion
            Assertion assertion = null;
            if (WSTrustConstants.SAML10_ASSERTION_TOKEN_TYPE.equals(tokenType)||
                WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML11Assertion(assertionId, issuer, keyInfo, claimedAttrs);
            } else if (WSTrustConstants.SAML11_ASSERTION_TOKEN_TYPE.equals(tokenType)){
                assertion = createSAML20Assertion(assertionId, issuer, keyInfo, claimedAttrs); 
            } else{
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
            Element signedAssertion = assertion.sign(request.getX509Certificate(), stsPrivKey);
            
            //javax.xml.bind.Unmarshaller u = eleFac.getContext().createUnmarshaller();
            //JAXBElement<AssertionType> aType = u.unmarshal(signedAssertion, AssertionType.class);
            //assertion =  new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion(aType.getValue());
            token = new GenericToken(signedAssertion);
            
            if (config.getEncryptIssuedToken()){
                // Create the encryption key 
                XMLCipher cipher = XMLCipher.getInstance(XMLCipher.AES_256);
                int keysizeInBytes = 32;
                byte[] skey = WSTrustUtil.generateRandomSecret(keysizeInBytes);
                cipher.init(XMLCipher.ENCRYPT_MODE, new SecretKeySpec(skey, "AES"));
                
                // Encrypt the assertion and return the Encrypteddata
                Document owner = signedAssertion.getOwnerDocument();
                EncryptedData encData = cipher.encryptData(owner, signedAssertion);
                String id = "uuid-" + UUID.randomUUID().toString();
                encData.setId(id);
                
                KeyInfo encKeyInfo = new KeyInfo(owner);
                EncryptedKey encKey = encryptKey(owner, skey, serCert);
                encKeyInfo.add(encKey);
                encData.setKeyInfo(encKeyInfo);
                
                token = new GenericToken(cipher.martial(encData));
                //JAXBElement<EncryptedDataType> eEle = u.unmarshal(cipher.martial(encData), EncryptedDataType.class);
                //return eEle.getValue();
            }else{
                token = new GenericToken(signedAssertion);
            }  
        } catch (XWSSecurityException ex){
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        }catch (XMLEncryptionException ex) {
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        }catch (JAXBException ex) {
            throw new WSTrustException(ex.getMessage(), ex);
        }catch (Exception ex) {
            ex.printStackTrace();
            throw new WSTrustException(ex.getMessage(), ex);
        }
       
        return token;
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
   
   private EncryptedKey encryptKey (Document doc, byte[] encryptedKey, X509Certificate cert) throws XMLEncryptionException, XWSSecurityException{
       PublicKey pubKey = cert.getPublicKey();
       XMLCipher cipher = XMLCipher.getInstance(XMLCipher.RSA_OAEP);
       cipher.init(XMLCipher.WRAP_MODE, pubKey);
            
       EncryptedKey encKey = cipher.encryptKey(doc, new SecretKeySpec(encryptedKey, "AES"));
       KeyInfo keyinfo = new KeyInfo(doc);
       //KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.ThumbPrintIdentifier_NS,null);
       //keyIdentifier.setValue(Base64.encode(X509ThumbPrintIdentifier.getThumbPrintIdentifier(serCert)));
       KeyIdentifier keyIdentifier = new KeyIdentifierImpl(MessageConstants.X509SubjectKeyIdentifier_NS,null);
       keyIdentifier.setValue(Base64.encode(X509SubjectKeyIdentifier.getSubjectKeyIdentifier(cert)));
       SecurityTokenReference str = new SecurityTokenReferenceImpl(keyIdentifier);
       keyinfo.addUnknownElement((Element)doc.importNode(WSTrustElementFactory.newInstance().toElement(str,null), true));
       encKey.setKeyInfo(keyinfo);
       
       return encKey;
   }
   
   private X509Certificate getServiceCertificate(CallbackHandler callbackHandler)throws WSTrustException{    
        // Get the service certificate 
        EncryptionKeyCallback.AliasX509CertificateRequest req = new EncryptionKeyCallback.AliasX509CertificateRequest(config.getCertAlias());
        EncryptionKeyCallback ec = new EncryptionKeyCallback(req);
        Callback[] callbacks = {ec};
        try{
            callbackHandler.handle(callbacks);
        }catch(IOException ex){
            throw new WSTrustException("Unable to get the service certificate", ex);
        }catch(UnsupportedCallbackException ex){
            throw new WSTrustException("Unable to get the service certificate", ex);
        }
            
        return req.getX509Certificate();
   }
   
   private KeyInfo createKeyInfo(String keyType, X509Certificate serCert, IssuedTokenContext ctx)throws WSTrustException{
       DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
       
       Document doc = null;
       try{
            doc = docFactory.newDocumentBuilder().newDocument();
       }catch(ParserConfigurationException ex){
           throw new WSTrustException(ex.getMessage(), ex);
       }
            
       KeyInfo keyInfo = new KeyInfo(doc);
       if (WSTrustConstants.SYMMETRIC_KEY.equals(keyType)){
            byte[] key = ctx.getProofKey();
            if (!config.getEncryptIssuedToken() && config.getEncryptIssuedKey()){
                try{
                    EncryptedKey encKey = encryptKey(doc, key, serCert);
                    keyInfo.add(encKey);
                }catch(Exception ex){
                    throw new WSTrustException(ex.getMessage(), ex);
                }
            }else{
                BinarySecret bs = eleFac.createBinarySecret(key, BinarySecret.SYMMETRIC_KEY_TYPE);
                keyInfo.addUnknownElement(eleFac.toElement(bs,doc));
            }
       }else if(WSTrustConstants.PUBLIC_KEY.equals(keyType)){
            X509Data x509data = new X509Data(doc);
            Set certs = ctx.getRequestorSubject().getPublicCredentials();
            if(certs == null){
                throw new WSTrustException("Unable to obtain client certificate");
            }
            boolean addedClientCert = false;
            for(Object o : certs){
                if(o instanceof X509Certificate){
                    X509Certificate clientCert = (X509Certificate)o;
                    try{
                        x509data.addCertificate(clientCert);
                        addedClientCert = true;
                    }catch(com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException ex){
                        throw new WSTrustException(ex.getMessage(), ex);
                    }
                }
            }
            if(!addedClientCert){
                throw new WSTrustException("Unable to obtain client certificate");
            }
            keyInfo.add(x509data);
       }
       
       return keyInfo;
   }
   
   private Assertion createSAML11Assertion(String assertionId, String issuer, KeyInfo keyInfo, Map claimedAttrs) throws WSTrustException{
        Assertion assertion = null;
        try{
            SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
      
            GregorianCalendar issuerInst = new GregorianCalendar(); 
            GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)config.getIssuedTokenTimeout());

            Conditions conditions = 
                 samlFac.createConditions(issuerInst, notOnOrAfter, null, null, null);
            Advice advice = samlFac.createAdvice(null, null, null);

            List<String> confirmationMethods = new ArrayList<String>();
            confirmationMethods.add(SAML_HOLDER_OF_KEY);

            SubjectConfirmation subjectConfirmation = samlFac.createSubjectConfirmation(
                  confirmationMethods, null, keyInfo.getElement());

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
            throw new WSTrustException("Unable to create SAML assertion", ex);
        }catch(XWSSecurityException ex){
            throw new WSTrustException("Unable to create the SAML assertion", ex);
        }
            
        return assertion;
   }
   
   private Assertion createSAML20Assertion(String assertionId, String issuer, KeyInfo keyInfo, Map claimedAttrs) throws WSTrustException{
       Assertion assertion = null;
       try{ 
            SAMLAssertionFactory samlFac = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
       
            // Create Conditions
            GregorianCalendar issuerInst = new GregorianCalendar(); 
            GregorianCalendar notOnOrAfter = new GregorianCalendar();
            notOnOrAfter.add(Calendar.MILLISECOND, (int)config.getIssuedTokenTimeout());

            Conditions conditions = 
                     samlFac.createConditions(issuerInst, notOnOrAfter, null, null, null);

            // Create Subject
            List<String> confirmationMethods = new ArrayList<String>();
            confirmationMethods.add(SAML_HOLDER_OF_KEY);

            SubjectConfirmation subjectConfirmation = samlFac.createSubjectConfirmation(
                confirmationMethods, null, keyInfo.getElement());

            com.sun.xml.wss.saml.Subject subj = null;
            QName principal = (QName)claimedAttrs.get(PRINCIPAL);
            if (principal != null){
                NameIdentifier nameId = samlFac.createNameIdentifier(principal.getLocalPart(), null, null);
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
                       samlFac.createAssertion(assertionId, issuerID, issuerInst, conditions, null, subj, statements);
        }catch(SAMLException ex){
            throw new WSTrustException("Unable to create SAML assertion", ex);
        }catch(XWSSecurityException ex){
            throw new WSTrustException("Unable to create the SAML assertion", ex);
        }
            
        return assertion;
   }
}
