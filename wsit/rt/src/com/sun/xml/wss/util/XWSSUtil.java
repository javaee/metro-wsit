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
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
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
     * @param cert java.security.cert.X509Certificate
     * @return byte[] representation of X509Certificate's SubjectKeyIdentifier
     */
    public static byte[] getSubjectKeyIdentifier(X509Certificate cert) {
        String SUBJECT_KEY_IDENTIFIER_OID = "2.5.29.14";
        byte[] subjectKeyIdentifier =
            cert.getExtensionValue(SUBJECT_KEY_IDENTIFIER_OID);
        if (subjectKeyIdentifier == null)
            return null;

        try {
            sun.security.x509.KeyIdentifier keyId = null;

            sun.security.util.DerValue derVal = new sun.security.util.DerValue(
                 new sun.security.util.DerInputStream(subjectKeyIdentifier).getOctetString());

            keyId = new sun.security.x509.KeyIdentifier(derVal.getOctetString());
            return keyId.getIdentifier();
        } catch (NoClassDefFoundError ncde) {
            if (subjectKeyIdentifier == null)
                return null;
            byte[] dest = new byte[subjectKeyIdentifier.length - 4];
            System.arraycopy(
                subjectKeyIdentifier, 4, dest, 0, subjectKeyIdentifier.length - 4);
            return dest;
        } catch ( java.io.IOException ex) {
            //ignore
            return null;
        }
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
                byte[] keyId = getSubjectKeyIdentifier(x509Cert);
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
