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

import java.util.Set;

/**
 * Represents BinarySecurityToken.
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.Com
 */
public interface X509Token extends Token{
 
 
    /**
     * returns the type of the token.
     * @return one of WSSX509V1TOKEN10,WSSX509V3TOKEN10,WSSX509PKCS7TOKEN10,WSSX509PKIPATHV1TOKEN10,WSSX509V1TOKEN11,WSSX509V3TOKEN11,WSSX509PKCS7TOKEN11,WSSX509PKIPATHV1TOKEN11
     */
    public String getTokenType();
    /**
     * returns a {@link java.util.Set } over the token reference types to be used.
     * @return either REQUIRE_KEY_IDENTIFIER_REFERENCE,REQUIRE_ISSUER_SERIAL_REFERENCE,REQUIRE_EMBEDDED_TOKEN_REFERENCE,REQUIRE_THUMBPRINT_REFERENCE
     */
    public Set getTokenRefernceType();
    
     /**
     * returns true if RequiredDerivedKey element is present under X509 Token.
     * @return true if RequireDerviedKeys element is present under X509 Token or false.
     */
    public boolean isRequireDerivedKeys();
 
}
