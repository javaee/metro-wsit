/*
 * $Id: RequestedReference.java,v 1.1 2006-05-03 22:57:19 arungupta Exp $
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


/**
 *
 * The trust spec talks about RequestedAttachedReference,
 * RequestedUnAttachedReference (in section 6.2)
 * Base class for these two above.
 * 
 * @author WS-Trust Implementation Team.
 */
public abstract interface RequestedReference {
    /**
     * Gets the value of the securityTokenReference property.
     * 
     * @return {@link SecurityTokenReference }
     *     
     */
    SecurityTokenReference getSTR();

    /**
     * Sets the value of the securityTokenReference property.
     * 
     * @param value {@link SecurityTokenReference }
     *     
     */
    void setSTR(SecurityTokenReference value);
    
}
