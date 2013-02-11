/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException.Code;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.AckRange;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Outbound sequence implementation
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class OutboundSequence extends AbstractSequence {

    public static final long INITIAL_LAST_MESSAGE_ID = Sequence.MIN_MESSAGE_ID - 1;
    private static final Logger LOGGER = Logger.getLogger(OutboundSequence.class);

    public OutboundSequence(SequenceData data, DeliveryQueueBuilder deliveryQueueBuilder, TimeSynchronizer timeSynchronizer) {
        super(data, deliveryQueueBuilder, timeSynchronizer);
    }

    public void registerMessage(ApplicationMessage message, boolean storeMessageFlag) throws DuplicateMessageRegistrationException, AbstractSoapFaultException {
        this.getState().verifyAcceptingMessageRegistration(getId(), Code.Sender);

        if (message.getSequenceId() != null) {
            throw new IllegalArgumentException(String.format(
                    "Cannot register message: Application message has been already registered on a sequence [ %s ].",
                    message.getSequenceId()));
        }

        message.setSequenceData(this.getId(), generateNextMessageId());
        if (storeMessageFlag) {
            data.attachMessageToUnackedMessageNumber(message);
        }
    }

    private long generateNextMessageId() throws MessageNumberRolloverException, IllegalStateException, DuplicateMessageRegistrationException {
        long nextId = data.incrementAndGetLastMessageNumber(true);

        if (nextId > Sequence.MAX_MESSAGE_ID) {
            throw LOGGER.logSevereException(new MessageNumberRolloverException(getId(), nextId));
        }

        return nextId;
    }

    public void acknowledgeMessageNumber(long messageId) {
        throw new UnsupportedOperationException(LocalizationMessages.WSRM_1101_UNSUPPORTED_OPERATION(this.getClass().getName()));
    }

    public void acknowledgeMessageNumbers(List<AckRange> ranges) throws InvalidAcknowledgementException, AbstractSoapFaultException {
        this.getState().verifyAcceptingAcknowledgement(getId(), Code.Sender);

        if (ranges == null || ranges.isEmpty()) {
            return;
        }

        AckRange.sort(ranges);

        // check proper bounds of acked ranges
        AckRange lastAckRange = ranges.get(ranges.size() - 1);
        if (data.getLastMessageNumber() < lastAckRange.upper) {
            throw new InvalidAcknowledgementException(this.getId(), lastAckRange.upper, ranges);
        }

        final Collection<Long> unackedMessageNumbers = data.getUnackedMessageNumbers();
        if (unackedMessageNumbers.isEmpty()) {
            // we have checked the ranges are ok and there's nothing to acknowledge.
            return;
        }

        // acknowledge messages
        Iterator<AckRange> rangeIterator = ranges.iterator();
        AckRange currentRange = rangeIterator.next();

        for (long unackedMessageNumber : unackedMessageNumbers) {
            if (unackedMessageNumber >= currentRange.lower && unackedMessageNumber <= currentRange.upper) {
                data.markAsAcknowledged(unackedMessageNumber);
            } else if (rangeIterator.hasNext()) {
                currentRange = rangeIterator.next();
            } else {
                break; // no more acked ranges
                }
        }

        this.getDeliveryQueue().onSequenceAcknowledgement();
    }
}
