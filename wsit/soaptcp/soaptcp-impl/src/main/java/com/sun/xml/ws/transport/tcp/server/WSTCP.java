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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.grizzly.GrizzlyTCPConnector;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class WSTCP {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static final String JAXWS_RI_RUNTIME = "WEB-INF/sun-jaxws.xml";
    
    private static final String ENABLE_PROTOCOL_CHECK = "-enableProtocolCheck";

    private final TCPContext context;
    private final ClassLoader initClassLoader;
    private WSTCPDelegate delegate;
    private Collection<WSTCPConnector> connectors;
    private final String contextPath;
    private boolean isProtocolCheck;
    
    public WSTCP(@NotNull final TCPContext context,
            @NotNull final ClassLoader initClassLoader,
    @NotNull final String contextPath) {
        this.context = context;
        this.initClassLoader = initClassLoader;
        this.contextPath = contextPath;
    }

    public boolean isProtocolCheck() {
        return isProtocolCheck;
    }

    public void setProtocolCheck(boolean isProtocolCheck) {
        this.isProtocolCheck = isProtocolCheck;
    }
    
    public @NotNull List<TCPAdapter> parseDeploymentDescriptor() throws IOException {
        final DeploymentDescriptorParser<TCPAdapter> parser = new DeploymentDescriptorParser<TCPAdapter>(
                initClassLoader, new TCPResourceLoader(context), null, TCPAdapter.FACTORY);
        final URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
        
        return parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
    }
    
    public @NotNull Collection<WSTCPConnector> initialize() throws IOException {
        final List<TCPAdapter> adapters = parseDeploymentDescriptor();
        delegate = new WSTCPDelegate();
        Collection<WSTCPConnector> connectors = new LinkedList<WSTCPConnector>();
        
        Iterator<TCPAdapter> it = adapters.iterator();
        while(it.hasNext()) {
            TCPAdapter adapter = it.next();
            final URI uri = adapter.getEndpoint().getPort().getAddress().getURI();
            final WSTCPURI tcpURI = WSTCPURI.parse(uri);

            if (isProtocolCheck && 
                    !TCPConstants.PROTOCOL_SCHEMA.equals(uri.getScheme())) {
                logger.log(Level.INFO, 
                        MessagesMessages.WSTCP_2002_STANDALONE_ADAPTER_NOT_REGISTERED(
                        adapter.name, adapter.urlPattern));
                it.remove();
                continue;
            }
            
            final WSTCPConnector connector = new GrizzlyTCPConnector(tcpURI.host,
                    tcpURI.port, delegate);
            connector.listen();
            connectors.add(connector);
            logger.log(Level.FINE,
                    MessagesMessages.WSTCP_2001_STANDALONE_ADAPTER_REGISTERED(
                    adapter.name, adapter.urlPattern));
        }
    
        delegate.registerAdapters(contextPath, adapters);
        return connectors;
    }
    
    public void process() throws IOException {
        connectors = initialize();
    }
    
    public void close() {
        if (connectors != null) {
            for(WSTCPConnector connector : connectors)
            connector.close();
        }
    }
    
    public static void main(final String[] args) {
        Set<String> params = new HashSet<String>();
        
        if (args.length < 1) {
            System.out.println(MessagesMessages.STANDALONE_RUN());
            System.exit(0);
        }
        
        for(int i=1; i<args.length; i++) {
            params.add(args[i]);
        }
        
        final String contextPath = args[0];

        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final TCPContext context = new TCPStandaloneContext(classloader);
        
        final WSTCP wsTCP = new WSTCP(context, classloader, contextPath);
        wsTCP.setProtocolCheck(params.contains(ENABLE_PROTOCOL_CHECK));
        
        try {
            wsTCP.process();
            System.out.println(MessagesMessages.STANDALONE_EXIT());
            System.in.read();
        } catch (Exception e) {
            logger.log(Level.SEVERE, MessagesMessages.WSTCP_2000_STANDALONE_EXCEPTION(), e);
        } finally {
            wsTCP.close();
        }
    }
}
