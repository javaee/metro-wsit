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
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public interface StatefulWebserviceFactory {

    /**
     * @param serviceName
     * @param portName
     * @return StatefulWebserviceManager
     */
    public StatefulWebServiceManager getManager(String serviceName, String portName);

    /**
     * @param serviceName
     * @param portName
     * @param address
     * @param addressingVersion
     * @param activityId
     * @param registrantId
     * @return EndpointReference
     */
    public EndpointReference createService(String serviceName,
                                           String portName,
                                           URI address,
                                           AddressingVersion addressingVersion,
                                           String activityId,
                                           String registrantId);
}
