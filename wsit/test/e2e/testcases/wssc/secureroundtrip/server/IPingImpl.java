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
package wssc.secureroundtrip.server;
import java.util.Hashtable;
import javax.xml.bind.JAXBElement;

import javax.xml.bind.*;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.Hashtable;

@WebService(endpointInterface="wssc.secureroundtrip.server.IPingService")
public class IPingImpl {

    /* JAX-WS initializes context for each request */
    @Resource
    private WebServiceContext context;

    /* Get Sesssion using well-known key in MessageContext */
    private Hashtable getSession() {
        return (Hashtable)context.getMessageContext()
                .get("com.sun.xml.ws.session");
    }

    /* Get String associated with SessionID for current request */

    private String getSessionData() {
	Hashtable sess = getSession();
        String ret = (String)sess.get("request_record");
        return ret != null ? ret : "";

    }

    /* Store String associated with SessionID for current request */
    private void setSessionData(String data) {
        Hashtable session = getSession();
        session.put("request_record", data);
    }

    /**
     * @param String
     */
    public String echo(String message) {
        /* append string to session data */
        setSessionData(getSessionData() + " " + message);
        System.out.println("The message is here : " + message);
        return getSessionData();
    }
}
