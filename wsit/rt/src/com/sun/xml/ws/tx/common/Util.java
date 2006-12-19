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
package com.sun.xml.ws.tx.common;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * This class contains various utility methods shared among
 * other modules.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.2 $
 * @since 1.0
 */
public class Util {


    /**
     * Return the InetAddress of the system hosting the coordination services (AS)
     * <p/>
     * TODO: this method should probably also return the right port number for the multiple AS on a single host case
     *
     * @return the InetAddress of the system hosting the coordination services
     */
    public static InetAddress getServiceAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method returns the fully qualified name of the host.  If
     * the name can't be resolved (on windows if there isn't a domain specified), just
     * host name is returned
     */
    private static String getServiceHostName() {
        String hostname = null;
        try {
            // look for full name
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            try {
                // fallback to ip address
                hostname = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        return hostname;
    }

    // system property overrides for host name, scheme, and port
    private static String schemeOverride = System.getProperty(Util.class.getName() + ".override.wstxservice.scheme", null);
    private static String hostOverride = System.getProperty(Util.class.getName() + ".override.wstxservice.host", null);
    private static String portOverride = System.getProperty(Util.class.getName() + ".override.wstxservice.port", null);

    /**
     * Construct a URI from the specified params.  Note that 'host', 'port', and 'scheme' can
     * be overridden by system properties (com.sun.xml.ws.tx.common.Util.override.wstxservice.host,
     * com.sun.xml.ws.tx.common.Util.override.wstxservice.port, com.sun.xml.ws.tx.common.Util.override.wstxservice.scheme).
     * <p/>
     * <b>Host name will be set according to the following precedence:</b><br>
     * <ol>
     * <li> if com.sun.xml.ws.tx.common.Util.override.host is set, this will override all other settings
     * <li> 'host' parameter value
     * <li> if 'host' parameter is null, then it will be calculated using {@link #getServiceHostName()}
     * </ol>
     *
     * @param scheme 'http' or 'https'
     * @param host   system hostname or null if this value should be computed automatically
     * @param port   port number
     * @param path   path starting with '/'
     * @return the resulting URI
     */
    public static URI createURI(final String scheme, final String host, final int port, final String path) {
        final String hostname;
        
        // calculate host name
        if (hostOverride == null) {
            if (host == null) {
                hostname = getServiceHostName();
            } else {
                hostname = host; // else just use 'host' parameter
            }
        } else {
            hostname = hostOverride;
        }

        URI uri = null;
        try {
            uri = new URI(
                    schemeOverride == null ? scheme : schemeOverride,
                    null, // user info
                    hostname,
                    portOverride == null ? port : Integer.parseInt(portOverride),
                    path,
                    null, // query
                    null // fragment
            );
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
    }

    private static long nextMsgId = 1;

    public static String getNextUniqueMessageId() {
        return "urn:uuid:msg-id-" + nextMsgId++;
    }
}
