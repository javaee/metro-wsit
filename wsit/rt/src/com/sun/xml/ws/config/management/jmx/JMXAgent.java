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
import com.sun.xml.ws.api.config.management.CommunicationServer;
import com.sun.xml.ws.api.config.management.Configurator;
import com.sun.xml.ws.api.config.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.config.management.EndpointStarter;
import com.sun.xml.ws.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.api.config.management.jmx.JmxConnectorServerCreator;
import com.sun.xml.ws.api.config.management.jmx.ReconfigMBean;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion.ImplementationRecord;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion.NestedParameters;
import com.sun.xml.ws.policy.PolicyConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
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
 * @param <T> The endpoint implementation class
 * 
 * @author Fabian Ritzmann
 */
public class JMXAgent<T> implements CommunicationServer<T> {

    private static final Logger LOGGER = Logger.getLogger(JMXAgent.class);
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JmxConnectorServerEnviroment");
    private static final QName JMX_SERVICE_URL_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JmxServiceUrl");
    private static final QName JMX_CONNECTOR_SERVER_CREATOR_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JmxConnectorServerCreator");
    private static final String JMX_SERVICE_URL_DEFAULT_PREFIX = "service:jmx:rmi:///jndi/rmi://localhost:8686/metro/";

    private MBeanServer server;
    private JMXConnectorServer connector;

    private String endpointId;
    private ManagedEndpoint<T> managedEndpoint;
    private EndpointCreationAttributes endpointCreationAttributes;
    private ClassLoader classLoader;
    private Configurator<T> configurator;


    public void init(ManagedEndpoint<T> endpoint, ManagedServiceAssertion assertion,
            EndpointCreationAttributes creationAttributes, ClassLoader classLoader,
            Configurator<T> configurator, EndpointStarter starter) {
        JMXServiceURL jmxUrl = null;
        this.managedEndpoint = endpoint;
        this.endpointId = endpoint.getId();
        this.endpointCreationAttributes = creationAttributes;
        this.classLoader = classLoader;
        this.configurator = configurator;

        this.server = MBeanServerFactory.createMBeanServer();
        jmxUrl = getServiceURL(assertion);
        final Map<String, String> env = getConnectorServerEnvironment(assertion);
        this.connector = getJmxConnectorServer(assertion, jmxUrl, env, this.server);
    }

    public void start() {
        if (this.server != null && this.connector != null) {
            final ReconfigMBean mbean = createMBean();
            try {
                server.registerMBean(mbean, getObjectName());
                connector.start();
                LOGGER.info(ManagementMessages.WSM_5001_ENDPOINT_CREATED(this.endpointId, this.connector.getAddress()));
            } catch (InstanceAlreadyExistsException ex) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_5041_MBEAN_INSTANCE_EXISTS(mbean), ex));
            } catch (MBeanRegistrationException ex) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_5042_MBEAN_REGISTRATION_FAILED(mbean), ex));
            } catch (NotCompliantMBeanException ex) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_5042_MBEAN_REGISTRATION_FAILED(mbean), ex));

            } catch (IOException ex) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_5043_MBEAN_CONNECTOR_START_FAILED(this.connector), ex));
            }
        }
        else {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5044_MBEAN_SERVER_START_FAILED(this.server, this.connector)));
        }
    }

    public void stop() {
        try {
            if (this.connector != null) {
                connector.stop();
            }
        } catch (IOException ex) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5046_MBEAN_CONNECTOR_STOP_FAILED(this.connector), ex));
        } finally {
            final ObjectName name = getObjectName();
            try {
                if (this.server != null) {
                    this.server.unregisterMBean(name);
                }
            } catch (InstanceNotFoundException ex) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_5047_MBEAN_UNREGISTER_INSTANCE_FAILED(name), ex));
            } catch (MBeanRegistrationException ex) {
                throw LOGGER.logSevereException(new WebServiceException(
                        ManagementMessages.WSM_5048_MBEAN_UNREGISTRATION_FAILED(name), ex));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("JMXAgent [ ");
        text.append("Endpoint ID = ").append(this.endpointId);
        text.append(", ManagedEndpoint = ").append(this.managedEndpoint);
        text.append(", MBeanServer = ").append(this.server);
        text.append(", JMXConnectorServer = ").append(this.connector);
        text.append(", EndpointCreationAttributes = ").append(this.endpointCreationAttributes);
        text.append(", ClassLoader = ").append(this.classLoader);
        text.append(" ]");
        return text.toString();
    }

    private ReconfigMBean createMBean() {
        final HashMap<String, MBeanAttribute> attributeToListener = new HashMap<String, MBeanAttribute>();
        final HashMap<String, ReconfigNotification> notificationToListener = new HashMap<String, ReconfigNotification>();
        final Reconfig mbean = new Reconfig(attributeToListener, notificationToListener);
        attributeToListener.put(ReconfigAttribute.SERVICE_WSDL_ATTRIBUTE_NAME,
                new ReconfigAttribute<T>(this.managedEndpoint, this.configurator,
                this.endpointCreationAttributes, classLoader));
        final ReconfigNotification notification = new ReconfigNotification(mbean, getObjectName());
        notificationToListener.put(notification.getName(), notification);
        this.managedEndpoint.addNotifier(notification);
        return mbean;
    }

    private ObjectName getObjectName() {
        final String name = "com.sun.xml.ws.config.management:className=" + this.endpointId;
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException ex) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5049_INVALID_OBJECT_NAME(name), ex));
        }
    }

    private JMXServiceURL getServiceURL(ManagedServiceAssertion managedService) {
        String jmxServiceUrl = null;
        try {
            final Map<QName, String> parameters = getParameters(managedService);
            if (parameters != null) {
                jmxServiceUrl = parameters.get(JMX_SERVICE_URL_PARAMETER_NAME);
            }
            // The previous parameters.get might have returned null.
            if (jmxServiceUrl == null) {
                // No JmxServiceUrl found, use default
                jmxServiceUrl = JMX_SERVICE_URL_DEFAULT_PREFIX + this.endpointId;
            }
            LOGGER.config(ManagementMessages.WSM_5005_JMX_SERVICE_URL(jmxServiceUrl));
            return new JMXServiceURL(jmxServiceUrl);
        } catch (MalformedURLException ex) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5050_INVALID_JMX_SERVICE_URL(jmxServiceUrl), ex));
        }
    }

    private static Map<String, String> getConnectorServerEnvironment(ManagedServiceAssertion assertion) {
        final Collection<ImplementationRecord> records = assertion.getCommunicationServerImplementations();
        for (ImplementationRecord record : records) {
            final String name = record.getImplementation();
            if (name == null || name.equals(JMXAgent.class.getName())) {
                final Collection<NestedParameters> nestedParameters = record.getNestedParameters();
                if (nestedParameters != null) {
                    for (NestedParameters parameter : nestedParameters) {
                        if (JMX_CONNECTOR_SERVER_ENVIRONMENT_NAME.equals(parameter.getName())) {
                            final Map<QName, String> parameters = parameter.getParameters();
                            final Map<String, String> result = new HashMap<String, String>();
                            if (parameters != null) {
                                for (QName key : parameters.keySet()) {
                                    result.put(key.getLocalPart(), parameters.get(key));
                                }
                            }
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static JMXConnectorServer getJmxConnectorServer(final ManagedServiceAssertion managedService,
            JMXServiceURL jmxUrl, final Map<String, String> env, final MBeanServer server) {
        String connectorServerName = null;
        try {
            final Map<QName, String> parameters = getParameters(managedService);

            if (parameters != null) {
                connectorServerName = parameters.get(JMX_CONNECTOR_SERVER_CREATOR_PARAMETER_NAME);
                if (connectorServerName != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(ManagementMessages.WSM_5051_FOUND_JMX_CONNECTOR_SERVER_CREATOR(connectorServerName));
                    }
                    @SuppressWarnings("unchecked")
                    final Class<JmxConnectorServerCreator> creatorClass =
                            (Class<JmxConnectorServerCreator>) Class.forName(connectorServerName);
                    final JmxConnectorServerCreator creator = creatorClass.newInstance();
                    if (LOGGER.isLoggable(Level.CONFIG)) {
                        LOGGER.config(ManagementMessages.WSM_5052_INVOKING_JMX_CONNECTOR_SERVER_CREATOR(
                                creator, jmxUrl, env, server));
                    }
                    return creator.create(jmxUrl, env, server);
                }
            }

            return JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, env, server);
        } catch (ClassNotFoundException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5053_CLASS_NOT_FOUND_JMX_CONNECTOR_SERVER_CREATOR(connectorServerName), e));
        } catch (ClassCastException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5054_FAILED_CLASS_CAST_JMX_CONNECTOR_SERVER_CREATOR(connectorServerName), e));
        } catch (InstantiationException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5054_FAILED_INSTANTIATION_JMX_CONNECTOR_SERVER_CREATOR(connectorServerName), e));
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5054_FAILED_INSTANTIATION_JMX_CONNECTOR_SERVER_CREATOR(connectorServerName), e));
        } catch (IOException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5045_MBEAN_CONNECTOR_CREATE_FAILED(jmxUrl), e));
        }
    }

    private static Map<QName, String> getParameters(final ManagedServiceAssertion assertion) {
        final Collection<ImplementationRecord> records = assertion.getCommunicationServerImplementations();
        Map<QName, String> parameters = null;
        for (ImplementationRecord record : records) {
            final String name = record.getImplementation();
            if (name == null || name.equals(JMXAgent.class.getName())) {
                parameters = record.getParameters();
                break;
            }
        }
        return parameters;
    }

}