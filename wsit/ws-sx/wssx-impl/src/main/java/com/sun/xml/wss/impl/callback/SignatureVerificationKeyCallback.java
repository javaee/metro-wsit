/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
 * $Id: SignatureVerificationKeyCallback.java,v 1.2 2010-10-21 15:37:24 snajper Exp $
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
