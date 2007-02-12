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

import com.sun.xml.ws.transport.tcp.connectioncache.spi.concurrent.ConcurrentQueueFactory;
import com.sun.xml.ws.transport.tcp.connectioncache.spi.transport.Connection;
import java.util.logging.Logger ;

abstract class ConnectionCacheBlockingBase<C extends Connection>
        extends ConnectionCacheBase<C> {
    
    protected int totalBusy ;	// Number of busy connections
    protected int totalIdle ;	// Number of idle connections
    
    ConnectionCacheBlockingBase( String cacheType, int highWaterMark,
            int numberToReclaim, Logger logger ) {
        
        super( cacheType, highWaterMark, numberToReclaim, logger ) ;
        
        this.totalBusy = 0 ;
        this.totalIdle = 0 ;
        
        this.reclaimableConnections =
                ConcurrentQueueFactory.<C>makeConcurrentQueue() ;
    }
    
    public synchronized long numberOfConnections() {
        return totalIdle + totalBusy ;
    }
    
    public synchronized long numberOfIdleConnections() {
        return totalIdle ;
    }
    
    public synchronized long numberOfBusyConnections() {
        return totalBusy ;
    }
    
    public synchronized long numberOfReclaimableConnections() {
        return reclaimableConnections.size() ;
    }
}

