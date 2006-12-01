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
package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.developer.StatefulWebServiceManager;

import javax.xml.ws.EndpointReference;
import java.net.URI;

/**
 * This class ...
 *
 * @author Joe.Fialli@Sun.COM Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
public interface StatefulWebserviceFactory {

    /**
     * Get the StatefulWebServiceManager with the specified name and port
     *
     * @param serviceName service name
     * @param portName port name
     * @return StatefulWebserviceManager
     */
    public StatefulWebServiceManager getManager(String serviceName, String portName);

    /**
     * Create an EPR with the specified data.
     * 
     * @param serviceName service name
     * @param portName port name
     * @param address address URI
     * @param addressingVersion ws-addressing version
     * @param activityId activity id
     * @param registrantId registrant id
     * @return EndpointReference for the service
     */
    public EndpointReference createService(String serviceName,
                                           String portName,
                                           URI address,
                                           AddressingVersion addressingVersion,
                                           String activityId,
                                           String registrantId);
}
