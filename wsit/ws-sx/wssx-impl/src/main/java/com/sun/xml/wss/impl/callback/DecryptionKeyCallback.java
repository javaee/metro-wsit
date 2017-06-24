/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: DecryptionKeyCallback.java,v 1.2 2010-10-21 15:37:24 snajper Exp $
 */

package com.sun.xml.wss.impl.callback;

import javax.security.auth.callback.Callback;

import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.math.BigInteger;

import javax.crypto.SecretKey;


/**
 * CallBack implementation for decryption key.
 *
 * @author XWS-Security Team
 */
public class DecryptionKeyCallback extends XWSSCallback implements Callback {

    public static interface Request {
    }

    private Request request;

    public DecryptionKeyCallback(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    /**
     * CallBackHandler handling this request should set the private key to be
     * used for decryption on the request.
     */
    public static abstract class PrivateKeyRequest implements Request {

        PrivateKey privateKey;

        /**
         * Set the PrivateKey to be used for Decryption.
         * @param privateKey <code>java.security.PrivateKey</code>
         */
        public void setPrivateKey(PrivateKey privateKey) {
            this.privateKey = privateKey;
        }

        /**
         * Get the PrivateKey.
         * @return <code>java.security.PrivateKey</code> object set on this request.
         */
        public PrivateKey getPrivateKey() {
            return privateKey;
        }
    }

        /**
     * Request for a private key when the X.509 Subject Key Identifier
     * value for a corresponding X.509 Certificate is given.
     */
    public static class X509SubjectKeyIdentifierBasedRequest
        extends PrivateKeyRequest {

        private byte[] x509SubjectKeyIdentifier;

        /** 
         * Constructor.
         * It takes the byte stream of X509SubjectKeyIdentifier.
         */
        public X509SubjectKeyIdentifierBasedRequest(
            byte[] x509SubjectKeyIdentifier) {
            this.x509SubjectKeyIdentifier = x509SubjectKeyIdentifier;
        }

        /**
         * Get the byte stream of X509SubjectKeyIdentifier set on this request.
         * @return byte[] X509SubjectKeyIdentifier value (byte stream).
         */
        public byte[] getSubjectKeyIdentifier() {
            return x509SubjectKeyIdentifier;
        }
    }
    
    /**
     * Request for a private key when the X.509 Thumb print
     * value for a corresponding X.509 Certificate is given.
     * TODO: extends PrivateKeyRequest for now
     */
    public static class ThumbprintBasedRequest
        extends PrivateKeyRequest {

        private byte[] x509Thumbprint;

        /** 
         * Constructor.
         * It takes the byte stream of X509ThumbPrint.
         */
        public ThumbprintBasedRequest(
            byte[] x509Thumbprint) {
            this.x509Thumbprint = x509Thumbprint;
        }

        /**
         * Get the byte stream of X509ThumbPrint set on this request.
         * @return byte[] X509ThumbPrint value (byte stream).
         */
        public byte[] getThumbprintIdentifier() {
            return x509Thumbprint;
        }
    }

    /**
     * Request for a private key when the Issuer Name and Serial Number
     * values for a corresponding X.509 Certificate are given.
     */
    public static class X509IssuerSerialBasedRequest
        extends PrivateKeyRequest {

        private String issuerName;
        private BigInteger serialNumber;

        /**
         * 
         *
         * @param issuerName Name of the issuer.
         * @param serialNumber serial number of the Certificate.
         *
         */
        public X509IssuerSerialBasedRequest(
            String issuerName,
            BigInteger serialNumber) {
            this.issuerName = issuerName;
            this.serialNumber = serialNumber;
        }

        /**
         * Get the issuer name.
         *
         * @return String representation of Certificate Issuer name.
         */
        public String getIssuerName() {
            return issuerName;
        }

        /**
         * Get the Certificate Serial Number.
         *
         * @return <code>java.math.BigInteger</code> representing the Ceritificate's 
         * serial number.
         *
         */
        public BigInteger getSerialNumber() {
            return serialNumber;
        }
    }

    /**
     * Request for a private key when a corresponding X.509 Certificate
     * is given.
     */
    public static class X509CertificateBasedRequest extends PrivateKeyRequest {

        private X509Certificate certificate;

        /**
         * Constructor.
         *
         * @param certificate <code>java.security.X509Certificate</code> 
         * to be used for Decryption.
         */
        public X509CertificateBasedRequest(X509Certificate certificate) {
            this.certificate = certificate;
        }

        /**
         * Get the X509Certificate stored in this Request.
         *
         * @return <code>java.security.X509Certificate</code>
         */
        public X509Certificate getX509Certificate() {
            return certificate;
        }
    }


    /**
     * Request for a symmetric key to be used for decryption.
     */
    public static abstract class SymmetricKeyRequest implements Request {

        SecretKey symmetricKey;

        /**
         * Constructor.
         *
         * @param symmetricKey <code>javax.crypto.SecretKey</code>
         * to be used for Decryption.
         *
         */
        public void setSymmetricKey(SecretKey symmetricKey) {
            this.symmetricKey = symmetricKey;
        }

        /**
         * Get the SymmetricKey stored in this Request.
         *
         * @return <code>javax.crypto.SecretKey</code>
         */
        public SecretKey getSymmetricKey() {
            return symmetricKey;
        }
    }

    /**
     * Given an alias get the <code>javax.crypto.SecretKey</code>
     */
    public static class AliasSymmetricKeyRequest extends SymmetricKeyRequest {

        private String alias;

        /**
         * Constructor.
         *
         * @param alias <code>java.lang.String</code> representing the alias of the 
         * SymmetircKey to be used for Decryption.
         *
         */
        public AliasSymmetricKeyRequest(String alias) {
            this.alias = alias;
        }

        /**
         * Get the alias stored in this Request.
         *
         */
        public String getAlias() {
            return alias;
        }
    }

    /**
     * A Callback initialized with this request should be handled if the
     * private key to be used for decryption is to be retrieved given the PublicKey
     */
    public static class PublicKeyBasedPrivKeyRequest extends PrivateKeyRequest {
                                                                                                                                                         
        private PublicKey pk;
               
        /**
         * Constructor.
         *
         * @param publicKey <code>java.security.PublicKey</code>.
         *
         */
        public PublicKeyBasedPrivKeyRequest(PublicKey publicKey) {
            this.pk = publicKey;
        }
             
        /**
         * Get the PublicKey stored in this Request.
         *
         * @return <code>java.security.PublicKey</code>.
         */
        public PublicKey getPublicKey() {
            return pk;
        }
    }

}
