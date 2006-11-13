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
    
    final protected ClientPipeAssemblerContext pipeAssemblerContext;
    
    public TCPTransportPipe(ClientPipeAssemblerContext context) {
        this.pipeAssemblerContext = context;
    }
    
    protected TCPTransportPipe(TCPTransportPipe that, PipeCloner cloner) {
        this(that.pipeAssemblerContext);
        cloner.add(that, this);
    }
    
    public void preDestroy() {
        if (clientTransport != null) {
            WSConnectionManager.getInstance().closeChannel(clientTransport.getConnectionContext());
        }
    }
    
    public Pipe copy(PipeCloner cloner) {
        return new TCPTransportPipe(this, cloner);
    }
    
    public Packet process(Packet packet) {
        logger.log(Level.FINE, "TCPTransportPipe.process entering");
        ChannelContext channelContext = null;
        WebServiceException failure = null;
        WSConnectionManager wsConnectionManager = WSConnectionManager.getInstance();
        
        int retryNum = 0;
        do {
            try {
                if (clientTransport == null) {
                    clientTransport = createClientTransport(wsConnectionManager,
                            packet.endpointAddress.getURI());
                }
                
                channelContext = clientTransport.getConnectionContext();
                
                wsConnectionManager.lockConnection(channelContext);
                
                // Taking Codec from ChannelContext
                Codec codec = channelContext.getCodec();
                ContentType ct = codec.getStaticContentType(packet);
                
                logger.log(Level.FINE, "TCPTransportPipe.process; send content-type: {0}", ct.getContentType());
                clientTransport.setContentType(ct.getContentType());
                
                logger.log(Level.FINE, "TCPTransportPipe.process: encode");
                codec.encode(packet, clientTransport.openOutputStream());
                
                logger.log(Level.FINE, "TCPTransportPipe.process: send");
                clientTransport.send();
                
                logger.log(Level.FINE, "TCPTransportPipe.process openInputStream");
                InputStream replyInputStream = clientTransport.openInputStream();
                
                logger.log(Level.FINE, "TCPTransportPipe.process process input data");
                if (clientTransport.getStatus() != TCPConstants.ERROR) {
                    Packet reply = packet.createClientResponse(null);
                    if (clientTransport.getStatus() != TCPConstants.ONE_WAY && !Boolean.FALSE.equals(packet.expectReply)) {
                        String contentTypeStr = clientTransport.getContentType();
                        logger.log(Level.FINE, "TCPTransportPipe.process; received content-type: {0}", contentTypeStr);
                        codec.decode(replyInputStream, contentTypeStr, reply);
                    }
                    return reply;
                } else {
                    throw new WebServiceException(clientTransport.getContentType());
                }
            } catch(ClientTransportException e) {
                prepareRetry(channelContext, retryNum, e);
                failure = new WebServiceException(e);
            } catch(IOException e) {
                prepareRetry(channelContext, retryNum, e);
                failure = new WebServiceException(e);
            } catch(Exception e) {
                retryNum = TCPConstants.CLIENT_MAX_FAIL_TRIES + 1;
                failure = new WebServiceException(e);
            } finally {
                if (channelContext != null) {
                    wsConnectionManager.freeConnection(channelContext);
                }
            }
            
        } while (failure != null && ++retryNum <= TCPConstants.CLIENT_MAX_FAIL_TRIES);
        
        assert failure != null;
        logger.log(Level.SEVERE, MessagesMessages.MESSAGE_PROCESS_FAILED(), failure);
        throw failure;
    }
    
    private void prepareRetry(ChannelContext channelContext, int retryNum, Exception e) {
        logger.log(Level.WARNING, MessagesMessages.SEND_RETRY(retryNum), e);
        clientTransport = null;
        if (channelContext != null) {
            WSConnectionManager.getInstance().abortConnection(channelContext);
        }
    }
    
    private @NotNull TCPClientTransport createClientTransport(@NotNull WSConnectionManager wsConnectionManager,
            @NotNull URI uri) throws InterruptedException, IOException, ServiceChannelException, VersionMismatchException {
        
        logger.log(Level.FINE, "TCPTransportPipe.createClientTransport");
        
        WSTCPURI tcpURI = WSTCPURI.parse(uri);
        if (tcpURI == null) throw new WebServiceException(MessagesMessages.INVALID_EP_URL());
        ChannelContext channelContext = wsConnectionManager.openChannel(tcpURI, pipeAssemblerContext);
        return new TCPClientTransport(channelContext);
    }
}
