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
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.ChannelSettings;
import com.sun.xml.ws.transport.tcp.util.ConnectionSession;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
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
        logger.log(Level.FINE, "Session: {0} opened!", connectionSession.hashCode());
    }
    
    public void closeSession() throws ServiceChannelException {
        final ChannelContext serviceChannelContext = getChannelContext();
        final ConnectionSession connectionSession = serviceChannelContext.getConnectionSession();
        logger.log(Level.FINE, "Session: {0} closed!", connectionSession.hashCode());
    }
    
    public ChannelSettings openChannel(
            @WebParam(name="channelSettings", mode=WebParam.Mode.IN) ChannelSettings channelSettings)
            throws ServiceChannelException {
        
        final ChannelContext serviceChannelContext = getChannelContext();
        final ServerConnectionSession connectionSession = (ServerConnectionSession) serviceChannelContext.getConnectionSession();
        
        final WSTCPAdapterRegistry adapterRegistry = getAdapterRegistry();
        final WSTCPURI tcpURI = channelSettings.getTargetWSURI();
        
        final TCPAdapter adapter = adapterRegistry.getTarget(tcpURI);
        if (adapter == null) throw new ServiceChannelException(TCPConstants.WS_NOT_FOUND_ERROR, "Service " + channelSettings.getWSServiceName() + "(" + tcpURI.toString() + ") not found!");
        
        channelSettings.setChannelId(connectionSession.getNextAvailChannelId());
        final ChannelContext openedChannelContext = new ChannelContext(connectionSession, channelSettings);
        final SOAPVersion soapVersion = adapter.getEndpoint().getBinding().getSOAPVersion();
        final Codec defaultCodec = adapter.getEndpoint().createCodec();
        ChannelContext.configureCodec(openedChannelContext, soapVersion, defaultCodec);
        
        connectionSession.registerChannel(openedChannelContext);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Session: {0}. Channel #{1} was opened for WS: {2}",  new Object[] {connectionSession.hashCode(), openedChannelContext.getChannelId(), openedChannelContext.getWSServiceName()});
        }
        return channelSettings;
    }
    
    public void closeChannel(int channelId)  throws ServiceChannelException {
        final ChannelContext serviceChannelContext = getChannelContext();
        final ServerConnectionSession connectionSession = (ServerConnectionSession) serviceChannelContext.getConnectionSession();
        
        connectionSession.deregisterChannel(channelId);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Session: {0}. Channel #{1} was closed!", new Object[] {connectionSession.hashCode(), channelId});
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
