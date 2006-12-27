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
import com.sun.xml.ws.api.pipe.ClientPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.pipe.TransportPipeFactory;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.servicechannel.stubs.ServiceChannelWSImplService;
import javax.xml.namespace.QName;

/**
 * @author Alexey Stashok
 */
public class TCPTransportPipeFactory extends TransportPipeFactory {
    private static final QName serviceChannelServiceName = new ServiceChannelWSImplService().getServiceName();
    
    @Override
    public Pipe doCreate(@NotNull final ClientPipeAssemblerContext context) {
        return doCreate(context, true);
    }
    
    public static Pipe doCreate(@NotNull final ClientPipeAssemblerContext context, final boolean checkSchema) {
        if (checkSchema && !TCPConstants.PROTOCOL_SCHEMA.equalsIgnoreCase(context.getAddress().getURI().getScheme())) {
            return null;
        }

        if (context.getService().getServiceName().equals(serviceChannelServiceName)) {
            return new ServiceChannelTransportPipe(context);
        }
        
        return new TCPTransportPipe(context);
    }
}