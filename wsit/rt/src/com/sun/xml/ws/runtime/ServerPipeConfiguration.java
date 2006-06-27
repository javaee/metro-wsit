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

package com.sun.xml.ws.runtime;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.policy.PolicyMap;

/**
 * {@link PipeConfiguration} for servers.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ServerPipeConfiguration extends PipeConfiguration {
    private final WSEndpoint endpoint;

    public ServerPipeConfiguration(PolicyMap policy, WSDLPort wsdlModel, WSEndpoint endpoint) {
        super(policy, wsdlModel);
        this.endpoint = endpoint;
    }

    /**
     * Gets the {@link WSEndpoint} for which the pipeline is being created.
     *
     * <p>
     * {@link WSEndpoint} provides information about the surrounding environment,
     * such as access to the application server.
     *
     * @return always non-null same object.
     */
    public WSEndpoint getEndpoint() {
        return endpoint;
    }

    public WSBinding getBinding() {
        return endpoint.getBinding();
    }
}
