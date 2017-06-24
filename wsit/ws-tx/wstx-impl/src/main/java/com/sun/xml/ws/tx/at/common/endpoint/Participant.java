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

package com.sun.xml.ws.tx.at.common.endpoint;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages; 
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.AddressingUtils;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.api.message.Header;

import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.WSATException;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.common.CoordinatorIF;
import com.sun.xml.ws.tx.at.common.ParticipantIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.at.common.client.CoordinatorProxyBuilder;
import com.sun.xml.ws.tx.dev.WSATRuntimeConfig;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.util.logging.Level;

/**
 * 
 * The common implementation for wsat10 and wsat11 Participants endpoints.
 *  This impl wraps the (sub)coordinator/TM of this server as a WS-AT participant
 */
public class Participant<T> implements ParticipantIF<T> {
   private static final Logger LOGGER = Logger.getLogger(Participant.class);

   private WebServiceContext m_context;
   private WSATVersion<T> m_version;

   public Participant(WebServiceContext context, WSATVersion<T> version) {
      this.m_context = context;
      this.m_version = version;
   }

   /**
    * prepare call on this subordinate
    * @param parameters T
    */
   public void prepare(T parameters) {
      if(WSATHelper.isDebugEnabled()) debug("prepare enter:" + parameters);
      CoordinatorIF<T> coordinatorPort = null;
      byte[] tid = null;
      try {
         tid = getWSATTid();
         coordinatorPort = getCoordinatorPortType();
         String vote = getTransactionaService().prepare(tid);
         if(WSATHelper.isDebugEnabled())
              debug("preparedOperation complete vote:" + vote + " for tid:" + stringForTidByteArray(tid));
         if(vote.equals(WSATConstants.READONLY))
             coordinatorPort.readOnlyOperation(createNotification());
         else if(vote.equals(WSATConstants.PREPARED))
             coordinatorPort.preparedOperation(createNotification());
      } catch (WSATException e) {
          e.printStackTrace();
          if(WSATRuntimeConfig.getInstance().isRollbackOnFailedPrepare()) {
              try {
                  log("prepare resulted in exception, issuing rollback for tid:" + stringForTidByteArray(tid) + " " + e);
                  getTransactionaService().rollback(tid);
              } catch (WSATException e1) {
                  e1.printStackTrace();
              }
          }
          log("prepare resulted in exception, sending aborted for tid:" + stringForTidByteArray(tid) + " " + e);
         if (coordinatorPort != null) coordinatorPort.abortedOperation(createNotification());
         else {
            log("prepare resulted in exception, unable to send abort as coordinatorPort was null" +
                        "for tid:" + stringForTidByteArray(tid) + " " + e); //should never occur
            throw new WebServiceException("coordinator port null during prepare");
         }
      }
      if(WSATHelper.isDebugEnabled())
          debug("prepare exit:" + parameters +" for tid:" + stringForTidByteArray(tid));
   }

   /**
    * commit call on this subordinate
    * @param parameters T
    */
   public void commit(T parameters) {
      if(WSATHelper.isDebugEnabled()) debug("commit enter:" + parameters);
      CoordinatorIF<T> coordinatorPort = null;
      boolean isCommitSuccessful = false;
      byte[] tid = null;
      Exception exception = null;
      try {
         tid = getWSATTid();
          //replyto is priority, more preformant, and necesary for  routing case
         coordinatorPort = getCoordinatorPortTypeForReplyTo();
         if(WSATHelper.isDebugEnabled())
                  debug("Participant.commit coordinatorPort:" + coordinatorPort + " for tid:" + stringForTidByteArray(tid) );
         if(isInForeignContextMap()) getTransactionaService().commit(tid);
         isCommitSuccessful = true;
         if(WSATHelper.isDebugEnabled())
             debug("committedOperation complete for tid:" + stringForTidByteArray(tid));
      } catch (WSATException e) {
         exception = e;
         log("WSATException during commit for tid:" + stringForTidByteArray(tid) + " " + e);
      } catch (IllegalArgumentException e) {
         exception = e;
         log("IllegalArgumentException during commit for tid:" + stringForTidByteArray(tid) + " " + e);
      }
      if (coordinatorPort==null) {
          if(WSATHelper.isDebugEnabled())
                  debug("Participant.commit coordinatorPort null, about to create from replyto for tid:" +
                          stringForTidByteArray(tid) + " ");
          coordinatorPort = getCoordinatorPortType();
          if(WSATHelper.isDebugEnabled())
                  debug("Participant.commit coordinatorPort null attempting to create from replyto coordinatorPort:" +
                          coordinatorPort + "for tid:" + stringForTidByteArray(tid));
          if(coordinatorPort==null)
              throw new WebServiceException("WS-AT coordinator port null during commit for transaction id:" +
                      stringForTidByteArray(tid));
          if(WSATHelper.isDebugEnabled())
                  debug("Participant.commit coordinatorPort obtained from replyto:"+coordinatorPort +
                          "for tid:" + stringForTidByteArray(tid));
      }
      if (!isCommitSuccessful)
          if(WSATHelper.isDebugEnabled())
              debug("Participant.commit was not successful, presuming previous completion occurred and sending " +
                      "committed for tid:" + stringForTidByteArray(tid) + " Exception:" + exception);
      coordinatorPort.committedOperation(createNotification());
      if(WSATHelper.isDebugEnabled())
          debug("committed reply sent, local commit success is " + isCommitSuccessful +
                  " , coordinatorPort:" + coordinatorPort + " for tid:" + stringForTidByteArray(tid));
      if(WSATHelper.isDebugEnabled())
          debug("commit exit:" + parameters + " for tid:" + stringForTidByteArray(tid));
   }

    /**
     * rollback call on this subordinate
     * @param parameters T
     */
   public void rollback(T parameters) {
      if(WSATHelper.isDebugEnabled()) debug("rollback parameters:" + parameters);
      CoordinatorIF<T> coordinatorPort = null;
      byte[] tid = null;
      try {
         tid = getWSATTid();
         coordinatorPort = getCoordinatorPortTypeForReplyTo(); //replyto is priority and necesary for   routing case
         if(isInForeignContextMap()) getTransactionaService().rollback(tid);
         if(WSATHelper.isDebugEnabled()) debug("rollback abortedOperation complete for tid:" + stringForTidByteArray(tid));
      } catch (IllegalArgumentException e) {
          //IllegalArgException if "No subordinate transaction FF1D-C9F..." in jta map from txservices.getParentReference
          log("rollback IllegalArgumentException for tid:" + stringForTidByteArray(tid) + " " + e);
      } catch (WSATException e) {  //indicates NOTA or
          log("rollback WSATException for tid:" + stringForTidByteArray(tid) + " " + e);
          if(e.errorCode!= XAException.XAER_NOTA && e.errorCode!= XAException.XAER_PROTO) //proto for GF
              throw new WebServiceException("Participant.rollback WSATException for tid:" +
                      stringForTidByteArray(tid) + " " + e);
      }
      if(coordinatorPort != null)
          coordinatorPort.abortedOperation(createNotification());
      else   {
          if(WSATHelper.isDebugEnabled())
              debug("Participant.rollback coordinatorPort null attempting to create from replyto for tid:" +
                      stringForTidByteArray(tid));
          coordinatorPort = getCoordinatorPortType();
          if(WSATHelper.isDebugEnabled())
              debug("Participant.rollback coordinatorPort null attempting to create from replyto for tid:" +
                      stringForTidByteArray(tid) + " coordinatorPort:" + coordinatorPort);
          if(coordinatorPort!=null) coordinatorPort.abortedOperation(createNotification());
          else {
              log("Coordinator port null during rollback for tid:" +
                      stringForTidByteArray(tid) + " about to throw exception/fault.");
              throw new WebServiceException("WS-AT Coordinator port null during rollback for tid:" +
                      stringForTidByteArray(tid));
          }
      }
      if(WSATHelper.isDebugEnabled()) debug("rollback exit:" + parameters + " for tid:" + stringForTidByteArray(tid));
   }

    /**
     * If coordinator address for parentref (as obtained from registerresponse) is not found try replyto for WS-AT 1.0
     * @return CoordinatorIF
     */
    CoordinatorIF<T> getCoordinatorPortTypeForReplyTo() {
            MessageHeaders headerList =
                    (MessageHeaders) m_context.getMessageContext().get(
                            com.sun.xml.ws.developer.JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
          AddressingVersion av = m_version.getAddressingVersion();
          WSEndpointReference wsReplyTo = AddressingUtils.getReplyTo(headerList, av, m_version.getSOPAVersion());
            if(wsReplyTo != null && !wsReplyTo.isNone() &&wsReplyTo.isAnonymous()){
              Header header = headerList.get(av.fromTag, true);
              if(header!=null)
                try {
                  wsReplyTo = header.readAsEPR(av);
                } catch (XMLStreamException e) {
                  log("XMLStreamException while reading ReplyTo EndpointReference:" + e);
                }
            }
            if(wsReplyTo == null || wsReplyTo.isNone() ||wsReplyTo.isAnonymous()){
                return null;
            }
            EndpointReference replyTo = wsReplyTo.toSpec();
            CoordinatorProxyBuilder<T> builder = m_version.newCoordinatorProxyBuilder().to(replyTo);
            CoordinatorIF<T> coordinatorPort = builder.build();
            if(WSATHelper.isDebugEnabled())
                debug("getCoordinatorPortType replytocoordinatorPort:" + coordinatorPort + "for wsReplyTo/from:"+wsReplyTo + " and replyTo/from:"+replyTo);
            return coordinatorPort;
    }


    /**
     * Get TransactionServices interface for () tx internal access
     * @return TransactionServices
     */
   TransactionServices getTransactionaService() {
      return WSATHelper.getTransactionServices();
   }

    /**
     * getWSATTidFromWebServiceContextHeaderList stripped of urn and uuid prefix
      * @return byte[] tid
     */
   byte[] getWSATTid() {
      String tidFromHeader = getWSATHelper().getWSATTidFromWebServiceContextHeaderList(m_context).
              replace("urn:", "").replace("uuid:", "");
      Xid xidFromWebServiceContextHeaderList = TransactionIdHelper.getInstance().wsatid2xid(tidFromHeader);
      byte[] tid = xidFromWebServiceContextHeaderList.getGlobalTransactionId();
      if(WSATHelper.isDebugEnabled()) debug("getWSATTid tid:" + stringForTidByteArray(tid));
      return tid;
   }

    /**
     * getCoordinatorPortType from parent ref corresponding to txid in context header list (as obtained from registerresponse)
     * @return CoordinatorIF
     */
   CoordinatorIF<T> getCoordinatorPortType() {
      String txid = getWSATHelper().getWSATTidFromWebServiceContextHeaderList(m_context);
      Xid xidFromWebServiceContextHeaderList = TransactionIdHelper.getInstance().wsatid2xid(txid);
      EndpointReference parentReference = getTransactionaService().getParentReference(xidFromWebServiceContextHeaderList);
      CoordinatorProxyBuilder<T> builder = m_version.newCoordinatorProxyBuilder().to(parentReference);
      CoordinatorIF<T> coordinatorPort = builder.build();
      if(WSATHelper.isDebugEnabled())
          debug("getCoordinatorPortType coordinatorPort:" + coordinatorPort + "for txid:" + txid +
                  " xid:"+xidFromWebServiceContextHeaderList + " parentRef:"+parentReference);
      return coordinatorPort;
   }

    /**
     * This gates actual subtx calls that may come in due to recovery where there is no prepared subordinate 
     * @return
     */
    boolean isInForeignContextMap() {
        try {
            String txid = getWSATHelper().getWSATTidFromWebServiceContextHeaderList(m_context);
            Xid xidFromWebServiceContextHeaderList = TransactionIdHelper.getInstance().wsatid2xid(txid);
            getTransactionaService().getParentReference(xidFromWebServiceContextHeaderList);
        } catch (Throwable t) {
            return false;
        }
        return true;
    }


    private String stringForTidByteArray(byte[] tid) {
       return (tid==null?null:new String(tid));
   }

   protected T createNotification() {
      return m_version.newNotificationBuilder().build();
   }

   protected WSATHelper getWSATHelper() {
      return m_version.getWSATHelper();
   }

   private void log(String msg) {
       LOGGER.info(LocalizationMessages.WSAT_4613_WSAT_PARTICIPANT(msg));
   }

   private void debug(String msg) {
       LOGGER.info(msg);
   }

}
