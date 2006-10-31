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

package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.encoding.LazyStreamCodec;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.transport.tcp.client.TCPTransportPipeFactory;
import com.sun.xml.ws.transport.tcp.wsit.TCPConstants;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipelineAssembler;
import com.sun.xml.ws.api.pipe.PipelineAssemblerFactory;
import com.sun.xml.ws.api.pipe.ServerPipeAssemblerContext;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.mex.server.MetadataServerPipe;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.policy.jaxws.documentfilter.PrivateAssertionFilter;
import com.sun.xml.ws.policy.util.PolicyMapUtil;
import com.sun.xml.ws.rm.Constants;
import com.sun.xml.ws.rm.jaxws.runtime.client.RMClientPipe;
import com.sun.xml.ws.rm.jaxws.runtime.server.RMServerPipe;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.wss.jaxws.impl.SecurityClientPipe;
import com.sun.xml.wss.jaxws.impl.SecurityServerPipe;

import java.io.InputStream;

import javax.servlet.ServletContext;

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
    
    private static final String SECURITY_POLICY_NAMESPACE_URI = "http://schemas.xmlsoap.org/ws/2005/07/securitypolicy";
    private static final String WSAT_SOAP_NSURI = "http://schemas.xmlsoap.org/ws/2004/10/wsat";
    private static final QName AT_ALWAYS_CAPABILITY = new QName(WSAT_SOAP_NSURI, "ATAlwaysCapability");
    private static final QName AT_ASSERTION = new QName(WSAT_SOAP_NSURI, "ATAssertion");
    private static final String AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI = "http://java.sun.com/xml/ns/wsit/2006/09/policy/transport/client";
    private static final QName AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION = new QName(AUTO_OPTIMIZED_TRANSPORT_POLICY_NAMESPACE_URI, "AutomaticallySelectOptimalTransport");
    
    //default security pipe classes for XWSS 2.0 Style Security Configuration Support
    private static final String xwss20ClientPipe = "com.sun.xml.xwss.XWSSClientPipe";
    private static final String xwss20ServerPipe = "com.sun.xml.xwss.XWSSServerPipe";
    
    private static class WsitPipelineAssembler implements PipelineAssembler {
        private BindingID bindingId;
        
        WsitPipelineAssembler(final BindingID bindingId) {
            this.bindingId = bindingId;
        }
        
        @NotNull
        public Pipe createClient(@NotNull ClientPipeAssemblerContext context) {
            PolicyMap policyMap = initPolicyMap(context);
            boolean isSecurityEnabled = isSecurityEnabled(policyMap, context.getWsdlModel());
            if (isSecurityEnabled) {
                setSecurityCodec(context);
            }
            // Transport pipe ALWAYS exist
            Pipe p;
            if (isOptimizedTransportEnabled(policyMap, context.getWsdlModel())) {
                p = TCPTransportPipeFactory.doCreate(context, false);
            } else {
                p = context.createTransportPipe();
            }
            p = dump(context, CLIENT_PREFIX, p);
            p = dumpAction(CLIENT_PREFIX + ACTION_SUFFIX, context.getBinding(), p);
            p = dump(context, CLIENT_PREFIX + TRANSPORT_SUFFIX, p);
            
            p = dump(context, CLIENT_PREFIX + WSS_SUFFIX + AFTER_SUFFIX, p);
            
            // check for Security
            SecurityClientPipe securityClientPipe = null;
            ClientPipelineHook hook = context.getContainer().getSPI(ClientPipelineHook.class);
            if (hook != null) {
                // TODO: how SecurityClientPipe will be passed to RMClientPipe. Currently SC+RM
                // TODO: will not work for JSR 109-based DD.
                // TODO: Vijay will follow with Ron if 196 & Policy-based pipe can be separate
                p = hook.createSecurityPipe(policyMap, context, p);
            } else {
                if (isSecurityEnabled) {
                    ClientPipeConfiguration config = new ClientPipeConfiguration(
                            policyMap, context.getWsdlModel(), context.getService(), context.getBinding());
                    p = new SecurityClientPipe(config, p);
                    securityClientPipe = (SecurityClientPipe) p;
                } else {
                    //look for XWSS 2.0 Style Security
                    if (policyMap.isEmpty() && isSecurityConfigPresent(context)) {
                        p = initializeXWSSClientPipe(context.getWsdlModel(), context.getService(), context.getBinding(), p);
                        //donot set securityClientPipe since this is a
                        // non WSIT scenario
                    }
                }
            }
            p = dump(context, CLIENT_PREFIX + WSS_SUFFIX + BEFORE_SUFFIX, p);
            
            // MEX pipe here
            
            p = dump(context, CLIENT_PREFIX + WSRM_SUFFIX + AFTER_SUFFIX, p);
            // check for WS-Reliable Messaging
            if (isReliableMessagingEnabled(policyMap, context.getWsdlModel())) {
                p = new RMClientPipe(context.getWsdlModel(),
                        context.getService(),
                        context.getBinding(),
                        securityClientPipe,
                        p);
            }
            p = dump(context, CLIENT_PREFIX + WSRM_SUFFIX + BEFORE_SUFFIX, p);
            
            p = dump(context, CLIENT_PREFIX + WSTX_SUFFIX + AFTER_SUFFIX, p);
            // check for WS-Atomic Transactions
            if (isTransactionsEnabled(policyMap, context.getWsdlModel(), false)) {
                try {
                    Class c = Class.forName("com.sun.xml.ws.tx.client.TxClientPipe");
                    Constructor ctor = c.getConstructor(ClientPipeConfiguration.class, Pipe.class);
                    p = (Pipe) ctor.newInstance(new ClientPipeConfiguration(policyMap,
                            context.getWsdlModel(),
                            context.getService(),
                            context.getBinding()),
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
            p = dump(context, CLIENT_PREFIX + WSTX_SUFFIX + BEFORE_SUFFIX, p);
            
            p = context.createClientMUPipe(p);
            p = context.createHandlerPipe(p);
            
            p = dump(context, CLIENT_PREFIX + WSA_SUFFIX + AFTER_SUFFIX, p);
            // check for WS-Addressing
            if (isAddressingEnabled(policyMap, context.getWsdlModel(), context.getBinding())) {
                p = context.createWsaPipe(p);
            }
            p = dump(context, CLIENT_PREFIX + WSA_SUFFIX + BEFORE_SUFFIX, p);
            
            return p;
        }
        
        @NotNull
        public Pipe createServer(ServerPipeAssemblerContext context) {
            context.getEndpoint().getServiceDefinition().addFilter(new PrivateAssertionFilter());
            PolicyMap policyMap = initPolicyMap(context);
            
            Pipe p = context.getTerminalPipe();
            p = context.createHandlerPipe(p);
            p = context.createServerMUPipe(p);
            p = context.createMonitoringPipe(p);
            
            p = dump(context, SERVER_PREFIX + WSTX_SUFFIX + AFTER_SUFFIX, p);
            // check for WS-Atomic Transactions
            if (isTransactionsEnabled(policyMap, context.getWsdlModel(), true)) {
                try {
                    Class c = Class.forName("com.sun.xml.ws.tx.service.TxServerPipe");
                    Constructor ctor = c.getConstructor(WSDLPort.class, PolicyMap.class, Pipe.class);
                    p = (Pipe) ctor.newInstance(context.getWsdlModel(), policyMap, p);
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
            p = dump(context, SERVER_PREFIX + WSTX_SUFFIX + BEFORE_SUFFIX, p);
            
            p = dump(context, SERVER_PREFIX + WSRM_SUFFIX + AFTER_SUFFIX, p);
            // check for WS-Reliable Messaging
            if (isReliableMessagingEnabled(policyMap, context.getWsdlModel())) {
                p = new RMServerPipe(context.getWsdlModel(), context.getEndpoint(), p);
            }
            p = dump(context, SERVER_PREFIX + WSRM_SUFFIX + BEFORE_SUFFIX, p);
            
            p = dump(context, SERVER_PREFIX + WSMEX_SUFFIX + AFTER_SUFFIX, p);
            // MEX pipe here
            p = new MetadataServerPipe(context.getEndpoint(), p);
            p = dump(context, SERVER_PREFIX + WSMEX_SUFFIX + BEFORE_SUFFIX, p);
            
            p = dump(context, SERVER_PREFIX + WSA_SUFFIX + AFTER_SUFFIX, p);
            // check for WS-Addressing
            if (isAddressingEnabled(policyMap, context.getWsdlModel(), context.getEndpoint().getBinding())) {
                p = context.createWsaPipe(p);
            }
            p = dump(context, SERVER_PREFIX + WSA_SUFFIX + BEFORE_SUFFIX, p);
            
            p = dump(context, SERVER_PREFIX + WSS_SUFFIX + AFTER_SUFFIX, p);
            // check for Security
            ServerPipelineHook hook = context.getEndpoint().getContainer().getSPI(ServerPipelineHook.class);
            if (hook != null){
                setSecurityCodec(context);
                p = hook.createSecurityPipe(policyMap, context.getSEIModel(), context.getWsdlModel(), context.getEndpoint(), p);
            }else {
                if (isSecurityEnabled(policyMap, context.getWsdlModel())) {
                    setSecurityCodec(context);
                    ServerPipeConfiguration config = new ServerPipeConfiguration(
                            policyMap, context.getWsdlModel(), context.getEndpoint());
                    p = new SecurityServerPipe(config, p);
                } else {
                    //look for XWSS 2.0 Style Security
                    if (policyMap.isEmpty() && isSecurityConfigPresent(context)) {
                        p = initializeXWSSServerPipe(context.getEndpoint(), context.getWsdlModel(), p);
                    }
                }
            }
            p = dump(context, SERVER_PREFIX + WSS_SUFFIX + BEFORE_SUFFIX, p);
            
            p = dump(context, SERVER_PREFIX + TRANSPORT_SUFFIX, p);
            p = dumpAction(SERVER_PREFIX + ACTION_SUFFIX, context.getEndpoint().getBinding(), p);
            p = dump(context, SERVER_PREFIX, p);
            
            return p;
        }
        
        private Pipe dump(ClientPipeAssemblerContext context, String name, Pipe p) {
            if (Boolean.getBoolean(name)) {
                context.createDumpPipe(name, System.out, p);
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
         * @param wsdlPort the WSDLPort object
         * @return true if OptimizedTransport is enabled, false otherwise
         */
        private boolean isOptimizedTransportEnabled(PolicyMap policyMap, WSDLPort port) {
            String schema = port.getAddress().getURI().getScheme();
            // if target endpoint URI starts with TCP schema - dont check policies, just return true
            if (com.sun.xml.ws.transport.tcp.util.TCPConstants.PROTOCOL_SCHEMA.equals(schema))
                return true;
            
            if (policyMap == null)
                return false;
            
            try {
                PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(port.getOwner().getName(),
                        port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
                
                if (policy != null && policy.contains(TCPConstants.TCPTRANSPORT_POLICY_ASSERTION) &&
                        policy.contains(AUTO_OPTIMIZED_TRANSPORT_POLICY_ASSERTION)) {
                    /* if client set to choose optimal transport and server has TCP transport policy
                       then need to check server side policy "enabled" attribute*/
                    for(AssertionSet assertionSet : policy) {
                        for(PolicyAssertion assertion : assertionSet) {
                            if(assertion.getName().equals(TCPConstants.TCPTRANSPORT_POLICY_ASSERTION)){
                                String value = assertion.getAttributeValue(new QName("enabled"));
                                if (value == null) return false;
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
         * Checks to see whether WS-Atomic Transactions are enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param wsdlPort the WSDLPort object
         * @param isServerSide true iff this method is being called from {@link PipelineAssembler#createServer(ServerPipeAssemblerContext)}
         * @return true if Transactions is enabled, false otherwise
         */
        private boolean isTransactionsEnabled(PolicyMap policyMap, WSDLPort wsdlPort, boolean isServerSide) {
            if (policyMap == null) {
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
         * Checks to see whether WS-ReliableMessaging is enabled or not.
         *
         * @param policyMap policy map for {@link this} assembler
         * @param port wsdl:port
         * @return true if ReliableMessaging is enabled, false otherwise
         */
        private boolean isReliableMessagingEnabled(PolicyMap policyMap, WSDLPort port) {
            if (policyMap == null)
                return false;
            
            try {
                PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(port.getOwner().getName(),
                        port.getName());
                Policy policy = policyMap.getEndpointEffectivePolicy(endpointKey);
                
                return (policy != null) && policy.contains(Constants.version);
            } catch (PolicyException e) {
                throw new WebServiceException(e);
            }
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
            if (AddressingVersion.isEnabled(binding))
                return true;
            
            if (null == policyMap)
                return false;
            
            try {
                PolicyMapKey endpointKey = policyMap.createWsdlEndpointScopeKey(port.getOwner().getName(),
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
                            wsdlPort.getName(),
                            wbo.getName());
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
        
        private Pipe dumpAction(String name, WSBinding binding, Pipe p) {
            if (Boolean.getBoolean(name)) {
                ServiceFinder<ActionDumpPipe> pipes = ServiceFinder.find(ActionDumpPipe.class);
                if (pipes != null) {
                    if (pipes.toArray().length > 0) {
                        return pipes.toArray()[0];
                    }
                }
                
                return new ActionDumpPipe(name, binding, p);
            }
            
            return p;
        }
        
        /**
         * Initializes the {@link PolicyMap} on the client side.
         *
         * @param context client assembler context
         * @return policy map
         */
        private PolicyMap initPolicyMap(ClientPipeAssemblerContext context) {
            PolicyMap map = null;
            
            WSDLPort wsdlPort = context.getWsdlModel();
            // In dispatch mode, wsdlPort is null
            if (wsdlPort != null) {
                WSDLModel model = wsdlPort.getBinding().getOwner();
                WSDLPolicyMapWrapper mapWrapper = model.getExtension(WSDLPolicyMapWrapper.class);
                if (mapWrapper != null) {
                    map = mapWrapper.getPolicyMap();
                }
            }
            
            return map;
        }
        
        /**
         * Initializes the {@link PolicyMap} on the server side.
         *
         * @param context server assembler context
         * @return initialized policy map
         */
        private PolicyMap initPolicyMap(ServerPipeAssemblerContext context) throws WebServiceException {
            PolicyMap map = null;
            
            WSDLPort wsdlPort = context.getWsdlModel();
            WSDLModel model = wsdlPort.getBinding().getOwner();
            WSDLPolicyMapWrapper mapWrapper = model.getExtension(WSDLPolicyMapWrapper.class);
            if (mapWrapper != null) {
                map = mapWrapper.getPolicyMap();
            }
            
            if (map != null) {
                try {
                    PolicyMapUtil.rejectAlternatives(map);
                } catch (PolicyException e) {
                    throw new WebServiceException(e);
                }
            }
            
            return map;
        }
        
        private void setSecurityCodec(ServerPipeAssemblerContext context){
            StreamSOAPCodec primaryCodec = Codecs.createSOAPEnvelopeXmlCodec(context.getEndpoint().getBinding().getSOAPVersion());
            LazyStreamCodec lsc = new LazyStreamCodec(primaryCodec);
            Codec fullCodec = Codecs.createSOAPBindingCodec(context.getEndpoint().getBinding(),lsc);
            context.setCodec(fullCodec);
        }
        
        private void setSecurityCodec(ClientPipeAssemblerContext context){
            StreamSOAPCodec primaryCodec = Codecs.createSOAPEnvelopeXmlCodec(context.getBinding().getSOAPVersion());
            LazyStreamCodec lsc = new LazyStreamCodec(primaryCodec);
            Codec fullCodec = Codecs.createSOAPBindingCodec(context.getBinding(),lsc);
            context.setCodec(fullCodec);
        }
        
    }
    
    public PipelineAssembler doCreate(final BindingID bindingId) {
        return new WsitPipelineAssembler(bindingId);
    }
    
    private static boolean isSecurityConfigPresent(ClientPipeAssemblerContext context) {
        //returning true by default for now, because the Client Side Security Config is
        //only accessible as a Runtime Property on BindingProvider.RequestContext
        return true;
    }
    
    private static boolean isSecurityConfigPresent(ServerPipeAssemblerContext context) {
        
        QName serviceQName = context.getEndpoint().getServiceName();
        QName portQName = context.getEndpoint().getPortName();
        //TODO: not sure which of the two above will give the service name as specified in DD
        String serviceLocalName = serviceQName.getLocalPart();
        
        ServletContext ctxt = context.getEndpoint().getContainer().getSPI(ServletContext.class);
        if (ctxt == null) {
            return false;
        }
       
        String serverName = "server";
        String serverConfig = "/WEB-INF/" + serverName + "_" + "security_config.xml";
        InputStream in = ctxt.getResourceAsStream(serverConfig);
        
        if (in == null) {
            serverConfig = "/WEB-INF/" + serviceLocalName + "_" + "security_config.xml";
            in = ctxt.getResourceAsStream(serverConfig);
        }
        
        if (in != null) {
            return true;
        }
        
        return false;
    }
    
    private static Pipe initializeXWSSClientPipe(WSDLPort prt, WSService svc, WSBinding bnd, Pipe nextP) {
        Pipe ret = new com.sun.xml.xwss.XWSSClientPipe(prt,svc, bnd, nextP);
        return ret;
    }
    
    private static Pipe initializeXWSSServerPipe(WSEndpoint epoint, WSDLPort prt, Pipe nextP) {
        Pipe ret = new com.sun.xml.xwss.XWSSServerPipe(epoint, prt, nextP);
        return ret;
    }
    
}
