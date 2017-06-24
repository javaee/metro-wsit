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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.transport.tcp.servicechannel.ServiceChannelWSImpl;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.xml.sax.EntityResolver;

/**
 * WSTCPModule. Singlton class, which contains SOAP/TCP related information.
 * 
 * @author Alexey Stashok
 */
public abstract class WSTCPModule {
    private static volatile WSTCPModule instance;
    
    protected static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    /**
     * Method returns initialized WSTCPModule instance
     * @throws IllegalStateException if instance was not initialized
     */
    public static @NotNull WSTCPModule getInstance() {
        if (instance == null) {
            throw new IllegalStateException(MessagesMessages.WSTCP_0007_TRANSPORT_MODULE_NOT_INITIALIZED());
        }
        
        return instance;
    }
    
    protected static void setInstance(WSTCPModule instance) {
        WSTCPModule.instance = instance;
    }
    
    public WSEndpoint<ServiceChannelWSImpl> createServiceChannelEndpoint() {
        final QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
        final QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
        final BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
        final WSBinding binding = bindingId.createBinding();

        return WSEndpoint.create(ServiceChannelWSImpl.class, true,
                    null,
                    serviceName, portName, null, binding,
                    null, null, (EntityResolver) null, true);
    }

    public abstract void register(@NotNull final String contextPath,
            @NotNull final List<TCPAdapter> adapters);
    
    public abstract void free(@NotNull final String contextPath,
            @NotNull final List<TCPAdapter> adapters);
    
    /**
     * Returns port, SOAP/TCP is listening on.
     * 
     * @return the port, SOAP/TCP is linstening on. -1 if SOAP/TCP doesn't open
     * own TCP port, but uses connections provided by runtime.
     */
    public abstract int getPort();
}
