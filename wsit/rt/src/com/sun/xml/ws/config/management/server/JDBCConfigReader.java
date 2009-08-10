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
package com.sun.xml.ws.config.management.server;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.ConfigReader;
import com.sun.xml.ws.api.config.management.EndpointStarter;
import com.sun.xml.ws.api.config.management.NamedParameters;
import com.sun.xml.ws.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.commons.DelayedTaskManager;
import com.sun.xml.ws.commons.DelayedTaskManager.DelayedTask;
import com.sun.xml.ws.commons.MaintenanceTaskExecutor;
import com.sun.xml.ws.config.management.ManagementConstants;
import com.sun.xml.ws.config.management.ManagementUtil;
import com.sun.xml.ws.config.management.persistence.JDBCConfigSaver;
import com.sun.xml.ws.config.management.policy.ManagedServiceAssertion;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class JDBCConfigReader implements ConfigReader {

    private static final Logger LOGGER = Logger.getLogger(ConfigPoller.class);
    private volatile ConfigPoller poller = null;

    public synchronized void init(NamedParameters parameters) {
        if (this.poller != null) {
            throw new IllegalStateException(String.format("Duplicate initialization detected: This instance of [ %s ] class has already been initialized", this.getClass().getName()));
        }
        // TODO make interval configurable
        this.poller = new ConfigPoller(parameters, 10000);
    }

    public synchronized void start() {
        if (poller == null) {
            throw new IllegalStateException(String.format("Unable to start poller task: This instance of [ %s ] class has not been initialized yet. Please call init() method first.", this.getClass().getName()));
        }

        poller.start();
    }

    public synchronized void stop() {
        if (poller == null) {
            throw new IllegalStateException(String.format("Unable to stop poller task: This instance of [ %s ] class has not been initialized yet. Please call init() method first.", this.getClass().getName()));
        }
        poller.stop();
    }

    private static class ConfigPoller implements DelayedTask {

        private final ManagedEndpoint endpoint;
        private final EndpointStarter endpointStarter;
        private final NamedParameters configParameters;
        private final long executionDelay;
        private volatile boolean stopped;
        private volatile long version = 0L;

        public ConfigPoller(final NamedParameters parameters, final long executionDelay) {
            this.endpoint = parameters.get(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME);
            this.endpointStarter = parameters.get(ManagedEndpoint.ENDPOINT_STARTER_PARAMETER_NAME);
            this.configParameters = parameters;

            this.executionDelay = executionDelay;
            this.stopped = true;

            ManagedServiceAssertion assertion = ManagementUtil.getAssertion(this.endpoint);
            final String start = assertion.getStart();
            // TODO log actions, put "notify" into constant
            if (start == null || !start.equals("notify")) {
                endpointStarter.startEndpoint();
            }
        }

        public void run(DelayedTaskManager manager) {
            if (stopped) {
                return;
            }

            Connection connection = null;
            try {
                final DataSource source = JDBCConfigSaver.getManagementDS();
                connection = source.getConnection();
                pollData(connection, endpoint.getId());
                connection.close();
            } catch (SQLException e) {
                // TODO add error message
                LOGGER.logSevereException(e);
            } catch (WebServiceException e) {
                // TODO add error message
                LOGGER.logSevereException(e);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // TODO add error message
                    LOGGER.logSevereException(e);
                }

                if (!stopped) {
                    // schedule next run
                    MaintenanceTaskExecutor.INSTANCE.register(this, executionDelay, TimeUnit.MILLISECONDS);
                }
            }
        }

        private void pollData(Connection connection, String endpointId) {
            PreparedStatement statement = null;
            try {
                final String query = "SELECT version, config FROM METRO_CONFIG WHERE id = ? AND version > ?";
                if (LOGGER.isLoggable(Level.FINER)) {
                    // TODO put log message into properties
                    LOGGER.finer("Executing SQL command: " + query);
                }
                statement = connection.prepareStatement(query);
                statement.setString(1, endpointId);
                statement.setLong(2, this.version);
                final ResultSet result = statement.executeQuery();
                if (result.next()) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        // TODO put log message into properties
                        LOGGER.fine("SQL query found updated configuration data");
                    }
                    // TODO put column names into constants and/or make them configurable
                    this.version = result.getLong("version");
                    final Reader data = result.getCharacterStream("config");
                    reconfigure(data);
                } else {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        // TODO put log message into properties
                        LOGGER.finer("SQL query did not find any updated configuration data");
                    }
                }
            } catch (SQLException e) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(e));
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        // TODO add error message
                        throw LOGGER.logSevereException(new WebServiceException(e));
                    }
                }
            }
        }

        private void reconfigure(Reader reader) {
            this.configParameters.put(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME, ManagementUtil.convert(reader));
            final ReDelegate redelegate = new ReDelegate();
            redelegate.recreate(this.configParameters);
            this.endpointStarter.startEndpoint();
        }

        public String getName() {
            return "JDBC policy configuration reader";
        }

        synchronized void start() {
            LOGGER.entering();
            try {
                if (stopped) {
                    stopped = false;
                    MaintenanceTaskExecutor.INSTANCE.register(this, 0, TimeUnit.MILLISECONDS);
                } else {
                    LOGGER.warning(String.format("Duplicate start of [ %s ] instance detected: Instance already runing", this.getName()));
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
    }
}
