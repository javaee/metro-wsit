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
