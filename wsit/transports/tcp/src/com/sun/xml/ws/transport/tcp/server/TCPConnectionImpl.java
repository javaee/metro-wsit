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

import com.sun.xml.ws.api.DistributedPropertySet;
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
public class TCPConnectionImpl extends DistributedPropertySet implements WebServiceContextDelegate {
    private ChannelContext connectionContext;
    private Connection connection;
    
    private String contentType;
    private int replyStatus;
    
    private InputStream inputStream;
    private OutputStream outputStream;
    
    private boolean isHeaderSerialized;
    
    public TCPConnectionImpl(ChannelContext connectionContext) {
        this.connectionContext = connectionContext;
        this.connection = connectionContext.getConnection();
    }
    
    public InputStream openInput() throws IOException {
        inputStream = connection.openInputStream();
        contentType = connectionContext.decodeContentType(connection.getContentId(),
                connection.getContentProps());
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
    
    public void setStatus(int statusCode) {
        replyStatus = statusCode;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
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
    public Principal getUserPrincipal(Packet request) {
        return null;
    }
    
    // Not supported
    public boolean isUserInRole(Packet request, String role) {
        return false;
    }
    
    public @NotNull String getEPRAddress(@NotNull Packet request, @NotNull WSEndpoint endpoint) {
        return connectionContext.getTargetWSURI().toString();
    }

    private void setMessageHeaders() throws IOException {
        if (!isHeaderSerialized) {
            isHeaderSerialized = true;
            
            int messageId = getMessageId();
            connection.setMessageId(messageId);
            if (FrameType.isFrameContainsParams(messageId)) {
                ContentType.EncodedContentType ect = connectionContext.encodeContentType(contentType);
                connection.setContentId(ect.mimeId);
                connection.setContentProps(ect.params);
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
    
    @Property(TCPConstants.CHANNEL_CONTEXT)
    public ChannelContext getConnectionContext() {
        return connectionContext;
    }
    
    private static PropertyMap model;
    static {
        model = parse(TCPConnectionImpl.class);
    }
    
    public DistributedPropertySet.PropertyMap getPropertyMap() {
        return model;
    }

}
