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

package com.sun.xml.ws.tx.at.runtime;

import com.sun.xml.ws.tx.at.WSATException;

import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;

/**
 * Defines the interface between WS-AT and underlying transaction processing system
 */
public interface TransactionServices {
    /**
     * The tx id of the tx on this thread
     * @return byte[] tid
     */
    byte[] getGlobalTransactionId();

    /**
     * Called by Registraion Service during register call in order to enlist WSAT XAResource
     *  (this is essentially the WSAT participant EPR wrapper that is serialized for recovery)
     * and return branchqual in order to create RegisterResponseType
     *
     * @param resource (WSAT)XAResource
     * @param xid Xid
     * @return byte[] branchqual to use for 
     * @throws WSATException any error during enlist as WSAT GatewayRM
     */
   Xid enlistResource(XAResource resource, Xid xid) throws WSATException;

    /**
     * Called by Registration service to register a volatile participant
     * @param synchronization javax.transaction.Synchronization
     * @param xid Xid
     * @throws WSATException wsatXAResource
     */
    void registerSynchronization(Synchronization synchronization, Xid xid) throws WSATException;

    /**
     * Called by server tube (WSATServerHelper) to infect thread with tx
     * @param timeout timeout/ttl
     * @param tId byte[]
     * @throws WSATException wsatXAResource
     */
  Xid importTransaction(int timeout, byte[] tId) throws WSATException;

    /**
     * Called by Participant endpoint to prepare tx/subordinate branch
     * @param tId byte[]
     * @return String vote, see WSATConstants
     * @throws WSATException wsatXAResource
     */
    String prepare(byte[] tId) throws WSATException;

    /**
     * Called by Participant endpoint to commit tx/subordinate branch
     * @param tId byte[]
     * @throws WSATException wsatXAResource
     */
    void commit(byte[] tId) throws WSATException;//commit tx/subordinate branch

    /**
     * Called by Participant endpoint to prepare tx/subordinate branch
     * @param tId byte[]
     * @throws WSATException wsatXAResource
     */
    void rollback(byte[] tId) throws WSATException;

    /**
     * Called by Coordinator replay operation
     * Bottom-up recovery call, as in JTS, a hint to resend
     * @param tId byte[]
     * @param xaResource (WSAT)XAResource
     * @throws WSATException wsatXAResource
     */
    void replayCompletion(String tId, XAResource xaResource) throws WSATException;

    /**
     * Called from Participant service to get the Coordinator(PortType) for this Xid
     * @param xid Xid
     * @return EndpointReference of Coordinator (as obtained from earlier RegisterResponse)
     */
    EndpointReference getParentReference(Xid xid);
}
