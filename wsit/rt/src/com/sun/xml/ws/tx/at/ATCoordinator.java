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

import com.sun.xml.ws.api.tx.Protocol;
import com.sun.xml.ws.api.tx.TXException;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import static com.sun.xml.ws.tx.at.ATParticipant.STATE.*;
import com.sun.xml.ws.tx.common.AT_2PC_State;
import static com.sun.xml.ws.tx.common.AT_2PC_State.ABORTING;
import static com.sun.xml.ws.tx.common.AT_2PC_State.ACTIVE;
import static com.sun.xml.ws.tx.common.AT_2PC_State.COMMITTING;
import static com.sun.xml.ws.tx.common.AT_2PC_State.PREPARED_SUCCESS;
import static com.sun.xml.ws.tx.common.AT_2PC_State.PREPARING;
import static com.sun.xml.ws.tx.common.Constants.*;
import com.sun.xml.ws.tx.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.common.Util;
import com.sun.xml.ws.tx.common.WsaHelper;
import com.sun.xml.ws.tx.common.TxFault;
import com.sun.xml.ws.tx.coordinator.CoordinationContextInterface;
import com.sun.xml.ws.tx.coordinator.Coordinator;
import com.sun.xml.ws.tx.coordinator.Registrant;
import com.sun.xml.ws.tx.webservice.member.at.WSATCoordinator;
import com.sun.xml.ws.tx.webservice.member.coord.CreateCoordinationContextType;
import java.util.Map;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Atomic Transaction Coordinator
 * <p/>
 * <p/>
 * Coordinator States: NONE, ACTIVE, Volatile2PCPrepare, Durable2PCPrepare, Committing, Aborting
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * Relationship between ATCoordinator and Java Transaction Manager.
 * ATCoordinator is registered as an XAResource with Java Transaction Manager.
 * This enables Java Transaction Manager to be root transaction manager that
 * for ATCoordinator durable 2pc participants.  ATCoordinator registers for
 * Transaction Synchronization if it has volatile participants. This enables
 * volatile participants to be prepared BEFORE durable 2pc participants are prepared.
 * <p/>
 * <p/>
 * <p/>
 * Coordination Context expires  specifies the period, measured from
 * the point in time at which the context was first created or received, after which a
 * transaction MAY be terminated solely due to its length of operation.
 * From that point forward, the coordinator MAY elect to unilaterally roll back the transaction,
 * so long as it has not made a commit decision.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @author Joe.Fialli@Sun.COM
 * @version $Revision: 1.10 $
 * @since 1.0
 */
public class ATCoordinator extends Coordinator implements Synchronization, XAResource {

    // TODO: workaround until jaxws-ri stateful webservice can compute this URI
    public static final URI localCoordinationProtocolServiceURI =
            Util.createURI(WSTX_WS_SCHEME, null, WSTX_WS_PORT, WSTX_WS_CONTEXT + "/wsat/coordinator");

    // TODO: short term solution so waitFor* do not hang.  Remove when implement transaction timeout.
    static private final int MAX_WAIT_ITERATION = 300;
    static private final long WAIT_SLEEP = 2000;

    static private TxLogger logger = TxLogger.getATLogger(ATCoordinator.class);

    enum ACTION { PREPARE, COMMIT, ROLLBACK };
    enum KIND { VOLATILE, DURABLE };
    
    /* map <Registrant.getId(), Registrant> of volatile 2pc participants */
    private final Map<String, ATParticipant> volatileParticipants = new HashMap<String, ATParticipant>();
    private AT_2PC_State volatileParticipantsState = ACTIVE;

    /* map <Registrant.getId(), Registrant> of durable 2pc participants */
    private final HashMap<String, ATParticipant> durableParticipants = new HashMap<String, ATParticipant>();
    private AT_2PC_State durableParticipantsState = ACTIVE;

    /* the completion registrant  - only allowed on root ATCoordinator
     */
    private ATCompletion completionRegistrant;


    /**
     * From WSAT 2004 S3.1.1,  new participants are not allowed once coordinator has responses from all volatile 2PC participants.
     */
    private boolean allowNewParticipants = true;

    // associated JTA transaction
    // since JTA provides no portable way to look up a transaction by id, cache Java Transaction with
    // ATCoordinator.
    protected Transaction transaction = null;

    /**
     * Construct a new Coordinator object from the specified context and soap request.
     * <p/>
     * This method is an entry point for the Activation service's createCoordinationContext
     * operation.  This entry point probably won't be used much, or not at all if we choose
     * not to publish the operation (which is optional in the WS-Coordination spec).
     *
     * @param context The coordination context
     * @param request The soap request
     */
    public ATCoordinator(CoordinationContextInterface context, CreateCoordinationContextType request) {
        super(context, request);
    }

    /**
     * Construct a new Coordinator object from the specified context.
     * <p/>
     * This constructor will be the main entry point for activity within the
     * AppServer.
     *
     * @param context The coordination context
     */
    public ATCoordinator(CoordinationContextInterface context) {
        super(context);
    }

    /**
     * Set once field.
     */
    public void setTransaction(final Transaction txn) {
        transaction = txn;
        if (txn == null) {
            return;
        }
        
//        if (!isSubordinateCoordinator()) {
            try {
                registerWithDurableParent();
            } catch (SystemException ex) {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("setTransaction", LocalizationMessages.XA_REGISTER_0004(ex.getLocalizedMessage()));
                    // TODO: link and rethrow
                }
            } catch (IllegalStateException ex) {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("setTransaction", LocalizationMessages.XA_REGISTER_0004(ex.getLocalizedMessage()));
                    // TODO: link and rethrow
                }
            } catch (RollbackException ex) {
                if (logger.isLogging(Level.SEVERE)) {
                    logger.severe("setTransaction", LocalizationMessages.XA_REGISTER_0004(ex.getLocalizedMessage()));
                    // TODO: link and rethrow
                }
            }
//        }
    }


    public Transaction getTransaction() {
        return transaction;
    }

    void setAborting() {
        volatileParticipantsState = ABORTING;
        durableParticipantsState = ABORTING;
    }

    boolean isAborting() {
        return volatileParticipantsState == ABORTING || durableParticipantsState == ABORTING;
    }

    /**
     * Get the list of {@link com.sun.xml.ws.tx.coordinator.Registrant}s for this coordinated activity.
     * <p/>
     * The returned list is unmodifiable (read-only).  Add new Registrants
     * with the {@link #addRegistrant(com.sun.xml.ws.tx.coordinator.Registrant, javax.xml.ws.WebServiceContext)} api instead.
     * <p/>
     *
     * @return the list of Registrant objects
     */
    public List<Registrant> getRegistrants() {
        final ArrayList<Registrant> list;
        if (completionRegistrant != null) {
            list = new ArrayList<Registrant>(volatileParticipants.size() + durableParticipants.size() + 1);
        } else {
            list = new ArrayList<Registrant>(volatileParticipants.size() + durableParticipants.size());
        }
        list.addAll(volatileParticipants.values());
        list.addAll(durableParticipants.values());
        if (completionRegistrant != null) {
            list.add(completionRegistrant);
        }
        return Collections.unmodifiableList(list);
    }

    protected void registerWithVolatileParent() {
        registerInterposedSynchronization();
    }

    /**
     * Enlist with parent of ATCoordinator which is JTA transaction manager.
     */
    protected boolean registerWithDurableParent() throws RollbackException, SystemException {
        boolean result = false;
        if (getTransaction() != null) {
            result = getTransaction().enlistResource(this);
        }
        return result;
    }

    /**
     * Add the specified Registrant to the list of registrants for this
     * coordinated activity.
     *
     * @param registrant The {@link Registrant}
     */
    @Override
    public void addRegistrant(final Registrant registrant, final WebServiceContext wsContext) {
        if (!allowNewParticipants) {
            // send fault S4.1 ws:coor Invalid State
            if(wsContext != null) {
                WsaHelper.sendFault(
                        wsContext,
                        SOAPVersion.SOAP_11,
                        TxFault.InvalidState,
                        "Invalid to register a new participant after the first durable participant is prepared.  Registrant id: " +
                                registrant.getIdValue());
            }
            throw new IllegalStateException(LocalizationMessages.LATE_PARTICIPANT_REGISTRATION_0002());
        }
        // TODO: check for duplicate registration and send fault S4.6 ws:coor Already Registered
        switch (registrant.getProtocol()) {
            case COMPLETION:
                // Unimplemented OPTIONAL functionality.
                // TODO: do we need to see if this field is already set?
                // TODO: disallow if subordinate coordinator
                completionRegistrant = (ATCompletion) registrant;
                break;

            case DURABLE:
                logger.fine("ATCoordinator.addRegistrant", getCoordIdPartId(registrant));
                durableParticipants.put(registrant.getIdValue(), (ATParticipant) registrant);
                break;

            case VOLATILE:
                volatileParticipants.put(registrant.getIdValue(), (ATParticipant) registrant);
                break;

            default:
                throw new UnsupportedOperationException(
                        LocalizationMessages.UNKNOWN_PROTOCOL_0003((registrant.getProtocol().getUri())));
        }
    }

    /**
     * Get the registrant with the specified id or null if it does not exist.
     *
     * @param id the registrant id
     * @return the Registrant object or null if the id does not exist
     */
    public Registrant getRegistrant(final String id) {
        Registrant r = volatileParticipants.get(id);

        if (r == null) {
            r = durableParticipants.get(id);
        }

        if ((r == null) && (completionRegistrant != null) && 
            (completionRegistrant.getId().getValue().equals(id))) {
            r = completionRegistrant;
        }

        return r;
    }
    
    public void removeRegistrant(final String id) {
        forget(id);
    }

    /**
     * Return a Collection of volatile 2pc participants.
     * <p/>
     * This Collection is modifiable.
     *
     * @return A modifiable Collection of the volatile 2pc participants
     */
    public Collection<ATParticipant> getVolatileParticipants() {
        return volatileParticipants.values();
    }

    public Collection<ATParticipant> getVolatileParticipantsSnapshot() {
        // Try an alternative to clone to get rid unchecked cast warning
        // (HashMap<String, ATParticipant>) volatileParticipants.clone();
        final HashMap<String, ATParticipant> vp =
                new HashMap<String, ATParticipant>(volatileParticipants);
        return vp.values();
    }

    /**
     * Return a Collection of durable 2pc participants.
     * <p/>
     * This Collection is modifiable.
     *
     * @return A modifiable Collection of the durable 2pc participants
     */
    public Collection<ATParticipant> getDurableParticipants() {
        return durableParticipants.values();
    }

    public Collection<ATParticipant> getDurableParticipantsSnapshot() {
        //return ((HashMap<String, ATParticipant>) durableParticipants.clone()).values();
        return new HashMap<String, ATParticipant>(durableParticipants).values();
    }


    /**
     * Get the completion registrant.
     *
     * @return The completion registrant
     */
    public ATCompletion getCompletionRegistrant() {
        return completionRegistrant;
    }

    /**
     * Send 2PC prepare to all volatile participants
     * <p/>
     * Volatile 2PC prepare constraint from 2004 WS-AT, section 3.3.1
     * the root coordinator begins the prepare phase of all participants registered for the Volatile 2PC protocol.
     * All participants registered for this protocol must respond before a Prepare is issued to a
     * participant registered for Durable 2PC. Further participants may register with the coordinator until the
     * coordinator issues a Prepare to any durable participant.
     */
    public void initiateVolatilePrepare() {
        // send prepare to all volatile participants before durable participants.
        // Section 3.3.1
        if (isAborting()) {
            initiateRollback();
            return;
        }
        volatileParticipantsState = PREPARING;
        actionForAllParticipants(getVolatileParticipantsSnapshot(), ACTION.PREPARE);
    }
    
    private void actionForAllParticipants(final Collection<ATParticipant> particpants, final ACTION action) {
        for (ATParticipant participant : particpants) {
            switch (action) {
                case PREPARE:
                    try {
                        participant.prepare();
                    } catch (TXException ex) {
                        setAborting();
                        return;
                    }
                    break;
                    
                case COMMIT:
                     try {
                        participant.commit();
                    } catch (TXException ex) {
                        setAborting();
                        return;
                    }
                    break;
                    
                case ROLLBACK:
                    participant.abort();
                    break;
                    
                default:
                    break;
            }
        }
    }

    /**
     * Wait for all volatile participants to respond to prepare.
     * <p/>
     * <p/>
     * Volatile participant state is set before this method returns.
     */
    protected void waitForVolatilePrepareResponse() {
        final String METHOD_NAME = "waitForVolatilePrepareResponse";
        final int numParticipants = getVolatileParticipants().size();
        if (volatileParticipantsState == PREPARED_SUCCESS || numParticipants == 0) {
            if (logger.isLogging(Level.FINER)) {
                logger.exiting(METHOD_NAME, "prepared coordId=" + getIdValue() + " state=" +
                        volatileParticipantsState + " numParticipants=" + numParticipants);
            }
            return;
        }

        boolean communicationTimeout = false;  // TODO: resend prepare due to communication timeout. Assume msg lost.
        boolean allPrepared;
        for (int i = 0; i < MAX_WAIT_ITERATION; i++) {
            allPrepared = true;  // assume all prepared until encounter participant is not
            final Iterator<ATParticipant> iter = getVolatileParticipantsSnapshot().iterator();
            while (iter.hasNext()) {
                final ATParticipant participant = iter.next();
                if (isAborting()) {
                    return;
                }
                switch (participant.getState()) {
                    case ACTIVE:
                        // accomodate late registration: volatile 2PC prepare can register new volatile or durable participant.
                        allPrepared = false;
                        try {
                            participant.prepare();
                        } catch (TXException ex) {
                                logger.warning(METHOD_NAME, LocalizationMessages.CAUGHT_TX_EX_DURING_PREPARE_0005(ex.getLocalizedMessage()));
                                setAborting();
                        }
                        break;

                    case PREPARING:
                        allPrepared = false;
                        if (communicationTimeout) {
                            try {
                                participant.prepare();
                            } catch (TXException ex) {
                                logger.warning(METHOD_NAME, LocalizationMessages.CAUGHT_TX_EX_DURING_PREPARE_0005(ex.getLocalizedMessage()));
                                setAborting();
                            }
                        }
                        break;

                    case ABORTING:
                        setAborting();
                        return;

                        // these states indicate a response to prepare request
                    case PREPARED:
                    case PREPARED_SUCCESS:
                    case COMMITTING:
                        break;

                    case NONE:
                    case COMMITTED:
                    case ABORTED:
                    case READONLY:
                        forget(participant);
                        break;
                }
            }
            if (isAborting()) {
                return;
            } else if (allPrepared) {
                volatileParticipantsState = PREPARED_SUCCESS;
                if (logger.isLogging(Level.FINER)) {
                    logger.exiting(METHOD_NAME, "prepared coordId=" + getIdValue() + " state=" +
                            volatileParticipantsState);
                }
                return;
            } else { //wait some before checking again
                try {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest(METHOD_NAME, "checking...");
                    }
                    Thread.sleep(WAIT_SLEEP);
                } catch (InterruptedException ex) {
                    logger.warning(METHOD_NAME, ex.getLocalizedMessage());
                }
            }
        }
        if (logger.isLogging(Level.FINE)) {
            dumpParticipantsState(getVolatileParticipantsSnapshot(), KIND.VOLATILE); 
        }
        setAborting();
        if (logger.isLogging(Level.FINER)) {
            logger.warning(METHOD_NAME, LocalizationMessages.TIMEOUT_0006(getIdValue(), volatileParticipantsState));
        }
    }


    /**
     * TODO: Each PREPARED/READONLY Volatile ATParticipant should check if it is time to start
     * the durable 2PC phase by calling this method.
     */
    public void initiateDurablePrepare() {
        final Collection<ATParticipant> ps = getDurableParticipants();
        final int numParticipants = ps == null ? 0 : ps.size();
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("initializeDurablePrepare", " coordId=" + getIdValue() +
                    " numDurableParticipants=" + numParticipants + " volatile participant state=" + volatileParticipantsState +
                    " numVolatileParticipants" + getVolatileParticipants().size());
        }

        // PRE-CONDITION: volatileParticipantState is PREPARED
        if (isAborting()) {
            initiateRollback();
            return;
        }

        assert volatileParticipantsState == PREPARED_SUCCESS || getVolatileParticipants().size() == 0;

        // No outstanding volatile participants at this point.

        // No new participants allowed as soon as durable 2PC begins.
        allowNewParticipants = false;
        durableParticipantsState = PREPARING;
        actionForAllParticipants(getDurableParticipantsSnapshot(), ACTION.PREPARE);
    }

    /**
     * Wait for all Durable participants to respond to prepare.
     * <p/>
     * <p/>
     * Durable participant state is set before this method returns.
     */
    protected void waitForDurablePrepareResponse() {
        // TODO: implement logic to resend prepare due to communication timeout.
        // Assumes prepare request was lost on way to participant OR the participant's response was lost/delayed.
        // boolean communicationTimeout = false;

        boolean allPrepared = false;
        for (int i = 0; i < MAX_WAIT_ITERATION; i++) {
            if (isAborting()) {
                break;
            }
            allPrepared = true;  // assume true until find at least one participant that is not prepared yet.
            for (ATParticipant participant : getDurableParticipantsSnapshot()) {
                switch (participant.getState()) {
                    case PREPARING:
                        allPrepared = false;
                        if (logger.isLogging(Level.FINEST)) {
                            logger.finest("intitatedurableParticipant", "not prepared, readonly or aborted " +
                                getCoordIdPartId(participant) + " state=" + participant.getState());
                        }
                        break;

                    case ACTIVE:
                    case NONE:
                        logger.warning("waitForDurablePrepareResponse",
                                LocalizationMessages.INITIATE_ROLLBACK_0007(this.getCoordIdPartId(participant), participant.getState()));
                        allPrepared = false;
                        setAborting();

                        // TODO: throw illegal state exception
                        assert false;

                    case ABORTING:
                        setAborting();
                        return;

                    // these states indicate a response to prepare request
                    case PREPARED:
                    case PREPARED_SUCCESS:
                    case COMMITTING:
                        break;

                    case ABORTED:
                        setAborting();
                        participant.forget();
                        break;
                        
                    case COMMITTED:
                    case READONLY:
                        participant.forget();
                        break;
                }
            }
            if (isAborting()) {
                return;
            } else if (allPrepared) {
                durableParticipantsState = PREPARED_SUCCESS;
                if (logger.isLogging(Level.FINER)) {
                    logger.exiting("waitForDurablePrepare", "coordId=" + getIdValue() +
                            "state:" + durableParticipantsState);
                }
                return;
            } else { //wait some before checking again
                try {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest("waitForDurablePrepare", "checking...");
                    }
                    Thread.sleep(WAIT_SLEEP);
                } catch (InterruptedException ex) {}
            }
        }
        if (logger.isLogging(Level.FINE)) {
            dumpParticipantsState(getDurableParticipantsSnapshot(), KIND.DURABLE); 
        }
        // some participants not prepared still, timing out
        setAborting();
        if (logger.isLogging(Level.WARNING)) {
            logger.warning("waitForDurablePrepare", LocalizationMessages.TIMEOUT_0006(getIdValue(), durableParticipantsState));
        }

    }
    
    private void dumpParticipantsState(final Collection<ATParticipant> lst, final KIND kind) {
        final StringBuffer str = new StringBuffer(100);
        str.append(" " + kind.toString() + " ");
        for (ATParticipant p: lst ) {
            str.append("Part: " + p.getIdValue() + " state:" + p.getState());
        }
        if (logger.isLogging(Level.FINE)) {
            logger.fine("dumpParticipantState", "coordId=" + getIdValue() + str);
        }
    }

    public void initiateCommit() {
        initiateVolatileCommit();
        initiateDurableCommit();
    }

    public void initiateDurableCommit() {
        // assert all participants must be in PREPARED, PREPARED_SUCCESS, COMMITTING, READONLY

        // PRE-CONDITION: durableParticipantState is PREPARED
        if (isAborting()) {
            initiateRollback();
            return;
        }
        if (durableParticipantsState != PREPARED_SUCCESS) {
                logger.warning("durableVolatileCommit", LocalizationMessages.UNEXPECTED_STATE_0008(durableParticipantsState));
        }
        durableParticipantsState = COMMITTING;
        actionForAllParticipants(getDurableParticipantsSnapshot(), ACTION.COMMIT);
    }

    public void initiateVolatileCommit() {
        // assert all participants must be in PREPARED, PREPARED_SUCCESS, COMMITTING, READONLY

        // PRE-CONDITION: durableParticipantState is PREPARED
        if (isAborting()) {
            initiateRollback();
            return;
        }
        if (volatileParticipantsState != PREPARED_SUCCESS && getVolatileParticipants().size() != 0) {
                logger.warning("initateVolatileCommit", LocalizationMessages.UNEXPECTED_STATE_0008(volatileParticipantsState));
        }

        // No new participants allowed as soon as durable 2PC begins.
        volatileParticipantsState = COMMITTING;
        actionForAllParticipants(getVolatileParticipantsSnapshot(), ACTION.COMMIT);
    }

    public void initiateRollback() {
        initiateVolatileRollback();
        initiateDurableRollback();
    }

    public void initiateDurableRollback() {
        durableParticipantsState = ABORTING;
        for (ATParticipant durableP : getDurableParticipantsSnapshot()) {
            durableP.abort();
        }
    }

    public void initiateVolatileRollback() {
        volatileParticipantsState = ABORTING;
        for (ATParticipant volatileP : getVolatileParticipantsSnapshot()) {
            volatileP.abort();
        }
    }

    /**
     * Register this with TransactionSynchronizationRegistery.  This should get called by JTS
     * transaction system before 2PC Participants and XAResources are prepared.
     */
    public void beforeCompletion() {
        initiateVolatilePrepare();
        waitForVolatilePrepareResponse();
    }

    public void afterCompletion(final int i) {
        waitForCommitOrRollbackResponse(Protocol.DURABLE);
        waitForCommitOrRollbackResponse(Protocol.VOLATILE);
        forget();
    }

    protected void waitForCommitOrRollbackResponse(final Protocol protocol) {
        // all participants have been committed or rolled back.
        // wait for all outstanding participants to send final notification of wsat COMMITTED or ABORTED.
        // boolean communicationTimeout = false;  // TODO: resend prepare due to communication timeout. Assume msg lost.
        boolean allProcessed;
        
        
        for (int i = 0; i < MAX_WAIT_ITERATION; i++) {
            allProcessed = true;  // assume all committed/aborted until encounter participant is not.
            if (protocol == Protocol.DURABLE) {
                
                for (ATParticipant participant : getDurableParticipantsSnapshot()) {
                    if (participant.getState() == COMMITTED ||
                            participant.getState() == ABORTED ||
                            participant.getState() == READONLY) {
                        participant.forget();
                    } else {
                        allProcessed = false;
                        if (logger.isLogging(Level.FINEST)) {
                            logger.finest("waitForCommitRollback", getCoordIdPartId(participant) + " state:" + participant.getState());
                        }
                        /*  Don't retry aggressively. Have to put communication timeout to retry.
                       if (isAborting()){
                           participant.abort();
                       } else {
                           try {
                               participant.commit();
                           } catch (TXException ex) {
                                logger.warning("waitForCommitOrRollbackResponse", ex.getLocalizedMessage());
                           }
                       }
                         **/
                    }
                }
            } else if (protocol == Protocol.VOLATILE) {
                // best effort to receive committed/aborted from volatile participants. But do not wait for them to send committed.
                allProcessed = true;  // assume all committed/aborted until encounter participant is not.
                for (ATParticipant participant : getVolatileParticipantsSnapshot()) {
                    if (participant.getState() != COMMITTED &&
                            participant.getState() != ABORTED ||
                            participant.getState() != READONLY) {
                        if (logger.isLogging(Level.WARNING)) {
                            logger.warning("waitForCommitOrRollbackResponse",
                                    LocalizationMessages.FORGETTING_0009(participant.getState(), getCoordIdPartId(participant)));
                        }
                        participant.forget();
                    }
                }
            }
            if (allProcessed) {
                if (logger.isLogging(Level.FINER)) {
                    logger.exiting("waitForCommitRollback", "coordId=" + getIdValue());
                }
                return;
            } else { //wait some before checking again
                try {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest("waitForCommitRollback", "checking...");
                    }
                    Thread.sleep(WAIT_SLEEP);
                } catch (InterruptedException ex) { }
            }
        }
    }

    /**
     * Synchronous prepare request invoked by JTS coordinator as part of its 2PC protocol.
     * <p/>
     * <p>Prepare this coordinator and return result of preparation.
     */
    public int prepare(final Xid xid) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_prepare(xid=" + xid + ")");
        }
        int result = 0;

        initiateDurablePrepare();

        // Map asynchonous WS-AT 2PC protocol to XAResource synchronous protocol.
        // Wait for all possible pending responses to prepare message.
        waitForDurablePrepareResponse();  // result in durableParticipantsState: PREPARED, COMMITTED, ABORTING

        // check if volatile or durable WS-AT participants aborted
        if (isAborting()) {
            // TODO:  be more specific on XAException error code for why rollback occurred. Using generic code now.
            throw new XAException(XAException.XA_RBROLLBACK);
        } else if (getDurableParticipants().size() == 0 && this.getVolatileParticipants().size() == 0) {
            result = XAResource.XA_RDONLY;
        } else {
            result = XAResource.XA_OK;
        }

        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_prepare", result);
        }
        return result;
    }

    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_commit(xid=" + xid + " ,onePhase=" + onePhase + ")");
        }
   
        int result = 0;
        if (onePhase) {
            // if one phase commit, need to do prepare here
            try {
                result = prepare(xid);
            } catch (XAException e) {
                if (logger.isLogging(Level.WARNING)) {
                    logger.warning("commit(1PC)", LocalizationMessages.PREPARE_FAILED_0010(e.toString()));
                }
                initiateRollback();
                waitForCommitOrRollbackResponse(Protocol.DURABLE);
                if (logger.isLogging(Level.FINER)) {
                    logger.exiting("XAResource_commit", e);
                }
                throw e;
            }   
        }
        
        // Commit volatile and durable 2PC participants.  No ordering required.
        if (result != XAResource.XA_RDONLY) {
            initiateCommit();
            waitForCommitOrRollbackResponse(Protocol.DURABLE);
            waitForCommitOrRollbackResponse(Protocol.VOLATILE);
        }
       
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_commit");
        }   
    }
    
    public void rollback(final Xid xid) throws XAException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("XAResource_rollback(xid=" + xid + ")");
        }
        // Commit volatile and durable 2PC participants.  No ordering required.
        initiateRollback();
        waitForCommitOrRollbackResponse(Protocol.DURABLE);
        waitForCommitOrRollbackResponse(Protocol.VOLATILE);
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("XAResource_rollback");
        }
    }

    public Xid[] recover(final int i) throws XAException {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public boolean setTransactionTimeout(final int i) throws XAException {
        setExpires(i * 1000L);
        return true;
    }

    public void start(final Xid xid, final int flags) throws XAException {
        // Start transaction hook
    }

    /**
     */
    public void end(final Xid xid, final int flags) throws XAException {
        switch (flags) {
            case TMSUCCESS:
                //TODO
                break;
            case TMSUSPEND:
                // TODO
                break;
            case TMFAIL:
                setAborting();
                break;
        }
    }

    /**
     * forget everything about this transaction.
     * <p/>
     * <p/>
     * Recovers resources held by a transaction.  After a transaction is committed or aborted, it is forgotten.
     */
    public void forget(final Xid xid) throws XAException {
        // TODO release resources held for xid.
        // Comment out exception, no need to fail when forget is called.
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    public int getTransactionTimeout() throws XAException {
        return (int) (getExpires() / 1000L);
    }

    public boolean isSameRM(final XAResource xAResource) throws XAException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void prepared(final String participantId) {
        prepared(participantId, null);
    }

    public void prepared(final String participantId, final EndpointReference unknownParticipantReplyEPR) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("prepared", getCoordIdPartId(participantId));
        }
        final ATParticipant participant = (ATParticipant) getRegistrant(participantId);
        if (participant == null) {
            if (unknownParticipantReplyEPR != null) {
                logger.warning("prepared", LocalizationMessages.UNKNOWN_CORD_OR_PART_0011(getCoordIdPartId(participantId), unknownParticipantReplyEPR));
                
                ATParticipant.getATParticipantWS(unknownParticipantReplyEPR, null, false).rollbackOperation(null);
            }
        } else {
            participant.prepared();
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("prepared", getCoordIdPartId(participantId));
        }
    }

    public void committed(final String participantId) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("committed", getCoordIdPartId(participantId));
        }
        final ATParticipant participant = (ATParticipant) getRegistrant(participantId);
        if (participant != null) {
            participant.committed();
            participant.forget();
        } else {
            logger.warning("committed", LocalizationMessages.UNKNOWN_PART_0012(getCoordIdPartId(participantId)));
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("committed", getCoordIdPartId(participantId));
        }
    }

    public void readonly(final String participantId) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("readonly", getCoordIdPartId(participantId));
        }
        final ATParticipant participant = (ATParticipant) getRegistrant(participantId);
        if (participant == null) {
            logger.warning("readonly", LocalizationMessages.UNKNOWN_PART_0012(getCoordIdPartId(participantId)));
        } else {
            participant.readonly();
            participant.forget();
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("readonly", getCoordIdPartId(participantId));
            }
        }
    }

    public void aborted(final String participantId) {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("aborted", getCoordIdPartId(participantId));
        }
        setAborting();
        final ATParticipant participant = (ATParticipant) getRegistrant(participantId);
        if (participant == null) {
            if (logger.isLogging(Level.WARNING)) {
                logger.warning("aborted", LocalizationMessages.UNKNOWN_PART_0012(getCoordIdPartId(participantId)));
            }
        } else {
            participant.aborted();
            participant.forget();
            if (logger.isLogging(Level.FINER)) {
                logger.exiting("aborted", getCoordIdPartId(participantId));
            }
        }
    }

    /**
     * Implement inbound event <i>replay</i> for Atomic Transaction 2PC Protocol(Coordinator View).
     */
    public void replay(final String participantId) {
        final String METHOD_NAME="replay";
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId(participantId));
        }

        final ATParticipant participant = (ATParticipant) getRegistrant(participantId);
        final ATParticipant.STATE state = participant.getState();
        switch (state) {
            case NONE:
                if (participant.isDurable()) {
                    participant.abort();
                } else {  // participant.isVolatile()
                    // TODO: Invalid State. Send back an invalid state fault.
                    if (logger.isLogging(Level.SEVERE)) {
                        logger.severe(METHOD_NAME, LocalizationMessages.INVALID_STATE_0013(getCoordIdPartId(participantId)));
                    }
                }
                break;
                
            case ACTIVE:
            case PREPARING:
            case ABORTING:
                participant.abort();
                break;
                
            case COMMITTING:
                try {
                    participant.commit();
                } catch (TXException ex) {
                    if (logger.isLogging(Level.WARNING)) {
                        logger.warning(METHOD_NAME, ex.getLocalizedMessage());
                    }
                }
                break;
            case PREPARED:
            case PREPARED_SUCCESS:
                // nothing to do for all other cases.
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, getCoordIdPartId(participantId));
        }
    }

    volatile private boolean registeredInterposedSynchronization = false;

    /**
     * Register interposed synchronization for this instance.
     * <p/>
     * Initial volatile participant registration triggers this registration.
     */
    private void registerInterposedSynchronization() {
        if (!registeredInterposedSynchronization) {
            registeredInterposedSynchronization = true;
            TransactionManagerImpl.getInstance().registerInterposedSynchronization(this);
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("registerInterposedSynchronization", "for WS-AT coordinated activity " + this.getIdValue());
            }
        }
    }

    public boolean isSubordinateCoordinator() {
        return false;
    }

    public EndpointReference getParentCoordinatorRegistrationEPR() {
        if (getContext() == null) {
            return null;
        } else {
            return getContext().getRootRegistrationService();
        }
    }

    static private WSATCoordinator wsatCoordinatorService = new WSATCoordinator();

    public static WSATCoordinator getWSATCoordinatorService() {
        return wsatCoordinatorService;
    }

    protected String getCoordIdPartId(final Registrant registrant) {
        return getCoordIdPartId(registrant.getIdValue());
    }

    protected String getCoordIdPartId(final String participantId) {
        return " coordId=" + getIdValue() + " partId:" + participantId + " ";
    }

    public void forget(final ATParticipant part) {
        forget(part.getIdValue());
    }

    public void forget(final String partId) {
        ATParticipant removed = volatileParticipants.remove(partId);
        if (removed != null) {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("forget", "forgot volatile participant " + getCoordIdPartId(partId));
            }
            return;
        }
        removed = durableParticipants.remove(partId);
        if (removed != null) {
            if (logger.isLogging(Level.FINE)) {
                logger.fine("forget", "forgot durable participant " + getCoordIdPartId(partId));
            }
            return;
        }
        /*
         * TODO: implement if optional completion is ever supported.
        if ((completionRegistrant != null) && (completionRegistrant.getId().equals(id))) {
            r = completionRegistrant;
        }
        */
    }

    static EndpointReference localCoordinatorProtocolService;
    
    static {
         MemberSubmissionEndpointReference epr = new MemberSubmissionEndpointReference();
         epr.addr = new MemberSubmissionEndpointReference.Address();
         epr.addr.uri = localCoordinationProtocolServiceURI.toString();
         localCoordinatorProtocolService = epr;
    }

    public EndpointReference getCoordinatorProtocolServiceForRegistrant(final Registrant r) {
        return localCoordinatorProtocolService;
    }

    /**
     * Return false if it is okay to rollback the transaction.
     * Do not allow transaction expiration after Phase 2 begins.
     */
    public boolean expirationGuard() {
        for (ATParticipant participant : getDurableParticipantsSnapshot()) {
            if (isSecondPhase(participant)) {
                return true;
            }
        }
        for (ATParticipant participant : getVolatileParticipantsSnapshot()) {
            if (isSecondPhase(participant)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSecondPhase(final ATParticipant p) {
        switch (p.state) {
            case ABORTING:
            case COMMITTING:
            case ABORTED:
            case COMMITTED:
                // too late to abort 2PC, already committed or rolled back transaction.
                return true;
            default:
                return false;
        }
    }

    @Override
    public void forget() {
        for (ATParticipant participant : getDurableParticipantsSnapshot()) {
            participant.forget();
        }
        for (ATParticipant participant : getVolatileParticipantsSnapshot()) {
            participant.forget();
        }
        super.forget();
    }
}    
