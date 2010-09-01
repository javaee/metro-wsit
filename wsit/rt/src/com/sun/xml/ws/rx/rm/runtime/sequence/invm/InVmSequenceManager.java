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
package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.MaintenanceTaskExecutor;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.RmConfiguration;
import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.rm.runtime.sequence.AbstractSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.InboundSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.OutboundSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceMaintenanceTask;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreFactory;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class InVmSequenceManager implements SequenceManager, ReplicationManager<String, AbstractSequence> {

    private static final Logger LOGGER = Logger.getLogger(InVmSequenceManager.class);
    /**
     * Internal in-memory data access lock
     */
    private final ReadWriteLock dataLock = new ReentrantReadWriteLock();
    /**
     * Internal in-memory storage of sequence data
     */
    private final HighlyAvailableMap<String, AbstractSequence> sequences;
    /**
     * Sequence data POJo backing store
     */
    private final BackingStore<String, SequenceDataPojo> sequenceDataBs;
    /**
     * Internal in-memory map of bound sequences
     */
    private final HighlyAvailableMap<String, String> boundSequences;
    /**
     * Inbound delivery queue builder
     */
    private final DeliveryQueueBuilder inboundQueueBuilder;
    /**
     * Outbound delivery queue builder
     */
    private final DeliveryQueueBuilder outboundQueueBuilder;
    /**
     * Unique identifier of the WS endpoint for which this particular sequence manager will be used
     */
    private final String uniqueEndpointId;
    /**
     * Inactivity timeout for a sequence
     */
    private final long sequenceInactivityTimeout;
    /**
     * Maximum number of concurrent inbound sequences
     */
    private final long maxConcurrentInboundSequences;
    /**
     * Actual number of concurrent inbound sequences
     */
    private final AtomicLong actualConcurrentInboundSequences;

    @SuppressWarnings("LeakingThisInConstructor")
    public InVmSequenceManager(String uniqueEndpointId, DeliveryQueueBuilder inboundQueueBuilder, DeliveryQueueBuilder outboundQueueBuilder, RmConfiguration configuration) {
        this.uniqueEndpointId = uniqueEndpointId;
        this.inboundQueueBuilder = inboundQueueBuilder;
        this.outboundQueueBuilder = outboundQueueBuilder;

        this.sequenceInactivityTimeout = configuration.getRmFeature().getSequenceInactivityTimeout();

        this.actualConcurrentInboundSequences = new AtomicLong(0);
        this.maxConcurrentInboundSequences = configuration.getRmFeature().getMaxConcurrentSessions();

        final BackingStoreFactory bsFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);
        this.boundSequences = HighlyAvailableMap.newInstanceForBs(
                new HashMap<String, String>(),
                HighAvailabilityProvider.INSTANCE.createBackingStore(
                bsFactory,
                uniqueEndpointId + "_BOUND_SEQUENCE_BS",
                String.class,
                String.class));

        this.sequenceDataBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                bsFactory,
                uniqueEndpointId + "_SEQUENCE_DATA_BS",
                String.class,
                SequenceDataPojo.class);
        this.sequences = HighlyAvailableMap.newInstance(new HashMap<String, AbstractSequence>(), this);

        MaintenanceTaskExecutor.INSTANCE.register(
                new SequenceMaintenanceTask(this, configuration.getRmFeature().getSequenceManagerMaintenancePeriod(), TimeUnit.MILLISECONDS),
                configuration.getRmFeature().getSequenceManagerMaintenancePeriod(),
                TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    public boolean persistent() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String uniqueEndpointId() {
        return uniqueEndpointId;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Sequence> sequences() {
        try {
            dataLock.readLock().lock();

            return new HashMap<String, Sequence>(sequences);
        } finally {
            dataLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> boundSequences() {
        try {
            dataLock.readLock().lock();

            return boundSequences.getLocalMapCopy();
        } finally {
            dataLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public long concurrentlyOpenedInboundSequencesCount() {
        return actualConcurrentInboundSequences.longValue();
    }

    /**
     * {@inheritDoc}
     */
    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException {
        SequenceDataPojo sequenceDataPojo = new SequenceDataPojo(sequenceId, strId, expirationTime, false, sequenceDataBs);
        sequenceDataPojo.setState(Sequence.State.CREATED);
        sequenceDataPojo.setAckRequestedFlag(false);
        sequenceDataPojo.setLastMessageNumber(OutboundSequence.INITIAL_LAST_MESSAGE_ID);
        sequenceDataPojo.setLastActivityTime(currentTimeInMillis());
        sequenceDataPojo.setLastAcknowledgementRequestTime(0L);

        SequenceData data = InVmSequenceData.newInstace(this, sequenceDataPojo);
        return registerSequence(new OutboundSequence(data, this.outboundQueueBuilder, this));
    }

    /**
     * {@inheritDoc}
     */
    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException {
        final long actualSessions = actualConcurrentInboundSequences.incrementAndGet();
        if (maxConcurrentInboundSequences >= 0) {
            if (maxConcurrentInboundSequences < actualSessions) {
                actualConcurrentInboundSequences.decrementAndGet();
                throw new RxRuntimeException(LocalizationMessages.WSRM_1156_MAX_CONCURRENT_SESSIONS_REACHED(maxConcurrentInboundSequences));
            }
        }

        SequenceDataPojo sequenceDataPojo = new SequenceDataPojo(sequenceId, strId, expirationTime, true, sequenceDataBs);
        sequenceDataPojo.setState(Sequence.State.CREATED);
        sequenceDataPojo.setAckRequestedFlag(false);
        sequenceDataPojo.setLastMessageNumber(InboundSequence.INITIAL_LAST_MESSAGE_ID);
        sequenceDataPojo.setLastActivityTime(currentTimeInMillis());
        sequenceDataPojo.setLastAcknowledgementRequestTime(0L);

        SequenceData data = InVmSequenceData.newInstace(this, sequenceDataPojo);
        return registerSequence(new InboundSequence(data, this.inboundQueueBuilder, this));
    }

    /**
     * {@inheritDoc}
     */
    public String generateSequenceUID() {
        return "uuid:" + UUID.randomUUID();
    }

    /**
     * {@inheritDoc}
     */
    public Sequence closeSequence(String sequenceId) throws UnknownSequenceException {
        Sequence sequence = getSequence(sequenceId);
        sequence.close();
        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    public Sequence getSequence(String sequenceId) throws UnknownSequenceException {
        try {
            dataLock.readLock().lock();
            Sequence sequence = sequences.get(sequenceId);
            if (sequence == null) {
                throw new UnknownSequenceException(sequenceId);
            }

            if (shouldTeminate(sequence)) {
                dataLock.readLock().unlock();
                tryTerminateSequence(sequenceId);
                dataLock.readLock().lock();
            }

            return sequence;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid(String sequenceId) {
        try {
            dataLock.readLock().lock();
            Sequence s = sequences.get(sequenceId);
            return s != null && s.getState() != Sequence.State.TERMINATING;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    private Sequence tryTerminateSequence(String sequenceId) {
        try {
            dataLock.writeLock().lock();
            final Sequence sequence = sequences.get(sequenceId);
            if (sequence == null) {
                return null;
            }

            if (sequence.getState() != Sequence.State.TERMINATING) {
                if (sequence instanceof InboundSequence) {
                    actualConcurrentInboundSequences.decrementAndGet();
                }
                sequence.preDestroy();
            }

            return sequence;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Sequence terminateSequence(String sequenceId) throws UnknownSequenceException {
        Sequence sequence = tryTerminateSequence(sequenceId);
        if (sequence == null) {
            throw new UnknownSequenceException(sequenceId);
        }

        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    public void bindSequences(String referenceSequenceId, String boundSequenceId) throws UnknownSequenceException {
        try {
            dataLock.writeLock().lock();
            if (!sequences.containsKey(referenceSequenceId)) {
                throw new UnknownSequenceException(referenceSequenceId);
            }

            if (!sequences.containsKey(boundSequenceId)) {
                throw new UnknownSequenceException(boundSequenceId);
            }

            boundSequences.put(referenceSequenceId, boundSequenceId);
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Sequence getBoundSequence(String referenceSequenceId) throws UnknownSequenceException {
        try {
            dataLock.readLock().lock();
            if (!isValid(referenceSequenceId)) {
                throw new UnknownSequenceException(referenceSequenceId);
            }

            return (boundSequences.containsKey(referenceSequenceId)) ? sequences.get(boundSequences.get(referenceSequenceId)) : null;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    /**
     * Registers a new sequence in the internal sequence storage
     *
     * @param sequence sequence object to be registered within the internal sequence storage
     */
    private Sequence registerSequence(AbstractSequence sequence) throws DuplicateSequenceException {
        try {
            dataLock.writeLock().lock();
            if (sequences.containsKey(sequence.getId())) {
                throw new DuplicateSequenceException(sequence.getId());
            } else {
                sequences.put(sequence.getId(), sequence);
            }

            return sequence;
        } finally {
            dataLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public long currentTimeInMillis() {
        return System.currentTimeMillis();
    }

    public void onMaintenance() {
        LOGGER.entering();
        try {
            dataLock.writeLock().lock();

            Iterator<String> sequenceKeyIterator = sequences.keySet().iterator();
            while (sequenceKeyIterator.hasNext()) {
                String key = sequenceKeyIterator.next();

                final Sequence sequence = sequences.get(key);
                if (shouldRemove(sequence)) {
                    LOGGER.config(LocalizationMessages.WSRM_1152_REMOVING_SEQUENCE(sequence.getId()));
                    sequenceKeyIterator.remove();
                    sequences.getReplicationManager().remove(key);

                    if (boundSequences.containsKey(sequence.getId())) {
                        boundSequences.remove(sequence.getId());
                    }
                } else if (shouldTeminate(sequence)) {
                    LOGGER.config(LocalizationMessages.WSRM_1153_TERMINATING_SEQUENCE(sequence.getId()));
                    tryTerminateSequence(sequence.getId());
                }
            }

        } finally {
            dataLock.writeLock().unlock();
            LOGGER.exiting();
        }
    }

    private boolean shouldTeminate(Sequence sequence) {
        return sequence.getState() != Sequence.State.TERMINATING && (sequence.isExpired() || sequence.getLastActivityTime() + sequenceInactivityTimeout < currentTimeInMillis());
    }

    private boolean shouldRemove(Sequence sequence) {
        // Right now we are going to remove all terminated sequences.
        // Later we may decide to introduce a timeout before a terminated
        // sequence is removed from the sequence storage
        return sequence.getState() == Sequence.State.TERMINATING;
    }

    public AbstractSequence load(String key) {
        SequenceDataPojo state = HighAvailabilityProvider.loadFrom(sequenceDataBs, key, null);
        if (state == null) {
            return null;
        }

        state.setBackingStore(sequenceDataBs);
        InVmSequenceData data = InVmSequenceData.loadReplica(null, state); // TODO HA time sync.
        return (state.isInbound()) ? new InboundSequence(data, this.outboundQueueBuilder, this) : new OutboundSequence(data, this.outboundQueueBuilder, this);
    }

    public String save(String key, AbstractSequence value, boolean isNew) {
        SequenceData _data = value.getData();
        if (!(_data instanceof InVmSequenceData)) {
            throw new IllegalArgumentException("Unsupported sequence data class: " + _data.getClass().getName());
        }

        InVmSequenceData data = (InVmSequenceData) _data;
        return HighAvailabilityProvider.saveTo(sequenceDataBs, key, data.getSequenceStatePojo(), isNew);
    }

    public void remove(String key) {
        HighAvailabilityProvider.removeFrom(sequenceDataBs, key);
    }

    public void close() {
        HighAvailabilityProvider.close(sequenceDataBs);
    }

    public void destroy() {
        HighAvailabilityProvider.destroy(sequenceDataBs);
    }
}
