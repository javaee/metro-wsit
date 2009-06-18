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
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.util.TimeSynchronizer;
import com.sun.xml.ws.rx.util.TimestampedCollection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
final class RedeliveryTask implements Runnable {
    public static interface DeliveryHandler {
        public void putToDeliveryQueue(ApplicationMessage message);
    }

    private static final Logger LOGGER = Logger.getLogger(RedeliveryTask.class);
    //
    private final TimestampedCollection<Object, ApplicationMessage> scheduledMessages = TimestampedCollection.newInstance();
    private final @NotNull DeliveryHandler deliveryHandler;
    private final @NotNull TimeSynchronizer timeSynchronizer;

    RedeliveryTask(@NotNull DeliveryHandler deliveryHandler, @NotNull TimeSynchronizer timeSynchronizer) {
        assert deliveryHandler != null;
        assert timeSynchronizer != null;

        this.deliveryHandler = deliveryHandler;
        this.timeSynchronizer = timeSynchronizer;
    }

    public void run() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(String.format("Periodic request resend task executed - registered message queue size: [ %d ]", scheduledMessages.size()));
        }

        Queue<ApplicationMessage> readyForResendQueue = new LinkedList<ApplicationMessage>();
        while (!scheduledMessages.isEmpty() && expired(scheduledMessages.getOldestRegistrationTimestamp())) {
            readyForResendQueue.add(scheduledMessages.removeOldest());
        }

        for (ApplicationMessage message : readyForResendQueue) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.info(String.format("Pputting to sequence [ %s ] delivery queue message with number [ %d ] ", message.getSequenceId(), message.getMessageNumber()));
            }
            deliveryHandler.putToDeliveryQueue(message);
        }
    }

    private final boolean expired(long resumeTime) {
        return timeSynchronizer.currentTimeInMillis() >= resumeTime;
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
    final boolean register(@NotNull ApplicationMessage message, long executionTime) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("A message with number [ %d ] has been scheduled for a resend on a sequence [ %s ]", message.getMessageNumber(), message.getSequenceId()));
        }
        return scheduledMessages.register(executionTime, message);
    }
}
