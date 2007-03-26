/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.tx.common;

import com.sun.enterprise.transaction.TransactionImport;
import java.util.logging.Level;
import javax.resource.spi.XATerminator;
import javax.transaction.SystemException;
import javax.transaction.xa.Xid;

/**
 *  Access Transaction Inflow Contract from Java Connector 1.5 API.
 *  Assumption is the underlying TransactionManager is implementing this 
 *  interface.
 *
 *  Separate this from TransactionManagerImpl since this provides mostly service side assistance.
 *  Assists in supporting application client and standalone client to separate from more commonly
 *  used methods in TransactionManagerImpl.
 */
public class TransactionImportManager implements TransactionImport {
    
    final static private TxLogger logger = TxLogger.getATLogger(TransactionManagerImpl.class);
    
    static final TransactionImport instance = new TransactionImportManager();
    static private TransactionImport javaeeTM;
    
    private TransactionImportManager() {
        try {
            javaeeTM = (TransactionImport)TransactionManagerImpl.getInstance().getTransactionManager();
        } catch (ClassCastException cce) {
            final String CLASSNAME = javaeeTM == null ? "null" : javaeeTM.getClass().getName();
            logger.severe("TransactionImportManager", 
                          LocalizationMessages.NO_TXN_IMPORT_2014(CLASSNAME), cce);
        }
    }
    
    public static TransactionImport getInstance() {
        return instance;
    }

    /**
     * Recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction.
     * <p/>
     *
     * @param xid the Xid object representing a transaction.
     */
    public void recreate(final Xid xid, final long timeout) {
       javaeeTM.recreate(xid, timeout);
    }

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(final Xid xid) {
        javaeeTM.release(xid);
    }

    /**
     * Used to import an external transaction into Java EE TM.
     */
    public XATerminator getXATerminator() {
        return javaeeTM.getXATerminator();
    }

     /**
     * Returns in seconds duration till current transaction times out.
     * Returns negative value if transaction has already timedout.
     * Returns 0 if there is no timeout.
     * Returns 0 if any exceptions occur looking up remaining transaction timeout.
     */
    public int getTransactionRemainingTimeout() throws SystemException {
        final String METHOD = "getRemainingTimeout";
        int result = 0;
        try {
            result = javaeeTM.getTransactionRemainingTimeout();
        } catch (IllegalStateException ise) {
            if (logger.isLogging(Level.FINEST)) {
                logger.finest(METHOD, "looking up remaining txn timeout, no current transaction", ise);
            } else {
                logger.info(METHOD, LocalizationMessages.TXN_MGR_OPERATION_FAILED_2008("getTransactionRemainingTimeout",
                                                                                    ise.getLocalizedMessage()));
            }
        } catch (Throwable t) {
            if (logger.isLogging(Level.FINEST)) {
                logger.finest(METHOD, "ignoring exception " + t.getClass().getName() + " thrown calling" +
                        "TM.getTransactionRemainingTimeout method" );
            } else {
                logger.info(METHOD, LocalizationMessages.TXN_MGR_OPERATION_FAILED_2008("getTransactionRemainingTimeout", 
                        t.getLocalizedMessage()));
         
            }
        }
        return result;
    }
}
