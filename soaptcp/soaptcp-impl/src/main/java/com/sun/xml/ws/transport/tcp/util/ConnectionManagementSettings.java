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

package com.sun.xml.ws.transport.tcp.util;

import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.sun.xml.ws.transport.tcp.util.TCPConstants.*;
/**
 * SOAP/TCP connection cache settings
 * 
 * @author Alexey Stashok
 */
public class ConnectionManagementSettings {    
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain);
    
    private static final int DEFAULT_VALUE = -1;

    private int highWatermark = DEFAULT_VALUE;
    private int maxParallelConnections = DEFAULT_VALUE;
    private int numberToReclaim = DEFAULT_VALUE;
    
    private static volatile ConnectionManagementSettingsHolder holder;
    
    public static ConnectionManagementSettingsHolder getSettingsHolder() {
        if (holder == null) {
            synchronized(ConnectionManagementSettings.class) {
                if (holder == null) {
                    if (!createDefaultHolder()) {
                        holder = new SystemPropsConnectionManagementSettingsHolder();
                    }
                }
            }
        }
        return holder;
    }
    
    public static void setSettingsHolder(ConnectionManagementSettingsHolder holder) {
        ConnectionManagementSettings.holder = holder;
    }
    
    // Client side constructor (outbound connection cache)
    public ConnectionManagementSettings(int highWatermark, 
            int maxParallelConnections, int numberToReclaim) {
        this.highWatermark = highWatermark != DEFAULT_VALUE ? 
            highWatermark : HIGH_WATER_MARK_CLIENT;
        this.maxParallelConnections = maxParallelConnections != DEFAULT_VALUE ? 
            maxParallelConnections : MAX_PARALLEL_CONNECTIONS_CLIENT;
        this.numberToReclaim = numberToReclaim != DEFAULT_VALUE ? 
            numberToReclaim : NUMBER_TO_RECLAIM_CLIENT;
    }
    
    // Server side constructor (inbound connection cache)
    public ConnectionManagementSettings(int highWatermark, int numberToReclaim) {
        this.highWatermark = highWatermark != DEFAULT_VALUE ? 
            highWatermark : HIGH_WATER_MARK_SERVER;
        this.maxParallelConnections = DEFAULT_VALUE;
        this.numberToReclaim = numberToReclaim != DEFAULT_VALUE ? 
            numberToReclaim : NUMBER_TO_RECLAIM_SERVER;
    }

    public int getHighWatermark() {
        return highWatermark;
    }
    
    public int getMaxParallelConnections() {
        return maxParallelConnections;
    }
    
    public int getNumberToReclaim() {
        return numberToReclaim;
    }
        
    /**
     * Method tries to load default connection settings holder (Policy implementation)
     * 
     * @return true, if policy based settings holder was initiated successfully,
     * false otherwise
     */
    private static boolean createDefaultHolder() {
        boolean isOk = true;
        try {
            Class<?> policyHolderClass =
                    Class.forName("com.sun.xml.ws.transport.tcp.wsit.PolicyConnectionManagementSettingsHolder");
            Method getSingltonMethod = policyHolderClass.getMethod("getInstance");
            holder = (ConnectionManagementSettingsHolder) getSingltonMethod.invoke(null);
            logger.log(Level.FINE, MessagesMessages.WSTCP_1150_CON_MNGMNT_SETTINGS_POLICY());
        } catch(Exception e) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1151_CON_MNGMNT_SETTINGS_SYST_PROPS());
            isOk = false;
        }

        return isOk;
    }

    /**
     * SOAP/TCP connection cache settings holder.
     * Contains client and server cache settings
     */
    public interface ConnectionManagementSettingsHolder {
        public ConnectionManagementSettings getClientSettings();
        public ConnectionManagementSettings getServerSettings();
    }
    
    /**
     * SOAP/TCP connection cache settings holder.
     * Implements holder, which gets connection settings from system properties.
     */
    private static class SystemPropsConnectionManagementSettingsHolder 
            implements ConnectionManagementSettingsHolder {
        private volatile ConnectionManagementSettings clientSettings;
        private volatile ConnectionManagementSettings serverSettings;
        
        public ConnectionManagementSettings getClientSettings() {
            if (clientSettings == null) {
                synchronized(this) {
                    if (clientSettings == null) {
                        clientSettings = createSettings(true);
                    }
                }
            }
            
            return clientSettings;
        }

        public ConnectionManagementSettings getServerSettings() {
            if (serverSettings == null) {
                synchronized(this) {
                    if (serverSettings == null) {
                        serverSettings = createSettings(false);
                    }
                }
            }
            
            return serverSettings;
        }
        
        private static ConnectionManagementSettings createSettings(boolean isClient) {
            int highWatermark = Integer.getInteger(TCPConstants.HIGH_WATER_MARK, 
                    DEFAULT_VALUE);
            
            int maxParallelConnections = Integer.getInteger(
                    TCPConstants.MAX_PARALLEL_CONNECTIONS, DEFAULT_VALUE);
            
            int numberToReclaim = Integer.getInteger(TCPConstants.NUMBER_TO_RECLAIM, 
                    DEFAULT_VALUE);
            
            
            ConnectionManagementSettings settings = null;
            if (isClient) {
                settings = new ConnectionManagementSettings(highWatermark,
                        maxParallelConnections, numberToReclaim);
            } else {
                settings = new ConnectionManagementSettings(highWatermark, 
                        numberToReclaim);
            }
            
            return settings;
        }
    }    
}
