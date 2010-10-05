package com.sun.xml.ws.rx.rm.runtime.sequence.invm;

import java.io.Serializable;

interface ReplicationManager<K extends Serializable, V> {

    V load(K key);

    String save(K key, V value, boolean isNew);

    void remove(K key);

    void close();

    void destroy();
}
