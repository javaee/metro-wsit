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

import com.sun.xml.ws.transport.tcp.connectioncache.impl.concurrent.ConcurrentQueueBlockingImpl;
import com.sun.xml.ws.transport.tcp.connectioncache.impl.concurrent.ConcurrentQueueImpl;

/** A factory class for creating instances of ConcurrentQueue.
 * Note that a rather unusual syntax is needed for calling these methods:
 *
 * ConcurrentQueueFactory.<V>makeXXXConcurrentQueue() 
 *
 * This is required because the type variable V is not used in the
 * parameters of the factory method, so the correct type
 * cannot be inferred by the compiler.
 */
public final class ConcurrentQueueFactory {
    private ConcurrentQueueFactory() {} 

    /** Create a ConcurrentQueue whose implementation uses conventional
     * locking to protect the data structure.
     */
    public static <V> ConcurrentQueue<V> makeBlockingConcurrentQueue() {
	return new ConcurrentQueueBlockingImpl<V>() ;
    }

    /** Create a ConcurrentQueue that does no locking at all.
     * For use in data structures that manage their own locking.
     */
    public static <V> ConcurrentQueue<V> makeConcurrentQueue() {
	return new ConcurrentQueueImpl<V>() ;
    }
}
