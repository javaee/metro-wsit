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
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.api.server.TransportBackChannel;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.MimeType;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public class TCPAdapter<TCPTK extends TCPAdapter.TCPToolkit> extends Adapter<TCPAdapter.TCPToolkit> {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    final String name;
    final String urlPattern;
    
    public TCPAdapter(@NotNull String name, @NotNull String urlPattern, @NotNull WSEndpoint endpoint) {
        super(endpoint);
        this.name = name;
        this.urlPattern = urlPattern;
    }
    
    public void handle(@NotNull ChannelContext channelContext) throws IOException {
        TCPConnectionImpl connection = new TCPConnectionImpl(channelContext);
        
        TCPToolkit tk = pool.take();
        try {
            tk.handle(connection);
            connection.flush();
        } finally {
            pool.recycle(tk);
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
    
    public static void sendErrorResponse(@NotNull ChannelContext channelContext,
            int errorCode,
            @NotNull String errorDescription) throws IOException {
        TCPConnectionImpl connection = new TCPConnectionImpl(channelContext);
        try {
            StringBuffer contentType = new StringBuffer();
            contentType.append(MimeType.ERROR.getMimeType());
            contentType.append(';');
            contentType.append(TCPConstants.ERROR_CODE_PROPERTY);
            contentType.append('=');
            contentType.append(errorCode);
            contentType.append(';');
            contentType.append(TCPConstants.ERROR_DESCRIPTION_PROPERTY);
            contentType.append('=');
            contentType.append(errorDescription);
            connection.setContentType(contentType.toString());
            connection.setStatus(TCPConstants.ERROR);
            connection.flush();
        } finally {
            connection.close();
        }
    }
    
    protected class TCPToolkit extends Adapter.Toolkit implements TransportBackChannel {
        protected TCPConnectionImpl connection;
        private boolean isClosed;
        
        protected void handle(@NotNull TCPConnectionImpl con) throws IOException {
            connection = con;
            isClosed = false;
            
            InputStream in = connection.openInput();
            Codec codec = getCodec(connection.getChannelContext());
            
            String ct = connection.getContentType();
            logger.log(Level.FINE, "TCPAdapter.TCPToolkit.handle; received content-type: {0}", ct);
            
            Packet packet = new Packet();
            codec.decode(in, ct, packet);
            logger.log(Level.FINE, "TCPAdapter.TCPToolkit.handle decoded");
            addCustomPacketSattellites(packet);
            try {
                logger.log(Level.FINE, "TCPAdapter.TCPToolkit.handle head.process");
                packet = head.process(packet, connection, this);
            } catch(Exception e) {
                if (!isClosed) {
                    writeInternalServerError();
                }
                return;
            }
            
            if (isClosed) {
                return;
            }
            
            ct = codec.getStaticContentType(packet).getContentType();
            if (ct == null) {
                throw new UnsupportedOperationException();
            } else {
                connection.setContentType(ct);
                if (packet.getMessage() == null) {
                    logger.log(Level.FINE, "TCPAdapter.TCPToolkit.handle; OneWay");
                    connection.setStatus(TCPConstants.ONE_WAY);
                } else {
                    logger.log(Level.FINE, "TCPAdapter.TCPToolkit.handle; send content-type: {0}", ct);
                    codec.encode(packet, connection.openOutput());
                }
            }
        }
        
        // Taking Codec from virtual connection's ChannelContext
        protected @NotNull Codec getCodec(@NotNull ChannelContext context) {
            return context.getCodec();
        }
        
        /**
         * Method could be overwritten by children to add some extra Satellites to Packet
         */
        public void addCustomPacketSattellites(@NotNull Packet packet) {
        }
        
        public void close() {
            logger.log(Level.FINE, "TCPAdapter.TCPToolkit.close; OneWay");
            connection.setStatus(TCPConstants.ONE_WAY);
            isClosed = true;
        }
        
        private void writeInternalServerError() {
            logger.log(Level.FINE, "TCPAdapter.TCPToolkit.writeInternalServerError");
            connection.setStatus(TCPConstants.RS_INTERNAL_ERROR);
        }
    };
    
    public static final AdapterFactory<TCPAdapter> FACTORY = new TCPAdapterList();
}
