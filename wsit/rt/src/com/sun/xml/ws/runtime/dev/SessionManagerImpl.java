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

/*
 * SessionManagerImpl.java
 *
 */

package com.sun.xml.ws.runtime.dev;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.security.secconv.WSSecureConversationRuntimeException;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.SecurityContextTokenInfo;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.xml.namespace.QName;

import javax.xml.ws.WebServiceException;

import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreFactory;

/**
 * In memory implementation of <code>SessionManager</code>
 *
 * @author Mike Grogan
 */
public class SessionManagerImpl extends SessionManager {
    
    /**
     * Map of session id --> session
     */
    private Map<String, Session> sessionMap
            = new HashMap<String, Session>();
    /**
     * Map of SecurityContextId --> IssuedTokenContext
     */
    private Map<String, IssuedTokenContext> issuedTokenContextMap
            = new HashMap<String, IssuedTokenContext>();
    /**
     * Map of wsu:Instance --> SecurityContextTokenInfo
     */
    private Map<String, SecurityContextTokenInfo> securityContextTokenInfoMap
            = new HashMap<String, SecurityContextTokenInfo>();

    private final BackingStore<String, SecurityContextTokenInfo> sctBs;
    
    /** Creates a new instance of SessionManagerImpl */
    public SessionManagerImpl(WSEndpoint endpoint) {
        final BackingStoreFactory bsFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);
        this.sctBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                bsFactory,
                endpoint.getServiceName() + ":" + endpoint.getPortName()+ "_SCT_BS",
                String.class,
                SecurityContextTokenInfo.class);
        
    }
    
    /**
     * Returns an existing session identified by the Key else null
     *
     * @param key The Session key.
     * @returns The Session with the given key.  <code>null</code> if none exists.
     */
    public Session  getSession(String key) {
        Session session = sessionMap.get(key);
        if (session ==null && HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()){
            SecurityContextTokenInfo sctInfo = HighAvailabilityProvider.loadFrom(sctBs, key, null);
            session = new Session(this, key, null);
            session.setSecurityInfo(sctInfo);
            sessionMap.put(key, session);
        }
        return session;
    }

    /**
     * Returns the Set of valid Session keys.
     *
     * @returns The Set of keys.
     */
    public Set<String> keys() {
        return sessionMap.keySet();
    }

    protected Collection<Session> sessions() {
        return sessionMap.values();
    }

    /**
     * Removed the Session with the given key.
     *
     * @param key The key of the Session to be removed.
     */
    public void terminateSession(String key) {
        sessionMap.remove(key);
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()){
            HighAvailabilityProvider.removeFrom(sctBs, key);
        }
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

        SecurityContextTokenInfo sctInfo = sess.getSecurityInfo();
        if (sctInfo != null && HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()){
            HighAvailabilityProvider.saveTo(sctBs, key, (SecurityContextTokenInfo)sctInfo, true);
        }
        return sess;
    }
    
     /**
     * Creates a Session with the given key, using an instance of 
     * synchronized java.util.Map<String, String> a sa holder for user-defined data.
     *
     * @param key The Session key to be used.
     * @returns The new Session.
     * 
     */ 
    public Session createSession(String key) {   
       return createSession(key, Collections.synchronizedMap(new HashMap<String, String>()));
    }
    
     
    /**
     * Does nothing in this implementation.
     *
     * @param key The key of the session to be saved
     */
    public void saveSession(String key) {
    }

     /**
     * Return the valid SecurityContext for matching key
     *
     * @param key The key of the security context to be looked
     * @returns IssuedTokenContext for security context key
     */
    
    public IssuedTokenContext getSecurityContext(String key, boolean checkExpiry){
        IssuedTokenContext ctx = issuedTokenContextMap.get(key);        
        if(ctx == null){
            // recovery of security context in case of crash
            Session session = getSession(key);            
            if (session != null) {
                // recreate context info based on data stored in the session
                SecurityContextTokenInfo sctInfo = session.getSecurityInfo();
                ctx = sctInfo.getIssuedTokenContext();
                // Add it to the Session Manager's local cache, after possible crash                
                addSecurityContext(key, ctx);               
            } else {                
                throw new WebServiceException("Could not locate SecureConversation session for Id:" + key);
            }
        }        

        if (ctx != null && checkExpiry){
            // Expiry check of security context token
            Calendar c = new GregorianCalendar();
            long offset = c.get(Calendar.ZONE_OFFSET);
            if (c.getTimeZone().inDaylightTime(c.getTime())) {
                offset += c.getTimeZone().getDSTSavings();
            }
            long beforeTime = c.getTimeInMillis();
            long currentTime = beforeTime - offset;
            
            c.setTimeInMillis(currentTime);
            
            Date currentTimeInDateFormat = c.getTime();
            if(!(currentTimeInDateFormat.after(ctx.getCreationTime())
                && currentTimeInDateFormat.before(ctx.getExpirationTime()))){
                throw new WSSecureConversationRuntimeException(new QName("RenewNeeded"), "The provided context token has expired");
            }            
        }
        if(((SecurityContextToken)ctx.getSecurityToken()).getInstance() != null){
            String sctInfoKey = ((SecurityContextToken)ctx.getSecurityToken()).getIdentifier().toString()+"_"+
                            ((SecurityContextToken)ctx.getSecurityToken()).getInstance();                    
            //ctx.setSecurityContextTokenInfo(securityContextTokenInfoMap.get(((SecurityContextToken)ctx.getSecurityToken()).getInstance()));
            ctx.setSecurityContextTokenInfo(securityContextTokenInfoMap.get(sctInfoKey));
        }
        return ctx;
    }

    /**
     * Add the SecurityContext with key in local cache
     *
     * @param key The key of the security context to be stored
     * @param itctx The IssuedTokenContext to be stored
     */
    public void addSecurityContext(String key, IssuedTokenContext itctx){
        issuedTokenContextMap.put(key, itctx);
        if(((SecurityContextToken)itctx.getSecurityToken()).getInstance() != null){
            String sctInfoKey = ((SecurityContextToken)itctx.getSecurityToken()).getIdentifier().toString()+"_"+
                            ((SecurityContextToken)itctx.getSecurityToken()).getInstance();                    
            //securityContextTokenInfoMap.put(((SecurityContextToken)itctx.getSecurityToken()).getInstance(), itctx.getSecurityContextTokenInfo());
            securityContextTokenInfoMap.put(sctInfoKey, itctx.getSecurityContextTokenInfo());
            itctx.setSecurityContextTokenInfo(null);
        }
    }
}
