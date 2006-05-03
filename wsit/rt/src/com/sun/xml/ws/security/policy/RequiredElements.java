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
 * Used to specify the header elements that the message MUST contain.
 * <p>
 * <pre><xmp>
 *      <sp:RequiredElements XPathVersion="xs:anyURI"? ... > 
 *          <sp:XPath>xs:string</sp:XPath>+ 
 *              ...
 *      </sp:RequiredElements>
 *</xmp></pre>
 * @author mayank.mishra@sun.com
 */
public interface RequiredElements extends Target {
    
    /**
     * Returns XPath Version in use.
     * @return xpath version 
     */
    public String getXPathVersion();
    
     /**
     * targets header elements message must contains .
     * @return {@link java.util.Iterator }
     */
    public Iterator getTargets();
    
}
