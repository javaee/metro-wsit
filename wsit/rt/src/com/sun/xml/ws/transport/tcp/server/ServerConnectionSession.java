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
import com.sun.xml.ws.transport.tcp.util.SessionCloseListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
public final class ServerConnectionSession extends ConnectionSession {
    private Map<Integer, ChannelContext> channelId2context = 
            new HashMap<Integer, ChannelContext>();
    
    private int channelCounter;
    
    public ServerConnectionSession(final Connection connection, final SessionCloseListener<ServerConnectionSession> sessionCloseListener) {
        super(connection, sessionCloseListener);
        channelCounter = 1;
        init();
    }
    
    public void registerChannel(@NotNull final ChannelContext context) {
        channelId2context.put(context.getChannelId(), context);
    }
    
    public @Nullable ChannelContext findWSServiceContextByChannelId(final int channelId) {
        return channelId2context.get(channelId);
    }
    
    public void deregisterChannel(final int channelId) {
        channelId2context.remove(channelId);
    }

    public void deregisterChannel(@NotNull final ChannelContext context) {
        deregisterChannel(context.getChannelId());
    }
    
    public void close() {
        super.close();

        channelId2context = null;
    }

    public int getChannelsAmount() {
        return channelId2context.size();
    }
    
    public synchronized int getNextAvailChannelId() {
        return channelCounter++;
    }    

}
