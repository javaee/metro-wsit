/*
 * $Id: Lifetime.java,v 1.2 2006-10-17 05:45:45 raharsha Exp $
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
import com.sun.xml.ws.security.wsu10.AttributedDateTime;

/**
 *
 * @author WS-Trust Implementation Team
 */
public interface Lifetime {
    /**
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link AttributedDateTime }
     *     
     */
    AttributedDateTime getCreated();

    /**
     * Gets the value of the expires property.
     * 
     * @return
     *     possible object is
     *     {@link AttributedDateTime }
     *     
     */
    AttributedDateTime getExpires();

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributedDateTime }
     *     
     */
    void setCreated(AttributedDateTime value);

    /**
     * Sets the value of the expires property.
     * 
     * @param value
     *     allowed object is
     *     {@link AttributedDateTime }
     *     
     */
    void setExpires(AttributedDateTime value);
    
}
