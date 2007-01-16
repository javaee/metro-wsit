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
 * This class handles the synchronous register =
 *
 * @version $Revision: 1.2 $
 * @since 1.0
 */
@MemberSubmissionAddressing
@Stateful
@WebService(serviceName = "Coordinator",
        portName = "Registration",
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.coord.RegistrationPortTypeRPC",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor",
        wsdlLocation = "WEB-INF/wsdl/wscoor.wsdl")
public class RegistrationPortTypeImpl implements com.sun.xml.ws.tx.webservice.member.coord.RegistrationPortTypeRPC {
    private static final TxLogger logger = TxLogger.getLogger(RegistrationPortTypeImpl.class);

    @Resource
    private WebServiceContext wsContext;

    /* stateful fields */
    public static StatefulWebServiceManager<RegistrationPortTypeImpl> manager;

    String activityId;

    public RegistrationPortTypeImpl() {
    }

    public RegistrationPortTypeImpl(String activityId) {
        this.activityId = activityId;
    }

    public RegisterResponseType registerOperation(RegisterType parameters) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("wscoor:synchRegister", parameters);
        }
        RegisterResponseType registerResponse =
                RegistrationManager.synchronousRegister(wsContext, activityId, parameters);
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("wscoor:synchRegister", registerResponse);
        }
        return registerResponse;
    }

}
