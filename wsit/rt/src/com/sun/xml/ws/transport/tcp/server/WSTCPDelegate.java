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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelWSImpl;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.xml.sax.EntityResolver;

/**
 * @author Alexey Stashok
 */
public final class WSTCPDelegate implements WSTCPAdapterRegistry, TCPMessageListener {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private final Map<String, TCPAdapter> fixedUrlPatternEndpoints = new HashMap<String, TCPAdapter>();
    private final List<TCPAdapter> pathUrlPatternEndpoints = new ArrayList<TCPAdapter>();
    
    private TCPAdapter serviceChannelWSAdapter;
    
    /**
     * Custom registry, where its possible to delegate Adapter search
     */
    private WSTCPAdapterRegistry customWSRegistry;
    
    public WSTCPDelegate() {
    }
    
    public void setCustomWSRegistry(@NotNull final WSTCPAdapterRegistry customWSRegistry) {
        this.customWSRegistry = customWSRegistry;
    }
    
    public void registerAdapters(@NotNull final String contextPath,
            @NotNull final List<TCPAdapter> adapters) {
        
        for(TCPAdapter adapter : adapters)
            registerEndpointUrlPattern(contextPath, adapter);
    }
    
    public void freeAdapters(@NotNull final String contextPath,
            @NotNull final List<TCPAdapter> adapters) {
        
        for(TCPAdapter adapter : adapters) {
            final String urlPattern = contextPath + adapter.urlPattern;
            logger.log(Level.FINE, MessagesMessages.WSTCP_1100_WSTCP_DELEGATE_DEREGISTER_ADAPTER(urlPattern));
            
            if (fixedUrlPatternEndpoints.remove(urlPattern) == null) {
                pathUrlPatternEndpoints.remove(adapter);
            }
        }
    }
    
    private void registerEndpointUrlPattern(@NotNull final String contextPath,
            @NotNull final TCPAdapter adapter) {
        
        final String urlPattern = contextPath + adapter.urlPattern;
        logger.log(Level.FINE, MessagesMessages.WSTCP_1101_WSTCP_DELEGATE_REGISTER_ADAPTER(urlPattern));
        
        if (urlPattern.endsWith("/*")) {
            pathUrlPatternEndpoints.add(adapter);
        } else {
            if (fixedUrlPatternEndpoints.containsKey(urlPattern)) {
                // Warning because of duplication
            } else {
                fixedUrlPatternEndpoints.put(urlPattern, adapter);
            }
        }
    }
    
    /**
     * Determines which {@link TCPAdapter} serves the given request.
     */
    public @Nullable TCPAdapter getTarget(@NotNull final WSTCPURI tcpURI) {
        TCPAdapter result = null;
        final String path = tcpURI.path;
        if (path != null) {
            result = fixedUrlPatternEndpoints.get(path);
            if (result == null) {
                for (TCPAdapter candidate : pathUrlPatternEndpoints) {
                    if (path.startsWith(candidate.getValidPath())) {
                        result = candidate;
                        break;
                    }
                }
            }
        }
        
        if (result ==  null && customWSRegistry != null) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1102_WSTCP_DELEGATE_GOING_TO_CUSTOM_REG(tcpURI));
            return customWSRegistry.getTarget(tcpURI);
        }
        
        return result;
    }
    
    /**
     * Implementation of TCPMessageListener.onMessage
     * method is called once request message come
     */
    public void onMessage(@NotNull final ChannelContext channelContext) {
        if (logger.isLoggable(Level.FINE)) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.FINE, MessagesMessages.WSTCP_1103_WSTCP_DELEGATE_ON_MESSAGE(connection.getHost(), connection.getPort(),
                    connection.getLocalHost(), connection.getLocalPort()));
        }
        try {
            TCPAdapter target = null;
            if (channelContext.getChannelId() > 0) {
                final WSTCPURI tcpURI = channelContext.getTargetWSURI();
                target = getTarget(tcpURI);
            } else {
                target = getServiceChannelWSAdapter();
            }
            
            if (target != null) {
                target.handle(channelContext);
            } else {
                TCPAdapter.sendErrorResponse(channelContext, TCPConstants.RS_NOT_FOUND, MessagesMessages.WSTCP_0003_TARGET_WS_NOT_FOUND());
            }
            
        } catch (JAXWSExceptionBase e) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0023_TARGET_EXEC_ERROR(connection.getHost(), connection.getPort()), e);
            
            try {
                TCPAdapter.sendErrorResponse(channelContext, TCPConstants.RS_INTERNAL_SERVER_ERROR, MessagesMessages.WSTCP_0004_CHECK_SERVER_LOG());
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, MessagesMessages.WSTCP_0002_SERVER_ERROR_MESSAGE_SENDING_FAILED(), ex);
            }
        } catch (Throwable e) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0023_TARGET_EXEC_ERROR(connection.getHost(), connection.getPort()), e);

            try {
                TCPAdapter.sendErrorResponse(channelContext, TCPConstants.RS_INTERNAL_SERVER_ERROR, MessagesMessages.WSTCP_0004_CHECK_SERVER_LOG());
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, MessagesMessages.WSTCP_0002_SERVER_ERROR_MESSAGE_SENDING_FAILED(), ex);
            }
        } finally {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1104_WSTCP_DELEGATE_ON_MESSAGE_COMPLETED());
        }
    }
    
    public void destroy() {
        logger.log(Level.FINE, MessagesMessages.WSTCP_1105_WSTCP_DELEGATE_DESTROY());
    }
    
    /**
     * Returns TCPAdapter for service channel
     * cannot do that once in constructor because of GF startup performance
     * initialize it lazy
     */
    private @NotNull TCPAdapter getServiceChannelWSAdapter() throws Exception {
        synchronized(this) {
            if (serviceChannelWSAdapter == null) {
                registerServiceChannelWSAdapter();
            }
        }
        
        return serviceChannelWSAdapter;
    }
    
    private void registerServiceChannelWSAdapter() throws Exception {
        final QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
        final QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
        final BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
        final WSBinding binding = bindingId.createBinding();
        
        final WSEndpoint<ServiceChannelWSImpl> endpoint = WSEndpoint.create(
                ServiceChannelWSImpl.class, true,
                InstanceResolver.createSingleton(ServiceChannelWSImpl.class.newInstance()).createInvoker(),
                serviceName, portName, null, binding,
                null, null, (EntityResolver) null, true
                );
        
        final String serviceNameLocal = serviceName.getLocalPart();
        
        serviceChannelWSAdapter = new TCPServiceChannelWSAdapter(serviceNameLocal,
                TCPConstants.SERVICE_CHANNEL_URL_PATTERN,
                endpoint,
                this);
        registerEndpointUrlPattern(TCPConstants.SERVICE_CHANNEL_CONTEXT_PATH,
                serviceChannelWSAdapter);
    }
}
