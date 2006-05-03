/*
 * $Id: SecurityEnvironment.java,v 1.1 2006-05-03 22:57:30 arungupta Exp $
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

package com.sun.xml.wss;

import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import javax.crypto.SecretKey;

import com.sun.xml.wss.core.Timestamp;
import com.sun.xml.wss.saml.Assertion;
//import com.sun.xml.wss.saml.assertion.saml11.jaxb10.AuthorityBinding;

import com.sun.xml.wss.impl.policy.mls.AuthenticationTokenPolicy;
import com.sun.xml.wss.impl.configuration.DynamicApplicationContext;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.util.Map;

/**
 * The SecurityEnvironment interface links the XWS-Security runtime with the 
 * environment/container in which it is running. The SecurityEnvironment interface
 * is invoked by the XWS-Security runtime to perform tasks such as retrieving keys,
 * validating tokens etc.
 *<P>
 * When using the XWS-Security configuration files with &lt;xwss:JAXRPCSecurity&gt; as 
 * the root element, a default implementation of this interface wraps the supplied CallbackHandler. 
 * The default implemenation of this interface bundled with XWS-Security invokes the CallbackHandler
 * to implement the methods of this interface.
 *<P>
 * This interface facilitates usage of XWS-Security in environments which do not necessarily have a
 * natural mapping to the XWS-Security defined callbacks.
 *<P>
 * There is generally a single SecurityEnvironment instance per-application, which is initialized at application startup.
 *<P>
 *Note: This interface is evolving and is subject to change in a later release
 */ 
public interface SecurityEnvironment {

    /**
     * Retrieves a reasonable default value for the current user's
     * X509Certificate if one exists.
     * @param context a Map of application and integration-layer specific properties 
     * @return the default certificate for the current user
     * @throws XWSSecurityException
     */
    public X509Certificate getDefaultCertificate(Map context) 
        throws XWSSecurityException;

    /**
     * @return the certificate corresponding to the alias
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param  alias the alias for identifying the certificate
     * @param  forSigning whether this request is for a Sign operation or Encrypt
     * @exception XWSSecurityException if there was an error while trying to locate the Cerificate
     */
    public X509Certificate getCertificate(Map context, String alias, boolean forSigning) 
        throws XWSSecurityException;

    /**
     *@return the SecretKey corresponding to the alias
     *@param context a Map of application and integration-layer specific properties 
     *@param  alias the alias for identifying the SecretKey
     *@param encryptMode whether this request is for an Encrypt or Decrypt operation
     *@exception XWSSecurityException if there was an error while trying to locate the SecretKey
     */
    public SecretKey getSecretKey(Map context, String alias, boolean encryptMode)
        throws XWSSecurityException;

    /**
     *@return the PrivateKey corresponding to the alias
     *@param context a Map of application and integration-layer specific properties 
     *@param  alias the alias for identifying the PrivateKey
     *@exception XWSSecurityException if there was an error while trying to locate the PrivateKey
     */
    public PrivateKey getPrivateKey(Map context, String alias)
        throws XWSSecurityException;

    /**
     * @return the PublicKey corresponding to a KeyIdentifier
     * @param context a Map of application and integration-layer specific properties 
     * @param keyIdentifier an Opaque identifier indicating 
     *            the X509 certificate.
     * @exception XWSSecurityException  if there was an error while trying to locate the PublicKey
     */
    public PublicKey getPublicKey(Map context, byte[] keyIdentifier)
        throws XWSSecurityException;

    
    public PublicKey getPublicKey(Map context, byte[] keyIdentifier, String valueType)
        throws XWSSecurityException;
    
    /**
     * @return the X509Certificate corresponding to a KeyIdentifier
     * @param context a Map of application and integration-layer specific properties 
     * @param keyIdentifier an Opaque identifier indicating 
     *            the X509 certificate.
     * @exception XWSSecurityException  if there was an error while trying to locate the X509Certificate
     */
    public X509Certificate getCertificate(Map context, byte[] keyIdentifier)
        throws XWSSecurityException;
    
    
    /**
     * @return the X509Certificate corresponding to a KeyIdentifier
     * @param context a Map of application and integration-layer specific properties 
     * @param identifier an Opaque identifier indicating the X509 certificate.
     * @param valueType 
     * @exception XWSSecurityException  if there was an error while trying to locate the X509Certificate
     */
    public X509Certificate getCertificate(Map context, byte[] identifier, String valueType)
        throws XWSSecurityException;

    /**
     * @return the PrivateKey corresponding to the X509Certificate
     * @param context a Map of application and integration-layer specific properties 
     * @param cert the X509Certificate
     * @throws XWSSecurityException if there was an error while trying to locate the PrivateKey
     */
    public PrivateKey getPrivateKey(Map context, X509Certificate cert)
        throws XWSSecurityException;

    /**
     * @return the PrivateKey corresponding to (serialNumber, issuerName)
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param serialNumber the serialNumber of the certificate
     * @param issuerName the issuerName of the certificate
     * @throws XWSSecurityException if there was an error while trying to locate the PrivateKey
     */
    public PrivateKey getPrivateKey(Map context, BigInteger serialNumber, String issuerName)
        throws XWSSecurityException;

    /**
     * @return the X509Certificate corresponding to a PublicKey
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param publicKey  the publicKey
     * @param forSign set to true if the public key is to be used for SignatureVerification
     * @throws XWSSecurityException if there was an error while trying to locate the PublicKey
     */
    public X509Certificate getCertificate(Map context, PublicKey publicKey, boolean forSign) 
        throws XWSSecurityException;

    /**
     * @return the PrivateKey corresponding to a KeyIdentifier
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param keyIdentifier an Opaque identifier indicating 
     *            the X509 certificate.
     * @throws XWSSecurityException if there was an error while trying to locate the PrivateKey
     */
    public PrivateKey getPrivateKey(Map context, byte[] keyIdentifier) 
        throws XWSSecurityException;

    
    public PrivateKey getPrivateKey(Map context, byte[] keyIdentifier, String valueType) 
        throws XWSSecurityException;
    
    /**
     * @return the PrivateKey corresponding to a PublicKey
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param publicKey  the publicKey
     * @param forSign set to true if the purpose is Signature
     * @throws XWSSecurityException if there was an error while trying to locate the PrivateKey
     */
    public PrivateKey getPrivateKey(Map context, PublicKey publicKey, boolean forSign) 
        throws XWSSecurityException;

    /**
     * @return the PublicKey corresponding to (serialNumber, issuerName)
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param serialNumber the serialNumber of the certificate
     * @param issuerName the issuerName of the certificate
     * @throws XWSSecurityException if there was an error while trying to locate the PublicKey
     */
    public PublicKey getPublicKey(Map context, BigInteger serialNumber, String issuerName)
        throws XWSSecurityException;

    /**
     * @return the X509Certificate corresponding to (serialNumber, issuerName)
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param serialNumber the serialNumber of the certificate
     * @param issuerName the issuerName of the certificate
     * @throws XWSSecurityException if there was an error while trying to locate the X509Certificate
     */
    public X509Certificate getCertificate(Map context, BigInteger serialNumber, String issuerName)
        throws XWSSecurityException;

    /**
     * Authenticate the user against a list of known username-password
     * pairs.
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param username the username
     * @param password the password
     * @return true if the username-password pair is valid, false otherwise
     * @throws XWSSecurityException if there was an error while trying to authenticate the username
     */
    public boolean authenticateUser(Map context, String username, String password) 
           throws XWSSecurityException;

    /**
     * Authenticate the user given the password digest.
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param username the username
     * @param passwordDigest the digested password
     * @param nonce the nonce which was part of the digest
     * @param created the creation time which was part of the digest
     * @return true if the password digest is valid, false otherwise
     * @throws XWSSecurityException if there was an error while trying to authenticate the username
     */
    public boolean authenticateUser(
        Map context,
        String username,
        String passwordDigest,
        String nonce,
        String created)
        throws XWSSecurityException;

    /**
     * @return the host/sender Subject, 
     *  null if subject is not available/initialized
     */
    public Subject getSubject();


    /**
     * Validate the creation time. It is an error if the
     * creation time is older than current local time minus 
     * TIMESTAMP_FRESHNESS_LIMIT minus MAX_CLOCK_SKEW
     *
     * @param context a Map of application and integration-layer specific properties 
     * @param creationTime the creation-time value
     * @param maxClockSkew (in milliseconds) the maximum clockskew
     * @param timestampFreshnessLimit (in milliseconds) the limit for which timestamps 
     * are considered fresh
     * @throws XWSSecurityException if there was an error while trying to validate the creationTime
     */
    public void validateCreationTime(
        Map context, String creationTime, long maxClockSkew, long timestampFreshnessLimit) 
        throws XWSSecurityException;

    /**
     * Validate the expiration time (wsu:Expires). It is an error if the
     * expiration time is older than current local time minus MAX_CLOCK_SKEW
     * @return true if this expiration time is valid
     * @param expirationTime the expiration-time value
     * @param maxClockSkew (in milliseconds) the maximum clockskew
     * @param timestampFreshnessLimit (in milliseconds) the limit for which timestamps 
     * are considered fresh
     * @throws XWSSecurityException if there was an error while trying to validate the expirationTime
     */
    /*public boolean validateExpirationTime(
        String expirationTime, long maxClockSkew, long timestampFreshnessLimit) 
        throws XWSSecurityException;*/

    /**
     * Validate an X509Certificate.
     * @return true, if the cert is a valid one, false otherwise.
     * @param cert the X509Certificate to be validated
     * @throws XWSSecurityException
     *     if there is some problem during validation.
     */
    public boolean validateCertificate(X509Certificate cert) 
        throws XWSSecurityException;

    /**
     * Update the public/private credentials of the subject of the party
     * whose username password pair is given.
     * @param subject the Subject of the requesting party
     * @param username the username of the requesting party
     * @param password the password of the requesting party
     */
    public void updateOtherPartySubject(
        Subject subject,
        String username,
        String password);

    /**
     * Update the public credentials of the subject of the party
     * whose certificate is given.
     * @param subject the Subject of the requesting party
     * @param cert the X509Certificate of the requesting party
     */
    public void updateOtherPartySubject(
        Subject subject,
        X509Certificate cert);

    /**
     * Update the public credentials of the subject of the party
     * whose Assertion is given.
     * @param subject the Subject of the requesting party
     * @param assertion the SAML Assertion of the requesting party
     */
    public void updateOtherPartySubject(
        Subject subject,
        Assertion assertion);
    
    /**
     * Update the public credentials of the subject of the party
     * whose EncryptedKey is given.
     * @param subject the Subject of the requesting party
     * @param secretKey the secretKey used
     */
    public void updateOtherPartySubject(
        Subject subject,
        Key secretKey);
    
    /**
     * Update the public credentials of the subject of the party
     * whose EncryptedKey is given.
     * @param subject the Subject of the requesting party
     * @param encryptedKey the SHA-1 of the cipherValue of EncryptedKey
     */
    public void updateOtherPartySubject(
        Subject subject,
        String encryptedKey);

   /* The three methods below are required to insulate integrating environments 
    * such as JSR 196 etc from XWS Policies and Dynamic Policy Callback
    * Also the SAML infrastructure would be different in different containers
    * The default implementation in JWSDP will make a DP callback, but
    * the notion of DP callbacks does not apply for JSR 196
    */

   /**
    * Validate the received SAML Assertion
    * Validations can include validating the Issuer and the Saml User, SAML Version etc.
    * Note: The SAML Condition (notBefore, notOnOrAfter) is validated by the XWS runtime
    *
    * @param context a Map of application and integration-layer specific properties 
    * @param assertion the Assertion to be validated
    * @throws XWSSecurityException  if there was an error while validating the SAML Assertion
    */
   public void validateSAMLAssertion(Map context, Element assertion) throws XWSSecurityException;

   /**
    * Locate and return a SAML Assertion, given the Authority binding and assertionId
    *
    * @param context a Map of application and integration-layer specific properties 
    * @param binding an org.w3c.dom.Element representing the SAML AuthorityBinding 
    * @param assertionId the Assertion ID of the SAML Assertion
    * @param ownerDoc the owner document into which the returned SAML Assertion should be imported to
    * @throws XWSSecurityException  if there was an error while trying to locate the SAML Assertion
    */
   public Element locateSAMLAssertion(
       Map context, Element binding, String assertionId, Document ownerDoc) 
       throws XWSSecurityException;

   /**
    * Locate and update the Policy argument with the SAML Assertion and/or the AuthorityBinding 
    *  and Assertion ID information.
    * The DynamicApplicationContext may contain information to be used by the implementation to make 
    * its runtime decisions on how to obtaim the SAML Assertion
    *
    * @param fpcontext a Map of application and integration-layer specific properties 
    * @param policy the SAML Assertion Policy to be populated
    * @param context the DynamicApplicationContext
    * @return populated SAML Assertion policy
    * @throws XWSSecurityException  if there was an error while trying to populate the SAML Assertion Policy
    */
   public AuthenticationTokenPolicy.SAMLAssertionBinding populateSAMLPolicy(Map fpcontext, AuthenticationTokenPolicy.SAMLAssertionBinding policy, 
       DynamicApplicationContext context) 
       throws XWSSecurityException;

   /**
    * 
    * @param context a Map of application and integration-layer specific properties 
    * @return the username using UsernameCallback
    * @throws XWSSecurityException  if there was an error while trying obtain the username
    */
   public String getUsername(Map context) throws XWSSecurityException;

   /**
    * 
    * @param context a Map of application and integration-layer specific properties 
    * @return the password using PasswordCallback
    * @throws XWSSecurityException  if there was an error while trying obtain the password
    */
   public String getPassword(Map context) throws XWSSecurityException;


    /**
     * Validate the creation time. It is an error if the
     * creation time is older than current local time minus 
     * TIMESTAMP_FRESHNESS_LIMIT minus MAX_CLOCK_SKEW
     * 
     * @param context a Map of application and integration-layer specific properties 
     * @param timestamp the Timestamp element
     * @param maxClockSkew (in milliseconds) the maximum clockskew
     * @param freshnessLimit (in milliseconds) the limit for which timestamps 
     * are considered fresh
     * @throws XWSSecurityException  if there was an error while trying validate the Timestamp
     */
   public void validateTimestamp(
        Map context, Timestamp timestamp, long maxClockSkew, long freshnessLimit)
        throws XWSSecurityException;

   /**
    * @return any Callback Handler associated with this Environment, null otherwise
    * @throws XWSSecurityException  if there was an error while trying retrieve the CallbackHandler
    */
   public CallbackHandler getCallbackHandler()
       throws XWSSecurityException;

    /**
     * Validate the given nonce. It is an error if the nonce matches any
     * stored nonce values on the server
     * if there is no error then the nonce is Cached.
     * @return true if this nonce is valid
     * @param nonce the encoded nonce value
     * @param created the creation time value
     * @param maxNonceAge the time in milliseconds for which this nonce 
     * will be stored on the receiver.
     * @throws XWSSecurityException  if there was an error while trying to validate the Nonce
     */
    public boolean validateAndCacheNonce(String nonce, String created, long maxNonceAge) 
        throws XWSSecurityException;

}
