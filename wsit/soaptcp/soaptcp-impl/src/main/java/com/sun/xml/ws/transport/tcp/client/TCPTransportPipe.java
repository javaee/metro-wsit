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
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.TubeCloner;
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
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.io.InputStream;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

/**
 * @author Alexey Stashok
 */
public class TCPTransportPipe extends AbstractTubeImpl {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".client");
    
    protected TCPClientTransport clientTransport = new TCPClientTransport();
    
    final protected Codec defaultCodec;
    final protected WSBinding wsBinding;
    final protected WSService wsService;
    final protected int customTCPPort;
    
    public TCPTransportPipe(final ClientTubeAssemblerContext context) {
        this(context, -1);
    }

    public TCPTransportPipe(ClientTubeAssemblerContext context, int customTCPPort) {
        this(context.getService(), context.getBinding(), context.getCodec(), customTCPPort);
    }
    
    protected TCPTransportPipe(final WSService wsService, final WSBinding wsBinding, 
            final Codec defaultCodec, final int customTCPPort) {
        this.wsService = wsService;
        this.wsBinding = wsBinding;
        this.defaultCodec = defaultCodec;
        this.customTCPPort = customTCPPort;
    }
    
    protected TCPTransportPipe(final TCPTransportPipe that, final TubeCloner cloner) {
        this(that.wsService, that.wsBinding, that.defaultCodec.copy(), that.customTCPPort);
        cloner.add(that, this);
    }

    public void preDestroy() {
        if (clientTransport != null && clientTransport.getConnectionContext() != null) {
            WSConnectionManager.getInstance().closeChannel(clientTransport.getConnectionContext());
        }
    }

    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new TCPTransportPipe(this, cloner);
    }

    public NextAction processRequest(Packet request) {
        return doReturnWith(process(request));
    }

    public NextAction processResponse(Packet response) {
        throw new IllegalStateException("TCPTransportPipe's processResponse shouldn't be called.");
    }

    public NextAction processException(Throwable t) {
        throw new IllegalStateException("TCPTransportPipe's processException shouldn't be called.");
    }

    @Override
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
                    if (clientTransport.getStatus() != TCPConstants.ONE_WAY) {
                        final String contentTypeStr = clientTransport.getContentType();
                        codec.decode(replyInputStream, contentTypeStr, reply);
                    } else {
                        releaseSession(channelContext);
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
        tcpURI.setCustomPort(customTCPPort);
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
