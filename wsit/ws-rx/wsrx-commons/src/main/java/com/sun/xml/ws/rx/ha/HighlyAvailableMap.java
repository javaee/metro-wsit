/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.rx.ha;

import com.sun.istack.logging.Logger;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import java.util.logging.Level;

import org.glassfish.ha.store.api.BackingStore;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class HighlyAvailableMap<K extends Serializable, V> implements Map<K, V> {
    private static final Logger LOGGER = Logger.getLogger(HighlyAvailableMap.class);

    public static final class NoopReplicationManager<K extends Serializable, V> implements ReplicationManager<K, V> {
        private final String loggerProlog;

        public NoopReplicationManager(String name) {
            this.loggerProlog = "[" + name + "]: ";
        }
        public V load(K key) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "load() method invoked for key: " + key);
            }
            return null;
        }

        public void save(K key, V value, boolean isNew) {
            // noop
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "save() method invoked for [key=" + key + ", value=" + value + ", isNew=" + isNew + "]");
            }            
        }

        public void remove(K key) {
            // noop
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "remove() method invoked for key: " + key);
            }
        }

        public void close() {
            // noop
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "close() invoked");
            }            
        }

        public void destroy() {
            // noop
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "destroy() invoked");
            }            
        }
    }

    public static final class SimpleReplicationManager<K extends Serializable, V extends Serializable> implements ReplicationManager<K, V> {

        private final BackingStore<K, V> backingStore;
        private final String loggerProlog;

        public SimpleReplicationManager(String name, BackingStore<K, V> backingStore) {
            this.backingStore = backingStore;
            this.loggerProlog = "[" + name + "]: ";
        }

        public V load(K key) {
            final V data = HighAvailabilityProvider.loadFrom(backingStore, key, null);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "loaded data for key [" + key + "]: " + data);
            }            
            return data;
        }

        public void save(K key, V value, boolean isNew) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "sending for replication [key=" + key + ", value=" + value + ", isNew=" + isNew + "]");
            }            
            HighAvailabilityProvider.saveTo(backingStore, key, value, isNew);
        }

        public void remove(K key) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "removing data for key: " + key);
            }
            HighAvailabilityProvider.removeFrom(backingStore, key);
        }

        public void close() {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "closing backing store");
            }            
            HighAvailabilityProvider.close(backingStore);
        }

        public void destroy() {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "destroying backing store");
            }            
            HighAvailabilityProvider.destroy(backingStore);
        }
    }

    public static final class StickyReplicationManager<K extends Serializable, V extends Serializable> implements ReplicationManager<K, V> {

        private final BackingStore<StickyKey, V> backingStore;
        private final String loggerProlog;

        public StickyReplicationManager(String name, BackingStore<StickyKey, V> backingStore) {
            this.backingStore = backingStore;
            this.loggerProlog = "[" + name + "]: ";
        }

        public V load(K key) {
            final V data = HighAvailabilityProvider.loadFrom(backingStore, new StickyKey(key), null);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "loaded data for key [" + key + "]: " + data);
            }                        
            return data;
        }

        public void save(final K key, final V value, final boolean isNew) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "sending for replication [key=" + key + ", value=" + value + ", isNew=" + isNew + "]");
            }            

            HaInfo haInfo = HaContext.currentHaInfo();
            if (haInfo != null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(loggerProlog + "Existing HaInfo found, using it for data replication: " + HaContext.asString(haInfo));
                }
                HighAvailabilityProvider.saveTo(backingStore, new StickyKey(key, haInfo.getKey()), value, isNew);
            } else {
                final StickyKey stickyKey = new StickyKey(key);
                final String replicaId = HighAvailabilityProvider.saveTo(backingStore, stickyKey, value, isNew);
                
                haInfo = new HaInfo(stickyKey.getHashKey(), replicaId, false);
                HaContext.updateHaInfo(haInfo);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(loggerProlog + "No HaInfo found, created new after data replication: " + HaContext.asString(haInfo));
                }
            }
        }

        public void remove(K key) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "removing data for key: " + key);
            }
            HighAvailabilityProvider.removeFrom(backingStore, new StickyKey(key));
        }

        public void close() {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "closing backing store");
            }            
            HighAvailabilityProvider.close(backingStore);
        }

        public void destroy() {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "destroying backing store");
            }            
            HighAvailabilityProvider.destroy(backingStore);
        }
    }

    private final Map<K, V> localMap;
    private final ReplicationManager<K, V> replicationManager;
    private final String loggerProlog;
    
    public static <K extends Serializable, V extends Serializable> HighlyAvailableMap<K, V> create(final String name, Map<K, V> wrappedMap, BackingStore<K, V> backingStore) {
        return new HighlyAvailableMap<K, V>(name, wrappedMap, new SimpleReplicationManager<K, V>(name + "_MANAGER", backingStore));
    }

    public static <K extends Serializable, V extends Serializable> HighlyAvailableMap<K, V> createSticky(final String name, Map<K, V> wrappedMap, BackingStore<StickyKey, V> backingStore) {
        return new HighlyAvailableMap<K, V>(name, wrappedMap, new StickyReplicationManager<K, V>(name + "_MANAGER", backingStore));
    }

    public static <K extends Serializable, V> HighlyAvailableMap<K, V> create(final String name, Map<K, V> wrappedMap, ReplicationManager<K, V> replicationManager) {
        if (replicationManager == null) {
            replicationManager = new NoopReplicationManager<K, V>(name + "_MANAGER");
        }
        
        return new HighlyAvailableMap<K, V>(name, wrappedMap, replicationManager);
    }

    private HighlyAvailableMap(final String name, Map<K, V> wrappedMap, ReplicationManager<K, V> replicationManager) {
        this.loggerProlog = "[" + name + "]: ";
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
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Retrieving data for key ["+ key + "]");
        }                    
        
        @SuppressWarnings("unchecked")
        K _key = (K) key;
        V value = localMap.get(_key);
        if (value != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Data for key ["+ key + "] found in a local cache: " + value);
            }                    
            return value;
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Data for key ["+ key + "] not found in the local cache - consulting replication manager");
        }                            
        return tryLoad(_key);
    }

    public V put(K key, V value) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Storing data for key ["+ key + "]: " + value);
        }                    
        
        V oldValue = localMap.put(key, value);
        replicationManager.save(key, value, oldValue == null);
                
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Old data replaced for key ["+ key + "]: " + oldValue);
        }                    

        return oldValue;
    }

    public V remove(Object key) {
        @SuppressWarnings("unchecked")
        K _key = (K) key;

        if (!localMap.containsKey(_key)) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Data for key ["+ key + "] not found in the local cache - consulting replication manager");
            }                    
            tryLoad(_key);
        }
        V oldValue = localMap.remove(_key);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Removing data for key ["+ key + "]: " + oldValue);
        }                    
        replicationManager.remove(_key);

        return oldValue;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    private V tryLoad(K key) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Using replication manager to load data for key ["+ key + "]");
        }                    
        
        V value = replicationManager.load(key);
        if (value != null) {
            localMap.put(key, value);
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Replication manager returned data for key ["+ key + "]: " + value);
        }                            

        return value;
    }

    public void clear() {
        for (K key : localMap.keySet()) {
            replicationManager.remove(key);            
        }
        
        localMap.clear();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "HA map cleared");
        }                    
        
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

    public void invalidateCache() {
        localMap.clear();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "local cache invalidated");
        }                    
    }

    public ReplicationManager<K, V> getReplicationManager() {
        return replicationManager;
    }

    public void close() {        
        replicationManager.close();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "HA map closed");
        }            
    }

    public void destroy() {
        replicationManager.destroy();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "HA map destroyed");
        }            
    }
}
