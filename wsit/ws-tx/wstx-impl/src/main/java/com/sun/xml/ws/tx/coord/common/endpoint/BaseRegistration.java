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

package com.sun.xml.ws.tx.coord.common.endpoint;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages; 
import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.tx.at.*;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.tx.at.common.TransactionManagerImpl;
import com.sun.xml.ws.tx.coord.common.EndpointReferenceBuilder;
import com.sun.xml.ws.tx.coord.common.RegistrationIF;
import com.sun.xml.ws.tx.coord.common.WSCUtil;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.xml.ws.tx.coord.common.types.BaseRegisterType;
import java.util.logging.Level;
import javax.transaction.SystemException;

import javax.transaction.xa.Xid;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;


public abstract class BaseRegistration<T extends EndpointReference,K,P> implements RegistrationIF<T,K,P> {

    private static final Logger LOGGER = Logger.getLogger(BaseRegistration.class);
    private static final Class LOGGERCLASS = BaseRegistration.class;
    WebServiceContext context;
    Transactional.Version version;

    protected BaseRegistration(WebServiceContext context, Transactional.Version version) {
        this.context = context;
        this.version = version;
    }
    
    public BaseRegisterResponseType<T,P> registerOperation(BaseRegisterType<T,K> parameters) {
        if (WSATHelper.isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(
                    null,LOGGERCLASS, Level.INFO, "WSAT4504_REGISTER_OPERATION_ENTERED", parameters, null);
//            LOGGER.info(LocalizationMessages.WSAT_4504_REGISTER_OPERATION_ENTERED(parameters));
        String txId = WSATHelper.getInstance().getWSATTidFromWebServiceContextHeaderList(context);
        Xid xidFromWebServiceContextHeaderList = TransactionIdHelper.getInstance().wsatid2xid(txId);
        Xid xid = processRegisterTypeAndEnlist(parameters, xidFromWebServiceContextHeaderList);
        BaseRegisterResponseType<T,P> registerResponseType = createRegisterResponseType(xid);
        try {
            TransactionManagerImpl.getInstance().getTransactionManager().suspend();
        } catch (SystemException ex) {
            ex.printStackTrace();
            Logger.getLogger(BaseRegistration.class).log(Level.SEVERE, null, ex);
        }
        if (WSATHelper.isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(
                    null,LOGGERCLASS, Level.INFO, "WSAT4505_REGISTER_OPERATION_EXITED", registerResponseType, null);
//            LOGGER.info(LocalizationMessages.WSAT_4505_REGISTER_OPERATION_EXITED(registerResponseType));
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
     * @return Xid xid
     */
     Xid processRegisterTypeAndEnlist(BaseRegisterType<T,K> parameters, Xid xid) {
        if (parameters == null) throw new WebServiceException(
                "The message contained invalid parameters and could not be processed. " +
                        "Parameter argument for registration was null");
        String protocolIdentifier = parameters.getProtocolIdentifier();
        if(parameters.isDurable()) {
            return enlistResource(xid, parameters.getParticipantProtocolService());
        } else if(parameters.isVolatile()) {
            registerSynchronization(xid, parameters.getParticipantProtocolService());
            return null;
        } else {     
            WSATImplInjection.getInstance().getLogging().log(
                    null,LOGGERCLASS, Level.SEVERE, "WSAT4580_UNKNOWN_PARTICIPANT_IDENTIFIER", protocolIdentifier, null);
//            LOGGER.severe(LocalizationMessages.WSAT_4580_UNKNOWN_PARTICIPANT_IDENTIFIER(protocolIdentifier));
            throw new WebServiceException("Unknown participant identifier:"+protocolIdentifier);
        }

    }


    /**
     * Create the RegisterResponseType providing the address of the Coordinator and the provided Xid,
     * converted to WS-AT id format, and branchqual as a reference parameters.
     *
     * @param xid Xid
     * @return RegisterResponseType
     */
    BaseRegisterResponseType<T,P> createRegisterResponseType(Xid xid) {
        BaseRegisterResponseType<T,P> registerResponseType = newRegisterResponseType();
        String coordinatorHostAndPort = getCoordinatorAddress();
        String txId = TransactionIdHelper.getInstance().xid2wsatid(xid);
        String branchQual = new String(xid.getBranchQualifier());
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
     * @return Xid xid
     */
    private Xid enlistResource(Xid xid, T epr) {
        if (WSATHelper.isDebugEnabled()) 
            WSATImplInjection.getInstance().getLogging().log(
                    null,LOGGERCLASS, Level.INFO, "WSAT4503_ENLIST_RESOURCE", new Object[]{epr, xid}, null);
//        LOGGER.info(LocalizationMessages.WSAT_4503_ENLIST_RESOURCE(epr, xid));
        WSATXAResource wsatXAResource = new WSATXAResource(version,epr, xid);
        try {
            Xid xidFromEnlist = getTransactionServices().enlistResource(wsatXAResource, xid);
            wsatXAResource.setXid(xidFromEnlist);
            wsatXAResource.setBranchQualifier(xidFromEnlist.getBranchQualifier());
            return xidFromEnlist;
        } catch (WSATException e) {
            e.printStackTrace(); 
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
        LOGGER.info(LocalizationMessages.WSAT_4525_REGISTER_SYNCHRONIZATION(epr, xid));
        WSATSynchronization wsatXAResource = new WSATSynchronization(version, epr, xid);
        try {
            getTransactionServices().registerSynchronization(wsatXAResource, xid);
        } catch (WSATException e) {
            WSATImplInjection.getInstance().getLogging().log(
                    null,LOGGERCLASS, Level.SEVERE, "WSAT4507_EXCEPTION_DURING_REGISTER_SYNCHRONIZATION", null, e);
//            LOGGER.severe(LocalizationMessages.WSAT_4507_EXCEPTION_DURING_REGISTER_SYNCHRONIZATION(), e);
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

