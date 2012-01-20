/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.config.management.server;

import com.sun.istack.logging.Logger;
//import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.config.management.EndpointCreationAttributes;
import com.sun.xml.ws.metro.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.server.EndpointFactory;

import java.util.logging.Level;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * Create a new WSEndpoint instance and use it to replace the existing WSEndpoint
 * instance in a ManagedEndpoint.
 *
 * @author Fabian Ritzmann, Martin Grebac
 */
public class ReDelegate {

    private static final Logger LOGGER = Logger.getLogger(ReDelegate.class);

    public static <T> void recreate(ManagedEndpoint<T> managedEndpoint, WebServiceFeature... features) {
        try {
            WSEndpoint<T> delegate = recreateEndpoint(managedEndpoint, features);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(ManagementMessages.WSM_5092_NEW_ENDPOINT_DELEGATE(delegate));
            }
            managedEndpoint.swapEndpointDelegate(delegate);

        } catch (Throwable e) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5091_ENDPOINT_CREATION_FAILED(), e));
        }
    }

    private static <T> WSEndpoint<T> recreateEndpoint(ManagedEndpoint<T> endpoint, WebServiceFeature ... features) {
        
        // This allows the new endpoint to register with the same name for monitoring
        // as the old one.
        endpoint.closeManagedObjectManager();
        
        EndpointCreationAttributes creationAttributes = endpoint.getCreationAttributes();

        // TODO - only set features or recreate the binding?
        // WSBinding newBinding = BindingImpl.create(endpoint.getBinding().getBindingId(), features);
        ((BindingImpl)endpoint.getBinding()).setFeatures(features);
        
        final WSEndpoint<T> result = EndpointFactory.createEndpoint(endpoint.getImplementationClass(),
                creationAttributes.isProcessHandlerAnnotation(),
                creationAttributes.getInvoker(),
                endpoint.getServiceName(),
                endpoint.getPortName(),
                endpoint.getContainer(),
                endpoint.getBinding(),
                null,
                null,
                creationAttributes.getEntityResolver(),
                creationAttributes.isTransportSynchronous());
        result.getComponentRegistry().addAll(endpoint.getComponentRegistry());
        
        return result;
    }
    
}
