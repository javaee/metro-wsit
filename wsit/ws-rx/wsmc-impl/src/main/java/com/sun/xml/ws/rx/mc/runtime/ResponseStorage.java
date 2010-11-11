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
import java.util.logging.Level;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreFactory;

final class ResponseStorage {

    private static final Logger LOGGER = Logger.getLogger(ResponseStorage.class);

    private static final class PendingMessageDataReplicationManager implements ReplicationManager<String, JaxwsMessage> {

        private final BackingStore<StickyKey, JaxwsMessageState> messageStateStore;
        private final String loggerProlog;

        public PendingMessageDataReplicationManager(final String endpointUid) {
            this.messageStateStore = HighAvailabilityProvider.INSTANCE.createBackingStore(
                    HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY),
                    endpointUid + "_MC_PENDING_MESSAGE_DATA_STORE",
                    StickyKey.class,
                    JaxwsMessageState.class);

            this.loggerProlog = "[MC message data manager endpointUid: " + endpointUid + "]: ";

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Created pending message backing store");
            }
        }

        public JaxwsMessage load(final String key) {
            final JaxwsMessageState state = HighAvailabilityProvider.loadFrom(messageStateStore, new StickyKey(key), null);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Message state loaded from pending message backing store for key [" + key + "]: " + ((state == null) ? null : state.toString()));
            }

            final JaxwsMessage message = state.toMessage();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Message state converted to a pending message: " + ((message == null) ? null : message.toString()));
            }
            return message;
        }

        public void save(final String key, final JaxwsMessage value, final boolean isNew) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Sending for replication pending message with a key [" + key + "]: " + value.toString() + ", isNew=" + isNew);
            }

            JaxwsMessageState state = value.getState();
            HaInfo haInfo = HaContext.currentHaInfo();
            if (haInfo != null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(loggerProlog + "Existing HaInfo found, using it for pending message state replication: " + HaContext.asString(haInfo));
                }

                HighAvailabilityProvider.saveTo(messageStateStore, new StickyKey(key, haInfo.getKey()), state, isNew);
            } else {
                final StickyKey stickyKey = new StickyKey(key);
                final String replicaId = HighAvailabilityProvider.saveTo(messageStateStore, stickyKey, state, isNew);

                haInfo = new HaInfo(stickyKey.getHashKey(), replicaId, false);
                HaContext.updateHaInfo(haInfo);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(loggerProlog + "No HaInfo found, created new after pending message state replication: " + HaContext.asString(haInfo));
                }
            }
        }

        public void remove(String key) {
            HighAvailabilityProvider.removeFrom(messageStateStore, new StickyKey(key));
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Removed pending message from the backing store for key [" + key + "]");
            }
        }

        public void close() {
            HighAvailabilityProvider.close(messageStateStore);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Closed pending message backing store");
            }
        }

        public void destroy() {
            HighAvailabilityProvider.destroy(messageStateStore);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Destroyed pending message backing store");
            }
        }
    }
    //
    private final ReentrantReadWriteLock storageLock = new ReentrantReadWriteLock();
    //
    private final HighlyAvailableMap<String, JaxwsMessage> pendingResponses;
    private final HighlyAvailableMap<String, PendingResponseIdentifiers> pendingResponseIdentifiers;
    private final String endpointUid;
    //

    public ResponseStorage(final String endpointUid) {
        StickyReplicationManager<String, PendingResponseIdentifiers> responseIdentifiersManager = null;
        PendingMessageDataReplicationManager responseManager = null;
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
            final BackingStoreFactory bsf = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);

            responseIdentifiersManager = new StickyReplicationManager<String, PendingResponseIdentifiers>(
                    endpointUid + "_MC_PENDING_MESSAGE_IDENTIFIERS_MAP_MANAGER",
                    HighAvailabilityProvider.INSTANCE.createBackingStore(
                    bsf,
                    endpointUid + "_MC_PENDING_MESSAGE_IDENTIFIERS_STORE",
                    StickyKey.class,
                    PendingResponseIdentifiers.class));

            responseManager = new PendingMessageDataReplicationManager(endpointUid);
        }
        this.pendingResponseIdentifiers = HighlyAvailableMap.create(endpointUid + "_MC_PENDING_MESSAGE_IDENTIFIERS_MAP", new HashMap<String, PendingResponseIdentifiers>(), responseIdentifiersManager);
        this.pendingResponses = HighlyAvailableMap.create(endpointUid + "_MC_PENDING_MESSAGE_DATA_MAP", new HashMap<String, JaxwsMessage>(), responseManager);
        this.endpointUid = endpointUid;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Response storage initialized");
        }
    }

    void store(@NotNull JaxwsMessage response, @NotNull final String clientUID) {
        try {
            storageLock.writeLock().lock();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Storing new response for client UID: " + clientUID);
            }

            pendingResponses.put(response.getCorrelationId(), response);

            PendingResponseIdentifiers clientResponses = pendingResponseIdentifiers.get(clientUID);
            if (clientResponses == null) {
                clientResponses = new PendingResponseIdentifiers();
            }
            if (!clientResponses.offer(response.getCorrelationId())) {
                LOGGER.severe(LocalizationMessages.WSMC_0104_ERROR_STORING_RESPONSE(clientUID));
            }
            pendingResponseIdentifiers.put(clientUID, clientResponses);
        } finally {
            storageLock.writeLock().unlock();
        }
    }

    public JaxwsMessage getPendingResponse(@NotNull final String clientUID) {
        try {
            storageLock.writeLock().lock();
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Retrieving stored pending response for client UID: " + clientUID);
            }

            PendingResponseIdentifiers clientResponses = pendingResponseIdentifiers.get(clientUID);
            if (clientResponses != null && !clientResponses.isEmpty()) {
                String messageId = clientResponses.poll();
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Found registered pending response with message id [" + messageId + "] for client UID: " + clientUID);
                }
                pendingResponseIdentifiers.put(clientUID, clientResponses);

                final JaxwsMessage response = pendingResponses.remove(messageId);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Retrieved and removed pending response message data for message id [" + messageId + "]: " + ((response == null) ? null : response.toString()));
                }
                if (response == null) {
                    LOGGER.warning("[WSMC-HA] endpoint UID [" + endpointUid + "]: No penidng response message data found for message id [" + messageId + "]");
                }

                return response;
            }
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: No pedning responses found for client UID: " + clientUID);
            }
            return null;
        } finally {
            storageLock.writeLock().unlock();
        }
    }

    public boolean hasPendingResponse(@NotNull String clientUID) {
        try {
            storageLock.readLock().lock();
            PendingResponseIdentifiers clientResponses = pendingResponseIdentifiers.get(clientUID);
            final boolean result = clientResponses != null && !clientResponses.isEmpty();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Pending responses avaliable for client UID [" + clientUID + "]: " + result);
            }

            return result;
        } finally {
            storageLock.readLock().unlock();
        }
    }

    void invalidateLocalCache() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Invalidation local caches for the response storage");
        }
        pendingResponseIdentifiers.invalidateCache();
        pendingResponses.invalidateCache();
    }
    
    void dispose() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[WSMC-HA] endpoint UID [" + endpointUid + "]: Disposing the response storage");
        }
        
        pendingResponseIdentifiers.close();
        pendingResponseIdentifiers.destroy();
        
        pendingResponses.close();
        pendingResponses.destroy();
    }
}
