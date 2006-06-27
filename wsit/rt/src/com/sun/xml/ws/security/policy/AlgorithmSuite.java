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

package com.sun.xml.ws.security.policy;

import com.sun.xml.ws.policy.PolicyAssertion;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents the AlgorithmSuite assertion.
 * <p>
 * Syntax :
 *
 * <pre><xmp>
 *  <sp:AlgorithmSuite ... >
 *      <wsp:Policy>
 *          (
 *          <sp:Basic256 ... /> |
 *          <sp:Basic192 ... /> |
 *          <sp:Basic128 ... /> |
 *          <sp:TripleDes ... /> |
 *          <sp:Basic256Rsa15 ... /> |
 *          <sp:Basic192Rsa15 ... /> |
 *          <sp:Basic128Rsa15 ... /> |
 *          <sp:TripleDesRsa15 ... /> |
 *          <sp:Basic256Sha256 ... /> |
 *          <sp:Basic192Sha256 ... /> |
 *          <sp:Basic128Sha256 ... /> |
 *          <sp:TripleDesSha256 ... /> |
 *          <sp:Basic256Sha256Rsa15 ... /> |
 *          <sp:Basic192Sha256Rsa15 ... /> |
 *          <sp:Basic128Sha256Rsa15 ... /> |
 *          <sp:TripleDesSha256Rsa15 ... /> |
 *
 *           ...)
 *          <sp:InclusiveC14N ... /> ?
 *          <sp:SOAPNormalization10 ... /> ?
 *          <sp:STRTransform10 ... /> ?
 *          <sp:XPath10 ... /> ?
 *          <sp:XPathFilter20 ... /> ?
 *          ...
 *      </wsp:Policy>
 *    ...
 *   </sp:AlgorithmSuite>
 *</xmp></pre>
 *
 * @author K.Venugopal@sun.com
 */
public interface AlgorithmSuite {
    
    public static final String INCLUSIVE14N="InclusiveC14N";
    public static final String SOAP_NORMALIZATION10="SOAPNormalization10";
    public static final String STR_TRANSFORM10="STRTransform10";
    public static final String XPATH10="XPath10";
    public static final String XPATH_FILTER20="XPathFilter20";
    public static int MAX_SKL = 256;
    public static int MAX_AKL = 4096;
    public static int MIN_AKL = 1024;
    /**
     * returns the Algorithm suite to be used.
     * @return {@link AlgorithmSuiteValue}
     */
    public AlgorithmSuiteValue getType();
    
    /**
     * Property set containing INCLUSIVE14N,SOAP_NORMALIZATION10,STR_TRANSFORM10,XPATH10,XPATH_FILTER20
     * @return list identifying the properties
     */
    public Set getAdditionalProps();
    
    
    /**
     * Gets the Digest algorithm identified by this AlgorithmSuite.
     * @return {@java.lang.String}
     */
    public String getDigestAlgorithm();
    
    /**
     * Gets the Encryption algorithm
     * @return
     */
    public String getEncryptionAlgorithm();
    
    /**
     * Gets the Symmetric key signature algorithm
     * @return
     */
    public String getSymmetricKeySignatureAlgorithm();
    
    /**
     * Gets the Asymmetric key signature algorithm
     * @return
     */
    public String getAsymmetricKeySignatureAlgorithm();
    
    /**
     * Gets the Symmetric Key algorithm
     * @return
     */
    public String getSymmetricKeyAlgorithm();
    
    /**
     * Get the Assymetric key algorithm
     * @return
     */
    public String getAsymmetricKeyAlgorithm();
    
    /**
     * Gets the Signature key derivation algorithm
     * @return
     */
    public String getSignatureKDAlogrithm();
    
    /**
     * Gets the Encryprion key derivation algorithm
     * @return
     */
    public String getEncryptionKDAlogrithm();
    
    
    /**
     * Gets minimum key length  for symmetric key algorithm.
     * @return
     */
    public int getMinSKLAlgorithm();
    
    /*
     * Gets the computed key algorithm
     */
    public String getComputedKeyAlgorithm();
    /*
     *Gets the Maximum symmetric key length
     */
    public int getMaxSymmetricKeyLength();
    /*
     *Gets the minimum Asymmetric key length
     */
    public int getMinAsymmetricKeyLength();
    /*
     *Gets the maximum Asymmetric key length
     */
    public int getMaxAsymmetricKeyLength();
}
