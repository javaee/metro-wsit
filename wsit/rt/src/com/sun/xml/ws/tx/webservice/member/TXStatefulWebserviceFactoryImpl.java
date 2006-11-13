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
package com.sun.xml.ws.tx.webservice.member;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import static com.sun.xml.ws.tx.common.Constants.UNKNOWN_ID;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactory;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.at.ParticipantPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationCoordinatorPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortTypeImpl;

import javax.xml.ws.EndpointReference;
import java.net.URI;

/**
 * This class ...
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
// suppress known deprecation warnings about using short term workaround StatefulWebService.export(Class, String webServiceEndpoint, PortType)
@SuppressWarnings("deprecation")
final public class TXStatefulWebserviceFactoryImpl implements StatefulWebserviceFactory {

    private static final TxLogger logger = TxLogger.getLogger(TXStatefulWebserviceFactoryImpl.class);


    public StatefulWebServiceManager getManager(String serviceName, String portName) {
        if (serviceName.equals(ParticipantPortTypeImpl.serviceName)) {
            // ParticipantPortTypeImpl && CoordinatorPortTypeImpl have the same serviceName
            if (portName.equals(ParticipantPortTypeImpl.portName)) {
                return ParticipantPortTypeImpl.manager;
            } else if (portName.equals(CoordinatorPortTypeImpl.portName)) {
                return CoordinatorPortTypeImpl.manager;
            } else {
                throw new IllegalStateException("Unable to resolve port '" + portName + "' for service '" + serviceName + "'");
            }
        } else if (serviceName.equals(RegistrationRequesterPortTypeImpl.serviceName)) {
            if (portName.equals(RegistrationRequesterPortTypeImpl.portName)) {
                return RegistrationRequesterPortTypeImpl.manager;
            } else if (portName.equals(RegistrationCoordinatorPortTypeImpl.portName)) {
                return RegistrationCoordinatorPortTypeImpl.manager;
            } else {
                throw new IllegalStateException("Unable to resolve port '" + portName + "' for service '" + serviceName + "'");
            }
        } else {
            throw new IllegalStateException("Unable to resolve service '" + serviceName + "'");
        }
    }

    public EndpointReference createService(String serviceName, String portName,
                                           URI address, AddressingVersion addressingVersion,
                                           String activityId, String registrantId) {
        registerFallback();
        if (serviceName.equals(ParticipantPortTypeImpl.serviceName)) {
            // ParticipantPortTypeImpl && CoordinatorPortTypeImpl have the same serviceName
            if (portName.equals(ParticipantPortTypeImpl.portName)) {
                ParticipantPortTypeImpl participant =
                        new ParticipantPortTypeImpl(activityId, registrantId);
                return ParticipantPortTypeImpl.manager.
                        export(addressingVersion.eprType.eprClass, address.toString(), participant);
            } else if (portName.equals(CoordinatorPortTypeImpl.portName)) {
                CoordinatorPortTypeImpl coordinator =
                        new CoordinatorPortTypeImpl(activityId, registrantId);
                return CoordinatorPortTypeImpl.manager.
                        export(addressingVersion.eprType.eprClass, address.toString(), coordinator);
            } else {
                throw new IllegalStateException("Unable to resolve port '" + portName + "' for service '" + serviceName + "'");
            }
        } else if (serviceName.equals(RegistrationRequesterPortTypeImpl.serviceName)) {
            if (portName.equals(RegistrationRequesterPortTypeImpl.portName)) {
                RegistrationRequesterPortTypeImpl registrationRequester =
                        new RegistrationRequesterPortTypeImpl(activityId, registrantId);
                return RegistrationRequesterPortTypeImpl.manager.
                        export(addressingVersion.eprType.eprClass, address.toString(), registrationRequester);
            } else if (portName.equals(RegistrationCoordinatorPortTypeImpl.portName)) {
                RegistrationCoordinatorPortTypeImpl registrationCoordinator =
                        new RegistrationCoordinatorPortTypeImpl(activityId);
                return RegistrationCoordinatorPortTypeImpl.manager.
                        export(addressingVersion.eprType.eprClass, address.toString(), registrationCoordinator);
            } else {
                throw new IllegalStateException("Unable to resolve port '" + portName + "' for service '" + serviceName + "'");
            }
        } else {
            throw new IllegalStateException("Unable to resolve service '" + serviceName + "'");
        }
    }

    static private boolean registeredFallback = false;
    /**
     * Instances that handle request for unknown StatefulWebService instance.
     * This can happen when a request for a StatefulWebService is received after the instance has
     * timed out or was explicitly unexported.
     */
    private void registerFallback() {
        if (!registeredFallback ) {
            registeredFallback = true;
            ParticipantPortTypeImpl participant =
                    new ParticipantPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
            ParticipantPortTypeImpl.manager.setFallbackInstance(participant);
            
            CoordinatorPortTypeImpl coordinator =
                    new CoordinatorPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
            CoordinatorPortTypeImpl.manager.setFallbackInstance(coordinator);
            
            RegistrationRequesterPortTypeImpl registrationRequester =
                    new RegistrationRequesterPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
            RegistrationRequesterPortTypeImpl.manager.setFallbackInstance(registrationRequester);
            
            RegistrationCoordinatorPortTypeImpl registrationCoordinator =
                    new RegistrationCoordinatorPortTypeImpl(UNKNOWN_ID);
            RegistrationCoordinatorPortTypeImpl.manager.setFallbackInstance(registrationCoordinator);
        }
    }
}
