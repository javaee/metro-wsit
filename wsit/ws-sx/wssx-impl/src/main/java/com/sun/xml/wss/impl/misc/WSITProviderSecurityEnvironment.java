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

/*
 * WSITProviderSecurityEnvironment.java
 *
 * Created on November 12, 2006, 1:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.misc;

import com.sun.xml.wss.AliasSelector;
import com.sun.xml.wss.impl.callback.SAMLAssertionValidator;
import java.io.IOException;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.util.Collection;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.message.callback.CertStoreCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.callback.PrivateKeyCallback;
import javax.security.auth.message.callback.SecretKeyCallback;
import javax.security.auth.message.callback.TrustStoreCallback;
import javax.security.auth.message.callback.CallerPrincipalCallback;

import java.math.BigInteger;

import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.KeyStoreException;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.cert.X509CertSelector;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.Timer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.GregorianCalendar;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.x500.X500Principal;
import javax.security.auth.x500.X500PrivateCredential;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.crypto.SecretKey;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.exceptions.Base64DecodingException;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.ws.security.impl.kerberos.KerberosLogin;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.wss.NonceManager;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSConstants;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.SecurityHeaderException;
import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.impl.callback.SAMLCallback;
import com.sun.xml.wss.impl.policy.mls.PrivateKeyBinding;

import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.callback.CertificateValidationCallback;

import com.sun.xml.wss.impl.callback.RuntimeProperties;
import com.sun.xml.wss.impl.callback.SAMLValidator;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import com.sun.xml.wss.impl.callback.ValidatorExtension;
import com.sun.xml.wss.saml.Assertion;
//import com.sun.xml.wss.saml.assertion.AuthorityBinding;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import java.security.AccessController;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.BindingProvider;

import org.ietf.jgss.GSSName;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.security.PrivilegedAction;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import com.sun.xml.wss.util.XWSSUtil;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.LoginContext;
import org.ietf.jgss.GSSCredential;


/**
 *
 * @author kumar.jayanti
 */
public class WSITProviderSecurityEnvironment implements SecurityEnvironment {

    /* menu of module options - includes algorithm Ids, keystore aliases etc., */
    private Map _securityOptions;

    /* Callbacks */
    private CallbackHandler _handler;

    /* Map of aliases-key passwords obtained via Module Options */
    //Map aliases_keypwds = null;

    // value of the maximum skew between the local times of two
    // systems (in milliseconds).
    // Keeping it 1 minute.
    protected final long MAX_CLOCK_SKEW = MessageConstants.MAX_CLOCK_SKEW;

    // milliseconds (set to 5 mins), time for which a timestamp is considered fresh
    protected final long TIMESTAMP_FRESHNESS_LIMIT = MessageConstants.TIMESTAMP_FRESHNESS_LIMIT;

    /** logger */
    protected static final Logger log =  Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    public static final String USERNAME_CBH = "username.callback.handler";
    public static final String PASSWORD_CBH = "password.callback.handler";
    
    private static final SimpleDateFormat calendarFormatter1 =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat calendarFormatter2 =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

    // Nonce Cache
    NonceCache nonceCache = null;

    // Nonce clean-up timer
    static final boolean USE_DAEMON_THREAD = true;
    static final Timer nonceCleanupTimer = new Timer(USE_DAEMON_THREAD);
    
    private String myAlias;
    private String keyPwd;
    private String peerEntityAlias;
    private String myUsername;
    private String myPassword;
    private String samlCBH;
    private String sV;
    private Class samlCbHandler;
    private CallbackHandler samlHandler= null;
    private Class samlValidator;
    private SAMLAssertionValidator sValidator;
    private String krbLoginModule = null;
    private String krbServicePrincipal = null;
    private boolean krbCredentialDelegation = false;
    private Class usernameCbHandler;
    private Class passwordCbHandler;
    private String mcs;
    private String tfl;
    private String mna; 
           
    protected long maxClockSkewG;
    protected long timestampFreshnessLimitG;
    protected long maxNonceAge = MessageConstants.MAX_NONCE_AGE;
   
    private boolean isAppClient = true;
    
    private X509Certificate selfCertificate = null;
    
    private String certSelectorClassName;
    private String crlSelectorClassName;
    private Class certSelectorClass;
    private Class crlSelectorClass;
    
    protected String revocationEnabledAttr;
    protected boolean revocationEnabled = false;
    
    private String keystoreCertSelectorClassName;
    private String truststoreCertSelectorClassName;
    
    private Class keystoreCertSelectorClass;
    private Class truststoreCertSelectorClass;
    
    private Container container = null;
    
    private String useXWSSCallbacksStr;
    private boolean useXWSSCallbacks=false;
    private CertificateValidationCallback.CertificateValidator certValidator;
    private Class certificateValidator;
    private Class usernameValidator;
    private Class timestampValidator;
    private com.sun.xml.wss.impl.callback.PasswordValidationCallback.PasswordValidator pwValidator;
    private TimestampValidationCallback.TimestampValidator tsValidator; 
    private String jaasLoginModuleForKeystore;
    private Subject loginContextSubjectForKeystore;
    private String keyStoreCBH;
    private CallbackHandler keystoreCbHandlerClass;
    private CallbackHandler usernameHandler;
    private CallbackHandler passwordHandler;
    /** Creates a new instance of WSITProviderSecurityEnvironment */
    @SuppressWarnings("empty-statement")
    public WSITProviderSecurityEnvironment(CallbackHandler handler, Map options, Properties configAssertions)
            throws XWSSecurityException {
        _handler = new PriviledgedHandler(handler);
        _securityOptions = options;

        if (_securityOptions != null) {
            String mo_aliases = (String) _securityOptions.get("ALIASES");
            String mo_keypwds = (String) _securityOptions.get("PASSWORDS");

            if (mo_aliases != null && mo_keypwds != null) {
                StringTokenizer aliases = new StringTokenizer(mo_aliases, " ");
                StringTokenizer keypwds = new StringTokenizer(mo_keypwds, " ");
                if (aliases.countTokens() != keypwds.countTokens()) {
                    ;
                }// log.INFO

            //while (aliases.hasMoreElements()) {                     
            //   aliases_keypwds.put(aliases.nextToken(), keypwds.nextToken());                      
            //}                  
            }
            container = (Container) _securityOptions.get("CONTAINER");
        }

        //store the relevant config assertions here
        this.myAlias = configAssertions.getProperty(DefaultCallbackHandler.MY_ALIAS);
        this.keyPwd = configAssertions.getProperty(DefaultCallbackHandler.KEY_PASSWORD);
        this.peerEntityAlias = configAssertions.getProperty(DefaultCallbackHandler.PEER_ENTITY_ALIAS);

        this.krbLoginModule = configAssertions.getProperty(DefaultCallbackHandler.KRB5_LOGIN_MODULE);
        this.krbServicePrincipal = configAssertions.getProperty(DefaultCallbackHandler.KRB5_SERVICE_PRINCIPAL);
        this.krbCredentialDelegation = Boolean.valueOf(configAssertions.getProperty(DefaultCallbackHandler.KRB5_CREDENTIAL_DELEGATION));

        String uCBH = configAssertions.getProperty(USERNAME_CBH);
        String pCBH = configAssertions.getProperty(PASSWORD_CBH);

        this.myUsername = configAssertions.getProperty(DefaultCallbackHandler.MY_USERNAME);
        this.myPassword = configAssertions.getProperty(DefaultCallbackHandler.MY_PASSWORD);
        this.samlCBH = configAssertions.getProperty(DefaultCallbackHandler.SAML_CBH);        
        if (this.samlCBH != null) {
            samlCbHandler = loadClass(samlCBH);
        }
        if (samlCbHandler != null) {
            try {
                samlHandler = (CallbackHandler) samlCbHandler.newInstance();
            } catch (InstantiationException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0715_EXCEPTION_CREATING_NEWINSTANCE(), ex);
                throw new XWSSecurityException(ex);
            } catch (IllegalAccessException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0715_EXCEPTION_CREATING_NEWINSTANCE(), ex);
                throw new XWSSecurityException(ex);
            }
        }

        sV = configAssertions.getProperty(DefaultCallbackHandler.SAML_VALIDATOR);
        if (sV != null) {
            samlValidator = loadClass(sV);
        }

        if (samlValidator != null) {
            try {
                sValidator = (SAMLAssertionValidator) samlValidator.newInstance();
            } catch (InstantiationException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0715_EXCEPTION_CREATING_NEWINSTANCE(), ex);
                throw new XWSSecurityException(ex);
            } catch (IllegalAccessException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0715_EXCEPTION_CREATING_NEWINSTANCE(), ex);
                throw new XWSSecurityException(ex);
            }
        }

        mcs = configAssertions.getProperty(DefaultCallbackHandler.MAX_CLOCK_SKEW_PROPERTY);
        tfl = configAssertions.getProperty(DefaultCallbackHandler.TIMESTAMP_FRESHNESS_LIMIT_PROPERTY);
        mna = configAssertions.getProperty(DefaultCallbackHandler.MAX_NONCE_AGE_PROPERTY);

        revocationEnabledAttr = configAssertions.getProperty(DefaultCallbackHandler.REVOCATION_ENABLED);
        if (revocationEnabledAttr != null) {
            this.revocationEnabled = Boolean.parseBoolean(revocationEnabledAttr);
        }
        maxClockSkewG = SecurityUtil.toLong(mcs);
        timestampFreshnessLimitG = SecurityUtil.toLong(tfl);
        if (mna != null) {
            maxNonceAge = SecurityUtil.toLong(mna);
        }

        useXWSSCallbacksStr = configAssertions.getProperty(DefaultCallbackHandler.USE_XWSS_CALLBACKS);
        if (useXWSSCallbacksStr != null) {
            this.useXWSSCallbacks = Boolean.parseBoolean(useXWSSCallbacksStr);
        }
        String cV = configAssertions.getProperty(DefaultCallbackHandler.CERTIFICATE_VALIDATOR);
        certificateValidator = loadClass(cV);
        String uV = configAssertions.getProperty(DefaultCallbackHandler.USERNAME_VALIDATOR);
        String tV = configAssertions.getProperty(DefaultCallbackHandler.TIMESTAMP_VALIDATOR);
        usernameValidator = loadClass(uV);
        timestampValidator = loadClass(tV);
        usernameCbHandler = loadClass(uCBH);
        passwordCbHandler = loadClass(pCBH);
        
        try {
            if (certificateValidator != null) {
                certValidator = (CertificateValidationCallback.CertificateValidator) certificateValidator.newInstance();
            }
            if (usernameValidator != null) {
                pwValidator = (com.sun.xml.wss.impl.callback.PasswordValidationCallback.PasswordValidator) usernameValidator.newInstance();
            }
            if (timestampValidator != null) {
                tsValidator = (TimestampValidationCallback.TimestampValidator) timestampValidator.newInstance();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, com.sun.xml.wss.logging.impl.misc.LogStringsMessages.WSS_1523_ERROR_GETTING_NEW_INSTANCE_CALLBACK_HANDLER(), e);
            throw new XWSSecurityException(e);
        }

        //determine if we are in an AppClient
        NameCallback nameCallback = new NameCallback("Username: ");
        try {
            Callback[] cbs = new Callback[]{nameCallback};
            _handler.handle(cbs);
        } catch (javax.security.auth.callback.UnsupportedCallbackException e) {
            this.isAppClient = false;
        } catch (Exception e) {
            this.isAppClient = false;
        }

        this.certSelectorClassName = configAssertions.getProperty(DefaultCallbackHandler.CERTSTORE_CERTSELECTOR);
        this.crlSelectorClassName = configAssertions.getProperty(DefaultCallbackHandler.CERTSTORE_CRLSELECTOR);
        this.certSelectorClass = loadClass(this.certSelectorClassName);
        this.crlSelectorClass = loadClass(this.crlSelectorClassName);

        this.keystoreCertSelectorClassName = configAssertions.getProperty(DefaultCallbackHandler.KEYSTORE_CERTSELECTOR);
        this.truststoreCertSelectorClassName = configAssertions.getProperty(DefaultCallbackHandler.TRUSTSTORE_CERTSELECTOR);
        keystoreCertSelectorClass = loadClass(this.keystoreCertSelectorClassName);
        truststoreCertSelectorClass = loadClass(this.truststoreCertSelectorClassName);

        jaasLoginModuleForKeystore = configAssertions.getProperty(DefaultCallbackHandler.JAAS_KEYSTORE_LOGIN_MODULE);
        keyStoreCBH = configAssertions.getProperty(DefaultCallbackHandler.KEYSTORE_CBH);
        loginContextSubjectForKeystore = initJAASKeyStoreLoginModule();
        
    //keep the self certificate handy
//           if (_handler != null && this.myAlias != null) {
//               try {
//                   PrivateKeyCallback.Request request = 
//                           new PrivateKeyCallback.AliasRequest(this.myAlias);
//                   PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
//                   
//                   Callback[] callbacks = new Callback[] { pkCallback };
//                   _handler.handle(callbacks);
//                   Certificate[] chain = pkCallback.getChain();
//                   if (chain != null) {
//                       selfCertificate = (X509Certificate)chain[0];
//                   }
//               } catch (Exception ex) {
//                   //ignore for now
//               }
//           }
    }
    /*
     * @throws XWSSecurityException
     */
    public PrivateKey getPrivateKey(Map context, String alias)
        throws XWSSecurityException {

        if (alias == null && context != null) {
            Object obj = context.get(XWSSConstants.PRIVATEKEY_PROPERTY);
            PrivateKey key = null;
            if (obj instanceof PrivateKey) {
                key = (PrivateKey) obj;
                return key;
            }
        }
        PrivateKey privateKey = null;
        try {
            PrivateKeyCallback.Request request =
                new PrivateKeyCallback.AliasRequest(alias);
            PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
            Callback[] callbacks = null;
            if (this.useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                callbacks = new Callback[] {props, pkCallback };
            } else {
                callbacks = new Callback[] { pkCallback };
            }
            _handler.handle(callbacks);
            privateKey = (PrivateKey) pkCallback.getKey();
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.AliasRequest"),
                    new Object[] { "PrivateKeyCallback.AliasRequest"});
            log.log(Level.SEVERE,LogStringsMessages.WSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG(),e);
             throw new XWSSecurityException(e);
        }

        if (privateKey == null) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0222_CANNOT_LOCATE_PRIVKEY(alias), new Object[] {alias});
           throw new XWSSecurityException(
             "Unable to locate private key for the alias: " + alias);
        } 

        return privateKey;
    }

    /*
     * Retrieves the PrivateKey corresponding to the cert 
     * with the given KeyIdentifier value
     *
     * @param keyIdentifier an Opaque identifier indicating
     * the X509 certificate
     *
     * @return the PrivateKey corresponding to the cert 
     *  with the given KeyIdentifier value
     *
     * @throws XWSSecurityException
     */
    public PrivateKey getPrivateKey(Map context, byte[] keyIdentifier)
        throws XWSSecurityException {

        if (context != null) {
            Object obj = context.get(XWSSConstants.PRIVATEKEY_PROPERTY);
            PrivateKey key = null;
            if (obj instanceof PrivateKey) {
                key = (PrivateKey) obj;
                Object cert = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
                if (XWSSUtil.matchesProgrammaticInfo(cert, keyIdentifier, MessageConstants.KEY_INDETIFIER_TYPE) != null) {
                    return key;
                }
            }
        }

        /*
           use PrivateKeyCallback
        */
        try {
           Subject subject = getSubject(context);
           if (subject != null) {
              Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
              if (set != null) {
                 Iterator it = set.iterator();
                 while (it.hasNext()) {
                    X500PrivateCredential cred = (X500PrivateCredential)it.next();
                    if (matchesKeyIdentifier(Base64.decode(keyIdentifier), 
                                             cred.getCertificate()))
                       return cred.getPrivateKey();
                 }
              }
           }

           PrivateKeyCallback.Request request = 
                   new PrivateKeyCallback.SubjectKeyIDRequest(keyIdentifier);
           PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
           Callback[] callbacks = null;
            if (this.useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                callbacks = new Callback[] {props, pkCallback };
            } else {
                callbacks = new Callback[] { pkCallback };
            }
           _handler.handle(callbacks);

           return pkCallback.getKey(); 
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.SubjectKeyIDRequest"),
                    new Object[] { "PrivateKeyCallback.SubjectKeyIDRequest"});
            log.log(Level.SEVERE,LogStringsMessages.WSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG(),e);
            throw new XWSSecurityException(e);
        }
    }

    /*
     * Retrieves the PrivateKey corresponding to the given cert 
     *
     * @param cert an X509 certificate
     *
     * @return the PrivateKey corresponding to the cert 
     *
     * @throws XWSSecurityException
     */
    public PrivateKey getPrivateKey(Map context, X509Certificate cert)
        throws XWSSecurityException {
        /*
           use PrivateKeyCallback
        */
        try {
            if (context != null) {
                Object obj = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
                if (cert != null && cert.equals(obj)) {
                    Object key = context.get(XWSSConstants.PRIVATEKEY_PROPERTY);
                    if (key != null && key instanceof PrivateKey) {
                        return (PrivateKey) key;
                    }
                }
            }
           Subject subject = getSubject(context);
           if (subject != null) {
              Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
              if (set != null) {
                 String issuerName = org.apache.xml.security.utils.RFC2253Parser.normalize(
                                  cert.getIssuerDN().getName());
                 Iterator it = set.iterator();
                 while (it.hasNext()) {
                    X500PrivateCredential cred = (X500PrivateCredential)it.next();
                    X509Certificate x509Cert = cred.getCertificate();
                    BigInteger serialNo = x509Cert.getSerialNumber();
                   X500Principal currentIssuerPrincipal = x509Cert.getIssuerX500Principal();
                   X500Principal issuerPrincipal = new X500Principal(issuerName);
                   if (serialNo.equals(cert.getSerialNumber())
                           && currentIssuerPrincipal.equals(issuerPrincipal)) {
                       return cred.getPrivateKey();
                   }
                 }
              }
           }

           PrivateKeyCallback.Request request = 
                   new PrivateKeyCallback.IssuerSerialNumRequest(
                        cert.getIssuerX500Principal(), cert.getSerialNumber());     
           PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
           Callback[] callbacks = null;
           if (this.useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                callbacks = new Callback[] {props, pkCallback };
            } else {
                callbacks = new Callback[] { pkCallback };
            }
           _handler.handle(callbacks);

           return pkCallback.getKey(); 
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.IssuerSerialNumRequest"),
                    new Object[] { "PrivateKeyCallback.IssuerSerialNumRequest"});
            log.log(Level.SEVERE,LogStringsMessages.WSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG(),e);
            throw new XWSSecurityException(e);
        }
     }

    /*
     * Retrieves the matching PrivateKey corresponding to cert whose
     * SerialNumber and IssuerName are given
     *
     * @param serialNumber X509Certificate SerialNumber
     * @param issuerName   X509Certificate IssuerName
     *
     * @return PrivateKey
     *
     * @throws XWSSecurityException
     */
    public PrivateKey getPrivateKey(Map context, BigInteger serialNumber, String issuerName)
        throws XWSSecurityException {
        /*
           use PrivateKeyCallback
        */
        try {
            if (context != null) {
                Object obj = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
                if (XWSSUtil.matchesProgrammaticInfo(obj, serialNumber, issuerName) != null) {
                    PrivateKey key = XWSSUtil.getProgrammaticPrivateKey(context);
                    if (key != null) {
                        return key;
                    }
                }
            }
           Subject subject = getSubject(context);
           if (subject != null) {
              Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
              if (set != null) {
                 Iterator it = set.iterator();
                 while (it.hasNext()) {
                    X500PrivateCredential cred = (X500PrivateCredential)it.next();
                    X509Certificate x509Cert = cred.getCertificate();
                    BigInteger serialNo = x509Cert.getSerialNumber();
                    //Fix for WSIT issue 1590
                   X500Principal currentIssuerPrincipal = x509Cert.getIssuerX500Principal();
                   X500Principal issuerPrincipal = new X500Principal(issuerName);
                   if (serialNo.equals(serialNumber)
                           && currentIssuerPrincipal.equals(issuerPrincipal)) {
                       return cred.getPrivateKey();
                   }
                 }
              }
           }

           PrivateKeyCallback.Request request = 
                   new PrivateKeyCallback.IssuerSerialNumRequest(
                            new X500Principal(issuerName), serialNumber);     
            PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
            Callback[] callbacks = null;
            if (this.useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                callbacks = new Callback[]{props, pkCallback};
            } else {
                callbacks = new Callback[]{pkCallback};
            }
            _handler.handle(callbacks);

           return pkCallback.getKey(); 
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.IssuerSerialNumRequest"),
                    new Object[] { "PrivateKeyCallback.IssuerSerialNumRequest"});
            log.log(Level.SEVERE,LogStringsMessages.WSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG(),e);
            throw new XWSSecurityException(e);
        }
    }

    /**
     * Retrieves a reasonable default value for the current user's
     * X509Certificate if one exists.
     * 
     * @return the default certificate for the current user
     *
     * @throws XWSSecurityException
     */
    public X509Certificate getDefaultCertificate(Map context) 
        throws XWSSecurityException {
        /* 
          use PrivateKeyCallback to get the
          certChain - return the first certificate
        */
        if (context != null) {
            Object obj = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
            if (obj != null && obj instanceof X509Certificate) {
                return (X509Certificate) obj;
            }
        }
        Subject subject = getSubject(context);
        if (subject != null) {
           Set set = subject.getPublicCredentials(X509Certificate.class);
           if (set != null && set.size() == 1) 
              return ((X509Certificate)(set.toArray())[0]); 
        }
        
        if (this.myAlias != null || this.keystoreCertSelectorClass != null) {
            return this.getCertificate(context, this.myAlias, true);
        }
 
        PrivateKeyCallback pkCallback = new PrivateKeyCallback(null);
        Callback[] _callbacks = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties props = new RuntimeProperties(context);
            _callbacks = new Callback[]{props, pkCallback};

        } else {
            _callbacks = new Callback[]{pkCallback};
        }
        try {
            _handler.handle(_callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback with null argument"),
                    new Object[] { "PrivateKeyCallback with null argument"});
            log.log(Level.SEVERE,LogStringsMessages.WSS_0217_CALLBACKHANDLER_HANDLE_EXCEPTION_LOG(),e);
            throw new XWSSecurityException(e);
        }
        
        Certificate[] chain = pkCallback.getChain();
        if (chain == null) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0296_NULL_CHAIN_CERT());
           throw new XWSSecurityException(
            "Empty certificate chain returned by PrivateKeyCallback");
        }
        return (X509Certificate)chain[0];
    }

    /**
     * Authenticate the user against a list of known username-password
     * pairs.
     *
     * @param username
     * @param password
     * @return true if the username-password pair is valid
     */
    public boolean authenticateUser(Map context,String username, String password) 
           throws XWSSecurityException {
        if (pwValidator != null) {
            com.sun.xml.wss.impl.callback.PasswordValidationCallback.PlainTextPasswordRequest request =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback.PlainTextPasswordRequest(username, password);
            com.sun.xml.wss.impl.callback.PasswordValidationCallback passwordValidationCallback =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback(request);
            ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
            passwordValidationCallback.setValidator(pwValidator);
            return passwordValidationCallback.getResult();
        }
         
        /*
          use PasswordValidationCallback
        */
        char[] pwd = (password == null) ? null : password.toCharArray(); 
        PasswordValidationCallback pvCallback = new PasswordValidationCallback(
                this.getRequesterSubject(context),username, pwd);
        Callback[] callbacks = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties xwsscb = new RuntimeProperties(context);
            callbacks = new Callback[]{xwsscb, pvCallback};
        } else {
            callbacks = new Callback[]{pvCallback};
        }
        try {
           _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION( "Authenticating User against list of Known username-password pairs"),
                    new Object[] { "Authenticating User against list of Known username-password pairs"});
           throw new XWSSecurityException(e);
        }

        // zero the password 
        if (pwd != null)
           pvCallback.clearPassword();

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,"Username Authentication done for " + username);
        }
        
        return pvCallback.getResult(); 
    }

    /**
     * Authenticate the user against a list of known usernames
     * @param username
     * @return password if the username is valid
     */
    public String authenticateUser(Map context,String username)
           throws XWSSecurityException {
        String password = null;
        if (pwValidator != null) {
            com.sun.xml.wss.impl.callback.PasswordValidationCallback.DerivedKeyPasswordRequest request =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback.DerivedKeyPasswordRequest(username);
            com.sun.xml.wss.impl.callback.PasswordValidationCallback passwordValidationCallback =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback(request);
            ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
            if(pwValidator != null && pwValidator instanceof com.sun.xml.wss.impl.callback.PasswordValidationCallback.DerivedKeyPasswordValidator){
                ((com.sun.xml.wss.impl.callback.PasswordValidationCallback.DerivedKeyPasswordValidator)pwValidator).setPassword(request);
                passwordValidationCallback.setValidator(pwValidator);
            }
            passwordValidationCallback.getResult();
            password =  request.getPassword();
        }      
        return password;
    }

    /**
     * Authenticate the user given the password digest.
     *
     * @param username
     * @param passwordDigest
     * @param nonce
     * @param created
     * @return true if the password digest is valid
     */
    public boolean authenticateUser(
        Map context,
        String username,
        String passwordDigest,
        String nonce,
        String created)
        throws XWSSecurityException {
        
         boolean result = false;
         if (pwValidator != null) {
            com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest request =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest(username, passwordDigest, nonce, created);
            com.sun.xml.wss.impl.callback.PasswordValidationCallback passwordValidationCallback =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback(request);
            ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
            if(pwValidator != null && pwValidator instanceof com.sun.xml.wss.impl.callback.PasswordValidationCallback.WsitDigestPasswordValidator){
                ((com.sun.xml.wss.impl.callback.PasswordValidationCallback.WsitDigestPasswordValidator)pwValidator).setPassword(request);
                passwordValidationCallback.setValidator(pwValidator);
            }
            return passwordValidationCallback.getResult();
        }
         
        if (this.useXWSSCallbacks) {
            com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest request =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest(
                    username, passwordDigest, nonce, created);
            com.sun.xml.wss.impl.callback.PasswordValidationCallback passwordValidationCallback =
                    new com.sun.xml.wss.impl.callback.PasswordValidationCallback(request);
            ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[]{passwordValidationCallback};
           
            try {
                _handler.handle(callbacks);
                if (passwordValidationCallback.getValidator() != null) {
                    result = passwordValidationCallback.getResult();
                    if (result == true) {
                        CallerPrincipalCallback pvCallback = new CallerPrincipalCallback(getSubject(context), username);
                        callbacks = new Callback[]{pvCallback};
                        try {
                            _handler.handle(callbacks);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("CallerPrincipalCallback"),
                                    new Object[]{"CallerPrincipalCallback"});
                            throw new XWSSecurityRuntimeException(e);
                        }
                    }
                    return result;
                }
            } catch (UnsupportedCallbackException ex) {
                //ignore if they don't support and try other routes
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "The Supplied JMAC CallbackHandler does not support com.sun.xml.wss.impl.callback.PasswordValidationCallback.DigestPasswordRequest");
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0225_FAILED_PASSWORD_VALIDATION_CALLBACK(), e);
                throw new XWSSecurityException(e);
            }
        }
 
        try {
            RealmAuthenticationAdapter adapter = RealmAuthenticationAdapter.newInstance(null);
            if (adapter != null) {
                result = adapter.authenticate(getSubject(context), username, passwordDigest, nonce, created, context);
            } else {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0295_PASSWORD_VAL_NOT_CONFIG_USERNAME_VAL());
                throw new XWSSecurityException("Error: No PasswordValidator Configured for UsernameToken Validation");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0225_FAILED_PASSWORD_VALIDATION_CALLBACK(), e);
            throw new XWSSecurityException(e);
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Username Authentication done for " + username);
        }
        return result;
    }  

    /**
     * Validate an X509Certificate.
     * @return true, if the cert is a valid one, false o/w.
     * @throws XWSSecurityException
     *     if there is some problem during validation.
     */
    public boolean validateCertificate(X509Certificate cert, Map context) 
        throws XWSSecurityException {
        
        if (this.certValidator != null) {
            CertificateValidationCallback certValCallback = new CertificateValidationCallback(cert, context);
            certValCallback.setValidator(certValidator);
            certValCallback.setRevocationEnabled(revocationEnabled);
            return certValCallback.getResult();            
        }
        if (this.useXWSSCallbacks) {
            CertificateValidationCallback certValCallback = new CertificateValidationCallback(cert, context);
            certValCallback.setRevocationEnabled(revocationEnabled);
            Callback[] callbacks = new Callback[]{certValCallback};
            try {
                _handler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0223_FAILED_CERTIFICATE_VALIDATION());
                throw SOAPUtil.newSOAPFaultException(
                        MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "Certificate validation failed",
                        e, true);
            }
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Certificate Validation called on certificate " + cert.getSubjectDN());
            }
            return certValCallback.getResult();
        }
        
        /*
          use TrustStore and CertStore 
        */ 
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0298_X_509_EXPIRED(), e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "X509Certificate Expired", e, true);
        } catch (CertificateNotYetValidException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0299_X_509_NOT_VALID(), e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        "X509Certificate not yet valid", e, true);
        }

        // for self-signed certificate
        if(cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal())){
            if(isTrustedSelfSigned(cert)){
                return true;
            }else{
                log.log(Level.SEVERE, com.sun.xml.wss.logging.impl.misc.LogStringsMessages.WSS_1533_X_509_SELF_SIGNED_CERTIFICATE_NOT_VALID());
                throw new XWSSecurityException("Validation of self signed certificate failed");
            }
        }

        //check keyUsage
        X509CertSelector certSelector = new X509CertSelector();
        certSelector.setCertificate(cert);
        PKIXBuilderParameters parameters;
        CertPathValidator certPathValidator = null;
        CertPath certPath = null;        
        List<Certificate> certChainList = new ArrayList<Certificate>();
        boolean caFound = false;
        Principal certChainIssuer = null;        
        int noOfEntriesInTrustStore = 0;
        boolean isIssuerCertMatched = false;
        
        try {
            Callback[] callbacks = null;
            CertStoreCallback csCallback = null;
            TrustStoreCallback tsCallback = null;

            if (tsCallback == null && csCallback == null) {
               csCallback = new CertStoreCallback();
               tsCallback = new TrustStoreCallback();
               callbacks = new Callback[] { csCallback, tsCallback };
            } else if (csCallback == null) {
               csCallback = new CertStoreCallback();
               callbacks = new Callback[] { csCallback };
            } else if (tsCallback == null) {
               tsCallback = new TrustStoreCallback();
               callbacks = new Callback[] { tsCallback };
            }

            
           try {
             _handler.handle(callbacks);
           } catch (Exception e) {
               log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("Validate an X509Certificate"),
                    new Object[] { "Validate an X509Certificate"});
             throw new XWSSecurityException(e);
           }
           
            Certificate[] certChain = null;
            String certAlias = tsCallback.getTrustStore().getCertificateAlias(cert);
            if(certAlias!= null){
                certChain = tsCallback.getTrustStore().getCertificateChain(certAlias);
            }
            if(certChain == null){
                certChainList.add(cert);
                certChainIssuer = cert.getIssuerX500Principal();
                noOfEntriesInTrustStore = tsCallback.getTrustStore().size();                
	    }else{
		certChainList = Arrays.asList(certChain);
	    }            
            while(!caFound && noOfEntriesInTrustStore-- != 0 && certChain == null){                
                Enumeration aliases = tsCallback.getTrustStore().aliases();                
                while (aliases.hasMoreElements()){
                    String alias = (String) aliases.nextElement();                                      
                    Certificate certificate = tsCallback.getTrustStore().getCertificate(alias);                    
                    if (certificate == null || !"X.509".equals(certificate.getType()) || certChainList.contains(certificate)) {
                        continue;
                    }
                    X509Certificate x509Cert = (X509Certificate) certificate;                    
                    if(certChainIssuer.equals(x509Cert.getSubjectX500Principal())){
                        certChainList.add(certificate);
                        if(x509Cert.getSubjectX500Principal().equals(x509Cert.getIssuerX500Principal())){
                            caFound = true;                            
                            break;
                        }else{                            
                            certChainIssuer = x509Cert.getIssuerDN();                            
                            if(!isIssuerCertMatched){
	                        isIssuerCertMatched = true;
                            }
                        }
                    }else{
                        continue;
                    }
                }
                if(!caFound){
                    if(!isIssuerCertMatched){                        
                        break;                        
                    }else{
                        isIssuerCertMatched = false;
                    }
                }
            }
            try{                                                
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                certPath = cf.generateCertPath(certChainList);
                certPathValidator = CertPathValidator.getInstance("PKIX");
            }catch(Exception e){
                log.log(Level.SEVERE, com.sun.xml.wss.logging.impl.misc.LogStringsMessages.WSS_1518_FAILEDTO_VALIDATE_CERTIFICATE(), e);
                throw new CertificateValidationCallback.CertificateValidationException(e.getMessage(), e);
            }
           
            parameters = new PKIXBuilderParameters(tsCallback.getTrustStore(), certSelector);
            parameters.setRevocationEnabled(revocationEnabled);
            parameters.addCertStore(csCallback.getCertStore());
            
        } catch (Exception e) {
            // Log Message
            log.log(Level.SEVERE, LogStringsMessages.WSS_0223_FAILED_CERTIFICATE_VALIDATION(), e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        e.getMessage(), e);
        }

        try {            
            certPathValidator.validate(certPath, parameters);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0223_FAILED_CERTIFICATE_VALIDATION(), e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        e.getMessage(), e);
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE,"Certificate Validation called on certificate " + cert.getSubjectDN());
        }
        
        return true;
    }

     private Subject initJAASKeyStoreLoginModule() {
        if (jaasLoginModuleForKeystore == null) {
            return null;
        }
        LoginContext lc = null;
        try {
            if (keyStoreCBH != null) {
                keystoreCbHandlerClass = (CallbackHandler) loadClass(keyStoreCBH).newInstance();
                lc = new LoginContext(jaasLoginModuleForKeystore, keystoreCbHandlerClass);
            }else {
                lc = new LoginContext(jaasLoginModuleForKeystore);
            }
            lc.login();
            return lc.getSubject();
        } catch (InstantiationException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0817_KEYSTORE_LOGIN_MODULE_LOGIN_ERROR(), ex);
             throw new XWSSecurityRuntimeException(ex);
        } catch (IllegalAccessException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0817_KEYSTORE_LOGIN_MODULE_LOGIN_ERROR(), ex);
             throw new XWSSecurityRuntimeException(ex);
        } catch (XWSSecurityException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0817_KEYSTORE_LOGIN_MODULE_LOGIN_ERROR(), ex);
             throw new XWSSecurityRuntimeException(ex);
        } catch (LoginException ex) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0817_KEYSTORE_LOGIN_MODULE_LOGIN_ERROR(), ex);
            throw new XWSSecurityRuntimeException(ex);
        }       
    }

    private boolean isTrustedSelfSigned(X509Certificate cert) throws XWSSecurityException {
        try {
            Callback[] callbacks = null;
            CertStoreCallback csCallback = null;
            TrustStoreCallback tsCallback = null;

            if (tsCallback == null && csCallback == null) {
                csCallback = new CertStoreCallback();
                tsCallback = new TrustStoreCallback();
                callbacks = new Callback[]{csCallback, tsCallback};
            } else if (csCallback == null) {
                csCallback = new CertStoreCallback();
                callbacks = new Callback[]{csCallback};
            } else if (tsCallback == null) {
                tsCallback = new TrustStoreCallback();
                callbacks = new Callback[]{tsCallback};
            }

            try {
                _handler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("Validate an X509Certificate"),
                        new Object[]{"Validate an X509Certificate"});
                throw new XWSSecurityException(e);
            }

            if (tsCallback.getTrustStore() == null) {
                return false;
            }

            Enumeration aliases = tsCallback.getTrustStore().aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate certificate = tsCallback.getTrustStore().getCertificate(alias);
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
            log.log(Level.SEVERE, LogStringsMessages.WSS_0223_FAILED_CERTIFICATE_VALIDATION(), e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        e.getMessage(), e);
        }
    }
    
    /**
     * @param keyIdMatch
     *            KeyIdentifier to search for
     * @return the matching Certificate
     */
    public X509Certificate getMatchingCertificate(Map context, byte[] keyIdMatch)
        throws XWSSecurityException {
        
        Subject subject = getSubject(context);
        if (subject != null) {
           Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
           if (set != null) {
              Iterator it = set.iterator();


              while (it.hasNext()) {
                 X500PrivateCredential cred = (X500PrivateCredential)it.next();
                 X509Certificate cert = cred.getCertificate();
                 if (matchesKeyIdentifier(keyIdMatch, cert))
                    return cert;
              }
           }
        }

        PrivateKeyCallback.Request request = new PrivateKeyCallback.SubjectKeyIDRequest(
                                                                    keyIdMatch);
        PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
        CertStoreCallback csCallback = new CertStoreCallback();
        TrustStoreCallback tsCallback = new TrustStoreCallback();

        Callback[] callbacks = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties props = new RuntimeProperties(context);
            callbacks = new Callback[]{props, pkCallback, tsCallback, csCallback};
        } else {
            callbacks = new Callback[]{pkCallback, tsCallback, csCallback};
        }
        try {
          _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.SubjectKeyIDRequest"),
                    new Object[] { "PrivateKeyCallback.SubjectKeyIDRequest"});
           throw new XWSSecurityException(e);
        }

        Certificate[] chain = pkCallback.getChain();
        if (chain != null) {
           if (chain.length == 1) {
               return (X509Certificate)chain[0];
           }
           for (int i=0; i<chain.length; i++) {
               X509Certificate x509Cert = (X509Certificate)chain[i]; 
               if (matchesKeyIdentifier(keyIdMatch, x509Cert))
                  return x509Cert;
           }  
        } 
 
        // if not found, look in CertStore followed by TrustStore
        CertStore certStore = csCallback.getCertStore();
        if (certStore != null) {
            CertSelector selector = null;
            /*if (this.certSelectorClass != null) {
                HashMap props = new HashMap();
                props.putAll(context);
                props.put(XWSSConstants.SUBJECTKEYIDENTIFIER, keyIdMatch);
                selector = XWSSUtil.getCertSelector(certSelectorClass, props);
            }*/
            if (selector == null) {
                selector = new KeyIdentifierCertSelector(keyIdMatch);
            }
            try {
                Collection certs = certStore.getCertificates(selector);
                if (!certs.isEmpty()) {
                    Iterator it = certs.iterator();
                    return (X509Certificate)it.next();
                }
            } catch (CertStoreException ex) {
                //ex.printStackTrace();
                log.log(Level.SEVERE, LogStringsMessages.WSS_0713_ERROR_IN_CERTSTORE_LOOKUP(),ex);
                throw new XWSSecurityException(ex);
            }   
        }
        
        KeyStore trustStore = tsCallback.getTrustStore();
        if (trustStore != null) { 
           X509Certificate otherPartyCert = getMatchingCertificate(keyIdMatch, trustStore);
           if (otherPartyCert != null) 
               return otherPartyCert;
        } 

        // if still not found, throw Exception  
        log.log(Level.SEVERE,LogStringsMessages.WSS_0706_NO_MATCHING_CERT(keyIdMatch),
                new Object[] { keyIdMatch });
        throw new XWSSecurityException(
            "No Matching Certificate for :"
                + new String(keyIdMatch) 
                + " found in KeyStore or TrustStore");
    }

    public X509Certificate getMatchingCertificate(Map context, BigInteger serialNumber, String issuerName)
        throws XWSSecurityException {

        Subject subject = getSubject(context);
        if (subject != null) {
           Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
           if (set != null) {
              Iterator it = set.iterator();
              while (it.hasNext()) {
                 X500PrivateCredential cred = (X500PrivateCredential)it.next();
                 X509Certificate x509Cert = cred.getCertificate();
                 BigInteger serialNo = x509Cert.getSerialNumber();
                 //Fix for WSIT issue 1590
                   X500Principal currentIssuerPrincipal = x509Cert.getIssuerX500Principal();
                   X500Principal issuerPrincipal = new X500Principal(issuerName);
                   if (serialNo.equals(serialNumber)
                           && currentIssuerPrincipal.equals(issuerPrincipal)) {
                       return x509Cert;
                   }
              }
           }
        }

        PrivateKeyCallback.Request request = new PrivateKeyCallback.IssuerSerialNumRequest(
                                                       new X500Principal(issuerName),
                                                       serialNumber);     
        PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
        TrustStoreCallback tsCallback = new TrustStoreCallback();
        CertStoreCallback csCallback = new CertStoreCallback();

        Callback[] callbacks = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties props = new RuntimeProperties(context);
            callbacks = new Callback[]{props, pkCallback, tsCallback, csCallback};
        } else {
            callbacks = new Callback[]{pkCallback, tsCallback, csCallback};
        }
        
        try {
          _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.IssuerSerialNumRequest"),
                    new Object[] { "PrivateKeyCallback.IssuerSerialNumRequest"});
           throw new XWSSecurityException(e);
        }

        Certificate[] chain = pkCallback.getChain();
        if (chain != null) {
           if (chain.length == 1) {
               return (X509Certificate)chain[0];
           }
           for (int i=0; i < chain.length; i++) {
               X509Certificate x509Cert = (X509Certificate)chain[i]; 
               if ( 
                   matchesIssuerSerialAndName(
                                   serialNumber,
                                   issuerName,
                                   x509Cert)) return x509Cert;
           }
        } else {           
            if ( log.isLoggable(Level.FINE)){
                log.log(Level.FINE, LogStringsMessages.WSS_0296_NULL_CHAIN_CERT());
            }
        } 
 
        // if not found, look in CertStore followed by TrustStore
        CertStore certStore = csCallback.getCertStore();
        if (certStore != null) {
            CertSelector selector = null;
            /*if (this.certSelectorClass != null) {
                HashMap props = new HashMap();
                props.putAll(context);
                props.put(XWSSConstants.ISSUERNAME,issuerName);
                props.put(XWSSConstants.ISSUERSERIAL,serialNumber);
                selector = XWSSUtil.getCertSelector(certSelectorClass, props);
            }*/
            if (selector == null) {
                selector = new IssuerNameAndSerialCertSelector(serialNumber, issuerName);
            }
            try {
                Collection certs = certStore.getCertificates(selector);
                if (!certs.isEmpty()) {
                    Iterator it = certs.iterator();
                    return (X509Certificate)it.next();
                }
            } catch (CertStoreException ex) {
                //ex.printStackTrace();
                log.log(Level.SEVERE, LogStringsMessages.WSS_0713_ERROR_IN_CERTSTORE_LOOKUP(),ex);
                throw new XWSSecurityException(ex);
            }   
        }
        
        // if not found, look in Truststore
        KeyStore trustStore = tsCallback.getTrustStore();
        if (trustStore != null) { 
            X509Certificate otherPartyCert = getMatchingCertificate(serialNumber, 
                                                                    issuerName,
                                                                    trustStore);
            if (otherPartyCert != null) 
                return otherPartyCert;
        } else {
            // log
            log.log(Level.SEVERE, LogStringsMessages.WSS_0707_NULL_TRUSTSTORE());
        }

        // if still not found, throw Exception    
        log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT( issuerName +" : " + serialNumber),
                new Object[] { issuerName +" : " + serialNumber });
        throw new XWSSecurityException(
            "No Matching Certificate for :" + issuerName +" : " + serialNumber
                + " found in KeyStore or TrustStore");
    }

    /**
     * @param keyIdMatch
     *            KeyIdentifier to search for
     * @return the matching Certificate
     */
    public X509Certificate getMatchingCertificate(Map context, byte[] keyIdMatch, String valueType)
        throws XWSSecurityException {
        
        X509Certificate certificate = XWSSUtil.matchesProgrammaticInfo(context, keyIdMatch, valueType);
        if (certificate != null) {
            return certificate;
        }
        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)){
            return getMatchingCertificate(context, keyIdMatch);
        } 
        
        if (!MessageConstants.THUMB_PRINT_TYPE.equals(valueType)) {
            throw new XWSSecurityException(
                "Internal Error : Unsupported Valuetype :" 
                    + valueType + " passed to getMatchingCertificate()");                
        }
        Subject subject = getSubject(context);
        if (subject != null) {
           Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
           if (set != null) {
              Iterator it = set.iterator();
              while (it.hasNext()) {
                 X500PrivateCredential cred = (X500PrivateCredential)it.next();
                 X509Certificate cert = cred.getCertificate();
                 if (matchesThumbPrint(keyIdMatch, cert))
                    return cert;
              }
           }
        }

        PrivateKeyCallback.Request request = new PrivateKeyCallback.DigestRequest(keyIdMatch, "SHA-1");
        PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
        TrustStoreCallback tsCallback = new TrustStoreCallback();
        CertStoreCallback  csCallback = new CertStoreCallback();

        Callback[] callbacks = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties props = new RuntimeProperties(context);
            callbacks = new Callback[]{props, pkCallback, tsCallback, csCallback};
        } else {
            callbacks = new Callback[]{pkCallback, tsCallback, csCallback};
        }
        try {
          _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.SubjectKeyIDRequest"),
                    new Object[] { "PrivateKeyCallback.SubjectKeyIDRequest"});
           throw new XWSSecurityException(e);
        }

        Certificate[] chain = pkCallback.getChain();
        if (chain != null) {
           if (chain.length == 1) {
               return (X509Certificate)chain[0];
           }
           for (int i=0; i<chain.length; i++) {
               X509Certificate x509Cert = (X509Certificate)chain[i];
               //why do i need to check again here, it is waste of time
               if (matchesThumbPrint(keyIdMatch, x509Cert)) {
                   return x509Cert;
               }
           }  
        } 
 
         // if not found, look in CertStore followed by TrustStore
        CertStore certStore = csCallback.getCertStore();
        if (certStore != null) {
            CertSelector selector = null;
            /*if (this.certSelectorClass != null) {
                HashMap props = new HashMap();
                props.putAll(context);
                props.put(XWSSConstants.THUMBPRINT,keyIdMatch);
                selector = XWSSUtil.getCertSelector(certSelectorClass, props);
            }*/
            if (selector == null) {
                selector = new DigestCertSelector(keyIdMatch, MessageConstants.SHA_1);
            }
            try {
                Collection certs = certStore.getCertificates(selector);
                if (!certs.isEmpty()) {
                    Iterator it = certs.iterator();
                    return (X509Certificate)it.next();
                }
            } catch (CertStoreException ex) {
                //ex.printStackTrace();
                log.log(Level.SEVERE, LogStringsMessages.WSS_0713_ERROR_IN_CERTSTORE_LOOKUP(),ex);
                throw new XWSSecurityException(ex);
            }   
        }
       
        // if not found, look in Truststore 
        KeyStore trustStore = tsCallback.getTrustStore();
        if (trustStore != null) { 
           X509Certificate otherPartyCert = getMatchingCertificate(keyIdMatch, trustStore, valueType);
           if (otherPartyCert != null) return otherPartyCert;
        } 

        // if still not found, throw Exception   
        log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(keyIdMatch),
                new Object[] { keyIdMatch });
        throw new XWSSecurityException(
            "No Matching Certificate for :"
                + new String(keyIdMatch)
                + " found in KeyStore or TrustStore");
    }

    public SecretKey getSecretKey(Map context, String alias, boolean encryptMode)
        throws XWSSecurityException {
        /*
           Use SecretKeyCallback 
        */
        SecretKeyCallback.Request request = new SecretKeyCallback.AliasRequest(alias);
        SecretKeyCallback skCallback = new SecretKeyCallback(request);
        Callback[] callbacks = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties props = new RuntimeProperties(context);
            callbacks = new Callback[]{props, skCallback};
        } else {
            callbacks = new Callback[]{skCallback};
        }
        try {
           _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION( "SecretKeyCallback.AliasRequest"),
                    new Object[] { "SecretKeyCallback.AliasRequest"});
            throw new XWSSecurityException(e);
        }
 
        return (SecretKey) skCallback.getKey();
    }

    public X509Certificate getCertificate(Map context, String alias, boolean forSigning)
        throws XWSSecurityException {
        String actualAlias = alias;
        X509Certificate cert = null;
        if (alias == null || "".equals(alias)) {
            if (forSigning) {
                //Look for CERTIFICATE_PROPERTY
                if (context != null) {
                    Object obj = context.get(XWSSConstants.CERTIFICATE_PROPERTY);
                    if (obj instanceof X509Certificate) {
                        return (X509Certificate) obj;
                    } else {
                        if (obj != null) {
                            throw new RuntimeException("CERTIFICATE_PROPERTY does not seem to be set to a valid X509Ceritificate");
                        }
                    }
                }
                if (this.myAlias != null) {
                    actualAlias = this.myAlias;
                } else {
                    //use the Alias selector if present.
                    //NOTE: the keystoreCertSelector is actually an AliasSelector it
                    // cannot be a certSelector (especially JSR 196 does not allow browsing
                    // a keystore) to do cert selection.
                    if (this.keystoreCertSelectorClass != null) {
                        AliasSelector selector = null;
                        try {
                            selector = (AliasSelector)
                                this.keystoreCertSelectorClass.newInstance();
                        } catch (IllegalAccessException ex) {
                            log.log(Level.SEVERE,LogStringsMessages.WSS_0811_EXCEPTION_INSTANTIATING_ALIASSELECTOR(), ex);
                            throw new RuntimeException(ex);
                        } catch (InstantiationException ex) {
                            log.log(Level.SEVERE,LogStringsMessages.WSS_0811_EXCEPTION_INSTANTIATING_ALIASSELECTOR(), ex);
                            throw new RuntimeException(ex);
                        }
                        actualAlias = selector.select(context);
                    }
                }
            } else {
                //for encryption
                if (context != null) {
                    Object obj = context.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
                    if (obj instanceof X509Certificate) {
                        return (X509Certificate) obj;
                    }
                }
                if (this.peerEntityAlias != null) {
                    actualAlias = this.peerEntityAlias;
                }
            }
        }
       
        PrivateKeyCallback pkCallback = null;
        if (forSigning) {
            try {
                Subject subject = getSubject(context);
                if (subject != null) {
                    Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
                    if (set != null) {
                        Iterator it = set.iterator();
                        while (it.hasNext()) {
                            X500PrivateCredential cred = (X500PrivateCredential)it.next();
                            if (cred.getAlias().equals(actualAlias))
                                return cred.getCertificate();
                        }
                    }
                }
                
                PrivateKeyCallback.Request request = new PrivateKeyCallback.AliasRequest(actualAlias);
                pkCallback = new PrivateKeyCallback(request);

                Callback[] callbacks = null;
                if (this.useXWSSCallbacks) {
                    RuntimeProperties props = new RuntimeProperties(context);
                    callbacks = new Callback[]{props, pkCallback};
                } else {
                    callbacks = new Callback[]{pkCallback};
                }
                _handler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                throw new XWSSecurityException(e);
            }
            
            Certificate[] chain = pkCallback.getChain();
            if (chain != null){
                cert = (X509Certificate)chain[0];
            } else {
                if (log.isLoggable(Level.FINE)){
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0296_NULL_CHAIN_CERT());
                }
            }
        } else {
            //for encryption
            if (actualAlias != null && !"".equals(actualAlias)) {
                TrustStoreCallback tsCallback = new TrustStoreCallback();
                Callback[] _callbacks = null;
                if (this.useXWSSCallbacks) {
                    RuntimeProperties props = new RuntimeProperties(context);
                    _callbacks = new Callback[]{props, tsCallback};
                } else {
                    _callbacks = new Callback[]{tsCallback};
                }
                try {
                    _handler.handle(_callbacks);
                } catch (IOException ex) {
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                    throw new XWSSecurityException(ex);
                } catch (UnsupportedCallbackException ex) {
                    log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                    throw new XWSSecurityException(ex);
                }
                if (tsCallback.getTrustStore() != null) {
                    try {
                        cert = (X509Certificate)tsCallback.getTrustStore().getCertificate(actualAlias);
                    } catch (KeyStoreException ex) {
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                        throw new XWSSecurityException(ex);
                    }
                }
            } else {
                
                //actualAlias == null || "".equals(actualAlias)
                // first if certStore configured then give it a chance
                if (this.certSelectorClass != null) {
                    CertStoreCallback csCallback = new CertStoreCallback();

                    Callback[] _callbacks = null;
                    if (this.useXWSSCallbacks) {
                        RuntimeProperties props = new RuntimeProperties(context);
                        _callbacks = new Callback[]{props, csCallback};
                    } else {
                        _callbacks = new Callback[]{csCallback};
                    }
                    try {
                        _handler.handle(_callbacks);
                    } catch (IOException ex) {
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                        throw new XWSSecurityException(ex);
                    } catch (UnsupportedCallbackException ex) {
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                        throw new XWSSecurityException(ex);
                    }
                    
                    if (csCallback.getCertStore() != null) {
                        CertSelector selector = XWSSUtil.getCertSelector(certSelectorClass, context);
                        if (selector != null) {
                            Collection certs = null;
                            try {
                                certs = csCallback.getCertStore().getCertificates(selector);
                            } catch (CertStoreException ex) {
                                log.log(Level.SEVERE, LogStringsMessages.WSS_0813_FAILEDTO_GETCERTIFICATE(), ex);
                                throw new RuntimeException(ex);
                            }
                            if (certs.size() > 0) {
                                cert = (X509Certificate)certs.iterator().next();
                            }
                        }
                    }
                }
                
                if (cert == null && this.truststoreCertSelectorClass != null) {
                    
                    TrustStoreCallback tsCallback = new TrustStoreCallback();
                    Callback[] _callbacks = null;
                    if (this.useXWSSCallbacks) {
                        RuntimeProperties props = new RuntimeProperties(context);
                        _callbacks = new Callback[]{props, tsCallback};
                    } else {
                        _callbacks = new Callback[]{tsCallback};
                    }
                    try {
                        _handler.handle(_callbacks);
                    } catch (IOException ex) {
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                        throw new XWSSecurityException(ex);
                    } catch (UnsupportedCallbackException ex) {
                        log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(alias), new Object[] {alias});
                        throw new XWSSecurityException(ex);
                    }
                    
                    KeyStore trustStore = tsCallback.getTrustStore();
                    
                    if (trustStore != null) {
                        if (this.truststoreCertSelectorClass != null) {
                            CertSelector selector = XWSSUtil.getCertSelector(truststoreCertSelectorClass, context);
                            if (selector != null) {
                                Enumeration aliases=null;
                                try {
                                    aliases = trustStore.aliases();
                                } catch (KeyStoreException ex) {
                                    log.log(Level.SEVERE, LogStringsMessages.WSS_0813_FAILEDTO_GETCERTIFICATE(), ex);
                                    throw new RuntimeException(ex);
                                }
                                while (aliases.hasMoreElements()) {
                                    String currAlias = (String) aliases.nextElement();
                                    Certificate thisCertificate = null;
                                    try {
                                        thisCertificate = trustStore.getCertificate(currAlias);
                                    } catch (KeyStoreException ex) {
                                        log.log(Level.SEVERE, LogStringsMessages.WSS_0813_FAILEDTO_GETCERTIFICATE(), ex);
                                        throw new RuntimeException(ex);
                                    }
                                    if ((thisCertificate instanceof X509Certificate)
                                    && selector.match(thisCertificate)) {
                                        return (X509Certificate)thisCertificate;
                                    }
                                }
                            }
                        }
                    }
                } 
                if (cert == null) {
                    cert = getDynamicCertificate(context);
                }
            }
        }
        
        if (cert == null) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0221_CANNOT_LOCATE_CERT(actualAlias));
           throw new XWSSecurityException(
             "Unable to locate certificate for the alias '" + actualAlias + "'");
        } 

        return cert;
    }

  
    private boolean isMyCert(X509Certificate certificate, Map context) {
        try {
            X509Certificate cert = getDefaultCertificate(context);
            if (cert != null && cert.equals(certificate)) {
                return true;
            }
        } catch (XWSSecurityException ex) {
            //ignore
        }
        return false;
    }

    private Class loadUsingResourceLoader(String classname) {
        Class ret = null;
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Entered loadUsingResourceLoader to load class.." + classname);
        }
        if (container != null) {
            // find the ResourceLoader implementation
            final ResourceLoader loader = container.getSPI(ResourceLoader.class);
            if (loader != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Obtained Non null ResourceLoader instance....");
                }
                try {
                    // JCAP's SPI implementation will return the directory from where the class(es) can be loaded
                    final URL classpathUrl = loader.getResource(classname);
                    // load the class using the URL
                    ClassLoader parent = this.getClass().getClassLoader();
                    URLClassLoader classloader = URLClassLoader.newInstance(new URL[]{classpathUrl}, parent);
                    ret = classloader.loadClass(classname);
                    return ret;
                } catch (ClassNotFoundException ex) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "Failed load class using ResourceLoader instance....", ex);
                    }
                } catch (MalformedURLException e) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "Failed load class using ResourceLoader instance....", e);
                    }
                }
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Failed to obtain ResourceLoader instance....");
                }
            }
        } else {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Failed to obtain \"Container\" for getting ResourceLoader SPI ....");
            }
        }
        return null;
    }

    private boolean matchesKeyIdentifier(
        byte[] keyIdMatch,
        X509Certificate x509Cert) throws XWSSecurityException {

        byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
        if (keyId == null) {
            // Cert does not contain a key identifier
            return false;
        }

        if (Arrays.equals(keyIdMatch, keyId)) {
            return true;
        }
        return false;
    }

      
    private boolean matchesThumbPrint(
        byte[] keyIdMatch,
        X509Certificate x509Cert) throws XWSSecurityException {

        byte[] keyId = XWSSUtil.getThumbprintIdentifier(x509Cert);
        if (keyId == null) {
            // Cert does not contain a key identifier
            return false;
        }

        if (Arrays.equals(keyIdMatch, keyId)) {
            return true;
        }
        return false;
    }

    private X509Certificate getMatchingCertificate(
        byte[] keyIdMatch,
        KeyStore kStore)
        throws XWSSecurityException {

        if (kStore == null) {
            return null;
        }

        try {
            Enumeration enum1 = kStore.aliases();
            while (enum1.hasMoreElements()) {
                String alias = (String) enum1.nextElement();

                Certificate cert = kStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }

                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }

                if (Arrays.equals(keyIdMatch, keyId)) {
                    return x509Cert;
                }
            }
        } catch (KeyStoreException kEx) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(keyIdMatch),
                    new Object[] { keyIdMatch });
            throw new XWSSecurityException(
                "No Matching Certificate for :" 
                    + new String(keyIdMatch) + " found in KeyStore.", kEx);    
        }
        return null;
    }

    private X509Certificate getMatchingCertificate(
        byte[] keyIdMatch,
        KeyStore kStore,
        String valueType)
        throws XWSSecurityException {

        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)){
            return getMatchingCertificate(keyIdMatch, kStore);
        }
        if (!MessageConstants.THUMB_PRINT_TYPE.equals(valueType)) {
            throw new XWSSecurityException(
                "Internal Error : Unsupported Valuetype :" 
                    + valueType + " passed to getMatchingCertificate()");                
        }
        // now handle thumbprint here
        if (kStore == null) {
            return null;
        }

        try {
            Enumeration enum1 = kStore.aliases();
            while (enum1.hasMoreElements()) {
                String alias = (String) enum1.nextElement();

                Certificate cert = kStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }

                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = XWSSUtil.getThumbprintIdentifier(x509Cert);

                if (Arrays.equals(keyIdMatch, keyId)) {
                    return x509Cert;
                }
            }
        } catch (KeyStoreException kEx) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(keyIdMatch),
                    new Object[] { keyIdMatch });
            throw new XWSSecurityException(
                "No Matching Certificate for :" 
                    + new String(keyIdMatch) + " found in KeyStore.", kEx);                
        }
        return null;
    }
    
    private boolean matchesIssuerSerialAndName(
        BigInteger serialNumberMatch,
        String issuerNameMatch,
        X509Certificate x509Cert) {

        BigInteger serialNumber = x509Cert.getSerialNumber();
        //Fix for WSIT issue 1590
        X500Principal currentIssuerPrincipal = x509Cert.getIssuerX500Principal();
        X500Principal issuerPrincipal = new X500Principal(issuerNameMatch);

        if (serialNumber.equals(serialNumberMatch)
            && currentIssuerPrincipal.equals(issuerPrincipal)) {
            return true;
        }
        return false;
    }

    private X509Certificate getMatchingCertificate(
        BigInteger serialNumber,
        String issuerName,
        KeyStore kStore)
        throws XWSSecurityException {

        if (kStore == null) {
            return null;
        }
        try {
            Enumeration enum1 = kStore.aliases();
            while (enum1.hasMoreElements()) {
                String alias = (String) enum1.nextElement();

                Certificate cert = kStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }

                X509Certificate x509Cert = (X509Certificate) cert;
                //Fix for WSIT issue 1590
                X500Principal currentIssuerPrincipal = x509Cert.getIssuerX500Principal();
                X500Principal issuerPrincipal = new X500Principal(issuerName);

                BigInteger thisSerialNumber = x509Cert.getSerialNumber();

                if (thisSerialNumber.equals(serialNumber) && currentIssuerPrincipal.getName().equals(issuerPrincipal)) {
                    return x509Cert;
                }
            }
        } catch (KeyStoreException kEx) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(issuerName + " : " + serialNumber),
                    new Object[] { issuerName + " : " + serialNumber });
            throw new XWSSecurityException(
                "No Matching Certificate for :" 
                    + issuerName + " : " + serialNumber + " found in KeyStore.", kEx);                
        }
        return null;
    }

	private X509Certificate getMatchingCertificate(
        PublicKey publicKey,
        KeyStore kStore)
        throws XWSSecurityException {
                                                                                                                                                                                    
        if (kStore == null) {
            return null;
        }
        try {
            Enumeration enum1 = kStore.aliases();
            while (enum1.hasMoreElements()) {
                String alias = (String) enum1.nextElement();

                Certificate cert = kStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
				X509Certificate x509Cert = (X509Certificate) cert;
                if (x509Cert.getPublicKey().equals(publicKey))
					return x509Cert;
            }
        } catch (KeyStoreException kEx) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(publicKey),
                    new Object[] { publicKey });
            throw new XWSSecurityException(
                "No Matching Certificate for :"
                    + publicKey + " found in KeyStore.", kEx);
        }
        return null;
    }

    public void updateOtherPartySubject(
        Subject subject,
        String username,
        String password) {
        
        CallerPrincipalCallback pvCallback = new CallerPrincipalCallback(subject, username);
        Callback[] callbacks = new Callback[] { pvCallback };
        try {
           _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION( "CallerPrincipalCallback"),
                    new Object[] { "CallerPrincipalCallback"});
           throw new XWSSecurityRuntimeException(e);
        }
    }
    
    public void updateOtherPartySubject(
        final Subject subject,
        final X509Certificate cert) {
      
       Principal principal = cert.getSubjectX500Principal();
       AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
            public Object run() {
                subject.getPublicCredentials().add(cert);
                return null;
            }
        });
        
        CallerPrincipalCallback pvCallback = new CallerPrincipalCallback(subject,principal);
        Callback[] callbacks = new Callback[] { pvCallback };
        try {
           _handler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION( "CallerPrincipalCallback"),
                    new Object[] { "CallerPrincipalCallback"});
           throw new XWSSecurityRuntimeException(e);
        }
    }

    public void updateOtherPartySubject(
        final Subject subject,
        final Assertion assertion) {
         if (sValidator instanceof SAMLValidator) {
                //nothing to do
                return;
        }
        AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
            public Object run() {
                subject.getPublicCredentials().add(assertion);
                return null;
            }
        });
    }


    public PublicKey getPublicKey(Map context, BigInteger serialNumber, String issuerName)
        throws XWSSecurityException {
      return getCertificate(context, serialNumber, issuerName).getPublicKey();
    }

    //TODO: unused method
    public PublicKey getPublicKey(String keyIdentifier)
        throws XWSSecurityException {
        try {
            return getMatchingCertificate(null,
                getDecodedBase64EncodedData(keyIdentifier))
                .getPublicKey();
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(keyIdentifier),
                    new Object[] { keyIdentifier });
        throw new XWSSecurityException("No Matching Certificate for :"
                + keyIdentifier + " found in KeyStore ");            
        }
    }

    public PublicKey getPublicKey(Map context, byte[] keyIdentifier)
        throws XWSSecurityException {
        try {
            return getCertificate(context, keyIdentifier).getPublicKey();
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0706_NO_MATCHING_CERT(keyIdentifier),
                    new Object[] { keyIdentifier });
            throw new XWSSecurityException(e);
        }
    }
    
    public PublicKey getPublicKey(Map context, byte[] identifier, String valueType)
    throws XWSSecurityException {
        return getCertificate(context, identifier, valueType).getPublicKey();
    }

    private byte[] getDecodedBase64EncodedData(String encodedData)
        throws XWSSecurityException {
        try {
            return Base64.decode(encodedData);
        } catch (Base64DecodingException e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0144_UNABLETO_DECODE_BASE_64_DATA(e.getMessage()) ,e);
            throw new SecurityHeaderException(
                "Unable to decode Base64 encoded data", e);
        }
    }

    public X509Certificate getCertificate(
        Map context,
        BigInteger serialNumber,
        String issuerName)
        throws XWSSecurityException {
        if (context != null) {
            Object obj = context.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
            X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, serialNumber, issuerName);
            if (cert != null) {
                return cert;
            }
        }
        return getMatchingCertificate(context, serialNumber, issuerName);
    }

    public X509Certificate getCertificate(String keyIdentifier)
        throws XWSSecurityException {
        try {
            byte[] decoded = getDecodedBase64EncodedData(keyIdentifier);
            return getMatchingCertificate(null, decoded);
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0711_ERROR_MATCH_CERT_FOR_DECODED_STRING(), e);
            throw new XWSSecurityException(e);
        }
    }

    public PrivateKey getPrivateKey(Map context, PublicKey publicKey, boolean forSign) {
       // throw new UnsupportedOperationException("getPrivateKey(Map context, PublicKey publicKey, boolean forSign)");
       //since we cannot browse the keystore for a matching public key, try and get the self certificate
        if (XWSSUtil.matchesProgrammaticInfo(context, publicKey) != null) {
            PrivateKey key = XWSSUtil.getProgrammaticPrivateKey(context);
            if (key != null) {
                return key;
            }
        }
        PublicKey myAliasKey = null;
        try {
            myAliasKey = this.getCertificate(context, this.myAlias, true).getPublicKey();
            if (myAliasKey.equals(publicKey)) {
                PrivateKey ret = getPrivateKey(context, this.myAlias);
                return ret;
            }
        } catch (XWSSecurityException ex) {
            //TODO: log here
            throw new XWSSecurityRuntimeException(ex);
        }
        //TODO: Log here
        throw new XWSSecurityRuntimeException("Could not locate Matching Private Key for: " + publicKey);
        
    }
    
    public X509Certificate getCertificate(Map context, byte[] ski) {
        X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(context, ski, MessageConstants.KEY_INDETIFIER_TYPE);
        if (cert != null) {
            return cert;
        }
        try {
            return this.getMatchingCertificate(context, ski);
        } catch (XWSSecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public X509Certificate getCertificate(Map context, PublicKey publicKey, boolean forSign)
        throws XWSSecurityException {

        if (context != null) {
            Object obj = context.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
            X509Certificate certificate = XWSSUtil.matchesProgrammaticInfo(obj, publicKey);
            if (certificate != null) {
                return certificate;
            }
        }
        Subject subject = getSubject(context);
        if (subject != null) {
            Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
	        if (set != null) {
    	   	    Iterator it = set.iterator();
		        while (it.hasNext()) {
        	        X500PrivateCredential cred = (X500PrivateCredential)it.next();
	                X509Certificate cert = cred.getCertificate();
               		if (cert.getPublicKey().equals(publicKey))
		                return cert;
        	    }
         	}
	    }
                                                                                                                                                             
        if (!forSign) {
            CertStoreCallback csCallback = new CertStoreCallback();
            TrustStoreCallback tsCallback = new TrustStoreCallback();

            Callback[] callbacks = null;
            if (this.useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                callbacks = new Callback[]{props, csCallback, tsCallback};
            } else {
                callbacks = new Callback[]{csCallback, tsCallback};
            }
                                                                                                                                                             
            try {
	            _handler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("CertStoreCallback"),
                    new Object[] { "CertStoreCallback"});
           	    throw new XWSSecurityException(e);
	        }
            // look in CertStore followed by Truststore to get certificate of a publicKey passed as argument
            CertStore certStore = csCallback.getCertStore();
            if (certStore != null) {
                CertSelector selector = null;
                /*if (this.certSelectorClass != null) {
                    HashMap props = new HashMap();
                    props.putAll(context);
                    props.put(XWSSConstants.PUBLICKEY, publicKey);
                    selector = XWSSUtil.getCertSelector(certSelectorClass, props);
                }*/
                if (selector == null) {
                    selector = new PublicKeyCertSelector(publicKey);
                }
                try {
                    Collection certs = certStore.getCertificates(selector);
                    if (!certs.isEmpty()) {
                        Iterator it = certs.iterator();
                        return (X509Certificate) it.next();
                    }
                } catch (CertStoreException ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0713_ERROR_IN_CERTSTORE_LOOKUP(), ex);
                    throw new XWSSecurityException(ex);
                }
            }

            KeyStore trustStore = tsCallback.getTrustStore();
            if (trustStore != null) {
                X509Certificate otherPartyCert = getMatchingCertificate(publicKey, trustStore);
                if (otherPartyCert != null)
                    return otherPartyCert;
            }
        } else {
            // search in keystore
            //TODO: not required currently, and also we cannot browse the GF keystore via 196 callbacks 
        }
                                                                                                                                                             
        // if still not found, throw Exception
        log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION(publicKey),
            new Object[] { publicKey });
        throw new XWSSecurityException(
            "No Matching Certificate for :"
            + publicKey
            + " found in KeyStore or TrustStore");                
    }
    
    public X509Certificate getCertificate(Map context, byte[] identifier, String valueType)
    throws XWSSecurityException {
        if (context != null) {
            Object obj = context.get(XWSSConstants.SERVER_CERTIFICATE_PROPERTY);
            X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(obj, identifier, valueType);
            if (cert != null) {
                return cert;
            }
        }
        if (com.sun.xml.wss.impl.MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return this.getMatchingCertificate(context,identifier);
        }
        //Handle Thumbprint Reference here
        return this.getMatchingCertificate(context, identifier, valueType);
    }
    

    public boolean validateSamlIssuer(String issuer) {
        return true;
    }

    public boolean validateSamlUser(String user, String domain, String format) {
        return true;
    }
    @SuppressWarnings("unchecked")
    public void setSubject(Subject subject, Map context) {
        context.put(MessageConstants.SELF_SUBJECT, subject);
    }
    @SuppressWarnings("unchecked")
    public void setRequesterSubject(Subject subject, Map context) {
        context.put(MessageConstants.AUTH_SUBJECT, subject);
    }

    public Subject getSubject() {
        return null;
    }

    public Subject getSubject(Map context) {
        if (context == null) {
            return null;
        }
        if (loginContextSubjectForKeystore != null){ //giving preference to jaas keystore logincontext's subject
            return  loginContextSubjectForKeystore;
        }
        return (Subject)context.get(MessageConstants.SELF_SUBJECT);
    }

    public Subject getRequesterSubject(final Map context) {
        if (context == null) {
            return null;
        }
        Subject otherPartySubject = (Subject)context.get(MessageConstants.AUTH_SUBJECT);
        if (otherPartySubject != null) {
            return otherPartySubject;
        }
        otherPartySubject = (Subject) AccessController.doPrivileged(
                new PrivilegedAction<Object>() {
            @SuppressWarnings("unchecked")
            public Object run() {
                Subject otherPartySubj = new Subject();
                context.put(MessageConstants.AUTH_SUBJECT,otherPartySubj);
                return otherPartySubj;
            }
        }
        );
        return otherPartySubject;
    }

    private Date getGMTDateWithSkewAdjusted(Calendar c, boolean addSkew) {
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;

        if (addSkew)
            currentTime = currentTime + MAX_CLOCK_SKEW;
        else
            currentTime = currentTime - MAX_CLOCK_SKEW;

        c.setTimeInMillis(currentTime);
        return c.getTime();
    }

     
    public String getUsername(Map context) throws XWSSecurityException {
        if (context == null) {
            return null;
        }
        if (this.myUsername != null) {
            return this.myUsername;
        } else {
            String username = (String) context.get(com.sun.xml.wss.XWSSConstants.USERNAME_PROPERTY);
            if (username == null) {
                //the property  below is dedprecated for xwss usage
                username = (String) context.get(BindingProvider.USERNAME_PROPERTY);
            }
            if (username != null) {
                return username;
            }
        }
        
        NameCallback nameCallback = new NameCallback("Username: ");
        try {
            Callback[] cbs = null;
            if (useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                cbs = new Callback[]{props, nameCallback};

            } else {
                cbs = new Callback[]{nameCallback};
            }

            if (usernameCbHandler != null) {
                usernameHandler = (CallbackHandler) usernameCbHandler.newInstance();
                usernameHandler.handle(cbs);
                nameCallback.setName(((javax.security.auth.callback.NameCallback) cbs[0]).getName());
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Got NULL for Username Callback Handler");
                }
                if (!this.isAppClient) {
                    //cannot make a Callback since GF CBH will throw UnSupported Exception
                    // for a servlet client
                    return null;
                }
                _handler.handle(cbs);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("NameCallback"),
                    new Object[]{"NameCallback"});
            throw new RuntimeException(e);
        }

        return nameCallback.getName();
    }

     
    public String getPassword(Map context) throws XWSSecurityException {

        //actually check if myPassword starts with $ etc
        if (this.myPassword != null) {
            if (this.myPassword.startsWith("$")) {
                String alias = this.myPassword.substring(1);
                SecretKeyCallback.AliasRequest ar =
                        new SecretKeyCallback.AliasRequest(alias);
                SecretKeyCallback skcb = new SecretKeyCallback(ar);
                Callback[] callbacks = null;
                if (this.useXWSSCallbacks) {
                    RuntimeProperties props = new RuntimeProperties(context);
                    callbacks = new Callback[]{props, skcb};
                } else {
                    callbacks = new Callback[]{skcb};
                }
                try {
                    this._handler.handle(callbacks);
                    javax.crypto.SecretKey key = skcb.getKey();
                    byte[] password = key.getEncoded();
                    return new String(password);
                } catch (Exception ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION( "SecretKeyCallback.AliasRequest"),
                            new Object[] { "SecretKeyCallback.AliasRequest"});
                    throw new XWSSecurityException(ex);
                }
            }  else {
                return this.myPassword;
            }
        } else {
            String password = (String) context.get(com.sun.xml.wss.XWSSConstants.PASSWORD_PROPERTY);
            if (password == null) {
                //the property below is deprecated
                password = (String) context.get(BindingProvider.PASSWORD_PROPERTY);
            }
            if (password != null) {
                return password;
            } 
        }
        PasswordCallback pwdCallback = new PasswordCallback("Password: ", false);
        Callback[] cbs = null;
        if (this.useXWSSCallbacks) {
            RuntimeProperties props = new RuntimeProperties(context);
            cbs = new Callback[]{props, pwdCallback};
        } else {
            cbs = new Callback[]{pwdCallback};
        }
        try {
            if (passwordCbHandler != null) {
                passwordHandler = (CallbackHandler) passwordCbHandler.newInstance();
                passwordHandler.handle(cbs);
                char[] pass = ((javax.security.auth.callback.PasswordCallback) cbs[0]).getPassword();
                pwdCallback.setPassword(pass);
            } else {
                if (!this.isAppClient) {
                    //servlet client: 109
                    //cannot make callback so return null
                    return null;
                }
                if (this.useXWSSCallbacks) {
                    RuntimeProperties props = new RuntimeProperties(context);
                    cbs = new Callback[]{props, pwdCallback};
                } else {
                    cbs = new Callback[]{pwdCallback};
                }
                _handler.handle(cbs);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0225_FAILED_PASSWORD_VALIDATION_CALLBACK(), e);
            throw new RuntimeException(e);
        }

        if (pwdCallback.getPassword() == null) {
            return null;
        }

        return new String(pwdCallback.getPassword());
    }
  
    public boolean validateAndCacheNonce(Map context, String nonce, String created, long nonceAge) throws XWSSecurityException {
        NonceManager nonceMgr = null;
        if (this.mna != null) {
            nonceMgr = NonceManager.getInstance(this.maxNonceAge, (WSEndpoint)context.get(MessageConstants.WSENDPOINT));
        } else {
            nonceMgr = NonceManager.getInstance(nonceAge, (WSEndpoint)context.get(MessageConstants.WSENDPOINT));
        }   
        return nonceMgr.validateNonce(nonce, created);
    }


    public void validateTimestamp(Map context, String created,
               String expires, long maxClockSkew, long freshnessLimit)
               throws XWSSecurityException{
        
        
        if (this.tsValidator != null) {
            TimestampValidationCallback.UTCTimestampRequest request =
                    new TimestampValidationCallback.UTCTimestampRequest(
                    created,
                    expires,
                    maxClockSkew,
                    freshnessLimit);

            TimestampValidationCallback timestampValidationCallback =
                    new TimestampValidationCallback(request);
            ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
            timestampValidationCallback.setValidator(tsValidator);
            try {
                timestampValidationCallback.getResult();
                return;
            } catch (TimestampValidationCallback.TimestampValidationException e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0229_FAILED_VALIDATING_TIME_STAMP(), e);
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
            }
        }

        if (this.useXWSSCallbacks) {
            TimestampValidationCallback.UTCTimestampRequest request =
                    new TimestampValidationCallback.UTCTimestampRequest(
                    created,
                    expires,
                    maxClockSkew,
                    freshnessLimit);

            TimestampValidationCallback timestampValidationCallback =
                    new TimestampValidationCallback(request);

            ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[]{timestampValidationCallback};
            try {
                _handler.handle(callbacks);
                return;
            } catch (UnsupportedCallbackException e) {
                //ignore so we can use default
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0229_FAILED_VALIDATING_TIME_STAMP(), e);
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
            }
            
        }
        if (expiresBeforeCreated(created, expires)) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0232_EXPIRED_MESSAGE());
            XWSSecurityException xwsse = new XWSSecurityException("Message expired!");
            throw DefaultSecurityEnvironmentImpl.newSOAPFaultException(
                    MessageConstants.WSU_MESSAGE_EXPIRED,
                    "Message expired!",
                    xwsse);
        }

        validateCreationTime(context, created, maxClockSkew, freshnessLimit);
        validateExpirationTime(expires, maxClockSkew, freshnessLimit);
    }


   public void validateTimestamp(Map context, Timestamp timestamp, long maxClockSkew, long freshnessLimit)
   throws XWSSecurityException {
        validateTimestamp(context, timestamp.getCreated(), timestamp.getExpires(),
                maxClockSkew, freshnessLimit);
    }

    private static boolean expiresBeforeCreated (String creationTime, String expirationTime) throws XWSSecurityException {
        Date created = null;
        Date expires = null;
        try {
            try {
                synchronized(calendarFormatter1) {
                    created = calendarFormatter1.parse(creationTime);
                }
                if (expirationTime != null) {
                    synchronized(calendarFormatter1) {
                        expires = calendarFormatter1.parse(expirationTime);
                    }
                }
            
            } catch (java.text.ParseException pe) {
                synchronized(calendarFormatter2) {
                    created = calendarFormatter2.parse(creationTime);
                }
                if (expirationTime != null) {
                    synchronized(calendarFormatter2) {
                       expires = calendarFormatter2.parse(expirationTime);
                    }
                }
            }
         } catch (java.text.ParseException pe) {
             log.log(Level.SEVERE, LogStringsMessages.WSS_0394_ERROR_PARSING_EXPIRATIONTIME());
             throw new XWSSecurityException(pe.getMessage());
         }
                                                                                                                                                             
        if ((expires != null) && expires.before(created))
            return true;

        return false;
    }

    public void validateCreationTime(
    Map context,
    String creationTime,
    long maxClockSkew,
    long timestampFreshnessLimit)
    throws XWSSecurityException {
        
        if (this.tsValidator != null) {
            TimestampValidationCallback.UTCTimestampRequest request =
                    new TimestampValidationCallback.UTCTimestampRequest(
                    creationTime,
                    null,
                    maxClockSkew,
                    timestampFreshnessLimit);

            request.isUsernameToken(true);
            TimestampValidationCallback timestampValidationCallback =
                    new TimestampValidationCallback(request);

            ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
            timestampValidationCallback.setValidator(tsValidator);
            try {
                timestampValidationCallback.getResult();
                return;
            } catch (TimestampValidationCallback.TimestampValidationException e) {
                log.log(Level.SEVERE,LogStringsMessages.WSS_0229_FAILED_VALIDATING_TIME_STAMP(), e);
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
            }
        }
        
        if (this.useXWSSCallbacks) {
            TimestampValidationCallback.UTCTimestampRequest request =
                    new TimestampValidationCallback.UTCTimestampRequest(
                    creationTime,
                    null,
                    maxClockSkew,
                    timestampFreshnessLimit);

            TimestampValidationCallback timestampValidationCallback =
                    new TimestampValidationCallback(request);

            ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[]{timestampValidationCallback};
            try {
                _handler.handle(callbacks);
                return;
            } catch (UnsupportedCallbackException e) {
                //ignore so we can use default
            } catch (Exception e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0229_FAILED_VALIDATING_TIME_STAMP(), e);
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
            }
            
        }
        
        long maxClockSkewActual  = maxClockSkew;
        long freshnessLimitActual = timestampFreshnessLimit;
        
        if (this.mcs != null && this.maxClockSkewG >= 0) {
            maxClockSkewActual = this.maxClockSkewG;
        }
        if (this.tfl != null && this.timestampFreshnessLimitG > 0) {
            freshnessLimitActual = this.timestampFreshnessLimitG;    
        }
        
        Date created;
        try {
            synchronized(calendarFormatter1) {
                created = calendarFormatter1.parse(creationTime);
            }
        } catch (java.text.ParseException pe) {
            try {
                synchronized(calendarFormatter2) {
                    created = calendarFormatter2.parse(creationTime);
                }
            } catch (java.text.ParseException pe1) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0226_FAILED_VALIDATING_DEFAULT_CREATION_TIME(), pe1);
                throw new XWSSecurityException(
                    "Exception while parsing Creation Time :" + pe1.getMessage());
            }
        }
            
        Date current = null;
        try {
            current = getFreshnessAndSkewAdjustedDate(maxClockSkewActual, freshnessLimitActual);
          
        } catch (java.text.ParseException pe) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0712_ERROR_ADJUST_SKEW_FRESHNESS_TIME(), pe);
            throw new XWSSecurityException(pe.getMessage());
        }

        if (created.before(current)) {
            XWSSecurityException xwsse = new XWSSecurityException(
                "Creation Time is older than configured Timestamp Freshness Interval!");
            throw SOAPUtil.newSOAPFaultException(
                MessageConstants.WSSE_INVALID_SECURITY,
                "Creation Time is older than configured Timestamp Freshness Interval!",
                xwsse, true);
        }
            
        Date currentTime = getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkewActual, true);

        if (currentTime.before(created)) {
            XWSSecurityException xwsse = new XWSSecurityException("Creation Time ahead of Current Time!");
            throw SOAPUtil.newSOAPFaultException(
                MessageConstants.WSSE_INVALID_SECURITY,
                "Creation Time ahead of Current Time!",
                xwsse, true);
        }

    }

    private void validateExpirationTime(
        String expirationTime, long maxClockSkew, long timestampFreshnessLimit)
        throws XWSSecurityException {
        long maxClockSkewActual  = maxClockSkew;
        long freshnessLimitActual = timestampFreshnessLimit;
        
        if (this.mcs != null && this.maxClockSkewG >= 0) {
            maxClockSkewActual = this.maxClockSkewG;
        }
        if (this.tfl != null && this.timestampFreshnessLimitG > 0) {
            freshnessLimitActual = this.timestampFreshnessLimitG;    
        }
        
        if (expirationTime != null) {
            Date expires;
            try {
                synchronized(calendarFormatter1) {
                    expires = calendarFormatter1.parse(expirationTime);
                }
            } catch (java.text.ParseException pe) {
                try {
                    synchronized(calendarFormatter2) {
                        expires = calendarFormatter2.parse(expirationTime);
                    }
                } catch (java.text.ParseException pe1) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0394_ERROR_PARSING_EXPIRATIONTIME());
                    throw new XWSSecurityException(
                        "Exception while parsing Expiration Time :" + pe1.getMessage());
                }
            }
                
            Date currentTime = getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkewActual, false);

            if (expires.before(currentTime)) {
                XWSSecurityException xwsse = new XWSSecurityException("Message Expired!");
                throw DefaultSecurityEnvironmentImpl.newSOAPFaultException(
                    MessageConstants.WSU_MESSAGE_EXPIRED,
                    "Message Expired!",
                    xwsse);
            }
        }
    }

    public CallbackHandler getCallbackHandler()
       throws XWSSecurityException {
        return _handler;
    }

  
    public void validateSAMLAssertion(Map context, Element assertion) throws XWSSecurityException {
        //Subject subj = (Subject) context.get(MessageConstants.AUTH_SUBJECT);
        if (sValidator != null) {
            try {
                if (sValidator instanceof ValidatorExtension) {
                    ((ValidatorExtension)sValidator).setRuntimeProperties(context);
                }
                if (sValidator instanceof SAMLValidator) {
                    ((SAMLValidator)sValidator).validate(assertion, context, getRequesterSubject(context));
                } else {
                sValidator.validate(assertion);
                }
            } catch (SAMLAssertionValidator.SAMLValidationException e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0716_FAILED_VALIDATE_SAML_ASSERTION(), e);
                  throw SOAPUtil.newSOAPFaultException(
                    MessageConstants.WSSE_FAILED_AUTHENTICATION,
                    "Validation failed for SAML Assertion ", e, true);
            }
        }
    }
    
    public Element locateSAMLAssertion(Map context, Element binding, String assertionId, Document ownerDoc)
    throws XWSSecurityException {
        
        if (samlHandler != null) {
            SAMLCallback sc = new SAMLCallback();
            sc.setAssertionId(assertionId);
            sc.setAuthorityBindingElement(binding);
            Callback[] cbs = new Callback[] {sc};
            try {
                samlHandler.handle(cbs);
            } catch (UnsupportedCallbackException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER(), ex);
                throw new XWSSecurityException(ex);
            } catch (IOException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER(), ex);
                throw new XWSSecurityException(ex);
            }
            return sc.getAssertionElement();
            
        }else {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0717_NO_SAML_CALLBACK_HANDLER());
            throw new XWSSecurityException(
                    new UnsupportedCallbackException(null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion"));
        }
        
    }
    
    public AuthenticationTokenPolicy.SAMLAssertionBinding populateSAMLPolicy(Map fpcontext, AuthenticationTokenPolicy.SAMLAssertionBinding samlBinding,
            DynamicApplicationContext context)
            throws XWSSecurityException {
        
        AuthenticationTokenPolicy.SAMLAssertionBinding ret = 
                (AuthenticationTokenPolicy.SAMLAssertionBinding)samlBinding.clone();
        if (AuthenticationTokenPolicy.SAMLAssertionBinding.SV_ASSERTION.equals(samlBinding.getAssertionType())) {
            
            if (samlHandler != null) {            
                SAMLCallback sc = new SAMLCallback();
                SecurityUtil.copy(sc.getRuntimeProperties(), fpcontext);
                sc.setConfirmationMethod(SAMLCallback.SV_ASSERTION_TYPE);
                sc.setSAMLVersion(samlBinding.getSAMLVersion());
                Callback[] cbs = new Callback[] {sc};
                try {
                    samlHandler.handle(cbs);
                } catch (UnsupportedCallbackException ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER(), ex);
                    throw new XWSSecurityException(ex);
                } catch (IOException ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER(), ex);
                    throw new XWSSecurityException(ex);
                }
                ret.setAssertion(sc.getAssertionElement());
                ret.setAssertion(sc.getAssertionReader());
                ret.setAuthorityBinding(sc.getAuthorityBindingElement());
                ret.setSAMLVersion(sc.getSAMLVersion());
                
            }else {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0717_NO_SAML_CALLBACK_HANDLER());
                throw new XWSSecurityException(
                        new UnsupportedCallbackException(null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion"));
            }
            
        } else {

            if (samlHandler != null) {
                
                SAMLCallback sc = new SAMLCallback();
                SecurityUtil.copy(sc.getRuntimeProperties(), fpcontext);
                sc.setConfirmationMethod(SAMLCallback.HOK_ASSERTION_TYPE);
                sc.setSAMLVersion(samlBinding.getSAMLVersion());
                Callback[] cbs = new Callback[] {sc};
                try {
                    samlHandler.handle(cbs);
                } catch (IOException ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER(), ex);
                    throw new XWSSecurityException(ex);
                } catch (UnsupportedCallbackException ex) {
                    log.log(Level.SEVERE, LogStringsMessages.WSS_0718_EXCEPTION_INVOKING_SAML_HANDLER(), ex);
                    throw new XWSSecurityException(ex);
                }
                ret.setAssertion(sc.getAssertionElement());
                ret.setAuthorityBinding(sc.getAuthorityBindingElement());
                ret.setAssertion(sc.getAssertionReader());
                PrivateKeyBinding pkBinding = (PrivateKeyBinding) ret.newPrivateKeyBinding();
                PrivateKey key = getPrivateKey(fpcontext, this.myAlias);
                pkBinding.setPrivateKey(key);
                ret.setAssertionId(sc.getAssertionId());
                ret.setSAMLVersion(sc.getSAMLVersion());
            } else {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0717_NO_SAML_CALLBACK_HANDLER());
                throw new XWSSecurityException(
                        new UnsupportedCallbackException(
                        null, "A Required SAML Callback Handler was not specified in configuration : Cannot Populate SAML Assertion"));
            } 
        }
        return ret;
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

    private static Date getFreshnessAndSkewAdjustedDate(
    long maxClockSkew, long timestampFreshnessLimit)
    throws ParseException {
        Calendar c = new GregorianCalendar();
        long offset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            offset += c.getTimeZone().getDSTSavings();
        }
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;
        
        // allow for clock_skew and timestamp_freshness
        long adjustedTime = currentTime - maxClockSkew - timestampFreshnessLimit;
        c.setTimeInMillis(adjustedTime);
        
        return c.getTime();
    }

    private X509Certificate getDynamicCertificate(Map context /*,KeyStore trustStore*/) {

        X509Certificate cert = null;
        X509Certificate self = null;
        Subject requesterSubject = getRequesterSubject(context);
        if (requesterSubject != null) {
            Set publicCredentials = requesterSubject.getPublicCredentials();
            for (Iterator it = publicCredentials.iterator(); it.hasNext();) {
                Object cred = it.next();
                if(cred instanceof java.security.cert.X509Certificate){
                    X509Certificate certificate = (java.security.cert.X509Certificate)cred;
                    if (!isMyCert(certificate, context)) {
                        cert = certificate;
                        break;
                    } else {
                        self= certificate;
                    }
                }
            }
            if (cert != null) {
                return cert;
            } else if (self != null) {
                //allow tests that use same cert for client and server
                return self;
            }
        } 
        /*
        String keyId = (String)context.get(MessageConstants.REQUESTER_KEYID);
        String issuerName = (String)context.get(MessageConstants.REQUESTER_ISSUERNAME);
        BigInteger issuerSerial = (BigInteger)context.get(MessageConstants.REQUESTER_SERIAL);
        //TODO: we are not looking for Thumbprints similarly here 
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
        
        return null;
    }
    

    public PrivateKey getPrivateKey(Map context, byte[] keyIdentifier, String valueType) 
        throws XWSSecurityException {
        X509Certificate cert = XWSSUtil.matchesProgrammaticInfo(context, keyIdentifier, valueType);
        if (cert != null) {
            PrivateKey key = XWSSUtil.getProgrammaticPrivateKey(context);
            if (key != null) {
                return key;
            }
        }
        if ( MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getPrivateKey(context, keyIdentifier);
        }
        if (!MessageConstants.THUMB_PRINT_TYPE.equals(valueType)) {
            throw new XWSSecurityException(
                "Internal Error : Unsupported Valuetype :" 
                    + valueType + " passed to getPrivateKey()");                
        }
        //Handle Thumbprint type here
        try {
           Subject subject = getSubject(context);
           if (subject != null) {
              Set set = subject.getPrivateCredentials(X500PrivateCredential.class);
              if (set != null) {
                 Iterator it = set.iterator();
                 while (it.hasNext()) {
                    X500PrivateCredential cred = (X500PrivateCredential)it.next();
                    if (matchesThumbPrint(Base64.decode(keyIdentifier), 
                                             cred.getCertificate()))
                       return cred.getPrivateKey();
                 }
              }
           }

           PrivateKeyCallback.Request request = 
                   new PrivateKeyCallback.DigestRequest(keyIdentifier, "SHA-1");
           PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
           Callback[] callbacks = null;
            if (this.useXWSSCallbacks) {
                RuntimeProperties props = new RuntimeProperties(context);
                callbacks = new Callback[]{props, pkCallback};
            } else {
                callbacks = new Callback[]{pkCallback};
            }
           _handler.handle(callbacks);

           return pkCallback.getKey(); 
        } catch (Exception e) {
            log.log(Level.SEVERE,LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("PrivateKeyCallback.SubjectKeyIDRequest"),
                    new Object[] { "PrivateKeyCallback.SubjectKeyIDRequest"});
            throw new XWSSecurityException(e);
        }
    }

    public void validateSAMLAssertion(Map context, XMLStreamReader assertion) throws XWSSecurityException {
        //Subject subj = (Subject) context.get(MessageConstants.AUTH_SUBJECT);
        if (sValidator != null) {
            try {
                if (sValidator instanceof ValidatorExtension) {
                    ((ValidatorExtension) sValidator).setRuntimeProperties(context);
                }
                if (sValidator instanceof SAMLValidator) {
                    ((SAMLValidator) sValidator).validate(assertion, context, getRequesterSubject(context));
                } else {
                    sValidator.validate(assertion);
                }
                
            } catch (SAMLAssertionValidator.SAMLValidationException e) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0716_FAILED_VALIDATE_SAML_ASSERTION(), e);
                throw new XWSSecurityException(e);
            }
        }
    }

    public void updateOtherPartySubject(final Subject subject, final XMLStreamReader assertion) {
        if (sValidator instanceof SAMLValidator) {
                //nothing to do
                return;
        }
        AccessController.doPrivileged(
           
                new PrivilegedAction<Object>() {
            public Object run() {
                subject.getPublicCredentials().add(assertion);
                return null;
            }
        });
                
    }

    public boolean isSelfCertificate(X509Certificate cert) {
        return false;
//        if (this.selfCertificate != null && this.selfCertificate.equals(cert)) {
//            return true;
//        }
//        
//        if (_handler != null) {
//            String alias = this.myAlias;
//            if (alias == null) {
//                if (this.keystoreCertSelectorClass != null) {
//                    AliasSelector selector = null;
//                    try {
//                        selector = (AliasSelector)
//                        this.keystoreCertSelectorClass.newInstance();
//                    } catch (IllegalAccessException ex) {
//                        log.log(Level.SEVERE,"WSS1532.exception.instantiating.aliasselector", ex);
//                        throw new RuntimeException(ex);
//                    } catch (InstantiationException ex) {
//                        log.log(Level.SEVERE,"WSS1532.exception.instantiating.aliasselector", ex);
//                        throw new RuntimeException(ex);
//                    }
//                    alias = selector.select(null);
//                }
//                
//            }
//            if (alias != null) {
//                try {
//                    PrivateKeyCallback.Request request =
//                            new PrivateKeyCallback.AliasRequest(alias);
//                    PrivateKeyCallback pkCallback = new PrivateKeyCallback(request);
//                    
//                    Callback[] callbacks = new Callback[] { pkCallback };
//                    _handler.handle(callbacks);
//                    Certificate[] chain = pkCallback.getChain();
//                    X509Certificate selfCert = null;
//                    if (chain != null) {
//                        selfCert = (X509Certificate)chain[0];
//                    }
//                    if (selfCert != null && selfCert.equals(cert)) {
//                        return true;
//                    }
//                } catch (Exception ex) {
//                    //ignore for now since we can always return false form this method
//                }
//            }
//        }
//        return false;
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
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Failed to load class Thread Context ClassLoader..." + classname);
                }
            }
        }
        // if context classloader didnt work, try this
        loader = this.getClass().getClassLoader();
        try {
            ret = loader.loadClass(classname);
            return ret;
        } catch (ClassNotFoundException e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Failed to load class using this.getClass().getClassLoader()..." + classname);
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Calling loadUsingResourceLoader to load class.." + classname);
        }
        if (ret == null) {
            ret = loadUsingResourceLoader(classname);
            if (ret != null) {
                return ret;
            }
        }

        log.log(Level.SEVERE, LogStringsMessages.WSS_0714_ERROR_GETTING_USER_CLASS(classname), new Object[]{classname});
        throw new XWSSecurityException("Could not find User Class " + classname);
    }

    

    public void updateOtherPartySubject(Subject subject, Subject bootStrapSubject) {
        SecurityUtil.copySubject(subject, bootStrapSubject);
    }

    public KerberosContext doKerberosLogin() throws XWSSecurityException {
        if(krbLoginModule == null || krbLoginModule.equals("")){
           throw new XWSSecurityException("Login Module for Kerberos login is not set or could not be obtained"); 
        }
        if(krbServicePrincipal == null || krbServicePrincipal.equals("")){
            throw new XWSSecurityException("Kerberos Service Principal is not set or could not be obtained");
        }
        return new KerberosLogin().login(krbLoginModule, krbServicePrincipal, krbCredentialDelegation);
    }

    public KerberosContext doKerberosLogin(byte[] tokenValue) throws XWSSecurityException {
        return new KerberosLogin().login(krbLoginModule, tokenValue);
    }

    public void updateOtherPartySubject(final Subject subject, 
            final GSSName clientCred, 
            final GSSCredential gssCred) {
        
        try {
            final KerberosPrincipal kerbPrincipal = new KerberosPrincipal(clientCred.toString());
            CallerPrincipalCallback pvCallback = new CallerPrincipalCallback(subject, kerbPrincipal.getName());
            Callback[] callbacks = new Callback[]{pvCallback};
            _handler.handle(callbacks);
            // adding the KerberosPrincipal to public credentials
            //TODO: check if this needs to be done in case of GF
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
//                  KerberosPrincipal kerbPrincipal = new KerberosPrincipal(clientCred.toString());
                    subject.getPrincipals().add(kerbPrincipal);
                    subject.getPublicCredentials().add(clientCred);
                    if(gssCred != null){
                        subject.getPrivateCredentials().add(gssCred);
                    }
                    return null; // nothing to return
                }
            });
        
        } catch (Exception e) {
            log.log(Level.SEVERE, LogStringsMessages.WSS_0216_CALLBACKHANDLER_HANDLE_EXCEPTION("CallerPrincipalCallback"),
                    new Object[]{"CallerPrincipalCallback"});
            throw new XWSSecurityRuntimeException(e);
        }
    }

    class PriviledgedHandler implements CallbackHandler {

        CallbackHandler delegate = null;

        public PriviledgedHandler(CallbackHandler handler) {
            delegate = handler;
        }

        public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    try {
                        delegate.handle(callbacks);
                        return null;
                    } catch (Exception ex) {
                        throw new XWSSecurityRuntimeException(ex);
                    }
                }
            });
        }
    }
}
