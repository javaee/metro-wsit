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

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.api.security.trust.config.TrustSPMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jiandong Guo
 */
public class DefaultTrustSPMetadata implements TrustSPMetadata{
    
    private String endpoint;
    private String tokenType;
    private String keyType;
    private String certAlias;
    private Map otherOptions = new HashMap<String, Object>();
        
    /** Creates a new instance of DefaultTrustSPMetedata */
    public DefaultTrustSPMetadata(String endpoint) {
        this.endpoint = endpoint;
    }
        
    public void setCertAlias(final String certAlias){
        this.certAlias = certAlias;
    }
        
    public String getCertAlias(){
        return this.certAlias;
    }
        
    public void setTokenType(final String tokenType){
        this.tokenType = tokenType;
    }
    
     public String getTokenType(){
        return this.tokenType;
    }
     
    public void setKeyType(final String keyType){
        this.keyType = keyType;
    }
    
    public String getKeyType(){
        return this.keyType;
    }
    
    public Map<String, Object> getOtherOptions(){
        return this.otherOptions;
    }
}
