package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.rx.rm.runtime.sequence.*;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.ha.store.annotations.StoreEntry;
import org.glassfish.ha.store.api.BackingStore;

@StoreEntry
final class InVmSequenceData implements SequenceData {
    private static final class UnackedMessageReplicationManager implements ReplicationManager<String, ApplicationMessage> {
        private static class ApplicationMessageState implements Serializable {
            private final byte[] data;
            private final int nextResendCount;
            private final String correlationId;
            private final String wsaAction;
            private final String sequenceId;
            private final long messageNumber;

            public ApplicationMessageState(ApplicationMessage _message) {
                if (!(_message instanceof JaxwsApplicationMessage)) {
                    throw new IllegalArgumentException("Unsupported message class: " + _message.getClass().getName());
                }

                JaxwsApplicationMessage message = (JaxwsApplicationMessage) _message;

                this.data = message.toBytes();
                this.nextResendCount = message.getNextResendCount();
                this.correlationId = message.getCorrelationId();
                this.wsaAction = message.getWsaAction();
                this.sequenceId = message.getSequenceId();
                this.messageNumber = message.getMessageNumber();
            }

            public ApplicationMessage toMessage() {
                ByteArrayInputStream bais = new ByteArrayInputStream(data);

                return JaxwsApplicationMessage.newInstance(
                    bais,
                    nextResendCount,
                    correlationId,
                    wsaAction,
                    sequenceId,
                    messageNumber);
            }
        }
        
        private BackingStore<String, ApplicationMessageState> unackedMesagesBs;

        public UnackedMessageReplicationManager(String sequenceId) {
            this.unackedMesagesBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                    HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY),
                    sequenceId + "_UNACKED_MESSAGES_BS",
                    String.class,
                    ApplicationMessageState.class);
        }

        public ApplicationMessage load(String key) {
            ApplicationMessageState state = HighAvailabilityProvider.loadFrom(unackedMesagesBs, key, null);
            return state.toMessage();
        }

        public String save(String key, ApplicationMessage value, boolean isNew) {
            ApplicationMessageState ams = new ApplicationMessageState(value);
            return HighAvailabilityProvider.saveTo(unackedMesagesBs, key, ams, isNew);
        }

        public void remove(String key) {
            HighAvailabilityProvider.removeFrom(unackedMesagesBs, key);
        }

        public void close() {
            HighAvailabilityProvider.close(unackedMesagesBs);
        }

        public void destroy() {
            HighAvailabilityProvider.destroy(unackedMesagesBs);
        }
    }

    // lock used to synchronize the access to the lastMessageId and unackedMessageIdentifiersStorage variables
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    //
    private final HighlyAvailableMap<String, ApplicationMessage> messageStore;
    private final SequenceDataPojo data;
    private final TimeSynchronizer timeSynchronizer;

    public static InVmSequenceData newInstace(@NotNull TimeSynchronizer timeSynchronizer, @NotNull SequenceDataPojo data) {
        return new InVmSequenceData(timeSynchronizer, data);
    }

    public static InVmSequenceData loadReplica(@NotNull TimeSynchronizer timeSynchronizer, @NotNull SequenceDataPojo data) {

        InVmSequenceData replica = new InVmSequenceData(timeSynchronizer, data);
        replica.initLocalCache();

        return replica;
    }

    private InVmSequenceData(@NotNull TimeSynchronizer timeSynchronizer, @NotNull SequenceDataPojo data) {
        assert timeSynchronizer != null;
        assert data != null;

        this.timeSynchronizer = timeSynchronizer;
        this.data = data;

        ReplicationManager<String, ApplicationMessage> rm = null;
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
            rm = new UnackedMessageReplicationManager(data.getSequenceId());
        }
        this.messageStore = HighlyAvailableMap.newInstance(new HashMap<String, ApplicationMessage>(), rm);
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
        return data.getSequenceId();
    }

    public String getBoundSecurityTokenReferenceId() {
        return data.getBoundSecurityTokenReferenceId();
    }

    public long getLastMessageNumber() {
        try {
            lockRead();
            return data.getLastMessageNumber();
        } finally {
            unlockRead();
        }
    }

    public State getState() {
        return data.getState();
    }

    public void setState(State newState) {
        updateLastActivityTime();

        data.setState(newState);
        data.replicate();
   }

    public boolean getAckRequestedFlag() {
        return data.getAckRequestedFlag();
    }

    public void setAckRequestedFlag(boolean newValue) {
        updateLastActivityTime();

        data.setAckRequestedFlag(newValue);
        data.replicate();
    }

    public long getLastAcknowledgementRequestTime() {
        return data.getLastAcknowledgementRequestTime();
    }

    public void setLastAcknowledgementRequestTime(long newTime) {
        updateLastActivityTime();

        data.setLastAcknowledgementRequestTime(newTime);
        data.replicate();
    }

    public long getLastActivityTime() {
        return data.getLastActivityTime();
    }

    private void updateLastActivityTime() {
        data.setLastActivityTime(timeSynchronizer.currentTimeInMillis());
    }

    public long getExpirationTime() {
        return data.getExpirationTime();
    }

    public final void attachMessageToUnackedMessageNumber(ApplicationMessage message) {
        updateLastActivityTime();

        try {
            lockWrite();
            Long msgNumberKey = getUnackedMessageIdentifierKey(message.getMessageNumber());

            data.getUnackedNumberToCorrelationIdMap().put(msgNumberKey, message.getCorrelationId());
            data.replicate();
            
            messageStore.put(message.getCorrelationId(), message);
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

            data.setLastMessageNumber(data.getLastMessageNumber() + 1);
            addUnackedMessageNumber(data.getLastMessageNumber(), received);
            data.replicate();
            
            return data.getLastMessageNumber();
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

            if (messageNumber > data.getLastMessageNumber()) {
                while (messageNumber > data.getLastMessageNumber() + 1) {
                    // new message - note that this will work even for the first message that arrives
                    // some message(s) got lost, add to all unacked message number set...
                    incrementAndGetLastMessageNumber(false);
                }

                incrementAndGetLastMessageNumber(true);
            } else {
                if (data.getReceivedUnackedMessageNumbers().contains(messageNumber) || // we already have such received unacked registration
                        !data.getAllUnackedMessageNumbers().contains(messageNumber)) { // not found among all unacked messages => has been already acknowledged
                    throw new DuplicateMessageRegistrationException(data.getSequenceId(), messageNumber);
                }

                addUnackedMessageNumber(messageNumber, true);
            }

            data.replicate();
        } finally {
            unlockWrite();
        }
    }

    /*
     * This method must be called from within a data write lock only.
     */
    private void addUnackedMessageNumber(long messageNumber, boolean received) {
        final Long newUnackedInstance = Long.valueOf(messageNumber);

        data.getAllUnackedMessageNumbers().add(newUnackedInstance);
        if (received) {
            data.getReceivedUnackedMessageNumbers().add(newUnackedInstance);
        }
    }

    public void markAsAcknowledged(long messageNumber) {
        updateLastActivityTime();

        try {
            lockWrite();
            data.getReceivedUnackedMessageNumbers().remove(messageNumber);
            data.getAllUnackedMessageNumbers().remove(messageNumber);
            final String correlationId = data.getUnackedNumberToCorrelationIdMap().remove(messageNumber);
            data.replicate();

            messageStore.remove(correlationId);
        } finally {
            unlockWrite();
        }
    }

    public ApplicationMessage retrieveMessage(String correlationId) {
        updateLastActivityTime();

        try {
            lockRead();
            return messageStore.get(correlationId);
        } finally {
            unlockRead();
        }
    }

    public List<Long> getUnackedMessageNumbers() {
        try {
            lockRead();
            return new ArrayList<Long>(data.getAllUnackedMessageNumbers());
        } finally {
            unlockRead();
        }

    }

    public List<Long> getLastMessageNumberWithUnackedMessageNumbers() {
        try {
            lockRead();

            LinkedList<Long> result = new LinkedList<Long>(data.getAllUnackedMessageNumbers());
            result.addFirst(data.getLastMessageNumber());

            return result;
        } finally {
            unlockRead();
        }
    }

    private Long getUnackedMessageIdentifierKey(long messageNumber) {
        try {
            lockRead();
            Long msgNumberKey = null;
            Iterator<Long> iterator = data.getReceivedUnackedMessageNumbers().iterator();
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

    SequenceDataPojo getSequenceStatePojo() {
        return data;
    }

    private void initLocalCache() {
        for (Long unackedMessageNumber : data.getReceivedUnackedMessageNumbers()) {
            messageStore.get(data.getUnackedNumberToCorrelationIdMap().get(unackedMessageNumber));
        }
    }
}
