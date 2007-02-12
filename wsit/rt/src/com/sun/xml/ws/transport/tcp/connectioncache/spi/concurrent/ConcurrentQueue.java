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

package com.sun.xml.ws.transport.tcp.connectioncache.spi.concurrent;

/** A class that provides a very simply unbounded queue.
 * The main requirement here is that the class support constant time (very fast)
 * deletion of arbitrary elements.  An instance of this class must be thread safe,
 * either by locking or by using a wait-free algorithm (preferred).
 * The interface is made as simple is possible to make it easier to produce
 * a wait-free implementation.
 */
public interface ConcurrentQueue<V> {
    /** A Handle provides the capability to delete an element of a ConcurrentQueue
     * very quickly.  Typically, the handle is stored in the element, so that an
     * element located from another data structure can be quickly deleted from 
     * a ConcurrentQueue.
     */
    public interface Handle<V> {
	/** Return the value that corresponds to this handle.
	 */
	V value() ;

	/** Delete the element corresponding to this handle 
	 * from the queue.  Takes constant time.  Returns
	 * true if the removal succeeded, or false if it failed.
	 * which can occur if another thread has already called
	 * poll or remove on this element.
	 */
	boolean remove() ;
    }

    /** Return the number of elements in the queue.
     */
    int size() ;

    /** Add a new element to the tail of the queue.
     * Returns a handle for the element in the queue.
     */
    Handle<V> offer( V arg ) ;

    /** Return an element from the head of the queue.
     * The element is removed from the queue.
     */
    V poll() ;
} 
