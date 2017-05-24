/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import java.util.Iterator;



/**
 * This interface identifies targets in the message that need to be integrity protected. The targets if present should be integrity protected.
 * <p>
 *  <B>Syntax:
 * <p>
 *  <pre>
 *   &lt;xmp&gt;
 *       &lt;sp:SignedParts ... &gt;
 *            &lt;sp:Body /&gt;?
 *            &lt;sp:Header Name="xs:NCName"? Namespace="xs:anyURI" ... /&gt;*
 *                  ...
 *       &lt;/sp:SignedParts&gt;
 *   &lt;/xmp&gt;
 * </pre>
 *
 * @author K.Venugopal@sun.com
 */


public interface SignedParts extends Target {
 
    /**
     *
     * @return true if the body is to be integrity protected.
     */
    public boolean hasBody();
    
    /**
     * 
     *  @return true if the attachments are to be integrity protected
     */
    public boolean hasAttachments();
    
    /**
     * 
     * @return the URI of transform to be applied on attachment, the default is
     *  Attachment-Complete-Transform
     */
    public String attachmentProtectionType();
    
    /**
     * {@link java.util.Iterator } over list of Headers that identify targets in the SOAP header
     * to be integrity protected.
     * @return {@link java.util.Iterator }
     */
    public Iterator  getHeaders();  
    
}
