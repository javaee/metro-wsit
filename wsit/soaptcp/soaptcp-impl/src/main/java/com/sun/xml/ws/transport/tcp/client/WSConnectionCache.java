/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.tcp.client;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.SessionAbortedException;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import java.util.Collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public class WSConnectionCache {
//    private static final Logger logger = Logger.getLogger(
//            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
//    
//    // map contains all connection sessions to certain destination
//    private final Map<Integer, Set<ConnectionSession>> allDstAddress2connectionSession;
//    
//    // map contains available connection sessions to certain destination (MAX_CHANNELS is not reached)
//    private final Map<Integer, ConcurrentLinkedQueue<ConnectionSession>> availableDstAddress2connectionSession;
//    
//    // set of locked connections, which are in use
//    private final Map<ConnectionSession, Thread> lockedConnections;
//    
//    public WSConnectionCache() {
//        allDstAddress2connectionSession = new HashMap<Integer, Set<ConnectionSession>>();
//        availableDstAddress2connectionSession = new HashMap<Integer, ConcurrentLinkedQueue<ConnectionSession>>();
//        lockedConnections = new HashMap<ConnectionSession, Thread>();
//    }
//    
//    public void registerConnectionSession(@NotNull final ConnectionSession connectionSession, final int dstAddrHashKey) {
//        ConcurrentLinkedQueue<ConnectionSession> availableConnectionSessions = availableDstAddress2connectionSession.get(dstAddrHashKey);
//        Set<ConnectionSession> allConnectionSessions = allDstAddress2connectionSession.get(dstAddrHashKey);
//        synchronized(this) {
//            //check if there is a record for such destination address
//            if (allConnectionSessions == null) {
//                allConnectionSessions = new HashSet<ConnectionSession>();
//                allDstAddress2connectionSession.put(dstAddrHashKey, allConnectionSessions);
//                availableConnectionSessions = new ConcurrentLinkedQueue<ConnectionSession>();
//                availableDstAddress2connectionSession.put(dstAddrHashKey, availableConnectionSessions);
//            }
//        }
//        availableConnectionSessions.offer(connectionSession);
//        allConnectionSessions.add(connectionSession);
//    }
//    
//    /**
//     * Get all active sessions for given destination host:port
//     */
//    public @NotNull Set<ConnectionSession> getAllConnectionsByAddr(final int dstAddrHashKey) {
//        final Set<ConnectionSession> allConnectionSessions = allDstAddress2connectionSession.get(dstAddrHashKey);
//        return allConnectionSessions != null ? allConnectionSessions : Collections.<ConnectionSession>emptySet();
//    }
//    
//    /**
//     * Get session, where it is available to create one more virtual connection
//     */
//    public @Nullable ConnectionSession pollAvailableConnectionByAddr(final int dstAddrHashKey) {
//        final ConcurrentLinkedQueue<ConnectionSession> availableConnectionSessions = availableDstAddress2connectionSession.get(dstAddrHashKey);
//        return availableConnectionSessions != null ? availableConnectionSessions.poll() : null;
//    }
//    
//    /**
//     * Put back session to available session list
//     */
//    public void offerAvailableConnectionByAddr(@NotNull final ConnectionSession connectionSession, final int dstAddrHashKey) {
//        final ConcurrentLinkedQueue<ConnectionSession> availableConnectionSessions = availableDstAddress2connectionSession.get(dstAddrHashKey);
//        availableConnectionSessions.offer(connectionSession);
//    }
//    
//    /**
//     * Destroy connection session
//     */
//    public void removeConnectionSession(final @NotNull ConnectionSession tcpConnectionSession) {
//        final int addressHashKey = tcpConnectionSession.getDstAddressHashKey();
//        final Set<ConnectionSession> allConnectionSessions = allDstAddress2connectionSession.get(addressHashKey);
//        
//        // method is called before ConnectionSession was registered in cache
//        if (allConnectionSessions != null) {
//            final ConcurrentLinkedQueue<ConnectionSession> availableConnectionSessions = availableDstAddress2connectionSession.get(addressHashKey);
//            
//            synchronized(tcpConnectionSession) {
//                // remove session from all and available lists
//                allConnectionSessions.remove(tcpConnectionSession);
//                availableConnectionSessions.remove(tcpConnectionSession);
//                
//                unlockConnection(tcpConnectionSession);
//                tcpConnectionSession.notifyAll();
//            }
//        }
//    }
//    
//    public void lockConnection(final @NotNull ConnectionSession tcpConnectionSession) throws InterruptedException, SessionAbortedException {
//        logger.log(Level.FINEST, MessagesMessages.WSTCP_1020_CONNECTION_CACHE_ENTER());
//        final Thread lockedThread = lockedConnections.get(tcpConnectionSession);
//        if (Thread.currentThread().equals(lockedThread)) return;
//        
//        synchronized(tcpConnectionSession) {
//            logger.log(Level.FINEST, MessagesMessages.WSTCP_1021_CONNECTION_CACHE_SYNC());
//            while(lockedConnections.containsKey(tcpConnectionSession)) {
//                tcpConnectionSession.wait();
//            }
//            
//            // check whether session was aborted?
//            final Set<ConnectionSession> allConnectionSessions = allDstAddress2connectionSession.get(tcpConnectionSession.getDstAddressHashKey());
//            if (allConnectionSessions.contains(tcpConnectionSession)) {
//                logger.log(Level.FINEST, MessagesMessages.WSTCP_1022_CONNECTION_CACHE_LOCK());
//                lockedConnections.put(tcpConnectionSession, Thread.currentThread());
//            } else {
//                logger.log(Level.FINEST, MessagesMessages.WSTCP_1023_CONNECTION_CACHE_SESSION_ABORTED());
//                throw new SessionAbortedException();
//            }
//        }
//    }
//    
//    public void unlockConnection(final @NotNull ConnectionSession tcpConnectionSession) {
//        synchronized(tcpConnectionSession) {
//            lockedConnections.remove(tcpConnectionSession);
//            tcpConnectionSession.notify();
//        }
//    }
}
