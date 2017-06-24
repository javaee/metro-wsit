/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages;
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
    private static final Logger LOGGER = Logger.getLogger(WSATSynchronization.class);
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
       if (WSATHelper.isDebugEnabled())
           LOGGER.info(LocalizationMessages.WSAT_4526_WSAT_SYNCHRONIZATION(m_epr.toString(), m_xid, ""));
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public void beforeCompletion() {
        if (WSATHelper.isDebugEnabled()) LOGGER.info(LocalizationMessages.WSAT_4527_BEFORE_COMPLETION_ENTERED(m_epr.toString(), m_xid));
        try {
            WSATHelper.getInstance().beforeCompletion( m_epr, m_xid, this);
            synchronized (this) {
                if (m_status.equals(WSATConstants.COMMITTED)) { // we received a reply from call already
                    if (WSATHelper.isDebugEnabled()) LOGGER.info(LocalizationMessages.WSAT_4528_BEFORE_COMPLETION_COMMITTED_BEFORE_WAIT(
                        m_epr.toString(), m_xid));
                    return;
                }
                if (WSATHelper.isDebugEnabled()) LOGGER.info(LocalizationMessages.WSAT_4529_BEFORE_COMPLETION_WAITING_FOR_REPLY(
                    m_epr.toString(), m_xid));
                this.wait(WSATHelper.getInstance().getWaitForReplyTimeout());
                if (WSATHelper.isDebugEnabled()) LOGGER.info(LocalizationMessages.WSAT_4530_BEFORE_COMPLETION_FINISHED_WAITING_FOR_REPLY(
                    m_epr.toString(), m_xid));
            }
            LOGGER.info(LocalizationMessages.WSAT_4531_BEFORE_COMPLETION_RECEIVED_REPLY_WITH_STATUS(
                m_status, m_epr.toString(), m_xid));
            if (!m_status.equals(WSATConstants.COMMITTED)) {
                LOGGER.severe(LocalizationMessages.WSAT_4532_BEFORE_COMPLETION_UNEXCEPTED_STATUS(
                    m_status, m_epr.toString(), m_xid));
                setRollbackOnly();
            }
        } catch (InterruptedException e) {
            LOGGER.severe(LocalizationMessages.WSAT_4533_BEFORE_COMPLETION_INTERRUPTED_EXCEPTION(m_epr.toString(), m_xid), e);
            setRollbackOnly();
        } catch (Exception e) {
            LOGGER.severe(LocalizationMessages.WSAT_4534_BEFORE_COMPLETION_EXCEPTION(m_epr.toString(), m_xid), e);
            setRollbackOnly();
        } finally {
            WSATHelper.getInstance().removeVolatileParticipant(m_xid);
            m_isRemovedFromMap = true;
        }
    }

    private void setRollbackOnly() {
        try {
            Transaction transaction = TransactionManagerImpl.getInstance().getTransaction();
            if (transaction != null) {
                transaction.setRollbackOnly();
            } else
                LOGGER.info(LocalizationMessages.WSAT_4536_BEFORE_COMPLETION_TRANSACTION_NULL_DURING_SET_ROLLBACK_ONLY(
                        m_epr.toString(), m_xid));
        } catch (SystemException e) {
            LOGGER.info(LocalizationMessages.WSAT_4535_BEFORE_COMPLETION_SYSTEM_EXCEPTION_DURING_SET_ROLLBACK_ONLY(
                    e, m_epr.toString(), m_xid));
        }
    }

    public void afterCompletion(int status) {
      if (WSATHelper.isDebugEnabled()) LOGGER.info(LocalizationMessages.WSAT_4537_AFTER_COMPLETION_STATUS(m_epr.toString(), m_xid, "" + status));
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
