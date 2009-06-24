/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.management.jmx;

//import java.io.IOException;
import com.sun.xml.ws.api.management.CommunicationAPI;
import com.sun.xml.ws.api.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.management.InitParameters;
import com.sun.xml.ws.api.management.ManagedEndpoint;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.lang.management.ManagementFactory;
//import java.net.MalformedURLException;
import java.util.HashMap;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
//import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
//import javax.management.remote.JMXConnectorServer;
//import javax.management.remote.JMXConnectorServerFactory;
//import javax.management.remote.JMXServiceURL;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class JMXAgent<T> implements CommunicationAPI {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(JMXAgent.class);

    private MBeanServer server;
//    private final JMXConnectorServer connector;
    
    private String endpointId;
    private ManagedEndpoint<T> managedEndpoint;
    private EndpointCreationAttributes endpointCreationAttributes;
    private ClassLoader classLoader;

//    public JMXAgent(ManagedEndpoint endpoint, EndpointCreationAttributes creationAttributes, ClassLoader classLoader) {
//        try {
//        this.managedEndpoint = endpoint;
//        this.endpointCreationAttributes = creationAttributes;
//        this.classLoader = classLoader;
//        this.server = ManagementFactory.getPlatformMBeanServer();
//            server = MBeanServerFactory.createMBeanServer();
//            JMXServiceURL jmxUrl = new JMXServiceURL("jmxmp", "localhost", 5555);
//            connector = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, server);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        } catch (IOException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        }
//    }

    public void init(InitParameters parameters) {
//        try {
        this.endpointId = parameters.get(ManagedEndpoint.ENDPOINT_ID_PARAMETER_NAME);
        this.managedEndpoint = parameters.get(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME);
        this.endpointCreationAttributes = parameters.get(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME);
        this.classLoader = parameters.get(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME);
        this.server = ManagementFactory.getPlatformMBeanServer();
//            server = MBeanServerFactory.createMBeanServer();
//            JMXServiceURL jmxUrl = new JMXServiceURL("jmxmp", "localhost", 5555);
//            connector = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, server);
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        } catch (IOException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        }
    }

    public void start() {
//        try {
//            if (server != null && connector != null) {
//                final ReconfigMBean openMBean = new ReconfigMBean();
//                server.registerMBean(openMBean, new ObjectName("com.sun.xml.ws:className=ReconfigMBean"));
//                connector.start();
//            }
//        } catch (MalformedObjectNameException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        } catch (InstanceAlreadyExistsException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        } catch (MBeanRegistrationException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        } catch (NotCompliantMBeanException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        } catch (IOException ex) {
//            Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//            throw new WebServiceException(ex);
//        }
        if (server != null) {
            try {
                final HashMap<String, MBeanAttribute> attributeToListener = new HashMap<String, MBeanAttribute>();
                attributeToListener.put(ReconfigMBeanAttribute.SERVICE_WSDL_ATTRIBUTE_NAME,
                                        new ReconfigMBeanAttribute<T>(this.managedEndpoint, this.endpointCreationAttributes, classLoader));
                final ReconfigMBean openMBean = new ReconfigMBean(attributeToListener);
                server.registerMBean(openMBean, getObjectName());
            } catch (InstanceAlreadyExistsException ex) {
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (MBeanRegistrationException ex) {
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (NotCompliantMBeanException ex) {
                throw LOGGER.logSevereException(new WebServiceException(ex));
            }
        }
    }

    public void stop() {
//        if (connector != null) {
//            try {
//                connector.stop();
//            } catch (IOException ex) {
//                Logger.getLogger(WSServletAgent.class.getName()).log(Level.SEVERE, null, ex);
//                throw new WebServiceException(ex);
//            }
//        }
        if (this.server != null) {
            try {
                this.server.unregisterMBean(getObjectName());
            } catch (InstanceNotFoundException ex) {
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (MBeanRegistrationException ex) {
                throw LOGGER.logSevereException(new WebServiceException(ex));
            }
        }
    }

    private final ObjectName getObjectName() {
        try {
            return new ObjectName("com.sun.xml.ws.management:className=" + this.endpointId);
        } catch (MalformedObjectNameException ex) {
            throw LOGGER.logSevereException(new WebServiceException(ex));
        }
    }

}