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
import com.sun.xml.ws.api.tx.Participant;
import com.sun.xml.ws.tx.at.ATCoordinator;
import com.sun.xml.ws.tx.at.ATParticipant;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.CoordinationManager;
import com.sun.xml.ws.tx.coordinator.Coordinator;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import java.util.HashMap;
import java.util.Map;
//import com.sun.xml.ws.tx.webservice.member.at.CompletionInitiatorPortType;
//import com.sun.xml.ws.tx.webservice.member.coord.CoordinationContextType;

/**
 * Represents an WS-AT capable Transaction.
 * <p/>
 * <p>Also has reference to related Java EE transaction that it is associated with.
 *
 * @author jf39279
 */
public class ATTransactionImpl implements ATTransaction {
    // related Java EE transaction
    private Transaction txn;

    private CoordinationContextInterface cc;
    private ATCoordinator coordinator = null;

    private static Map<String, ATTransactionImpl> coordIdATtxnMap =
            new HashMap<String, ATTransactionImpl>();

    static public ATTransactionImpl get(String coordinationIdentity) {
        return coordIdATtxnMap.get(coordinationIdentity);
    }

    public void forget() {
        coordIdATtxnMap.remove(cc.getIdentifier());
    }

    /**
     * Creates a new instance of ATTransactionImpl
     */
    public ATTransactionImpl(Transaction jtaTxn, CoordinationContextInterface cc) {
        this.txn = jtaTxn;
        this.cc = cc;
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        //TODO: First pass only.  
        txn.commit();
    }

    public boolean delistResource(XAResource xAResource, int i) throws IllegalStateException, SystemException {
        // TODO: First pass only.
        return txn.delistResource(xAResource, i);
    }

    /**
     * Wrapper XAResource as a participant and enlist with this transaction's coordinator.
     * <p/>
     * Contemplating removing this method but leave for now.
     * <p/>
     * XAResources from managed connections are automatically enlisted with Java EE transaction.
     */
    public boolean enlistResource(XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
        return txn.enlistResource(xaResource);

    }

    public boolean enlistParticipant(Participant participant) {
        ATParticipant atParticipant =
                new ATParticipant(getATCoordinator(), participant);
        atParticipant.register();
        return true;
    }


    public void registerSynchronization(Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
        txn.registerSynchronization(synchronization);
    }

    public void rollback() throws IllegalStateException, SystemException {
        txn.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        txn.setRollbackOnly();
    }

    /**
     * Get coordinator for this transaction.
     */
    private Coordinator getATCoordinator() {
        if (coordinator == null) {
            coordinator = (ATCoordinator) CoordinationManager.getInstance().getCoordinator(cc.getIdentifier());
        }
        return coordinator;
    }

    // TODO Support transaction created on remote Coordination Service
    //      Use WSAT Completion Protocol when transaction initiator process remote from coordinator service.
    //      (note: not typical case so not high priority to implement)
    //private CompletionInitiatorPortType compWS;
    //private javax.xml.ws.addressing.EndpointReference completionCoordinatorEPR;

    public int getStatus() throws SystemException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
