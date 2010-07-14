package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.util.List;

public interface SequenceData {

    String getSequenceId();

    long getExpirationTime();

    String getBoundSecurityTokenReferenceId();

    long getLastMessageNumber();

    boolean getAckRequestedFlag();

    void setAckRequestedFlag(boolean newValue);

    long getLastAcknowledgementRequestTime();

    void setLastAcknowledgementRequestTime(long newTime);

    /**
     * Provides information on the last activity time of this sequence data instance. Following is the
     * list of operations invocation of which causes an update of last activity time:
     * 
     * <ul>
     *   <li>{@link #attachMessageToUnackedMessageNumber(ApplicationMessage) }</li>
     *   <li>{@link #incrementAndGetLastMessageNumber(boolean) }</li>
     *   <li>{@link #markAsAcknowledged(long) }</li>
     *   <li>{@link #registerUnackedMessageNumber(long, boolean) }</li>
     *   <li>{@link #retrieveMessage(java.lang.String) }</li>
     *   <li>{@link #setAckRequestedFlag(boolean) }</li>
     *   <li>{@link #setLastAcknowledgementRequestTime(long) }</li>
     *   <li>{@link #setState(com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State) }</li>
     * </ul>
     *
     * @return last activity time of the sequence
     */
    long getLastActivityTime();

    State getState();

    void setState(State newState);

    /**
     * Increments last message number on the sequence and returns new last message number value.
     * Method automatically marks the newly created message number as unacknowledged.
     *
     * @param received this flag specifies whether the message with such number should be marked
     * as received on the sequence.
     *
     * @return new value of the last message number
     */
    long incrementAndGetLastMessageNumber(boolean received);

    /**
     * Registers the message number as unacknowledged.
     * <p/>
     * If the value of {@code messageNumber} parameter is greater that the actual 
     * last mesasge number, the last message number value is increased to the value of
     * {@code messageNumber} parameter. All message numbers lying between the original
     * and new value of last message number are automatically marked as unacknowldeged
     * and not received.
     *
     * @param messageNumber unacknowledged message number to register
     *
     * @return new value of the last message number
     * @exception DuplicateMessageRegistrationException in case such registration already exists
     */
    void registerReceivedUnackedMessageNumber(long messageNumber) throws DuplicateMessageRegistrationException;

    /**
     * Removes the provided {@code messageNumber} from the collection of unacked message
     * numbers and and marks stored message with given {@code messageNumber} (if any)
     * as eligible for removal from the underlying message storage.
     *
     * This method does nothing if there's no such unacknowledged message number found
     *
     * @param messageNumber
     */
    void markAsAcknowledged(long messageNumber);

    void attachMessageToUnackedMessageNumber(ApplicationMessage message);

    ApplicationMessage retrieveMessage(String correlationId);

    public List<Long> getUnackedMessageNumbers();

    /**
     * In contrast to {@link #getUnackedMessageNumbers()}, this method returns allways a non-empty
     * {@link List} in which first item represents a current last message number value (see also
     * {@link #getLastMessageNumber()}. The items following the first item represent
     * the collection of unacked message numbers (see also {@link #getUnackedMessageNumbers()}).
     * <p/>
     * This special method was introduced in order to allow for an atomic retrieval of both values.
     *
     * @return {@link List} where first item represents the last message number and all subsequent
     * values represent unacknowledged message numbers.
     *
     */
    public List<Long> getLastMessageNumberWithUnackedMessageNumbers();
}
