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

import com.sun.enterprise.transaction.TransactionImport;
import com.sun.xml.ws.tx.at.CoordinationXid;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.XATerminator;
import javax.transaction.*;
import javax.transaction.xa.Xid;
import java.util.HashMap;
import java.util.Map;

/**
 * Access hosting JTA 1.1 TransactionManager and TransactionSynchronizationRegistry.
 * <p/>
 * <p> Dependencies: Sun Java System Application Server publishes TM at JNDI name:
 *
 * @author jf39279
 */
public class TransactionManagerImpl implements TransactionManager, TransactionSynchronizationRegistry, TransactionImport {
    private static TransactionManagerImpl singleton = null;
    private TransactionManager javaeeTM;
    private TransactionSynchronizationRegistry javaeeSynchReg;
    private Map<Xid, ATTransactionImpl> jtaatTxnMap;

    // no standardized JNDI name exists across as implementations for TM, this is Sun App Server specific.
    private static final String AS_TXN_MGR_JNDI_NAME = "java:appserver/TransactionManager";

    // standardized name by JTA 1.1 spec
    private static final String TXN_SYNC_REG_JNDI_NAME = "java:comp/TransactionSynchronizationRegistry";

    static public TransactionManagerImpl getInstance() {
        if (singleton == null) {
            singleton = new TransactionManagerImpl();

            // call this to install WSAT Transaction Manager into jndi namespace.
            ATTransactionManagerImpl.getInstance();
        }
        return singleton;
    }

    static private Object jndiLookup(String jndiName) {
        Object result = null;
        try {
            Context ctx = new InitialContext();
            result = ctx.lookup(jndiName);
        } catch (NamingException e) {
            // Handle the error
            System.err.println(e);
        }
        return result;
    }


    /**
     * Creates a new instance of TransactionManagerImpl
     */
    private TransactionManagerImpl() {
        javaeeTM = (TransactionManager) jndiLookup(AS_TXN_MGR_JNDI_NAME);
        javaeeSynchReg = (TransactionSynchronizationRegistry) jndiLookup(TXN_SYNC_REG_JNDI_NAME);
        jtaatTxnMap = new HashMap<Xid, ATTransactionImpl>();
    }

    public void begin() throws NotSupportedException, SystemException {
        javaeeTM.begin();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        javaeeTM.commit();
    }

    public int getStatus() throws SystemException {
        // TODO do we want TM status or WSAT-Coordinator status
        return javaeeTM.getStatus();
    }

    public javax.transaction.Transaction getTransaction() throws SystemException {
        Transaction javaeeTxn = javaeeTM.getTransaction();
        return javaeeTxn;
    }

    public Transaction getTransaction(CoordinationContextInterface cc) throws SystemException {
        return getTransaction(cc.getIdentifier());
    }

    public Transaction getTransaction(String CoordinationCtxId) throws SystemException {
        return jtaatTxnMap.get(CoordinationXid.get(CoordinationCtxId));

    }

    public long getDefaultTransactionTimeout() {
        // TODO: get this default from application server's transaction default timeout property
        return 37376L;
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

    public Object getTransactionKey() {
        return javaeeSynchReg.getTransactionKey();
    }

    public void putResource(Object object, Object object0) {
        javaeeSynchReg.putResource(object, object0);
    }

    public Object getResource(Object object) {
        return javaeeSynchReg.getResource(object);
    }

    public void registerInterposedSynchronization(Synchronization synchronization) {
        javaeeSynchReg.registerInterposedSynchronization(synchronization);
    }

    public int getTransactionStatus() {
        // TODO:
        return javaeeSynchReg.getTransactionStatus();
    }

    public boolean getRollbackOnly() {
        // TODO:
        return javaeeSynchReg.getRollbackOnly();
    }

    // *****************************************************************************************
    // Extensions needed to JTA TransactionManager contract.

    private TransactionImport getTxnImportTM() {
        return (com.sun.enterprise.transaction.TransactionImport) javaeeTM;
    }

    /**
     * Recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction.
     * <p/>
     *
     * @param xid the Xid object representing a transaction.
     */
    public void recreate(Xid xid, long timeout) {
        getTxnImportTM().recreate(xid, timeout);
    }

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(Xid xid) {
        getTxnImportTM().release(xid);
    }

    /**
     * Used to import an external transaction into Java EE TM.
     */
    public XATerminator getXATerminator() {
        return getTxnImportTM().getXATerminator();
    }

    /**
     * Get the coordination context associated with the current transaction.
     * <p/>
     * Returns null if none set.
     */
    public CoordinationContextInterface getCoordinationContext() {
        return (CoordinationContextInterface) getResource("WSCOOR-SUN");
    }

    /**
     * Set the coordination context associated with the current transaction.
     */
    public void setCoordinationContext(CoordinationContextInterface coordinationContext) {
        putResource("WSCOOR-SUN", coordinationContext);
    }
}
