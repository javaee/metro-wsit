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

package simple.util.xwss.saml;

import java.io.*;
import java.util.*;
import java.math.BigInteger;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.*;

import com.sun.xml.wss.saml.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import com.sun.org.apache.xml.internal.security.keys.KeyInfo;

public  class SamlCallbackHandler implements CallbackHandler {    
    
    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;
    
    private String trustStoreURL;
    private String trustStorePassword;
    private String trustStoreType;
    
    private KeyStore keyStore;
    private KeyStore trustStore;
    
    private static final String fileSeparator = System.getProperty("file.separator");
    
    private  UnsupportedCallbackException unsupported =
					new UnsupportedCallbackException(null,
						"Unsupported Callback Type Encountered");
    
    private  static Element svAssertion = null;
    private  static Element svAssertion20 = null;
    private  static Element hokAssertion = null;
    private  static Element hokAssertion20 = null;
    
    public static final String holderOfKeyConfirmation =
    "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    
    public static final String senderVouchesConfirmation =
    "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";
    
    public static final String holderOfKeyConfirmation_saml20 =
    "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
    
    public static final String senderVouchesConfirmation_saml20 =
    "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";
    
    String home = System.getProperty("WSIT_HOME");
    String client_priv_key_alias="xws-security-client";

    public SamlCallbackHandler() {
        try {

            this.keyStoreURL = home + fileSeparator + "domains" + fileSeparator + "domain1" + 
                    fileSeparator + "config" + fileSeparator + "keystore.jks";
            this.keyStoreType = "JKS";
            this.keyStorePassword = "changeit";
                                                                                                                                                             
            this.trustStoreURL = home + fileSeparator + "domains" + fileSeparator + "domain1" + 
				fileSeparator + "config" + fileSeparator + "cacerts.jks";
            this.trustStoreType = "JKS";
            this.trustStorePassword = "changeit";
 
            initKeyStore();
            initTrustStore();			
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i=0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
                try{
                    SAMLCallback samlCallback = (SAMLCallback)callbacks[i];
                    if (samlCallback.getConfirmationMethod().equals(samlCallback.SV_ASSERTION_TYPE)){
                            samlCallback.setAssertionElement(createSVSAMLAssertion());
                            //samlCallback.setAssertionElement(createSVSAMLAssertion20());
                            svAssertion=samlCallback.getAssertionElement();
                    }else if (samlCallback.getConfirmationMethod().equals(samlCallback.HOK_ASSERTION_TYPE)){
                            samlCallback.setAssertionElement(createHOKSAMLAssertion());
                            //samlCallback.setAssertionElement(createHOKSAMLAssertion20());
                            hokAssertion=samlCallback.getAssertionElement();
                    }else{
                            throw new Exception("SAML Assertion Type is not matched.");
                    }
                }catch(Exception ex){
                        ex.printStackTrace();
                }
            } else {
                throw unsupported;
            }
        }
    }
    
    private static Element createSVSAMLAssertion() {
        Assertion assertion = null;
        try {
            // create the assertion id
            String assertionID = String.valueOf(System.currentTimeMillis());
            String issuer = "CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US";
            
            
            GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60*60*1000;

            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before= (GregorianCalendar)c.clone();
            
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar)c.clone();
            
            GregorianCalendar issueInstant = new GregorianCalendar();
            // statements
            List statements = new LinkedList();


            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);

            NameIdentifier nmId =
            factory.createNameIdentifier(
            "CN=SAML User,OU=SU,O=SAML User,L=Los Angeles,ST=CA,C=US",
            null, // not sure abt this value
            "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName");

            SubjectConfirmation scf =
            factory.createSubjectConfirmation("urn:oasis:names:tc:SAML:1.0:cm:sender-vouches");
           
 
            Subject subj = factory.createSubject(nmId, scf);
           
            List attributes = new LinkedList();

            List attributeValues = new LinkedList();
            attributeValues.add("ATTRIBUTE1");
            attributes.add( factory.createAttribute(
                "attribute1",
                "urn:com:sun:xml:wss:attribute",
                 attributeValues));

            statements.add(
            factory.createAttributeStatement(subj, attributes));
            
            Conditions conditions = factory.createConditions(before, after, null, null, null);
            
            assertion = factory.createAssertion(assertionID, issuer, issueInstant,
            conditions, null, statements);
            assertion.setMajorVersion(BigInteger.ONE);
            assertion.setMinorVersion(BigInteger.ONE);
            return assertion.toElement(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Element createSVSAMLAssertion20() {
        Assertion assertion = null;
        try {
            // create the assertion id
            String aID = String.valueOf(System.currentTimeMillis());                        
            
            GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60*60*1000;

            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before= (GregorianCalendar)c.clone();
            
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar)c.clone();
            
            GregorianCalendar issueInstant = new GregorianCalendar();
            // statements
            List statements = new LinkedList();

            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);

            NameID nmId = factory.createNameID(
            "CN=SAML User,OU=SU,O=SAML User,L=Los Angeles,ST=CA,C=US",
            null, // not sure abt this value
            "urn:oasis:names:tc:SAML:2.0:nameid-format:X509SubjectName");
                        
            SubjectConfirmation scf =
            factory.createSubjectConfirmation(nmId, "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches");
           
            Subject subj = factory.createSubject(nmId, scf);
           
            List attributes = new LinkedList();

            List attributeValues = new LinkedList();
            attributeValues.add("ATTRIBUTE1");
            attributes.add( factory.createAttribute(
                "attribute1", attributeValues));

            statements.add(
            factory.createAttributeStatement(attributes));
            
            Conditions conditions = factory.createConditions(before, after, null, null, null, null);
            
            assertion = factory.createAssertion(aID, nmId, issueInstant, conditions, null, subj, statements);
            assertion.setVersion("2.0");            
 
            return assertion.toElement(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private  Element createHOKSAMLAssertion() {
        
        Assertion assertion = null;
        try {
                             
            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
                                                                                                
            // create the assertion id
            String assertionID = String.valueOf(System.currentTimeMillis());
            String issuer = "CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US";
                                                                                                                             
                                                                                                                             
            GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60*60*1000;
                                                                                                                             
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before= (GregorianCalendar)c.clone();
                                                                                                                             
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar)c.clone();
                                                                                                                             
            GregorianCalendar issueInstant = new GregorianCalendar();
            // statements
            List statements = new LinkedList();
            NameIdentifier nmId =
            factory.createNameIdentifier(
            "CN=SAML User,OU=SU,O=SAML User,L=Los Angeles,ST=CA,C=US",
            null, // not sure abt this value
            "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName");           

            //default priv key cert req
            SignatureKeyCallback.DefaultPrivKeyCertRequest request =
            new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            getDefaultPrivKeyCert(request);
            
            if ( request.getX509Certificate() == null ) {
                throw new RuntimeException("Not able to resolve the Default Certificate");
            }                                                                                                                 
            PublicKey pubKey = request.getX509Certificate().getPublicKey();
            PrivateKey privKey = request.getPrivateKey();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

            Document doc = docFactory.newDocumentBuilder().newDocument();

            KeyInfo keyInfo = new KeyInfo(doc);
            keyInfo.addKeyValue(pubKey);

            List subConfirmation = new ArrayList();
            subConfirmation.add(holderOfKeyConfirmation);

            SubjectConfirmation scf =
            factory.createSubjectConfirmation(subConfirmation, null, keyInfo.getElement());
                                                                                                                             
                                                                                                                             
            Subject subj = factory.createSubject(nmId, scf);
                                                                                                                             
            List attributes = new LinkedList();
            List attributeValues = new LinkedList();
            attributeValues.add("ATTRIBUTE1");
            attributes.add( factory.createAttribute(
                "attribute1",
                "urn:com:sun:xml:wss:attribute",
                attributeValues));
                                                                                                                             
            statements.add(
            factory.createAttributeStatement(subj, attributes));
                                                                                                                             
            Conditions conditions = factory.createConditions(before, after, null, null, null);
                                                                                                                             
            assertion = factory.createAssertion(assertionID, issuer, issueInstant,
            conditions, null, statements);
            assertion.setMajorVersion(BigInteger.ONE);
            assertion.setMinorVersion(BigInteger.ONE);
 
            return assertion.sign(pubKey, privKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
	}
 
    private  Element createHOKSAMLAssertion20() {
        
        Assertion assertion = null;
        try {
                             
            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
                                                                                                
            // create the assertion id
            String assertionID = String.valueOf(System.currentTimeMillis());
            //String issuer = "CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US";

			GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60*60*1000;
                                                                                                                             
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before= (GregorianCalendar)c.clone();
                                                                                                                             
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar)c.clone();
                                                                                                                             
            GregorianCalendar issueInstant = new GregorianCalendar();
            // statements
            List statements = new LinkedList();
            NameID nmId = factory.createNameID("CN=SAML User,OU=SU,O=SAML User,L=Los Angeles,ST=CA,C=US",
            null, // not sure abt this value
            "urn:oasis:names:tc:SAML:2.0:nameid-format:X509SubjectName");           

            //default priv key cert req
            SignatureKeyCallback.DefaultPrivKeyCertRequest request =
	            new SignatureKeyCallback.DefaultPrivKeyCertRequest();

            getDefaultPrivKeyCert(request);
            
            if ( request.getX509Certificate() == null ) {
                throw new RuntimeException("Not able to resolve the Default Certificate");
            }                                                                                                                 
            PublicKey pubKey = request.getX509Certificate().getPublicKey();
            PrivateKey privKey = request.getPrivateKey();

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            Document doc = docFactory.newDocumentBuilder().newDocument();

            KeyInfo keyInfo = new KeyInfo(doc);
            keyInfo.addKeyValue(pubKey);

            List subConfirmation = new ArrayList();
            subConfirmation.add(holderOfKeyConfirmation_saml20);
			SubjectConfirmationData scd = factory.createSubjectConfirmationData(null, null, null, null, null, keyInfo.getElement());

            SubjectConfirmation scf = factory.createSubjectConfirmation(nmId, scd, holderOfKeyConfirmation_saml20);
                                                                                                                             
                                                                                                                             
            Subject subj = factory.createSubject(nmId, scf);
                                                                                                                             
            List attributes = new LinkedList();
            List attributeValues = new LinkedList();
            attributeValues.add("ATTRIBUTE1");
            attributes.add( factory.createAttribute(
                "attribute1",
                attributeValues));
                                                                                                                             
            statements.add(factory.createAttributeStatement(attributes));
                                                                                                                             
            Conditions conditions = factory.createConditions(before, after, null, null, null);
                                                                                                                             
            assertion = factory.createAssertion(assertionID, nmId, issueInstant,
				            conditions, null, subj, statements);
            assertion.setVersion("2.0");
 
            return assertion.sign(pubKey, privKey);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
    }  
    
    private void initKeyStore() throws IOException {
        try {
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(keyStoreURL), keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void initTrustStore() throws IOException {
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(new FileInputStream(trustStoreURL), trustStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void getDefaultPrivKeyCert(
    SignatureKeyCallback.DefaultPrivKeyCertRequest request)
    throws IOException {
        
        String uniqueAlias = null;
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String currentAlias = (String) aliases.nextElement();
                if (currentAlias.equals(client_priv_key_alias)){
                    if (keyStore.isKeyEntry(currentAlias)) {
                        Certificate thisCertificate = keyStore.getCertificate(currentAlias);
                        if (thisCertificate != null) {
                            if (thisCertificate instanceof X509Certificate) {
                                if (uniqueAlias == null) {
                                    uniqueAlias = currentAlias;                                
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (uniqueAlias != null) {
                request.setX509Certificate(
                (X509Certificate) keyStore.getCertificate(uniqueAlias));
                request.setPrivateKey(
                (PrivateKey) keyStore.getKey(uniqueAlias, keyStorePassword.toCharArray()));
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }
}

