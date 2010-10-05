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
package com.sun.xml.wss;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.wss.impl.misc.DefaultNonceManager;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import com.sun.xml.wss.logging.LogDomainConstants;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

import java.net.URL;
import java.util.WeakHashMap;

/**
 * This abstract class defines an SPI that Metro Application developers can implement, to handle custom
 * validation of Nonces used in conjunction with Password-Digest Authentication. A repeated nonce would
 * generally indicate a possible replay-attack.
 * 
 * The SPI implementation class needs to be
 * specified as a META-INF/services entry with name "com.sun.xml.xwss.NonceManager". 
 * A default implementation of this SPI is returned if no entry is configured.
 *
 * 
 */
@ManagedObject
@Description("per-endpoint NonceManager")
@AMXMetadata(type = "WSNonceManager")
public abstract class NonceManager {

    protected static final Logger LOGGER =
            Logger.getLogger(LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    public static final String nonceManager = "com.sun.xml.xwss.NonceManager";
    private static final String NONCE_MANAGER = "NonceManager"; // monitoring
    private static WeakHashMap<WSEndpoint, NonceManager> nonceMgrMap = new WeakHashMap<WSEndpoint, NonceManager>();
    private static NonceManager jaxRPCNonceManager = null;
    private long maxNonceAge;

    /**
     * 
     * @return the approximate maximum age for which a received nonce would be stored by the NonceManager
     */
    @ManagedAttribute
    public long getMaxNonceAge() {
        return maxNonceAge;
    }

    /**
     * Set the approximate maximum age for which a received nonce needs to be stored by the NonceManager
     * @param maxNonceAge  
     */
    public void setMaxNonceAge(long maxNonceAge) {
        this.maxNonceAge = maxNonceAge;
    }

    /**
     * Exception to be thrown when an Error in processing received nonces occurs.
     * A Nonce-replay would also be indicated by a NonceException.
     */
    public static class NonceException extends XWSSecurityException {

        /**
         * Constructor specifying the message string.
         * @param message the exception message string
         */
        public NonceException(String message) {
            super(message);
        }

        /**
         * Constructor specifying the message string and a  nested exception
         * @param message the exception message string
         * @param cause the nested exception as a Throwable
         */
        public NonceException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructor specifying a nested exception
         * @param cause the nested exception as a Throwable
         */
        public NonceException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * 
     * @param nonce the nonce to be validated
     * @param created the creation time of the nonce as indicated in the UsernameToken
     * @return true if the nonce is not a replay
     * @throws com.sun.xml.wss.NonceManager.NonceException  if a replay is detected
     */
    public abstract boolean validateNonce(String nonce, String created) throws NonceException;

    /**
     * 
     * @param maxNonceAge the approximate maximum age for which a received nonce would be stored by the NonceManager
     * @return the singleton instance of the configured NonceManager, calling getInstance with different maxNonceAge 
     * will have no effect and will instead return the same NonceManager which was initialized first.
     */
    public static synchronized NonceManager getInstance(final long maxNonceAge, final WSEndpoint endpoint) {

        if (endpoint == null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,
                        String.format("getInstance(): endpoint is null: using singleton"));
            }
        }

        NonceManager nonceMgr =
                endpoint != null ? nonceMgrMap.get(endpoint) : jaxRPCNonceManager;

        if (nonceMgr != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,
                        String.format("getInstance(%s): found existing: %s",
                        endpoint, nonceMgr));
            }
            return nonceMgr;
        }

        final URL url = SecurityUtil.loadFromClasspath("/META-INF/services/" + nonceManager);
        if (url != null) {
            Object obj = SecurityUtil.loadSPIClass(url, nonceManager);
            if ((obj != null) && !(obj instanceof NonceManager)) {
                throw new XWSSecurityRuntimeException("Class :" + obj.getClass().getName() + " is not a valid NonceManager");
            }
            nonceMgr = (NonceManager) obj;
        }

        /*if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()) {
        final BackingStoreFactory bsFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);
        BackingStore<String, HANonceManager.MyPojo> backingStore = HighAvailabilityProvider.INSTANCE.createBackingStore(bsFactory, "HANonceManagerStore", String.class, HANonceManager.MyPojo.class);
        nonceMgr = new HANonceManager(backingStore, maxNonceAge);
        } else {
        nonceMgr = new DefaultNonceManager();
        }*/

        if (nonceMgr == null) {
            nonceMgr = new DefaultNonceManager();
        }

        nonceMgr.setMaxNonceAge(maxNonceAge);

        if (endpoint != null) {
            nonceMgrMap.put(endpoint, nonceMgr);
            endpoint.getManagedObjectManager().registerAtRoot(nonceMgr, NONCE_MANAGER);
        } else {
            jaxRPCNonceManager = nonceMgr;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE,
                    String.format("getInstance(%s): created: %s",
                    endpoint, nonceMgr));
        }

        return nonceMgr;
    }

    public static synchronized void deleteInstance(final WSEndpoint endpoint) {
        final Object o = endpoint != null ? nonceMgrMap.remove(endpoint) : jaxRPCNonceManager;
        if (endpoint == null) {
            jaxRPCNonceManager = null;
        }
        if (endpoint != null && o != null) {
            endpoint.getManagedObjectManager().unregister(o);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE,
                    String.format("deleteInstance(%s): %s", endpoint, o));
        }

    }
}
