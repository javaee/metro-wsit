/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
package com.sun.xml.ws.rx.mc.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.AddressingUtils;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.Container;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.mc.dev.AdditionalResponses;
import com.sun.xml.ws.rx.mc.localization.LocalizationMessages;
import com.sun.xml.ws.rx.mc.protocol.wsmc200702.MakeConnectionElement;
import com.sun.xml.ws.rx.mc.protocol.wsmc200702.MessagePendingElement;
import com.sun.xml.ws.rx.message.jaxws.JaxwsMessage;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.rx.util.FiberExecutor;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Node;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class McServerTube extends AbstractFilterTubeImpl {

    private static final class AppRequestProcessingCallback implements Fiber.CompletionCallback {

        private static final Logger LOGGER = Logger.getLogger(AppRequestProcessingCallback.class);
        private final ResponseStorage responseStorage;
        private final String clientUID;
        private final McConfiguration configuration;

        public AppRequestProcessingCallback(@NotNull ResponseStorage responseStorage, @NotNull String clientUID, @NotNull McConfiguration configuration) {
            this.responseStorage = responseStorage;
            this.clientUID = clientUID;
            this.configuration = configuration;
        }

        public void onCompletion(Packet response) {
            try {
                LOGGER.finer(LocalizationMessages.WSMC_0105_STORING_RESPONSE(clientUID));
                HaContext.initFrom(response);

                storeResponse(response);
                final AdditionalResponses additionalResponses = response.getSatellite(AdditionalResponses.class);

                if (additionalResponses != null) {
                    for (Packet additionalResponse : additionalResponses.getAdditionalResponsePacketQueue()) {
                        storeResponse(additionalResponse);
                    }
                } else {
                    LOGGER.fine("Response packet did not contain any AdditionalResponses property set.");
                }
            } finally {
                HaContext.clear();
            }
        }

        public void onCompletion(Throwable error) {
            LOGGER.severe(LocalizationMessages.WSMC_0106_EXCEPTION_IN_REQUEST_PROCESSING(clientUID), error);
        }

        private void storeResponse(Packet response) {
            if (response.getMessage() != null) {
                final MessageHeaders headers = response.getMessage().getHeaders();
                headers.remove(configuration.getAddressingVersion().toTag);
                headers.add(Headers.create(configuration.getAddressingVersion().toTag, configuration.getRuntimeVersion().getAnonymousAddress(clientUID)));

                JaxwsMessage responseMessage = new JaxwsMessage(response, AddressingUtils.getMessageID(headers, configuration.getAddressingVersion(), configuration.getSoapVersion()));
                responseStorage.store(responseMessage, clientUID);
            }
        }
    }
    //
    private static final Logger LOGGER = Logger.getLogger(McServerTube.class);
    //
    private final McConfiguration configuration;
    private final FiberExecutor fiberExecutor;
    private final ResponseStorage responseStorage;
    private final Communicator communicator;

    McServerTube(McConfiguration configuration, Tube tubelineHead, Container container) {
        super(tubelineHead);

        this.configuration = configuration;
        this.fiberExecutor = new FiberExecutor("McServerTubeCommunicator", tubelineHead);
        this.responseStorage = new ResponseStorage(configuration.getUniqueEndpointId());
        this.communicator = Communicator.builder("mc-server-tube-communincator")
                .soapVersion(configuration.getSoapVersion())
                .addressingVersion(configuration.getAddressingVersion())
                .tubelineHead(super.next)
                .jaxbContext(configuration.getRuntimeVersion().getJaxbContext(configuration.getAddressingVersion()))
                .container(container)
                .build();
    }

    McServerTube(McServerTube original, TubeCloner cloner) {
        super(original, cloner);

        this.configuration = original.configuration;
        this.fiberExecutor = original.fiberExecutor;
        this.responseStorage = original.responseStorage;
        this.communicator = original.communicator;
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        LOGGER.entering();
        try {
            return new McServerTube(this, cloner);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    @SuppressWarnings("element-type-mismatch")
    public NextAction processRequest(Packet request) {
        try {
            LOGGER.entering();
            HaContext.initFrom(request);
            if (HaContext.failoverDetected()) {
                responseStorage.invalidateLocalCache();
            }

            assert request.getMessage() != null : "Unexpected [null] message in the server-side Tube.processRequest()";

            String clientUID = getClientUID(request);
            if (isMakeConnectionRequest(request)) {
                return handleMakeConnectionRequest(request, clientUID);
            }

            if (clientUID == null) {
                // don't bother - this is not a WS-MC enabled request
                return super.processRequest(request);
            } else {
                // removing replyTo header and faultTo header to prevent addressing server tube from
                // treating this request as non-anonymous
                request.getMessage().getHeaders().remove(configuration.getAddressingVersion().replyToTag);
                request.getMessage().getHeaders().remove(configuration.getAddressingVersion().faultToTag);
            }

            Packet requestCopy = request.copy(true);

            request.addSatellite(new AdditionalResponses());
            fiberExecutor.start(request, new AppRequestProcessingCallback(responseStorage, clientUID, configuration));

            return super.doReturnWith(createEmptyResponse(requestCopy));
        } finally {
            HaContext.clear();
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processResponse(Packet response) {
        try {
            LOGGER.entering();

            // with WS-MC enabled messages, this method gets never invoked
            return super.processResponse(response);
        } finally {
            LOGGER.exiting();
        }
    }

    private NextAction handleMakeConnectionRequest(Packet request, String clientUID) {
        try {
            LOGGER.entering();

            MakeConnectionElement mcElement;
            try {
                mcElement = request.getMessage().readPayloadAsJAXB(configuration.getRuntimeVersion().getUnmarshaller(configuration.getAddressingVersion()));
            } catch (JAXBException ex) {
                throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSMC_0107_ERROR_UNMARSHALLING_PROTOCOL_MESSAGE(), ex));
            }

            if (mcElement.getAddress() == null) {
                // WS-I RSP v1.0: R2102   If a wsmc:MakeConnection request does not contain a wsmc:Address child element
                // (in violation of R2100), the MC-RECEIVER MUST generate a wsmc:MissingSelection fault.
                return super.doReturnWith(createSoapFaultResponse(
                        request,
                        configuration.getSoapVersion(),
                        configuration.getAddressingVersion(),
                        configuration.getRuntimeVersion().protocolVersion.wsmcFaultAction,
                        configuration.getSoapVersion().faultCodeServer,
                        configuration.getRuntimeVersion().protocolVersion.missingSelectionFaultCode,
                        "The MakeConnection element did not contain any selection criteria.",
                        null));
            }

            if (!mcElement.getAny().isEmpty()) {
                // WS-I RSP v1.0: R2103 If a wsmc:MakeConnection request contains a wsrm:Identifier element
                // (in violation of R2101) the MC-RECEIVER MUST generate a wsmc:UnsupportedSelection fault.
                List<SoapFaultDetailEntry> unsupportedSelections = new ArrayList<SoapFaultDetailEntry>(mcElement.getAny().size());
                for (Object element : mcElement.getAny()) {
                    if (element instanceof Node) {
                        Node selectionNode = ((Node) element);
                        unsupportedSelections.add(new SoapFaultDetailEntry(
                                configuration.getRuntimeVersion().protocolVersion.unsupportedSelectionFaultCode,
                                new QName(selectionNode.getNamespaceURI(), selectionNode.getLocalName()).toString()));
                    }
                }

                return super.doReturnWith(createSoapFaultResponse(
                        request,
                        configuration.getSoapVersion(),
                        configuration.getAddressingVersion(),
                        configuration.getRuntimeVersion().protocolVersion.wsmcFaultAction,
                        configuration.getSoapVersion().faultCodeServer,
                        configuration.getRuntimeVersion().protocolVersion.unsupportedSelectionFaultCode,
                        "The extension element used in the message selection is not supported by the MakeConnection receiver.",
                        unsupportedSelections));
            }

            String selectionUID = configuration.getRuntimeVersion().getClientId(mcElement.getAddress().getValue());

            if (selectionUID == null) {
                // TODO return a MissingSelection SOAP fault
                throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSMC_0108_NULL_SELECTION_ADDRESS()));
            }

            if (clientUID != null && !selectionUID.equals(clientUID)) {
                // Fixed WSIT issue #1334:
                // This cannot be an excpetion, because according to the WS=MakeConnection specification,
                // section 3.2 [ http://docs.oasis-open.org/ws-rx/wsmc/200702/wsmc-1.1-spec-os.html#_Toc162743906 ]:
                //
                // ...
                // Since the message exchange pattern use by MakeConnection is untraditional, the following points
                // need to be reiterated for clarification:
                // * The MakeConnection message is logically part of a one-way operation; there is no reply message
                //   to the MakeConnection itself, and any response flowing on the transport back-channel is a pending message.
                // * Since there is no reply message to MakeConnection, the WS-Addressing specific rules in
                //   section 3.4 "Formulating a Reply Message" are not used. Therefore, the value of any wsa:ReplyTo element
                //   in the MakeConnection message has no effective impact since the WS-Addressing [reply endpoint] property
                //   that is set by the presence of wsa:ReplyTo is not used.
                // ...
                //
                // Because of the above, we just log a warning message
                LOGGER.warning(LocalizationMessages.WSMC_0109_SELECTION_ADDRESS_NOT_MATCHING_WSA_REPLYTO(selectionUID, clientUID));
            }

            Packet response = null;
            
            final JaxwsMessage pendingMessage = responseStorage.getPendingResponse(selectionUID);
            if (pendingMessage != null) {
                LOGGER.finer(LocalizationMessages.WSMC_0110_PENDING_MESSAGE_FOUND_FOR_SELECTION_UUID(selectionUID));

                if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
                    if (pendingMessage.getPacket() == null) {
                        // FIXME: loaded from DB without a valid packet - create one
                        // ...this is a workaround until JAX-WS RI API provides a mechanism how to (de)serialize whole Packet
                        pendingMessage.setPacket(communicator.createEmptyResponsePacket(request, pendingMessage.getWsaAction()));
                    }
                }
                response = pendingMessage.getPacket();
            }

            if (response == null) {
                LOGGER.finer(LocalizationMessages.WSMC_0111_NO_PENDING_MESSAGE_FOUND_FOR_SELECTION_UUID(selectionUID));
                response = createEmptyResponse(request);
            } else {
                Message message = response.getMessage();
                if (message != null) {
                    MessageHeaders headers = message.getHeaders();
                    headers.add(Headers.create(
                            configuration.getRuntimeVersion().getJaxbContext(configuration.getAddressingVersion()),
                            new MessagePendingElement(Boolean.valueOf(selectionUID != null && responseStorage.hasPendingResponse(selectionUID)))));
                }
            }

            return super.doReturnWith(response);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public NextAction processException(Throwable t) {
        try {
            LOGGER.entering();

            return super.processException(t);
        } finally {
            LOGGER.exiting();
        }
    }

    @Override
    public void preDestroy() {
        responseStorage.dispose();
        
        super.preDestroy();
    }

    private String getClientUID(Packet request) {
        Header replyToHeader = request.getMessage().getHeaders().get(configuration.getAddressingVersion().replyToTag, false);
        if (replyToHeader != null) {
            try {
                String replyToAddress = replyToHeader.readAsEPR(configuration.getAddressingVersion()).getAddress();
                return configuration.getRuntimeVersion().getClientId(replyToAddress);
            } catch (XMLStreamException ex) {
                throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSMC_0103_ERROR_RETRIEVING_WSA_REPLYTO_CONTENT(), ex));
            }
        }

        return null;
    }

    private boolean isMakeConnectionRequest(final Packet request) {
        return configuration.getRuntimeVersion().protocolVersion.wsmcAction.equals(AddressingUtils.getAction(request.getMessage().getHeaders(), configuration.getAddressingVersion(), configuration.getSoapVersion()));
    }

    private Packet createEmptyResponse(Packet request) {
        return request.createServerResponse(null, null, null, "");
    }

    private Packet createSoapFaultResponse(Packet request, SOAPVersion soapVersion, AddressingVersion av, String action, QName code, QName subcode, String faultReasonText, List<SoapFaultDetailEntry> detailEntries) {
        try {
            SOAPFault soapFault = soapVersion.saajSoapFactory.createFault();

            // common SOAP1.1 and SOAP1.2 Fault settings
            if (faultReasonText != null) {
                soapFault.setFaultString(faultReasonText, java.util.Locale.ENGLISH);
            }

            // SOAP version-specific SOAP Fault settings
            switch (soapVersion) {
                case SOAP_11:
                    soapFault.setFaultCode(subcode);
                    break;
                case SOAP_12:
                    soapFault.setFaultCode(code);
                    soapFault.appendFaultSubcode(subcode);

                    if (detailEntries != null && !detailEntries.isEmpty()) {
                        final Detail detail = soapFault.addDetail();
                        for (SoapFaultDetailEntry entry : detailEntries) {
                            detail.addDetailEntry(entry.name).setValue(entry.value);
                        }
                    }
                    break;
                default:
                    throw new RxRuntimeException("Unsupported SOAP version: '" + soapVersion.toString() + "'");
            }

            Message soapFaultMessage = Messages.create(soapFault);

            return request.createServerResponse(soapFaultMessage, av, soapVersion, action);

        } catch (SOAPException ex) {
            throw new RxRuntimeException("Error creating a SOAP fault", ex);
        }
    }
}
