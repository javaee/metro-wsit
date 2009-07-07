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

package com.sun.xml.ws.management.persistence;

import com.sun.xml.ws.api.management.InitParameters;
import com.sun.xml.ws.api.management.ManagedEndpoint;
import com.sun.xml.ws.api.management.PersistenceAPI;
import com.sun.xml.ws.management.ManagementConstants;
import com.sun.xml.ws.management.ManagementLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class PersistConfig implements PersistenceAPI {

    private static final ManagementLogger LOGGER = ManagementLogger.getLogger(PersistConfig.class);

    public void persist(final InitParameters parameters) {
        final ManagedEndpoint endpoint = parameters.get(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME);
        final String newConfig = parameters.get(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME);
        writeData(endpoint.getId(), newConfig);
    }

    private void writeData(final String endpointId, final String data) {
        Connection connection = null;
        Statement statement = null;
        try {
            final DataSource source = getManagementDS();
            connection = source.getConnection();
            statement = connection.createStatement();
            writeData(statement, endpointId, data);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(e));
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                // TODO add error message
                throw LOGGER.logSevereException(new WebServiceException(e));
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    // TODO add error message
                    throw LOGGER.logSevereException(new WebServiceException(e));
                }
            }
        }
    }

    private void writeData(final Statement statement, final String endpointId, final String config) {
        try {
            final String update = "UPDATE METRO_CONFIG " +
                    "SET version = version + 1, config = '" + config + "' " +
                    "WHERE id = '" + endpointId + "'";
            if (LOGGER.isLoggable(Level.FINE)) {
                // TODO put log message into properties
                LOGGER.fine("Executing SQL command: " + update);
            }
            final int rowCount = statement.executeUpdate(update);
            if (rowCount == 0) {
                final String insert = "INSERT INTO METRO_CONFIG (id, version, config) " +
                        "VALUES ('" + endpointId + "', 1, '" + config + "')";
                if (LOGGER.isLoggable(Level.FINE)) {
                    // TODO put log message into properties
                    LOGGER.fine("SQL UPDATE returned row count 0. Executing SQL command: " + insert);
                }
                statement.executeUpdate(insert);
            }
        } catch (SQLException e) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(e));
        }
    }

    private DataSource getManagementDS() {
        try {
            InitialContext initCtx = new InitialContext();
            return (DataSource) initCtx.lookup("jdbc/managementDS");
        } catch (NamingException e) {
            // TODO add error message
            throw LOGGER.logSevereException(new WebServiceException(e));
        }
    }

}