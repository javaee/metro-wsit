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

import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.runtime.sequence.Sequence.AckRange;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * TODO javadoc
 * TODO make class thread-safe
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class OutboundSequence extends AbstractSequence {

    private static final RmLogger LOGGER = RmLogger.getLogger(OutboundSequence.class);
    //
    private final List<Long> unackedMessageIdentifiers;
    private final Map<Long, Object> weakMessageStorage;
    private final Map<Long, Long> weakIdtoCorrelationIdMap;
    

    public OutboundSequence(
            String sequenceId,
            String securityContextTokenId,
            long expirationTime) {
        super(sequenceId, securityContextTokenId, expirationTime, Sequence.MIN_MESSAGE_ID - 1);

        this.unackedMessageIdentifiers = new LinkedList<Long>();
        this.weakMessageStorage = new WeakHashMap<Long, Object>();
        this.weakIdtoCorrelationIdMap = new WeakHashMap<Long, Long>();
    }

    @Override
    protected Collection<Long> getUnackedMessageIdStorage() {
        return unackedMessageIdentifiers;
    }

    @Override
    public long generateNextMessageId() throws MessageNumberRolloverException, IllegalStateException {
        if (getStatus() != Sequence.Status.CREATED) {
            throw new IllegalStateException(LocalizationMessages.WSRM_1136_WRONG_SEQUENCE_STATE_NEXT_MESSAGE_ID_REJECTED(getId(), getStatus()));
        }

        try {
            messageIdLock.writeLock().lock();

            long nextId = getLastMessageId() + 1;
            if (nextId > Sequence.MAX_MESSAGE_ID) {
                throw LOGGER.logSevereException(new MessageNumberRolloverException(getId(), nextId));
            }

            updateLastMessageId(nextId);
            
            // making sure we have a new, uncached long object which GC can dispose later - used in storeMessage()
            unackedMessageIdentifiers.add(new Long(nextId)); 
            return nextId;
        } finally {
            messageIdLock.writeLock().unlock();
        }
    }

    @Override
    public void storeMessage(long correlationId, long id, Object message) throws UnsupportedOperationException {
        Long idKey;
        try {
            messageIdLock.readLock().lock();
            int index = unackedMessageIdentifiers.indexOf(id);
            if (index >= 0) { // the id is in the list
                idKey = unackedMessageIdentifiers.get(index);
            } else {
                idKey = new Long(id); // creating a new uncached long object that GC can dispose
            }            
        } finally {
            messageIdLock.readLock().unlock();
        }

        // we hold message while correlation id is still around in id-to-correlationId map
        // we hold correlation id while id is still around in unacked ids list
        Long correlationIdKey = new Long(correlationId);
        weakIdtoCorrelationIdMap.put(idKey, correlationIdKey);
        weakMessageStorage.put(correlationIdKey, message); 
    }

    @Override
    public Object retrieveMessage(long correlationId) throws UnsupportedOperationException {
        return weakMessageStorage.get(correlationId);
    }

    public void acknowledgeMessageId(long messageId) throws IllegalMessageIdentifierException {
        // NOTE: This method will most likely not be used in our implementation as we expect range-based 
        //       acknowledgements on outbound sequence. Thus we are not trying to optimize the implementation
        if (!unackedMessageIdentifiers.remove(messageId)) {
            throw new IllegalMessageIdentifierException(messageId);
        }
    }

    public void acknowledgeMessageIds(List<AckRange> ranges) throws IllegalMessageIdentifierException {
        try {
            messageIdLock.writeLock().lock();

            if (ranges == null || ranges.isEmpty()) {
                return;
            }

            if (ranges.size() > 1) {
                Collections.sort(ranges, new Comparator<AckRange>() {

                    public int compare(AckRange range1, AckRange range2) {
                        if (range1.lower <= range2.lower) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            }

            // check proper bounds of acked ranges
            AckRange lastAckRange = ranges.get(ranges.size() - 1);
            if (getLastMessageId() < lastAckRange.upper) {
                throw new IllegalMessageIdentifierException(lastAckRange.upper);
            }

            if (unackedMessageIdentifiers.isEmpty()) {
                // we have checked the ranges are ok and there's nothing to acknowledge.
                return;
            }

            // acknowledge messages
            Iterator<Long> unackedIterator = unackedMessageIdentifiers.iterator();
            Iterator<AckRange> rangeIterator = ranges.iterator();
            AckRange currentRange = rangeIterator.next();
            while (unackedIterator.hasNext()) {
                long unackedIndex = unackedIterator.next();
                if (unackedIndex >= currentRange.lower && unackedIndex <= currentRange.upper) {
                    unackedIterator.remove();
                } else if (rangeIterator.hasNext()) {
                    currentRange = rangeIterator.next();
                } else {
                    break; // no more acked ranges
                }
            }
        } finally {
            messageIdLock.writeLock().unlock();
        }
    }
}
