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

package com.sun.xml.ws.transport.tcp.io;

import com.sun.xml.ws.transport.tcp.pool.ByteBufferStreamPool;
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
public class Connection {
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
    private Map<Integer, String> contentProps;
    
    public Connection(SocketChannel socketChannel) {
        inputStream = byteBufferInputStreamPool.take();
        outputStream = byteBufferOutputStreamPool.take();
        setSocketChannel(socketChannel);
    }
    
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
    
    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        inputStream.setSocketChannel(socketChannel);
        outputStream.setSocketChannel(socketChannel);
    }
    
    /*
     * Method should be called each time InputStream is used for new message reading!!!
     */
    public void prepareForReading() throws IOException {
        if (inputStreamRef != null) {
            BufferedMessageInputStream is = inputStreamRef.get();
            // if InputStream is used by some lazy reader - buffer message
            if (inputStream.isMessageInProcess() && is != null && !is.isClosed()) {
                is.bufferMessage();
                logger.log(Level.FINEST, "Buffering Connection.InputStream. Size: {0}", is.getBufferedSize());
            }
        }
        
        // double check input stream doesnt have earlier read message
        if (inputStream.isMessageInProcess()) {
            inputStream.skipToEndOfMessage();
        }
        
        inputStream.reset();
        inputStream.forceHeaderRead();
        
        channelId = inputStream.getChannelId();
        messageId = inputStream.getMessageId();
        
        if (FrameType.isFrameContainsParams(messageId)) {
            contentId = inputStream.getContentId();
            contentProps = inputStream.getContentProps();
        }
    }
    
    /*
     * Method should be called <b>once</b> each time for new message reading!!!
     * prepareForReading() should be called before!
     */
    public InputStream openInputStream() throws IOException {
        BufferedMessageInputStream is = new BufferedMessageInputStream(inputStream);
        inputStreamRef = new WeakReference<BufferedMessageInputStream>(is);
        return is;
    }
    
    public OutputStream openOutputStream() {
        outputStream.reset();
        outputStream.setChannelId(channelId);
        outputStream.setMessageId(messageId);
        outputStream.setContentId(contentId);
        outputStream.setContentProps(contentProps);
        
        return outputStream;
    }
    
    public void flush() throws IOException {
        outputStream.flushLast();
    }
    
    public boolean isDirectMode() {
        return isDirectMode;
    }
    
    public void setDirectMode(boolean isDirectMode) {
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
    public void setChannelId(int channelId) {
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
    public void setMessageId(int messageId) {
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
    public void setContentId(int contentId) {
        this.contentId = contentId;
    }
    
    /**
     * Get request/response contentProps
     */
    public Map<Integer, String> getContentProps() {
        return contentProps;
    }
    
    /**
     * Set request/response contentProps
     */
    public void setContentProps(Map<Integer, String> contentProps) {
        this.contentProps = contentProps;
    }
    
    /**
     * Set messageBuffer for InputStream
     * some message part could be preread before
     */
    public void setInputStreamByteBuffer(ByteBuffer messageBuffer) {
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
    
    public static Connection create(String host, int port) throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Opening connection host: {0} port: {1}", new Object[] {host, port});
        }
        SocketChannel socketChannel = SocketChannel.open();
        Socket socket = socketChannel.socket();
        socket.connect(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        
        Connection connection = new Connection(socketChannel);

        ByteBuffer byteBuffer = ByteBufferFactory.allocateView(TCPConstants.DEFAULT_FRAME_SIZE, TCPConstants.DEFAULT_USE_DIRECT_BUFFER);
        byteBuffer.position(0);
        byteBuffer.limit(0);
        
        connection.setInputStreamByteBuffer(byteBuffer);
        
        return connection;
    }
    
    protected void finalize() throws Throwable {
        close();
    }
}
