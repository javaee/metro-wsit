/*
 * $Id: CertificateValidationCallback.java,v 1.1 2006-05-03 22:57:42 arungupta Exp $
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

/**
 * This Callback is intended for X.509 certificate validation
 * A validator that implements the CertificateValidator interface
 * should be set on the callback by the callback handler.
 *
 * @author XWS-Security Team.
 */
public class CertificateValidationCallback extends XWSSCallback implements Callback {

    private boolean result = false;

    private CertificateValidator validator;

    private X509Certificate certificate;

    public CertificateValidationCallback(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public boolean getResult() {
        try {
            if (validator != null)
                result = validator.validate(certificate);
        } catch (Exception e) {
            return false;
        }
        return result;
    }

    /**
     * This method must be invoked while handling this CallBack.
     */
    public void setValidator(CertificateValidator validator) {
        this.validator = validator;
    }


    public static interface CertificateValidator {

        /** 
         * Certificate validator.
         * @param certificate <code>java.security.cert.X509Certificate</code>
         * @return true if the certificate is valid else false
         */
        public boolean validate(X509Certificate certificate)
            throws CertificateValidationException;
    }


    public static class CertificateValidationException extends Exception {

        public CertificateValidationException(String message) {
            super(message);
        }

        public CertificateValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    
        public CertificateValidationException(Throwable cause) {
            super(cause);
        }
    }
}
