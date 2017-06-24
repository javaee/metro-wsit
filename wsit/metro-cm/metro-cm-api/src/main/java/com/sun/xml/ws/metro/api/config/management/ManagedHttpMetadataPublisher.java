/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.metro.api.config.management;

import com.sun.istack.NotNull;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.server.EndpointComponent;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.HttpMetadataPublisher;
import com.sun.xml.ws.transport.http.WSHTTPConnection;

import java.io.IOException;

/**
 * Publish WSDL of a managed endpoint.
 * 
 * This implementation makes sure the WSDL policies are updated when the endpoint
 * was reconfigured.
 *
 * @author Fabian Ritzmann
 */
class ManagedHttpMetadataPublisher extends HttpMetadataPublisher implements EndpointComponent {

    private static final Logger LOGGER = Logger.getLogger(ManagedHttpMetadataPublisher.class);

    public <T> T getSPI(Class<T> spiType) {
        if (spiType.isAssignableFrom(this.getClass())) {
            return spiType.cast(this);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean handleMetadataRequest(HttpAdapter adapter, WSHTTPConnection connection)
            throws IOException {
        final String query = connection.getQueryString();
        if (isWSDLQuery(query)) {
            publishWSDL(connection, adapter);
            return true;
        }
        else if (isInitQuery(query)) {
            LOGGER.info(ManagementMessages.WSM_5100_INIT_RECEIVED());
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns true if the given query string is for WSDL request.
     *
     * @param query
     *      String like "wsdl=2".
     *      Can be null.
     * @return true for WSDL requests
     *         false for web service requests
     */
    private boolean isWSDLQuery(String query) {
        return query != null && (query.equals("WSDL") || query.startsWith("wsdl"));
    }

    /**
     * Returns true if the given query string is init-cm. The case is ignored.
     *
     * @param query The query string. May be null.
     * @return True if the query string is init-cm. False otherwise.
     */
    private boolean isInitQuery(String query) {
        return query != null && query.toLowerCase().equals("init-cm");
    }

    /**
     * Sends out the WSDL (and other referenced documents)
     * in response to the GET requests to URLs like "?wsdl" or "?xsd=2".
     *
     * @param connection
     *      The connection to which the data will be sent.
     * @param adapter
     *      The HttpAdapter that handles the connection.
     *
     * @throws IOException when I/O errors happen
     */
    private void publishWSDL(@NotNull WSHTTPConnection connection, final @NotNull HttpAdapter adapter)
            throws IOException {
        // If the service definition has changed in the meantime, reprocess it
        final ServiceDefinition currentServiceDefinition = adapter.getEndpoint().getServiceDefinition();
        if (adapter.getServiceDefinition() != currentServiceDefinition) {
            adapter.initWSDLMap(currentServiceDefinition);
        }
        adapter.publishWSDL(connection);
    }

}
