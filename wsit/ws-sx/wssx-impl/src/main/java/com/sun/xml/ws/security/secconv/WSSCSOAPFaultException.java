/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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
