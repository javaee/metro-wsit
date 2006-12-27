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

import com.sun.enterprise.webservice.JAXWSAdapterRegistry;
import com.sun.enterprise.webservice.EjbRuntimeEndpointInfo;
import com.sun.enterprise.webservice.WebServiceEjbEndpointRegistry;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.server.WSTCPAdapterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class WSTCPAdapterRegistryImpl implements WSTCPAdapterRegistry {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    /**
     * Registry holds correspondents between service name and adapter
     */
    final Map<String, TCPAdapter> registry = new ConcurrentHashMap<String, TCPAdapter>();
    private static final WSTCPAdapterRegistryImpl instance = new WSTCPAdapterRegistryImpl();
    
    private WSTCPAdapterRegistryImpl() {}
    
    public static @NotNull WSTCPAdapterRegistryImpl getInstance() {
        return instance;
    }
    
    public TCPAdapter getTarget(@NotNull final WSTCPURI requestURI) {
        // path should have format like "/context-root/url-pattern"
        final int delim = requestURI.path.lastIndexOf('/');
        final String contextRoot = requestURI.path.substring(0, delim);
        final String urlPattern = requestURI.path.substring(delim, requestURI.path.length());
        
        if (contextRoot != null && urlPattern != null) {
            final WSEndpointDescriptor wsEndpointDescriptor = AppServWSRegistry.getInstance().get(contextRoot, urlPattern);
            if (wsEndpointDescriptor != null) {
                TCPAdapter adapter = registry.get(requestURI.path);
                if (adapter == null) {
                    try {
                        adapter = createWSAdapter(wsEndpointDescriptor);
                        registry.put(requestURI.path, adapter);
                        logger.log(Level.FINE, "WSTCPAdapterRegistryImpl. Register adapter. Path: {0}", requestURI.path);
                    } catch (Exception e) {
                        // This common exception is thrown from ejbEndPtInfo.prepareInvocation(true)
                        logger.log(Level.SEVERE, "WSTCPAdapterRegistryImpl. " + 
                                MessagesMessages.WSTCP_0008_ERROR_TCP_ADAPTER_CREATE(
                                wsEndpointDescriptor.getWSServiceName()), e);
                    }
                }
                return adapter;
            }
        }
        
        return null;
    }
    
    
    public void deleteTargetFor(@NotNull final String path) {
        logger.log(Level.FINE, "WSTCPAdapterRegistryImpl. DeRegister adapter for {0}", path);
        registry.remove(path);
    }
    
    private TCPAdapter createWSAdapter(@NotNull final WSEndpointDescriptor wsEndpointDescriptor) throws Exception {
        Adapter adapter;
        if (wsEndpointDescriptor.isEJB()) {
            final EjbRuntimeEndpointInfo ejbEndPtInfo = (EjbRuntimeEndpointInfo) WebServiceEjbEndpointRegistry.getRegistry().
                    getEjbWebServiceEndpoint(wsEndpointDescriptor.getURI(), "POST", null);
            adapter = (Adapter) ejbEndPtInfo.prepareInvocation(true);
        } else {
            final String uri = wsEndpointDescriptor.getURI();
            adapter = JAXWSAdapterRegistry.getInstance().getAdapter(wsEndpointDescriptor.getContextRoot(), uri, uri);
        }
        
//@TODO implement checkAdapterSupportsTCP
//        checkAdapterSupportsTCP(adapter);
        final TCPAdapter tcpAdapter = new TCP109Adapter(wsEndpointDescriptor.getWSServiceName().toString(),
                wsEndpointDescriptor.getContextRoot(),
                wsEndpointDescriptor.getUrlPattern(),
                adapter.getEndpoint(),
                new ServletFakeArtifactSet(wsEndpointDescriptor.getRequestURL()),
                wsEndpointDescriptor.isEJB());
        
        return tcpAdapter;
    }
}
