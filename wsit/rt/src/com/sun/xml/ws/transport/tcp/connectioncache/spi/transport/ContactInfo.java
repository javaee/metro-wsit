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

import java.io.IOException ;

/** The ContactInfo represents the information needed to establish a connection
 * to a (possibly different) process.  This is a subset of the PEPt 2.0 connection.
 * Any implemetnation of this interface must define hashCode and equals properly so that
 * it may be used in a Map.
 */
public interface ContactInfo<C extends Connection> {
    /** Create a new Connection from this ContactInfo.
     * Throws an IOException if Connection creation fails.
     */
    C createConnection() throws IOException ;
}
