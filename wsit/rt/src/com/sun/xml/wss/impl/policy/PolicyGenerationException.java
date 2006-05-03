/*
 * $Id: PolicyGenerationException.java,v 1.1 2006-05-03 22:57:53 arungupta Exp $
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

package com.sun.xml.wss.impl.policy;

/**
 * Thrown by the classes implementing the Policy framework
 */
public class PolicyGenerationException extends com.sun.xml.wss.XWSSecurityException {
    
    /**
     * Constructs an Exception specifying a message
     * @param message  the exception string
     */
    public PolicyGenerationException (String message) {
        super (message);
    }
    
    /**
     * Constructs an Exception with a nested exception and specifying a message
     * @param message the exception string
     * @param cause the original cause
     */
    public PolicyGenerationException (String message, Throwable cause) {
        super (message, cause);
    }
    
    /**
     * An Exception wrapper around another exception
     * @param cause the original cause
     */
    public PolicyGenerationException (Throwable cause) {
        super (cause);
    }
}

