/*
 * $Id: SignatureKeyCallback.java,v 1.1 2006-05-03 22:57:44 arungupta Exp $
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

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;


/**
 * CallBack implementation for signature key.
 *
 * @author XWS-Security Team
 */
public class SignatureKeyCallback extends XWSSCallback implements Callback {

    public static interface Request {
    }

    private Request request;

    public SignatureKeyCallback(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    /**
     * A CallbackHandler handling an instance of this request should make
     * sure that a private key and a corresponding X.509 certificate must
     * be set on the request.
     */
    public static abstract class PrivKeyCertRequest implements Request {

        PrivateKey privateKey;

        X509Certificate certificate;

        /**
         * Set the Private Key used for Signature Calculation.
         *
         * @param privateKey <code>java.security.PrivateKey</code> representing the
         * PrivateKey to be used for Signature value calculation.
         *
         */
        public void setPrivateKey(PrivateKey privateKey) {
            this.privateKey = privateKey;
        }

        /**
         * Get the PrivateKey stored in this Request.
         *
         * @return <code>java.security.PrivateKey<code> - PrivateKey to be used for 
         * Signature value calculation.
         */
        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * Set the X509Certificate used for Signature verification.
         *
         * @param certificate <code>java.security.X509Certificate</code> to be 
         * used for Signature Verification.
         *
         */
        public void setX509Certificate(X509Certificate certificate) {
            this.certificate = certificate;
        }

        /**
         * Get the X509Certificate stored in this Request.
         *
         * @return <code>java.security.X509Certificate</code> - X509Certificate
         * to be used for Signature Verification.
         */
        public X509Certificate getX509Certificate() {
            return certificate;
        }
    }

    /**
     * A Callback initialized with this request should be handled if there's
     * some default private key to be used for signing.
     */
    public static class DefaultPrivKeyCertRequest
        extends PrivKeyCertRequest {
    }

    /**
     * A Callback initialized with this request should be handled if the
     * private key to be used for signing is mapped to some alias.
     */
    public static class AliasPrivKeyCertRequest extends PrivKeyCertRequest {

        private String alias;

        /**
         * Constructor.
         *
         * @param alias <code>java.lang.String</code> representing the alias of
         * the PrivateKey to be used for Signature calculation.
         */
        public AliasPrivKeyCertRequest(String alias) {
            this.alias = alias;
        }

        /**
         * Get the alias stored in this Request.
         *
         * @return <code>java.lang.String</code> representing the alias of the PrivateKey
         * to be used for Signature calculation.
         */
        public String getAlias() {
            return alias;
        }
    }

    /**
     * A Callback initialized with this request should be handled if the
     * private key to be used for signing is to be retrieved given the PublicKey
     */
    public static class PublicKeyBasedPrivKeyCertRequest extends PrivKeyCertRequest {

        private PublicKey pk;

        /**
         * Constructor.
         *
         * @param publicKey <code>java.security.PublicKey</code>.
         */
        public PublicKeyBasedPrivKeyCertRequest(PublicKey publicKey) {
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
