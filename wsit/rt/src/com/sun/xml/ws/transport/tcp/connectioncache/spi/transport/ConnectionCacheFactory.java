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

package com.sun.xml.ws.transport.tcp.connectioncache.spi.transport;

import com.sun.xml.ws.transport.tcp.connectioncache.impl.transport.InboundConnectionCacheBlockingImpl;
import com.sun.xml.ws.transport.tcp.connectioncache.impl.transport.OutboundConnectionCacheBlockingImpl;
import java.util.logging.Logger ;

/** A factory class for creating connections caches.
 * Note that a rather unusual syntax is needed for calling these methods:
 *
 * ConnectionCacheFactory.<V>makeXXXCache() 
 *
 * This is required because the type variable V is not used in the
 * parameters of the factory method (there are no parameters).
 */
public final class ConnectionCacheFactory {
    private ConnectionCacheFactory() {}

    public static <C extends Connection> OutboundConnectionCache<C>
    makeBlockingOutboundConnectionCache( String cacheType, int highWaterMark,
	int numberToReclaim, int maxParallelConnections, Logger logger ) {

	return new OutboundConnectionCacheBlockingImpl<C>( cacheType, highWaterMark,
	    numberToReclaim, maxParallelConnections, logger ) ;
    }

    public static <C extends Connection> InboundConnectionCache<C>
    makeBlockingInboundConnectionCache( String cacheType, int highWaterMark,
	int numberToReclaim, Logger logger ) {
	return new InboundConnectionCacheBlockingImpl<C>( cacheType,
	    highWaterMark, numberToReclaim, logger ) ;
    }
}
