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
package com.sun.xml.ws.rm.runtime.sequence;

import java.util.List;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface Sequence {

    public static final long UNSPECIFIED_MESSAGE_ID = 0; // this MUST be 0 in order for AbstractSequence.createAckRanges() method to work properly
    public static final long MIN_MESSAGE_ID = 1;
    public static final long MAX_MESSAGE_ID = 9223372036854775807L;
    
    public enum Status {
        // CREATING(10) not needed
        CREATED(15),
        CLOSING(20),
        CLOSED(25),
        TERMINATING(30);

        private int value;
        
        private Status(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static Status valueToStatus(int value) {
            for (Status status : Status.values()) {
                if (status.value == value) {
                    return status;
                }
            }
            
            return null;
        }
    }

    public class AckRange {

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
    public String getId();

    /**
     * This operation is supported by outbound sequences only. 
     * <p/>
     * Generates a new message identifier and registers it within the sequence
     * 
     * @return the next message identifier that should be used for the next message sent on the sequence.
     * 
     * @exception MessageNumberRolloverException in case the message identifier counter overflows
     * 
     * @exception IllegalStateException in case the sequence is closed
     * 
     * @exception UnsupportedOperationException in case the sequence is inbound and does not support generating message identifiers
     */
    public long generateNextMessageId() throws MessageNumberRolloverException, IllegalStateException, UnsupportedOperationException;

    /**
     * This operation is supported by outbound sequences only. 
     * <p/>
     * Stores a message within the sequence. The message is guaranteed to remain stored
     * in the sequence until the message id associated with the message is acknowledged
     * 
     * @param correlationId identifier of a message that correlates with the stored message
     * 
     * @param id message identifier attached to the message
     * 
     * @param message the message that is supposed to be stored in the sequence 
     *        until the message is not acknowledged as received
     * 
     * @exception IllegalStateException in case the sequence is closed
     * 
     * @exception UnsupportedOperationException in case the sequence is inbound and does not support generating message identifiers
     */
    public void storeMessage(long correlationId, long id, Object message) throws IllegalStateException, UnsupportedOperationException;

    /**
     * This operation is supported by outbound sequences only. 
     * <p/>
     * Retrieves a message stored within the sequence if avalable. May return {@code null}
     * if no stored message under given message id is available.
     * <p/>
     * Availability of the message depends on the message identifier acknowledgement. 
     * Message, if stored (see {@link #storeMessage(long, java.lang.Object)} remains 
     * available for retrieval until it is acknowledged. Once the message identifier 
     * associated with the stored message has been acknowledged, availability of the 
     * stored message is no longer guaranteed and stored message becomes eligible for 
     * garbage collection.
     * <p/>
     * Note however, that message MAY still be available even after it has been acknowledged.
     * Thus it is NOT safe to use this method as a test of a message acknowledgement.
     * 
     * @param correlationId identifier of a message that correlates with the stored message
     * 
     * @return the message that is stored in the sequence if available, {@code null} otherwise.
     * 
     * @exception IllegalMessageIdentifierException in case the message number is not 
     *            registered with the sequence.
     * 
     * @exception UnsupportedOperationException in case the sequence is inbound and does not support generating message identifiers
     */
    public Object retrieveMessage(long correlationId) throws UnsupportedOperationException, IllegalMessageIdentifierException;
    
    /**
     * Registers given message identifiers with the sequence as aknowledged
     * 
     * @param ranges message identifier ranges to be acknowledged
     * 
     * @exception IllegalMessageIdentifierException in case this is an {@link InboundSequence} instance and a messages 
     * with the given identifiers have been already registered
     * 
     * @exception IllegalStateException in case the sequence is closed
     */
    public void acknowledgeMessageIds(List<AckRange> ranges) throws IllegalMessageIdentifierException, IllegalStateException;

    /**
     * Registers given message identifier with the sequence as aknowledged
     * 
     * @param messageId message identifier to be acknowledged
     * 
     * @exception IllegalMessageIdentifierException in case this is an {@link InboundSequence} instance and a message 
     * with the given identifier has been already registered
     * 
     * @exception IllegalStateException in case the sequence is closed
     */
    public void acknowledgeMessageId(long messageId) throws IllegalMessageIdentifierException, IllegalStateException;

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
     * Provides information on the last message id sent on this sequence
     * 
     * @return last message identifier registered on this sequence
     */
    public long getLastMessageId();

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
    public boolean hasPendingAcknowledgements();

    /**
     * Provides information on the status of the message sequence
     * 
     * @return current status of the message sequence
     */
    public Status getStatus();

    /**
     * This method should be called to set the AckRequested flag, which indicates a pending request for acknowledgement of all
     * message identifiers registered with this sequence. The flag is automatically cleared once {@link #getAcknowledgedMessageIds()}
     * method is called.
     */
    public void setAckRequestedFlag();

    /**
     * Provides information on the actual AckRequested flag status
     * 
     * @return {@code true} if the AckRequested flag is set, {@code false} otherwise
     */
    public boolean isAckRequested();
    
    /**
     * Provides information on a security session to which this sequence is bound to.
     * 
     * @return security token reference identifier to which this sequence is bound to.
     */
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
    public boolean isClosed();

    /**
     * Provides information on the sequence expiration status.
     * 
     * @return {@code true} if the sequence has already expired, {@code false} otherwise
     */
    public boolean isExpired();
    
    /**
     * Provides information on the last activity time of this sequence
     * 
     * @return last actiit time on the sequence in miliseconds
     */
    public long getLastActivityTime();
    
    /**
     * Manually updates the last activit time of the sequence to present time
     */
    public void updateLastActivityTime();
    
    /**
     * The method is called during the sequence termination to allow sequence object to release its allocated resources
     */
    public void preDestroy();
}
