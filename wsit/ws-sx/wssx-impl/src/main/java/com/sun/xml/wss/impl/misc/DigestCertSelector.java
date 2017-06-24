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
 * DigestCertSelector.java
 *
 * Created on February 26, 2007, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.misc;

import java.security.cert.CertSelector;
import java.security.cert.Certificate;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.security.cert.X509Certificate;
import java.util.Arrays;


import com.sun.xml.wss.logging.LogStringsMessages;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;

/**
 *
 * @author Kumar Jayanti
 */
public class DigestCertSelector implements CertSelector {
    
    private final byte[] keyId;
    private final String algorithm;
     /** logger */
    protected static final Logger log =  Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    
    
    
    /** Creates a new instance of KeyIdentifierCertSelector */
    public DigestCertSelector(byte[] keyIdValue, String algo) {
        this.keyId = keyIdValue;
        this.algorithm = algo;
    }

    public boolean match(Certificate cert) {
        if (cert instanceof X509Certificate) {
            byte[] thumbPrintIdentifier = null;
                                                                                                                      
            try {
                thumbPrintIdentifier = MessageDigest.getInstance(this.algorithm).digest(cert.getEncoded());
            } catch ( NoSuchAlgorithmException ex ) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0708_NO_DIGEST_ALGORITHM(),ex);
                throw new RuntimeException("Digest algorithm SHA-1 not found");
            } catch ( CertificateEncodingException ex) {
                log.log(Level.SEVERE, LogStringsMessages.WSS_0709_ERROR_GETTING_RAW_CONTENT(),ex);
                throw new RuntimeException("Error while getting certificate's raw content");
            }
        
            if (Arrays.equals(thumbPrintIdentifier, keyId)) {
                return true;
            }  
        }
        return false;
    }
    
    public Object clone() {
        return new DigestCertSelector(this.keyId, this.algorithm);
    }
}
