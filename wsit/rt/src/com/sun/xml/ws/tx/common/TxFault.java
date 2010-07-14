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
package com.sun.xml.ws.tx.common;

import com.sun.istack.NotNull;
import static com.sun.xml.ws.tx.common.Constants.WSAT_FAULT_ACTION_URI;
import static com.sun.xml.ws.tx.common.Constants.WSCOOR_FAULT_ACTION_URI;

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
 * @version $Revision: 1.6.22.2 $
 * @since 1.0
 */
public enum TxFault {

    InvalidState(
            new QName(Constants.WSCOOR_SOAP_NSURI, "InvalidState", "wscoor"),
            "The message was invalid for the current state of the activity",
            WSCOOR_FAULT_ACTION_URI
    ),

    InvalidProtocol(
            new QName(Constants.WSCOOR_SOAP_NSURI, "InvalidProtocol", "wscoor"),
            "The protocol is invalid or is not supported by the coordinator",
            WSCOOR_FAULT_ACTION_URI
    ),

    InvalidParameters(
            new QName(Constants.WSCOOR_SOAP_NSURI, "InvalidParameters", "wscoor"),
            "The message contained invalid parameters and could not be processed",
            WSCOOR_FAULT_ACTION_URI
    ),

    NoActivity(
            new QName(Constants.WSCOOR_SOAP_NSURI, "NoActivity", "wscoor"),
            "The participant is not responding and is presumed to have ended",
            WSCOOR_FAULT_ACTION_URI
    ),

    ContextRefused(
            new QName(Constants.WSCOOR_SOAP_NSURI, "ContextRefused", "wscoor"),
            "The coordination context that was provided could not be accepted",
            WSCOOR_FAULT_ACTION_URI
    ),

    AlreadyRegistered(
            new QName(Constants.WSCOOR_SOAP_NSURI, "AlreadyRegistered", "wscoor"),
            "The participant has already registered for the same protocol",
            WSCOOR_FAULT_ACTION_URI
    ),

    InconsistentState(
            new QName(Constants.WSAT_OASIS_NSURI, "InconsistentState", "wsat"),
            "A global consistency failure has occurred.  This is an unrecoverable condition",
            WSAT_FAULT_ACTION_URI
    );

    public final QName subcode;
    public final String reason;
    public final String actionURI;

    private TxFault(@NotNull QName subcode, @NotNull String reason, @NotNull String action) {
        this.subcode = subcode;
        this.reason = reason;
        this.actionURI = action;
    }
}
