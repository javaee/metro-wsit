/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public final class WSTCP {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static final String JAXWS_RI_RUNTIME = "WEB-INF/sun-jaxws.xml";
    
    private final TCPContext context;
    private final ClassLoader initClassLoader;
    private WSTCPDelegate delegate;
    private WSTCPConnector connector;
    private final String contextPath;
    
    public WSTCP(@NotNull final TCPContext context,
            @NotNull final ClassLoader initClassLoader,
    @NotNull final String contextPath) {
        this.context = context;
        this.initClassLoader = initClassLoader;
        this.contextPath = contextPath;
    }
    
    public @NotNull List<TCPAdapter> parseDeploymentDescriptor() throws IOException {
        final DeploymentDescriptorParser<TCPAdapter> parser = new DeploymentDescriptorParser<TCPAdapter>(
                initClassLoader, new TCPResourceLoader(context), null, TCPAdapter.FACTORY);
        final URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
        
        return parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
    }
    
    public @NotNull WSTCPConnector initialize() throws IOException {
        final List<TCPAdapter> adapters = parseDeploymentDescriptor();
        delegate = new WSTCPDelegate();
        delegate.registerAdapters(contextPath, adapters);
        
        final TCPAdapter adapter = adapters.get(0);
        final URI uri = adapter.getEndpoint().getPort().getAddress().getURI();
        
        final WSTCPURI tcpURI = WSTCPURI.parse(uri);
        
        final WSTCPConnector connector = new GrizzlyTCPConnector(tcpURI.host,
                tcpURI.port, delegate);
        connector.listen();
        return connector;
    }
    
    public void process() throws IOException {
        connector = initialize();
    }
    
    public void close() {
        if (connector != null) {
            connector.close();
        }
    }
    
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.out.println(MessagesMessages.STANDALONE_RUN());
            System.exit(0);
        }
        
        final String contextPath = args[0];
        
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final TCPContext context = new TCPStandaloneContext(classloader);
        
        final WSTCP wsTCP = new WSTCP(context, classloader, contextPath);
        
        try {
            wsTCP.process();
            System.out.println(MessagesMessages.STANDALONE_EXIT());
            System.in.read();
        } catch (Exception e) {
        } finally {
            wsTCP.close();
        }
    }
}
