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

package com.sun.xml.xwss;

import java.io.InputStream;
import javax.security.auth.callback.CallbackHandler;

import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.config.SecurityConfigurationXmlReader;
import com.sun.xml.wss.impl.config.ApplicationSecurityConfiguration;
import com.sun.xml.wss.impl.misc.DefaultSecurityEnvironmentImpl;
import java.io.IOException;
import java.net.URL;

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
    
    public SecurityConfiguration(URL configUrl)throws XWSSecurityException {
        
        if (configUrl == null) {
            configEmpty = true;
            return;
        }

       InputStream config = null;
       try {
           config = configUrl.openStream();
           
           if (config == null) {
               configEmpty = true;
               return;
           }
           
           configuration = SecurityConfigurationXmlReader.
                   createApplicationSecurityConfiguration(config);
           callbackhandler = (CallbackHandler)Class.forName(
                   configuration.getSecurityEnvironmentHandler(),true,
                   Thread.currentThread().getContextClassLoader()).newInstance();
           securityEnvironment =
                   new DefaultSecurityEnvironmentImpl(callbackhandler);
           
       } catch (IOException e) {
           throw new XWSSecurityException(e);
       } catch (Exception e) {
           throw new XWSSecurityException(e);
       } finally {
           try {
               if (config != null) {
                   config.close();
               }
           } catch (IOException e) {
               //do nothing
           }
       }
    } 
    
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
