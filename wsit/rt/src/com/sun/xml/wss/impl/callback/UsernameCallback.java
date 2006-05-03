/*
 * $Id: UsernameCallback.java,v 1.1 2006-05-03 22:57:44 arungupta Exp $
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
package com.sun.xml.wss.impl.callback;

import com.sun.xml.wss.impl.MessageConstants;
import javax.security.auth.callback.Callback;

/**
 * This Callback should be handled if the username for the username token
 * needs to be supplied at run-time.
 *
 * @author XWS-Security Team
 */
public class UsernameCallback extends XWSSCallback implements Callback {

    private String username;

    /**
     * Set the Username.
     *
     * @param username <code>java.lang.String</code> representing the Username.
     */
    public void setUsername(String username) {
        if ( username == null || MessageConstants._EMPTY.equals(username) ) {
            throw new RuntimeException("Username can not be empty or NULL");
        }
        this.username = username;
    }

    /** 
     * Get the Username.
     *
     * @return <code>java.lang.String</code> representing the Username.
     */
    public String getUsername() {
        return username;
    }
}
