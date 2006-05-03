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

/*
 * AssertionUtil.java
 *
 * Created on August 18, 2005, 6:40 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.saml;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.sun.org.apache.xml.internal.security.keys.KeyInfo;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;
import com.sun.org.apache.xml.internal.security.utils.EncryptionConstants;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AssertionType;
import java.math.BigInteger;
import java.security.Key;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.bind.JAXBElement;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import com.sun.xml.wss.core.ReferenceElement;
import com.sun.xml.wss.core.SecurityTokenReference;
import com.sun.xml.wss.core.KeyInfoHeaderBlock;
import com.sun.xml.wss.core.reference.KeyIdentifier;
import com.sun.xml.wss.core.reference.DirectReference;
import com.sun.xml.wss.core.reference.X509IssuerSerial;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.impl.callback.*;
import com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AssertionImpl;
import javax.xml.bind.JAXBContext;

/**
 *
 * @author abhijit.das@Sun.COM
 */

public class AssertionUtil {
    
    private static Logger log = Logger.getLogger(LogDomainConstants.WSS_API_DOMAIN, LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    /** Creates a new instance of AssertionUtil */
    private AssertionUtil(CallbackHandler callbackHandler) {
        //do nothing
    }
    
    /**
     * Retrive the key from HOK SAML Assertion
     *
     * @param assertion An <code>org.w3c.dom.Element</code> representation of SAML Assertion
     *
     * @param callbackHandler A <code>javax.security.auth.callback.CallbackHandler</code> object used to retrive the key
     *
     * @return java.security.Key
     * @throws XWSSecurityException
     *
     */
    public static Key getSubjectConfirmationKey(Element assertion , CallbackHandler callbackHandler) throws XWSSecurityException {
        
        NodeList nl1 = assertion.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "SubjectConfirmation");
        
        if ( nl1.getLength() == 0) {
            throw new XWSSecurityException("SAML Assertion does not contain a key");
        }
        
        NodeList nl = ((Element)(nl1.item(0))).getElementsByTagNameNS(MessageConstants.DSIG_NS, "KeyInfo");
        
        if ( nl.getLength() == 0) {
            throw new XWSSecurityException("SAML Assertion does not contain a key");
        }
        
        try {
            Element keyInfoElem = (Element)nl.item(0);
            
            KeyInfo keyInfo = new KeyInfo(keyInfoElem, null);
            
            if (keyInfo.containsKeyValue()) {
                return keyInfo.itemKeyValue(0).getPublicKey();
            } else if (keyInfo.containsX509Data()) {
                return resolveX509Data(keyInfo.itemX509Data(0), callbackHandler);
            } else if(keyInfo.length(EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_ENCRYPTEDKEY) > 0){
                return resolveEncryptedKey(keyInfo.itemEncryptedKey(0), callbackHandler);
            }
            else {
                throw new XWSSecurityException("Unsupported Key Information");
            }
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }
    
    public static Key resolveX509Data(X509Data x509Data, CallbackHandler callbackHandler) throws XWSSecurityException {
        x509Data.getElement().normalize();
        X509Certificate cert =  null;
        
        try {
            if (x509Data.containsCertificate()) {
                cert = (x509Data.itemCertificate(0)).getX509Certificate();
            } else if (x509Data.containsSKI()) {
                byte[] keyIdentifier = x509Data.itemSKI(0).getSKIBytes();
                SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                        new SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest(keyIdentifier);
                SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
                
                Callback[] callbacks = new Callback[] {verifyKeyCallback};
                try {
                    callbackHandler.handle(callbacks);
                } catch (Exception e) {
                    throw new XWSSecurityException(e);
                }
                
                cert = certRequest.getX509Certificate();
                
                if (cert == null) {
                    throw new XWSSecurityException("No Matching public key for " + Base64.encode(keyIdentifier) + " subject key identifier found");
                }
            } else if (x509Data.containsIssuerSerial()) {
                
                String issuerName = x509Data.itemIssuerSerial(0).getIssuerName();
                BigInteger serialNumber = x509Data.itemIssuerSerial(0).getSerialNumber();
                
                SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                        new SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest(
                        issuerName,
                        serialNumber);
                SignatureVerificationKeyCallback verifyKeyCallback =
                        new SignatureVerificationKeyCallback(certRequest);
                
                Callback[] callbacks = new Callback[] {verifyKeyCallback};
                
                try {
                    callbackHandler.handle(callbacks);
                } catch (Exception e) {
                    throw new XWSSecurityException(e);
                }
                
                cert = certRequest.getX509Certificate();
                
                if (cert == null) {
                    throw new XWSSecurityException(
                            "No Matching public key for serial number " + serialNumber + " and issuer name " + issuerName + " found");
                }
            } else {
                throw new XWSSecurityException(
                        "Unsupported child element of X509Data encountered");
            }
            
            return cert.getPublicKey();
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }
    
    public static Key resolveEncryptedKey(EncryptedKey encryptedKey, CallbackHandler callbackHandler) throws XWSSecurityException {
        
        //Get the private key to decrypt the encrypted key
        KeyInfo keyInfo = encryptedKey.getKeyInfo();
        KeyInfoHeaderBlock keyInfoHb = new KeyInfoHeaderBlock(keyInfo);
        Key kek = null;
        try{
            if (keyInfoHb.containsSecurityTokenReference()){
                kek = processSecurityTokenReference(keyInfoHb, callbackHandler);
            } else if (keyInfoHb.containsKeyValue()) {
               
                SecurityEnvironment secEnv = new DefaultSecurityEnvironmentImpl(callbackHandler);
                KeyValue keyValue = keyInfoHb.getKeyValue(0);
                keyValue.getElement().normalize();
                kek = secEnv.getPrivateKey(null, keyValue.getPublicKey(), false);
                
            } else if (keyInfoHb.containsX509Data()) {
                kek = processX509Data(keyInfoHb, callbackHandler);                
            } 
            else{
                throw new XWSSecurityException("Unsupported Key Information");
            }
        // Decrypt the encrypted secret key and return
            String algorithmURI = encryptedKey.getEncryptionMethod().getAlgorithm();
            XMLCipher xmlCipher = XMLCipher.getInstance();
            xmlCipher.init(XMLCipher.UNWRAP_MODE, null);
            xmlCipher.setKEK(kek);
        
            return xmlCipher.decryptKey(encryptedKey, algorithmURI); 
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        } 
        
    }
    
    private static Key processSecurityTokenReference(KeyInfoHeaderBlock keyInfo, CallbackHandler callbackHandler)throws XWSSecurityException {
        Key returnKey = null;
        
        SecurityEnvironment secEnv = new DefaultSecurityEnvironmentImpl(callbackHandler);
        
        SecurityTokenReference str = keyInfo.getSecurityTokenReference(0);
        ReferenceElement refElement = str.getReference();
        
        if (refElement instanceof KeyIdentifier) {
            KeyIdentifier keyId = (KeyIdentifier)refElement;
            byte[] decodedValue = keyId.getDecodedReferenceValue().getBytes();
            if (MessageConstants.X509SubjectKeyIdentifier_NS.equals(keyId.getValueType()) ||
                    MessageConstants.X509v3SubjectKeyIdentifier_NS.equals(keyId.getValueType())) {
                returnKey = secEnv.getPrivateKey(null, decodedValue);
            }  else if(MessageConstants.ThumbPrintIdentifier_NS.equals(keyId.getValueType())){
                returnKey = secEnv.getPrivateKey(null, decodedValue, MessageConstants.THUMB_PRINT_TYPE);
            }
            
        } /*else if(refElement instanceof DirectReference){
            String uri = ((DirectReference) refElement).getURI();
            
            
        }*/ else if (refElement instanceof X509IssuerSerial) {
            BigInteger serialNumber = ((X509IssuerSerial) refElement).getSerialNumber();
            String issuerName = ((X509IssuerSerial) refElement).getIssuerName();
            
            returnKey = secEnv.getPrivateKey(null, serialNumber, issuerName);
        }else {
            log.log(
                    Level.SEVERE, "WSS0338.unsupported.reference.mechanism");     
            throw new XWSSecurityException(
                    "Key reference mechanism not supported");
        }
        return returnKey;
    }
    
    private static Key processX509Data(KeyInfoHeaderBlock keyInfo, CallbackHandler callbackHandler)throws XWSSecurityException {
        SecurityEnvironment secEnv = new DefaultSecurityEnvironmentImpl(callbackHandler);
        X509Data x509Data = keyInfo.getX509Data(0);
        X509Certificate cert =  null;
        try {
               if (x509Data.containsCertificate()) {
                    cert = (x509Data.itemCertificate(0)).getX509Certificate();
               } else if (x509Data.containsSKI()) {
                    return secEnv.getPrivateKey(null, x509Data.itemSKI(0).getSKIBytes());
                } else if (x509Data.containsIssuerSerial()) {
                    return secEnv.getPrivateKey(null, 
                           x509Data.itemIssuerSerial(0).getSerialNumber(),
                           x509Data.itemIssuerSerial(0).getIssuerName());
                } else {
                    log.log(Level.SEVERE, "WSS0339.unsupported.keyinfo");
                    throw new XWSSecurityException(
                            "Unsupported child element of X509Data encountered");
                }
                return secEnv.getPrivateKey(null, cert);
                
            } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0602.illegal.x509.data", e.getMessage());
            throw new XWSSecurityException(e);
        }
    }
    
    public static Assertion fromElement(org.w3c.dom.Element element)
    throws SAMLException {
        try {
            if ( System.getProperty("com.sun.xml.wss.saml.binding.jaxb") != null ) {
                JAXBContext jc =
                        JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb10");
                javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
                return new com.sun.xml.wss.saml.assertion.saml11.jaxb10.Assertion(
                        (com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AssertionImpl)u.unmarshal(element));
            } else {
                JAXBContext jc =
                        JAXBContext.newInstance("com.sun.xml.wss.saml.internal.saml11.jaxb20");
                javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
                Object el = u.unmarshal(element);
                return new com.sun.xml.wss.saml.assertion.saml11.jaxb20.Assertion((AssertionType)((JAXBElement)el).getValue());
            }
        } catch ( Exception ex) {
            // log here
            throw new SAMLException(ex);
        }
    }
    
    
    public static String getConfirmationMethod(Element assertion) {
        if (assertion == null) {
            throw new RuntimeException("SAML Assertion was Null");
        }
        NodeList nl = assertion.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "ConfirmationMethod");
        if ( nl.getLength() == 0)
            return null;
        
        Element confirmationMethod = (Element)nl.item(0);
        try {
            return confirmationMethod.getTextContent();
        } catch (DOMException ex) {
            //ignore
            return null;
        }
    }
    
    
    public static Element getSubjectConfirmationKeyInfo(Element assertion)
    throws XWSSecurityException {
        
        try {
            NodeList nl1 = assertion.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "SubjectConfirmation");
            
            if ( nl1.getLength() == 0) {
                throw new XWSSecurityException("SAML Assertion does not contain a key");
            }
            
            NodeList nl = ((Element)(nl1.item(0))).getElementsByTagNameNS(MessageConstants.DSIG_NS, "KeyInfo");
            
            if ( nl.getLength() == 0) {
                throw new XWSSecurityException("SAML Assertion does not contain a key");
            }
            
            //NodeList nl = assertion.getElementsByTagNameNS(MessageConstants.DSIG_NS, "KeyInfo");
            if ( nl.getLength() != 0) {
                Element keyInfo = (Element)nl.item(0);
                if(keyInfo != null){
                    return keyInfo;
                }
            }
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        throw new XWSSecurityException(
                "Unable to locate KeyInfo inside SubjectConfirmation of SAML Assertion");
    }
}
