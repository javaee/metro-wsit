package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;

public interface SequenceData {

    void lockRead();

    void lockWrite();

    void unlockRead();

    void unlockWrite();

    String getSequenceId();

    String getBoundSecurityTokenReferenceId();

    long getLastMessageId();

    boolean getAckRequestedFlag();

    long getLastAcknowledgementRequestTime();

    long getLastActivityTime();

    State getState();

    void setAckRequestedFlag(boolean newValue);

    void setLastAcknowledgementRequestTime(long newTime);

    void setLastActivityTime(long newTime);

    void setLastMessageId(long newLastMessageId);

    void setState(State newState);

    long getExpirationTime();

    ApplicationMessage retrieveMessage(String correlationId);

    ApplicationMessage retrieveUnackedMessage(long messageNumber);

    void storeMessage(ApplicationMessage message, Long msgNumberKey) throws UnsupportedOperationException;
}
