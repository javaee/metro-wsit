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
package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * Inbound sequence optimized for low memory footprint, fast message acknowledgement and ack range calculation optimized 
 * for standard scenario (no lost messages). This class is not reentrant.
 * 
 * TODO make class thread-safe
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class InboundSequence extends AbstractSequence {

    private static final Logger LOGGER = Logger.getLogger(InboundSequence.class);
    //
    private final Set<Long> allUnackedMessageNumbers;
    private final Set<Long> registeredUnackedMessageNumbers;

    InboundSequence(
            String sequenceId,
            String securityContextTokenId,
            long expirationTime,
            DeliveryQueueBuilder deliveryQueueBuilder) {

        super(sequenceId, securityContextTokenId, expirationTime, Sequence.UNSPECIFIED_MESSAGE_ID, deliveryQueueBuilder);

        this.allUnackedMessageNumbers = new TreeSet<Long>();
        this.registeredUnackedMessageNumbers = new HashSet<Long>();
    }

    public void registerMessage(ApplicationMessage message, boolean storeMessageFlag) throws DuplicateMessageRegistrationException, IllegalStateException {
        if (getStatus() != Sequence.Status.CREATED) {
            // TODO L10N
            throw new IllegalStateException("Wrong sequence state: " + getStatus());
        }

        if (!this.getId().equals(message.getSequenceId())) {
            throw new IllegalArgumentException(String.format(
                    "Cannot register message: sequence identifier on the application message [ %s ] " +
                    "is different from the identifier of this sequence [ %s ].",
                    message.getSequenceId(),
                    this.getId()));
        }

        try {
            messageIdLock.writeLock().lock();

            if (message.getMessageNumber() > getLastMessageId()) {
                // new message - note that this will work even for the first message that arrives
                // some message(s) got lost, add to all unacked message number set...
                for (long lostIdentifier = getLastMessageId() + 1; lostIdentifier <= message.getMessageNumber(); lostIdentifier++) {
                    allUnackedMessageNumbers.add(lostIdentifier);
                }
                updateLastMessageId(message.getMessageNumber());
            } else if (registeredUnackedMessageNumbers.contains(message.getMessageNumber())) {
                // duplicate message
                throw LOGGER.logException(new DuplicateMessageRegistrationException(this.getId(), message.getMessageNumber()), Level.FINE);
            }

            registeredUnackedMessageNumbers.add(message.getMessageNumber());

            if (storeMessageFlag) {
                storeMessage(message);
            }
        } finally {
            messageIdLock.writeLock().unlock();
        }
    }

    @Override
    Collection<Long> getUnackedMessageIdStorage() {
        return allUnackedMessageNumbers;
    }

    public void acknowledgeMessageIds(List<AckRange> ranges) throws IllegalMessageIdentifierException, IllegalStateException {
        // NOTE: This method is not meant to be used on inbound sequence in our implementation right now as we recieve 
        //       only one message at a time. Thus we don't bother optimizing it.
        for (AckRange range : ranges) {
            for (long index = range.lower; index <= range.upper; index++) {
                acknowledgeMessageId(index);
            }
        }
    }

    public void acknowledgeMessageId(long messageId) throws IllegalMessageIdentifierException, IllegalStateException {
        if (getStatus() != Sequence.Status.CREATED) {
            throw new IllegalStateException(LocalizationMessages.WSRM_1135_WRONG_SEQUENCE_STATE_ACKNOWLEDGEMENT_REJECTED(getId(), getStatus()));
        }

        try {
            messageIdLock.writeLock().lock();

            if (!registeredUnackedMessageNumbers.remove(messageId)) {
                // TODO L10N
                LOGGER.warning(String.format(
                        "Message number [ %d ] not found among the %s unacknowledged message numbers on a sequence [ %s ].",
                        messageId,
                        "registered",
                        this.getId()));
            }

            if (!allUnackedMessageNumbers.remove(messageId)) {
                // TODO L10N
                LOGGER.warning(String.format(
                        "Message number [ %d ] not found among the %s unacknowledged message numbers on a sequence [ %s ].",
                        messageId,
                        "all",
                        this.getId()));
            }
        } finally {
            messageIdLock.writeLock().unlock();
        }

        this.getDeliveryQueue().onSequenceAcknowledgement();
    }

    @Override
    protected Long getUnackedMessageIdentifierKey(long messageNumber) {
        Long msgNumberKey = null;
        Iterator<Long> iterator = registeredUnackedMessageNumbers.iterator();
        while (iterator.hasNext()) {
            msgNumberKey = iterator.next();
            if (msgNumberKey.longValue() == messageNumber) {
                break;
            }
        }

        return msgNumberKey;
    }
}
