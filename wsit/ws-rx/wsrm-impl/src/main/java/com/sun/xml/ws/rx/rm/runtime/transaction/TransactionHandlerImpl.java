/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.rx.rm.runtime.transaction;

import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.sun.istack.Nullable;
import com.sun.istack.logging.Logger;

/**
*
* @author Uday Joshi <uday.joshi at oracle.com>
*/
public class TransactionHandlerImpl implements TransactionHandler {
    private static final Logger LOGGER = Logger
            .getLogger(TransactionHandlerImpl.class);

    private UserTransaction userTransaction = null;

    @Override
    public void begin(int txTimeout) throws TransactionException {
        UserTransaction userTransaction = getUserTransaction();

        try {
            userTransaction.setTransactionTimeout(txTimeout);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Transaction timeout set to " + txTimeout);
            }
        } catch (final SystemException se) {
            String message = "Not able to set transaction timeout on UserTransaction.";
            LOGGER.severe(message, se);
            throw new TransactionException(message, se);
        }

        try {
            userTransaction.begin();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("UserTransaction started.");
            }
        } catch (final Throwable t) {
            String message = "Not able to begin UserTransaction.";
            LOGGER.severe(message, t);
            throw new TransactionException(message, t);
        }
    }

    @Override
    public void commit() throws TransactionException {
        UserTransaction userTransaction = getUserTransaction();
        try {
            userTransaction.commit();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("UserTransaction committed successfully.");
            }
        } catch (final Throwable t) {
            String message = "Not able to commit UserTransaction.";
            LOGGER.severe(message, t);
            throw new TransactionException(message, t);
        }
    }

    @Override
    public void rollback() throws TransactionException {
        UserTransaction userTransaction = getUserTransaction();
        try {
            userTransaction.rollback();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("UserTransaction rolled back successfully.");
            }
        } catch (final Throwable t) {
            String message = "Not able to roll back UserTransaction.";
            LOGGER.severe(message, t);
            throw new TransactionException(message, t);
        }
    }

    @Override
    public void setRollbackOnly() throws TransactionException {
        UserTransaction userTransaction = getUserTransaction();
        try {
            userTransaction.setRollbackOnly();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("UserTransaction marked for roll back successfully.");
            }
        } catch (final Throwable t) {
            String message = "Not able to mark UserTransaction for roll back.";
            LOGGER.severe(message, t);
            throw new TransactionException(message, t);
        }
    }

    @Override
    public boolean userTransactionAvailable() throws TransactionException {
        UserTransaction ut = getUserTransaction();
        return (ut != null);
    }

    @Override
    public boolean isActive() throws TransactionException {
        int status = getStatus();
        return status == Status.STATUS_ACTIVE;
    }

    @Override
    public boolean isMarkedForRollback() throws TransactionException {
        int status = getStatus();
        return status == Status.STATUS_MARKED_ROLLBACK;
    }

    @Override
    public boolean canBegin() {
        boolean txExists = transactionExists();
        return !txExists;
    }

    @Override
    public boolean transactionExists() throws TransactionException {
        int status = getStatus();
        boolean noTX = (Status.STATUS_NO_TRANSACTION == status ||
                Status.STATUS_COMMITTED == status ||
                Status.STATUS_ROLLEDBACK == status);
        return !noTX;
    }

    @Override
    public int getStatus() throws TransactionException {
        UserTransaction ut = getUserTransaction();
        int status = Status.STATUS_NO_TRANSACTION;
        try {
            status = ut.getStatus();
        } catch (final SystemException se) {
            String message = "Not able to get UserTransaction status.";
            LOGGER.severe(message, se);
            throw new TransactionException(message, se);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("UserTransaction status is: " + status);
        }

        return status;
    }

    @Override
    public String getStatusAsString() throws TransactionException {
        int status = getStatus();
        String statusString = null;

        switch (status) {
        case 0:
            statusString = "STATUS_ACTIVE";
            break;
        case 1:
            statusString = "STATUS_MARKED_ROLLBACK";
            break;
        case 2:
            statusString = "STATUS_PREPARED";
            break;
        case 3:
            statusString = "STATUS_COMMITTED";
            break;
        case 4:
            statusString = "STATUS_ROLLEDBACK";
            break;
        case 5:
            statusString = "STATUS_UNKNOWN";
            break;
        case 6:
            statusString = "STATUS_NO_TRANSACTION";
            break;
        case 7:
            statusString = "STATUS_PREPARING";
            break;
        case 8:
            statusString = "STATUS_COMMITTING";
            break;
        case 9:
            statusString = "STATUS_ROLLING_BACK";
            break;
        default:
            statusString = "INVALID VALUE";
            break;
        }
        return statusString;
    }

    private @Nullable UserTransaction getUserTransaction() {
        if (userTransaction != null) {
            return userTransaction;
        }

        try {
            Context initialContext = new InitialContext();
            userTransaction = (UserTransaction) initialContext
                    .lookup("java:comp/UserTransaction");
        } catch (final NamingException ne) {
            //this is expected off server, just log a warning
            String message = "Not able to lookup UserTransaction from InitialContext.";
            LOGGER.warning(message, ne);
        }

        return userTransaction;
    }
}
