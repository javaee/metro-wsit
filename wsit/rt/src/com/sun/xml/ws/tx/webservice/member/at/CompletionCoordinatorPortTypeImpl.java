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
 * @version $Revision: 1.1 $
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
