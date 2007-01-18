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
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.Version;
import com.sun.xml.ws.transport.tcp.util.VersionController;
import com.sun.xml.ws.transport.tcp.io.DataInOutUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class IncomeMessageProcessor {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private final TCPMessageListener listener;
    
    private static Map<Integer, IncomeMessageProcessor> portMessageProcessors =
            new HashMap<Integer, IncomeMessageProcessor>(1);
    
    public static void registerListener(final int port, @NotNull final TCPMessageListener listener) {
        portMessageProcessors.put(port, new IncomeMessageProcessor(listener));
    }
    
    public static void releaseListener(final int port) {
        portMessageProcessors.remove(port);
    }
    
    public static @Nullable IncomeMessageProcessor getMessageProcessorForPort(final int port) {
        return portMessageProcessors.get(port);
    }
    
    public IncomeMessageProcessor(final TCPMessageListener listener) {
        this.listener = listener;
    }
    
    public void process(@NotNull final ByteBuffer messageBuffer, @NotNull final SocketChannel socketChannel) throws IOException {
        // get TCPConnectionSession associated with SocketChannel
        logger.log(Level.FINE, MessagesMessages.WSTCP_1080_INCOME_MSG_PROC_ENTER(Connection.getHost(socketChannel), Connection.getPort(socketChannel)));
        
        ConnectionSession connectionSession = getConnectionSession(socketChannel); //@TODO take it from nio framework?
        
        if (connectionSession == null) {
            // First message on connection
            logger.log(Level.FINE, MessagesMessages.WSTCP_1081_INCOME_MSG_CREATE_NEW_SESSION());
            connectionSession = createConnectionSession(socketChannel, messageBuffer);
            if (connectionSession != null) {
                offerConnectionSession(connectionSession);
            } else {
                // Client's version is not supported
                logger.log(Level.WARNING, MessagesMessages.WSTCP_0006_VERSION_MISMATCH());
            }
            return;
        }
        
        final Connection connection = connectionSession.getConnection();
        connection.setInputStreamByteBuffer(messageBuffer);
        
        try {
            do {
                connection.prepareForReading();  // Reading headers
                
                final int channelId = connection.getChannelId();
                final ChannelContext channelContext = connectionSession.findWSServiceContextByChannelId(channelId);
                
                listener.onMessage(channelContext);
            } while(messageBuffer.hasRemaining());
        } finally {
            offerConnectionSession(connectionSession);
        }
    }
    
    /**
     *  associative map for SocketChannel and correspondent ConnectionContext
     *  in future probably should be replaced, as could be handled by
     *  nio framework
     */
    private final Map<SocketChannel, ConnectionSession> connectionSessionMap =
            new WeakHashMap<SocketChannel, ConnectionSession>();
    private @Nullable ConnectionSession getConnectionSession(
            @NotNull final SocketChannel socketChannel) throws IOException {
        
        final ConnectionSession connectionSession = connectionSessionMap.get(socketChannel);
        if (connectionSession == null) {
            return null;
        }
        
        // Restore socketChannel in connection
        connectionSession.getConnection().setSocketChannel(socketChannel);
        return connectionSession;
    }
    
    /**
     *  Create new ConnectionSession for just came request, but check
     *  version compatibilities before
     */
    private @Nullable ConnectionSession createConnectionSession(
            @NotNull final SocketChannel socketChannel,
    @NotNull final ByteBuffer messageBuffer) throws IOException {
        
        final Connection connection = new Connection(socketChannel);
        connection.setInputStreamByteBuffer(messageBuffer);
        if (!checkMagicAndVersionCompatibility(connection)) {
            connection.close();
            return null;
        }
        
        return new ConnectionSession(connection);
    }
    
    private void offerConnectionSession(@NotNull final ConnectionSession connectionSession) {
        connectionSessionMap.put(connectionSession.getConnection().getSocketChannel(), connectionSession);
        
        // to let WeakHashMap clean socketChannel if not use
        connectionSession.getConnection().setSocketChannel(null);
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
        
        final VersionController.VersionSupport successCode = versionController.checkVersionSupport(
                clientFramingVersion, clientConnectionManagementVersion);
        
        final OutputStream outputStream = connection.openOutputStream();
        final Version framingVersion = versionController.getFramingVersion();
        final Version connectionManagementVersion = versionController.getConnectionManagementVersion();
        
        DataInOutUtils.writeInts4(outputStream, successCode.ordinal(),
                framingVersion.getMajor(),
                framingVersion.getMinor(),
                connectionManagementVersion.getMajor(),
                connectionManagementVersion.getMinor());
        connection.flush();
        
        
        connection.setDirectMode(false);
        
        logger.log(Level.FINE, MessagesMessages.WSTCP_1083_INCOME_MSG_VERSION_CHECK_RESULT(clientFramingVersion, clientConnectionManagementVersion, framingVersion, connectionManagementVersion, successCode));
        return successCode == VersionController.VersionSupport.FULLY_SUPPORTED;
    }
    
    
}
