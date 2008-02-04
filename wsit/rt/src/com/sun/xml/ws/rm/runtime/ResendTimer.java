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

import com.sun.xml.ws.rm.localization.RmLogger;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class ResendTimer {

    private static final RmLogger LOGGER = RmLogger.getLogger(ResendTimer.class);
    private static final long DELAY = 2000;
    private static final long PERIOD = 2000;
    private Timer timer;
    private final WeakReference<ClientSession> sessionReference;

    /**
     *
     */
    public ResendTimer(ClientSession session) {
        this.sessionReference = new WeakReference<ClientSession>(session);
    }

    /**
     * Starts the resend timer
     */
    public synchronized void start() {
        if (timer != null) {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("ResendTimer is already running!"));
        }
        timer = new Timer(true);
        timer.schedule(new TimerTask() {

            public void run() {
                ClientSession cs = sessionReference.get();
                if (cs != null) {
                    cs.resend();
                } else {
                    stop();
                }
            }
        }, DELAY, PERIOD);
    }

    /**
     * Stops the resend timer
     */
    public synchronized void stop() {
        if (timer == null) {
            // TODO L10N
            throw LOGGER.logSevereException(new IllegalStateException("RetryTimer is not running currently!"));
        }
        timer.cancel();
        timer = null;
    }
}
