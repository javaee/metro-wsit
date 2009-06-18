package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.rx.rm.runtime.sequence.*;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class InVmSequenceData implements SequenceData {

    private final ReadWriteLock messageIdLock = new ReentrantReadWriteLock();
    // lock used to synchronize the access to the lastMessageId and unackedMessageIdentifiersStorage variables
    //
    private final String sequenceId;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    //
    private volatile State state;
    private volatile boolean ackRequestedFlag;
    private volatile long lastMessageId;
    private volatile long lastActivityTime;
    private volatile long lastAcknowledgementRequestTime;
    //
    private final Map<String, ApplicationMessage> weakMessageStorage;
    private final Map<Long, String> weakUnackedNumberToCorrelationIdMap;

    public InVmSequenceData(String sequenceId, String securityContextTokenId, long expirationTime, long lastMessageId, long lastActivityTime) {
        this(sequenceId, securityContextTokenId, expirationTime, State.CREATED, false, lastMessageId, lastActivityTime, 0L);
    }

    public InVmSequenceData(String sequenceId, String securityContextTokenId, long expirationTime, State state, boolean ackRequestedFlag, long lastMessageId, long lastActivityTime, long lastAcknowledgementRequestTime) {
        super();
        this.sequenceId = sequenceId;
        this.boundSecurityTokenReferenceId = securityContextTokenId;
        this.expirationTime = expirationTime;
        this.state = state;
        this.ackRequestedFlag = ackRequestedFlag;
        this.lastMessageId = lastMessageId;
        this.lastActivityTime = lastActivityTime;
        this.lastAcknowledgementRequestTime = lastAcknowledgementRequestTime;
        this.weakMessageStorage = new WeakHashMap<String, ApplicationMessage>();
        this.weakUnackedNumberToCorrelationIdMap = new WeakHashMap<Long, String>();
    }

    public void lockRead() {
        messageIdLock.readLock().lock();
    }

    public void unlockRead() {
        messageIdLock.readLock().unlock();
    }

    public void lockWrite() {
        messageIdLock.writeLock().lock();
    }

    public void unlockWrite() {
        messageIdLock.writeLock().unlock();
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getLastMessageId() {
        try {
            lockRead();
            return lastMessageId;
        } finally {
            unlockRead();
        }
    }

    public void setLastMessageId(long newLastMessageId) {
        try {
            lockWrite();
            this.lastMessageId = newLastMessageId;
        } finally {
            unlockWrite();
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        state = newState;
    }

    public boolean getAckRequestedFlag() {
        return ackRequestedFlag;
    }

    public void setAckRequestedFlag(boolean newValue) {
        ackRequestedFlag = newValue;
    }

    public long getLastAcknowledgementRequestTime() {
        return lastAcknowledgementRequestTime;
    }

    public void setLastAcknowledgementRequestTime(long newTime) {
        lastAcknowledgementRequestTime = newTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long newTime) {
        lastActivityTime = newTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public final void storeMessage(ApplicationMessage message, Long msgNumberKey) throws UnsupportedOperationException {
        assert msgNumberKey != null;
        try {
            lockWrite();
            // NOTE this must be a new String object
            String correlationKey = new String(message.getCorrelationId());
            weakUnackedNumberToCorrelationIdMap.put(msgNumberKey, correlationKey);
            weakMessageStorage.put(correlationKey, message);
        } finally {
            unlockWrite();
        }
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        try {
            lockRead();
            return weakMessageStorage.get(correlationId);
        } finally {
            unlockRead();
        }
    }

    public ApplicationMessage retrieveUnackedMessage(long messageNumber) {
        try {
            lockRead();
            String correlationKey = weakUnackedNumberToCorrelationIdMap.get(messageNumber);
            return (correlationKey != null) ? weakMessageStorage.get(correlationKey) : null;
        } finally {
            unlockRead();
        }
    }
}
