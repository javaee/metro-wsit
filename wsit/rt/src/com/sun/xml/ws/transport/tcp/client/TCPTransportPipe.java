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
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.VersionMismatchException;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.io.InputStream;
import javax.xml.ws.WebServiceException;

/**
 * @author Alexey Stashok
 */
public class TCPTransportPipe implements Pipe {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    protected TCPClientTransport clientTransport;
    
    final protected Codec defaultCodec;
    final protected WSBinding wsBinding;
    final protected WSService wsService;
    
    public TCPTransportPipe(final ClientPipeAssemblerContext context) {
        this(context.getService(), context.getBinding(), context.getCodec());
    }
    
    protected TCPTransportPipe(final WSService wsService, final WSBinding wsBinding, final Codec defaultCodec) {
        this.wsService = wsService;
        this.wsBinding = wsBinding;
        this.defaultCodec = defaultCodec;
    }
    
    protected TCPTransportPipe(final TCPTransportPipe that, final PipeCloner cloner) {
        this(that.wsService, that.wsBinding, that.defaultCodec.copy());
        cloner.add(that, this);
    }
    
    public void preDestroy() {
        if (clientTransport != null) {
            WSConnectionManager.getInstance().closeChannel(clientTransport.getConnectionContext());
        }
    }
    
    public Pipe copy(final PipeCloner cloner) {
        return new TCPTransportPipe(this, cloner);
    }
    
    public Packet process(final Packet packet) {
        logger.log(Level.FINE, MessagesMessages.WSTCP_1010_TCP_TP_PROCESS_ENTER(packet.endpointAddress));
        ChannelContext channelContext = null;
        WebServiceException failure = null;
        final WSConnectionManager wsConnectionManager = WSConnectionManager.getInstance();
        
        int retryNum = 0;
        do {
            try {
                if (clientTransport == null) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1011_TCP_TP_PROCESS_TRANSPORT_CREATE());
                    clientTransport = createClientTransport(wsConnectionManager,
                            packet.endpointAddress.getURI());
                } else {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1012_TCP_TP_PROCESS_TRANSPORT_REUSE());
                }
                
                channelContext = clientTransport.getConnectionContext();
                
                wsConnectionManager.lockConnection(channelContext);
                
                // Taking Codec from ChannelContext
                final Codec codec = channelContext.getCodec();
                final ContentType ct = codec.getStaticContentType(packet);
                clientTransport.setContentType(ct.getContentType());
                
                logger.log(Level.FINE, MessagesMessages.WSTCP_1013_TCP_TP_PROCESS_ENCODE(ct.getContentType()));
                codec.encode(packet, clientTransport.openOutputStream());
                
                logger.log(Level.FINE, MessagesMessages.WSTCP_1014_TCP_TP_PROCESS_SEND());
                clientTransport.send();
                
                logger.log(Level.FINE, MessagesMessages.WSTCP_1015_TCP_TP_PROCESS_OPEN_PREPARE_READING());
                final InputStream replyInputStream = clientTransport.openInputStream();
                
                logger.log(Level.FINE, MessagesMessages.WSTCP_1016_TCP_TP_PROCESS_OPEN_PROCESS_READING(clientTransport.getStatus(), clientTransport.getContentType()));
                if (clientTransport.getStatus() != TCPConstants.ERROR) {
                    final Packet reply = packet.createClientResponse(null);
                    if (clientTransport.getStatus() != TCPConstants.ONE_WAY && !Boolean.FALSE.equals(packet.expectReply)) {
                        final String contentTypeStr = clientTransport.getContentType();
                        codec.decode(replyInputStream, contentTypeStr, reply);
                    }
                    return reply;
                } else {
                    logger.log(Level.SEVERE, MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(clientTransport.getContentType()));
                    throw new WebServiceException(MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(clientTransport.getContentType()));
                }
            } catch(ClientTransportException e) {
                prepareRetry(channelContext, retryNum, e);
                failure = e;
            } catch(IOException e) {
                prepareRetry(channelContext, retryNum, e);
                failure = new WebServiceException(MessagesMessages.WSTCP_0017_ERROR_WS_EXECUTION_ON_CLIENT(), e);
            } catch(Exception e) {
                retryNum = TCPConstants.CLIENT_MAX_FAIL_TRIES + 1;
                failure = new WebServiceException(MessagesMessages.WSTCP_0017_ERROR_WS_EXECUTION_ON_CLIENT(), e);
            } finally {
                if (channelContext != null) {
                    wsConnectionManager.freeConnection(channelContext);
                }
            }
            
        } while (failure != null && ++retryNum <= TCPConstants.CLIENT_MAX_FAIL_TRIES);
        
        assert failure != null;
        logger.log(Level.SEVERE, MessagesMessages.WSTCP_0001_MESSAGE_PROCESS_FAILED(), failure);
        throw failure;
    }
    
    private void prepareRetry(final ChannelContext channelContext, final int retryNum, final Exception e) {
        logger.log(Level.FINE, MessagesMessages.WSTCP_0012_SEND_RETRY(retryNum), e);
        clientTransport = null;
        if (channelContext != null) {
            WSConnectionManager.getInstance().abortConnection(channelContext);
        }
    }
    
    private @NotNull TCPClientTransport createClientTransport(@NotNull final WSConnectionManager wsConnectionManager,
            @NotNull final URI uri) throws InterruptedException, IOException, ServiceChannelException, VersionMismatchException {
        
        final WSTCPURI tcpURI = WSTCPURI.parse(uri);
        if (tcpURI == null) throw new WebServiceException(MessagesMessages.WSTCP_0005_INVALID_EP_URL(uri.toString()));
        final ChannelContext channelContext = wsConnectionManager.openChannel(tcpURI, wsService, wsBinding, defaultCodec);
        return new TCPClientTransport(channelContext);
    }
}
