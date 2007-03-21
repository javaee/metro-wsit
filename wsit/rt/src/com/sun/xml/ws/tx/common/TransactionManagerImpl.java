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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

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
    final private static TransactionManagerImpl singleton = new TransactionManagerImpl();
    final private TransactionManager javaeeTM;
    final private TransactionSynchronizationRegistry javaeeSynchReg;

    final static private TxLogger logger = TxLogger.getATLogger(TransactionManagerImpl.class);

    // no standardized JNDI name exists across as implementations for TM, this is Sun App Server specific.
    private static final String AS_TXN_MGR_JNDI_NAME = "java:appserver/TransactionManager";

    // standardized name by JTA 1.1 spec
    private static final String TXN_SYNC_REG_JNDI_NAME = "java:comp/TransactionSynchronizationRegistry";
    
    private static final String USER_TRANSACTION_JNDI_NAME = "java:comp/UserTransaction";

    static public TransactionManagerImpl getInstance() {
        return singleton;
    }

    static private Object jndiLookup(final String jndiName) {
        Object result = null;
        try {
            final Context ctx = new InitialContext();
            result = ctx.lookup(jndiName);
        } catch (NamingException e) {
            logger.warning("jndiLookup", LocalizationMessages.FAILED_JNDI_LOOKUP_2001(jndiName));
        }
        return result;
    }
    
    public UserTransaction getUserTransaction() {
        return (UserTransaction)jndiLookup(USER_TRANSACTION_JNDI_NAME);
    }
    
    /**
     * Creates a new instance of TransactionManagerImpl
     */
    private TransactionManagerImpl() {
        javaeeTM = (TransactionManager) jndiLookup(AS_TXN_MGR_JNDI_NAME);
        javaeeSynchReg = (TransactionSynchronizationRegistry) jndiLookup(TXN_SYNC_REG_JNDI_NAME);
    }
    
    public void begin() throws NotSupportedException, SystemException {
        javaeeTM.begin();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        javaeeTM.commit();
    }

    public int getStatus() throws SystemException {
        return javaeeTM.getStatus();
    }

    public javax.transaction.Transaction getTransaction() throws SystemException {
        return javaeeTM.getTransaction();
    }

    public void resume(final Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {
        javaeeTM.resume(transaction);
        servletPreInvokeTx();
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
        servletPostInvokeTx(true);
        return javaeeTM.suspend();
    }

    public Object getTransactionKey() {
        return javaeeSynchReg.getTransactionKey();
    }

    public void putResource(final Object object, final Object object0) {
        javaeeSynchReg.putResource(object, object0);
    }

    public Object getResource(final Object object) {
        return javaeeSynchReg.getResource(object);
    }

    public void registerInterposedSynchronization(final Synchronization synchronization) {
        javaeeSynchReg.registerInterposedSynchronization(synchronization);
    }
    
    public void registerSynchronization(final Synchronization sync) {
        final String METHOD="registerSynchronization";
        
        if (sync == null) {
            return;
        }
        
        Transaction txn = null;
        try {
            txn = javaeeTM.getTransaction();
        } catch (SystemException ex) {
             logger.info(METHOD, LocalizationMessages.OPERATION_FAILED_2010("getTransaction"), ex);
        }
        if (txn == null) {
            logger.warning(METHOD, LocalizationMessages.REGISTER_SYNCH_NO_CURRENT_TXN_2011(sync.getClass().getName()));
        } else {
            try {
                txn.registerSynchronization(sync);
            } catch (IllegalStateException ex) {
                   logger.info(METHOD, LocalizationMessages.OPERATION_FAILED_2010(METHOD), ex);
            } catch (RollbackException ex) {
                   logger.info(METHOD, LocalizationMessages.OPERATION_FAILED_2010(METHOD), ex);
            } catch (SystemException ex) {
                  logger.info(METHOD, LocalizationMessages.OPERATION_FAILED_2010(METHOD), ex);
            }
        }
    }

    public int getTransactionStatus() {
        return javaeeSynchReg.getTransactionStatus();
    }

    public boolean getRollbackOnly() {
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
    public void recreate(final Xid xid, final long timeout) {
        getTxnImportTM().recreate(xid, timeout);
    }

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(final Xid xid) {
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
    public void setCoordinationContext(final CoordinationContextInterface coordCtx) {
        putResource("WSCOOR-SUN", coordCtx);
    }
    
    static private Method getMethod(Class theClass, String methodName, Class param) {
        Method method = null;
        try {
            if (param == null) {
                method = theClass.getMethod(methodName);
            } else {
                method = theClass.getMethod(methodName, param);
            }
            logger.finest("getMethod", "found Sun App Server 9.1 container specific method via reflection " + theClass.getName() + "."  + methodName);
        } catch (Exception e) {
            logger.finest("getMethod", "reflection lookup of  " + theClass.getName() + "." + methodName + "("
                   + (param == null ? "" : param.getName()) 
                   + ") failed with handled exception ", e);
        }
        return method;
    }
    
    static private boolean initialized = false;
    static private Method servletPreInvokeTxMethod = null;
    static private Method servletPostInvokeTxMethod = null;
    
    private void initServletMethods() {
         if (initialized == false) {
            initialized = true;
            servletPreInvokeTxMethod = getMethod(javaeeTM.getClass(), "servletPreInvokeTx", null);
            servletPostInvokeTxMethod = getMethod(javaeeTM.getClass(), "servletPostInvokeTx", boolean.class);
         }
    }
    
     /**
     * PreInvoke Transaction configuration for Servlet Container.
     * BaseContainer.preInvokeTx() handles all this for CMT EJB.
     *
     * Compensate that J2EEInstanceListener.handleBeforeEvent(BEFORE_SERVICE_EVENT)
     * gets called before WSIT WSTX Service pipe associates a JTA txn with incoming thread.
     *
     * Precondition: assumes JTA transaction already associated with current thread.
     * 
     * Note: this method is a no-op when invoked on an EJB.
     */
    public void servletPreInvokeTx() {
       final String METHOD = "servletPreInvokeTx";
       initServletMethods();
       if (servletPreInvokeTxMethod != null) {
            try {
                servletPreInvokeTxMethod.invoke(javaeeTM);
            } catch (Throwable ex) {
                logger.info(METHOD, LocalizationMessages.OPERATION_FAILED_2010(METHOD), ex);
            }
       }
    }
    
    /**
     * PostInvoke Transaction configuration for Servlet Container.
     * BaseContainer.preInvokeTx() handles all this for CMT EJB.
     *
     * Precondition: assumed called prior to current transcation being suspended or released.
     *
     * Note: this method is a no-op when invoked on an EJB. The J2EE method only has an effect
     * on servlets.
     * 
     * @param suspend indicate whether the delisting is due to suspension or transaction completion(commmit/rollback)
     */
    public void servletPostInvokeTx(Boolean suspend) {
          final String METHOD = "servletPostInvokeTx";
       initServletMethods();
       if (servletPostInvokeTxMethod != null) {
            try {
                servletPostInvokeTxMethod.invoke(javaeeTM, suspend);
            } catch (Throwable ex) {
                 logger.info(METHOD, LocalizationMessages.OPERATION_FAILED_2010(METHOD), ex);
            }
       }
    }
 
    public int getTransactionRemainingTimeout() throws SystemException {
        final String METHOD = "getRemainingTimeout";
        int result = 0;
        try {
            result = getTxnImportTM().getTransactionRemainingTimeout();
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
    
    /**
     * Returns in seconds duration till current transaction times out.
     * Returns negative value if transaction has already timedout.
     * Returns 0 if there is no timeout.
     * Returns 0 if any exceptions occur looking up remaining transaction timeout.
     */
    public int getRemainingTimeout() {
        final String METHOD="getRemainingTimeout";
        try {
            return getTransactionRemainingTimeout();
        } catch (SystemException se) {
            if (logger.isLogging(Level.FINEST)) {
                logger.finest(METHOD, "getRemainingTimeout stack trace", se);
            } else {
                logger.info(METHOD, 
                        LocalizationMessages.TXN_MGR_OPERATION_FAILED_2008("getTransactionRemainingTimeout",
                                                                           se.getLocalizedMessage()));
            }
            return 0;
        }
    }
}
