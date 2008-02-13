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
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.security.secconv.WSSecureConversationException;
import com.sun.xml.ws.security.secext10.SecurityTokenReferenceType;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.xml.bind.JAXBElement;

/**
 * Transmits standalone protocol messages over the wire.
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class ProtocolCommunicator {

    private static final RmLogger LOGGER = RmLogger.getLogger(ProtocolCommunicator.class);
    private final Tube tubeline;
    private final AddressingVersion addressingVersion;
    private final SOAPVersion soapVersion;
    private Packet musterRequestPacket;
    private final SecureConversationInitiator scInitiator;
    private volatile Engine fiberEngine;
    private final ReadWriteLock fiberEngineLock = new ReentrantReadWriteLock();

    public ProtocolCommunicator(Tube tubeline, SecureConversationInitiator scInitiator, SOAPVersion soapVersion, AddressingVersion addressingVersion) {
        this.tubeline = tubeline;
        this.scInitiator = scInitiator;
        this.soapVersion = soapVersion;
        this.addressingVersion = addressingVersion;
    }

    /**
     * This method must be called before the {@link ProtocolCommunicator} is used for the first time to send a message.
     * 
     * @param muster a packet that will be used as a muster for creating new request packets used to carry the messages.
     */
    public void registerMusterRequestPacket(Packet muster) {
        musterRequestPacket = muster;
    }

    /**
     * Creates a new request packet and wraps a {@link Message} instance into it
     * 
     * @param message nullable, {@link Message} instance to be wrapped into the packet
     * @return a new request packet that wraps given {@link Message} instance
     */
    private Packet createPacket(Message message, String action) {
        Packet newPacket = musterRequestPacket.copy(false);
        newPacket.setMessage(message);

        /*ADDRESSING FIX_ME
        Current API does not allow assignment of non-anon reply to, if we
        need to support non-anon acksTo.
         */
        message.assertOneWay(false); // TODO do we really need to call this assert here?
        message.getHeaders().fillRequestAddressingHeaders(
                newPacket,
                addressingVersion,
                soapVersion,
                false,
                action);

        return newPacket;
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
                strElement = scInitiator.startSecureConversation(musterRequestPacket.copy(false));
            } catch (WSSecureConversationException ex) {
                // TODO L10N
                LOGGER.severe("Unable to start secure conversation", ex);
            }
        }
        return (strElement != null) ? strElement.getValue() : null;
    }

    /**
     * Sends protocol request message and returns the corresponding response message.
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
        return message.getHeaders().getAction(addressingVersion, soapVersion);
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
