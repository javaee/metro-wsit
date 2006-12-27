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

package com.sun.xml.ws.transport.tcp.grizzly;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import com.sun.xml.ws.transport.tcp.server.IncomeMessageProcessor;
import com.sun.xml.ws.transport.tcp.server.TCPMessageListener;
import com.sun.xml.ws.transport.tcp.server.WSTCPConnector;
import java.net.InetAddress;

/**
 * @author Alexey Stashok
 */
public class GrizzlyTCPConnector implements WSTCPConnector {
    private SelectorThread selectorThread;
    
    private String host;
    private int port;
    private TCPMessageListener listener;
    
    private final boolean isPortUnificationMode;
    
    public GrizzlyTCPConnector(@NotNull final String host, final int port,
            @NotNull final TCPMessageListener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        isPortUnificationMode = false;
    }
    
    public GrizzlyTCPConnector(@NotNull final TCPMessageListener listener) {
        this.listener = listener;
        isPortUnificationMode = true;
    }
    
    public void listen() throws Exception {
        if (isPortUnificationMode) {
            listenOnUnifiedPort();
        } else {
            listenOnNewPort();
        }
    }
    
    public void listenOnNewPort() throws Exception {
        try {
            IncomeMessageProcessor.registerListener(getPort(), getListener());
            
            selectorThread = new SelectorThread();
            selectorThread.setClassLoader(WSTCPStreamAlgorithm.class.getClassLoader());
            selectorThread.setAlgorithmClassName(WSTCPStreamAlgorithm.class.getName());
            selectorThread.setAddress(InetAddress.getByName(getHost()));
            selectorThread.setPort(getPort());
            selectorThread.setBufferSize(TCPConstants.DEFAULT_FRAME_SIZE);
            selectorThread.initEndpoint();
            selectorThread.start();
        } catch (Exception ex) {
            close();
            throw ex;
        }
    }
    
    public void listenOnUnifiedPort() {
        WSTCPProtocolHandler.setIncomingMessageProcessor(new IncomeMessageProcessor(listener));
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
