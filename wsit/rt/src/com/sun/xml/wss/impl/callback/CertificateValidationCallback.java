/*
 * $Id: CertificateValidationCallback.java,v 1.4 2010-09-29 13:56:40 sm228678 Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.security.opt.impl.util.SOAPUtil;
import com.sun.xml.wss.impl.MessageConstants;
import javax.security.auth.callback.Callback;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

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
    
    private boolean revocationEnabled = false;

    public CertificateValidationCallback(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateValidationCallback(X509Certificate certificate, Map context) {
        this.certificate = certificate;
        this.runtimeProperties = (Map)context;
    }
    
    public boolean getResult() {
        try {
            if (validator != null)  {
                if (validator instanceof ValidatorExtension) {
                    ((ValidatorExtension)validator).setRuntimeProperties(runtimeProperties);
                }
                result = validator.validate(certificate);
            }
        } catch (CertificateValidationCallback.CertificateValidationException ex) {
            throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        ex.getMessage(), ex);
        } catch (Exception e) {
             throw SOAPUtil.newSOAPFaultException(MessageConstants.WSSE_INVALID_SECURITY_TOKEN,
                        e.getMessage(), e);
        }
        return result;
    }

    /**
     * This method must be invoked while handling this CallBack.
     */
    public void setValidator(CertificateValidator validator) {
        this.validator = validator;
        if (this.validator instanceof ValidatorExtension) {
            ((ValidatorExtension)this.validator).setRuntimeProperties(this.getRuntimeProperties());
        }
    }

    public boolean isRevocationEnabled() {
        return revocationEnabled;
    }

    public void setRevocationEnabled(boolean revocationEnabled) {
        this.revocationEnabled = revocationEnabled;
    }


    public static interface CertificateValidator  {

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
