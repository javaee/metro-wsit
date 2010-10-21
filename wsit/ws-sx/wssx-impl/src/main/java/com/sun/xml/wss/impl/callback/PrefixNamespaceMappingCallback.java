/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: PrefixNamespaceMappingCallback.java,v 1.2 2010-10-21 15:37:24 snajper Exp $
 */

package com.sun.xml.wss.impl.callback;

import javax.security.auth.callback.Callback;
import java.util.Properties;

/**
 * Note: This callback has been deprecated and disabled.
 * <P>
 * This callback is an optional callback that can be handled by an
 * implementation of CallbackHandler to register any prefix versus
 * namespace-uri mappings that the developer wants to make use of in the
 * security configuration.
 *
 * <p>Note: The following prefix-namespace mappings are supported by default
 * and hence do not require to be registered.
 *
 * <ul>
 * <li>env       : http://schemas.xmlsoap.org/soap/envelope/ </li>
 * <li>S         : http://schemas.xmlsoap.org/soap/envelope/ </li>
 * <li>SOAP-ENV  : http://schemas.xmlsoap.org/soap/envelope/ </li>
 * <li>ds        : http://www.w3.org/2000/09/xmldsig# </li>
 * <li>xenc      : http://www.w3.org/2001/04/xmlenc# </li>
 * <li>wsse      : http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd </li>
 * <li>wsu       : http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd </li>
 * <li>saml      : urn:oasis:names:tc:SAML:1.0:assertion </li>
 * </ul>
 * 
 * @deprecated : since XWS 2.0 EA
 */
public class PrefixNamespaceMappingCallback extends XWSSCallback implements Callback {
                                                                                                         
    private Properties prefixNamespaceMappings = null;

    /**
     * Set the prefix:namespace-uri mappings to be registered
     *
     * @param mappings the <code>Properties</code> to be registered
     */
    public void setMappings(Properties mappings) {
        prefixNamespaceMappings = mappings;
    }

    /**
     * @return the prefix:namespace-uri mappings
     */
    public Properties getMappings() {
        return prefixNamespaceMappings;
    }
}
