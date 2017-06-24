/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
    
    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is any object.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. 
     * 
     * 
     * @return
     *     always non-null
     */
    Map<String, Object> getOtherOptions();
    
    /**
     *  Get <code>CallbackHandler</code> for handling certificates for the 
     *  service provider and keys for the STS.
     *
     */
    CallbackHandler getCallbackHandler();
    
   // void addTokenGenerator(IssuedTokenGenerator tokenGen, String tokenType);
    
    //IssuedTokenGenerator getTokenGenerator(String tokenType);
    
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
