/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.at.ParticipantPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationCoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationPortTypeRPC;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortType;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 * This class handles all address calculations for the wstx-service enpoints
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.6.6.2 $
 * @since 1.0
 */
public class AddressManager {

    static final private TxLogger logger = TxLogger.getLogger(AddressManager.class);

    private static String hostAndPort;
    private static String secureHostAndPort;

    private static final String context = "/__wstx-services";

    // override props - default to https
    private static String preferredScheme = System.getProperty("com.sun.xml.ws.tx.preferredScheme", "https");
    private static String httpPortOverride = System.getProperty("com.sun.xml.ws.tx.httpPortOverride", null);
    private static String httpsPortOverride = System.getProperty("com.sun.xml.ws.tx.httpsPortOverride", null);
    private static String hostNameOverride = System.getProperty("com.sun.xml.ws.tx.hostNameOverride", null);

    static {
        // get the host and port info from GF
        callGetDefaultVirtualServerHostAndPort();

        // log a message if the preferred scheme has been overridden to http
        if ((preferredScheme.equals("http") && (logger.isLogging(Level.FINEST)))) {
            logger.fine("static initializer", "preferred scheme has been set to: " + preferredScheme);
        }

        // logic to override the dynamic settings
        // users can select a preferred scheme and override the http and https port numbers if necessary
        calculateHostAndPortOverrides();
    }

    private AddressManager() {
    }

    /**
     * Return an address for the specified port type using the preferred scheme.
     * <p/>
     * This method should be used most of the time unless you explicitly know which
     * scheme you want back.
     *
     * @param portType the endpoint port type
     * @return a URI representing the preferred endpoint address
     */
    public static URI getPreferredAddress(Class portType) {
        return "http".equals(preferredScheme) ? getAddress(portType, false) : getAddress(portType, true);
    }

    /**
     * Return an address for the specified port type based on the 'secure' flag.  If
     * the 'secure' flag is true, the address will begin with "https", otherwise it
     * will begin with "http".
     *
     * @param portType the endpoint port type
     * @param secure flag to control whether the address is secure or non-secure
     * @return a URI representing the endpoint address with the specified secure or non-secure scheme
     */
    public static URI getAddress(Class portType, boolean secure) {
        StringBuilder addr = new StringBuilder();

        if (secure) {
            addr.append("https://").append(secureHostAndPort).append(context);
        } else { // non-secure
            addr.append("http://").append(hostAndPort).append(context);
        }

        // assign the proper context path onto the end of the scheme, host, port, and context root
        // TODO: are there other addresses we need to generate?
        if (portType == RegistrationCoordinatorPortType.class) {
            addr.append("/wscoor/coordinator/register");
        } else if (portType == RegistrationRequesterPortType.class) {
            addr.append("/wscoor/coordinator/registerResponse");
        } else if (portType == RegistrationPortTypeRPC.class) {
            addr.append("/wscoor/coordinator/synchRegister");
        } else if (portType == CoordinatorPortType.class) {
            addr.append("/wsat/coordinator");
        } else if (portType == ParticipantPortType.class) {
            addr.append("/wsat/2pc");
        } else {
            return null;
        }

        URI uri = null;
        try {
            uri = new URI(addr.toString());
        } catch (URISyntaxException e) {
        }

        return uri;
    }

    /**
     * Call appserv-core/com.sun.enterprise.webservice.WsTxUtils in GF to get the host and port information.
     */
    private static void callGetDefaultVirtualServerHostAndPort() {
        try {
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("static initializer", "getting host and port from AS...");
            }
           
            hostAndPort = HostAndPortLookup.getInstance().getDefaultVirtualServerHostAndPort(false);
            secureHostAndPort = HostAndPortLookup.getInstance().getDefaultVirtualServerHostAndPort(true);

            if (secureHostAndPort == null || hostAndPort == null) {
                // there must have been an error on the GF-side, so fallback to a default
                fallback();
                logger.warning("getAddress",
                        LocalizationMessages.HOST_AND_PORT_LOOKUP_FAILURE_2015(preferredScheme + "://" + secureHostAndPort));
            }

            // this is an undocumented override
            if (hostNameOverride != null) {
                hostAndPort = hostNameOverride + hostAndPort.substring(hostAndPort.indexOf(':'), hostAndPort.length());
                secureHostAndPort = hostNameOverride + secureHostAndPort.substring(secureHostAndPort.indexOf(':'), secureHostAndPort.length());
                logger.finest("static initializer", "manual hostname overridden to: " + hostNameOverride);
            }

            if (logger.isLogging(Level.FINEST)) {
                logger.finest("static initializer", "hostAndPort: " + hostAndPort);
                logger.finest("static initializer", "secureHostAndPort: " + secureHostAndPort);
            }
        } catch (Throwable t) {  // trap every possible failure calling the gf code
            fallback();
            logger.info("static initializer",
                    LocalizationMessages.HOST_AND_PORT_LOOKUP_FAILURE_2015(preferredScheme + "://" + hostAndPort),
                    t);
            logger.finest("static initializer",
                    LocalizationMessages.HOST_AND_PORT_LOOKUP_FAILURE_2015(preferredScheme + "://" + secureHostAndPort),
                    t);
        }
    }
    
    /**
     * Lookup any system props that override the preferred scheme, and http/s ports
     */
    private static void calculateHostAndPortOverrides() {
        final String hostName = hostAndPort.substring(0, hostAndPort.indexOf(':'));
        if ("http".equals(preferredScheme)) {
            if (httpPortOverride != null) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest("static initializer", "http port overriden to: " + httpPortOverride);
                }
                hostAndPort = hostName + ':' + httpPortOverride;
            }
        } else if ("https".equals(preferredScheme)) {
            if (httpsPortOverride != null) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest("static initializer", "https port overriden to: " + httpsPortOverride);
                }
                secureHostAndPort = hostName + ':' + httpsPortOverride;
            }
        } else {
            logger.warning("static initializer", LocalizationMessages.PREFERRED_SCHEME_ERROR_2016(preferredScheme));
            preferredScheme = "https";
            if (httpsPortOverride != null) {
                if (logger.isLogging(Level.FINEST)) {
                    logger.finest("static initializer", "https port overriden to: " + httpsPortOverride);
                }
                secureHostAndPort = hostName + ':' + httpsPortOverride;
            }
        }
    }

    /**
     * Set the static fields to sane value that might work as a last-ditch effort.
     */
    private static void fallback() {
        // worst-case scenario: fall back to https://canonicalhostname:8181, but also set
        // hostAndPort since it is used to ping the services.
        preferredScheme = "https";
        final String hostName = getServiceHostName();
        hostAndPort = hostName + ":8080";
        secureHostAndPort = hostName + ":8181";
    }

    /**
     * This method returns the fully qualified name of the host.  If
     * the name can't be resolved (on windows if there isn't a domain specified), just
     * host name is returned
     *
     * @return the host name
     */
    private static String getServiceHostName() {
        String hostname = null;
        try {
            // look for full name
            hostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException ex) {
            logger.warning("getServiceHostName", LocalizationMessages.FQDN_LOOKUP_FAILURE_2012(), ex);
            try {
                // fallback to ip address
                hostname = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.warning("getServiceHostName", LocalizationMessages.FQDN_LOOKUP_FAILURE_2012(), e);
            }
        }

        return hostname;
    }

}