/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPError;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
import com.sun.xml.ws.util.Pool;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public class TCPAdapter extends Adapter<TCPAdapter.TCPToolkit> {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    final String name;
    final String urlPattern;
    
    public TCPAdapter(@NotNull final String name, @NotNull final String urlPattern, @NotNull final WSEndpoint endpoint) {
        super(endpoint);
        this.name = name;
        this.urlPattern = urlPattern;
    }
    
    public void handle(@NotNull final ChannelContext channelContext) throws IOException, WSTCPException {
        final TCPConnectionImpl connection = new TCPConnectionImpl(channelContext);

        final Pool<TCPToolkit> currentPool = getPool();
        final TCPToolkit tk = currentPool.take();
        try {
            tk.handle(connection);
            connection.flush();
        } finally {
            currentPool.recycle(tk);
            connection.close();
        }
    }
    
    protected TCPAdapter.TCPToolkit createToolkit() {
        return new TCPToolkit();
    }
    
    /**
     * Returns the "/abc/def/ghi" portion if
     * the URL pattern is "/abc/def/ghi/*".
     */
    public String getValidPath() {
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0, urlPattern.length() - 2);
        } else {
            return urlPattern;
        }
    }
    
    public static void sendErrorResponse(@NotNull final ChannelContext channelContext,
            final WSTCPError message) throws IOException, WSTCPException {
        final TCPConnectionImpl connection = new TCPConnectionImpl(channelContext);
        connection.sendErrorMessage(message);
    }
    
    public class TCPToolkit extends Adapter.Toolkit implements TransportBackChannel {
        protected TCPConnectionImpl connection;
        private boolean isClosed;
        
        protected void handle(@NotNull final TCPConnectionImpl con) throws IOException, WSTCPException {
            connection = con;
            isClosed = false;
            
            final InputStream in = connection.openInput();
            final Codec currentCodec = getCodec(connection.getChannelContext());
            
            String ct = connection.getContentType();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1090_TCP_ADAPTER_REQ_CONTENT_TYPE(ct));
            }
            
            Packet packet = new Packet();
            currentCodec.decode(in, ct, packet);
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1091_TCP_ADAPTER_DECODED());
            }
            addCustomPacketSattellites(packet);
            packet = head.process(packet, connection, this);
            
            if (isClosed) {
                return;
            }
            
            ct = currentCodec.getStaticContentType(packet).getContentType();
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1092_TCP_ADAPTER_RPL_CONTENT_TYPE(ct));
            }
            if (ct == null) {
                throw new UnsupportedOperationException(MessagesMessages.WSTCP_0021_TCP_ADAPTER_UNSUPPORTER_OPERATION());
            } else {
                connection.setContentType(ct);
                if (packet.getMessage() == null) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, MessagesMessages.WSTCP_1093_TCP_ADAPTER_ONE_WAY());
                    }
                    connection.setStatus(TCPConstants.ONE_WAY);
                } else {
                    currentCodec.encode(packet, connection.openOutput());
                }
            }
        }
        
        // Taking Codec from virtual connection's ChannelContext
        protected @NotNull Codec getCodec(@NotNull final ChannelContext context) {
            return context.getCodec();
        }
        
        /**
         * Method could be overwritten by children to add some extra Satellites to Packet
         */
        public void addCustomPacketSattellites(@NotNull final Packet packet) {
        }
        
        public void close() {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1094_TCP_ADAPTER_CLOSE());
            }
            connection.setStatus(TCPConstants.ONE_WAY);
            isClosed = true;
        }
    };
    
    public static final AdapterFactory<TCPAdapter> FACTORY = new TCPAdapterList();
}
