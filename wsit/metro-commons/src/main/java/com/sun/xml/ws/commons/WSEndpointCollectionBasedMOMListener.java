/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.commons;

import com.sun.xml.ws.api.server.LazyMOMProvider;
import com.sun.xml.ws.api.server.WSEndpoint;
import org.glassfish.gmbal.ManagedObjectManager;

import java.util.Map;

/**
 * Default implementation of {@link LazyMOMProvider.DefaultScopeChangeListener} for manager factories handling {@link WSEndpoint} instances.
 *
 * @param <T>
 */
public class WSEndpointCollectionBasedMOMListener<T extends MOMRegistrationAware> implements LazyMOMProvider.DefaultScopeChangeListener {

    private final Object lock;
    private final Map<WSEndpoint, T> registrationAwareMap;
    private final String registrationName;

    private LazyMOMProvider.Scope lazyMOMProviderScope = LazyMOMProvider.Scope.STANDALONE;

    public WSEndpointCollectionBasedMOMListener(String registrationName, Map<WSEndpoint, T> registrationAwareMap) {
        this(new Object(), registrationName, registrationAwareMap);
    }
    
    public WSEndpointCollectionBasedMOMListener(Object lock, String registrationName, Map<WSEndpoint, T> registrationAwareMap) {
        this.lock = lock;
        this.registrationName = registrationName;
        this.registrationAwareMap = registrationAwareMap;
    }

    /**
     * Initializes this listener. Currently this means that listener is registering itself at {@link LazyMOMProvider}.
     */
    public void initialize() {
        // register this listener at provider
        LazyMOMProvider.INSTANCE.registerListener(this);
    }

    /**
     * Returns an indication whether a object can be directly registered at {@link org.glassfish.gmbal.ManagedObjectManager}.
     *
     * @return {@code true} if a object can be registered, {@code false} otherwise
     */
    public boolean canRegisterAtMOM() {
        return lazyMOMProviderScope != LazyMOMProvider.Scope.GLASSFISH_NO_JMX;
    }

    private void registerObjectsAtMOM() {
        synchronized (lock) {
            for (Map.Entry<WSEndpoint, T> entry : registrationAwareMap.entrySet()) {
                registerAtMOM(entry.getValue(), entry.getKey());
            }
        }
    }

    public void registerAtMOM(MOMRegistrationAware momRegistrationAware, WSEndpoint wsEndpoint) {
        registerAtMOM(momRegistrationAware, wsEndpoint.getManagedObjectManager());
    }

    public void registerAtMOM(MOMRegistrationAware momRegistrationAware, ManagedObjectManager managedObjectManager) {
        if (!momRegistrationAware.isRegisteredAtMOM()) {
            managedObjectManager.registerAtRoot(momRegistrationAware, registrationName);
            momRegistrationAware.setRegisteredAtMOM(true);
        }
    }

    public void scopeChanged(LazyMOMProvider.Scope scope) {
        synchronized (lock) {
            if (this.lazyMOMProviderScope == scope) {
                return;
            }

            this.lazyMOMProviderScope = scope;
        }

        switch (scope) {
            case GLASSFISH_JMX:
                registerObjectsAtMOM();
                break;
            case GLASSFISH_NO_JMX:
                unregisterObjectsFromMOM();
                break;
            default:
                // do nothing, STANDALONE is the default behavior
        }
    }

    private void unregisterObjectsFromMOM() {
        synchronized (lock) {
            for (Map.Entry<WSEndpoint, T> entry : registrationAwareMap.entrySet()) {
                if (entry.getValue().isRegisteredAtMOM()) {
                    unregisterFromMOM(entry.getValue(), entry.getKey());
                }
            }
        }
    }

    public void unregisterFromMOM(MOMRegistrationAware momRegistrationAware, ManagedObjectManager managedObjectManager) {
        managedObjectManager.unregister(momRegistrationAware);
        momRegistrationAware.setRegisteredAtMOM(false);
    }

    public void unregisterFromMOM(MOMRegistrationAware momRegistrationAware, WSEndpoint wsEndpoint) {
        registerAtMOM(momRegistrationAware, wsEndpoint.getManagedObjectManager());
    }

}
