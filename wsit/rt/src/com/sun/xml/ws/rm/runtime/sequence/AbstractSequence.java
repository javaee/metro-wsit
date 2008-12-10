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
import com.sun.xml.ws.rm.runtime.RxConfiguration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO javadoc
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
abstract class AbstractSequence implements Sequence {

    protected final ReadWriteLock messageIdLock = new ReentrantReadWriteLock(); // lock used to synchronize the access to the lastMessageId and unackedMessageIdentifiersStorage variables     
    //
    private final String sequenceId;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    //
    private final AtomicReference<Status> status;
    private final AtomicBoolean ackRequestedFlag;
    //
    private long lastMessageId;
    private long lastActivityTime;

    /**
     * Initializes instance fields.
     * 
     * @param id sequence identifier
     * 
     * @param securityContextTokenId security context token identifier bound to this sequence
     * 
     * @param expirationTime sequence expiration time
     */
    AbstractSequence(
            String sequenceId,
            String securityContextTokenId,
            long expirationTime,
            long initalLastMessageId) {

        this.sequenceId = sequenceId;
        this.boundSecurityTokenReferenceId = securityContextTokenId;
        this.expirationTime = expirationTime;
        this.status = new AtomicReference<Status>(Status.CREATED);
        this.ackRequestedFlag = new AtomicBoolean(false);
        this.lastActivityTime = System.currentTimeMillis();
        this.lastMessageId = initalLastMessageId;
    }

    public String getId() {
        return sequenceId; // no need to synchronize
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }

    public long generateNextMessageId() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(LocalizationMessages.WSRM_1101_UNSUPPORTED_INTERFACE_OPERATION_IN_IMPLEMENTATION(Sequence.class.getName()));
    }


    public long getLastMessageId() {
        try {
            messageIdLock.readLock().lock();
            return lastMessageId;
        } finally {
            messageIdLock.readLock().unlock();
        }
    }
    
    public void storeMessage(long correlationId, long id, Object message) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(LocalizationMessages.WSRM_1101_UNSUPPORTED_INTERFACE_OPERATION_IN_IMPLEMENTATION(Sequence.class.getName()));
    }

    public Object retrieveMessage(long correlationId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(LocalizationMessages.WSRM_1101_UNSUPPORTED_INTERFACE_OPERATION_IN_IMPLEMENTATION(Sequence.class.getName()));
    }

    public List<AckRange> getAcknowledgedMessageIds() {
        messageIdLock.readLock().lock();
        try {
            if (getLastMessageId() == Sequence.UNSPECIFIED_MESSAGE_ID) {
                // no message associated with the sequence yet
                return Collections.emptyList();
            } else if (getUnackedMessageIdStorage().isEmpty()) {
                // no unacked indexes - we have a single acked range
                return Arrays.asList(new AckRange(Sequence.MIN_MESSAGE_ID, getLastMessageId()));
            } else {
                // need to calculate ranges from the unacked indexes
                List<AckRange> result = new LinkedList<Sequence.AckRange>();

                Iterator<Long> unackedIndexIterator = getUnackedMessageIdStorage().iterator();
                long lastBottomAckRange = Sequence.MIN_MESSAGE_ID;
                while (unackedIndexIterator.hasNext()) {
                    long lastUnacked = unackedIndexIterator.next();
                    if (lastBottomAckRange < lastUnacked) {
                        result.add(new AckRange(lastBottomAckRange, lastUnacked - 1));
                    }
                    lastBottomAckRange = lastUnacked + 1;
                }
                if (lastBottomAckRange <= getLastMessageId()) {
                    result.add(new AckRange(lastBottomAckRange, getLastMessageId()));
                }
                
                
                
                return result;
            }
        } finally {
            messageIdLock.readLock().unlock();
        }
    }

    public boolean isAcknowledged(long messageId) {
        try{
            messageIdLock.readLock().lock();
            if (messageId > getLastMessageId()) {
                return false;
            }

            return !getUnackedMessageIdStorage().contains(messageId);
        } finally {
            messageIdLock.readLock().unlock();
        }
    }       

    public boolean hasPendingAcknowledgements() {
        try {
            messageIdLock.readLock().lock();
            return !getUnackedMessageIdStorage().isEmpty();
        } finally {
            messageIdLock.readLock().unlock();
        }
    }

    public Status getStatus() {
        return status.get();
    }

    public void setAckRequestedFlag() {
        ackRequestedFlag.set(true);
    }

    public void clearAckRequestedFlag() {
        ackRequestedFlag.set(false);
    }

    public boolean isAckRequested() {
        return ackRequestedFlag.get();
    }

    public void close() {
        status.set(Status.CLOSED);
    }

    public boolean isClosed() {
        Status currentStatus = status.get();
        return currentStatus == Status.CLOSING || currentStatus == Status.CLOSED || currentStatus == Status.TERMINATING;
    }

    public boolean isExpired() {
        return (expirationTime == Sequence.NO_EXPIRATION) ? false : System.currentTimeMillis() < expirationTime;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    public void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }

    public void preDestroy() {
        // nothing to do...
    }

    abstract Collection<Long> getUnackedMessageIdStorage();

    final long updateLastMessageId(long newValue) {
        try {
            messageIdLock.writeLock().lock();
            long oldValue = lastMessageId;
            lastMessageId = newValue;
            return oldValue;
        } finally {
            messageIdLock.writeLock().unlock();
        }        
    }
    
    final void setStatus(Status newStatus) {
        status.set(newStatus);
    }
}
