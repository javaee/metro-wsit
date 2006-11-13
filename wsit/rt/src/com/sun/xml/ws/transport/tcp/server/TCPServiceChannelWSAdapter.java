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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.DistributedPropertySet;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;

/**
 * @author Alexey Stashok
 */
public class TCPServiceChannelWSAdapter extends TCPAdapter {
    private WSTCPAdapterRegistry adapterRegistry;
    
    public TCPServiceChannelWSAdapter(@NotNull String name,
            @NotNull String urlPattern,
    @NotNull WSEndpoint endpoint,
    @NotNull WSTCPAdapterRegistry adapterRegistry) {
        super(name, urlPattern, endpoint);
        this.adapterRegistry = adapterRegistry;
    }
    
    @Override
    protected TCPAdapter.TCPToolkit createToolkit() {
        return new ServiceChannelTCPToolkit();
    }
    
    
    class ServiceChannelTCPToolkit extends TCPAdapter.TCPToolkit {
        private ServiceChannelWSSatellite serviceChannelWSSatellite;
        
        public ServiceChannelTCPToolkit() {
            serviceChannelWSSatellite = new ServiceChannelWSSatellite(TCPServiceChannelWSAdapter.this);
        }
        
        // Taking Codec from virtual connection's ChannelContext
        @Override
        protected @NotNull Codec getCodec(@NotNull ChannelContext context) {
            return codec;
        }
        
        @Override
        protected void handle(@NotNull TCPConnectionImpl con) throws IOException {
            serviceChannelWSSatellite.setConnectionContext(con.getChannelContext());
            super.handle(con);
        }
        
        @Override
        public void addCustomPacketSattellites(@NotNull Packet packet) {
            super.addCustomPacketSattellites(packet);
            packet.addSatellite(serviceChannelWSSatellite);
        }
    };
    
    
    public static class ServiceChannelWSSatellite extends DistributedPropertySet {
        private TCPServiceChannelWSAdapter serviceChannelWSAdapter;
        private ChannelContext channelContext;
        
        ServiceChannelWSSatellite(@NotNull TCPServiceChannelWSAdapter serviceChannelWSAdapter) {
            this.serviceChannelWSAdapter = serviceChannelWSAdapter;
        }
        
        protected void setConnectionContext(ChannelContext channelContext) {
            this.channelContext = channelContext;
        }
        
        @Property(TCPConstants.ADAPTER_REGISTRY)
        public @NotNull WSTCPAdapterRegistry getAdapterRegistry() {
            return serviceChannelWSAdapter.adapterRegistry;
        }
        
        @Property(TCPConstants.CHANNEL_CONTEXT)
        public ChannelContext getChannelContext() {
            return channelContext;
        }
        
        private static PropertyMap model;
        static {
            model = parse(ServiceChannelWSSatellite.class);
        }
        
        public PropertySet.PropertyMap getPropertyMap() {
            return model;
        }
    }
}
