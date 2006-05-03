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
import java.util.Iterator;
import javax.xml.ws.addressing.EndpointReference;

/**
 * This element represents a requirement for an issued token, that is one issued by some token
 * issuer using the mechanisms defined in WS-Trust.
 *
 * @author K.Venugopal@sun.com
 */
public interface IssuedToken extends Token{
    
    /**
     * returns {@link javax.xml.ws.addressing.EndpointReference } which is the issuer for the issued token.
     * @return {@link javax.xml.ws.addressing.EndpointReference} or null
     */
    public EndpointReference getIssuer();
   
    /**
     * returns {@link RequestSecurityTokenTemplate }
     * @return {@link RequestSecurityTokenTemplate}
     */
    public RequestSecurityTokenTemplate getRequestSecurityTokenTemplate();  
  
    
    /**
     * returns a {@link java.util.Iterator } over the token reference types to be used.
     * @return either REQUIRE_KEY_IDENTIFIER_REFERENCE,REQUIRE_ISSUER_SERIAL_REFERENCE,REQUIRE_EMBEDDED_TOKEN_REFERENCE,REQUIRE_THUMBPRINT_REFERENCE
     */
    public Iterator getTokenRefernceType();
    
    public boolean isRequireDerivedKeys();
    
}
