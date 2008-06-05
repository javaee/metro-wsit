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
package com.sun.xml.ws.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.RmRuntimeException;
import com.sun.xml.ws.rm.RmVersion;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.rm.runtime.sequence.Sequence.AckRange;
import com.sun.xml.ws.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.wss.impl.MessageConstants;
import java.net.URI;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author m_potociar
 */
public abstract class PacketAdapter {

    private static final RmLogger LOGGER = RmLogger.getLogger(PacketAdapter.class);
    protected Packet packet;
    protected Message message;
    private boolean isSequenceDataInit;
    private boolean isAckRequestedHeaderDataInit;
    private String sequenceId;
    private String ackRequestedHeaderSequenceId;
    private long messageNumber;
    private final Configuration configuration;
    private final RmVersion rmVersion;
    private final SOAPVersion soapVersion;
    private final AddressingVersion addressingVersion;

//    /**
//     * Creates a new packet adapter based on the configuration. This adapter is empty
//     * and does not contain any underlying packet yet. To attach the packet to the
//     * created {@link PacketAdapter} instance, use {@link #attach(com.sun.xml.ws.api.message.Packet)}
//     * method.
//     * 
//     * @param configuration configuration used to configure newly created packet
//     * @return new empty {@link PacketAdapter} instance
//     */
//    public static PacketAdapter create(@NotNull Configuration configuration) {
//        switch (configuration.getRmVersion()) {
//            case WSRM10:
//                return new Rm10PacketAdapter(configuration);
//            case WSRM11:
//                return new Rm11PacketAdapter(configuration);
//            default:
//                throw new IllegalStateException(LocalizationMessages.WSRM_1104_RM_VERSION_NOT_SUPPORTED(configuration.getRmVersion().namespaceUri));
//        }
//    }

    /**
     * Creates a new packet adapter based on the configuration and attaches a provided 
     * {@code packet} instance to it. To detach the packet from the
     * created {@link PacketAdapter} instance, use {@link #detach()}
     * method.
     * 
     * @param configuration configuration used to configure newly created packet
     * @param packet {@link Packet} instance to be attached to the newly created packet adapter
     * @return new empty {@link PacketAdapter} instance
     */
    public static PacketAdapter create(@NotNull Configuration configuration, @NotNull Packet packet) {
        switch (configuration.getRmVersion()) {
            case WSRM10:
                return new Rm10PacketAdapter(configuration, packet);
            case WSRM11:
                return new Rm11PacketAdapter(configuration, packet);
            default:
                throw new IllegalStateException(LocalizationMessages.WSRM_1104_RM_VERSION_NOT_SUPPORTED(configuration.getRmVersion().namespaceUri));
        }
    }

    /**
     * TODO javadoc
     */
    protected PacketAdapter(@NotNull Configuration configuration, @NotNull Packet packet) {
        this.configuration = configuration;
        
        // cache frequently accessed config data
        this.rmVersion = configuration.getRmVersion();
        this.soapVersion = configuration.getSoapVersion();
        this.addressingVersion = configuration.getAddressingVersion();

        insertPacket(packet);
    }

    private final void insertPacket(Packet packet) {
        this.packet = packet;
        if (packet.getMessage() != null) {
            this.message = packet.getMessage();
        }
        
    }
    
//    /**
//     * TODO javadoc
//     */
//    public PacketAdapter attach(@NotNull Packet packet) {
//        this.packet = packet;
//        if (packet.getMessage() != null) {
//            this.message = packet.getMessage();
//        }
//
//        return this;
//    }

    /**
     * TODO javadoc
     * 
     * @return
     */
    public final void consume() {
        if (message != null) {
            message.consume();
        }
    }

    /**
     * TODO javadoc
     */
    public final Packet getPacket() {
        try {
            return packet;
        } finally {
            packet = null;
            message = null;
        }
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
    public final PacketAdapter createServerResponseAdapter(Object jaxbElement, String wsaAction) {
        return PacketAdapter.create(configuration, packet.createServerResponse(
                Messages.create(rmVersion.jaxbContext, jaxbElement, soapVersion),
                addressingVersion,
                soapVersion,
                wsaAction));        
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

        message.getHeaders().add(Headers.create(rmVersion.jaxbContext, jaxbHeaderContent));
    }

    /**
     * TODO javadoc
     */
    public abstract void appendSequenceHeader(@NotNull String sequenceId, long messageNumber) throws RmRuntimeException;

    /**
     * TODO javadoc
     */
    public abstract void appendAckRequestedHeader(@NotNull String sequenceId) throws RmRuntimeException;

    /**
     * TODO javadoc
     */
    public abstract void appendSequenceAcknowledgementHeader(@NotNull String sequenceId, List<AckRange> acknowledgedIndexes) throws RmRuntimeException;

    /**
     * TODO javadoc
     * 
     * Creates a new JAX-WS {@link Message} object that doesn't have any payload
     * and sets it as the current packet content.
     * 
     * @param wsaAction WS-Addressing action header to set 
     * 
     * @return the updated {@link PacketAdapter} instance
     */
    public final PacketAdapter setEmptyMessage(String wsaAction) {
        return setMessage(Messages.createEmpty(soapVersion), wsaAction);
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
    public final PacketAdapter setMessage(Object jaxbElement, String wsaAction) {
        return setMessage(Messages.create(rmVersion.jaxbContext, jaxbElement, soapVersion), wsaAction);
    }

    /**
     * TODO javadoc
     * 
     * @param message
     * @param wsaAction
     * @return
     */
    public final PacketAdapter setMessage(Message newMessage, String wsaAction) {
        checkPacketReadyState();

        this.packet.setMessage(newMessage);
        this.message = newMessage;

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
        return message == null;
    }

    /**
     * Provides information about value of the addressing {@code Action} header 
     * of the message in the wrapped {@link Packet} instance.
     * 
     * @return addressing {@code Action} header of the message in the wrapped {@link Packet} instance.
     */
    public String getWsaAction() {
        checkMessageReadyState();

        return message.getHeaders().getAction(addressingVersion, soapVersion);
    }

    /**
     * Provides information about value of the addressing {@code To} header 
     * of the message in the wrapped {@link Packet} instance.
     * 
     * @return addressing {@code To} header of the message in the wrapped {@link Packet} instance.
     */
    public String getDestination() {
        checkMessageReadyState();

        return message.getHeaders().getTo(addressingVersion, soapVersion);
    }
    
    /**
     * Utility method which retrieves the RM header with the specified name from the underlying {@link Message}'s 
     * {@link HeaderList) in the form of JAXB element and marks the header as understood.
     * 
     * @param headers list of message headers; must not be {@code null}
     * 
     * @param name the name of the {@link com.sun.xml.ws.api.message.Header} to find.
     * 
     * @return RM header with the specified name in the form of JAXB element or {@code null} in case no such header was found
     */
    public final <T> T readHeaderAsUnderstood(String name) throws RmRuntimeException {
        Header header = message.getHeaders().get(rmVersion.namespaceUri, name, true);
        if (header == null) {
            return (T) null;
        }

        try {
            return (T) header.readAsJAXB(rmVersion.jaxbUnmarshaller);
        } catch (JAXBException ex) {
            throw LOGGER.logSevereException(new RmRuntimeException(LocalizationMessages.WSRM_1122_ERROR_MARSHALLING_RM_HEADER(rmVersion.namespaceUri + "#" + name), ex));
        }
    }

    /**
     * Unmarshalls underlying JAXWS {@link Message} using JAXB context of a configured RM version
     *  
     * @return message content unmarshalled JAXB bean
     * 
     * @throws com.sun.xml.ws.rm.RmException in case the message unmarshalling failed
     */
    public final <T> T unmarshallMessage() throws RmRuntimeException {
        checkMessageReadyState();

        try {
            return (T) message.readPayloadAsJAXB(rmVersion.jaxbUnmarshaller);
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RmRuntimeException(LocalizationMessages.WSRM_1123_ERROR_UNMARSHALLING_MESSAGE(), e));
        }
    }

    public String getSequenceId() throws RmRuntimeException {
        if (!isSequenceDataInit) {
            initSequenceHeaderData();
            isSequenceDataInit = true;
        }
        return sequenceId;
    }

    public long getMessageNumber() throws RmRuntimeException {
        if (!isSequenceDataInit) {
            initSequenceHeaderData();
            isSequenceDataInit = true;
        }
        return messageNumber;
    }

    protected abstract void initSequenceHeaderData() throws RmRuntimeException;

    protected final void setSequenceData(String sequenceId, long messageNumber) {
        this.sequenceId = sequenceId;
        this.messageNumber = messageNumber;
    }

    protected abstract String initAckRequestedHeaderData() throws RmRuntimeException;

    public final String getAckRequestedHeaderSequenceId() throws RmRuntimeException {
        if (!isAckRequestedHeaderDataInit) {
            ackRequestedHeaderSequenceId = initAckRequestedHeaderData();
            isAckRequestedHeaderDataInit = true;
        }

        return ackRequestedHeaderSequenceId;
    }

    public abstract void processAcknowledgements(SequenceManager sequenceManager, String expectedAckedSequenceId) throws RmRuntimeException;

    /**
     * TODO javadoc
     */
    public String getSecurityContextTokenId() {
        com.sun.xml.ws.security.SecurityContextToken sct = (com.sun.xml.ws.security.SecurityContextToken) packet.invocationProperties.get(com.sun.xml.wss.impl.MessageConstants.INCOMING_SCT);
        return (sct != null) ? sct.getIdentifier().toString() : null;
    }

    /**
     * Checks internal state of this {@link PacketAdapter} instance whether it is 
     * safe to perform message update operations. Success of this condition guarantees
     * the success of {@link #checkPacketUpdateState()} operation.
     * 
     * @throws java.lang.IllegalStateException if the check fails
     */
    protected final void checkMessageReadyState() throws IllegalStateException {
        if (message == null) {
            throw new IllegalStateException("This PacketAdapter instance does not contain a packet with a non-null message");
        }
    }

    /**
     * Checks internal state of this {@link PacketAdapter} instance whether it is 
     * safe to perform packet update operations. Success of this condition does not 
     * guarantee the success of {@link #checkMessageUpdateState()} operation.
     * 
     * @throws java.lang.IllegalStateException if the check fails
     */
    protected final void checkPacketReadyState() throws IllegalStateException {
        if (packet == null) {
            throw new IllegalStateException("This PacketAdapter instance does not contain a packet with a non-null message");
        }
    }

    /**
     * Determines whether the security token reference used to secure the message 
     * wrapped in this adapter is the expected one
     *
     * @param expectedStrId expected security token reference identifier 
     * @returns {code true} if the actual STR identifier equals to the expected one
     */
    private boolean checkSecurityTokenReferenceId(String expectedStrId) {
        SecurityContextToken sct = (SecurityContextToken) packet.invocationProperties.get(MessageConstants.INCOMING_SCT);
        if (sct != null) {
            URI sctIdentifierUri = sct.getIdentifier();
            return (sctIdentifierUri != null) ? sctIdentifierUri.toString().equals(expectedStrId) : expectedStrId == null;
        } else {
            return expectedStrId == null;
        }
    }
}
