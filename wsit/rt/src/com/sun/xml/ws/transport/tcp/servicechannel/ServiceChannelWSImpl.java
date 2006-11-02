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

@WebService
public class ServiceChannelWSImpl {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    @Resource
    private WebServiceContext wsContext;
    
    public int initiateSession() {
        ChannelContext serviceChannelContext = getChannelContext();
        ConnectionSession connectionSession = serviceChannelContext.getConnectionSession();
        logger.log(Level.FINE, "Session: {0} opened!", connectionSession.hashCode());
        return 0;
    }
    
    public int closeSession() {
        ChannelContext serviceChannelContext = getChannelContext();
        ConnectionSession connectionSession = serviceChannelContext.getConnectionSession();
        logger.log(Level.FINE, "Session: {0} closed!", connectionSession.hashCode());
        return 0;
    }
    
    public ChannelSettings openChannel(
            @WebParam(name="channelSettings", mode=WebParam.Mode.IN) ChannelSettings channelSettings)
            throws ServiceChannelException {
        
        ChannelContext serviceChannelContext = getChannelContext();
        ConnectionSession connectionSession = serviceChannelContext.getConnectionSession();
        
        WSTCPAdapterRegistry adapterRegistry = getAdapterRegistry();
        WSTCPURI tcpURI = channelSettings.getTargetWSURI();
        
        TCPAdapter adapter = adapterRegistry.getTarget(tcpURI);
        if (adapter == null) throw new ServiceChannelException("Service " + channelSettings.getWSServiceName() + "(" + tcpURI.toString() + ") not found!");
        
        channelSettings.setChannelId(connectionSession.getNextAvailChannelId());
        ChannelContext openedChannelContext = new ChannelContext(connectionSession, channelSettings);
        SOAPVersion soapVersion = adapter.getEndpoint().getBinding().getSOAPVersion();
        Codec defaultCodec = adapter.getEndpoint().createCodec();
        ChannelContext.configureCodec(openedChannelContext, soapVersion, defaultCodec);
        
        connectionSession.registerChannel(openedChannelContext.getChannelId(), openedChannelContext);
        
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Session: {0}. Channel #{1} was opened for WS: {2}",  new Object[] {connectionSession.hashCode(), openedChannelContext.getChannelId(), openedChannelContext.getWSServiceName()});
        }
        return channelSettings;
    }
    
    public int closeChannel(int channelId) {
        ChannelContext serviceChannelContext = getChannelContext();
        ConnectionSession connectionSession = serviceChannelContext.getConnectionSession();
        
        connectionSession.deregisterChannel(channelId);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Session: {0}. Channel #{1} was closed!", new Object[] {connectionSession.hashCode(), channelId});
        }
        return 0;
    }
    
    private @NotNull ChannelContext getChannelContext() {
        MessageContext messageContext = wsContext.getMessageContext();
        return (ChannelContext) messageContext.get(TCPConstants.CHANNEL_CONTEXT);
    }
    
    private @NotNull WSTCPAdapterRegistry getAdapterRegistry() {
        MessageContext messageContext = wsContext.getMessageContext();
        return (WSTCPAdapterRegistry) messageContext.get(TCPConstants.ADAPTER_REGISTRY);
    }
}
