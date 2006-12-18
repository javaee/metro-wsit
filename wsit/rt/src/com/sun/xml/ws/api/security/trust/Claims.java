/*
* $Id: Claims.java,v 1.1 2006-12-18 23:32:02 jdg6688 Exp $
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

package com.sun.xml.ws.api.security.trust;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author Jiandong Guo
 */
public interface Claims {
    /**
     * Gets the value of the any property.
     */
    List<Object> getAny();

    /**
     * Gets the value of the dialect property.
     * 
     * @return {@link String }
     *     
     */
    String getDialect();

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * @return
     *     always non-null
     */
    Map<QName, String> getOtherAttributes();

    /**
     * Sets the value of the dialect property.
     * 
     * @param value
     *     {@link String }
     *     
     */
    void setDialect(String value);  
}
