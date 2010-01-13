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
package com.sun.xml.ws.rx.rm.faults;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.runtime.RuntimeContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public abstract class AbstractSoapFaultException extends RxRuntimeException {

    public static enum Code {
        Sender {
             QName asQName(SOAPVersion sv) {
                return sv.faultCodeClient;
             }
        },
        Receiver {
             QName asQName(SOAPVersion sv) {
                return sv.faultCodeServer;
             }            
        };

        abstract QName asQName(SOAPVersion sv);
    }

    private final boolean mustTryTodeliver;
    private final String faultReasonText;

    protected AbstractSoapFaultException(String exceptionMessage, String faultReasonText, boolean mustTryToDeliver, Throwable cause) {
        super(exceptionMessage, cause);
        
        this.faultReasonText = faultReasonText;
        this.mustTryTodeliver = mustTryToDeliver;
    }

    protected AbstractSoapFaultException(String exceptionMessage, String faultReasonText, boolean mustTryToDeliver) {
        super(exceptionMessage);

        this.faultReasonText = faultReasonText;
        this.mustTryTodeliver = mustTryToDeliver;
    }

    public abstract Code getCode();

    public abstract QName getSubcode(RmVersion rv);

    public final String getReason() {
        return faultReasonText;
    }

    public abstract String getDetailValue();

    public boolean mustTryToDeliver() {
        return mustTryTodeliver;
    }

    public Packet toRequest(RuntimeContext rc) {
        return rc.communicator.createRequestPacket(
                createSoapFaultMessage(rc, true),
                getProperFaultActionForAddressingVersion(rc.rmVersion, rc.addressingVersion),
                false);
    }

    public Packet toResponse(RuntimeContext rc, Packet request) {
        return rc.communicator.createResponsePacket(
                request,
                createSoapFaultMessage(rc, true),
                getProperFaultActionForAddressingVersion(rc.rmVersion, rc.addressingVersion));
    }

    protected final Message createSoapFaultMessage(RuntimeContext rc, boolean attachSequenceFaultElement) {
        try {
            SOAPFault soapFault = rc.soapVersion.saajSoapFactory.createFault();

            // common SOAP1.1 and SOAP1.2 Fault settings
            if (faultReasonText != null) {
                soapFault.setFaultString(faultReasonText, java.util.Locale.ENGLISH);
            }
            
            // SOAP version-specific SOAP Fault settings
            switch (rc.soapVersion) {
                case SOAP_11:
                    soapFault.setFaultCode(getSubcode(rc.rmVersion));
                    break;
                case SOAP_12:
                    soapFault.setFaultCode(getCode().asQName(rc.soapVersion));
                    soapFault.appendFaultSubcode(getSubcode(rc.rmVersion));
                    if (getDetailValue() != null) {
                        soapFault.addDetail().setValue(getDetailValue());
                    }
                    break;
                default:
                    throw new RxRuntimeException("Unsupported SOAP version: '" + rc.soapVersion.toString() + "'");
            }

            Message soapFaultMessage = Messages.create(soapFault);

            if (attachSequenceFaultElement && rc.soapVersion == SOAPVersion.SOAP_11) {
                soapFaultMessage.getHeaders().add(rc.protocolHandler.createSequenceFaultElementHeader(getSubcode(rc.rmVersion), getDetailValue()));
            }

            return soapFaultMessage;

        } catch (SOAPException ex) {
            throw new RxRuntimeException("Error creating a SOAP fault", ex);
        }
    }

    /**
     * TODO javadoc
     *
     * @return
     */
    protected static String getProperFaultActionForAddressingVersion(RmVersion rmVersion, AddressingVersion addressingVersion) {
        return (addressingVersion == AddressingVersion.MEMBER) ? addressingVersion.getDefaultFaultAction() : rmVersion.wsrmFaultAction;
    }
}
