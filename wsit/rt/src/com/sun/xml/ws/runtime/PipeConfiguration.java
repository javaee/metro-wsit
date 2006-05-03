package com.sun.xml.ws.runtime;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipelineAssembler;
import com.sun.xml.ws.policy.PolicyMap;

import javax.xml.ws.Dispatch;

/**
 * Entry point to the various configuration information
 * necessary for constructing {@link Pipe}s.
 *
 * <p>
 * This object is created by a {@link PipelineAssembler} and
 * passed as a constructor parameter to most pipes,
 * so that they can access configuration information.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class PipeConfiguration {
    private final PolicyMap policy;
    private final WSDLPort wsdlModel;

    PipeConfiguration(PolicyMap policy, WSDLPort wsdlModel) {
        this.policy = policy;
        this.wsdlModel = wsdlModel;
    }

    /**
     * Gets the {@link PolicyMap} that represents
     * the policy information applicable to the current pipeline.
     *
     * @return always non-null same object.
     */
    public PolicyMap getPolicyMap() {
        return policy;
    }

    /**
     * Gets the {@link WSDLPort} that represents
     * the WSDL information about the port for which
     * a pipeline is created.
     *
     * <p>
     * This model is present only when the client
     * provided WSDL to the JAX-WS runtime in some means
     * (such as indirectly through SEI or through {@link Dispatch}.)
     *
     * <p>
     * JAX-WS allows modes of operations where no WSDL
     * is available for the current pipeline, and in which
     * case this model is not present.
     *
     * @return null if this model is not present.
     *         If non-null, it's always the same object.
     */
    public WSDLPort getWSDLModel() {
        return wsdlModel;
    }

    /**
     * Gets the applicable {@link WSBinding} for this pipeline.
     *
     * @return always non-null.
     */
    public abstract WSBinding getBinding();
}
