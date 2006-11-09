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
package com.sun.xml.ws.tx.webservice.member.coord;

import com.sun.xml.ws.tx.common.TxLogger;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.logging.Level;

/**
 * This class handles the createCoordinationContextResponse web service method.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
@WebService(serviceName = "Coordinator",
        portName = "ActivationRequester",
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.coord.ActivationRequesterPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor",
        wsdlLocation = "WEB-INF/wsdl/wscoor.wsdl")
public class ActivationRequesterPortTypeImpl implements ActivationRequesterPortType {
    private static final TxLogger logger = TxLogger.getLogger(ActivationRequesterPortTypeImpl.class);

    @Resource
    private WebServiceContext wsContext;

    /**
     * Handle ws:coor &lt;CreateCoordinationContextResponse> messages.
     * <p/>
     * <pre>
     * &lt;CreateCoordinationContextResponse>
     *   &lt;CoordinationContext> ... &lt;/CoordinationContext>
     *   ...
     * &lt;/CreateCoordinationContextResponse>
     * </pre>
     *
     * @param parameters contains the &lt;createCoordinationContextResponse> message
     */
    public void createCoordinationContextResponseOperation(CreateCoordinationContextResponseType parameters) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("createCoordinationContextResponseOperation", parameters);
            logger.exiting("createCoordinationContextResponseOperation");
        }
        throw new UnsupportedOperationException("not implemented yet");
    }
}
