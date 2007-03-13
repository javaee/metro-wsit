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
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
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
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1001_TCP_SERVICE_TP_PROCESS_ENTER(packet.endpointAddress));
        }
        ChannelContext channelContext = null;
        final WSConnectionManager wsConnectionManager = WSConnectionManager.getInstance();
        
        try {
            final ContentType ct = defaultCodec.getStaticContentType(packet);
            
            channelContext = clientTransport.getConnectionContext();
            if (channelContext != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1002_TCP_SERVICE_TP_PROCESS_TRANSPORT_REUSE());
                }
                wsConnectionManager.lockConnection(channelContext.getConnectionSession());
            } else {
                // Initiate new connection session
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1003_TCP_SERVICE_TP_PROCESS_TRANSPORT_CREATE());
                }
                final ConnectionSession connectionSession = (ConnectionSession) packet.invocationProperties.get(TCPConstants.TCP_SESSION);
                channelContext = connectionSession.getServiceChannelContext();
                clientTransport.setup(channelContext);
            }
            
            clientTransport.setContentType(ct.getContentType());
            /* write transport SOAPAction header if required
             * in HTTP this param is sent as HTTP header, in SOAP/TCP
             * it is part of content-type (similar to SOAP 1.2) */
            writeTransportSOAPActionHeaderIfRequired(channelContext, ct, packet);
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1004_TCP_SERVICE_TP_PROCESS_ENCODE(ct.getContentType()));
            }
            defaultCodec.encode(packet, clientTransport.openOutputStream());
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1005_TCP_SERVICE_TP_PROCESS_SEND());
            }
            clientTransport.send();
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1006_TCP_SERVICE_TP_PROCESS_OPEN_PREPARE_READING());
            }
            final InputStream replyInputStream = clientTransport.openInputStream();
            
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1007_TCP_SERVICE_TP_PROCESS_OPEN_PROCESS_READING(clientTransport.getStatus(), clientTransport.getContentType()));
            }
            if (clientTransport.getStatus() != TCPConstants.ERROR) {
                final String contentTypeStr = clientTransport.getContentType();
                
                final Packet reply = packet.createClientResponse(null);
                defaultCodec.decode(replyInputStream, contentTypeStr, reply);
                
                reply.addSatellite(clientTransport);
                return reply;
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.SEVERE, MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(clientTransport.getContentType()));
                }
                throw new WebServiceException(MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(clientTransport.getContentType()));
            }
        } catch(WebServiceException wex) {
            abortSession(channelContext);
            throw wex;
        } catch(Exception ex) {
            abortSession(channelContext);
            clientTransport.setup(null);
            
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_0017_ERROR_WS_EXECUTION_ON_CLIENT(), ex);
            throw new WebServiceException(MessagesMessages.WSTCP_0017_ERROR_WS_EXECUTION_ON_CLIENT(), ex);
        }
    }
    
    public Pipe copy(final PipeCloner cloner) {
        return new ServiceChannelTransportPipe(this, cloner);
    }
    
}
