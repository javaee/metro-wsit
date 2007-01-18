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

package com.sun.xml.ws.transport.tcp.server.glassfish;

import com.sun.enterprise.webservice.EjbRuntimeEndpointInfo;
import com.sun.enterprise.webservice.WebServiceEjbEndpointRegistry;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class AppServWSRegistry {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static final AppServWSRegistry instance = new AppServWSRegistry();
    
    private final Map<String, Map<String, WSEndpointDescriptor>> registry;
    
    public static AppServWSRegistry getInstance() {
        return instance;
    }
    
    private AppServWSRegistry() {
        registry = new HashMap<String, Map<String, WSEndpointDescriptor>>();
        final WSEndpointLifeCycleListener lifecycleListener = new WSEndpointLifeCycleListener();
        
        final WebServiceEngine engine = WebServiceEngineFactory.getInstance().getEngine();
        engine.addLifecycleListener(lifecycleListener);
        
        populateEndpoints(engine);
    }
    
    /**
     * Populate currently registered WS Endpoints and register them
     */
    private void populateEndpoints(@NotNull final WebServiceEngine engine) {
        final Iterator<Endpoint> endpoints = engine.getEndpoints();
        while(endpoints.hasNext()) {
            registerEndpoint(endpoints.next());
        }
    }
    
    /**
     * Lookup endpoint's decriptor in registry
     */
    public @Nullable WSEndpointDescriptor get(@NotNull final String wsServiceName, @NotNull final String endpointName) {
        final Map<String, WSEndpointDescriptor> endpointMap = registry.get(wsServiceName);
        if (endpointMap != null) {
            return endpointMap.get(endpointName);
        }
        
        return null;
    }
    
    /**
     * Method is used by WS invoker to clear some EJB invoker state ???
     */
    public @NotNull EjbRuntimeEndpointInfo getEjbRuntimeEndpointInfo(@NotNull final String service,
            @NotNull final String endpointName) {
        
        final WSEndpointDescriptor wsEndpointDescriptor = get(service, endpointName);
        EjbRuntimeEndpointInfo endpointInfo = null;
        
        if (wsEndpointDescriptor.isEJB()) {
            endpointInfo = (EjbRuntimeEndpointInfo) WebServiceEjbEndpointRegistry.
                    getRegistry().getEjbWebServiceEndpoint(wsEndpointDescriptor.getURI(), "POST", null);
        }
        
        return endpointInfo;
    }
    
    /**
     * Register new WS Endpoint
     */
    protected void registerEndpoint(@NotNull final Endpoint endpoint) {
        final WebServiceEndpoint wsServiceDescriptor = endpoint.getDescriptor();
        
        if(wsServiceDescriptor != null && isTCPEnabled(wsServiceDescriptor)) {
            final String contextRoot = getEndpointContextRoot(wsServiceDescriptor);
            final String urlPattern = getEndpointUrlPattern(wsServiceDescriptor);
            
            // ContextRoot could be represented as leading slash or without (GF API changes from time to time)
            // So we use slashed version for registries
            final String slashedContextRoot = ensureSlash(contextRoot);
            final String slashedUrlPattern = ensureSlash(urlPattern);
            
            final String path = slashedContextRoot + slashedUrlPattern;
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1110_APP_SERV_REG_REGISTER_ENDPOINT(
                        wsServiceDescriptor.getServiceName(), path, wsServiceDescriptor.implementedByEjbComponent()));
            }
            final WSEndpointDescriptor descriptor = new WSEndpointDescriptor(wsServiceDescriptor,
                    contextRoot,
                    urlPattern,
                    endpoint.getEndpointSelector());
            addToRegistry(slashedContextRoot, slashedUrlPattern, descriptor);
        }
    }
    
    /**
     * Deregister WS Endpoint
     */
    protected void deregisterEndpoint(@NotNull final Endpoint endpoint) {
        final WebServiceEndpoint wsServiceDescriptor = endpoint.getDescriptor();
        final String contextRoot = getEndpointContextRoot(wsServiceDescriptor);
        final String urlPattern = getEndpointUrlPattern(wsServiceDescriptor);
        
        // ContextRoot could be represented as leading slash or without (GF API changes from time to time)
        // So we use slashed version for registries
        final String slashedContextRoot = ensureSlash(contextRoot);
        final String slashedUrlPattern = ensureSlash(urlPattern);

        final String path = slashedContextRoot + slashedUrlPattern;
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1111_APP_SERV_REG_DEREGISTER_ENDPOINT(
                    wsServiceDescriptor.getWebService().getName(),
                    path, wsServiceDescriptor.implementedByEjbComponent()));
        }
        removeFromRegistry(slashedContextRoot, slashedUrlPattern);
        WSTCPAdapterRegistryImpl.getInstance().deleteTargetFor(path);
    }
    
    private void addToRegistry(@NotNull String contextRoot,
            @NotNull String urlPattern,
    @NotNull final WSEndpointDescriptor wsDescriptor) {
        
        contextRoot = ensureSlash(contextRoot);
        urlPattern = ensureSlash(urlPattern);
        Map<String, WSEndpointDescriptor> endpointMap = registry.get(contextRoot);
        if (endpointMap == null) {
            endpointMap = new HashMap<String, WSEndpointDescriptor>();
            registry.put(contextRoot, endpointMap);
        }
        
        endpointMap.put(urlPattern, wsDescriptor);
    }
    
    private WSEndpointDescriptor removeFromRegistry(@NotNull final String wsServiceName,
            @NotNull final String endpointName) {
        final Map<String, WSEndpointDescriptor> endpointMap = registry.get(wsServiceName);
        if (endpointMap != null) {
            return endpointMap.remove(endpointName);
        }
        
        return null;
    }
        
    private @NotNull String getEndpointContextRoot(@NotNull final WebServiceEndpoint wsServiceDescriptor) {
        String contextRoot;
        if(!wsServiceDescriptor.implementedByEjbComponent()) {
            contextRoot = wsServiceDescriptor.getWebComponentImpl().
                    getWebBundleDescriptor().getContextRoot();
            logger.log(Level.FINE, MessagesMessages.WSTCP_1112_APP_SERV_REG_GET_ENDP_CR_NON_EJB(contextRoot));
        } else {
            final String[] path = wsServiceDescriptor.getEndpointAddressUri().split("/");
            contextRoot = "/" + path[1];
            logger.log(Level.FINE, MessagesMessages.WSTCP_1113_APP_SERV_REG_GET_ENDP_CR_EJB(contextRoot));
        }
        
        return contextRoot;
    }
    
    private @NotNull String getEndpointUrlPattern(@NotNull final WebServiceEndpoint wsServiceDescriptor) {
        String urlPattern;
        if(!wsServiceDescriptor.implementedByEjbComponent()) {
            urlPattern = wsServiceDescriptor.getEndpointAddressUri();
            logger.log(Level.FINE, MessagesMessages.WSTCP_1114_APP_SERV_REG_GET_ENDP_URL_PATTERN_NON_EJB(urlPattern));
        } else {
            final String[] path = wsServiceDescriptor.getEndpointAddressUri().split("/");
            if (path.length < 3) {
                return "";
            }
            
            urlPattern = "/" + path[2];
            logger.log(Level.FINE, MessagesMessages.WSTCP_1115_APP_SERV_REG_GET_ENDP_URL_PATTERN_EJB(urlPattern));
        }
        
        return urlPattern;
    }

    private @Nullable String ensureSlash(@Nullable String s) {
        if (s != null && s.length() > 0 && s.charAt(0) != '/') {
            return "/" + s;
        }
        
        return s;
    }
    
    private boolean isTCPEnabled(final com.sun.enterprise.deployment.WebServiceEndpoint webServiceDesc) {
        return true;
    }
}
