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

import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueue;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO javadoc
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class AbstractSequence implements Sequence {

    protected final SequenceData data;
    private final DeliveryQueue deliveryQueue;

    /**
     * Initializes instance fields.
     * 
     * @param id sequence identifier
     * 
     * @param securityContextTokenId security context token identifier bound to this sequence
     * 
     * @param expirationTime sequence expiration time
     */
    AbstractSequence(SequenceData data, DeliveryQueueBuilder deliveryQueueBuilder) {
        this.data = data;

        // TODO initialize delivery queue
        deliveryQueueBuilder.sequence(this);
        this.deliveryQueue = deliveryQueueBuilder.build();
    }

    public String getId() {
        return data.getSequenceId();
    }

    public String getBoundSecurityTokenReferenceId() {
        return data.getBoundSecurityTokenReferenceId();
    }

    public long getLastMessageId() {
        return data.getLastMessageId();
    }

    public List<AckRange> getAcknowledgedMessageIds() {
        data.lockRead();
        try {
            if (getLastMessageId() == Sequence.UNSPECIFIED_MESSAGE_ID) {
                // no message associated with the sequence yet
                return Collections.emptyList();
            } else if (getUnackedMessageIdStorage().isEmpty()) {
                // no unacked indexes - we have a single acked range
                return Arrays.asList(new AckRange(Sequence.MIN_MESSAGE_ID, getLastMessageId()));
            } else {
                // need to calculate ranges from the unacked indexes
                List<AckRange> result = new LinkedList<Sequence.AckRange>();

                Iterator<Long> unackedIndexIterator = getUnackedMessageIdStorage().iterator();
                long lastBottomAckRange = Sequence.MIN_MESSAGE_ID;
                while (unackedIndexIterator.hasNext()) {
                    long lastUnacked = unackedIndexIterator.next();
                    if (lastBottomAckRange < lastUnacked) {
                        result.add(new AckRange(lastBottomAckRange, lastUnacked - 1));
                    }
                    lastBottomAckRange = lastUnacked + 1;
                }
                if (lastBottomAckRange <= getLastMessageId()) {
                    result.add(new AckRange(lastBottomAckRange, getLastMessageId()));
                }



                return result;
            }
        } finally {
            data.unlockRead();
        }
    }

    public boolean isAcknowledged(long messageId) {
        try {
            data.lockRead();
            if (messageId > getLastMessageId()) {
                return false;
            }

            return !getUnackedMessageIdStorage().contains(messageId);
        } finally {
            data.unlockRead();
        }
    }

    public boolean hasUnacknowledgedMessages() {
        try {
            data.lockRead();
            return !getUnackedMessageIdStorage().isEmpty();
        } finally {
            data.unlockRead();
        }
    }

    public State getState() {
        return data.getState();
    }

    public void setState(State newState) {
        this.data.setState(newState);
    }

    public void setAckRequestedFlag() {
        data.setAckRequestedFlag(true);
    }

    public void clearAckRequestedFlag() {
        data.setAckRequestedFlag(false);
    }

    public boolean isAckRequested() {
        return data.getAckRequestedFlag();
    }

    public void updateLastAcknowledgementRequestTime() {
        data.setLastAcknowledgementRequestTime(System.currentTimeMillis());
    }

    public long getLastActivityTime() {
        return data.getLastActivityTime();
    }

    public void updateLastActivityTime() {
        data.setLastActivityTime(System.currentTimeMillis());
    }

    public boolean isStandaloneAcknowledgementRequestSchedulable(long delayPeriod) {
        return System.currentTimeMillis() - data.getLastAcknowledgementRequestTime() > delayPeriod && hasUnacknowledgedMessages();
    }

    public void close() {
        data.setState(State.CLOSED);
    }

    public boolean isClosed() {
        State currentStatus = data.getState();
        return currentStatus == State.CLOSING || currentStatus == State.CLOSED || currentStatus == State.TERMINATING;
    }

    public boolean isExpired() {
        return (data.getExpirationTime() == Sequence.NO_EXPIRATION) ? false : System.currentTimeMillis() < data.getExpirationTime();
    }

    public void preDestroy() {
        // nothing to do...
    }

    abstract Collection<Long> getUnackedMessageIdStorage();

    public final void storeMessage(ApplicationMessage message, Long msgNumberKey) throws UnsupportedOperationException {
        data.storeMessage(message, msgNumberKey);
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        return data.retrieveMessage(correlationId);
    }

    public ApplicationMessage retrieveUnackedMessage(long messageNumber) {
        return data.retrieveUnackedMessage(messageNumber);
    }

    public DeliveryQueue getDeliveryQueue() {
        try {
            data.lockRead();

            return deliveryQueue;
        } finally {
            data.unlockRead();
        }
    }

    protected final void checkSequenceCreatedStatus(String message, AbstractSoapFaultException.Code code) throws AbstractSoapFaultException {
        switch (getState()) {
            case CLOSING:
            case CLOSED:
                throw new SequenceClosedException(message);
            case TERMINATING:
                throw new SequenceTerminatedException(message, code);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final AbstractSequence other = (AbstractSequence) obj;
        if ((this.data.getSequenceId() == null) ? (other.data.getSequenceId() != null) : !this.data.getSequenceId().equals(other.data.getSequenceId())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.data.getSequenceId() != null ? this.data.getSequenceId().hashCode() : 0);
        return hash;
    }
}
