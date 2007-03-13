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
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.DistributedPropertySet;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.io.DataInOutUtils;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.FrameType;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPError;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Alexey Stashok
 */
public class TCPClientTransport extends DistributedPropertySet {
    private ChannelContext channelContext;
    private Connection connection;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    // Response status
    private int status;
    // Request/response content type
    private String contentType;
    
    private WSTCPError error;
    
    public TCPClientTransport() {
    }
    
    public TCPClientTransport(final @NotNull ChannelContext channelContext) {
        setup(channelContext);
    }
    
    public void setup(final @Nullable ChannelContext channelContext) {
        this.channelContext = channelContext;
        if (channelContext != null) {
            this.connection = channelContext.getConnection();
        }
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(final int status) {
        this.status = status;
    }
    
    /*
     * Getting output stream.
     * Making some stream preparation before
     */
    public @NotNull OutputStream openOutputStream() throws WSTCPException {
        connection.setChannelId(channelContext.getChannelId());
        connection.setMessageId(FrameType.MESSAGE);
        channelContext.setContentType(contentType);
        
        outputStream = connection.openOutputStream();
        return outputStream;
    }
    
    /*
     * Getting input stream.
     * Making some stream preparation before
     */
    public @NotNull InputStream openInputStream() throws IOException, WSTCPException {
        connection.prepareForReading();
        inputStream = connection.openInputStream();
        final int messageId = connection.getMessageId();
        status = convertToReplyStatus(messageId);
        if (FrameType.isFrameContainsParams(messageId)) {
            contentType = channelContext.getContentType();
        }
        
        if (status == TCPConstants.ERROR) {
            error = parseErrorMessagePayload();
        }
        
        return inputStream;
    }
    
    public void send() throws IOException {
        connection.flush();
    }
    
    public void close() {
        error = null;
        // Perform some cleanings
    }
    
    public void setContentType(final @NotNull String contentType) {
        this.contentType = contentType;
    }
    
    public @Nullable String getContentType() {
        return contentType;
    }
    
    public @Nullable WSTCPError getError() {
        return error;
    }

    private @Nullable WSTCPError parseErrorMessagePayload() throws IOException {
        final int[] params = new int[3];
        DataInOutUtils.readInts4(inputStream, params, 3);
        final int errorCode = params[0];
        final int errorSubCode = params[1];
        final int errorDescriptionBufferLength = params[2];
        
        final byte[] errorDescriptionBuffer = new byte[errorDescriptionBufferLength];
        DataInOutUtils.readFully(inputStream, errorDescriptionBuffer);
        
        String errorDescription = new String(errorDescriptionBuffer, TCPConstants.UTF8);
        return WSTCPError.createError(errorCode, errorSubCode, errorDescription);
    }

    private int convertToReplyStatus(final int messageId) {
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
    
    private static final PropertyMap model;
    static {
        model = parse(TCPClientTransport.class);
    }
    
    public DistributedPropertySet.PropertyMap getPropertyMap() {
        return model;
    }
}
