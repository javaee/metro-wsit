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

package com.sun.xml.ws.rx.util;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.AddressingUtils;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.security.trust.WSTrustException;
import com.sun.xml.ws.message.RelatesToHeader;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Transmits standalone protocol messages over the wire. Provides also some additional 
 * utility methods for creating and unmarshalling JAXWS {@link Message} and {@link Header}
 * objects.
 *
 * <b>
 * WARNING: This class is a private utility class used by WS-RX implementation. 
 * Any usage outside the intended scope is strongly discouraged. The API exposed
 * by this class may be changed, replaced or removed without any advance notice.
 * </b>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class Communicator {

    public static final class Builder {

        private final String name;
        private Tube tubelineHead;
        private SecureConversationInitiator scInitiator;
        private AddressingVersion addressingVersion = AddressingVersion.W3C;
        private SOAPVersion soapVersion = SOAPVersion.SOAP_12;
        private JAXBRIContext jaxbContext;

        private Builder(String name) {
            this.name = name;
        }

        public Builder addressingVersion(AddressingVersion value) {
            this.addressingVersion = value;

            return this;
        }

        public Builder jaxbContext(JAXBRIContext value) {
            this.jaxbContext = value;

            return this;
        }

        public Builder secureConversationInitiator(SecureConversationInitiator value) {
            this.scInitiator = value;

            return this;
        }

        public Builder soapVersion(SOAPVersion value) {
            this.soapVersion = value;

            return this;
        }

        public Builder tubelineHead(Tube value) {
            this.tubelineHead = value;

            return this;
        }

        public Communicator build() {
            if (tubelineHead == null) {
                throw new IllegalStateException("Cannot create communicator instance: tubeline head has not been set.");
            }
            if (jaxbContext == null) {
                throw new IllegalStateException("Cannot create communicator instance: JAXB context has not been set.");
            }

            return new Communicator(name, tubelineHead, scInitiator, addressingVersion, soapVersion, jaxbContext);
        }
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }
    // TODO P2 introduce an inner builder class
    private static final Logger LOGGER = Logger.getLogger(Communicator.class);
    public final QName soapMustUnderstandAttributeName;
    //
    private final SecureConversationInitiator scInitiator;
    //
    private final AddressingVersion addressingVersion;
    private final SOAPVersion soapVersion;
    private final JAXBRIContext jaxbContext;
    //
    private FiberExecutor fiberExecutor;
    private volatile EndpointAddress destinationAddress;

    private Communicator(
            @NotNull String name,
            @NotNull Tube tubeline,
            @Nullable SecureConversationInitiator scInitiator,
            @NotNull AddressingVersion addressingVersion,
            @NotNull SOAPVersion soapVersion,
            @NotNull JAXBRIContext jaxbContext) {
        this.destinationAddress = null;
        this.fiberExecutor = new FiberExecutor(name, tubeline);
        this.scInitiator = scInitiator;
        this.addressingVersion = addressingVersion;
        this.soapVersion = soapVersion;
        this.soapMustUnderstandAttributeName = new QName(soapVersion.nsUri, "mustUnderstand");
        this.jaxbContext = jaxbContext;
    }

    public final Packet createRequestPacket(Object jaxbElement, String wsaAction, boolean expectReply) {
        Message message = Messages.create(jaxbContext, jaxbElement, soapVersion);

        return createRequestPacket(message, wsaAction, expectReply);
    }

    public final Packet createRequestPacket(Message message, String wsaAction, boolean expectReply) {
        if (destinationAddress == null) {
            throw new IllegalStateException("Destination address is not defined in this communicator instance");
        }

        Packet packet = new Packet(message);
        packet.endpointAddress = destinationAddress;
        packet.expectReply = expectReply;
        AddressingUtils.fillRequestAddressingHeaders(
                message.getHeaders(),
                packet,
                addressingVersion,
                soapVersion,
                !expectReply,
                wsaAction);

        return packet;
    }

    public final Packet createRequestPacket(Packet originalRequestPacket, Object jaxbElement, String wsaAction, boolean expectReply) {
        if (originalRequestPacket != null) { // server side request transferred as a response
            Packet request = createResponsePacket(originalRequestPacket, jaxbElement, wsaAction, false);

            final MessageHeaders requestHeaders = request.getMessage().getHeaders();
            if (expectReply) { // attach wsa:ReplyTo header from the original request
                final String endpointAddress = AddressingUtils.getTo(originalRequestPacket.getMessage().getHeaders(), addressingVersion, soapVersion);
                requestHeaders.add(createReplyToHeader(endpointAddress));
            }
            requestHeaders.remove(addressingVersion.relatesToTag);

            return request;
        } else {
            Message message = Messages.create(jaxbContext, jaxbElement, soapVersion);
            return createRequestPacket(message, wsaAction, expectReply);
        }
    }

    private Header createReplyToHeader(String address) {
        WSEndpointReference wsepr = new WSEndpointReference(address, addressingVersion);
        return wsepr.createHeader(addressingVersion.replyToTag);
    }

    /**
     * Creates a new empty request packet
     *
     * @return a new empty request packet
     */
    public Packet createEmptyRequestPacket(boolean expectReply) {
        if (destinationAddress == null) {
            throw new IllegalStateException("Destination address is not defined in this communicator instance");
        }

        Packet packet = new Packet();
        packet.endpointAddress = destinationAddress;
        packet.expectReply = expectReply;

        return packet;
    }

    /**
     * Creates a new empty request packet with an empty message that has WS-A action set
     *
     * @return a new empty request packet
     */
    public Packet createEmptyRequestPacket(String requestWsaAction, boolean expectReply) {
        return createRequestPacket(Messages.createEmpty(soapVersion), requestWsaAction, expectReply);
    }

    /**
     * Creates new response packet based for the supplied request packet
     * with the provided response WS-Addressing action set.
     *
     * @param requestPacket original request the newly created response belongs to
     * @param responseWsaAction WS-Addressing action header value to be set
     * @param isClientResponse determines whether the response is technically a client request
     *
     * @return newly created response packet
     */
    public Packet createResponsePacket(@NotNull Packet requestPacket, Object jaxbElement, String responseWsaAction, boolean isClientResponse) {
        assert requestPacket != null : "Request packet must not be 'null' when creating a response";
        if (!isClientResponse) { // server side response
            return requestPacket.createServerResponse(
                    Messages.create(jaxbContext, jaxbElement, soapVersion),
                    addressingVersion,
                    soapVersion,
                    responseWsaAction);
        } else { // client side response transferred as a request
            Packet response = createRequestPacket(jaxbElement, responseWsaAction, false);
            response.getMessage().getHeaders().add(new RelatesToHeader(
                    addressingVersion.relatesToTag,
                    AddressingUtils.getMessageID(requestPacket.getMessage().getHeaders(), addressingVersion, soapVersion)));
            return response;
        }
    }

    /**
     * Creates new response packet based for the supplied request packet
     * with the provided response WS-Addressing action set.
     *
     * @param requestPacket original request the newly created response belongs to
     * @param responseWsaAction WS-Addressing action header value to be set
     *
     * @return newly created response packet
     */
    public Packet createResponsePacket(Packet requestPacket, Message message, String responseWsaAction) {
        if (requestPacket != null) { // server side response
            return requestPacket.createServerResponse(
                    message,
                    addressingVersion,
                    soapVersion,
                    responseWsaAction);
        } else { // client side response transferred as a request
            return createRequestPacket(message, responseWsaAction, false);
        }
    }

    /**
     * Creates an empty (no SOAP body payload) new response packet based for the
     * supplied request packet with the provided response WS-Addressing action set.
     *
     * @param requestPacket original request the newly created response belongs to
     * @param responseWsaAction WS-Addressing action header value to be set
     *
     * @return newly created empty (no SOAP body payload) response packet
     */
    public Packet createEmptyResponsePacket(Packet requestPacket, String responseWsaAction) {
        if (requestPacket != null) { // server side response
            return requestPacket.createServerResponse(
                    Messages.createEmpty(soapVersion),
                    addressingVersion,
                    soapVersion,
                    responseWsaAction);
        } else { // client side response transferred as a request
            return createEmptyRequestPacket(responseWsaAction, false);
        }
    }

    /**
     * Creates a null (no message) response packet based for the supplied request packet.
     *
     * @param requestPacket original request the newly created response belongs to
     *
     * @return newly created null (no message) response packet
     */
    public Packet createNullResponsePacket(Packet requestPacket) {
        if (requestPacket.transportBackChannel != null) {
            requestPacket.transportBackChannel.close();
        }

        Packet emptyReturnPacket = new Packet();
        emptyReturnPacket.invocationProperties.putAll(requestPacket.invocationProperties);
        return emptyReturnPacket;
    }

    /**
     * Creates a new JAX-WS {@link Message} object that doesn't have any payload
     * and sets it as the current packet content as a request message.
     *
     * @param wsaAction WS-Addressing action header to set
     *
     * @return the updated {@link PacketAdapter} instance
     */
    public final Packet setEmptyRequestMessage(Packet request, String wsaAction) {
        Message message = Messages.createEmpty(soapVersion);
        request.setMessage(message);
        AddressingUtils.fillRequestAddressingHeaders(
                message.getHeaders(),
                request,
                addressingVersion,
                soapVersion,
                false,
                wsaAction);


        return request;
    }

    /**
     * Overwrites the {@link Message} of the response packet with a newly created empty {@link Message} instance.
     * Unlike {@link Packet#setMessage(Message)}, this method fills in the {@link Message}'s WS-Addressing headers
     * correctly, based on the provided request packet WS-Addressing headers.
     *
     * @param requestAdapter
     * @param wsaAction
     * @return
     */
    public final Packet setEmptyResponseMessage(Packet response, Packet request, String wsaAction) {
        Message message = Messages.createEmpty(soapVersion);
        response.setResponseMessage(request, message, addressingVersion, soapVersion, wsaAction);
        return response;
    }

    /**
     * Returns the value of WS-Addressing {@code Action} header of a message stored
     * in the {@link Packet}.
     *
     * @param packet JAX-WS RI packet
     * @return Value of WS-Addressing {@code Action} header, {@code null} if the header is not present
     */
    public String getWsaAction(Packet packet) {
        if (packet == null || packet.getMessage() == null) {
            return null;
        }

        return AddressingUtils.getAction(packet.getMessage().getHeaders(), addressingVersion, soapVersion);
    }

    /**
     * Returns the value of WS-Addressing {@code To} header of a message stored
     * in the {@link Packet}.
     *
     * @param packet JAX-WS RI packet
     * @return Value of WS-Addressing {@code To} header, {@code null} if the header is not present
     */
    public String getWsaTo(Packet packet) {
        if (packet == null || packet.getMessage() == null) {
            return null;
        }

        return AddressingUtils.getTo(packet.getMessage().getHeaders(), addressingVersion, soapVersion);
    }

    /**
     * If security is enabled, tries to initate secured conversation and obtain the security token reference.
     * 
     * @return security token reference of the initiated secured conversation, or {@code null} if there is no SC configured
     */
    public SecurityTokenReferenceType tryStartSecureConversation(Packet request) throws WSTrustException {
        if (scInitiator == null) {
            return null;
        }

        Packet emptyPacket = createEmptyRequestPacket(false);
        emptyPacket.invocationProperties.putAll(request.invocationProperties);
        @SuppressWarnings("unchecked")
        JAXBElement<SecurityTokenReferenceType> strElement = scInitiator.startSecureConversation(emptyPacket);

        return (strElement != null) ? strElement.getValue() : null;
    }

    /**
     * Sends the request {@link Packet} and returns the corresponding response {@link Packet}.
     * This method should be used for Req-Res MEP
     *
     * @param request {@link Packet} containing the message to be send
     * @return response {@link Message} wrapped in a response {@link Packet} received
     */
    public Packet send(@NotNull Packet request) {
        if (fiberExecutor == null) {
            LOGGER.fine("Cannot send messages: this Communicator instance has been closed");
        }

        return fiberExecutor.runSync(request);
    }

    /**
     * Asynchronously sends the request {@link Packet}
     *
     * @param request {@link Packet} containing the message to be send
     * @param completionCallbackHandler completion callback handler to process the response.
     *        May be {@code null}. In such case a generic completion callback handler will be used.
     */
    public void sendAsync(@NotNull final Packet request, @Nullable final Fiber.CompletionCallback completionCallbackHandler) {
        if (fiberExecutor == null) {
            LOGGER.fine("Cannot send messages: this Communicator instance has been closed");
        }

        if (completionCallbackHandler != null) {
            fiberExecutor.start(request, completionCallbackHandler);
        } else {
            fiberExecutor.start(request, new Fiber.CompletionCallback() {

                public void onCompletion(Packet response) {
                    // do nothing
                }

                public void onCompletion(Throwable error) {
                    LOGGER.warning("Unexpected exception occured", error);
                }
            });
        }
    }

    /**
     * Provides the destination endpoint reference this {@link Communicator} is pointing to. 
     * May return {@code null} (typically when used on the server side).
     * 
     * @return destination endpoint reference or {@code null} in case the destination address has
     *         not been specified when constructing this {@link Communicator} instance.
     */
    public
    @Nullable
    EndpointAddress getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(EndpointAddress newValue) {
        this.destinationAddress = newValue;
    }

    public void setDestinationAddressFrom(Packet packet) {
        this.destinationAddress = packet.endpointAddress;
    }

    public AddressingVersion getAddressingVersion() {
        return addressingVersion;
    }

    public SOAPVersion getSoapVersion() {
        return soapVersion;
    }

    public void close() {
        final FiberExecutor fe = this.fiberExecutor;
        if (fe != null) {
            fe.close();
            this.fiberExecutor = null;
        }
    }

    public boolean isClosed() {
        return this.fiberExecutor == null;
    }
}
