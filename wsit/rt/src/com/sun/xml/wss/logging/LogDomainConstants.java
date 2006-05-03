/*
 * $Id: LogDomainConstants.java,v 1.1 2006-05-03 22:58:02 arungupta Exp $
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

package com.sun.xml.wss.logging;
import com.sun.xml.wss.*;

/**
 * @author XWS-Security Team
 *
 * This interface defines a number of constants pertaining to Logging domains.
 */

public interface LogDomainConstants {
    
    public static final String MODULE_TOP_LEVEL_DOMAIN =
            "javax.enterprise.resource.xml.webservices.security";
    
    public static final String WSS_API_DOMAIN = MODULE_TOP_LEVEL_DOMAIN;
    
    public static String CONFIGURATION_DOMAIN = MODULE_TOP_LEVEL_DOMAIN;
    
    public static String FILTER_DOMAIN = MODULE_TOP_LEVEL_DOMAIN;
    
    public static final String PACKAGE_ROOT = "com.sun.xml.wss.logging";
    
    public static final String WSS_API_DOMAIN_BUNDLE =
            PACKAGE_ROOT + ".LogStrings";
    
    public static final String FILTER_DOMAIN_BUNDLE =
            PACKAGE_ROOT + ".LogStrings";
    
    public static final String CONFIGURATION_DOMAIN_BUNDLE =
            PACKAGE_ROOT + ".LogStrings";
    
    public static final String SAML_API_DOMAIN =
            MODULE_TOP_LEVEL_DOMAIN + ".saml";
    
    public static final String SAML_API_DOMAIN_BUNDLE =
            PACKAGE_ROOT+".saml" + ".LogStrings";
    
    public static final String IMPL_DOMAIN =
            PACKAGE_ROOT + ".impl";
    
    public static final String IMPL_DOMAIN_BUNDLE = PACKAGE_ROOT + ".LogStrings";
    public static final String IMPL_SIGNATURE_DOMAIN= IMPL_DOMAIN+".dsig";
    public static final String IMPL_SIGNATURE_DOMAIN_BUNDLE =IMPL_SIGNATURE_DOMAIN + ".LogStrings";
    
    public static final String IMPL_CRYPTO_DOMAIN= IMPL_DOMAIN+".crypto";
    public static final String IMPL_CRYPTO_DOMAIN_BUNDLE = IMPL_CRYPTO_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_CANON_DOMAIN= IMPL_DOMAIN+".c14n";
    public static final String IMPL_CANON_DOMAIN_BUNDLE = IMPL_CANON_DOMAIN+ ".LogStrings";

    public static final String IMPL_CONFIG_DOMAIN= IMPL_DOMAIN+".configuration";
    public static final String IMPL_CONFIG_DOMAIN_BUNDLE = IMPL_CONFIG_DOMAIN+ ".LogStrings";

    public static final String IMPL_FILTER_DOMAIN= IMPL_DOMAIN+".filter";
    public static final String IMPL_FILTER_DOMAIN_BUNDLE = IMPL_FILTER_DOMAIN+ ".LogStrings";

}
