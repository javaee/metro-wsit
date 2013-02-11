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
package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.ha.ReplicationManager;
import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.JaxwsApplicationMessage.JaxwsApplicationMessageState;
import java.util.logging.Level;
import org.glassfish.ha.store.api.BackingStore;

final class UnackedMessageReplicationManager implements ReplicationManager<String, ApplicationMessage> {

    private static final Logger LOGGER = Logger.getLogger(UnackedMessageReplicationManager.class);
    private BackingStore<StickyKey, JaxwsApplicationMessageState> unackedMesagesBs;
    private final String loggerProlog;

    public UnackedMessageReplicationManager(final String uniqueEndpointId) {
        this.loggerProlog = "[" + uniqueEndpointId + "_UNACKED_MESSAGES_MANAGER]: ";
        this.unackedMesagesBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY),
                uniqueEndpointId + "_UNACKED_MESSAGES_BS",
                StickyKey.class,
                JaxwsApplicationMessageState.class);
    }

    public ApplicationMessage load(String key) {
        JaxwsApplicationMessageState state = HighAvailabilityProvider.loadFrom(unackedMesagesBs, new StickyKey(key), null);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Message state loaded from unacked message backing store for key [" + key + "]: " + ((state == null) ? null : state.toString()));
        }

        JaxwsApplicationMessage message = null;
        if (state != null) {
            message = state.toMessage();
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Message state converted to a unacked message: " + ((message == null) ? null : message.toString()));
        }
        return message;
    }

    public void save(final String key, final ApplicationMessage _value, final boolean isNew) {
        if (!(_value instanceof JaxwsApplicationMessage)) {
            throw new IllegalArgumentException("Unsupported application message type: " + _value.getClass().getName());
        }
        JaxwsApplicationMessageState value = ((JaxwsApplicationMessage) _value).getState();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Sending for replication unacked message with a key [" + key + "]: " + value.toString() + ", isNew=" + isNew);
        }
        
        HaInfo haInfo = HaContext.currentHaInfo();
        if (haInfo != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "Existing HaInfo found, using it for unacked message state replication: " + HaContext.asString(haInfo));
            }
            
            HaContext.udpateReplicaInstance(HighAvailabilityProvider.saveTo(unackedMesagesBs, new StickyKey(key, haInfo.getKey()), value, isNew));
        } else {
            final StickyKey stickyKey = new StickyKey(key);
            final String replicaId = HighAvailabilityProvider.saveTo(unackedMesagesBs, stickyKey, value, isNew);
            
            haInfo = new HaInfo(stickyKey.getHashKey(), replicaId, false);
            HaContext.updateHaInfo(haInfo);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(loggerProlog + "No HaInfo found, created new after unacked message state replication: " + HaContext.asString(haInfo));
            }
        }                       
    }

    public void remove(String key) {
        HighAvailabilityProvider.removeFrom(unackedMesagesBs, new StickyKey(key));
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Removed unacked message from the backing store for key [" + key + "]");
        }
    }

    public void close() {
        HighAvailabilityProvider.close(unackedMesagesBs);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Closed unacked message backing store");
        }
    }

    public void destroy() {
        HighAvailabilityProvider.destroy(unackedMesagesBs);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(loggerProlog + "Destroyed unacked message backing store");
        }
    }
}
