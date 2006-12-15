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

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import static com.sun.xml.ws.tx.common.Constants.COORDINATION_CONTEXT;
import static com.sun.xml.ws.tx.common.Constants.WSCOOR_SOAP_NSURI;
import com.sun.xml.ws.tx.coordinator.CoordinationContextBase;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

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
    private com.sun.xml.ws.api.message.Message message = null;

    /* Caches of representations of CoordinationContext */
    private Header ccHdr = null;
    static final private int NOT_FOUND = -1;
    private int ccHdrIndex = NOT_FOUND;

    private CoordinationContextInterface CC = null;

    /**
     * Public ctor takes wrapped JAX-WS message as its argument.
     * @param message core message
     */
    public Message(@NotNull com.sun.xml.ws.api.message.Message message) {
        this.message = message;
    }


    /**
     * Get the CoordinationContext Header Element from the underlying
     * JAX-WS message's HeaderList. Only understand the header iff CoordinationContext is
     * for coordinationType.
     * @return the coordination context in this message
     */
    @NotNull
    public com.sun.xml.ws.api.message.Header getCoordCtxHeader() {
        if (ccHdr == null && message != null) {
            HeaderList hdrList = message.getHeaders();
            if (hdrList != null) {
                hdrList = message.getHeaders();
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
     * @return index of coordination context in header list or null if not found
     * @param namespace namespace
     * @param localName local name
     */
    @Nullable
    public com.sun.xml.ws.api.message.Header getCoordCtxHeader(@NotNull String namespace, @NotNull String localName) {
        if (ccHdr == null && message != null) {
            ccHdrIndex = NOT_FOUND;
            HeaderList hlst = message.getHeaders();
            int len = hlst.size();
            for (int i = 0; i < len; i++) {
                Header h = hlst.get(i);
                if (h.getLocalPart().equals(localName) && h.getNamespaceURI().equals(namespace)) {
                    ccHdrIndex = i;
                    ccHdr = h;
                }
            }
        }
        return ccHdr;
    }
    
    /**
     * 
     * @param unmarshaller jaxb unmarshaller
     * @return the coordination context
     */
    @Nullable
    public CoordinationContextInterface getCoordinationContext(@NotNull Unmarshaller unmarshaller) throws JAXBException 
    {
        if (CC == null) {
            Header ccHdr = getCoordCtxHeader(WSCOOR_SOAP_NSURI, COORDINATION_CONTEXT);
            if (ccHdr != null) {
                try {
                    CC = CoordinationContextBase.createCoordinationContext(ccHdr.readAsJAXB(unmarshaller));
                } catch (JAXBException e) {
                    if (logger.isLogging(Level.WARNING)) {
                        logger.warning("getCoordinationContext", "can not unmarshal 2004 WS-Coordination CoordinationContext. Exception message: " + e.getLocalizedMessage());
                    }
                    throw e;
                }
            }
        }
        return CC;
    }

    /**
     * Denote that CoordinationContext SOAP Header was processed and considered understood.
     */
    public void setCoordCtxUnderstood() {
        if (ccHdr != null && ccHdrIndex != NOT_FOUND) {
            message.getHeaders().understood(ccHdrIndex);
        }
    }

    /**
     * Get the wsdl bound operation for the specified port
     * @param port port
     * @return the wsdl operation or null if not found
     */
    @Nullable
    public WSDLBoundOperation getOperation(@NotNull WSDLPort port) {
        return message.getOperation(port);
    }
}
