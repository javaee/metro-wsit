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
package com.sun.xml.ws.tx.webservice.member.at;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

/**
 * Remote transaction initiator notified of 2PC outcome via this protocol.
 * <p/>
 * Optional protocol not implemented yet.  Must be implemented if Coordination Activation service is supported.
 *
 * @author Joe.Fialli@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
@WebService(serviceName = "WSATCoordinator",
        portName = "CompletionInitiator",
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.at.CompletionInitiatorPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat",
        wsdlLocation = "WEB-INF/wsdl/wsat.wsdl")
public class CompletionInitiatorPortTypeImpl implements CompletionInitiatorPortType {

    @Resource
    private WebServiceContext wsContext;

    public void committedOperation(Notification parameters) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void abortedOperation(Notification parameters) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
