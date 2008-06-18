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
package com.sun.xml.ws.rm.faults;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.policy.Configuration;
import java.util.Locale;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class AbstractRmSoapFault extends RmException {

    /**
     * SOAP 1.1 Sender Fault
     */
    private static final QName SOAP_1_1_SENDER_FAULT =
            new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Client", SOAPConstants.SOAP_ENV_PREFIX);
    /**
     * SOAP 1.2 Receiver Fault
     */
    private static final QName SOAP_1_1_RECEIVER_FAULT =
            new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server", SOAPConstants.SOAP_ENV_PREFIX);
    //
    private final transient Packet soapFaultResponse;

    public AbstractRmSoapFault(Configuration configuration, Packet request, QName subcode, String reason) {
        super(reason);
        this.soapFaultResponse = createRmProcessingSoapFaultResponse(configuration, request, subcode, reason);
    }

    public AbstractRmSoapFault(Packet soapFaultResponse, String reason) {
        super(reason);
        this.soapFaultResponse = soapFaultResponse;
    }

    public Packet getSoapFaultResponse() {
        return soapFaultResponse;
    }

    /**
     * Creates a SOAP fault response that occured while processing the RM headers of a request
     * 
     * @param requestPacket the request that caused the fault
     * @param subcode WS-RM specific code FQN as defined in the WS-RM specification
     * @param reason English language reason element
     * @return response packet filled with a generated SOAP fault
     * @throws RmRuntimeException in case of any errors while creating the SOAP fault response packet
     */
    protected static Packet createRmProcessingSoapFaultResponse(Configuration configuration, Packet request, QName subcode, String reason) throws RmRuntimeException {
        try {
            SOAPFault soapFault = configuration.getSoapVersion().saajSoapFactory.createFault();

            // common SOAP1.1 and SOAP1.2 Fault settings 
            if (reason != null) {
                soapFault.setFaultString(reason, Locale.ENGLISH);
            }

            // SOAP version-specific SOAP Fault settings
            // FIXME: check if the code we generate is allways a Sender.
            switch (configuration.getSoapVersion()) {
                case SOAP_11:
                    soapFault.setFaultCode(SOAP_1_1_SENDER_FAULT);
                    break;
                case SOAP_12:
                    soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                    soapFault.appendFaultSubcode(subcode);
                    break;
                default:
                    throw new RmRuntimeException("Unsupported SOAP version: '" + configuration.getSoapVersion().toString() + "'");
            }

            Message soapFaultMessage = Messages.create(soapFault);
            if (configuration.getSoapVersion() == SOAPVersion.SOAP_11) {
                Header faultHeader;
                if (configuration.getRmVersion() == RmVersion.WSRM10) {
                    faultHeader = Headers.create(RmVersion.WSRM11.jaxbContext, new com.sun.xml.ws.rm.v200502.SequenceFaultElement(subcode));
                } else {
                    faultHeader = Headers.create(RmVersion.WSRM11.jaxbContext, new com.sun.xml.ws.rm.v200702.SequenceFaultElement(subcode));
                }
                soapFaultMessage.getHeaders().add(faultHeader);
            }

            return request.createServerResponse(
                    soapFaultMessage,
                    configuration.getAddressingVersion(),
                    configuration.getSoapVersion(),
                    getProperFaultActionForAddressingVersion(configuration));
        } catch (SOAPException ex) {
            throw new RmRuntimeException("Error creating a SOAP fault", ex);
        }
    }

    /**
     * Creates a SOAP fault response that occured while processing the CreateSequence request message
     * 
     * @param requestPacket the request that caused the fault
     * @param subcode WS-RM specific code FQN as defined in the WS-RM specification
     * @param reason English language reason element
     * @return response packet filled with a generated SOAP fault
     * @throws RmRuntimeException in case of any errors while creating the SOAP fault response packet
     */
    protected static Packet createCreateSequenceProcessingSoapFaultResponse(Configuration configuration, Packet request, QName subcode, String reason) throws RmRuntimeException {
        try {
            SOAPFault soapFault = configuration.getSoapVersion().saajSoapFactory.createFault();

            // common SOAP1.1 and SOAP1.2 Fault settings 
            if (reason != null) {
                soapFault.setFaultString(reason, Locale.ENGLISH);
            }

            // SOAP version-specific SOAP Fault settings
            // FIXME: check if the code we generate is allways a Sender.
            switch (configuration.getSoapVersion()) {
                case SOAP_11:
                    soapFault.setFaultCode(subcode);
                    break;
                case SOAP_12:
                    soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                    soapFault.appendFaultSubcode(subcode);
                    break;
                default:
                    throw new RmRuntimeException("Unsupported SOAP version: '" + configuration.getSoapVersion().toString() + "'");
            }

            Message soapFaultMessage = Messages.create(soapFault);
            return request.createServerResponse(
                    soapFaultMessage,
                    configuration.getAddressingVersion(),
                    configuration.getSoapVersion(),
                    getProperFaultActionForAddressingVersion(configuration));
        } catch (SOAPException ex) {
            throw new RmRuntimeException("Error creating a SOAP fault", ex);
        }
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    private static String getProperFaultActionForAddressingVersion(Configuration configuration) {
        return (configuration.getAddressingVersion() == AddressingVersion.MEMBER) ? configuration.getAddressingVersion().getDefaultFaultAction() : configuration.getRmVersion().wsrmFaultAction;
    }
}
