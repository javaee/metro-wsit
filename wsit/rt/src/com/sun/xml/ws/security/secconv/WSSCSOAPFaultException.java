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

package com.sun.xml.ws.security.secconv;

import javax.xml.namespace.QName;

import com.sun.xml.ws.security.trust.WSTrustSOAPFaultException;

/**
 * Captures the SOAPFault that needs to be thrown by an Security Context Token Service when a 
 * processing error occurs
 */
public class WSSCSOAPFaultException extends WSTrustSOAPFaultException {
    
    public static final QName WS_SC_BAD_CONTEXT_TOKEN_FAULT = new QName(WSSCConstants.WSC_NAMESPACE, "BadContextToken", WSSCConstants.WSC_PREFIX);
    public static final QName WS_SC_UNSUPPORTED_CONTEXT_TOKEN_FAULT = new QName(WSSCConstants.WSC_NAMESPACE, "UnsupportedContextToken", WSSCConstants.WSC_PREFIX);
    public static final QName WS_SC_UNKNOWN_DERIVATION_SOURCE_FAULT = new QName(WSSCConstants.WSC_NAMESPACE, "UnknownDerivationSource", WSSCConstants.WSC_PREFIX);
    public static final QName WS_SC_RENED_NEEDED_FAULT = new QName(WSSCConstants.WSC_NAMESPACE, "RenewNeeded", WSSCConstants.WSC_PREFIX);
    public static final QName WS_SC_UNABLE_TO_RENEW_FAULT = new QName(WSSCConstants.WSC_NAMESPACE, "UnableToRenew", WSSCConstants.WSC_PREFIX);
  
    public static final String WS_SC_BAD_CONTEXT_TOKEN_FAULTSTRING = "The requested context elements are insufficient or unsupported";
    public static final String WS_SC_UNSUPPORTED_CONTEXT_TOKEN_FAULTSTRING = "Not all of the values associated with the SCT are supported";
    public static final String WS_SC_UNKNOWN_DERIVATION_SOURCE_FAULTSTRING = "The specified source for the derivation is unknown";
    public static final String WS_SC_RENED_NEEDED_FAULTSTRING = "The provided context token has expired";
    public static final String WS_SC_UNABLE_TO_RENEW_FAULTSTRING = "The specified context token could not be renewed";
    
    /** 
     * Creates a new instance of WSSCSOAPFaultException 
     */
    public WSSCSOAPFaultException(String message, Throwable cause, QName faultCode, String faultString) {
        super(message,cause, faultCode, faultString);
    }
}
