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
package com.sun.xml.ws.tx.coordinator;

import com.sun.istack.NotNull;

import javax.xml.ws.EndpointReference;

/**
 * Abstract base class for implementations of {@link CoordinationContextInterface}
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
public abstract class CoordinationContextBase implements CoordinationContextInterface {
    EndpointReference rootRegistrationService = null;

    public void setRootCoordinatorRegistrationService(@NotNull EndpointReference rootRegistrationService) {
        this.rootRegistrationService = rootRegistrationService;
    }

    @NotNull
    public EndpointReference getRootRegistrationService() {
        return rootRegistrationService;
    }

    @NotNull
    public static CoordinationContextInterface createCoordinationContext(@NotNull Object cc) {
        if (cc instanceof com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext) {
            return new CoordinationContext200410((com.sun.xml.ws.tx.webservice.member.coord.CoordinationContext) cc);
        } else {
            throw new UnsupportedOperationException("CoordinationContextBase:createCoordinatorContext(): instance of unsupported class:" +
                    cc.getClass().getName());
        }
    }
}
