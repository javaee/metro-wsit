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
 * AlgorithmSuite.java
 *
 * Created on September 5, 2006, 10:44 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl;

/**
 *
 * @author sk112103
 */
public class AlgorithmSuite {
    
    private String digestAlgo;
    private String encryptionAlgo;
    private String symKeyAlgo;
    private String asymKeyAlgo;
    
    /** Creates a new instance of AlgorithmSuite 
     * TODO : Created a minimal CTOR for now. Add more info into this as needed.
     * Created to remove dependence of XWSS on WS-SecurityPolicy.
     */
    public AlgorithmSuite(String digAlgo, String encAlgo, String symkAlgo, String asymkAlgo ) {
        this.digestAlgo = digAlgo;
        this.encryptionAlgo = encAlgo;
        this.symKeyAlgo = symkAlgo;
        this.asymKeyAlgo = asymkAlgo;
    }
    
    public String getDigestAlgorithm() {
        return digestAlgo;
    }
    
    
    public String getEncryptionAlgorithm() {
        return encryptionAlgo;
    }
    
    
    public String getSymmetricKeyAlgorithm() {
        return symKeyAlgo;
    }
    
    public String getAsymmetricKeyAlgorithm() {
        return asymKeyAlgo;
    }
    
    public String getSignatureKDAlogrithm() {
        throw new UnsupportedOperationException("getSignatureKDAlogrithm not supported");
    }
    
    public String getEncryptionKDAlogrithm() {
        throw new UnsupportedOperationException("getEncryptionKDAlogrithm not supported");
    }
    
    public int getMinSKLAlgorithm() {
        throw new UnsupportedOperationException("getMinSKLAlgorithm not supported");
    }
    
    public String getSymmetricKeySignatureAlgorithm() {
        throw new UnsupportedOperationException("getSymmetricKeySignatureAlgorithm not supported");
    }
    
    public String getAsymmetricKeySignatureAlgorithm() {
      throw new UnsupportedOperationException(" getAsymmetricKeySignatureAlgorithm not supported");
    }
    
}
