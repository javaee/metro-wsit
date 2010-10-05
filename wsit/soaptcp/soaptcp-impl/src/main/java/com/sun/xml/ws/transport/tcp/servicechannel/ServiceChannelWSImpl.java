/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.ws.transport.tcp.servicechannel;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.transport.tcp.server.ServerConnectionSession;
import com.sun.xml.ws.transport.tcp.util.BindingUtils;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ChannelSettings;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.server.WSTCPAdapterRegistry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * @author Alexey Stashok
 */

@SuppressWarnings({"unchecked"})
@WebService
public class ServiceChannelWSImpl {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    @Resource
    private WebServiceContext wsContext;
    
    public void initiateSession() throws ServiceChannelException {
        final ChannelContext serviceChannelContext = getChannelContext();
        final ConnectionSession connectionSession = serviceChannelContext.getConnectionSession();
        logger.log(Level.FINE, MessagesMessages.WSTCP_1140_SOAPTCP_SESSION_OPEN(connectionSession.hashCode()));
    }
    
    @WebResult(name = "channelId")
    public int openChannel(
            @WebParam(name="targetWSURI", mode=WebParam.Mode.IN)String targetWSURI,
            @WebParam(name="negotiatedMimeTypes", mode=WebParam.Mode.INOUT) Holder<List<String>> negotiatedMimeTypes,
            @WebParam(name="negotiatedParams", mode=WebParam.Mode.INOUT) Holder<List<String>> negotiatedParams)
            throws ServiceChannelException {
        final ChannelContext serviceChannelContext = getChannelContext();
        final ServerConnectionSession connectionSession = (ServerConnectionSession) serviceChannelContext.getConnectionSession();
        
        final WSTCPAdapterRegistry adapterRegistry = getAdapterRegistry();
        
        final WSTCPURI tcpURI = WSTCPURI.parse(targetWSURI);
        final TCPAdapter adapter = adapterRegistry.getTarget(tcpURI);
        if (adapter == null) throw new ServiceChannelException(ServiceChannelErrorCode.UNKNOWN_ENDPOINT_ADDRESS, MessagesMessages.WSTCP_0034_WS_ENDPOINT_NOT_FOUND(targetWSURI));
        
        final BindingUtils.NegotiatedBindingContent serviceSupportedContent =
                BindingUtils.getNegotiatedContentTypesAndParams(adapter.getEndpoint().getBinding());
        
        negotiatedMimeTypes.value.retainAll(serviceSupportedContent.negotiatedMimeTypes);
        if (negotiatedMimeTypes.value.isEmpty()) {
            throw new ServiceChannelException(ServiceChannelErrorCode.CONTENT_NEGOTIATION_FAILED, MessagesMessages.WSTCP_0033_CONTENT_NEGOTIATION_FAILED(targetWSURI, serviceSupportedContent.negotiatedMimeTypes));
        }
        
        negotiatedParams.value.retainAll(serviceSupportedContent.negotiatedParams);
        
        int channelId = connectionSession.getNextAvailChannelId();
        ChannelSettings channelSettings = new ChannelSettings(negotiatedMimeTypes.value, negotiatedParams.value, channelId, adapter.getEndpoint().getServiceName(), tcpURI);
        final ChannelContext openedChannelContext = new ChannelContext(connectionSession, channelSettings);
        final SOAPVersion soapVersion = adapter.getEndpoint().getBinding().getSOAPVersion();
        final Codec defaultCodec = adapter.getEndpoint().createCodec();
        ChannelContext.configureCodec(openedChannelContext, soapVersion, defaultCodec);
        
        connectionSession.registerChannel(openedChannelContext);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1141_SOAPTCP_CHANNEL_OPEN(connectionSession.hashCode(), openedChannelContext.getChannelId(), targetWSURI));
        }
        return channelId;
    }
    
    public void closeChannel(
            @WebParam(name="channelId", mode=WebParam.Mode.IN) int channelId)  throws ServiceChannelException {
        final ChannelContext serviceChannelContext = getChannelContext();
        final ServerConnectionSession connectionSession = (ServerConnectionSession) serviceChannelContext.getConnectionSession();
        
        if (connectionSession.findWSServiceContextByChannelId(channelId) != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, MessagesMessages.WSTCP_1142_SOAPTCP_CHANNEL_CLOSE(connectionSession.hashCode(), channelId));
            }
            connectionSession.deregisterChannel(channelId);
        } else {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, MessagesMessages.WSTCP_0035_UNKNOWN_CHANNEL_UD("Session: " + connectionSession.hashCode() + " Channel-id: " + channelId));
            }
            throw new ServiceChannelException(ServiceChannelErrorCode.UNKNOWN_CHANNEL_ID, MessagesMessages.WSTCP_0035_UNKNOWN_CHANNEL_UD(channelId));
        }
    }
    
    private @NotNull ChannelContext getChannelContext() {
        final MessageContext messageContext = wsContext.getMessageContext();
        return (ChannelContext) messageContext.get(TCPConstants.CHANNEL_CONTEXT);
    }
    
    private @NotNull WSTCPAdapterRegistry getAdapterRegistry() {
        final MessageContext messageContext = wsContext.getMessageContext();
        return (WSTCPAdapterRegistry) messageContext.get(TCPConstants.ADAPTER_REGISTRY);
    }
}
