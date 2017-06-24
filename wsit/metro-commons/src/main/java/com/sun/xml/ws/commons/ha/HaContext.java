/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.commons.ha;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.message.Packet;
import java.util.logging.Level;

/**
 * Runtime HA context implemented using thread local state data
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class HaContext {

    private static final Logger LOGGER = Logger.getLogger(HaContext.class);

    /**
     * Internal state data of the HA context
     */
    public static final class State {
        
        private static final State EMPTY = new State(null, null);

        private final Packet packet;
        private final HaInfo haInfo;

        private State(Packet packet, HaInfo haInfo) {
            this.packet = packet;
            this.haInfo = haInfo;
        }

        @Override
        public String toString() {
            return "HaState{packet=" + packet + ", haInfo=" + HaContext.asString(haInfo) + '}';
        }
    }
    //
    private static final ThreadLocal<State> state = new ThreadLocal<State>()  {

        @Override
        protected State initialValue() {
            return State.EMPTY;
        }
    };

    public static State initFrom(final Packet packet) {
        final State oldState = state.get();

        HaInfo haInfo = null;
        if (packet != null && packet.supports(Packet.HA_INFO)) {
            haInfo = (HaInfo) packet.get(Packet.HA_INFO);
        }
        final State newState = new State(packet, haInfo);
        state.set(newState);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[METRO-HA] " + Thread.currentThread().toString() + " : Initialized from packet - replaced old " + oldState.toString() + " with a new " + newState.toString());
        }

        return oldState;
    }

    public static State initFrom(final State newState) {
        final State oldState = state.get();

        state.set(newState);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[METRO-HA] " + Thread.currentThread().toString() + " : Initialized from state - replaced old " + oldState.toString() + " with a new " + newState.toString());
        }

        return oldState;
    }

    public static State currentState() {
        return state.get();
    }

    public static void clear() {
        state.set(State.EMPTY);
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[METRO-HA] " + Thread.currentThread().toString() + " : Current HA state cleared");
        }

    }

    public static HaInfo currentHaInfo() {
        return state.get().haInfo;
    }

    public static void udpateReplicaInstance(final String replicaInstance) {
        boolean updateNeeded = false;
        final State currentState = state.get(); 
        
        if (currentState.haInfo == null) {
            throw new IllegalStateException("Unable to update replicaInstance. Current HaInfo in the local thread is null.");
        }

        if (replicaInstance == null) {
            updateNeeded = currentState.haInfo.getReplicaInstance() != null;
        } else {
            updateNeeded = !replicaInstance.equals(currentState.haInfo.getReplicaInstance());
        }

        if (updateNeeded) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("[METRO-HA] " + Thread.currentThread().toString() + " : Replica instance value changed to '" + replicaInstance + "'. Updating current HaInfo.");
            }
            
            final HaInfo old = currentState.haInfo;
            updateHaInfo(new HaInfo(old.getKey(), replicaInstance, old.isFailOver()));
        } else if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[METRO-HA] " + Thread.currentThread().toString() + " : New replica instance value '" + replicaInstance + "' same as current - no HaInfo update necessary");
        }
    }

    public static void updateHaInfo(final HaInfo newValue) {
        final Packet packet = state.get().packet;
        state.set(new State(packet, newValue));
        if (packet != null && packet.supports(Packet.HA_INFO)) {
            packet.put(Packet.HA_INFO, newValue);
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("[METRO-HA] " + Thread.currentThread().toString() + " : HaInfo value updated: " + asString(newValue));
        }
    }

    public static boolean failoverDetected() {
        final HaInfo haInfo = state.get().haInfo;
        return haInfo != null && haInfo.isFailOver();
    }

    public static String asString(HaInfo haInfo) {
        if (haInfo == null) {
            return "null";
        }

        return "HaInfo{hashableKey=" + haInfo.getKey() + ", replicaInstance=" + haInfo.getReplicaInstance() + ", isFailover=" + haInfo.isFailOver() + "}";
    }
}
