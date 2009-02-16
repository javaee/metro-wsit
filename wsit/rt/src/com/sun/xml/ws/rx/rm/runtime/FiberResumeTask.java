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
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.rx.util.TimestampedCollection;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class FiberResumeTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(FiberResumeTask.class);

    private static class FiberRegistration {

        @NotNull private final Fiber fiber;
        private final Packet packet;

        FiberRegistration(Fiber fiber, Packet packet) {
            this.fiber = fiber;
            this.packet = packet;
        }
    }

    private final TimestampedCollection<Object, FiberRegistration> suspendedFibers = new TimestampedCollection<Object, FiberRegistration>();
    private final ClientSession session;

    public FiberResumeTask(ClientSession session) {
        this.session = session;
    }

    public void run() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(String.format("Periodic fiber resume task executed - suspended queue size: [ %d ]", suspendedFibers.size()));
        }
        while (!suspendedFibers.isEmpty() && expired(suspendedFibers.getOldestRegistrationTimestamp())) {
            FiberRegistration registration = suspendedFibers.removeOldest();

            registration.fiber.resume(session.appendOutgoingAcknowledgementHeaders(registration.packet));

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(String.format("Fiber %s resumed with packet%n%s", registration.fiber, registration.packet));
            }
        }
    }

    private final boolean expired(long resumeTime) {
        return System.currentTimeMillis() >= resumeTime;
    }

    /**
     * Registers data for a timed execution
     *
     * @param fiber a fiber to be resumed at a given {@code executionTime}.
     * @param packet a Packet to resume a given {@code fiber} with.
     * @param executionTime determines the time of execution for a given data
     *
     * @return {@code true} if the {@code request} has been successfully registered, {@code false} otherwise.
     */
    final boolean register(@NotNull Fiber fiber, Packet packet, long executionTime) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Fiber %s registered for resume with packet%n%s", fiber, packet));
        }
        return suspendedFibers.register(executionTime, new FiberRegistration(fiber, packet));
    }
}
