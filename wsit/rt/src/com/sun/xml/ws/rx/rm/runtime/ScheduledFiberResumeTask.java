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
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class ScheduledFiberResumeTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ScheduledFiberResumeTask.class);

    private static class FiberRegistration {

        private final long timestamp;
        @NotNull private final Fiber fiber;
        private final Packet packet;
        private final long resumeTime;

        FiberRegistration(Fiber fiber, Packet packet, long resumeTime) {
            this.timestamp = System.currentTimeMillis();
            this.fiber = fiber;
            this.packet = packet;
            this.resumeTime = resumeTime;
        }

        boolean expired() {
            return System.currentTimeMillis() >= resumeTime;
        }
    }
    private final Queue<FiberRegistration> fiberResumeQueue = new PriorityBlockingQueue<FiberRegistration>(10, new Comparator<FiberRegistration> () {

        public int compare(FiberRegistration fr1, FiberRegistration fr2) {
            return (fr1.resumeTime < fr2.resumeTime) ? -1 : (fr1.resumeTime == fr2.resumeTime) ? 0 : 1;
        }

    });

    ScheduledFiberResumeTask() {
        super();
    }

    public void run() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(String.format("Periodic fiber task executed - resume queue size: [ %d ]", fiberResumeQueue.size()));
        }
        while (!fiberResumeQueue.isEmpty() && fiberResumeQueue.peek().expired()) {
            FiberRegistration registration = fiberResumeQueue.poll();
            registration.fiber.resume(registration.packet);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(String.format("Fiber %s resumed with packet%n%s", registration.fiber, registration.packet));
            }
        }
    }

    /**
     * Registers given fiber for a resume
     * 
     * @param fiber a fiber to be resumed after preconfigured interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    final boolean registerForResume(@NotNull Fiber fiber, Packet packet, long resumeTime) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer(String.format("Fiber %s registered for resume with packet%n%s", fiber, packet));
        }
        return fiberResumeQueue.offer(new FiberRegistration(fiber, packet, resumeTime));
    }
}
