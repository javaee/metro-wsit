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

package com.sun.xml.ws.tx.at.v11.types;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "WSAT11Service", targetNamespace = "http://docs.oasis-open.org/ws-tx/wsat/2006/06", wsdlLocation = "file:/scratch/pparkins/dev/src1034//modules/wsee/src//wsee/wstx/WEB-INF/wsdls/wsat11/wstx-wsat-1.1-wsdl-200702.wsdl")
public class WSAT11Service
    extends Service
{

    private final static URL WSAT11SERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(com.sun.xml.ws.tx.at.v11.types.WSAT11Service.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.sun.xml.ws.tx.at.v11.types.WSAT11Service.class.getResource(".");
            url = new URL(baseUrl, "file:/scratch/pparkins/dev/src1034//modules/wsee/src//wsee/wstx/WEB-INF/wsdls/wsat11/wstx-wsat-1.1-wsdl-200702.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/scratch/pparkins/dev/src1034//modules/wsee/src//wsee/wstx/WEB-INF/wsdls/wsat11/wstx-wsat-1.1-wsdl-200702.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        WSAT11SERVICE_WSDL_LOCATION = url;
    }

    public WSAT11Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WSAT11Service() {
        super(WSAT11SERVICE_WSDL_LOCATION, new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "WSAT11Service"));
    }

    /**
     * 
     * @return
     *     returns CoordinatorPortType
     */
    @WebEndpoint(name = "CoordinatorPort")
    public CoordinatorPortType getCoordinatorPort() {
        return super.getPort(new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "CoordinatorPort"), CoordinatorPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CoordinatorPortType
     */
    @WebEndpoint(name = "CoordinatorPort")
    public CoordinatorPortType getCoordinatorPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "CoordinatorPort"), CoordinatorPortType.class, features);
    }

    /**
     * 
     * @return
     *     returns ParticipantPortType
     */
    @WebEndpoint(name = "ParticipantPort")
    public ParticipantPortType getParticipantPort() {
        return super.getPort(new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "ParticipantPort"), ParticipantPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ParticipantPortType
     */
    @WebEndpoint(name = "ParticipantPort")
    public ParticipantPortType getParticipantPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "ParticipantPort"), ParticipantPortType.class, features);
    }

}
