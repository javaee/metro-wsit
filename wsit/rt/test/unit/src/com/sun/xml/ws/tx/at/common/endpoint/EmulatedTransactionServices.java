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
package com.sun.xml.ws.tx.at.common.endpoint;

import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.WSATException;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;

import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;

/**
 *
 * @author paulparkinson
 */
public class EmulatedTransactionServices implements TransactionServices {

        public boolean m_isRollbackException = false;
        public boolean m_isPrepareException = false;
        public boolean m_isCommitException = false;
        String m_prepareVote = WSATConstants.PREPARED;

        public byte[] getGlobalTransactionId() //the tx id of the tx on this thread
        {
            return new byte[0];
        }

        public byte[] enlistResource(XAResource resource, Xid xid) throws WSATException //enlist XAResource (this is essentially the WSAT participant EPR wrapper)
        {
            return new byte[]{};
        }

        public void registerSynchronization(Synchronization synchronization, Xid xid) throws WSATException {

        }

        public int getExpires()//the transaction timeout value
        {
            return 0;
        }

        public Xid importTransaction(int timeout, byte[] tId) throws WSATException //infect thread with tx
        {
            return null;
        }

        public String prepare(byte[] tId) throws WSATException//prepare tx/subordinate branch
        {
            if (m_isPrepareException) throw new WSATException("test exception from prepare");
            return m_prepareVote;
        }

        public void commit(byte[] tId) throws WSATException//commit tx/subordinate branch
        {
            if (m_isCommitException) throw new WSATException("test exception from commit");
        }

        public void rollback(byte[] tId) throws WSATException//rollback tx/subordinate branch
        {
            if (m_isRollbackException) throw new WSATException("test exception from rollback");
        }

        public void replayCompletion(String tId, XAResource xaResource) throws WSATException//bottom-up recovery call, as in JTS, a hint to resend
        {

        }

        public EndpointReference getParentReference(Xid xid) {
            return null;
        }

        public void setPrepareVoteReturn(String vote) {
            m_prepareVote = vote;
        }
}
