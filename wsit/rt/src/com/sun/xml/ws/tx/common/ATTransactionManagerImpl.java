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
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.api.tx.ATTransaction;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.HashMap;
import java.util.Map;

/**
 * Inject WS-Coordination/WS-AtomicTransaction support into JTA 1.1 TransactionManager and
 * TransactionSynchronizationRegistry.
 * <p/>
 *
 * @author jf39279
 */
public class ATTransactionManagerImpl implements TransactionManager {

    final private static ATTransactionManagerImpl singleton = new ATTransactionManagerImpl();
    final private TransactionManager javaeeTM;
    final private TransactionSynchronizationRegistry javaeeSynchReg;
    final private Map<String, ATTransaction> coordIdTxnMap;


    static public ATTransactionManagerImpl getInstance() {
        return singleton;
    }

    public ATTransaction getTransaction(final String coordId) {
        return coordIdTxnMap.get(coordId);
    }

    /**
     * Creates a new instance of TransactionManagerImpl
     */
    private ATTransactionManagerImpl() {
        javaeeTM = TransactionManagerImpl.getInstance();
        javaeeSynchReg = (TransactionSynchronizationRegistry) javaeeTM;
        coordIdTxnMap = new HashMap<String, ATTransaction>();
    }

    public void begin() throws NotSupportedException, SystemException {
        javaeeTM.begin();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        javaeeTM.commit();
    }

    public int getStatus() throws SystemException {
        // TODO return ATCoordinator status mapped to java ee transaction status
        // return javaeeTM.getStatus();
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public javax.transaction.Transaction getTransaction() throws SystemException {
        ATTransaction result = null;
        final Transaction jtaTxn = javaeeTM.getTransaction();
        if (jtaTxn == null) {
            return result;
        } 
        CoordinationContextInterface cc = this.getCoordinationContext();
        if (cc != null) {
            result = getTransaction(cc.getIdentifier());
            if (result == null) {
                result = new ATTransactionImpl(jtaTxn, cc);
                coordIdTxnMap.put(cc.getIdentifier(), result);
            }
        }
        return result;
    }

    public long getDefaultTransactionTimeout() {
        // TODO: should this be a WS-AT default transaction timeout
        return 0;
    }


    public void resume(final Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        javaeeTM.resume(transaction);
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        javaeeTM.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException {
        javaeeSynchReg.setRollbackOnly();
    }

    public void setTransactionTimeout(final int seconds) throws SystemException {
        javaeeTM.setTransactionTimeout(seconds);
    }

    public Transaction suspend() throws SystemException {
        return javaeeTM.suspend();
    }

    /**
     * Get the coordination context associated with the current transaction.
     * <p/>
     * Returns null if none set.
     */
    public CoordinationContextInterface getCoordinationContext() {
        try { 
            return (CoordinationContextInterface) javaeeSynchReg.getResource("WSCOOR-SUN");
        } catch (IllegalStateException ise) {
            return null;  // no current JTA transaction, so return null
        }
    }

    /**
     * Set the coordination context associated with the current transaction.
     */
    public void setCoordinationContext(final CoordinationContextInterface coordinationCtx) {
        javaeeSynchReg.putResource("WSCOOR-SUN", coordinationCtx);
    }


}
