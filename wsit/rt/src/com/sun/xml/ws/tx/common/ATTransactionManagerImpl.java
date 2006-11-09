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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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

    private static ATTransactionManagerImpl singleton = null;
    private TransactionManager javaeeTM;
    private TransactionSynchronizationRegistry javaeeSynchReg;
    private Map<String, ATTransaction> coordIdTxnMap;


    static public ATTransactionManagerImpl getInstance() {
        if (singleton == null) {
            singleton = new ATTransactionManagerImpl();
            // Application server does not allow this.  Only known app server services can use this technique.
            // Pursuing alternative mechanism.
            // jndiBind("java:comp/env/WSATTransactionManager", singleton);
        }
        return singleton;
    }

    static private void jndiBind(String jndiName, Object obj) {
        try {
            Context ctx = new InitialContext();
            ctx.bind(jndiName, obj);
        } catch (NamingException e) {
            // Handle the error
            System.err.println(e);
        }
    }

    public ATTransaction getTransaction(String coordId) {
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
        String currentCoordId = getCoordinationContext().getIdentifier();
        ATTransaction result = getTransaction(currentCoordId);
        if (result == null) {
            result = new ATTransactionImpl(javaeeTM.getTransaction(), getCoordinationContext());
            coordIdTxnMap.put(currentCoordId, result);
        }
        return result;
    }

    public long getDefaultTransactionTimeout() {
        // TODO: should this be a WS-AT default transaction timeout
        return 0;
    }


    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        javaeeTM.resume(transaction);
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        javaeeTM.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException {
        javaeeSynchReg.setRollbackOnly();
    }

    public void setTransactionTimeout(int i) throws SystemException {
        javaeeTM.setTransactionTimeout(i);
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
        return (CoordinationContextInterface) javaeeSynchReg.getResource("WSCOOR-SUN");
    }

    /**
     * Set the coordination context associated with the current transaction.
     */
    public void setCoordinationContext(CoordinationContextInterface coordinationContext) {
        javaeeSynchReg.putResource("WSCOOR-SUN", coordinationContext);
    }


}
