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
 * ClientSession.java
 *
 * @author Mike Grogan
 * Created on February 26, 2006, 4:35 PM
 *
 */

package com.sun.xml.ws.rm.jaxws.runtime.client;
import com.sun.xml.ws.rm.jaxws.runtime.Session;
import javax.xml.ws.BindingProvider;

/**
 * Client side implementation of Session, set in BindingProvider property.
 * In addition to SessionID, ability to Terminate the underlying 
 * ClientOutboundSequence is exposed in the close method.
 */
public class ClientSession extends Session{
    
    private RMClientPipe pipe;
    /**
     */
    public ClientSession(String id, RMClientPipe pipe) {
        super(id);
        this.pipe = pipe;
    }
    
    public void close() {
        if (pipe != null) {
            pipe.preDestroy();
        }
        pipe = null;
    }
    
    public static ClientSession getSession(BindingProvider proxy) {
        return (ClientSession)proxy.getRequestContext().get("rmsession");
    }
    
    public static void setSession(BindingProvider proxy, ClientSession session) {
        proxy.getRequestContext().put("rmsession", session);
    }
    
}
