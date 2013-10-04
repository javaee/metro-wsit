/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.rx.rm.runtime;

import com.sun.xml.ws.api.Component;
import com.sun.xml.ws.commons.DelayedTaskManager;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.ha.HaContext;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
class RedeliveryTaskExecutor {
    private static final Logger LOGGER = Logger.getLogger(RedeliveryTaskExecutor.class);
    private static volatile DelayedTaskManager delayedTaskManager;

    private RedeliveryTaskExecutor() {
    }

    // This method delivers message using caller's thread. No thread switching.
    public static boolean deliverUsingCurrentThread(
            final ApplicationMessage message, long delay, TimeUnit timeUnit,
            final MessageHandler messageHandler) {

        try {
            Thread.sleep(timeUnit.toMillis(delay));
        } catch (InterruptedException e) {
            //ignore and redeliver
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format(
                    "Attempting redelivery of a message with number [ %d ] on a sequence [ %s ]",
                    message.getMessageNumber(),
                    message.getSequenceId()));
        }

        messageHandler.putToDeliveryQueue(message);
        return true;
    }
    
    // Not used anymore in favor of deliverUsingCurrentThread.
    @Deprecated
    public static boolean register(final ApplicationMessage message, long delay, TimeUnit timeUnit, final MessageHandler messageHandler, Component container) {
        final HaContext.State state = HaContext.currentState();

        if (delayedTaskManager == null) {
            synchronized(RedeliveryTaskExecutor.class) {
                if (delayedTaskManager == null) {
                    delayedTaskManager = DelayedTaskManager.createManager("redelivery-task-executor", 5, container);
                }
            }
        }
        
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format(
                    "A message with number [ %d ] has been scheduled for a redelivery "
                    + "on a sequence [ %s ] with a delay of %d %s "
                    + "using current HA context state [ %s ]",
                    message.getMessageNumber(),
                    message.getSequenceId(),
                    delay,
                    timeUnit.toString().toLowerCase(),
                    state.toString()));
        }

        return delayedTaskManager.register(new DelayedTaskManager.DelayedTask()   {

            public void run(DelayedTaskManager manager) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format(
                            "Attempting redelivery of a message with number [ %d ] on a sequence [ %s ]",
                            message.getMessageNumber(),
                            message.getSequenceId()));
                }
                final HaContext.State oldState = HaContext.initFrom(state);
                try {
                    messageHandler.putToDeliveryQueue(message);
                } finally {
                    HaContext.initFrom(oldState);
                }
            }

            public String getName() {
                return String.format("redelivery of a message with number [ %d ] on a sequenece [ %s ]", message.getMessageNumber(), message.getSequenceId());
            }
        }, delay, timeUnit);
    }
}
