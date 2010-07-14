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
