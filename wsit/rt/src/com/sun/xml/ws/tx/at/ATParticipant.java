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

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.OneWayFeature;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.tx.Participant;
import com.sun.xml.ws.api.tx.Protocol;
import com.sun.xml.ws.api.tx.TXException;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.tx.common.AddressManager;
import static com.sun.xml.ws.tx.common.Constants.WSAT_SOAP_NSURI;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactory;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactoryFactory;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.coordinator.Coordinator;
import com.sun.xml.ws.tx.coordinator.Registrant;
import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.at.ParticipantPortType;
import com.sun.xml.ws.tx.webservice.member.at.ParticipantPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.coord.RegisterType;

import javax.transaction.xa.Xid;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import java.net.URI;
import java.util.logging.Level;

/**
 * This class encapsulates a WS-AT participant.
 * <p/>
 * <p> A participant represents one of the three ws-at protocols:
 * completion, volatile 2PC or durable 2PC.
 * <p/>
 * <p> Participant lifecycle consist of generating a endpoint reference
 * <p/>
 * <p/>
 * Transaction timeout from Participants perspective.
 * Coordination Context expires  specifies the period, measured from
 * the point in time at which the context was first created or received, after which a
 * transaction MAY be terminated solely due to its length of operation.
 * A 2PC participant MAY elect to abort its work in the transaction so long as it has not
 * already decided to prepare.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.18.22.2 $
 * @since 1.0
 */
public class ATParticipant extends Registrant {

    static public enum STATE {
        NONE, ACTIVE, PREPARING, PREPARED, PREPARED_SUCCESS, COMMITTING, ABORTING, COMMITTED, ABORTED, READONLY
    }

    /* PPS */
    public  static final URI LOCAL_PPS_URI =
            AddressManager.getPreferredAddress(ParticipantPortType.class);

    protected STATE state = STATE.NONE;
    protected Xid xid;

    // Equivalent to an XAResource for WSAT
    private Participant participant = null;
    final private boolean remoteParticipant;

    static private TxLogger logger = TxLogger.getATLogger(ATParticipant.class);
    static final String WSAT_COORDINATOR = "WSATCoordinator";
    
    private EndpointReference exportCoordinatorProtocolServiceForATParticipant(final Coordinator coord) {
        return StatefulWebserviceFactoryFactory.getInstance().createService(WSAT_COORDINATOR, "Coordinator",
                ATCoordinator.localCoordinationProtocolServiceURI, AddressingVersion.MEMBER,
                coord.getIdValue(), this.getIdValue(), coord.getExpires());
    }

    /**
     * Register will figure out if participant will register with local or remote Coordination Protocol Service.
     */
    public ATParticipant(Coordinator parent, Participant participant) {
        super(parent, participant.getProtocol());
        this.participant = participant;
        this.remoteParticipant = false;

        if (logger.isLogging(Level.FINEST)) {
            logger.finest("ATParticipant", getCoordIdPartId());
        }
        // TODO: implement participant timeout from parent.getExpires().
    }

    /**
     * Remote ATParticipant with a local Coordinator.
     * ParticipantProtocolService received as part of registerRequest.
     */
    public ATParticipant(Coordinator parent, RegisterType registerRequest) {
        super(parent, registerRequest);
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("Remote ATParticipant", getCoordIdPartId());
        }
        participant = null;
        setCoordinatorProtocolService(exportCoordinatorProtocolServiceForATParticipant(parent));
        remoteParticipant = true;
        if (logger.isLogging(Level.FINEST)) {
            logger.finest("Remote ATParticipant:", getCoordIdPartId() + " CPS:" + getCoordinatorProtocolService());
        }
    }


    public ParticipantPortType getParticipantPort(final EndpointReference epr) {
        return ATCoordinator.getWSATCoordinatorService().getParticipant(epr);
    }

    public boolean isVolatile() {
        return getProtocol() == Protocol.VOLATILE;
    }


    public boolean isDurable() {
        return getProtocol() == Protocol.DURABLE;
    }

    /**
     * A participant is forgotten after it has sent committed or aborted to coordinator.
     */
    public void forget() {
        if (isRemoteCPS() && localParticipantProtocolService != null) {
            final ParticipantPortTypeImpl ppti = 
                    ParticipantPortTypeImpl.getManager().resolve(localParticipantProtocolService);
            
            // could resolve to null if stateful webservice timeout already unexported automatically.
            if (ppti != null) {
                ParticipantPortTypeImpl.getManager().unexport(ppti);
            } 
        }
        localParticipantProtocolService = null;
        if (remoteParticipant) {
            final CoordinatorPortTypeImpl cpti = CoordinatorPortTypeImpl.getManager().resolve(getCoordinatorProtocolService());
            if (cpti != null) {
                CoordinatorPortTypeImpl.getManager().unexport(cpti);
            }
        }
        
        getATCoordinator().forget(this);
    }

    private CoordinatorPortType getATCoordinatorWS(final boolean nonterminalNotify) {
        if (getCoordinatorProtocolService() == null && !isRegistrationCompleted()) {
            logger.warning("getATCoordinatorWS",
                    LocalizationMessages.NO_REG_RESP_0014(
                            getATCoordinator().getContext().getRootRegistrationService().toString(),
                            getCoordIdPartId()));
        }

        return getATCoordinatorWS(getCoordinatorProtocolService(),
                getParticipantProtocolService(), nonterminalNotify);
    }

    public static CoordinatorPortType getATCoordinatorWS(final EndpointReference toCPS, 
                                                         final EndpointReference replyToPPS,
                                                         final boolean nonterminalNotify) {
        final OneWayFeature owf = new OneWayFeature();
        WSEndpointReference wsepr = null;
        if (nonterminalNotify && replyToPPS != null) {
            try {
                wsepr = new WSEndpointReference(replyToPPS);
            } catch (Exception xse) {
                logger.severe("getATCoordinatorWS", LocalizationMessages.REPLYTOPPS_EPR_EXCEPTION_0015(replyToPPS.toString(), xse.getLocalizedMessage()));
            }
            if (wsepr != null) {
                owf.setReplyTo(wsepr);
            } else {
                logger.warning("getATCoordinatorWS", LocalizationMessages.NULL_PPS_EPR_WARNING_0016());
            }
        }
        assert toCPS != null;
        return ATCoordinator.getWSATCoordinatorService().getCoordinator(toCPS, owf);
    }

    private ParticipantPortType getATParticipantWS(final boolean nonterminalNotification) {
        return this.getATParticipantWS(this.getParticipantProtocolService(),
                this.getCoordinatorProtocolService(), nonterminalNotification);
    }


    public static ParticipantPortType getATParticipantWS(final EndpointReference toPPS, 
                                                         final EndpointReference replyToCPS,
                                                         final boolean nonterminalNotification) {
        final OneWayFeature owf = new OneWayFeature();
        WSEndpointReference wsepr = null;
        if (nonterminalNotification && replyToCPS != null) {
            try {
                wsepr = new WSEndpointReference(replyToCPS);
            } catch (Exception xse) {
                logger.severe("getATCoordinatorWS", LocalizationMessages.REPLYTOPPS_EPR_EXCEPTION_0015(replyToCPS.toString(), xse.getLocalizedMessage()));
            }
            if (wsepr != null) {
                owf.setReplyTo(wsepr);
            } else {
                logger.warning("getATParticipantWS", LocalizationMessages.NULL_CPS_EPR_WARNING_0018());
            }
            
        }
        assert toPPS != null;
        return ATCoordinator.getWSATCoordinatorService().getParticipant(toPPS, owf);
    }

    public ATCoordinator getATCoordinator() {
        return (ATCoordinator) getCoordinator();
    }


    /**
     * Return participant's state for Atomic Transaction 2PC Protocol.
     */
    public ATParticipant.STATE getState() {
        return state;
    }

    protected Xid getXid() {
        return xid;
    }

    /**
     * Returns participant state. or (something for abort).
     */
    public void prepare() throws TXException {
        final String METHOD_NAME = "prepare";
        
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, "coordId=" + getCoordinator().getIdValue() + " partId=" + getIdValue());
        }
        switch (getState()) {
            case NONE:
            case ABORTING:
                abort();
                throw new TXException("Rollback");

            case ACTIVE:
                internalPrepare();
                break;

            case PREPARED_SUCCESS:
                // just resend
                if (isRemoteCPS()) {
                    try {
                        getATCoordinatorWS(true).preparedOperation(null);
                    } catch (WebServiceException wse) {
                        logger.warning(METHOD_NAME, LocalizationMessages.PREPARE_FAILED_0010(wse.getLocalizedMessage()));
                       
                        throw wse;
                    } catch (Exception e) {
                        logger.severe(METHOD_NAME, LocalizationMessages.PREPARE_FAILED_0010(e.getLocalizedMessage()));
                    }
                } else {
                    getATCoordinator().prepared(getIdValue());
                }
                break;
                
            case PREPARING:
            case PREPARED:
            case COMMITTING:
                // ignore PREPARE in these states
                break;
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting(METHOD_NAME, "coordId=" + getCoordinator().getIdValue() + " partId=" + getIdValue());
        }
    }

    private void internalPrepare() throws TXException {
        if (remoteParticipant) {
            remotePrepare();
        } else {
            localPrepare();
        }
    }

    private void remotePrepare() {
        state = STATE.PREPARING;
        // TODO: resend if don't receive prepared notfication from coordinator in some communication timeout amount of time
        try {
            getATParticipantWS(true).prepareOperation(null);
        } catch (WebServiceException wse) {
            logger.warning("remotePrepare", LocalizationMessages.PREPARE_FAILED_0010(wse.getLocalizedMessage()));
            throw wse;
        } catch (Exception e) {
            logger.severe("remotePrepare", LocalizationMessages.PREPARE_FAILED_0010(e.getLocalizedMessage()));
        }
    }

    private void localPrepare() throws TXException {
        final String METHOD_NAME = "localPrepare";
        if (logger.isLogging(Level.FINER)) {
            logger.entering(METHOD_NAME, getCoordIdPartId());
        }
        Participant.STATE result = null;
        state = STATE.PREPARING;
        try {
            result = participant.prepare();
        } catch (TXException e) {
            // failure during prepare, just abort

            // set participant to null. don't want to call its abort(), it already knows its aborted
            participant = null;
            abort();
            throw new TXException("Rollback");
        } catch (Exception e) {
            participant = null;
            abort();
            throw new TXException("Rollback");
        }
        switch (result) {
            case P_OK:
                state = STATE.PREPARED;
                if (isRemoteCPS()) {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest(METHOD_NAME, "send prepared to remote coordinator"
                                + getIdValue());
                    }
                    try {
                        getATCoordinatorWS(true).preparedOperation(null);
                    } catch (WebServiceException wse) {
                        logger.warning(METHOD_NAME, LocalizationMessages.PREPARE_FAILED_0010(wse.getLocalizedMessage()));
                        throw wse;
                    }
                } else {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest(METHOD_NAME, "send prepared to local coordinator"
                                + getIdValue());
                    }
                    getATCoordinator().prepared(this.getIdValue());
                }
                state = STATE.PREPARED_SUCCESS;
                break;

            case P_READONLY:
                state = STATE.READONLY;
                if (isRemoteCPS()) {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest(METHOD_NAME, "send readonly to remote coordinator for participant id"
                                + getIdValue());
                    }
                    try {
                        getATCoordinatorWS(false).readOnlyOperation(null);
                    } catch (WebServiceException wse) {
                         logger.warning(METHOD_NAME, "readonly to web service failed. "
                                        + wse.getLocalizedMessage());
                         throw wse;
                    }
                } else {
                    if (logger.isLogging(Level.FINEST)) {
                        logger.finest(METHOD_NAME, "send readonly to remote coordinator for participant id" +
                                getIdValue());
                    }
                    getATCoordinator().readonly(getIdValue());
                }
                if (logger.isLogging(Level.FINE)) {
                    logger.fine(METHOD_NAME, "readonly " + getCoordIdPartId());
                }
                forget();
                break;
        }
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("ATParticipant.localPrepare");
        }
    }

    /**
     * Send Terminal notification
     */
    private void remoteCommit() {
        // TODO: resend if don't receive committed notification from coordinator in some communication timeout amount of time

        if (logger.isLogging(Level.FINER)) {
            logger.entering("remoteCommit()", getIdValue());
        }
        this.getATParticipantWS(true).commitOperation(null);
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("remoteCommit");
        }
    }

    public void commit() throws TXException {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("commit" + getCoordIdPartId());
        }
        if (remoteParticipant) {
            remoteCommit();
        } else {
            localCommit();
        }
        if (logger.isLogging(Level.FINER)) {
            logger.entering("commit" + getCoordIdPartId());
        }
    }

    private void localCommit() {
        final String METHOD_NAME = "localCommit";
        switch (getState()) {
            case NONE:

                // TODO send committed using wsa:replyTo EPR.
                // this case
                break;
            case ABORTING:
                logger.warning(METHOD_NAME, LocalizationMessages.INCONSISTENT_STATE_0020(getState(), getCoordIdPartId())); 
                //fault wsat:InconsistentInternalState
                abort();

                break;
            case ACTIVE:
            case PREPARING:
            case PREPARED:
                logger.warning(METHOD_NAME, LocalizationMessages.INCONSISTENT_STATE_0020(getState(), getCoordIdPartId()));
                // TODO throw fault coor:InvalidState
                abort();
                break;

            case PREPARED_SUCCESS:
                state = STATE.COMMITTING;
                participant.commit();
                participant = null;   // no longer need to contact participant.
                if (logger.isLogging(Level.FINE)) {
                    logger.fine(METHOD_NAME, "committed " + getCoordIdPartId());
                }
                if (isRemoteCPS()) {
                    try {
                        getATCoordinatorWS(false).committedOperation(null);
                    } catch (WebServiceException wse) {
                        logger.warning(METHOD_NAME, LocalizationMessages.COMMITTED_FAILED_0021(wse.getLocalizedMessage()));
                        throw wse;
                    }
                } else {
                    getATCoordinator().committed(getIdValue());
                }
                forget();
                break;

            case COMMITTING:
                if (isRemoteCPS()) {
                    getATCoordinatorWS(false).committedOperation(null);
                } else {
                    getATCoordinator().committed(getIdValue());
                }
                forget();

                break;
        }
    }

    public void abort() {
        if (logger.isLogging(Level.FINER)) {
            logger.entering("abort", getCoordIdPartId());
        }

        //TODO. put switch statement over all possible 2pc transaction state.
        //      invalid states require fault to be sent

        state = STATE.ABORTING;

        // local rollback
        if (participant != null) {
            participant.abort();
            participant = null;   // no need to contact participant anymore
        }
        // pass rollback to remote participant
        if (remoteParticipant) {
            remoteRollback();
        } else {
            localRollback();
        }

        if (logger.isLogging(Level.FINER)) {
            logger.exiting("abort", getCoordIdPartId());
        }
    }

    private void localRollback() {
        if (isRemoteCPS()) {
            try {
                getATCoordinatorWS(false).abortedOperation(null);
            } catch (WebServiceException wse) {
                logger.warning("localRollback", LocalizationMessages.PREPARED_FAILED_0019(wse.getLocalizedMessage()));
                throw wse;
            }
        } else {
            getATCoordinator().aborted(getIdValue());
        }

        if (logger.isLogging(Level.FINE)) {
            logger.fine("abort", getCoordIdPartId());
        }
        forget();
    }


    /**
     * Send terminal notification
     */
    private void remoteRollback() {
        // TODO: resend if don't receive aborted notification from coordinator in some communication timeout amount of time

        if (logger.isLogging(Level.FINER)) {
            logger.entering("remoteRollack", getCoordIdPartId());
        }
        getATParticipantWS(true).rollbackOperation(null);
        if (logger.isLogging(Level.FINER)) {
            logger.exiting("remoteRollback", getCoordIdPartId());
        }
    }

    public void setCoordinatorProtocolService(final EndpointReference cps) {
        super.setCoordinatorProtocolService(cps);

        if (cps != null) {
            // wscoor:registerResponse successfully communicated CPS, change participant's state
            state = STATE.ACTIVE;
        }
    }

    void prepared() {
        // TODO:  given current state, check if it is valid to set to this state.
        state = STATE.PREPARED_SUCCESS;
         if (logger.isLogging(Level.FINE)) {
            logger.fine("prepared", this.getCoordIdPartId() + " STATE=" + state.toString());
        }
    }

    void committed() {
        // TODO: verify state transition does not need to throw invalid state fault.
        state = STATE.COMMITTED;
        if (logger.isLogging(Level.FINE)) {
            logger.fine("committed", this.getCoordIdPartId() + " STATE=" + state.toString());
        }
    }

    void readonly() {
        // TODO: verify state transition does not need to throw invalid state fault.
        state = STATE.READONLY;
        if (logger.isLogging(Level.FINE)) {
            logger.fine("readonly", this.getCoordIdPartId() + " STATE=" + state.toString());
        }
    }

    void aborted() {
        // TODO: verify state transition does not need to throw invalid state fault.
        state = STATE.ABORTED;
        if (logger.isLogging(Level.FINE)) {
            logger.fine("aborted", this.getCoordIdPartId() + " STATE=" + state.toString());
        }
    }

    /**
     * This fault is sent by a participant to indicate that it cannot fulfill its obligations.
     * This indicates a global consistency failure and is an unrecoverable condition.
     *
     * @param soapVersion SOAP verion for returned fault.
     */
    /*
    private Packet newInconsistentInternalStateFault(final SOAPVersion soapVersion, final String detail) {
        Packet faultResponsePacket = null;
        // wsa:Action Constants.WSAT_FAULT_ACTION_URI
        // [Code] Sender
        // [Subcode] wsat:InconsistentInternalState
        // [Reason] A global consistency failure has occurred. This is an unrecoverable condition.
        // [Detail] detail
        throw new UnsupportedOperationException("Not implemented yet");
    }
    */

    /**
     * @see com.sun.xml.ws.tx.common.WsaHelper
     * @deprecated since now
     */
    SOAPFault createSOAPFault(final String message) {
        try {
            final SOAPFault fault = SOAPVersion.SOAP_11.saajSoapFactory.createFault();
            fault.setFaultString(message);
            // TODO: fix deprecated constant reference
            // fault.setFaultCode(JAXWSAConstants.SOAP11_SENDER_QNAME);
            fault.appendFaultSubcode(new QName(WSAT_SOAP_NSURI, "InconsistentInternalState"));
            fault.setFaultRole("A global consistent failure has occurred. This is an unrecoverable condition.");
            return fault;
        } catch (SOAPException ex) {
            throw new WebServiceException(ex);
        }
    }

    private String getCoordIdPartId() {
        return " coordId=" + getCoordinator().getIdValue() + " partId=" + getIdValue() + " ";
    }

    private EndpointReference localParticipantProtocolService = null;

    /**
     * No need to export an external stateful web service for this usage case.
     */
    public EndpointReference getLocalParticipantProtocolService() {
        if (localParticipantProtocolService == null) {
            if (isRemoteCPS()) {
                final StatefulWebserviceFactory swf = StatefulWebserviceFactoryFactory.getInstance();
                localParticipantProtocolService =
                        swf.createService(WSAT_COORDINATOR, "Participant",
                                LOCAL_PPS_URI, AddressingVersion.MEMBER,
                                getATCoordinator().getIdValue(), this.getId().getValue(), getATCoordinator().getExpires());
            } else {
                localParticipantProtocolService = getLocalParticipantProtocolServiceEPR();
            }
        }
        return localParticipantProtocolService;
    }
    
    static public EndpointReference getLocalParticipantProtocolServiceEPR() {
         final MemberSubmissionEndpointReference epr = new MemberSubmissionEndpointReference();
         epr.addr = new MemberSubmissionEndpointReference.Address();
         epr.addr.uri = LOCAL_PPS_URI.toString();
         return epr;
    }
}
