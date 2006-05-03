/*
 * $Id: CancelTarget.java,v 1.1 2006-05-03 22:57:16 arungupta Exp $
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

import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;
import com.sun.xml.ws.security.Token;

/**
 * Defines Binding for requesting security tokens to be cancelled.
 *
 * @author WS-Trust Implementation Team
 */
public interface CancelTarget {
    
    /**
     *Constants denoting type of Cancel Target
     */
    public static final String STR_TARGET_TYPE="SecurityTokenReference";
    public static final String CUSTOM_TARGET_TYPE = "Custom";
    
    /**
     * Get the type of the Cancel Target information item
     */
    String getTargetType();
    
    /**
     * Gets the value of the any property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    Object getAny();

    /**
     * Sets the value of the any property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    void setAny(Object value);
    
     /**
     * Set the STR for the Token to be Cancelled
     */
    void setSecurityTokenReference(SecurityTokenReference ref);
    
    /**
     * Get the STR for the Token to be Cancelled
     */
    SecurityTokenReference getSecurityTokenReference();
    
    /**
     * Set the token to be Cancelled
     */
    void setToken(Token token);
    
     /**
     * Get the token to be Cancelled
     */
    Token getToken();
    
}
