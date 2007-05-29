/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
