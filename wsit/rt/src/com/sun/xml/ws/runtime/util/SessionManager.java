/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.runtime.util;

import com.sun.istack.logging.Logger;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.util.ServiceFinder;

import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedObjectManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * The <code>SessionManager</code> is used to obtain session information
 * This can be implemented using persisitent storage mechanisms or using transient storage
 * Even if it is implemented using persistent storage the implementation should take care 
 * of backing by  a cache which will avoid the overhead of serialization and database 
 * operations
 * <p>
 * Additonally the <code>SessionManager</code> is responsible for managing the lifecycle
 * events for the sessions. It exposes methods to create and terminate the session
 * Periodically the <code>SessionManager</code> will  check for sessions who have been inactive for
 * a  predefined amount of time and then will terminate those sessions
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */

@ManagedObject
@Description("Session manager used by RM and SC")
@AMXMetadata(type="RM_SC_SessionManager")
public abstract class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class);
    private static Map<WSEndpoint, SessionManager> sessionManagers = new HashMap<WSEndpoint, SessionManager>();
     
    /**
     * Returns an existing session identified by the Key else null
     *
     * @param key The Session key.
     * @returns The Session with the given key.  <code>null</code> if none exists.
     */
    public abstract Session  getSession(String key) ;

    /**
     * Returns the Set of valid Session keys.
     *
     * @returns The Set of keys.
     */
    @ManagedAttribute
    @Description("The set of valid Session keys")
    public abstract Set<String> keys();

    /**
     * @returns The Collection of valid Sessions.
     */
    @ManagedAttribute
    @Description("The collection of valid Sessions")
    protected abstract Collection<Session> sessions();

    /**
     * Removed the Session with the given key.
     *
     * @param key The key of the Session to be removed.
     */
    public abstract void terminateSession(String key);

    /**
     * Creates a Session with the given key, using a new instance
     * of the specified Class as a holder for user-defined data.  The
     * specified Class must have a default ctor.
     *
     * @param key The Session key to be used.
     * @returns The new Session.. <code>null</code> if the given
     * class cannot be instantiated.
     * 
     */ 
    public abstract Session createSession(String key, Class clasz);
    
     /**
     * Creates a Session with the given key, using the specified Object
     * as a holder for user-defined data.
     *
     * @param key The Session key to be used.
     * @param obj The object to use as a holder for user data in the session.
     * @returns The new Session. 
     * 
     */ 
    public abstract Session createSession(String key, Object obj);
    
     /**
     * Creates a Session with the given key, using an instance of 
     * java.util.Hashtable<String, String> asa holder for user-defined data.
     *
     * @param key The Session key to be used.
     * @returns The new Session.
     * 
     */ 
    public abstract Session createSession(String key);
    
     
    /**
     * Saves the state of the Session with the given key.
     *
     * @param key The key of the session to be saved
     */
    public abstract void saveSession(String key);
    
    /**
     * Return the valid SecurityContext for matching key
     *
     * @param key The key of the security context to be looked
     * @param expiryCheck indicates whether to check the token expiry or not, 
     *                    As in case of renew we don't need to check token expiry
     * @returns IssuedTokenContext for security context key
     */
    public abstract IssuedTokenContext getSecurityContext(String key, boolean checkExpiry);
    
    /**
     * Add the SecurityContext with key in local cache
     *
     * @param key The key of the security context to be stored     
     * @param itctx The IssuedTokenContext to be stored
     */
    public abstract void addSecurityContext(String key, IssuedTokenContext itctx);
    
    public static void removeSessionManager(WSEndpoint endpoint){
        synchronized (SessionManager.class) {
            try {
                LOGGER.entering();
                Object o = sessionManagers.remove(endpoint);
                LOGGER.config(String.format("removeSessionManager(%s): %s",
                                            endpoint, o));
                if (o != null) {
                    endpoint.getManagedObjectManager().unregister(o);
                }
            } finally {
                LOGGER.exiting();
            }
        }
    }

    /**
     * Returns the single instance of SessionManager
     * Use the usual services mechanism to find implementing class.  If not
     * found, use <code>com.sun.xml.ws.runtime.util.SessionManager</code> 
     * by default.
     *
     * @return The value of the <code>manager</code> field.
     */ 
    public static SessionManager getSessionManager(WSEndpoint endpoint) {
        synchronized (SessionManager.class) {
            try {
                LOGGER.entering();
                SessionManager sm = sessionManagers.get(endpoint);
                if (sm == null) {
                    ServiceFinder<SessionManager> finder = 
                        ServiceFinder.find(SessionManager.class);
                    if (finder != null && finder.toArray().length > 0) {
                        sm = finder.toArray()[0];
                    } else {
                        sm = new SessionManagerImpl();
                    }
                    sessionManagers.put(endpoint, sm);
                    endpoint.getManagedObjectManager()
                        .registerAtRoot(sm, "RM_SC_SessionManager");
                    LOGGER.config(String.format("getSessionManager(%s): created: %s", endpoint, sm));
                } else {
                    LOGGER.config(String.format("getSessionManager(%s): found existing: %s", endpoint, sm));
                }
                return sm;
            } finally {
                LOGGER.exiting();
            }
        }
    }
}

