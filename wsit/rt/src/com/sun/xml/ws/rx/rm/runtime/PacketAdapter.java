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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.xml.ws.rx.RxConfiguration;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.RmVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class PacketAdapter {

    private static final Logger LOGGER = Logger.getLogger(PacketAdapter.class);
    private static final String SEQUENCE_PROPERTY = "com.sun.xml.ws.sequence";
    private static final String MESSAGE_NUMBER_PROPERTY = "com.sun.xml.ws.messagenumber";
    //
    Message message;
    //
    private Packet packet;
    private boolean isSequenceDataInit;
    private boolean isAckRequestedHeaderDataInit;
    private String sequenceId;
    private String ackRequestedHeaderSequenceId;
    private long messageNumber;
    // TODO remove this workaround
    private boolean messageConsumed;
    //
    private final RmVersion rmVersion;
    private final SOAPVersion soapVersion;
    private final AddressingVersion addressingVersion;
    private final Unmarshaller jaxbUnmarshaller;

    /**
     * Provides an instance of a packet adapter based on the configuration and attaches a provided 
     * {@code packet} instance to it.
     * 
     * @param configuration configuration used to configure newly created packet
     * 
     * @param packet {@link Packet} instance to be attached to the newly created packet adapter
     * 
     * @return new empty {@link PacketAdapter} instance
     */
    public static PacketAdapter getInstance(@NotNull RxConfiguration configuration, @NotNull Packet packet) {
        return getInstance(configuration.getRmVersion(), configuration.getSoapVersion(), configuration.getAddressingVersion(), packet);
    }

    public static PacketAdapter getInstance(@NotNull RmVersion rmVersion, @NotNull SOAPVersion soapVersion, @NotNull AddressingVersion addressingVersion, @NotNull Packet packet) {
        switch (rmVersion) {
            case WSRM200502:
                return new Rm10PacketAdapter(soapVersion, addressingVersion, packet);
            case WSRM200702:
                return new Rm11PacketAdapter(soapVersion, addressingVersion, packet);
            default:
                throw new IllegalStateException(LocalizationMessages.WSRM_1104_RM_VERSION_NOT_SUPPORTED(rmVersion.namespaceUri));
        }
    }

    /**
     * TODO javadoc
     */
    PacketAdapter(@NotNull RmVersion rmVersion, @NotNull SOAPVersion soapVersion, @NotNull AddressingVersion addressingVersion, @NotNull Packet packet) {
        this.rmVersion = rmVersion;
        this.soapVersion = soapVersion;
        this.addressingVersion = addressingVersion;
        this.jaxbUnmarshaller = rmVersion.createUnmarshaller(addressingVersion);

        insertPacket(packet);
    }

    /**
     * This method sets the {@link Packet}'s {@code expectReply} flag to {@code true}
     */
    void setExpectReply() {
        checkPacketReadyState();

        this.packet.expectReply = Boolean.TRUE;
    }

    private final void insertPacket(Packet packet) {
        this.packet = packet;
        if (packet.getMessage() != null) {
            this.message = packet.getMessage();
        }

    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final void consume() {
        if (message != null && !messageConsumed) {
            messageConsumed = true; // TODO remove this workaround
            message.consume();
        }
    }

    /**
     * TODO javadoc
     */
    public final Packet getPacket() {
        return packet;
    }

    /**
     * TODO javadoc
     */
    public final Packet copyPacket(boolean copyMessage) {
        return packet.copy(copyMessage);
    }

    /**
     * TODO javadoc
     */
    public final PacketAdapter createServerResponse(Object jaxbElement, String wsaAction) {
        return PacketAdapter.getInstance(rmVersion, soapVersion, addressingVersion, packet.createServerResponse(
                Messages.create(rmVersion.getJaxbContext(addressingVersion), jaxbElement, soapVersion),
                addressingVersion,
                soapVersion,
                wsaAction));
    }

    /**
     * TODO javadoc
     */
    public final PacketAdapter createEmptyServerResponse(String wsaAction) {
        return PacketAdapter.getInstance(rmVersion, soapVersion, addressingVersion, packet.createServerResponse(
                Messages.createEmpty(soapVersion),
                addressingVersion,
                soapVersion,
                wsaAction));
    }

    /**
     * TODO javadoc
     * 
     * @param requestAdapter
     * @param inboundSequence
     * @param wsaAction
     * @return
     * @throws RxRuntimeException
     */
    public final PacketAdapter createAckResponse(Sequence sequence, String wsaAction) throws RxRuntimeException {
        PacketAdapter responseAdapter = this.createEmptyServerResponse(wsaAction);
        responseAdapter.appendSequenceAcknowledgementHeader(sequence);
        return responseAdapter;
    }
    
    public final PacketAdapter closeTransportAndReturnNull() {
        this.packet.transportBackChannel.close();
        Packet emptyReturnPacket = new Packet();
        emptyReturnPacket.invocationProperties.putAll(this.packet.invocationProperties);
        return PacketAdapter.getInstance(rmVersion, soapVersion, addressingVersion, emptyReturnPacket);
    }

    /**
     * Utility method which creates a RM {@link Header} with the specified JAXB bean content
     * and adds it to the message stored in the underlying packet.
     * 
     * @param jaxbHeaderContent content of the newly created {@link Header}
     * 
     * @throws java.lang.IllegalStateException in case of failed internal state check
     */
    public final void appendHeader(Object jaxbHeaderContent) throws IllegalStateException {
        checkMessageReadyState();

        message.getHeaders().add(Headers.create(rmVersion.getJaxbContext(addressingVersion), jaxbHeaderContent));
    }

    /**
     * TODO javadoc
     */
    public abstract void appendSequenceHeader(@NotNull String sequenceId, long messageNumber) throws RxRuntimeException;

    /**
     * TODO javadoc
     */
    protected abstract void appendAckRequestedHeader(@NotNull String sequenceId) throws RxRuntimeException;

    /**
     * TODO javadoc
     */
    public abstract void appendSequenceAcknowledgementHeader(@NotNull Sequence sequence) throws RxRuntimeException;

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final PacketAdapter keepTransportBackChannelOpen() {
        this.packet.keepTransportBackChannelOpen();
        
        return this;
    }
    
    /**
     * Creates a new JAX-WS {@link Message} object that doesn't have any payload
     * and sets it as the current packet content as a request message.
     * 
     * @param wsaAction WS-Addressing action header to set 
     * 
     * @return the updated {@link PacketAdapter} instance
     */
    public final PacketAdapter setEmptyRequestMessage(String wsaAction) {
        return setRequestMessage(Messages.createEmpty(soapVersion), wsaAction);
    }
    
    /**
     * TODO javadoc
     * 
     * @param requestAdapter
     * @param wsaAction
     * @return
     */
    public final PacketAdapter setEmptyResponseMessage(PacketAdapter requestAdapter, String wsaAction) {
// TODO remove the commented code once the new code is proven to work
//        checkPacketReadyState();
//        
//        Packet tempPacket = requestAdapter.packet.createServerResponse(
//                Messages.createEmpty(soapVersion),
//                addressingVersion,
//                soapVersion,
//                wsaAction);
//        
//        this.packet.setMessage(tempPacket.getMessage());
//        this.message = this.packet.getMessage();

        checkPacketReadyState();
        
        this.message = Messages.createEmpty(soapVersion);
        this.packet.setResponseMessage(requestAdapter.packet, message, addressingVersion, soapVersion, wsaAction);
        return this;        
    }

    /**
     * TODO javadoc
     * 
     * Creates a new JAX-WS {@link Message} object backed by a JAXB bean using JAXB context of a configured RM version.
     *
     * @param jaxbObject
     *      The JAXB object that represents the payload. must not be null. This object
     *      must be bound to an element (which means it either is a {@link JAXBElement} or
     *      an instanceof a class with {@link XmlRootElement}).
     * @param wsaAction
     * 
     * @return the updated {@link PacketAdapter} instance
     */
    public final PacketAdapter setRequestMessage(Object jaxbElement, String wsaAction) {
        return setRequestMessage(Messages.create(rmVersion.getJaxbContext(addressingVersion), jaxbElement, soapVersion), wsaAction);
    }

    /**
     * TODO javadoc
     * 
     * @param message
     * @param wsaAction
     * @return
     */
    private final PacketAdapter setRequestMessage(Message newMessage, String wsaAction) {
        checkPacketReadyState();

        this.message = newMessage;
        this.packet.setMessage(this.message);

        this.message.assertOneWay(false); // TODO do we really need to call this assert here?
        this.message.getHeaders().fillRequestAddressingHeaders(
                this.packet,
                addressingVersion,
                soapVersion,
                false,
                wsaAction);

        return this;
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final boolean isProtocolMessage() {
        return (message == null) ? false : rmVersion.isRmAction(getWsaAction());
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final boolean isProtocolRequest() {
        return (message == null) ? false : rmVersion.isRmProtocolRequest(getWsaAction());
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final boolean isProtocolResponse() {
        return (message == null) ? false : rmVersion.isRmProtocolResponse(getWsaAction());
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final boolean isRmFault() {
        return (message == null) ? false : rmVersion.isRmFault(getWsaAction());
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final boolean isFault() {
        return (message == null) ? false : message.isFault();
    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final boolean containsMessage() {
        return message != null;
    }

    /**
     * Provides information about value of the addressing {@code Action} header 
     * of the message in the wrapped {@link Packet} instance.
     * 
     * @return addressing {@code Action} header of the message in the wrapped {@link Packet} instance.
     */
    public final String getWsaAction() {
        checkMessageReadyState();

        return message.getHeaders().getAction(addressingVersion, soapVersion);
    }

    /**
     * Provides information about value of the addressing {@code To} header 
     * of the message in the wrapped {@link Packet} instance.
     * 
     * @return addressing {@code To} header of the message in the wrapped {@link Packet} instance.
     */
    public final String getDestination() {
        checkMessageReadyState();

        return message.getHeaders().getTo(addressingVersion, soapVersion);
    }

    /**
     * Utility method which retrieves the RM header with the specified name from the underlying {@link Message}'s 
     * {@link HeaderList) in the form of JAXB element and marks the header as understood.
     * 
     * @param name the name of the {@link com.sun.xml.ws.api.message.Header} to find.
     * 
     * @return RM header with the specified name in the form of JAXB element or {@code null} in case no such header was found
     */
    public final <T> T readHeaderAsUnderstood(String nsUri, String name) throws RxRuntimeException {
        checkMessageReadyState();

        Header header = message.getHeaders().get(nsUri, name, true);
        if (header == null) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked") T result = (T) header.readAsJAXB(jaxbUnmarshaller);
            return result;
        } catch (JAXBException ex) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1122_ERROR_MARSHALLING_RM_HEADER(nsUri + "#" + name), ex));
        }
    }

    /**
     * Unmarshalls underlying JAXWS {@link Message} using JAXB context of a configured RM version
     *  
     * @return message content unmarshalled JAXB bean
     * 
     * @throws com.sun.xml.ws.rm.RxException in case the message unmarshalling failed
     */
    public final <T> T unmarshallMessage() throws RxRuntimeException {
        checkMessageReadyState();

        try {
            @SuppressWarnings("unchecked") T result = (T) message.readPayloadAsJAXB(jaxbUnmarshaller);
            return result;
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RxRuntimeException(LocalizationMessages.WSRM_1123_ERROR_UNMARSHALLING_MESSAGE(), e));
        } finally {
            messageConsumed = true; // TODO remove this workaround
        }
    }

    /**
     * TODO javadoc
     */
    public final String getSequenceId() throws RxRuntimeException {
        if (!isSequenceDataInit) {
            initSequenceHeaderData();
            isSequenceDataInit = true;
        }
        return sequenceId;
    }

    /**
     * TODO javadoc
     */
    public final long getMessageNumber() throws RxRuntimeException {
        if (!isSequenceDataInit) {
            initSequenceHeaderData();
            isSequenceDataInit = true;
        }
        return messageNumber;
    }

    /**
     * TODO javadoc
     */
    abstract void initSequenceHeaderData() throws RxRuntimeException;

    /**
     * TODO javadoc
     */
    public final void setSequenceData(String sequenceId, long messageNumber) {
        this.sequenceId = sequenceId;
        this.messageNumber = messageNumber;
    }

    /**
     * TODO javadoc
     */
    abstract String initAckRequestedHeaderData() throws RxRuntimeException;

    /**
     * TODO javadoc
     */
    public final String getAckRequestedHeaderSequenceId() throws RxRuntimeException {
        if (!isAckRequestedHeaderDataInit) {
            ackRequestedHeaderSequenceId = initAckRequestedHeaderData();
            isAckRequestedHeaderDataInit = true;
        }

        return ackRequestedHeaderSequenceId;
    }

    /**
     * TODO javadoc
     */
    public abstract void processAcknowledgements(SequenceManager sequenceManager, String expectedAckedSequenceId) throws RxRuntimeException;

    /**
     * TODO javadoc
     */
    public Session getSession() {
        String sessionId = (String) packet.invocationProperties.get(Session.SESSION_ID_KEY);
        if (sessionId == null) {
            return null;
        }

        return SessionManager.getSessionManager().getSession(sessionId);
    }

    /**
     * TODO javadoc
     */
    public boolean hasSession() {
        return getSession() != null;
    }

    /**
     * TODO javadoc
     */
    public void setSession(String sessionId) {
        packet.invocationProperties.put(Session.SESSION_ID_KEY, sessionId);

        Session session = SessionManager.getSessionManager().getSession(sessionId);
        packet.invocationProperties.put(Session.SESSION_KEY, session.getUserData());
    }

    public void exposeSequenceDataToUser() {
        packet.invocationProperties.put(SEQUENCE_PROPERTY, sequenceId);
        packet.invocationProperties.put(MESSAGE_NUMBER_PROPERTY, messageNumber);
    }
    
    /**
     * TODO javadoc
     */
    public final String getSecurityContextTokenId() {
        Session session = getSession();
        return (session != null) ? session.getSecurityInfo().getIdentifier() : null;
    }

    /**
     * Checks internal state of this {@link PacketAdapter} instance whether it is 
     * safe to perform message read or update operations. Success of this condition guarantees
     * the success of {@link #checkPacketUpdateState()} operation.
     * 
     * @throws java.lang.IllegalStateException if the check fails
     */
    public final void checkMessageReadyState() throws IllegalStateException {
        if (message == null) {
            throw new IllegalStateException("This PacketAdapter instance does not contain a packet with a non-null message");
        }
    }

    /**
     * Checks internal state of this {@link PacketAdapter} instance whether it is 
     * safe to perform packet read or update operations. Success of this condition does not 
     * guarantee the success of {@link #checkMessageUpdateState()} operation.
     * 
     * @throws java.lang.IllegalStateException if the check fails
     */
    public final void checkPacketReadyState() throws IllegalStateException {
        if (packet == null) {
            throw new IllegalStateException("This PacketAdapter instance does not contain a packet with a non-null message");
        }
    }

    /**
     * Determines whether the security context token identifier used to secure the message 
     * wrapped in this adapter is the expected one
     *
     * @param expectedStrId expected security context token identifier 
     * @returns {code true} if the actual security context token identifier equals to the expected one
     */
    public final boolean isSecurityContextTokenIdValid(String expectedSctId) {
        String actualSctId = getSecurityContextTokenId();
        return (expectedSctId != null) ? expectedSctId.equals(actualSctId) : actualSctId == null;
    }
}
