/*
 * $Id: NonceCache.java,v 1.1 2006-05-03 22:57:51 arungupta Exp $
 */
                                                                                                                   
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.wss.impl.misc;

import java.util.Hashtable;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;


/*
 * This class holds a Nonce Cache and is a TimerTask
 */
public class NonceCache extends TimerTask {

     /** logger */
    protected static Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

     // Nonce Cache
    private Hashtable nonceCache = new Hashtable();
    private Hashtable oldNonceCache = new Hashtable();

    // default
    private long MAX_NONCE_AGE = 900000 ;

    // flag to indicate if this timertask is scheduled into the Timer queue
    private boolean scheduledFlag = false;
    private boolean canceledFlag = false;

    public NonceCache() {}
   
    public NonceCache(long maxNonceAge) {
        MAX_NONCE_AGE = maxNonceAge;
    }

    public boolean validateAndCacheNonce(String nonce, String created) {
        if (nonceCache.containsKey(nonce)|| oldNonceCache.containsKey(nonce)) {
            log.log(Level.INFO, 
                    "Nonce Cache already contains the new Nonce Value received :" + nonce);
            return false;
        }

        if (MessageConstants.debug)
            log.log(Level.FINE, "Storing Nonce Value " + nonce  + " into " + this);

        nonceCache.put(nonce, created);
        return true;
    }

    public boolean isScheduled() {
        return scheduledFlag;
    }

    public void scheduled(boolean flag) {
        scheduledFlag = flag;
    }

    public boolean wasCanceled() {
        return canceledFlag;
    }

    public void run() {

        if (nonceCache.size() == 0) {
            boolean canceled = cancel();
            if (MessageConstants.debug)
                log.log(Level.FINE, "Canceled Timer Task due to inactivity ...for " + this); 
            return;
        }

        if (MessageConstants.debug)
            log.log(Level.FINE, "Clearing old Nonce values...for " + this);

        oldNonceCache.clear();
        Hashtable temp = nonceCache;
        nonceCache = oldNonceCache;
        oldNonceCache = temp;
    }

    public boolean cancel() {
        boolean ret = super.cancel();
        canceledFlag = true;
        oldNonceCache.clear();
        nonceCache.clear();

        return ret;
    }

    public long getMaxNonceAge() {
        return MAX_NONCE_AGE;
    }

}

