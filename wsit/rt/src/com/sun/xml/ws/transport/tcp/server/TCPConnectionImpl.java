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

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ContentType;
import com.sun.xml.ws.transport.tcp.util.FrameType;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import com.sun.istack.NotNull;

/**
 * @author Alexey Stashok
 */
public class TCPConnectionImpl implements WebServiceContextDelegate {
    private final ChannelContext channelContext;
    private final Connection connection;
    
    private String contentType;
    private int replyStatus;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    private boolean isHeaderSerialized;
    
    public TCPConnectionImpl(final ChannelContext channelContext) {
        this.channelContext = channelContext;
        this.connection = channelContext.getConnection();
    }
    
    public InputStream openInput() throws IOException {
        inputStream = connection.openInputStream();
        contentType = channelContext.decodeContentType();
        return inputStream;
    }
    
    public OutputStream openOutput() {
        try {
            setMessageHeaders();
        } catch (IOException ex) {
        }
        
        outputStream = connection.openOutputStream();
        return outputStream;
    }
    
    public int getStatus() {
        return replyStatus;
    }
    
    public void setStatus(final int statusCode) {
        replyStatus = statusCode;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
    
    public void flush() throws IOException {
        if (outputStream == null) {
            setMessageHeaders();
            outputStream = connection.openOutputStream();
        }
        
        connection.flush();
    }
    
    public void close() {
    }
    
    // Not supported
    public Principal getUserPrincipal(final Packet request) {
        return null;
    }
    
    // Not supported
    public boolean isUserInRole(final Packet request, final String role) {
        return false;
    }
    
    public @NotNull String getEPRAddress(@NotNull final Packet request, @NotNull final WSEndpoint endpoint) {
        return channelContext.getTargetWSURI().toString();
    }

    public String getWSDLAddress(@NotNull final Packet request, 
            @NotNull final WSEndpoint endpoint) {
        return null;
    }
    
    private void setMessageHeaders() throws IOException {
        if (!isHeaderSerialized) {
            isHeaderSerialized = true;
            
            final int messageId = getMessageId();
            connection.setMessageId(messageId);
            if (FrameType.isFrameContainsParams(messageId)) {
                channelContext.encodeContentType(contentType);
            }
        }
    }
    
    private int getMessageId() {
        if (getStatus() == TCPConstants.ONE_WAY) {
            return FrameType.NULL;
        } else if (getStatus() != TCPConstants.OK) {
            return FrameType.ERROR;
        }
        
        return FrameType.MESSAGE;
    }
    
    public ChannelContext getChannelContext() {
        return channelContext;
    }
}
