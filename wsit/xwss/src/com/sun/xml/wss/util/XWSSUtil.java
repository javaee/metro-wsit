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
 * XWSSUtil.java
 *
 * Created on December 14, 2005, 11:18 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.wss.util;

import com.sun.org.apache.xml.internal.security.utils.RFC2253Parser;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.core.reference.X509SubjectKeyIdentifier;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.util.Arrays;
import java.util.Enumeration;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import java.io.IOException;

import java.math.BigInteger;
import javax.crypto.SecretKey;

/**
 *
 * @author Abhijit Das
 */
public abstract class XWSSUtil {
    
    /**
     * 
     * @param ski byte[] representing SubjectKeyIdentifier
     * @param trustStore java.security.KeyStore 
     * @return X509Certificate from trustStore if present otherwise null.
     * @throws java.io.IOException 
     */
    
    public static X509Certificate getCertificateFromTrustStore(byte[] ski, KeyStore trustStore)
        throws IOException {

        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate)cert;
                byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }
    
    
    /**
     * 
     * @param issuerName Certificate Issuer Name
     * @param serialNumber Serial number of the certificate
     * @param trustStore java.security.Keystore
     * @throws java.io.IOException 
     * @return java.security.X509Certificate 
     */
     public static X509Certificate getCertificateFromTrustStore(
        String issuerName,
        BigInteger serialNumber, KeyStore trustStore )
        throws IOException {

        try {
            Enumeration aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                Certificate cert = trustStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                    RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) &&
                    thisSerialNumber.equals(serialNumber)) {
                    return x509Cert;
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
     }
     
    /**
     * 
     * @param ski 
     * @param keyStore 
     * @param keyStorePassword 
     * @throws java.io.IOException 
     * @return 
     */
     public static PrivateKey getPrivateKey(byte[] ski, KeyStore keyStore, String keyStorePassword) throws IOException {

        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                byte[] keyId = X509SubjectKeyIdentifier.getSubjectKeyIdentifier(x509Cert);
                if (keyId == null) {
                    // Cert does not contain a key identifier
                    continue;
                }
                if (Arrays.equals(ski, keyId)) {
                    // Asuumed key password same as the keystore password
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
     }
     
     
    /**
     * 
     * @param issuerName 
     * @param serialNumber 
     * @param keyStore 
     * @param keyStorePassword 
     * @throws java.io.IOException 
     * @return 
     */
     public static PrivateKey getPrivateKey(
        String issuerName,
        BigInteger serialNumber, KeyStore keyStore, String keyStorePassword)
        throws IOException {

        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert == null || !"X.509".equals(cert.getType())) {
                    continue;
                }
                X509Certificate x509Cert = (X509Certificate) cert;
                String thisIssuerName =
                    RFC2253Parser.normalize(x509Cert.getIssuerDN().getName());
                BigInteger thisSerialNumber = x509Cert.getSerialNumber();
                if (thisIssuerName.equals(issuerName) &&
                    thisSerialNumber.equals(serialNumber)) {
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
                }
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
     }
     
     
    /**
     * 
     * @param certificate 
     * @param keyStore 
     * @param keyStorePassword 
     * @throws java.io.IOException 
     * @return 
     */
     public static PrivateKey getPrivateKey(X509Certificate certificate, KeyStore keyStore, String keyStorePassword)
        throws IOException {

        try {
            Enumeration aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if (!keyStore.isKeyEntry(alias))
                    continue;
                Certificate cert = keyStore.getCertificate(alias);
                if (cert != null && cert.equals(certificate))
                    return (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return null;
     }
     
    /**
     * 
     * @param algorithm 
     * @throws com.sun.xml.wss.XWSSecurityException 
     * @return 
     */
     public static SecretKey generateSymmetricKey(String algorithm) throws XWSSecurityException {
         return SecurityUtil.generateSymmetricKey(algorithm);
     }
     
}
