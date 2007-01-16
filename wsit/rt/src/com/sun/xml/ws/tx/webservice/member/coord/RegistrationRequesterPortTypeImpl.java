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

import com.sun.xml.ws.developer.MemberSubmissionAddressing;
import com.sun.xml.ws.developer.Stateful;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.coordinator.RegistrationManager;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.logging.Level;

/**
 * This class handles the registerResponce web service method
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
@MemberSubmissionAddressing
@Stateful
@WebService(serviceName = RegistrationRequesterPortTypeImpl.serviceName,
        portName = RegistrationRequesterPortTypeImpl.portName,
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor",
        wsdlLocation = "WEB-INF/wsdl/wscoor.wsdl")
public class RegistrationRequesterPortTypeImpl implements RegistrationRequesterPortType {

    public static final String serviceName = "Coordinator";
    public static final String portName = "RegistrationRequester";

    private static final TxLogger logger = TxLogger.getLogger(RegistrationRequesterPortTypeImpl.class);

    @Resource
    private WebServiceContext wsContext;

    /* stateful fields */
    public static StatefulWebServiceManager<RegistrationRequesterPortTypeImpl> manager;
    private String activityId;
    private String registrantId;

    public RegistrationRequesterPortTypeImpl() {
    }

    /**
     * Constructor for maintaining state
     *
     * @param activityId
     * @param registrantId
     */
    public RegistrationRequesterPortTypeImpl(String activityId, String registrantId) {
        this.activityId = activityId;
        this.registrantId = registrantId;
    }

    /**
     * Handle ws:coor &lt;RegisterResponse> messages.
     * <p/>
     * <pre>
     * &lt;RegisterResponse ...>
     *   &lt;CoordinatorProtocolService> ... &lt;/CoordinatorProtocolService>
     *   ...
     * &lt;/RegisterResponse>
     * </pre>
     *
     * @param parameters contains the &lt;registerResponse> message
     */
    public void registerResponseOperation(RegisterResponseType parameters) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("wscoor:registerResponse", parameters);
        }
        RegistrationManager.getInstance().registerResponse(wsContext, activityId, registrantId, parameters);
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("wscoor:registerResponse");
        }
    }
}
