/*
 * $Id: EncryptionKeyCallback.java,v 1.3.2.2 2010-07-14 14:06:14 m_potociar Exp $
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

import java.security.cert.X509Certificate;
import java.security.PublicKey;

import javax.crypto.SecretKey;


/**
 * CallBack implementation for encryption key.
 *
 * @author XWS-Security Team
 */
public class EncryptionKeyCallback extends XWSSCallback implements Callback {

    public static interface Request {
    }

    private Request request;

    public EncryptionKeyCallback(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    /**
     * A CallbackHandler handling an instance of this request should make
     * sure that an X.509 certificate must be set on the request.
     */
    public static abstract class X509CertificateRequest implements Request {

        X509Certificate certificate;

        public void setX509Certificate(X509Certificate certificate) {
            this.certificate = certificate;
        }

        public X509Certificate getX509Certificate() {
            return certificate;
        }
    }

    /**
     * A Callback initialized with this request should be handled if there's
     * some default X.509 certificate to be used for encryption.
     */
    public static class DefaultX509CertificateRequest
        extends X509CertificateRequest {
    }

    /**
     * A Callback initialized with this request should be handled if the
     * X.509 certificate to be used for encryption is mapped to some alias.
     */
    public static class AliasX509CertificateRequest
        extends X509CertificateRequest {

        private String alias;

        /**
         * Constructor.
         *
         * @param alias <code>String</code> representing the alias of the X509Certificate.
         *
         */
        public AliasX509CertificateRequest(String alias) {
            this.alias = alias;
        }

        /**
         * Get the alias stored in this Request.
         *
         * @return <code>java.lang.String</code>
         */
        public String getAlias() {
            return alias;
        }
    }

     /**
     * A CallbackHandler handling an instance of this request should make
     * sure that a symmetric key must be set on the request.
     */
    public static abstract class SymmetricKeyRequest implements Request {

        SecretKey symmetricKey;

        /**
         * Constructor.
         *
         * @param symmetricKey <code>javax.crypto.SecretKey</code> representing the
         * SymmetricKey to be used for Encryption.
         */
        public void setSymmetricKey(SecretKey symmetricKey) {
            this.symmetricKey = symmetricKey;
        }

        /**
         * Get the SymmetricKey stored in this Request.
         *
         * @return <code>javax.crypto.SecretKey</code>.
         *
         */
        public SecretKey getSymmetricKey() {
            return symmetricKey;
        }
    }

    /**
     * A CallbackHandler handling an instance of this request should make
     * sure that a symmetric key alias must be set on the request.
     */
    public static class AliasSymmetricKeyRequest
        extends SymmetricKeyRequest {

        private String alias;

        /**
         * Constructor.
         *
         * @param alias <code>java.lang.String</code> representing the alias of the
         * SymmetricKey to be used for Encryption.
         */
        public AliasSymmetricKeyRequest(String alias) {
            this.alias = alias;
        }

        /**
         * Get the alias stored in this Request.
         *
         * @return <code>java.lang.String</code> - alias of the SymmetricKey
         */
        public String getAlias() {
            return alias;
        }
    }

    /*Request for an X.509 certificate given the Public Key
     * This is an optional request and need not be handled
     * by the handler.
     *
     * The runtime makes a callback with this request to obtain
     * the certificate corresponding to the PublicKey. 
     */
    public static class PublicKeyBasedRequest
        extends X509CertificateRequest {
                                                                                                  
        PublicKey pubKey = null;
           
        /**
         * Constructor.
         *
         * @param pk <code>java.security.PublicKey</code> representing the PublicKey
         * to be used for Encryption.
         */
        public PublicKeyBasedRequest(PublicKey pk) {
            pubKey = pk;
        }
                                     
        
        /**
         * Get the PublicKey stored in this Request.
         *
         * @return <code>java.security.PublicKey</code>
         */
        public PublicKey getPublicKey() {
            return pubKey;
        }
                                                                                                  
    }

}
