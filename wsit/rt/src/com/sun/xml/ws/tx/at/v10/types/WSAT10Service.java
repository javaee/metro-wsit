/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
*
* The contents of this file are subject to the terms of either the GNU
* General Public License Version 2 only ("GPL") or the Common Development
* and Distribution License("CDDL") (collectively, the "License").  You
* may not use this file except in compliance with the License. You can obtain
* a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
* or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
* language governing permissions and limitations under the License.
*
* When distributing the software, include this License Header Notice in each
* file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
* Sun designates this particular file as subject to the "Classpath" exception
* as provided by Sun in the GPL Version 2 section of the License file that
* accompanied this code.  If applicable, add the following below the License
* Header, with the fields enclosed by brackets [] replaced by your own
* identifying information: "Portions Copyrighted [year]
* [name of copyright owner]"
*
* Contributor(s):
*
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
package com.sun.xml.ws.tx.at.v10.types;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

@WebServiceClient(name = "WSAT10Service", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat", wsdlLocation = "file:/scratch/pparkins/dev/src1034//modules/wsee/src//wsee/wstx/WEB-INF/wsdls/wsat10/wsat.wsdl")
public class WSAT10Service
    extends Service
{

    private final static URL WSAT10SERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(com.sun.xml.ws.tx.at.v10.types.WSAT10Service.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.sun.xml.ws.tx.at.v10.types.WSAT10Service.class.getResource(".");
            url = new URL(baseUrl, "file:/scratch/pparkins/dev/src1034//modules/wsee/src//wsee/wstx/WEB-INF/wsdls/wsat10/wsat.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/scratch/pparkins/dev/src1034//modules/wsee/src//wsee/wstx/WEB-INF/wsdls/wsat10/wsat.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        WSAT10SERVICE_WSDL_LOCATION = url;
    }

    public WSAT10Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WSAT10Service() {
        super(WSAT10SERVICE_WSDL_LOCATION, new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "WSAT10Service"));
    }

    /**
     * 
     * @return
     *     returns CoordinatorPortType
     */
    @WebEndpoint(name = "CoordinatorPortTypePort")
    public CoordinatorPortType getCoordinatorPortTypePort() {
        return super.getPort(new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "CoordinatorPortTypePort"), CoordinatorPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CoordinatorPortType
     */
    @WebEndpoint(name = "CoordinatorPortTypePort")
    public CoordinatorPortType getCoordinatorPortTypePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "CoordinatorPortTypePort"), CoordinatorPortType.class, features);
    }

    /**
     * 
     * @return
     *     returns ParticipantPortType
     */
    @WebEndpoint(name = "ParticipantPortTypePort")
    public ParticipantPortType getParticipantPortTypePort() {
        return super.getPort(new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "ParticipantPortTypePort"), ParticipantPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ParticipantPortType
     */
    @WebEndpoint(name = "ParticipantPortTypePort")
    public ParticipantPortType getParticipantPortTypePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://schemas.xmlsoap.org/ws/2004/10/wsat", "ParticipantPortTypePort"), ParticipantPortType.class, features);
    }

}
