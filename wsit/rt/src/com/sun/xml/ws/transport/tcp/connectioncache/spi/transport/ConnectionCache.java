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

/** A connection cache manages a group of connections which may be re-used
 * for sending and receiving messages.
 */
public interface ConnectionCache<C extends Connection> {
    /** User-provided indentifier for an instance of the 
     * OutboundConnectionCache.
     */
    String getCacheType() ;

    /** Total number of connections currently managed by the cache.
     */
    long numberOfConnections() ;

    /** Number of idle connections; that is, connections for which the number of
     * get/release or responseReceived/responseProcessed calls are equal.
     */
    long numberOfIdleConnections() ;

    /** Number of non-idle connections.  Normally, busy+idle==total, but this
     * may not be strictly true due to concurrent updates to the connection 
     * cache.
     */
    long numberOfBusyConnections() ;

    /** Number of idle connections that are reclaimable.  Such connections
     * are not in use, and are not waiting to handle any responses.
     */
    long numberOfReclaimableConnections() ;

    /** Threshold at which connection reclamation begins.
     */
    int highWaterMark() ;

    /** Number of connections to reclaim each time reclamation starts.
     */
    int numberToReclaim() ;
    
    /** Close a connection, regardless of its state.  This may cause requests
     * to fail to be sent, and responses to be lost.  Intended for 
     * handling serious errors, such as loss of framing on a TCP stream,
     * that require closing the connection.
     */
    void close( final C conn ) ;
}
