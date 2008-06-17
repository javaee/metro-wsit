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
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class InMemorySequenceData implements SequenceData {

    private final String sequenceId;
    private final String boundSecurityTokenReferenceId;
    private final long expirationTime;
    //
    private final Collection<Long> unackedMessageIdentifiersStorage;
    private final ReadWriteLock messageIdLock = new ReentrantReadWriteLock(); // lock used to synchronize the access to the lastMessageId and unackedMessageIdentifiersStorage variables 
    //
    private long lastActivityTime;
    private Status status;
    private boolean ackRequestedFlag;
    private long lastMessageId;

    public InMemorySequenceData(
            Collection<Long> unackedMessageIdentifiersStorage, 
            String sequenceId,
            String boundSecurityTokenReferenceId,
            long expirationTime,
            long lastMessageId, 
            Status status, 
            boolean ackRequestedFlag) {
        this.unackedMessageIdentifiersStorage = unackedMessageIdentifiersStorage;
        this.sequenceId = sequenceId;
        this.boundSecurityTokenReferenceId = boundSecurityTokenReferenceId;
        this.expirationTime = expirationTime;
        this.lastActivityTime = System.currentTimeMillis();
        this.lastMessageId = lastMessageId;
        this.status = status;
        this.ackRequestedFlag = ackRequestedFlag;
    }

    public boolean isAckRequestedFlag() {
        return ackRequestedFlag;
    }

    public void setAckRequestedFlag(boolean ackRequestedFlag) {
        this.ackRequestedFlag = ackRequestedFlag;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getExpirationTime() {
        return expirationTime; // no need to synchronize

    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void updateLastActivityTime() {
        this.lastActivityTime = System.currentTimeMillis();
    }
    
    public String getSequenceId() {
        return sequenceId; // no need to synchronize

    }

    public long getLastMessageId() {
        try {
            messageIdLock.readLock().lock();
            return lastMessageId;
        } finally {
            messageIdLock.readLock().unlock();
        }
    }

    public long updateLastMessageId(long newId) {
        try {
            messageIdLock.writeLock().lock();
            long oldValue = lastMessageId;
            lastMessageId = newId;
            return oldValue;
        } finally {
            messageIdLock.writeLock().unlock();
        }
    }

    public long incrementAndGetLastMessageId() {
        try {
            messageIdLock.writeLock().lock();
            lastMessageId++;
            return lastMessageId;
        } finally {
            messageIdLock.writeLock().unlock();
        }
    }

    public Collection<Long> getAllUnackedIndexes() {
        try {
            messageIdLock.readLock().lock();
            return unackedMessageIdentifiersStorage;
        } finally {
            messageIdLock.readLock().unlock();
        }
    }

    public boolean noUnackedMessageIds() {
        try {
            messageIdLock.readLock().lock();
            return unackedMessageIdentifiersStorage.isEmpty();
        } finally {
            messageIdLock.readLock().unlock();
        }
    }

    public void addUnackedMessageId(long messageId) {
        try {
            messageIdLock.writeLock().lock();
            unackedMessageIdentifiersStorage.add(messageId);
        } finally {
            messageIdLock.writeLock().unlock();            
        }
    }

    public boolean removeUnackedMessageId(long messageId) {
        try {
            messageIdLock.writeLock().lock();
            return unackedMessageIdentifiersStorage.remove(messageId);
        } finally {
            messageIdLock.writeLock().unlock();            
        }
    }
    
    public void acquireMessageIdDataReadOnlyLock() {
        messageIdLock.readLock().lock();
    }

    public void releaseMessageIdDataReadOnlyLock() {
        messageIdLock.readLock().unlock();
    }
    
    public void acquireMessageIdDataReadWriteLock() {
        messageIdLock.writeLock().lock();
    }

    public void releaseMessageIdDataReadWriteLock() {
        messageIdLock.writeLock().unlock();
    }

    public String getBoundSecurityTokenReferenceId() {
        return boundSecurityTokenReferenceId;
    }
}
