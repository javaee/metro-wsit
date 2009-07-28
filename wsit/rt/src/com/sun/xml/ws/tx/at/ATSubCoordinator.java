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
package com.sun.xml.ws.tx.at;

import com.sun.enterprise.transaction.TransactionImport;
import com.sun.xml.ws.api.tx.Participant;
import com.sun.xml.ws.api.tx.Protocol;
import com.sun.xml.ws.api.tx.TXException;
import com.sun.xml.ws.tx.common.TransactionImportManager;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.Registrant;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import javax.resource.spi.XATerminator;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.WebServiceContext;
import java.util.logging.Level;
import javax.xml.ws.WebServiceException;

/**
 * @author jf39279
 */
public class ATSubCoordinator extends ATCoordinator {
    static private TxLogger logger = TxLogger.getATLogger(ATCoordinator.class);
    
    final TransactionImport importTm = TransactionImportManager.getInstance();

    // This Subordinate coordinator is a volatile participant of parent coordinator.
    private ATParticipant rootVolatileParticipant = null;
    // This subordinate coordinator is also a durable participant of its parent coordinator.
    private ATParticipant rootDurableParticipant = null;

    private boolean guardTimeout = false;

    private boolean forgotten = false;
    
    /**
     * Creates a new instance of ATSubCoordinator
     */
    public ATSubCoordinator(CoordinationContextInterface context, CreateCoordinationContextType request) {
        super(context, request);
        assert getContext().getRootRegistrationService() != null;
    }

    public ATSubCoordinator(CoordinationContextInterface context) {
        super(context);
        assert getContext().getRootRegistrationService() != null;
    }

    @Override
    public void setTransaction(final Transaction txn) {
        super.setTransaction(txn);
        if (txn == null) {
            xaTerminator = null;
        } else if (xaTerminator == null) {
            xaTerminator = importTm.getXATerminator();
        }
    }

    @Override
    protected void registerWithVolatileParent() {
        if (rootVolatileParticipant == null) {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("ATSubCoordinator.registerWithVolatileParent", "register volatile2PC with coordId:" +
                        getIdValue());
            }
            rootVolatileParticipant = new ATParticipant(this, new VolatileParticipant());
            rootVolatileParticipant.register();
        }
    }

    @Override
    protected boolean registerWithDurableParent() {
        boolean result = false;
        if (rootDurableParticipant == null) {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("ATSubCoordinator.registerWithDurableParent", "register durable2PC with coordId:" +
                        getIdValue());
            }
            rootDurableParticipant = new ATParticipant(this, new DurableParticipant());
            try {
                rootDurableParticipant.register();
                result = true;
            } catch (Exception e) {
                logger.severe("registerWithDurableParent", LocalizationMessages.REG_WITH_DURABLE_FAILED_0022(getCoordIdPartId(rootDurableParticipant)));
                throw new WebServiceException(
                        LocalizationMessages.REG_WITH_DURABLE_FAILED_0022(getCoordIdPartId(rootDurableParticipant)),
                        e);
                
            }
        }
        return result;
    }

    @Override
    public boolean isSubordinateCoordinator() {
        return true;
    }

    private Xid xid = null;

    public Xid getCoordinationXid() {
        if (xid == null) {
            xid = CoordinationXid.lookupOrCreate(getContext().getIdentifier());
        }
        return xid;
    }

    public class VolatileParticipant implements Participant {
        public Protocol getProtocol() {
            return Protocol.VOLATILE;
        }

        public Participant.STATE prepare() throws TXException {
            synchronized(this) {
                initiateVolatilePrepare();
                waitForVolatilePrepareResponse();
                if (isAborting()) {
                    rootVolatileParticipant.aborted();
                    throw new TXException("VolatileParticipant.prepare aborted");
                } else if (getVolatileParticipants().size() == 0) {
                    rootVolatileParticipant.readonly();
                    return Participant.STATE.P_READONLY;
                } else {
                    rootVolatileParticipant.prepared();
                    guardTimeout = true;
                }
            }
            return Participant.STATE.P_OK;
        }

        public void commit() {
            initiateVolatileCommit();
            rootVolatileParticipant.committed();
        }

        public void abort() {
            initiateVolatileRollback();
            // TODO: is waitForVolatileRollbackResponse needed.
            rootVolatileParticipant.aborted();
        }
    }

    public class DurableParticipant implements Participant {
        int xaResult = XAResource.XA_OK;

        public Protocol getProtocol() {
            return Protocol.DURABLE;
        }

        public Participant.STATE prepare() throws TXException {
            Participant.STATE result = Participant.STATE.P_OK;
            final String METHOD = "durableParticipant";
            if (logger.isLogging(Level.FINER)) {
                logger.entering(METHOD, getCoordIdPartId(rootDurableParticipant));
            }
            
            // synchronize to avoid race conditions between Coordinator time out and potentially
            // multiple WS-AT prepare messages. (Coordinator can resend prepare after some interval
            // has passed with no reply.
            synchronized (this) {
                initiateDurablePrepare();
                if (getXATerminator() != null) {
                    try {
                        if (logger.isLogging(Level.FINER)) {
                            logger.entering("XATerminator.prepare()");
                        }
                        xaResult = getXATerminator().prepare(getCoordinationXid());
                        if (logger.isLogging(Level.FINER)) {
                            logger.exiting("XATerminator.prepare()", xaResult);
                        }
                    } catch (XAException ex) {
                        setAborting();
                        if (logger.isLogging(Level.FINEST)) {
                            logger.finest(METHOD, LocalizationMessages.XATERM_THREW_0023(ex.getClass().getName()), ex);
                        } else {
                            logger.info(METHOD, LocalizationMessages.XATERM_THREW_0023(ex.getClass().getName()));
                        }
                        // TODO: set linked exception ex to TxException
                        throw new TXException(ex.getClass().getName());
                    }
                    
                    // Next line required to support remote participants in this coordinators durable participants.
                    waitForDurablePrepareResponse();
                    if (isAborting()) {
                        abort();
                    } else if (xaResult == XAResource.XA_RDONLY && getDurableParticipants().size() == 0) {
                        guardTimeout = true;
                        rootDurableParticipant.readonly();
                        result = Participant.STATE.P_READONLY;
                    } else if (xaResult == XAResource.XA_OK) {  //implied no durable participants are aborting since isAborting() is false
                        // Participant Subordinator coordinator must not timeout after sending 
                        // prepared to superior coordinator.
                        guardTimeout = true;
                        rootDurableParticipant.prepared();
                    }
                }
            }
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("ATSubCoordinator.durableParticipant", getCoordIdPartId(rootDurableParticipant));
            }
            
            return result;
        }

        public void commit() {
            final String METHOD = "ATSubCoordinator.DurableParticipant.commit";
            boolean xaCommitFailed = false;
            synchronized(this) {
                initiateDurableCommit();
                if (getXATerminator() != null && xaResult == XA_OK) {
                    try {
                        getXATerminator().commit(getCoordinationXid(), false);
                    } catch (XAException ex) {
                        xaCommitFailed = true;
                        
                        logger.severe(METHOD, LocalizationMessages.XATERM_THREW_0023(ex.getLocalizedMessage()));
                        
                    }
                }
                if (xaCommitFailed || isAborting()) {
                    logger.severe(METHOD, LocalizationMessages.ABORT_DURING_COMMIT_0024(getIdValue()));
                    rootDurableParticipant.aborted();
                } else {
                    logger.info(METHOD, LocalizationMessages.COMMITTED_SUB_COOR_0025(getIdValue()));
                    rootDurableParticipant.committed();
                }
            }
        }

        public void abort() {
            final String METHOD = "ATSubCoordinator.DurableParticipant.abort";
            synchronized(this) {
                if (getXATerminator() != null && xaResult == XA_OK) {
                    try {
                        logger.severe(METHOD, LocalizationMessages.XATERM_ABORT_0026(getIdValue()));
                        getXATerminator().rollback(getCoordinationXid());
                    } catch (XAException ex) {
                        logger.severe(METHOD, LocalizationMessages.CAUGHT_XAEX_0027(ex.getMessage()));
                    }
                }
                initiateDurableRollback();
                waitForCommitOrRollbackResponse(Protocol.DURABLE);
                rootDurableParticipant.aborted();
            }
        }
    }

    private XATerminator xaTerminator = null;

    private XATerminator getXATerminator() {
        if (xaTerminator == null) {
            xaTerminator = importTm.getXATerminator();
        }
        return xaTerminator;
    }

    // Synchronization

    /**
     * Subordinate Coordinator does not use this mechanism. Ensure it is not called.
     */
    @Override
    public void beforeCompletion() {
        // Ensure that beforeCompletion disabled for subordinate coordinator
        // Subordinate coordinator should not be registered with TransactionSynchronizationRegistry.
        throw new UnsupportedOperationException("No beforeCompletion for subordinate coordinator");
    }

    @Override
    public void afterCompletion(final int i) {
        // Ensure that afterCompletion disabled for subordinate coordinator    
        throw new UnsupportedOperationException("No afterCompletion for subordinate coordinator");
    }

    // XAResource  Not implemented yet

    /**
     * Synchronous prepare request from subordinate coordinator.
     * <p/>
     * <p>Prepare this coordinator and return result of preparation.
     */
    @Override
    public int prepare(final Xid xid) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_prepare(xid=" + xid + ")");
        }
        int result = 0;
        result = XA_OK;
        // TODO: Prepare AT Subordinate Coordinator state so it can be recovered in case of failure.
        //       Only durable participants need to be recoverable.

        // This coordinator prepares when root ws-at coordinator sends a prepare to this subordinate
        // coordinator that is a participant to that coordinator.
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_prepare", result);
        }
        return result;
    }

    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_commit(xid=" + xid + " ,onePhase=" + onePhase + ")");
        }
        // TODO: Commit AT Subordinate Coordinator state so it can be recovered in case of failure.
        //       Only durable participants need to be recoverable.

        // This coordinator commits when root ws-at coordinator sends a prepare to this subordinate
        // coordinator that is a participant to that coordinator.
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_commit");
        }
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_rollback(xid=" + xid + ")");
        }
        // TODO: Rollack AT Subordinate Coordinator state.
        //       Only durable participants require to be rolled back.

        // This coordinator rolls back when root ws-at coordinator sends a abort to this subordinate
        // coordinator that is a participant to that coordinator.
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_rollback");
        }
    }

    @Override
    public void addRegistrant(final Registrant registrant, final WebServiceContext wsContext) {
        //
        if (registerWithRootRegistrationService(registrant)) {
            // registrant is either volatile or durable participant with subordinate coordinator's parent coordinator.
            // do not add this participant to ATSubCoordinator list of participants it is managing.
            return;
        }
        super.addRegistrant(registrant, wsContext);
        switch (registrant.getProtocol()) {
            case VOLATILE:
                registerWithVolatileParent();
                break;

            case DURABLE:
                registerWithDurableParent();
                break;

            case COMPLETION:
                throw new UnsupportedOperationException("can not register for completion with subordinate coordinator");
        }
    }

    /**
     * Get the registrant with the specified id or null if it does not exist.
     *
     * @param id the registrant id
     * @return the Registrant object or null if the id does not exist
     */
    @Override
    public Registrant getRegistrant(final String id) {
        Registrant result = super.getRegistrant(id);

        // check subordinate participants
        if (result == null && rootVolatileParticipant != null &&
            rootVolatileParticipant.getIdValue().equals(id)) {
                result = rootVolatileParticipant;
        }
        if (result == null && rootDurableParticipant != null &&
            rootDurableParticipant.getIdValue().equals(id)) {
                result = rootDurableParticipant;
        }

        return result;
    }

    @Override
    public void removeRegistrant(final String id) {
        forget(id);
    }

    /**
     * Represent all volatile and durable participants for this subordinate coordinator through
     * two special participants of SubordinateCoordinator. They are the only participants that parent
     * coordinator is aware of.
     */
    @Override
    public boolean registerWithRootRegistrationService(final Registrant participant) {
        // Note: intended instance comparision, not object comparison
        if (participant == rootVolatileParticipant || participant == rootDurableParticipant) {
            return true;
        } else {
            return super.registerWithRootRegistrationService(participant);
        }
    }

    @Override
    public boolean hasOutstandingParticipants() {
        return rootDurableParticipant != null || rootVolatileParticipant != null || super.hasOutstandingParticipants();
    }    
    
    @Override
    public void forget(final String partId) {
        if (rootVolatileParticipant != null && rootVolatileParticipant.getIdValue().equals(partId)) {
            rootVolatileParticipant = null;
            if (logger.isLogging(Level.FINE)) {
                logger.fine("forget", "forgot volatile participant link to parent " + getCoordIdPartId(partId));
            }
            if (!hasOutstandingParticipants()) {
                forget();
            }
            return;
        }
        if (rootDurableParticipant != null && rootDurableParticipant.getIdValue().equals(partId)) {
            rootDurableParticipant = null;
            if (logger.isLogging(Level.FINE)) {
                logger.fine("forget", "forgot durable participant link to parent " + getCoordIdPartId(partId));
            }
            if (!hasOutstandingParticipants()) {
                forget();
            }
            return;
        }

        // see if just a regular participant of this coordinator
        super.forget(partId);
    }

  
    
    @Override
    public boolean expirationGuard() {
        synchronized (this) {
            return guardTimeout;
        }
    }

    @Override
    public void forget() {
        synchronized(this) {
            if (forgotten) {
                return;
            } else {
                forgotten = true;
            }
            if (rootVolatileParticipant != null){
                rootVolatileParticipant.forget();
                rootVolatileParticipant = null;
            }
            if (rootDurableParticipant != null){
                rootDurableParticipant.forget();
                rootDurableParticipant = null;
            }
            CoordinationXid.forget(this.getIdValue());
            super.forget();
        }
    }
  
     /**
     * Import a transactional context from an external transaction manager via WS-AT Coordination Context
     * that was propagated in a SOAP request message.
     *
     * @see #endImportTransaction()
     */
    public void beginImportTransaction() {
        Transaction currentTxn = null;
        
        try {    
            importTm.recreate(getCoordinationXid(), getExpires());
        } catch (IllegalStateException ex) {
            String message = LocalizationMessages.IMPORT_TRANSACTION_FAILED_0028(getIdValue(),
                                                                                 getCoordinationXid().toString());
            logger.warning("beginImportTransaction", message, ex);
            throw new WebServiceException(message, ex);
        } 
        try{  
            currentTxn = tm.getTransaction();
        } catch (SystemException ex) {
            String message = LocalizationMessages.IMPORT_TXN_GET_TXN_FAILED_0030(getIdValue());
            logger.warning("beginImportTransaction", message, ex);
            throw new WebServiceException(message, ex);
        }     
        assert currentTxn != null;
        setTransaction(currentTxn);
        tm.setCoordinationContext(getContext());
    }

    /**
     * Ends the importing of an external transaction.
     * <p/>
     * <p> Post-condition: terminates beginImportTransaction.
     *
     * @see #beginImportTransaction()
     */
    public void endImportTransaction() { 
        if (transaction != null) {
            try {
                importTm.release(getCoordinationXid());
            } catch (Error e) {
                logger.warning("endImportTransaction",
                        LocalizationMessages.EXCEPTION_RELEASING_IMPORTED_TRANSACTION_0029(), e);
            }
            setTransaction(null);
        }
    }

    
}
