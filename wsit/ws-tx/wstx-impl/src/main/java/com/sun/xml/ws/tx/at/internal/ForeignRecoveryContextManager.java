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

package com.sun.xml.ws.tx.at.internal;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.WSATHelper;
import com.sun.xml.ws.tx.at.common.CoordinatorIF;
import com.sun.xml.ws.tx.at.common.WSATVersion;
import com.sun.xml.ws.tx.dev.WSATRuntimeConfig;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.Xid;
import javax.xml.ws.WebServiceException;
import java.io.*;
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
    volatile int counter;
    private static final Logger LOGGER_ContextRunnable = Logger.getLogger(ContextRunnable.class);
    private static final Logger LOGGER_RecoveryContextWorker = Logger.getLogger(RecoveryContextWorker.class);

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
            ForeignRecoveryContext contextWorker = recoveredContexts.get(xid).getContext();
            FileOutputStream fos;
            ObjectOutputStream out;
            try {
                String logLocation =
                        WSATGatewayRM.txlogdirInbound + File.separator + System.currentTimeMillis() + "-" + counter++;
                contextWorker.setTxLogLocation(logLocation);
                fos = new FileOutputStream(logLocation);
                out = new ObjectOutputStream(fos);
                out.writeObject(contextWorker);
                out.close();
                fos.flush();
            } catch (Throwable e) {
                throw new WebServiceException("Unable to persist log for inbound transaction Xid:" + xid, e);
            }
        }
    }

    public void delete(XidImpl xid) {
        if (WSATRuntimeConfig.getInstance().isWSATRecoveryEnabled()) {
            ForeignRecoveryContext contextWorker = recoveredContexts.get(xid).getContext();
            String logLocation = contextWorker.getTxLogLocation();
            try {
                new File(logLocation).delete();
            } catch (Throwable e) {
                LOGGER_RecoveryContextWorker.warning("Unable to delete WS-AT log file:" + logLocation);
            }
        }
    }

        //for testing only
    Map<Xid, RecoveryContextWorker> getRecoveredContexts() {
        return recoveredContexts;
    }

    public ForeignRecoveryContext getForeignRecoveryContext(Xid xid) {
        ForeignRecoveryContextManager.RecoveryContextWorker recoveryContextWorker = recoveredContexts.get(xid);
        return recoveryContextWorker==null?null:recoveryContextWorker.getContext();
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
                            if (isEligibleForBottomUpQuery(rc, transaction)) {
                                if (lastReplay == 0) rc.setLastReplayMillis(System.currentTimeMillis()); //runtime
                                replayList.add(rc);

                            }
                        } catch (Throwable e) { 
                            debug(
                                    "ForeignRecoveryContextManager$ContextTimerListener.timerExpired error scheduling " +
                                            "work for recovery context:" + rc.context +
                                            " Exception getting transaction status, transaction may be null:" + e);
                        }
                    }
                }
            }
            if (!replayList.isEmpty()) {
                debug(
                        "ForeignRecoveryContextManager$ContextTimerListener.timerExpired replayList.size():" +
                                replayList.size());
            }
            for (RecoveryContextWorker rc : replayList) {
                boolean isScheduled = rc.isScheduled();
                if (!isScheduled){
                    if((System.currentTimeMillis() - rc.getLastReplayMillis()) > INDOUBT_TIMEOUT * rc.getRetryCount())
                    {
                        rc.setScheduled(true);
                        rc.incrementRetryCount();
                        new Thread(rc).start();//todo use workManager.schedule(rc);
                    }

                }
            }
        }

        boolean isEligibleForBottomUpQuery(RecoveryContextWorker rc, Transaction transaction) throws SystemException {
            return rc.context.isRecovered() ||
                    (transaction != null && transaction.getStatus() == Status.STATUS_PREPARED);
        }

        private void debug (String message) {
            LOGGER_ContextRunnable.info(message);
        }

    }

    private class RecoveryContextWorker implements Runnable {

        ForeignRecoveryContext context;
        long lastReplayMillis;
        boolean scheduled;
        private int retryCount = 1;

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
                    debug("no Xid mapping for recovered context " + context);
                    return;
                }
                debug("about to send Prepared recovery call for " + context);
                CoordinatorIF coordinatorPort =
                        WSATHelper.getInstance(context.getVersion()).getCoordinatorPort(
                                context.getEndpointReference(), xid);
                debug("About to send Prepared recovery call for " + context + " with coordinatorPort:" + coordinatorPort);
                Object notification = WSATVersion.getInstance(context.getVersion()).newNotificationBuilder().build();
                Transaction transaction = context.getTransaction();
                if (isEligibleForBottomUpQuery(this, transaction)) coordinatorPort.preparedOperation(notification);
                debug("Prepared recovery call for " + context + " returned successfully");
            } catch (Throwable e) {
                debug("Prepared recovery call error for " + context + " exception:" + e);
            } finally {
                synchronized (this) {
                    scheduled = false;
                    lastReplayMillis = System.currentTimeMillis();
                }
            }
        }

        boolean isEligibleForBottomUpQuery(RecoveryContextWorker rc, Transaction transaction) throws SystemException {
            return rc.context.isRecovered() ||
                    (transaction != null && transaction.getStatus() == Status.STATUS_PREPARED);
        }

        //the following is provided to backoff bottom up requests exponentially...
        void incrementRetryCount() {  // this is weak but does the job and insures no overflow
            if (retryCount * 2 * INDOUBT_TIMEOUT < Integer.MAX_VALUE / 3) retryCount *= 2;
            debug("Next recovery call for " + context + " in:" + retryCount * INDOUBT_TIMEOUT + "ms");
        }

        int getRetryCount() {
            return retryCount;
        }

        ForeignRecoveryContext getContext() {
            return context;
        }

        private void debug (String message) {
            LOGGER_RecoveryContextWorker.info(message);
        }
    }



}
