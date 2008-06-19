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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum FlowControledFibers {

    INSTANCE;

    private static class FiberRegistration {

        PacketAdapter packetAdapter;
        Fiber fiber;

        FiberRegistration(Fiber fiber, PacketAdapter packetAdapter) {
            this.fiber = fiber;
            this.packetAdapter = packetAdapter;
        }
    }
    private final Map<String, Queue<FiberRegistration>> repository;
    private final ReadWriteLock repositoryLock;
    private final Comparator<FiberRegistration> registrationComparator;

    private FlowControledFibers() {
        repository = new HashMap<String, Queue<FiberRegistration>>();
        repositoryLock = new ReentrantReadWriteLock();
        registrationComparator = new Comparator<FiberRegistration>() {

            public int compare(FiberRegistration r1, FiberRegistration r2) {
                return (r1.packetAdapter.getMessageNumber() < r2.packetAdapter.getMessageNumber()) ? -1 : (r1.packetAdapter.getMessageNumber() == r2.packetAdapter.getMessageNumber()) ? 0 : 1;
            }
        };
    }

    public void registerForResume(Fiber fiber, PacketAdapter packetAdapter) {
        FiberRegistration registration = new FiberRegistration(fiber, packetAdapter);
        String sequenceId = packetAdapter.getSequenceId();

        try {
            repositoryLock.writeLock().lock();

            Queue<FiberRegistration> sequenceFibers;
            if (!repository.containsKey(sequenceId)) {
                sequenceFibers = new PriorityQueue<FiberRegistration>(10, registrationComparator);
                repository.put(sequenceId, sequenceFibers);
            } else {
                sequenceFibers = repository.get(sequenceId);
            }

            sequenceFibers.add(registration);
        } finally {
            repositoryLock.writeLock().unlock();
        }
    }

    public boolean tryResume(String sequenceId, long messageId) {
        Queue<FiberRegistration> sequenceBuffer = getSequenceBuffer(sequenceId);

        if (sequenceBuffer == null) {
            return false;
        }

        FiberRegistration registration;
        try {
            repositoryLock.writeLock().lock();

            if (sequenceBuffer.peek().packetAdapter.getMessageNumber() != messageId) {
                return false;
            } else {
                registration = sequenceBuffer.poll();
            }
        } finally {
            repositoryLock.writeLock().lock();
        }

        registration.fiber.resume(registration.packetAdapter.getPacket());
        return true;
    }

    public int getUsedBufferSize(String sequenceId) {
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
