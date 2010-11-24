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

package com.sun.xml.ws.tx.at.internal;

import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.common.CoordinatorIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.dev.WSATRuntimeConfig;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForeignRecoveryContextManager {

    private static final int REPLAY_TIMER_INTERVAL_MS =
            new Integer(System.getProperty("com.sun.xml.ws.tx.at.internal.indoubt.timeout.interval", "10000"));
    //It would be easy enough to base this off of transaction timeout, however, as a subordinate, we only have access to
    // the ttl which is not really a true indication of transaction timeout to go by.
    private static final int INDOUBT_TIMEOUT =
            new Integer(System.getProperty("com.sun.xml.ws.tx.at.internal.indoubt.timeout", "90000"));
    private static ForeignRecoveryContextManager singleton = new ForeignRecoveryContextManager();


    private Map<Xid, RecoveryContextWorker> recoveredContexts = new HashMap<Xid, RecoveryContextWorker>();

    private ForeignRecoveryContextManager() {
    }

    public static ForeignRecoveryContextManager getInstance() {
        return singleton;
    }

    /**
     * Called from WSATServerHelper.register to create registerResponseType
     * This should be a get, each time as we shouldn't be preparing a register call for the same
     *  server twice (this should be gated by "if (TransactionIdHelper.getInstance().getXid(tid.getBytes()) == null)"
     *
     * @param xid Xid the foreign xid of the imported transaction
     * @return ForeignRecoveryContext
     */
    public synchronized ForeignRecoveryContext addAndGetForeignRecoveryContextForTidByteArray(Xid xid) {
        RecoveryContextWorker rc = recoveredContexts.get(xid);
        if (rc != null) return rc.context;
        ForeignRecoveryContext frc = new ForeignRecoveryContext(xid);
        add(frc, false);
        return frc;
    }

    /**
     * Start this recovery thread
     */
    void start() {
        new Thread(new ContextRunnable()).start();
    }

    /**
     * Called when read from log for recovery
     * @param context ForeignRecoveryContext
     */
    synchronized void add(ForeignRecoveryContext context) {
        add(context, true);
    }

    /**
     * Called when read from log for recovery or from register
     * @param context ForeignRecoveryContext
     * @param isRecovery true if this is population from tx log, false if this is runtime register call prep
     */
    synchronized void add(ForeignRecoveryContext context, boolean isRecovery) {
        if(context==null) return;
        recoveredContexts.put(context.getXid(), new RecoveryContextWorker(context, isRecovery?-1:0));
    }

    synchronized void persist(Xid xid) {
        if (WSATRuntimeConfig.getInstance().isWSATRecoveryEnabled()) {
            ForeignRecoveryContextManager.RecoveryContextWorker contextWorker = recoveredContexts.get(xid);
            FileOutputStream fos = null;
            ObjectOutputStream out = null;
            try {
                fos = new FileOutputStream(
                        WSATGatewayRM.txlogdirInbound + File.separator +
                                xid.getGlobalTransactionId() + xid.getBranchQualifier()); //todo use time+counter here too
                out = new ObjectOutputStream(fos);
                out.writeObject(contextWorker);
                out.close();
                fos.flush();
            } catch (Throwable e) {
                throw new WebServiceException("Unable to persist log for inbound transaction Xid:" + xid, e);
            }
        }
    }

        //for testing only
    Map<Xid, RecoveryContextWorker> getRecoveredContexts() {
        return recoveredContexts;
    }

    public ForeignRecoveryContext getForeignRecoveryContext(Xid xid) {
        return recoveredContexts.get(xid).getContext();
    }

    /**
     * Called as part of deleteForeignState in TransactionServicesImpl after completion of rollback or commit
     * @param fxid Xid
     */
    synchronized void remove(Xid fxid) {
        recoveredContexts.remove(fxid);
    }

    private class ContextRunnable implements Runnable {

        public void run() {
           while(true) {
               doRun();
               try {
                   Thread.sleep(5 * 60 * 1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();  
               }
           }
        }

        public void doRun() {
            List<RecoveryContextWorker> replayList = new ArrayList<RecoveryContextWorker>();
            synchronized (ForeignRecoveryContextManager.this) {
                for (RecoveryContextWorker rc : recoveredContexts.values()) {
                    long lastReplay = rc.getLastReplayMillis();
                    if (lastReplay == -1) replayList.add(rc); // recovery
                    else {
                        try {
                            Transaction transaction = rc.context.getTransaction();
                            if (transaction != null && transaction.getStatus() == Status.STATUS_PREPARED) {
                                if (lastReplay == 0) rc.setLastReplayMillis(System.currentTimeMillis()); //runtime
                                replayList.add(rc);

                            }
                        } catch (Throwable e) { 
//todoremove                            debugWSAT.debug(
//todoremove                                    "ForeignRecoveryContextManager$ContextTimerListener.timerExpired error scheduling " +
//todoremove                                            "work for recovery context:" + rc.context +
//todoremove                                            " Exception getting transaction status, transaction may be null:" + e);
                        }
                    }
                }
            }
//todoremove            if (debugWSAT.isDebugEnabled() && !replayList.isEmpty()) {
//todoremove                debugWSAT.debug(
//todoremove                        "ForeignRecoveryContextManager$ContextTimerListener.timerExpired replayList.size():" +
//todoremove                                replayList.size());
//todoremove            }
            for (RecoveryContextWorker rc : replayList) {
                boolean isScheduled = rc.isScheduled();
                if (!isScheduled){
                    if((System.currentTimeMillis() - rc.getLastReplayMillis()) > INDOUBT_TIMEOUT * rc.getRetryCount())
                    {
                        rc.setScheduled(true);
                        rc.incrementRetryCount();
 //todoremove                       workManager.schedule(rc);
                    }

                }
            }
        }

    }

    private class RecoveryContextWorker { //todoremoveextends WorkAdapter {

        ForeignRecoveryContext context;
        long lastReplayMillis;
        boolean scheduled;

        /**
         * Only constructor
         * @param context ForeignRecoveryContext
         * @param lastReplay 0 if for runtime (to create register param), prevents add to replayList in
         *                    ContextTimerListener unless there is truly a long in-doubt tx
         *                  -1 if for recovery to add to add to replayList in ContextTimerListener immediately
         *                    for recovery call
         */
        RecoveryContextWorker(ForeignRecoveryContext context, int lastReplay) {
            this.context = context;
            this.lastReplayMillis = lastReplay;
        }

        synchronized long getLastReplayMillis() {
            return lastReplayMillis;
        }

        /**
         * Called during ContextTimerListener processing to indicate a non-zero value thereby indicating the tx is
         *  in prepared/in-doubt state and eligible for bottom-up queries
         * @param lastReplayMillis long
         */
        synchronized void setLastReplayMillis(long lastReplayMillis) {
            this.lastReplayMillis = lastReplayMillis;
        }

        synchronized boolean isScheduled() {
            return scheduled;
        }

        synchronized void setScheduled(boolean b) {
            this.scheduled = b;
        }

        public void run() {
            try {
                Xid xid = context.getXid();
                if (xid == null) {
//todoremove                    if (debugWSAT.isDebugEnabled()) {
//todoremove                        debugWSAT.debug("no Xid mapping for recovered context " + context);
//todoremove                    }
                    return;
                }
//todoremove                if (debugWSAT.isDebugEnabled()) debugWSAT.debug("about to send Prepared recovery call for " + context);
                CoordinatorIF coordinatorPort =
                        WSATHelper.getInstance(context.getVersion()).getCoordinatorPort(
                                context.getEndpointReference(), xid);
//todoremove                if (debugWSAT.isDebugEnabled())
//todoremove                    debugWSAT.debug(
//todoremove                            "About to send Prepared recovery call for " + context +
//todoremove                                    " with coordinatorPort:" + coordinatorPort);
                Object notification = WSATVersion.getInstance(context.getVersion()).newNotificationBuilder().build();
                Transaction transaction = context.getTransaction();
                if (transaction != null && transaction.getStatus() == Status.STATUS_PREPARED)
                    coordinatorPort.preparedOperation(notification);
//todoremove                if (debugWSAT.isDebugEnabled())
//todoremove                    debugWSAT.debug("Prepared recovery call for " + context + " returned successfully");
            } catch (Throwable e) {
//todoremove                debugWSAT.debug("Prepared recovery call error for " + context, e);
            } finally {
                synchronized (this) {
                    scheduled = false;
                    lastReplayMillis = System.currentTimeMillis();
                }
            }
        }

        //the following is provided to backoff bottom up requests exponentially...

        private int retryCount = 1;
        void incrementRetryCount() {  // this is weak but does the job and insures no overflow
            if (retryCount * 2 * INDOUBT_TIMEOUT < Integer.MAX_VALUE / 3) retryCount *= 2;
//todoremove                if (debugWSAT.isDebugEnabled())
//todoremove                    debugWSAT.debug("Next recovery call for " + context + " in:"+ retryCount * INDOUBT_TIMEOUT);
        }

        int getRetryCount() {
            return retryCount;
        }

        ForeignRecoveryContext getContext() {
            return context;
        }
    }



}
