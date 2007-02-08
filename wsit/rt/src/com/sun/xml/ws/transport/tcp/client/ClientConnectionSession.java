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
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.SessionCloseListener;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
@SuppressWarnings({"unchecked"})
public final class ClientConnectionSession extends ConnectionSession {
    static {
        // Set dumb URI for service WS channel
        zeroChannelSettings.setTargetWSURI(WSTCPURI.parse(TCPConstants.PROTOCOL_SCHEMA + "://somehost:8080/service"));
    }

    private Map<String, Object> attributes = new HashMap<String, Object>(2);
    private Map<String, ChannelContext> url2ChannelMap = new HashMap<String, ChannelContext>();
    
    private final int dstAddressHashKey;
    
    private boolean isClosed;
    
    public ClientConnectionSession(final int dstAddressHashKey, final Connection connection, final SessionCloseListener sessionCloseListener) {
        super(connection, sessionCloseListener);
        this.dstAddressHashKey = dstAddressHashKey;
        init();
    }
    
    public void registerChannel(@NotNull final ChannelContext context) {
        url2ChannelMap.put(context.getTargetWSURI().toString(), context);
    }
        
    public void deregisterChannel(@NotNull final ChannelContext context) {
        String wsTCPURLString = context.getTargetWSURI().toString();
        ChannelContext channelToRemove = url2ChannelMap.get(wsTCPURLString);
        if (channelToRemove.getChannelId() == context.getChannelId()) {
            url2ChannelMap.remove(wsTCPURLString);
        }
    }
    
    public @Nullable ChannelContext findWSServiceContextByURI(@NotNull final WSTCPURI wsTCPURI) {
        return url2ChannelMap.get(wsTCPURI.toString());
    }

    public void onReadCompleted() {
        WSConnectionManager.getInstance().freeConnection(this);
    }
    
    public void close() {
        super.close();
        attributes = null;
    }
    
    public void setAttribute(@NotNull final String name, final Object value) {
        attributes.put(name, value);
    }
    
    public @Nullable Object getAttribute(@NotNull final String name) {
        return attributes.get(name);
    }
    
    public int getDstAddressHashKey() {
        return dstAddressHashKey;
    }
    
    public int getChannelsAmount() {
        return url2ChannelMap.size();
    }
}
