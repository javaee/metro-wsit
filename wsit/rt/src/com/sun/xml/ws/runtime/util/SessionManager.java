
package com.sun.xml.ws.runtime.util;

import java.util.Set;
import com.sun.xml.ws.util.ServiceFinder;

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

public abstract class SessionManager {

    
    private static SessionManager manager;
     
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
    public abstract Set<String> getKeys();

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
     * Returns the single instance of SessionManager
     * Use the usual services mechanism to find implementing class.  If not
     * found, use <code>com.sun.xml.ws.runtime.util.SessionManager</code> 
     * by default.
     *
     * @return The value of the <code>manager</code> field.
     */ 
    public static SessionManager getSessionManager() {
         synchronized (SessionManager.class) {
             if (manager == null) {
                 ServiceFinder<SessionManager> finder = 
                         ServiceFinder.find(SessionManager.class);
                 if (finder != null && finder.toArray().length > 0) {
                    manager = finder.toArray()[0];
                 } else {
                    manager = new SessionManagerImpl();
                 }
             }
             return manager;
         }
     }

}

