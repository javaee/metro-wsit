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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.DistributedPropertySet;
import com.sun.xml.ws.api.PropertySet;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.util.WSTCPException;
import java.io.IOException;

/**
 * @author Alexey Stashok
 */
public final class TCPServiceChannelWSAdapter extends TCPAdapter {
    private final WSTCPAdapterRegistry adapterRegistry;
    
    public TCPServiceChannelWSAdapter(@NotNull final String name,
            @NotNull final String urlPattern,
    @NotNull final WSEndpoint endpoint,
    @NotNull final WSTCPAdapterRegistry adapterRegistry) {
        super(name, urlPattern, endpoint);
        this.adapterRegistry = adapterRegistry;
    }
    
    @Override
    protected TCPAdapter.TCPToolkit createToolkit() {
        return new ServiceChannelTCPToolkit();
    }    
    
    class ServiceChannelTCPToolkit extends TCPAdapter.TCPToolkit {
        private final ServiceChannelWSSatellite serviceChannelWSSatellite;
        
        public ServiceChannelTCPToolkit() {
            serviceChannelWSSatellite = new ServiceChannelWSSatellite(TCPServiceChannelWSAdapter.this);
        }
        
        // Taking Codec from virtual connection's ChannelContext
        @Override
        protected @NotNull Codec getCodec(@NotNull final ChannelContext context) {
            return codec;
        }
        
        @Override
        protected void handle(@NotNull final TCPConnectionImpl con) throws IOException, WSTCPException {
            serviceChannelWSSatellite.setConnectionContext(con.getChannelContext());
            super.handle(con);
        }
        
        @Override
        public void addCustomPacketSattellites(@NotNull final Packet packet) {
            super.addCustomPacketSattellites(packet);
            packet.addSatellite(serviceChannelWSSatellite);
        }
    };
    
    
    public static final class ServiceChannelWSSatellite extends DistributedPropertySet {
        private final TCPServiceChannelWSAdapter serviceChannelWSAdapter;
        private ChannelContext channelContext;
        
        ServiceChannelWSSatellite(@NotNull final TCPServiceChannelWSAdapter serviceChannelWSAdapter) {
            this.serviceChannelWSAdapter = serviceChannelWSAdapter;
        }
        
        protected void setConnectionContext(final ChannelContext channelContext) {
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
        
        private static final PropertyMap model;
        static {
            model = parse(ServiceChannelWSSatellite.class);
        }
        
        public PropertySet.PropertyMap getPropertyMap() {
            return model;
        }
    }
}
