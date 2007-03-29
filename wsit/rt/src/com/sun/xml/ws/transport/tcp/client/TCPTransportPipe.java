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
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.VersionMismatchException;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
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
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

/**
 * @author Alexey Stashok
 */
public class TCPTransportPipe implements Pipe {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    protected TCPClientTransport clientTransport = new TCPClientTransport();
    
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
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1010_TCP_TP_PROCESS_ENTER(packet.endpointAddress));
        }
        ChannelContext channelContext = null;
        WebServiceException failure = null;
        final WSConnectionManager wsConnectionManager = WSConnectionManager.getInstance();
        
        int retryNum = 0;
        do {
            try {
                setupClientTransport(wsConnectionManager, packet.endpointAddress.getURI());
                channelContext = clientTransport.getConnectionContext();
                
                wsConnectionManager.lockConnection(channelContext.getConnectionSession());
                
                // Taking Codec from ChannelContext
                final Codec codec = channelContext.getCodec();
                final ContentType ct = codec.getStaticContentType(packet);
                clientTransport.setContentType(ct.getContentType());
                /* write transport SOAPAction header if required
                 * in HTTP this param is sent as HTTP header, in SOAP/TCP
                 * it is part of content-type (similar to SOAP 1.2) */
                writeTransportSOAPActionHeaderIfRequired(channelContext, ct, packet);
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1013_TCP_TP_PROCESS_ENCODE(ct.getContentType()));
                }
                codec.encode(packet, clientTransport.openOutputStream());
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1014_TCP_TP_PROCESS_SEND());
                }
                clientTransport.send();
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1015_TCP_TP_PROCESS_OPEN_PREPARE_READING());
                }
                final InputStream replyInputStream = clientTransport.openInputStream();
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, MessagesMessages.WSTCP_1016_TCP_TP_PROCESS_OPEN_PROCESS_READING(clientTransport.getStatus(), clientTransport.getContentType()));
                }
                if (clientTransport.getStatus() != TCPConstants.ERROR) {
                    final Packet reply = packet.createClientResponse(null);
                    if (clientTransport.getStatus() != TCPConstants.ONE_WAY && !Boolean.FALSE.equals(packet.expectReply)) {
                        final String contentTypeStr = clientTransport.getContentType();
                        codec.decode(replyInputStream, contentTypeStr, reply);
                    }
                    return reply;
                } else {
                    logger.log(Level.SEVERE, MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(clientTransport.getError()));
                    throw new WSTCPException(clientTransport.getError());
                }
            } catch(ClientTransportException e) {
                abortSession(channelContext);
                failure = e;
            } catch(WSTCPException e) {
                if (e.getError().isCritical()) {
                    abortSession(channelContext);
                } else {
                    releaseSession(channelContext);
                }
                failure = new WebServiceException(MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(e.getError()), e);
            } catch(IOException e) {
                abortSession(channelContext);
                failure = new WebServiceException(MessagesMessages.WSTCP_0017_ERROR_WS_EXECUTION_ON_CLIENT(), e);
            } catch(ServiceChannelException e) {
                releaseSession(channelContext);
                retryNum = TCPConstants.CLIENT_MAX_FAIL_TRIES + 1;
                failure = new WebServiceException(MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(e.getFaultInfo().getErrorCode() + ":" + e.getMessage()), e);
            } catch(Exception e) {
                abortSession(channelContext);
                retryNum = TCPConstants.CLIENT_MAX_FAIL_TRIES + 1;
                failure = new WebServiceException(MessagesMessages.WSTCP_0017_ERROR_WS_EXECUTION_ON_CLIENT(), e);
            }
            
            if (logger.isLoggable(Level.FINE) && canRetry(retryNum + 1)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_0012_SEND_RETRY(retryNum), failure);
            }
        } while (canRetry(++retryNum));
        
        assert failure != null;
        logger.log(Level.SEVERE, MessagesMessages.WSTCP_0001_MESSAGE_PROCESS_FAILED(), failure);
        throw failure;
    }
    
    protected void writeTransportSOAPActionHeaderIfRequired(ChannelContext channelContext, ContentType ct, Packet packet) {
        String soapActionTransportHeader = getSOAPAction(ct.getSOAPActionHeader(), packet);
        if (soapActionTransportHeader != null) {
            try {
                int transportSoapActionParamId = channelContext.encodeParam(TCPConstants.TRANSPORT_SOAP_ACTION_PROPERTY);
                channelContext.getConnection().setContentProperty(transportSoapActionParamId, soapActionTransportHeader);
            } catch (WSTCPException ex) {
                logger.log(Level.WARNING, MessagesMessages.WSTCP_0032_UNEXPECTED_TRANSPORT_SOAP_ACTION(), ex);
            }
        }
    }
    
    protected void abortSession(final ChannelContext channelContext) {
        if (channelContext != null) {
            WSConnectionManager.getInstance().abortConnection(channelContext.getConnectionSession());
        }
    }
    
    protected void releaseSession(final ChannelContext channelContext) {
        if (channelContext != null) {
            WSConnectionManager.getInstance().freeConnection(channelContext.getConnectionSession());
        }
    }
    
    private @NotNull void setupClientTransport(@NotNull final WSConnectionManager wsConnectionManager,
            @NotNull final URI uri) throws InterruptedException, IOException, ServiceChannelException, VersionMismatchException {
        
        final WSTCPURI tcpURI = WSTCPURI.parse(uri);
        if (tcpURI == null) throw new WebServiceException(MessagesMessages.WSTCP_0005_INVALID_EP_URL(uri.toString()));
        final ChannelContext channelContext = wsConnectionManager.openChannel(tcpURI, wsService, wsBinding, defaultCodec);
        clientTransport.setup(channelContext);
    }
    
    /**
     * get SOAPAction header if the soapAction parameter is non-null or BindingProvider properties set.
     * BindingProvider properties take precedence.
     */
    private @Nullable String getSOAPAction(String soapAction, Packet packet) {
        Boolean useAction = (Boolean) packet.invocationProperties.get(BindingProvider.SOAPACTION_USE_PROPERTY);
        String sAction = null;
        boolean use = (useAction != null) ? useAction.booleanValue() : false;
        
        if (use) {
            //TODO check if it needs to be quoted
            sAction = packet.soapAction;
        }
        //request Property soapAction overrides wsdl
        if (sAction != null) {
            return sAction;
        } else {
            return soapAction;
        }
    }
    
    private static boolean canRetry(int retryNum) {
        return retryNum <= TCPConstants.CLIENT_MAX_FAIL_TRIES;
    }
}
