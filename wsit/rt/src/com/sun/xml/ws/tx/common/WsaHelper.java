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

import com.sun.xml.ws.api.SOAPVersion;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import java.util.Locale;

/**
 * @author jf39279
 */
public class WsaHelper {

    /**
     * @deprecated
     */
    public static String dump(EndpointReference epr) {
        throw new UnsupportedOperationException("just use EndpointReference.toString() or WSEndpointReference.toString() instead");
    }

    public static SOAPFault createFault(SOAPVersion soapVer, TxFault fault, String message) {
        try {
            SOAPFactory soapFactory = soapVer.saajSoapFactory;
            SOAPFault soapFault = soapFactory.createFault();

            if (soapVer == SOAPVersion.SOAP_11) {
                soapFault.setFaultCode(fault.subcode);
                soapFault.setFaultString(fault.reason + ": " + message, Locale.ENGLISH);
            } else { // SOAP 1.2
                soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                soapFault.appendFaultSubcode(fault.subcode);
                soapFault.setFaultString(fault.reason + ": " + message, Locale.ENGLISH);
            }
            return soapFault;
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }
}










