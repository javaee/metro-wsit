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

package com.sun.xml.ws.security.policy;

import java.util.Set;

/**
 * Represents the AlgorithmSuite assertion.
 * <p>
 * Syntax :
 *
 * <pre>
 *  &lt;xmp&gt;
 *  &lt;sp:AlgorithmSuite ... &gt;
 *      &lt;wsp:Policy&gt;
 *          (
 *          &lt;sp:Basic256 ... /&gt; |
 *          &lt;sp:Basic192 ... /&gt; |
 *          &lt;sp:Basic128 ... /&gt; |
 *          &lt;sp:TripleDes ... /&gt; |
 *          &lt;sp:Basic256Rsa15 ... /&gt; |
 *          &lt;sp:Basic192Rsa15 ... /&gt; |
 *          &lt;sp:Basic128Rsa15 ... /&gt; |
 *          &lt;sp:TripleDesRsa15 ... /&gt; |
 *          &lt;sp:Basic256Sha256 ... /&gt; |
 *          &lt;sp:Basic192Sha256 ... /&gt; |
 *          &lt;sp:Basic128Sha256 ... /&gt; |
 *          &lt;sp:TripleDesSha256 ... /&gt; |
 *          &lt;sp:Basic256Sha256Rsa15 ... /&gt; |
 *          &lt;sp:Basic192Sha256Rsa15 ... /&gt; |
 *          &lt;sp:Basic128Sha256Rsa15 ... /&gt; |
 *          &lt;sp:TripleDesSha256Rsa15 ... /&gt; |
 *
 *           ...)
 *          &lt;sp:InclusiveC14N ... /&gt; ?
 *          &lt;sp:SOAPNormalization10 ... /&gt; ?
 *          &lt;sp:STRTransform10 ... /&gt; ?
 *          &lt;sp:XPath10 ... /&gt; ?
 *          &lt;sp:XPathFilter20 ... /&gt; ?
 *          ...
 *      &lt;/wsp:Policy&gt;
 *    ...
 *   &lt;/sp:AlgorithmSuite&gt;
 * &lt;/xmp&gt;
 *</pre>
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

    public void setSignatureAlgorithm(String sigAlgo);

    public String getSignatureAlgorithm();
}
