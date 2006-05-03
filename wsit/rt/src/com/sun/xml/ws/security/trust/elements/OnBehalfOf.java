/*
 * $Id: OnBehalfOf.java,v 1.1 2006-05-03 22:57:18 arungupta Exp $
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

import javax.xml.ws.addressing.EndpointReference;
import com.sun.xml.ws.security.trust.elements.str.SecurityTokenReference;

/**
 *
 * @author WS-Trust Implementation Team
 */

public interface OnBehalfOf {
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
     * Get the endpoint reference of the issuer, null if none exists.
     */
    EndpointReference getEndpointReference();

   /**
     * Set the endpoint reference of the issuer.
     */
    void setEndpointReference(EndpointReference endpointReference);

   /**
     * Set the STR for OnBehalfOf.
     */
    void setSecurityTokenReference(SecurityTokenReference ref);

    /**
     * Get the STR for OnBehalfOf, null if none exists.
     */
    SecurityTokenReference getSecurityTokenReference();

}
