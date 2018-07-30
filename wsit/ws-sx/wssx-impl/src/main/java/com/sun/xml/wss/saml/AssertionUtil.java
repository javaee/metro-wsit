/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.keys.content.KeyValue;
import org.apache.xml.security.utils.EncryptionConstants;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
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
import com.sun.xml.wss.core.reference.X509IssuerSerial;
import com.sun.xml.wss.logging.LogDomainConstants;

import com.sun.xml.wss.impl.callback.*;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;
import com.sun.xml.wss.saml.util.SAML20JAXBUtil;
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
        
        NodeList nl1 = assertion.getElementsByTagName("SubjectConfirmation");
                
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
                throw new XWSSecurityException("Unsupported KeyValueType :" + keyId.getValueType());
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

            if (element.getAttributeNode("ID") != null){
                JAXBContext jc = SAML20JAXBUtil.getJAXBContext();

                javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
                Object el = u.unmarshal(element);
                return new com.sun.xml.wss.saml.assertion.saml20.jaxb20.Assertion((com.sun.xml.wss.saml.internal.saml20.jaxb20.AssertionType)((JAXBElement)el).getValue());
            }else{
                JAXBContext jc = SAMLJAXBUtil.getJAXBContext();

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
        NodeList nl = null;
        
        if (assertion.getAttributeNode("ID") != null){
            nl = assertion.getElementsByTagNameNS(MessageConstants.SAML_v2_0_NS, "SubjectConfirmation");            
        }else{
            nl = assertion.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "ConfirmationMethod");
        }
        if ( nl.getLength() == 0)
            return null;
        
        Element confirmationMethod = (Element)nl.item(0);
        try {
            if (assertion.getAttributeNode("ID") != null){
                return confirmationMethod.getAttribute("Method");
            }else{                
                return confirmationMethod.getTextContent();
            }
        } catch (DOMException ex) {
            //ignore
            return null;
        }
    }
    
    private static NodeList skipAdviceValidation(Element assertion, NodeList nodeList){
        boolean keyPresent = false;
        int returnNodeIndex = 0;
        for (int i = 0; i < nodeList.getLength(); i++){
            if(nodeList.item(i).getParentNode().getParentNode().getParentNode().getParentNode().getLocalName().equals("Advice")){
                // skip this node
            }else{
                keyPresent = true;
                returnNodeIndex = i;
                break;
            }
        }
        if(keyPresent){
            return ((Element)nodeList.item(returnNodeIndex)).getElementsByTagNameNS(MessageConstants.DSIG_NS, "KeyInfo");
        }else{
            return null;
        }
    }
    
    public static Element getSubjectConfirmationKeyInfo(Element assertion)
    throws XWSSecurityException {
        
        try {
            NodeList nl = null;
            NodeList nl1 = null;
            
            if (assertion.getAttributeNode("ID") != null){
                nl1 = assertion.getElementsByTagNameNS(MessageConstants.SAML_v2_0_NS, "SubjectConfirmationData");
            }else{
                nl1 = assertion.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS, "SubjectConfirmation");
            }
            
            if ( nl1.getLength() == 0) {
                throw new XWSSecurityException("SAML Assertion does not contain a key");
            }
            
            nl = skipAdviceValidation(assertion, nl1);
            
            if ( nl == null || nl.getLength() == 0) {
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
