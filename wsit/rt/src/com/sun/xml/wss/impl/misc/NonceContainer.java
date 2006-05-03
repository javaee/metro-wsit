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

import java.util.Timer;
import java.util.Hashtable;
import java.util.Enumeration;

public class NonceContainer {


    //stores applicationId Vs NonceCache pairs
    static Hashtable nonceTable = new Hashtable();

    public static boolean validateAndCacheNonce(
        String applicationId, String nonce, String created, long maxNonceAge) {

        NonceCache cache = (NonceCache)nonceTable.get(applicationId);

        if ((cache == null) || (cache.wasCanceled()))
            cache = initNonceCache(applicationId, maxNonceAge);

        setNonceCacheCleanup(cache);
        return cache.validateAndCacheNonce(nonce, created);
    }

    private static synchronized NonceCache initNonceCache(
        String applicationId, long maxNonceAge) {

        NonceCache cache = (NonceCache)nonceTable.get(applicationId);

        if (cache == null) {
            if (maxNonceAge == 0)
                cache = new NonceCache();
            else 
                cache = new NonceCache(maxNonceAge);
            //log.log(Level.FINE, "Creating NonceCache for first time....." + cache);
            nonceTable.put(applicationId, cache);
        } else if (cache.wasCanceled()) {
            if (maxNonceAge == 0)
                cache = new NonceCache(); 
            else 
                cache = new NonceCache(maxNonceAge);
            //log.log(Level.FINE, "Re-creating NonceCache because it was canceled....." + nonceCache);
            nonceTable.put(applicationId, cache);
        }

        return cache;
    }

    private static synchronized void setNonceCacheCleanup(NonceCache nonceCache) {

        if (!nonceCache.isScheduled()) {
            //log.log(Level.FINE, "Scheduling Nonce Reclaimer task...... for " + this + ":" + nonceCache);
             DefaultSecurityEnvironmentImpl.nonceCleanupTimer.schedule(
                    nonceCache,
                    nonceCache.getMaxNonceAge(), // run it the first time after
                    nonceCache.getMaxNonceAge()); //repeat every
             nonceCache.scheduled(true);
        }
    }

    /**
     * Management method to cleanup an entry in NonceContainer
     */
    public static synchronized void cleanup(String applicationId) {
        NonceCache cache = (NonceCache)nonceTable.get(applicationId);
        if (cache != null) {
            cache.cancel();   
            nonceTable.remove(applicationId);
        }
    }

    /**
     * Management method to cleanup the NonceContainer
     */
    public static synchronized void cleanup() {
        // cancel all timer tasks
        Enumeration keys = nonceTable.keys();

        while (keys.hasMoreElements()) {
            String appId = (String)keys.nextElement();
            NonceCache cache = (NonceCache)nonceTable.get(appId);

            if (cache != null) {
                cache.cancel();   
            }
        }
        nonceTable.clear();
    }
}
