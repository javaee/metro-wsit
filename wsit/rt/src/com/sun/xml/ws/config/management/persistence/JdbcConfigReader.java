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

package com.sun.xml.ws.config.management.persistence;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.ConfigReader;
import com.sun.xml.ws.api.config.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.config.management.EndpointStarter;
import com.sun.xml.ws.api.config.management.NamedParameters;
import com.sun.xml.ws.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion.ImplementationRecord;
import com.sun.xml.ws.commons.DelayedTaskManager;
import com.sun.xml.ws.commons.DelayedTaskManager.DelayedTask;
import com.sun.xml.ws.config.management.ManagementConstants;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.config.management.ManagementUtil;
import com.sun.xml.ws.config.management.ManagementUtil.JdbcTableNames;
import com.sun.xml.ws.config.management.server.ReDelegate;
import com.sun.xml.ws.policy.PolicyConstants;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * This implementation polls a JDBC data sources for changes and if it finds a new
 * configuration, it reconfigures the managed endpoint.
 *
 * This implementation starts the endpoint immediately unless the ManagedService
 * start attribute was set to "notify". Otherwise it will start the endpoint
 * only when it finds new configuration data.
 *
 * @param <T> The endpoint implementation class type.
 * @author Fabian Ritzmann
 */
public class JdbcConfigReader<T> implements ConfigReader<T> {

    private static final Logger LOGGER = Logger.getLogger(JdbcConfigReader.class);
    private static final QName POLLING_INTERVAL_PARAMETER_NAME =
            new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "PollingInterval");
    private static final long DEFAULT_POLLING_INTERVAL = 10000L;

    private ManagedEndpoint<T> endpoint;
    private ManagedServiceAssertion assertion;
    private EndpointCreationAttributes creationAttributes;
    private ClassLoader endpointClassLoader;
    private EndpointStarter endpointStarter;
    private long pollingInterval;

    private volatile ConfigPoller<T> poller = null;


    public synchronized void init(ManagedEndpoint<T> endpoint, ManagedServiceAssertion assertion,
            EndpointCreationAttributes attributes, ClassLoader classLoader, EndpointStarter starter) {
        this.endpoint = endpoint;
        this.assertion = assertion;
        this.creationAttributes = attributes;
        this.endpointClassLoader = classLoader;
        this.endpointStarter = starter;

        this.pollingInterval = DEFAULT_POLLING_INTERVAL;
        final ImplementationRecord record = assertion.getConfigReaderImplementation();
        if (record != null) {
            final String className = record.getImplementation();
            if (className == null || className.equals(JdbcConfigReader.class.getName())) {
                String pollingIntervalText = null;
                try {
                    final Map<QName, String> classParameters = record.getParameters();
                    pollingIntervalText = classParameters.get(POLLING_INTERVAL_PARAMETER_NAME);
                    if (pollingIntervalText != null) {
                        this.pollingInterval = Long.parseLong(pollingIntervalText);
                    }
                } catch (NumberFormatException e) {
                    throw LOGGER.logSevereException(new WebServiceException(
                            ManagementMessages.WSM_5039_FAILED_NUMBER_CONVERSION(
                            POLLING_INTERVAL_PARAMETER_NAME, pollingIntervalText), e));
                }
            }
        }
    }

    public synchronized void start(NamedParameters parameters) throws IllegalStateException {
        if (this.poller != null && !this.poller.isStopped()) {
            throw LOGGER.logSevereException(new IllegalStateException(
                    ManagementMessages.WSM_5087_FAILED_POLLER_START()));
        }
        this.poller = new ConfigPoller<T>(this.endpoint, this.assertion, this.creationAttributes,
                this.endpointClassLoader, this.endpointStarter, this.pollingInterval);
        this.poller.start();
    }

    public synchronized void stop() throws IllegalStateException {
        if (poller == null) {
            throw LOGGER.logSevereException(new IllegalStateException(
                    ManagementMessages.WSM_5033_POLLER_STOP_FAILED(getClass().getName())));
        }
        poller.stop();
    }


    /**
     * This implementation polls the database for a new version of the configuration
     * data. It reconfigures the endpoint as soon as it finds a new version.
     *
     * This implementation starts the endpoint immediately unless the ManagedService
     * start attribute was set to "notify". Otherwise it will start the endpoint
     * only when it finds new configuration data.
     *
     * @param <T> The endpoint implementation class type.
     */
    private static class ConfigPoller<T> implements DelayedTask {

        private static final String START_ATTRIBUTE_NOTIFY_VALUE_NAME = "notify";
        private static final String POLLER_NAME = "Configuration management JDBC poller";

        private final ManagedEndpoint<T> endpoint;
        private final EndpointCreationAttributes creationAttributes;
        private final ClassLoader classLoader;
        private final EndpointStarter endpointStarter;
        private final ManagedServiceAssertion managedService;
        private final long executionDelay;
        private final DelayedTaskManager taskManager =
                DelayedTaskManager.createSingleThreadedManager("config-management-jdbc-poller");

        private volatile boolean stopped;
        private volatile long version = 0L;

        public ConfigPoller(final ManagedEndpoint<T> endpoint, final ManagedServiceAssertion assertion,
                final EndpointCreationAttributes attributes, final ClassLoader classLoader,
                final EndpointStarter starter, final long executionDelay) {
            this.endpoint = endpoint;
            this.creationAttributes = attributes;
            this.classLoader = classLoader;
            this.endpointStarter = starter;

            this.managedService = assertion;

            this.executionDelay = executionDelay;
            this.stopped = true;

            final String start = this.managedService.getStart();
            if (start == null || !start.equals(START_ATTRIBUTE_NOTIFY_VALUE_NAME)) {
                if (LOGGER.isLoggable(Level.CONFIG)) {
                    LOGGER.config(ManagementMessages.WSM_5035_START_ENDPOINT_IMMEDIATELY(start));
                }
                this.endpointStarter.startEndpoint();
            }
            else {
                if (LOGGER.isLoggable(Level.CONFIG)) {
                    LOGGER.config(ManagementMessages.WSM_5036_WAIT_ENDPOINT_START(start));
                }
            }
            
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(ManagementMessages.WSM_5040_CREATED_POLLER(executionDelay));
            }
        }

        public String getName() {
            return POLLER_NAME;
        }

        public void run(final DelayedTaskManager manager) {
            if (stopped) {
                return;
            }

            Connection connection = null;
            DataSource source = null;
            try {
                final ImplementationRecord record = this.managedService.getConfigReaderImplementation();
                final JdbcTableNames tableNames = ManagementUtil.getJdbcTableNames(record,
                        JdbcConfigReader.class.getName());
                source = ManagementUtil.getJdbcDataSource(record,
                        JdbcConfigReader.class.getName());
                connection = source.getConnection();
                pollData(connection, tableNames, endpoint.getId());
                connection.close();
            } catch (SQLException e) {
                LOGGER.warning(ManagementMessages.WSM_5021_NO_DB_CONNECT(source), e);
            } catch (Throwable e) {
                LOGGER.severe(ManagementMessages.WSM_5037_FAILED_RECONFIGURE(), e);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    LOGGER.warning(ManagementMessages.WSM_5022_NO_DB_CLOSE(connection), e);
                }

                if (!stopped) {
                    // schedule next run
                    this.taskManager.register(this, executionDelay, TimeUnit.MILLISECONDS);
                }
            }
        }

        synchronized void start() {
            LOGGER.entering();
            try {
                if (stopped) {
                    stopped = false;
                    this.taskManager.register(this, 0, TimeUnit.MILLISECONDS);
                } else {
                    LOGGER.warning(ManagementMessages.WSM_5034_DUPLICATE_START(getName()));
                }
            } finally {
                LOGGER.exiting();
            }
        }

        synchronized void stop() {
            LOGGER.entering();
            try {
                stopped = true;
            } finally {
                LOGGER.exiting();
            }
        }

        synchronized boolean isStopped() {
            return this.stopped;
        }

        private void pollData(final Connection connection, final JdbcTableNames tableNames, final String endpointId) {
            PreparedStatement statement = null;
            try {
                final String query = "SELECT " + tableNames.getVersionName() + ", " +
                        tableNames.getConfigName() + " FROM " + tableNames.getTableName() +
                        " WHERE " + tableNames.getIdName() + " = ? AND " + tableNames.getVersionName() + " > ?";
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(ManagementMessages.WSM_5023_EXECUTE_SQL(query));
                }
                statement = connection.prepareStatement(query);
                statement.setString(1, endpointId);
                statement.setLong(2, this.version);
                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(ManagementMessages.WSM_5029_FOUND_UPDATED_CONFIG());
                    }
                    this.version = result.getLong(tableNames.getVersionName());
                    final Reader data = result.getCharacterStream(tableNames.getConfigName());
                    reconfigure(data);
                } else {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finer(ManagementMessages.WSM_5030_NO_UPDATED_CONFIG());
                    }
                }
            } catch (SQLException e) {
                throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5038_FAILED_CONFIG_READ(), e));
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        LOGGER.warning(ManagementMessages.WSM_5026_FAILED_STATEMENT_CLOSE(statement), e);
                    }
                }
            }
        }

        private void reconfigure(Reader reader) {
            final NamedParameters parameters = new NamedParameters()
                    .put(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME, this.endpoint)
                    .put(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME, this.creationAttributes)
                    .put(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME, this.classLoader)
                    .put(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME, ManagementUtil.convert(reader));
            ReDelegate.recreate(parameters);
            this.endpointStarter.startEndpoint();
        }

    }

}