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
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import static com.sun.xml.ws.tx.common.Constants.COORDINATION_CONTEXT;
import static com.sun.xml.ws.tx.common.Constants.WSCOOR_SOAP_NSURI;
import com.sun.xml.ws.tx.coordinator.CoordinationContextBase;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.logging.Level;

/**
 * WS-TX view of a Message.
 *
 * @author jf39279
 */
public class Message {

    static final private TxLogger logger = TxLogger.getLogger(Message.class);


    /**
     * The JAX-WS Message wrapped by this instance.
     */
    final private com.sun.xml.ws.api.message.Message coreMessage;
    final private com.sun.xml.ws.api.message.HeaderList hdrList;
    private SOAPVersion SOAP_VERSION;
    private AddressingVersion ADDRESSING_VERSION;
    
    /* Caches of representations of CoordinationContext */
    private Header ccHdr = null;
    static final private int NOT_FOUND = -1;
    private int ccHdrIndex = NOT_FOUND;

    private CoordinationContextInterface cc = null;

    /**
     * Public ctor takes wrapped JAX-WS message as its argument.
     *
     * @param message core message
     */
    public Message(@NotNull final com.sun.xml.ws.api.message.Message message,
                   final WSBinding wsBinding) {
        this.coreMessage = message;
        this.hdrList = (message == null ? null : message.getHeaders());
        SOAP_VERSION = (wsBinding == null ? null : wsBinding.getSOAPVersion());
        ADDRESSING_VERSION = (wsBinding == null ? null : wsBinding.getAddressingVersion());
    }

    /**
     * Public ctor takes wrapped JAX-WS message as its argument.
     *
     * @param message core message
     */
    public Message(@NotNull final com.sun.xml.ws.api.message.Message message) {
         this(message, null);
    }
    
    /**
     * Get the CoordinationContext Header Element from the underlying
     * JAX-WS message's HeaderList. Only understand the header iff CoordinationContext is
     * for coordinationType.
     *
     * @return the coordination context in this message
     */
    @NotNull
    public com.sun.xml.ws.api.message.Header getCoordCtxHeader() {
        if (ccHdr == null) {
            if (hdrList != null) {
                ccHdr = hdrList.get(WSCOOR_SOAP_NSURI, COORDINATION_CONTEXT, false);

                /*
                * Note: include when supporting OASIS WS-TX
                * don't check for it since it will be a must understand soap header,
                * must understand header processing should flag it is not processed.
               if (ccHdr == null) {
                   hdrList.get(Constants.WSAT_OASIS_NSURI, COORDINATION_CONTEXT, true);
                   ....
               }
                */

            }
        }
        return ccHdr;
    }

    /**
     * Get the CoordinationContext Header Element from the underlying
     * JAX-WS message's HeaderList. Only understand the header iff CoordinationContext is
     * for coordinationType.
     *
     * @param namespace namespace
     * @param localName local name
     * @return index of coordination context in header list or null if not found
     */
    @Nullable
    public com.sun.xml.ws.api.message.Header getCoordCtxHeader(@NotNull final String namespace, @NotNull final String localName) {
        if (ccHdr == null && coreMessage != null) {
            ccHdrIndex = NOT_FOUND;
            final int len = hdrList.size();
            for (int i = 0; i < len; i++) {
                final Header h = hdrList.get(i);
                if (h.getLocalPart().equals(localName) && h.getNamespaceURI().equals(namespace)) {
                    ccHdrIndex = i;
                    ccHdr = h;
                    break;
                }
            }
        }
        return ccHdr;
    }

    /**
     * @param unmarshaller jaxb unmarshaller
     * @return the coordination context
     */
    @Nullable
    public CoordinationContextInterface getCoordinationContext(@NotNull final Unmarshaller unmarshaller) throws JAXBException {
        if (cc == null) {
            final Header ccHdr = getCoordCtxHeader(WSCOOR_SOAP_NSURI, COORDINATION_CONTEXT);
            if (ccHdr != null) {
                try {
                    cc = CoordinationContextBase.createCoordinationContext(ccHdr.readAsJAXB(unmarshaller));
                } catch (JAXBException e) {
                    logger.warning("getCoordinationContext", LocalizationMessages.CANNOT_UNMARSHAL_CONTEXT_2000(e.getLocalizedMessage()));
                    throw e;
                }
            }
        }
        return cc;
    }

    /**
     * Denote that CoordinationContext SOAP Header was processed and considered understood.
     */
    public void setCoordCtxUnderstood() {
        if (ccHdr != null && ccHdrIndex != NOT_FOUND) {
            coreMessage.getHeaders().understood(ccHdrIndex);
        }
    }

    /**
     * Get the wsdl bound operation for the specified port
     *
     * @param port port
     * @return the wsdl operation or null if not found
     */
    @Nullable
    public WSDLBoundOperation getOperation(@NotNull final WSDLPort port) {
        return coreMessage.getOperation(port);
    }
    
   /**
     * @return the ws-addressing MessageId for this message
     */
    public String getMessageID() {
        String result = null;
        if (hdrList != null && ADDRESSING_VERSION != null && SOAP_VERSION != null) {
            result = hdrList.getMessageID(ADDRESSING_VERSION, SOAP_VERSION);
        }
        return result;
    }
    
    /**
     * @return the ws-addressing To for this message
     */
    public String getTo() {
        String result = null;
        if (hdrList != null && ADDRESSING_VERSION != null && SOAP_VERSION != null) {
            result = hdrList.getTo(ADDRESSING_VERSION, SOAP_VERSION);
        }
        return result;
    }
    
     /**
     * @return the ws-addressing Action for this message
     */
    public String getAction() {
        String result = null;
        if (hdrList != null && ADDRESSING_VERSION != null && SOAP_VERSION != null) {
            result = hdrList.getAction(ADDRESSING_VERSION, SOAP_VERSION);
        }
        return result;
    }
    
     /**
     * @return the ws-addressing FaultTo for this message
     */
    public WSEndpointReference getFaultTo() {
        WSEndpointReference result = null;
        if (hdrList != null && ADDRESSING_VERSION != null && SOAP_VERSION != null) {
            result = hdrList.getFaultTo(ADDRESSING_VERSION, SOAP_VERSION);
        }
        return result;
    }
    
    /**
     * @return the ws-addressing ReplyTo for this message
     */
    public WSEndpointReference getReplyTo() {
        WSEndpointReference result = null;
        if (hdrList != null&& ADDRESSING_VERSION != null && SOAP_VERSION != null) {
            result = hdrList.getReplyTo(ADDRESSING_VERSION, SOAP_VERSION);
        }
        return result;
    }
}
