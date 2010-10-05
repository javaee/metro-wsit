/*
 * $Id: NonceCache.java,v 1.1 2010-10-05 11:42:09 m_potociar Exp $
 */
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.wss.impl.misc;

import com.sun.xml.wss.NonceManager;
import com.sun.xml.wss.NonceManager.NonceException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/*
 * This class holds a Nonce Cache and is a TimerTask
 */
@ManagedData
public class NonceCache extends TimerTask {

    /** logger */
    protected static final Logger log =
            Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
    // Nonce Cache
    private Map<String, String> nonceCache = Collections.synchronizedMap(new HashMap<String, String>());
    private Map<String, String> oldNonceCache = Collections.synchronizedMap(new HashMap<String, String>());

    @ManagedAttribute // Only for monitoring
    private Map<String, String> getNonceCache() {
        return nonceCache;
    }

    @ManagedAttribute // Only for monitoring
    private Map<String, String> getOldNonceCache() {
        return oldNonceCache;
    }
    // default
    private long MAX_NONCE_AGE = MessageConstants.MAX_NONCE_AGE;
    // flag to indicate if this timertask is scheduled into the Timer queue
    private boolean scheduledFlag = false;
    private boolean canceledFlag = false;

    public NonceCache() {
    }

    public NonceCache(long maxNonceAge) {
        MAX_NONCE_AGE = maxNonceAge;
    }

    @SuppressWarnings("unchecked")
    public boolean validateAndCacheNonce(String nonce, String created) throws NonceException {
        if (nonceCache.containsKey(nonce) || oldNonceCache.containsKey(nonce)) {            
            log.log(Level.WARNING, LogStringsMessages.WSS_0815_NONCE_REPEATED_ERROR(nonce));
            throw new NonceManager.NonceException(LogStringsMessages.WSS_0815_NONCE_REPEATED_ERROR(nonce));
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Storing Nonce Value {0} into {1}", new Object[]{nonce, this});
        }

        nonceCache.put(nonce, created);
        return true;
    }

    @ManagedAttribute
    public boolean isScheduled() {
        return scheduledFlag;
    }

    public void scheduled(boolean flag) {
        scheduledFlag = flag;
    }

    @ManagedAttribute
    public boolean wasCanceled() {
        return canceledFlag;
    }

    public void run() {

        if (nonceCache.isEmpty()) {
            cancel();
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Canceled Timer Task due to inactivity ...for {0}", this);
            }
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Clearing old Nonce values...for {0}", this);
        }

        oldNonceCache.clear();
        Map<String, String> temp = nonceCache;
        nonceCache = oldNonceCache;
        oldNonceCache = temp;
    }

    @Override
    public boolean cancel() {
        boolean ret = super.cancel();
        canceledFlag = true;
        oldNonceCache.clear();
        nonceCache.clear();

        return ret;
    }

    @ManagedAttribute
    public long getMaxNonceAge() {
        return MAX_NONCE_AGE;
    }
}
