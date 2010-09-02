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

import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.internal.ForeignRecoveryContext;
import com.sun.xml.ws.tx.at.internal.ForeignRecoveryContextManager;
import com.sun.xml.ws.tx.at.WSATException;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.api.Transactional;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.coord.common.CoordinationContextBuilder;
import com.sun.xml.ws.tx.coord.common.RegistrationIF;
import com.sun.xml.ws.tx.coord.common.WSCBuilderFactory;
import com.sun.xml.ws.tx.coord.common.client.RegistrationMessageBuilder;
import com.sun.xml.ws.tx.coord.common.client.RegistrationProxyBuilder;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterType;
import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;

import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;

public class WSATServerHelper implements WSATServer {
    public void doHandleRequest(HeaderList headers, TransactionalAttribute tx) {
        if(WSATHelper.isDebugEnabled())
            debug("processRequest HeaderList:"+headers+
                    " TransactionalAttribute:"+tx+ " isEnabled:"+tx.isEnabled());
        if (tx.isEnabled()) {
            CoordinationContextBuilder ccBuilder =
                    CoordinationContextBuilder.headers(headers,tx.getVersion());
            if(ccBuilder != null) {
                processIncomingTransaction(headers, ccBuilder);
            } else {
                if(tx.isRequired()) throw new WebServiceException("transaction context is required to be inflowed");
            }
        }
    }
    
    public void doHandleResponse(TransactionalAttribute transactionalAttribute) {
        if (transactionalAttribute!=null && transactionalAttribute.isEnabled()) {
//todoremove             if(WSATHelper.isDebugEnabled()) debug("processResponse isTransactionalAnnotationPresent about to suspend");
//todoremove             javax.transaction.Transaction suspendedTx =
//todoremove                     TransactionHelper.getTransactionHelper().getTransactionManager().forceSuspend();
//todoremove             if(WSATHelper.isDebugEnabled()) debug("processResponse suspend was successful tx:" + suspendedTx);
        }
    }

    public void doHandleException(Throwable throwable) {
//todoremove             if(WSATHelper.isDebugEnabled()) debug("processException about to suspend if transaction is present due to:"+ throwable);
//todoremove             javax.transaction.Transaction suspendedTx =
//todoremove                     TransactionHelper.getTransactionHelper().getTransactionManager().forceSuspend();
//todoremove             if(WSATHelper.isDebugEnabled()) debug("processException suspend was successful tx:" + suspendedTx);
    }
    
    /**
     * builder can not be null.
     * //ref params
     * //"Identifier in registerOperation is null" wscoor:InvalidState if omitted
     * ReferenceParameters referenceParameters = registrationCoordinatorEndpointReference.getReferenceParameters();
     * List<Object> list = referenceParameters.getElements();
     * for (Object aList : list) header.addChildElement((SOAPElement) aList);
     * //Request messages
     * //    MUST include a wsa:MessageID header.
     * //    MUST include a wsa:ReplyTo header.
     */
    private void processIncomingTransaction(
            HeaderList headers, CoordinationContextBuilder builder) {
        if(WSATHelper.isDebugEnabled())debug("in processingIncomingTransaction");
        //we either need to fast suspend immediately and resume after register as we are doing or move this after register
        CoordinationContextIF cc = builder.buildFromHeader();
        long timeout = cc.getExpires().getValue();
        String tid = cc.getIdentifier().getValue().replace("urn:","").replaceAll("uuid:","");
        boolean isRegistered = false; //TransactionIdHelper.getInstance().getXid(tid.getBytes()) == null;
        try { //todo if foreignXid is not null then notRegisterred should be false
          Xid foreignXid = 
                  WSATHelper.getTransactionServices().importTransaction(
                  (int) timeout, tid.getBytes());
          if(foreignXid!=null) isRegistered = true;
          if(!isRegistered) {
              register(headers, builder, cc, foreignXid, timeout);
          }
        } catch (WSATException e) {
            throw new WebServiceException(e);
        }
    }

    private void register(
            HeaderList headers, CoordinationContextBuilder builder, CoordinationContextIF cc, Xid foreignXid, long timeout)
    {
       String participantId = TransactionIdHelper.getInstance().xid2wsatid(foreignXid);
       Transactional.Version version = builder.getVersion();
        WSCBuilderFactory factory = WSCBuilderFactory.newInstance(version);
        RegistrationMessageBuilder rrBuilder = factory.newWSATRegistrationRequestBuilder();
        BaseRegisterType registerType = rrBuilder.durable(true).txId(participantId).routing().build();
 //todoremove        if(WSATHelper.isDebugEnabled())debug("About to suspend tx before registerOperation call");
//todoremove         javax.transaction.Transaction suspendedTx =
//todoremove                 TransactionHelper.getTransactionHelper().getTransactionManager().forceSuspend();
//todoremove         if(WSATHelper.isDebugEnabled())debug("Suspend was successful for tx:" + suspendedTx);
        RegistrationProxyBuilder proxyBuilder = factory.newRegistrationProxyBuilder();
        proxyBuilder.
                to(cc.getRegistrationService()).
                txIdForReference(participantId).
                timeout(timeout);
        RegistrationIF proxyIF = proxyBuilder.build();
//todoremove         if(WSATHelper.isDebugEnabled())
//todoremove             debug("Before registerOperation call, suspend was successful for tx:" + suspendedTx +
//todoremove                             " registration service proxy:"+proxyIF);
        BaseRegisterResponseType registerResponseType = proxyIF.registerOperation(registerType);
        if(WSATHelper.isDebugEnabled())debug("Return from registerOperation call:"+registerResponseType);
        if (registerResponseType != null){
            EndpointReference epr = registerResponseType.getCoordinatorProtocolService();
            ForeignRecoveryContext frc =
                    ForeignRecoveryContextManager.getInstance().addAndGetForeignRecoveryContextForTidByteArray(
                            foreignXid);
            frc.setEndpointReference(epr,builder.getVersion());
            TransactionManagerImpl.getInstance().putResource(
                    WSATConstants.TXPROP_WSAT_FOREIGN_RECOVERY_CONTEXT, frc);
        } else {
            log("Sending fault. Context refused registerResponseType is null");
            throw new WebServiceException("Sending fault. Context refused registerResponseType is null");
//        todo    WSATFaultFactory.throwContextRefusedFault();
        }
//todoremove         TransactionHelper.getTransactionHelper().getTransactionManager().forceResume(suspendedTx);
//todoremove         if(WSATHelper.isDebugEnabled()) debug("Returned from registerOperation call resumed tx:" + suspendedTx);
    }

    public void log(String message) {
//todoremove         WseeWsatLogger.logWSATServerHelper("WSATServerInterceptor:" + message);
    }

    private void debug(String message) {
		WSATHelper.getInstance().debug("WSATServerInterceptor:" + message);
    }
}
