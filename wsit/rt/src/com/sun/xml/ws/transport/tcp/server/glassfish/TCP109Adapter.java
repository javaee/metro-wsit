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

package com.sun.xml.ws.transport.tcp.server.glassfish;

import com.sun.enterprise.webservice.EjbRuntimeEndpointInfo;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.util.ChannelContext;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import java.io.IOException;

/**
 * @author Alexey Stashok
 */
public class TCP109Adapter extends TCPAdapter {
    
    /**
     * Currently 109 deployed WS's pipeline relies on Servlet request and response
     * attributes. So its temporary workaround to make 109 work with TCP
     */
    private ServletFakeArtifactSet servletFakeArtifactSet;
    private boolean isEJB;
    private String contextRoot;
    
    public TCP109Adapter(
            @NotNull String name,
    @NotNull String contextRoot,
    @NotNull String urlPattern,
    @NotNull WSEndpoint endpoint,
    @NotNull ServletFakeArtifactSet servletFakeArtifactSet,
    boolean isEJB) {
        super(name, urlPattern, endpoint);
        this.contextRoot = contextRoot;
        this.servletFakeArtifactSet = servletFakeArtifactSet;
        this.isEJB = isEJB;
    }
    
    
    @Override
    public void handle(@NotNull ChannelContext channelContext) throws IOException {
        EjbRuntimeEndpointInfo ejbRuntimeEndpointInfo = null;
        
        if (isEJB) {
            ejbRuntimeEndpointInfo = AppServWSRegistry.getInstance().
                    getEjbRuntimeEndpointInfo(contextRoot, getValidPath());
            try {
                ejbRuntimeEndpointInfo.prepareInvocation(true);
            } catch (Exception e) {
                throw new IOException(e.getClass().getName());
            }
        }
        
        try {
            super.handle(channelContext);
        } finally {
            if (isEJB) {
                if(ejbRuntimeEndpointInfo != null)
                    ejbRuntimeEndpointInfo.releaseImplementor();
            }
        }
    }
    
    class TCP109Toolkit extends TCPAdapter.TCPToolkit {
        // if its Adapter from 109 deployed WS - add fake Servlet artifacts
        @Override
        public void addCustomPacketSattellites(@NotNull Packet packet) {
            super.addCustomPacketSattellites(packet);
            packet.addSatellite(servletFakeArtifactSet);
        }
    }
}
