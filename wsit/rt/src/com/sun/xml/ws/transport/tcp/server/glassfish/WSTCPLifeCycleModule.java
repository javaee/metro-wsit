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

import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.tcp.grizzly.GrizzlyTCPConnector;
import com.sun.xml.ws.transport.tcp.server.*;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * @author Alexey Stashok
 */

public final class WSTCPLifeCycleModule implements LifecycleListener {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private WSTCPConnector connector;
    private WSTCPDelegate delegate;
    
    public void handleEvent(@NotNull final LifecycleEvent lifecycleEvent) throws ServerLifecycleException {
        final int eventType = lifecycleEvent.getEventType();
        if (eventType == LifecycleEvent.INIT_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.INIT_EVENT");
        } else if (eventType == LifecycleEvent.STARTUP_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.STARTUP_EVENT");
            try {
                delegate = new WSTCPDelegate();
                
                lifecycleEvent.getLifecycleEventContext().getInitialContext().bind("TCPLifeCycle", this);
            } catch (NamingException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (eventType == LifecycleEvent.READY_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.READY_EVENT");
            try {
                delegate.setCustomWSRegistry(WSTCPAdapterRegistryImpl.getInstance());
                connector = new GrizzlyTCPConnector(delegate);
                connector.listen();
                
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (eventType == LifecycleEvent.SHUTDOWN_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.SHUTDOWN_EVENT");
            try {
                lifecycleEvent.getLifecycleEventContext().getInitialContext().unbind("TCPLifeCycle");
            } catch (NamingException ex) {
                logger.log(Level.WARNING, MessagesMessages.WSTCP_0007_TRANSPORT_MODULE_NOT_REGISTERED(), ex);
            }
            
            if (delegate != null) {
                delegate.destroy();
            }
            
            if (connector != null) {
                connector.close();
            }
            
        }
    }
    
    public void register(@NotNull final String contextPath,
            @NotNull final List<TCPAdapter> adapters) {
        delegate.registerAdapters(contextPath, adapters);
    }
    
    public void free(@NotNull final String contextPath, 
            @NotNull final List<TCPAdapter> adapters) {
        delegate.freeAdapters(contextPath, adapters);
    }
}
