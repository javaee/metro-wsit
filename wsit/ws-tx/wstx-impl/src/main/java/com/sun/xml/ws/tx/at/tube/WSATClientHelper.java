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
package com.sun.xml.ws.tx.at.tube;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Headers;
import com.sun.xml.ws.tx.at.WSATConstants;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.tx.at.WSATHelper;

import com.sun.xml.ws.tx.at.common.TransactionImportManager;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.at.internal.XidImpl;
import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.coord.common.WSATCoordinationContextBuilder;
import com.sun.xml.ws.tx.coord.common.WSCBuilderFactory;
import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;
import java.util.UUID;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;


public class WSATClientHelper implements WSATClient {

    /**
     * For outbound case, if transaction exists, suspend and store it and attach CoordinationContext to SOAP Header
     * For return of outbound case, if suspend transaction exists, resume it
     *
     * @return a list of Header that should be added the request message
     */
    public List<Header> doHandleRequest(TransactionalAttribute transactionalAttribute, Map<String, Object> map) {
//        if (!transactionalAttribute.isEnabled())
//            return null;
//        Transaction transaction = WSATTubeHelper.getTransaction();
//        boolean isTxPresent = transaction != null;
//        if (!isTxPresent) {
//todoremove           if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logOutboundApplicationMessageNoTransaction();
//           if(transactionalAttribute.isRequired())
//              throw new WebServiceException("no transaction to be exported!");
//            else return null;
//         }
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logOutboundApplicationMessageTransactionBeforeAddingContext(transaction);
        List<Header> addedHeaders = processTransactionalRequest(transactionalAttribute, map);
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logOutboundApplicationMessageTransactionAfterAddingContext(transaction);
        return addedHeaders;
    }

    public boolean doHandleResponse(Map<String, Object> map) {
       return resumeAndClearXidTxMap(map);
    }

    public void doHandleException(Map<String, Object> map) {
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logInboundApplicationMessage();
       resumeAndClearXidTxMap(map);
    }

    private boolean resumeAndClearXidTxMap(Map<String, Object> map) {
        //todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logInboundApplicationMessage();
        Xid xid = getWSATXidFromMap(map);
        if (xid != null) {
            WSATHelper.getInstance().removeFromXidToTransactionMap(xid);
        }
        Transaction transaction = getWSATTransactionFromMap(map);
        return !(transaction != null && !resume(transaction));
    }

   private Transaction getWSATTransactionFromMap(Map map) {
      Transaction transaction = (Transaction)map.get(WSATConstants.WSAT_TRANSACTION);
      return transaction;
   }

   private Xid getWSATXidFromMap(Map map) {
      Xid xid = (Xid)map.get(WSATConstants.WSAT_TRANSACTION_XID);
      return xid;
   }

   /**
     * Resume the transaction previous suspended by this handler.
     *
     * @return boolean if resume was successful
     */
    private boolean resume(Transaction transaction) {
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logWillResumeInClientSideHandler(transaction, Thread.currentThread());
    try {
        TransactionManagerImpl.getInstance().getTransactionManager().resume(transaction);
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logResumedInClientSideHandler(transaction, Thread.currentThread());
            return true; //todo redundant catch blocks below...
    } catch (InvalidTransactionException e) {
            if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logInvalidTransactionExceptionInClientSideHandler(
//todoremove                         e, transaction, Thread.currentThread());
//todoremove            WSATTubeHelper.getTransactionHelper().getTransactionManager().forceResume(transaction);
//todoremove             transaction.setRollbackOnly(e);
//todoremove               WSATTubeHelper.getTransactionHelper().getTransactionManager().forceResume(transaction);
            try {
                transaction.setRollbackOnly();
            } catch (IllegalStateException ex) {
                Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (SystemException ex) {
                Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return false;
        } catch (SystemException e) {
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logSystemExceptionInClientSideHandler(
//todoremove                         e, transaction, Thread.currentThread());
//todoremove            WSATTubeHelper.getTransactionHelper().getTransactionManager().forceResume(transaction);
            try {
                transaction.setRollbackOnly();
                return false;
            } catch (IllegalStateException ex) {
                Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (SystemException ex) {
                Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

        } 
    }

    /**
     * Process ClientRequestInfo received for this outgoing request.
     * Determine if this transaction interceptor is interested in the operation.
     * Get the current transaction.  One should exist as we checked for it's presence before calling this method
     * If no transaction exists do nothing.
     * If one does exist create and add the transaction CoordinationContext.
     *
     * @param transactionalAttribute
     * @return false if there are any issues with suspend or message header creation (namely SOAPException),
     *         true otherwise
     */
    private List<Header> processTransactionalRequest(TransactionalAttribute transactionalAttribute, Map<String, Object> map) {
        List<Header> headers = new ArrayList<Header>();
        String txId = null;
        //todo get cluster/servername, make this unique
        String s = UUID.randomUUID().toString().replace("urn:","").replaceAll("uuid:","").trim();
        Xid xid = new XidImpl(1234,new String(""+System.currentTimeMillis()).getBytes(), new byte[]{});
        txId = TransactionIdHelper.getInstance().xid2wsatid(xid);
        long ttl = 0;
        try {
            ttl = TransactionImportManager.getInstance().getTransactionRemainingTimeout(); //todoremove verify if this call is from inbound only suspendedTransaction.getTimeToLiveMillis();
            //todoremove         if (WSATHelper.isDebugEnabled())
            //todoremove             WseeWsatLogger.logWSATInfoInClientSideHandler(txId, ttl, suspendedTransaction, Thread.currentThread());
        } catch (SystemException ex) {
            Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logWSATInfoInClientSideHandler(txId, ttl, suspendedTransaction, Thread.currentThread());
        WSCBuilderFactory builderFactory =
                WSCBuilderFactory.newInstance(transactionalAttribute.getVersion());
        WSATCoordinationContextBuilder builder  =
                builderFactory.newWSATCoordinationContextBuilder();
        CoordinationContextIF cc =
                builder.txId(txId).expires(ttl).soapVersion(transactionalAttribute.getSoapVersion()).mustUnderstand(true).build();
        Header coordinationHeader =
                Headers.create(cc.getJAXBRIContext(),cc.getDelegate());
        headers.add(coordinationHeader);
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logOutboundApplicationMessageTransactionAfterAddingContext(suspendedTransaction);
        Transaction suspendedTransaction = suspend(map); //note suspension moved after context creation
        map.put(WSATConstants.WSAT_TRANSACTION_XID, xid);
        WSATHelper.getInstance().putToXidToTransactionMap(xid, suspendedTransaction);
        return headers;
    }

    /**
     * Suspend the transaction on th current thread which will be resumed upon return
     *
     * @return Transaction not null if suspend is successful
     */
    private Transaction suspend(Map<String, Object> map) {
        Transaction suspendedTransaction = null;
       try {
           suspendedTransaction = TransactionManagerImpl.getInstance().getTransactionManager().suspend();
//todoremove                     WSATTubeHelper.getTransactionHelper().getTransactionManager();
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logAboutToSuspendInClientSideHandler(clientTransactionManager, Thread.currentThread());
           map.put(WSATConstants.WSAT_TRANSACTION, suspendedTransaction);
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logSuspendedInClientSideHandler(suspendedTransaction, Thread.currentThread());
         } catch (SystemException e) {
            //tx should always be null here as suspend would either work or not
//todoremove             WseeWsatLogger.logSystemExceptionDuringSuspend(e, suspendedTransaction, Thread.currentThread());
            return null;
         }
         return suspendedTransaction;
    }

}