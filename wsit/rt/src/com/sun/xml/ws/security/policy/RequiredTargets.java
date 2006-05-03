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
 * Specifies list of SOAP Headers that must be present in the SOAP Messages.
 * @author K.Venugopal@sun.com
 */
public interface RequiredTargets extends Target{
    
   
    /**
     * {@link java.util.Iterator } over the list of required targetlist.
     * @return {@link java.util.Iterator }
     */
    public Iterator getTargets();
 
    
    /**
     * returns the XPath Version that is being used.
     * @return XPath Version
     */
    public String getXPathExpression();
}
