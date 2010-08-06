package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.xml.ws.rx.rm.runtime.sequence.*;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

    private final TimeSynchronizer timeSynchronizer;

    public InVmSequenceData(TimeSynchronizer timeSynchronizer, String sequenceId, String securityContextTokenId, long expirationTime, long lastMessageId, long lastActivityTime) {
        this(timeSynchronizer, sequenceId, securityContextTokenId, expirationTime, State.CREATED, false, lastMessageId, lastActivityTime, 0L);
    }

    public InVmSequenceData(TimeSynchronizer timeSynchronizer, String sequenceId, String securityContextTokenId, long expirationTime, State state, boolean ackRequestedFlag, long lastMessageId, long lastActivityTime, long lastAcknowledgementRequestTime) {
        super();

        this.timeSynchronizer = timeSynchronizer;

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

    private void lockRead() {
        dataLock.readLock().lock();
    }

    private void unlockRead() {
        dataLock.readLock().unlock();
    }

    private void lockWrite() {
        dataLock.writeLock().lock();
    }

    private void unlockWrite() {
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

    public State getState() {
        return state;
    }

    public void setState(State newState) {
        updateLastActivityTime();

        state = newState;
    }

    public boolean getAckRequestedFlag() {
        return ackRequestedFlag;
    }

    public void setAckRequestedFlag(boolean newValue) {
        updateLastActivityTime();

        ackRequestedFlag = newValue;
    }

    public long getLastAcknowledgementRequestTime() {
        return lastAcknowledgementRequestTime;
    }

    public void setLastAcknowledgementRequestTime(long newTime) {
        updateLastActivityTime();

        lastAcknowledgementRequestTime = newTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    private void updateLastActivityTime() {
        lastActivityTime = timeSynchronizer.currentTimeInMillis();
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public final void attachMessageToUnackedMessageNumber(ApplicationMessage message) {
         updateLastActivityTime();

        try {
            lockWrite();
            // NOTE this must be a new String object
            @SuppressWarnings("RedundantStringConstructorCall")
            String correlationKey = new String(message.getCorrelationId());

            Long msgNumberKey = getUnackedMessageIdentifierKey(message.getMessageNumber());

            weakUnackedNumberToCorrelationIdMap.put(msgNumberKey, correlationKey);
            weakMessageStorage.put(correlationKey, message);
        } finally {
            unlockWrite();
        }
    }

    /**
     * {@inheritDoc}
     */
    public long incrementAndGetLastMessageNumber(boolean received) {
        updateLastActivityTime();

        try {
            dataLock.writeLock().lock();

            addUnackedMessageNumber(++lastMessageNumber, received);
            return lastMessageNumber;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void registerReceivedUnackedMessageNumber(long messageNumber) throws DuplicateMessageRegistrationException {
        updateLastActivityTime();

        try {
            lockWrite();

            if (messageNumber > lastMessageNumber) {
                while (messageNumber > lastMessageNumber + 1) {
                    // new message - note that this will work even for the first message that arrives
                    // some message(s) got lost, add to all unacked message number set...
                    incrementAndGetLastMessageNumber(false);
                }

                incrementAndGetLastMessageNumber(true);
            } else {
                if (receivedUnackedMessageNumbers.contains(messageNumber) || // we already have such received unacked registration
                        !allUnackedMessageNumbers.contains(messageNumber)) { // not found among all unacked messages => has been already acknowledged
                    throw new DuplicateMessageRegistrationException(sequenceId, messageNumber);
                }

                addUnackedMessageNumber(messageNumber, true);
            }
        } finally {
            unlockWrite();
        }
    }

    /*
     * This method must be called from within a data write lock only.
     */
    private void addUnackedMessageNumber(long messageNumber, boolean received) {
        final Long newUnackedInstance = new Long(messageNumber);

        allUnackedMessageNumbers.add(newUnackedInstance);
        if (received) {
            receivedUnackedMessageNumbers.add(newUnackedInstance);
        }
    }

    public void markAsAcknowledged(long messageNumber) {
        updateLastActivityTime();

        try {
            lockWrite();
            receivedUnackedMessageNumbers.remove(messageNumber);
            allUnackedMessageNumbers.remove(messageNumber);
        } finally {
            unlockWrite();
        }
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        updateLastActivityTime();

        try {
            lockRead();
            return weakMessageStorage.get(correlationId);
        } finally {
            unlockRead();
        }
    }

    public List<Long> getUnackedMessageNumbers() {
        try {
            lockRead();
            return new ArrayList<Long>(allUnackedMessageNumbers);
        } finally {
            unlockRead();
        }

    }

    public List<Long> getLastMessageNumberWithUnackedMessageNumbers() {
        try {
            lockRead();

            LinkedList<Long> data = new LinkedList<Long>(allUnackedMessageNumbers);
            data.addFirst(lastMessageNumber);

            return data;
        } finally {
            unlockRead();
        }
    }

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
}
