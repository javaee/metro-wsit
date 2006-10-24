package com.sun.xml.ws.runtime.util;
import com.sun.xml.ws.rm.Sequence;
import com.sun.xml.ws.security.SecurityContextTokenInfo;

/**
 * The <code> Session </Session> object is used to manage state between multiple requests
 * from the same client. It contains a session key field to uniquely identify the Session, 
 * a <code>SecurityInfo</code> field that contains the security parameters used to
 * protect the session and  userdata field that can store the state for multiple 
 * requests from the client.
 *
 * @author Bhakti Mehta
 * @author Mike Grogan
 */
public  class Session {
    
    /**
     * Well-known invocationProperty names
     */
    public static final String SESSION_ID_KEY = "com.sun.xml.ws.sessionid";
    public static final String SESSION_KEY = "com.sun.xml.ws.session";

    /**
     * Session manager that can handle Sessions of this exact type.
     * (Different SessionManagers might use different subclasses of this Class)
     */
    private final SessionManager manager;
    
    /*
     * These fields might be persisted
     */
    /**
     * Unique key based either on the SCT or RM sequence id for the session
     */
    private final String key;
    
    /**
     * A container for user-defined data that will be exposed in WebServiceContext.
     */
    private final Object userData;
    
    
    private SecurityContextTokenInfo securityInfo;
    private Sequence sequence;
    
    /*
     * These fields are for internal use
     */
    private final long creationTime;
    private long lastAccessedTime;
    
    /**
     * Public constructor
     *
     * @param manager - A <code>SessionManager</code> that can handle <code>Sessions</code>
     * of this type.  
     * @param key - The unique session id
     * @param data - Holder for user-defined data.
     */
    public Session(SessionManager manager, String key, Object userData) {
        this.manager = manager;
        this.userData = userData;
        this.key = key;
        creationTime = lastAccessedTime = 
              System.currentTimeMillis();
    }
    

    /**
     * Accessor for Session Key.
     *
     * @returns The session key
     */
    public String getSessionKey() {
        return key;
    }
    
    /**
     * Accessor for the <code>userData</code> field.
     *
     * @return The value of the field.
     */
    public Object getUserData() {
        return userData;
    }
    
    
    /**
    * Accessor for the <code>securityInfo</code> field.
    * 
    * @returns The value of the field.
    */
    public SecurityContextTokenInfo getSecurityInfo() {
        return securityInfo;
    }
    
   /**
    * Mutator for the <code>securityInfo</code> field.
    * 
    * @returns The value of the field.
    */
    public void setSecurityInfo(SecurityContextTokenInfo securityInfo) {
        this.securityInfo = securityInfo;
    }
    
    
   /**
    * Accessor for the <code>sequence</code> field.
    * 
    * @return The value of the field.
    */
    public Sequence getSequence() {
        return sequence;
    }
    
    /**
    * Mutator for the <code>securityInfo</code> field.
    * 
    * @returns The value of the field.
    */
    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }
    
    /**
     * Accessor for creation time.
     *
     * @returns The creation time.
     */
    public  long getCreationTime() {
        return creationTime;
    }

    /**
     * Accessor for lastAccessed time, which can be used to invalidate Sessions 
     * have not been active since a certain time.
     *
     * @returns The lastAccessedTime
     */
    public  long getLastAccessedTime() {
        return lastAccessedTime;
    }
    
    /**
     * Resets the lastAccessedTime to the current time.
     */
    public void resetLastAccessedTime() {
        lastAccessedTime = System.currentTimeMillis();
    }
    
    /**
     * Saves the state of the session using whatever persistence mechanism the
     * <code>SessionManager</code> offers.
     */
    public void save() {
        manager.saveSession(getSessionKey());
    }

}

