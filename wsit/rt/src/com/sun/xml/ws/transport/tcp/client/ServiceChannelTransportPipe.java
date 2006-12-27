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
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.WebServiceException;

/**
 * @author Alexey Stashok
 */
public final class ServiceChannelTransportPipe extends TCPTransportPipe {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    public ServiceChannelTransportPipe(@NotNull final ClientPipeAssemblerContext context) {
        super(context);
    }
    
    private ServiceChannelTransportPipe(final ServiceChannelTransportPipe that, final PipeCloner cloner) {
        super(that, cloner);
    }
    
    public Packet process(final Packet packet) {
        logger.log(Level.FINE, "ServiceChannelTransportPipe.process entering");
        ChannelContext channelContext = null;
        final WSConnectionManager wsConnectionManager = WSConnectionManager.getInstance();
        
        try {
            final Codec codec = pipeAssemblerContext.getCodec();
            final ContentType ct = codec.getStaticContentType(packet);
            
            if (clientTransport != null) {
                logger.log(Level.FINE, "ServiceChannelTransportPipe.process use client transport");
                channelContext = clientTransport.getConnectionContext();
                wsConnectionManager.lockConnection(channelContext);
            } else {
                // Initiate new connection session
                logger.log(Level.FINE, "ServiceChannelTransportPipe.process create client transport");
                final ConnectionSession connectionSession = (ConnectionSession) packet.invocationProperties.get(TCPConstants.TCP_SESSION);
                channelContext = connectionSession.findWSServiceContextByChannelId(0);
                clientTransport = new TCPClientTransport(channelContext);
            }
            
            clientTransport.setContentType(ct.getContentType());
            logger.log(Level.FINE, "ServiceChannelTransportPipe.process encode. ContentType: {0}", ct.getContentType());
            codec.encode(packet, clientTransport.openOutputStream());
            
            logger.log(Level.FINE, "ServiceChannelTransportPipe.process send");
            clientTransport.send();
            
            logger.log(Level.FINE, "ServiceChannelTransportPipe.process openInputStream");
            final InputStream replyInputStream = clientTransport.openInputStream();
            
            logger.log(Level.FINE, "ServiceChannelTransportPipe.process process input data");
            if (clientTransport.getStatus() != TCPConstants.ERROR) {
                final String contentTypeStr = clientTransport.getContentType();
                logger.log(Level.FINE, "ServiceChannelTransportPipe.process; received content-type: {0}", contentTypeStr);
                
                final Packet reply = packet.createClientResponse(null);
                codec.decode(replyInputStream, contentTypeStr, reply);
                
                reply.addSatellite(clientTransport);
                return reply;
            } else {
                throw new WebServiceException(clientTransport.getContentType());
            }
        } catch(WebServiceException wex) {
            throw wex;
        } catch(Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            if (channelContext != null) {
                wsConnectionManager.abortConnection(channelContext);
            }
            clientTransport = null;
            throw new WebServiceException(ex);
        } finally {
            if (channelContext != null) {
                wsConnectionManager.freeConnection(channelContext);
            }
        }
    }
    
    public Pipe copy(final PipeCloner cloner) {
        return new ServiceChannelTransportPipe(this, cloner);
    }
    
}
