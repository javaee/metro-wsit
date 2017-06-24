/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: LogDomainConstants.java,v 1.2 2010-10-21 15:37:46 snajper Exp $
 */

package com.sun.xml.wss.logging;
import java.util.logging.Logger;

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
    
    public static final String MISC_API_DOMAIN_BUNDLE =
            PACKAGE_ROOT+".misc" + ".LogStrings";
    
    public static final String IMPL_DOMAIN =
            PACKAGE_ROOT + ".impl";
    
    public static final String IMPL_DOMAIN_BUNDLE = PACKAGE_ROOT + ".LogStrings";
    public static final String IMPL_SIGNATURE_DOMAIN= IMPL_DOMAIN+".dsig";
    public static final String IMPL_SIGNATURE_DOMAIN_BUNDLE =IMPL_SIGNATURE_DOMAIN + ".LogStrings";

    public static final String IMPL_MISC_DOMAIN= IMPL_DOMAIN+".misc";
    public static final String IMPL_MISC_DOMAIN_BUNDLE = IMPL_MISC_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_CRYPTO_DOMAIN= IMPL_DOMAIN+".crypto";
    public static final String IMPL_CRYPTO_DOMAIN_BUNDLE = IMPL_CRYPTO_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_CANON_DOMAIN= IMPL_DOMAIN+".c14n";
    public static final String IMPL_CANON_DOMAIN_BUNDLE = IMPL_CANON_DOMAIN+ ".LogStrings";

    public static final String IMPL_CONFIG_DOMAIN= IMPL_DOMAIN+".configuration";
    public static final String IMPL_CONFIG_DOMAIN_BUNDLE = IMPL_CONFIG_DOMAIN+ ".LogStrings";

    public static final String IMPL_FILTER_DOMAIN= IMPL_DOMAIN+".filter";
    public static final String IMPL_FILTER_DOMAIN_BUNDLE = IMPL_FILTER_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_OPT_DOMAIN = IMPL_DOMAIN+".opt";
    public static final String IMPL_OPT_DOMAIN_BUNDLE = IMPL_OPT_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_OPT_SIGNATURE_DOMAIN = IMPL_OPT_DOMAIN+".signature";
    public static final String IMPL_OPT_SIGNATURE_DOMAIN_BUNDLE = IMPL_OPT_SIGNATURE_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_OPT_CRYPTO_DOMAIN = IMPL_OPT_DOMAIN+".crypto";
    public static final String IMPL_OPT_CRYPTO_DOMAIN_BUNDLE = IMPL_OPT_CRYPTO_DOMAIN+ ".LogStrings";
    
    public static final String IMPL_OPT_TOKEN_DOMAIN = IMPL_OPT_DOMAIN+".token";
    public static final String IMPL_OPT_TOKEN_DOMAIN_BUNDLE = IMPL_OPT_TOKEN_DOMAIN+ ".LogStrings";
    
    public static final Logger CRYPTO_IMPL_LOGGER =  Logger.getLogger(IMPL_CRYPTO_DOMAIN,
            IMPL_CRYPTO_DOMAIN_BUNDLE);

}
