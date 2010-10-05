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

import com.sun.xml.ws.api.tx.at.Transactional;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;

/**
 * Volatile WS-AT Participant
 */
public class WSATSynchronization implements Synchronization {
    Xid m_xid;
    String m_status = UNKNOWN;
    private static final String UNKNOWN = "UNKNOWN";
    boolean m_isRemovedFromMap = false;
    Transactional.Version m_version;
    EndpointReference m_epr;

    public WSATSynchronization(EndpointReference epr, Xid xid) {
      this(Transactional.Version.WSAT10, epr, xid);
    }

    public WSATSynchronization(Transactional.Version version, EndpointReference epr, Xid xid) {
        this.m_version = version;
        m_xid = xid;
        m_epr = epr;
       //todoremove  if (WSATHelper.isDebugEnabled())
          //todoremove   WseeWsatLogger.logWSATSynchronization( m_epr.toString(), m_xid, "");
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public void beforeCompletion() {
      //todoremove   if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logBeforeCompletionEntered( m_epr.toString(), m_xid);
        try {
            WSATHelper.getInstance().beforeCompletion( m_epr, m_xid, this);
            synchronized (this) {
                if (m_status.equals(WSATConstants.COMMITTED)) { // we received a reply from call already
//todoremove                     if (WSATHelper.isDebugEnabled())
//todoremove                         WseeWsatLogger.logBeforeCompletionCommittedBeforeWait( m_epr.toString(), m_xid);
                    return;
                }
//todoremove                 if (WSATHelper.isDebugEnabled())
//todoremove                     WseeWsatLogger.logBeforeCompletionWaitingForReply(m_epr.toString(), m_xid);
                this.wait(WSATHelper.getInstance().getWaitForReplyTimeout());
//todoremove                 if (WSATHelper.isDebugEnabled())
//todoremove                     WseeWsatLogger.logBeforeCompletionFinishedWaitingForReply(m_epr.toString(), m_xid);
            }
//todoremove             WseeWsatLogger.logBeforeCompletionReceivedReplyWithStatus(m_status, m_epr.toString(), m_xid);
            if (!m_status.equals(WSATConstants.COMMITTED)) {
//todoremove                 WseeWsatLogger.logBeforeCompletionUnexceptedStatus(m_status, m_epr.toString(), m_xid);
                setRollbackOnly();
            }
        } catch (InterruptedException e) {
//todoremove             WseeWsatLogger.logBeforeCompletionInterruptedException(e, m_epr.toString(), m_xid);
            setRollbackOnly();
        } catch (Exception e) {
//todoremove             WseeWsatLogger.logBeforeCompletionException(e, m_epr.toString(), m_xid);
            setRollbackOnly();
        } finally {
            WSATHelper.getInstance().removeVolatileParticipant(m_xid);
            m_isRemovedFromMap = true;
        }
    }

    private void setRollbackOnly() {
        Transaction transaction = null;//todoremove TransactionHelper.getTransactionHelper().getTransaction();
        if (transaction != null) {
            try {
                transaction.setRollbackOnly();
            } catch (SystemException e) {
        //todoremove         WseeWsatLogger.logBeforeCompletionSystemExceptionDuringSetRollbackOnly(e, m_epr.toString(), m_xid);
            }
        } else ;//todoremove WseeWsatLogger.logBeforeCompletionTransactionNullDuringSetRollbackOnly(m_epr.toString(), m_xid);
    }

    public void afterCompletion(int status) {
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logAfterCompletionStatus(m_epr.toString(), m_xid, "" + status);
        //no-op
    }

    /**
     * Simply used for equality
     *
     * @return Xid that identifies this XAResource as there is a 1-to-1 relationship
     */
    Xid getXid() {
        return m_xid;
    }

    /**
     * Equality check based on instanceof and Xid that identifies this XAResource
     *
     * @param obj Object to conduct equality check against
     * @return if equal
     */
    public boolean equals(Object obj) {
        return obj instanceof WSATSynchronization && ((WSATSynchronization) obj).getXid().equals(m_xid);
    }

    /**
     * Prevents leaks
     *
     * @throws Throwable he <code>Exception</code> raised by this method
     */
    protected void finalize() throws Throwable {
        super.finalize();
        if (!m_isRemovedFromMap) WSATHelper.getInstance().removeVolatileParticipant(m_xid);
    }
}
