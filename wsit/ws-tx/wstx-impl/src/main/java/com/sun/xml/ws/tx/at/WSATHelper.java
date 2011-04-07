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

package com.sun.xml.ws.tx.at;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages; 
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.tx.at.runtime.TransactionIdHelper;
import com.sun.xml.ws.tx.at.runtime.TransactionServices;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.tx.at.internal.BranchXidImpl;
import com.sun.xml.ws.tx.at.internal.TransactionServicesImpl;
import com.sun.xml.ws.tx.at.common.*;
import com.sun.xml.ws.tx.at.common.client.CoordinatorProxyBuilder;
import com.sun.xml.ws.tx.at.common.client.ParticipantProxyBuilder;
import com.sun.xml.ws.tx.at.internal.XidImpl;
import com.sun.xml.ws.tx.dev.WSATRuntimeConfig;

import java.util.logging.Level;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import javax.xml.soap.SOAPException;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.transaction.Transaction;

/**
 * This singleton serves not only as a helper and utility but as the core of the WS-AT resource manager and
 * holds the datastructures that maintain the endpoint references for durable/XAResource and volatile/Synchronization
 * WS-AT transaction participants.
 * Rather than translate back and forth between the  WS-AT and internal Xid, the  WS-AT could simply be used for
 * the keys in this class, however, we are working with Xids as keys and identity for convenience and
 * better debug logging information as there do not appear to be any substantial performance implications.
 */
public class WSATHelper<T> {


    private static final Class LOGGERCLASS = WSATHelper.class;
//    private static final Logger LOGGER = Logger.getLogger(WSATHelper.class);

    public final static WSATHelper V10 = new WSATHelper().WSATVersion(WSATVersion.v10);
    public final static WSATHelper V11 = new WSATHelper() {
        @Override
        public String getRegistrationCoordinatorAddress() {
            return getHostAndPort() + WSATConstants.WSAT11_REGISTRATIONCOORDINATORPORTTYPEPORT;
        }

        @Override
        public String getCoordinatorAddress() {
            return getHostAndPort() + WSATConstants.WSAT11_COORDINATORPORTTYPEPORT;
        }

        @Override
        public String getParticipantAddress() {
            return getHostAndPort() + WSATConstants.WSAT11_PARTICIPANTPORTTYPEPORT;
        }

        @Override
        public String getRegistrationRequesterAddress() {
            return getHostAndPort() + WSATConstants.WSAT11_REGISTRATIONREQUESTERPORTTYPEPORT;
            //throw new UnsupportedOperationException("Async registration is not supported by WS-AT since 1.1! ");
        }
    }.WSATVersion(WSATVersion.v11);

    //BranchXidImpl wrapper is used for caching mechanism as equals method considers branchqual where XidImpl equals method does/may not
    private Map<WSATXAResource, ParticipantIF<T>> m_durableParticipantPortMap = new HashMap<WSATXAResource, ParticipantIF<T>>();
    private final Object m_durableParticipantPortMapLock = new Object();
    private Map<Xid, WSATXAResource> m_durableParticipantXAResourceMap = new HashMap<Xid, WSATXAResource>();
 
    private final Object m_durableParticipantXAResourceMapLock = new Object();

    private Map<Xid, ParticipantIF<T>> m_volatileParticipantPortMap = new HashMap<Xid, ParticipantIF<T>>();
    private final Object m_volatileParticipantPortMapLock = new Object();
    private Map<Xid, WSATSynchronization> m_volatileParticipantSynchronizationMap = new HashMap<Xid, WSATSynchronization>();
    private final Object m_volatileParticipantSynchronizationMapLock = new Object();
    private final int m_waitForReplyTimeout =
            new Integer(System.getProperty("com.sun.xml.ws.tx.at.reply.timeout", "120"));
    private final boolean m_isUseLocalServerAddress =
            Boolean.valueOf(System.getProperty("com.sun.xml.ws.tx.at.use.local.server.address", "false"));
    protected WSATVersion<T> builderFactory;
    private Map<Xid, Transaction> m_xidToTransactionMap = new HashMap<Xid, Transaction>();

    WSATHelper WSATVersion(WSATVersion builderFactory) {
        this.builderFactory = builderFactory;
        return this;
    }

    protected WSATHelper (){
        // do nothing
    }

    
    public static WSATHelper getInstance() {
      return V10;
    }

    public static WSATHelper getInstance(Transactional.Version version) {
        if(version== Transactional.Version.WSAT10||version== Transactional.Version.DEFAULT)
           return V10;
        else if(version== Transactional.Version.WSAT12||version== Transactional.Version.WSAT11)
          return V11;
        throw new WebServiceException("not supported WSAT version");
    }



    /**
     * Return the TransactionServices
     * See interface for details...
     *
     * @return TransactionServices which interfaces WS-AT with underlying transaction processing system
     */
    public static TransactionServices getTransactionServices() {
        return TransactionServicesImpl.getInstance();
    }

    /**
     * Amount of time to wait for a reply from a prepare, rollback, commit, or beforeCompletion call before throwing
     * the appropriate exception, errorcode, etc.
     *
     * @return time in milliseconds
     */
    public int getWaitForReplyTimeout() {
        return m_waitForReplyTimeout * 1000;
    }

    /**
     * Called by Coordinator in order to update status and unblock async/one-way calls made for durable participants
     *
     * @param xid    XId
     * @param status String
     * @return boolean true if the status was set successfully
     */
    public boolean setDurableParticipantStatus(Xid xid, String status) {
        WSATXAResource wsatXAResourceLock;
        synchronized (m_durableParticipantXAResourceMapLock) {
            wsatXAResourceLock = getDurableParticipantXAResourceMap().get(new BranchXidImpl(xid));
        }
        if (wsatXAResourceLock == null) {
            return false;
        }
        synchronized (wsatXAResourceLock) {
            wsatXAResourceLock.setStatus(status);
            wsatXAResourceLock.notifyAll(); //if it's possible that more than one thread is waiting we want to notify all
            return true;
        }
    }

    /**
     * Called by Coordinator in order to update status and unblock async/one-way calls made for volatile participants
     *
     * @param xid    XId
     * @param status String
     * @return boolean true if the status was set successfully
     */
    boolean setVolatileParticipantStatus(Xid xid, String status) {
        WSATSynchronization wsatSynchronization;
        synchronized (m_volatileParticipantSynchronizationMapLock) {
            wsatSynchronization = m_volatileParticipantSynchronizationMap.get(xid);
        }
        if (wsatSynchronization == null) {
            if (isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                        "WSAT_4581_XID_NOT_IN_DURABLE_RESOURCE_MAP", new Object[]{xid, status}, null);
//                LOGGER.info(LocalizationMessages.WSAT_4581_XID_NOT_IN_DURABLE_RESOURCE_MAP(xid, status));
            return false;
        }
        synchronized (wsatSynchronization) {
            wsatSynchronization.setStatus(status);
            wsatSynchronization.notifyAll();
            return true;
        }
    }

    /**
     * Called by WSATXAResource in order to clear the cache/maps for this Xid
     *
     * @param wsatXAResource WSATXAResource
     */
    void removeDurableParticipant(WSATXAResource wsatXAResource) {
        synchronized (m_durableParticipantPortMapLock) {
            if (getDurableParticipantPortMap().containsKey(wsatXAResource)) {
                m_durableParticipantPortMap.remove(wsatXAResource);
                if (isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                            "WSAT_4583_DURABLE_PORT_REMOVED", wsatXAResource, null);
//              LOGGER.info(LocalizationMessages.WSAT_4583_DURABLE_PORT_REMOVED(wsatXAResource));
            }
        }
        synchronized (m_durableParticipantXAResourceMapLock) {
            if (getDurableParticipantXAResourceMap().containsKey(wsatXAResource.getXid())) {
                getDurableParticipantXAResourceMap().remove(wsatXAResource.getXid());
                if (isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                            "WSAT_4584_DURABLE_XARESOURCE_REMOVED", wsatXAResource, null);
//              LOGGER.info(LocalizationMessages.WSAT_4584_DURABLE_XARESOURCE_REMOVED(wsatXAResource));
            }
        }
    }

    /**
     * Called by WSATSynchronization in order to clear the cache/maps for this Xid
     *
     * @param xid Xid
     */
    void removeVolatileParticipant(Xid xid) {
        synchronized (m_volatileParticipantPortMapLock) {
            if (m_volatileParticipantPortMap.containsKey(new BranchXidImpl(xid))) {
                m_volatileParticipantPortMap.remove(new BranchXidImpl(xid));
                if (isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                            "WSAT_4585_VOLATILE_PORT_REMOVED", new BranchXidImpl(xid), null);
//              LOGGER.info(LocalizationMessages.WSAT_4585_VOLATILE_PORT_REMOVED(new BranchXidImpl(xid)));
            }
        }
        synchronized (m_volatileParticipantSynchronizationMapLock) {
            if (m_volatileParticipantSynchronizationMap.containsKey(new BranchXidImpl(xid))) {
                m_volatileParticipantSynchronizationMap.remove(new BranchXidImpl(xid));
                if (isDebugEnabled())
                    WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                            "WSAT_4586_VOLATILE_SYNCHRONIZATION_REMOVED", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4586_VOLATILE_SYNCHRONIZATION_REMOVED(xid));
            }
        }
    }

    /**
     * Get/create participant port and place it in the cache, issue prepare upon it, and place the WSATXAResource in the map.
     *
     * @param epr             EndpointReference participant endpoint reference
     * @param xid                 Xid of transaction as obtained from WSATXAResource
     * @param wsatXAResource      WSATXAResource
     * @throws XAException  xaException
     */
    public void prepare(EndpointReference epr, Xid xid,WSATXAResource wsatXAResource)
            throws XAException {
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4587_ABOUT_TO_SEND_PREPARE", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4587_ABOUT_TO_SEND_PREPARE(xid, Thread.currentThread()));
        synchronized (m_durableParticipantXAResourceMapLock) {
            putInDurableParticipantXAResourceMap(wsatXAResource, xid);
        }
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4589_DURABLE_PARTICIPANT_XARESOURCE_PLACED_IN_CACHE_FROM_PREPARE", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4589_DURABLE_PARTICIPANT_XARESOURCE_PLACED_IN_CACHE_FROM_PREPARE(xid));
        ParticipantIF<T> port = getDurableParticipantPort(epr, xid, wsatXAResource);
        T notification = builderFactory.newNotificationBuilder().build();
        port.prepare(notification);
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log( null, LOGGERCLASS, Level.INFO, "WSAT_4588_PREPARE_SENT",
                    new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4588_PREPARE_SENT(xid, Thread.currentThread()));
    }

    private void putInDurableParticipantXAResourceMap(WSATXAResource wsatXAResource, Xid xid) {
        //todo this is GF specific to strip trailing pad
        byte[] xidBqual = xid.getBranchQualifier();
        byte[] bqual  = new byte[xidBqual.length - 1];
        System.arraycopy(xidBqual, 0, bqual, 0, bqual.length);

        Xid xidImpl = new XidImpl(xid.getFormatId(), xid.getGlobalTransactionId(), bqual);
        BranchXidImpl branchXid = new BranchXidImpl(xidImpl);
        getDurableParticipantXAResourceMap().put(branchXid, wsatXAResource); //place in map first
    }

    /**
     * Unlike rollback, Xids are not added to the durable participant XAResource map during commit as prepare must always be
     * called in WS-AT (there is no onePhase commit) and prepare must add the Xid to the map.
     *
     * @param epr             EndpointReference participant endpoint reference
     * @param xid                 Xid of transaction as obtained from WSATXAResource
     * @param wsatXAResource      WSATXAResource
     * @throws XAException xaException
     */
    public void commit(EndpointReference epr, Xid xid,WSATXAResource wsatXAResource)
            throws XAException {
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4590_ABOUT_TO_SEND_COMMIT", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4590_ABOUT_TO_SEND_COMMIT(xid, Thread.currentThread()));
        T notification = builderFactory.newNotificationBuilder().build();
        getDurableParticipantPort(epr, xid, wsatXAResource).commit(notification);
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4591_COMMIT_SENT", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4591_COMMIT_SENT(xid, Thread.currentThread()));
    }

    /**
     * Rollback can be called before or after prepare so we could do a state check here to avoid the
     * redundant put in the latter case, but it is harmless to re-put and likely not a drastic performance concern.
     *
     * @param epr             EndpointReference participant endpoint reference
     * @param xid                 Xid of transaction as obtained from WSATXAResource
     * @param wsatXAResource      WSATXAResource
     * @throws XAException xaException
     */
    public void rollback(EndpointReference epr, Xid xid,WSATXAResource wsatXAResource)
            throws XAException {
      if (isDebugEnabled())
          WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                  "WSAT_4592_ABOUT_TO_SEND_ROLLBACK", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4592_ABOUT_TO_SEND_ROLLBACK(xid, Thread.currentThread()));
        synchronized (m_durableParticipantXAResourceMapLock) {
            putInDurableParticipantXAResourceMap(wsatXAResource, xid);
        }
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4594_ROLLBACK_PARTICIPANT_XARESOURCE_PLACED_IN_CACHE", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4594_ROLLBACK_PARTICIPANT_XARESOURCE_PLACED_IN_CACHE(xid));
        T notification = builderFactory.newNotificationBuilder().build();
        getDurableParticipantPort(epr, xid, wsatXAResource).rollback(notification); //place in map first
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4593_ROLLBACK_SENT", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4593_ROLLBACK_SENT(xid, Thread.currentThread()));
    }

    /**
     * beforeCompletion call on volatile participant
     *
     * @param epr             EndpointReference participant endpoint reference
     * @param xid                 Xid of transaction
     * @param wsatSynchronization WSATSynchronization
     * @throws SOAPException soapException
     */
    public void beforeCompletion(
            EndpointReference epr, Xid xid, WSATSynchronization wsatSynchronization)
            throws SOAPException {
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4595_ABOUT_TO_SEND_PREPARE_VOLATILE", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4595_ABOUT_TO_SEND_PREPARE_VOLATILE(
//            xid, Thread.currentThread()));
        T notification = builderFactory.newNotificationBuilder().build();
        getVolatileParticipantPort(epr, xid).prepare(notification);
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4596_PREPARE_VOLATILE_SENT", new Object[]{xid, Thread.currentThread()}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4596_PREPARE_VOLATILE_SENT(xid, Thread.currentThread()));
        synchronized (m_volatileParticipantSynchronizationMapLock) {
            m_volatileParticipantSynchronizationMap.put(new BranchXidImpl(xid), wsatSynchronization);
        }
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4597_PREPARE_PARTICIPANT_SYNCHRONIZATION_PLACED_IN_CACHE", xid, null);
//            LOGGER.info(LocalizationMessages.WSAT_4597_PREPARE_PARTICIPANT_SYNCHRONIZATION_PLACED_IN_CACHE(xid));
    }

    /**
     * Return volatile ParticipantPortType either from cache or created anew.  If created add to the cache.
     *
     * @param epr             EndpointReference participant endpoint reference
     * @param xid                 Xid of transaction
     * @return ParticipantPortType created
     * @throws SOAPException if there is any issue/SOAPException while creating the (communication) ParticipantPortType
     */
    private ParticipantIF<T> getVolatileParticipantPort(EndpointReference epr, Xid xid)
            throws SOAPException {
        ParticipantIF<T> participantPort;
        synchronized (m_volatileParticipantPortMapLock) {
            participantPort = m_volatileParticipantPortMap.get(new BranchXidImpl(xid));
        }
        if (participantPort != null) {
            if (isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                        "WSAT_4598_VOLATILE_PARTICIPANT_RETRIEVED_FROM_CACHE", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4598_VOLATILE_PARTICIPANT_RETRIEVED_FROM_CACHE(xid));
            return participantPort;
        }
        participantPort = getParticipantPort(epr, xid, null);
        synchronized (m_volatileParticipantPortMapLock) {
            m_volatileParticipantPortMap.put(new BranchXidImpl(xid), participantPort);
        }
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4599_VOLATILE_PARTICIPANT_PORT_PLACED_IN_CACHE", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4599_VOLATILE_PARTICIPANT_PORT_PLACED_IN_CACHE(xid));
        return participantPort;
    }

    /**
     * Return durable ParticipantPortType either from cache or created anew.  If created add to the cache.
     *
     * @param epr             EndpointReference  participant endpoint reference
     * @param xid                 Xid of transaction
     * @param wsatXAResource WSATXAResource
     * @return ParticipantPortType created
     * @throws XAException XAException.XAER_RMFAIL if there is any issue/SOAPException while creating the (communication) ParticipantPortType
     */
    private ParticipantIF<T> getDurableParticipantPort(EndpointReference epr, Xid xid, WSATXAResource wsatXAResource)
            throws XAException {
        ParticipantIF<T> participantPort;
        synchronized (m_durableParticipantPortMapLock) {
            participantPort = getDurableParticipantPortMap().get(wsatXAResource);
        }
        if (participantPort != null) {
            if (isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                        "WSAT_4600_DURABLE_PARTICIPANT_PORT_RETREIVED_FROM_CACHE", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4600_DURABLE_PARTICIPANT_PORT_RETREIVED_FROM_CACHE(xid));
            return participantPort;
        }
        try {
            participantPort = getParticipantPort(epr, xid, new String(wsatXAResource.getXid().getBranchQualifier()));
        } catch (SOAPException e) {
            if (isDebugEnabled())
                WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                        "WSAT_4601_CANNOT_CREATE_DURABLE_PARTICIPANT_PORT", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4601_CANNOT_CREATE_DURABLE_PARTICIPANT_PORT(xid));
            e.printStackTrace();
            XAException xaException = new XAException("Unable to create durable participant port:" + e);
            xaException.initCause(e);
            xaException.errorCode = XAException.XAER_RMFAIL;
            throw xaException;
        }
        synchronized (m_durableParticipantXAResourceMapLock) { //redundant for runtime case, required for recovery
            putInDurableParticipantXAResourceMap(wsatXAResource, xid);
        }
	    synchronized (m_durableParticipantPortMapLock) {
            getDurableParticipantPortMap().put(wsatXAResource, participantPort);
        }
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4602_DURABLE_PARTICIPANT_PORT_PLACED_IN_CACHE", xid, null);
//              LOGGER.info(LocalizationMessages.WSAT_4602_DURABLE_PARTICIPANT_PORT_PLACED_IN_CACHE(xid));
        return participantPort;
    }


    /**
     * Creates and returns a ParticipantPortType, whether it be durable or volatile, for the provided address, Xid,
     * and reference parameter Elements/Nodes
     *
     * @param epr             EndpointReference participant endpoint reference
     * @param xid                 Xid of transaction
     * @param bqual           String bqual of transaction
     * @return ParticipantPortType created
     * @throws SOAPException soapException
     */
    public ParticipantIF<T> getParticipantPort(EndpointReference epr, Xid xid, String bqual)
            throws SOAPException {
        String txId = TransactionIdHelper.getInstance().xid2wsatid(xid);
        ParticipantProxyBuilder<T> proxyBuilder = builderFactory.newParticipantProxyBuilder();
        ParticipantIF<T> participantProxyIF = proxyBuilder.to(epr).txIdForReference(txId, bqual).build();
        if (isDebugEnabled())
            WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO,
                    "WSAT_4603_SUCCESSFULLY_CREATED_PARTICIPANT_PORT", new Object[]{participantProxyIF, xid}, null);
//              LOGGER.info(LocalizationMessages.WSAT_4603_SUCCESSFULLY_CREATED_PARTICIPANT_PORT(participantProxyIF, xid));
        return participantProxyIF;
    }

    /**
     * Called from ForeignRecoveryContext.run
     * @param epr EndpointReference for to
     * @param xid Xid to find
     * @return CoordinatorIF Coordinator port for Xid
     */
    public CoordinatorIF<T> getCoordinatorPort(EndpointReference epr, Xid xid) {
        if (isDebugEnabled()) debug("WSATHelper.getCoordinatorPort xid:" + xid + " epr:" + epr);
        String txId = TransactionIdHelper.getInstance().xid2wsatid(xid);
        CoordinatorProxyBuilder<T> proxyBuilder = builderFactory.newCoordinatorProxyBuilder();
        CoordinatorIF<T> coordinatorProxy = proxyBuilder.to(epr).txIdForReference(txId, "").build();
        if (isDebugEnabled())
            debug("WSATHelper.getCoordinatorPort xid:" + xid + " epr:" + epr +
                    " coordinatorProxy:"+coordinatorProxy);
        return coordinatorProxy;
    }

    public String getRoutingAddress() {
        return "none";
    }

    /**
     * Return the host and port the WS-AT endpoints are deployed to or the frontend as the case may be
     * @return String URL with host and port  
     */
    static String getHostAndPort() {
        return WSATRuntimeConfig.getInstance().getHostAndPort();
    }

    public String getRegistrationCoordinatorAddress() {
        return getHostAndPort() + WSATConstants.WSAT_REGISTRATIONCOORDINATORPORTTYPEPORT;
    }

    public String getCoordinatorAddress() {
        return getHostAndPort() + WSATConstants.WSAT_COORDINATORPORTTYPEPORT;
    }

    public String getParticipantAddress() {
        return getHostAndPort() + WSATConstants.WSAT_PARTICIPANTPORTTYPEPORT;
    }

    public String getRegistrationRequesterAddress() {
        return getHostAndPort() + WSATConstants.WSAT_REGISTRATIONREQUESTERPORTTYPEPORT;
    }

    /**
     * Given a WebServiceContext extract and return the  WS-AT transaction id and return the translated Xid
     *
     * @param context WebServiceContext
     * @return WLXid found in WebServiceContext or fault
     */
    public Xid getXidFromWebServiceContextHeaderList(WebServiceContext context) {
        String txId = getWSATTidFromWebServiceContextHeaderList(context);
        return TransactionIdHelper.getInstance().wsatid2xid(txId);
    }

    /**
     * Used by getXidFromWebServiceContextHeaderList in WSATHelper and replayOperation of Coordinator service
     *
     * @param context WebServiceContext
     * @return WS-AT Txid String
     */
    public String getWSATTidFromWebServiceContextHeaderList(WebServiceContext context) {
        javax.xml.ws.handler.MessageContext messageContext = context.getMessageContext();
        HeaderList headerList =
                (HeaderList) messageContext.get(com.sun.xml.ws.developer.JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
        Iterator<Header> headers = headerList.getHeaders(WSATConstants.TXID_QNAME, false);
        if (!headers.hasNext()) {
            throw new WebServiceException("txid does not exist in header");
        }
        String txId = headers.next().getStringContent();
        return txId;
    }

    /**
     * Called by Coordinator to get/create Xid
     * @param context WebServiceContext
     * @return String bqual
     */
    public String getBQualFromWebServiceContextHeaderList(WebServiceContext context) {
        javax.xml.ws.handler.MessageContext messageContext = context.getMessageContext();
        HeaderList headerList =
                (HeaderList) messageContext.get(com.sun.xml.ws.developer.JAXWSProperties.INBOUND_HEADER_LIST_PROPERTY);
        Iterator<Header> headers = headerList.getHeaders(WSATConstants.BRANCHQUAL_QNAME, false);
        if (!headers.hasNext())
            throw new WebServiceException("branchqual does not exist in header"); //WSATFaultFactory.throwContextRefusedFault();
        String bqual = headers.next().getStringContent();
        if(bqual!=null) bqual = bqual.replaceAll("&#044;", ",");
        if (isDebugEnabled())
            debug("WSATHelper.getBQualFromWebServiceContextHeaderList returning bqual:" + bqual + " on thread:" + Thread.currentThread());
        return bqual;
    }

    /**
     * Need to check if debug is enabled before all logging to prevent unnecessary object creation.
     *
     * @return true if debug for the WS-AT logger is enabled, false otherwise
     */
    public static boolean isDebugEnabled() {
        return true;
    }

    public Map<WSATXAResource, ParticipantIF<T>>  getDurableParticipantPortMap() {
        return m_durableParticipantPortMap;
    }

    Map<Xid, WSATXAResource> getDurableParticipantXAResourceMap() {
        return m_durableParticipantXAResourceMap;
    }

    public Map<Xid, WSATSynchronization> getVolatileParticipantSynchronizationMap() {
        return m_volatileParticipantSynchronizationMap;
    }

    public Map<Xid, ParticipantIF<T>> getVolatileParticipantPortMap() {
        return m_volatileParticipantPortMap;
    }

    /**
     * Called by client side outbound tube where suspended tx is placed
     * @param xid
     * @param transaction
     */
    public void putToXidToTransactionMap(Xid xid, Transaction transaction) {
        m_xidToTransactionMap.put(new XidImpl(xid), transaction);
    }

    /**
     * Called by transactionservices enlistResource before calling wsatgatewayrm.enlist
     * @param xid Xid
     * @return Transaction associated with Xid
     */
    public Transaction getFromXidToTransactionMap(Xid xid) {
        Transaction transaction = m_xidToTransactionMap.get(xid);
        return transaction;
    }

    public void removeFromXidToTransactionMap(Xid xid) {
        m_xidToTransactionMap.remove(xid);
    }

    private void debug(String msg) {
        WSATImplInjection.getInstance().getLogging().log(null, LOGGERCLASS, Level.INFO, null, msg, null);
//        LOGGER.log(Level.INFO, msg);
    }

    public static String assignUUID(){
        return UUID.randomUUID().toString();
    }

}
