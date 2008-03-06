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
package com.sun.xml.ws.mex.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.PortAddressResolver;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

/**
 *
 * @author Fabian Ritzmann
 */
class MEXAddressResolver extends PortAddressResolver {

    private static final Logger LOGGER = Logger.getLogger(MEXAddressResolver.class.getName());
    private final QName service;
    private final String port;
    private final String address;

    MEXAddressResolver(@NotNull final QName serviceName, @NotNull final QName portName,
            @NotNull final String address) {
        this.service = serviceName;
        this.port = portName.getLocalPart();
        this.address = address;
    }

    @Override
    public String getAddressFor(@NotNull final QName serviceName, @NotNull final String portName) {
        return this.address;
    }

    @Override
    public String getAddressFor(@NotNull final QName serviceName, @NotNull final String portName,
            final String currentAddress) {
        LOGGER.entering(MEXAddressResolver.class.getName(), "getAddressFor", 
                new Object[] {serviceName, portName, currentAddress});
        String result = null;
        if (this.service.equals(serviceName) && this.port.equals(portName)) {
            result = getAddressFor(serviceName, portName);
            if (currentAddress != null) {
                try {
                    final URL addressUrl = new URL(this.address);
                    try {
                        final URL currentAddressUrl = new URL(currentAddress);
                        if (currentAddressUrl.getProtocol().toLowerCase().equals("http") &&
                                addressUrl.getProtocol().toLowerCase().equals("https")) {
                            result = currentAddress;
                        }
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(MEXAddressResolver.class.getName()).fine(ex.toString());
                    }
                } catch (MalformedURLException ex) {
                    Logger.getLogger(MEXAddressResolver.class.getName()).fine(ex.toString());
                }
            }
        }
        LOGGER.exiting(MEXAddressResolver.class.getName(), "getAddressFor", result);
        return result;
    }
}
