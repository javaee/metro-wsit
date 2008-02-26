/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.io.DataInOutUtils;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.FrameType;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.ws.transport.tcp.util.WSTCPError;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
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
    
    public InputStream openInput() throws IOException, WSTCPException {
        inputStream = connection.openInputStream();
        contentType = channelContext.getContentType();
        return inputStream;
    }
    
    public OutputStream openOutput() throws IOException, WSTCPException {
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
    
    public void flush() throws IOException, WSTCPException {
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
    
    public void sendErrorMessage(WSTCPError message) throws IOException, WSTCPException {
        setStatus(TCPConstants.ERROR);
        OutputStream output = openOutput();
        String description = message.getDescription();
        DataInOutUtils.writeInts4(output, message.getCode(), message.getSubCode(), description.length());
        output.write(description.getBytes(TCPConstants.UTF8));
        flush();
    }

    private void setMessageHeaders() throws IOException, WSTCPException {
        if (!isHeaderSerialized) {
            isHeaderSerialized = true;
            
            final int messageId = getMessageId();
            connection.setMessageId(messageId);
            if (FrameType.isFrameContainsParams(messageId)) {
                channelContext.setContentType(contentType);
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
