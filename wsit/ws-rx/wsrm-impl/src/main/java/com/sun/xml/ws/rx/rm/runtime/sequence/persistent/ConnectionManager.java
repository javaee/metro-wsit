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

package com.sun.xml.ws.rx.rm.runtime.sequence.persistent;

import com.sun.istack.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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

    Connection getConnection() throws PersistenceException {
        try {
            Connection connection = dataSourceProvider.getDataSource().getConnection();
            
            // connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            connection.setAutoCommit(false);
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
        rollback(sqlConnection, true);
    }

    void rollback(Connection sqlConnection, boolean markRollbackForXA) {
        if (isDistributedTransactionInUse()) {
            try {
                if (markRollbackForXA) {
                    //Do not roll back ourselves here as we don't own this distributed TX
                    //but mark it so that the only possible outcome of the TX is to 
                    //roll back the TX
                    getUserTransaction().setRollbackOnly();
                }
            } catch (IllegalStateException ise) {
                LOGGER.warning("Was not able to mark distributed transaction for rollback", ise);
            } catch (SystemException se) {
                LOGGER.warning("Was not able to mark distributed transaction for rollback", se);
            }
        } else {
            try {
                sqlConnection.rollback();
            } catch (SQLException ex) {
                LOGGER.warning("Unexpected exception occured while performing transaction rollback", ex);
            }
        }
    }

    void commit(Connection sqlConnection) throws PersistenceException {
        if (isDistributedTransactionInUse()) {
            //Do nothing as the distributed TX will eventually get  
            //committed and this work will be part of that
        } else {
            try {
                sqlConnection.commit();
            } catch (SQLException ex) {
                throw LOGGER.logSevereException(new PersistenceException("Unexpected exception occured while performing transaction commit", ex));
            }
        }
    }

    private boolean isDistributedTransactionInUse() {
        boolean result = false;
        int status = Status.STATUS_NO_TRANSACTION;
        try {
            UserTransaction userTransaction = getUserTransaction();
            if (userTransaction != null) {
                status = userTransaction.getStatus();
            }
        } catch (SystemException se) {
            LOGGER.warning("Not able to determine if distributed transaction is in use", se);
        }

        if (status != Status.STATUS_NO_TRANSACTION) {
            result = true;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Determined that distributed transaction is in use. Status code: " + status);    
            }
        }
        return result;
    }

    private UserTransaction getUserTransaction() {
        UserTransaction userTransaction = null;
        try {
            Context initialContext = new InitialContext();
            userTransaction = 
                    (UserTransaction)initialContext.lookup("java:comp/UserTransaction");
        } catch (NamingException ne) {
            LOGGER.warning("Not able to lookup UserTransaction from InitialContext", ne);
        }
        return userTransaction;
    }
}
