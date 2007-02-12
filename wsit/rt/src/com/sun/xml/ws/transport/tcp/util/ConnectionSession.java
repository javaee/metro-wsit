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

package com.sun.xml.ws.transport.tcp.util;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.transport.tcp.io.Connection;
import java.io.IOException;

/**
 * @author Alexey Stashok
 */
@SuppressWarnings({"unchecked"})
public abstract class ConnectionSession implements com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.Connection {
    protected static final ChannelSettings zeroChannelSettings = new ChannelSettings();
    
    private Connection connection;
    
    private boolean isClosed;
    
    private ChannelContext zeroChannelContext;
    private final SessionCloseListener sessionCloseListener;
    
    public abstract void registerChannel(@NotNull final ChannelContext context);
    
    public abstract void deregisterChannel(@NotNull final ChannelContext context);
    
    public abstract int getChannelsAmount();
    
    public ConnectionSession(final Connection connection, final SessionCloseListener sessionCloseListener) {
        this.connection = connection;
        this.sessionCloseListener = sessionCloseListener;
    }
    
    protected void init() {
        zeroChannelContext = new ChannelContext(this, zeroChannelSettings);
        registerChannel(zeroChannelContext);
    }
    
    // Stub for getAttribute
    public @Nullable Object getAttribute(@NotNull final String name) {return null;}
    
    // Stub for setAttribute
    public void setAttribute(@NotNull final String name, @Nullable final Object value) {}
    
    // Stub for read completed event processing
    public void onReadCompleted() {}
    
    public @Nullable ChannelContext findWSServiceContextByURI(@NotNull final WSTCPURI wsTCPURI) {return null;}
    
    public @Nullable ChannelContext findWSServiceContextByChannelId(final int channelId) {return null;}
    
    public @NotNull ChannelContext getServiceChannelContext() {
        return zeroChannelContext;
    }
    
    public void close() {
        if (sessionCloseListener != null) {
            sessionCloseListener.notifySessionClose(this);
        }
        
        synchronized(this) {
            if (isClosed) return;
            isClosed = true;
        }
        
        try {
            connection.close();
        } catch (IOException ex) {
        }
        
        connection = null;
    }
    
    public Connection getConnection() {
        return connection;
    }

}
