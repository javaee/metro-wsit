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
import com.sun.xml.ws.security.trust.elements.RequestedAttachedReference;
import com.sun.xml.ws.security.trust.elements.RequestedUnattachedReference;

import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
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
    HashMap<String, byte[]> secretMap = new HashMap();
    Date creationTime = null;
    Date expirationTime = null;
    
    private static WSSCElementFactory factory = WSSCElementFactory.newInstance();
    
    // default constructor
    public SecurityContextTokenInfoImpl() {
        
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    /*
     * external Id corresponds to the wsu Id on the token.
     */
    public String getExternalId() {
        return extId;
    }
    
    public void setExternalId(String externalId) {
        this.extId = externalId;
    }
    
    public byte[] getSecret() {
        return secret;
    }
    
    public byte[] getInstanceSecret(String instance) {
        return secretMap.get(instance);
    }
    
    public void addInstance(String instance, byte[] key) {
        if (instance == null) {
            this.secret = key;
        } else {
            secretMap.put(instance, key);
        }
    }
    
    public Date getCreationTime() {
        return creationTime;
    }
    
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
    
    public Date getExpirationTime() {
        return expirationTime;
    }
    
    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    
    public Set getInstanceKeys() {
      return null;
// TBD   
      //return secretMap.keySet();
    }
    
    public IssuedTokenContext getIssuedTokenContext() {
        
        IssuedTokenContext itc = new IssuedTokenContextImpl();
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
        
        SecurityContextToken token = factory.createSecurityContextToken(
                uri, null , this.getExternalId().toString());
        itc.setSecurityToken(token);
        
        // Create references
        SecurityTokenReference attachedReference = createSecurityTokenReference(token.getWsuId(),false);
        RequestedAttachedReference rar = factory.createRequestedAttachedReference(attachedReference);
        SecurityTokenReference unattachedReference = createSecurityTokenReference(token.getIdentifier().toString(), true);
        RequestedUnattachedReference rur = factory.createRequestedUnattachedReference(unattachedReference);
        
        itc.setAttachedSecurityTokenReference(attachedReference);
        itc.setUnAttachedSecurityTokenReference(unattachedReference);
        
        return itc;
    }
    
    private SecurityTokenReference createSecurityTokenReference(String id, boolean unattached){
        String uri = (unattached?id:"#"+id);
        Reference ref = factory.createDirectReference(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE, uri);
        return factory.createSecurityTokenReference(ref);
    }
    
    //public static IssuedTokenContext getIssuedTokenContext(SecurityTokenReference reference) {
    public IssuedTokenContext getIssuedTokenContext(com.sun.xml.ws.security.SecurityTokenReference reference) {
        // get str id -> get Session corresponding to id
        // from session get corresponding SCTInfo ->
        // return sctinfo's IssuedTokenContext.
        String id = reference.getId();
        Session session =
                SessionManager.getSessionManager().getSession(id);
        return session.getSecurityInfo().getIssuedTokenContext();
    }
    
    
}
