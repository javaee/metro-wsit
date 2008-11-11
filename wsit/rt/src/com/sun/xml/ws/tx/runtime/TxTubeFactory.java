package com.sun.xml.ws.tx.runtime;

import com.sun.xml.ws.assembler.*;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.tx.client.TxClientPipe;
import com.sun.xml.ws.tx.common.Util;
import com.sun.xml.ws.tx.service.TxServerPipe;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

public final class TxTubeFactory implements TubeFactory {

    private static final String WSAT_SOAP_NSURI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    private static final QName AT_ALWAYS_CAPABILITY = new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");
    private static final QName AT_ASSERTION = new QName(WSAT_SOAP_NSURI, "ATAssertion");

    /**
     * Adds TX tube to the client-side tubeline, depending on whether TX is enabled or not.
     *
     * @param context wsit client tubeline assembler context
     * @return new tail of the client-side tubeline
     */
    public Tube createTube(ClientTubelineAssemblyContext context) {
        if (isTransactionsEnabled(context.getPolicyMap(), context.getWsdlPort(), false)) {
            return PipeAdapter.adapt(new TxClientPipe(context, context.getAdaptedTubelineHead()));
        } else {
            return context.getTubelineHead();
        }
    }

    /**
     * Adds TX tube to the service-side tubeline, depending on whether TX is enabled or not.
     *
     * @param context wsit service tubeline assembler context
     * @return new head of the service-side tubeline
     */
    public Tube createTube(ServerTubelineAssemblyContext context) {
        if (isTransactionsEnabled(context.getPolicyMap(), context.getWsdlPort(), true)) {
            return PipeAdapter.adapt(new TxServerPipe(context, context.getAdaptedTubelineHead()));
        } else {
            return context.getTubelineHead();
        }
    }

    /**
     * Checks to see whether WS-Atomic Transactions are enabled or not.
     *
     * @param policyMap policy map for {@link this} assembler
     * @param wsdlPort the WSDLPort object
     * @param isServerSide true iff this method is being called from {@link PipelineAssembler#createServer(ServerPipeAssemblerContext)}
     * @return true if Transactions is enabled, false otherwise
     */
    private boolean isTransactionsEnabled(PolicyMap policyMap, WSDLPort wsdlPort, boolean isServerSide) {
        if (policyMap == null || wsdlPort == null || !Util.isJTAAvailable()) {
            // false for standalone WSIT client or WSIT Service in Tomcat
            return false;
        }
        try {
            PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(), wsdlPort.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
            for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(), wsdlPort.getName(), wbo.getName());
                policy = policyMap.getOperationEffectivePolicy(operationKey);
                if (policy != null) {
                    // look for ATAlwaysCapable on the server side
                    if ((isServerSide) && (policy.contains(AT_ALWAYS_CAPABILITY))) {
                        return true;
                    }
                    // look for ATAssertion in both client and server
                    if (policy.contains(AT_ASSERTION)) {
                        return true;
                    }
                }
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
        return false;
    }
}
