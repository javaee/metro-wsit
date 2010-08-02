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
package com.sun.xml.ws.tx.coord.common.endpoint;

import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.*;
import com.sun.xml.ws.tx.at.api.Transactional;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;
import com.sun.xml.ws.tx.coord.common.RegistrationIF;
import com.sun.xml.ws.tx.coord.common.WSCUtil;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterType;

import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;


public abstract class BaseRegistration<T extends EndpointReference,K,P> implements RegistrationIF<T,K,P> {

    WebServiceContext context;
    Transactional.Version version;

    protected BaseRegistration(WebServiceContext context, Transactional.Version version) {
        this.context = context;
        this.version = version;
    }
    public BaseRegisterResponseType<T,P> registerOperation(BaseRegisterType<T,K> parameters) {
 //todoremove        if (WSATHelper.isDebugEnabled())
 //todoremove            WseeWsatLogger.logRegisterOperationEntered(parameters);
        String txId = WSATHelper.getInstance().getWSATTidFromWebServiceContextHeaderList(context);
        Xid xidFromWebServiceContextHeaderList = TransactionIdHelper.getInstance().wsatid2xid(txId);
 //todoremove        checkIssuedTokenAtApplicationLevel(txId);
        byte[] bqual = processRegisterTypeAndEnlist(parameters, xidFromWebServiceContextHeaderList);
        BaseRegisterResponseType<T,P> registerResponseType =
                createRegisterResponseType(xidFromWebServiceContextHeaderList, bqual);
    //todoremove     if (WSATHelper.isDebugEnabled())
     //todoremove        WseeWsatLogger.logRegisterOperationExited(registerResponseType);
        return registerResponseType;
    }


    /**
     * Extract Participant EndpointReferenceType from RegisterType.
     * Obtain ReferenceParametersType from EndpointReferenceType.
     * Convert ReferenceParametersType to Node array so that it is Serializable.
     * Call enlistResource in order to create WSATXAResource and register Participant for durable participant
     * or call registerSynchronization in order to create WSATSynchronization and register Participant for
     * volatile participant
     *
     * @param parameters RegisterType
     * @param xid        Xid
     */
     byte[] processRegisterTypeAndEnlist(BaseRegisterType<T,K> parameters, Xid xid) {
        if (parameters == null) WSATFaultFactory.throwInvalidParametersFault();
        String protocolIdentifier = parameters.getProtocolIdentifier();
        if(parameters.isDurable()) {
            return enlistResource(xid, parameters.getParticipantProtocolService());
        } else if(parameters.isVolatile()) {
            registerSynchronization(xid, parameters.getParticipantProtocolService());
            return null;
        } else {
//todoremove             WseeWsatLogger.logUnknownParticipantIdentifier(protocolIdentifier);
            throw new WebServiceException("unknown participant identifier:"+protocolIdentifier);
//   todo         WSATFaultFactory.throwContextRefusedFault();
//            return null; //this is actually unreachable
        }

    }


    /**
     * Create the RegisterResponseType providing the address of the Coordinator and the provided Xid,
     * converted to WS-AT id format, and branchqual as a reference parameters.
     *
     * @param xid Xid
     * @return RegisterResponseType
     */
    BaseRegisterResponseType<T,P> createRegisterResponseType(Xid xid, byte[] bqual) {
        BaseRegisterResponseType<T,P> registerResponseType = newRegisterResponseType();
        String coordinatorHostAndPort = getCoordinatorAddress();
        String txId = TransactionIdHelper.getInstance().xid2wsatid(xid);
        String branchQual = new String(bqual);
        EndpointReferenceBuilder<T> builder = getEndpointReferenceBuilder();
        T endpointReference =
                builder.address(coordinatorHostAndPort).referenceParameter(
                        WSCUtil.referenceElementTxId(txId),
                        WSCUtil.referenceElementBranchQual(branchQual),
                                WSCUtil.referenceElementRoutingInfo()).build();
        registerResponseType.setCoordinatorProtocolService(endpointReference);
        return registerResponseType;
    }

    /**
     * Called by Registration service to
     * Log enlistment.
     * Create Serializable WSATXAResource.
     * Set branch qualifier on WSATXAResource
     * Return branchqual in order to createRegisterResponseType
     *
     * @param xid              Xid
     * @param epr              EndpointReferenceType obtained from RegisterType parameters provided to registerOperation
     */
    private byte[] enlistResource(Xid xid, T epr) {
//todoremove         if (WSATHelper.isDebugEnabled())
//todoremove             WseeWsatLogger.logEnlistResource(epr, xid);
        WSATXAResource wsatXAResource = new WSATXAResource(version,epr, xid);
        try {
            byte bqual[] = getTransactionServices().enlistResource(wsatXAResource, xid);
            wsatXAResource.setBranchQualifier(bqual);
            return bqual;
        } catch (WSATException e) {
            e.printStackTrace();  //todo logging
            throw new WebServiceException(e);
        }
    }

    /**
     * Log and register Synchronization
     *
     * @param xid                 Xid
     * @param epr
     *                            EndpointReferenceType
     */
    private void registerSynchronization(Xid xid, T epr) {
//todoremove         WseeWsatLogger.logRegisterSynchronization(epr, xid);
        WSATSynchronization wsatXAResource = new WSATSynchronization(version, epr, xid);
        try {
            getTransactionServices().registerSynchronization(wsatXAResource, xid);
        } catch (WSATException e) {
//todoremove             WseeWsatLogger.logExceptionDuringRegisterSynchronization(e);
            WSATFaultFactory.throwContextRefusedFault();
        }


    }

    protected abstract EndpointReferenceBuilder<T> getEndpointReferenceBuilder();
    protected abstract BaseRegisterResponseType<T,P> newRegisterResponseType();
    protected abstract String getCoordinatorAddress();

    protected TransactionServices getTransactionServices() {
     return WSATHelper.getTransactionServices();
   }
}

