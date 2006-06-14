/*
 * $Id: DefaultCallbackHandler.java,v 1.2 2006-06-14 11:45:42 kumarjayanti Exp $
 *
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

package com.sun.xml.wss.impl.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.net.URI;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.math.BigInteger;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureVerificationKeyCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import com.sun.xml.wss.impl.callback.SAMLAssertionValidator;
import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;

import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;

import com.sun.xml.ws.security.trust.WSTrustConstants;

import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;


/**
 * A sample implementation of a CallbackHandler.
 */
public  class DefaultCallbackHandler implements CallbackHandler {

    public static final String KEYSTORE_URL="keystore.url";
    public static final String KEYSTORE_TYPE="keystore.type";
    public static final String KEYSTORE_PASSWORD="keystore.password";
    public static final String MY_ALIAS="my.alias";
    public static final String MY_USERNAME="my.username";
    public static final String MY_PASSWORD="my.password";

    public static final String TRUSTSTORE_URL="truststore.url";
    public static final String TRUSTSTORE_TYPE="truststore.type";
    public static final String TRUSTSTORE_PASSWORD="truststore.password";
    public static final String PEER_ENTITY_ALIAS="peerentity.alias";
    public static final String STS_ALIAS="sts.alias";
    public static final String SERVICE_ALIAS="service.alias";

    public static final String USERNAME_CBH="username.callback.handler";
    public static final String PASSWORD_CBH="password.callback.handler";

    public static final String USERNAME_VALIDATOR="username.validator";
    public static final String SAML_VALIDATOR="saml.validator";
    public static final String TIMESTAMP_VALIDATOR="timestamp.validator";
    public static final String CERTIFICATE_VALIDATOR="certificate.validator";

    public static final String MAX_CLOCK_SKEW_PROPERTY="max.clock.skew";
    public static final String MAX_NONCE_AGE_PROPERTY="max.nonce.age";
    public static final String TIMESTAMP_FRESHNESS_LIMIT_PROPERTY="timestamp.freshness.limit";

    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;
    private String myAlias;

    private String trustStoreURL;
    private String trustStorePassword;
    private String trustStoreType;
    private String peerEntityAlias;
    private String stsAlias;
    private String serviceAlias;

    private String myUsername;
    private String myPassword;

    private KeyStore keyStore;
    private KeyStore trustStore;

    private Class usernameCbHandler;
    private Class passwordCbHandler;

    private Class usernameValidator;
    private Class timestampValidator;
    private Class samlValidator;
    private Class certificateValidator;

    private String home = null;

    protected long maxClockSkewG;
    protected long timestampFreshnessLimitG;
    protected long maxNonceAge;


    private static final String fileSeparator = System.getProperty("file.separator");
    private static final UnsupportedCallbackException unsupported = 
        new UnsupportedCallbackException(null, "Unsupported Callback Type Encountered");
    private static final URI ISSUE_REQUEST_URI = URI.create(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);

   
    private CallbackHandler usernameHandler;
    private CallbackHandler passwordHandler;

    private PasswordValidationCallback.PasswordValidator pwValidator;
    private TimestampValidationCallback.TimestampValidator tsValidator;
    private CertificateValidationCallback.CertificateValidator certValidator;
    private SAMLAssertionValidator sValidator;

    private CertificateValidationCallback.CertificateValidator defaultCertValidator;
    private TimestampValidationCallback.TimestampValidator defaultTSValidator;
    private PasswordValidationCallback.PasswordValidator defaultPWValidator;


    public DefaultCallbackHandler(String clientOrServer, Properties assertions) throws Exception {

             Properties properties = null;
             if (assertions != null && !assertions.isEmpty()) {
                 properties = assertions;
             } else {
                 //fallback option
                 properties = new Properties();
                 String resource = clientOrServer + "-security-env.properties";
                 InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                 if (in != null) {
                     properties.load(in);
                 } else {
                     //throw new XWSSecurityException("Resource " + resource + " could not be located in classpath");
                 }
             }


             this.keyStoreURL =  properties.getProperty(KEYSTORE_URL);
             this.keyStoreURL = resolveHome(this.keyStoreURL);
             this.keyStoreType = properties.getProperty(KEYSTORE_TYPE);
             this.keyStorePassword = properties.getProperty(KEYSTORE_PASSWORD);
             this.myAlias = properties.getProperty(MY_ALIAS);
             this.myUsername =  properties.getProperty(MY_USERNAME);
             this.myPassword =  properties.getProperty(MY_PASSWORD);

                                                                                                         
             this.trustStoreURL = properties.getProperty(TRUSTSTORE_URL);
             this.trustStoreURL = resolveHome(this.trustStoreURL);
             this.keyStoreType = properties.getProperty(KEYSTORE_TYPE);
             this.trustStoreType = properties.getProperty(TRUSTSTORE_TYPE);
             this.trustStorePassword = properties.getProperty(TRUSTSTORE_PASSWORD);
             this.peerEntityAlias =  properties.getProperty(PEER_ENTITY_ALIAS);
             this.stsAlias =  properties.getProperty(STS_ALIAS);
             this.serviceAlias = properties.getProperty(SERVICE_ALIAS);


             String uCBH = properties.getProperty(USERNAME_CBH);
             String pCBH = properties.getProperty(PASSWORD_CBH);

             String uV = properties.getProperty(USERNAME_VALIDATOR);
             String sV = properties.getProperty(SAML_VALIDATOR);
             String tV = properties.getProperty(TIMESTAMP_VALIDATOR);
             String cV = properties.getProperty(CERTIFICATE_VALIDATOR);

             usernameCbHandler = loadClass(uCBH);
             passwordCbHandler = loadClass(pCBH);
             usernameValidator = loadClass(uV);
             samlValidator = loadClass(sV);
             timestampValidator = loadClass(tV);
             certificateValidator = loadClass(cV);

             String mcs = properties.getProperty(MAX_CLOCK_SKEW_PROPERTY);
             String tfl = properties.getProperty(TIMESTAMP_FRESHNESS_LIMIT_PROPERTY);
             String mna = properties.getProperty(MAX_NONCE_AGE_PROPERTY);

             maxClockSkewG =  toLong(mcs);
             timestampFreshnessLimitG = toLong(tfl);
             maxNonceAge = toLong(mna);

             initTrustStore();
             initKeyStore();
             initNewInstances();

             defaultCertValidator = new X509CertificateValidatorImpl();
             defaultTSValidator = new  DefaultTimestampValidator();
    }

    private void handleUsernameCallback(UsernameCallback cb) throws IOException, UnsupportedCallbackException {
        if (myUsername != null) {
            cb.setUsername(myUsername);
        } else if (usernameHandler != null) {
            javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("Username="); 
            Callback[] cbs = new Callback[] {nc};
            usernameHandler.handle(cbs); 
            cb.setUsername(((javax.security.auth.callback.NameCallback)cbs[0]).getName());
        } else {
            throw new UnsupportedCallbackException(null, "Username Handler Not Configured");
        }
    }

    private void handlePasswordCallback(PasswordCallback cb) throws IOException, UnsupportedCallbackException {
        if (myPassword != null) {
            cb.setPassword(myPassword);
        } else if (passwordHandler != null) {
            javax.security.auth.callback.PasswordCallback pc = new javax.security.auth.callback.PasswordCallback("Password=", false); 
            Callback[] cbs = new Callback[] {pc};
            passwordHandler.handle(cbs); 
            char[] pass = ((javax.security.auth.callback.PasswordCallback)cbs[0]).getPassword();
            cb.setPassword(new String(pass));
        } else {
            throw new UnsupportedCallbackException(null, "Password Handler Not Configured");
        }

    }

    private void handlePasswordValidation(PasswordValidationCallback cb) throws IOException, UnsupportedCallbackException {
        if (cb.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
            if (pwValidator != null) {
                cb.setValidator(pwValidator);
            } else {
                throw new UnsupportedCallbackException(null, "Password Validator Not Specified in Configuration");
            }
        } else if (cb.getRequest() instanceof PasswordValidationCallback.DigestPasswordRequest) {
            throw new UnsupportedCallbackException(null, "Digest Authentication for Passwords Not Supported");
        } else {
            throw new UnsupportedCallbackException(null, "Usupported Reuqest Type for Password Validation");
        }
    }

   private void handleTimestampValidation(TimestampValidationCallback cb) throws IOException, UnsupportedCallbackException {
        if (tsValidator != null) {
            cb.setValidator(tsValidator);
        } else {
            // this is for BC reasons, but will be enabled later
            cb.setValidator(defaultTSValidator);
        }
   }

   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        
        for (int i=0; i < callbacks.length; i++) {
            
          if (callbacks[i] instanceof UsernameCallback) {
                UsernameCallback cb = (UsernameCallback)callbacks[i];
                handleUsernameCallback(cb);
 
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback cb = (PasswordCallback)callbacks[i];
                handlePasswordCallback(cb);

            } else if (callbacks[i] instanceof PasswordValidationCallback) {
                PasswordValidationCallback cb = (PasswordValidationCallback) callbacks[i];
                handlePasswordValidation(cb);
                
            } else if (callbacks[i] instanceof TimestampValidationCallback) {
                TimestampValidationCallback cb = (TimestampValidationCallback) callbacks[i];
                handleTimestampValidation(cb);
                                                                             
            } else if (callbacks[i] instanceof SignatureVerificationKeyCallback) {

                SignatureVerificationKeyCallback cb = (SignatureVerificationKeyCallback)callbacks[i];
                
                if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    // subject keyid request
                    SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                    (SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    X509Certificate cert =
                    getCertificateFromTrustStore(
                    request.getSubjectKeyIdentifier());
                    request.setX509Certificate(cert);
                    
                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) {
                    // issuer serial request
                    SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest request =
                    (SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    X509Certificate cert =
                    getCertificateFromTrustStore(
                    request.getIssuerName(),
                    request.getSerialNumber());
                    request.setX509Certificate(cert);
                    
                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.ThumbprintBasedRequest) {
                    SignatureVerificationKeyCallback.ThumbprintBasedRequest request =
                    (SignatureVerificationKeyCallback.ThumbprintBasedRequest) cb.getRequest();
                    X509Certificate cert =
                    getCertificateFromTrustStoreForThumbprint(
                    request.getThumbprintIdentifier());
                    request.setX509Certificate(cert);

                } else  {
                    throw unsupported;
                }
                
            } else if (callbacks[i] instanceof SignatureKeyCallback) {
                SignatureKeyCallback cb = (SignatureKeyCallback)callbacks[i];
                
                if (cb.getRequest() instanceof SignatureKeyCallback.DefaultPrivKeyCertRequest) {
                    // default priv key cert req
                    SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    (SignatureKeyCallback.DefaultPrivKeyCertRequest) cb.getRequest();
                    getDefaultPrivKeyCert(request);
                    
                } else if (cb.getRequest() instanceof SignatureKeyCallback.AliasPrivKeyCertRequest) {
                    SignatureKeyCallback.AliasPrivKeyCertRequest request =
                    (SignatureKeyCallback.AliasPrivKeyCertRequest) cb.getRequest();
                    String alias = request.getAlias();
                    try {
                        X509Certificate cert =
                        (X509Certificate) keyStore.getCertificate(alias);
                        request.setX509Certificate(cert);
                        // Assuming key passwords same as the keystore password
                        PrivateKey privKey =
                        (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                        request.setPrivateKey(privKey);
                    } catch (Exception e) {
                        throw new IOException(e.getMessage());
                    }
                    
                } else {
                    throw unsupported;
                }
                
            } else if (callbacks[i] instanceof DecryptionKeyCallback) {
                DecryptionKeyCallback cb = (DecryptionKeyCallback)callbacks[i];
                
                if (cb.getRequest() instanceof  DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                    (DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    byte[] ski = request.getSubjectKeyIdentifier();
                    PrivateKey privKey = getPrivateKey(ski);
                    request.setPrivateKey(privKey);
                    
                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509IssuerSerialBasedRequest) {
                    DecryptionKeyCallback.X509IssuerSerialBasedRequest request =
                    (DecryptionKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    String issuerName = request.getIssuerName();
                    BigInteger serialNumber = request.getSerialNumber();
                    PrivateKey privKey = getPrivateKey(issuerName, serialNumber);
                    request.setPrivateKey(privKey);
                    
                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509CertificateBasedRequest) {
                    DecryptionKeyCallback.X509CertificateBasedRequest request =
                    (DecryptionKeyCallback.X509CertificateBasedRequest) cb.getRequest();
                    X509Certificate cert = request.getX509Certificate();
                    PrivateKey privKey = getPrivateKey(cert);
                    request.setPrivateKey(privKey);
                    
                } else if (cb.getRequest() instanceof  DecryptionKeyCallback.ThumbprintBasedRequest) {
                    DecryptionKeyCallback.ThumbprintBasedRequest request =
                    (DecryptionKeyCallback.ThumbprintBasedRequest) cb.getRequest();
                    byte[] ski = request.getThumbprintIdentifier();
                    PrivateKey privKey = getPrivateKeyForThumbprint(ski);
                    request.setPrivateKey(privKey);
                } else  {
                    throw unsupported;
                }
                
            } else if (callbacks[i] instanceof EncryptionKeyCallback) {
                EncryptionKeyCallback cb = (EncryptionKeyCallback)callbacks[i];
                
                if (cb.getRequest() instanceof EncryptionKeyCallback.AliasX509CertificateRequest) {
                    EncryptionKeyCallback.AliasX509CertificateRequest request =
                    (EncryptionKeyCallback.AliasX509CertificateRequest) cb.getRequest();
                    
                    Map runtimeProperties = cb.getRuntimeProperties();
                    if(isTrustMessage(runtimeProperties)){
                        getSTSCertificateFromTrustStore(request);
                        return;
                    }

                    String alias = request.getAlias();
                    if ("".equals(alias) || (alias == null)) {
                        getDefaultCertificateFromTrustStore(request);
                    } else {
                        try {
                            X509Certificate cert =
                            (X509Certificate) trustStore.getCertificate(alias);
                            request.setX509Certificate(cert);
                        } catch (Exception e) {
                            throw new IOException(e.getMessage());
                        }
                    }
                    
                } else if (cb.getRequest() instanceof EncryptionKeyCallback.AliasSymmetricKeyRequest) {
                    throw unsupported;
                }
                
            } else if (callbacks[i] instanceof CertificateValidationCallback) {
                CertificateValidationCallback cb = (CertificateValidationCallback)callbacks[i];
                if (certValidator != null) {
                    cb.setValidator(certValidator);
                } else {
                    cb.setValidator(defaultCertValidator);
                }
            } else  if (callbacks[i] instanceof DynamicPolicyCallback) {
               DynamicPolicyCallback dp = (DynamicPolicyCallback)callbacks[i];
               SecurityPolicy policy = dp.getSecurityPolicy();
               if (policy instanceof AuthenticationTokenPolicy.SAMLAssertionBinding) {
                   AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                   (AuthenticationTokenPolicy.SAMLAssertionBinding)
                   ((AuthenticationTokenPolicy.SAMLAssertionBinding)policy).clone();

                   if ((samlBinding.getAssertion() == null) && (samlBinding.getAuthorityBinding() == null)) {
                       populateAssertion(samlBinding, dp);
                   } else if (samlBinding.getAssertion() != null) {
                       validateSAMLAssertion(samlBinding);
                   } else if ((samlBinding.getAuthorityBinding() != null) && (samlBinding.getAssertionId() != null)) {                       
                       locateSAMLAssertion(samlBinding);
                   } else {
                       throw new UnsupportedCallbackException(null, "Missing information from SAML Policy");
                   }
               }
            } else {
                throw unsupported;
            }
        }
    }

    private void populateAssertion(AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding, DynamicPolicyCallback dp) 
         throws UnsupportedCallbackException {
        throw new UnsupportedCallbackException(null, "Internal Error: Cannot Populate SAML Assertion");
    }

    private void validateSAMLAssertion(AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding) 
         throws UnsupportedCallbackException {
        if (sValidator != null) {
            try {
                sValidator.validate(samlBinding.getAssertion());
            } catch (SAMLAssertionValidator.SAMLValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void locateSAMLAssertion(AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding) 
         throws UnsupportedCallbackException {
        throw new UnsupportedCallbackException(null, "Internal Error: Cannot Locate SAML Assertion");
    }

    private void initTrustStore() throws IOException {
        try {
            if (trustStoreURL == null) {
                return;
            }
            trustStore = KeyStore.getInstance(trustStoreType);
            trustStore.load(new FileInputStream(trustStoreURL), trustStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private void initKeyStore() throws IOException {
        try {
            if (keyStoreURL == null) {
                return;
            }
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(new FileInputStream(keyStoreURL), keyStorePassword.toCharArray());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }


    private X509Certificate getCertificateFromTrustStore(byte[] ski)
        throws IOException {

        try {
            if (trustStore == null) return null;
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private X509Certificate getCertificateFromTrustStore(
        String issuerName,
        BigInteger serialNumber)
        throws IOException {

        try {
            if (trustStore == null) return null;
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                    RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) &&
                    thisSerialNumber.equals(serialNumber)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public PrivateKey getPrivateKey(byte[] ski) throws IOException {

        try {
            if (keyStore == null) return null;
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    // Asuumed key password same as the keystore password
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public PrivateKey getPrivateKey(
        String issuerName,
        BigInteger serialNumber)
        throws IOException {

        try {
            if (keyStore == null) return null;
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                    RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) &&
                    thisSerialNumber.equals(serialNumber)) {
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    public PrivateKey getPrivateKey(X509Certificate certificate)
        throws IOException {

        try {
            if (keyStore == null) return null;
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null && cert.equals(certificate))
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }


    private void getDefaultCertificateFromTrustStore(
        EncryptionKeyCallback.AliasX509CertificateRequest req) throws IOException {
           
            try {
                if (trustStore == null) return;
                String currentAlias = null;
                if (peerEntityAlias != null) {
                    currentAlias = peerEntityAlias;
                } else {
                    Enumeration aliases = trustStore.aliases();
	            while (aliases.hasMoreElements()) {
                        currentAlias = (String) aliases.nextElement();
                        if (!"certificate-authority".equals(currentAlias) && !"root".equals(currentAlias)) {
                            break;
                        } else {
                            currentAlias = null;
                        }
                    }
                }
                if (currentAlias != null) {
                    //System.out.println("Encryption Key Alias=" + currentAlias);
                     X509Certificate thisCertificate = (X509Certificate)
                            trustStore.getCertificate(currentAlias);
                     req.setX509Certificate(thisCertificate);
                     return;
                } else {
                    throw new RuntimeException("Cannot locate  PEER Entity certificate from TrustStore");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }
    
    private void getSTSCertificateFromTrustStore(
        EncryptionKeyCallback.AliasX509CertificateRequest req) throws IOException {
           
            try {
                if (trustStore == null) return;
                String currentAlias = null;

                if (this.stsAlias != null) {
                    currentAlias = this.stsAlias;
                } else {
                    Enumeration aliases = trustStore.aliases();
	            while (aliases.hasMoreElements()) {
                        currentAlias = (String) aliases.nextElement();
                        if ("wssip".equals(currentAlias)) {
                            break;
                        } else {
                            currentAlias = null;
                        }
                    }
                }
                if (currentAlias != null) {
                    //System.out.println("Encryption Key Alias=" + currentAlias);
                    X509Certificate thisCertificate = (X509Certificate)
                        trustStore.getCertificate(currentAlias);
                    req.setX509Certificate(thisCertificate);
                    return;
                } else {
                    throw new RuntimeException("Cannot locate Security Token Service certificate from TrustStore");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }


    private void getDefaultPrivKeyCert(
        SignatureKeyCallback.DefaultPrivKeyCertRequest request)
        throws IOException {

        if (keyStore == null) return;
        String uniqueAlias = null;
        try {
            if (myAlias != null) {
                uniqueAlias = myAlias;
            } else {
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
            }
            if (uniqueAlias != null) {
                //System.out.println("Signing Key Alias=" + uniqueAlias);
                request.setX509Certificate(
                    (X509Certificate) keyStore.getCertificate(uniqueAlias));
                request.setPrivateKey(
                    (PrivateKey) keyStore.getKey(uniqueAlias, keyStorePassword.toCharArray()));
            } else {
                throw new RuntimeException("Cannot locate default certificate and privateKey from KeyStore");
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private static byte[] getSubjectKeyIdentifier(X509Certificate cert) {
        String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";
        byte[] subjectKeyIdentifier =
            cert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
        if (subjectKeyIdentifier == null)
            return null;
         
        try {
            sun.security.x509.KeyIdentifier keyId = null; 
         
            sun.security.util.DerValue derVal = new sun.security.util.DerValue(
                 new sun.security.util.DerInputStream(subjectKeyIdentifier).getOctetString());
         
            keyId = new sun.security.x509.KeyIdentifier(derVal.getOctetString());
            return keyId.getIdentifier();
        } catch (NoClassDefFoundError ncde) {
            if (subjectKeyIdentifier == null)
                return null;
            byte[] dest = new byte[subjectKeyIdentifier.length - 4];
            System.arraycopy(
                subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length - 4);
            return dest;
        } catch ( java.io.IOException ex) {
            //ignore
            return null;
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
                    if ( utcTimestampRequest.getExpired() != null ) 
                        expired = calendarFormatter1.parse(utcTimestampRequest.getExpired());
                } catch (java.text.ParseException pe) {
                    created = calendarFormatter2.parse(utcTimestampRequest.getCreated());
                    if ( utcTimestampRequest.getExpired() != null ) 
                        expired = calendarFormatter2.parse(utcTimestampRequest.getExpired());
                }
            } catch ( java.text.ParseException pe ) {
                throw new TimestampValidationCallback.TimestampValidationException(pe.getMessage());
            }

            long maxClockSkewLocal = utcTimestampRequest.getMaxClockSkew();
            if (maxClockSkewG > 0) {
                maxClockSkewLocal = maxClockSkewG;
            }
            long tsfLocal = utcTimestampRequest.getTimestampFreshnessLimit();
            if (timestampFreshnessLimitG > 0) {
                tsfLocal = timestampFreshnessLimitG;
            }

            // validate creation time
            validateCreationTime(created, maxClockSkewLocal, tsfLocal);
             
            // validate expiration time
            if ( expired != null )
                validateExpirationTime(expired, maxClockSkewLocal, tsfLocal);
        }
    }

    public void validateExpirationTime(
        Date expires, long maxClockSkew, long timestampFreshnessLimit)
        throws TimestampValidationCallback.TimestampValidationException {
                
        //System.out.println("Validate Expiration time called");
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

        //System.out.println("Validate Creation time called");
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
        
        if (addSkew)
            currentTime = currentTime + maxClockSkew;
        else
            currentTime = currentTime - maxClockSkew;
        
        c.setTimeInMillis(currentTime);
        return c.getTime();
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
                Enumeration aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    if (keyStore.isKeyEntry(alias)) {
                        X509Certificate x509Cert =
                            (X509Certificate) keyStore.getCertificate(alias);
                        if (x509Cert != null) {
                            if (x509Cert.equals(cert))
                                return true;
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

   
     private X509Certificate getCertificateFromTrustStoreForThumbprint(byte[] ski) throws IOException {
                                                                                                                                         
        try {
            if (trustStore == null) return null;
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getThumbprintIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

     public static byte[] getThumbprintIdentifier(X509Certificate cert)
       throws Exception {
        byte[] thumbPrintIdentifier = null;
                                                                                                                      
        try {
            thumbPrintIdentifier = MessageDigest.getInstance("SHA-1").digest(cert.getEncoded());
        } catch ( NoSuchAlgorithmException ex ) {
            throw new Exception("Digest algorithm SHA-1 not found");
        } catch ( CertificateEncodingException ex) {
            throw new Exception("Error while getting certificate's raw content");
        }
        return thumbPrintIdentifier;
    }
   
    
    public PrivateKey getPrivateKeyForThumbprint(byte[] ski) throws IOException {
                                                                                                                                         
        try {
            if (keyStore == null) return null;
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = getThumbprintIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    // Asuumed key password same as the keystore password
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    //TODO: add RENEW/CANCEL etc out here
    protected boolean isTrustMessage(Map map){
        String s = (String)map.get("isTrustMessage");
        if (s != null) {
            return s.equals("true");
        } else {
            AddressingProperties ap = (AddressingProperties)map.get("javax.xml.ws.addressing.context");
            if (ap != null) {
                AttributedURI uri = ap.getAction();
                if (uri != null && ISSUE_REQUEST_URI.equals(ap.getAction().getURI())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Class loadClass(String classname) throws XWSSecurityException {
        if (classname == null) {
            return null;
        }
        Class ret = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                ret = loader.loadClass(classname);
                return ret;
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        // if context classloader didnt work, try this
        loader = this.getClass().getClassLoader();
        try {
            ret = loader.loadClass(classname);
            return ret;
        } catch (ClassNotFoundException e) {
                // ignore
        }
        throw new XWSSecurityException("Could not find User Class " + classname);
    }

    private long toLong(String lng) throws Exception {
        if (lng == null) {
            return 0;
        }
        Long ret = 0L;
        try {
            ret = Long.valueOf(lng);
        }catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        return ret; 
    }

    private void initNewInstances() throws XWSSecurityException {
     
        try {

            if (usernameCbHandler != null) {
                usernameHandler = (CallbackHandler)usernameCbHandler.newInstance();
            }
            if (passwordCbHandler != null) {
                passwordHandler = (CallbackHandler)passwordCbHandler.newInstance();
            }

            if (usernameValidator != null) {
                pwValidator = (PasswordValidationCallback.PasswordValidator)usernameValidator.newInstance();
            }

            if (timestampValidator != null) {
                tsValidator = (TimestampValidationCallback.TimestampValidator)timestampValidator.newInstance();
            }

            if (samlValidator != null) {
                sValidator = (SAMLAssertionValidator)samlValidator.newInstance();
            }

            if (certificateValidator != null) {
                certValidator = (CertificateValidationCallback.CertificateValidator)certificateValidator.newInstance();
            }
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }

   private String resolveHome(String url) {
       if (url == null) {
           return null;
       }
       if (url.startsWith("$WSIT_HOME")) {
           String wsitHome = System.getProperty("WSIT_HOME");
           if (wsitHome != null) {
               String ret= url.replace("$WSIT_HOME", wsitHome);
               return ret;
           } else {
               throw new RuntimeException("System Property WSIT_HOME not set");
           }
       } else {
           return url;
       }
   }

}
