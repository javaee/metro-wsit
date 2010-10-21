/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.tx.coord.v10.client;

import com.sun.xml.ws.tx.coord.v10.types.RegistrationCoordinatorPortType;
import com.sun.xml.ws.tx.coord.v10.types.RegistrationPortTypeRPC;
import com.sun.xml.ws.tx.coord.v10.types.RegistrationRequesterPortType;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import java.net.URL;




@WebServiceClient(name = "RegistrationService_V10", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wscoor", wsdlLocation = "wscoor.wsdl")
public class RegistrationServiceV10
    extends Service
{
    private static final String WSDL = "/META-INF/wstx/wsdls/wsc10/wscoor.wsdl";
    private final static URL REGISTRATIONSERVICEV10_WSDL_LOCATION = RegistrationServiceV10.class.getResource(WSDL);

    public RegistrationServiceV10(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RegistrationServiceV10() {
        super(REGISTRATIONSERVICEV10_WSDL_LOCATION, new QName("http://schemas.xmlsoap.org/ws/2004/10/wscoor", "Coordinator"));//todo paul was RegistrationService_V10
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RegistrationRequesterPortType
     */
    @WebEndpoint(name = "RegistrationRequesterPortTypePort")
    public RegistrationRequesterPortType getRegistrationRequesterPortTypePort(EndpointReference epr,WebServiceFeature... features) {
        return super.getPort(epr, RegistrationRequesterPortType.class, features);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RegistrationPortTypeRPC
     */
    @WebEndpoint(name = "RegistrationPortTypeRPCPort")
    public RegistrationPortTypeRPC getRegistrationPortTypeRPCPort(EndpointReference epr, WebServiceFeature... features) {
        return super.getPort(epr, RegistrationPortTypeRPC.class, features);
    }


    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RegistrationCoordinatorPortType
     */
    @WebEndpoint(name = "RegistrationCoordinatorPortTypePort")
    public RegistrationCoordinatorPortType getRegistrationCoordinatorPortTypePort(EndpointReference epr, WebServiceFeature... features) {
        return super.getPort(epr, RegistrationCoordinatorPortType.class, features);
    }

}
