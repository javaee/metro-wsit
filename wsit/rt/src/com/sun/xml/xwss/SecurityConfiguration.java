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

package com.sun.xml.xwss;

import java.io.InputStream;
import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;

/**
 * Digester for XWS-Security configuration.
 * @since JAXWS 2.0
 */

public class SecurityConfiguration implements XWSSecurityConfiguration {

    //public static final String MESSAGE_SECURITY_CONFIGURATION =
     //   "com.sun.xml.ws.security.configuration";

    private ApplicationSecurityConfiguration configuration = null;
    private CallbackHandler callbackhandler = null;
    private SecurityEnvironment securityEnvironment = null;
    private boolean configEmpty = false;
    
    /**
     * 
     * @param config XWSS Security Configuration.
     * @throws com.sun.xml.wss.XWSSecurityException is XWS-Security configuration file is not wellformed.
     */
    public SecurityConfiguration(InputStream config) 
        throws XWSSecurityException {
          
        if (config == null) {
            configEmpty = true;
            return;
        }

        try {
            configuration = SecurityConfigurationXmlReader.
                createApplicationSecurityConfiguration(config);
            callbackhandler = (CallbackHandler)Class.forName(
                configuration.getSecurityEnvironmentHandler(),true, 
                Thread.currentThread().getContextClassLoader()).newInstance();
            securityEnvironment =  
                new DefaultSecurityEnvironmentImpl(callbackhandler);
        } catch (Exception e) {
            throw new XWSSecurityException(e);
        }
    }

    /**
     * 
     * @return digested form XWS-Security configuration.
     */
    public ApplicationSecurityConfiguration getSecurityConfiguration() {
        return configuration;
    }

    /**
     * 
     * @return  instance of SecurityEnvironment configured in the XWS-Security Configuration 
     * file.
     */
    public SecurityEnvironment getSecurityEnvironment() {
         return securityEnvironment;        
    }

    /**
     * 
     * @return 
     */
    public boolean isEmpty() {
        return configEmpty;
    }
}
