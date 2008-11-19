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

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Transmits standalone protocol messages over the wire. Provides also some additional utility mehtods for creating and
 * unmarshalling JAXWS {@link Message} and {@link Header} objects.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class ProtocolCommunicator {

    private static final RmLogger LOGGER = RmLogger.getLogger(ProtocolCommunicator.class);

    final QName soapMustUnderstandAttributeName;
    //
    private volatile Engine fiberEngine;
    //
    private final ReadWriteLock fiberEngineLock = new ReentrantReadWriteLock();
    private final AtomicReference<Packet> musterRequestPacket;
    private final Tube tubeline;
    private final SecureConversationInitiator scInitiator;
    private final AddressingVersion addressingVersion;

    ProtocolCommunicator(Tube tubeline, SecureConversationInitiator scInitiator, AddressingVersion addressingVersion, SOAPVersion soapVersion) {
        this.tubeline = tubeline;
        this.scInitiator = scInitiator;
        this.addressingVersion = addressingVersion;
        this.soapMustUnderstandAttributeName = new QName(soapVersion.nsUri, "mustUnderstand");
        this.musterRequestPacket = new AtomicReference<Packet>();
    }

    /**
     * This method must be called before the {@link ProtocolCommunicator} is used for the first time to send a message.
     * 
     * @param muster a packet that will be used as a muster for creating new request packets used to carry the messages.
     */
    void registerMusterRequestPacket(Packet muster) {
        musterRequestPacket.set(muster);
    }

    /**
     * If security is enabled, tries to initate secured conversation and obtain the security token reference.
     * 
     * @return security token reference of the initiated secured conversation, or {@code null} if there is no SC configured
     */
    SecurityTokenReferenceType tryStartSecureConversation() {
        SecurityTokenReferenceType strType = null;
        if (scInitiator != null) {
            try {
                @SuppressWarnings("unchecked") 
                JAXBElement<SecurityTokenReferenceType> strElement = scInitiator.startSecureConversation(musterRequestPacket.get().copy(false));
                
                strType = (strElement != null) ? strElement.getValue() : null;
            } catch (WSSecureConversationException ex) {
                LOGGER.severe(LocalizationMessages.WSRM_1121_SECURE_CONVERSATION_INIT_FAILED(), ex);
            }
        }
        return strType;
    }

    /**
     * Sends the request {@link Packet} and returns the corresponding response {@link Packet}.
     * 
     * @param request {@link Packet} containing the message to be send
     * @return response {@link Message} wrapped in a response {@link Packet} received
     */
    Packet send(Packet request) {
        Fiber fiber = getFiberEngine().createFiber(); // TODO: could we possibly reuse the same fiber?

        return fiber.runSync(tubeline, request);
    }

    /**
     * Provides the destination endpoint reference this {@link ProtocolCommunicator} is pointing to. May return {@code null} 
     * in case the {@link ProtocolCommunicator} instance has not yet been initialized by a call to 
     * {@link #registerMusterRequestPacket(Packet)} method.
     * 
     * @return destination endpoint reference or {@code null} in case the {@link ProtocolCommunicator} instance has not 
     *         been initialized yet
     */
    WSEndpointReference getDestination() {
        Packet packet = musterRequestPacket.get();
        return (packet != null) ? new WSEndpointReference(packet.endpointAddress.toString(), addressingVersion) : null;
    }
        
    /**
     * Creates a new empty request packet based on the muster packet registered 
     * with this {@link ProtocolCommunicator} instance.
     * 
     * @return a new empty request packet
     */    
    Packet createEmptyRequestPacket() {
        return musterRequestPacket.get().copy(false);
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
