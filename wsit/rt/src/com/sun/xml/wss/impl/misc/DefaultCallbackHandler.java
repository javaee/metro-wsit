/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.xml.wss.impl.misc;

import com.sun.xml.wss.AliasSelector;
import com.sun.xml.wss.impl.callback.CertStoreCallback;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

import java.net.URI;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.util.Collection;
import java.util.Enumeration;
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
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
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

import java.security.NoSuchAlgorithmException;


import com.sun.xml.ws.security.trust.WSTrustConstants;
import com.sun.xml.wss.RealmAuthenticationAdapter;

import com.sun.xml.wss.XWSSConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.callback.KeyStoreCallback;
import com.sun.xml.wss.impl.callback.PrivateKeyCallback;
import com.sun.xml.wss.impl.callback.SAMLCallback;
import com.sun.xml.wss.impl.callback.SAMLValidator;
import com.sun.xml.wss.impl.callback.ValidatorExtension;
import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;
import com.sun.xml.wss.util.XWSSUtil;
import java.net.URL;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;
import javax.xml.ws.BindingProvider;
import org.w3c.dom.Element;

/**
 * A sample implementation of a CallbackHandler.
 */
public class DefaultCallbackHandler implements CallbackHandler {

    public static final String KEYSTORE_URL = "keystore.url";
    public static final String KEYSTORE_TYPE = "keystore.type";
    public static final String KEYSTORE_PASSWORD = "keystore.password";
    public static final String KEY_PASSWORD = "key.password";
    public static final String MY_ALIAS = "my.alias";
    public static final String MY_USERNAME = "my.username";
    public static final String MY_PASSWORD = "my.password";
    public static final String MY_ITERATIONS = "my.iterations";
    public static final String TRUSTSTORE_URL = "truststore.url";
    public static final String TRUSTSTORE_TYPE = "truststore.type";
    public static final String TRUSTSTORE_PASSWORD = "truststore.password";
    public static final String PEER_ENTITY_ALIAS = "peerentity.alias";
    public static final String STS_ALIAS = "sts.alias";
    public static final String SERVICE_ALIAS = "service.alias";
    public static final String USERNAME_CBH = "username.callback.handler";
    public static final String PASSWORD_CBH = "password.callback.handler";
    public static final String SAML_CBH = "saml.callback.handler";
    public static final String KEYSTORE_CBH = "keystore.callback.handler";
    public static final String TRUSTSTORE_CBH = "truststore.callback.handler";
    public static final String USERNAME_VALIDATOR = "username.validator";
    public static final String SAML_VALIDATOR = "saml.validator";
    public static final String TIMESTAMP_VALIDATOR = "timestamp.validator";
    public static final String CERTIFICATE_VALIDATOR = "certificate.validator";
    public static final String MAX_CLOCK_SKEW_PROPERTY = "max.clock.skew";
    public static final String MAX_NONCE_AGE_PROPERTY = "max.nonce.age";
    public static final String TIMESTAMP_FRESHNESS_LIMIT_PROPERTY = "timestamp.freshness.limit";
    public static final String REVOCATION_ENABLED = "revocation.enabled";
    public static final String CERTSTORE_CBH = "certstore.cbh";
    public static final String CERTSTORE_CERTSELECTOR = "certstore.certselector";
    public static final String CERTSTORE_CRLSELECTOR = "certstore.crlselector";
    //this one is actually an AliasSelector for Keystore
    public static final String KEYSTORE_CERTSELECTOR = "keystore.certselector";
    public static final String TRUSTSTORE_CERTSELECTOR = "truststore.certselector";
    //A CallbackHandler for Java Message Authentication SPI for Containers
    public static final String JMAC_CALLBACK_HANDLER = "jmac.callbackhandler";
    public static final String KRB5_LOGIN_MODULE = "krb5.login.module";
    public static final String KRB5_SERVICE_PRINCIPAL = "krb5.service.principal";
    public static final String KRB5_CREDENTIAL_DELEGATION = "krb5.credential.delegation";
    public static final String USE_XWSS_CALLBACKS = "user.xwss.callbacks";
    private String keyStoreURL;
    private String keyStorePassword;
    private String keyStoreType;
    private String myAlias;
    private String keyPwd;
    private char[] keyPassword = null;
    private String trustStoreURL;
    private String trustStorePassword;
    private String trustStoreType;
    private String peerEntityAlias;
    //private String stsAlias;
    //private String serviceAlias;
    private String certStoreCBHClassName;
    private String certSelectorClassName;
    private String crlSelectorClassName;
    private String keystoreCertSelectorClassName;
    private String truststoreCertSelectorClassName;
    private String myUsername;
    private String myPassword;
    private KeyStore keyStore;
    private KeyStore trustStore;
    private Class usernameCbHandler;
    private Class passwordCbHandler;
    private Class samlCbHandler;
    private Class keystoreCbHandler;
    private Class truststoreCbHandler;
    private Class certstoreCbHandler;
    private Class certSelectorClass;
    private Class crlSelectorClass;
    private Class usernameValidator;
    private Class timestampValidator;
    private Class samlValidator;
    private Class certificateValidator;
    //private String home = null;
    protected long maxClockSkewG;
    protected long timestampFreshnessLimitG;
    protected long maxNonceAge;
    protected String revocationEnabledAttr;
    protected boolean revocationEnabled = false;
    protected String mcs = null;
    protected String tfl = null;
    protected String mna = null;
    private static Logger log = Logger.getLogger(LogDomainConstants.IMPL_MISC_DOMAIN, LogDomainConstants.IMPL_MISC_DOMAIN_BUNDLE);
    private static final String fileSeparator = System.getProperty("file.separator");
    private static final UnsupportedCallbackException unsupported =
            new UnsupportedCallbackException(null, "Unsupported Callback Type Encountered");
    private static final URI ISSUE_REQUEST_URI = URI.create(WSTrustConstants.REQUEST_SECURITY_TOKEN_ISSUE_ACTION);
    private CallbackHandler usernameHandler;
    private CallbackHandler passwordHandler;
    private CallbackHandler samlHandler;
    private CallbackHandler certstoreHandler;
    private CallbackHandler keystoreHandler;
    private CallbackHandler truststoreHandler;
    private PasswordValidationCallback.PasswordValidator pwValidator;
    private TimestampValidationCallback.TimestampValidator tsValidator;
    private CertificateValidationCallback.CertificateValidator certValidator;
    private SAMLAssertionValidator sValidator;
    private CertificateValidationCallback.CertificateValidator defaultCertValidator;
    private TimestampValidationCallback.TimestampValidator defaultTSValidator;
    //private PasswordValidationCallback.PasswordValidator defaultPWValidator;
    private RealmAuthenticationAdapter usernameAuthenticator = null;
    private RealmAuthenticationAdapter defRealmAuthenticator = null;
    private CertStore certStore = null;
    private Class keystoreCertSelectorClass;
    private Class truststoreCertSelectorClass;
    private String useXWSSCallbacksStr;
    private boolean useXWSSCallbacks;

    public DefaultCallbackHandler(String clientOrServer, Properties assertions) throws XWSSecurityException {

        Properties properties = null;
        if (assertions != null && !assertions.isEmpty()) {
            properties = assertions;
        } else {
            //fallback option
            properties = new Properties();
            String resource = clientOrServer + "-security-env.properties";
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in != null) {
                try {
                    properties.load(in);
                } catch (IOException ex) {
                    throw new XWSSecurityException(ex);
                }
            } else {
                //throw new XWSSecurityException("Resource " + resource + " could not be located in classpath");
            }
        }


        this.keyStoreURL = properties.getProperty(KEYSTORE_URL);
        this.keyStoreURL = resolveHome(this.keyStoreURL);
        this.keyStoreType = properties.getProperty(KEYSTORE_TYPE);
        this.keyStorePassword = properties.getProperty(KEYSTORE_PASSWORD);
        this.keyPwd = properties.getProperty(KEY_PASSWORD);
        this.myAlias = properties.getProperty(MY_ALIAS);
        this.myUsername = properties.getProperty(MY_USERNAME);
        this.myPassword = properties.getProperty(MY_PASSWORD);


        this.trustStoreURL = properties.getProperty(TRUSTSTORE_URL);
        this.trustStoreURL = resolveHome(this.trustStoreURL);
        this.keyStoreType = properties.getProperty(KEYSTORE_TYPE);
        this.trustStoreType = properties.getProperty(TRUSTSTORE_TYPE);
        this.trustStorePassword = properties.getProperty(TRUSTSTORE_PASSWORD);
        this.peerEntityAlias = properties.getProperty(PEER_ENTITY_ALIAS);
        //this.stsAlias =  properties.getProperty(STS_ALIAS);
        //this.serviceAlias = properties.getProperty(SERVICE_ALIAS);

        this.certStoreCBHClassName = properties.getProperty(CERTSTORE_CBH);
        this.certSelectorClassName = properties.getProperty(CERTSTORE_CERTSELECTOR);
        this.crlSelectorClassName = properties.getProperty(CERTSTORE_CRLSELECTOR);

        this.keystoreCertSelectorClassName = properties.getProperty(KEYSTORE_CERTSELECTOR);
        this.truststoreCertSelectorClassName = properties.getProperty(TRUSTSTORE_CERTSELECTOR);

        String uCBH = properties.getProperty(USERNAME_CBH);
        String pCBH = properties.getProperty(PASSWORD_CBH);
        String sCBH = properties.getProperty(SAML_CBH);
        String keystoreCBH = properties.getProperty(KEYSTORE_CBH);
        String truststoreCBH = properties.getProperty(TRUSTSTORE_CBH);
        String uV = properties.getProperty(USERNAME_VALIDATOR);
        String sV = properties.getProperty(SAML_VALIDATOR);
        String tV = properties.getProperty(TIMESTAMP_VALIDATOR);
        String cV = properties.getProperty(CERTIFICATE_VALIDATOR);

        usernameCbHandler = loadClass(uCBH);
        passwordCbHandler = loadClass(pCBH);
        samlCbHandler = loadClass(sCBH);
        keystoreCbHandler = loadClass(keystoreCBH);
        truststoreCbHandler = loadClass(truststoreCBH);

        usernameValidator = loadClass(uV);
        samlValidator = loadClass(sV);
        timestampValidator = loadClass(tV);
        certificateValidator = loadClass(cV);

        keystoreCertSelectorClass = loadClass(this.keystoreCertSelectorClassName);
        truststoreCertSelectorClass = loadClass(this.truststoreCertSelectorClassName);

        this.certstoreCbHandler = loadClass(this.certStoreCBHClassName);
        this.certSelectorClass = loadClass(this.certSelectorClassName);
        this.crlSelectorClass = loadClass(this.crlSelectorClassName);


        mcs = properties.getProperty(MAX_CLOCK_SKEW_PROPERTY);
        tfl = properties.getProperty(TIMESTAMP_FRESHNESS_LIMIT_PROPERTY);
        mna = properties.getProperty(MAX_NONCE_AGE_PROPERTY);
        revocationEnabledAttr = properties.getProperty(REVOCATION_ENABLED);
        if (revocationEnabledAttr != null) {
            this.revocationEnabled = Boolean.parseBoolean(revocationEnabledAttr);
        }

        useXWSSCallbacksStr = properties.getProperty(USE_XWSS_CALLBACKS);
        if (useXWSSCallbacksStr != null) {
            this.useXWSSCallbacks = Boolean.parseBoolean(useXWSSCallbacksStr);
        }
        maxClockSkewG = toLong(mcs);
        timestampFreshnessLimitG = toLong(tfl);
        maxNonceAge = toLong(mna);

        initTrustStore();
        initKeyStore();
        initNewInstances();

        defaultCertValidator = new X509CertificateValidatorImpl();
        defaultTSValidator = new DefaultTimestampValidator();
    }

    public DefaultCallbackHandler(
            String clientOrServer, Properties assertions, RealmAuthenticationAdapter adapter) throws Exception {
        this(clientOrServer, assertions);
        usernameAuthenticator = adapter;
        if (adapter == null) {
            defRealmAuthenticator = RealmAuthenticationAdapter.newInstance(null);
        }
    }

    /**
     *
     * @param cb
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    private void handleUsernameCallback(UsernameCallback cb) throws IOException, UnsupportedCallbackException {
        if (myUsername != null) {
            cb.setUsername(myUsername);
        } else {
            String username = (String) cb.getRuntimeProperties().get(com.sun.xml.wss.XWSSConstants.USERNAME_PROPERTY);
            if (username == null) {
                //the property  below is dedprecated for xwss usage
                username = (String) cb.getRuntimeProperties().get(BindingProvider.USERNAME_PROPERTY);
            }
            if (username != null) {
                cb.setUsername(username);
            } else if (usernameHandler != null) {
                Callback[] cbs = null;
                if (useXWSSCallbacks) {
                    cbs = new Callback[]{cb};
                    usernameHandler.handle(cbs);
                } else {
                    javax.security.auth.callback.NameCallback nc = new javax.security.auth.callback.NameCallback("Username=");
                    cbs = new Callback[]{nc};
                    usernameHandler.handle(cbs);
                    cb.setUsername(((javax.security.auth.callback.NameCallback) cbs[0]).getName());
                }

            } else {
                log.log(Level.SEVERE, "WSS1500.invalid.usernameHandler");
                throw new UnsupportedCallbackException(null, "Username Handler Not Configured");
            }
        }
    }

    /**
     *
     * @param cb
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    private void handlePasswordCallback(PasswordCallback cb) throws IOException, UnsupportedCallbackException {
        if (myPassword != null) {
            cb.setPassword(myPassword);
        } else {
            String password = (String) cb.getRuntimeProperties().get(com.sun.xml.wss.XWSSConstants.PASSWORD_PROPERTY);
            if (password == null) {
                //the property below is deprecated
                password = (String) cb.getRuntimeProperties().get(BindingProvider.PASSWORD_PROPERTY);
            }
            if (password != null) {
                cb.setPassword(password);
            } else if (passwordHandler != null) {
                Callback[] cbs = null;
                if (this.useXWSSCallbacks) {
                    cbs = new Callback[]{cb};
                    passwordHandler.handle(cbs);
                } else {
                    javax.security.auth.callback.PasswordCallback pc = new javax.security.auth.callback.PasswordCallback("Password=", false);
                    cbs = new Callback[]{pc};
                    passwordHandler.handle(cbs);
                    char[] pass = ((javax.security.auth.callback.PasswordCallback) cbs[0]).getPassword();
                    cb.setPassword(new String(pass));
                }

            } else {
                log.log(Level.SEVERE, "WSS1525.invalid.passwordHandler");
                throw new UnsupportedCallbackException(null, "Password Handler Not Configured");
            }
        }
    }

    /**
     *
     * @param cb
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    private void handlePasswordValidation(PasswordValidationCallback cb) throws IOException, UnsupportedCallbackException {
        if (cb.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
            if (pwValidator != null) {
                cb.setValidator(pwValidator);
            } else {
                if (usernameAuthenticator != null) {
                    cb.setRealmAuthentcationAdapter(usernameAuthenticator);
                } else {
                    cb.setRealmAuthentcationAdapter(defRealmAuthenticator);
                }
            }
        } else if (cb.getRequest() instanceof PasswordValidationCallback.DigestPasswordRequest) {
            PasswordValidationCallback.DigestPasswordRequest request =
                    (PasswordValidationCallback.DigestPasswordRequest) cb.getRequest();
            if (pwValidator != null && pwValidator instanceof PasswordValidationCallback.WsitDigestPasswordValidator) {
                ((PasswordValidationCallback.WsitDigestPasswordValidator) pwValidator).setPassword(request);
                cb.setValidator(pwValidator);
            }
        } else if (cb.getRequest() instanceof PasswordValidationCallback.DerivedKeyPasswordRequest) {
            PasswordValidationCallback.DerivedKeyPasswordRequest request =
                    (PasswordValidationCallback.DerivedKeyPasswordRequest) cb.getRequest();
            if (pwValidator != null && pwValidator instanceof PasswordValidationCallback.DerivedKeyPasswordValidator) {
                ((PasswordValidationCallback.DerivedKeyPasswordValidator) pwValidator).setPassword(request);
                cb.setValidator(pwValidator);
            }
        } else {
            log.log(Level.SEVERE, "WSS1503.unsupported.requesttype");
            throw new UnsupportedCallbackException(null, "Unsupported Request Type for Password Validation");
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

    /**
     *
     * @param callbacks
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof UsernameCallback) {
                UsernameCallback cb = (UsernameCallback) callbacks[i];
                handleUsernameCallback(cb);

            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback cb = (PasswordCallback) callbacks[i];
                handlePasswordCallback(cb);

            } else if (callbacks[i] instanceof PasswordValidationCallback) {
                PasswordValidationCallback cb = (PasswordValidationCallback) callbacks[i];
                handlePasswordValidation(cb);

            } else if (callbacks[i] instanceof TimestampValidationCallback) {
                TimestampValidationCallback cb = (TimestampValidationCallback) callbacks[i];
                handleTimestampValidation(cb);

            } else if (callbacks[i] instanceof SignatureVerificationKeyCallback) {

                SignatureVerificationKeyCallback cb = (SignatureVerificationKeyCallback) callbacks[i];

                if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    // subject keyid request
                    SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                            (SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    X509Certificate cert =
                            getCertificateFromTrustStore(
                            request.getSubjectKeyIdentifier(), cb.getRuntimeProperties());
                    request.setX509Certificate(cert);

                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) {
                    // issuer serial request
                    SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest request =
                            (SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    X509Certificate cert =
                            getCertificateFromTrustStore(
                            request.getIssuerName(),
                            request.getSerialNumber(), cb.getRuntimeProperties());
                    request.setX509Certificate(cert);

                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.ThumbprintBasedRequest) {
                    SignatureVerificationKeyCallback.ThumbprintBasedRequest request =
                            (SignatureVerificationKeyCallback.ThumbprintBasedRequest) cb.getRequest();
                    X509Certificate cert =
                            getCertificateFromTrustStoreForThumbprint(
                            request.getThumbprintIdentifier(), cb.getRuntimeProperties());
                    request.setX509Certificate(cert);

                } else if (cb.getRequest() instanceof SignatureVerificationKeyCallback.PublicKeyBasedRequest) {
                    SignatureVerificationKeyCallback.PublicKeyBasedRequest request =
                            (SignatureVerificationKeyCallback.PublicKeyBasedRequest) cb.getRequest();
                    X509Certificate cert =
                            getCertificateFromTrustStoreForSAML(request.getPublicKey(), cb.getRuntimeProperties());
                    request.setX509Certificate(cert);
                } else {
                    log.log(Level.SEVERE, "WSS1504.unsupported.callbackType");
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof SignatureKeyCallback) {
                SignatureKeyCallback cb = (SignatureKeyCallback) callbacks[i];

                if (cb.getRequest() instanceof SignatureKeyCallback.DefaultPrivKeyCertRequest) {
                    // default priv key cert req
                    SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                            (SignatureKeyCallback.DefaultPrivKeyCertRequest) cb.getRequest();
                    getDefaultPrivKeyCert(request, cb.getRuntimeProperties());

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
                                //(PrivateKey) keyStore.getKey(alias, this.keyPassword);
                                getPrivateKey(cb.getRuntimeProperties(), alias);
                        request.setPrivateKey(privKey);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
                        throw new RuntimeException(e);
                    }

                } else {
                    log.log(Level.SEVERE, "WSS1504.unsupported.callbackType");
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof DecryptionKeyCallback) {
                DecryptionKeyCallback cb = (DecryptionKeyCallback) callbacks[i];

                if (cb.getRequest() instanceof DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest) {
                    DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest request =
                            (DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest) cb.getRequest();
                    byte[] ski = request.getSubjectKeyIdentifier();
                    PrivateKey privKey = getPrivateKey(ski, cb.getRuntimeProperties());
                    request.setPrivateKey(privKey);

                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509IssuerSerialBasedRequest) {
                    DecryptionKeyCallback.X509IssuerSerialBasedRequest request =
                            (DecryptionKeyCallback.X509IssuerSerialBasedRequest) cb.getRequest();
                    String issuerName = request.getIssuerName();
                    BigInteger serialNumber = request.getSerialNumber();
                    PrivateKey privKey = getPrivateKey(issuerName, serialNumber, cb.getRuntimeProperties());
                    request.setPrivateKey(privKey);

                } else if (cb.getRequest() instanceof DecryptionKeyCallback.X509CertificateBasedRequest) {
                    DecryptionKeyCallback.X509CertificateBasedRequest request =
                            (DecryptionKeyCallback.X509CertificateBasedRequest) cb.getRequest();
                    X509Certificate cert = request.getX509Certificate();
                    PrivateKey privKey = getPrivateKey(cert, cb.getRuntimeProperties());
                    request.setPrivateKey(privKey);

                } else if (cb.getRequest() instanceof DecryptionKeyCallback.ThumbprintBasedRequest) {
                    DecryptionKeyCallback.ThumbprintBasedRequest request =
                            (DecryptionKeyCallback.ThumbprintBasedRequest) cb.getRequest();
                    byte[] ski = request.getThumbprintIdentifier();
                    PrivateKey privKey = getPrivateKeyForThumbprint(ski, cb.getRuntimeProperties());
                    request.setPrivateKey(privKey);
                } else if (cb.getRequest() instanceof DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest) {
                    DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest request =
                            (DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest) cb.getRequest();

                    PrivateKey privKey = getPrivateKeyFromKeyStore(request.getPublicKey(), cb.getRuntimeProperties());
                    request.setPrivateKey(privKey);
                } else {
                    log.log(Level.SEVERE, "WSS1504.unsupported.callbackType");
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof EncryptionKeyCallback) {
                EncryptionKeyCallback cb = (EncryptionKeyCallback) callbacks[i];

                if (cb.getRequest() instanceof EncryptionKeyCallback.AliasX509CertificateRequest) {
                    EncryptionKeyCallback.AliasX509CertificateRequest request =
                            (EncryptionKeyCallback.AliasX509CertificateRequest) cb.getRequest();

                    String alias = request.getAlias();
                    if ("".equals(alias) || (alias == null)) {
                        getDefaultCertificateFromTrustStore(cb.getRuntimeProperties(), request);
                    } else {
                        try {
                            KeyStore tStore = this.getTrustStore(cb.getRuntimeProperties());
                            if (tStore != null) {
                                X509Certificate cert =
                                        (X509Certificate) tStore.getCertificate(alias);
                                request.setX509Certificate(cert);
                            }
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", e);
                            throw new RuntimeException(e);
                        }
                    }

                } else if (cb.getRequest() instanceof EncryptionKeyCallback.PublicKeyBasedRequest) {
                    EncryptionKeyCallback.PublicKeyBasedRequest request =
                            (EncryptionKeyCallback.PublicKeyBasedRequest) cb.getRequest();
                    try {
                        X509Certificate cert =
                                getCertificateFromTrustStoreForSAML(request.getPublicKey(), cb.getRuntimeProperties());
                        request.setX509Certificate(cert);
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", e);
                        throw new RuntimeException(e);
                    }
                } else if (cb.getRequest() instanceof EncryptionKeyCallback.AliasSymmetricKeyRequest) {
                    log.log(Level.SEVERE, "WSS1504.unsupported.callbackType");
                    throw unsupported;
                }

            } else if (callbacks[i] instanceof CertificateValidationCallback) {
                CertificateValidationCallback cb = (CertificateValidationCallback) callbacks[i];
                getTrustStore(cb.getRuntimeProperties());
                cb.setValidator(certValidator);


            } else if (callbacks[i] instanceof DynamicPolicyCallback) {
                DynamicPolicyCallback dp = (DynamicPolicyCallback) callbacks[i];
                SecurityPolicy policy = dp.getSecurityPolicy();
                if (policy instanceof AuthenticationTokenPolicy.SAMLAssertionBinding) {
                    AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding =
                            (AuthenticationTokenPolicy.SAMLAssertionBinding) ((AuthenticationTokenPolicy.SAMLAssertionBinding) policy).clone();

                    if ((samlBinding.getAssertion() == null) && (samlBinding.getAuthorityBinding() == null) && (samlBinding.getAssertionReader() == null)) {
                        populateAssertion(samlBinding, dp);
                    } else if (samlBinding.getAssertion() != null || samlBinding.getAssertionReader() != null) {
                        Subject subj =
                                (Subject) dp.getRuntimeProperties().get(MessageConstants.AUTH_SUBJECT);
                        validateSAMLAssertion(samlBinding, subj, dp.getRuntimeProperties());
                    } else if ((samlBinding.getAuthorityBinding() != null) && (samlBinding.getAssertionId() != null)) {
                        locateSAMLAssertion(samlBinding, dp.getRuntimeProperties());
                    } else {
                        log.log(Level.SEVERE, "WSS1506.invalid.SAMLPolicy");
                        throw new UnsupportedCallbackException(null, "SAML Assertion not present in the Policy");
                    }
                }
            } else {
                log.log(Level.SEVERE, "WSS1504.unsupported.callbackType");
                throw unsupported;
            }
        }
    }

    /**
     *
     * @param certificate
     * @param runtimeProps
     * @return
     */
    private boolean isMyCert(X509Certificate certificate, Map runtimeProps) {
        try {
            SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                    new SignatureKeyCallback.DefaultPrivKeyCertRequest();
            getDefaultPrivKeyCert(request, runtimeProps);
            X509Certificate cert = request.getX509Certificate();
            if (cert != null && cert.equals(certificate)) {
                return true;
            }
        } catch (IOException ex) {
            //ignore
        }
        return false;
    }

    /**
     *
     * @param samlBinding
     * @param dp
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    private void populateAssertion(AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding, DynamicPolicyCallback dp)
            throws IOException, UnsupportedCallbackException {

        if (AuthenticationTokenPolicy.SAMLAssertionBinding.SV_ASSERTION.equals(samlBinding.getAssertionType())) {

            if (samlHandler != null) {

                SAMLCallback sc = new SAMLCallback();
                SecurityUtil.copy(sc.getRuntimeProperties(), dp.getRuntimeProperties());
                sc.setConfirmationMethod(SAMLCallback.SV_ASSERTION_TYPE);
                sc.setSAMLVersion(samlBinding.getSAMLVersion());
                Callback[] cbs = new Callback[]{sc};
                samlHandler.handle(cbs);
                samlBinding.setAssertion(sc.getAssertionElement());
                samlBinding.setAuthorityBinding(sc.getAuthorityBindingElement());
                dp.setSecurityPolicy(samlBinding);
                samlBinding.setAssertionId(sc.getAssertionId());
            } else {
                log.log(Level.SEVERE, "WSS1507.no.SAMLCallbackHandler");
                throw new UnsupportedCallbackException(null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion");
            }

        } else {

            if (samlHandler != null) {

                SAMLCallback sc = new SAMLCallback();
                SecurityUtil.copy(sc.getRuntimeProperties(), dp.getRuntimeProperties());
                sc.setConfirmationMethod(SAMLCallback.HOK_ASSERTION_TYPE);
                sc.setSAMLVersion(samlBinding.getSAMLVersion());
                Callback[] cbs = new Callback[]{sc};
                samlHandler.handle(cbs);
                samlBinding.setAssertion(sc.getAssertionElement());
                samlBinding.setAuthorityBinding(sc.getAuthorityBindingElement());
                samlBinding.setAssertionId(sc.getAssertionId());
                dp.setSecurityPolicy(samlBinding);
                PrivateKeyBinding pkBinding = (PrivateKeyBinding) samlBinding.newPrivateKeyBinding();

                SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                        new SignatureKeyCallback.DefaultPrivKeyCertRequest();
                getDefaultPrivKeyCert(request, dp.getRuntimeProperties());
                pkBinding.setPrivateKey(request.getPrivateKey());

            } else {
                log.log(Level.SEVERE, "WSS1507.no.SAMLCallbackHandler");
                throw new UnsupportedCallbackException(null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion");
            }
        }
    }

    /**
     *
     * @param samlBinding
     * @param subj
     * @param props
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    private void validateSAMLAssertion(
            AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding, Subject subj, Map props)
            throws IOException, UnsupportedCallbackException {
        if (sValidator != null) {
            try {
                if (sValidator instanceof ValidatorExtension) {
                    ((ValidatorExtension) sValidator).setRuntimeProperties(props);
                }
                if (samlBinding.getAssertion() != null) {
                    if (sValidator instanceof SAMLValidator) {
                        ((SAMLValidator) sValidator).validate(samlBinding.getAssertion(), props, subj);
                    } else {
                        sValidator.validate(samlBinding.getAssertion());
                    }
                } else if (samlBinding.getAssertionReader() != null) {
                    if (sValidator instanceof SAMLValidator) {
                        ((SAMLValidator) sValidator).validate(samlBinding.getAssertionReader(), props, subj);
                    } else {
                        sValidator.validate(samlBinding.getAssertionReader());
                    }
                }
            } catch (SAMLAssertionValidator.SAMLValidationException e) {
                log.log(Level.SEVERE, "WSS1508.failed.validateSAMLAssertion", e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 
     * @param samlBinding
     * @param context
     * @throws java.io.IOException
     * @throws javax.security.auth.callback.UnsupportedCallbackException
     */
    private void locateSAMLAssertion(AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding, Map context)
            throws IOException, UnsupportedCallbackException {

        Element binding = samlBinding.getAuthorityBinding();
        String assertionId = samlBinding.getAssertionId();
        // use the above information to locate the assertion
        // this simple impl will just set the assertion
        if (AuthenticationTokenPolicy.SAMLAssertionBinding.SV_ASSERTION.equals(samlBinding.getAssertionType())) {
            if (samlHandler != null) {
                SAMLCallback sc = new SAMLCallback();
                sc.setConfirmationMethod(SAMLCallback.SV_ASSERTION_TYPE);
                sc.setSAMLVersion(samlBinding.getSAMLVersion());
                sc.setAssertionId(assertionId);
                sc.setAuthorityBindingElement(binding);
                Callback[] cbs = new Callback[]{sc};
                samlHandler.handle(cbs);
                samlBinding.setAssertion(sc.getAssertionElement());
            } else {
                log.log(Level.SEVERE, "WSS1507.no.SAMLCallbackHandler");
                throw new UnsupportedCallbackException(null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion");
            }

        } else {

            if (samlHandler != null) {
                SAMLCallback sc = new SAMLCallback();
                sc.setConfirmationMethod(SAMLCallback.HOK_ASSERTION_TYPE);
                sc.setSAMLVersion(samlBinding.getSAMLVersion());
                sc.setAssertionId(assertionId);
                sc.setAuthorityBindingElement(binding);
                Callback[] cbs = new Callback[]{sc};
                samlHandler.handle(cbs);
                samlBinding.setAssertion(sc.getAssertionElement());
                PrivateKeyBinding pkBinding = (PrivateKeyBinding) samlBinding.newPrivateKeyBinding();

                SignatureKeyCallback.DefaultPrivKeyCertRequest request =
                        new SignatureKeyCallback.DefaultPrivKeyCertRequest();
                getDefaultPrivKeyCert(request, context);
                pkBinding.setPrivateKey(request.getPrivateKey());

            } else {
                log.log(Level.SEVERE, "WSS1507.no.SAMLCallbackHandler");
                throw new UnsupportedCallbackException(null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion");
            }
        }
    }

    /**
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private void initTrustStore() throws XWSSecurityException {
        try {

            if (trustStoreURL == null) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for TrustStore URL");
                }
                return;
            }
            if (this.trustStorePassword == null) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for TrustStore Password");
                }
            }

            char[] trustStorePasswordChars = null;
            //check here if trustStorePassword is a CBH className
            Class cbh = this.loadClassSilent(trustStorePassword);
            if (cbh != null) {
                CallbackHandler hdlr = (CallbackHandler) cbh.newInstance();
                javax.security.auth.callback.PasswordCallback pc =
                        new javax.security.auth.callback.PasswordCallback("TrustStorePassword", false);
                Callback[] cbs = new Callback[]{pc};
                hdlr.handle(cbs);
                trustStorePasswordChars = ((javax.security.auth.callback.PasswordCallback) cbs[0]).getPassword();
            } else {
                //the user supplied value is a Password for the truststore
                trustStorePasswordChars = trustStorePassword.toCharArray();
            }

            trustStore = KeyStore.getInstance(trustStoreType);
            InputStream is = null;
            URL tURL = SecurityUtil.loadFromClasspath("META-INF/" + trustStoreURL);

            try {
                if (tURL != null) {
                    is = tURL.openStream();
                } else {
                    is = new FileInputStream(trustStoreURL);
                }
                trustStore.load(is, trustStorePasswordChars);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1509.failed.init.truststore", e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @throws com.sun.xml.wss.XWSSecurityException
     */
    private void initKeyStore() throws XWSSecurityException {
        try {
            if (keyStoreURL == null) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for KeyStore URL");
                }
                return;
            }

            if (keyStorePassword == null) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for KeyStore PASSWORD");
                }
                return;
            }

            char[] keyStorePasswordChars = null;
            //check here if keyStorePassword is a CBH className
            Class cbh = this.loadClassSilent(keyStorePassword);
            if (cbh != null) {
                CallbackHandler hdlr = (CallbackHandler) cbh.newInstance();
                javax.security.auth.callback.PasswordCallback pc =
                        new javax.security.auth.callback.PasswordCallback("KeyStorePassword", false);
                Callback[] cbs = new Callback[]{pc};
                hdlr.handle(cbs);
                keyStorePasswordChars = ((javax.security.auth.callback.PasswordCallback) cbs[0]).getPassword();
            } else {
                //the user supplied value is a Password for the keystore
                keyStorePasswordChars = keyStorePassword.toCharArray();
            }

            //now initialize KeyPassword if any ?
            if (this.keyPwd == null) {
                this.keyPassword = keyStorePasswordChars;
            } else {
                initKeyPassword();
            }

            keyStore = KeyStore.getInstance(keyStoreType);
            InputStream is = null;
            URL kURL = SecurityUtil.loadFromClasspath("META-INF/" + keyStoreURL);
            try {
                if (kURL != null) {
                    is = kURL.openStream();
                } else {
                    is = new FileInputStream(keyStoreURL);
                }
                keyStore.load(is, keyStorePasswordChars);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1510.failed.init.keystore", e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param ski
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    private X509Certificate getCertificateFromTrustStore(byte[] ski, Map runtimeProps)
            throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, ski, MessageConstants.KEY_INDETIFIER_TYPE);
                if (cert != null) {
                    return cert;
                }
            }
            if (getTrustStore(runtimeProps) == null && getCertStore(runtimeProps) == null) {
                return null;
            }
            if (trustStore != null) {
                Enumeration aliases = trustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    Certificate cert = trustStore.getCertificate(alias);
                    if (cert == null || !"X.509".equals(cert.getType())) {
                        continue;
                    }
                    X509Certificate x509Cert = (X509Certificate) cert;
                    byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                    if (keyId == null) {
                        // Cert does not contain a key identifier
                        continue;
                    }
                    if (Arrays.equals(ski, keyId)) {
                        return x509Cert;
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", e);
            throw new RuntimeException(e);
        }
        //now search in CertStore if present
        if (this.certStore != null) {
            CertSelector selector = null;
            /* if (this.certSelectorClass != null) {
            HashMap props = new HashMap();
            props.putAll(runtimeProps);
            props.put(XWSSConstants.SUBJECTKEYIDENTIFIER, ski);
            selector = XWSSUtil.getCertSelector(certSelectorClass, props);
            }*/
            if (selector == null) {
                selector = new KeyIdentifierCertSelector(ski);
            }
            Collection certs = null;
            try {
                certs = certStore.getCertificates(selector);
            } catch (CertStoreException ex) {
                log.log(Level.SEVERE, "WSS1530.exception.in.certstore.lookup", ex);
                throw new RuntimeException(ex);
            }
            if (certs.size() > 0) {
                return (X509Certificate) certs.iterator().next();
            }
        }
        return null;
    }

    /**
     *
     * @param issuerName
     * @param serialNumber
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    private X509Certificate getCertificateFromTrustStore(
            String issuerName,
            BigInteger serialNumber, Map runtimeProps)
            throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, serialNumber, issuerName);
                if (cert != null) {
                    return cert;
                }
            }

            if (getTrustStore(runtimeProps) == null && getCertStore(runtimeProps) == null) {
                return null;
            }
            //now search in CertStore if present
            if (this.certStore != null) {
                CertSelector selector = null;
                /*if (this.certSelectorClass != null) {
                Map props = new HashMap();
                props.putAll(runtimeProps);
                props.put(XWSSConstants.ISSUERNAME, issuerName);
                props.put(XWSSConstants.ISSUERSERIAL, serialNumber);
                selector = XWSSUtil.getCertSelector(certSelectorClass, props);
                }*/
                if (selector == null) {
                    selector = new IssuerNameAndSerialCertSelector(serialNumber, issuerName);
                }
                Collection certs = null;
                try {
                    certs = certStore.getCertificates(selector);
                } catch (CertStoreException ex) {
                    log.log(Level.SEVERE, "WSS1530.exception.in.certstore.lookup", ex);
                    throw new RuntimeException(ex);
                }
                if (certs.size() > 0) {
                    return (X509Certificate) certs.iterator().next();
                }
            }
            if (trustStore != null) {
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
                    if (thisIssuerName.equals(issuerName)
                            && thisSerialNumber.equals(serialNumber)) {
                        return x509Cert;
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", e);
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     *
     * @param ski
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    public PrivateKey getPrivateKey(byte[] ski, Map runtimeProps) throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, ski, MessageConstants.KEY_INDETIFIER_TYPE);
                if (cert != null) {
                    return XWSSUtil.getProgrammaticPrivateKey(runtimeProps);
                }
            }
            if (getKeyStore(runtimeProps) == null) {
                return null;
            }
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    // Asuumed key password same as the keystore password
                    //return (PrivateKey) keyStore.getKey(alias, this.keyPassword);
                    return getPrivateKey(runtimeProps, alias);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     *
     * @param issuerName
     * @param serialNumber
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    public PrivateKey getPrivateKey(
            String issuerName,
            BigInteger serialNumber, Map runtimeProps)
            throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, serialNumber, issuerName);
                if (cert != null) {
                    return XWSSUtil.getProgrammaticPrivateKey(runtimeProps);
                }
            }
            if (getKeyStore(runtimeProps) == null) {
                return null;
            }
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                        RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName)
                        && thisSerialNumber.equals(serialNumber)) {
                    //return (PrivateKey) keyStore.getKey(alias, this.keyPassword);
                    return getPrivateKey(runtimeProps, alias);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     *
     * @param certificate
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    public PrivateKey getPrivateKey(X509Certificate certificate, Map runtimeProps)
            throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.CERTIFICATE_PROPERTY);
                if (obj != null && obj.equals(certificate)) {
                    return XWSSUtil.getProgrammaticPrivateKey(runtimeProps);
                }
            }
            if (getKeyStore(runtimeProps) == null) {
                return null;
            }
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null && cert.equals(certificate)) {
                    //return (PrivateKey) keyStore.getKey(alias, this.keyPassword);
                    return getPrivateKey(runtimeProps, alias);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     *
     * @param context
     * @param req
     * @throws java.io.IOException
     */
    private void getDefaultCertificateFromTrustStore(Map context,
            EncryptionKeyCallback.AliasX509CertificateRequest req) throws IOException {

        String currentAlias = null;
        //now try the SERVER_CERTIFICATE_PROPERTY
        Object obj = context.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
        if (obj instanceof X509Certificate) {
            req.setX509Certificate((X509Certificate) obj);
            return;
        }
        if (peerEntityAlias != null) {
            currentAlias = peerEntityAlias;
        } else {
            //try to locate in certstore using user supplied CertSelector
            getCertStore(context);
            if (certStore != null) {
                CertSelector selector = null;
                if (this.certSelectorClass != null) {
                    selector = XWSSUtil.getCertSelector(certSelectorClass, context);
                }
                if (selector != null) {
                    Collection certs = null;
                    try {
                        certs = certStore.getCertificates(selector);
                    } catch (CertStoreException ex) {
                        log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", ex);
                        throw new RuntimeException(ex);
                    }
                    if (certs.size() > 0) {
                        req.setX509Certificate((X509Certificate) certs.iterator().next());
                        return;
                    }
                }
            }

            if (getTrustStore(context) != null) {
                if (this.truststoreCertSelectorClass != null) {
                    CertSelector selector = XWSSUtil.getCertSelector(this.truststoreCertSelectorClass, context);
                    if (selector != null) {
                        Enumeration aliases = null;
                        try {
                            aliases = trustStore.aliases();
                        } catch (KeyStoreException ex) {
                            log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", ex);
                            throw new RuntimeException(ex);
                        }
                        while (aliases.hasMoreElements()) {
                            String currAlias = (String) aliases.nextElement();
                            Certificate thisCertificate = null;
                            try {
                                thisCertificate = trustStore.getCertificate(currAlias);
                            } catch (KeyStoreException ex) {
                                log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", ex);
                                throw new RuntimeException(ex);
                            }
                            if ((thisCertificate instanceof X509Certificate) && selector.match(thisCertificate)) {
                                req.setX509Certificate((X509Certificate) thisCertificate);
                                return;
                            }
                        }
                    }

                } else {
                    //now try dynamic certificate
                    //TODO : this code should be outside the if(getTruststore())
                    X509Certificate cert =
                            getDynamicCertificate(context);
                    if (cert != null) {
                        req.setX509Certificate(cert);
                        return;
                    }
                    //TODO: remove this code below, this code is not correct anyway
                    Enumeration aliases = null;
                    try {
                        aliases = trustStore.aliases();
                    } catch (KeyStoreException ex) {
                        log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", ex);
                        throw new RuntimeException(ex);
                    }
                    while (aliases.hasMoreElements()) {
                        currentAlias = (String) aliases.nextElement();
                        if (!"certificate-authority".equals(currentAlias) && !"root".equals(currentAlias)) {
                            log.log(Level.WARNING, "truststore peeralias not found,picking up the arbitrary certificate ");
                            break;
                        } else {
                            currentAlias = null;
                        }
                    }
                }
            }
        }

        if (getTrustStore(context) != null && currentAlias != null) {
            X509Certificate thisCertificate = null;
            try {
                thisCertificate = (X509Certificate) trustStore.getCertificate(currentAlias);
            } catch (KeyStoreException ex) {
                log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", ex);
                throw new RuntimeException(ex);
            }
            req.setX509Certificate(thisCertificate);
            return;
        } else {
            log.log(Level.SEVERE, "WSS1511.failed.locate.peerCertificate");
            throw new RuntimeException("An Error occurred while locating PEER Entity certificate in TrustStore");
        }
    }

    /**
     * 
     * @param request
     * @param context
     * @throws java.io.IOException
     */
    private void getDefaultPrivKeyCert(
            SignatureKeyCallback.DefaultPrivKeyCertRequest request, Map context)
            throws IOException {

        Object obj = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
        boolean foundCert = false;
        if (obj instanceof X509Certificate) {
            request.setX509Certificate((X509Certificate) obj);
            foundCert = true;
        }
        obj = context.get(XWSSConstants.PRIVATEKEY_PROPERTY);
        if (obj instanceof PrivateKey) {
            request.setPrivateKey((PrivateKey) obj);
            if (foundCert) {
                return;
            }
        }

        getKeyStore(context);
        String uniqueAlias = null;
        try {
            if (this.myAlias != null) {
                uniqueAlias = this.myAlias;
            } else {
                //if Keystore CertSelector Provided use it
                //It is actually an AliasSelector for the sake of uniformity with
                // JSR 196 Callbacks. JSR 196 Callbacks do not allow browsing the
                // Keystore (although they allow browsing TrustStore)
                if (this.keystoreCertSelectorClass != null) {
                    AliasSelector selector = null;
                    try {
                        selector = (AliasSelector) this.keystoreCertSelectorClass.newInstance();
                    } catch (IllegalAccessException ex) {
                        log.log(Level.SEVERE, "WSS1532.exception.instantiating.aliasselector", ex);
                        throw new RuntimeException(ex);
                    } catch (InstantiationException ex) {
                        log.log(Level.SEVERE, "WSS1532.exception.instantiating.aliasselector", ex);
                        throw new RuntimeException(ex);
                    }
                    uniqueAlias = selector.select(context);
                } /*else {*/
                // if alias selector fails, select a unique private key entry if one exists
                if (uniqueAlias == null) {
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
            }
            if (uniqueAlias != null) {
                //System.out.println("Signing Key Alias=" + uniqueAlias);
                request.setX509Certificate(
                        (X509Certificate) keyStore.getCertificate(uniqueAlias));
                request.setPrivateKey(
                        //(PrivateKey) keyStore.getKey(uniqueAlias, this.keyPassword));
                        getPrivateKey(context, uniqueAlias));
            } else {

                log.log(Level.SEVERE, "WSS1512.failed.locate.certificate.privatekey");
                throw new RuntimeException("An Error occurred while locating default certificate and privateKey in KeyStore");

            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    private class DefaultTimestampValidator implements TimestampValidationCallback.TimestampValidator {

        public void validate(TimestampValidationCallback.Request request)
                throws TimestampValidationCallback.TimestampValidationException {


            // validate timestamp creation and expiration time.
            TimestampValidationCallback.UTCTimestampRequest utcTimestampRequest =
                    (TimestampValidationCallback.UTCTimestampRequest) request;


            SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
            Date created = null;
            Date expired = null;


            try {
                created = calendarFormatter1.parse(utcTimestampRequest.getCreated());
                if (utcTimestampRequest.getExpired() != null) {
                    expired = calendarFormatter1.parse(utcTimestampRequest.getExpired());
                }
            } catch (java.text.ParseException pe) {
                try {
                    created = calendarFormatter2.parse(utcTimestampRequest.getCreated());
                    if (utcTimestampRequest.getExpired() != null) {
                        expired = calendarFormatter2.parse(utcTimestampRequest.getExpired());
                    }
                } catch (java.text.ParseException ipe) {
                    log.log(Level.SEVERE, "WSS1513.exception.validate.timestamp", ipe);
                    throw new TimestampValidationCallback.TimestampValidationException(ipe.getMessage());
                }
            }

            long maxClockSkewLocal = utcTimestampRequest.getMaxClockSkew();
            if (mcs != null && maxClockSkewG >= 0) {
                maxClockSkewLocal = maxClockSkewG;
            }
            long tsfLocal = utcTimestampRequest.getTimestampFreshnessLimit();
            if (tfl != null && timestampFreshnessLimitG > 0) {
                tsfLocal = timestampFreshnessLimitG;
            }

            // validate creation time
            validateCreationTime(created, maxClockSkewLocal, tsfLocal);

            // validate expiration time
            if (expired != null) {
                validateExpirationTime(expired, maxClockSkewLocal, tsfLocal);
            }
        }
    }

    public void validateExpirationTime(
            Date expires, long maxClockSkew, long timestampFreshnessLimit)
            throws TimestampValidationCallback.TimestampValidationException {

        //System.out.println("Validate Expiration time called");
        Date currentTime =
                getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, false);
        if (expires.before(currentTime)) {
            log.log(Level.SEVERE, "WSS1514.error.aheadCurrentTime");
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
            log.log(Level.SEVERE, "WSS1515.error.currentTime");
            log.log(Level.SEVERE, "Creation time:" + created);
            log.log(Level.SEVERE, "Current time:" + current);
            throw new TimestampValidationCallback.TimestampValidationException(
                    "The creation time is older than "
                    + " currenttime - timestamp-freshness-limit - max-clock-skew");
        }

        Date currentTime =
                getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
        if (currentTime.before(created)) {
            log.log(Level.SEVERE, "WSS1516.error.creationAheadCurrent.time");
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

    /**
     *
     */
    public class X509CertificateValidatorImpl implements CertificateValidationCallback.CertificateValidator, ValidatorExtension {

        private Map runtimeProps = null;

        public X509CertificateValidatorImpl() {
        }

        public boolean validate(X509Certificate certificate)
                throws CertificateValidationCallback.CertificateValidationException {

            try {
                certificate.checkValidity();
            } catch (CertificateExpiredException e) {
                log.log(Level.SEVERE, "WSS1517.X509.expired", e);
                throw new CertificateValidationCallback.CertificateValidationException("X509Certificate Expired", e);
            } catch (CertificateNotYetValidException e) {
                log.log(Level.SEVERE, "WSS1527.X509.notValid", e);
                throw new CertificateValidationCallback.CertificateValidationException("X509Certificate Not Yet Valid", e);
            }

            // for self-signed certificate
            if (certificate.getIssuerX500Principal().equals(certificate.getSubjectX500Principal())) {
                if (isTrustedSelfSigned(certificate, getTrustStore(this.runtimeProps))) {
                    return true;
                } else {
                    log.log(Level.SEVERE, "WSS1533.X509.SelfSignedCertificate.notValid");
                    throw new CertificateValidationCallback.CertificateValidationException("Validation of self signed certificate failed");
                }
            }

            X509CertSelector certSelector = new X509CertSelector();
            certSelector.setCertificate(certificate);

            PKIXBuilderParameters parameters;
            CertPathValidator certValidator = null;
            CertPath certPath = null;
            List<Certificate> certChainList = new ArrayList<Certificate>();
            boolean caFound = false;
            Principal certChainIssuer = null;
            int noOfEntriesInTrustStore = 0;
            boolean isIssuerCertMatched = false;

            try {
                KeyStore tStore = getTrustStore(this.runtimeProps);
                CertStore cStore = getCertStore(this.runtimeProps);
                parameters = new PKIXBuilderParameters(tStore, certSelector);
                parameters.setRevocationEnabled(revocationEnabled);
                if (cStore != null) {
                    parameters.addCertStore(cStore);
                } else {
                    //create a CertStore on the fly with CollectionCertStoreParameters since some JDK's
                    //cannot build chains to certs only contained in a TrustStore
                    CertStore cs = CertStore.getInstance("Collection",
                            new CollectionCertStoreParameters(Collections.singleton(certificate)));
                    parameters.addCertStore(cs);
                }

                Certificate[] certChain = null;
                String certAlias = tStore.getCertificateAlias(certificate);
                if (certAlias != null) {
                    certChain = tStore.getCertificateChain(certAlias);
                }
                if (certChain == null) {
                    certChainList.add(certificate);
                    certChainIssuer = certificate.getIssuerX500Principal();
                    noOfEntriesInTrustStore = tStore.size();
                } else {
                    certChainList = Arrays.asList(certChain);
                }
                while (!caFound && noOfEntriesInTrustStore-- != 0 && certChain == null) {
                    Enumeration aliases = tStore.aliases();
                    while (aliases.hasMoreElements()) {
                        String alias = (String) aliases.nextElement();
                        Certificate cert = tStore.getCertificate(alias);
                        if (cert == null || !"X.509".equals(cert.getType()) || certChainList.contains(cert)) {
                            continue;
                        }
                        X509Certificate x509Cert = (X509Certificate) cert;
                        if (certChainIssuer.equals(x509Cert.getSubjectX500Principal())) {
                            certChainList.add(cert);
                            if (x509Cert.getSubjectX500Principal().equals(x509Cert.getIssuerX500Principal())) {
                                caFound = true;
                                break;
                            } else {
                                certChainIssuer = x509Cert.getIssuerDN();
                                if (!isIssuerCertMatched) {
                                    isIssuerCertMatched = true;
                                }
                            }
                        } else {
                            continue;
                        }
                    }
                    if (!caFound) {
                        if (!isIssuerCertMatched) {
                            break;
                        } else {
                            isIssuerCertMatched = false;
                        }
                    }
                }
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certPath = cf.generateCertPath(certChainList);
                certValidator = CertPathValidator.getInstance("PKIX");

            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS1518.failedto.validate.certificate", e);
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }

            try {
                certValidator.validate(certPath, parameters);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS1518.failedto.validate.certificate", e);
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }
            return true;
        }

        private boolean isTrustedSelfSigned(X509Certificate cert, KeyStore trustStore)
                throws CertificateValidationCallback.CertificateValidationException {
            if (trustStore == null) {
                return false;
            }
            try {
                Enumeration aliases = trustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    Certificate certificate = trustStore.getCertificate(alias);
                    if (certificate == null || !"X.509".equals(certificate.getType())) {
                        continue;
                    }
                    X509Certificate x509Cert = (X509Certificate) certificate;
                    if (x509Cert != null && x509Cert.equals(cert)) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS1518.failedto.validate.certificate", e);
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }
        }

        public void setRuntimeProperties(Map props) {
            this.runtimeProps = props;
        }
//        private boolean isSelfCert(X509Certificate cert)
//                throws CertificateValidationCallback.CertificateValidationException {
//            if (keyStore == null) {
//                return false;
//            }
//            try {
//                Enumeration aliases = keyStore.aliases();
//                while (aliases.hasMoreElements()) {
//                    String alias = (String) aliases.nextElement();
//                    if (keyStore.isKeyEntry(alias)) {
//                        X509Certificate x509Cert =
//                                (X509Certificate) keyStore.getCertificate(alias);
//                        if (x509Cert != null) {
//                            if (x509Cert.equals(cert)) {
//                                return true;
//                            }
//                        }
//                    }
//                }
//                return false;
//            } catch (Exception e) {
//                log.log(Level.SEVERE, "WSS1518.failedto.validate.certificate", e);
//                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
//            }
//        }
    }

    /**
     *
     * @param ski
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    private X509Certificate getCertificateFromTrustStoreForThumbprint(byte[] ski, Map runtimeProps) throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, ski, MessageConstants.THUMB_PRINT_TYPE);
                if (cert != null) {
                    return cert;
                }
            }
            if (getTrustStore(runtimeProps) == null && getCertStore(runtimeProps) == null) {
                return null;
            }
            if (trustStore != null) {
                Enumeration aliases = trustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    Certificate cert = trustStore.getCertificate(alias);
                    if (cert == null || !"X.509".equals(cert.getType())) {
                        continue;
                    }
                    X509Certificate x509Cert = (X509Certificate) cert;
                    byte[] keyId = XWSSUtil.getThumbprintIdentifier(x509Cert);
                    if (keyId == null) {
                        // Cert does not contain a key identifier
                        continue;
                    }
                    if (Arrays.equals(ski, keyId)) {
                        return x509Cert;
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", e);
            throw new RuntimeException(e);
        }
        //now search in CertStore if present
        if (this.certStore != null) {
            CertSelector selector = null;
            /*if (this.certSelectorClass != null) {
            Map props = new HashMap();
            props.putAll(runtimeProps);
            props.put(XWSSConstants.THUMBPRINT, ski);
            selector = XWSSUtil.getCertSelector(certSelectorClass, props);
            }*/
            if (selector == null) {
                selector = new DigestCertSelector(ski, MessageConstants.SHA_1);
            }
            Collection certs = null;
            try {
                certs = certStore.getCertificates(selector);
            } catch (CertStoreException ex) {
                log.log(Level.SEVERE, "WSS1530.exception.in.certstore.lookup", ex);
                throw new RuntimeException(ex);
            }
            if (certs.size() > 0) {
                return (X509Certificate) certs.iterator().next();
            }
        }

        return null;
    }

    /**
     *
     * @param ski
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    public PrivateKey getPrivateKeyForThumbprint(byte[] ski, Map runtimeProps) throws IOException {

        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, ski, MessageConstants.THUMB_PRINT_TYPE);
                if (cert != null) {
                    return XWSSUtil.getProgrammaticPrivateKey(runtimeProps);
                }
            }
            if (getKeyStore(runtimeProps) == null) {
                return null;
            }
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                }
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = XWSSUtil.getThumbprintIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    // Asuumed key password same as the keystore password
                    //return (PrivateKey) keyStore.getKey(alias, this.keyPassword);
                    return getPrivateKey(runtimeProps, alias);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    private Class loadClassSilent(String classname) {
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
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "LoadClassSilent: could not load class " + classname, e);
                }
            }
        }
        // if context classloader didnt work, try this
        loader = this.getClass().getClassLoader();
        if (loader != null) {
            try {
                ret = loader.loadClass(classname);
                return ret;
            } catch (ClassNotFoundException e) {
                // ignore
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "LoadClassSilent: could not load class " + classname, e);
                }
            }
        }
        return null;
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
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "LoadClass: could not load class " + classname, e);
                }
            }
        }
        // if context classloader didnt work, try this
        loader = this.getClass().getClassLoader();
        try {
            ret = loader.loadClass(classname);
            return ret;
        } catch (ClassNotFoundException e) {
            // ignore
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "LoadClass: could not load class " + classname, e);
            }
        }
        log.log(Level.SEVERE, "WSS1521.error.getting.userClass");
        throw new XWSSecurityException("Could not find User Class " + classname);
    }

    private long toLong(String lng) throws XWSSecurityException {
        if (lng == null) {
            return 0;
        }
        Long ret = 0L;
        ret = Long.valueOf(lng);
        try {
            ret = Long.valueOf(lng);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1522.error.getting.longValue", e);
            throw new XWSSecurityException(e);
        }
        return ret;
    }

    private void initNewInstances() throws XWSSecurityException {

        try {

            if (usernameCbHandler != null) {
                usernameHandler = (CallbackHandler) usernameCbHandler.newInstance();
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for Username Callback Handler");
                }
            }
            if (passwordCbHandler != null) {
                passwordHandler = (CallbackHandler) passwordCbHandler.newInstance();
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for Password Callback Handler");
                }
            }

            if (samlCbHandler != null) {
                samlHandler = (CallbackHandler) samlCbHandler.newInstance();
            }

            if (usernameValidator != null) {
                pwValidator = (PasswordValidationCallback.PasswordValidator) usernameValidator.newInstance();
            }

            if (timestampValidator != null) {
                tsValidator = (TimestampValidationCallback.TimestampValidator) timestampValidator.newInstance();
            }

            if (samlValidator != null) {
                sValidator = (SAMLAssertionValidator) samlValidator.newInstance();
            }

            if (certificateValidator != null) {
                certValidator = (CertificateValidationCallback.CertificateValidator) certificateValidator.newInstance();
            } else {
                // fallback to the default instance
                certValidator = new X509CertificateValidatorImpl();
            }

            if (this.certstoreCbHandler != null) {
                this.certstoreHandler = (CallbackHandler) this.certstoreCbHandler.newInstance();
            }

            if (this.keystoreCbHandler != null) {
                this.keystoreHandler = (CallbackHandler) this.keystoreCbHandler.newInstance();
            }
            if (this.truststoreCbHandler != null) {
                this.truststoreHandler = (CallbackHandler) this.truststoreCbHandler.newInstance();
            }


        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1523.error.getting.newInstance.CallbackHandler", e);
            throw new XWSSecurityException(e);
        }
//        if (this.certstoreHandler != null) {
//            //keep the certstore handy...
//            CertStoreCallback cb = new CertStoreCallback();
//            Callback[] callbacks = new Callback[]{cb};
//            try {
//                this.certstoreHandler.handle(callbacks);
//                this.certStore = cb.getCertStore();
//            } catch (UnsupportedCallbackException ex) {
//                log.log(Level.SEVERE, "WSS1529.exception.in.certstore.callback", ex);
//                throw new XWSSecurityException(ex);
//            } catch (IOException ex) {
//                log.log(Level.SEVERE, "WSS1529.exception.in.certstore.callback", ex);
//                throw new XWSSecurityException(ex);
//            }
//        }

    }

    /**
     *
     * @param pk
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    private X509Certificate getCertificateFromTrustStoreForSAML(PublicKey pk, Map runtimeProps)
            throws IOException {
        try {
            if (runtimeProps != null) {
                Object obj = runtimeProps.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
                X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, pk);
                if (cert != null) {
                    return cert;
                }
            }
            if (getTrustStore(runtimeProps) == null && getCertStore(runtimeProps) == null) {
                return null;
            }
            if (trustStore != null) {
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
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1526.failedto.getcertificate", e);
            throw new RuntimeException(e);
        }
        if (certStore != null) {
            CertSelector selector = null;
            /*if (this.certSelectorClass != null) {
            Map props = new HashMap();
            props.putAll(runtimeProps);
            props.put(XWSSConstants.PUBLICKEY, pk);
            selector = XWSSUtil.getCertSelector(certSelectorClass, props);
            }*/
            if (selector == null) {
                selector = new PublicKeyCertSelector(pk);
            }
            Collection certs = null;
            try {
                certs = certStore.getCertificates(selector);
            } catch (CertStoreException ex) {
                log.log(Level.SEVERE, "WSS1530.exception.in.certstore.lookup", ex);
                throw new RuntimeException(ex);
            }
            if (certs.size() > 0) {
                return (X509Certificate) certs.iterator().next();
            }

        }
        return null;
    }

    /**
     *
     * @param pk
     * @param runtimeProps
     * @return
     * @throws java.io.IOException
     */
    private PrivateKey getPrivateKeyFromKeyStore(PublicKey pk, Map runtimeProps)
            throws IOException {
        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias)) {
                    continue;
                } else {
                    // Just returning the first one here
                    //PrivateKey key = (PrivateKey) keyStore.getKey(alias, this.keyPassword);
                    Certificate cert = keyStore.getCertificate(alias);
                    if (pk.equals(cert.getPublicKey())) {
                        PrivateKey key = getPrivateKey(runtimeProps, alias);
                        return key;
                    }
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS1505.failedto.getkey", e);
            throw new RuntimeException(e);
        }
        return null;
    }

    private String resolveHome(String url) {
        if (url == null) {
            return null;
        }
        if (url.startsWith("$WSIT_HOME")) {
            String wsitHome = System.getProperty("WSIT_HOME");
            if (wsitHome != null) {
                String ret = url.replace("$WSIT_HOME", wsitHome);
                return ret;
            } else {
                log.log(Level.SEVERE, "WSS1524.unableto.resolve.URI.WSIT_HOME.notset");
                throw new RuntimeException("The following config URL: " + url + " in the WSDL could not be resolved because System Property WSIT_HOME was not set");
            }
        } else {
            return url;
        }
    }

    private void initKeyPassword() {
        //NOTE: this is called only when this.keyPwd is non-null
        // check if this.keyPwd is a CBH
        try {
            Class cbh = this.loadClassSilent(this.keyPwd);
            if (cbh != null) {
                CallbackHandler hdlr = (CallbackHandler) cbh.newInstance();
                javax.security.auth.callback.PasswordCallback pc =
                        new javax.security.auth.callback.PasswordCallback("KeyPassword", false);
                Callback[] cbs = new Callback[]{pc};
                hdlr.handle(cbs);
                this.keyPassword = ((javax.security.auth.callback.PasswordCallback) cbs[0]).getPassword();
            } else {
                //the user supplied value is a Password for the key alias
                this.keyPassword = this.keyPwd.toCharArray();
            }
        } catch (java.lang.InstantiationException ex) {
            log.log(Level.SEVERE, "WSS1528.failed.initialize.key.password", ex);
            throw new RuntimeException(ex);
        } catch (java.io.IOException e) {
            log.log(Level.SEVERE, "WSS1528.failed.initialize.key.password", e);
            throw new RuntimeException(e);
        } catch (java.lang.IllegalAccessException ie) {
            log.log(Level.SEVERE, "WSS1528.failed.initialize.key.password", ie);
            throw new RuntimeException(ie);
        } catch (javax.security.auth.callback.UnsupportedCallbackException ue) {
            log.log(Level.SEVERE, "WSS1528.failed.initialize.key.password", ue);
            throw new RuntimeException(ue);
        }
    }

    /**
     *
     * @param context
     * @return
     */
    private X509Certificate getDynamicCertificate(Map context) {

        X509Certificate cert = null;
        X509Certificate self = null;
        Subject requesterSubject = getRequesterSubject(context);
        if (requesterSubject != null) {
            Set publicCredentials = requesterSubject.getPublicCredentials();
            for (Iterator it = publicCredentials.iterator(); it.hasNext();) {
                Object cred = it.next();
                if (cred instanceof java.security.cert.X509Certificate) {
                    X509Certificate certificate = (java.security.cert.X509Certificate) cred;
                    if (!isMyCert(certificate, context)) {
                        cert = certificate;
                        break;
                    } else {
                        self = certificate;
                    }
                }
            }
            if (cert != null) {
                return cert;
            } else if (self != null) {
                //this is to allow tests where server and client use the same cert
                return self;
            }
        }
        /*
        String keyId = (String)context.get(MessageConstants.REQUESTER_KEYID);
        String issuerName = (String)context.get(MessageConstants.REQUESTER_ISSUERNAME);
        BigInteger issuerSerial = (BigInteger)context.get(MessageConstants.REQUESTER_SERIAL);
        if (keyId != null) {
        try {
        cert = getMatchingCertificate(keyId.getBytes(), trustStore);
        if (cert != null)
        return cert;
        } catch (XWSSecurityException e) {}
        } else if ((issuerName != null) && (issuerSerial != null)) {
        try {
        cert = getMatchingCertificate(issuerSerial, issuerName, trustStore);
        if (cert != null)
        return cert;
        } catch (XWSSecurityException e) {}
        } */
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Could not locate Incoming Client Certificate in Caller Subject");
        }

        return null;
    }

    public Subject getRequesterSubject(final Map context) {
        //return (Subject)context.get(MessageConstants.AUTH_SUBJECT);
        Subject otherPartySubject = (Subject) context.get(MessageConstants.AUTH_SUBJECT);
        if (otherPartySubject != null) {
            return otherPartySubject;
        }
        otherPartySubject = (Subject) AccessController.doPrivileged(
                new PrivilegedAction<Object>() {

                    @SuppressWarnings("unchecked")
                    public Object run() {
                        Subject otherPartySubj = new Subject();
                        context.put(MessageConstants.AUTH_SUBJECT, otherPartySubj);
                        return otherPartySubj;
                    }
                });
        return otherPartySubject;
    }

    private KeyStore getKeyStore(Map runtimeProps) {
        try {
            if (keyStore != null) {
                return keyStore;
            }
            return getKeyStoreUsingCallback(runtimeProps);
        } finally {
            if (this.keyStore == null) {
                log.log(Level.SEVERE, "Could not locate KeyStore, check keystore assertion in WSIT configuration");
                throw new XWSSecurityRuntimeException("Could not locate KeyStore, check keystore assertion in WSIT configuration");
            }
        }
    }

    private synchronized KeyStore getKeyStoreUsingCallback(Map runtimeProps) {
        if (keyStore == null && keystoreHandler != null) {
            try {
                KeyStoreCallback cb = new KeyStoreCallback();
                SecurityUtil.copy(cb.getRuntimeProperties(), runtimeProps);
                Callback[] cbs = new Callback[]{cb};
                this.keystoreHandler.handle(cbs);
                keyStore = cb.getKeystore();
                if (keyStore == null) {
                    log.log(Level.SEVERE, "No KeyStore set in KeyStorCallback  by CallbackHandler");
                    throw new XWSSecurityRuntimeException("No KeyStore set in KeyStorCallback  by CallbackHandler");
                }
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            } catch (UnsupportedCallbackException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        return keyStore;
    }

    private KeyStore getTrustStore(Map runtimeProps) {
        if (trustStore != null) {
            return trustStore;
        }
//        if (this.truststoreHandler == null) {
//            log.log(Level.SEVERE, "Could not locate TrustStore, check truststore assertion in WSIT configuration");
//            throw new XWSSecurityRuntimeException("Could not locate TrustStore, check truststore assertion in WSIT configuration");
//        }
        return getTrustStoreUsingCallback(runtimeProps);
    }

    private CertStore getCertStore(Map runtimeProps) {
        if (this.certStore != null) {
            return certStore;
        }
        return getCertStoreUsingCallback(runtimeProps);
    }

    private synchronized CertStore getCertStoreUsingCallback(Map runtimeProps) {
        if (this.certstoreHandler != null) {
            //keep the certstore handy...
            CertStoreCallback cb = new CertStoreCallback();
            SecurityUtil.copy(cb.getRuntimeProperties(), runtimeProps);
            Callback[] callbacks = new Callback[]{cb};
            try {
                this.certstoreHandler.handle(callbacks);
                this.certStore = cb.getCertStore();
            } catch (UnsupportedCallbackException ex) {
                log.log(Level.SEVERE, "WSS1529.exception.in.certstore.callback", ex);
                throw new XWSSecurityRuntimeException(ex);
            } catch (IOException ex) {
                log.log(Level.SEVERE, "WSS1529.exception.in.certstore.callback", ex);
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        return certStore;
    }

    /**
     *
     * @param runtimeProps
     * @return
     */
    private synchronized KeyStore getTrustStoreUsingCallback(Map runtimeProps) {

        if (trustStore == null && truststoreHandler != null) {
            try {
                KeyStoreCallback cb = new KeyStoreCallback();
                SecurityUtil.copy(cb.getRuntimeProperties(), runtimeProps);
                Callback[] cbs = new Callback[]{cb};
                this.truststoreHandler.handle(cbs);
                trustStore = cb.getKeystore();
                if (trustStore == null) {
                    log.log(Level.SEVERE, "No TrustStore set in KeyStorCallback  by CallbackHandler");
                    throw new XWSSecurityRuntimeException("No TrustStore set in KeyStorCallback  by CallbackHandler");
                }
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            } catch (UnsupportedCallbackException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        return trustStore;
    }

    /**
     * 
     * @param runtimeProps
     * @param alias
     * @return
     */
    private PrivateKey getPrivateKey(Map runtimeProps, String alias) {
        PrivateKey privKey = null;
        if (this.keystoreHandler != null) {
            try {
                PrivateKeyCallback cb = new PrivateKeyCallback();
                cb.setKeystore(keyStore);
                cb.setAlias(alias);
                SecurityUtil.copy(cb.getRuntimeProperties(), runtimeProps);
                Callback[] cbs = new Callback[]{cb};
                this.keystoreHandler.handle(cbs);
                privKey = cb.getKey();
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            } catch (UnsupportedCallbackException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            }

        } else {
            try {
                privKey = (PrivateKey) keyStore.getKey(alias, this.keyPassword);
            } catch (KeyStoreException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            } catch (UnrecoverableKeyException ex) {
                log.log(Level.SEVERE, null, ex);
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        if (privKey == null) {
            log.log(Level.SEVERE, "PrivateKey returned by PrivateKeyCallback was Null");
            throw new XWSSecurityRuntimeException("PrivateKey returned by PrivateKeyCallback was Null");
        }
        return privKey;
    }

    public SAMLAssertionValidator getSAMLValidator() {
        return sValidator;
    }
}
 
