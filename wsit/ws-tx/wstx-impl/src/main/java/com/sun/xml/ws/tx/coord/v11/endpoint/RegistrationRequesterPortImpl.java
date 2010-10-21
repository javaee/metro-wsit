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

package com.sun.xml.ws.tx.coord.v11.endpoint;

import com.sun.xml.ws.tx.coord.common.endpoint.RegistrationRequester;
import com.sun.xml.ws.tx.coord.v11.XmlTypeAdapter;
import com.sun.xml.ws.tx.coord.v11.types.RegisterResponseType;
import com.sun.xml.ws.tx.coord.v11.types.RegistrationRequesterPortType;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

@WebService(portName = "RegistrationRequesterPort", serviceName = "RegistrationService_V11", targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", wsdlLocation = "/wsdls/wsc11/wstx-wscoor-1.1-wsdl-200702.wsdl", endpointInterface = "com.sun.xml.ws.tx.coord.v11.types.RegistrationRequesterPortType")
@BindingType("http://schemas.xmlsoap.org/wsdl/soap/http")
@Addressing
public class RegistrationRequesterPortImpl
    implements RegistrationRequesterPortType
{

    @javax.annotation.Resource
    private WebServiceContext m_context;

    public RegistrationRequesterPortImpl() {
    }

    /**
     *
     * @param parameters
     */
    public void registerResponse(RegisterResponseType parameters) {
        RegistrationRequester proxy = new RegistrationRequesterImpl(m_context);
        proxy.registerResponse(XmlTypeAdapter.adapt(parameters));
    }

}
