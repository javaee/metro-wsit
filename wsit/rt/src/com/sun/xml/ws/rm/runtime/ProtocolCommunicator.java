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

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.rm.RmException;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.Configuration;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

/**
 * Transmits standalone protocol messages over the wire. Provides also some additional utility mehtods for creating and
 * unmarshalling JAXWS {@link Message} and {@link Header} objects.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ProtocolCommunicator {

    private static final RmLogger LOGGER = RmLogger.getLogger(ProtocolCommunicator.class);
    private volatile Engine fiberEngine;
    private final ReadWriteLock fiberEngineLock = new ReentrantReadWriteLock();
    private final AtomicReference<Packet> musterRequestPacket;
    public final QName soapMustUnderstandAttributeName;
    private final Tube tubeline;
    private final SecureConversationInitiator scInitiator;
    private final Configuration configuration;

    public ProtocolCommunicator(Tube tubeline, SecureConversationInitiator scInitiator, Configuration configuration) {
        this.tubeline = tubeline;
        this.scInitiator = scInitiator;
        if (configuration.getAddressingVersion() == AddressingVersion.W3C) {
            this.configuration = configuration;
        } else {
            throw LOGGER.logSevereException(new IllegalStateException(LocalizationMessages.WSRM_1120_UNSUPPORTED_WSA_VERSION()));
        }
        this.soapMustUnderstandAttributeName = new QName(configuration.getSoapVersion().nsUri, "mustUnderstand");
        this.musterRequestPacket = new AtomicReference<Packet>();
    }

    /**
     * This method must be called before the {@link ProtocolCommunicator} is used for the first time to send a message.
     * 
     * @param muster a packet that will be used as a muster for creating new request packets used to carry the messages.
     */
    public void registerMusterRequestPacket(Packet muster) {
        musterRequestPacket.set(muster);
    }

    /**
     * If security is enabled, tries to initate secured conversation and obtain the security token reference.
     * 
     * @return security token reference of the initiated secured conversation, or {@code null} if there is no SC configured
     */
    public SecurityTokenReferenceType tryStartSecureConversation() {
        JAXBElement<SecurityTokenReferenceType> strElement = null;
        if (scInitiator != null) {
            try {
                strElement = scInitiator.startSecureConversation(musterRequestPacket.get().copy(false));
            } catch (WSSecureConversationException ex) {
                LOGGER.severe(LocalizationMessages.WSRM_1121_SECURE_CONVERSATION_INIT_FAILED(), ex);
            }
        }
        return (strElement != null) ? strElement.getValue() : null;
    }

    /**
     * Creates a new JAX-WS {@link Message} object backed by a JAXB bean using JAXB context of a configured RM version.
     *
     * @param jaxbObject
     *      The JAXB object that represents the payload. must not be null. This object
     *      must be bound to an element (which means it either is a {@link JAXBElement} or
     *      an instanceof a class with {@link XmlRootElement}).
     * 
     * @return new JAX-WS {@link Message} object backed by a JAXB bean
     */
    public Message createMessage(Object jaxbElement) {
        return Messages.create(configuration.getRmVersion().jaxbContext, jaxbElement, configuration.getSoapVersion());
    }

    /**
     * Creates a new JAX-WS {@link Message} object that doesn't have any payload.
     * 
     * @return new JAX-WS {@link Message} object with no payload
     */
    public Message createEmptyMessage() {
        return Messages.createEmpty(configuration.getSoapVersion());
    }

    /**
     * Utility method which creates a RM {@link Header} with the specified JAXB bean content
     * 
     * @param jaxbHeaderContent content of the newly created {@link Header}
     * 
     * @return created RM {@link Header} with the specified JAXB bean content
     */
    protected final Header createHeader(Object jaxbHeaderContent) {
        return Headers.create(configuration.getRmVersion().jaxbContext, jaxbHeaderContent);
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
    public final <T> T readHeaderAsUnderstood(HeaderList headers, String name) throws RmException {
        Header header = headers.get(configuration.getRmVersion().namespaceUri, name, true);
        if (header == null) {
            return (T) null;
        }

        try {
            return (T) header.readAsJAXB(configuration.getRmVersion().jaxbUnmarshaller);
        } catch (JAXBException ex) {
            throw LOGGER.logSevereException(new RmException(LocalizationMessages.WSRM_1122_ERROR_MARSHALLING_RM_HEADER(configuration.getRmVersion().namespaceUri + "#" + name), ex));
        }
    }

    /**
     * Unmarshalls given JAXWS {@link Message} using JAXB context of a configured RM version
     * 
     * @param message JAXWS {@link Message} to be unmarshalled
     * 
     * @return message content unmarshalled JAXB bean
     * 
     * @throws com.sun.xml.ws.rm.RmException in case the message unmarshalling failed
     */
    public final <T> T unmarshallMessage(Message message) throws RmException {
        try {
            return (T) message.readPayloadAsJAXB(configuration.getRmVersion().jaxbUnmarshaller);
        } catch (JAXBException e) {
            throw LOGGER.logSevereException(new RmException(LocalizationMessages.WSRM_1123_ERROR_UNMARSHALLING_MESSAGE(), e));
        }
    }

    /**
     * Sends the request message and returns the corresponding response message.
     * 
     * @param requestMessage message to send
     * @return response message received
     */
    public Message send(Message requestMessage, String action) {
        Fiber fiber = getFiberEngine().createFiber(); // TODO: could we possibly reuse the same fiber?
        Packet responsePacket = fiber.runSync(tubeline, createPacket(requestMessage, action));
        return responsePacket.getMessage();
    }

    /**
     * Provides information about value of the addressing {@code Action} header of the message
     * 
     * @param message to be inspected
     * @return addressing {@code Action} header of the message
     */
    public String getAction(Message message) {
        return message.getHeaders().getAction(configuration.getAddressingVersion(), configuration.getSoapVersion());
    }

    /**
     * Provides the destination endpoint reference this {@link ProtocolCommunicator} is pointing to. May return {@code null} 
     * in case the {@link ProtocolCommunicator} instance has not yet been initialized by a call to 
     * {@link #registerMusterRequestPacket(Packet)} method.
     * 
     * @return destination endpoint reference or {@code null} in case the {@link ProtocolCommunicator} instance has not 
     *         been initialized yet
     */
    public WSEndpointReference getDestination() {
        Packet packet = musterRequestPacket.get();
        return (packet != null) ? new WSEndpointReference(packet.endpointAddress.toString(), configuration.getAddressingVersion()) : null;
    }

    /**
     * Creates a new request packet and wraps a {@link Message} instance into it
     * 
     * @param message nullable, {@link Message} instance to be wrapped into the packet
     * @return a new request packet that wraps given {@link Message} instance
     */
    private Packet createPacket(Message message, String action) {
        Packet newPacket = musterRequestPacket.get().copy(false);
        newPacket.setMessage(message);

        message.assertOneWay(false); // TODO do we really need to call this assert here?
        message.getHeaders().fillRequestAddressingHeaders(
                newPacket,
                configuration.getAddressingVersion(),
                configuration.getSoapVersion(),
                false,
                action);

        return newPacket;
    }

    private Engine getFiberEngine() {
        try {
            fiberEngineLock.readLock().lock();
            if (fiberEngine == null) {
                fiberEngineLock.readLock().unlock();
                try {
                    fiberEngineLock.writeLock().lock();
                    if (fiberEngine == null) {
                        fiberEngine = Fiber.current().owner;
                    }
                } finally {
                    fiberEngineLock.readLock().lock();
                    fiberEngineLock.writeLock().unlock();
                }
            }
            return fiberEngine;
        } finally {
            fiberEngineLock.readLock().unlock();
        }
    }
}
