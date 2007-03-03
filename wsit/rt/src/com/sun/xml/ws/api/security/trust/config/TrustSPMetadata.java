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
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.xml.ws.api.security.trust.config;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 * <p>
 * This interface captures metadata of a service provider. 
 * </p>
 @author Jiandong Guo
 */
public interface TrustSPMetadata{
                
     /**
     * Gets the alias for the certificate of the service provider.
     * 
     * @return the cert alias of the service provider
     */
    String getCertAlias();
    
    /**
     * Gets the token type for the  service provider.
     * 
     * @return the token type of the service provider
     */
    String getTokenType();
     
    /**
     * Gets the key type for the  service provider.
     * 
     * @return the key type of the service provider
     */
    String getKeyType();
    
      /**
     * Gets a map that contains the other attributes for the service provider.
     * 
     * @return
     *     always non-null
     */
    Map<String, Object> getOtherOptions(); 
}
