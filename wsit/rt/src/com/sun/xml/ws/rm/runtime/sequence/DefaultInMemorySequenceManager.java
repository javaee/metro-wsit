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
package com.sun.xml.ws.rm.runtime.sequence;

import com.sun.xml.ws.rm.runtime.sequence.Sequence.Status;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class DefaultInMemorySequenceManager implements SequenceManager {

    private final ReadWriteLock internalDataAccessLock = new ReentrantReadWriteLock();
    private final Map<String, Sequence> sequences = new HashMap<String, Sequence>();
    private final Map<String, String> boundSequences = new HashMap<String, String>();

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

    public boolean isValid(String sequenceId) {
        try {
            internalDataAccessLock.readLock().lock();
            return sequences.containsKey(sequenceId);
        } finally {
            internalDataAccessLock.readLock().unlock();
        }
    }

    public Sequence createOutboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException {
        SequenceData data = new InMemorySequenceData(
                new LinkedList<Long>(), 
                sequenceId, 
                strId, 
                expirationTime, 
                Sequence.MIN_MESSAGE_ID - 1, Status.CREATING, false);

        return registerSequence(new OutboundSequence(data));
    }

    public Sequence createInboundSequence(String sequenceId, String strId, long expirationTime) throws DuplicateSequenceException {
        SequenceData data = new InMemorySequenceData(new TreeSet<Long>(), sequenceId, strId, expirationTime, Sequence.UNSPECIFIED_MESSAGE_ID, Status.CREATING, false);

        return registerSequence(new InboundSequence(data));
    }

    public String generateSequenceUID() {
        return "uuid:" + UUID.randomUUID();
    }

    public void closeSequence(String sequenceId) throws UnknownSequenceException {
        Sequence sequence = getSequence(sequenceId);
        sequence.close();
    }

    public Sequence terminateSequence(String sequenceId) throws UnknownSequenceException {
        try {
            internalDataAccessLock.writeLock().lock();
            if (sequences.containsKey(sequenceId)) {
                Sequence sequence = sequences.remove(sequenceId);

                if (boundSequences.containsKey(sequenceId)) {
                    boundSequences.remove(sequenceId);
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
     * Registers a new sequence in the internal sequence storage
     * 
     * @param sequence sequence object to be registered within the internal sequence storage
     */
    private Sequence registerSequence(Sequence sequence) throws DuplicateSequenceException {
        try {
            internalDataAccessLock.writeLock().lock();
            if (sequences.containsKey(sequence.getId())) {
                throw new DuplicateSequenceException(sequence.getId());
            } else {
                sequences.put(sequence.getId(), sequence);
            }

            return sequence;
        } finally {
            internalDataAccessLock.writeLock().unlock();
        }
    }

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

    public Sequence getBoundSequence(String referenceSequenceId) throws UnknownSequenceException {
        try {
            internalDataAccessLock.readLock().lock();
            if (!isValid(referenceSequenceId)) {
                throw new UnknownSequenceException(referenceSequenceId);
            }

            if (boundSequences.containsKey(referenceSequenceId)) {
                return sequences.get(boundSequences.get(referenceSequenceId));
            } else {
                return null;
            }
        } finally {
            internalDataAccessLock.readLock().unlock();
        }
    }
}
