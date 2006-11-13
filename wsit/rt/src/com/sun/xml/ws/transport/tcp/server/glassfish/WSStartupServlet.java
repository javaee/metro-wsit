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

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.server.TCPContext;
import com.sun.xml.ws.transport.tcp.server.TCPResourceLoader;
import com.sun.xml.ws.transport.tcp.server.TCPServletContext;
import com.sun.xml.ws.transport.tcp.server.WSTCPException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

/**
 * @author JAX-WS team
 */
public class WSStartupServlet extends HttpServlet
        implements ServletContextAttributeListener, ServletContextListener {
    
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");
    
    private static final String JAXWS_RI_RUNTIME = "/WEB-INF/sun-jaxws.xml";
    
    private WSTCPLifeCycleModule transportModule;
    
    private List<TCPAdapter> adapters;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
    
    public void init() throws ServletException {
        super.init();
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }
    
    public void destroy() {
        super.destroy();
    }
    
    public void contextInitialized(ServletContextEvent contextEvent) {
        logger.log(Level.FINE, "WSStartupServlet.contextInitialized");
        InputStream is = null;
        ServletContext servletContext = contextEvent.getServletContext();
        TCPContext context = new TCPServletContext(servletContext);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        ServletContainer container = new ServletContainer(servletContext);
        
        try {
            InitialContext initialContext = new InitialContext();
            
            transportModule = (WSTCPLifeCycleModule) initialContext.lookup("TCPLifeCycle");
            if (transportModule == null) {
                throw new WSTCPException(MessagesMessages.TRANSPORT_MODULE_NOT_REGISTERED());
            }
            
            DeploymentDescriptorParser<TCPAdapter> parser = new DeploymentDescriptorParser<TCPAdapter>(
                    classLoader, new TCPResourceLoader(context), container, TCPAdapter.FACTORY);
            URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
            if(sunJaxWsXml==null)
                throw new WebServiceException(MessagesMessages.NO_JAXWS_DESCRIPTOR());
            adapters = parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());
            
            transportModule.register(servletContext.getContextPath(), adapters);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new WSTCPException("listener.parsingFailed", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                }
            }
        }
        
    }
    
    public void contextDestroyed(ServletContextEvent contextEvent) {
        logger.log(Level.FINE, "WSStartupServlet.contextDestroyed");
        if (transportModule != null && adapters != null) {
            transportModule.free(contextEvent.getServletContext().getContextPath(),
                    adapters);
        }
    }
    
    public void attributeAdded(ServletContextAttributeEvent scab) {
    }
    
    public void attributeRemoved(ServletContextAttributeEvent scab) {
    }
    
    public void attributeReplaced(ServletContextAttributeEvent scab) {
    }
    
    /**
     * Provides access to {@link ServletContext} via {@link Container}. Pipes
     * can get ServletContext from Container and use it to load some resources.
     */
    private static class ServletContainer extends Container {
        private final ServletContext servletContext;
        
        ServletContainer(ServletContext servletContext) {
            this.servletContext = servletContext;
        }
        
        public <T> T getSPI(Class<T> spiType) {
            if (spiType == ServletContext.class) {
                return (T) servletContext;
            }
            return null;
        }
    }
}
