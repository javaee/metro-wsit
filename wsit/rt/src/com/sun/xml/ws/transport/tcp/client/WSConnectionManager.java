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
import com.sun.xml.ws.transport.tcp.util.SessionAbortedException;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.BindingProvider;

/**
 * @author Alexey Stashok
 */
public class WSConnectionManager {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    private static final int MAX_CHANNELS_PER_CONNECTION = 10;
    
    private static final WSConnectionManager instance = new WSConnectionManager();
    
    public static WSConnectionManager getInstance() {
        return instance;
    }
    
    private final WSConnectionCache wsConnectionCache;
    
    private WSConnectionManager() {
        wsConnectionCache = new WSConnectionCache();
    }
    
    public @NotNull ChannelContext openChannel(@NotNull final WSTCPURI uri,
            @NotNull final WSService wsService, @NotNull final WSBinding wsBinding, final @NotNull Codec defaultCodec) throws InterruptedException, IOException,
    ServiceChannelException, VersionMismatchException {
        final int uriHashKey = uri.hashCode();
        
        logger.log(Level.FINE, MessagesMessages.WSTCP_1030_CONNECTION_MANAGER_ENTER(uri, wsService.getServiceName(), wsBinding.getBindingID(), defaultCodec.getClass().getName()));
        // Try to use available connection to endpoint
        final ConnectionSession availableConnectionSession = wsConnectionCache.pollAvailableConnectionByAddr(uriHashKey);
        
        if (availableConnectionSession != null) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1031_CONNECTION_MANAGER_USE_OPENED_SESSION());
            
            // if there is available connection session - use it
            wsConnectionCache.lockConnection(availableConnectionSession);
            
            final ChannelContext channelContext = doOpenChannel(availableConnectionSession, uri, wsService, wsBinding, defaultCodec);
            
            // if session is supposed to accept more virtual connections - return it to available queue
            if (availableConnectionSession.getChannelsAmount() < MAX_CHANNELS_PER_CONNECTION) {
                logger.log(Level.FINEST, MessagesMessages.WSTCP_1032_CONNECTION_MANAGER_OFFER_SESSION_FOR_REUSE());
                wsConnectionCache.offerAvailableConnectionByAddr(availableConnectionSession, uriHashKey);
            }
            
            logger.log(Level.FINE, MessagesMessages.WSTCP_1033_CONNECTION_MANAGER_RETURN_CHANNEL_CONTEXT(channelContext.getChannelId()));
            return channelContext;
        }
        
        // if there is no available sessions to endpoint - create new
        final ConnectionSession connectionSession = createConnectionSession(uri);
        wsConnectionCache.registerConnectionSession(connectionSession, uriHashKey);
        wsConnectionCache.lockConnection(connectionSession);
        final ChannelContext channelContext = doOpenChannel(connectionSession, uri, wsService, wsBinding, defaultCodec);
        logger.log(Level.FINE, MessagesMessages.WSTCP_1033_CONNECTION_MANAGER_RETURN_CHANNEL_CONTEXT(channelContext.getChannelId()));
        return channelContext;
    }
    
    public void closeChannel(@NotNull final ChannelContext channelContext) {
        final ConnectionSession connectionSession = channelContext.getConnectionSession();
        final ServiceChannelWSImpl serviceChannelWSImplPort = getSessionServiceChannel(connectionSession);
        
        try {
            lockConnection(channelContext);
            final int channelId = channelContext.getChannelId();
            serviceChannelWSImplPort.closeChannel(channelId);
            connectionSession.deregisterChannel(channelId);
        } catch (SessionAbortedException ex) {
            // if session was closed before
        } catch (InterruptedException ex) {
        } finally {
            freeConnection(channelContext);
        }
    }
    
    public void lockConnection(@NotNull final ChannelContext channelContext) throws InterruptedException, SessionAbortedException {
        wsConnectionCache.lockConnection(channelContext.getConnectionSession());
    }
    
    public void freeConnection(@NotNull final ChannelContext channelContext) {
        wsConnectionCache.unlockConnection(channelContext.getConnectionSession());
    }
    
    public void abortConnection(@NotNull final ChannelContext channelContext) {
        final ConnectionSession tcpConnectionSession = channelContext.getConnectionSession();
        wsConnectionCache.removeConnectionSession(tcpConnectionSession);
        tcpConnectionSession.abort();
    }
    
    /**
     * Open new tcp connection and establish service virtual connection
     */
    private @NotNull ConnectionSession createConnectionSession(@NotNull final WSTCPURI tcpURI) throws VersionMismatchException {
        try {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1034_CONNECTION_MANAGER_CREATE_SESSION_ENTER(tcpURI));
            final Connection connection = Connection.create(tcpURI.host, tcpURI.port);
            doSendMagicAndCheckVersions(connection);
            final ConnectionSession connectionSession = new ConnectionSession(tcpURI.hashCode(), connection);
            
            final ServiceChannelWSImplService serviceChannelWS = new ServiceChannelWSImplService();
            final ServiceChannelWSImpl serviceChannelWSImplPort = serviceChannelWS.getServiceChannelWSImplPort();
            connectionSession.setAttribute(TCPConstants.SERVICE_PIPELINE_ATTR_NAME, serviceChannelWSImplPort);
            
            final BindingProvider bindingProvider = (BindingProvider) serviceChannelWSImplPort;
            bindingProvider.getRequestContext().put(TCPConstants.TCP_SESSION, connectionSession);
            
            logger.log(Level.FINE, MessagesMessages.WSTCP_1035_CONNECTION_MANAGER_INITIATE_SESSION());
            
            //@TODO check initiateSession result
            serviceChannelWSImplPort.initiateSession();
            
            return connectionSession;
        } catch (IOException e) {
//@TODO uncommit after next JAX-WS integration
//            throw new ClientTransportException(MessagesMessages.localizableERROR_PROTOCOL_VERSION_EXCHANGE(), e);
            throw new ClientTransportException(e);
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
        logger.log(Level.FINEST, MessagesMessages.WSTCP_1036_CONNECTION_MANAGER_DO_OPEN_CHANNEL_ENTER());
        
        final ServiceChannelWSImpl serviceChannelWSImplPort = getSessionServiceChannel(connectionSession);
        
        // Send to server possible mime types and parameters
        final BindingUtils.NegotiatedBindingContent negotiatedContent = BindingUtils.getNegotiatedContentTypesAndParams(wsBinding);
        final ChannelSettings clientSettings = new ChannelSettings(negotiatedContent.negotiatedMimeTypes, negotiatedContent.negotiatedParams,
                0, wsService.getServiceName(), targetWSURI);
        
        logger.log(Level.FINEST, MessagesMessages.WSTCP_1037_CONNECTION_MANAGER_DO_OPEN_WS_CALL(clientSettings));
        final ChannelSettings serverSettings = serviceChannelWSImplPort.openChannel(clientSettings);
        logger.log(Level.FINEST, MessagesMessages.WSTCP_1038_CONNECTION_MANAGER_DO_OPEN_PROCESS_SERVER_SETTINGS(serverSettings));
        final ChannelContext channelContext = new ChannelContext(connectionSession, serverSettings);
        
        ChannelContext.configureCodec(channelContext, wsBinding.getSOAPVersion(), defaultCodec);
        
        logger.log(Level.FINEST, MessagesMessages.WSTCP_1039_CONNECTION_MANAGER_DO_OPEN_REGISTER_CHANNEL(channelContext.getChannelId()));
        connectionSession.registerChannel(serverSettings.getChannelId(), channelContext);
        return channelContext;
    }
    
    /**
     * Get ConnectionSession's ServiceChannel web service
     */
    private @NotNull ServiceChannelWSImpl getSessionServiceChannel(@NotNull final ConnectionSession connectionSession) {
        return (ServiceChannelWSImpl) connectionSession.getAttribute(TCPConstants.SERVICE_PIPELINE_ATTR_NAME);
    }
    
    private void doSendMagicAndCheckVersions(final Connection connection) throws IOException, VersionMismatchException {
        final VersionController versionController = VersionController.getInstance();
        final Version framingVersion = versionController.getFramingVersion();
        final Version connectionManagementVersion = versionController.getConnectionManagementVersion();
        
        logger.log(Level.FINE, MessagesMessages.WSTCP_1040_CONNECTION_MANAGER_DO_CHECK_VERSION_ENTER(framingVersion, connectionManagementVersion));
        connection.setDirectMode(true);
        
        final OutputStream outputStream = connection.openOutputStream();
        outputStream.write(TCPConstants.PROTOCOL_SCHEMA.getBytes("US-ASCII"));
        
        DataInOutUtils.writeInts4(outputStream, framingVersion.getMajor(),
                framingVersion.getMinor(),
                connectionManagementVersion.getMajor(),
                connectionManagementVersion.getMinor());
        connection.flush();
        logger.log(Level.FINE, MessagesMessages.WSTCP_1041_CONNECTION_MANAGER_DO_CHECK_VERSION_SENT());
        
        final InputStream inputStream = connection.openInputStream();
        final int[] versionInfo = new int[5];
        
        DataInOutUtils.readInts4(inputStream, versionInfo, 5);
        final int success = versionInfo[0];
        
        logger.log(Level.FINE, MessagesMessages.WSTCP_1042_CONNECTION_MANAGER_DO_CHECK_VERSION_RESULT(success));
        final Version serverFramingVersion = new Version(versionInfo[1], versionInfo[2]);
        final Version serverConnectionManagementVersion = new Version(versionInfo[3], versionInfo[4]);
        
        connection.setDirectMode(false);
        
        if (success != VersionController.VersionSupport.FULLY_SUPPORTED.ordinal()) {
            throw new VersionMismatchException(MessagesMessages.WSTCP_0006_VERSION_MISMATCH(), serverFramingVersion,
                    serverConnectionManagementVersion);
        }
    }
    
}
