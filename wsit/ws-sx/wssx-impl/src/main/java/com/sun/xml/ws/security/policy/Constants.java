/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.security.policy;

/**
 *
 * @author K.Venugopal@sun.com
 */
public interface Constants {  
    
    public static final String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
    public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
    public static final String AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
    public static final String AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
    public static final String AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
    public static final String TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
    public static final String KW_AES128 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";
    public static final String KW_AES192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";
    public static final String KW_AES256 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";
    public static final String KW_TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";
    public static final String KW_RSA_OAEP = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
    public static final String KW_RSA15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
    public static final String PSHA1 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
    public static final String PSHA1_L128 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
    public static final String PSHA1_L192 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
    public static final String PSHA1_L256 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
    public static final String XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";
    public static final String XPATH20 = "http://www.w3.org/2002/06/xmldsig-filter2";
    public static final String C14N = "http://www.w3.org/2001/10/xml-c14n#";
    public static final String EXC14N = "http://www.w3.org/2001/10/xml-exc-c14n#";
    public static final String SNT = "http://www.w3.org/TR/soap12-n11n";
    public static final String STRT10 = "http://docs.oasis-open.org/wss/2004/xx/oasis-2004xx-wss-soapmessage- security-1.0#STR-Transform";
    
    //TODO:: Remove this constants from here.-Abhijit.
    
    public final static String MUSTSUPPORT_REF_THUMBPRINT = "MustSupportRefThumbprint";
    public final static String MUSTSUPPORT_REF_ENCRYPTED_KEY = "MustSupportRefEncryptedKey";
    public final static String REQUIRED_SIGNATURE_CONFIRMATION = "RequireSignatureConfirmation";
    
    public static final String MUST_SUPPORT_CLIENT_CHALLENGE = "MustSupportClientChallenge";
    public static final String MUST_SUPPORT_SERVER_CHALLENGE = "MustSupportServerChallenge";
    public static final String REQUIRE_CLIENT_ENTROPY = "RequireClientEntropy";
    public static final String REQUIRE_SERVER_ENTROPY= "RequireServerEntropy";
    public static final String MUST_SUPPORT_ISSUED_TOKENS = "MustSupportIssuedTokens";
    
    public static final String REQUIRE_REQUEST_SECURITY_TOKEN_COLLECTION = "RequireRequestSecurityTokenCollection";
    public static final String REQUIRE_APPLIES_TO = "RequireAppliesTo";
}
