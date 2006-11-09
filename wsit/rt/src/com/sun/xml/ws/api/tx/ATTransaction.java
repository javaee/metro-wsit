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

package com.sun.xml.ws.api.tx;

/**
 * WS-AT Transaction abstraction to enable the enlistment of volatile and durable Partipants.
 * <p/>
 * How to get an ATTransaction.
 * <code>
 * &#64;Resource(name="java:/comp/wsatTxnMgr", type=javax.transaction.TransactionManager.class )
 * javax.transaction.TransactionManager wsatTxnMgr;
 * <p/>
 * //In the scope of a wsat transaction, perform the following:
 * ATTransaction atTxn = (ATTransaction)wsatTxnMgr.getTransaction();
 * </code>
 *
 * @see Participant
 */
public interface ATTransaction extends javax.transaction.Transaction {
    /**
     * Enlist a participant with current WS-AT transaction context.
     *
     * @return <i>true</i> if the participant was enlisted successfully; otherwise <i>false</i>.
     */
    public boolean enlistParticipant(Participant participant);
}
