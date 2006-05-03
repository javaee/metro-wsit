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
