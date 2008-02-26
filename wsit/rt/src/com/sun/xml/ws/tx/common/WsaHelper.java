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
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.MemberSubmissionAddressingFeature;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Locale;

/**
 * WS-Addressing helper methods.
 * 
 * @author Ryan.Shoemaker@Sun.COM
 */
public class WsaHelper {
    static final private TxLogger logger = TxLogger.getLogger(WsaHelper.class);

    static HeaderList getInboundHeaderList(@NotNull WebServiceContext wsContext) {
        MessageContext msgContext = wsContext.getMessageContext();
        return (HeaderList) msgContext.get(JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
    }

    public static String getMsgID(@NotNull WebServiceContext wsContext) {
        HeaderList headers = getInboundHeaderList(wsContext);
        return headers.getMessageID(AddressingVersion.MEMBER, SOAPVersion.SOAP_11);
    }

    public static EndpointReference getReplyTo(@NotNull WebServiceContext wsContext) {
        HeaderList headers = getInboundHeaderList(wsContext);
        return (headers.getReplyTo(AddressingVersion.MEMBER, SOAPVersion.SOAP_11)).toSpec();
    }

    public static WSEndpointReference getFaultTo(@NotNull WebServiceContext wsContext) {
        HeaderList headers = getInboundHeaderList(wsContext);
        return headers.getFaultTo(AddressingVersion.MEMBER, SOAPVersion.SOAP_11);
    }


    /**
     * Create a SOAPFault from the specified information.
     *
     * @param soapVer soap version
     * @param fault fault enum
     * @param message message
     * @return the new SOAPFault
     */
    @NotNull
    static SOAPFault createFault(@NotNull final SOAPVersion soapVer, @NotNull final TxFault fault,
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
     * @param soapVer
     * @param fault
     * @param message
     * @param msgID
     */
    public static void sendFault(@Nullable final WSEndpointReference faultTo,
                                 @NotNull final EndpointReference replyTo,
                                 @NotNull final SOAPVersion soapVer,
                                 @NotNull final TxFault fault,
                                 @NotNull final String message,
                                 @NotNull final String msgID) {

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
        d.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        d.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, fault.actionURI);
        
        try {
            d.invokeOneWay(new DOMSource(createFault(soapVer, fault, message)));
        } catch (WebServiceException e) {
            logger.finer("sendFault", e.getLocalizedMessage(), e);
        }
    }

    public static void sendFault(@NotNull WebServiceContext wsContext,
                                 @NotNull final SOAPVersion soapVer,
                                 @NotNull final TxFault fault,
                                 @NotNull final String message) {

        String msgID = WsaHelper.getMsgID(wsContext);
        EndpointReference replyTo = WsaHelper.getReplyTo(wsContext);
        WSEndpointReference faultTo = WsaHelper.getFaultTo(wsContext);

        sendFault(faultTo, replyTo, soapVer, fault, message, msgID);
    }

}










