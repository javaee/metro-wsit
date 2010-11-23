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

/*
 * SessionManagerImpl.java
 *
 */

package com.sun.xml.ws.runtime.dev;

import com.sun.xml.ws.api.ha.HaInfo;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.security.secconv.WSSecureConversationRuntimeException;
import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.commons.ha.StickyKey;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.SecurityTokenReference;
import com.sun.xml.ws.security.Token;
import com.sun.xml.wss.XWSSecurityException;
import java.net.URI;
import java.security.Key;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.security.auth.Subject;
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

    private final BackingStore<StickyKey, HASecurityContextTokenInfo> sctBs;
    
    /** Creates a new instance of SessionManagerImpl */
    public SessionManagerImpl(WSEndpoint endpoint, boolean isSC) {
        if (isSC){
            final BackingStoreFactory bsFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);
            this.sctBs = HighAvailabilityProvider.INSTANCE.createBackingStore(
                bsFactory,
                endpoint.getServiceName() + ":" + endpoint.getPortName()+ "_SCT_BS",
                StickyKey.class,
                HASecurityContextTokenInfo.class);
        } else{
            sctBs = null;
        }
    }
    
    /**
     * Returns an existing session identified by the Key else null
     *
     * @param key The Session key.
     * @returns The Session with the given key.  <code>null</code> if none exists.
     */
    public Session  getSession(String key) {
        Session session = sessionMap.get(key);
        if (session == null && HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured() && sctBs != null){
            SecurityContextTokenInfo sctInfo = HighAvailabilityProvider.loadFrom(sctBs, new StickyKey(key), null);
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
        if (HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured() && sctBs != null){
            HighAvailabilityProvider.removeFrom(sctBs, new StickyKey(key));
        }
    }

    /**
     * Creates a Session with the given key, using a new instance
     * of the specified Class as a holder for user-defined data.  The
     * specified Class must have a default constructor.
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
        return createSession(key, null, obj);
    }
    
    public Session createSession(String key, SecurityContextTokenInfo sctInfo, Object obj) {
        
        Session sess = new Session(this, key, obj);
        sessionMap.put(key, sess);

        if (sctInfo != null && HighAvailabilityProvider.INSTANCE.isHaEnvironmentConfigured()){
            HASecurityContextTokenInfo hasctInfo = new HASecurityContextTokenInfo(sctInfo);
            HaInfo haInfo = HaContext.currentHaInfo();
            if (haInfo != null) {
                HighAvailabilityProvider.saveTo(sctBs, new StickyKey(key, haInfo.getKey()), hasctInfo, true);
            } else {
                final StickyKey stickyKey = new StickyKey(key);
                final String replicaId = HighAvailabilityProvider.saveTo(sctBs, stickyKey, hasctInfo, true);
                HaContext.updateHaInfo(new HaInfo(stickyKey.getHashKey(), replicaId, false));
            }
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
            boolean recovered = false;
            Session session = getSession(key);            
            if (session != null) {
                // recreate context info based on data stored in the session
                SecurityContextTokenInfo sctInfo = session.getSecurityInfo();
                if (sctInfo != null) {
                    ctx = sctInfo.getIssuedTokenContext();
                    // Add it to the Session Manager's local cache, after possible crash                
                    addSecurityContext(key, ctx);               
                    recovered = true;
                }
            }
            
            if (!recovered){                
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
    
    static class HASecurityContextTokenInfo implements SecurityContextTokenInfo{
        
        String identifier = null;
        String extId = null;
        String instance = null;
        byte[] secret = null;
        Map<String, byte[]> secretMap = new HashMap<String, byte[]>();
        Date creationTime = null;
        Date expirationTime = null;

        public HASecurityContextTokenInfo() {
            
        }
        
        public HASecurityContextTokenInfo(SecurityContextTokenInfo sctInfo) {
            
        }

    
        public String getIdentifier() {
            return identifier;
        }
    
        public void setIdentifier(final String identifier) {
            this.identifier = identifier;
        }

        /*
         * external Id corresponds to the wsu Id on the token.
         */
        public String getExternalId() {
            return extId;
        }

        public void setExternalId(final String externalId) {
            this.extId = externalId;
        }
    
        public String getInstance() {
            return instance;
        }

        public void setInstance(final String instance) {
            this.instance = instance;
        }

        public byte[] getSecret() {
            byte [] newSecret = new byte[secret.length];
            System.arraycopy(secret,0,newSecret,0,secret.length);
            return newSecret;
        }

        public byte[] getInstanceSecret(final String instance) {
            return secretMap.get(instance);
        }

        public void addInstance(final String instance, final byte[] key) {
            byte [] newKey = new byte[key.length];
            System.arraycopy(key,0,newKey,0,key.length);
            if (instance == null) {
                this.secret = newKey;
            } else {
                secretMap.put(instance, newKey);
            }
        }
    
        public Date getCreationTime() {
            return new Date(creationTime.getTime());
        }

        public void setCreationTime(final Date creationTime) {
            this.creationTime = new Date(creationTime.getTime());
        }

        public Date getExpirationTime() {
            return new Date(expirationTime.getTime());
        }

        public void setExpirationTime(final Date expirationTime) {
            this.expirationTime = new Date(expirationTime.getTime());
        }

        public Set getInstanceKeys() {
          return null;
        }
    
        public IssuedTokenContext getIssuedTokenContext() {

            final IssuedTokenContext itc = new HAIssuedTokenContext();
            itc.setCreationTime(getCreationTime());
            itc.setExpirationTime(getExpirationTime());
            itc.setProofKey(getSecret());
            itc.setSecurityContextTokenInfo(this);
        
            return itc;
        }

        public IssuedTokenContext getIssuedTokenContext(SecurityTokenReference reference) {
            return null;
        }
    }
    
    static class HAIssuedTokenContext implements IssuedTokenContext {
        X509Certificate x509Certificate = null;
        Token securityToken = null;
        Token associatedProofToken = null;
        Token secTokenReference = null;
        Token unAttachedSecTokenReference = null;
        ArrayList<Object> securityPolicies = new ArrayList<Object>();
        Object otherPartyEntropy = null;
        Object selfEntropy = null;
        URI computedKeyAlgorithm;
        String sigAlgorithm;
        String encAlgorithm;
        String canonicalizationAlgorithm;
        String signWith;
        String encryptWith;
        byte[] proofKey = null; // used in SecureConversation
        SecurityContextTokenInfo sctInfo = null; // used in SecureConversation
        Date creationTime = null;
        Date expiryTime = null;
        String username = null;
        String endPointAddress = null;
        Subject subject;
        KeyPair proofKeyPair;
        String authType = null;
        String tokenType = null;
        String keyType = null;
        String tokenIssuer = null;
        Token target = null;

        Map<String, Object> otherProps = new HashMap<String, Object>();

        public X509Certificate getRequestorCertificate() {
            return x509Certificate;
        }

        public void setRequestorCertificate(X509Certificate cert) {
            this.x509Certificate = cert;
        }

        public Subject getRequestorSubject(){
            return subject;
        }

        public void setRequestorSubject(Subject subject){
            this.subject = subject;
        }

        public String getRequestorUsername() {
            return username;
        }

        public void setRequestorUsername(String username) {
            this.username = username;
        }


        public void setSecurityToken(Token securityToken) {
            this.securityToken = securityToken;
        }

        public Token getSecurityToken() {
            return securityToken;
        }

        public void setAssociatedProofToken(Token associatedProofToken) {
            this.associatedProofToken = associatedProofToken;
        }

        public Token getAssociatedProofToken() {
            return associatedProofToken;
        }

        public Token getAttachedSecurityTokenReference() {
            return secTokenReference;
        }

        public void setAttachedSecurityTokenReference(Token secTokenReference) {
            this.secTokenReference = secTokenReference;
        }

        public Token getUnAttachedSecurityTokenReference() {
            return unAttachedSecTokenReference;
        }

        public void setUnAttachedSecurityTokenReference(Token secTokenReference) {
            this.unAttachedSecTokenReference = secTokenReference;
        }

        public ArrayList<Object> getSecurityPolicy() {
            return securityPolicies;
        }

        public void setOtherPartyEntropy(Object otherPartyEntropy) {
            this.otherPartyEntropy = otherPartyEntropy;
        }

        public Object getOtherPartyEntropy() {
            return otherPartyEntropy;
        }

        public Key getDecipheredOtherPartyEntropy(Key privKey) throws XWSSecurityException {
            return null;
        }

        public void setSelfEntropy(Object selfEntropy) {
            this.selfEntropy = selfEntropy;
        }

        public Object getSelfEntropy() {
            return selfEntropy;
        }


        public URI getComputedKeyAlgorithmFromProofToken() {
            return computedKeyAlgorithm;
        }

        public void setComputedKeyAlgorithmFromProofToken(URI computedKeyAlgorithm) {
            this.computedKeyAlgorithm = computedKeyAlgorithm;
        }

        public void setProofKey(byte[] key){
            this.proofKey = key;
        }

        public byte[] getProofKey() {
            return proofKey;
        }

        public void setProofKeyPair(KeyPair keys){
            this.proofKeyPair = keys;
        }

        public KeyPair getProofKeyPair(){
            return this.proofKeyPair;
        }

        public void setAuthnContextClass(String authType){
            this.authType = authType;
        }

        public String getAuthnContextClass(){
            return this.authType;
        }

        public Date getCreationTime() {
            return creationTime;
        }

        public Date getExpirationTime() {
            return expiryTime;
        }

        public void setCreationTime(Date date) {
            creationTime = date;
        }

        public void  setExpirationTime(Date date) {
            expiryTime = date;
        }

        /**
         * set the endpointaddress
         */
        public void  setEndpointAddress(String endPointAddress){
            this.endPointAddress = endPointAddress;
        }

        /**
         *get the endpoint address
         */
        public String getEndpointAddress(){
            return this.endPointAddress;
        }

        public void destroy() {

        }

        public SecurityContextTokenInfo getSecurityContextTokenInfo() {
            return sctInfo;
        }

        public void setSecurityContextTokenInfo(SecurityContextTokenInfo sctInfo) {
            this.sctInfo = sctInfo;
        }

        public Map<String, Object> getOtherProperties() {
            return this.otherProps;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setKeyType(String keyType) {
            this.keyType = keyType;
        }

        public String getKeyType() {
            return keyType;
        }

        public void setAppliesTo(String appliesTo) {
            this.endPointAddress = appliesTo;
        }

        public String getAppliesTo() {
            return endPointAddress;
        }

        public void setTokenIssuer(String issuer) {
            this.tokenIssuer = issuer;
        }

        public String getTokenIssuer() {
            return tokenIssuer;
        }

        public void setSignatureAlgorithm(String sigAlg){
            this.sigAlgorithm = sigAlg;
        }

        public String getSignatureAlgorithm(){
            return sigAlgorithm;
        }

        public void setEncryptionAlgorithm(String encAlg){
            this.encAlgorithm = encAlg;
        }

        public String getEncryptionAlgorithm(){
            return encAlgorithm;
        }

        public void setCanonicalizationAlgorithm(String canonAlg){
            this.canonicalizationAlgorithm = canonAlg;
        }

        public String getCanonicalizationAlgorithm(){
            return canonicalizationAlgorithm;
        }

        public void setSignWith(String signWithAlgo){
            this.signWith = signWithAlgo;
        }

        public String getSignWith(){
            return signWith;
        }    

        public void setEncryptWith(String encryptWithAlgo){
            this.encryptWith = encryptWithAlgo;
        }

        public String getEncryptWith(){
            return encryptWith;
        }

        public void setTarget(Token target) {
            this.target = target;
        }

        public Token getTarget() {
            return target;
        }
    }
}
