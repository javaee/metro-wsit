/*
 * Assertion.java
 *
 * Created on August 18, 2005, 12:08 PM
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


package com.sun.xml.wss.saml;

import com.sun.xml.ws.security.Token;
import com.sun.xml.wss.XWSSecurityException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.xml.crypto.dsig.DigestMethod;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.security.cert.X509Certificate;


/**
 *
 * @author abhijit.das@sun.COM
 */

/**
 * This interface stands for <code>Assertion</code> element. An Assertion is a package
 * of information that supplies one or more <code>Statement</code> made by an
 * issuer. There are three kinds of assertions Authentication, Authorization
 * Decision and Attribute assertion.
 * <pre>
 *
 *       &lt;Assertion  AssertionID="1124370015917" IssueInstant="2005-08-18T18:30:15.917+05:30"
 *                      Issuer="CN=Assertion Issuer,OU=AI,O=Assertion Issuer,L=Waltham,ST=MA,C=US"
 *                      MajorVersion="1" MinorVersion="1"
 *                      xmlns="urn:oasis:names:tc:SAML:1.0:assertion">
 *         &lt;Conditions NotBefore="2005-08-16T13:21:50.503+05:30"
 *                        NotOnOrAfter="2005-08-16T15:21:50.504+05:30"/>
 *         &lt;Subject xmlns="urn:oasis:names:tc:SAML:1.0:assertion">
 *             &lt;NameIdentifier Format="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName">
 *                 CN=SAML User,OU=SU,O=SAML User,L=Los Angeles,ST=CA,C=US
 *             &lt;/NameIdentifier>
 *
 *             &lt;SubjectConfirmation>
 *                 &lt;ConfirmationMethod>urn:oasis:names:tc:SAML:1.0:cm:sender-vouches&lt;/ConfirmationMethod>
 *             &lt;/SubjectConfirmation>
 *         &lt;/Subject>
 *         &lt;Attribute AttributeName="attribute1" AttributeNamespace="urn:com:sun:xml:wss:attribute">
 *             &lt;AttributeValue>ATTRIBUTE1&lt;/AttributeValue>
 *         &lt;/Attribute>
 *      &lt;Assertion>
 * </pre>
 */
public interface Assertion extends Token {
    
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param pubKey A <code>java.security.PublicKey</code> representing the public key used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     *
     * By Default DigestMethod.SHA1 and SignatureMethod.RSA_SHA1 will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(PublicKey pubKey, PrivateKey privKey) throws SAMLException;
    
    public Element sign(X509Certificate cert, PrivateKey privKey) throws SAMLException;
    
    
    /**
     * sign the saml assertion (Enveloped Signature)
     * @param digestMethod DigestMethod to be used
     * @param signatureMethod SignatureMethod to be used.
     * @param pubKey PublicKey to be used for Signature verification
     * @param privKey PrivateKey to be used for Signature calculation
     *
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    
    public Element sign(DigestMethod digestMethod, String signatureMethod,PublicKey pubKey, PrivateKey privKey) throws SAMLException ;
    
    public Element sign(DigestMethod digestMethod, String signatureMethod, X509Certificate cert, PrivateKey privKey) throws SAMLException ;
    /**
     * Set the saml major version
     * @param value A <code>java.math.BigInteger</code> representing
     * saml major version
     */
    public void setMajorVersion(java.math.BigInteger value);
    
    /**
     * Set the saml minor version
     * @param value A <code>java.math.BigInteger</code> representing
     * saml minor version
     */
    
    public void setMinorVersion(java.math.BigInteger value);
    
    
    /**
     * Convert SAML Assertion to <code>org.w3c.dom.Element</code>
     * @param doc the context <code>org.w3c.dom.Node</code> for the creation of the
     *      resulting <code>Element</code>.
     * @return org.w3c.dom.Element element representation of SAML Assertion
     */
    public Element toElement(Node doc) throws XWSSecurityException;
    
    
    public String getIssuer();
    
    
    public String getAssertionID();
    
    public BigInteger getMajorVersion();
    
    public BigInteger getMinorVersion();
}
