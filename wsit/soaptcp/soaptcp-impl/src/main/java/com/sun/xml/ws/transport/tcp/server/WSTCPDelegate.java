/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelCreator;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPError;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelWSImpl;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class WSTCPDelegate implements WSTCPAdapterRegistry, TCPMessageListener {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private final Map<String, TCPAdapter> fixedUrlPatternEndpoints = new HashMap<String, TCPAdapter>();
    private final List<TCPAdapter> pathUrlPatternEndpoints = new ArrayList<TCPAdapter>();
    
    private volatile TCPAdapter serviceChannelWSAdapter;
    
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
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1102_WSTCP_DELEGATE_GOING_TO_CUSTOM_REG(tcpURI));
            }
            
            return customWSRegistry.getTarget(tcpURI);
        }
        
        return result;
    }
    
    /**
     * Implementation of TCPMessageListener.onError
     * method is called if error occured during frame processing
     * on upper level
     */
    public void onError(ChannelContext channelContext, WSTCPError error) throws IOException {
        sendErrorResponse(channelContext, error);
    }
    
    /**
     * Implementation of TCPMessageListener.onMessage
     * method is called once request message come
     */
    public void onMessage(@NotNull final ChannelContext channelContext) throws IOException {
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
                TCPAdapter.sendErrorResponse(channelContext,
                        WSTCPError.createNonCriticalError(TCPConstants.UNKNOWN_CHANNEL_ID,
                        MessagesMessages.WSTCP_0026_UNKNOWN_CHANNEL_ID(channelContext.getChannelId())));
            }
            
        } catch (WSTCPException e) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0023_TARGET_EXEC_ERROR(connection.getHost(), connection.getPort()), e);
            
            sendErrorResponse(channelContext, e.getError());
            
            if (e.getError().isCritical()) {
                channelContext.getConnectionSession().close();
            }
        } catch (JAXWSExceptionBase e) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0023_TARGET_EXEC_ERROR(connection.getHost(), connection.getPort()), e);

            sendErrorResponse(channelContext, WSTCPError.createNonCriticalError(TCPConstants.GENERAL_CHANNEL_ERROR,
                    MessagesMessages.WSTCP_0025_GENERAL_CHANNEL_ERROR(MessagesMessages.WSTCP_0004_CHECK_SERVER_LOG())));
        } catch (IOException e) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0023_TARGET_EXEC_ERROR(connection.getHost(), connection.getPort()), e);
            throw e;
        } catch (Exception e) {
            final Connection connection = channelContext.getConnection();
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0023_TARGET_EXEC_ERROR(connection.getHost(), connection.getPort()), e);
            sendErrorResponse(channelContext, WSTCPError.createNonCriticalError(TCPConstants.GENERAL_CHANNEL_ERROR,
                    MessagesMessages.WSTCP_0025_GENERAL_CHANNEL_ERROR(MessagesMessages.WSTCP_0004_CHECK_SERVER_LOG())));
        } finally {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1104_WSTCP_DELEGATE_ON_MESSAGE_COMPLETED());
            }
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
        if (serviceChannelWSAdapter == null) {
            registerServiceChannelWSAdapter();
        }
        
        return serviceChannelWSAdapter;
    }
    
    private void sendErrorResponse(ChannelContext channelContext, WSTCPError error) throws IOException {
        try {
            TCPAdapter.sendErrorResponse(channelContext, error);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0002_SERVER_ERROR_MESSAGE_SENDING_FAILED(), e);
            throw new IOException(e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    private synchronized void registerServiceChannelWSAdapter() throws Exception {
        if (serviceChannelWSAdapter == null) {
            WSEndpoint<ServiceChannelWSImpl> endpoint = ServiceChannelCreator.getServiceChannelEndpointInstance();
            final String serviceNameLocal = endpoint.getServiceName().getLocalPart();
            
            serviceChannelWSAdapter = new TCPServiceChannelWSAdapter(serviceNameLocal,
                    TCPConstants.SERVICE_CHANNEL_URL_PATTERN,
                    endpoint,
                    this);
            registerEndpointUrlPattern(TCPConstants.SERVICE_CHANNEL_CONTEXT_PATH,
                    serviceChannelWSAdapter);
        }
    }
}
