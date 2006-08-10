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
package wsrm.session.server;

import com.sun.xml.ws.rm.jaxws.runtime.Session;
import com.sun.xml.ws.rm.jaxws.runtime.server.ServerSession;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;



@WebService(endpointInterface="wsrm.session.server.RMDemo")
@javax.xml.ws.BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class RMDemoImpl {


    /* JAX-WS initializes context for each request */
    @Resource
    private WebServiceContext context;

    /* Get Sesssion using well-known key in MessageContext */
    private Session getSession() {
        return (Session)context.getMessageContext()
                .get("com.sun.xml.ws.session");
    }

    /* Get String associated with SessionID for current request */

    private String getSessionData() {
        ServerSession session = (ServerSession)getSession();
        String ret = (String)session.getSessionData();
        return ret != null ? ret : "";

    }

    /* Store String associated with SessionID for current request */
    private void setSessionData(String data) {
        ServerSession session = (ServerSession)getSession();
        session.setSessionData(data);
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

