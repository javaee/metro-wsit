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

package com.sun.xml.ws.transport.tcp.client;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
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
    
    public ServiceChannelTransportPipe(@NotNull final ClientTubeAssemblerContext context) {
        super(context);
    }

    public ServiceChannelTransportPipe(ClientTubeAssemblerContext context, int customTCPPort) {
        super(context, customTCPPort);
    }
    
    private ServiceChannelTransportPipe(final ServiceChannelTransportPipe that, final TubeCloner cloner) {
        super(that, cloner);
    }
    
    @Override
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
                final ConnectionSession connectionSession = 
                        (ConnectionSession) packet.invocationProperties.get(TCPConstants.TCP_SESSION);
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
                logger.log(Level.SEVERE, MessagesMessages.WSTCP_0016_ERROR_WS_EXECUTION_ON_SERVER(clientTransport.getError()));
                throw new WSTCPException(clientTransport.getError());
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
    
    @Override
    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ServiceChannelTransportPipe(this, cloner);
    }
}
