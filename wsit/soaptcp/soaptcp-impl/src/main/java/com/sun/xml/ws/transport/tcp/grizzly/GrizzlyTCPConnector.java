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

package com.sun.xml.ws.transport.tcp.grizzly;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.server.IncomeMessageProcessor;
import com.sun.xml.ws.transport.tcp.server.TCPMessageListener;
import com.sun.xml.ws.transport.tcp.server.WSTCPConnector;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

/**
 * @author Alexey Stashok
 */
public class GrizzlyTCPConnector implements WSTCPConnector {
    private SelectorThread selectorThread;
    
    private String host;
    private int port;
    private TCPMessageListener listener;
    private final Properties properties;
    
    private final boolean isPortUnificationMode;
    
    public GrizzlyTCPConnector(@NotNull final String host, final int port,
            @NotNull final TCPMessageListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        isPortUnificationMode = false;
        properties = new Properties();
    }
    
    public GrizzlyTCPConnector(@NotNull final TCPMessageListener listener, @NotNull final Properties properties) {
        this.listener = listener;
        isPortUnificationMode = true;
        this.properties = properties;
        port = -1;
    }
    
    public void listen() throws IOException {
        if (isPortUnificationMode) {
            listenOnUnifiedPort();
        } else {
            listenOnNewPort();
        }
    }
    
    public void listenOnNewPort() throws IOException {
        try {
            IncomeMessageProcessor.registerListener(port, listener, properties);
            
            selectorThread = new SelectorThread();
            selectorThread.setClassLoader(WSTCPStreamAlgorithm.class.getClassLoader());
            selectorThread.setAlgorithmClassName(WSTCPStreamAlgorithm.class.getName());
            selectorThread.setAddress(InetAddress.getByName(host));
            selectorThread.setPort(port);
            selectorThread.setBufferSize(TCPConstants.DEFAULT_FRAME_SIZE);
            selectorThread.setMaxKeepAliveRequests(-1);
            selectorThread.initEndpoint();
            selectorThread.start();
        } catch (IOException e) {
            close();
            throw e;
        } catch (InstantiationException e) {
            close();
            throw new IOException(e.getClass().getName() + ": " + e.getMessage());
        }
    }
    
    public void listenOnUnifiedPort() {
        WSTCPProtocolHandler.setIncomingMessageProcessor(IncomeMessageProcessor.registerListener(0, listener, properties));
    }
    
    public void close() {
        if (selectorThread != null) {
            selectorThread.stopEndpoint();
            IncomeMessageProcessor.releaseListener(selectorThread.getPort());
            selectorThread = null;
        }
    }
    public String getHost() {
        return host;
    }
    
    public void setHost(final String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(final int port) {
        this.port = port;
    }
    
    public TCPMessageListener getListener() {
        return listener;
    }
    
    public void setListener(final TCPMessageListener listener) {
        this.listener = listener;
    }
    
    
    public void setFrameSize(final int frameSize) {
        selectorThread.setBufferSize(frameSize);
    }
    
    public int getFrameSize() {
        return selectorThread.getBufferSize();
    }
}
