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
import com.sun.istack.Nullable;
import com.sun.xml.ws.rx.rm.faults.AbstractSoapFaultException;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueue;
import java.util.List;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
@ManagedObject
@Description("RM Sequence")
public interface Sequence {

    public static final long UNSPECIFIED_MESSAGE_ID = 0; // this MUST be 0 in order for AbstractSequence.createAckRanges() method to work properly
    public static final long MIN_MESSAGE_ID = 1;
    public static final long MAX_MESSAGE_ID = 9223372036854775807L;
    public static final long NO_EXPIRY = -1;
    
    public static enum State {
        // CREATING(10) not needed
        CREATED(15),
        CLOSING(20),
        CLOSED(25),
        TERMINATING(30);

        private int value;
        
        private State(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static State valueToStatus(int value) {
            for (State status : State.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            
            return null;
        }
    }

    public static enum IncompleteSequenceBehavior {
        /**
         * The default value which indicates that no acknowledged messages in the Sequence
         * will be discarded.
         */
        NO_DISCARD,
        /**
         * Value indicates that the entire Sequence MUST be discarded if the
         * Sequence is closed, or terminated,  when there are one or more gaps
         * in the final SequenceAcknowledgement.
         */
        DISCARD_ENTIRE_SEQUENCE,
        /**
         * Value indicates that messages in the Sequence beyond the first gap
         * MUST be discarded when there are one or more gaps in the final SequenceAcknowledgement.
         */
        DISCARD_FOLLOWING_FIRST_GAP;

        public static IncompleteSequenceBehavior getDefault() {
            return NO_DISCARD;
        }
    }

    public static class AckRange {

        public final long lower;
        public final long upper;

        public AckRange(long lower, long upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }

    /**
     * Returns unique identifier of the sequence
     * 
     * @return unique sequence identifier
     */
    @ManagedAttribute
    @Description("Unique sequence identifier")
    public String getId();

    /**
     * Provides information on the message number of the last message registered on this sequence
     *
     * @return message number of the last message registered on this sequence
     */
    @ManagedAttribute
    @Description("Last message identifier register on this sequence")
    public long getLastMessageId();

    /**
     * Registers given message with the sequence
     *
     * @param message application message to be registered
     * @param storeMessageFlag boolean flag indicating whether message should be stored until acknowledged or not
     *
     * TODO should throw duplicate message exception instead of the IMIE
     *
     * @exception DuplicateMessageNumberException in case a message with such message number
     * has already been registered with the sequence
     *
     * @exception AbstractSoapFaultException in a case the sequence is closed or terminated
     */
    public void registerMessage(@NotNull ApplicationMessage message, boolean storeMessageFlag) throws DuplicateMessageRegistrationException, AbstractSoapFaultException;

    /**
     * Retrieves a message stored within the sequence under the provided {@code correlationId}
     * if avalable. May return {@code null} if no stored message under given {@code correlationId}
     * is available.
     * <p/>
     * Availability of the message depends on the message identifier acknowledgement.
     * Message, if stored (see {@link #registerMessage(com.sun.xml.ws.rx.rm.runtime.ApplicationMessage, boolean)}
     * remains available for retrieval until it is acknowledged. Once the message identifier
     * associated with the stored message has been acknowledged, availability of the
     * stored message is no longer guaranteed and stored message becomes eligible for
     * garbage collection (if stored in memory) or removal.
     * <p/>
     * Note however, that message MAY still be available even after it has been acknowledged.
     * Thus it is NOT safe to use this method as a test of a message acknowledgement.
     *
     * @param correlationId correlation identifier of the stored {@link ApplicationMessage}
     *
     * @return the message that is stored in the sequence if available, {@code null} otherwise.
     */
    public @Nullable ApplicationMessage retrieveMessage(@NotNull String correlationId);

    /**
     * TODO consider removing as not used anywhere
     *
     * Retrieves an unacknowledged message with the provided {@code messageNumber} stored
     * within the sequence if avalable. May return {@code null} if no unacknowledged message
     * with a given {@code messageNumber} is available.
     * <p/>
     * Availability of the message depends on the message identifier acknowledgement.
     * Message, if stored (see {@link #registerMessage(com.sun.xml.ws.rx.rm.runtime.ApplicationMessage, boolean)}
     * remains available for retrieval until it is acknowledged. Once the message identifier
     * associated with the stored message has been acknowledged, message will not be available.
     * <p/>
     * Note that this behavior is different from the behavior of {@link #retrieveMessage(java.lang.String)}
     * method, where Message may remain available for a while even after it has been acknowledged.
     *
     * @param correlationId correlation identifier of the stored {@link ApplicationMessage}
     *
     * @return the message that is stored in the sequence if available, {@code null} otherwise.
     */
    public @Nullable ApplicationMessage retrieveUnackedMessage(long messageNumber);

    /**
     * Updates a delivery queue for this sequence with any unacknowledged messages that 
     * should be sent and returns the delivery queue instance. Messages in the queue are
     * the ones currently waiting for a delivery.
     *
     * @return delivery queue with a messages waiting for a delivery on this particular sequence
     */
    public DeliveryQueue getDeliveryQueue();

    /**
     * Marks given message identifiers with the sequence as aknowledged
     *
     * @param ranges message identifier ranges to be acknowledged
     *
     * @exception InvalidAcknowledgementException is generated when acked ranges contain
     * a SequenceAcknowledgement covering messages that have not been sent.
     * 
     * @exception AbstractSoapFaultException in case the sequence is terminated
     */
    public void acknowledgeMessageIds(List<AckRange> ranges) throws InvalidAcknowledgementException, AbstractSoapFaultException;

    /**
     * Marks given message identifier with the sequence as aknowledged
     *
     * @param messageId message identifier to be acknowledged
     *
     * @exception IllegalMessageIdentifierException in case this is an {@link InboundSequence} instance and a message
     * with the given identifier has been already acknowledged
     *
     * @exception AbstractSoapFaultException in case the sequence is terminated
     */
    public void acknowledgeMessageId(long messageId) throws IllegalMessageIdentifierException, AbstractSoapFaultException;

    /**
     * Determines whether such message number has been already acknowledged on the sequence
     * or not.
     *
     * @param messageId message identifier to test
     * @return {@code true } or {@code false} depending on whether a message with such message
     *         identifer has been already acknowledged or not
     */
    public boolean isAcknowledged(long messageId);

    /**
     * Provides a collection of ranges of messages identifier acknowledged with the sequence
     * 
     * @return collection of ranges of messages identifier registered with the sequence
     */
    public List<AckRange> getAcknowledgedMessageIds();

    /**
     * The method may be called to determine whether the sequence has some unacknowledged messages or not
     * 
     * @return {@code true} if the sequence has any unacknowledged message identifiers, {@code false} otherwise
     */
    @ManagedAttribute
    @Description("True if the sequence has unacknowledged message identifiers")
    public boolean hasUnacknowledgedMessages();

    /**
     * Provides information on the state of the message sequence
     * 
     * @return current state of the message sequence
     */
    @ManagedAttribute
    @Description("Runtime state of the sequence")
    public State getState();

    /**
     * This method should be called to set the AckRequested flag, which indicates 
     * a pending request for acknowledgement of all message identifiers registered 
     * with this sequence.
     */
    public void setAckRequestedFlag();
    
    /**
     * This method should be called to clear the AckRequested flag, which indicates 
     * that any pending requests for acknowledgement of all message identifiers registered 
     * with this sequence were satisfied.
     */
    public void clearAckRequestedFlag();

    /**
     * Provides information on the actual AckRequested flag status
     * 
     * @return {@code true} if the AckRequested flag is set, {@code false} otherwise
     */
    @ManagedAttribute
    @Description("True if AckRequested flag set")
    public boolean isAckRequested();

    /**
     * Updates information on when was the last acknowledgement request for this sequence
     * sent to current time.
     */
    public void updateLastAcknowledgementRequestTime();

    /**
     * Determines whether a standalone acnowledgement request can be scheduled or not
     * based on the {@link #hasPendingAcknowledgements()} value, last acknowledgement request time
     * (see {@link #updateLastAcknowledgementRequestTime()}) and {@code delayPeriod}
     * parameter.
     * 
     * Returns {@code true} if the sequence has any pending acknowledgements is set and last
     * acknowledgement request time is older than delay period substracted from the current time.
     * Returns {@code false} otherwise.
     *
     * @param delayPeriod delay period that should pass since the last acknowledgement request
     * before an autonomous acnowledgement request is sent.
     *
     * @return {@code true} or {@code false} depending on whether
     */
    public boolean isStandaloneAcknowledgementRequestSchedulable(long delayPeriod);
    /**
     * Provides information on a security session to which this sequence is bound to.
     * 
     * @return security token reference identifier to which this sequence is bound to.
     */
    @ManagedAttribute
    @Description("The security token reference identifier to which this sequence is bound")
    public String getBoundSecurityTokenReferenceId();

    /**
     * Closes the sequence. Subsequent calls to this method have no effect.
     * <p>
     * Once this method is called, any subsequent calls to the {@link #getNextMessageId()} method will
     * result in a {@link IllegalStateException} being raised. It is however still possible to accept message identifier 
     * acknowledgements, as well as retrieve any other information on the sequence.
     */
    public void close();

    /**
     * Provides information on the sequence closed status.
     * 
     * @return {@code true} if the sequence has been closed, {@code false} otherwise
     */
    @ManagedAttribute
    @Description("True if the sequence has been closed")
    public boolean isClosed();

    /**
     * Provides information on the sequence expiration status.
     * 
     * @return {@code true} if the sequence has already expired, {@code false} otherwise
     */
    @ManagedAttribute
    @Description("True if the sequence has expired")
    public boolean isExpired();
    
    /**
     * Provides information on the last activity time of this sequence
     *
     * TODO: implement automatic updating of sequence last activity time
     *
     * @return last activity time on the sequence in milliseconds
     */
    @ManagedAttribute
    @Description("Last activity time on the sequence in milliseconds")
    public long getLastActivityTime();
    
    /**
     * The method is called during the sequence termination to allow sequence object to release its allocated resources
     */
    public void preDestroy();
}
