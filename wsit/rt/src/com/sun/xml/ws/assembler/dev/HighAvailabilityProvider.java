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

package com.sun.xml.ws.assembler.dev;

import com.sun.istack.logging.Logger;
import java.io.Serializable;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.spi.BackingStoreFactoryRegistry;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public enum HighAvailabilityProvider {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(HighAvailabilityProvider.class);

    public static enum StoreType {
        IN_MEMORY("replicated"), // FIXME replace with a constant reference when available
        NOOP(BackingStoreConfiguration.NO_OP_PERSISTENCE_TYPE);

        private final String storeTypeId;

        private StoreType(String storeTypeId) {
            this.storeTypeId = storeTypeId;
        }
    }

    private static class HaEnvironment {
        public static final HaEnvironment NO_HA_ENVIRONMENT = new HaEnvironment(null, null);

        private final String clusterName;
        private final String instanceName;

        private HaEnvironment(final String clusterName, final String instanceName) {
            this.clusterName = clusterName;
            this.instanceName = instanceName;
        }

        public static HaEnvironment getInstance(final String clusterName, final String instanceName) {
            if (clusterName == null && instanceName == null) {
                return NO_HA_ENVIRONMENT;
            }

            return new HaEnvironment(clusterName, instanceName);
        }

        public String getClusterName() {
            return clusterName;
        }

        public String getInstanceName() {
            return instanceName;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HaEnvironment other = (HaEnvironment) obj;
            if ((this.clusterName == null) ? (other.clusterName != null) : !this.clusterName.equals(other.clusterName)) {
                return false;
            }
            if ((this.instanceName == null) ? (other.instanceName != null) : !this.instanceName.equals(other.instanceName)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.clusterName != null ? this.clusterName.hashCode() : 0);
            hash = 89 * hash + (this.instanceName != null ? this.instanceName.hashCode() : 0);
            return hash;
        }
    }

    private volatile HaEnvironment haEnvironment = HaEnvironment.NO_HA_ENVIRONMENT;

    public void initHaEnvironment(final String clusterName, final String instanceName) {
        this.haEnvironment = HaEnvironment.getInstance(clusterName, instanceName);
    }

    public <K extends Serializable, V extends Serializable> BackingStoreConfiguration<K, V> initBackingStoreConfiguration(
            final String storeName,
            final Class<K> keyClass,
            final Class<V> valueClass) {

        final HaEnvironment env = this.haEnvironment; // prevents synchronization issues with concurrent invocation of initEnvironment(...)

        return new BackingStoreConfiguration<K, V>()
                .setClusterName(env.clusterName)
                .setInstanceName(env.getInstanceName())
                .setStoreName(storeName)
                .setKeyClazz(keyClass)
                .setValueClazz(valueClass);
    }

    public BackingStoreFactory getBackingStoreFactory(final StoreType type) throws HighAvailabilityProviderException {
        if (!isHaEnvironmentConfigured()) {
            return getSafeBackingStoreFactory(StoreType.NOOP);
        }

        return getSafeBackingStoreFactory(type);
    }

    private BackingStoreFactory getSafeBackingStoreFactory(final StoreType type) throws HighAvailabilityProviderException {
        try {
            return BackingStoreFactoryRegistry.getFactoryInstance(type.storeTypeId);
        } catch (BackingStoreException ex) {
            throw LOGGER.logSevereException(new HighAvailabilityProviderException("", ex)); // TODO message
        }
    }

    /**
     * Provides information on whether there is a HA service available in the
     * current JVM or not.
     * 
     * @return {@code true} in case there is a HA service available in the current
     *         JVM, {@code false} otherwise
     */
    public boolean isHaEnvironmentConfigured() {
        return !HaEnvironment.NO_HA_ENVIRONMENT.equals(this.haEnvironment);
    }
}
