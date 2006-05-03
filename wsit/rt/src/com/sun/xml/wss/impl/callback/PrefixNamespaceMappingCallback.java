/*
 * $Id: PrefixNamespaceMappingCallback.java,v 1.1 2006-05-03 22:57:43 arungupta Exp $
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
