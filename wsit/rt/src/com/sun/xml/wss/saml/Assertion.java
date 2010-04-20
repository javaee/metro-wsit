/*
 * Assertion.java
 *
 * Created on August 18, 2005, 12:08 PM
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


package com.sun.xml.wss.saml;

import com.sun.xml.ws.api.security.Token;
import com.sun.xml.wss.XWSSecurityException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.xml.crypto.dsig.DigestMethod;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author shyam.rao@sun.com
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
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(PublicKey pubKey, PrivateKey privKey) throws SAMLException;
    
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param cert A <code>java.security.cert.X509Certificate</code> representing the certificate used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     *
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(X509Certificate cert, PrivateKey privKey) throws SAMLException;
    
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param cert A <code>java.security.cert.X509Certificate</code> representing the certificate used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     * @param alwaysIncludeCert A flag to tell whether to incude the certificate in the SAML signature.
     *
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(X509Certificate cert, PrivateKey privKey, boolean alwaysIncludeCert) throws SAMLException;
    
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param cert A <code>java.security.cert.X509Certificate</code> representing the certificate used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     * @param alwaysIncludeCert A flag to tell whether to incude the certificate in the SAML signature.
     * @param signatureMethod A <code>javax.xml.crypto.dsig.SignatureMethod</code> representing the signature algorithm used SAML signature.
     * @param canonicalizationMethod A <code>javax.xml.crypto.dsig.CanonicalizationMethod</code> representing the canonicalization algorithm used SAML signature.
     * 
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(X509Certificate cert, PrivateKey privKey, boolean alwaysIncludeCert, String signatureMethod, String canonicalizationMethod) throws SAMLException;
           
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param digestMethod A <code>javax.xml.crypto.dsig.DigestMethod</code> representing the digest method used for SAML signature.
     * @param signatureMethod A <code>javax.xml.crypto.dsig.SignatureMethod</code> representing the signature algorithm used SAML signature.
     * @param pubKey A <code>java.security.PublicKey</code> representing the public key used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     *
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(DigestMethod digestMethod, String signatureMethod,PublicKey pubKey, PrivateKey privKey) throws SAMLException ;
    
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param digestMethod A <code>javax.xml.crypto.dsig.DigestMethod</code> representing the digest method used for SAML signature.
     * @param signatureMethod A <code>javax.xml.crypto.dsig.SignatureMethod</code> representing the signature algorithm used SAML signature.
     * @param cert A <code>java.security.cert.X509Certificate</code> representing the certificate used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     *
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(DigestMethod digestMethod, String signatureMethod, X509Certificate cert, PrivateKey privKey) throws SAMLException ;
    
    /**
     * Sign the SAML Assertion - Enveloped Signature
     *
     * @param digestMethod A <code>javax.xml.crypto.dsig.DigestMethod</code> representing the digest method used for SAML signature.
     * @param signatureMethod A <code>javax.xml.crypto.dsig.SignatureMethod</code> representing the signature algorithm used SAML signature.
     * @param cert A <code>java.security.cert.X509Certificate</code> representing the certificate used for Signature verification
     * @param privKey A <code>java.security.PrivateKey</code> representing the private key used for Signature calculation.
     * @param alwaysIncludeCert A flag to tell whether to incude the certificate in the SAML signature.
     * 
     * By Default DigestMethod.SHA1, SignatureMethod.RSA_SHA1 and CanonicalizationMethod.EXCLUSIVE will be used.
     * @return An <code>org.w3c.dom.Element</code> representation of Signed SAML Assertion
     */
    public Element sign(DigestMethod digestMethod, String signatureMethod, X509Certificate cert, PrivateKey privKey, boolean alwaysIncludeCert) throws SAMLException ;        
    
    /**
     * @deprecated
     * Set the saml major version for SAML1.0 and SAML1.1
     * @param value A <code>java.math.BigInteger</code> representing saml major version.
     * 
     */    
    public void setMajorVersion(java.math.BigInteger value);
    
    /**
     * @deprecated
     * Set the saml minor version for SAML1.0 and SAML1.1
     * @param value A <code>java.math.BigInteger</code> representing saml minor version.
     * 
     */    
    public void setMinorVersion(java.math.BigInteger value);
    
    /**
     * @deprecated
     * Set the saml version for SAML2.0
     * @param version A <code>java.lang.String</code> representing saml version
     * 
     */
    public void setVersion(String version);
        
    /**
     * Convert SAML Assertion to <code>org.w3c.dom.Element</code>
     * @param doc the context <code>org.w3c.dom.Node</code> for the creation of the resulting <code>org.w3c.dom.Element</code>.
     * 
     * @return org.w3c.dom.Element element representation of SAML Assertion
     */
    public Element toElement(Node doc) throws XWSSecurityException;
        
    /**
     * Get the issuer of SAML Assertion 
     * 
     * @return An <code>java.lang.String</code> representing saml issuer.
     */
    public String getSamlIssuer();            
       
    /**
     * Get the SAML Assertion ID for SAML1.0 and SAML1.1
     * 
     * @return An <code>java.lang.String</code> representing saml assertion ID.
     */
    public String getAssertionID();
    
    /**
     * Get the SAML Assertion ID for SAML2.0
     * 
     * @return An <code>java.lang.String</code> representing saml assertion ID.
     */
    public String getID();
    
    /**
     * Get the SAML Vertion SAML2.0
     * 
     * @return An <code>java.lang.String</code> representing saml version.
     */
    public String getVersion();
    
    /**
     * Get the SAML Major Vertion for SAML1.0 and SAML1.1
     * 
     * @return An <code>java.math.BigInteger</code> representing saml major version.
     */
    public BigInteger getMajorVersion();   
    
    /**
     * Get the SAML Minor Vertion for SAML1.0 and SAML1.1
     * 
     * @return An <code>java.math.BigInteger</code> representing saml minor version.
     */
    public BigInteger getMinorVersion();
    
    /**
     * Gets the value of the issueInstant property.
     * 
     * @return A {@link java.lang.String } representing the issue timestamp of the SAML Assertion
     *     
     */
    public String getIssueInstance();
    
    /**
     * Return all statements presents in the SAML Assertion.
     * 
     * @return An <code>java.util.List</code> of </code>java.lang.Object</Object> 
     * representing all statements present inside the SAML assertion.
     * 
     */
    public List<Object> getStatements();
    
    /**
     * Gets the value of the conditions property of SAML
     * 
     * @return A {@link Conditions} representing conditions of the SAML Assertion.
     *     
     */
    public Conditions getConditions();
    
    /**
     * Gets the value of the advice property of SAML
     * 
     * @return An {@link Advice} representing Advice element present in the SAML Assertion.
     *     
     */
    public Advice getAdvice();
    
    /**
     * Gets the value of the subject property of SAML 2.0
     * This method should be applied only on SAML 2.0 assertion. 
     * For SAML1.1 and SAML1.0, first get a list of statements of the SAML assertion 
     * by calling getStatements() on the <code>Assertion</code> object, then call 
     * the getSubject() on each statement.
     * 
     * @return A {@link Subject} representing Subject of SAML 2.0
     *     
     */
    public Subject getSubject();        
    
    /**
     * Verify the SAML signature with the Public Key
     * 
     * @param pubKey A <code>java.security.PublicKey</code> representing the public key used for Signature verification
     * 
     * @return An {@link Boolean} representing whether SAML signature verification is successful or not.
     *     
     */
    public boolean verifySignature(PublicKey pubKey) throws SAMLException;
    
}