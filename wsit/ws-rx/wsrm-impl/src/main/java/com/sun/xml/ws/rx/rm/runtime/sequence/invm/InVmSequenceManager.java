/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.commons.AbstractMOMRegistrationAware;
import com.sun.xml.ws.commons.MaintenanceTaskExecutor;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.ha.HighlyAvailableMap;
import com.sun.xml.ws.rx.ha.ReplicationManager;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.LocalIDManager;
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
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class InVmSequenceManager extends AbstractMOMRegistrationAware implements SequenceManager, ReplicationManager<String, AbstractSequence> {

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
    private final BackingStore<StickyKey, SequenceDataPojo> sequenceDataBs;
    /**
     * Internal in-memory map of bound sequences
     */
    private final HighlyAvailableMap<String, String> boundSequences;
    /**
     * Internal in-memory map of unacknowledged messages
     */
    private final HighlyAvailableMap<String, ApplicationMessage> unackedMessageStore;
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
    /**
     * Internal variable to store information about whether or not this instance 
     * of the SequenceManager is still valid.
     */
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    //
    private final String loggerProlog;
    
    private final LocalIDManager localIDManager;

    @SuppressWarnings("LeakingThisInConstructor")
    public InVmSequenceManager(String uniqueEndpointId, DeliveryQueueBuilder inboundQueueBuilder, DeliveryQueueBuilder outboundQueueBuilder, RmConfiguration configuration, Container container, LocalIDManager localIDManager) {
        this.loggerProlog = "[" + uniqueEndpointId + "_SEQUENCE_MANAGER]: ";
        this.uniqueEndpointId = uniqueEndpointId;
        this.inboundQueueBuilder = inboundQueueBuilder;
        this.outboundQueueBuilder = outboundQueueBuilder;
        this.localIDManager = localIDManager;

        this.sequenceInactivityTimeout = configuration.getRmFeature().getSequenceInactivityTimeout();

        this.actualConcurrentInboundSequences = new AtomicLong(0);
        this.maxConcurrentInboundSequences = configuration.getRmFeature().getMaxConcurrentSessions();

        final BackingStoreFactory bsFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);
        
        /*
         * We need to explicitly set the classloader that loads the Metro module classes
         * to workaround the GF HA issue http://java.net/jira/browse/GLASSFISH-15084
         * when value class is from the Java core lib.
         */ 
        final String boundSequencesBsName = uniqueEndpointId + "_BOUND_SEQUENCE_BS";
        final BackingStoreConfiguration<StickyKey, String> boundSequencesBsConfig = HighAvailabilityProvider.INSTANCE.initBackingStoreConfiguration(
                boundSequencesBsName,
                StickyKey.class,
                String.class);
        boundSequencesBsConfig.setClassLoader(this.getClass().getClassLoader());                
        final BackingStore<StickyKey, String> boundSequencesBs;
        try {
            boundSequencesBs = bsFactory.createBackingStore(boundSequencesBsConfig);
        } catch (BackingStoreException ex) {
            throw new RxRuntimeException(LocalizationMessages.WSRM_1142_ERROR_CREATING_HA_BACKING_STORE(boundSequencesBsName), ex);
        }
        this.boundSequences = HighlyAvailableMap.createSticky(
                uniqueEndpointId + "_BOUND_SEQUENCE_MAP", boundSequencesBs);

        this.sequenceDataBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                bsFactory,
                uniqueEndpointId + "_SEQUENCE_DATA_BS",
                StickyKey.class,
                SequenceDataPojo.class);
        this.sequences = HighlyAvailableMap.create(uniqueEndpointId + "_SEQUENCE_DATA_MAP", this);

        UnackedMessageReplicationManager unackedMsgRM = null;
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
            unackedMsgRM = new UnackedMessageReplicationManager(uniqueEndpointId);
        }

        this.unackedMessageStore = HighlyAvailableMap.create(uniqueEndpointId + "_UNACKED_MESSAGES_MAP", unackedMsgRM);

        MaintenanceTaskExecutor.register(
                new SequenceMaintenanceTask(this, configuration.getRmFeature().getSequenceManagerMaintenancePeriod(), TimeUnit.MILLISECONDS),
                configuration.getRmFeature().getSequenceManagerMaintenancePeriod(),
                TimeUnit.MILLISECONDS, container);
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

        SequenceData data = InVmSequenceData.newInstace(sequenceDataPojo, this, unackedMessageStore);
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

        SequenceData data = InVmSequenceData.newInstace(sequenceDataPojo, this, unackedMessageStore);
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
        if (sequenceId == null) {
            throw new UnknownSequenceException("[null-sequence-identifier]");
        }

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
    public Sequence getInboundSequence(String sequenceId) throws UnknownSequenceException {
        final Sequence sequence = getSequence(sequenceId);

        if (!(sequence instanceof InboundSequence)) {
            throw new UnknownSequenceException(sequenceId);
        }

        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    public Sequence getOutboundSequence(String sequenceId) throws UnknownSequenceException {
        final Sequence sequence = getSequence(sequenceId);

        if (!(sequence instanceof OutboundSequence)) {
            throw new UnknownSequenceException(sequenceId);
        }

        return sequence;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid(String sequenceId) {
        if (sequenceId == null) {
            return false;
        }
        try {
            dataLock.readLock().lock();
            Sequence s = sequences.get(sequenceId);
            return s != null && s.getState() != Sequence.State.TERMINATING;
        } finally {
            dataLock.readLock().unlock();
        }
    }

    private Sequence tryTerminateSequence(String sequenceId) {
        if (sequenceId == null) {
            return null;
        }
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

    public boolean onMaintenance() {
        LOGGER.entering();
        
        final boolean continueMaintenance = !this.disposed.get();
        
        try {
            dataLock.writeLock().lock();
            if (continueMaintenance) {
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
                        
                        if (localIDManager != null) {
                            localIDManager.markSequenceTermination(sequence.getId());
                        }
                    } else if (shouldTeminate(sequence)) {
                        LOGGER.config(LocalizationMessages.WSRM_1153_TERMINATING_SEQUENCE(sequence.getId()));
                        tryTerminateSequence(sequence.getId());
                    }
                }
            }
            
            return continueMaintenance;

        } finally {
            dataLock.writeLock().unlock();
            LOGGER.exiting(continueMaintenance);
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

    public void invalidateCache() {
        this.sequences.invalidateCache();
        this.boundSequences.invalidateCache();
        this.unackedMessageStore.invalidateCache();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Local cache invalidated");
        }
    }

    public void dispose() {
        if (this.disposed.compareAndSet(false, true)) {        
            this.sequences.close();
            this.sequences.destroy();

            this.boundSequences.close();
            this.boundSequences.destroy();

            this.unackedMessageStore.close();
            this.unackedMessageStore.destroy();            
        }
    }

    public AbstractSequence load(String key) {
        SequenceDataPojo state = HighAvailabilityProvider.loadFrom(sequenceDataBs, new StickyKey(key), null);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Sequence state data loaded from backing store for key [" + key + "]: " + ((state == null) ? null : state.toString()));
        }
        if (state == null) {
            return null;
        }

        state.setBackingStore(sequenceDataBs);
        InVmSequenceData data = InVmSequenceData.loadReplica(state, this, unackedMessageStore); // TODO HA time sync.

        final AbstractSequence sequence;
        if (state.isInbound()) {
            if (HaContext.failoverDetected() && !data.getUnackedMessageNumbers().isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(loggerProlog + "Unacked messages detected during failover of an inbound sequence data [" + data.getSequenceId() + "]: Registering as failed-over");
                }
                data.markUnackedAsFailedOver();
            }

            sequence = new InboundSequence(data, this.inboundQueueBuilder, this);
        } else {
            sequence = new OutboundSequence(data, this.outboundQueueBuilder, this);
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Sequence state data for key [" + key + "] converted into sequence of class: " + sequence.getClass());
        }
        return sequence;
    }

    public void save(String key, AbstractSequence sequence, boolean isNew) {
        SequenceData _data = sequence.getData();
        if (!(_data instanceof InVmSequenceData)) {
            throw new IllegalArgumentException("Unsupported sequence data class: " + _data.getClass().getName());
        }

        SequenceDataPojo value = ((InVmSequenceData) _data).getSequenceStatePojo();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Sending for replication sequence data with a key [" + key + "]: " + value.toString() + ", isNew=" + isNew);
        }

        HaInfo haInfo = HaContext.currentHaInfo();
        if (haInfo != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Existing HaInfo found, using it for sequence data replication: " + HaContext.asString(haInfo));
            }
            HaContext.udpateReplicaInstance(HighAvailabilityProvider.saveTo(sequenceDataBs, new StickyKey(key, haInfo.getKey()), value, isNew));
        } else {
            final StickyKey stickyKey = new StickyKey(key);
            final String replicaId = HighAvailabilityProvider.saveTo(sequenceDataBs, stickyKey, value, isNew);

            haInfo = new HaInfo(stickyKey.getHashKey(), replicaId, false);
            HaContext.updateHaInfo(haInfo);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "No HaInfo found, created new after sequence data replication: " + HaContext.asString(haInfo));
            }
        }
    }

    public void remove(String key) {
        HighAvailabilityProvider.removeFrom(sequenceDataBs, new StickyKey(key));
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Removed sequence data from the backing store for key [" + key + "]");
        }
    }

    public void close() {
        HighAvailabilityProvider.close(sequenceDataBs);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Closed sequence data backing store");
        }
    }

    public void destroy() {
        HighAvailabilityProvider.destroy(sequenceDataBs);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Destroyed sequence data backing store");
        }
    }
}
