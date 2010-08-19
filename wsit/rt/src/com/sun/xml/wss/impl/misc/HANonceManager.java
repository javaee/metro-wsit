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

import com.sun.xml.wss.NonceManager;
import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreException;

/**
 *
 * @author suresh
 */
public class HANonceManager extends NonceManager {

    private Long maxNonceAge;
    private BackingStore<String, MyPojo> backingStore = null;
    private final ScheduledExecutorService singleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();    

    public HANonceManager(BackingStore<String, MyPojo> backingStore, final long maxNonceAge) {
        this.backingStore = backingStore;
        this.maxNonceAge = maxNonceAge;
        singleThreadScheduledExecutor.scheduleAtFixedRate(new HANM(), maxNonceAge, maxNonceAge, TimeUnit.MILLISECONDS);

    }

    public boolean validateNonce(String nonce, MyPojo created) throws NonceException {
        try {          
            MyPojo value = null;
            try {
                value = backingStore.load(nonce, null);
            } catch (Exception ex) {
            }
            if (value != null) {
                final String message = "Nonce Repeated : Nonce Cache already contains the nonce value :" + nonce;
                LOGGER.log(Level.WARNING, message);
                throw new NonceManager.NonceException(message);
            } else {
                backingStore.save(nonce, created, true);
                LOGGER.log(Level.INFO, " nonce saved ");
            }
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public boolean validateNonce(String nonce, String created) throws NonceException {
        byte[] data = created.getBytes();
        MyPojo pojo = new MyPojo();
        pojo.setData(data);
        return this.validateNonce(nonce, pojo);
    }

    public class HANM implements Runnable {

        public void run() {
            try {
                int removed = backingStore.removeExpired(maxNonceAge);
            } catch (BackingStoreException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    static public class MyPojo implements Serializable {

        byte[] data;

        public void setData(byte[] data) {
            this.data = data;
        }

        public byte[] getData() {
            return this.data;
        }
    }
}
