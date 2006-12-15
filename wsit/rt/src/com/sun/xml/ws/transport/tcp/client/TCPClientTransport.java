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

import com.sun.xml.ws.api.DistributedPropertySet;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ContentType;
import com.sun.xml.ws.transport.tcp.util.FrameType;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public class TCPClientTransport extends DistributedPropertySet {
    private static final long TIMEOUT_INTERVAL = 60000;
    private static final int COMPLETED = 0xFFFF;
    private static final int READ_TRY = 10;
    
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    private ChannelContext channelContext;
    private Connection connection;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    // Response status
    private int status;
    // Request/response content type
    private String contentType;
    
    public TCPClientTransport(ChannelContext channelContext) {
        this.channelContext = channelContext;
        this.connection = channelContext.getConnection();
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    /*
     * Getting output stream.
     * Making some stream preparation before
     */
    public OutputStream openOutputStream() {
        connection.setChannelId(channelContext.getChannelId());
        connection.setMessageId(FrameType.MESSAGE);
        ContentType.EncodedContentType ect = channelContext.encodeContentType(contentType);
        connection.setContentId(ect.mimeId);
        connection.setContentProps(ect.params);
        
        outputStream = connection.openOutputStream();
        return outputStream;
    }
    
    /*
     * Getting input stream.
     * Making some stream preparation before
     */
    public InputStream openInputStream() throws IOException {
        connection.prepareForReading();
        inputStream = connection.openInputStream();
        int messageId = connection.getMessageId();
        status = convertToReplyStatus(messageId);
        if (FrameType.isFrameContainsParams(messageId)) {
            contentType = channelContext.decodeContentType(connection.getContentId(),
                    connection.getContentProps());
        }
        
        return inputStream;
    }
    
    public void send() throws IOException {
        connection.flush();
    }
    
    public void close() {
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    private int convertToReplyStatus(int messageId) {
        if (messageId == FrameType.NULL) {
            return TCPConstants.ONE_WAY;
        } else if (messageId == FrameType.ERROR) {
            return TCPConstants.ERROR;
        }
        
        return TCPConstants.OK;
    }
    
    @Property(TCPConstants.CHANNEL_CONTEXT)
    public ChannelContext getConnectionContext() {
        return channelContext;
    }
    
    private static PropertyMap model;
    static {
        model = parse(TCPClientTransport.class);
    }
    
    public DistributedPropertySet.PropertyMap getPropertyMap() {
        return model;
    }
}
