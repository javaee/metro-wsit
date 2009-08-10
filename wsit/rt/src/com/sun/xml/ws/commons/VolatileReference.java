package com.sun.xml.ws.commons;

final class VolatileReference<V> {

    public volatile V value;

    public VolatileReference(V value) {
        super();
        this.value = value;
    }
}
