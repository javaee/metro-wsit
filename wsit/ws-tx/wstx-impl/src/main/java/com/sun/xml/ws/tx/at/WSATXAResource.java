/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages; 
import com.sun.xml.ws.api.tx.at.Transactional;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.logging.Level;


public class WSATXAResource implements WSATConstants, XAResource, Serializable {

//    private static final Logger LOGGER = Logger.getLogger(WSATXAResource.class);
    private static final Class LOGGERCLASS = WSATXAResource.class;
    static final long serialVersionUID = -5827137400010343968L;
    private Xid m_xid;
    static final String ACTIVE = "ACTIVE";
    private volatile String m_status = ACTIVE;
    private Transactional.Version m_version;
    private boolean m_isRemovedFromMap = false;
    transient private EndpointReference m_epr;

    /**
     * Constructor used for runtime
     * @param epr SEndpointReference participant endpoint reference
     * @param xid Xid of transaction
     */
    public WSATXAResource(EndpointReference epr, Xid xid) {
        this(Transactional.Version.WSAT10, epr, xid, false);
    }

    /**
     * Constructor used for runtime
     * @param version Transactional.Version
     * @param epr EndpointReference participant endpoint reference
     * @param xid Xid of transaction
     */
    public WSATXAResource(Transactional.Version version, EndpointReference epr, Xid xid) {
        this(version, epr, xid, false);
    }

    /**
     * Constructor used for recovery
     * @param version Transactional.Version
     * @param epr EndpointReference participant endpoint reference
     * @param xid Xid of transaction
     * @param isRecovery true if for recovery, false if not (ie if for runtime)
     */
    public WSATXAResource(Transactional.Version version, EndpointReference epr, Xid xid, boolean isRecovery) {
        m_version  = version;
        if(epr==null)
          throw new IllegalArgumentException("endpoint reference can't be null");
        m_epr = epr;
        m_xid = xid;
        if (WSATHelper.isDebugEnabled())
          WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4538_WSAT_XARESOURCE",
                  new Object[]{m_epr.toString(), m_xid,""}, null);
//            LOGGER.info(LocalizationMessages.WSAT_4538_WSAT_XARESOURCE(m_epr.toString(), m_xid,""));
        if (isRecovery) m_status = PREPARED;

    }

    WSATHelper getWSATHelper() {
        return WSATHelper.getInstance(m_version);
    }

    /**
     * Called by Coordinator service in reaction to completion operation call in order to setStatus accordingly
     * @param status String status as found in WSATConstants.
     */
    public void setStatus(String status) {
        m_status = status;
    }

    /**
     * @param xid Xid The actual Xid passed in is ignored and the member variable used instead as the value passed in
     *            as there is a final 1-to-1 relationship between WSATXAResource and Xid.  In reality is doesn't matter but in
     *            order to be consistent with rollback (where it does matter), the member variable is used
     * @return int prepare vote
     * @throws XAException xaException
     */
    public int prepare(Xid xid) throws XAException {
        debug("prepare xid:"+xid);
        if (WSATHelper.isDebugEnabled())
          WSATImplInjection.getInstance().getLogging().log(
                  null, LOGGERCLASS, Level.INFO, "WSAT4539_PREPARE", new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4539_PREPARE(m_epr.toString(), m_xid));
        getWSATHelper().prepare(m_epr, m_xid, this);
        try {
            synchronized (this) {
                // we received a reply already
                if (m_status.equals(READONLY)) {
                    return XAResource.XA_RDONLY;
                } else if (m_status.equals(PREPARED)) {
                    if (WSATHelper.isDebugEnabled())
                        WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4540_PREPARED_BEFORE_WAIT",
                                new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4540_PREPARED_BEFORE_WAIT(m_epr.toString(), m_xid));
                    return XAResource.XA_OK;
                } else if (m_status.equals(ABORTED)) {
                    throw newFailedStateXAExceptionForMethodNameAndErrorcode("prepare", XAException.XA_RBROLLBACK);
                }
                if (WSATHelper.isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4541_PREPARE_WAITING_FOR_REPLY",
                            new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4541_PREPARE_WAITING_FOR_REPLY(
//                    m_epr.toString(), m_xid));
                this.wait(getWaitForReplyTimeout());
                if (WSATHelper.isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4542_PREPARE_FINISHED_WAITING_FOR_REPLY",
                            new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4542_PREPARE_FINISHED_WAITING_FOR_REPLY(
//                    m_epr.toString(), m_xid));
            }
            if (WSATHelper.isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.INFO, "WSAT4543_PREPARE_RECEIVED_REPLY_STATUS",
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4543_PREPARE_RECEIVED_REPLY_STATUS(
//                m_status, m_epr.toString(), m_xid));
            if (m_status.equals(READONLY)) {
                logSuccess("preparereadonly");
                return XAResource.XA_RDONLY;
            } else if (m_status.equals(PREPARED)) {
                logSuccess("prepareprepared");
                return XAResource.XA_OK;
            } else if (m_status.equals(ABORTED)) {
                throw newFailedStateXAExceptionForMethodNameAndErrorcode("prepare", XAException.XA_RBROLLBACK);
            }
            WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.SEVERE, "WSAT4544_FAILED_STATE_FOR_PREPARE",
                    new Object[]{m_status, m_epr.toString(), m_xid}, null);
//            LOGGER.severe(LocalizationMessages.WSAT_4544_FAILED_STATE_FOR_PREPARE(m_status, m_epr.toString(), m_xid));
            throw newFailedStateXAExceptionForMethodNameAndErrorcode("prepare", XAException.XAER_RMFAIL);
        } catch (InterruptedException e) {
            WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.INFO, "WSAT4545_INTERRUPTED_EXCEPTION_DURING_PREPARE",
                    new Object[]{e, m_epr.toString(), m_xid}, null);
//            LOGGER.info(LocalizationMessages.WSAT_4545_INTERRUPTED_EXCEPTION_DURING_PREPARE(e, m_epr.toString(), m_xid));
            XAException xaException = new XAException("InterruptedException during WS-AT XAResource prepare");
            xaException.errorCode = XAException.XAER_RMFAIL;
            xaException.initCause(e);
            throw xaException;
        }
    }


   private XAException newFailedStateXAExceptionForMethodNameAndErrorcode(String method, int errorcode) {
       XAException xaException = new XAException("Failed state during "+method+" of WS-AT XAResource:"+this);
       xaException.errorCode = errorcode;
       return xaException;
   }

    /**
     * Prevents leaks in the event of protocol exceptions, abandonments, etc.
     * This has the requirement that transactions are never recreated for recovery from scratch using in-memory records
     *
     * @throws Throwable he <code>Exception</code> raised by this method
     */
    protected void finalize() throws Throwable {
        super.finalize();
        if (!m_isRemovedFromMap) getWSATHelper().removeDurableParticipant(this);
    }

    /**
     * Commit.
     *
     * @param xid      Xid The actual Xid passed in is ignored and the member variable used instead as there is a final 
     *                 1-to-1 relationship between WSATXAResource and Xid during construction.  In reality is doesn't matter but in
     *                 order to be consistent with rollback (where it does matter), the member variable is used
     * @param onePhase there is no single phase commit in WS-AT and so this is ignored
     * @throws XAException xaException
     */
    public void commit(Xid xid, boolean onePhase) throws XAException {
        debug("commit xid:"+xid+" onePhase:"+onePhase);
        if (WSATHelper.isDebugEnabled())
          WSATImplInjection.getInstance().getLogging().log(
                  null, LOGGERCLASS, Level.INFO, "WSAT4546_COMMIT", new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4546_COMMIT( m_epr.toString(), m_xid));
        getWSATHelper().commit(m_epr, m_xid, this);
        try {
            synchronized (this) {
                if (m_status.equals(COMMITTED)) { // we received a reply already
                    if (WSATHelper.isDebugEnabled())
                        WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4547_COMMIT_BEFORE_WAIT",
                                new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4547_COMMIT_BEFORE_WAIT(m_epr.toString(), m_xid));
                    getWSATHelper().removeDurableParticipant(this);
                    m_isRemovedFromMap = true;
                    return;
                }
                if (WSATHelper.isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4548_COMMIT_WAITING_FOR_REPLY",
                            new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4548_COMMIT_WAITING_FOR_REPLY(m_epr.toString(), m_xid));
                this.wait(getWaitForReplyTimeout());
                if (WSATHelper.isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4549_COMMIT_FINISHED_WAITING_FOR_REPLY",
                            new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4549_COMMIT_FINISHED_WAITING_FOR_REPLY(
//                    m_epr.toString(), m_xid));
            }
            if (WSATHelper.isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.INFO, "WSAT4550_COMMIT_RECEIVED_REPLY_STATUS",
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4550_COMMIT_RECEIVED_REPLY_STATUS(
//                m_status, m_epr.toString(), m_xid));
            if (m_status.equals(COMMITTED)) {
                logSuccess("preparecommitted");
                getWSATHelper().removeDurableParticipant(this);
                m_isRemovedFromMap = true;
            } else if (m_status.equals(PREPARED)) {//timed outs
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.SEVERE, "WSAT4551_FAILED_STATE_FOR_COMMIT",
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                LOGGER.severe(LocalizationMessages.WSAT_4551_FAILED_STATE_FOR_COMMIT(
//                    m_status, m_epr.toString(), m_xid));
                XAException xaException = newFailedStateXAExceptionForMethodNameAndErrorcode("commit", XAException.XAER_RMFAIL);
                log("Failed state during WS-AT XAResource commit:" + m_status, xaException);
                throw xaException;
            } else {  //should not occur as there is no transition from state ACTIVE TO commit action
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.SEVERE, "WSAT4551_FAILED_STATE_FOR_COMMIT",
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                LOGGER.severe(LocalizationMessages.WSAT_4551_FAILED_STATE_FOR_COMMIT(
//                    m_status, m_epr.toString(), m_xid));
                XAException xaException = newFailedStateXAExceptionForMethodNameAndErrorcode("commit", XAException.XAER_PROTO);
                log("Failed state during WS-AT XAResource commit:" + m_status, xaException);
                throw xaException;
            }
        } catch (InterruptedException e) {
            WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.SEVERE, "WSAT4552_INTERRUPTED_EXCEPTION_DURING_COMMIT",
                    new Object[]{m_epr.toString(), m_xid}, null);
//            LOGGER.severe(LocalizationMessages.WSAT_4552_INTERRUPTED_EXCEPTION_DURING_COMMIT(m_epr.toString(), m_xid), e);
            XAException xaException = new XAException("InterruptedException during WS-AT XAResource commit:"+e);
            xaException.errorCode = XAException.XAER_RMFAIL;
            xaException.initCause(e);
            throw xaException;
        } finally {
            getWSATHelper().removeDurableParticipant(this);
        }
    }

    /**
     * Returns the amount of time to wait for replies to prepare, commit, and rollback calls
     * @return wait time in milliseconds
     */
    int getWaitForReplyTimeout() {
        return getWSATHelper().getWaitForReplyTimeout();
    }

    /**
     * @param xid Xid The actual Xid passed in is ignored and the member variable used instead as the value passed in
     *            will be null for bottom-up recovery and because there is a final 1-to-1 relationship between WSATXAResource and Xid.
     * @throws XAException
     */
    public void rollback(Xid xid) throws XAException {
        debug("rollback xid:"+xid);
        if (WSATHelper.isDebugEnabled())
          WSATImplInjection.getInstance().getLogging().log(
                  null, LOGGERCLASS, Level.INFO, "WSAT4553_ROLLBACK", new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4553_ROLLBACK(m_epr.toString(), m_xid));
        getWSATHelper().rollback(m_epr, m_xid, this);
        try {
            synchronized (this) {
                if (m_status.equals(ABORTED)) { // we received a reply already
                    if (WSATHelper.isDebugEnabled())
                        WSATImplInjection.getInstance().getLogging().log(
                                null, LOGGERCLASS, Level.INFO, "WSAT4554_ROLLBACK_BEFORE_WAIT",
                                new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4554_ROLLBACK_BEFORE_WAIT(m_epr.toString(), m_xid));
                    getWSATHelper().removeDurableParticipant(this);
                    m_isRemovedFromMap = true;
                    return;
                }
                if (WSATHelper.isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4555_ROLLBACK_WAITING_FOR_REPLY",
                            new Object[]{m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4555_ROLLBACK_WAITING_FOR_REPLY(m_epr.toString(), m_xid));
                this.wait(getWaitForReplyTimeout());
                if (WSATHelper.isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.INFO, "WSAT4556_ROLLBACK_FINISHED_WAITING_FOR_REPLY",
                            new Object[]{"WSAT4556_ROLLBACK_FINISHED_WAITING_FOR_REPLY"}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4556_ROLLBACK_FINISHED_WAITING_FOR_REPLY(m_epr.toString(), m_xid));
            }
            if (WSATHelper.isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.INFO, "WSAT4557_ROLLBACK_RECEIVED_REPLY_STATUS",
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                  LOGGER.info(LocalizationMessages.WSAT_4557_ROLLBACK_RECEIVED_REPLY_STATUS(m_status, m_epr.toString(), m_xid));
            if (m_status.equals(ABORTED)) {
                logSuccess("rollbackaborted");
                getWSATHelper().removeDurableParticipant(this);
                m_isRemovedFromMap = true;
            } else if (m_status.equals(PREPARED)) { // timed outs and recovering txs
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.SEVERE, "WSAT4558_FAILED_STATE_FOR_ROLLBACK", 
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                LOGGER.severe(LocalizationMessages.WSAT_4558_FAILED_STATE_FOR_ROLLBACK(m_status, m_epr.toString(), m_xid));
                throw newFailedStateXAExceptionForMethodNameAndErrorcode("rollback", XAException.XAER_RMFAIL);
            } else {
                WSATImplInjection.getInstance().getLogging().log(
                        null, LOGGERCLASS, Level.SEVERE, "WSAT4558_FAILED_STATE_FOR_ROLLBACK",
                        new Object[]{m_status, m_epr.toString(), m_xid}, null);
//                LOGGER.severe(LocalizationMessages.WSAT_4558_FAILED_STATE_FOR_ROLLBACK( m_status, m_epr.toString(), m_xid));
                throw newFailedStateXAExceptionForMethodNameAndErrorcode("rollback", XAException.XAER_RMFAIL);
            }
        } catch (InterruptedException e) {
            WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.SEVERE, "WSAT4559_INTERRUPTED_EXCEPTION_DURING_ROLLBACK",
                    new Object[]{m_epr.toString(), m_xid}, null);
//            LOGGER.severe(LocalizationMessages.WSAT_4559_INTERRUPTED_EXCEPTION_DURING_ROLLBACK(m_epr.toString(), m_xid), e);
            XAException xaException = new XAException("InterruptedException during WS-AT XAResource rollback");
            xaException.errorCode = XAException.XAER_RMFAIL;
            xaException.initCause(e);
            throw xaException;
        } finally {
            getWSATHelper().removeDurableParticipant(this);
        }
    }


    /**
     * Not applicable to WS-AT
     *
     * @param xid Xid
     * @throws XAException
     */
    public void forget(Xid xid) throws XAException {

    }

    /**
     * Not applicable to WS-AT
     *
     * @param i timeout
     * @return boolean
     * @throws XAException
     */
    public boolean setTransactionTimeout(int i) throws XAException {
        return true;
    }

    /**
     * Not applicable to WS-AT
     *
     * @param xid Xid
     * @param i   flag
     * @throws XAException
     */
    public void start(Xid xid, int i) throws XAException {

    }

    /**
     * Not applicable to WS-AT
     *
     * @param xid Xid
     * @param i   flag
     * @throws XAException xaexception
     */
    public void end(Xid xid, int i) throws XAException {

    }

    /**
     * Not applicable to WS-AT
     *
     * @return int timeout
     * @throws XAException
     */
    public int getTransactionTimeout() throws XAException {
        return Integer.MAX_VALUE;
    }

    /**
     * Not applicable to WS-AT
     *
     * @param xaResource XAResource
     * @return boolean must be false
     * @throws XAException xaexeception
     */
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return false;
    }

    /**
     * Not applicable to WS-AT
     *
     * @param i flag
     * @return empty array
     * @throws XAException xaexception
     */
    public Xid[] recover(int i) throws XAException {
        return new Xid[0];
    }

    /**
     * Returns Xid for use in equality, as key in durable participant map, and from gateway registerWSATResource of
     *  subordinate/participant.
     *
     * @return Xid that identifies this XAResource
     */
    public Xid getXid() {
        return m_xid;
    }

    /**
     * Called from Registration to set branch qualifier that was generated during enlist
     * @param bqual byte[]
     */
    public void setBranchQualifier(byte[] bqual){
    }

    public void setXid(Xid xid){
        m_xid = xid;
    }

    /**
     * Equality check based on instanceof and Xid that identifies this Synchronization
     *
     * @param obj Object to conduct equality check against
     * @return if equal
     */
    public boolean equals(Object obj) {
        return obj instanceof WSATXAResource &&
                ((WSATXAResource) obj).getXid().equals(m_xid) &&
                ((WSATXAResource) obj).m_epr.toString().equals(m_epr.toString());
    }

    private void writeObject(ObjectOutputStream oos)
        throws IOException {
      oos.defaultWriteObject();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      m_epr.writeTo(new StreamResult(bos));
      byte[] eprBytes = bos.toByteArray();
      oos.writeInt(eprBytes.length);
      oos.write(eprBytes);
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
      int len = ois.readInt();
      byte[] eprBytes = new byte[len];
      ois.readFully(eprBytes);
      m_epr = EndpointReference.readFrom(new StreamSource(new ByteArrayInputStream(eprBytes)));
      m_status = PREPARED;  //would not be in log unless prepare was complete
    }

   private void log(String message, XAException xaex) {  //todo msgcat, this is only used in two places in commit
      WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.WARNING, null, message + " XAException.errorcode:" + xaex.errorCode, xaex);
//      LOGGER.warning( message + " XAException.errorcode:" + xaex.errorCode, xaex);
   }

   private void logSuccess(String method) {
      WSATImplInjection.getInstance().getLogging().log(
              null, LOGGERCLASS, Level.INFO, null,
              "success state during " + method + " of WS-AT XAResource:" + this, null);
//      LOGGER.info("success state during " + method + " of WS-AT XAResource:" + this);
   }

   private void debug(String msg) {
      if (isDebugEnabled())
          WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO, null, msg, null);
//      LOGGER.info(msg);
   }

    public String toString() {
        return "WSATXAResource: xid" + m_xid + " status:" + m_status + " epr:" + m_epr + " isRemovedFromMap:" + m_isRemovedFromMap;
    }

    boolean isDebugEnabled(){
        return WSATHelper.getInstance().isDebugEnabled();
    }

}
