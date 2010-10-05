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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.xml.wss.impl.misc;

import com.sun.xml.ws.api.ha.HighAvailabilityProvider;
import com.sun.xml.wss.NonceManager;
import com.sun.xml.wss.logging.LogStringsMessages;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;

/**
 *
 * @author suresh
 */
public class HANonceManager extends NonceManager {

    private Long maxNonceAge;
    private BackingStore<String, HAPojo> backingStore = null;
    private final ScheduledExecutorService singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public HANonceManager(final long maxNonceAge) {
        this.maxNonceAge = maxNonceAge;

        try {
            final BackingStoreConfiguration<String, HANonceManager.HAPojo> bsConfig = HighAvailabilityProvider.INSTANCE.initBackingStoreConfiguration("HANonceManagerStore", String.class, HANonceManager.HAPojo.class);
            //maxNonceAge is in milliseconds so convert it into seconds
            bsConfig.getVendorSpecificSettings().put("max.idle.timeout.in.seconds", maxNonceAge / 1000L);
            bsConfig.setClassLoader(ClassLoader.getSystemClassLoader());
            //not sure whether this statement required or not ?
            bsConfig.getVendorSpecificSettings().put(BackingStoreConfiguration.START_GMS, true);
            final BackingStoreFactory bsFactory = HighAvailabilityProvider.INSTANCE.getBackingStoreFactory(HighAvailabilityProvider.StoreType.IN_MEMORY);
            backingStore = bsFactory.createBackingStore(bsConfig);
            //System.out.println("conf is : " + bsConfig);
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        singleThreadScheduledExecutor.scheduleAtFixedRate(new nonceCleanupTask(), maxNonceAge, maxNonceAge, TimeUnit.MILLISECONDS);
    }

    public HANonceManager(BackingStore<String, HAPojo> backingStore, final long maxNonceAge) {
        this.backingStore = backingStore;
        this.maxNonceAge = maxNonceAge;      

        singleThreadScheduledExecutor.scheduleAtFixedRate(new nonceCleanupTask(), maxNonceAge, maxNonceAge, TimeUnit.MILLISECONDS);
    }


    @Override
    public boolean validateNonce(String nonce, String created) throws NonceException {
        byte[] data = created.getBytes();
        HAPojo pojo = new HAPojo();
        pojo.setData(data);
        try {
            HAPojo value = null;
            try {
                value = HighAvailabilityProvider.INSTANCE.loadFrom(backingStore, nonce, null);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, " exception during load command ");
            }
            if (value != null) {
                final String message = "Nonce Repeated : Nonce Cache already contains the nonce value :" + nonce;
                LOGGER.log(Level.WARNING, LogStringsMessages.WSS_0815_NONCE_REPEATED_ERROR(nonce));
                throw new NonceManager.NonceException(message);
            } else {
                HighAvailabilityProvider.INSTANCE.saveTo(backingStore, nonce, pojo, true);
                LOGGER.log(Level.INFO, " nonce " + nonce + " saved ");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    //return this.validateNonce(nonce, pojo);
    }

    public class nonceCleanupTask implements Runnable {

        public void run() {
            try {
                int removed = backingStore.removeExpired(maxNonceAge);
                System.out.println("removed no. of entries = " + removed);                
            } catch (BackingStoreException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void remove(String key) throws BackingStoreException{
        backingStore.remove(key);
    }

    static public class HAPojo implements Serializable {

        byte[] data;

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return this.data;
        }
    }
}
