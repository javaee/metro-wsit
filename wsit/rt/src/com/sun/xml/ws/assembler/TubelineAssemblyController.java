/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.encoding.LazyStreamCodec;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.transport.tcp.wsit.TCPConstants;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.rm.runtime.RmTubeAppender;
import com.sun.xml.ws.rm.runtime.testing.PacketFilteringTubeAppender;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.transport.tcp.wsit.TCPTransportPipeFactory;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.tx.client.TxClientPipe;
import com.sun.xml.ws.tx.service.TxServerPipe;
import com.sun.xml.ws.util.ServiceConfigurationError;
import com.sun.xml.wss.jaxws.impl.SecurityClientPipe;
import com.sun.xml.wss.jaxws.impl.SecurityServerPipe;
import com.sun.xml.xwss.XWSSClientPipe;
import com.sun.xml.xwss.XWSSServerPipe;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class TubelineAssemblyController {
    private static final String BEFORE_SUFFIX = ".before";
    private static final String AFTER_SUFFIX = ".after";
    private static final String TRANSPORT_SUFFIX = ".transport";
    private static final String WSS_SUFFIX = ".wss";
    private static final String WSA_SUFFIX = ".wsa";
    private static final String WSRM_SUFFIX = ".wsrm";
    private static final String WSTX_SUFFIX = ".wstx";

    public static class TxTubeAppender implements TubeAppender {

        private static final String WSAT_SOAP_NSURI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
        private static final QName AT_ALWAYS_CAPABILITY = new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");
        private static final QName AT_ASSERTION = new QName(WSAT_SOAP_NSURI, "ATAssertion");

        /**
         * Adds TX tube to the client-side tubeline, depending on whether TX is enabled or not.
         * 
         * @param context wsit client tubeline assembler context
         * @return new tail of the client-side tubeline
         */
        public Tube appendTube(WsitClientTubeAssemblyContext context) {
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
        public Tube appendTube(WsitServerTubeAssemblyContext context) {
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
    }

    public static class DumpTubeAppender implements TubeAppender {

        public static final String PREFIX = "com.sun.xml.ws.assembler";
        public static final String CLIENT_PREFIX = PREFIX + ".client";
        public static final String SERVER_PREFIX = PREFIX + ".server";
        private final String name;

        public DumpTubeAppender(String name) {
            this.name = name;
        }

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            if (Boolean.getBoolean(CLIENT_PREFIX + name)) {
                return context.getWrappedContext().createDumpTube(name, System.out, context.getTubelineHead());
            }

            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            if (Boolean.getBoolean(SERVER_PREFIX + name)) {
                return context.getWrappedContext().createDumpTube(name, System.out, context.getTubelineHead());
            }

            return context.getTubelineHead();
        }
    }

    public static class MessageDumpingTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            MessageDumpingFeature msgDumper = context.getBinding().getFeature(MessageDumpingFeature.class);
            if (msgDumper != null) {
                return msgDumper.createMessageDumpingTube(context.getTubelineHead());
            }

            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
        }
    }

    public static class TransportTubeAppender implements TubeAppender {

        private static final String AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/transport/client";
        private static final QName AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION = new QName(AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI, "AutomaticallySelectOptimalTransport");

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            if (isOptimizedTransportEnabled(context.getPolicyMap(), context.getWsdlPort(), context.getPortInfo())) {
                return TCPTransportPipeFactory.doCreate(context, false);
            } else {
                return context.getWrappedContext().createTransportTube();
            }
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
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
    }

    public static class AddressingTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            if (isAddressingEnabled(context.getPolicyMap(), context.getWsdlPort(), context.getBinding())) {
                return context.getWrappedContext().createWsaTube(context.getTubelineHead());
            }

            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            if (isAddressingEnabled(context.getPolicyMap(), context.getWsdlPort(), context.getEndpoint().getBinding())) {
                return context.getWrappedContext().createWsaTube(context.getTubelineHead());
            }

            return context.getTubelineHead();
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
    }

    public static class SecurityTubeAppender implements TubeAppender, TubelineAssemblyContextUpdater {

        private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
        //Added for Security Pipe Unification with JSR 196 on GlassFish
        private static final String ENDPOINT = "ENDPOINT";
        private static final String NEXT_PIPE = "NEXT_PIPE";
        private static final String POLICY = "POLICY";
        private static final String SEI_MODEL = "SEI_MODEL";
        // private static final String SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
        private static final String WSDL_MODEL = "WSDL_MODEL";
        private static final String GF_SERVER_SEC_PIPE = "com.sun.enterprise.webservice.CommonServerSecurityPipe";
        private Boolean securityEnabled = null;

        public void prepareContext(WsitClientTubeAssemblyContext context) throws WebServiceException {
            if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                context.setCodec(createSecurityCodec(context.getBinding()));
            }
        }

        public void prepareContext(WsitServerTubeAssemblyContext context) throws WebServiceException {
            if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                context.setCodec(createSecurityCodec(context.getEndpoint().getBinding()));
            }
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            ServerPipelineHook hook = context.getEndpoint().getContainer().getSPI(ServerPipelineHook.class);
            if (hook != null) {
                // TODO ask security to implement the hook.createSecurityTube(context);
                Pipe securityPipe = hook.createSecurityPipe(
                        context.getPolicyMap(), 
                        context.getSEIModel(), 
                        context.getWsdlPort(),
                        context.getEndpoint(),
                        context.getAdaptedTubelineHead());
                return PipeAdapter.adapt(securityPipe);
                // THIS IS A NEW CODE THAT SHOULD REPLACE THE PIPE-BASED CODE
                // return hook.createSecurityTube(context);
            } else if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                if (serverTubeLineHookExists()) {
                    return createSecurityTube(context);
                } else {
                    //TODO: Log a FINE message indicating could not use Unified Tube.
                    return PipeAdapter.adapt(new SecurityServerPipe(context, context.getAdaptedTubelineHead()));
                }

            } else {
                try {
                    //look for XWSS 2.0 Style Security
                    if (!context.isPolicyAvailable() && isSecurityConfigPresent(context)) {
                        return initializeXWSSServerTube(context);
                    }
                } catch (NoClassDefFoundError err) {
                    // do nothing
                }
            }

            return context.getTubelineHead();
        }

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            //Look for pipe-creation hook exposed in contaner.
            ClientPipelineHook hook = context.getContainer().getSPI(ClientPipelineHook.class);
            if (hook == null) {
                //If not found, look for pipe-creation hook using services
                ClientPipelineHook[] hooks = loadSPs(ClientPipelineHook.class);
                if (hooks != null && hooks.length > 0) {
                    hook = hooks[0];
                }
            }
            //If either mechanism for finding a ClientPipelineHook has found one, use it.
            if (hook != null) {
                // TODO ask security to implement the hook.createSecurityTube(context);
                ClientPipeAssemblerContext pipeContext = new ClientPipeAssemblerContext(
                        context.getAddress(),
                        context.getWsdlPort(),
                        context.getService(),
                        context.getBinding(),
                        context.getContainer()
                        );
                Pipe securityPipe = hook.createSecurityPipe(context.getPolicyMap(), pipeContext, context.getAdaptedTubelineHead());
                 if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                     context.setScInitiator((SecureConversationInitiator) securityPipe);
                 }
                return PipeAdapter.adapt(securityPipe);
                // THIS IS A NEW CODE THAT SHOULD REPLACE THE PIPE-BASED CODE
                // Tube securityTube = hook.createSecurityTube(context); 
                // if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                //     // TODO remove when RM is able to use the context.getImplementation() method, 
                //     // this will be possible once Security pipe is converted to a tube
                //     context.setScInitiator((SecureConversationInitiator) securityTube);
                // }
                // return securityTube;
            } else if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                //Use the default WSIT Client Security Pipe
                Pipe securityPipe = new SecurityClientPipe(context, context.getAdaptedTubelineHead());
                context.setScInitiator((SecureConversationInitiator) securityPipe);
                return PipeAdapter.adapt(securityPipe);
            } else if (!context.isPolicyAvailable() && isSecurityConfigPresent(context)) {
                //look for XWSS 2.0 Style Security
                // policyMap may be null in case of client dispatch without a client config file
                return initializeXWSSClientTube(context);
            } else {
                return context.getTubelineHead();
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
            if (securityEnabled != null) {
                return securityEnabled; // we have already called this method before
            }

            if (policyMap == null || wsdlPort == null) {
                return securityEnabled = false;
            }

            try {
                PolicyMapKey endpointKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(),
                        wsdlPort.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);

                if ((policy != null) &&
                        (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                        policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri) ||
                        policy.contains(SecurityPolicyVersion.SECURITYPOLICY200512.namespaceUri))) {
                    return securityEnabled = true;
                }

                for (WSDLBoundOperation wbo : wsdlPort.getBinding().getBindingOperations()) {
                    PolicyMapKey operationKey = PolicyMap.createWsdlOperationScopeKey(wsdlPort.getOwner().getName(),
                            wsdlPort.getName(),
                            wbo.getName());
                    policy = policyMap.getOperationEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return securityEnabled = true;
                    }

                    policy = policyMap.getInputMessageEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return securityEnabled = true;
                    }

                    policy = policyMap.getOutputMessageEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return securityEnabled = true;
                    }

                    policy = policyMap.getFaultMessageEffectivePolicy(operationKey);
                    if ((policy != null) &&
                            (policy.contains(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri) ||
                            policy.contains(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri))) {
                        return securityEnabled = true;
                    }
                }
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }

            return securityEnabled = false;
        }

        private Codec createSecurityCodec(WSBinding binding) {
            StreamSOAPCodec primaryCodec = Codecs.createSOAPEnvelopeXmlCodec(binding.getSOAPVersion());
            LazyStreamCodec lsc = new LazyStreamCodec(primaryCodec);
            return Codecs.createSOAPBindingCodec(binding, lsc);
        }

        private static <P> P[] loadSPs(final Class<P> svcClass) {
            return ServiceFinder.find(svcClass).toArray();
        }

        private boolean serverTubeLineHookExists() {
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

        private boolean isSecurityConfigPresent(WsitClientTubeAssemblyContext context) {
            // returning true for empty policy map by default for now, because the Client Side Security Config is only 
            // accessible as a Runtime Property on BindingProvider.RequestContext
            return true;
        }

        private boolean isSecurityConfigPresent(WsitServerTubeAssemblyContext context) {

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

        private Tube initializeXWSSClientTube(WsitClientTubeAssemblyContext context) {
            return PipeAdapter.adapt(new XWSSClientPipe(context.getWsdlPort(), context.getService(), context.getBinding(), context.getAdaptedTubelineHead()));
        }

        private Tube initializeXWSSServerTube(WsitServerTubeAssemblyContext context) {
            return PipeAdapter.adapt(new XWSSServerPipe(context.getEndpoint(), context.getWsdlPort(), context.getAdaptedTubelineHead()));
        }

        @SuppressWarnings("unchecked")
        private Tube createSecurityTube(WsitServerTubeAssemblyContext context) {
            HashMap props = new HashMap();
            props.put(POLICY, context.getPolicyMap());
            props.put(SEI_MODEL, context.getSEIModel());
            props.put(WSDL_MODEL, context.getWsdlPort());
            props.put(ENDPOINT, context.getEndpoint());
            props.put(NEXT_PIPE, context.getAdaptedTubelineHead());
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
                        return PipeAdapter.adapt((Pipe) ctor.newInstance(props, context.getAdaptedTubelineHead(), false));
                    }
                }

                return context.getTubelineHead();
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

    public static class MonitoringTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createMonitoringTube(context.getTubelineHead());
        }
    }

    public static class MustUnderstandTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createClientMUTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createServerMUTube(context.getTubelineHead());
        }
    }

    public static class ValidationTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createValidationTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createValidationTube(context.getTubelineHead());
        }
    }
    
    public static class HandlerTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createHandlerTube(context.getTubelineHead());
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().createHandlerTube(context.getTubelineHead());
        }
    }
    
    public static class TerminalTubeAppender implements TubeAppender {

        public Tube appendTube(WsitClientTubeAssemblyContext context) throws WebServiceException {
            return context.getTubelineHead();
        }

        public Tube appendTube(WsitServerTubeAssemblyContext context) throws WebServiceException {
            return context.getWrappedContext().getTerminalTube();
        }
    }
    
    private static final TubeAppender transportAppender = new TransportTubeAppender();
    private static final TubeAppender messageDumpingAppender = new MessageDumpingTubeAppender();
    private static final TubeAppender packetFilteringAppender = new PacketFilteringTubeAppender();    
    private static final TubeAppender actionDumpAppender = new ActionDumpTubeAppender();
    private static final TubeAppender securityAppender = new SecurityTubeAppender();
    private static final TubeAppender reliableMessagingAppender = new RmTubeAppender();
    private static final TubeAppender transactionsAppender = new TxTubeAppender();
    private static final TubeAppender addressingAppender = new AddressingTubeAppender();
    private static final TubeAppender monitoringAppender = new MonitoringTubeAppender();
    private static final TubeAppender mustUnderstandAppender = new MustUnderstandTubeAppender();
    private static final TubeAppender validationAppender = new ValidationTubeAppender();
    private static final TubeAppender handlerAppender = new HandlerTubeAppender();
    private static final TubeAppender terminalAppender = new TerminalTubeAppender();
    
    private static final TubeAppender[] clientAppenders = new TubeAppender[]{
        transportAppender,
        messageDumpingAppender,
        packetFilteringAppender,
        new DumpTubeAppender(""),
        actionDumpAppender,
        new DumpTubeAppender(TRANSPORT_SUFFIX),
        new DumpTubeAppender(WSS_SUFFIX + AFTER_SUFFIX),
        securityAppender,
        new DumpTubeAppender(WSS_SUFFIX + BEFORE_SUFFIX),
        // TODO MEX pipe here
        new DumpTubeAppender(WSRM_SUFFIX + AFTER_SUFFIX),
        reliableMessagingAppender,
        new DumpTubeAppender(WSRM_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSTX_SUFFIX + AFTER_SUFFIX),
        transactionsAppender,
        new DumpTubeAppender(WSTX_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSA_SUFFIX + AFTER_SUFFIX),
        addressingAppender,
        new DumpTubeAppender(WSA_SUFFIX + BEFORE_SUFFIX),
        monitoringAppender,
        mustUnderstandAppender,
        validationAppender,
        handlerAppender/*,
        terminalAppender*/
    };
    
    private static final TubeAppender[] serverAppenders = new TubeAppender[]{
        terminalAppender,
        validationAppender,
        handlerAppender,
        mustUnderstandAppender,
        monitoringAppender,
        new DumpTubeAppender(WSTX_SUFFIX + AFTER_SUFFIX),
        transactionsAppender,
        new DumpTubeAppender(WSTX_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSRM_SUFFIX + AFTER_SUFFIX),
        reliableMessagingAppender,
        new DumpTubeAppender(WSRM_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(WSA_SUFFIX + AFTER_SUFFIX),
        addressingAppender,
        new DumpTubeAppender(WSA_SUFFIX + BEFORE_SUFFIX),
        // TODO MEX pipe here ?
        new DumpTubeAppender(WSS_SUFFIX + AFTER_SUFFIX),
        securityAppender,
        new DumpTubeAppender(WSS_SUFFIX + BEFORE_SUFFIX),
        new DumpTubeAppender(TRANSPORT_SUFFIX),
        actionDumpAppender,
        new DumpTubeAppender(""),
        packetFilteringAppender,
        messageDumpingAppender /*,
        transportAppender*/
    };
    
    Collection<TubeAppender> getClientSideAppenders() {
        return Arrays.asList(clientAppenders);
    }

    Collection<TubeAppender> getServerSideAppenders() {
        return Arrays.asList(serverAppenders);        
    }
}
