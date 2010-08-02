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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import com.sun.xml.ws.tx.at.WSATHelper;
//import com.sun.xml.ws.tx.common.TransactionImportManager;
//import com.sun.xml.ws.tx.common.TransactionImportWrapper;
import com.sun.xml.ws.tx.at.common.TransactionImportManager;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.coord.common.WSATCoordinationContextBuilder;
import com.sun.xml.ws.tx.coord.common.WSCBuilderFactory;
import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.ws.WebServiceException;


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
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logInboundApplicationMessage();
       Transaction transaction = getWSATTransactionFromMap(map);
       if (transaction != null && !resume(transaction)) return false;
        return true;
    }

    public void doHandleException(Map<String, Object> map) {
//todoremove         if (WSATHelper.isDebugEnabled()) WseeWsatLogger.logInboundApplicationMessage();
       Transaction transaction = getWSATTransactionFromMap(map);
       if (transaction != null) resume(transaction);
    }

   private Transaction getWSATTransactionFromMap(Map map) {
      Transaction transaction = (Transaction)map.get("wsat.transaction");
      return transaction;
   }

   /**
     * Resume the transaction previous suspended by this handler.
     *
     * @return boolean if resume was successful
     */
    private boolean resume(Transaction transaction) {
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logWillResumeInClientSideHandler(transaction, Thread.currentThread());
//todoremove         try {
//todoremove            WSATTubeHelper.getTransactionHelper().getTransactionManager().resume(transaction);
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logResumedInClientSideHandler(transaction, Thread.currentThread());
            return true;
/**        } catch (InvalidTransactionException e) {
            if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logInvalidTransactionExceptionInClientSideHandler(
//todoremove                         e, transaction, Thread.currentThread());
            WSATTubeHelper.getTransactionHelper().getTransactionManager().forceResume(transaction);
//todoremove             transaction.setRollbackOnly(e);
               WSATTubeHelper.getTransactionHelper().getTransactionManager().forceResume(transaction);
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
            WSATTubeHelper.getTransactionHelper().getTransactionManager().forceResume(transaction);
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

        } */
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
    private List<Header> processTransactionalRequest(TransactionalAttribute transactionalAttribute, Map map) {
        Transaction suspendedTransaction = suspend(map);
//todoremove        if (suspendedTransaction==null) return null;
        List<Header> headers = new ArrayList<Header>();
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logSuspendSuccessfulInClientSideHandler(suspendedTransaction, Thread.currentThread());

        String txId = "wsattesttxid";
        try {
            txId = TransactionManagerImpl.getInstance().getTransaction().toString(); //todoremoveWSATTubeHelper.getWSATTxIdForTransaction(suspendedTransaction);
        } catch (SystemException ex) {
            Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        long ttl = 30000; //todoremove

        try {
            ttl = TransactionImportManager.getInstance().getTransactionRemainingTimeout(); //todoremove suspendedTransaction.getTimeToLiveMillis();
            //todoremove         if (WSATHelper.isDebugEnabled())
            //todoremove             WseeWsatLogger.logWSATInfoInClientSideHandler(txId, ttl, suspendedTransaction, Thread.currentThread());
        } catch (SystemException ex) {
            Logger.getLogger(WSATClientHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logWSATInfoInClientSideHandler(txId, ttl, suspendedTransaction, Thread.currentThread());

        WSCBuilderFactory builderFactory = WSCBuilderFactory.newInstance(transactionalAttribute.getVersion());
        WSATCoordinationContextBuilder builder  = builderFactory.newWSATCoordinationContextBuilder();
        CoordinationContextIF cc = builder.txId(txId).expires(ttl).soapVersion(transactionalAttribute.getSoapVersion()).mustUnderstand(true).build();
        Header coordinationHeader = Headers.create(cc.getJAXBRIContext(),cc.getDelegate());
        headers.add(coordinationHeader);

//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logOutboundApplicationMessageTransactionAfterAddingContext(suspendedTransaction);


        return headers;
    }

    /**
     * Suspend the transaction on th current thread which will be resumed upon return
     *
     * @return Transaction not null if suspend is successful
     */
    private Transaction suspend(Map map) {
        Transaction suspendedTransaction = null;
  //       try {

//todo         TransactionImportManager.getInstance().release(null);
             //todoremove             TransactionManager clientTransactionManager =
//todoremove                     WSATTubeHelper.getTransactionHelper().getTransactionManager();
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logAboutToSuspendInClientSideHandler(clientTransactionManager, Thread.currentThread());
//todoremove             suspendedTransaction = (Transaction)clientTransactionManager.suspend();
//todoremove             suspendedTransaction.setLocalProperty(Constants.OTS_TX_EXPORT_PROPNAME, null);
//todoremove             map.put("wsat.transaction", suspendedTransaction);
//todoremove             if (WSATHelper.isDebugEnabled())
//todoremove                 WseeWsatLogger.logSuspendedInClientSideHandler(suspendedTransaction, Thread.currentThread());
    //     } catch (SystemException e) {
            //tx should always be null here as suspend would either work or not
//todoremove             WseeWsatLogger.logSystemExceptionDuringSuspend(e, suspendedTransaction, Thread.currentThread());
   //         return null;
  //       }
         return suspendedTransaction;
    }

}