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

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.WSEndpoint;
import javax.xml.namespace.QName;
import org.xml.sax.EntityResolver;

/**
 * @author Alexey Stashok
 */
public class ServiceChannelCreator {
    
    private static final WSEndpoint<ServiceChannelWSImpl> endpoint = createEndpoint();
    
    private static WSEndpoint<ServiceChannelWSImpl> createEndpoint() {
        try {
            final QName serviceName = WSEndpoint.getDefaultServiceName(ServiceChannelWSImpl.class);
            final QName portName = WSEndpoint.getDefaultPortName(serviceName, ServiceChannelWSImpl.class);
            final BindingID bindingId = BindingID.parse(ServiceChannelWSImpl.class);
            final WSBinding binding = bindingId.createBinding();
            
            return WSEndpoint.create(
                    ServiceChannelWSImpl.class, true,
                    InstanceResolver.createSingleton(ServiceChannelWSImpl.class.newInstance()).createInvoker(),
                    serviceName, portName, null, binding,
                    null, null, (EntityResolver) null, true
                    );
        } catch (InstantiationException ex) {
        } catch (IllegalAccessException ex) {
        }
        
        return null;
    }
    
    public static WSEndpoint<ServiceChannelWSImpl> getServiceChannelEndpointInstance() {
        return endpoint;
    }
}
