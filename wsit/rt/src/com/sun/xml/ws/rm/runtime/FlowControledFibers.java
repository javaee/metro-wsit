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
package com.sun.xml.ws.rm.runtime;

import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.commons.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
enum FlowControledFibers {

    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(FlowControledFibers.class);
    private static class FiberRegistration implements Comparable<FiberRegistration>{

        final PacketAdapter packetAdapter;
        final Fiber fiber;
        final long messageNumber;

        FiberRegistration(Fiber fiber, PacketAdapter packetAdapter) {
            this.fiber = fiber;
            this.packetAdapter = packetAdapter;
            this.messageNumber = packetAdapter.getMessageNumber();
        }

        @Override
        public boolean equals(Object that) {
            if (!(that instanceof FiberRegistration)) {
                return false;
            }

            final FiberRegistration thatRegistration = (FiberRegistration) that;
            if (this.messageNumber == thatRegistration.messageNumber) {
                return true;
            }
            
            return false;
        }

        @Override
        public int hashCode() {
            return 53 * 7 + (int) this.messageNumber;
        }

        public int compareTo(FiberRegistration that) {
            return (this.messageNumber < that.messageNumber) ? -1 : (this.messageNumber == that.messageNumber) ? 0 : 1;
        }
    }
    //
    private final Map<String, Queue<FiberRegistration>> repository;
    private final ReadWriteLock repositoryLock;
    
    private FlowControledFibers() {
        repository = new HashMap<String, Queue<FiberRegistration>>();
        repositoryLock = new ReentrantReadWriteLock();
    }

    boolean registerForResume(Fiber fiber, PacketAdapter packetAdapter) {       
        FiberRegistration registration = new FiberRegistration(fiber, packetAdapter);
        String sequenceId = packetAdapter.getSequenceId();

        try {
            repositoryLock.writeLock().lock();

            Queue<FiberRegistration> sequenceFibers;
            if (!repository.containsKey(sequenceId)) {
                sequenceFibers = new PriorityQueue<FiberRegistration>(10);
                repository.put(sequenceId, sequenceFibers);
            } else {
                sequenceFibers = repository.get(sequenceId);
            }

            // if the priority queue already contains the registration with given message number
            // the method returns false without actually registering the fiber and packetAdapter
            if (sequenceFibers.contains(registration)) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format("Duplicate registration: Another fiber already suspended for message [ %d ] on the sequence [ %s ]", registration.messageNumber, sequenceId));
                }
                return false;
            } else {
                boolean offerResult = sequenceFibers.offer(registration);
                
                if (!offerResult && LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format("Adding fiber to the suspended fiber's queue failed for message [ %d ] on the sequence [ %s ]", registration.messageNumber, sequenceId));
                }
                return offerResult;
            }
        } finally {
            repositoryLock.writeLock().unlock();
        }
    }

    boolean tryResume(String sequenceId, long messageId) {
        Queue<FiberRegistration> sequenceBuffer = getSequenceBuffer(sequenceId);

        if (sequenceBuffer == null || sequenceBuffer.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(String.format("Nothing to resume: No fibers suspended on the sequence [ %s ]", sequenceId));
            }
            return false;
        }

        FiberRegistration registration;
        try {
            repositoryLock.writeLock().lock();

            long nextSuspendedMessageId = sequenceBuffer.peek().packetAdapter.getMessageNumber();
            if (nextSuspendedMessageId != messageId) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format("No fiber resumed: Next suspended message [ %d ] is not as expected [ %d ] on the sequence [ %s ].", nextSuspendedMessageId, messageId, sequenceId));
                }
                return false;
            } else {
                registration = sequenceBuffer.poll();
            }
        } finally {
            repositoryLock.writeLock().unlock();
        }

        registration.fiber.resume(registration.packetAdapter.getPacket());
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Resuming fiber for the suspended message [ %d ] is on the sequence [ %s ].", registration.messageNumber, sequenceId));
        }
        return true;
    }

    int getUsedBufferSize(String sequenceId) {
        Queue<FiberRegistration> sequenceBuffer = getSequenceBuffer(sequenceId);

        if (sequenceBuffer == null) {
            return 0;
        }

        try {
            repositoryLock.readLock().lock();
            return sequenceBuffer.size();
        } finally {
            repositoryLock.readLock().unlock();
        }
    }

    private Queue<FiberRegistration> getSequenceBuffer(String sequenceId) {
        try {
            repositoryLock.readLock().lock();

            return repository.get(sequenceId);
        } finally {
            repositoryLock.readLock().unlock();
        }
    }
}
