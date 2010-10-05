/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.security.policy;


/**
 *
 * @author K.Venugopal@sun.com
 */
public interface Token{
    public static final String WSS11 = "WSS11";
    public static final String WSS10 = "WSS10";
    public static final String REQUIRE_KEY_IDENTIFIER_REFERENCE="RequireKeyIdentifierReference";
    public static final String REQUIRE_ISSUER_SERIAL_REFERENCE="RequireIssuerSerialReference";
    public static final String REQUIRE_EMBEDDED_TOKEN_REFERENCE="RequireEmbeddedTokenReference";
    public static final String REQUIRE_THUMBPRINT_REFERENCE="RequireThumbprintReference";
    public static final String REQUIRE_EXTERNAL_URI_REFERENCE = "RequireExternalUriReference";
    public static final String REQUIRE_EXTERNAL_REFERENCE = "RequireExternalReference";
    public static final String REQUIRE_INTERNAL_REFERENCE  = "RequireInternalReference";
    public static final String WSSX509V1TOKEN10 ="WssX509V1Token10";
    public static final String WSSX509V3TOKEN10="WssX509V3Token10";
    public static final String WSSX509PKCS7TOKEN10="WssX509Pkcs7Token10";
    public static final String WSSX509PKIPATHV1TOKEN10="WssX509PkiPathV1Token10";
    public static final String WSSX509V1TOKEN11="WssX509V1Token11";
    public static final String WSSX509V3TOKEN11="WssX509V3Token11";
    public static final String WSSX509PKCS7TOKEN11="WssX509Pkcs7Token11";
    public static final String WSSX509PKIPATHV1TOKEN11="WssX509PkiPathV1Token11";
    public static final String WSSKERBEROS_V5_AP_REQ_TOKEN11 = "WssKerberosV5ApReqToken11";
    public static final String WSSKERBEROS_GSS_V5_AP_REQ_TOKEN11="WssGssKerberosV5ApReqToken11";
    public static final String REQUIRE_DERIVED_KEYS="RequireDerivedKeys";
    public static final String SC10_SECURITYCONTEXT_TOKEN="SC10SecurityContextToken";
    public static final String WSS_SAML_V10_TOKEN10="WssSamlV10Token10";
    public static final String WSS_SAML_V11_TOKEN10="WssSamlV11Token10";
    public static final String WSS_SAML_V10_TOKEN11="WssSamlV10Token11";
    public static final String WSS_SAML_V11_TOKEN11="WssSamlV11Token11";
    public static final String WSS_SAML_V20_TOKEN11="WssSamlV20Token11";
    public static final String WSS_REL_V10_TOKEN10="WssRelV10Token10";
    public static final String WSS_REL_V20_TOKEN10="WssRelV20Token10";
    public static final String WSS_REL_V10_TOKEN11="WssRelV10Token11";
    public static final String WSS_REL_V20_TOKEN11="WssRelV20Token11";
    public static final String WSS_USERNAME_TOKEN_10 ="WssUsernameToken10";
    public static final String WSS_USERNAME_TOKEN_11 ="WssUsernameToken11";
    public static final String RSA_KEYVALUE_TOKEN = "RsaKeyValue";
    
    /**
     * returns the token inclusion value
     * @return one of <CODE>ONCE</CODE>,<CODE>NEVER</CODE>,<CODE>ALWAYS_TO_RECIPIENT</CODE>,<CODE>ALWAYS</CODE>
     */
    public String getIncludeToken();
    
    
    /**
     * Unique Id assigned to the token.
     * @return String representation of the token id.
     */
    public String getTokenId();
    
    /**
     * @return version of SecurityPolicy being used
     */
    public SecurityPolicyVersion getSecurityPolicyVersion();
}
