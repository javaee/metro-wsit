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

package com.sun.xml.ws.transport.tcp.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;
import com.sun.xml.ws.transport.tcp.grizzly.GrizzlyTCPConnector;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alexey Stashok
 */
public class WSTCP {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static final String JAXWS_RI_RUNTIME = "WEB-INF/sun-jaxws.xml";
    
    private TCPContext context;
    private ClassLoader initClassLoader;
    private WSTCPDelegate delegate;
    private WSTCPConnector connector;
    private String contextPath;
    
    public WSTCP(@NotNull TCPContext context,
            @NotNull ClassLoader initClassLoader,
    @NotNull String contextPath) {
        this.context = context;
        this.initClassLoader = initClassLoader;
        this.contextPath = contextPath;
    }
    
    public @NotNull List<TCPAdapter> parseDeploymentDescriptor() throws IOException {
        DeploymentDescriptorParser<TCPAdapter> parser = new DeploymentDescriptorParser<TCPAdapter>(
                initClassLoader, new TCPResourceLoader(context), null, TCPAdapter.FACTORY);
        URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
        
        return parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
    }
    
    public @NotNull WSTCPConnector initialize() {
        try {
            List<TCPAdapter> adapters = parseDeploymentDescriptor();
            delegate = new WSTCPDelegate();
            delegate.registerAdapters(contextPath, adapters);
            
            TCPAdapter adapter = adapters.get(0);
            URI uri = adapter.getEndpoint().getPort().getAddress().getURI();

            WSTCPURI tcpURI = WSTCPURI.parse(uri);
            
            WSTCPConnector connector = new GrizzlyTCPConnector(tcpURI.host,
                    tcpURI.port, delegate);
            connector.listen();
            return connector;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new WSTCPException("listener.parsingFailed", e);
        }
        
    }
    
    public void process() {
        connector = initialize();
    }
    
    public void close() {
        if (connector != null) {
            connector.close();
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(MessagesMessages.STANDALONE_RUN());
            System.exit(0);
        }
        
        String contextPath = args[0];
        
        boolean b = true;
        
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        TCPContext context = new TCPStandaloneContext(classloader);
        
        WSTCP wsTCP = new WSTCP(context, classloader, contextPath);
        wsTCP.process();
        
        try {
            System.out.println(MessagesMessages.STANDALONE_EXIT());
            System.in.read();
        } catch (Exception e) {
        } finally {
            wsTCP.close();
        }
    }
}
