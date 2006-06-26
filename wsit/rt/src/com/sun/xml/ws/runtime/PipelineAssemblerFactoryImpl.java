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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingBuilderFactory;
import javax.xml.namespace.QName;

import com.sun.xml.ws.addressing.jaxws.WsaClientPipe;
import com.sun.xml.ws.addressing.jaxws.WsaServerPipe;
import com.sun.xml.ws.addressing.jaxws.WsaWSDLPortExtension;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipelineAssembler;
import com.sun.xml.ws.api.pipe.PipelineAssemblerFactory;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.dispatch.StandalonePipeAssembler;
import com.sun.xml.ws.mex.server.MetadataServerPipe;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.rm.RMConstants;
import com.sun.xml.ws.rm.jaxws.runtime.client.RMClientPipe;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMServerPipe;
import com.sun.xml.ws.util.pipe.DumpPipe;
import com.sun.xml.wss.jaxws.impl.SecurityClientPipe;
import com.sun.xml.wss.jaxws.impl.SecurityServerPipe;

/**
 * Tango PipelineAssembler.
 *
 * @author Arun Gupta
 */
public final class PipelineAssemblerFactoryImpl extends PipelineAssemblerFactory {

    private static final String PREFIX = "com.sun.xml.ws.runtime";
    private static final String CLIENT_PREFIX = PREFIX + ".client";
    private static final String SERVER_PREFIX = PREFIX + ".server";
    private static final String BEFORE_SUFFIX = ".before";
    private static final String AFTER_SUFFIX = ".after";
    private static final String TRANSPORT_SUFFIX = ".transport";
    private static final String WSS_SUFFIX = ".wss";
    private static final String WSA_SUFFIX = ".wsa";
    private static final String WSMEX_SUFFIX = ".wsmex";
    private static final String WSRM_SUFFIX = ".wsrm";
    private static final String WSTX_SUFFIX = ".wstx";
    private static final String CLIENT_CONFIGURATION_FILENAME = "wsit-client.xml";

    private PolicyMap policyMap;

    public PipelineAssembler doCreate(final BindingID bindingId) {
        return new StandalonePipeAssembler() {
            @Override
            public Pipe createClient(EndpointAddress address, WSDLPort wsdlPort, WSService service, WSBinding binding) {
                Pipe p;
                SecurityClientPipe securityClientPipe = null;
                try {
                    initPolicyMap(wsdlPort, true);
                } catch (PolicyException ex) {
                    throw new WebServiceException(ex);
                }

                // Transport pipe ALWAYS exist
                p = createTransport(address, wsdlPort, service, binding);

                p = dump(CLIENT_PREFIX, p);

                p = dump(CLIENT_PREFIX + TRANSPORT_SUFFIX, p);

                p = dump(CLIENT_PREFIX + WSS_SUFFIX + AFTER_SUFFIX, p);
                // check for Security
                if (isSecurityEnabled(wsdlPort)) {
                    ClientPipeConfiguration config = new ClientPipeConfiguration(
                            policyMap, wsdlPort, service, binding);
                    p = new SecurityClientPipe(config, p);
                    securityClientPipe = (SecurityClientPipe) p;
                }
                p = dump(CLIENT_PREFIX + WSS_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(CLIENT_PREFIX + WSA_SUFFIX + AFTER_SUFFIX, p);
                // check for WS-Addressing
                if (isAddressingEnabled(wsdlPort, bindingId)) {
                    p = new WsaClientPipe(wsdlPort, binding, p);
                }
                p = dump(CLIENT_PREFIX + WSA_SUFFIX + BEFORE_SUFFIX, p);

                // MEX pipe here

                p = dump(CLIENT_PREFIX + WSRM_SUFFIX + AFTER_SUFFIX, p);
                // check for WS-Reliable Messaging
                if (isReliableMessagingEnabled(wsdlPort)) {
                    p = new RMClientPipe(wsdlPort, service, binding, securityClientPipe, p);
                }
                p = dump(CLIENT_PREFIX + WSRM_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(CLIENT_PREFIX + WSTX_SUFFIX + AFTER_SUFFIX, p);
                // check for WS-Atomic Transactions
                if (isTransactionsEnabled(wsdlPort, false)) {
                    try {
                        Class c = Class.forName("com.sun.xml.ws.tx.client.TxClientPipe");
                        Constructor ctor = c.getConstructor(ClientPipeConfiguration.class, Pipe.class);
                        p = (Pipe) ctor.newInstance(new ClientPipeConfiguration(policyMap, wsdlPort, service, binding),
                                                    p);
                    } catch (ClassNotFoundException e) {
                        throw new WebServiceException(e);
                    } catch (NoSuchMethodException e) {
                        throw new WebServiceException(e);
                    } catch (InstantiationException e) {
                        throw new WebServiceException(e);
                    } catch (IllegalAccessException e) {
                        throw new WebServiceException(e);
                    } catch (InvocationTargetException e) {
                        throw new WebServiceException(e);
                    }
//                    p = new TxClientPipe(new ClientPipeConfiguration(policyMap, wsdlPort, service, binding), p);
                }
                p = dump(CLIENT_PREFIX + WSTX_SUFFIX + BEFORE_SUFFIX, p);

                return p;
            }

            @Override
            public Pipe createServer(SEIModel seiModel, WSDLPort wsdlPort, WSEndpoint endpoint, Pipe terminal) {
                Pipe p = terminal;
                try {
                    initPolicyMap(wsdlPort, false);
                } catch (PolicyException ex) {
                    throw new WebServiceException(ex);
                }

                p = dump(SERVER_PREFIX + WSTX_SUFFIX + AFTER_SUFFIX, p);
                // check for WS-Atomic Transactions
                if (isTransactionsEnabled(wsdlPort, true)) {
                    try {
                        Class c = Class.forName("com.sun.xml.ws.tx.client.TxServerPipe");
                        Constructor ctor = c.getConstructor(ClientPipeConfiguration.class, Pipe.class);
                        p = (Pipe) ctor.newInstance(wsdlPort, policyMap, p);
                    } catch (ClassNotFoundException e) {
                        throw new WebServiceException(e);
                    } catch (NoSuchMethodException e) {
                        throw new WebServiceException(e);
                    } catch (InstantiationException e) {
                        throw new WebServiceException(e);
                    } catch (IllegalAccessException e) {
                        throw new WebServiceException(e);
                    } catch (InvocationTargetException e) {
                        throw new WebServiceException(e);
                    }
//                    p = new TxServerPipe(wsdlPort, policyMap, p);
                }
                p = dump(SERVER_PREFIX + WSTX_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(SERVER_PREFIX + WSRM_SUFFIX + AFTER_SUFFIX, p);
                // check for WS-Reliable Messaging
                if (isReliableMessagingEnabled(wsdlPort)) {
                    p = new RMServerPipe(wsdlPort, endpoint, p);
                }
                p = dump(SERVER_PREFIX + WSRM_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(SERVER_PREFIX + WSMEX_SUFFIX + AFTER_SUFFIX, p);
                // MEX pipe here
                p = new MetadataServerPipe(endpoint, p);
                p = dump(SERVER_PREFIX + WSMEX_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(SERVER_PREFIX + WSA_SUFFIX + AFTER_SUFFIX, p);
                // check for WS-Addressing
                if (isAddressingEnabled(wsdlPort, bindingId)) {
                    p = new WsaServerPipe(seiModel, wsdlPort, endpoint.getBinding(), p);
                }
                p = dump(SERVER_PREFIX + WSA_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(SERVER_PREFIX + WSS_SUFFIX + AFTER_SUFFIX, p);
                // check for Security
                if (isSecurityEnabled(wsdlPort)) {
                    ServerPipeConfiguration config = new ServerPipeConfiguration(
                            policyMap, wsdlPort, endpoint);
                    p = new SecurityServerPipe(config, p);
                }
                p = dump(SERVER_PREFIX + WSS_SUFFIX + BEFORE_SUFFIX, p);

                p = dump(SERVER_PREFIX + TRANSPORT_SUFFIX, p);

                p = dump(SERVER_PREFIX, p);

                return p;
            }

            public Pipe createServer(WSDLPort wsdlPort, WSEndpoint endpoint, Pipe terminal) {
                return createServer(null, wsdlPort, endpoint, terminal);
            }

            private Pipe dump(String name, Pipe p) {
                if (Boolean.getBoolean(name)) {
                    p = new DumpPipe(name, System.out, p);
                }

                return p;
            }
        };
    }

    /**
     * Checks to see whether WS-Atomic Transactions are enabled or not.
     *
     * @param wsdlPort the WSDLPort object
     * @param isServerSide true iff this method is being called from {@link PipelineAssembler#createServer(SEIModel,WSDLPort,WSEndpoint,Pipe)}
     * @return true if Transactions is enabled, false otherwise
     */
    private boolean isTransactionsEnabled(WSDLPort wsdlPort, boolean isServerSide) {

        // TODO: this is a temporary hack so that ws-tx can explicitly enable their
        // TODO: pipes for development purposes, while insulating it from the rest of the stack
        if (System.getProperty("enableTx") == null) {
            return false;
        }

        if (policyMap == null) {
            return false;
        }

        try {
            PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                                                                            wsdlPort.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

            for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                                                                                  wsdlPort.getName(), wbo.getName());
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

    /**
     * Checks to see whether WS-ReliableMessaging is enabled or not.
     *
     * @param port
     * @return true if ReliableMessaging is enabled, false otherwise
     */
    private boolean isReliableMessagingEnabled(WSDLPort port) {
        if (policyMap == null)
            return false;

        try {
            PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

            return (policy != null) && policy.contains(RMConstants.version);
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * Checks to see whether WS-Addressing is enabled or not.
     *
     * @param port wsdl:port
     * @param bindingId Binding identifier as specified in the deployment descriptor
     * @return true if Addressing is enabled, false otherwise
     */
    private boolean isAddressingEnabled(WSDLPort port, BindingID bindingId) {
        String param = bindingId.getParameter("addressing", "");

        if (null != param && (param.equals("1.0") || param.equals("submission"))) {
            return true;
        }

        WsaWSDLPortExtension ww = port.getExtension(WsaWSDLPortExtension.class);
        if (ww != null && ww.isEnabled())
            return true;

        if (null == policyMap)
            return false;

        try {
            PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

            AddressingBuilderFactory abf = AddressingBuilderFactory.newInstance();
            AddressingConstants ac = abf.newAddressingBuilder().newAddressingConstants();
            AddressingConstants ac2 = abf.newAddressingBuilder("http://schemas.xmlsoap.org/ws/2004/08/addressing").newAddressingConstants();

            return (policy != null) &&
                    (policy.contains(ADDRESSING_POLICY_NAMESPACE_URI) ||
                            policy.contains(ac.getNamespaceURI()) ||
                            policy.contains(ac2.getNamespaceURI()));
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * Checks to see whether WS-Security is enabled or not.
     *
     * @param wsdlPort
     * @return true if Security is enabled, false otherwise
     */
    private boolean isSecurityEnabled(WSDLPort wsdlPort) {
        if (policyMap == null)
            return false;

        try {
            PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                                                                            wsdlPort.getName());
            Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

            if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI)) {
                return true;
            }

            for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                PolicyMapKey operationKey = policyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                                                                                  wsdlPort.getName(), wbo.getName());
                policy = policyMap.getOperationEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;

                policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;

                policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;

                policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                if ((policy != null) && policy.contains(SECURITY_POLICY_NAMESPACE_URI))
                    return true;
            }
        } catch (PolicyException e) {
            throw new WebServiceException(e);
        }

        return false;
    }

    /**
     * Initializes the PolicyMap.
     *
     * @param wsdlPort wsdl:port.
     */
    private void initPolicyMap(WSDLPort wsdlPort, boolean isClient) throws PolicyException {
        if (wsdlPort != null) {
            WSDLBoundPortType binding = wsdlPort.getBinding();
            WSDLModel model = binding.getOwner();
            WSDLPolicyMapWrapper mapWrapper = model.getExtension(WSDLPolicyMapWrapper.class);
            if (mapWrapper != null) {
                if (isClient) {
                    URL clientConfigUrl = Thread.currentThread().getContextClassLoader().getResource(
                            CLIENT_CONFIGURATION_FILENAME);
                    mapWrapper.addClientConfigToMap(clientConfigUrl);
                }
                policyMap = mapWrapper.getPolicyMap();
            }
        }
    }

    private static final String SECURITY_POLICY_NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    private static final String ADDRESSING_POLICY_NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2004/09/policy/addressing";
    private static final String WSAT_SOAP_NSURI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    private static final QName AT_ALWAYS_CAPABILITY = new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");
    private static final QName AT_ASSERTION = new QName(WSAT_SOAP_NSURI, "ATAssertion");
}
