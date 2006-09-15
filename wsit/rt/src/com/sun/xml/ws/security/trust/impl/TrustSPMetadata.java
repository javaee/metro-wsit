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

/*
 * TrustSPMetedata.java
 *
 * Created on March 26, 2006, 9:20 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.ws.security.trust.impl;

import com.sun.xml.ws.security.trust.Configuration;

import javax.security.auth.callback.CallbackHandler;

/**
 *
 * @author Jiandong Guo
 */
public class TrustSPMetadata implements Configuration{
    
    private String endpoint;
    private String tokenType;
    private String type;
    private String issuer;
    private boolean encryptIssuedToken;
    private boolean encryptIssuedKey;
    private String certAlias;
    private long issuedTokenTimeout;
    //private String callbackHandlerName;
    private CallbackHandler handler;

        
    /** Creates a new instance of TrustSPMetedata */
    public TrustSPMetadata(String endpoint, String tokenType) {
        this.endpoint = endpoint;
        this.tokenType = tokenType;
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
        
    public void setCertAlias(String certAlias){
        this.certAlias = certAlias;
    }
        
    public String getCertAlias(){
        return this.certAlias;
    }
        
    public void setIssuedTokenTimeout(long issuedTokenTimeout){
        this.issuedTokenTimeout = issuedTokenTimeout;
    }
        
    public long getIssuedTokenTimeout(){
        return this.issuedTokenTimeout;
    }
    
    public void setCallbackHandler(CallbackHandler handler){
        this.handler = handler;
    }
    
    public CallbackHandler getCallbackHandler(){
        return this.handler;
    }
    
  /*  public void setCallbackHandlerName(String callbackHandlerName){
        this.callbackHandlerName = callbackHandlerName;
    }
    
    public String getCallbackHandlerName(){
        return this.callbackHandlerName;
    }
  */
}
