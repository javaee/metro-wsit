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
package com.sun.xml.ws.tx.common;

import com.sun.istack.NotNull;

import javax.xml.namespace.QName;

/**
 * Enumeration of ws:coor fault types.
 * <p/>
 * Note: fault "code" is only used in SOAP 1.2 faults and should be filled in
 * dynamically when the SOAPFault is being constructed.  All of the faults
 * specified by ws:coor and ws:at should use SOAPConstants.SOAP_SENDER_FAULT.
 * <p/>
 * Note: fault "detail" is only required in SOAP 1.2 faults, but is never
 * specified by ws:coor or ws:at, so I'm leaving this field out as well
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
public enum TxFault {

    InvalidState(
            new QName(Constants.WSCOOR_SOAP_NSURI, "InvalidState", "wscoor"),
            "The message was invalid for the current state of the activity"
    ),

    InvalidProtocol(
            new QName(Constants.WSCOOR_SOAP_NSURI, "InvalidProtocol", "wscoor"),
            "The protocol is invalid or is not supported by the coordinator"
    ),

    InvalidParameters(
            new QName(Constants.WSCOOR_SOAP_NSURI, "InvalidParameters", "wscoor"),
            "The message contained invalid parameters and could not be processed"
    ),

    NoActivity(
            new QName(Constants.WSCOOR_SOAP_NSURI, "NoActivity", "wscoor"),
            "The participant is not responding and is presumed to have ended"
    ),

    ContextRefused(
            new QName(Constants.WSCOOR_SOAP_NSURI, "ContextRefused", "wscoor"),
            "The coordination context that was provided could not be accepted"
    ),

    AlreadyRegistered(
            new QName(Constants.WSCOOR_SOAP_NSURI, "AlreadyRegistered", "wscoor"),
            "The participant has already registered for the same protocol"
    ),

    InconsistentState(
            new QName(Constants.WSAT_OASIS_NSURI, "InconsistentState", "wsat"),
            "A global consistency failure has occurred.  This is an unrecoverable condition"
    );

    public final QName subcode;
    public final String reason;

    private TxFault(@NotNull QName subcode, @NotNull String reason) {
        this.subcode = subcode;
        this.reason = reason;
    }
}
