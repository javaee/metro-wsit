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
 * ServerSession.java
 *
 * @author Mike Grogan
 * Created on February 26, 2006, 4:43 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.server;
import com.sun.xml.ws.rm.jaxws.runtime.Session;
/**
 *
 */
public class ServerSession extends Session {

    /**
     * Property bag exposed to user of session.
     */
    private Object sessionData;
    
    /**
     */
    public ServerSession(String id,Object d) {
        super(id);
        this.sessionData = d;
    }

    public ServerSession (String id) {
        super(id);
        this.sessionData = null;
    }
    
    public  Object getSessionData() {
        return this.sessionData;
    }

    public void setSessionData(Object data) {
        this.sessionData = data;
    }

    
    
}
