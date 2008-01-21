/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.ws.assembler;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.policy.jaxws.client.PolicyFeature;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;

/**
 * The context is a wrapper around the existing JAX-WS {@link ClientTubeAssemblerContext} with additional features
 * 
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class WsitClientTubeAssemblyContext extends WsitTubeAssemblyContext {

    private final @NotNull ClientTubeAssemblerContext wrappedContext;
    private final PolicyMap policyMap;
    private final WSPortInfo portInfo; // TODO: is this really needed?
    private final WSDLPort wsdlPort;
    private SecureConversationInitiator scInitiator;
    // TODO: add next tube getter/package-private setter
    // TODO: replace the PipeConfiguration

    public WsitClientTubeAssemblyContext(@NotNull ClientTubeAssemblerContext context) {
        this.wrappedContext = context;

        WSDLPort _port = context.getWsdlModel();
        WSPortInfo _portInfo = null;
        PolicyMap _policyMap = null;
        if (_port != null) {
            // Usually, the WSDL model holds the server and client policy maps merged into one
            WSDLPolicyMapWrapper mapWrapper = _port.getBinding().getOwner().getExtension(WSDLPolicyMapWrapper.class);
            if (mapWrapper != null) {
                _policyMap = mapWrapper.getPolicyMap();
            }
        } else { 
            // In dispatch mode, wsdlPort is null and we don't have a server policy map, so we read the
            // client policy map only
            PolicyFeature feature = context.getBinding().getFeature(PolicyFeature.class);
            if (feature != null) {
                _policyMap = feature.getPolicyMap();
                _portInfo = feature.getPortInfo();

                // Dispatch client: Extract WSDLPort from client config (if we have one).
                if (_portInfo != null && feature.getWsdlModel() != null) {
                    WSDLService service = feature.getWsdlModel().getService(_portInfo.getServiceName());
                    if (service != null) {
                        _port = service.get(_portInfo.getPortName());
                    }
                }
            }
        }        
        this.wsdlPort = _port;
        this.portInfo = _portInfo;
        this.policyMap = _policyMap;
    }

    public PolicyMap getPolicyMap() {
        return policyMap;
    }
    
    public boolean isPolicyAvailable() {
        return policyMap != null && !policyMap.isEmpty();
    }

    /**
     * The created pipeline will be used to serve this port.
     * Null if the service isn't associated with any port definition in WSDL,
     * and otherwise non-null.
     * 
     * Replaces {@link ClientTubeAssemblerContext#getWSDLModel()}
     */
    public WSDLPort getWsdlPort() {
        return wsdlPort;
    }

    public WSPortInfo getPortInfo() {
        return portInfo;
    }
    
    /**
     * The endpoint address. Always non-null. This parameter is taken separately
     * from {@link com.sun.xml.ws.api.model.wsdl.WSDLPort} (even though there's {@link com.sun.xml.ws.api.model.wsdl.WSDLPort#getAddress()})
     * because sometimes WSDL is not available.
     */
    public @NotNull EndpointAddress getAddress() {
        return wrappedContext.getAddress();
    }

    /**
     * The pipeline is created for this {@link com.sun.xml.ws.api.WSService}.
     * Always non-null. (To be precise, the newly created pipeline
     * is owned by a proxy or a dispatch created from this {@link com.sun.xml.ws.api.WSService}.)
     */
    public @NotNull WSService getService() {
        return wrappedContext.getService();
    }

    /**
     * The binding of the new pipeline to be created.
     */
    public @NotNull WSBinding getBinding() {
        return wrappedContext.getBinding();
    }

    /**
     * The created pipeline will use seiModel to get java concepts for the endpoint
     *
     * @return Null if the service doesn't have SEI model e.g. Dispatch,
     *         and otherwise non-null.
     */
    public @Nullable SEIModel getSEIModel() {
        return wrappedContext.getSEIModel();
    }

    /**
     * Returns the Container in which the client is running
     *
     * @return Container in which client is running
     */
    public Container getContainer() {
        return wrappedContext.getContainer();
    }       

    /**
     * Gets the {@link Codec} that is set by {@link #setCodec} or the default codec
     * based on the binding.
     *
     * @return codec to be used for web service requests
     */
    public @NotNull Codec getCodec() {
        return wrappedContext.getCodec();
    }

    /**
     * Interception point to change {@link Codec} during {@link Tube}line assembly. The
     * new codec will be used by jax-ws client runtime for encoding/decoding web service
     * request/response messages. The new codec should be used by the transport tubes.
     *
     * <p>
     * the codec should correctly implement {@link Codec#copy} since it is used while
     * serving requests concurrently.
     *
     * @param codec codec to be used for web service requests
     */
    public void setCodec(@NotNull Codec codec) {
        wrappedContext.setCodec(codec);
    }
    
    /*
     * TODO: remove following methods; this is just a hack until all pipes get converted to tubes
     */ 
    
    public SecureConversationInitiator getScInitiator() {
        return scInitiator;
    }
    
    public void setScInitiator(SecureConversationInitiator initiator) {
        this.scInitiator = initiator;
    }
}
