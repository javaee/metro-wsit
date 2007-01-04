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

/**
 * Identifies targets that if present in the message should be confidentiality protected.
 *<p>
 *  <pre><xmp>
 *      <sp:EncryptedParts ... >
 *          <sp:Body/>?
 *          <sp:Header Name="xs:NCName"? Namespace="xs:anyURI" ... />*
 *              ...
 *      </sp:EncryptedParts>
 *</xmp></pre>
 * @author K.Venugopal@sun.com
 */
public interface EncryptedParts extends Target {
 
    /**
     *
     * @return true if the body is part of the target list.
     */
    public boolean hasBody();
   
    /**
     * returns list of SOAP Headers that need to protected.
     * @return {@link java.util.Iterator} over the list of SOAP Headers
     */
    public Iterator getTargets();
    

    /**
     * removes SOAP Body from the list of targets to be confidentiality protected.
     */
    public void removeBody();
}
