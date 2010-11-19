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

package com.sun.xml.ws.config.management.persistence;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.metro.api.config.management.NamedParameters;
import com.sun.xml.ws.metro.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.metro.api.config.management.ConfigSaver;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion.ImplementationRecord;
import com.sun.xml.ws.config.management.ManagementConstants;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.config.management.ManagementUtil;
import com.sun.xml.ws.config.management.ManagementUtil.JdbcTableNames;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceException;

/**
 * Default implementation that persists the new configuration data with JDBC.
 *
 * @param <T> The endpoint implementation class type.
 * @author Fabian Ritzmann
 */
public class JDBCConfigSaver<T> implements ConfigSaver<T> {

    private static final Logger LOGGER = Logger.getLogger(JDBCConfigSaver.class);

    private ManagedEndpoint<T> endpoint;
    private ManagedServiceAssertion assertion;

    public void init(ManagedEndpoint<T> endpoint, ManagedServiceAssertion assertion) {
        this.endpoint = endpoint;
        this.assertion = assertion;
    }

    /**
     * Persist the data.
     *
     * @param parameters The parameters must contain the ManagedEndpoint instance
     *   and the data to be persisted.
     */
    public void persist(final NamedParameters parameters) {
        final String newConfig = parameters.get(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME);
        validate(newConfig);

        Connection connection = null;
        DataSource source = null;
        try {
            final ImplementationRecord record = this.assertion.getConfigSaverImplementation();
            source = ManagementUtil.getJdbcDataSource(record, JDBCConfigSaver.class.getName());
            connection = source.getConnection();
            final JdbcTableNames tableNames = ManagementUtil.getJdbcTableNames(
                    record, JDBCConfigSaver.class.getName());
            writeData(connection, tableNames, this.endpoint.getId(), newConfig);
        } catch (SQLException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5021_NO_DB_CONNECT(source), e));
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.warning(ManagementMessages.WSM_5022_NO_DB_CLOSE(connection), e);
            }
        }
    }

    /**
     * The best way to ensure that the configuration data is valid is to do a
     * full parse before we are writing it to the database. This may cost a
     * little performance but since the reconfiguration is happening asynchronously,
     * it allows to give immediate feedback.
     *
     * @param configData The new policies.
     * @throws WebServiceException If parsing the policies failed.
     */
    private static void validate(final String configData) throws WebServiceException {
        try {
            ExternalAttachmentsUnmarshaller.unmarshal(new StringReader(configData));
        } catch (PolicyException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5097_FAILED_CONFIG_PARSE(), e));
        }
    }

    private static void writeData(final Connection connection,
            final JdbcTableNames tableNames, final String endpointId, final String config) {
        PreparedStatement updateStatement = null;
        PreparedStatement insertStatement = null;
        try {
            final String update = "UPDATE " + tableNames.getTableName() + " SET " +
                    tableNames.getVersionName() + " = " + tableNames.getVersionName() +
                    " + 1, " + tableNames.getConfigName() + " = ? WHERE id = ?";
            updateStatement = connection.prepareStatement(update);
            updateStatement.setCharacterStream(1, new StringReader(config), config.length());
            updateStatement.setString(2, endpointId);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(ManagementMessages.WSM_5023_EXECUTE_SQL(update));
            }
            final int rowCount = updateStatement.executeUpdate();
            if (rowCount == 0) {
                final String insert = "INSERT INTO " + tableNames.getTableName() +
                        " (" + tableNames.getIdName() + ", " + tableNames.getVersionName() +
                        ", " + tableNames.getConfigName() + ") VALUES (?, 1, ?)";
                insertStatement = connection.prepareStatement(insert);
                insertStatement.setString(1, endpointId);
                insertStatement.setCharacterStream(2, new StringReader(config), config.length());
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(ManagementMessages.WSM_5024_EXECUTE_SQL_UPDATE(insert));
                }
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5025_SQL_FAILED(), e));
        } finally {
            try {
                if (updateStatement != null) {
                    updateStatement.close();
                }
            } catch (SQLException e) {
                LOGGER.warning(ManagementMessages.WSM_5026_FAILED_STATEMENT_CLOSE(updateStatement), e);
            } finally {
                try {
                    if (insertStatement != null) {
                        insertStatement.close();
                    }
                } catch (SQLException e) {
                    LOGGER.warning(ManagementMessages.WSM_5026_FAILED_STATEMENT_CLOSE(insertStatement), e);
                }
            }
        }
    }

}
