/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
