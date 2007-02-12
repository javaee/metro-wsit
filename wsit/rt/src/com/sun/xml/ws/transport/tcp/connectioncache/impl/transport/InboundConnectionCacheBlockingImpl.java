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

package com.sun.xml.ws.transport.tcp.connectioncache.impl.transport;

import com.sun.xml.ws.transport.tcp.connectioncache.spi.concurrent.ConcurrentQueue;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.Connection;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.InboundConnectionCache;
import java.io.IOException ;

import java.util.logging.Logger ;

import java.util.Map ;
import java.util.HashMap ;

/** Manage connections that are initiated from another VM. 
 *
 * @author Ken Cavanaugh
 */
public final class InboundConnectionCacheBlockingImpl<C extends Connection> 
    extends ConnectionCacheBlockingBase<C> 
    implements InboundConnectionCache<C> {

    private final Map<C,ConnectionState<C>> connectionMap ;

    protected String thisClassName() {
	return "InboundConnectionCacheBlockingImpl" ;
    }

    private static final class ConnectionState<C extends Connection> {
	final C connection ;		// Connection of the 
					// ConnectionState
	int busyCount ;			// Number of calls to 
					// get without release
	int expectedResponseCount ;	// Number of expected 
					// responses not yet 
					// received

	ConcurrentQueue.Handle<C> reclaimableHandle ;  // non-null iff connection 
						    // is not in use and has no
						    // outstanding requests

	ConnectionState( final C conn ) {
	    this.connection = conn ;

	    busyCount = 0 ;
	    expectedResponseCount = 0 ;
	    reclaimableHandle = null ;
	}
    }

    public InboundConnectionCacheBlockingImpl( final String cacheType, 
	final int highWaterMark, final int numberToReclaim, 
	Logger logger ) {

	super( cacheType, highWaterMark, numberToReclaim, logger ) ;

	this.connectionMap = new HashMap<C,ConnectionState<C>>() ;

	if (debug()) {
	    dprint(".constructor completed: " + getCacheType() );
	}
    }

    // We do not need to define equals or hashCode for this class.

    public synchronized void requestReceived( final C conn ) {
	if (debug())
	    dprint( "->requestReceived: connection " + conn ) ;

	try {
	    ConnectionState<C> cs = getConnectionState( conn ) ;

	    final int totalConnections = totalBusy + totalIdle ;
	    if (totalConnections > highWaterMark())
		reclaim() ;

	    ConcurrentQueue.Handle<C> reclaimHandle = cs.reclaimableHandle ;
	    if (reclaimHandle != null) {
		if (debug())
		    dprint( ".requestReceived: " + conn 
			+ " removed from reclaimableQueue" ) ;
		reclaimHandle.remove() ;
	    }

	    int count = cs.busyCount++ ;
	    if (count == 0) {
		if (debug())
		    dprint( ".requestReceived: " + conn 
			+ " transition from idle to busy" ) ;

		totalIdle-- ;
		totalBusy++ ;
	    }
	} finally {
	    if (debug())
		dprint( "<-requestReceived: connection " + conn ) ;
	}
    }

    public synchronized void requestProcessed( final C conn, 
	final int numResponsesExpected ) {

	if (debug())
	    dprint( "->requestProcessed: connection " + conn 
		+ " expecting " + numResponsesExpected + " responses" ) ;

	try {
	    final ConnectionState<C> cs = connectionMap.get( conn ) ;

	    if (cs == null) {
		if (debug())
		    dprint( ".release: connection " + conn + " was closed" ) ;

		return ; 
	    } else {
		cs.expectedResponseCount += numResponsesExpected ;
		int numResp = cs.expectedResponseCount ;
		int numBusy = --cs.busyCount ;

		if (debug()) {
		    dprint( ".release: " + numResp + " responses expected" ) ;
		    dprint( ".release: " + numBusy + 
			" busy count for connection" ) ;
		}

		if (numBusy == 0) {
		    totalBusy-- ;
		    totalIdle++ ;

		    if (numResp == 0) {
			if (debug())
			    dprint( ".release: "
				+ "queuing reclaimable connection "
				+ conn ) ;

			if ((totalBusy+totalIdle) > highWaterMark()) {
			    close( conn ) ;
			} else {
			    cs.reclaimableHandle = 
				reclaimableConnections.offer( conn ) ;
			}
		    }
		}
	    }
	} finally {
	    if (debug())
		dprint( "<-requestProcessed" ) ;
	}
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    public synchronized void responseSent( final C conn ) {
	if (debug())
	    dprint( "->responseSent: " + conn ) ;

	try {
	    final ConnectionState<C> cs = connectionMap.get( conn ) ;
	    final int waitCount = --cs.expectedResponseCount ;
	    if (waitCount == 0) {
		if (debug())
		    dprint( ".responseSent: " + conn + " is now reclaimable" ) ;

		if ((totalBusy+totalIdle) > highWaterMark()) {
		    if (debug()) {
			dprint( ".responseSent: " + conn 
			    + " closing connection" ) ; 
		    }

		    close( conn ) ;
		} else {
		    cs.reclaimableHandle = 
			reclaimableConnections.offer( conn ) ;

		    if (debug()) {
			dprint( ".responseSent: " + conn 
			    + " is now reclaimable" ) ; 
		    }
		}
	    } else {
		if (debug()) {
		    dprint( ".responseSent: " + conn + " waitCount=" 
			+ waitCount ) ;
		}
	    }
	} finally {
	    if (debug()) {
		dprint( "<-responseSent: " + conn ) ;
	    }
	}
    }

    /** Close a connection, regardless of whether the connection is busy
     * or not.
     */
    public synchronized void close( final C conn ) {
	if (debug()) 
	    dprint( "->close: " + conn ) ;
	
	try {
	    final ConnectionState<C> cs = connectionMap.remove( conn ) ;
	    int count = cs.busyCount ;
	    if (debug())
		dprint( ".close: " + conn + " count = " + count ) ;

	    if (count == 0)
		totalIdle-- ;
	    else
		totalBusy-- ;

	    final ConcurrentQueue.Handle rh = cs.reclaimableHandle ;
	    if (rh != null) {
		if (debug())
		    dprint( ".close: " + conn + " connection was reclaimable" ) ;

		rh.remove() ;
	    }

	    try {
		conn.close() ;
	    } catch (IOException exc) {
		if (debug())
		    dprint( ".close: " + conn + " close threw "  + exc ) ;
	    }
	} finally {
	    if (debug())
		dprint( "<-close: " + conn ) ;
	}
    }

    // Atomically either get the ConnectionState for conn OR 
    // create a new one AND put it in the cache
    private ConnectionState<C> getConnectionState( C conn ) {
	// This should be the only place a CacheEntry is constructed.
	if (debug())
	    dprint( "->getConnectionState: " + conn ) ;

	try {
	    ConnectionState<C> result = connectionMap.get( conn ) ;
	    if (result == null) {
		if (debug())
		    dprint( ".getConnectionState: " + conn + 
			" creating new ConnectionState instance" ) ;
		result = new ConnectionState<C>( conn ) ;
		connectionMap.put( conn, result ) ;
		totalIdle++ ;
	    }
	    
	    return result ;
	} finally {
	    if (debug())
		dprint( "<-getConnectionState: " + conn ) ;
	}
    }
}

// End of file.
