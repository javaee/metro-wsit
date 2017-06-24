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
 * IssuerNameAndSerialCertSelector.java
 *
 * Created on February 28, 2007, 11:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.wss.impl.misc;

import java.math.BigInteger;
import java.security.cert.CertSelector;
import java.security.cert.Certificate;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import com.sun.xml.wss.XWSSecurityException;

import java.security.cert.CertificateEncodingException;
import javax.security.auth.x500.X500Principal;

/**
 *
 * @author kumar jayanti
 */
public class IssuerNameAndSerialCertSelector implements CertSelector {
    
    private final BigInteger serialNumber;
    private final String issuerName;
    
       /** logger */
    protected static final Logger log =  Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
  
    /** Creates a new instance of IssuerNameAndSerialCertSelector */
    public IssuerNameAndSerialCertSelector(BigInteger serialNum, String issuer) {
        this.serialNumber = serialNum;
        this.issuerName = issuer;
    }

    public boolean match(Certificate cert) {
        if (cert instanceof X509Certificate) {
           if (this.matchesIssuerSerialAndName(this.serialNumber, this.issuerName, (X509Certificate)cert)) {
               return true;
           }     
        }
        return false;
    }
    
    public Object clone() {
        return new IssuerNameAndSerialCertSelector(this.serialNumber, this.issuerName);
    }
    
    private boolean matchesIssuerSerialAndName(
        BigInteger serialNumberMatch,
        String issuerNameMatch,
        X509Certificate x509Cert) {
  
        
        X500Principal thisIssuerPrincipal = x509Cert.getIssuerX500Principal();
        X500Principal issuerPrincipal = new X500Principal(issuerName);

        BigInteger thisSerialNumber = x509Cert.getSerialNumber();


        if (serialNumber.equals(serialNumberMatch)
                && issuerPrincipal.equals(thisIssuerPrincipal)) {
            return true;
        }
        return false;
    }
}
