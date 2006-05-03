/*
 * $Id: UseKey.java,v 1.1 2006-05-03 22:57:20 arungupta Exp $
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

package com.sun.xml.ws.security.trust.elements;

import java.net.URI;

import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;

/**
 *
 * @author WS-Trust Implementation Team
 */
public interface UseKey {
     
    /**
     * Get the type of the UseKey information item
     */
    String getTargetType();
    
    /**
     * Set the type of the DelegateTo information item
     *  @param targetType {@link String}
     */
    void setTargetType(String targetType);
    
    /**
     * Gets the value of the any property.
     *
     *
     * @return possible object is
     *     {@link Element }
     *     {@link Object }
     */
    Object getAny();
    
    /**
     * Sets the value of the any property.
     *
     *
     * @param value
     *     allowed object is
     *     {@link Element }
     *     {@link Object }
     */
    void setAny(Object value);
    
    /**
     * Set the STR for the Token as the contents of UseKey
     */
    void setSecurityTokenReference(SecurityTokenReference ref);
    
    /**
     * Get the STR contained in this Element, null otherwise
     */
    SecurityTokenReference getSecurityTokenReference();
    
    /**
     * Set the Token as the contents of UseKey
     */
    void setToken(Token token);
    
    /**
     * Get the Token contained in the element, null otherwise.
     */
    Token getToken();    
    
    /**
     * Set the option Sig attribute of UseKey
     */
    void setSignatureID(URI sigID);
    
    /**
     * get the Sig attribute value if set, null otherwise
     */
    URI getSignatureID();
}
