/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.glassfish.ha.store.api.BackingStore;

/**
 * This class represents a plain sequence data POJO bean which is used as an internal
 * state holder for {@link InVmSequenceData} instances as well as for HA replication
 * sequence state replication.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class SequenceDataPojo implements Serializable /*Storeable*/ {
    private static final Logger LOGGER = Logger.getLogger(SequenceDataPojo.class);

    static final long serialVersionUID = -5024744406713321676L;
    //
    private transient BackingStore<StickyKey, SequenceDataPojo> backingStore;
    //
    private String sequenceId;
    private String boundSecurityTokenReferenceId;
    private long expirationTime;
    //
    private volatile State state;
    private volatile boolean ackRequestedFlag;
    private volatile long lastMessageNumber;
    private volatile long lastActivityTime;
    private volatile long lastAcknowledgementRequestTime;
    //
    private Set<Long> allUnackedMessageNumbers;
    private Set<Long> receivedUnackedMessageNumbers;
    private Set<Long> failedOverUnackedMessageNumbers;
    //
    private Map<Long, String> unackedNumberToCorrelationIdMap;
    private boolean inbound;

    protected SequenceDataPojo() {
    }

    public SequenceDataPojo(
            String sequenceId,
            String boundSecurityTokenReferenceId,
            long expirationTime,
            boolean isInbound,
            BackingStore<StickyKey, SequenceDataPojo> bs) {

        this.sequenceId = sequenceId;
        this.boundSecurityTokenReferenceId = boundSecurityTokenReferenceId;
        this.expirationTime = expirationTime;
        this.inbound = isInbound;

        this.allUnackedMessageNumbers = new TreeSet<Long>();
        this.receivedUnackedMessageNumbers = new HashSet<Long>();
        this.failedOverUnackedMessageNumbers = new HashSet<Long>();
        //
        this.unackedNumberToCorrelationIdMap = new HashMap<Long, String>();
        //
        this.backingStore = bs;
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public boolean getAckRequestedFlag() {
        return ackRequestedFlag;
    }

    public void setAckRequestedFlag(boolean ackRequestedFlag) {
        this.ackRequestedFlag = ackRequestedFlag;
        dirty(Parameter.ackRequestedFlag);
    }

    public long getLastAcknowledgementRequestTime() {
        return lastAcknowledgementRequestTime;
    }

    public void setLastAcknowledgementRequestTime(long lastAcknowledgementRequestTime) {
        this.lastAcknowledgementRequestTime = lastAcknowledgementRequestTime;
        dirty(Parameter.lastAcknowledgementRequestTime);
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(long lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
        dirty(Parameter.lastActivityTime);
    }

    public long getLastMessageNumber() {
        return lastMessageNumber;
    }

    public void setLastMessageNumber(long lastMessageNumber) {
        this.lastMessageNumber = lastMessageNumber;
        dirty(Parameter.lastMessageNumber);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        dirty(Parameter.state);
    }

    public Set<Long> getAllUnackedMessageNumbers() {
        return allUnackedMessageNumbers;
    }

    public Set<Long> getReceivedUnackedMessageNumbers() {
        return receivedUnackedMessageNumbers;
    }

    public Set<Long> getFailedOverUnackedMessageNumbers() {
        return failedOverUnackedMessageNumbers;
    }

    public Map<Long, String> getUnackedNumberToCorrelationIdMap() {
        return unackedNumberToCorrelationIdMap;
    }

    public boolean isInbound() {
        return inbound;
    }

    public void setBackingStore(BackingStore<StickyKey, SequenceDataPojo> backingStore) {
        this.backingStore = backingStore;
    }

    public void replicate() {
        if (backingStore != null && dirty) {
            HaInfo haInfo = HaContext.currentHaInfo();
            if (haInfo != null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Sequence " + sequenceId + "]: Existing HaInfo found, using it for sequence state data replication: " + HaContext.asString(haInfo));
                }

                HaContext.udpateReplicaInstance(HighAvailabilityProvider.saveTo(backingStore, new StickyKey(sequenceId, haInfo.getKey()), this, false));
            } else {
                final StickyKey stickyKey = new StickyKey(sequenceId);
                final String replicaId = HighAvailabilityProvider.saveTo(backingStore, stickyKey, this, false);

                haInfo = new HaInfo(stickyKey.getHashKey(), replicaId, false);
                HaContext.updateHaInfo(haInfo);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Sequence " + sequenceId + "]: No HaInfo found, created new after sequence state data replication: " + HaContext.asString(haInfo));
                }
            }
        }
        resetDirty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SequenceDataPojo other = (SequenceDataPojo) obj;
        if ((this.sequenceId == null) ? (other.sequenceId != null) : !this.sequenceId.equals(other.sequenceId)) {
            return false;
        }
        if ((this.boundSecurityTokenReferenceId == null) ? (other.boundSecurityTokenReferenceId != null) : !this.boundSecurityTokenReferenceId.equals(other.boundSecurityTokenReferenceId)) {
            return false;
        }
        if (this.expirationTime != other.expirationTime) {
            return false;
        }
        if (this.state != other.state) {
            return false;
        }
        if (this.ackRequestedFlag != other.ackRequestedFlag) {
            return false;
        }
        if (this.lastMessageNumber != other.lastMessageNumber) {
            return false;
        }
        if (this.lastActivityTime != other.lastActivityTime) {
            return false;
        }
        if (this.lastAcknowledgementRequestTime != other.lastAcknowledgementRequestTime) {
            return false;
        }
        if (this.allUnackedMessageNumbers != other.allUnackedMessageNumbers && (this.allUnackedMessageNumbers == null || !this.allUnackedMessageNumbers.equals(other.allUnackedMessageNumbers))) {
            return false;
        }
        if (this.receivedUnackedMessageNumbers != other.receivedUnackedMessageNumbers && (this.receivedUnackedMessageNumbers == null || !this.receivedUnackedMessageNumbers.equals(other.receivedUnackedMessageNumbers))) {
            return false;
        }
        if (this.unackedNumberToCorrelationIdMap != other.unackedNumberToCorrelationIdMap && (this.unackedNumberToCorrelationIdMap == null || !this.unackedNumberToCorrelationIdMap.equals(other.unackedNumberToCorrelationIdMap))) {
            return false;
        }
        if (this.inbound != other.inbound) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.sequenceId != null ? this.sequenceId.hashCode() : 0);
        hash = 61 * hash + (this.boundSecurityTokenReferenceId != null ? this.boundSecurityTokenReferenceId.hashCode() : 0);
        hash = 61 * hash + (int) (this.expirationTime ^ (this.expirationTime >>> 32));
        hash = 61 * hash + (this.state != null ? this.state.hashCode() : 0);
        hash = 61 * hash + (this.ackRequestedFlag ? 1 : 0);
        hash = 61 * hash + (int) (this.lastMessageNumber ^ (this.lastMessageNumber >>> 32));
        hash = 61 * hash + (int) (this.lastActivityTime ^ (this.lastActivityTime >>> 32));
        hash = 61 * hash + (int) (this.lastAcknowledgementRequestTime ^ (this.lastAcknowledgementRequestTime >>> 32));
        hash = 61 * hash + (this.allUnackedMessageNumbers != null ? this.allUnackedMessageNumbers.hashCode() : 0);
        hash = 61 * hash + (this.receivedUnackedMessageNumbers != null ? this.receivedUnackedMessageNumbers.hashCode() : 0);
        hash = 61 * hash + (this.failedOverUnackedMessageNumbers != null ? this.failedOverUnackedMessageNumbers.hashCode() : 0);
        hash = 61 * hash + (this.unackedNumberToCorrelationIdMap != null ? this.unackedNumberToCorrelationIdMap.hashCode() : 0);
        hash = 61 * hash + (this.inbound ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "SequenceDataPojo"
                + "{\n\tbackingStore=" + backingStore
                + ",\n\tsequenceId=" + sequenceId
                + ",\n\tboundSecurityTokenReferenceId=" + boundSecurityTokenReferenceId
                + ",\n\texpirationTime=" + expirationTime
                + ",\n\tstate=" + state
                + ",\n\tackRequestedFlag=" + ackRequestedFlag
                + ",\n\tlastMessageNumber=" + lastMessageNumber
                + ",\n\tlastActivityTime=" + lastActivityTime
                + ",\n\tlastAcknowledgementRequestTime=" + lastAcknowledgementRequestTime
                + ",\n\tallUnackedMessageNumbers=" + allUnackedMessageNumbers
                + ",\n\treceivedUnackedMessageNumbers=" + receivedUnackedMessageNumbers
                + ",\n\tfailedOverUnackedMessageNumbers=" + failedOverUnackedMessageNumbers
                + ",\n\tunackedNumberToCorrelationIdMap=" + unackedNumberToCorrelationIdMap
                + ",\n\tinbound=" + inbound
                + ",\n\tdirty=" + dirty
                + "\n}";
    }

    private static enum Parameter {

        sequenceId("sequenceId", 0),
        boundSecurityTokenReferenceId("boundSecurityTokenReferenceId", 1),
        expirationTime("expirationTime", 2),
        //
        state("state", 3),
        ackRequestedFlag("ackRequestedFlag", 4),
        lastMessageNumber("lastMessageNumber", 5),
        lastActivityTime("lastActivityTime", 6),
        lastAcknowledgementRequestTime("lastAcknowledgementRequestTime", 7),
        //
        allUnackedMessageNumbers("allUnackedMessageNumbers", 8),
        receivedUnackedMessageNumbers("receivedUnackedMessageNumbers", 9),
        failedOverUnackedMessageNumbers("failedOverUnackedMessageNumbers", 10),
        //
        unackedNumberToCorrelationIdMap("unackedNumberToCorrelationIdMap", 11),
        inbound("inbound", 12);
        //
        public final String name;
        public final int index;

        private Parameter(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }
    private volatile boolean dirty = false;

    private void dirty(Parameter p) {
//        _storeable_dirtyStatus[p.index] = true;
        dirty = true;
    }

    public void resetDirty() {
        dirty = false;
//        Arrays.fill(_storeable_dirtyStatus, false);
    }
//    //
//    private static final String[] _storeable_attributeNames = new String[Parameter.values().length];
//    //
//    private long _storeable_version = 0;
//    private long _storeable_lastAccessTime = 0;
//    private long _storeable_maxIdleTime = 0;
//    private boolean[] _storeable_dirtyStatus = new boolean[Parameter.values().length];
//
//    static {
//        for (Parameter p : Parameter.values()) {
//            _storeable_attributeNames[p.index] = p.name;
//        }
//    }
//
//    public String[] _storeable_getAttributeNames() {
//        return Arrays.copyOf(_storeable_attributeNames, _storeable_attributeNames.length);
//    }
//
//    public boolean[] _storeable_getDirtyStatus() {
//        return Arrays.copyOf(_storeable_dirtyStatus, _storeable_dirtyStatus.length);
//    }
//
//    public long _storeable_getLastAccessTime() {
//        return _storeable_lastAccessTime;
//    }
//
//    public long _storeable_getMaxIdleTime() {
//        return _storeable_maxIdleTime;
//    }
//
//    public long _storeable_getVersion() {
//        return _storeable_version;
//    }
//
//    public void _storeable_setLastAccessTime(long value) {
//        _storeable_lastAccessTime = value;
//    }
//
//    public void _storeable_setMaxIdleTime(long value) {
//        _storeable_maxIdleTime = value;
//    }
//
//    public void _storeable_setVersion(long value) {
//        _storeable_version = value;
//    }
//
//    public void _storeable_readState(InputStream is) throws IOException {
//        ObjectInputStream ois = new ObjectInputStream(is);
//
//        SequenceDataPojo data;
//        try {
//            data = (SequenceDataPojo) ois.readObject();
//        } catch (ClassNotFoundException ex) {
//            throw new IOException("Unable to read SequenceDataPojo instance from stream", ex);
//        }
//
//        this.sequenceId = data.sequenceId;
//        this.boundSecurityTokenReferenceId = data.boundSecurityTokenReferenceId;
//        this.expirationTime = data.expirationTime;
//        //
//        this.state = data.state;
//        this.ackRequestedFlag = data.ackRequestedFlag;
//        this.lastMessageNumber = data.lastMessageNumber;
//        this.lastActivityTime = data.lastActivityTime;
//        this.lastAcknowledgementRequestTime = data.lastAcknowledgementRequestTime;
//        //
//        this.allUnackedMessageNumbers = data.allUnackedMessageNumbers;
//        this.receivedUnackedMessageNumbers = data.receivedUnackedMessageNumbers;
//        //
//        this.unackedNumberToCorrelationIdMap = data.unackedNumberToCorrelationIdMap;
//        this.inbound = data.inbound;
//        //
//        //
//        this._storeable_version = data._storeable_version;
//        this._storeable_lastAccessTime = data._storeable_lastAccessTime;
//        this._storeable_maxIdleTime = data._storeable_maxIdleTime;
//
//        resetDirty();
//    }
//
//    public void _storeable_writeState(OutputStream os) throws IOException {
//        ObjectOutputStream oos = new ObjectOutputStream(os);
//        oos.writeObject(this);
//    }
}
