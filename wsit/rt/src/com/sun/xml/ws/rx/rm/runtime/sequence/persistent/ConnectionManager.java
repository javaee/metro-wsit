/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.rm.runtime.sequence.persistent;

import com.sun.xml.ws.commons.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class ConnectionManager {

    /**
     * Logger instance
     */
    private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class);

    private final DataSourceProvider dataSourceProvider;

    public static ConnectionManager getInstance(DataSourceProvider dataSourceProvider) {
        return new ConnectionManager(dataSourceProvider);
    }

    private ConnectionManager(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    Connection getConnection(boolean autoCommit) throws PersistenceException {
        try {
            Connection connection = dataSourceProvider.getDataSource().getConnection();
            
            // connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(autoCommit);
            return connection;
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException("Unable to setup required JDBC connection parameters", ex));
        }

    }

    PreparedStatement prepareStatement(Connection sqlConnection, String sqlStatement) throws SQLException {
        LOGGER.finer(String.format("Preparing SQL statement:\n%s", sqlStatement));

        return sqlConnection.prepareStatement(sqlStatement, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    }

    void recycle(ResultSet... resources) {
        for (ResultSet resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (SQLException ex) {
                    LOGGER.logException(ex, Level.WARNING);
                }
            }
        }
    }

    void recycle(PreparedStatement... resources) {
        for (PreparedStatement resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (SQLException ex) {
                    LOGGER.logException(ex, Level.WARNING);
                }
            }
        }
    }

    void recycle(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                LOGGER.logException(ex, Level.WARNING);
            }
        }
    }


    void rollback(Connection sqlConnection) {
        try {
            sqlConnection.rollback();
        } catch (SQLException ex) {
            LOGGER.warning("Unexpected exception occured while performing transaction rollback", ex);
        }
    }

    void commit(Connection sqlConnection) throws PersistenceException {
        try {
            sqlConnection.commit();
        } catch (SQLException ex) {
            throw LOGGER.logSevereException(new PersistenceException("Unexpected exception occured while performing transaction commit", ex));
        }
    }
}
