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

import com.sun.xml.ws.rx.rm.runtime.delivery.DeliveryQueueBuilder;
import com.sun.xml.ws.rx.rm.runtime.sequence.AbstractSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.DuplicateSequenceException;
import com.sun.xml.ws.rx.rm.runtime.sequence.InboundSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.OutboundSequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence;
import com.sun.xml.ws.rx.rm.runtime.sequence.Sequence.State;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceData;
import com.sun.xml.ws.rx.rm.runtime.sequence.SequenceManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.UnknownSequenceException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
@ManagedObject
@Description("In Memory RM Sequence Manager")
@AMXMetadata(type = "RMSequenceManager", pathPart = "RMSequenceManager")
public final class InVmSequenceManager implements SequenceManager {

    /**
     * Internal in-memory data access lock
     */
    private final ReadWriteLock internalDataAccessLock = new ReentrantReadWriteLock();
    /**
     * Internal in-memory storage of sequence data
     */
    private final Map<String, AbstractSequence> sequences = new HashMap<String, AbstractSequence>();
    /**
     * Internal in-memory map of bound sequences
     */
    private final Map<String, String> boundSequences = new HashMap<String, String>();
    /**
     * Monitoring manager
     */
    private final ManagedObjectManager managedObjectManager;

    public InVmSequenceManager(SequenceManager.Type type, ManagedObjectManager managedObjectManager) {
        this.managedObjectManager = managedObjectManager;
        if (managedObjectManager != null) {
            managedObjectManager.registerAtRoot(this, type.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, ? extends Sequence> sequences() {
        return sequences;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> boundSequences() {
        return boundSequences;
    }

    /**
     * {@inheritDoc}
     */
    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime, DeliveryQueueBuilder deliveryQueueBuilder) throws DuplicateSequenceException {
        SequenceData data = new InVmSequenceData(sequenceId, strId, expirationTime, OutboundSequence.INITIAL_LAST_MESSAGE_ID);
        return registerSequence(new OutboundSequence(data, deliveryQueueBuilder));
    }

    /**
     * {@inheritDoc}
     */
    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime, DeliveryQueueBuilder deliveryQueueBuilder) throws DuplicateSequenceException {
        SequenceData data = new InVmSequenceData(sequenceId, strId, expirationTime, InboundSequence.INITIAL_LAST_MESSAGE_ID);
        return registerSequence(new InboundSequence(data, deliveryQueueBuilder));
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
            internalDataAccessLock.readLock().lock();
            if (sequences.containsKey(sequenceId)) {
                return sequences.get(sequenceId);
            } else {
                throw new UnknownSequenceException(sequenceId);
            }
        } finally {
            internalDataAccessLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid(String sequenceId) {
        try {
            internalDataAccessLock.readLock().lock();
            Sequence s = sequences.get(sequenceId);
            return s != null && s.getState() != Sequence.State.TERMINATING;
        } finally {
            internalDataAccessLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Sequence terminateSequence(String sequenceId) throws UnknownSequenceException {
        try {
            internalDataAccessLock.writeLock().lock();
            if (sequences.containsKey(sequenceId)) {
                AbstractSequence sequence = sequences.remove(sequenceId);
                sequence.setState(State.TERMINATING);

                if (boundSequences.containsKey(sequenceId)) {
                    boundSequences.remove(sequenceId);

                }

                if (managedObjectManager != null) {
                    managedObjectManager.unregister(sequence);
                }
                sequence.preDestroy();

                return sequence;
            } else {
                throw new UnknownSequenceException(sequenceId);
            }
        } finally {
            internalDataAccessLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void bindSequences(String referenceSequenceId, String boundSequenceId) throws UnknownSequenceException {
        try {
            internalDataAccessLock.writeLock().lock();
            if (!sequences.containsKey(referenceSequenceId)) {
                throw new UnknownSequenceException(referenceSequenceId);
            }

            if (!sequences.containsKey(boundSequenceId)) {
                throw new UnknownSequenceException(boundSequenceId);
            }

            boundSequences.put(referenceSequenceId, boundSequenceId);
        } finally {
            internalDataAccessLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Sequence getBoundSequence(String referenceSequenceId) throws UnknownSequenceException {
        try {
            internalDataAccessLock.readLock().lock();
            if (!isValid(referenceSequenceId)) {
                throw new UnknownSequenceException(referenceSequenceId);
            }

            return (boundSequences.containsKey(referenceSequenceId)) ? sequences.get(boundSequences.get(referenceSequenceId)) : null;
        } finally {
            internalDataAccessLock.readLock().unlock();
        }
    }

    /**
     * Registers a new sequence in the internal sequence storage
     *
     * @param sequence sequence object to be registered within the internal sequence storage
     */
    private Sequence registerSequence(AbstractSequence sequence) throws DuplicateSequenceException {
        try {
            internalDataAccessLock.writeLock().lock();
            if (sequences.containsKey(sequence.getId())) {
                throw new DuplicateSequenceException(sequence.getId());
            } else {
                sequences.put(sequence.getId(), sequence);
                if (managedObjectManager != null) {
                    managedObjectManager.register(this, sequence, sequence.getId().replace(':', '-'));
                }
            }

            return sequence;
        } finally {
            internalDataAccessLock.writeLock().unlock();
        }
    }
}
