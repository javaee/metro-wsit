/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.rx.rm.runtime.sequence.persistent;

import com.sun.istack.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class EmbeddedDerbyDbInstance {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedDerbyDbInstance.class);
    private static final String EMBEDDED_DERBY_DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    /**
     * Derby connection URL
     */
    private final String connectionUrl;
    private final String databaseName;
    private Connection connection;

    private EmbeddedDerbyDbInstance(String databaseName) throws PersistenceException {
        this.databaseName = databaseName;
        this.connectionUrl = String.format("jdbc:derby:%s;create=true", databaseName);
        try {
            // Loading the Derby JDBC driver. When the embedded Driver is used, this action also start the Derby engine.
            Class.forName(EMBEDDED_DERBY_DRIVER_CLASS_NAME).newInstance();
            LOGGER.config(EMBEDDED_DERBY_DRIVER_CLASS_NAME + " loaded.");
        } catch (java.lang.ClassNotFoundException ex) {
            LOGGER.severe(String.format("Unable to load JDBC driver class '%s'. Please, check your classpath", EMBEDDED_DERBY_DRIVER_CLASS_NAME), ex);
        } catch (InstantiationException ex) {
            LOGGER.severe(String.format("Unable to instantiate the JDBC driver class '%s'. Please, check your classpath", EMBEDDED_DERBY_DRIVER_CLASS_NAME), ex);
        } catch (IllegalAccessException ex) {
            LOGGER.severe(String.format("Unable to access the JDBC driver class '%s'. Please, check your security policy", EMBEDDED_DERBY_DRIVER_CLASS_NAME), ex);
        }

        this.connection = createConnection(databaseName, connectionUrl);
    }

    private static Connection createConnection(String databaseName, String connectionUrl) {
        try {
            Connection connection = DriverManager.getConnection(connectionUrl);
            LOGGER.config(String.format("Connection to database [ %s ] established succesfully", databaseName));
            return connection;
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format("Connection to database could not be [ %s ] established", databaseName), ex));
        }

    }

    public static EmbeddedDerbyDbInstance start(String databaseName) throws PersistenceException {
        return new EmbeddedDerbyDbInstance(databaseName);
    }

    public void stop() {
        try {
            connection.close();
        } catch (SQLException ex) {
            LOGGER.warning("Error closing connection", ex);
        }

        // shutting down ddatabase
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException ex) {
            if (ex.getSQLState().equals("XJ015")) {
                // Shutdown throws the XJ015 exception to confirm success.
                LOGGER.config("Database was shut down.");
            } else {
                LOGGER.warning("An unexpected error occured while shutting down the database.", ex);
            }
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = createConnection(databaseName, connectionUrl);
            }
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException("Connection.isClosed() invocation failed", ex));
        }

        return connection;
    }

    public void releaseConnection(final Connection connection) {
        // do nothing
    }

    public void releaseStatement(final Statement statement) {
        try {
            statement.close();
        } catch (SQLException ex) {
            LOGGER.warning("Closing an SQL statement threw an exception", ex);
        }
    }

    public boolean tableExists(String tableName) throws PersistenceException {
        tableName = tableName.replaceAll("\\s", ""); // protection against malicious code execution attack :)

        final Connection con = getConnection();
        Statement s = null;
        try {
            s = con.createStatement();
            s.execute(String.format("SELECT COUNT(*) FROM %s", tableName));

            return true;
        } catch (SQLException ex) {
            String theError = ex.getSQLState();
            if (theError.equals("42X05")) {
                // Table does not exist
                return false;
            } else if (theError.equals("42X14") || theError.equals("42821")) {
                throw LOGGER.logSevereException(new PersistenceException(String.format("Incorrect table definition. Drop and recreate table %s", tableName), ex));
            } else {
                throw LOGGER.logSevereException(new PersistenceException("Unexpected exception", ex));
            }
        } finally {
            releaseStatement(s);
            releaseConnection(con);
        }
    }

    public void createTable(String tableName, String createTableStatement, boolean dropIfExists) {
        tableName = tableName.replaceAll("\\s", "");

        final Connection con = getConnection();
        try {
            if (tableExists(tableName)) {
                if (dropIfExists) { // drop table
                    Statement s = null;
                    try {
                        LOGGER.config(String.format("Dropping table %s", tableName));
                        s = con.createStatement();
                        s.execute(String.format("DROP TABLE %s", tableName));
                    } catch (SQLException ex) {
                        throw LOGGER.logSevereException(new PersistenceException(String.format("An unexpected exception occured while dropping table [ %s ]", tableName), ex));
                    } finally {
                        releaseStatement(s);
                    }
                } else { // just delete all data
                    Statement s = null;
                    try {
                        LOGGER.config(String.format("Deleting all records from table %s", tableName));
                        s = con.createStatement();
                        s.execute(String.format("DELETE FROM %s", tableName));
                    } catch (SQLException ex) {
                        throw LOGGER.logSevereException(new PersistenceException(String.format("An unexpected exception occured while deleting all records from table [ %s ]", tableName), ex));
                    } finally {
                        releaseStatement(s);
                    }
                }
            }
            
            if (!tableExists(tableName)) { // table was not dropped or didn't exist
                Statement s = null;
                try {
                    LOGGER.config(String.format("Creating table %s", tableName));
                    s = con.createStatement();
                    LOGGER.info(String.format("Executing SQL statement to create [ %s ] table:\n%S", tableName, createTableStatement));
                    s.execute(String.format(createTableStatement));
                } catch (SQLException ex) {
                    throw LOGGER.logSevereException(new PersistenceException(String.format("An unexpected exception occured while creating table [ %s ]", tableName), ex));
                } finally {
                    releaseStatement(s);
                }
            }
        } finally {
            releaseConnection(con);
        }
    }

    public void execute(String sqlCommand) {
        Connection con = getConnection();
        Statement s = null;
        try {
            LOGGER.config(String.format("Executing SQL statement:\n%s", sqlCommand));
            s = con.createStatement();
            s.execute(sqlCommand);
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException(String.format("An unexpected exception occured while executing statement:\n%s", sqlCommand), ex));
        } finally {
            releaseStatement(s);
            releaseConnection(con);
        }
    }
}
