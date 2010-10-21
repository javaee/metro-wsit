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
 * STSConfiguration.java
 *
 * Created on March 24, 2006, 1:19 PM
 *
 */

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.api.security.trust.config.STSConfiguration;
import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;

import java.util.HashMap;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 *
 * @author Jiandong Guo
 */
public class DefaultSTSConfiguration implements STSConfiguration{
    private Map<String, TrustSPMetadata> spMap = new HashMap<String, TrustSPMetadata>();
    private String type;
    private String issuer;
    private boolean encryptIssuedToken = false;
    private boolean encryptIssuedKey = true;
    private long issuedTokenTimeout;
    
    private CallbackHandler callbackHandler;
    
    private Map<String, Object> otherOptions = new HashMap<String, Object>();
    
    
    public void addTrustSPMetadata(final TrustSPMetadata data, final String spEndpoint){
        spMap.put(spEndpoint, data);
    }
    
    public TrustSPMetadata getTrustSPMetadata(final String spEndpoint){
        return (TrustSPMetadata)spMap.get(spEndpoint);
    }
    
    public void setType(String type){
        this.type = type;
    } 
    
    public String getType(){
        return this.type;
    }
    
    public void setIssuer(String issuer){
        this.issuer = issuer;
    }
        
    public String getIssuer(){
        return this.issuer;
    }
      
    public void setEncryptIssuedToken(boolean encryptIssuedToken){
        this.encryptIssuedToken = encryptIssuedToken;
    }
    
    public boolean getEncryptIssuedToken(){
        return this.encryptIssuedToken;
    }
        
    public void setEncryptIssuedKey(boolean encryptIssuedKey){
        this.encryptIssuedKey = encryptIssuedKey;
    }
    
    public boolean getEncryptIssuedKey(){
        return this.encryptIssuedKey;
    }
        
    public void setIssuedTokenTimeout(long issuedTokenTimeout){
        this.issuedTokenTimeout = issuedTokenTimeout;
    }
    
    public long getIssuedTokenTimeout(){
        return this.issuedTokenTimeout;
    }
    
    public void setCallbackHandler(CallbackHandler callbackHandler){
        this.callbackHandler = callbackHandler;
    }
    
    public CallbackHandler getCallbackHandler(){
        return this.callbackHandler;
    }
    
    public Map<String, Object> getOtherOptions(){
        return this.otherOptions;
    }
}
