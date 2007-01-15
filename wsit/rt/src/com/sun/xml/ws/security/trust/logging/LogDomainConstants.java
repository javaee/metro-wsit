/*
 * $Id: LogDomainConstants.java,v 1.2 2007-01-15 10:29:53 raharsha Exp $
 */

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

package com.sun.xml.ws.security.trust.logging;

import com.sun.xml.wss.*;

/**
 * @author Manveen Kaur
 *
 * This class defines a number of constants pertaining to Logging domains.
 */

public class LogDomainConstants {
    
    public static final String MODULE_TOP_LEVEL_DOMAIN =
            "javax.enterprise.resource.xml.webservices.security.trust";
    
    public static final String TRUST_IMPL_DOMAIN = MODULE_TOP_LEVEL_DOMAIN;
    
    public static final String PACKAGE_ROOT = "com.sun.xml.ws.security.trust.logging";
    
    public static final String TRUST_IMPL_DOMAIN_BUNDLE =
            PACKAGE_ROOT + ".LogStrings";
        
}
