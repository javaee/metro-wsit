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
package com.sun.xml.ws.rx.mc.runtime;

import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.Packet;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.ScheduledTaskManager;
import com.sun.xml.ws.rx.RxRuntimeException;
import com.sun.xml.ws.rx.mc.localization.LocalizationMessages;
import com.sun.xml.ws.rx.mc.protocol.wsmc200702.MakeConnectionElement;
import com.sun.xml.ws.rx.mc.dev.ProtocolMessageHandler;
import com.sun.xml.ws.rx.util.Communicator;
import com.sun.xml.ws.rx.util.SuspendedFiberStorage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class MakeConnectionSenderTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(MakeConnectionSenderTask.class);
    //
    private final String wsmcAnonymousAddress;
    private final Header wsmcAnnonymousReplyToHeader;
    private final Header wsmcAnnonymousFaultToHeader;
    private long lastMcMessageTimestamp;
    private final AtomicBoolean isMcRequestPending;
    private int scheduledMcRequestCounter;
    private final McConfiguration configuration;
    private final Communicator communicator;
    private final SuspendedFiberStorage suspendedFiberStorage;
    private final Map<String, ProtocolMessageHandler> mapOfRegisteredProtocolMessageHandlers;
    //
    private final ScheduledTaskManager scheduler;
    private final AtomicBoolean isRunning;
    private final AtomicBoolean wasShutdown;

    MakeConnectionSenderTask(
            final Communicator communicator,
            final SuspendedFiberStorage suspendedFiberStorage,
            final String wsmcAnonymousAddress,
            final Header wsmcAnnonymousReplyToHeader,
            final Header wsmcAnnonymousFaultToHeader,
            final McConfiguration configuration) {
        this.communicator = communicator;
        this.suspendedFiberStorage = suspendedFiberStorage;
        this.wsmcAnonymousAddress = wsmcAnonymousAddress;
        this.wsmcAnnonymousReplyToHeader = wsmcAnnonymousReplyToHeader;
        this.wsmcAnnonymousFaultToHeader = wsmcAnnonymousFaultToHeader;
        this.configuration = configuration;
        this.mapOfRegisteredProtocolMessageHandlers = new HashMap<String, ProtocolMessageHandler>();

        this.lastMcMessageTimestamp = System.currentTimeMillis();
        this.isMcRequestPending = new AtomicBoolean(false);
        this.scheduledMcRequestCounter = 0;

        this.scheduler = new ScheduledTaskManager("MakeConnectionSenderTask");
        this.isRunning = new AtomicBoolean(false);
        this.wasShutdown = new AtomicBoolean(false);
    }

    /**
     * This task can only be started once and then shut down. It cannot be stopped. Once it has been shut down, it cannot be restarted.
     */
    public void start() {
        if (wasShutdown.get()) {
            throw new IllegalStateException("This task instance has already been shut down in the past.");
        }

        if (isRunning.compareAndSet(false, true)) {
            // TODO P2 make it configurable
            this.scheduler.startTask(this, 2000, 500);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean wasShutdown() {
        return wasShutdown.get();
    }

    public void shutdown() {
        if (isRunning.compareAndSet(true, false) && wasShutdown.compareAndSet(false, true)) {
            this.scheduler.shutdown();
        }
    }

    /**
     * This method is resumed periodicaly by a Timer. First, it checks if ALL of the following conditions
     * are satisfied:
     * <ul>
     *   <li>There is no MakeConnection request already pending</li>
     *   <li>A preconfigured interval has passed since last MakeConnection request</li>
     *   <li>There are suspended fibers waiting for a response or there are pending MC
     *       requests that were scheduled programatically via {@link #scheduleMcRequest()} method</li>
     * </ul>
     * If all the above conditions are astisfied a new MakeConnection request is sent. If not,
     * method terminates without any further action.
     */
    public synchronized void run() {
        if (!isMcRequestPending.get() && resendMakeConnectionIntervalPassed() && (scheduledMcRequestCounter > 0 || suspendedFibersReadyForResend())) {
            sendMcRequest();
        }
    }

    private boolean suspendedFibersReadyForResend() {
        // TODO P3 enable exponential backoff algorithm
        while (!suspendedFiberStorage.isEmpty()) {
            final long oldestRegistrationAge = System.currentTimeMillis() - suspendedFiberStorage.getOldestRegistrationTimestamp();

            if (oldestRegistrationAge > configuration.getFeature().getResponseRetrievalTimeout()) {
                suspendedFiberStorage.removeOldest().resume(new RxRuntimeException(LocalizationMessages.WSMC_0123_RESPONSE_RETRIEVAL_TIMED_OUT()));
            } else {
                return oldestRegistrationAge > configuration.getFeature().getBaseMakeConnectionRequetsInterval();
            }
        }

        return false;
    }

    private synchronized boolean resendMakeConnectionIntervalPassed() {
        // TODO P3 enable exponential backoff algorithm
        return System.currentTimeMillis() - lastMcMessageTimestamp > configuration.getFeature().getBaseMakeConnectionRequetsInterval();
    }

    synchronized void register(ProtocolMessageHandler handler) {
        for (String wsaAction : handler.getSuportedWsaActions()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(String.format(
                        "Registering ProtocolMessageHandler of class [ %s ] to process WS-A action [ %s ]",
                        handler.getClass().getName(),
                        wsaAction));
            }

            final ProtocolMessageHandler oldHandler = mapOfRegisteredProtocolMessageHandlers.put(wsaAction, handler);

            if (oldHandler != null && LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning(LocalizationMessages.WSMC_0101_DUPLICATE_PROTOCOL_MESSAGE_HANDLER(
                        wsaAction,
                        oldHandler.getClass().getName(),
                        handler.getClass().getName()));
            }
        }
    }

    synchronized void scheduleMcRequest() {
        scheduledMcRequestCounter++;
    }

    private void sendMcRequest() {
        Packet mcRequest = communicator.createRequestPacket(new MakeConnectionElement(wsmcAnonymousAddress), configuration.getRuntimeVersion().protocolVersion.wsmcAction, true);
        McClientTube.setMcAnnonymousHeaders(
                mcRequest.getMessage().getHeaders(),
                configuration.getAddressingVersion(),
                wsmcAnnonymousReplyToHeader,
                wsmcAnnonymousFaultToHeader);

        isMcRequestPending.set(true);
        try {
            communicator.sendAsync(mcRequest, new WsMcResponseHandler(configuration, this, suspendedFiberStorage, mapOfRegisteredProtocolMessageHandlers));
        } finally {
            lastMcMessageTimestamp = System.currentTimeMillis();
            if (--scheduledMcRequestCounter < 0) {
                scheduledMcRequestCounter = 0;
            }
        }
    }

    synchronized void clearMcRequestPendingFlag() {
        isMcRequestPending.set(false);
    }
}
