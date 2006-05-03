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

/*
 * RMSecurityException.java
 *
 * @author Mike Grogan
 * Created on February 14, 2006, 10:31 AM
 *
 */

package com.sun.xml.ws.rm;

/**
     * Subclass of RMException thrown when an incorrect STR is
     * used to secure an inbound message.
     */
public class RMSecurityException extends RMException {
    
    
    public RMSecurityException()  {
        super();
    }
    
    public RMSecurityException(String s) {
        super(s);
    }
    
    public RMSecurityException(Throwable e) {
        super(e);
    }
     
}
