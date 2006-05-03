/*
 * $Id: DefaultSecurityEnvironmentImpl.java,v 1.1 2006-05-03 22:57:51 arungupta Exp $
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

import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.saml.util.SAMLUtil;
//import com.sun.xml.wss.saml.internal.impl.AssertionImpl;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.AccessController;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.Properties;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.crypto.SecretKey;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.x500.X500Principal;

import javax.xml.namespace.QName;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.xml.wss.impl.misc.Base64;
import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;

import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.SubjectAccessor;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;

import com.sun.xml.wss.impl.FilterProcessingContext;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.WssSoapFaultException;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.impl.SecurableSoapMessage;

import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.sun.xml.wss.impl.callback.SignatureKeyCallback;
import com.sun.xml.wss.impl.callback.EncryptionKeyCallback;
import com.sun.xml.wss.impl.callback.SignatureVerificationKeyCallback;
import com.sun.xml.wss.impl.callback.DecryptionKeyCallback;
import com.sun.xml.wss.impl.callback.CertificateValidationCallback;
import com.sun.xml.wss.impl.callback.DynamicPolicyCallback;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;

import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.saml.AuthorityBinding;
import com.sun.xml.wss.saml.NameIdentifier;


import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

//TODO: support PrefixNamespaceMappingCallback
public class DefaultSecurityEnvironmentImpl implements SecurityEnvironment {
    
    
    static final boolean USE_DAEMON_THREAD = true;
    static final Timer nonceCleanupTimer = new Timer(USE_DAEMON_THREAD);
    
    // Zone offset
    private static final long offset;
    
    static {
        Calendar c = new GregorianCalendar();
        long calculatedOffset = c.get(Calendar.ZONE_OFFSET);
        if (c.getTimeZone().inDaylightTime(c.getTime())) {
            calculatedOffset += c.getTimeZone().getDSTSavings();
        }
        offset = calculatedOffset;
    }
    
    // Nonce Cache
    NonceCache nonceCache = null;
    
    /** logger */
    protected static Logger log =  Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    // milliseconds (set to 5 mins), time for which a timestamp is considered fresh
    private final SimpleDateFormat calendarFormatter1 =   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat calendarFormatter2 =   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
    
    CallbackHandler callbackHandler = null;
    
    public DefaultSecurityEnvironmentImpl(CallbackHandler cHandler) {
        callbackHandler = cHandler;
    }
    
    /*
     * Applicable only for the signing case
     */
    public X509Certificate getDefaultCertificate(Map context) throws XWSSecurityException {
        
        X509Certificate defaultCert = null;
        
        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureKeyCallback.DefaultPrivKeyCertRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        defaultCert = privKeyRequest.getX509Certificate();
        
        if (defaultCert == null) {
            log.log(Level.SEVERE,"WSS0218.cannot.locate.default.cert");
            throw new XWSSecurityException(
                    "Unable to locate a default certificate");
        }
        return defaultCert;
    }
    
    public  SignatureKeyCallback.PrivKeyCertRequest
            getDefaultPrivKeyCertRequest(Map context) throws XWSSecurityException{
        
        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureKeyCallback.DefaultPrivKeyCertRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        return privKeyRequest;
    }
    
    public SignatureKeyCallback.AliasPrivKeyCertRequest
            getAliasPrivKeyCertRequest(String certIdentifier)  throws XWSSecurityException {
        
        SignatureKeyCallback.AliasPrivKeyCertRequest request =
                new SignatureKeyCallback.AliasPrivKeyCertRequest(certIdentifier);
        
        SignatureKeyCallback sigCallback = new SignatureKeyCallback(request);
        Callback[] callback = new Callback[] {sigCallback};
        try {
            callbackHandler.handle(callback);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureKeyCallback.AliasPrivKeyCertRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        return request;
    }
    
    public  PrivateKey getDefaultPrivateKey(Map context) throws XWSSecurityException {
        
        PrivateKey defaultPrivKey = null;
        
        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureKeyCallback.DefaultPrivKeyCertRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        defaultPrivKey = privKeyRequest.getPrivateKey();
        
        if (defaultPrivKey == null) {
            log.log(Level.SEVERE,"WSS0219.cannot.locate.default.privkey");
            throw new XWSSecurityException(
                    "Unable to locate a default certificate");
        }
        return defaultPrivKey;
    }
    
    public SecretKey getSecretKey(Map context, String alias, boolean encryptMode)
    throws XWSSecurityException {
        
        SecretKey symmetricKey = null;
        
        if (encryptMode) {
            EncryptionKeyCallback.SymmetricKeyRequest symmKeyRequest =
                    new EncryptionKeyCallback.AliasSymmetricKeyRequest(alias);
            EncryptionKeyCallback encKeyCallback = new EncryptionKeyCallback(symmKeyRequest);
            ProcessingContext.copy(encKeyCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {encKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "EncryptionKeyCallback.AliasSymmetricKeyRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            symmetricKey = symmKeyRequest.getSymmetricKey();
        } else {
            DecryptionKeyCallback.SymmetricKeyRequest symmKeyRequest =
                    new DecryptionKeyCallback.AliasSymmetricKeyRequest(alias);
            DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(symmKeyRequest);
            ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {decryptKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "DecryptionKeyCallback.AliasSymmetricKeyRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            symmetricKey = symmKeyRequest.getSymmetricKey();
        }
        
        if (symmetricKey == null) {
            log.log(Level.SEVERE,"WSS0220.cannot.locate.symmetrickey.for.decrypt");
            throw new XWSSecurityException(
                    "Could not locate the symmetric key for alias '" + alias + "'");
        }
        return symmetricKey;
    }
    
    public X509Certificate getCertificate(Map context, String alias, boolean forSigning)
    throws XWSSecurityException {
        
        X509Certificate cert = null;
        
        if (((alias == null) || ("".equals(alias)) && forSigning))
            return getDefaultCertificate(context);
        
        if (forSigning) {
            SignatureKeyCallback.PrivKeyCertRequest certRequest =
                    new SignatureKeyCallback.AliasPrivKeyCertRequest(alias);
            SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(certRequest);
            ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {sigKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "SignatureKeyCallback.AliasPrivKeyCertRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            cert = certRequest.getX509Certificate();
        } else {
            EncryptionKeyCallback.X509CertificateRequest certRequest =
                    new EncryptionKeyCallback.AliasX509CertificateRequest(alias);
            EncryptionKeyCallback encKeyCallback = new EncryptionKeyCallback(certRequest);
            ProcessingContext.copy(encKeyCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {encKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "EncryptionKeyCallback.AliasX509CertificateRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            cert = certRequest.getX509Certificate();
        }
        
        if (cert == null) {
            String val = forSigning ? "Signature" : "Key Ecnryption";
            log.log(Level.SEVERE,"WSS0221.cannot.locate.cert", new Object[] {val});
            throw new XWSSecurityException(
                    "Unable to locate certificate for the alias '" + alias + "'");
        }
        return cert;
    }
    
    public X509Certificate getCertificate(Map context, PublicKey publicKey, boolean forSign)
    throws XWSSecurityException {
        if (forSign) {
            SignatureVerificationKeyCallback.PublicKeyBasedRequest pubKeyReq =
                    new SignatureVerificationKeyCallback.PublicKeyBasedRequest(publicKey);
            SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(pubKeyReq);
            ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {verifyKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (UnsupportedCallbackException e1) {
                //ignore;
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "SignatureVerificationKeyCallback.PublicKeyBasedRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            return pubKeyReq.getX509Certificate();
        } else {
            EncryptionKeyCallback.PublicKeyBasedRequest pubKeyReq =
                    new EncryptionKeyCallback.PublicKeyBasedRequest(publicKey);
            EncryptionKeyCallback encCallback = new EncryptionKeyCallback(pubKeyReq);
            ProcessingContext.copy(encCallback.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {encCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (UnsupportedCallbackException e1) {
                //ignore;
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "EncryptionKeyCallback.PublicKeyBasedRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            return pubKeyReq.getX509Certificate();
        }
    }
    
    public PrivateKey getPrivateKey(Map context, String alias)
    throws XWSSecurityException {
        
        PrivateKey privKey = null;
        
        if (alias == null)
            return getDefaultPrivateKey(context);
        
        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.AliasPrivKeyCertRequest(alias);
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureKeyCallback.AliasPrivKeyCertRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        privKey = privKeyRequest.getPrivateKey();
        
        if (privKey == null) {
            log.log(Level.SEVERE,"WSS0222.cannot.locate.privkey", new Object[] {alias});
            throw new XWSSecurityException(
                    "Unable to locate private key for the alias " + alias);
        }
        return privKey;
    }
    
    
    public PrivateKey getPrivateKey(Map context, byte[] identifier, String valueType)
    throws XWSSecurityException {
        if ( MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getPrivateKey(context, identifier);
        }
        
        PrivateKey privateKey = null;
        
        DecryptionKeyCallback.PrivateKeyRequest privKeyRequest =
                new DecryptionKeyCallback.ThumbprintBasedRequest(identifier);
        DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(privKeyRequest);
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "DecryptionKeyCallback.ThumbprintBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        privateKey = privKeyRequest.getPrivateKey();
        
        if (privateKey == null) {
            // not found so throw an exception
            log.log(Level.SEVERE,"WSS0222.cannot.locate.privkey", new Object[] {identifier});
            throw new XWSSecurityException(
                    "No Matching private key for " + Base64.encode(identifier) + " thumb print identifier found");
        }
        return privateKey;
    }
    
    public PrivateKey getPrivateKey(Map context, byte[] keyIdentifier)
    throws XWSSecurityException {
        
        PrivateKey privateKey = null;
        
        DecryptionKeyCallback.PrivateKeyRequest privKeyRequest =
                new DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest(keyIdentifier);
        DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(privKeyRequest);
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        privateKey = privKeyRequest.getPrivateKey();
        
        if (privateKey == null) {
            // not found so throw an exception
            log.log(Level.SEVERE,"WSS0222.cannot.locate.privkey", new Object[] {keyIdentifier});
            throw new XWSSecurityException(
                    "No Matching private key for " + Base64.encode(keyIdentifier) + " subject key identifier found");
        }
        return privateKey;
    }
    
    
    public PrivateKey getPrivateKey(Map context, BigInteger serialNumber, String issuerName)
    throws XWSSecurityException {
        
        PrivateKey privateKey = null;
        
        DecryptionKeyCallback.PrivateKeyRequest privKeyRequest =
                new DecryptionKeyCallback.X509IssuerSerialBasedRequest(issuerName, serialNumber);
        DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(privKeyRequest);
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "DecryptionKeyCallback.X509IssuerSerialBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        privateKey = privKeyRequest.getPrivateKey();
        
        if (privateKey == null) {
            // not found so throw an exception
            log.log(Level.SEVERE,"WSS0222.cannot.locate.privkey",
                    new Object[] {serialNumber + ":" + issuerName});
                    throw new XWSSecurityException(
                            "No Matching private key for serial number " + serialNumber + " and issuer name " + issuerName + " found");
        }
        
        return privateKey;
    }
    
    
    public PublicKey getPublicKey(Map context, byte[] identifier, String valueType)
    throws XWSSecurityException {
        return getCertificate(context, identifier, valueType).getPublicKey();
    }
    
    public PublicKey getPublicKey(Map context, byte[] keyIdentifier)
    throws XWSSecurityException {
        return getCertificate(context, keyIdentifier).getPublicKey();
    }
    
    
    public X509Certificate getCertificate(Map context, byte[] identifier, String valueType)
    throws XWSSecurityException {
        if ( MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getCertificate(context, identifier);
        }
        
        //Else if it is Thumbprint
        X509Certificate cert = null;
        
        SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                new SignatureVerificationKeyCallback.ThumbprintBasedRequest(identifier);
        SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
        ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {verifyKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureVerificationKeyCallback.ThumbprintBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
                    
        }
        cert = certRequest.getX509Certificate();
        
        if (cert == null) {
            // not found so throw an exception
            log.log(Level.SEVERE,"WSS0221.cannot.locate.cert", new Object[] {identifier});
            throw new XWSSecurityException("No Matching public key for " + Base64.encode(identifier) + " thumb print identifier found");
        }
        return cert;
    }
    
    public  X509Certificate getCertificate(Map context, byte[] keyIdentifier)
    throws XWSSecurityException {
        
        X509Certificate cert = null;
        
        SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                new SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest(keyIdentifier);
        SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
        ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {verifyKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
                    
        }
        cert = certRequest.getX509Certificate();
        
        if (cert == null) {
            // not found so throw an exception
            log.log(Level.SEVERE,"WSS0221.cannot.locate.cert", new Object[] {keyIdentifier});
            throw new XWSSecurityException("No Matching public key for " + Base64.encode(keyIdentifier) + " subject key identifier found");
        }
        return cert;
    }
    
    public  PublicKey getPublicKey(Map context, BigInteger serialNumber, String issuerName)
    throws XWSSecurityException {
        
        return getCertificate(context, serialNumber, issuerName).getPublicKey();
    }
    
    public  X509Certificate getCertificate(Map context, BigInteger serialNumber, String issuerName)
    throws XWSSecurityException {
        
        X509Certificate cert = null;
        
        SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                new SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest(issuerName, serialNumber);
        SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
        ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {verifyKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        cert = certRequest.getX509Certificate();
        
        if (cert == null) {
            // not found so throw an exception
            log.log(Level.SEVERE,"WSS0221.cannot.locate.cert", new Object[] {serialNumber + ":" + issuerName});
            throw new XWSSecurityException(
                    "No Matching public key for serial number " + serialNumber + " and issuer name " + issuerName + " found");
        }
        
        return cert;
    }
    
    
    
    public boolean validateCertificate(X509Certificate cert)
    throws XWSSecurityException {
        
        CertificateValidationCallback certValCallback = new CertificateValidationCallback(cert);
        Callback[] callbacks = new Callback[] {certValCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            throw newSOAPFaultException(
                    MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "Certificate validation failed",
                    e);
        }
        return certValCallback.getResult();
    }
    
    public void updateOtherPartySubject(
            final Subject subject, final String username, final String password) {
        AccessController.doPrivileged(new PrivilegedAction() {
            
            public Object run() {
                String x500Name = "CN=" + username;
                Principal principal = new X500Principal(x500Name);
                subject.getPrincipals().add(principal);
                if (password != null) {
                    subject.getPrivateCredentials().add(password);
                }
                return null; // nothing to return
            }
        });
    }
    
    public void updateOtherPartySubject(
            final Subject subject,
            final X509Certificate cert) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Principal principal = cert.getSubjectX500Principal();
                subject.getPrincipals().add(principal);
                subject.getPublicCredentials().add(cert);
                return null; // nothing to return
            }
        });
    }
    
    public void updateOtherPartySubject(
            final Subject subject,
            final Assertion assertion) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                subject.getPublicCredentials().add(assertion);
                return null; // nothing to return
            }
        });
    }
    
    public void updateOtherPartySubject(
            final Subject subject,
            final Key secretKey) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                subject.getPublicCredentials().add(secretKey);
                return null; // nothing to return
            }
        });
    }
    
    public void updateOtherPartySubject(
            final Subject subject,
            final String ek) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                String encryptedKey = "EK" + ek;
                subject.getPublicCredentials().add(encryptedKey);
                return null; // nothing to return
            }
        });
    }   
    
    public static Subject getSubject(final FilterProcessingContext context){
        Subject otherPartySubject =
                (Subject) AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
                Subject otherPartySubject =
                        (Subject)context.getExtraneousProperty(MessageConstants.AUTH_SUBJECT);
                if (otherPartySubject == null) {
                    otherPartySubject =SubjectAccessor.getRequesterSubject();
                    if (otherPartySubject == null){
                        otherPartySubject = new Subject();
                        SubjectAccessor.setRequesterSubject(otherPartySubject);
                    }
                    context.setExtraneousProperty(MessageConstants.AUTH_SUBJECT,otherPartySubject);
                }
                return otherPartySubject;
            }
        }
        );
        return otherPartySubject;
    }
    
    
    public  PrivateKey getPrivateKey(Map context, X509Certificate cert)
    throws XWSSecurityException {
        
        PrivateKey privateKey = null;
        
        DecryptionKeyCallback.PrivateKeyRequest privateKeyRequest =
                new DecryptionKeyCallback.X509CertificateBasedRequest(cert);
        DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(privateKeyRequest);
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "DecryptionKeyCallback.X509CertificateBasedRequest"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        privateKey = privateKeyRequest.getPrivateKey();
        
        if (privateKey == null) {
            log.log(Level.SEVERE,"WSS0222.cannot.locate.privkey", new Object[] {"given certificate"});
            throw new XWSSecurityException(
                    "Could not retrieve private Key matching the given certificate");
        }
        return privateKey;
    }
    
    public  PrivateKey getPrivateKey(Map context, PublicKey publicKey, boolean forSign)
    throws XWSSecurityException {
        if (forSign)  {
            SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest req =
                    new SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest(publicKey);
            SignatureKeyCallback skc = new SignatureKeyCallback(req);
            ProcessingContext.copy(skc.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {skc};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            return req.getPrivateKey();
        } else {
            DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest req =
                    new DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest(publicKey);
            DecryptionKeyCallback dkc = new DecryptionKeyCallback(req);
            ProcessingContext.copy(dkc.getRuntimeProperties(), context);
            Callback[] callbacks = new Callback[] {dkc};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[] { "DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest"});
                        log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                        throw new XWSSecurityException(e);
            }
            return req.getPrivateKey();
        }
    }
    
    public Subject getSubject() {
        throw new UnsupportedOperationException(
                "This environment does not have an associated Subject");
    }
    
    public boolean authenticateUser(
            Map context,
            String username,
            String passwordDigest,
            String nonce,
            String created)
            throws XWSSecurityException {
        
        PasswordValidationCallback.DigestPasswordRequest request =
                new PasswordValidationCallback.DigestPasswordRequest(
                username, passwordDigest, nonce, created);
        PasswordValidationCallback passwordValidationCallback =
                new PasswordValidationCallback(request);
        ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {passwordValidationCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        return passwordValidationCallback.getResult();
    }
    
    public boolean authenticateUser(Map context, String username, String password)
    throws XWSSecurityException {
        
        PasswordValidationCallback.PlainTextPasswordRequest request =
                new PasswordValidationCallback.PlainTextPasswordRequest(username, password);
        PasswordValidationCallback passwordValidationCallback =
                new PasswordValidationCallback(request);
        ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {passwordValidationCallback};
        boolean result = false;
        try {
            callbackHandler.handle(callbacks);
            result = passwordValidationCallback.getResult();
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        return result;
    }
    
    //Default creation time validation code. This will be
    //executed when user does not provide TimestampVlidation.
    private void defaultValidateCreationTime(
            String creationTime,
            long maxClockSkew,
            long timestampFreshnessLimit)
            throws XWSSecurityException {
        
        Date created = null;
        try {
            try {
                synchronized(calendarFormatter1) {
                    created = calendarFormatter1.parse(creationTime);
                }
            } catch (java.text.ParseException e ) {
                synchronized(calendarFormatter2) {
                    created = calendarFormatter2.parse(creationTime);
                }
            }
        } catch (java.text.ParseException e) {
            throw new XWSSecurityException(e);
        }
        
        Date current = getFreshnessAndSkewAdjustedDate(maxClockSkew, timestampFreshnessLimit);
        
        if (created.before(current)) {
            throw new XWSSecurityException(
                    "The creation time is older than " +
                    " currenttime - timestamp-freshness-limit - max-clock-skew");
        }
        
        Date currentTime =
                getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
        if (currentTime.before(created)) {
            throw new XWSSecurityException(
                    "The creation time is ahead of the current time.");
        }
    }
    
    /**
     *
     * @param creationTime
     * @throws XWSSecurityException
     * @return
     */
    public void validateCreationTime(
            Map context,
            String creationTime,
            long maxClockSkew,
            long timestampFreshnessLimit)
            throws XWSSecurityException {
        
        TimestampValidationCallback.UTCTimestampRequest request =
                new TimestampValidationCallback.UTCTimestampRequest(
                creationTime,
                null,
                maxClockSkew,
                timestampFreshnessLimit
                );
        
        request.isUsernameToken(true);
        TimestampValidationCallback timestampValidationCallback =
                new TimestampValidationCallback(request);
        ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {timestampValidationCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            defaultValidateCreationTime(creationTime, maxClockSkew, timestampFreshnessLimit);
            return;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        try {
            timestampValidationCallback.getResult();
        } catch (TimestampValidationCallback.TimestampValidationException e) {
            throw new XWSSecurityException(e);
        }
    }
    
    //TODO implement this using callbacks
    public  boolean validateSamlIssuer(String issuer) {
        throw new UnsupportedOperationException();
    }
    
    //TODO implement this using callbacks
    public  boolean validateSamlUser(
            String user, String domain, String format) {
        throw new UnsupportedOperationException();
    }
    
    
    public String getUsername(Map context) throws XWSSecurityException {
        UsernameCallback usernameCallback = new UsernameCallback();
        ProcessingContext.copy(usernameCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {usernameCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[] { "UsernameCallback"});
                    log.log(Level.SEVERE,"WSS0217.callbackhandler.handle.exception.log",e);
                    throw new XWSSecurityException(e);
        }
        return usernameCallback.getUsername();
    }
    
    public String getPassword(Map context) throws XWSSecurityException {
        PasswordCallback passwordCallback = new PasswordCallback();
        ProcessingContext.copy(passwordCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {passwordCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            throw new XWSSecurityException(e.getMessage(), e);
        }
        return passwordCallback.getPassword();
    }
    
    
    //Default expiration time validation code. This will be
    //executed when user does not provide TimestampVlidation.
    private void defaultValidateExpirationTime(
            String expirationTime, long maxClockSkew, long timestampFreshnessLimit)
            throws XWSSecurityException {
        
        if (expirationTime != null) {
            Date expires;
            try {
                
                try {
                    synchronized(calendarFormatter1) {
                        expires = calendarFormatter1.parse(expirationTime);
                    }
                } catch (java.text.ParseException pe) {
                    synchronized(calendarFormatter2) {
                        expires = calendarFormatter2.parse(expirationTime);
                    }
                }
                
            } catch(Exception e) {
                log.log(Level.SEVERE, "WSS0394.error.parsing.expirationtime");
                throw new XWSSecurityException(e);
            }
            
            Date currentTime =
                    getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, false);
            if (expires.before(currentTime)) {
                log.log(Level.SEVERE, "WSS0393.current.ahead.of.expires");
                throw new XWSSecurityException("The current time is ahead of the expiration time in Timestamp");
            }
        }
        
    }
    
    public void validateTimestamp(
            Map context, Timestamp timestamp, long maxClockSkew, long freshnessLimit)
            throws XWSSecurityException {
        
        //System.out.println("\nCreated : " + timestamp.getCreated());
        //System.out.println("Expired : " + timestamp.getExpires());
        //System.out.println("\n\n");
        if (expiresBeforeCreated(timestamp.getCreated(), timestamp.getExpires())) {
            XWSSecurityException xwsse = new XWSSecurityException("Message expired!");
            throw newSOAPFaultException(
                    MessageConstants.WSU_MESSAGE_EXPIRED,
                    "Message expired!",
                    xwsse);
        }
        
        
        TimestampValidationCallback.UTCTimestampRequest request =
                new TimestampValidationCallback.UTCTimestampRequest(
                timestamp.getCreated(),
                timestamp.getExpires(),
                maxClockSkew,
                freshnessLimit);
        
        TimestampValidationCallback timestampValidationCallback =
                new TimestampValidationCallback(request);
        ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[] {timestampValidationCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch ( UnsupportedCallbackException e) {
            //System.out.println("Validate Timestamp ...");
            defaultValidateCreationTime(timestamp.getCreated(), maxClockSkew, freshnessLimit);
            defaultValidateExpirationTime(timestamp.getExpires(), maxClockSkew, freshnessLimit);
            return;
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        
        try {
            timestampValidationCallback.getResult();
        } catch (TimestampValidationCallback.TimestampValidationException e) {
            throw new XWSSecurityException(e);
        }
        
    }
    
    /**
     * Create and initialize a WssSoapFaultException. This method is used in
     * conjunction with generateClientFault.
     */
    public static WssSoapFaultException newSOAPFaultException(
            QName faultCode,
            String faultstring,
            Throwable th) {
        WssSoapFaultException sfe =
                new WssSoapFaultException(faultCode, faultstring, null, null);
        sfe.initCause(th);
        return sfe;
    }
    
    private static Date getGMTDateWithSkewAdjusted(
            Calendar c, long maxClockSkew, boolean addSkew) {
        //long offset = c.get(Calendar.ZONE_OFFSET);
        //if (c.getTimeZone().inDaylightTime(c.getTime())) {
        //    offset += c.getTimeZone().getDSTSavings();
        //}
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
            long maxClockSkew, long timestampFreshnessLimit) {
        Calendar c = new GregorianCalendar();
        //long offset = c.get(Calendar.ZONE_OFFSET);
        //if (c.getTimeZone().inDaylightTime(c.getTime())) {
        //    offset += c.getTimeZone().getDSTSavings();
        //}
        long beforeTime = c.getTimeInMillis();
        long currentTime = beforeTime - offset;
        
        // allow for clock_skew and timestamp_freshness
        long adjustedTime = currentTime - maxClockSkew - timestampFreshnessLimit;
        c.setTimeInMillis(adjustedTime);
        
        return c.getTime();
    }
    
    private boolean expiresBeforeCreated(
            String creationTime, String expirationTime) throws XWSSecurityException {
        Date created = null;
        Date expires = null;
        try {
            try {
                synchronized(calendarFormatter1) {
                    created = calendarFormatter1.parse(creationTime);
                    if (expirationTime != null) {
                        expires = calendarFormatter1.parse(expirationTime);
                    }
                }
            } catch (java.text.ParseException pe) {
                synchronized(calendarFormatter2) {
                    created = calendarFormatter2.parse(creationTime);
                    if (expirationTime != null) {
                        expires = calendarFormatter2.parse(expirationTime);
                    }
                }
            }
        } catch (java.text.ParseException pe) {
            throw new XWSSecurityException(pe.getMessage());
        }
        
        if ((expires != null) && expires.equals(created))
            return true;
        
        if ((expires != null) && expires.before(created))
            return true;
        
        return false;
    }
    
    
    public boolean validateAndCacheNonce(String nonce, String created, long maxNonceAge)
    throws XWSSecurityException {
        
        if ((nonceCache == null) || ((nonceCache != null) && nonceCache.wasCanceled())) {
            initNonceCache(maxNonceAge);
        }
        
        //  check if the reclaimer Task is scheduled or not
        if (!nonceCache.isScheduled()) {
            if (MessageConstants.debug)
                log.log(Level.FINE,
                        "About to Store a new Nonce, but Reclaimer not Scheduled, so scheduling one" + nonceCache);
            setNonceCacheCleanup();
        }
        
        return nonceCache.validateAndCacheNonce(nonce,created);
    }
    
    public void validateSAMLAssertion(Map context, Element assertion) throws XWSSecurityException {
        
        AuthenticationTokenPolicy authPolicy = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                (AuthenticationTokenPolicy.SAMLAssertionBinding) authPolicy.newSAMLAssertionFeatureBinding();
        samlPolicy.setAssertion(assertion);
        
        DynamicPolicyCallback dynamicCallback =
                new DynamicPolicyCallback(samlPolicy, null);
        ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), context);
        try {
            Callback[] callbacks = new Callback[] {dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_FAILED_AUTHENTICATION,
                    "Validation failed for SAML Assertion ", e);
        }
    }
    
    public Element locateSAMLAssertion(Map context, Element binding, String assertionId, Document ownerDoc)
    throws XWSSecurityException {
        
        AuthenticationTokenPolicy authPolicy = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                (AuthenticationTokenPolicy.SAMLAssertionBinding) authPolicy.newSAMLAssertionFeatureBinding();
        samlPolicy.setAuthorityBinding(binding);
        samlPolicy.setAssertionId(assertionId);
        
        DynamicPolicyCallback dynamicCallback =
                new DynamicPolicyCallback(samlPolicy, null);
        ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), context);
        try {
            Callback[] callbacks = new Callback[] {dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        Element assertion = samlPolicy.getAssertion();
        if (assertion == null) {
            throw new XWSSecurityException("SAML Assertion not set into Policy by CallbackHandler");
        }
        
        return assertion;
    }
    
    public AuthenticationTokenPolicy.SAMLAssertionBinding populateSAMLPolicy(Map fpcontext, AuthenticationTokenPolicy.SAMLAssertionBinding policy,
            DynamicApplicationContext context)
            throws XWSSecurityException {
        
        DynamicPolicyCallback dynamicCallback =
                new DynamicPolicyCallback(policy, context);
        ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), fpcontext);
        try {
            Callback[] callbacks = new Callback[] {dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
        return (AuthenticationTokenPolicy.SAMLAssertionBinding)dynamicCallback.getSecurityPolicy();
    }
    
    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }
    
    
    private synchronized void initNonceCache(long maxNonceAge) {
        
        if (nonceCache == null) {
            if (maxNonceAge == 0) {
                nonceCache = new NonceCache();
            } else {
                nonceCache = new NonceCache(maxNonceAge);
            }
            if (MessageConstants.debug)
                log.log(Level.FINE, "Creating NonceCache for first time....." + nonceCache);
        } else if (nonceCache.wasCanceled()) {
            if (maxNonceAge == 0) {
                nonceCache = new NonceCache();
            } else {
                nonceCache = new NonceCache(maxNonceAge);
            }
            if (MessageConstants.debug)
                log.log(Level.FINE, "Re-creating NonceCache because it was canceled....." + nonceCache);
        }
    }
    
    private synchronized void setNonceCacheCleanup() {
        
        if (!nonceCache.isScheduled()) {
            if (MessageConstants.debug)
                log.log(Level.FINE, "Scheduling Nonce Reclaimer task...... for " + this + ":" + nonceCache);
            nonceCleanupTimer.schedule(
                    nonceCache,
                    nonceCache.getMaxNonceAge(), // run it the first time after
                    nonceCache.getMaxNonceAge()); //repeat every
            nonceCache.scheduled(true);
        }
    }
    
    private void validateSamlVersion(Assertion assertion) {
        BigInteger major = ((com.sun.xml.wss.saml.Assertion)assertion).getMajorVersion();
        BigInteger minor = ((com.sun.xml.wss.saml.Assertion)assertion).getMinorVersion();
        
        if (major.intValue() != 1) {
            log.log(Level.SEVERE, "WSS0404.saml.invalid.version");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "Major version is not 1 for SAML Assertion:"
                    + ((com.sun.xml.wss.saml.Assertion)assertion).getAssertionID() ,
                    new Exception(
                    "Major version is not 1 for SAML Assertion"));
        }
        
        if ((minor.intValue() != 0) && (minor.intValue() != 1)) {
            log.log(Level.SEVERE, "WSS0404.saml.invalid.version");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "Minor version is not 0/1 for SAML Assertion:"
                    + ((com.sun.xml.wss.saml.Assertion)assertion).getAssertionID() ,
                    new Exception(
                    "Minor version is not 0/1 for SAML Assertion"));
        }
    }
    
    private void validateIssuer(
            SecurableSoapMessage secMessage,
            Assertion assertion) {
    }
    
    
    private void validateSamlUser(
            SecurableSoapMessage  secMessage,
            Assertion assertion){
        String user = null;
        
    }
    
}
