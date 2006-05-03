/*
 * $Id: SignatureVerificationKeyCallback.java,v 1.1 2006-05-03 22:57:44 arungupta Exp $
 */

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

package com.sun.xml.wss.impl.callback;

import javax.security.auth.callback.Callback;

import java.security.cert.X509Certificate;
import java.security.PublicKey;

import java.math.BigInteger;

/**
 * CallBack implementation for signature verification key.
 *
 * @author XWS-Security Team
 */
public class SignatureVerificationKeyCallback extends XWSSCallback implements Callback {

    public static interface Request {
    }

    private Request request;

    public SignatureVerificationKeyCallback(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    /**
     * A CallbackHandler handling an instance of this request should make
     * sure that an X.509 certificate (to be used for signature verification)
     * must be set on the request.
     */
    public static abstract class X509CertificateRequest implements Request {

        X509Certificate certificate;

        /**
         * Set the X509Certificate used for Signature Verification.
         *
         * @param certificate <code>java.security.X509Certificate</code> representing 
         * X509Certificate to be used for Signature Verification.
         *
         */
        public void setX509Certificate(X509Certificate certificate) {
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
     * Request for a private key when the X.509 Thumb print
     * value for a corresponding X.509 Certificate is given.
     * TODO: extending X509CertificateRequest for now
     */
    public static class ThumbprintBasedRequest
        extends X509CertificateRequest {

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
     * Request for an X.509 certificate whose X.509 Subject Key Identifier
     * value is given.
     */
    public static class X509SubjectKeyIdentifierBasedRequest
        extends X509CertificateRequest {

        private byte[] x509SubjectKeyIdentifier;

        /**
         * Constructor.
         *
         * @param x509SubjectKeyIdentifier - Byte stream representing the X509SubjectKeyIdentifier
         * value.
         */
        public X509SubjectKeyIdentifierBasedRequest(
            byte[] x509SubjectKeyIdentifier) {
            this.x509SubjectKeyIdentifier = x509SubjectKeyIdentifier;
        }

        /**
         * Get the byte stream of X509SubjectKeyIdentifier value stored in this Request.
         *
         * @return - byte[] representation of X509SubjectKeyIdentifier value.
         */
        public byte[] getSubjectKeyIdentifier() {
            return x509SubjectKeyIdentifier;
        }
    }

    /**
     * Request for an X.509 certificate whose Issuer Name and Serial Number
     * values are given.
     */
    public static class X509IssuerSerialBasedRequest
        extends X509CertificateRequest {

        private String issuerName;
        private BigInteger serialNumber;

        /**
         * Constructor.
         * 
         * @param issuerName <code>java.lang.String</code> representing Certificate Issuer Name.
         * @param serialNumber <code>java.math.BigInteger</code> representing the setial
         * number of X509Certificate.
         */
        public X509IssuerSerialBasedRequest(
            String issuerName,
            BigInteger serialNumber) {
            this.issuerName = issuerName;
            this.serialNumber = serialNumber;
        }

        /**
         * Get the Certificate Issuer Name.
         *
         * @return <code>java.lang.String</code> representing the certificate issuer name.
         */
        public String getIssuerName() {
            return issuerName;
        }

        /**
         * Get the serial number of X509Certificate.
         *
         * @return <code>java.math.BigInteger</code> representing the Certificate's serial number.
         */
        public BigInteger getSerialNumber() {
            return serialNumber;
        }
    }

    /**
     * Request for an X.509 certificate given the Public Key
     * This is an optional request and need not be handled
     * by the handler.
     *
     * The runtime makes a callback with this request to obtain 
     * the certificate corresponding to the PublicKey. The returned 
     * certificate is stored in the requestor Subject for later use 
     * by the  Application.
     */
    public static class PublicKeyBasedRequest
        extends X509CertificateRequest {

        PublicKey pubKey = null;

        /**
         * Constructor.
         *
         * @param pk <code>java.security.PublicKey</code> representing the PublicKey
         * to be used for Signature Verification.
         */
        public PublicKeyBasedRequest(PublicKey pk) {
            pubKey = pk;
        }

        /**
         * Get the PublicKey stored in this Request.
         *
         * @return <code>java.security.PublicKey</code> representing the PublicKey used 
         * for Signature Verification.
         */
        public PublicKey getPublicKey() {
            return pubKey;
        }

    }
}
