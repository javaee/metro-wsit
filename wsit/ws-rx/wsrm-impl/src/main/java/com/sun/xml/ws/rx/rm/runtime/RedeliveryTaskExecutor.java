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

import com.sun.xml.ws.commons.DelayedTaskManager;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.ha.HaContext;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
enum RedeliveryTaskExecutor {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(RedeliveryTaskExecutor.class);

    private final DelayedTaskManager delayedTaskManager;

    private RedeliveryTaskExecutor() {
        this.delayedTaskManager = DelayedTaskManager.createManager("redelivery-task-executor", 5);
    }

    public boolean register(final ApplicationMessage message, long delay, TimeUnit timeUnit, final MessageHandler messageHandler) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("A message with number [ %d ] has been scheduled for a redelivery on a sequence [ %s ] with a delay of %d %s", message.getMessageNumber(), message.getSequenceId(), delay, timeUnit.toString().toLowerCase()));
        }

        final HaContext.State state = HaContext.currentState();

        return delayedTaskManager.register(new DelayedTaskManager.DelayedTask() {

            public void run(DelayedTaskManager manager) {
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
