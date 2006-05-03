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

package com.sun.xml.ws.security.policy;

/**
 *
 * @author K.Venugopal@sun.com
 */
public interface Constants {
    public static final String SECURITY_POLICY_2005_07 = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
//    public static final String BASIC256="Basic256";
//    public static final String BASIC192="Basic192";
//    public static final String BASIC128="Basic128";
//    public static final String TRIPLEDES="TripleDes";
//    public static final String BASIC256_RSA15="Basic256Rsa15";
//    public static final String BASIC192_RSA15="Basic192Rsa15";
//    public static final String BASIC128_RSA15="Basic128Rsa15";
//    public static final String TRIPLEDES_RSA15="TripleDesRsa15";
//    public static final String BASIC256_SHA256="Basic256Sha256";
//    public static final String BASIC192_SHA256="Basic192Sha256";
//    public static final String BASIC128_SHA256="Basic128Sha256";
//    public static final String TRIPLEDES_SHA256="TripleDesSha256";
//    public static final String BASIC256_SHA256_RSA15="Basic256Sha256Rsa15";
//    public static final String BASIC192_SHA256_RSA15="Basic192Sha256Rsa15";
//    public static final String BASIC128_SHA256_RSA15="Basic128Sha256Rsa15";
//    public static final String TRIPLEDES_SHA256_RSA15="TripleDesSha256Rsa15";
  
    
    public static final String HMAC_SHA1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
    public static final String RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    public static final String SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
    public static final String SHA256 = "http://www.w3.org/2001/04/xmlenc#sha256";
    public static final String SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
    public static final String AES128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
    public static final String AES192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
    public static final String AES256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
    public static final String TRIPLE_DES = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
    public static final String KW_AES128 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";
    public static final String KW_AES192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";
    public static final String KW_AES256 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";
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
}
