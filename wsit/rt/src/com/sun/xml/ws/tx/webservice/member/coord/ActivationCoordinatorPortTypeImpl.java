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
import com.sun.xml.ws.tx.coordinator.CoordinationManager;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.logging.Level;

/**
 * This class handles the createCoordinationContext web service request
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
@WebService(serviceName = "Coordinator",
        portName = "ActivationCoordinator",
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.coord.ActivationCoordinatorPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor",
        wsdlLocation = "WEB-INF/wsdl/wscoor.wsdl")
public class ActivationCoordinatorPortTypeImpl implements ActivationCoordinatorPortType {
    private static final TxLogger logger = TxLogger.getLogger(ActivationCoordinatorPortTypeImpl.class);

    @Resource
    private WebServiceContext wsContext;

    /**
     * Handle &lt;createCoordinationContext> message.
     * <p/>
     * This method will process the request and construct a new
     * {@link com.sun.xml.ws.tx.coordinator.Coordinator} to cache it in the
     * {@link com.sun.xml.ws.tx.coordinator.CoordinationManager}.
     * <p/>
     * <p/>
     * This method will only be used for interop with other coordinators.  Java clients
     * will typically contact their local coordinator via more performant paths.
     * <p/>
     * <pre>
     * &lt;CreateCoordinationContext ...>
     *   &lt;Expires> ... &lt;/Expires>?
     *   &lt;CurrentContext> ... &lt;/CurrentContext>?
     *   &lt;CoordinationType> ... &lt;/CoordinationType>
     *   ...
     * &lt;/CreateCoordinationContext>
     * </pre>
     *
     * @param parameters contains the &lt;CreateCoordinationContext> message
     */
    public void createCoordinationContextOperation(CreateCoordinationContextType parameters) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("wscoor:createCoordinationContext", parameters);
        }
        CoordinationManager coordMgr = CoordinationManager.getInstance();
        coordMgr.lookupOrCreateCoordinator(parameters);
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("wscoor:createCoordinationContext");
        }
    }
}
