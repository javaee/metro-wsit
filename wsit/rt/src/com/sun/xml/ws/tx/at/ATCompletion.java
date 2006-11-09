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
package com.sun.xml.ws.tx.at;

import com.sun.xml.ws.tx.common.Constants;
import com.sun.xml.ws.tx.coordinator.Coordinator;
import com.sun.xml.ws.tx.coordinator.Registrant;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterType;

import javax.xml.ws.EndpointReference;


/**
 * This class encapsulates a WS-AT completion registrant
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.1 $
 * @since 1.0
 */
public class ATCompletion extends Registrant {

    public ATCompletion(Coordinator parent, RegisterType registerRequest) {
        super(parent, registerRequest);

        final String protocolId = registerRequest.getProtocolIdentifier();
        assert(Constants.WSAT_COMPLETION_PROTOCOL.equals(protocolId));
    }

    public EndpointReference getParticipantProtocolService() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void commit() {
        // TODO send completion committed to Completion Intiator web service
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void rollback() {
        // TODO send completion aborted to Completion Initiator web service
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public EndpointReference getLocalParticipantProtocolService() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean expirationGuard() {
        throw new UnsupportedOperationException();
    }

}