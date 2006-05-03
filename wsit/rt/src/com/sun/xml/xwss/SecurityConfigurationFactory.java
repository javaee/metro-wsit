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
import com.sun.xml.wss.XWSSecurityException;

/**
 * A Factory for creating an XWSSecurityConfiguration object(s). An XWSSecurityConfiguration object is used
 * by a JAXWS 2.0 Client to specify the client side security configuration.
 * A JAXWS client would specify the client side security configuration in the following manner
 * <PRE>
 *  FileInputStream f = new FileInputStream("./etc/client_security_config.xml");
 *  XWSSecurityConfiguration config = SecurityConfigurationFactory.newXWSSecurityConfiguration(f);
 *  ((BindingProvider)stub).getRequestContext().
                put(XWSSecurityConfiguration.MESSAGE_SECURITY_CONFIGURATION, config);
 * </PRE>
 *
 * @since JAXWS 2.0
 */

public class SecurityConfigurationFactory {

    /**
     * 
     * @param config XWSS Security Configuration.
     * @throws com.sun.xml.wss.XWSSecurityException is XWS-Security configuration file is not wellformed.
     */
    public static XWSSecurityConfiguration newXWSSecurityConfiguration(InputStream config) 
        throws XWSSecurityException {
        return new SecurityConfiguration(config);      
    }

}
