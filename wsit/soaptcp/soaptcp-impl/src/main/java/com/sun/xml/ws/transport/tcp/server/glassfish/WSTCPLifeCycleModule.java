/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.transport.tcp.server.glassfish;

import com.sun.appserv.server.LifecycleEvent;
import com.sun.appserv.server.LifecycleListener;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.tcp.grizzly.GrizzlyTCPConnector;
import com.sun.xml.ws.transport.tcp.server.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * GlassFish lifecycle module, which works as SOAP/TCP endpoints registry.
 *
 * @author Alexey Stashok
 */

public final class WSTCPLifeCycleModule extends WSTCPModule implements LifecycleListener {
    private WSTCPConnector connector;
    private WSTCPDelegate delegate;
    private Properties properties;
    
    public void handleEvent(@NotNull final LifecycleEvent lifecycleEvent) throws ServerLifecycleException {
        final int eventType = lifecycleEvent.getEventType();
        if (eventType == LifecycleEvent.INIT_EVENT) {
            WSTCPModule.setInstance(this);
            logger.log(Level.FINE, "WSTCPLifeCycleModule.INIT_EVENT");
            properties = (Properties) lifecycleEvent.getData();
        } else if (eventType == LifecycleEvent.STARTUP_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.STARTUP_EVENT");
            delegate = new WSTCPDelegate();
        } else if (eventType == LifecycleEvent.READY_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.READY_EVENT");
            try {
                AppServWSRegistry.getInstance();
                delegate.setCustomWSRegistry(WSTCPAdapterRegistryImpl.getInstance());
                connector = new GrizzlyTCPConnector(delegate, properties);
                connector.listen();
                
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        } else if (eventType == LifecycleEvent.SHUTDOWN_EVENT) {
            logger.log(Level.FINE, "WSTCPLifeCycleModule.SHUTDOWN_EVENT");
            WSTCPModule.setInstance(null);
            
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

    @Override
    public int getPort() {
        if (connector != null) {
            return connector.getPort();
        }
        
        return -1;
    }
}
