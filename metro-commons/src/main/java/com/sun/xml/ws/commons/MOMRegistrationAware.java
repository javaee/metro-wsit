package com.sun.xml.ws.commons;

/**
 * Provides information whether or not is an object registered at {@link org.glassfish.gmbal.ManagedObjectManager} or not.
 * All classes that use explicit registration at {@link org.glassfish.gmbal.ManagedObjectManager} should implement this
 * interface as it is used when deferring Gmbal API calls in {@link WSEndpointCollectionBasedMOMListener}
 */
public interface MOMRegistrationAware {

    boolean isRegisteredAtMOM();

    void setRegisteredAtMOM(boolean isRegisteredAtMOM);

}
