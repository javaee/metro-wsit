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

package com.sun.xml.ws.config.management.jmx;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.CommunicationAPI;
import com.sun.xml.ws.api.config.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.config.management.InitParameters;
import com.sun.xml.ws.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.config.management.ManagementUtil;
import com.sun.xml.ws.config.management.server.ConfigPoller;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;

import com.sun.xml.ws.rx.util.ScheduledTaskManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * Implements the JMX server and connector for a managed endpoint.
 *
 * @author Fabian Ritzmann
 */
public class JMXAgent<T> implements CommunicationAPI {

    private static final Logger LOGGER = Logger.getLogger(JMXAgent.class);
    private static final QName JMX_SERVICE_URL_PARAMETER_QNAME = new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JMXServiceURL");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_PARAMETER_QNAME = new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JMXConnectorServerEnvironment");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_PARAMETER_QNAME = new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "Entry");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_KEY_ATTRIBUTE_QNAME = new QName("", "key");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_VALUE_ATTRIBUTE_QNAME = new QName("", "value");
    private static final String JMX_SERVICE_URL_DEFAULT_PREFIX = "service:jmx:rmi:///jndi/rmi://localhost:8686/metro/";

    private final ScheduledTaskManager taskManager = new ScheduledTaskManager();

    private MBeanServer server;
    private JMXConnectorServer connector;

    private String endpointId;
    private ManagedEndpoint<T> managedEndpoint;
    private EndpointCreationAttributes endpointCreationAttributes;
    private ClassLoader classLoader;


    public void init(InitParameters parameters) {
        try {
            this.endpointId = parameters.get(ManagedEndpoint.ENDPOINT_ID_PARAMETER_NAME);
            this.managedEndpoint = parameters.get(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME);
            this.endpointCreationAttributes = parameters.get(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME);
            this.classLoader = parameters.get(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME);

            // TODO allow to register a callback handler that creates an MBeanServer and a JMXConnectorServer
            this.server = MBeanServerFactory.createMBeanServer();
            final PolicyAssertion managedService = ManagementUtil.getAssertion(this.managedEndpoint.getServiceName(),
                    this.managedEndpoint.getPortName(), this.managedEndpoint.getPolicyMap());
            final JMXServiceURL jmxUrl = getServiceURL(managedService);
            final Map<String, Object> env = getEnvironment(managedService);
            this.connector = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, env, server);
        } catch (MalformedURLException e) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(e));
        } catch (IOException e) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(e));
        }
    }

    public void start() {
        if (server != null && connector != null) {
            try {
                server.registerMBean(createMBean(), getObjectName());
                connector.start();
                LOGGER.info(ManagementMessages.WSM_5001_ENDPOINT_CREATED(this.endpointId, connector.getAddress()));

                // TODO create proper interfaces, make interval configurable
                final ConfigPoller poller = new ConfigPoller(new InitParameters()
                        .put(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME, this.managedEndpoint)
                        .put(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME, this.endpointCreationAttributes)
                        .put(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME, this.classLoader));
                this.taskManager.startTask(poller, 0, 10000);
            } catch (InstanceAlreadyExistsException ex) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (MBeanRegistrationException ex) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (NotCompliantMBeanException ex) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (IOException ex) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(ex));
            }
        }
    }

    public void stop() {
        try {
            if (this.connector != null) {
                connector.stop();
            }
        } catch (IOException ex) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(ex));
        } finally {
            try {
                if (this.server != null) {
                    this.server.unregisterMBean(getObjectName());
                }
            } catch (InstanceNotFoundException ex) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } catch (MBeanRegistrationException ex) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(ex));
            } finally {
                this.taskManager.stopAll();
            }
        }
    }

    private ReconfigMBean createMBean() {
        final HashMap<String, MBeanAttribute> attributeToListener = new HashMap<String, MBeanAttribute>();
        attributeToListener.put(ReconfigMBeanAttribute.SERVICE_WSDL_ATTRIBUTE_NAME,
                new ReconfigMBeanAttribute<T>(this.managedEndpoint, this.endpointCreationAttributes, classLoader));
        return new ReconfigMBean(attributeToListener);
    }

    private ObjectName getObjectName() {
        try {
            return new ObjectName("com.sun.xml.ws.config.management:className=" + this.endpointId);
        } catch (MalformedObjectNameException ex) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(ex));
        }
    }

    private JMXServiceURL getServiceURL(PolicyAssertion managedService) {
        try {
            final Iterator<PolicyAssertion> parameters = managedService.getParametersIterator();
            while (parameters.hasNext()) {
                final PolicyAssertion parameter = parameters.next();
                if (JMX_SERVICE_URL_PARAMETER_QNAME.equals(parameter.getName())) {
                    return new JMXServiceURL(parameter.getValue().trim());
                }
            }
            // No JMXServiceURL found, return default
            final String jmxServiceURL = JMX_SERVICE_URL_DEFAULT_PREFIX + this.endpointId;
            LOGGER.config(ManagementMessages.WSM_5005_DEFAULT_JMX_SERVICE_URL(jmxServiceURL));
            return new JMXServiceURL(jmxServiceURL);
        } catch (MalformedURLException ex) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(ex));
        }
    }

    private Map<String, Object> getEnvironment(PolicyAssertion managedService) {
        final Map<String, Object> result = new HashMap<String, Object>();
        final Iterator<PolicyAssertion> parameters = managedService.getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (JMX_CONNECTOR_SERVER_ENVIRONMENT_PARAMETER_QNAME.equals(parameter.getName())) {
                final Iterator<PolicyAssertion> entries = parameter.getParametersIterator();
                while (entries.hasNext()) {
                    final PolicyAssertion entry = entries.next();
                    if (!JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_PARAMETER_QNAME.equals(entry.getName())) {
                        throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5006_UNEXPECTED_ENTRY(entry)));
                    }
                    else {
                        final String key = entry.getAttributeValue(JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_KEY_ATTRIBUTE_QNAME);
                        final String value = entry.getAttributeValue(JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_VALUE_ATTRIBUTE_QNAME);
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }
}