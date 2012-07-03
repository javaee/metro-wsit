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
 * $Id: WSTrustSOAPFaultException.java,v 1.2 2010-10-21 15:36:48 snajper Exp $
 */

package com.sun.xml.ws.security.trust;

import javax.xml.namespace.QName;

/**
 * Captures the SOAPFault that needs to be thrown by an STS when a processing error occurs
 * @author Kumar Jayanti
 */
public class WSTrustSOAPFaultException extends RuntimeException {
    
    public static final QName WS_TRUST_INVALID_REQUEST_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "InvalidRequest", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_FAILED_AUTHENTICATION_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "FailedAuthentication", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_REQUEST_FAILED_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "RequestFailed", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_INVALID_SECURITYTOKEN_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "InvalidSecurityToken", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_AUTHENTICATION_BAD_ELEMENTS_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "AuthenticationBadElements", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_EXPIRED_DATA_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "ExpiredData", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_INVALID_TIMERANGE_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "InvalidTimeRange", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_INVALID_SCOPE_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "InvalidScope", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_RENEW_NEEDED_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "RenewNeeded", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_UNABLE_TO_RENEW_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "UnableToRenew", WSTrustConstants.WST_PREFIX);
    public static final QName WS_TRUST_BAD_REQUEST_FAULT = new QName(WSTrustConstants.WST_NAMESPACE, "BadRequest", WSTrustConstants.WST_PREFIX);

    
    public static final String WS_TRUST_INVALID_REQUEST_FAULTSTRING = "The request was invalid or malformed";
    public static final String WS_TRUST_FAILED_AUTHENTICATION_FAULTSTRING = "Authentication Failed";
    public static final String WS_TRUST_REQUEST_FAILED_FAULTSTRING = "The specified request failed";
    public static final String WS_TRUST_INVALID_SECURITYTOKEN_FAULTSTRING = "Security Token has been Revoked";
    public static final String WS_TRUST_AUTHENTICATION_BAD_ELEMENTS_FAULTSTRING = "Insufficient Digest Elements";
    public static final String WS_TRUST_BAD_REQUEST_FAULTSTRING = "The specified RequestSecurityToken is not understood";
    public static final String WS_TRUST_EXPIRED_DATA_FAULTSTRING = "The request data is out-of-date";
    public static final String WS_TRUST_INVALID_TIMERANGE_FAULTSTRING = "The requested time range is invalid or unsupported";
    public static final String WS_TRUST_INVALID_SCOPE_FAULTSTRING = "The request scope is invalid or unsupported";
    public static final String WS_TRUST_RENEW_NEEDED_FAULTSTRING = "A renewable security token has expired";
    public static final String WS_TRUST_UNABLE_TO_RENEW_FAULTSTRING = "The requested renewal failed";
    
    

    private final QName faultCode;
    private final String faultString;
    
    /** 
     * Creates a new instance of WSTrustSOAPFaultException 
     */
    public WSTrustSOAPFaultException(String message, Throwable cause, QName faultCode, String faultString) {
        super(message,cause);
        this.faultCode = faultCode;
        this.faultString = faultString;
    }
    
    /**
     * Get the FaultString for this exception
     */
    public String getFaultString() {
        return faultString;
    }
    
    /**
     * Get the FaultCode (QName) for this exception
     */
    public QName getFaultCode() {
        return faultCode;
    }
    
}
