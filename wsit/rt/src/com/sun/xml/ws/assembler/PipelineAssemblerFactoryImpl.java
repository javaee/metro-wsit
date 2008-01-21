/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.ws.tx.common.Util;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.sun.istack.NotNull;
import com.sun.xml.ws.encoding.LazyStreamCodec;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.transport.tcp.wsit.TCPConstants;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipelineAssembler;
import com.sun.xml.ws.api.pipe.PipelineAssemblerFactory;
import com.sun.xml.ws.api.pipe.ServerPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.xmlstreamwriter.documentfilter.WsdlDocumentFilter;
import com.sun.xml.ws.rm.policy.RmTubeAppender;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.transport.tcp.wsit.TCPTransportPipeFactory;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.tx.client.TxClientPipe;
import com.sun.xml.ws.tx.service.TxServerPipe;
import com.sun.xml.ws.util.ServiceConfigurationError;
import com.sun.xml.wss.jaxws.impl.SecurityClientPipe;
import com.sun.xml.wss.jaxws.impl.SecurityServerPipe;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * WSIT PipelineAssembler.
 *
 * @author Arun Gupta
 */
@SuppressWarnings("deprecation")
public final class PipelineAssemblerFactoryImpl extends PipelineAssemblerFactory {

    private static final String PREFIX = "com.sun.xml.ws.assembler";
    private static final String CLIENT_PREFIX = PREFIX + ".client";
    private static final String SERVER_PREFIX = PREFIX + ".server";
    private static final String BEFORE_SUFFIX = ".before";
    private static final String AFTER_SUFFIX = ".after";
    private static final String TRANSPORT_SUFFIX = ".transport";
    private static final String ACTION_SUFFIX = ".action";
    private static final String WSS_SUFFIX = ".wss";
    private static final String WSA_SUFFIX = ".wsa";
    private static final String WSMEX_SUFFIX = ".wsmex";
    private static final String WSRM_SUFFIX = ".wsrm";
    private static final String WSTX_SUFFIX = ".wstx";
    private static final String WSAT_SOAP_NSURI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    private static final QName AT_ALWAYS_CAPABILITY = new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");
    private static final QName AT_ASSERTION = new QName(WSAT_SOAP_NSURI, "ATAssertion");
    private static final String AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/transport/client";
    private static final QName AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION = new QName(AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI, "AutomaticallySelectOptimalTransport");
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    //Added for Security Pipe Unification with JSR 196 on GlassFish
    static final String ENDPOINT = "ENDPOINT";
    static final String NEXT_PIPE = "NEXT_PIPE";
    static final String POLICY = "POLICY";
    static final String SEI_MODEL = "SEI_MODEL";
    static final String SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
    static final String WSDL_MODEL = "WSDL_MODEL";
    static final String GF_SERVER_SEC_PIPE = "com.sun.enterprise.webservice.CommonServerSecurityPipe";
    //----------------
    private static class WsitPipelineAssembler implements PipelineAssembler {

        private BindingID bindingId;

        WsitPipelineAssembler(final BindingID bindingId) {
            this.bindingId = bindingId;
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public Pipe createClient(@NotNull ClientPipeAssemblerContext context) {

            WsitClientTubeAssemblyContext wsitContext = new WsitClientTubeAssemblyContext(context);

            boolean isSecurityEnabled = isSecurityEnabled(wsitContext.getPolicyMap(), wsitContext.getWsdlPort());
            if (isSecurityEnabled) {
                setSecurityCodec(wsitContext);
            }
            // Transport pipe ALWAYS exist
            Pipe tail;
            if (isOptimizedTransportEnabled(wsitContext.getPolicyMap(), wsitContext.getWsdlPort(), wsitContext.getPortInfo())) {
                tail = TCPTransportPipeFactory.doCreate(context, false);
            } else {
                tail = context.createTransportPipe();
            }

            MessageDumpingFeature msgDumper = wsitContext.getBinding().getFeature(MessageDumpingFeature.class);
            if (msgDumper != null) {
                tail = PipeAdapter.adapt(msgDumper.createMessageDumpingTube(PipeAdapter.adapt(tail)));
            }

            tail = dump(context, CLIENT_PREFIX, tail);
            tail = dumpAction(CLIENT_PREFIX + ACTION_SUFFIX, context.getBinding(), tail);
            tail = dump(context, CLIENT_PREFIX + TRANSPORT_SUFFIX, tail);

            tail = dump(context, CLIENT_PREFIX + WSS_SUFFIX + AFTER_SUFFIX, tail);

            // check for Security
            //Look for pipe-creation hook exposed in contaner.
            ClientPipelineHook hook = wsitContext.getContainer().getSPI(ClientPipelineHook.class);
            if (hook == null) {
                //If not found, look for pipe-creation hook using services
                ClientPipelineHook[] hooks = loadSPs(ClientPipelineHook.class);
                if (hooks != null && hooks.length > 0) {
                    hook = hooks[0];
                }
            }
            //If either mechanism for finding a ClientPipelineHook has found one, use it.
            if (hook != null) {
                tail = hook.createSecurityPipe(wsitContext.getPolicyMap(), context, tail);
                if (isSecurityEnabled) {
                    wsitContext.setScInitiator((SecureConversationInitiator) tail);
                }
            } else {
                if (isSecurityEnabled) {
                    //Use the default WSIT Client Security Pipe
                    tail = new SecurityClientPipe(wsitContext, tail);
                    wsitContext.setScInitiator((SecureConversationInitiator) tail);
                } else {
                    //look for XWSS 2.0 Style Security
                    // policyMap may be null in case of client dispatch without a client config file
                    if (!wsitContext.isPolicyAvailable() && isSecurityConfigPresent(wsitContext)) {
                        tail = initializeXWSSClientPipe(wsitContext, tail);
                    }
                }
            }
            tail = dump(context, CLIENT_PREFIX + WSS_SUFFIX + BEFORE_SUFFIX, tail);

            // MEX pipe here

            tail = dump(context, CLIENT_PREFIX + WSRM_SUFFIX + AFTER_SUFFIX, tail);
            
            // check for WS-Reliable Messaging
            tail = new RmTubeAppender().appendPipe(wsitContext, tail);            
            // tail = PipeAdapter.adapt(new RmTubeAppender().appendTube(wsitContext, PipeAdapter.adapt(tail)));
            
            tail = dump(context, CLIENT_PREFIX + WSRM_SUFFIX + BEFORE_SUFFIX, tail);

            tail = dump(context, CLIENT_PREFIX + WSTX_SUFFIX + AFTER_SUFFIX, tail);
            // check for WS-Atomic Transactions
            tail = appendTxPipe(wsitContext, tail);
            tail = dump(context, CLIENT_PREFIX + WSTX_SUFFIX + BEFORE_SUFFIX, tail);

            tail = dump(context, CLIENT_PREFIX + WSA_SUFFIX + AFTER_SUFFIX, tail);
            // check for WS-Addressing
            if (isAddressingEnabled(wsitContext.getPolicyMap(), wsitContext.getWsdlPort(), wsitContext.getBinding())) {
                tail = context.createWsaPipe(tail);
            }
            tail = dump(context, CLIENT_PREFIX + WSA_SUFFIX + BEFORE_SUFFIX, tail);
            tail = context.createClientMUPipe(tail);
            tail = context.createValidationPipe(tail);
            tail = context.createHandlerPipe(tail);

            return tail;
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public Pipe createServer(@NotNull ServerPipeAssemblerContext context) {
            WsitServerTubeAssemblyContext wsitContext = new WsitServerTubeAssemblyContext(context);
            ServiceDefinition sd = wsitContext.getEndpoint().getServiceDefinition();
            if (sd != null) {
                sd.addFilter(new WsdlDocumentFilter());
            }

            Pipe head = context.getTerminalPipe();
            head = context.createValidationPipe(head);
            head = context.createHandlerPipe(head);
            head = context.createServerMUPipe(head);
            head = context.createMonitoringPipe(head);

            head = dump(context, SERVER_PREFIX + WSTX_SUFFIX + AFTER_SUFFIX, head);
            // check for WS-Atomic Transactions
            head = appendTxPipe(wsitContext, head);
            head = dump(context, SERVER_PREFIX + WSTX_SUFFIX + BEFORE_SUFFIX, head);

            head = dump(context, SERVER_PREFIX + WSRM_SUFFIX + AFTER_SUFFIX, head);
            
            // check for WS-Reliable Messaging            
            head = new RmTubeAppender().appendPipe(wsitContext, head);
            // head = PipeAdapter.adapt(new RmTubeAppender().appendTube(wsitContext, PipeAdapter.adapt(head)));
            
            head = dump(context, SERVER_PREFIX + WSRM_SUFFIX + BEFORE_SUFFIX, head);

            head = dump(context, SERVER_PREFIX + WSA_SUFFIX + AFTER_SUFFIX, head);
            // check for WS-Addressing
            if (isAddressingEnabled(wsitContext.getPolicyMap(), wsitContext.getWsdlPort(), wsitContext.getEndpoint().getBinding())) {
                head = context.createWsaPipe(head);
            }
            head = dump(context, SERVER_PREFIX + WSA_SUFFIX + BEFORE_SUFFIX, head);

            head = dump(context, SERVER_PREFIX + WSS_SUFFIX + AFTER_SUFFIX, head);

            // check for Security
            boolean securityIsEnabled = isSecurityEnabled(wsitContext.getPolicyMap(), wsitContext.getWsdlPort());
            ServerPipelineHook hook = wsitContext.getEndpoint().getContainer().getSPI(ServerPipelineHook.class);

            if (hook != null) {
                if (securityIsEnabled) {
                    setSecurityCodec(wsitContext);
                }
                head = hook.createSecurityPipe(wsitContext.getPolicyMap(), wsitContext.getSEIModel(), wsitContext.getWsdlPort(), wsitContext.getEndpoint(), head);
            } else {
                if (securityIsEnabled) {
                    setSecurityCodec(wsitContext);

                    if (serverPipeLineHookExists()) {
                        head = createSecurityPipe(wsitContext, head);
                    } else {
                        //TODO: Log a FINE message indicating could not use Unified Pipe.
                        head = new SecurityServerPipe(wsitContext, head);
                    }

                } else {
                    try {
                        //look for XWSS 2.0 Style Security
                        if (!wsitContext.isPolicyAvailable() && isSecurityConfigPresent(wsitContext)) {
                            head = initializeXWSSServerPipe(wsitContext, head);
                        }
                    } catch (NoClassDefFoundError err) {
                        // do nothing
                    }
                }
            }
            head = dump(context, SERVER_PREFIX + WSS_SUFFIX + BEFORE_SUFFIX, head);

            head = dump(context, SERVER_PREFIX + TRANSPORT_SUFFIX, head);
            head = dumpAction(SERVER_PREFIX + ACTION_SUFFIX, wsitContext.getEndpoint().getBinding(), head);
            head = dump(context, SERVER_PREFIX, head);

            return head;
        }

        private Pipe dump(ClientPipeAssemblerContext context, String name, Pipe p) {
            if (Boolean.getBoolean(name)) {
                p = context.createDumpPipe(name, System.out, p);
            }

            return p;
        }

        private Pipe dump(ServerPipeAssemblerContext context, String name, Pipe p) {
            if (Boolean.getBoolean(name)) {
                p = context.createDumpPipe(name, System.out, p);
            }

            return p;
        }

        /**
         * Checks to see whether OptimizedTransport is enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param port the WSDLPort object
         * @param portInfo the WSPortInfo object
         * @return true if OptimizedTransport is enabled, false otherwise
         */
        private boolean isOptimizedTransportEnabled(PolicyMap policyMap, WSDLPort port, WSPortInfo portInfo) {
            if (policyMap == null || port == null) {
                return false;
            }

            String schema = null;
            if (port != null) {
                schema = port.getAddress().getURI().getScheme();
            } else if (portInfo != null) {
                schema = portInfo.getEndpointAddress().getURI().getScheme();
            }
            if (com.sun.xml.ws.transport.tcp.util.TCPConstants.PROTOCOL_SCHEMA.equals(schema)) {
                // if target endpoint URI starts with TCP schema - dont check policies, just return true
                return true;
            }

            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(port.getOwner().getName(), port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                if (policy != null && policy.contains(TCPConstants.TCPTRANSPORT_POLICY_ASSERTION) &&
                        policy.contains(AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION)) {
                    /* if client set to choose optimal transport and server has TCP transport policy
                    then need to check server side policy "enabled" attribute*/
                    for (AssertionSet assertionSet : policy) {
                        for (PolicyAssertion assertion : assertionSet) {
                            if (assertion.getName().equals(TCPConstants.TCPTRANSPORT_POLICY_ASSERTION)) {
                                String value = assertion.getAttributeValue(new QName("enabled"));
                                if (value == null) {
                                    return false;
                                }
                                value = value.trim();

                                return (Boolean.valueOf(value) || value.equalsIgnoreCase("yes"));
                            }
                        }
                    }
                }

                return false;
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }
        }

        /**
         * Adds TX tube to the client-side tubeline, depending on whether TX is enabled or not.
         * 
         * @param context wsit client tubeline assembler context
         * @param tubelineTail tail of the client-side tubeline being constructed
         * @return new tail of the client-side tubeline
         */
        private Pipe appendTxPipe(WsitClientTubeAssemblyContext context, Pipe next) {
            if (isTransactionsEnabled(context.getPolicyMap(), context.getWsdlPort(), false)) {
                return new TxClientPipe(context, next);
             } else {
                return next;
            }
        }

        /**
         * Adds TX tube to the service-side tubeline, depending on whether TX is enabled or not.
         * 
         * @param context wsit service tubeline assembler context
         * @param tubelineTail tail of the service-side tubeline being constructed
         * @return new head of the service-side tubeline
         */
        private Pipe appendTxPipe(WsitServerTubeAssemblyContext context, Pipe next) {
            if (isTransactionsEnabled(context.getPolicyMap(), context.getWsdlPort(), true)) {
                return new TxServerPipe(context, next);
            } else {
                return next;
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
            if (policyMap == null || wsdlPort == null ||
                    !Util.isJTAAvailable()) {     // false for standalone WSIT client or WSIT Service in Tomcat
                return false;
            }
            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                    PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                            wsdlPort.getName(),
                            wbo.getName());
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
         * Checks to see whether WS-Addressing is enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param port wsdl:port
         * @param binding Binding
         * @return true if Addressing is enabled, false otherwise
         */
        private boolean isAddressingEnabled(PolicyMap policyMap, WSDLPort port, WSBinding binding) {
            if (AddressingVersion.isEnabled(binding)) {
                return true;
            }

            if (policyMap == null || port == null) {
                return false;
            }

            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(port.getOwner().getName(),
                        port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                return (policy != null) &&
                        (policy.contains(AddressingVersion.W3C.policyNsUri) ||
                        policy.contains(AddressingVersion.MEMBER.policyNsUri));
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }
        }

        /**
         * Checks to see whether WS-Security is enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param wsdlPort wsdl:port
         * @return true if Security is enabled, false otherwise
         */
        private boolean isSecurityEnabled(PolicyMap policyMap, WSDLPort wsdlPort) {
            if (policyMap == null || wsdlPort == null) {
                return false;
            }

            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                if ((policy != null) &&
                        (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                        policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri) ||
                        policy.contains(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri))) {
                    return true;
                }

                for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                    PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                            wsdlPort.getName(),
                            wbo.getName());
                    policy = policyMap.getOperationEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return true;
                    }

                    policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return true;
                    }

                    policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return true;
                    }

                    policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return true;
                    }
                }
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }

            return false;
        }

        private static <P> P[] loadSPs(final Class<P> svcClass) {
            return ServiceFinder.find(svcClass).toArray();
        }

        private Pipe dumpAction(String name, WSBinding binding, Pipe p) {
            if (Boolean.getBoolean(name)) {
                ActionDumpPipe[] pipes = loadSPs(ActionDumpPipe.class);
                if (pipes.length > 0) {
                    return pipes[0];
                }
                return new ActionDumpPipe(name, binding, p);
            }
            return p;
        }

        private boolean serverPipeLineHookExists() {
            // The ServerPipeline Hook in GF fails to create the Pipe because GF ServerPipeCreator does not have a
            // Default CTOR.
            //TODO: change this method impl later.
            try {
                ServerPipelineHook[] hooks = loadSPs(ServerPipelineHook.class);
                if (hooks != null && hooks.length > 0) {
                    return true;
                }
            } catch (ServiceConfigurationError ex) {
                //workaround since GF ServerPipeCreator has no Default CTOR.
                if (ex.getCause() instanceof InstantiationException) {
                    return true;
                }
                return false;
            }
            return false;
        }

        private void setSecurityCodec(WsitServerTubeAssemblyContext context) {
            StreamSOAPCodec primaryCodec = Codecs.createSOAPEnvelopeXmlCodec(context.getEndpoint().getBinding().getSOAPVersion());
            LazyStreamCodec lsc = new LazyStreamCodec(primaryCodec);
            Codec fullCodec = Codecs.createSOAPBindingCodec(context.getEndpoint().getBinding(), lsc);
            context.setCodec(fullCodec);
        }

        private void setSecurityCodec(WsitClientTubeAssemblyContext context) {
            StreamSOAPCodec primaryCodec = Codecs.createSOAPEnvelopeXmlCodec(context.getBinding().getSOAPVersion());
            LazyStreamCodec lsc = new LazyStreamCodec(primaryCodec);
            Codec fullCodec = Codecs.createSOAPBindingCodec(context.getBinding(), lsc);
            context.setCodec(fullCodec);
        }
    }

    public PipelineAssembler doCreate(final BindingID bindingId) {
        return new WsitPipelineAssembler(bindingId);
    }

    private static boolean isSecurityConfigPresent(WsitClientTubeAssemblyContext context) {
        // returning true for empty policy map by default for now, because the Client Side Security Config is only 
        // accessible as a Runtime Property on BindingProvider.RequestContext
        return true;
    }

    private static boolean isSecurityConfigPresent(WsitServerTubeAssemblyContext context) {

        QName serviceQName = context.getEndpoint().getServiceName();
        //TODO: not sure which of the two above will give the service name as specified in DD
        String serviceLocalName = serviceQName.getLocalPart();
        Container container = context.getEndpoint().getContainer();

        Object ctxt = null;
        if (container != null) {
            try {
                final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                ctxt = container.getSPI(contextClass);
            } catch (ClassNotFoundException e) {
                //log here that the ServletContext was not found
            }
        }
        String serverName = "server";
        if (ctxt != null) {

            String serverConfig = "/WEB-INF/" + serverName + "_" + "security_config.xml";
            URL url = SecurityUtil.loadFromContext(serverConfig, ctxt);

            if (url == null) {
                serverConfig = "/WEB-INF/" + serviceLocalName + "_" + "security_config.xml";
                url = SecurityUtil.loadFromContext(serverConfig, ctxt);
            }

            if (url != null) {
                return true;
            }
        } else {
            //this could be an EJB or JDK6 endpoint
            //so let us try to locate the config from META-INF classpath
            String serverConfig = "META-INF/" + serverName + "_" + "security_config.xml";
            URL url = SecurityUtil.loadFromClasspath(serverConfig);
            if (url == null) {
                serverConfig = "META-INF/" + serviceLocalName + "_" + "security_config.xml";
                url = SecurityUtil.loadFromClasspath(serverConfig);
            }

            if (url != null) {
                return true;
            }
        }
        return false;
    }

    private static Pipe initializeXWSSClientPipe(WsitClientTubeAssemblyContext context, Pipe next) {
        return new com.sun.xml.xwss.XWSSClientPipe(context.getWsdlPort(), context.getService(), context.getBinding(), next);
    }

    private static Pipe initializeXWSSServerPipe(WsitServerTubeAssemblyContext context, Pipe next) {
        return new com.sun.xml.xwss.XWSSServerPipe(context.getEndpoint(), context.getWsdlPort(), next);
    }

    @SuppressWarnings("unchecked")
    private static Pipe createSecurityPipe(WsitServerTubeAssemblyContext context, Pipe next) {
        HashMap props = new HashMap();
        props.put(POLICY, context.getPolicyMap());
        props.put(SEI_MODEL, context.getSEIModel());
        props.put(WSDL_MODEL, context.getWsdlPort());
        props.put(ENDPOINT, context.getEndpoint());
        props.put(NEXT_PIPE, next);
        //TODO: set it based on  owner.getBinding() but it is not clear
        // how SOAP/TCP is disthinguished.

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class gfServerPipeClass = null;
            if (loader != null) {
                gfServerPipeClass = loader.loadClass(GF_SERVER_SEC_PIPE);
            } else {
                gfServerPipeClass = Class.forName(GF_SERVER_SEC_PIPE);
            }
            if (gfServerPipeClass != null) {
                //now instantiate the class
                Constructor[] ctors = gfServerPipeClass.getDeclaredConstructors();
                Constructor ctor = null;
                for (int i = 0; i < ctors.length; i++) {
                    ctor = ctors[i];
                    Class[] paramTypes = ctor.getParameterTypes();
                    if (paramTypes[0].equals(Map.class)) {
                        break;
                    }
                }
                //Constructor ctor = gfServerPipeClass.getConstructor(Map.class, Pipe.class, Boolean.class);
                if (ctor != null) {
                    return (Pipe) ctor.newInstance(props, next, false);
                }
            }
            
            return next;
        } catch (InstantiationException ex) {
            throw new WebServiceException(ex);
        } catch (IllegalAccessException ex) {
            throw new WebServiceException(ex);
        } catch (IllegalArgumentException ex) {
            throw new WebServiceException(ex);
        } catch (InvocationTargetException ex) {
            throw new WebServiceException(ex);
        } catch (SecurityException ex) {
            throw new WebServiceException(ex);
        } catch (ClassNotFoundException ex) {
            throw new WebServiceException(ex);
        }
    }
}
