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
    
    /**
     * returns the issuer for the X509 token.
     * @return returns the issuer
     */
    public Issuer getIssuer();
    
    /**
     * 
     * @return the issuer name for X509 token
     */
    public IssuerName getIssuerName();
    
    /**
     * 
     * @return Claims
     */ 
    public Claims getClaims();
     
}
