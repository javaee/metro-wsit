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
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.util.ChannelSettings;
import com.sun.xml.ws.transport.tcp.util.SessionCloseListener;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.Version;
import com.sun.xml.ws.transport.tcp.util.VersionController;
import com.sun.xml.ws.transport.tcp.io.DataInOutUtils;
import com.sun.xml.ws.transport.tcp.util.WSTCPError;
import com.sun.xml.ws.transport.tcp.util.ConnectionManagementSettings;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.InboundConnectionCache;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.ConnectionCacheFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
@SuppressWarnings({"unchecked"})
public final class IncomeMessageProcessor implements SessionCloseListener {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private final TCPMessageListener listener;
    
    // Properties passed to IncomeMessageProcessor by SOAP/TCP launcher
    private final Properties properties;
    
    // Cache for inbound connections (orb). Initialized on first SOAP/TCP request
    private volatile InboundConnectionCache<ServerConnectionSession> connectionCache;
    
    private static Map<Integer, IncomeMessageProcessor> portMessageProcessors =
            new HashMap<Integer, IncomeMessageProcessor>(1);
    
    public static IncomeMessageProcessor registerListener(final int port, @NotNull final TCPMessageListener listener,
            @NotNull final Properties properties) {
        IncomeMessageProcessor processor = new IncomeMessageProcessor(listener, properties);
        portMessageProcessors.put(port, processor);
        return processor;
    }
    
    public static void releaseListener(final int port) {
        portMessageProcessors.remove(port);
    }
    
    public static @Nullable IncomeMessageProcessor getMessageProcessorForPort(final int port) {
        return portMessageProcessors.get(port);
    }
    
    private IncomeMessageProcessor(final @NotNull TCPMessageListener listener) {
        this(listener, null);
    }
    
    private IncomeMessageProcessor(final @NotNull TCPMessageListener listener, final @Nullable Properties properties) {
        this.listener = listener;
        this.properties = properties;
    }
    
    public void process(@NotNull final ByteBuffer messageBuffer, @NotNull final SocketChannel socketChannel) throws IOException {
        // get TCPConnectionSession associated with SocketChannel
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1080_INCOME_MSG_PROC_ENTER(Connection.getHost(socketChannel), Connection.getPort(socketChannel)));
        }
        
        if (connectionCache == null) {
            setupInboundConnectionCache();
        }
        
        ServerConnectionSession connectionSession = getConnectionSession(socketChannel); //@TODO take it from nio framework?
        
        if (connectionSession == null) {
            // First message on connection
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1081_INCOME_MSG_CREATE_NEW_SESSION());
            }
            connectionSession = createConnectionSession(socketChannel, messageBuffer);
            if (connectionSession != null) {
                // Connection is opened. Magic and version are compatible
                connectionCache.requestReceived(connectionSession);
                connectionCache.responseSent(connectionSession);
                offerConnectionSession(connectionSession);
            } else {
                // Client's version is not supported
                logger.log(Level.WARNING, MessagesMessages.WSTCP_0006_VERSION_MISMATCH());
            }
            return;
        }
        
        final Connection connection = connectionSession.getConnection();
        connection.setInputStreamByteBuffer(messageBuffer);
        connectionCache.requestReceived(connectionSession);
        
        try {
            do {
                connection.prepareForReading();  // Reading headers
                
                final int channelId = connection.getChannelId();
                final ChannelContext channelContext = connectionSession.findWSServiceContextByChannelId(channelId);
                
                if (channelContext != null) {
                    listener.onMessage(channelContext);
                } else {
                    // Create fake channel context for received channel-id and session
                    ChannelContext fakeChannelContext = createFakeChannelContext(channelId, connectionSession);
                    // Notify error on channel context
                    listener.onError(fakeChannelContext, WSTCPError.createNonCriticalError(TCPConstants.UNKNOWN_CHANNEL_ID,
                            MessagesMessages.WSTCP_0026_UNKNOWN_CHANNEL_ID(channelId)));
                }
            } while(messageBuffer.hasRemaining());
        } finally {
            offerConnectionSession(connectionSession);
            connectionCache.responseSent(connectionSession);
        }
    }
    
    /**
     *  associative map for SocketChannel and correspondent ConnectionContext
     *  in future probably should be replaced, as could be handled by
     *  nio framework
     */
    private final Map<SocketChannel, ServerConnectionSession> connectionSessionMap =
            new WeakHashMap<SocketChannel, ServerConnectionSession>();
    private @Nullable ServerConnectionSession getConnectionSession(
            @NotNull final SocketChannel socketChannel) {
        
        final ServerConnectionSession connectionSession = connectionSessionMap.get(socketChannel);
        if (connectionSession == null) {
            return null;
        }
        
        // Restore socketChannel in connection
        connectionSession.getConnection().setSocketChannel(socketChannel);
        return connectionSession;
    }
    
    private void offerConnectionSession(@NotNull final ServerConnectionSession connectionSession) {
        connectionSessionMap.put(connectionSession.getConnection().getSocketChannel(), connectionSession);
        
        // to let WeakHashMap clean socketChannel if not use
        connectionSession.getConnection().setSocketChannel(null);
    }
    
    /**
     * Remove session entry from session map
     */
    private void removeConnectionSessionBySocketChannel(@NotNull final SocketChannel socketChannel) {
        connectionSessionMap.remove(socketChannel);
    }
    
    /**
     *  Create new ConnectionSession for just came request, but check
     *  version compatibilities before
     */
    private @Nullable ServerConnectionSession createConnectionSession(
            @NotNull final SocketChannel socketChannel,
    @NotNull final ByteBuffer messageBuffer) throws IOException {
        
        final Connection connection = new Connection(socketChannel);
        connection.setInputStreamByteBuffer(messageBuffer);
        if (!checkMagicAndVersionCompatibility(connection)) {
            connection.close();
            return null;
        }
        
        return new ServerConnectionSession(connection, this);
    }
    
    private boolean checkMagicAndVersionCompatibility(@NotNull final Connection connection) throws IOException {
        logger.log(Level.FINE, MessagesMessages.WSTCP_1082_INCOME_MSG_VERSION_CHECK_ENTER());
        
        connection.setDirectMode(true);
        final InputStream inputStream = connection.openInputStream();
        
        final byte[] magicBuf = new byte[TCPConstants.PROTOCOL_SCHEMA.length()];
        DataInOutUtils.readFully(inputStream, magicBuf);
        final String magic = new String(magicBuf, "US-ASCII");
        if (!TCPConstants.PROTOCOL_SCHEMA.equals(magic)) {
            logger.log(Level.WARNING, MessagesMessages.WSTCP_0020_WRONG_MAGIC(magic));
            return false;
        }
        
        final int[] versionInfo = new int[4];
        
        DataInOutUtils.readInts4(inputStream, versionInfo, 4);
        
        final Version clientFramingVersion = new Version(versionInfo[0], versionInfo[1]);
        final Version clientConnectionManagementVersion = new Version(versionInfo[2], versionInfo[3]);
        
        final VersionController versionController = VersionController.getInstance();
        
        final boolean isSupported = versionController.isVersionSupported(
                clientFramingVersion, clientConnectionManagementVersion);
        
        final OutputStream outputStream = connection.openOutputStream();
        
        final Version framingVersion = isSupported ? clientFramingVersion :
            versionController.getClosestSupportedFramingVersion(clientFramingVersion);
        final Version connectionManagementVersion = isSupported ? clientConnectionManagementVersion :
            versionController.getClosestSupportedConnectionManagementVersion(clientConnectionManagementVersion);
        
        DataInOutUtils.writeInts4(outputStream,
                framingVersion.getMajor(),
                framingVersion.getMinor(),
                connectionManagementVersion.getMajor(),
                connectionManagementVersion.getMinor());
        connection.flush();
        
        
        connection.setDirectMode(false);
        
        logger.log(Level.FINE, MessagesMessages.WSTCP_1083_INCOME_MSG_VERSION_CHECK_RESULT(clientFramingVersion, clientConnectionManagementVersion, framingVersion, connectionManagementVersion, isSupported));
        return isSupported;
    }
    
    /**
     * Close callback method
     * Will be called by NIO framework, when it will decide to close connection
     */
    public void notifyClosed(@NotNull final SocketChannel socketChannel) {
        if (connectionCache != null) {
            connectionCache.close(getConnectionSession(socketChannel));
        }
    }
    
    /**
     * Close callback method
     * Will be called by Connection.close() to let IncomeMessageProcessor
     * remove the correspondent session from Map
     */
    public void notifySessionClose(@NotNull final ConnectionSession connectionSession) {
        removeConnectionSessionBySocketChannel(connectionSession.getConnection().getSocketChannel());
    }
    
    private synchronized void setupInboundConnectionCache() {
        if (connectionCache == null) {
            ConnectionManagementSettings settings = 
                    ConnectionManagementSettings.getSettingsHolder().getServerSettings();
            
            int highWatermark = settings.getHighWatermark();
            int numberToReclaim = settings.getNumberToReclaim();
            
            connectionCache = ConnectionCacheFactory.<ServerConnectionSession>makeBlockingInboundConnectionCache("SOAP/TCP server side cache",
                    highWatermark, numberToReclaim, logger);
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, 
                        MessagesMessages.WSTCP_1084_INCOME_MSG_SERVER_SIDE_CONNECTION_CACHE(
                        highWatermark, numberToReclaim));
            }
        }
    }
    
    /**
     * Method creates fake channel context for defined channel-id and ConnectionSession
     * Normally channel context should be created only by Connection Management service
     */
    private ChannelContext createFakeChannelContext(int channelId, @NotNull ConnectionSession connectionSession) {
        return new ChannelContext(connectionSession, new ChannelSettings(Collections.<String>emptyList(),
                Collections.<String>emptyList(), channelId, null, null));
    }
}
