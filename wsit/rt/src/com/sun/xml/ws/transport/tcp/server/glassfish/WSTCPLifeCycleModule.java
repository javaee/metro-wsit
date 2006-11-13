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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/**
 * @author Alexey Stashok
 */

public class WSTCPLifeCycleModule implements LifecycleListener {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private WSTCPConnector connector;
    private WSTCPDelegate delegate;
//    private Properties props;
    
    public void handleEvent(@NotNull LifecycleEvent lifecycleEvent) throws ServerLifecycleException {
        int eventType = lifecycleEvent.getEventType();
        if (eventType == LifecycleEvent.INIT_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.INIT_EVENT");
//            props = (Properties) lifecycleEvent.getData();
        } else if (eventType == LifecycleEvent.STARTUP_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.STARTUP_EVENT");
            try {
                delegate = new WSTCPDelegate();
                
                lifecycleEvent.getLifecycleEventContext().getInitialContext().bind("TCPLifeCycle", this);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (eventType == LifecycleEvent.READY_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.READY_EVENT");
            try {
                AppServWSRegistry.getInstance();
                delegate.setCustomWSRegistry(WSTCPAdapterRegistryImpl.getInstance());
//                String host = props.getProperty("host", "localhost");
//                String portS = props.getProperty("port");
//                int port;
//                if (portS != null) {
//                    port = Integer.parseInt(portS);
//                } else {
//                    throw new WSTCPException("Property 'port' is not set!");
//                }
                
//                connector = new GrizzlyTCPConnector(host, port, delegate);
                connector = new GrizzlyTCPConnector(delegate);
                connector.listen();
                
                
            } catch (Throwable e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (eventType == LifecycleEvent.SHUTDOWN_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.SHUTDOWN_EVENT");
            try {
                lifecycleEvent.getLifecycleEventContext().getInitialContext().unbind("TCPLifeCycle");
            } catch (NamingException ex) {
                logger.log(Level.WARNING, MessagesMessages.TRANSPORT_MODULE_NOT_REGISTERED());
            }
            
            if (delegate != null) {
                delegate.destroy();
            }
            
            if (connector != null) {
                connector.close();
            }
            
        }
    }
    
    public void register(@NotNull String contextPath,
            @NotNull List<TCPAdapter> adapters) {
        delegate.registerAdapters(contextPath, adapters);
    }
    
    public void free(@NotNull String contextPath, 
            @NotNull List<TCPAdapter> adapters) {
        delegate.freeAdapters(contextPath, adapters);
    }
}
