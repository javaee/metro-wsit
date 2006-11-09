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
package com.sun.xml.ws.tx.at;

import com.sun.xml.ws.api.tx.Participant;
import com.sun.xml.ws.api.tx.Protocol;
import com.sun.xml.ws.api.tx.TXException;
import com.sun.xml.ws.tx.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.Registrant;
import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;

import javax.resource.spi.XATerminator;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.logging.Level;

/**
 * @author jf39279
 */
public class ATSubCoordinator extends ATCoordinator {
    static private TxLogger logger = TxLogger.getATLogger(ATCoordinator.class);

    // get EPR from rootVolatileParticipant.getCoordinationProtocolService() (after registerResponse)
    private CoordinatorPortType parentVolatileCoordinator = null;
    // get EPR from rootDurableParticipant.getCoordinationProtocolService() (after registerResponse)
    private CoordinatorPortType parentDurableCoordinator = null;

    // This Subordinate coordinator is a volatile participant of parent coordinator.
    private ATParticipant rootVolatileParticipant = null;
    // This subordinate coordinator is also a durable participant of its parent coordinator.
    private ATParticipant rootDurableParticipant = null;


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

    public void setTransaction(Transaction txn) {
        super.setTransaction(txn);
        if (txn != null && xaTerminator != null) {
            xaTerminator = TransactionManagerImpl.getInstance().getXATerminator();
        }
    }

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
                // TODO: must retry in future several times.
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("registerWithDurableParent", " failed. " + getCoordIdPartId(rootDurableParticipant));
                }
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean isSubordinateCoordinator() {
        return true;
    }

    private Xid xid = null;

    private Xid getCoordinationXid() {
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
            }
            return Participant.STATE.P_OK;
        }

        public void commit() {
            initiateVolatileCommit();
            // TODO: is waitForVolatileCommitResponse() needed.
            rootVolatileParticipant.committed();
        }

        public void abort() {
            // TODO: should this only be volatile participants or is okay to do both.
            initiateVolatileRollback();
            // TODO: is waitForVolatileRollbackResponse needed.
            rootVolatileParticipant.aborted();
        }
    }

    public class DurableParticipant implements Participant {
        int xaResult = XAResource.XA_RDONLY;

        public Protocol getProtocol() {
            return Protocol.DURABLE;
        }

        public Participant.STATE prepare() throws TXException {
            logger.entering("ATSubCoordinator.durableParticipant", getCoordIdPartId(rootDurableParticipant));
            XAException xaPrepareException = null;
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
                    if (logger.isLogging(Level.INFO)) {
                        logger.info("DurableParticipant.prepare", "XATerminator threw exception " + ex.getLocalizedMessage());
                    }
                    throw new TXException("DurableParticipant.prepare threw " + ex.getClass().getName());
                }
            }
            // Next line required to support remote participants in this coordinators durable participants.
            //waitForDurablePrepareResponse();
            if (isAborting()) {
                abort();
            } else if (xaResult == XAResource.XA_RDONLY && getDurableParticipants().size() == 0) {
                rootVolatileParticipant.readonly();
            } else if (xaResult == XAResource.XA_OK)
            {  //implied no durable participants are aborting since isAborting() is false
                rootVolatileParticipant.prepared();
            }
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("ATSubCoordinator.durableParticipant", getCoordIdPartId(rootDurableParticipant));
            }
            return Participant.STATE.P_OK;
        }

        public void commit() {
            boolean xaCommitFailed = false;
            initiateDurableCommit();
            if (getXATerminator() != null && xaResult == XA_OK) {
                try {
                    getXATerminator().commit(getCoordinationXid(), false);
                } catch (XAException ex) {
                    xaCommitFailed = true;
                    if (logger.isLogging(Level.SEVERE)) {
                        logger.severe("ATSubCoordinator.DurableParticipant.commit", "XATerminator.commit() threw exception "
                                + ex.getLocalizedMessage());
                    }
                }
            }
            // TODO send fault when failure occur in commit
            // waitForCommitOrRollbackResponse(Protocol.DURABLE);
            if (xaCommitFailed || isAborting()) {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("ATSubCoordinator.DurableParticipant.commit", "abort during commit processing. coordId="
                            + getIdValue());
                }
                // TODO: check WS-AT CV state table if should send aborted to root coordinator here.
                // rootVolatileParticipant.aborted();
            } else {
                if (logger.isLogging(Level.INFO)) {
                    logger.info("DurableParticipant.committed", "committed subordinate coordId=" + getIdValue());
                }
                rootDurableParticipant.committed();
            }
        }

        public void abort() {
            initiateDurableRollback();
            // waitForCommitOrRollbackResponse(Protocol.DURABLE);
            rootDurableParticipant.aborted();
        }
    }

    private XATerminator xaTerminator = null;

    private XATerminator getXATerminator() {
        if (xaTerminator == null) {
            xaTerminator = TransactionManagerImpl.getInstance().getXATerminator();
        }
        return xaTerminator;
    }

    // Synchronization

    /**
     * Subordinate Coordinator does not use this mechanism. Ensure it is not called.
     */
    public void beforeCompletion() {
        // Ensure that beforeCompletion disabled for subordinate coordinator
        // Subordinate coordinator should not be registered with TransactionSynchronizationRegistry.
        throw new UnsupportedOperationException("No beforeCompletion for subordinate coordinator");
    }

    public void afterCompletion(int i) {
        // Ensure that afterCompletion disabled for subordinate coordinator    
        //    waitForCommitOrRollbackResponse();
        throw new UnsupportedOperationException("No beforeCompletion for subordinate coordinator");
    }

    // XAResource  Not implemented yet

    /**
     * Synchronous prepare request from subordinate coordinator.
     * <p/>
     * <p>Prepare this coordinator and return result of preparation.
     */
    public int prepare(Xid xid) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_prepare(xid=" + xid + ")");
        }
        int result = XA_OK;
        // TODO: Prepare AT Subordinate Coordinator state so it can be recovered in case of failure.
        //       Only durable participants need to be recoverable.

        // This coordinator prepares when root ws-at coordinator sends a prepare to this subordinate
        // coordinator that is a participant to that coordinator.
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_prepare", result);
        }
        return result;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
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

    public void rollback(Xid xid) throws XAException {
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
    public void addRegistrant(Registrant registrant) {
        //
        if (registerWithRootRegistrationService(registrant)) {
            // registrant is either volatile or durable participant with subordinate coordinator's parent coordinator.
            // do not add this participant to ATSubCoordinator list of participants it is managing.
            return;
        }
        super.addRegistrant(registrant);
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
    public Registrant getRegistrant(String id) {
        Registrant result = super.getRegistrant(id);

        // check subordinate participants
        if (result == null && rootVolatileParticipant != null) {
            if (rootVolatileParticipant.getIdValue().equals(id)) {
                result = rootVolatileParticipant;
            }
        }
        if (result == null && rootDurableParticipant != null) {
            if (rootDurableParticipant.getIdValue().equals(id)) {
                result = rootDurableParticipant;
            }
        }

        return result;
    }

    /**
     * Represent all volatile and durable participants for this subordinate coordinator through
     * two special participants of SubordinateCoordinator. They are the only participants that parent
     * coordinator is aware of.
     */
    public boolean registerWithRootRegistrationService(Registrant r) {
        if (r == rootVolatileParticipant || r == rootDurableParticipant) {
            return true;
        } else {
            return super.registerWithRootRegistrationService(r);
        }
    }

    public void forget(String partId) {
        if (rootVolatileParticipant != null && rootVolatileParticipant.getIdValue().equals(partId)) {
            rootVolatileParticipant = null;
            if (logger.isLogging(Level.FINE)) {
                logger.fine("forget", "forgot volatile participant link to parent " + getCoordIdPartId(partId));
            }
            return;
        }
        if (rootDurableParticipant != null && rootDurableParticipant.getIdValue().equals(partId)) {
            rootDurableParticipant = null;
            if (logger.isLogging(Level.FINE)) {
                logger.fine("forget", "forgot durable participant link to parent " + getCoordIdPartId(partId));
            }
            return;
        }

        // see if just a regular participant of this coordinator
        super.forget(partId);
    }
}
