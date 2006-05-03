/*
 * $Id: SecurityHeaderException.java,v 1.1 2006-05-03 22:57:38 arungupta Exp $
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
package com.sun.xml.wss.impl;

import com.sun.xml.wss.*;

/**
 * A SecurityHeaderException indicates that there is a problem with the
 * security header elements and subelements.
 * It indicates that there is an error in the input message to a MessageFilter.  
 * For example, a ds:keyInfo element may not contain a reference to a
 * security token. If such a reference is missing, then to
 * indicate this problem, an instance of this Exception would be thrown.
 * 
 * <p>
 * This is as opposed to a problem with processing the message itself.  An
 * example would be a MessageFilter that needs to look up data in a
 * database that is not currently available. A XWSSecurityException would
 * be thrown in the latter case.
 *
 * @author Edwin Goei
 * @author Manveen Kaur
 *
 */
public class SecurityHeaderException extends XWSSecurityException {
    public SecurityHeaderException(String message) {
        super(message);
    }

    public SecurityHeaderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SecurityHeaderException(Throwable cause) {
        super(cause);
    }
}
