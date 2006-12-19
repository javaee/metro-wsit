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
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.util.Locale;
import java.util.Collections;

/**
 * WS-Addressing helper methods.
 * 
 * @author Ryan.Shoemaker@Sun.COM
 */
public class WsaHelper {

    /**
     * Create a SOAPFault from the specified information.
     *
     * @param soapVer soap version
     * @param fault fault enum
     * @param message message
     * @return the new SOAPFault
     */
    @NotNull
    public static SOAPFault createFault(@NotNull final SOAPVersion soapVer, @NotNull final TxFault fault, 
                                        @NotNull final String message) {
        try {
            final SOAPFactory soapFactory = soapVer.saajSoapFactory;
            final SOAPFault soapFault = soapFactory.createFault();
 
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
 
    /**
     * Dispatch a fault, adding any necessary headers to 'fault' in the process.
     *
     * @param faultTo
     * @param replyTo
     * @param fault
     */
    public static void sendFault(@Nullable final WSEndpointReference faultTo, @NotNull final EndpointReference replyTo,
                                 @NotNull final SOAPFault fault, final String msgID) {
        final WSEndpointReference to = faultTo != null ? faultTo : new WSEndpointReference(replyTo);

        final WSService s = WSService.create();
        final QName port = new QName("foo", "bar");
        s.addPort(port, SOAPBinding.SOAP11HTTP_BINDING, to.getAddress());

        // one-way feature
        final OneWayFeature owf = new OneWayFeature();
        owf.setRelatesToID(msgID);
        // member submission addressing feature
        final WebServiceFeature af = new MemberSubmissionAddressingFeature(true);

        final Dispatch<Source> d = s.createDispatch(port, to, Source.class, Service.Mode.PAYLOAD, owf, af);
        d.invokeOneWay(new DOMSource(fault));
    }
}










