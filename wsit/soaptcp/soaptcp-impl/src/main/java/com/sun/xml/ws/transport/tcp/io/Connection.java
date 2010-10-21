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

package com.sun.xml.ws.transport.tcp.io;

import com.sun.xml.ws.transport.tcp.pool.ByteBufferStreamPool;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ByteBufferFactory;
import com.sun.xml.ws.transport.tcp.util.FrameType;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class Connection {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain);
    
    private static ByteBufferStreamPool<FramedMessageInputStream> byteBufferInputStreamPool =
            new ByteBufferStreamPool<FramedMessageInputStream>(FramedMessageInputStream.class);
    private static ByteBufferStreamPool<FramedMessageOutputStream> byteBufferOutputStreamPool =
            new ByteBufferStreamPool<FramedMessageOutputStream>(FramedMessageOutputStream.class);
    
    private SocketChannel socketChannel;
    
    private WeakReference<BufferedMessageInputStream> inputStreamRef;
    
    private FramedMessageInputStream inputStream;
    private FramedMessageOutputStream outputStream;
    
    /** is message framed or direct mode is used */
    private boolean isDirectMode;
    
    private int messageId;
    private int channelId;
    private int contentId;
    
    public Connection(final SocketChannel socketChannel) {
        inputStream = byteBufferInputStreamPool.take();
        outputStream = byteBufferOutputStreamPool.take();
        setSocketChannel(socketChannel);
    }
    
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    
    public void setSocketChannel(final SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        inputStream.setSocketChannel(socketChannel);
        outputStream.setSocketChannel(socketChannel);
    }
    
    /*
     * Method should be called each time InputStream is used for new message reading!!!
     */
    public void prepareForReading() throws IOException {
        if (inputStreamRef != null) {
            final BufferedMessageInputStream is = inputStreamRef.get();
            // if InputStream is used by some lazy reader - buffer message
            if (inputStream.isMessageInProcess() && is != null && !is.isClosed()) {
                is.bufferMessage();
                logger.log(Level.FINEST, MessagesMessages.WSTCP_1050_CONNECTION_BUFFERING_IS(is.getBufferedSize()));
            }
        }
        
        // double check input stream doesnt have earlier read message
        if (inputStream.isMessageInProcess()) {
            inputStream.skipToEndOfMessage();
        }
        
        inputStream.reset();
        outputStream.reset();
        
        inputStream.forceHeaderRead();
        
        channelId = inputStream.getChannelId();
        messageId = inputStream.getMessageId();
        
        if (FrameType.isFrameContainsParams(messageId)) {
            contentId = inputStream.getContentId();
        }
    }
    
    /*
     * Method should be called <b>once</b> each time for new message reading!!!
     * prepareForReading() should be called before!
     */
    public InputStream openInputStream() throws IOException {
        final BufferedMessageInputStream is = new BufferedMessageInputStream(inputStream);
        inputStreamRef = new WeakReference<BufferedMessageInputStream>(is);
        return is;
    }
    
    public OutputStream openOutputStream() throws IOException {
        outputStream.setChannelId(channelId);
        outputStream.setMessageId(messageId);
        outputStream.setContentId(contentId);
        
        outputStream.buildHeader();
        return outputStream;
    }
    
    public void flush() throws IOException {
        outputStream.flushLast();
    }
    
    public boolean isDirectMode() {
        return isDirectMode;
    }
    
    public void setDirectMode(final boolean isDirectMode) {
        this.isDirectMode = isDirectMode;
        inputStream.setDirectMode(isDirectMode);
        outputStream.setDirectMode(isDirectMode);
    }
    /**
     * Get channel id
     */
    public int getChannelId() {
        return channelId;
    }
    
    /**
     * Set channel id
     */
    public void setChannelId(final int channelId) {
        this.channelId = channelId;
    }
    
    /**
     * Get request/response messageId of 1st frame
     */
    public int getMessageId() {
        return messageId;
    }
    
    /**
     * Set request/response messageId of 1st frame
     */
    public void setMessageId(final int messageId) {
        this.messageId = messageId;
    }
    
    /**
     * Get request/response contentId
     */
    public int getContentId() {
        return contentId;
    }
    
    /**
     * Set request/response contentId
     */
    public void setContentId(final int contentId) {
        this.contentId = contentId;
    }
    
    /**
     * Get request content properties
     */
    public Map<Integer, String> getContentProperties() {
        return inputStream.getContentProperties();
    }
    
    /**
     * Set response content properties
     */
    public void setContentProperty(int key, String value) {
        outputStream.setContentProperty(key, value);
    }
    
    /**
     * Set messageBuffer for InputStream
     * some message part could be preread before
     */
    public void setInputStreamByteBuffer(final ByteBuffer messageBuffer) {
        inputStream.setByteBuffer(messageBuffer);
    }
    
    public void close() throws IOException {
        if (inputStream != null) {
            byteBufferInputStreamPool.release(inputStream);
            inputStream = null;
        }
        
        if (outputStream != null) {
            byteBufferOutputStreamPool.release(outputStream);
            outputStream = null;
        }
        
        socketChannel.close();
    }
    
    public static Connection create(final String host, final int port) throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1051_CONNECTION_OPEN_TCP_SOCKET(host, port));
        }
        final SocketChannel socketChannel = SocketChannel.open();
        final Socket socket = socketChannel.socket();
        socket.connect(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        
        final Connection connection = new Connection(socketChannel);
        
        final ByteBuffer byteBuffer = ByteBufferFactory.allocateView(TCPConstants.DEFAULT_FRAME_SIZE, TCPConstants.DEFAULT_USE_DIRECT_BUFFER);
        byteBuffer.position(0);
        byteBuffer.limit(0);
        
        connection.setInputStreamByteBuffer(byteBuffer);
        
        return connection;
    }
    
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    public String getHost() {
        return getHost(socketChannel);
    }
    
    public int getPort() {
        return getPort(socketChannel);
    }
    
    public String getLocalHost() {
        return getLocalHost(socketChannel);
    }
    
    public int getLocalPort() {
        return getLocalPort(socketChannel);
    }
    
    @Override
    public String toString() {
        return "host: " + getHost() + " port: " + getPort();
    }
    
    public static String getHost(final SocketChannel socketChannel) {
        return socketChannel.socket().getInetAddress().getHostAddress();
    }
    
    public static int getPort(final SocketChannel socketChannel) {
        return socketChannel.socket().getPort();
    }
    
    public static String getLocalHost(final SocketChannel socketChannel) {
        return socketChannel.socket().getLocalAddress().getHostAddress();
    }
    
    public static int getLocalPort(final SocketChannel socketChannel) {
        return socketChannel.socket().getLocalPort();
    }
}
