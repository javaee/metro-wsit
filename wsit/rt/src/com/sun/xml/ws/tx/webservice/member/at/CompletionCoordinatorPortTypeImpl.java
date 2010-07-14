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
package com.sun.xml.ws.tx.webservice.member.at;

import com.sun.xml.ws.tx.at.ATCompletion;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

/**
 * WS-AT Coordinator notified of remote transaction initiator's desire to commit or rollback.
 * <p/>
 * <p/>
 * Optional protocol not yet implemented. Must be implemented if Coordination Activation service is supported.
 * Typically, transaction initiator process is co-located locally with ws-at coordinator and
 * this is achieved by local private interactions, not the standardized protocol.
 *
 * @author Joe.Fialli@Sun.COM
 * @version $Revision: 1.3.22.2 $
 * @since 1.0
 */
@WebService(serviceName = "WSATCoordinator",
        portName = "CompletionCoordinator",
        endpointInterface = "com.sun.xml.ws.tx.webservice.member.at.CompletionCoordinatorPortType",
        targetNamespace = "http://schemas.xmlsoap.org/ws/2004/10/wsat",
        wsdlLocation = "WEB-INF/wsdl/wsat.wsdl")
public class CompletionCoordinatorPortTypeImpl implements CompletionCoordinatorPortType {

    @Resource
    private WebServiceContext wsContext;

    private ATCompletion getATCompletion() {
        /*
         * TODO: fix this to get inbound 2004 WS-A Reference Parameters from message context property (added by
         *       TxRefParamHandler.
        AddressingProperties ap = getWSAProperties();
        assert(ap != null);
        // TODO: get activity Id and registrant id from wsContext.
        //       lookup in ATCoordinator.getRegistrant
        return null;
         */
        throw new UnsupportedOperationException("not implemented yet");
    }

    public void commitOperation(Notification parameters) {
        throw new UnsupportedOperationException("Not yet implemented");
        /*
        ATCompletion completeCoord = getATCompletion();
        if (completeCoord != null ) {
            completeCoord.commit();
        } else {
            // Check if wsa:replyTo used when completeCoord not found.
            // TODO: send committed to wsa:replyTo   
        }
         */
    }

    public void rollbackOperation(Notification parameters) {
        throw new UnsupportedOperationException("Not yet implemented");
        /*
        ATCompletion completeCoord = getATCompletion();
        if (completeCoord != null ) {
            completeCoord.rollback();
        } else {
            // Check if wsa:replyTo used when completeCoord not found.
            // TODO: send committed to wsa:replyTo   
        }  
        */
    }
}
