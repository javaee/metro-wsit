/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.NamedThreadFactory;
import com.sun.xml.ws.rx.util.DelayedReference;
import java.util.concurrent.Callable;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class RedeliveryTask implements Callable<Integer> {

    public static interface DeliveryHandler {
        public void putToDeliveryQueue(ApplicationMessage message);
    }
    //
    private static final Logger LOGGER = Logger.getLogger(RedeliveryTask.class);
    private static final ExecutorService executorService = Executors.newCachedThreadPool(new NamedThreadFactory("redelivery-task-executor"));
    // TODO uncomment once this thread is singleton: private static final ExecutorService executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("redelivery-task-executor"));

    //
    private final DelayQueue<DelayedReference<ApplicationMessage>> scheduledMessages = new DelayQueue<DelayedReference<ApplicationMessage>>();
    private final DeliveryHandler deliveryHandler;
    private final AtomicBoolean isRunning;
    private final AtomicReference<Future<Integer>> runningStatusFutureReference;


    private RedeliveryTask(@NotNull DeliveryHandler deliveryHandler) {
        assert deliveryHandler != null;

        this.deliveryHandler = deliveryHandler;
        this.isRunning = new AtomicBoolean(false);
        this.runningStatusFutureReference = new AtomicReference<Future<Integer>>();
    }

    public static RedeliveryTask getInstance(@NotNull DeliveryHandler deliveryHandler) {
        // TODO make singleton - a single thread should be able to take care of
        // all redelivery awaiting messages from all endpoints / clients in a single VM
        return new RedeliveryTask(deliveryHandler);
    }

    /**
     * This method contains main task loop. It should not be called directly from outside.
     */
    public Integer call() throws InterruptedException {
        try {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest(String.format("Redelivery task executed - registered message queue size: [ %d ]", scheduledMessages.size()));
            }


            while (!scheduledMessages.isEmpty()) {
                DelayedReference<ApplicationMessage> delayedMessageReference = scheduledMessages.take();
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format("Putting to sequence [ %s ] delivery queue message with number [ %d ] ", delayedMessageReference.getData().getSequenceId(), delayedMessageReference.getData().getMessageNumber()));
                }
                deliveryHandler.putToDeliveryQueue(delayedMessageReference.getData());
            }

            isRunning.set(false);

            return 0;
        } catch (InterruptedException ex) {
            throw LOGGER.logException(ex, Level.CONFIG);
        } finally {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest(String.format("Redelivery task execution finished - registered message queue size: [ %d ]", scheduledMessages.size()));
            }            
        }

    }

    /**
     * Registers data for a timed execution
     *
     * @param request a packet to be resent at a given {@code executionTime}.
     * @param resendCounter number of the resend attempt for a given packet
     * @param executionTime determines the time of execution for a given {@code packet}
     *
     * @return {@code true} if the {@code request} has been successfully registered, {@code false} otherwise.
     */
    public final boolean register(@NotNull ApplicationMessage message, long delay, TimeUnit timeUnit) {

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format(
                    "A message with number [ %d ] has been scheduled for a redelivery on a sequence [ %s ] with a delay of %d %s",
                    message.getMessageNumber(),
                    message.getSequenceId(),
                    delay,
                    timeUnit.toString().toLowerCase()));
        }

        boolean offerResult = scheduledMessages.offer(new DelayedReference<ApplicationMessage>(message, delay, timeUnit));

        if (isRunning.compareAndSet(false, true)) {
            runningStatusFutureReference.set(RedeliveryTask.executorService.submit(this));
        }

        return offerResult;

    }

    /**
     * Stops execution of the redelivery task
     */
    public void stop() {
        runningStatusFutureReference.get().cancel(true);
    }
}
