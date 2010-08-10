/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.assembler.dev.HighAvailabilityProvider;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.glassfish.ha.store.api.BackingStore;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class HighlyAvailableMap<K extends Serializable, V> implements Map<K, V> {

    private static final class NoopReplicationManager<K extends Serializable, V> implements ReplicationManager<K, V> {

        public V load(K key) {
            return null;
        }

        public String save(K key, V value, boolean isNew) {
            return "";
        }

        public void remove(K key) {
            // noop
        }

        public void close() {
            // noop
        }

        public void destroy() {
            // noop
        }
    }

    private static final class SimpleReplicationManager<K extends Serializable, V extends Serializable> implements ReplicationManager<K, V> {

        private final BackingStore<K, V> backingStore;

        public SimpleReplicationManager(BackingStore<K, V> backingStore) {
            this.backingStore = backingStore;
        }

        public V load(K key) {
            return HighAvailabilityProvider.INSTANCE.loadFrom(backingStore, key, null);
        }

        public String save(K key, V value, boolean isNew) {
            return HighAvailabilityProvider.INSTANCE.saveTo(backingStore, key, value, isNew);
        }

        public void remove(K key) {
            HighAvailabilityProvider.INSTANCE.removeFrom(backingStore, key);
        }

        public void close() {
            HighAvailabilityProvider.INSTANCE.close(backingStore);
        }

        public void destroy() {
            HighAvailabilityProvider.INSTANCE.destroy(backingStore);
        }
    }

    private final Map<K, V> localMap;
    private final ReplicationManager<K, V> replicationManager;

    public static <K extends Serializable, V extends Serializable> HighlyAvailableMap<K, V> newInstanceForBs(Map<K, V> wrappedMap, BackingStore<K, V> backingStore) {
        return new HighlyAvailableMap<K, V>(wrappedMap, new SimpleReplicationManager<K, V>(backingStore));
    }

    public static <K extends Serializable, V> HighlyAvailableMap<K, V> newInstance(Map<K, V> wrappedMap, ReplicationManager<K, V> replicationManager) {
        if (replicationManager == null) {
            replicationManager = new NoopReplicationManager<K, V>();
        }
        
        return new HighlyAvailableMap<K, V>(wrappedMap, replicationManager);
    }

    private HighlyAvailableMap(Map<K, V> wrappedMap, ReplicationManager<K, V> replicationManager) {
        this.localMap = wrappedMap;
        this.replicationManager = replicationManager;
    }

    public int size() {
        return localMap.size();
    }

    public boolean isEmpty() {
        return localMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        @SuppressWarnings("unchecked")
        K _key = (K) key;

        if (localMap.containsKey(_key)) {
            return true;
        }

        return tryLoad(_key) != null;
    }

    @SuppressWarnings("unchecked")
    public boolean containsValue(Object value) {
        return localMap.containsValue((V) value);
    }

    public V get(Object key) {
        @SuppressWarnings("unchecked")
        K _key = (K) key;
        V value = localMap.get(_key);
        if (value != null) {
            return value;
        }

        return tryLoad(_key);
    }

    public V put(K key, V value) {
        V oldValue = localMap.put(key, value);
        replicationManager.save(key, value, oldValue == null);

        return oldValue;
    }

    public V remove(Object key) {
        @SuppressWarnings("unchecked")
        K _key = (K) key;

        V oldValue = localMap.remove(_key);
        replicationManager.remove(_key);

        return oldValue;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    private V tryLoad(K key) {
        V value = replicationManager.load(key);
        if (value != null) {
            localMap.put(key, value);
        }

        return value;
    }

    public void clear() {
        localMap.clear();
    }

    public Set<K> keySet() {
        return localMap.keySet();
    }

    public Collection<V> values() {
        return localMap.values();
    }

    public Set<Entry<K, V>> entrySet() {
        return localMap.entrySet();
    }

    public Map<K, V> getLocalMapCopy() {
        return new HashMap<K, V>(localMap);
    }

    public ReplicationManager<K, V> getReplicationManager() {
        return replicationManager;
    }

    public void close() {
        replicationManager.close();
    }

    public void destroy() {
        replicationManager.destroy();
    }
}
