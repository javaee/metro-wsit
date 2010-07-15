/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.tx.at.api.Transactional;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;


public class WSATXAResource implements WSATConstants, XAResource, Serializable {

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

//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logWSATXAResource(m_epr.toString(), m_xid,"");
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
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logPrepare(m_epr.toString(), m_xid);
        getWSATHelper().prepare(m_epr, m_xid, this);
        try {
            synchronized (this) {
                // we received a reply already
                if (m_status.equals(READONLY)) {
                    return XAResource.XA_RDONLY;
                } else if (m_status.equals(PREPARED)) {
//todoremove                     if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logPreparedBeforeWait(m_epr.toString(), m_xid);
                    return XAResource.XA_OK;
                } else if (m_status.equals(ABORTED)) {
                    throw newFailedStateXAExceptionForMethodNameAndErrorcode("prepare", XAException.XA_RBROLLBACK);
                }
//todoremove                 if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logPrepareWaitingForReply(m_epr.toString(), m_xid);
                this.wait(getWaitForReplyTimeout());
//todoremove                 if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logPrepareFinishedWaitingForReply(m_epr.toString(), m_xid);
            }
//todoremove             if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logPrepareReceivedReplyStatus(m_status, m_epr.toString(), m_xid);
            if (m_status.equals(READONLY)) {
                logSuccess("preparereadonly");
                return XAResource.XA_RDONLY;
            } else if (m_status.equals(PREPARED)) {
                logSuccess("prepareprepared");
                return XAResource.XA_OK;
            } else if (m_status.equals(ABORTED)) {
                throw newFailedStateXAExceptionForMethodNameAndErrorcode("prepare", XAException.XA_RBROLLBACK);
            }
//todoremove             WseeWsatLogger.logFailedStateForPrepare(m_status, m_epr.toString(), m_xid);
            throw newFailedStateXAExceptionForMethodNameAndErrorcode("prepare", XAException.XAER_RMFAIL);
        } catch (InterruptedException e) {
//todoremove             WseeWsatLogger.logInterruptedExceptionDuringPrepare(e, m_epr.toString(), m_xid);
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
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logCommit(m_epr.toString(), m_xid);
        getWSATHelper().commit(m_epr, m_xid, this);
        try {
            synchronized (this) {
                if (m_status.equals(COMMITTED)) { // we received a reply already
//todoremove                     if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logCommitBeforeWait(m_epr.toString(), m_xid);
                    getWSATHelper().removeDurableParticipant(this);
                    m_isRemovedFromMap = true;
                    return;
                }
//todoremove                 if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logCommitWaitingForReply(m_epr.toString(), m_xid);
                this.wait(getWaitForReplyTimeout());
//todoremove                 if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logCommitFinishedWaitingForReply(m_epr.toString(), m_xid);
            }
//todoremove             if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logCommitReceivedReplyStatus(m_status, m_epr.toString(), m_xid);
            if (m_status.equals(COMMITTED)) {
                logSuccess("preparecommitted");
                getWSATHelper().removeDurableParticipant(this);
                m_isRemovedFromMap = true;
            } else if (m_status.equals(PREPARED)) {//timed outs  
//todoremove                 WseeWsatLogger.logFailedStateForCommit(m_status, m_epr.toString(), m_xid);
                XAException xaException = newFailedStateXAExceptionForMethodNameAndErrorcode("commit", XAException.XAER_RMFAIL);
                log("Failed state during WS-AT XAResource commit:" + m_status);
                throw xaException;
            } else {  //should not occur as there is no transition from state ACTIVE TO commit action
//todoremove                 WseeWsatLogger.logFailedStateForCommit(m_status, m_epr.toString(), m_xid);
                XAException xaException = newFailedStateXAExceptionForMethodNameAndErrorcode("commit", XAException.XAER_PROTO);
                log("Failed state during WS-AT XAResource commit:" + m_status);
                throw xaException;
            }
        } catch (InterruptedException e) {
//todoremove             WseeWsatLogger.logInterruptedExceptionDuringCommit(e, m_epr.toString(), m_xid);
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
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logRollback(m_epr.toString(), m_xid);
        getWSATHelper().rollback(m_epr, m_xid, this);
        try {
            synchronized (this) {
                if (m_status.equals(ABORTED)) { // we received a reply already
//todoremove                     if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logRollbackBeforeWait(m_epr.toString(), m_xid);
                    getWSATHelper().removeDurableParticipant(this);
                    m_isRemovedFromMap = true;
                    return;
                }
//todoremove                 if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logRollbackWaitingForReply(m_epr.toString(), m_xid);
                this.wait(getWaitForReplyTimeout());
//todoremove                 if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logRollbackFinishedWaitingForReply(m_epr.toString(), m_xid);
            }
//todoremove             if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logRollbackReceivedReplyStatus(m_status, m_epr.toString(), m_xid);
            if (m_status.equals(ABORTED)) {
                logSuccess("rollbackaborted");
                getWSATHelper().removeDurableParticipant(this);
                m_isRemovedFromMap = true;
            } else if (m_status.equals(PREPARED)) { // timed outs
//todoremove                 WseeWsatLogger.logFailedStateForRollback(m_status, m_epr.toString(), m_xid);
                throw newFailedStateXAExceptionForMethodNameAndErrorcode("rollback", XAException.XAER_RMFAIL);
            } else {
//todoremove                 WseeWsatLogger.logFailedStateForRollback(m_status, m_epr.toString(), m_xid);
                throw newFailedStateXAExceptionForMethodNameAndErrorcode("rollback", XAException.XAER_RMFAIL);
            }
        } catch (InterruptedException e) {
//todoremove             WseeWsatLogger.logInterruptedExceptionDuringRollback(e, m_epr.toString(), m_xid);
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
//todoremove         m_xid = new XidImpl(m_xid.getFormatId(), m_xid.getGlobalTransactionId(), bqual);
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
    }

   private void log(String message) {  //todo this is only used in two places and they should be logs not just debug
      WSATHelper.getInstance().debug("WSATXAResource:" + message);
   }


   private void logSuccess(String method) {
       WSATHelper.getInstance().debug("success state during "+method+" of WS-AT XAResource:"+this);
   }

    public String toString() {
        return "WSATXAResource: xid" + m_xid + " status:" + m_status + " epr:" + m_epr + " isRemovedFromMap:" + m_isRemovedFromMap;
    }

}
