package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.util.Collection;

public interface SequenceData {

    void lockRead();

    void lockWrite();

    void unlockRead();

    void unlockWrite();

    String getSequenceId();

    long getExpirationTime();

    String getBoundSecurityTokenReferenceId();

    long getLastMessageNumber();

    void setLastMessageNumber(long newLastMessageNumber);

    boolean getAckRequestedFlag();

    void setAckRequestedFlag(boolean newValue);

    long getLastAcknowledgementRequestTime();

    void setLastAcknowledgementRequestTime(long newTime);

    long getLastActivityTime();

    void setLastActivityTime(long newTime);
    
    State getState();

    void setState(State newState);

    void registerUnackedMessageNumber(long messageNumber, boolean received) throws DuplicateMessageRegistrationException;

    /**
     * Removes the provided {@code messageNumber} from the collevtion of unacked message
     * numbers and and marks stored message with given {@code messageNumber} (if any)
     * as eligible for removal from the underlying message storage.
     *
     * @param messageNumber
     */
    void markAsAcknowledged(long messageNumber);

    void attachMessageToUnackedMessageNumber(ApplicationMessage message);

    ApplicationMessage retrieveMessage(String correlationId);

    public Collection<Long> getUnackedMessageNumbers();
}
