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

package com.sun.xml.ws.rx.rm.runtime.delivery;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.ha.HaContext;
import com.sun.xml.ws.rx.rm.runtime.ApplicationMessage;
import com.sun.xml.ws.rx.rm.runtime.RuntimeContext;

import java.util.concurrent.Executor;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public final class Postman {

    private static final Logger LOGGER = Logger.getLogger(Postman.class);

    public static interface Callback {

        /**
         * Implementation of this method is responsible for processing RM data in a
         * protocol dependent way and delivering the application message
         * using underlying message transport and processing framework
         *
         * @param message
         */
        public void deliver(ApplicationMessage message);
        
        public RuntimeContext getRuntimeContext();
    }

    private final Executor executor;

    Postman() {
        // In-line Executor runs the task in the caller's thread
        // (so as to prevent thread hopping)
        executor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }
    
    public void deliver(final ApplicationMessage message, final Callback deliveryCallback) {
        final HaContext.State state = HaContext.currentState();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format(
                    "Scheduling delivery execution of a message with number [ %d ] on a sequence [ %s ] "
                    + "using current HA context state [ %s ]",
                    message.getMessageNumber(),
                    message.getSequenceId(),
                    state.toString()));
        }

        executor.execute(new Runnable()  {

            public void run() {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer(String.format(
                            "Executing delivery of a message with number [ %d ] on a sequence [ %s ]",
                            message.getMessageNumber(),
                            message.getSequenceId()));
                }

                final HaContext.State oldState = HaContext.initFrom(state);

                try {
                    deliveryCallback.deliver(message);
                } finally {
                    HaContext.initFrom(oldState);
                }
            }
        });
    }
}
