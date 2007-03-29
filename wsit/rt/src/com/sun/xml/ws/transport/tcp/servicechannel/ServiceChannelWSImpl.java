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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
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
    
    public ChannelSettings openChannel(
            @WebParam(name="channelSettings", mode=WebParam.Mode.IN) ChannelSettings channelSettings)
            throws ServiceChannelException {
        
        final ChannelContext serviceChannelContext = getChannelContext();
        final ServerConnectionSession connectionSession = (ServerConnectionSession) serviceChannelContext.getConnectionSession();
        
        final WSTCPAdapterRegistry adapterRegistry = getAdapterRegistry();
        final WSTCPURI tcpURI = channelSettings.getTargetWSURI();
        
        final TCPAdapter adapter = adapterRegistry.getTarget(tcpURI);
        if (adapter == null) throw new ServiceChannelException(ServiceChannelErrorCode.UNKNOWN_ENDPOINT_ADDRESS, MessagesMessages.WSTCP_0034_WS_ENDPOINT_NOT_FOUND(tcpURI));
        
        final BindingUtils.NegotiatedBindingContent serviceSupportedContent =
                BindingUtils.getNegotiatedContentTypesAndParams(adapter.getEndpoint().getBinding());
        
        channelSettings.getNegotiatedMimeTypes().retainAll(serviceSupportedContent.negotiatedMimeTypes);
        if (channelSettings.getNegotiatedMimeTypes().size() == 0) {
            throw new ServiceChannelException(ServiceChannelErrorCode.CONTENT_NEGOTIATION_FAILED, MessagesMessages.WSTCP_0033_CONTENT_NEGOTIATION_FAILED(tcpURI, serviceSupportedContent.negotiatedMimeTypes));
        }
        
        channelSettings.getNegotiatedParams().retainAll(serviceSupportedContent.negotiatedParams);
        
        channelSettings.setChannelId(connectionSession.getNextAvailChannelId());
        final ChannelContext openedChannelContext = new ChannelContext(connectionSession, channelSettings);
        final SOAPVersion soapVersion = adapter.getEndpoint().getBinding().getSOAPVersion();
        final Codec defaultCodec = adapter.getEndpoint().createCodec();
        ChannelContext.configureCodec(openedChannelContext, soapVersion, defaultCodec);
        
        connectionSession.registerChannel(openedChannelContext);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1141_SOAPTCP_CHANNEL_OPEN(connectionSession.hashCode(), openedChannelContext.getChannelId(), tcpURI));
        }
        return channelSettings;
    }
    
    public void closeChannel(int channelId)  throws ServiceChannelException {
        final ChannelContext serviceChannelContext = getChannelContext();
        final ServerConnectionSession connectionSession = (ServerConnectionSession) serviceChannelContext.getConnectionSession();
        
        connectionSession.deregisterChannel(channelId);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, MessagesMessages.WSTCP_1142_SOAPTCP_CHANNEL_CLOSE(connectionSession.hashCode(), channelId));
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
