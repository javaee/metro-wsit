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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public class AppServWSRegistry {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static AppServWSRegistry instance = new AppServWSRegistry();
    
    private Map<String, Map<String, WSEndpointDescriptor>> registry;
    
    public static AppServWSRegistry getInstance() {
        return instance;
    }
    
    private AppServWSRegistry() {
        registry = new HashMap<String, Map<String, WSEndpointDescriptor>>();
        WSEndpointLifeCycleListener lifecycleListener = new WSEndpointLifeCycleListener();
        
        WebServiceEngine engine = WebServiceEngineFactory.getInstance().getEngine();
        engine.addLifecycleListener(lifecycleListener);
        
        populateEndpoints(engine);
    }
    
    /**
     * Populate currently registered WS Endpoints and register them
     */
    private void populateEndpoints(@NotNull WebServiceEngine engine) {
        Iterator<Endpoint> endpoints = engine.getEndpoints();
        while(endpoints.hasNext()) {
            registerEndpoint(endpoints.next());
        }
    }
    
    /**
     * Lookup endpoint's decriptor in registry
     */
    public @Nullable WSEndpointDescriptor get(@NotNull String wsServiceName, @NotNull String endpointName) {
        Map<String, WSEndpointDescriptor> endpointMap = registry.get(wsServiceName);
        if (endpointMap != null) {
            return endpointMap.get(endpointName);
        }
        
        return null;
    }
    
    /**
     * Method is used by WS invoker to clear some EJB invoker state ???
     */
    public @NotNull EjbRuntimeEndpointInfo getEjbRuntimeEndpointInfo(@NotNull String service,
            @NotNull String endpointName) {
        
        WSEndpointDescriptor wsEndpointDescriptor = get(service, endpointName);
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
    protected void registerEndpoint(@NotNull Endpoint endpoint) {
        WebServiceEndpoint wsServiceDescriptor = endpoint.getDescriptor();
        
        if(wsServiceDescriptor != null && isTCPEnabled(wsServiceDescriptor)) {
            String endpointName = getEndpointName(wsServiceDescriptor);
            
            boolean ejbType = wsServiceDescriptor.implementedByEjbComponent();
            
            String contextRoot = getEndpointContextRoot(wsServiceDescriptor);
            String urlPattern = getEndpointUrlPattern(wsServiceDescriptor);
            String path = contextRoot + urlPattern;
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "AppServWSRegistry.registerEndpoint: ServiceName: {0} path: {1} isEJB: {2}",
                        new Object[] {wsServiceDescriptor.getServiceName(), path, wsServiceDescriptor.implementedByEjbComponent()});
            }
            WSEndpointDescriptor descriptor = new WSEndpointDescriptor(wsServiceDescriptor,
                    contextRoot,
                    urlPattern,
                    endpoint.getEndpointSelector());
            addToRegistry(contextRoot, urlPattern, descriptor);
        }
    }
    
    /**
     * Deregister WS Endpoint
     */
    protected void deregisterEndpoint(@NotNull Endpoint endpoint) {
        WebServiceEndpoint wsServiceDescriptor = endpoint.getDescriptor();
        String contextRoot = getEndpointContextRoot(wsServiceDescriptor);
        String urlPattern = getEndpointUrlPattern(wsServiceDescriptor);
        
        String path = contextRoot + urlPattern;
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "AppServWSRegistry.deregisterEndpoint: ServiceName: {0}" +
                    " path: {1} isEJB: {2}",
                    new Object[] {wsServiceDescriptor.getWebService().getName(),
                    path, wsServiceDescriptor.implementedByEjbComponent()});
        }
        removeFromRegistry(contextRoot, urlPattern);
        WSTCPAdapterRegistryImpl.getInstance().deleteTargetFor(path);
    }
    
    private void addToRegistry(@NotNull String contextRoot,
            @NotNull String urlPattern,
    @NotNull WSEndpointDescriptor wsDescriptor) {
        
        Map<String, WSEndpointDescriptor> endpointMap = registry.get(contextRoot);
        if (endpointMap == null) {
            endpointMap = new HashMap<String, WSEndpointDescriptor>();
            registry.put(contextRoot, endpointMap);
        }
        
        endpointMap.put(urlPattern, wsDescriptor);
    }
    
    private WSEndpointDescriptor removeFromRegistry(@NotNull String wsServiceName,
            @NotNull String endpointName) {
        Map<String, WSEndpointDescriptor> endpointMap = registry.get(wsServiceName);
        if (endpointMap != null) {
            return endpointMap.remove(endpointName);
        }
        
        return null;
    }
    
    private @NotNull String getEndpointName(@NotNull WebServiceEndpoint wsServiceDescriptor) {
//            String endpointName = wsServiceDescriptor.hasWsdlPort() ? wsServiceDescriptor.getWsdlPort().getLocalPart() : wsServiceDescriptor.getEndpointName();
        return wsServiceDescriptor.getEndpointName();
    }
    
    private @NotNull String getEndpointContextRoot(@NotNull WebServiceEndpoint wsServiceDescriptor) {
        String contextRoot;
        if(!wsServiceDescriptor.implementedByEjbComponent()) {
            contextRoot = "/" +
                    wsServiceDescriptor.getWebComponentImpl().
                    getWebBundleDescriptor().getContextRoot();
            logger.log(Level.FINE, "AppServWSRegistry.getEndpointContextRoot nonEJB WS. ContextRoot: {0}", contextRoot);
        } else {
            logger.log(Level.FINE, "AppServWSRegistry.getEndpointContextRoot EJB WS. ContextRoot: {0}", wsServiceDescriptor.getEndpointAddressUri());
            String[] path = wsServiceDescriptor.getEndpointAddressUri().split("/");
            contextRoot = "/" + path[1];
        }
        
        return contextRoot;
    }
    
    private @NotNull String getEndpointUrlPattern(@NotNull WebServiceEndpoint wsServiceDescriptor) {
        String urlPattern;
        if(!wsServiceDescriptor.implementedByEjbComponent()) {
            urlPattern = wsServiceDescriptor.getEndpointAddressUri();
            logger.log(Level.FINE, "AppServWSRegistry.getEndpointUrlPattern nonEJB WS. URLPattern: {0}", urlPattern);
        } else {
            logger.log(Level.FINE, "AppServWSRegistry.getEndpointUrlPattern EJB WS. URLPattern: {0}", wsServiceDescriptor.getEndpointAddressUri());
            String[] path = wsServiceDescriptor.getEndpointAddressUri().split("/");
            if (path.length < 3) {
                return "";
            }
            
            urlPattern = "/" + path[2];
        }
        
        return urlPattern;
    }
    private boolean isTCPEnabled(com.sun.enterprise.deployment.WebServiceEndpoint webServiceDesc) {
        return true;
    }
}
