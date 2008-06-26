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
package com.sun.xml.ws.rm.runtime;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.rm.localization.LocalizationMessages;
import com.sun.xml.ws.rm.localization.RmLogger;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
final class PeriodicFiberResumeTask implements Runnable {

    private static final RmLogger LOGGER = RmLogger.getLogger(PeriodicFiberResumeTask.class);
    
    private static class FiberRegistration {

        private final long timestamp;
        @NotNull final Fiber fiber;
        final Packet packet;

        FiberRegistration(Fiber fiber, Packet packet) {
            this.timestamp = System.currentTimeMillis();
            this.fiber = fiber;
            this.packet = packet;
        }

        boolean expired(long period) {
            return System.currentTimeMillis() - timestamp >= period;
        }
    }
    private final Queue<FiberRegistration> fiberResumeQueue = new ConcurrentLinkedQueue<FiberRegistration>();
    private long resumePeriod;

    PeriodicFiberResumeTask(long period) {
        super();
        this.resumePeriod = period;
    }

    public void run() {
        while (!fiberResumeQueue.isEmpty() && fiberResumeQueue.peek().expired(resumePeriod)) {
            FiberRegistration registration = fiberResumeQueue.poll();            
            registration.fiber.resume(registration.packet);
            if (LOGGER.isLoggable(Level.FINER)) {                
                LOGGER.finer(LocalizationMessages.WSRM_1127_FIBER_RESUMED(registration.fiber.toString(), registration.packet.toString()));
            }
        }
    }

    /**
     * Registers given fiber for a resume
     * 
     * @param fiber a fiber to be resumed after preconfigured interval has passed
     * @return {@code true} if the fiber was successfully registered; {@code false} otherwise.
     */
    final boolean registerForResume(@NotNull Fiber fiber, Packet packet) {
        return fiberResumeQueue.offer(new FiberRegistration(fiber, packet));
    }
}
