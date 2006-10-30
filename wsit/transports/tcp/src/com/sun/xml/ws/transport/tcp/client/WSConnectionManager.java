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
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.ClientTransportException;
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
    
    private WSConnectionCache wsConnectionCache;
    
    private WSConnectionManager() {
        wsConnectionCache = new WSConnectionCache();
    }
    
    public @NotNull ChannelContext openChannel(@NotNull WSTCPURI uri,
            @NotNull ClientPipeAssemblerContext pipeAssemblerContext) throws InterruptedException, IOException,
    ServiceChannelException, VersionMismatchException {
        int uriHashKey = uri.hashCode();
        
        logger.log(Level.FINE, "openChannel. Opening channel");
        // Try to use available connection to endpoint
        ConnectionSession availableConnectionSession = wsConnectionCache.pollAvailableConnectionByAddr(uriHashKey);
        
        if (availableConnectionSession != null) {
            logger.log(Level.FINE, "openChannel. Use opened session.");
            
            // if there is available connection session - use it
            wsConnectionCache.lockConnection(availableConnectionSession);
            
            ChannelContext channelContext = doOpenChannel(availableConnectionSession, uri, pipeAssemblerContext);
            
            // if session is supposed to accept more virtual connections - return it to available queue
            if (availableConnectionSession.getChannelsAmount() < MAX_CHANNELS_PER_CONNECTION) {
                logger.log(Level.FINEST, "openChannel. Offer session for reuse");
                wsConnectionCache.offerAvailableConnectionByAddr(availableConnectionSession, uriHashKey);
            }
            
            logger.log(Level.FINE, "openChannel. Return channelContext");
            return channelContext;
        }
        
        // if there is no available sessions to endpoint - create new
        ConnectionSession connectionSession = createConnectionSession(uri);
        wsConnectionCache.registerConnectionSession(connectionSession, uriHashKey);
        logger.log(Level.FINEST, "openChannel. Lock on session");
        wsConnectionCache.lockConnection(connectionSession);
        ChannelContext channelContext = doOpenChannel(connectionSession, uri, pipeAssemblerContext);
        logger.log(Level.FINE, "openChannel. Return channelContext");
        return channelContext;
    }
    
    public void closeChannel(@NotNull ChannelContext channelContext) {
        ConnectionSession connectionSession = channelContext.getConnectionSession();
        ServiceChannelWSImpl serviceChannelWSImplPort = getSessionServiceChannel(connectionSession);
        
        try {
            lockConnection(channelContext);
            int channelId = channelContext.getChannelId();
            serviceChannelWSImplPort.closeChannel(channelId);
            connectionSession.deregisterChannel(channelId);
        } catch (SessionAbortedException ex) {
            // if session was closed before
        } catch (InterruptedException ex) {
        } finally {
            freeConnection(channelContext);
        }
    }
    
    public void lockConnection(@NotNull ChannelContext channelContext) throws InterruptedException, SessionAbortedException {
        wsConnectionCache.lockConnection(channelContext.getConnectionSession());
    }
    
    public void freeConnection(@NotNull ChannelContext channelContext) {
        wsConnectionCache.unlockConnection(channelContext.getConnectionSession());
    }
    
    public void abortConnection(@NotNull ChannelContext channelContext) {
        ConnectionSession tcpConnectionSession = channelContext.getConnectionSession();
        wsConnectionCache.removeConnectionSession(tcpConnectionSession);
        tcpConnectionSession.abort();
    }
    
    /**
     * Open new tcp connection and establish service virtual connection
     */
    private @NotNull ConnectionSession createConnectionSession(@NotNull WSTCPURI tcpURI) throws VersionMismatchException {
        try {
            logger.log(Level.FINE, "WSConnectionManager.createConnectionSession");
            Connection connection = Connection.create(tcpURI.host, tcpURI.port);
            doCheckVersions(connection);
            ConnectionSession connectionSession = new ConnectionSession(tcpURI.hashCode(), connection);
            
            ServiceChannelWSImplService serviceChannelWS = new ServiceChannelWSImplService();
            ServiceChannelWSImpl serviceChannelWSImplPort = serviceChannelWS.getServiceChannelWSImplPort();
            connectionSession.setAttribute(TCPConstants.SERVICE_PIPELINE_ATTR_NAME, serviceChannelWSImplPort);
            
            BindingProvider bindingProvider = (BindingProvider) serviceChannelWSImplPort;
            bindingProvider.getRequestContext().put(TCPConstants.TCP_SESSION, connectionSession);
            
            logger.log(Level.FINE, "WSConnectionManager.createConnectionSession: call ServiceWS.initiateSession");
            int result = serviceChannelWSImplPort.initiateSession();
            
            return connectionSession;
        } catch (IOException e) {
            throw new ClientTransportException("TCP.client.failed", e);
        }
    }
    
    /**
     * Open new channel over existing connection session
     */
    private @NotNull ChannelContext doOpenChannel(
            @NotNull ConnectionSession connectionSession,
    @NotNull WSTCPURI targetWSURI,
    @NotNull ClientPipeAssemblerContext pipeAssemblerContext)
    throws IOException, ServiceChannelException {
        logger.log(Level.FINEST, "doOpenChannel");
        
        ServiceChannelWSImpl serviceChannelWSImplPort = getSessionServiceChannel(connectionSession);
        
        BindingProvider bindingProvider = (BindingProvider) serviceChannelWSImplPort;
        
        // Send to server possible mime types and parameters
        WSBinding binding = pipeAssemblerContext.getBinding();
        WSService service = pipeAssemblerContext.getService();
        Codec defaultCodec = pipeAssemblerContext.getCodec();
        
        BindingUtils.NegotiatedBindingContent negotiatedContent = BindingUtils.getNegotiatedContentTypesAndParams(binding);
        ChannelSettings clientSettings = new ChannelSettings(negotiatedContent.negotiatedMimeTypes, negotiatedContent.negotiatedParams,
                0, service.getServiceName(), targetWSURI);
        
        logger.log(Level.FINEST, "doOpenChannel: call ServiceWS.openChannel");
        ChannelSettings serverSettings = serviceChannelWSImplPort.openChannel(clientSettings);
        logger.log(Level.FINEST, "doOpenChannel: process server settings");
        ChannelContext channelContext = new ChannelContext(connectionSession, serverSettings);
        
        ChannelContext.configureCodec(channelContext, binding.getSOAPVersion(), defaultCodec);
        
        logger.log(Level.FINEST, "doOpenChannel: register channel");
        connectionSession.registerChannel(serverSettings.getChannelId(), channelContext);
        return channelContext;
    }
    
    /**
     * Get ConnectionSession's ServiceChannel web service
     */
    private @NotNull ServiceChannelWSImpl getSessionServiceChannel(@NotNull ConnectionSession connectionSession) {
        return (ServiceChannelWSImpl) connectionSession.getAttribute(TCPConstants.SERVICE_PIPELINE_ATTR_NAME);
    }
    
    private void doCheckVersions(Connection connection) throws IOException, VersionMismatchException {
        logger.log(Level.FINE, "doCheckVersions entering");
        connection.setDirectMode(true);
        OutputStream outputStream = connection.openOutputStream();
        VersionController versionController = VersionController.getInstance();
        Version framingVersion = versionController.getFramingVersion();
        Version connectionManagementVersion = versionController.getConnectionManagementVersion();
        
        DataInOutUtils.writeInts4(outputStream, framingVersion.getMajor(),
                framingVersion.getMinor(),
                connectionManagementVersion.getMajor(),
                connectionManagementVersion.getMinor());
        connection.flush();
        logger.log(Level.FINE, "doCheckVersions version sent");
        
        InputStream inputStream = connection.openInputStream();
        int[] versionInfo = new int[5];
        
        DataInOutUtils.readInts4(inputStream, versionInfo, 5);
        int success = versionInfo[0];
        
        logger.log(Level.FINE, "doCheckVersions result: {0}", success);
        Version serverFramingVersion = new Version(versionInfo[1], versionInfo[2]);
        Version serverConnectionManagementVersion = new Version(versionInfo[3], versionInfo[4]);
        
        if (success != VersionController.VersionSupport.FULLY_SUPPORTED.ordinal()) {
            throw new VersionMismatchException("Version mismatch!", serverFramingVersion,
                    serverConnectionManagementVersion);
        }
        
        connection.setDirectMode(false);
    }
    
}
