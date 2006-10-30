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
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
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
public class IncomeMessageProcessor {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private TCPMessageListener listener;
    
    private static Map<Integer, IncomeMessageProcessor> portMessageProcessors = new HashMap(1);
    
    public static void registerListener(int port, @NotNull TCPMessageListener listener) {
        portMessageProcessors.put(port, new IncomeMessageProcessor(listener));
    }
    
    public static void releaseListener(int port) {
        portMessageProcessors.remove(port);
    }
    
    public static @Nullable IncomeMessageProcessor getMessageProcessorForPort(int port) {
        return portMessageProcessors.get(port);
    }
    
    private IncomeMessageProcessor(TCPMessageListener listener) {
        this.listener = listener;
    }
    
    public void process(@NotNull ByteBuffer messageBuffer, @NotNull SocketChannel socketChannel) throws IOException {
        // get TCPConnectionSession associated with SocketChannel
        logger.log(Level.FINE, "IncomeMessageProcessor.process entering");
        
        ConnectionSession connectionSession = getConnectionSession(socketChannel); //@TODO take it from nio framework?
        
        if (connectionSession == null) {
            // First message on connection
            connectionSession = createConnectionSession(socketChannel, messageBuffer);
            if (connectionSession != null) {
                returnConnectionSession(connectionSession);
            } else {
                // Client's version is not supported
                logger.log(Level.WARNING, "IncomeMessageProcessor.process: Version mismatch");
            }
            return;
        }
        
        Connection connection = connectionSession.getConnection();
        connection.setInputStreamByteBuffer(messageBuffer);
        
        try {
            while(messageBuffer.hasRemaining()) {
                connection.prepareForReading();  // Reading headers
                int channelId = connection.getChannelId();
                ChannelContext channelContext = connectionSession.findWSServiceContextByChannelId(channelId);
                
                listener.onMessage(channelContext);
            }
        } finally {
            returnConnectionSession(connectionSession);
        }
        
        logger.log(Level.FINE, "IncomeMessageProcessor.process exiting");
    }
    
    /**
     *  associative map for SocketChannel and correspondent ConnectionContext
     *  in future probably should be replaced, as could be handled by
     *  nio framework
     */
    private Map<SocketChannel, ConnectionSession> connectionSessionMap = new WeakHashMap();
    private @Nullable ConnectionSession getConnectionSession(
            @NotNull SocketChannel socketChannel) throws IOException {
        
        ConnectionSession connectionSession = connectionSessionMap.get(socketChannel);
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
            @NotNull SocketChannel socketChannel,
    @NotNull ByteBuffer messageBuffer) throws IOException {
        logger.log(Level.FINE, "IncomeMessageProcessor.createConnectionSession entering");
        
        Connection connection = new Connection(socketChannel);
        connection.setInputStreamByteBuffer(messageBuffer);
        if (!checkVersionCompatibility(connection)) {
            connection.close();
            return null;
        }
        
        logger.log(Level.FINE, "IncomeMessageProcessor.createConnectionSession exiting");
        return new ConnectionSession(connection);
    }
    
    private void returnConnectionSession(@NotNull ConnectionSession connectionSession) {
        connectionSessionMap.put(connectionSession.getConnection().getSocketChannel(), connectionSession);
        
        // to let WeakHashMap clean socketChannel if not use
        connectionSession.getConnection().setSocketChannel(null);
    }
    
    private boolean checkVersionCompatibility(@NotNull Connection connection) throws IOException {
        logger.log(Level.FINE, "IncomeMessageProcessor.checkVersionCompatibility entering");
        connection.setDirectMode(true);
        
        InputStream inputStream = connection.openInputStream();
        int[] versionInfo = new int[4];
        
        DataInOutUtils.readInts4(inputStream, versionInfo, 4);
        
        Version clientFramingVersion = new Version(versionInfo[0], versionInfo[1]);
        Version clientConnectionManagementVersion = new Version(versionInfo[2], versionInfo[3]);
        
        VersionController versionController = VersionController.getInstance();
        
        VersionController.VersionSupport successCode = versionController.checkVersionSupport(
                clientFramingVersion, clientConnectionManagementVersion);
        
        OutputStream outputStream = connection.openOutputStream();
        Version framingVersion = versionController.getFramingVersion();
        Version connectionManagementVersion = versionController.getConnectionManagementVersion();
        
        DataInOutUtils.writeInts4(outputStream, successCode.ordinal(),
                framingVersion.getMajor(),
                framingVersion.getMinor(),
                connectionManagementVersion.getMajor(),
                connectionManagementVersion.getMinor());
        connection.flush();
        
        
        connection.setDirectMode(false);
        
        logger.log(Level.FINE, "IncomeMessageProcessor.checkVersionCompatibility successCode: {0}", successCode);
        return successCode == VersionController.VersionSupport.FULLY_SUPPORTED;
    }
    
    
}
