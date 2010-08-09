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

import com.sun.istack.NotNull;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueue;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides abstract sequence implementation common to both - inbound and outbound
 * sequence
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class AbstractSequence implements Sequence {

    protected final SequenceData data;
    private final DeliveryQueue deliveryQueue;
    private final TimeSynchronizer timeSynchronizer;

    /**
     * Initializes instance fields.
     * 
     * @param id sequence identifier
     * 
     * @param securityContextTokenId security context token identifier bound to this sequence
     * 
     * @param expirationTime sequence expiration time
     */
    @SuppressWarnings("LeakingThisInConstructor")
    AbstractSequence(@NotNull SequenceData data, @NotNull DeliveryQueueBuilder deliveryQueueBuilder, @NotNull TimeSynchronizer timeSynchronizer) {
        assert data != null;
        assert deliveryQueueBuilder != null;
        assert timeSynchronizer != null;


        this.data = data;
        this.timeSynchronizer = timeSynchronizer;

        deliveryQueueBuilder.sequence(this);
        this.deliveryQueue = deliveryQueueBuilder.build();
    }

    public String getId() {
        return data.getSequenceId();
    }

    public String getBoundSecurityTokenReferenceId() {
        return data.getBoundSecurityTokenReferenceId();
    }

    public long getLastMessageNumber() {
        return data.getLastMessageNumber();
    }

    public List<AckRange> getAcknowledgedMessageNumbers() {
        List<Long> values = data.getLastMessageNumberWithUnackedMessageNumbers();

        final long lastMessageNumber = values.remove(0);
        final List<Long> unackedMessageNumbers = values;

        if (lastMessageNumber == Sequence.UNSPECIFIED_MESSAGE_ID) {
            // no message associated with the sequence yet
            return Collections.emptyList();
        } else if (unackedMessageNumbers.isEmpty()) {
            // no unacked indexes - we have a single acked range
            return Arrays.asList(new AckRange(Sequence.MIN_MESSAGE_ID, lastMessageNumber));
        } else {
            // need to calculate ranges from the unacked indexes
            List<AckRange> result = new LinkedList<Sequence.AckRange>();

            long lastBottomAckRange = Sequence.MIN_MESSAGE_ID;
            for (long lastUnacked : unackedMessageNumbers) {
                if (lastBottomAckRange < lastUnacked) {
                    result.add(new AckRange(lastBottomAckRange, lastUnacked - 1));
                }
                lastBottomAckRange = lastUnacked + 1;
            }
            if (lastBottomAckRange <= lastMessageNumber) {
                result.add(new AckRange(lastBottomAckRange, lastMessageNumber));
            }

            return result;
        }
    }

    public boolean hasUnacknowledgedMessages() {
        return !data.getUnackedMessageNumbers().isEmpty();
    }

    public State getState() {
        return data.getState();
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
        data.setLastAcknowledgementRequestTime(timeSynchronizer.currentTimeInMillis());
    }

    public long getLastActivityTime() {
        return data.getLastActivityTime();
    }

    public boolean isStandaloneAcknowledgementRequestSchedulable(long delayPeriod) {
        return timeSynchronizer.currentTimeInMillis() - data.getLastAcknowledgementRequestTime() > delayPeriod && hasUnacknowledgedMessages();
    }

    public void close() {
        data.setState(State.CLOSED);
        deliveryQueue.close();
    }

    public boolean isClosed() {
        State currentStatus = data.getState();
        return currentStatus == State.CLOSING || currentStatus == State.CLOSED || currentStatus == State.TERMINATING;
    }

    public boolean isExpired() {
        return (data.getExpirationTime() == Sequence.NO_EXPIRY) ? false : timeSynchronizer.currentTimeInMillis() > data.getExpirationTime();
    }

    public void preDestroy() {
        data.setState(State.TERMINATING);

        // nothing else to do...
    }
    
    public ApplicationMessage retrieveMessage(String correlationId) {
        return data.retrieveMessage(correlationId);
    }

    public DeliveryQueue getDeliveryQueue() {
        return deliveryQueue;
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
