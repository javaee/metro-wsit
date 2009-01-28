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

package com.sun.xml.ws.transport.tcp.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelSettings;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.SessionAbortedException;
import com.sun.xml.ws.transport.tcp.util.SessionCloseListener;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.Version;
import com.sun.xml.ws.transport.tcp.util.VersionController;
import com.sun.xml.ws.transport.tcp.util.VersionMismatchException;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelException;
import com.sun.xml.ws.transport.tcp.servicechannel.stubs.ServiceChannelWSImpl;
import com.sun.xml.ws.transport.tcp.servicechannel.stubs.ServiceChannelWSImplService;
import com.sun.xml.ws.transport.tcp.util.BindingUtils;
import com.sun.xml.ws.transport.tcp.io.DataInOutUtils;
import com.sun.xml.ws.transport.tcp.util.ConnectionManagementSettings;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.ConnectionFinder;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.OutboundConnectionCache;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.ConnectionCacheFactory;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.ContactInfo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

/**
 * @author Alexey Stashok
 */
@SuppressWarnings({"unchecked"})
public class WSConnectionManager implements ConnectionFinder<ConnectionSession>,
        SessionCloseListener<ConnectionSession> {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    private static final WSConnectionManager instance = new WSConnectionManager();
    
    // set of locked connections, which are in use
    private final Map<ConnectionSession, Thread> lockedConnections = new WeakHashMap<ConnectionSession, Thread>();
    
    public static WSConnectionManager getInstance() {
        return instance;
    }
    
    // Cache for outbound connections (orb)
    private volatile OutboundConnectionCache<ConnectionSession> connectionCache;
    
    private WSConnectionManager() {
        ConnectionManagementSettings settings = 
                ConnectionManagementSettings.getSettingsHolder().getClientSettings();
        int highWatermark = settings.getHighWatermark();
        int numberToReclaim = settings.getNumberToReclaim();
        int maxParallelConnections = settings.getMaxParallelConnections();
        
        connectionCache = ConnectionCacheFactory.<ConnectionSession>makeBlockingOutboundConnectionCache("SOAP/TCP client side cache",
                highWatermark, numberToReclaim, maxParallelConnections, logger);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, 
                    MessagesMessages.WSTCP_1044_CONNECTION_MANAGER_CLIENT_SIDE_CONNECTION_CACHE(
                    highWatermark, maxParallelConnections, numberToReclaim));
        }
    }
    
    public @NotNull ChannelContext openChannel(@NotNull final WSTCPURI uri,
            @NotNull final WSService wsService, @NotNull final WSBinding wsBinding, final @NotNull Codec defaultCodec) throws InterruptedException, IOException,
    ServiceChannelException, VersionMismatchException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1030_CONNECTION_MANAGER_ENTER(uri, wsService.getServiceName(), wsBinding.getBindingID(), defaultCodec.getClass().getName()));
        }
        
        // Try to use available connection to endpoint
        final ConnectionSession session = connectionCache.get(uri, this);
        ChannelContext channelContext = session.findWSServiceContextByURI(uri);
        if (channelContext == null) {
            lockConnection(session);
            channelContext = session.findWSServiceContextByURI(uri);
            if (channelContext == null) {
                channelContext = doOpenChannel(session, uri, wsService, wsBinding, defaultCodec);
            }
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1033_CONNECTION_MANAGER_RETURN_CHANNEL_CONTEXT(channelContext.getChannelId()));
        }
        return channelContext;
    }
    
    public void closeChannel(@NotNull final ChannelContext channelContext) {
        final ConnectionSession connectionSession = channelContext.getConnectionSession();
        final ServiceChannelWSImpl serviceChannelWSImplPort = getSessionServiceChannel(connectionSession);
        
        try {
            lockConnection(connectionSession);
            serviceChannelWSImplPort.closeChannel(channelContext.getChannelId());
            connectionSession.deregisterChannel(channelContext);
        } catch (SessionAbortedException e) {
            // if session was closed before
        } catch (InterruptedException e) {
        } catch (ServiceChannelException e) {
        } finally {
            freeConnection(connectionSession);
        }
    }
    
    public void lockConnection(@NotNull final ConnectionSession connectionSession) throws InterruptedException, SessionAbortedException {
        synchronized(connectionSession) {
            do {
                final Thread thread = lockedConnections.get(connectionSession);
                if (thread == null) {
                    lockedConnections.put(connectionSession, Thread.currentThread());
                    return;
                } else if (thread.equals(Thread.currentThread())) {
                    return;
                }
                connectionSession.wait(500);
            } while(true);
        }
    }
    
    public void freeConnection(@NotNull final ConnectionSession connectionSession) {
        connectionCache.release(connectionSession, 0);
        synchronized(connectionSession) {
            lockedConnections.remove(connectionSession);
            connectionSession.notify();
        }
    }

    public void abortConnection(@NotNull final ConnectionSession connectionSession) {
        connectionCache.close(connectionSession);
    }    

    /**
     * Open new tcp connection and establish service virtual connection
     */
    public @NotNull ConnectionSession createConnectionSession(@NotNull final WSTCPURI tcpURI) throws VersionMismatchException, ServiceChannelException {
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1034_CONNECTION_MANAGER_CREATE_SESSION_ENTER(tcpURI));
            }
            final Connection connection = Connection.create(tcpURI.host, tcpURI.getEffectivePort());
            doSendMagicAndCheckVersions(connection);
            final ConnectionSession connectionSession = new ClientConnectionSession(connection, this);
            
            final ServiceChannelWSImplService serviceChannelWS = new ServiceChannelWSImplService();
            final ServiceChannelWSImpl serviceChannelWSImplPort = serviceChannelWS.getServiceChannelWSImplPort();
            connectionSession.setAttribute(TCPConstants.SERVICE_PIPELINE_ATTR_NAME, serviceChannelWSImplPort);
            
            final BindingProvider bindingProvider = (BindingProvider) serviceChannelWSImplPort;
            bindingProvider.getRequestContext().put(TCPConstants.TCP_SESSION, connectionSession);
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1035_CONNECTION_MANAGER_INITIATE_SESSION());
            }
            
            //@TODO check initiateSession result
            serviceChannelWSImplPort.initiateSession();
            
            return connectionSession;
        } catch (IOException e) {
            // ClientTransportException could be processed special way, outside transport layer
            throw new ClientTransportException(MessagesMessages.localizableWSTCP_0015_ERROR_PROTOCOL_VERSION_EXCHANGE(), e);
        }
    }
    
    /**
     * Open new channel over existing connection session
     */
    private @NotNull ChannelContext doOpenChannel(
            @NotNull final ConnectionSession connectionSession,
    @NotNull final WSTCPURI targetWSURI,
    @NotNull final WSService wsService,
    @NotNull final WSBinding wsBinding,
    final @NotNull Codec defaultCodec)
    throws IOException, ServiceChannelException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1036_CONNECTION_MANAGER_DO_OPEN_CHANNEL_ENTER());
        }
        final ServiceChannelWSImpl serviceChannelWSImplPort = getSessionServiceChannel(connectionSession);
        
        // Send to server possible mime types and parameters
        final BindingUtils.NegotiatedBindingContent negotiatedContent = BindingUtils.getNegotiatedContentTypesAndParams(wsBinding);
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1037_CONNECTION_MANAGER_DO_OPEN_WS_CALL(targetWSURI, negotiatedContent.negotiatedMimeTypes, negotiatedContent.negotiatedParams));
        }

        Holder<List<String>> negotiatedMimeTypesHolder = new Holder<List<String>>(negotiatedContent.negotiatedMimeTypes);
        Holder<List<String>> negotiatedParamsHolder = new Holder<List<String>>(negotiatedContent.negotiatedParams);
        final int channelId = serviceChannelWSImplPort.openChannel(targetWSURI.toString(),
                negotiatedMimeTypesHolder,
                negotiatedParamsHolder);
        
        ChannelSettings settings = new ChannelSettings(negotiatedMimeTypesHolder.value, 
                negotiatedParamsHolder.value, channelId, wsService.getServiceName(), targetWSURI);
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1038_CONNECTION_MANAGER_DO_OPEN_PROCESS_SERVER_SETTINGS(settings));
        }
        final ChannelContext channelContext = new ChannelContext(connectionSession, settings);
        
        ChannelContext.configureCodec(channelContext, wsBinding.getSOAPVersion(), defaultCodec);
        
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, MessagesMessages.WSTCP_1039_CONNECTION_MANAGER_DO_OPEN_REGISTER_CHANNEL(channelContext.getChannelId()));
        }
        connectionSession.registerChannel(channelContext);
        return channelContext;
    }
    
    /**
     * Get ConnectionSession's ServiceChannel web service
     */
    private @NotNull ServiceChannelWSImpl getSessionServiceChannel(@NotNull final ConnectionSession connectionSession) {
        return (ServiceChannelWSImpl) connectionSession.getAttribute(TCPConstants.SERVICE_PIPELINE_ATTR_NAME);
    }
        
    public ConnectionSession find(final ContactInfo<ConnectionSession> contactInfo,
            final Collection<ConnectionSession> idleConnections,
            final Collection<ConnectionSession> busyConnections) throws IOException {
        final WSTCPURI wsTCPURI = (WSTCPURI) contactInfo;
        ConnectionSession lru = null;
        for(ConnectionSession connectionSession : idleConnections) {
            if (connectionSession.findWSServiceContextByURI(wsTCPURI) != null) {
                return connectionSession;
            }
            if (lru == null) lru = connectionSession;
        }
        
        if (lru != null || connectionCache.canCreateNewConnection(contactInfo)) return lru;
        
        for(ConnectionSession connectionSession : busyConnections) {
            if (connectionSession.findWSServiceContextByURI(wsTCPURI) != null) {
                return connectionSession;
            }
            if (lru == null) lru = connectionSession;
        }
        
        return lru;
    }
    
    public void notifySessionClose(ConnectionSession connectionSession) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1043_CONNECTION_MANAGER_NOTIFY_SESSION_CLOSE(connectionSession.getConnection()));
        }
        freeConnection(connectionSession);
    }
    
    private static void doSendMagicAndCheckVersions(final Connection connection) throws IOException, VersionMismatchException {
        final VersionController versionController = VersionController.getInstance();
        final Version framingVersion = versionController.getFramingVersion();
        final Version connectionManagementVersion = versionController.getConnectionManagementVersion();
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1040_CONNECTION_MANAGER_DO_CHECK_VERSION_ENTER(framingVersion, connectionManagementVersion));
        }
        connection.setDirectMode(true);
        
        final OutputStream outputStream = connection.openOutputStream();
        outputStream.write(TCPConstants.PROTOCOL_SCHEMA.getBytes("US-ASCII"));
        
        DataInOutUtils.writeInts4(outputStream, framingVersion.getMajor(),
                framingVersion.getMinor(),
                connectionManagementVersion.getMajor(),
                connectionManagementVersion.getMinor());
        connection.flush();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1041_CONNECTION_MANAGER_DO_CHECK_VERSION_SENT());
        }
        
        final InputStream inputStream = connection.openInputStream();
        final int[] versionInfo = new int[4];
        
        DataInOutUtils.readInts4(inputStream, versionInfo, 4);
        
        final Version serverFramingVersion = new Version(versionInfo[0], versionInfo[1]);
        final Version serverConnectionManagementVersion = new Version(versionInfo[2], versionInfo[3]);
        
        connection.setDirectMode(false);
        
        final boolean success = versionController.isVersionSupported(serverFramingVersion, serverConnectionManagementVersion);

        if (!success) {
            throw new VersionMismatchException(MessagesMessages.WSTCP_0006_VERSION_MISMATCH(), serverFramingVersion,
                    serverConnectionManagementVersion);
        }
    }    
}
