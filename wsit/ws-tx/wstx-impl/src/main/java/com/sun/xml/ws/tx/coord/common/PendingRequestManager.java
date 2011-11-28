/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.tx.coord.common;

import com.sun.xml.ws.tx.coord.common.types.BaseRegisterResponseType;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages; 

import javax.xml.ws.WebServiceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PendingRequestManager {
    private static final Logger LOGGER = Logger.getLogger(PendingRequestManager.class);
    static ConcurrentHashMap<String, ResponseBox> pendingRequests = new ConcurrentHashMap<String, ResponseBox>();

    public static ResponseBox reqisterRequest(String msgId) {
        ResponseBox box = new ResponseBox();
        pendingRequests.put(msgId, box);
        return box;
    }

    public static void removeRequest(String msgId) {
        pendingRequests.remove(msgId);
    }


    public static void registryReponse(String msgId, BaseRegisterResponseType repsonse) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(LocalizationMessages.WSAT_4620_GET_RESPONSE_REQUEST("\t" + msgId));
        }
        ResponseBox box = pendingRequests.remove(msgId);
        if (box != null) {
            box.put(repsonse);
        } else if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(LocalizationMessages.WSAT_4621_IGNORE_RESPONSE("\t" + msgId));
        }
    }

    static public class ResponseBox {
        private boolean isSet = false;
        private BaseRegisterResponseType type;

        ResponseBox() {

        }

        public synchronized void put(BaseRegisterResponseType type) {
            this.type = type;
            isSet = true;
            this.notify();
        }

        public synchronized BaseRegisterResponseType getResponse(long timeout) {
            /* A thread can also wake up without being notified, interrupted, or
            * timing out, a so-called <i>spurious wakeup</i>.  While this will rarely
            * occur in practice, applications must guard against it by testing for
            * the condition that should have caused the thread to be awakened, and
            * continuing to wait if the condition is not satisfied.  In other words,
            * waits should always occur in loops, like this one:
            */

            long start = System.currentTimeMillis();
            while (!isSet) {
                try {
                    wait(timeout);
                    long end = System.currentTimeMillis();
                    timeout = timeout - (end -start);
                    if(timeout<=0)
                       break;
                    else start = end;
                } catch (InterruptedException e) {
                    throw new WebServiceException(e);
                }
            }
            
            return type;
        }
    }

}
