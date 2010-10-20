/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.rx.mc.runtime;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.rx.ha.HighlyAvailableMap;
import com.sun.xml.ws.rx.ha.HighlyAvailableMap.StickyReplicationManager;
import com.sun.xml.ws.rx.ha.ReplicationManager;
import com.sun.xml.ws.rx.mc.localization.LocalizationMessages;
import com.sun.xml.ws.rx.message.jaxws.JaxwsMessage;
import com.sun.xml.ws.rx.message.jaxws.JaxwsMessage.JaxwsMessageState;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreFactory;

final class ResponseStorage {

    private static final Logger LOGGER = Logger.getLogger(ResponseStorage.class);

    private static final class PendingMessageReplicationManager implements ReplicationManager<String, JaxwsMessage> {

        private BackingStore<StickyKey, JaxwsMessageState> messageStateStore;

        public PendingMessageReplicationManager(final String uniqueEndpointId) {
            this.messageStateStore = HighAvailabilityProvider.INSTANCE.createBackingStore(
                    HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY),
                    uniqueEndpointId + "_MC_PENDING_MESSAGE_STORE",
                    StickyKey.class,
                    JaxwsMessageState.class);
        }

        public JaxwsMessage load(String key) {
            JaxwsMessageState state = HighAvailabilityProvider.loadFrom(messageStateStore, new StickyKey(key), null);

            return state.toMessage();
        }

        public void save(final String key, final JaxwsMessage value, final boolean isNew) {
            JaxwsMessageState state = value.getState();
            HaInfo haInfo = HaContext.currentHaInfo();
            if (haInfo != null) {
                HighAvailabilityProvider.saveTo(messageStateStore, new StickyKey(key, haInfo.getKey()), state, isNew);
            } else {
                final StickyKey stickyKey = new StickyKey(key);
                final String replicaId = HighAvailabilityProvider.saveTo(messageStateStore, stickyKey, state, isNew);
                HaContext.updateHaInfo(new HaInfo(stickyKey.getHashKey(), replicaId, false));
            }
        }

        public void remove(String key) {
            HighAvailabilityProvider.removeFrom(messageStateStore, new StickyKey(key));
        }

        public void close() {
            HighAvailabilityProvider.close(messageStateStore);
        }

        public void destroy() {
            HighAvailabilityProvider.destroy(messageStateStore);
        }
    }
    final HighlyAvailableMap<String, JaxwsMessage> pendingResponses;
    final HighlyAvailableMap<String, PendingResponseIdentifiers> pendingResponseIdentifiers;
    final ReentrantReadWriteLock storageLock = new ReentrantReadWriteLock();

    public ResponseStorage(final String uniqueEndpointId) {
        StickyReplicationManager<String, PendingResponseIdentifiers> responseIdentifiersManager = null;
        PendingMessageReplicationManager responseManager = null;
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
            final BackingStoreFactory bsf = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);

            responseIdentifiersManager = new StickyReplicationManager<String, PendingResponseIdentifiers>(HighAvailabilityProvider.INSTANCE.createBackingStore(
                    bsf,
                    uniqueEndpointId + "_MC_CLIENT_PENDING_MESSAGE_IDENTIFIERS",
                    StickyKey.class,
                    PendingResponseIdentifiers.class));
            
            responseManager = new PendingMessageReplicationManager(uniqueEndpointId);
        }
        this.pendingResponseIdentifiers = HighlyAvailableMap.create(new HashMap<String, PendingResponseIdentifiers>(), responseIdentifiersManager);
        this.pendingResponses = HighlyAvailableMap.create(new HashMap<String, JaxwsMessage>(), responseManager);
    }

    void store(@NotNull JaxwsMessage response, @NotNull String clientUID) {
        try {
            storageLock.writeLock().lock();
            PendingResponseIdentifiers clientResponses = pendingResponseIdentifiers.get(clientUID);
            if (clientResponses == null) {
                clientResponses = new PendingResponseIdentifiers();
            }
            pendingResponses.put(response.getCorrelationId(), response);
            if (!clientResponses.offer(response.getCorrelationId())) {
                LOGGER.severe(LocalizationMessages.WSMC_0104_ERROR_STORING_RESPONSE(clientUID));
            }
            pendingResponseIdentifiers.put(clientUID, clientResponses);
        } finally {
            storageLock.writeLock().unlock();
        }
    }

    public JaxwsMessage getPendingResponse(@NotNull String clientUID) {
        try {
            storageLock.readLock().lock();
            PendingResponseIdentifiers clientResponses = pendingResponseIdentifiers.get(clientUID);
            if (clientResponses != null && !clientResponses.isEmpty()) {
                String messageId = clientResponses.poll();
                pendingResponseIdentifiers.put(clientUID, clientResponses);
                return pendingResponses.remove(messageId);
            }
            return null;
        } finally {
            storageLock.readLock().unlock();
        }
    }

    public boolean hasPendingResponse(@NotNull String clientUID) {
        try {
            storageLock.readLock().lock();
            PendingResponseIdentifiers clientResponses = pendingResponseIdentifiers.get(clientUID);
            return clientResponses != null && !clientResponses.isEmpty();
        } finally {
            storageLock.readLock().unlock();
        }
    }

    void invalidateLocalCache() {
        pendingResponseIdentifiers.invalidateCache();
        pendingResponses.invalidateCache();
    }
}
