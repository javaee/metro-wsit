package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.rx.rm.runtime.sequence.*;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class InVmSequenceData implements SequenceData {

    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    // lock used to synchronize the access to the lastMessageId and unackedMessageIdentifiersStorage variables
    //
    private final String sequenceId;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    //
    private volatile State state;
    private volatile boolean ackRequestedFlag;
    private volatile long lastMessageNumber;
    private volatile long lastActivityTime;
    private volatile long lastAcknowledgementRequestTime;
    //
    private final Collection<Long> allUnackedMessageNumbers;
    private final Collection<Long> receivedUnackedMessageNumbers;
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
        this.lastMessageNumber = lastMessageId;
        this.lastActivityTime = lastActivityTime;
        this.lastAcknowledgementRequestTime = lastAcknowledgementRequestTime;

        this.allUnackedMessageNumbers = new TreeSet<Long>();
//        this.allUnackedMessageNumbers = new LinkedList<Long>();
        this.receivedUnackedMessageNumbers = new HashSet<Long>();

        this.weakMessageStorage = new WeakHashMap<String, ApplicationMessage>();
        this.weakUnackedNumberToCorrelationIdMap = new WeakHashMap<Long, String>();
    }

    public void lockRead() {
        dataLock.readLock().lock();
    }

    public void unlockRead() {
        dataLock.readLock().unlock();
    }

    public void lockWrite() {
        dataLock.writeLock().lock();
    }

    public void unlockWrite() {
        dataLock.writeLock().unlock();
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getLastMessageNumber() {
        try {
            lockRead();
            return lastMessageNumber;
        } finally {
            unlockRead();
        }
    }

    public void setLastMessageNumber(long newLastMessageNumber) {
        try {
            lockWrite();
            this.lastMessageNumber = newLastMessageNumber;
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

    public final void attachMessageToUnackedMessageNumber(ApplicationMessage message) {
        try {
            lockWrite();
            // NOTE this must be a new String object
            String correlationKey = new String(message.getCorrelationId());

            Long msgNumberKey = getUnackedMessageIdentifierKey(message.getMessageNumber());

            weakUnackedNumberToCorrelationIdMap.put(msgNumberKey, correlationKey);
            weakMessageStorage.put(correlationKey, message);
        } finally {
            unlockWrite();
        }
    }

    public void registerUnackedMessageNumber(long messageNumber, boolean received) throws DuplicateMessageRegistrationException {
        try {
            lockWrite();

            if (received && receivedUnackedMessageNumbers.contains(messageNumber) || !received && allUnackedMessageNumbers.contains(messageNumber)) {
                throw new DuplicateMessageRegistrationException(sequenceId, messageNumber);
            }

            final Long newUnackedInstance = new Long(messageNumber);

            allUnackedMessageNumbers.add(newUnackedInstance);
            if (received) {
                receivedUnackedMessageNumbers.add(newUnackedInstance);
            }
        } finally {
            unlockWrite();
        }
    }

    public void markAsAcknowledged(long messageNumber) {
        try {
            lockWrite();
            if (!receivedUnackedMessageNumbers.remove(messageNumber)) {
                throw new IllegalMessageIdentifierException(sequenceId, messageNumber);
            }

            boolean removedFromAll = allUnackedMessageNumbers.remove(messageNumber);
            assert removedFromAll;
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

    public Collection<Long> getUnackedMessageNumbers() {
        try {
            lockRead();
            return Collections.unmodifiableCollection(new ArrayList<Long>(allUnackedMessageNumbers));
        } finally {
            unlockRead();
        }

    }

// INBOUND
    private Long getUnackedMessageIdentifierKey(long messageNumber) {
        try {
            lockRead();
            Long msgNumberKey = null;
            Iterator<Long> iterator = receivedUnackedMessageNumbers.iterator();
            while (iterator.hasNext()) {
                msgNumberKey = iterator.next();
                if (msgNumberKey.longValue() == messageNumber) {
                    break;
                }
            }

            return msgNumberKey;
        } finally {
            unlockRead();
        }
    }
// OUTBOUND
//    private Long getUnackedMessageIdentifierKey(long messageNumber) {
//        Long msgNumberKey = null;
//        int index = allUnackedMessageNumbers.indexOf(messageNumber);
//        if (index >= 0) {
//            // the id is in the list
//            msgNumberKey = allUnackedMessageNumbers.get(index);
//        }
//        return msgNumberKey;
//    }
}
