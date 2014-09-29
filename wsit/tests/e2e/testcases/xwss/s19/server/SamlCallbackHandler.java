/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: SamlCallbackHandler.java,v 1.2 2010-10-21 14:23:29 snajper Exp $
 */
package xwss.s19.server;

import java.io.*;

import java.util.*;

import java.math.BigInteger;



import java.text.SimpleDateFormat;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.impl.policy.mls.SymmetricKeyBinding;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.configuration.StaticApplicationContext;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.callback.*;
import com.sun.xml.wss.saml.*;
import com.sun.xml.wss.saml.util.SAMLUtil;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.security.cert.Certificate;
import org.apache.xml.security.keys.KeyInfo;

public class SamlCallbackHandler implements CallbackHandler {

    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;
    private String trustStoreURL;
    private String trustStorePassword;
    private String trustStoreType;
    private KeyStore keyStore;
    private KeyStore trustStore;
    private static final String fileSeparator = System.getProperty("file.separator");
    private UnsupportedCallbackException unsupported =
            new UnsupportedCallbackException(null, "Unsupported Callback Type Encountered");
    private static Element svAssertion = null;
    private static Element svAssertion20 = null;
    private static Element hokAssertion = null;
    private static Element hokAssertion20 = null;
    public static final String holderOfKeyConfirmation =
            "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";
    public static final String senderVouchesConfirmation =
            "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";
    public static final String holderOfKeyConfirmation_saml20 =
            "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";
    public static final String senderVouchesConfirmation_saml20 =
            "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";
    String home = null;

    public SamlCallbackHandler() {

        try {
            Properties properties = new Properties();
            setContainerHome();
            String clientPropsFile = home + fileSeparator + "xws-security" + fileSeparator + "etc" + fileSeparator + "client-security-env.properties";
            properties.load(new FileInputStream(clientPropsFile));
            this.keyStoreURL = home + properties.getProperty("keystore.url");
            this.keyStoreType = properties.getProperty("keystore.type");
            this.keyStorePassword = properties.getProperty("keystore.password");
            this.trustStoreURL = home + properties.getProperty("truststore.url");
            this.trustStoreType = properties.getProperty("truststore.type");
            this.trustStorePassword = properties.getProperty("truststore.password");
            initKeyStore();
            initTrustStore();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setContainerHome() {
        this.home = System.getProperty("WSIT_HOME");
        if (this.home == null) {
            this.home = System.getProperty("xtest.sjsas.home");
        }
        if (this.home == null) {
            this.home = System.getProperty("tomcat.home");
        }
        if (this.home == null) {
            System.out.println("WARNING: Could not locate container.home in PlugFestServerCallbackHandler");
        }
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
                try {
                    SAMLCallback samlCallback = (SAMLCallback) callbacks[i];
                    if (samlCallback.getConfirmationMethod().equals(samlCallback.SV_ASSERTION_TYPE)) {
                        samlCallback.setAssertionElement(createSVSAMLAssertion());
                        svAssertion = samlCallback.getAssertionElement();
                    } else if (samlCallback.getConfirmationMethod().equals(samlCallback.HOK_ASSERTION_TYPE)) {
                        //samlCallback.setAssertionElement(createHOKSAMLAssertion());
                        samlCallback.setAssertionElement(createHOKSAMLAssertion20());
                        hokAssertion = samlCallback.getAssertionElement();
                    } else {
                        throw new Exception("SAML Assertion Type is not matched.");
                    }
                } catch (Exception ex) {
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
            long offsetHours = 60 * 60 * 1000;
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before = (GregorianCalendar) c.clone();
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar) c.clone();
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
            attributes.add(factory.createAttribute(
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
            long offsetHours = 60 * 60 * 1000;
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before = (GregorianCalendar) c.clone();
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar) c.clone();
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
            attributes.add(factory.createAttribute(
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

    private Element createHOKSAMLAssertion() {
        Assertion assertion = null;
        try {
            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML1_1);
            // create the assertion id
            String assertionID = String.valueOf(System.currentTimeMillis());
            String issuer = "CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US";
            GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60 * 60 * 1000;
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before = (GregorianCalendar) c.clone();
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar) c.clone();
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
            if (request.getX509Certificate() == null) {
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
            attributes.add(factory.createAttribute(
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
            //return assertion.sign(pubKey, privKey);
            Element assertion1 = assertion.sign(pubKey, privKey);
            Assertion ass1 = AssertionUtil.fromElement(assertion1);
            ass1.getAdvice();
            Conditions cond = ass1.getConditions();
            System.out.println("cond.getNotBeforeDate() : "+cond.getNotBeforeDate());
            System.out.println("cond.getNotOnOrAfterDate() : "+cond.getNotOnOrAfterDate());
            List<Object> condAbsList = cond.getConditions();            
            Iterator it = condAbsList.iterator();
            while(it.hasNext()){
                Object obj = it.next();
                if(obj instanceof AudienceRestrictionCondition){
                    AudienceRestrictionCondition arcond = (AudienceRestrictionCondition)obj;
                    System.out.println("arcond.getAudience() : "+arcond.getAudience());
                }else{
                    System.out.println(" conditions :");
                }
            }
            List<Object> statementList = ass1.getStatements();
            Iterator ite = statementList.iterator();
            while(ite.hasNext()){
                Object object = ite.next();
                if(object instanceof AttributeStatement){
                    AttributeStatement attStmt = (AttributeStatement)object;
                    Iterator ite1 = attStmt.getAttributes().iterator();
                    while(ite1.hasNext()){
                        Attribute object1 = (Attribute)ite1.next();
                        System.out.println("object1.getFriendlyName() : "+object1.getFriendlyName());
                        System.out.println("object1.getName() : "+object1.getName());
                        System.out.println("object1.getNameFormat() : "+object1.getNameFormat());
                        System.out.println("object1.getAttributes() : "+object1.getAttributes());                        
                    }                    
                    Subject sub = attStmt.getSubject();
                    NameIdentifier nameId = sub.getNameIdentifier();
                    if(nameId != null){
                        System.out.println("nameId.getFormat() : " +nameId.getFormat());
                        System.out.println("nameId.getNameQualifier() : "+nameId.getNameQualifier());
                        System.out.println("nameId.getValue() : "+nameId.getValue());
                    }
                    SubjectConfirmation sc = sub.getSubjectConfirmation();
                    if(sc != null){                                            
                        System.out.println("sc.getConfirmationMethod() : "+sc.getConfirmationMethod());
                        System.out.println("sc.getSubjectConfirmationDataForSAML11() : "+sc.getSubjectConfirmationDataForSAML11());
                    }
                }else if(object instanceof AuthenticationStatement){
                    AuthenticationStatement authStmt = (AuthenticationStatement)object;                    
                    System.out.println(authStmt.getAuthenticationInstantDate());
                    System.out.println(authStmt.getAuthenticationMethod());
                    System.out.println(authStmt.getSubjectLocalityDNSAddress());
                    System.out.println(authStmt.getSubjectLocalityIPAddress());
                    Subject sub = authStmt.getSubject();
                    NameIdentifier nameId = sub.getNameIdentifier();
                    System.out.println("nameId.getFormat() : " +nameId.getFormat());
                    System.out.println("nameId.getNameQualifier() : "+nameId.getNameQualifier());
                    System.out.println("nameId.getValue() : "+nameId.getValue());
                    SubjectConfirmation sc = sub.getSubjectConfirmation();
                    System.out.println("sc.getConfirmationMethod() : "+sc.getConfirmationMethod());
                    System.out.println("sc.getSubjectConfirmationDataForSAML11() : "+sc.getSubjectConfirmationDataForSAML11());
                }else if(object instanceof AuthorizationDecisionStatement){
                    AuthorizationDecisionStatement authDecStmt = (AuthorizationDecisionStatement)object;                    
                    System.out.println(authDecStmt.getDecisionValue());
                    System.out.println(authDecStmt.getResource());
                    Subject sub = authDecStmt.getSubject();
                    NameIdentifier nameId = sub.getNameIdentifier();
                    System.out.println("nameId.getFormat() : " +nameId.getFormat());
                    System.out.println("nameId.getNameQualifier() : "+nameId.getNameQualifier());
                    System.out.println("nameId.getValue() : "+nameId.getValue());
                    SubjectConfirmation sc = sub.getSubjectConfirmation();
                    System.out.println("sc.getConfirmationMethod() : "+sc.getConfirmationMethod());
                    System.out.println("sc.getSubjectConfirmationDataForSAML11() : "+sc.getSubjectConfirmationDataForSAML11());                    
                    Iterator<Action> it2 = (Iterator<Action>) authDecStmt.getActionList();
                    while(it2.hasNext()){
                        Action act = (Action)it2.next();
                        System.out.println("act.getNamespace() : "+act.getNamespace());
                        System.out.println("act.getValue() : "+act.getValue());
                    }
                }
            }
            return assertion1;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Element createHOKSAMLAssertion20() {
        Assertion assertion = null;
        try {
            SAMLAssertionFactory factory = SAMLAssertionFactory.newInstance(SAMLAssertionFactory.SAML2_0);
            // create the assertion id
            String assertionID = String.valueOf(System.currentTimeMillis());
            //String issuer = "CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US";
            GregorianCalendar c = new GregorianCalendar();
            long beforeTime = c.getTimeInMillis();
            // roll the time by one hour
            long offsetHours = 60 * 60 * 1000;
            c.setTimeInMillis(beforeTime - offsetHours);
            GregorianCalendar before = (GregorianCalendar) c.clone();
            c = new GregorianCalendar();
            long afterTime = c.getTimeInMillis();
            c.setTimeInMillis(afterTime + offsetHours);
            GregorianCalendar after = (GregorianCalendar) c.clone();
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
            if (request.getX509Certificate() == null) {
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
            attributes.add(factory.createAttribute(
                    "attribute1",
                    attributeValues));
            statements.add(factory.createAttributeStatement(attributes));
            Conditions conditions = factory.createConditions(before, after, null, null, null, null);
            assertion = factory.createAssertion(assertionID, nmId, issueInstant,
                    conditions, null, subj, statements);
            assertion.setVersion("2.0");
            //return assertion.sign(pubKey, privKey);
            Element assertion1 = assertion.sign(pubKey, privKey);
            Assertion ass1 = AssertionUtil.fromElement(assertion1);
            Subject sub = ass1.getSubject();
            NameID nameId = sub.getNameId();
            System.out.println("nameId.getFormat() : " +nameId.getFormat());
            System.out.println("nameId.getNameQualifier() : "+nameId.getNameQualifier());
            System.out.println("nameId.getValue() : "+nameId.getValue());
            SubjectConfirmation sc = sub.getSubjectConfirmation();
            System.out.println("sc.getConfirmationMethod() : "+sc.getConfirmationMethod());
            System.out.println("sc.getSubjectConfirmationDataForSAML20() : "+sc.getSubjectConfirmationDataForSAML20());
            SubjectConfirmationData scData= sc.getSubjectConfirmationDataForSAML20();
            System.out.println("scData.getAddress() : "+scData.getAddress());
            System.out.println("scData.getInResponseTo() : "+scData.getInResponseTo());
            System.out.println("scData.getRecipient() : "+scData.getRecipient());
            System.out.println("scData.getNotBeforeDate() : "+scData.getNotBeforeDate());
            System.out.println("scData.getNotOnOrAfterDate() : "+scData.getNotOnOrAfterDate());
            ass1.getAdvice();
            Conditions cond = ass1.getConditions();
            System.out.println("cond.getNotBeforeDate() : "+cond.getNotBeforeDate());
            System.out.println("cond.getNotOnOrAfterDate() : "+cond.getNotOnOrAfterDate());
            List<Object> condAbsList = cond.getConditions();
            Iterator it = condAbsList.iterator();
            while(it.hasNext()){
                Object obj = it.next();
                if(obj instanceof AudienceRestriction){
                    AudienceRestriction arcond = (AudienceRestriction)obj;
                    System.out.println("arcond.getAudience() : "+arcond.getAudience());
                }else{
                    System.out.println(" conditions :");
                }
            }
            List<Object> statementList = ass1.getStatements();
            Iterator ite = statementList.iterator();
            while(ite.hasNext()){
                Object object = ite.next();
                if(object instanceof AttributeStatement){
                    AttributeStatement attStmt = (AttributeStatement)object;
                    Iterator ite1 = attStmt.getAttributes().iterator();
                    while(ite1.hasNext()){
                        Attribute object1 = (Attribute)ite1.next();
                        System.out.println("object1.getFriendlyName() : "+object1.getFriendlyName());
                        System.out.println("object1.getName() : "+object1.getName());
                        System.out.println("object1.getNameFormat() : "+object1.getNameFormat());
                        System.out.println("object1.getAttributes() : "+object1.getAttributes());                        
                    }                    
                }else if(object instanceof AuthnStatement){
                    AuthnStatement authStmt = (AuthnStatement)object;                    
                    System.out.println("authStmt.getAuthnInstantDate() : "+authStmt.getAuthnInstantDate());
                    System.out.println("authStmt.getSessionNotOnOrAfterDate() : "+authStmt.getSessionNotOnOrAfterDate());
                    System.out.println("authStmt.getAuthenticatingAuthority() : "+authStmt.getAuthenticatingAuthority());
                    System.out.println("authStmt.getAuthnContextClassRef() : "+authStmt.getAuthnContextClassRef());
                    System.out.println("authStmt.getSessionIndex() : "+authStmt.getSessionIndex());
                    System.out.println("authStmt.getSubjectLocalityAddress() : "+authStmt.getSubjectLocalityAddress());
                    System.out.println("authStmt.getSubjectLocalityDNSName() : "+authStmt.getSubjectLocalityDNSName());
                }
            }
            return assertion1;
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
                if (keyStore.isKeyEntry(currentAlias)) {
                    Certificate thisCertificate = keyStore.getCertificate(currentAlias);
                    if (thisCertificate != null) {
                        if (thisCertificate instanceof X509Certificate) {
                            if (uniqueAlias == null) {
                                uniqueAlias = currentAlias;
                            } else {
                                // Not unique!
                                uniqueAlias = null;
                                break;
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

    private PrivateKey getPrivateKeyFromKeyStore(PublicKey pk)
            throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                } else {
                    // Just returning the first one here
                    PrivateKey key = (PrivateKey) keyStore.getKey(alias, "changeit".toCharArray());
                    return key;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private X509Certificate getCertificateFromKeyStore(PublicKey pk)
            throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                if (x509Cert.getPublicKey().equals(pk)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private X509Certificate getCertificateFromTrustStore(PublicKey pk)
            throws IOException {
        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                if (x509Cert.getPublicKey().equals(pk)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private class X509CertificateValidatorImpl implements CertificateValidationCallback.CertificateValidator {

        public boolean validate(X509Certificate certificate)
                throws CertificateValidationCallback.CertificateValidationException {
            if (isSelfCert(certificate)) {
                return true;
            }
            try {
                certificate.checkValidity();
            } catch (CertificateExpiredException e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException("X509Certificate Expired", e);
            } catch (CertificateNotYetValidException e) {
                e.printStackTrace();

                throw new CertificateValidationCallback.CertificateValidationException("X509Certificate not yet valid", e);
            }
            X509CertSelector certSelector = new X509CertSelector();
            certSelector.setCertificate(certificate);
            PKIXBuilderParameters parameters;
            CertPathBuilder builder;
            try {
                parameters = new PKIXBuilderParameters(trustStore, certSelector);
                parameters.setRevocationEnabled(false);
                builder = CertPathBuilder.getInstance("PKIX");
            } catch (Exception e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }
            try {
                PKIXCertPathBuilderResult result =
                       (PKIXCertPathBuilderResult) builder.build(parameters);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private boolean isSelfCert(X509Certificate cert)
                throws CertificateValidationCallback.CertificateValidationException {
            try {
                if (keyStore == null) {
                   initKeyStore();
               }
                Enumeration aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        X509Certificate x509Cert =
                               (X509Certificate) keyStore.getCertificate(alias);
                        if (x509Cert != null) {
                            if (x509Cert.equals(cert)) {
                               return true;
                            }
                        }
                    }
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }
        }
    }

    private class DefaultTimestampValidator implements TimestampValidationCallback.TimestampValidator {

        public void validate(TimestampValidationCallback.Request request)
                throws TimestampValidationCallback.TimestampValidationException {
            // validate timestamp creation and expiration time.
            TimestampValidationCallback.UTCTimestampRequest utcTimestampRequest =
                    (TimestampValidationCallback.UTCTimestampRequest) request;
            SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
            Date created = null;
            Date expired = null;
            try {
                try {
                    created = calendarFormatter1.parse(utcTimestampRequest.getCreated());
                    if (utcTimestampRequest.getExpired() != null) {
                        expired = calendarFormatter1.parse(utcTimestampRequest.getExpired());
                    }
                } catch (java.text.ParseException pe) {
                    created = calendarFormatter2.parse(utcTimestampRequest.getCreated());
                    if (utcTimestampRequest.getExpired() != null) {
                        expired = calendarFormatter2.parse(utcTimestampRequest.getExpired());
                    }
                }
            } catch (java.text.ParseException pe) {
                throw new TimestampValidationCallback.TimestampValidationException(pe.getMessage());
            }
            long maxClockSkew = utcTimestampRequest.getMaxClockSkew();
            long timestampFreshnessLimit = utcTimestampRequest.getTimestampFreshnessLimit();
            // validate creation time
            validateCreationTime(created, maxClockSkew, timestampFreshnessLimit);
            // validate expiration time
            if (expired != null) {
                validateExpirationTime(expired, maxClockSkew, timestampFreshnessLimit);
            }
        }
    }

    public void validateExpirationTime(
            Date expires, long maxClockSkew, long timestampFreshnessLimit)
            throws TimestampValidationCallback.TimestampValidationException {
        Date currentTime =
                getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, false);
        if (expires.before(currentTime)) {
            throw new TimestampValidationCallback.TimestampValidationException(
                    "The current time is ahead of the expiration time in Timestamp");
        }
    }

    public void validateCreationTime(
            Date created,
            long maxClockSkew,
            long timestampFreshnessLimit)
            throws TimestampValidationCallback.TimestampValidationException {
        Date current = getFreshnessAndSkewAdjustedDate(maxClockSkew, timestampFreshnessLimit);
        if (created.before(current)) {
            throw new TimestampValidationCallback.TimestampValidationException(
                    "The creation time is older than " +
                    " currenttime - timestamp-freshness-limit - max-clock-skew");
        }
        Date currentTime =
                getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
        if (currentTime.before(created)) {
            throw new TimestampValidationCallback.TimestampValidationException(
                    "The creation time is ahead of the current time.");
        }
    }

    private static Date getFreshnessAndSkewAdjustedDate(
            long maxClockSkew, long timestampFreshnessLimit) {

        Calendar c = new GregorianCalendar();
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;
        long adjustedTime = currentTime - maxClockSkew - timestampFreshnessLimit;
        c.setTimeInMillis(adjustedTime);
        return c.getTime();
    }

    private static Date getGMTDateWithSkewAdjusted(
            Calendar c, long maxClockSkew, boolean addSkew) {

        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;
        if (addSkew) {
            currentTime = currentTime + maxClockSkew;
        } else {
            currentTime = currentTime - maxClockSkew;
        }
        c.setTimeInMillis(currentTime);
        return c.getTime();

    }

    private String getContainerHome() {
        String _home = "";
        String fileSeparator = System.getProperty("file.separator");
        String contHome = System.getProperty("catalina.home");
        if (contHome != null) {
            String isAS = System.getProperty("com.sun.aas.instanceRoot");
            if (isAS != null) {
                _home = contHome + fileSeparator +
                        ".." + fileSeparator + "..";
            } else {
                _home = contHome;
            }
        } else {
            _home = System.getProperty("jwsdp.home");
            if (_home == null) {
                _home = System.getProperty("as.home");
            }
        }
        return _home;
    }
}

