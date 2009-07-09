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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException.Code;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.List;

/**
 * Inbound sequence optimized for low memory footprint, fast message acknowledgement and ack range calculation optimized 
 * for standard scenario (no lost messages). This class is not reentrant.
 * 
 * TODO make class thread-safe
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class InboundSequence extends AbstractSequence {
    public static final long INITIAL_LAST_MESSAGE_ID = Sequence.UNSPECIFIED_MESSAGE_ID;

    private static final Logger LOGGER = Logger.getLogger(InboundSequence.class);
    //
//    private final Set<Long> allUnackedMessageNumbers;
//    private final Set<Long> receivedUnackedMessageNumbers;

    public InboundSequence(SequenceData data, DeliveryQueueBuilder deliveryQueueBuilder, TimeSynchronizer timeSynchronizer) {
        // super(sequenceId, securityContextTokenId, expirationTime, Sequence.UNSPECIFIED_MESSAGE_ID, deliveryQueueBuilder);
        super(data, deliveryQueueBuilder, timeSynchronizer);
//
//        this.allUnackedMessageNumbers = new TreeSet<Long>();
//        this.receivedUnackedMessageNumbers = new HashSet<Long>();
    }

    public void registerMessage(ApplicationMessage message, boolean storeMessageFlag) throws DuplicateMessageRegistrationException, IllegalStateException {
        checkSequenceCreatedStatus(
                    "Cannot register message: This sequence is not in ready to accept messages.",
                    Code.Receiver); // TODO P2 message

        if (!this.getId().equals(message.getSequenceId())) {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalArgumentException(String.format(
                    "Cannot register message: sequence identifier on the application message [ %s ] " +
                    "is different from the identifier of this sequence [ %s ].",
                    message.getSequenceId(),
                    this.getId())));
        }

        try {
            data.lockWrite();

            if (message.getMessageNumber() > data.getLastMessageNumber()) {
                // new message - note that this will work even for the first message that arrives
                // some message(s) got lost, add to all unacked message number set...
                for (long lostIdentifier = data.getLastMessageNumber() + 1; lostIdentifier <= message.getMessageNumber(); lostIdentifier++) {
                    data.registerUnackedMessageNumber(lostIdentifier, false);
//                    allUnackedMessageNumbers.add(lostIdentifier);
                }
                data.setLastMessageNumber(message.getMessageNumber());
//            } else if (receivedUnackedMessageNumbers.contains(message.getMessageNumber())) {
                // duplicate message
//                throw LOGGER.logException(new DuplicateMessageRegistrationException(this.getId(), message.getMessageNumber()), Level.FINE);
            }

            data.registerUnackedMessageNumber(message.getMessageNumber(), true);
//            receivedUnackedMessageNumbers.add(message.getMessageNumber());

            if (storeMessageFlag) {
                data.attachMessageToUnackedMessageNumber(message);
            }
        } finally {
            data.unlockWrite();
        }
    }

//    @Override
//    Collection<Long> getUnackedMessageNumbers() {
//        return allUnackedMessageNumbers;
//    }

    public void acknowledgeMessageNumbers(List<AckRange> ranges) {
        // TODO L10N
        throw new UnsupportedOperationException(String.format("This operation is not supported on %s class", this.getClass().getName()));
    }

    public void acknowledgeMessageNumber(long messageId) throws IllegalMessageIdentifierException, IllegalStateException {
        checkSequenceCreatedStatus(LocalizationMessages.WSRM_1135_WRONG_SEQUENCE_STATE_ACKNOWLEDGEMENT_REJECTED(getId(), getState()), Code.Receiver);

        try {
            data.lockWrite();

            data.markAsAcknowledged(messageId);

//            if (!registeredUnackedMessageNumbers.remove(messageId)) {
//                throw LOGGER.logSevereException(new IllegalMessageIdentifierException(getId(), messageId));
//            }
//
//            boolean removedFromAll = allUnackedMessageNumbers.remove(messageId);
//            assert removedFromAll;

        } finally {
            data.unlockWrite();
        }

        this.getDeliveryQueue().onSequenceAcknowledgement();
    }
}
