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
package com.sun.xml.ws.rx.rm.runtime.sequence;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.commons.DelayedTaskManager;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class SequenceMaintenanceTask implements DelayedTaskManager.DelayedTask {

    private static final Logger LOGGER = Logger.getLogger(SequenceMaintenanceTask.class);
    private final WeakReference<SequenceManager> smReference;
    private final long period;
    private final TimeUnit timeUnit;
    private final String endpointUid;

    public SequenceMaintenanceTask(@NotNull SequenceManager sequenceManager, long period, @NotNull TimeUnit timeUnit) {
        assert sequenceManager != null;
        assert period > 0;
        assert timeUnit != null;

        this.smReference = new WeakReference<SequenceManager>(sequenceManager);
        this.period = period;
        this.timeUnit = timeUnit;
        this.endpointUid = sequenceManager.uniqueEndpointId();
    }

    public void run(DelayedTaskManager manager) {
        SequenceManager sequenceManager = smReference.get();
        if (sequenceManager != null) {
            sequenceManager.onMaintenance();

            if (!manager.isClosed()) {
                boolean registrationSuccesfull = manager.register(this, period, timeUnit);

                if (!registrationSuccesfull) {
                    LOGGER.config(LocalizationMessages.WSRM_1150_UNABLE_TO_RESCHEDULE_SEQUENCE_MAINTENANCE_TASK(endpointUid));
                }
            }
        } else {
            LOGGER.config(LocalizationMessages.WSRM_1151_TERMINATING_SEQUENCE_MAINTENANCE_TASK(endpointUid));
        }
    }

    public String getName() {
        return "sequence maintenance task";
    }
}
