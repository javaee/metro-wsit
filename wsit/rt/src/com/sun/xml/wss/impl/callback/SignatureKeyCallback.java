/*
 * $Id: SignatureKeyCallback.java,v 1.3 2010-03-20 12:35:09 kumarjayanti Exp $
 */

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
