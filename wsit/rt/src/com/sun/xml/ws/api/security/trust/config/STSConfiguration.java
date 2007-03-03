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

import java.util.Map;

/** This interface contains the attributes for configuring an STS.
 *
 * @author Jiandong Guo
 */
public interface STSConfiguration {
    
    /**
     * Gets the implementation class of <code>WSTrustContract</code> for this STS.
     * 
     * @return class name 
     */
    String getType();
        
    /**
     *  Get the Issuer for the STS which is a unique string identifing the STS.
     *
     */
    String getIssuer();
        
    /**
     *  Retruns true if the issued tokens from this STS must be encrypted.
     *
     */
    boolean getEncryptIssuedToken();
        
    /**
     *  Retruns true if the issued keys from this STS must be encrypted.
     *
     */
    boolean getEncryptIssuedKey();
        
    long getIssuedTokenTimeout();
    
    /**
     *  Set <code>CallbackHandler</code> for handling certificates for the 
     *  service provider and keys for the STS.
     *
     */
    void setCallbackHandler(CallbackHandler callbackHandler);
    
    Map<String, Object> getOtherOptions();
    
    /**
     *  Get <code>CallbackHandler</code> for handling certificates for the 
     *  service provider and keys for the STS.
     *
     */
    CallbackHandler getCallbackHandler();
    
    /**
     *  Add <code>TrustMetadata</code> for the service provider as identified by the given 
     *  end point.
     */
    void addTrustSPMetadata(TrustSPMetadata data, String spEndpoint);
    
    /**
     *  Get <code>TrustMetadata</code> for the service provider as identified by the given 
     *  end point.
     */
    TrustSPMetadata getTrustSPMetadata(String spEndpoint);
}
