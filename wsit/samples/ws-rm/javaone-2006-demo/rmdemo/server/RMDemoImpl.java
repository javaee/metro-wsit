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
package rmdemo.server;

import com.sun.xml.ws.runtime.util.SessionManager;
import com.sun.xml.ws.rm.jaxws.runtime.Session;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.HashMap;
import java.util.Hashtable;


@WebService(endpointInterface="rmdemo.server.RMDemo")
public class RMDemoImpl {

    /* Store a String for each session */
    private final SessionManager sessionManager = SessionManager.getSessionManager();

    /* JAX-WS initializes context for each request */
    @Resource
    private WebServiceContext context;

    /* Get SesssionId using well-known key in MessageContext */
    private String getSessionId() {
        return (String)context.getMessageContext()
                .get("com.sun.xml.ws.sessionid");

    }

    /* Get String associated with SessionID for current request */

    private String getSessionData() {
        String id = getSessionId();
        Hashtable userData = (Hashtable)sessionManager.getSession(id).getUserData();
       String ret = (String)userData.get(id);
        return ret != null ? ret : "";

    }

    /* Store String associated with SessionID for current request */
    private void setSessionData(String data) {
        String id = getSessionId();
        com.sun.xml.ws.runtime.util.Session session = sessionManager.getSession(id);
        Hashtable table = (Hashtable)session.getUserData()   ;
        table.put(id,data);
     
    }

    /* RMDemo Methods */

    @WebMethod
    public void addString(String s ) {
        /* append string to session data */
        setSessionData(getSessionData() + " " + s);
    }



    @WebMethod
    public String getResult() {
        /* return session data */
        return getSessionData();
    }

}

