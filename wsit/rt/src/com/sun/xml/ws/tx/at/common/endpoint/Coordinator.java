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

import com.sun.xml.ws.tx.at.internal.XidImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.WSATException;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.WSATXAResource;
import com.sun.xml.ws.tx.at.common.CoordinatorIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;

import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;

/**
 *
 * This is the common implementation for wsat10 and wsat11 Coordinators endpoints.
 */
public class Coordinator<T> implements CoordinatorIF<T> {
    @javax.annotation.Resource
    private WebServiceContext m_context;
    private WSATVersion<T> m_version;

    public Coordinator(WebServiceContext m_context, WSATVersion<T> m_version) {
        this.m_context = m_context;
        this.m_version = m_version;
    }

    /**
     * Prepared response
     * Get Xid and update status in order to notify.
     * If Xid does not exist this must be a recovery call
     * @param parameters Notification
     */
    public void preparedOperation(T parameters) {
//todoremove         if(isDebugEnabled()) WseeWsatLogger.logPreparedOperationEntered(parameters);
        Xid xidFromWebServiceContextHeaderList = getXid();
 //todoremove         if(isDebugEnabled()) WseeWsatLogger.logPreparedOperation(xidFromWebServiceContextHeaderList);
        if (!getWSATHelper().setDurableParticipantStatus(xidFromWebServiceContextHeaderList, WSATXAResource.PREPARED)) {
            //Xid does not exist so must/better be recovery
            replayOperation(parameters);
        }
  //todoremove        if(isDebugEnabled()) WseeWsatLogger.logPreparedOperationExited(parameters);
    }

    /**
     * Aborted response
     * Get Xid and update status in order to notify.
     * @param parameters Notification
     */
    public void abortedOperation(T parameters) {
//todoremove          if(isDebugEnabled()) WseeWsatLogger.logAbortedOperationEntered(parameters);
        Xid xidFromWebServiceContextHeaderList = getXid();
//todoremove          if(isDebugEnabled()) WseeWsatLogger.logAbortedOperation(xidFromWebServiceContextHeaderList);
        getWSATHelper().setDurableParticipantStatus(xidFromWebServiceContextHeaderList, WSATXAResource.ABORTED);
 //todoremove         if(isDebugEnabled()) WseeWsatLogger.logAbortedOperationExited(parameters);
    }

    /**
     * ReadOnly response
     * Get Xid and update status in order to notify.
     * @param parameters Notification
     */
    public void readOnlyOperation(T parameters) {
//todoremove          if(isDebugEnabled()) WseeWsatLogger.logReadOnlyOperationEntered(parameters);
        Xid xidFromWebServiceContextHeaderList = getXid();
//todoremove          if(isDebugEnabled())  WseeWsatLogger.logReadOnlyOperation(xidFromWebServiceContextHeaderList);
        getWSATHelper().setDurableParticipantStatus(xidFromWebServiceContextHeaderList, WSATXAResource.READONLY);
 //todoremove         if(isDebugEnabled()) WseeWsatLogger.logReadOnlyOperationExited(parameters);
    }

    /**
     * Committed response
     * Get Xid and update status in order to notify.
     * @param parameters Notification
     */
    public void committedOperation(T parameters) {
//todoremove          if(isDebugEnabled()) WseeWsatLogger.logCommittedOperationEntered(parameters);
        Xid xidFromWebServiceContextHeaderList = getXid();
//todoremove          if(isDebugEnabled())  WseeWsatLogger.logCommittedOperation(xidFromWebServiceContextHeaderList);
        getWSATHelper().setDurableParticipantStatus(xidFromWebServiceContextHeaderList, WSATXAResource.COMMITTED);
 //todoremove         if(isDebugEnabled()) WseeWsatLogger.logCommittedOperationExited(parameters);
    }

    /**
     * WS-AT 1.0 recovery operation
     * Get Xid and issue replay
     * @param parameters  Notification
     */
    public void replayOperation(T parameters) {
//todoremove          if(isDebugEnabled()) WseeWsatLogger.logReplayOperationEntered(parameters);
        Xid xidFromWebServiceContextHeaderList = getXid();
        String wsatTid = getWSATHelper().getWSATTidFromWebServiceContextHeaderList(m_context);
//todoremove          if(isDebugEnabled())  WseeWsatLogger.logReplayOperation(xidFromWebServiceContextHeaderList);
        try {
            getTransactionServices().replayCompletion(
                    wsatTid, createWSATXAResourceForXidFromReplyTo(xidFromWebServiceContextHeaderList));
        } catch (WSATException e) {
 //todoremove             if(isDebugEnabled())  WseeWsatLogger.logReplayOperationSOAPException(xidFromWebServiceContextHeaderList, e);
            //there is no consequence, recovery reattempt should be (re)issued by subordinate
        }
//todoremove          if(isDebugEnabled()) WseeWsatLogger.logAbortedOperationExited(parameters);
    }

    /**
     * Return TransactionServicesImpl in order to issue replayCompletion
     * @return TransactionServices
     */
    protected TransactionServices getTransactionServices() {
        return WSATHelper.getTransactionServices();
    }

    /**
     * For recovery/replay create and return WSATResource from reply to
     * @param xid Xid
     * @return WSATXAResource
     */
     WSATXAResource createWSATXAResourceForXidFromReplyTo(Xid xid) {
        HeaderList headerList = (HeaderList) m_context.getMessageContext().get(
                        com.sun.xml.ws.developer.JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
        WSEndpointReference wsReplyTo = headerList.getReplyTo(AddressingVersion.W3C, SOAPVersion.SOAP_12);
        EndpointReference replyTo = wsReplyTo.toSpec();
        return new WSATXAResource(m_version.getVersion(), replyTo, xid, true);
    }

    /**
     * Get the Xid in the header from the WebServiceContext
     * @return Xid
     */
    Xid getXid() {
        Xid xid= getWSATHelper().getXidFromWebServiceContextHeaderList(m_context);
        String bqual = getWSATHelper().getBQualFromWebServiceContextHeaderList(m_context);
        return new XidImpl(xid.getFormatId(), xid.getGlobalTransactionId(), bqual.getBytes());
    }

    boolean isDebugEnabled() {
        return WSATHelper.isDebugEnabled();
    }

    /**
     * Return the WSATHelper singleton
     * @return WSATHelper for version
     */
    protected WSATHelper getWSATHelper() {
        return m_version.getWSATHelper();
    }

}
