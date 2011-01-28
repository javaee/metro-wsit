/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.tx.at.tube;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.common.TransactionImportManager;
import com.sun.xml.ws.tx.at.internal.WSATGatewayRM;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.internal.XidImpl;
import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.internal.ForeignRecoveryContext;
import com.sun.xml.ws.tx.at.internal.ForeignRecoveryContextManager;
import com.sun.xml.ws.tx.at.WSATException;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.coord.common.CoordinationContextBuilder;
import com.sun.xml.ws.tx.coord.common.RegistrationIF;
import com.sun.xml.ws.tx.coord.common.WSCBuilderFactory;
import com.sun.xml.ws.tx.coord.common.client.RegistrationMessageBuilder;
import com.sun.xml.ws.tx.coord.common.client.RegistrationProxyBuilder;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterType;
import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;

public class WSATServerHelper implements WSATServer {
    private static final Logger LOGGER = Logger.getLogger(WSATServerHelper.class);
    Xid xidToResume; //todo should not rely on tube member vars, use context map instead

    public void doHandleRequest(HeaderList headers, TransactionalAttribute tx) {
        if (WSATHelper.isDebugEnabled())
            debug("processRequest HeaderList:" + headers + " TransactionalAttribute:" + tx + " isEnabled:" + tx.isEnabled());
        CoordinationContextBuilder ccBuilder = CoordinationContextBuilder.headers(headers, tx.getVersion());
        if (ccBuilder != null) {
            while(!WSATGatewayRM.isReadyForRuntime) {
                debug("WS-AT recovery is enabled but WS-AT is not ready for runtime.  Processing WS-AT recovery log files...");
                WSATGatewayRM.getInstance().recover();
            }
            xidToResume = processIncomingTransaction(ccBuilder);
        } else {
            if (tx.isRequired()) throw new WebServiceException("transaction context is required to be inflowed");
        }
    }

    public void doHandleResponse(TransactionalAttribute transactionalAttribute) {
        if(xidToResume!=null) {
            debug("doHandleResponse about to suspend " + xidToResume);
            TransactionImportManager.getInstance().release(xidToResume);
        }
    }

    public void doHandleException(Throwable throwable) {
        if(xidToResume!=null) {
            debug("doHandleException about to suspend " + xidToResume + " Exception:" + throwable);
            TransactionImportManager.getInstance().release(xidToResume);
        }
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
     * @param builder CoordinationContextBuilder
     */
    private Xid processIncomingTransaction(CoordinationContextBuilder builder) {
        if(WSATHelper.isDebugEnabled()) debug("in processingIncomingTransaction builder:"+builder);
        //we either need to fast suspend immediately and resume after register as we are doing or move this after register
        CoordinationContextIF cc = builder.buildFromHeader();
        long timeout = cc.getExpires().getValue();
        String tid = cc.getIdentifier().getValue().replace("urn:","").replaceAll("uuid:","");
        boolean isRegistered = false;
        Xid foreignXid = null; //serves as a boolean
        try {
          foreignXid = WSATHelper.getTransactionServices().importTransaction((int) timeout, tid.getBytes());
          if(foreignXid!=null) isRegistered = true;
          if(!isRegistered) {
              foreignXid = new XidImpl(tid.getBytes());
              register(builder, cc, foreignXid, timeout, tid);
          }
        } catch (Exception e) {
            if(foreignXid!=null) {
                TransactionImportManager.getInstance().release(foreignXid);
            } else {
                debug("in processingIncomingTransaction WSATException foreignXid is null");
            }
            throw new WebServiceException(e);
        }
        return foreignXid;
    }

    private void register(
            CoordinationContextBuilder builder, CoordinationContextIF cc,
            Xid foreignXid, long timeout, String participantId)
    {
        participantId = TransactionIdHelper.getInstance().xid2wsatid(foreignXid);
        Transactional.Version version = builder.getVersion();
        WSCBuilderFactory factory = WSCBuilderFactory.newInstance(version);
        RegistrationMessageBuilder rrBuilder = factory.newWSATRegistrationRequestBuilder();
        BaseRegisterType registerType = rrBuilder.durable(true).txId(participantId).routing().build();
        RegistrationProxyBuilder proxyBuilder = factory.newRegistrationProxyBuilder();
        proxyBuilder.
                to(cc.getRegistrationService()).
                txIdForReference(participantId).
                timeout(timeout);
        RegistrationIF proxyIF = proxyBuilder.build();
        BaseRegisterResponseType registerResponseType = proxyIF.registerOperation(registerType);
        if(WSATHelper.isDebugEnabled()) debug("Return from registerOperation call:"+registerResponseType);
        if (registerResponseType != null){
            EndpointReference epr = registerResponseType.getCoordinatorProtocolService();
            ForeignRecoveryContext frc =
                    ForeignRecoveryContextManager.getInstance().addAndGetForeignRecoveryContextForTidByteArray(
                            foreignXid);
            frc.setEndpointReference(epr,builder.getVersion());
            TransactionManagerImpl.getInstance().putResource(
                    WSATConstants.TXPROP_WSAT_FOREIGN_RECOVERY_CONTEXT, frc);
        } else {
            log("Sending fault. Context refused registerResponseType is null (this may be due to request timeout)");
            throw new WebServiceException(
                    "Sending fault. Context refused registerResponseType is null (this may be due to request timeout)");
        }
    }

    public void log(String message) {
        LOGGER.info(LocalizationMessages.WSAT_4612_WSAT_SERVERHELPER(message));
    }

    private void debug(String message) {
		LOGGER.info(message);
    }
}
