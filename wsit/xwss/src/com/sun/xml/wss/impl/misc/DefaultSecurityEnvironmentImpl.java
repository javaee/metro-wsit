/*
 * $Id: DefaultSecurityEnvironmentImpl.java,v 1.53 2009/09/09 09:36:04 kumarjayanti Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.security.impl.kerberos.KerberosContext;
import com.sun.xml.ws.security.impl.kerberos.KerberosLogin;
import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.wss.NonceManager;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.RealmAuthenticationAdapter;
import java.math.BigInteger;
import java.security.Key;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.impl.FilterProcessingContext;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
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
import com.sun.xml.wss.impl.callback.SAMLValidator;
import com.sun.xml.wss.impl.callback.TimestampValidationCallback;
import com.sun.xml.wss.saml.Assertion;
import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.xml.stream.XMLStreamReader;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

//TODO: support PrefixNamespaceMappingCallback
public class DefaultSecurityEnvironmentImpl implements SecurityEnvironment {

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
  
    /** logger */
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN, LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    // milliseconds (set to 5 mins), time for which a timestamp is considered fresh
    private final SimpleDateFormat calendarFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat calendarFormatter2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
    private CallbackHandler callbackHandler = null;
    private boolean isDefaultHandler = false;
    private X509Certificate selfCertificate = null;
    private Properties configAssertions = null;
    
    private long maxNonceAge = MessageConstants.MAX_NONCE_AGE;
    private String mnaProperty = null;
    
    public DefaultSecurityEnvironmentImpl(CallbackHandler cHandler) {
        callbackHandler = cHandler;
        if (callbackHandler instanceof DefaultCallbackHandler) {
            isDefaultHandler = true;
        }
        // keep the self certificate handy
//        if (callbackHandler != null && (callbackHandler instanceof DefaultCallbackHandler)) {
//            try {
//                X509Certificate defaultCert = null;
//                SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
//                        new SignatureKeyCallback.DefaultPrivKeyCertRequest();
//                SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
//                Callback[] callbacks = new Callback[]{sigKeyCallback};
//                callbackHandler.handle(callbacks);
//                selfCertificate = privKeyRequest.getX509Certificate();
//            } catch (Exception e) {
//            //ignore for now
//            }
//        }
    }

    public DefaultSecurityEnvironmentImpl(CallbackHandler cHandler, Properties confAssertions) {
        this.configAssertions = confAssertions;
        callbackHandler = cHandler;
        if (callbackHandler instanceof DefaultCallbackHandler) {
            isDefaultHandler = true;
        }
        //store the relevant config assertions here
        String myAlias = configAssertions.getProperty(DefaultCallbackHandler.MY_ALIAS);
        this.mnaProperty = configAssertions.getProperty(DefaultCallbackHandler.MAX_NONCE_AGE_PROPERTY);
        if (this.mnaProperty != null) {
            try {
                maxNonceAge = SecurityUtil.toLong(mnaProperty);
            } catch (XWSSecurityException ex) {
                log.log(Level.FINE, 
                        "Exception while converting maxNonceAge config property, Setting MaxNonceAge to Default value" 
                        + MessageConstants.MAX_NONCE_AGE);
                maxNonceAge = MessageConstants.MAX_NONCE_AGE;
            }
        }
        // keep the self certificate handy
//        if (callbackHandler != null && myAlias != null && (callbackHandler instanceof DefaultCallbackHandler)) {
//            try {
//                X509Certificate defaultCert = null;
//                SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
//                        new SignatureKeyCallback.DefaultPrivKeyCertRequest();
//                SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
//                Callback[] callbacks = new Callback[]{sigKeyCallback};
//                callbackHandler.handle(callbacks);
//                selfCertificate = privKeyRequest.getX509Certificate();
//            } catch (Exception e) {
//            //ignore for now
//            }
//        }
    }
    /*
     * Applicable only for the signing case
     */

    public X509Certificate getDefaultCertificate(Map context) throws XWSSecurityException {

        X509Certificate defaultCert = null;

        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        //we want to give all runtime properties to be used by CertSelectors
        if (context != null /*&& !isDefaultHandler*/) {
            ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        }
        Callback[] callbacks = new Callback[]{sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureKeyCallback.DefaultPrivKeyCertRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        defaultCert = privKeyRequest.getX509Certificate();

        if (defaultCert == null) {
            log.log(Level.SEVERE, "WSS0218.cannot.locate.default.cert");
            throw new XWSSecurityException(
                    "Unable to locate a default certificate");
        }
        return defaultCert;
    }

    public SignatureKeyCallback.PrivKeyCertRequest getDefaultPrivKeyCertRequest(Map context) throws XWSSecurityException {

        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        //we want all runtime props to be available to certSelectors
        if (context != null /*&& !isDefaultHandler*/) {
            ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        }
        Callback[] callbacks = new Callback[]{sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureKeyCallback.DefaultPrivKeyCertRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        return privKeyRequest;
    }

    public SignatureKeyCallback.AliasPrivKeyCertRequest getAliasPrivKeyCertRequest(String certIdentifier) throws XWSSecurityException {

        SignatureKeyCallback.AliasPrivKeyCertRequest request =
                new SignatureKeyCallback.AliasPrivKeyCertRequest(certIdentifier);

        SignatureKeyCallback sigCallback = new SignatureKeyCallback(request);
        Callback[] callback = new Callback[]{sigCallback};
        try {
            callbackHandler.handle(callback);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureKeyCallback.AliasPrivKeyCertRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        return request;
    }

    public PrivateKey getDefaultPrivateKey(Map context) throws XWSSecurityException {

        PrivateKey defaultPrivKey = null;

        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.DefaultPrivKeyCertRequest();
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
        //we want to give all runtime props to CertSelector(s)
        if (context != null /*&& !isDefaultHandler*/) {
            ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
        }
        Callback[] callbacks = new Callback[]{sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureKeyCallback.DefaultPrivKeyCertRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        defaultPrivKey = privKeyRequest.getPrivateKey();

        if (defaultPrivKey == null) {
            log.log(Level.SEVERE, "WSS0219.cannot.locate.default.privkey");
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
            //if (!isDefaultHandler) {
                ProcessingContext.copy(encKeyCallback.getRuntimeProperties(), context);
            //}
            Callback[] callbacks = new Callback[]{encKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"EncryptionKeyCallback.AliasSymmetricKeyRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            symmetricKey = symmKeyRequest.getSymmetricKey();
        } else {
            DecryptionKeyCallback.SymmetricKeyRequest symmKeyRequest =
                    new DecryptionKeyCallback.AliasSymmetricKeyRequest(alias);
            DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(symmKeyRequest);
            //if (!isDefaultHandler) {
                ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
            //}
            Callback[] callbacks = new Callback[]{decryptKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"DecryptionKeyCallback.AliasSymmetricKeyRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            symmetricKey = symmKeyRequest.getSymmetricKey();
        }

        if (symmetricKey == null) {
            log.log(Level.SEVERE, "WSS0220.cannot.locate.symmetrickey.for.decrypt");
            throw new XWSSecurityException(
                    "Could not locate the symmetric key for alias '" + alias + "'");
        }
        return symmetricKey;
    }

    public X509Certificate getCertificate(Map context, String alias, boolean forSigning)
            throws XWSSecurityException {

        X509Certificate cert = null;

        if (((alias == null) || ("".equals(alias)) && forSigning)) {
            return getDefaultCertificate(context);
        }

        if (forSigning) {
            SignatureKeyCallback.PrivKeyCertRequest certRequest =
                    new SignatureKeyCallback.AliasPrivKeyCertRequest(alias);
            SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(certRequest);
//            if (!isDefaultHandler) {
            ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
//            }
            Callback[] callbacks = new Callback[]{sigKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"SignatureKeyCallback.AliasPrivKeyCertRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            cert = certRequest.getX509Certificate();
        } else {
            EncryptionKeyCallback.X509CertificateRequest certRequest =
                    new EncryptionKeyCallback.AliasX509CertificateRequest(alias);
            EncryptionKeyCallback encKeyCallback = new EncryptionKeyCallback(certRequest);
            //incase of EncryptionKeyCallback.AliasX509Request we need all runtime, properties
            //so we can pass them to CertSelector(s) if any...
            //if (!isDefaultHandler) {
            ProcessingContext.copy(encKeyCallback.getRuntimeProperties(), context);
            //} else {
            //    encKeyCallback.getRuntimeProperties().
            //           put(MessageConstants.AUTH_SUBJECT, context.get(MessageConstants.AUTH_SUBJECT));
            //}
            Callback[] callbacks = new Callback[]{encKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"EncryptionKeyCallback.AliasX509CertificateRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            cert = certRequest.getX509Certificate();
        }

        if (cert == null) {
            String val = forSigning ? "Signature" : "Key Encryption";
            log.log(Level.SEVERE, "WSS0221.cannot.locate.cert", new Object[]{val});
            throw new XWSSecurityException(
                    "Unable to locate certificate for the alias '" + alias + "'");
        }
        return cert;
    }

    public X509Certificate getCertificate(Map context, PublicKey publicKey, boolean forSign)
            throws XWSSecurityException {
        if (!forSign) {
            SignatureVerificationKeyCallback.PublicKeyBasedRequest pubKeyReq =
                    new SignatureVerificationKeyCallback.PublicKeyBasedRequest(publicKey);
            SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(pubKeyReq);
//            if (!isDefaultHandler) {
            ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
//            }
            Callback[] callbacks = new Callback[]{verifyKeyCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (UnsupportedCallbackException e1) {
            //ignore;
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"SignatureVerificationKeyCallback.PublicKeyBasedRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            return pubKeyReq.getX509Certificate();
        } else {
            EncryptionKeyCallback.PublicKeyBasedRequest pubKeyReq =
                    new EncryptionKeyCallback.PublicKeyBasedRequest(publicKey);
            EncryptionKeyCallback encCallback = new EncryptionKeyCallback(pubKeyReq);
//            if (!isDefaultHandler) {
            ProcessingContext.copy(encCallback.getRuntimeProperties(), context);
//            }
            Callback[] callbacks = new Callback[]{encCallback};
            try {
                callbackHandler.handle(callbacks);
            } catch (UnsupportedCallbackException e1) {
            //ignore;
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"EncryptionKeyCallback.PublicKeyBasedRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            return pubKeyReq.getX509Certificate();
        }
    }

    public PrivateKey getPrivateKey(Map context, String alias)
            throws XWSSecurityException {

        PrivateKey privKey = null;

        if (alias == null) {
            return getDefaultPrivateKey(context);
        }

        SignatureKeyCallback.PrivKeyCertRequest privKeyRequest =
                new SignatureKeyCallback.AliasPrivKeyCertRequest(alias);
        SignatureKeyCallback sigKeyCallback = new SignatureKeyCallback(privKeyRequest);
//        if (!isDefaultHandler) {
        ProcessingContext.copy(sigKeyCallback.getRuntimeProperties(), context);
//        }
        Callback[] callbacks = new Callback[]{sigKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureKeyCallback.AliasPrivKeyCertRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        privKey = privKeyRequest.getPrivateKey();

        if (privKey == null) {
            log.log(Level.SEVERE, "WSS0222.cannot.locate.privkey", new Object[]{alias});
            throw new XWSSecurityException(
                    "Unable to locate private key for the alias " + alias);
        }
        return privKey;
    }

    public PrivateKey getPrivateKey(Map context, byte[] identifier, String valueType)
            throws XWSSecurityException {
        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getPrivateKey(context, identifier);
        }

        PrivateKey privateKey = null;

        DecryptionKeyCallback.PrivateKeyRequest privKeyRequest =
                new DecryptionKeyCallback.ThumbprintBasedRequest(identifier);
        DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(privKeyRequest);
//        if (!isDefaultHandler) {
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
//        }
        Callback[] callbacks = new Callback[]{decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"DecryptionKeyCallback.ThumbprintBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        privateKey = privKeyRequest.getPrivateKey();

        if (privateKey == null) {
            // not found so throw an exception
            log.log(Level.SEVERE, "WSS0222.cannot.locate.privkey", new Object[]{identifier});
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
//        if (!isDefaultHandler) {
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
//        }
        Callback[] callbacks = new Callback[]{decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"DecryptionKeyCallback.X509SubjectKeyIdentifierBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        privateKey = privKeyRequest.getPrivateKey();

        if (privateKey == null) {
            // not found so throw an exception
            log.log(Level.SEVERE, "WSS0222.cannot.locate.privkey", new Object[]{keyIdentifier});
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
//        if (!isDefaultHandler) {
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
//        }
        Callback[] callbacks = new Callback[]{decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"DecryptionKeyCallback.X509IssuerSerialBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        privateKey = privKeyRequest.getPrivateKey();

        if (privateKey == null) {
            // not found so throw an exception
            log.log(Level.SEVERE, "WSS0222.cannot.locate.privkey",
                    new Object[]{serialNumber + ":" + issuerName});
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
        if (MessageConstants.KEY_INDETIFIER_TYPE.equals(valueType)) {
            return getCertificate(context, identifier);
        }

        //Else if it is Thumbprint
        X509Certificate cert = null;

        SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                new SignatureVerificationKeyCallback.ThumbprintBasedRequest(identifier);
        SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
        //if (!isDefaultHandler) {
            ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
        //}
        Callback[] callbacks = new Callback[]{verifyKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureVerificationKeyCallback.ThumbprintBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);

        }
        cert = certRequest.getX509Certificate();

        if (cert == null) {
            // not found so throw an exception
            log.log(Level.SEVERE, "WSS0221.cannot.locate.cert", new Object[]{identifier});
            throw new XWSSecurityException("No Matching public key for " + Base64.encode(identifier) + " thumb print identifier found");
        }
        return cert;
    }

    public X509Certificate getCertificate(Map context, byte[] keyIdentifier)
            throws XWSSecurityException {

        X509Certificate cert = null;

        SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                new SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest(keyIdentifier);
        SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
        //if (!isDefaultHandler) {
            ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
        //}
        Callback[] callbacks = new Callback[]{verifyKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureVerificationKeyCallback.X509SubjectKeyIdentifierBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);

        }
        cert = certRequest.getX509Certificate();

        if (cert == null) {
            // not found so throw an exception
            log.log(Level.SEVERE, "WSS0221.cannot.locate.cert", new Object[]{keyIdentifier});
            throw new XWSSecurityException("No Matching public key for " + Base64.encode(keyIdentifier) + " subject key identifier found");
        }
        return cert;
    }

    public PublicKey getPublicKey(Map context, BigInteger serialNumber, String issuerName)
            throws XWSSecurityException {

        return getCertificate(context, serialNumber, issuerName).getPublicKey();
    }

    public X509Certificate getCertificate(Map context, BigInteger serialNumber, String issuerName)
            throws XWSSecurityException {

        X509Certificate cert = null;

        SignatureVerificationKeyCallback.X509CertificateRequest certRequest =
                new SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest(issuerName, serialNumber);
        SignatureVerificationKeyCallback verifyKeyCallback = new SignatureVerificationKeyCallback(certRequest);
        //if (!isDefaultHandler) {
            ProcessingContext.copy(verifyKeyCallback.getRuntimeProperties(), context);
        //}
        Callback[] callbacks = new Callback[]{verifyKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"SignatureVerificationKeyCallback.X509IssuerSerialBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        cert = certRequest.getX509Certificate();

        if (cert == null) {
            // not found so throw an exception
            log.log(Level.SEVERE, "WSS0221.cannot.locate.cert", new Object[]{serialNumber + ":" + issuerName});
            throw new XWSSecurityException(
                    "No Matching public key for serial number " + serialNumber + " and issuer name " + issuerName + " found");
        }

        return cert;
    }

    public boolean validateCertificate(X509Certificate cert, Map context)
            throws XWSSecurityException {

        CertificateValidationCallback certValCallback = new CertificateValidationCallback(cert, context);
        Callback[] callbacks = new Callback[]{certValCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0223.failed.certificate.validation");
            throw newSOAPFaultException(
                    MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "Certificate validation failed",
                    e);
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Certificate Validation called on certificate " + cert.getSubjectDN());
        }
        return certValCallback.getResult();
        
    }

    public void updateOtherPartySubject(
            final Subject subject, final String username, final String password) {
    //do nothing....
    }

    private void updateUsernameInSubject(
            final Subject subject, final String username, final String password) {

        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                String x500Name = "CN=" + username;
                // we can remove this and make a CallerPrincipalCallback
                Principal principal = null;
                try {
                    principal = new X500Principal(x500Name);
                    subject.getPrincipals().add(principal);
                } catch(Throwable t) {


                    //not all principals can be X500Names
                    //ignore if there was an Exception
                }
                subject.getPublicCredentials().add(username);
                //do not update password in subject ?.
//                if (password != null) {
//                    subject.getPrivateCredentials().add(password);
//                }
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
         if (callbackHandler instanceof DefaultCallbackHandler) {
                if (((DefaultCallbackHandler)callbackHandler).getSAMLValidator() 
                        instanceof SAMLValidator)
                return;
        }
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
//        AccessController.doPrivileged(new PrivilegedAction() {
//
//            public Object run() {
//                subject.getPublicCredentials().add(secretKey);
//                return null; // nothing to return
//            }
//        });
    }

    public void updateOtherPartySubject(
            final Subject subject,
            final String ek) {
//        AccessController.doPrivileged(new PrivilegedAction() {
//
//            public Object run() {
//                String encryptedKey = "EK" + ek;
//                subject.getPublicCredentials().add(encryptedKey);
//                return null; // nothing to return
//            }
//        });
    }

    public static Subject getSubject(final Map context) {
        Subject otherPartySubject =
                (Subject) context.get(MessageConstants.AUTH_SUBJECT);
        if (otherPartySubject != null) {
            return otherPartySubject;
        }
        otherPartySubject =
                (Subject) AccessController.doPrivileged(
                new PrivilegedAction() {

                    public Object run() {
                        Subject otherPartySubj = new Subject();
                        context.put(MessageConstants.AUTH_SUBJECT, otherPartySubj);
                        return otherPartySubj;
                    }
                });
        return otherPartySubject;
    }

    public static Subject getSubject(final FilterProcessingContext context) {
        Subject otherPartySubject = (Subject) context.getExtraneousProperty(MessageConstants.AUTH_SUBJECT);
        if (otherPartySubject != null) {
            return otherPartySubject;
        }
        otherPartySubject =
                (Subject) AccessController.doPrivileged(
                new PrivilegedAction() {

                    public Object run() {
                        Subject otherPartySubj = new Subject();
                        context.setExtraneousProperty(MessageConstants.AUTH_SUBJECT, otherPartySubj);
                        return otherPartySubj;
                    }
                });
        return otherPartySubject;
    }

    public PrivateKey getPrivateKey(Map context, X509Certificate cert)
            throws XWSSecurityException {

        PrivateKey privateKey = null;

        DecryptionKeyCallback.PrivateKeyRequest privateKeyRequest =
                new DecryptionKeyCallback.X509CertificateBasedRequest(cert);
        DecryptionKeyCallback decryptKeyCallback = new DecryptionKeyCallback(privateKeyRequest);
//        if (!isDefaultHandler) {
        ProcessingContext.copy(decryptKeyCallback.getRuntimeProperties(), context);
//        }
        Callback[] callbacks = new Callback[]{decryptKeyCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"DecryptionKeyCallback.X509CertificateBasedRequest"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        privateKey = privateKeyRequest.getPrivateKey();

        if (privateKey == null) {
            log.log(Level.SEVERE, "WSS0222.cannot.locate.privkey", new Object[]{"given certificate"});
            throw new XWSSecurityException(
                    "Could not retrieve private Key matching the given certificate");
        }
        return privateKey;
    }

    public PrivateKey getPrivateKey(Map context, PublicKey publicKey, boolean forSign)
            throws XWSSecurityException {
        if (forSign) {
            SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest req =
                    new SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest(publicKey);
            SignatureKeyCallback skc = new SignatureKeyCallback(req);
//            if (!isDefaultHandler) {
            ProcessingContext.copy(skc.getRuntimeProperties(), context);
//            }
            Callback[] callbacks = new Callback[]{skc};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"SignatureKeyCallback.PublicKeyBasedPrivKeyCertRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            return req.getPrivateKey();
        } else {
            DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest req =
                    new DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest(publicKey);
            DecryptionKeyCallback dkc = new DecryptionKeyCallback(req);
//            if (!isDefaultHandler) {
            ProcessingContext.copy(dkc.getRuntimeProperties(), context);
//            }
            Callback[] callbacks = new Callback[]{dkc};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                        new Object[]{"DecryptionKeyCallback.PublicKeyBasedPrivKeyRequest"});
                log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
                throw new XWSSecurityException(e);
            }
            return req.getPrivateKey();
        }
    }

    public Subject getSubject() {
        log.log(Level.SEVERE, "WSS0224.unsupported.AssociatedSubject");
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
        Callback[] callbacks = new Callback[]{passwordValidationCallback};

        boolean result = false;
        try {
            callbackHandler.handle(callbacks);
            RealmAuthenticationAdapter adapter = passwordValidationCallback.getRealmAuthenticationAdapter();
            if (passwordValidationCallback.getValidator() != null) {
                result = passwordValidationCallback.getResult();
                if (result == true) {
                    updateUsernameInSubject(getSubject(context), username, null);
                }
            } else if (adapter != null) {
                result = adapter.authenticate(getSubject(context), username, passwordDigest, nonce, created, context);
            } else {
                log.log(Level.SEVERE, "WSS0295.password.val.not.config.username.val");
                throw new XWSSecurityException("Error: No PasswordValidator Configured for UsernameToken Validation");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0225.error.PasswordValidationCallback", e);
            throw new XWSSecurityException(e);
        }
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Username Authentication done for " + username);
        }
        return result;
    }

    public boolean authenticateUser(Map context, String username, String password)
            throws XWSSecurityException {

        PasswordValidationCallback.PlainTextPasswordRequest request =
                new PasswordValidationCallback.PlainTextPasswordRequest(username, password);
        PasswordValidationCallback passwordValidationCallback =
                new PasswordValidationCallback(request);
        ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[]{passwordValidationCallback};
        boolean result = false;
        try {
            callbackHandler.handle(callbacks);
            RealmAuthenticationAdapter adapter = passwordValidationCallback.getRealmAuthenticationAdapter();
            if (passwordValidationCallback.getValidator() != null) {
                result = passwordValidationCallback.getResult();
                if (result == true) {
                    updateUsernameInSubject(getSubject(context), username, password);
                }
            } else if (adapter != null) {
                result = adapter.authenticate(getSubject(context), username, password, context);
            } else {
                log.log(Level.SEVERE, "WSS0295.password.val.not.config.username.val");
                throw new XWSSecurityException("Error: No PasswordValidator Configured for UsernameToken Validation");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0225.error.PasswordValidationCallback", e);
            throw new XWSSecurityException(e);
        }
        return result;
    }

    public String authenticateUser(Map context, String username )
            throws XWSSecurityException {

        PasswordValidationCallback.DerivedKeyPasswordRequest request =
                new PasswordValidationCallback.DerivedKeyPasswordRequest(username);
        PasswordValidationCallback passwordValidationCallback =
                new PasswordValidationCallback(request);
        ProcessingContext.copy(passwordValidationCallback.getRuntimeProperties(), context);
        Callback[] callbacks = new Callback[]{passwordValidationCallback};
        boolean result = false;
        try {
            callbackHandler.handle(callbacks);
            RealmAuthenticationAdapter adapter = passwordValidationCallback.getRealmAuthenticationAdapter();
            if (passwordValidationCallback.getValidator() != null) {
                result = passwordValidationCallback.getResult();

                if (result == true) {
                updateUsernameInSubject(getSubject(context), username, null);
                }
            } 
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0225.error.PasswordValidationCallback", e);
            throw new XWSSecurityException(e);
        }
        return request.getPassword();
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
            synchronized (calendarFormatter1) {
                created = calendarFormatter1.parse(creationTime);
            }
        } catch (java.text.ParseException e) {
            synchronized (calendarFormatter2) {
                try {
                    created = calendarFormatter2.parse(creationTime);
                } catch (java.text.ParseException ex) {
                    log.log(Level.SEVERE, "WSS0226.failed.Validating.DefaultCreationTime", ex);
                    throw new XWSSecurityException(ex);
                    
                }
            }
        }


        Date current = getFreshnessAndSkewAdjustedDate(maxClockSkew, timestampFreshnessLimit);

        if (created.before(current)) {
            log.log(Level.SEVERE, "WSS0227.invalid.older.CreationTime");
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,  "The creation time is older than " +
                    " currenttime - timestamp-freshness-limit - max-clock-skew", null);
        }

        Date currentTime =
                getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, true);
        if (currentTime.before(created)) {
            log.log(Level.SEVERE, "WSS0228.invalid.ahead.CreationTime");
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "The creation time is ahead of the current time.", null);
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
                timestampFreshnessLimit);

        request.isUsernameToken(true);
        TimestampValidationCallback timestampValidationCallback =
                new TimestampValidationCallback(request);
        if (!isDefaultHandler) {
            ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
        }
        Callback[] callbacks = new Callback[]{timestampValidationCallback};
        boolean unSupported = false;
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            unSupported = true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0226.failed.Validating.DefaultCreationTime");
            throw new XWSSecurityException(e);
        }
        
        if (unSupported) {
            defaultValidateCreationTime(creationTime, maxClockSkew, timestampFreshnessLimit);
            return;
        }

        try {
            timestampValidationCallback.getResult();
        } catch (TimestampValidationCallback.TimestampValidationException e) {
            log.log(Level.SEVERE, "WSS0229.failed.Validating.TimeStamp", e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
        }
    }

    //TODO implement this using callbacks
    public boolean validateSamlIssuer(String issuer) {
        log.log(Level.SEVERE, "WSS0230.unsupported.Validating.SAMLIssuer");
        throw new UnsupportedOperationException("SAML Issuer Validation not yet supported");
    }

    //TODO implement this using callbacks
    public boolean validateSamlUser(
            String user, String domain, String format) {
        log.log(Level.SEVERE, "WSS0231.unsupported.Validating.SAMLUser");
        throw new UnsupportedOperationException("SAML User Validation not yet supported");
    }

    public String getUsername(Map context) throws XWSSecurityException {
        UsernameCallback usernameCallback = new UsernameCallback();
        /*if (!isDefaultHandler) {*/
        ProcessingContext.copy(usernameCallback.getRuntimeProperties(), context);
        /*}*/
        Callback[] callbacks = new Callback[]{usernameCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0216.callbackhandler.handle.exception",
                    new Object[]{"UsernameCallback"});
            log.log(Level.SEVERE, "WSS0217.callbackhandler.handle.exception.log", e);
            throw new XWSSecurityException(e);
        }
        return usernameCallback.getUsername();
    }

    public String getPassword(Map context) throws XWSSecurityException {
        PasswordCallback passwordCallback = new PasswordCallback();
        /*if (!isDefaultHandler) {*/
        ProcessingContext.copy(passwordCallback.getRuntimeProperties(), context);
        /*}*/
        Callback[] callbacks = new Callback[]{passwordCallback};
        try {
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0225.failed.PasswordValidationCallback", e);
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
            Date expires=null;
            try {
                synchronized (calendarFormatter1) {
                    expires = calendarFormatter1.parse(expirationTime);
                }
            } catch (java.text.ParseException pe) {
                synchronized (calendarFormatter2) {
                    try {
                        expires = calendarFormatter2.parse(expirationTime);
                    } catch (java.text.ParseException e) {
                        log.log(Level.SEVERE, "WSS0394.error.parsing.expirationtime");
                        throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
                    }
                }
            }


            Date currentTime =
                    getGMTDateWithSkewAdjusted(new GregorianCalendar(), maxClockSkew, false);
            if (expires.before(currentTime)) {
                log.log(Level.SEVERE, "WSS0393.current.ahead.of.expires");
                throw SOAPUtil.newSOAPFaultException(MessageConstants.WSU_MESSAGE_EXPIRED,"The current time is ahead of the expiration time in Timestamp",null);
            }
        }

    }

    public void validateTimestamp(
            Map context, Timestamp timestamp, long maxClockSkew, long freshnessLimit)
            throws XWSSecurityException {
        validateTimestamp(context, timestamp.getCreated(), timestamp.getExpires(),
                maxClockSkew, freshnessLimit);
    }

    public void validateTimestamp(Map context, String created,
            String expires, long maxClockSkew, long freshnessLimit)
            throws XWSSecurityException {
        if (expiresBeforeCreated(created, expires)) {
            XWSSecurityException xwsse = new XWSSecurityException("Message expired!");
            log.log(Level.SEVERE, "WSS0232.expired.Message");
            throw newSOAPFaultException(
                    MessageConstants.WSU_MESSAGE_EXPIRED,
                    "Message expired!",
                    xwsse);
        }

        TimestampValidationCallback.UTCTimestampRequest request =
                new TimestampValidationCallback.UTCTimestampRequest(
                created,
                expires,
                maxClockSkew,
                freshnessLimit);

        TimestampValidationCallback timestampValidationCallback =
                new TimestampValidationCallback(request);
        if (!isDefaultHandler) {
            ProcessingContext.copy(timestampValidationCallback.getRuntimeProperties(), context);
        }
        Callback[] callbacks = new Callback[]{timestampValidationCallback};
        boolean unSupported = false;
        try {
            callbackHandler.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            unSupported = true;    
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0229.failed.Validating.TimeStamp", e);
            throw new XWSSecurityException(e);
        }

        if (unSupported) {
            //System.out.println("Validate Timestamp ...");
            defaultValidateCreationTime(created, maxClockSkew, freshnessLimit);
            defaultValidateExpirationTime(expires, maxClockSkew, freshnessLimit);
            return;
        }
        
        try {
            timestampValidationCallback.getResult();
        } catch (TimestampValidationCallback.TimestampValidationException e) {
            log.log(Level.SEVERE, "WSS0229.failed.Validating.TimeStamp", e);
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN, e.getMessage(), e);
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

        if (addSkew) {
            currentTime = currentTime + maxClockSkew;
        } else {
            currentTime = currentTime - maxClockSkew;
        }

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
            synchronized (calendarFormatter1) {
                created = calendarFormatter1.parse(creationTime);
                if (expirationTime != null) {
                    expires = calendarFormatter1.parse(expirationTime);
                }
            }
        } catch (java.text.ParseException pe) {
            synchronized (calendarFormatter2) {
                try {
                    created = calendarFormatter2.parse(creationTime);
                    if (expirationTime != null) {
                        expires = calendarFormatter2.parse(expirationTime);
                    }
                } catch (java.text.ParseException xpe) {
                    log.log(Level.SEVERE, "WSS0233.invalid.expire.before.creation", xpe);
                    throw new XWSSecurityException(xpe.getMessage());
                }
            }
        }


        if ((expires != null) && expires.equals(created)) {
            return true;
        }

        if ((expires != null) && expires.before(created)) {
            return true;
        }

        return false;
    }

    
    public void validateSAMLAssertion(Map context, Element assertion) throws XWSSecurityException {

        AuthenticationTokenPolicy authPolicy = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                (AuthenticationTokenPolicy.SAMLAssertionBinding) authPolicy.newSAMLAssertionFeatureBinding();
        samlPolicy.setAssertion(assertion);

        DynamicPolicyCallback dynamicCallback =
                new DynamicPolicyCallback(samlPolicy, null);
        //let runtime properties be visible here
//        if (!isDefaultHandler) {
        ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), context);
//        } else {
//            dynamicCallback.getRuntimeProperties().
//                    put(MessageConstants.AUTH_SUBJECT, context.get(MessageConstants.AUTH_SUBJECT));
//        }
        try {
            Callback[] callbacks = new Callback[]{dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0234.failed.Validate.SAMLAssertion", e);
            throw SOAPUtil.newSOAPFaultException(
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
        //let runtime props be visible here
//        if (!isDefaultHandler) {
            ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), context);
//        }
        try {
            Callback[] callbacks = new Callback[]{dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0235.failed.locate.SAMLAssertion", e);
            throw new XWSSecurityException(e);
        }
        Element assertion = samlPolicy.getAssertion();
        if (assertion == null) {
            log.log(Level.SEVERE, "WSS0236.null.SAMLAssertion");
            throw new XWSSecurityException("SAML Assertion not set into Policy by CallbackHandler");
        }

        return assertion;
    }

    public AuthenticationTokenPolicy.SAMLAssertionBinding populateSAMLPolicy(Map fpcontext, AuthenticationTokenPolicy.SAMLAssertionBinding policy,
            DynamicApplicationContext context)
            throws XWSSecurityException {

        DynamicPolicyCallback dynamicCallback =
                new DynamicPolicyCallback(policy, context);
        if (context != null /* && !isDefaultHandler*/) {
            ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), fpcontext);
        }
        try {
            Callback[] callbacks = new Callback[]{dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0237.failed.DynamicPolicyCallback", e);
            throw new XWSSecurityException(e);
        }
        return (AuthenticationTokenPolicy.SAMLAssertionBinding) dynamicCallback.getSecurityPolicy();
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }
    
    private void validateSamlVersion(Assertion assertion) {
        BigInteger major = ((com.sun.xml.wss.saml.Assertion) assertion).getMajorVersion();
        BigInteger minor = ((com.sun.xml.wss.saml.Assertion) assertion).getMinorVersion();

        if (major.intValue() != 1) {
            log.log(Level.SEVERE, "WSS0404.saml.invalid.version");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "Major version is not 1 for SAML Assertion:" + ((com.sun.xml.wss.saml.Assertion) assertion).getAssertionID(),
                    new Exception(
                    "Major version is not 1 for SAML Assertion"));
        }

        if ((minor.intValue() != 0) && (minor.intValue() != 1)) {
            log.log(Level.SEVERE, "WSS0404.saml.invalid.version");
            throw SecurableSoapMessage.newSOAPFaultException(
                    MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                    "Minor version is not 0/1 for SAML Assertion:" + ((com.sun.xml.wss.saml.Assertion) assertion).getAssertionID(),
                    new Exception(
                    "Minor version is not 0/1 for SAML Assertion"));
        }
    }

    private void validateIssuer(
            SecurableSoapMessage secMessage,
            Assertion assertion) {
    }

    private void validateSamlUser(
            SecurableSoapMessage secMessage,
            Assertion assertion) {
        String user = null;

    }

    public void validateSAMLAssertion(Map context, XMLStreamReader assertion) throws XWSSecurityException {

        AuthenticationTokenPolicy authPolicy = new AuthenticationTokenPolicy();
        AuthenticationTokenPolicy.SAMLAssertionBinding samlPolicy =
                (AuthenticationTokenPolicy.SAMLAssertionBinding) authPolicy.newSAMLAssertionFeatureBinding();
        samlPolicy.setAssertion(assertion);

        DynamicPolicyCallback dynamicCallback =
                new DynamicPolicyCallback(samlPolicy, null);
//        if (!isDefaultHandler) {
            ProcessingContext.copy(dynamicCallback.getRuntimeProperties(), context);
//        } else {
            if (context.get(MessageConstants.AUTH_SUBJECT) == null) {
            dynamicCallback.getRuntimeProperties().
                    put(MessageConstants.AUTH_SUBJECT, this.getSubject(context));
            }
//        }
        try {
            Callback[] callbacks = new Callback[]{dynamicCallback};
            callbackHandler.handle(callbacks);
        } catch (Exception e) {
            log.log(Level.SEVERE, "WSS0234.failed.Validate.SAMLAssertion", e);
            throw SOAPUtil.newSOAPFaultException(
                    MessageConstants.WSSE_FAILED_AUTHENTICATION,
                    "Validation failed for SAML Assertion ", e);
        }
    }

    public void updateOtherPartySubject(final Subject subject, final XMLStreamReader assertion) {
        if (callbackHandler instanceof DefaultCallbackHandler) {
                if (((DefaultCallbackHandler)callbackHandler).getSAMLValidator() 
                        instanceof SAMLValidator)
                return;
        }
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                subject.getPublicCredentials().add(assertion);
                return null; // nothing to return
            }
        });
    }

    public boolean isSelfCertificate(X509Certificate cert) {
//        if (this.selfCertificate != null && this.selfCertificate.equals(cert)) {
//            return true;
//        }
        return false;
    }

    public void updateOtherPartySubject(Subject subject, Subject bootStrapSubject) {
        SecurityUtil.copySubject(subject, bootStrapSubject);
    }

    public KerberosContext doKerberosLogin() throws XWSSecurityException {
        String loginModule = configAssertions.getProperty(DefaultCallbackHandler.KRB5_LOGIN_MODULE);
        String servicePrincipal = configAssertions.getProperty(DefaultCallbackHandler.KRB5_SERVICE_PRINCIPAL);
        boolean credentialDelegation = Boolean.valueOf(configAssertions.getProperty(DefaultCallbackHandler.KRB5_CREDENTIAL_DELEGATION));
        if (loginModule == null || loginModule.equals("")) {
            throw new XWSSecurityException("Login Module for Kerberos login is not set or could not be obtained");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new XWSSecurityException("Kerberos Service Principal is not set or could not be obtained");
        }
        return new KerberosLogin().login(loginModule, servicePrincipal, credentialDelegation);
    }

    public KerberosContext doKerberosLogin(byte[] tokenValue) throws XWSSecurityException {
        String loginModule = configAssertions.getProperty(DefaultCallbackHandler.KRB5_LOGIN_MODULE);
        return new KerberosLogin().login(loginModule, tokenValue);
    }
    
    public void updateOtherPartySubject(final Subject subject, 
            final GSSName clientCred, 
            final GSSCredential gssCred) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                KerberosPrincipal kerbPrincipal = new KerberosPrincipal(clientCred.toString());
                subject.getPrincipals().add(kerbPrincipal);
                subject.getPublicCredentials().add(clientCred);
                if(gssCred != null){
                    subject.getPrivateCredentials().add(gssCred);
                }
                return null; // nothing to return
            }
        });
    }

    public boolean validateAndCacheNonce(Map context,String nonce, String created, long nonceAge) throws XWSSecurityException {
        NonceManager nonceMgr = null;
        if (this.mnaProperty != null) {
            nonceMgr = NonceManager.getInstance(this.maxNonceAge, (WSEndpoint)context.get(MessageConstants.WSENDPOINT));
        } else {
            nonceMgr = NonceManager.getInstance(nonceAge, (WSEndpoint)context.get(MessageConstants.WSENDPOINT));
        }   
        
        return nonceMgr.validateNonce(nonce, created);
    }
       
        
}
