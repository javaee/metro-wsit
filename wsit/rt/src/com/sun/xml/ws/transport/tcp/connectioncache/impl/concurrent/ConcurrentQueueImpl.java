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

package com.sun.xml.ws.transport.tcp.connectioncache.impl.concurrent;

import com.sun.xml.ws.transport.tcp.connectioncache.spi.concurrent.ConcurrentQueue;

public class ConcurrentQueueImpl<V> implements ConcurrentQueue<V> {
    // This implementation of ConcurrentQueue is unsynchronized, for use in
    // other implementations that manage concurrency with locks.
    //
    // Structure: Head points to a node containing a null value, which is a special marker.
    // head.next is the first element, head.prev is the last.  The queue is empty if
    // head.next == head.prev == head.
    final Entry<V> head = new Entry<V>( null ) ;
    int count = 0 ;

    public ConcurrentQueueImpl() {
	head.next = head ;
	head.prev = head ;
    }

    private final class Entry<V> {
	Entry<V> next = null ;
	Entry<V> prev = null ;
	private HandleImpl<V> handle ;

	Entry( V value ) {
	    handle = new HandleImpl<V>( this, value ) ;
	}

	HandleImpl<V> handle() {
	    return handle ;
	}
    }

    private final class HandleImpl<V> implements Handle<V> {
	private Entry<V> entry ;
	private final V value ;
	private boolean valid ;

	HandleImpl( Entry<V> entry, V value ) {
	    this.entry = entry ;
	    this.value = value ;
	    this.valid = true ;
	}

	Entry<V> entry() {
	    return entry ;
	}

	public V value() {
	    return value ;
	}

	/** Delete the element corresponding to this handle 
	 * from the queue.  Takes constant time.
	 */
	public boolean remove() {
	    if (!valid) {
		return false ;
	    }

	    valid = false ;

	    entry.next.prev = entry.prev ;
	    entry.prev.next = entry.next ;
	    count-- ;

	    entry.prev = null ;
	    entry.next = null ;
	    entry.handle = null ;
	    entry = null ;
	    valid = false ;
	    return true ;
	}
    }

    public int size() {
	return count ;
    }

    /** Add a new element to the tail of the queue.
     * Returns a handle for the element in the queue.
     */
    public Handle<V> offer( V arg ) {
	if (arg == null)
	    throw new IllegalArgumentException( "Argument cannot be null" ) ;

	Entry<V> entry = new Entry<V>( arg ) ;
	
	entry.next = head ;
	entry.prev = head.prev ;
	head.prev.next = entry ;
	head.prev = entry ;
	count++ ;

	return entry.handle() ;
    }

    /** Return an element from the head of the queue.
     * The element is removed from the queue.
     */
    public V poll() {
	Entry<V> first = null ;
	V value = null ;

	first = head.next ;
	if (first == head)
	    return null ;
	else {
	    value = first.handle().value() ;

	    // assert that the following expression returns true!
	    first.handle().remove() ;
	}

	// Once first is removed from the queue, it is invisible to other threads,
	// so we don't need to synchronize here.
	first.next = null ;
	first.prev = null ;
	return value ;
    }
} 
