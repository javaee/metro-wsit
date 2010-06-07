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
package com.sun.xml.ws.rx.util;

import com.sun.istack.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO javadoc
 *
 * <b>
 * WARNING: This class is a private utility class used by WS-RX implementation. Any usage outside
 * the intedned scope is strongly discouraged. The API exposed by this class may be changed, replaced
 * or removed without any advance notice.
 * </b>
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class TimestampedCollection<K, V> {

    public static <K, V> TimestampedCollection<K, V> newInstance() {
        return new TimestampedCollection<K, V>();
    }

    //
    private static class TimestampedRegistration<K, V> implements Comparable<TimestampedRegistration<K,V>> {

        private final long timestamp;
        private final K key;
        private final @NotNull V value;

        public TimestampedRegistration(long timestamp, K key, @NotNull V value) {
            this.timestamp = timestamp;
            this.key = key;
            this.value = value;
        }

        public int compareTo(TimestampedRegistration<K, V> other) {
            return this.timestamp < other.timestamp ? -1 : this.timestamp == other.timestamp ? 0 : 1;
        }
    }
    /**
     * Primary registration collection
     */
    private final PriorityQueue<TimestampedRegistration<K, V>> timestampedPriorityQueue = new PriorityQueue<TimestampedRegistration<K, V>>();
    /**
     * Correlation key to registration mapping, may contain fewer elements than the whole collection.
     */
    private final Map<K, TimestampedRegistration<K, V>> correlationMap = new HashMap<K, TimestampedRegistration<K, V>>();
    /**
     * Data access lock
     */
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    TimestampedCollection() { 
        // package private constructor - only classes in this package can extend this class
    }

    /**
     * Registers a {@code subject} and maps it to a given {@code correlationId}.
     * The newly created registration is timestamped which allows for later removal
     * based on the age of registration using {@link #removeOldest()} method.
     *
     * @param correlationId correlation identifier to be associated with a given {@code subject}
     * @param subject a primary registration object
     *
     * @return old {@code subject} associated with a given {@code correlationId}
     *         or {@code null} if there's no such {@code subject}
     *
     * @see #remove(java.lang.Object)
     * @see #removeOldest()
     */
    public V register(@NotNull K correlationId, @NotNull V subject) {
        try {
            TimestampedRegistration<K, V> tr = new TimestampedRegistration<K, V>(System.currentTimeMillis(), correlationId, subject);
            rwLock.writeLock().lock();
            
            timestampedPriorityQueue.offer(tr);
            TimestampedRegistration<K, V> oldTr = correlationMap.put(tr.key, tr);
            if (oldTr != null) {
                removeFromQueue(oldTr);
                return oldTr.value;
            }

            return null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Creates a new {@code subject} registration. The newly created registration
     * is timestamped which allows for later removal based on the age of registration
     * using {@link #removeOldest()} method.
     *
     * @param subject a primary registration subject
     *
     * @return {@code true} if the registration was successfull, {@code false} otherwise
     *
     * @see #removeOldest()
     */
    public boolean register(@NotNull V subject) {
        return register(System.currentTimeMillis(), subject);
    }

    /**
     * Creates a new {@code subject} registration. The newly created registration
     * is timestamped using a value of the {@code timestamp} parameter which allows
     * for later removal based on the age of registration using {@link #removeOldest()}
     * method.
     *
     * @param timestamp a timestamp to be used for the registration
     * @param subject a primary registration subject
     * 
     * @return {@code true} if the registration was successfull, {@code false} otherwise
     *
     * @see #removeOldest()
     */
    public boolean register(long timestamp, @NotNull V subject) {
        try {
            TimestampedRegistration<K, V> tr = new TimestampedRegistration<K, V>(timestamp, null, subject);
            rwLock.writeLock().lock();

            return timestampedPriorityQueue.offer(tr);
            // we don't put anything into correlationMap
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    /**
     * Removes a registration from the collection based on a {@code correlationId} and returns
     * the value of the registered {@code subject}. This method may return {@code null}
     *
     * @param correlationId identifier to be associated with an already registered {@code subject}
     *
     * @return a registered {@code subject} associated with a given {@code correlationId}
     *         or {@code null} if there's no such {@code subject}
     *
     * @see #register(java.lang.Object, java.lang.Object) 
     */
    public V remove(@NotNull K correlationId) {
        try {
            rwLock.writeLock().lock();
            TimestampedRegistration<K, V> tr = correlationMap.remove(correlationId);
            if (tr == null) {
                return null;
            }
            removeFromQueue(tr);

            return tr.value;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Removes the oldest registration from the collection and returns the value of
     * the registered {@code subject}.
     *
     * @return an oldest registered {@code subject}
     *
     * @throws NoSuchElementException if the underlying collection is empty.
     *
     * @see #register(java.lang.Object, java.lang.Object)
     * @see #register(java.lang.Object)
     * @see #register(long, java.lang.Object) 
     */
    public V removeOldest() {
        try {
            rwLock.writeLock().lock();
            TimestampedRegistration<K, V> tr = timestampedPriorityQueue.poll();
            try {
                if (tr.key != null) {
                    correlationMap.remove(tr.key);
                }
                return tr.value;
            } catch (NullPointerException cause) {
                NoSuchElementException ex = new NoSuchElementException("The underlying collection is empty.");
                ex.initCause(cause);
                throw ex;
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Removes all values from the time-stamped collection and returns them as an ordered FIFO 
     * list.
     *
     * @return ordered FIFO list of the removed values. Returns empty list in case there are no
     *         values stored in the collection.
     */
    public List<V> removeAll() {
        try {
            rwLock.writeLock().lock();
            if (timestampedPriorityQueue.isEmpty()) {
                return Collections.emptyList();
            }

            List<V> values = new ArrayList<V>(timestampedPriorityQueue.size());

            while (!timestampedPriorityQueue.isEmpty()) {
                values.add(removeOldest());
            }

            return values;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Call this function to determine whether the collection is empty or not.
     *
     * @return {@code true} if the collection is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        try {
            rwLock.readLock().lock();            
            return timestampedPriorityQueue.isEmpty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Returns the number of elements in this collection. If the collection
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this collection.
     */
    public int size() {
        try {
            rwLock.readLock().lock();
            return timestampedPriorityQueue.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Returns a timestamp of the oldest registered subject.
     *
     * @return timestamp of the oldest registered subject.
     *
     * @throws NoSuchElementException if the underlying collection is empty.
     *
     * @see #removeOldest()
     */
    public long getOldestRegistrationTimestamp() {
        try {
            rwLock.readLock().lock();
            try {
                return timestampedPriorityQueue.peek().timestamp;
            } catch (NullPointerException cause) {
                NoSuchElementException ex = new NoSuchElementException("The underlying collection is empty.");
                ex.initCause(cause);
                throw ex;
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void removeFromQueue(TimestampedRegistration<K, V> tr) {
        Iterator<TimestampedRegistration<K, V>> it = timestampedPriorityQueue.iterator();
        while (it.hasNext()) {
            if (it.next() == tr) {
                // this must be the same instance
                it.remove();
                break;
            }
        }
    }
}
