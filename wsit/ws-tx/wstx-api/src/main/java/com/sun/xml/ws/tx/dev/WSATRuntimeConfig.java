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
package com.sun.xml.ws.tx.dev;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class WSATRuntimeConfig {

    private static final Lock DATA_LOCK = new ReentrantLock();

    public static final class Initializer {
        
        public Initializer hostName(String value) {
            WSATRuntimeConfig.hostName = value;
            
            return this;
        }

        public Initializer httpPort(String value) {
            WSATRuntimeConfig.httpPort = value;
            
            return this;
        }

        public Initializer httpsPort(String value) {
            WSATRuntimeConfig.httpsPort = value;
            
            return this;
        }

        public Initializer txLogLocation(String value) {
            WSATRuntimeConfig.txLogLocation = value;
            
            return this;
        }
        
        public Initializer enableWsatRecovery(boolean value) {
            WSATRuntimeConfig.isWsatRecoveryEnabled = value;
            
            return this;
        }

        public Initializer enableWsatSsl(boolean value) {
            WSATRuntimeConfig.isWsatSslEnabled = value;
            
            return this;
        }

        public void done() {
            DATA_LOCK.unlock();
        }
    }
    private static WSATRuntimeConfig instance;
    private static boolean isWsatRecoveryEnabled = Boolean.valueOf(System.getProperty("wsat.recovery.enabled", "true"));
    private static String txLogLocation;
    private static boolean isWsatSslEnabled = Boolean.valueOf(System.getProperty("wsat.ssl.enabled", "false"));
    private static String hostName = "localhost";
    private static String httpPort = "8080";
    private static String httpsPort = "8181";

    private WSATRuntimeConfig() {
        // do nothing
    }

    public static Initializer initializer() {
        DATA_LOCK.lock();

        return new Initializer();
    }

    public static WSATRuntimeConfig getInstance() {
        DATA_LOCK.lock();
        try {
            if (instance == null) {
                instance = new WSATRuntimeConfig();
            }
            return instance;
        } finally {
            DATA_LOCK.unlock();
        }
    }

    /**
     * Is WS-AT recovery and therefore WS-AT transaction logging enabled
     * @return
     */
    public boolean isWSATRecoveryEnabled() {
        return isWsatRecoveryEnabled;
    }

    /**
     * Return Protocol, host, and port String
     * @return for example "https://localhost:8181/"
     */
    public String getHostAndPort() {
        return isWsatSslEnabled ? "https://" + hostName + ":" + httpsPort : "http://" + hostName + ":" + httpPort;
    }

    /**
     * Return the underlying transaction log location
     * @return String directory
     */
    public String getTxLogLocation() {
        return txLogLocation;
    }

    public interface RecoveryEventListener {

        /**
         * Indicate to the listener that recovery for a specific instance is about to start.
         * @param delegated identifies whether it is part of a delegated transaction recovery
         * @param instance the instance name for which transaction recovery is performed, null if unknown
         */
        void beforeRecovery(boolean delegated, String instance);

        /**
         * Indicate to the listener that recovery is over.
         * @param success <code>true</code> if the recovery operation finished successfully
         * @param delegated identifies whether it is part of a delegated transaction recovery
         * @param instance the instance name for which transaction recovery is performed, null if unknown
         */
        void afterRecovery(boolean success, boolean delegated, String instance);
    }

    public class WSATRecoveryEventListener implements RecoveryEventListener {

        public void beforeRecovery(boolean delegated, String instance) {
            if (!delegated) {
                return;
            }
            //read logs of peer/crashed server
//		delegatedtxlogdir = txlogdir + File.separator + ".." + File.separator  ".." + File.separator + "wsat" + File.separator + instance;
//		wsatRecoveryResourceHandlerForDelegated = createWSATRecoveryResourceHandlerForDelegated(delegatedtxlogdir);  // in reality this will likely be deferred until xaresource.recover call
//		TransactionImportManager.getInstance().registerRecoveryResourceHandler(WSATGatewayRM.getInstance());
        }

        public void afterRecovery(boolean success, boolean delegated, String instance) {
        }
    }
}
