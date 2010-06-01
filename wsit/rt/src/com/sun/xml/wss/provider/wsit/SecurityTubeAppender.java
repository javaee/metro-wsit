/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.wss.provider.wsit;

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.wss.impl.misc.SecurityUtil;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.encoding.LazyStreamCodec;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.PipeAdapter;
import com.sun.xml.ws.assembler.ClientPipelineHook;
import com.sun.xml.ws.assembler.ServerPipelineHook;
import com.sun.xml.ws.assembler.TubeAppender;
import com.sun.xml.ws.assembler.TubelineAssemblyContextUpdater;
import com.sun.xml.ws.assembler.WsitClientTubeAssemblyContext;
import com.sun.xml.ws.assembler.WsitServerTubeAssemblyContext;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.security.opt.impl.util.JAXBUtil;
import com.sun.xml.ws.security.secconv.SecureConversationInitiator;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.util.ServiceConfigurationError;
import com.sun.xml.wss.jaxws.impl.SecurityClientTube;
import com.sun.xml.wss.jaxws.impl.SecurityServerTube;
import com.sun.xml.xwss.XWSSClientPipe;
import com.sun.xml.xwss.XWSSServerPipe;
import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.message.config.AuthConfigFactory;

public class SecurityTubeAppender implements TubeAppender, TubelineAssemblyContextUpdater {

    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    //Added for Security Pipe Unification with JSR 196 on GlassFish
    private static final String ENDPOINT = "ENDPOINT";
    private static final String NEXT_PIPE = "NEXT_PIPE";
    private static final String POLICY = "POLICY";
    private static final String SEI_MODEL = "SEI_MODEL";
    // private static final String SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
    private static final String WSDL_MODEL = "WSDL_MODEL";
    private static final String GF_SERVER_SEC_PIPE = "com.sun.enterprise.webservice.CommonServerSecurityPipe";

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
        //TEMP: uncomment this ServerPipelineHook hook = context.getEndpoint().getContainer().getSPI(ServerPipelineHook.class);
        ServerPipelineHook[] hooks = getServerTubeLineHooks();
        ServerPipelineHook hook = null;
        if (hooks != null && hooks.length > 0 && hooks[0] instanceof com.sun.xml.wss.provider.wsit.ServerPipeCreator) {
            //we let it override GF defaults
            hook = hooks[0];
            //set the Factory to JMACAuthConfigFactory if it is not already set to 
            //something else.
            initializeJMAC();
        } else {
            hook = context.getEndpoint().getContainer().getSPI(ServerPipelineHook.class);
        }

        if (hook != null) {
            // TODO ask security to implement the hook.createSecurityTube(context);
            Tube head = context.getTubelineHead();
            Tube securityTube = hook.createSecurityTube(context);
            if (head == securityTube) {
                //it means we have hit the Default BaseClass Impl (shown above)
                // so fall back to createSecurityPipe
                // This can happen if we are running on SALIFIN/9.1.1 or any previous Appserver
                Pipe securityPipe = hook.createSecurityPipe(
                        context.getPolicyMap(),
                        context.getSEIModel(),
                        context.getWsdlPort(),
                        context.getEndpoint(),
                        context.getAdaptedTubelineHead());
                securityTube = PipeAdapter.adapt(securityPipe);
            }
            return securityTube;
        } else if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
            if (hooks != null && hooks.length == 0) {
                return createSecurityTube(context);
            } else if (hooks != null && hooks.length > 0) {
                hook = hooks[0];
                Tube head = context.getTubelineHead();
                Tube securityTube = hook.createSecurityTube(context);
                if (head == securityTube) {
                    //it means we have hit the Default BaseClass Impl (shown above)
                    // so fall back to createSecurityPipe
                    // This can happen if we are running on SALIFIN/9.1.1 or any previous Appserver
                    Pipe securityPipe = hook.createSecurityPipe(
                            context.getPolicyMap(),
                            context.getSEIModel(),
                            context.getWsdlPort(),
                            context.getEndpoint(),
                            context.getAdaptedTubelineHead());
                    securityTube = PipeAdapter.adapt(securityPipe);
                }
                return securityTube;

            } else {
                //TODO: Log a FINE message indicating could not use Unified Tube.
                //return PipeAdapter.adapt(new SecurityServerPipe(context, context.getAdaptedTubelineHead()));
                return new SecurityServerTube(context, context.getTubelineHead());
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
        ClientPipelineHook hook = null;
        ClientPipelineHook[] hooks = getClientTublineHooks(context);
        if (context.getSEIModel() != null) {
            JAXBUtil.setSEIJAXBContext(context.getSEIModel().getJAXBContext());
        }
        if (hooks != null && hooks.length > 0) {
            for (ClientPipelineHook h : hooks) {
                if (h instanceof com.sun.xml.wss.provider.wsit.ClientPipeCreator) {
                    //give it preference
                    hook = h;
                    //set the Factory to JMACAuthConfigFactory if it is not already set to
                    //something else.
                    initializeJMAC();
                    break;
                }
            }
        }
        if (hook == null) {
            //Look for pipe-creation hook exposed in contaner.
            hook = context.getContainer().getSPI(ClientPipelineHook.class);
        }

        //If either mechanism for finding a ClientPipelineHook has found one, use it.
        if (hook != null) {
            Tube head = context.getTubelineHead();
            Tube securityTube = hook.createSecurityTube(context);
            if (head == securityTube) {
                //we have hit default baseclass impl
                //can happen with SailFin and GlassFish 9.x
                ClientPipeAssemblerContext pipeContext = new ClientPipeAssemblerContext(
                        context.getAddress(),
                        context.getWsdlPort(),
                        context.getService(),
                        context.getBinding(),
                        context.getContainer());
                Pipe securityPipe = hook.createSecurityPipe(context.getPolicyMap(), pipeContext, context.getAdaptedTubelineHead());
                if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
                    context.setScInitiator((SecureConversationInitiator) securityPipe);
                }
                securityTube = PipeAdapter.adapt(securityPipe);
            }
            return securityTube;
        } else if (isSecurityEnabled(context.getPolicyMap(), context.getWsdlPort())) {
            //Use the default WSIT Client Security Pipe
            //Pipe securityPipe = new SecurityClientPipe(context, context.getAdaptedTubelineHead());
            Tube securityTube = new SecurityClientTube(context, context.getTubelineHead());
            //context.setScInitiator((SecureConversationInitiator) securityPipe);
            context.setScInitiator((SecureConversationInitiator) securityTube);
            //return PipeAdapter.adapt(securityPipe);
            return securityTube;
        } else if (!context.isPolicyAvailable() && isSecurityConfigPresent(context)) {
            //look for XWSS 2.0 Style Security
            // policyMap may be null in case of client dispatch without a client config file
            return initializeXWSSClientTube(context);
        } else {
            return context.getTubelineHead();
        }
    }

    private ClientPipelineHook[] getClientTublineHooks(WsitClientTubeAssemblyContext context) {
        try {
            ClientPipelineHook[] hooks = loadSPs(ClientPipelineHook.class);
            if (hooks != null && hooks.length > 0) {
                return hooks;
            }
        } catch (ServiceConfigurationError ex) {
            if (ex.getCause() instanceof InstantiationException) {
                return new ClientPipelineHook[0];
            }
            return null;
        }
        return null;
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

    private Codec createSecurityCodec(WSBinding binding) {
        StreamSOAPCodec primaryCodec = Codecs.createSOAPEnvelopeXmlCodec(binding.getSOAPVersion());
        LazyStreamCodec lsc = new LazyStreamCodec(primaryCodec);
        return Codecs.createSOAPBindingCodec(binding, lsc);
    }

    private static <P> P[] loadSPs(final Class<P> svcClass) {
        return ServiceFinder.find(svcClass).toArray();
    }

    private ServerPipelineHook[] getServerTubeLineHooks() {
        // The ServerPipeline Hook in GF fails to create the Pipe because GF ServerPipeCreator does not have a
        // Default CTOR.
        //TODO: change this method impl later.
        try {
            ServerPipelineHook[] hooks = loadSPs(ServerPipelineHook.class);
            if (hooks != null && hooks.length > 0) {
                return hooks;
            }
        } catch (ServiceConfigurationError ex) {
            //workaround since GF ServerPipeCreator has no Default CTOR.
            if (ex.getCause() instanceof InstantiationException) {
                return new ServerPipelineHook[0];
            }
            return null;
        }
        return null;
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

    @SuppressWarnings("unchecked")
    private void initializeJMAC() {
        // define default factory if it is not already defined
        // factory will be constructed on first getFactory call.
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                /*String defaultFactory = Security.getProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY);
                if (defaultFactory == null || !(JMACAuthConfigFactory.class.getName().equals(defaultFactory))) {
                Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY,
                JMACAuthConfigFactory.class.getName());
                }*/
                AuthConfigFactory factory = AuthConfigFactory.getFactory();
                if (factory == null || !(factory instanceof JMACAuthConfigFactory)) {
                    AuthConfigFactory.setFactory(new JMACAuthConfigFactory(loader));
                }
                return null; // nothing to return
            }
            });
    }
    }
