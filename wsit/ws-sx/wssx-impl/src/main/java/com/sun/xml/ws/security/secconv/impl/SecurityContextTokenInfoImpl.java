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

package com.sun.xml.ws.security.secconv.impl;

import com.sun.xml.ws.security.IssuedTokenContext;
import com.sun.xml.ws.security.SecurityContextToken;
import com.sun.xml.ws.security.SecurityContextTokenInfo;
import com.sun.xml.ws.security.secconv.WSSCConstants;
import com.sun.xml.ws.security.impl.IssuedTokenContextImpl;
import com.sun.xml.ws.security.trust.WSTrustElementFactory;
import com.sun.xml.ws.security.secconv.WSSCVersion;

import com.sun.xml.ws.security.trust.elements.str.Reference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

import java.net.URI;
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
    String instance = null;
    byte[] secret = null;
    Map<String, byte[]> secretMap = new HashMap<String, byte[]>();
    Date creationTime = null;
    Date expirationTime = null;
    
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
        URI uri = URI.create(this.getIdentifier());
        
        final SecurityContextToken token = WSTrustElementFactory.newInstance(WSSCVersion.WSSC_10).createSecurityContextToken(
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
        final Reference ref = WSTrustElementFactory.newInstance(WSSCVersion.WSSC_10).createDirectReference(WSSCConstants.SECURITY_CONTEXT_TOKEN_TYPE, uri);
        return WSTrustElementFactory.newInstance(WSSCVersion.WSSC_10).createSecurityTokenReference(ref);
    }
    
    //public static IssuedTokenContext getIssuedTokenContext(SecurityTokenReference reference) {
    public IssuedTokenContext getIssuedTokenContext(final com.sun.xml.ws.security.SecurityTokenReference reference) {
        // get str id -> get Session corresponding to id
        // from session get corresponding SCTInfo ->
        // return sctinfo's IssuedTokenContext.
        //final String id = reference.getId();
       // final Session session =
             //   SessionManager.getSessionManager().getSession(id);
       // return session.getSecurityInfo().getIssuedTokenContext();
        return null;
    }    
}
