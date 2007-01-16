/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.security.secconv.impl;

import com.sun.xml.ws.runtime.util.Session;
import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.secconv.WSSCElementFactory;

import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The </code>SecurityContextTokenInfo</code> class represents security parameters
 * which will be saved in the <code>Session</code> object so that whenever the endpoint
 * crashes the security negotiations can be resumed from its original state and no new
 * negotiations need to be done.
 *
 * @author Manveen Kaur (manveen.kaur@sun.com)
 */
public class SecurityContextTokenInfoImpl implements SecurityContextTokenInfo {
    
    String identifier = null;
    String extId = null;
    byte[] secret = null;
    Map<String, byte[]> secretMap = new HashMap<String, byte[]>();
    Date creationTime = null;
    Date expirationTime = null;
    
    private static WSSCElementFactory factory = WSSCElementFactory.newInstance();
    
    // default constructor
    public SecurityContextTokenInfoImpl() {
        //empty constructor
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
// TBD   
      //return secretMap.keySet();
    }
    
    public IssuedTokenContext getIssuedTokenContext() {
        
        final IssuedTokenContext itc = new IssuedTokenContextImpl();
        itc.setCreationTime(this.getCreationTime());
        itc.setExpirationTime(this.getExpirationTime());
        itc.setProofKey(this.getSecret());
        itc.setSecurityContextTokenInfo(this);
        
        // create security token based on id and extId
        URI uri = null;
        try {
            uri = new URI(this.getIdentifier());
        } catch (URISyntaxException ex){
            throw new RuntimeException(ex.getMessage(), ex);
        }
        
        final SecurityContextToken token = factory.createSecurityContextToken(
                uri, null , this.getExternalId());
        itc.setSecurityToken(token);
        
        // Create references
        final SecurityTokenReference attachedReference = createSecurityTokenReference(token.getWsuId(),false);
        //RequestedAttachedReference rar = factory.createRequestedAttachedReference(attachedReference);
        final SecurityTokenReference unattachedRef = createSecurityTokenReference(token.getIdentifier().toString(), true);
        //RequestedUnattachedReference rur = factory.createRequestedUnattachedReference(unattachedRef);
        
        itc.setAttachedSecurityTokenReference(attachedReference);
        itc.setUnAttachedSecurityTokenReference(unattachedRef);
        
        return itc;
    }
    
    private SecurityTokenReference createSecurityTokenReference(final String id, final boolean unattached){
        final String uri = (unattached?id:"#"+id);
        final Reference ref = factory.createDirectReference(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE, uri);
        return factory.createSecurityTokenReference(ref);
    }
    
    //public static IssuedTokenContext getIssuedTokenContext(SecurityTokenReference reference) {
    public IssuedTokenContext getIssuedTokenContext(final com.sun.xml.ws.security.SecurityTokenReference reference) {
        // get str id -> get Session corresponding to id
        // from session get corresponding SCTInfo ->
        // return sctinfo's IssuedTokenContext.
        final String id = reference.getId();
        final Session session =
                SessionManager.getSessionManager().getSession(id);
        return session.getSecurityInfo().getIssuedTokenContext();
    }
    
    
}
