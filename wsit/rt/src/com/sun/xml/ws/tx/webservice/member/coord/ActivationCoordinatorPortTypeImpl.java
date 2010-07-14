/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * @version $Revision: 1.3.22.2 $
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
