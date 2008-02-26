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
    final private Transaction txn;

    final private CoordinationContextInterface coordCtx;
    final private ATCoordinator COORDINATOR; 
            
    private static Map<String, ATTransactionImpl> coordIdATtxnMap =
            new HashMap<String, ATTransactionImpl>();

    static public ATTransactionImpl get(final String coordinationId) {
        return coordIdATtxnMap.get(coordinationId);
    }

    public void forget() {
        coordIdATtxnMap.remove(coordCtx.getIdentifier());
    }

    /**
     * Creates a new instance of ATTransactionImpl
     */
    public ATTransactionImpl(Transaction jtaTxn, CoordinationContextInterface coordCtx) {
        this.txn = jtaTxn;
        this.coordCtx = coordCtx;
        COORDINATOR = 
               (ATCoordinator) CoordinationManager.getInstance().getCoordinator(coordCtx.getIdentifier());
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        //TODO: First pass only.  
        txn.commit();
    }

    public boolean delistResource(final XAResource xAResource, final int state) throws IllegalStateException, SystemException {
        // TODO: First pass only.
        return txn.delistResource(xAResource, state);
    }

    /**
     * Wrapper XAResource as a participant and enlist with this transaction's coordinator.
     * <p/>
     * Contemplating removing this method but leave for now.
     * <p/>
     * XAResources from managed connections are automatically enlisted with Java EE transaction.
     */
    public boolean enlistResource(final XAResource xaResource) throws RollbackException, IllegalStateException, SystemException {
        return txn.enlistResource(xaResource);

    }

    public boolean enlistParticipant(final Participant participant) {
        final ATParticipant atParticipant =
                new ATParticipant(getATCoordinator(), participant);
        atParticipant.register();
        return true;
    }


    public void registerSynchronization(final Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
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
        return COORDINATOR;
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
