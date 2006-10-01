/*
 * SessionManagerImpl.java
 *
 */

package com.sun.xml.ws.runtime.util;
import java.util.Set;
import java.util.Hashtable;

/**
 * In memory implementation of <code>SessionManager</code>
 *
 * @author Mike Grogan
 */
public class SessionManagerImpl extends SessionManager {
    
    /**
     * Map of session id --> session
     */
    private Hashtable<String, Session> sessionMap
            = new Hashtable<String, Session>();
    
    
    /** Creates a new instance of SessionManagerImpl */
    public SessionManagerImpl() {
        
    }
    
    /**
     * Returns an existing session identified by the Key else null
     *
     * @param key The Session key.
     * @returns The Session with the given key.  <code>null</code> if none exists.
     */
    public Session  getSession(String key) {
        return sessionMap.get(key);
    }

    /**
     * Returns the Set of valid Session keys.
     *
     * @returns The Set of keys.
     */
    public Set<String> getKeys() {
        return sessionMap.keySet();
    }

    /**
     * Removed the Session with the given key.
     *
     * @param key The key of the Session to be removed.
     */
    public void terminateSession(String key) {
        sessionMap.remove(key);
    }

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
    public  Session createSession(String key, Class clasz) {
        Session sess;
        try {
            sess = new Session(this, key, clasz.newInstance());
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException ee) {
            return null;
        }
        
        sessionMap.put(key, sess);
        return sess;
        
    }
    
    
    /**
     * Creates a Session with the given key, using the specified Object
     * as a holder for user-defined data.
     *
     * @param key The Session key to be used.
     * @param obj The object to use as a holder for user data in the session.
     * @returns The new Session. 
     * 
     */ 
    public Session createSession(String key, Object obj) {
        
        Session sess = new Session(this, key, obj);
        sessionMap.put(key, sess);
        return sess;
    }
    
     /**
     * Creates a Session with the given key, using an instance of 
     * java.util.Hashtable<String, String> asa holder for user-defined data.
     *
     * @param key The Session key to be used.
     * @returns The new Session.
     * 
     */ 
    public Session createSession(String key) {   
       return createSession(key, new java.util.Hashtable<String, String>());
    }
    
 
     
    /**
     * Does nothing in this implementation.
     *
     * @param key The key of the session to be saved
     */
    public void saveSession(String key) {        
    }

}
