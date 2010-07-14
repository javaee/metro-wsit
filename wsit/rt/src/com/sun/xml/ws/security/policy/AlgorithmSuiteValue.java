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

/*
 * AlgorithmSuiteValue.java
 *
 * Created on February 14, 2006, 2:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.policy;

/**
 * AlgorithmSuiteValue identifies the algorithm to be used to protect the message.
 * @author Abhijit Das
 */
public enum AlgorithmSuiteValue {
    Basic256(
            Constants.SHA1,
            Constants.AES256,
            Constants.KW_AES256,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L256,
            Constants.PSHA1_L192,
            256),
    Basic192(
            Constants.SHA1,
            Constants.AES192,
            Constants.KW_AES192,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic128(
            Constants.SHA1,
            Constants.AES128,
            Constants.KW_AES128,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L128,
            Constants.PSHA1_L128,
            128),
    TripleDes(
            Constants.SHA1,
            Constants.TRIPLE_DES,
            Constants.KW_TRIPLE_DES,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic256Rsa15(
            Constants.SHA1,
            Constants.AES256,
            Constants.KW_AES256,
            Constants.KW_RSA15,
            Constants.PSHA1_L256,
            Constants.PSHA1_L192,
            256),
    Basic192Rsa15(
            Constants.SHA1,
            Constants.AES192,
            Constants.KW_AES192,
            Constants.KW_RSA15,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic128Rsa15(
            Constants.SHA1,
            Constants.AES128,
            Constants.KW_AES128,
            Constants.KW_RSA15,
            Constants.PSHA1_L128,
            Constants.PSHA1_L128,
            128),
    TripleDesRsa15(
            Constants.SHA1,
            Constants.TRIPLE_DES,
            Constants.KW_TRIPLE_DES,
            Constants.KW_RSA15,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic256Sha256(
            Constants.SHA256,
            Constants.AES256,
            Constants.KW_AES256,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L256,
            Constants.PSHA1_L192,
            256),
    Basic192Sha256(
            Constants.SHA256,
            Constants.AES192,
            Constants.KW_AES192,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic128Sha256(
            Constants.SHA256,
            Constants.AES128,
            Constants.KW_AES128,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L128,
            Constants.PSHA1_L128,
            128),
    TripleDesSha256(
            Constants.SHA256,
            Constants.TRIPLE_DES,
            Constants.KW_TRIPLE_DES,
            Constants.KW_RSA_OAEP,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic256Sha256Rsa15(
            Constants.SHA256,
            Constants.AES256,
            Constants.KW_AES256,
            Constants.KW_RSA15,
            Constants.PSHA1_L256,
            Constants.PSHA1_L192,
            256),
    Basic192Sha256Rsa15(
            Constants.SHA256,
            Constants.AES192,
            Constants.KW_AES192,
            Constants.KW_RSA15,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192),
    Basic128Sha256Rsa15(
            Constants.SHA256,
            Constants.AES128,
            Constants.KW_AES128,
            Constants.KW_RSA15,
            Constants.PSHA1_L128,
            Constants.PSHA1_L128,
            128),
    TripleDesSha256Rsa15(
            Constants.SHA256,
            Constants.TRIPLE_DES,
            Constants.KW_TRIPLE_DES,
            Constants.KW_RSA15,
            Constants.PSHA1_L192,
            Constants.PSHA1_L192,
            192);
            
    
    private final String dsigAlgorithm;
    private final String encAlgorithm;
    private final String symKWAlgorithm;
    private final String asymKWAlgorithm;
    private final String encKDAlgorithm;
    private final String sigKDAlgorithm;
    private final int minSKLAlgorithm;
    
    
    AlgorithmSuiteValue(
            String dsigAlgorithm,
            String encAlgorithm,
            String symKWAlgorithm,
            String asymKWAlgorithm,
            String encKDAlgorithm,
            String sigKDAlgorithm,
            int minSKLAlgorithm) {
        
        this.dsigAlgorithm = dsigAlgorithm;
        this.encAlgorithm = encAlgorithm;
        this.symKWAlgorithm = symKWAlgorithm;
        this.asymKWAlgorithm = asymKWAlgorithm;
        this.encKDAlgorithm = encKDAlgorithm;
        this.sigKDAlgorithm = sigKDAlgorithm;
        this.minSKLAlgorithm = minSKLAlgorithm;
    }

    public String getDigAlgorithm() {
        return dsigAlgorithm;
    }

    public String getEncAlgorithm() {
        return encAlgorithm;
    }

    public String getSymKWAlgorithm() {
        return symKWAlgorithm;
    }

    public String getAsymKWAlgorithm() {
        return asymKWAlgorithm;
    }

    public String getEncKDAlgorithm() {
        return encKDAlgorithm;
    }

    public String getSigKDAlgorithm() {
        return sigKDAlgorithm;
    }

    public int getMinSKLAlgorithm() {
        return minSKLAlgorithm;
    }
}
