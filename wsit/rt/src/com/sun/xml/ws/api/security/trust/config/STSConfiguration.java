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
 * STSConfiguration.java
 *
 * Created on January 23, 2007, 1:19 PM
 *
 */

package com.sun.xml.ws.api.security.trust.config;

import javax.security.auth.callback.CallbackHandler;

/**
 *
 * @author Jiandong Guo
 */
public interface STSConfiguration {
    
    String getType();
        
    String getIssuer();
        
    boolean getEncryptIssuedToken();
        
    boolean getEncryptIssuedKey();
        
    long getIssuedTokenTimeout();
    
    void setCallbackHandler(CallbackHandler callbackHandler);
    
    CallbackHandler getCallbackHandler();
    
    void addTrustSPMetadata(TrustSPMetadata data, String spEndpoint);
    
    TrustSPMetadata getTrustSPMetadata(String spEndpoint);
}
