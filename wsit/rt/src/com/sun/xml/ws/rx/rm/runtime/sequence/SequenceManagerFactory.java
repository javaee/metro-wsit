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
package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.xml.ws.rx.rm.runtime.sequence.invm.InVmSequenceManager;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.gmbal.ManagedObjectManager;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum SequenceManagerFactory {
    INSTANCE;

//    private final ReadWriteLock clientCacheLock = new ReentrantReadWriteLock();
//    private final Map<Object, SequenceManager> clientSequenceManagerCache = new WeakHashMap<Object, SequenceManager>();
    private final SequenceManager clientSequenceManager = new InVmSequenceManager(SequenceManager.Type.CLIENT, null);

    private final ReadWriteLock serviceCacheLock = new ReentrantReadWriteLock();
    private final Map<Object, SequenceManager> serviceSequenceManagerCache = new WeakHashMap<Object, SequenceManager>();
        
    private SequenceManagerFactory() {
        // TODO: load from external configuration and revert to default if not present
    }

    public SequenceManager getClientSequenceManager(ManagedObjectManager managedObjectManager) {
        // TODO change this once it is clear how to obtain endpoint on the client side
        return clientSequenceManager;
    }

    public SequenceManager getServerSequenceManager(Object correlationId, ManagedObjectManager managedObjectManager) {
        return getInMemorySequenceManager(correlationId, SequenceManager.Type.SERVICE, managedObjectManager, serviceSequenceManagerCache, serviceCacheLock);
    }

    private SequenceManager getInMemorySequenceManager(Object correlationId, SequenceManager.Type type, ManagedObjectManager mom, Map<Object, SequenceManager> cache, ReadWriteLock cacheLock) {
        try {
            cacheLock.readLock().lock();
            SequenceManager sequenceManager = cache.get(correlationId);
            if (sequenceManager == null) {
                cacheLock.readLock().unlock();
                try {
                    cacheLock.writeLock().lock();
                    sequenceManager = cache.get(correlationId);
                    if (sequenceManager == null) {
                        sequenceManager = new InVmSequenceManager(type, mom);
                        cache.put(correlationId, sequenceManager);
                    }
                } finally {
                    cacheLock.readLock().lock();
                    cacheLock.writeLock().unlock();
                }
            }

            return sequenceManager;
        } finally {
            cacheLock.readLock().unlock();
        }
    }
}
