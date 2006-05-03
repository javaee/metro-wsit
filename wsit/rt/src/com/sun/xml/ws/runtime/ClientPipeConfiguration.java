package com.sun.xml.ws.runtime;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.PolicyMap;

/**
 * {@link PipeConfiguration} for client.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ClientPipeConfiguration extends PipeConfiguration {
    private final WSService service;
    private final WSBinding binding;

    public ClientPipeConfiguration(PolicyMap policy, WSDLPort wsdlModel, WSService service, WSBinding binding) {
        super(policy, wsdlModel);
        this.service = service;
        this.binding = binding;
    }

    /**
     * Returns the {@link WSService} object that owns the port,
     * which in turn owns the pipeline to be created.
     *
     * <p>
     * {@link WSService} provides an access to various service-level
     * information.
     *
     * @return always non-null.
     */
    public WSService getService() {
        return service;
    }

    public WSBinding getBinding() {
        return binding;
    }
}
