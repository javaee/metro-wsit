/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.transport.tcp.client;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.transport.tcp.io.Connection;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.SessionCloseListener;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Stashok
 */
@SuppressWarnings({"unchecked"})
public final class ClientConnectionSession extends ConnectionSession {
    private Map<String, Object> attributes = new HashMap<String, Object>(2);
    private Map<String, ChannelContext> url2ChannelMap = new HashMap<String, ChannelContext>();
    
    public ClientConnectionSession(final Connection connection, final SessionCloseListener sessionCloseListener) {
        super(connection, sessionCloseListener);
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
    
    public int getChannelsAmount() {
        return url2ChannelMap.size();
    }
}
