/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.mex.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.mex.client.MetadataClient.Protocol;

import static com.sun.xml.ws.mex.MetadataConstants.GET_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_1;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_2;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_ANON;
import static com.sun.xml.ws.mex.MetadataConstants.WSA_PREFIX;

/**
 * Class for making mex Get requests (which are the same
 * as ws-transfer Get requests). Currently only http requests
 * are supported.
 */
public class MetadataUtil {
    
    // the transport-specific code is (mostly) here
    private final HttpPoster postClient;
    
    private static final Logger logger =
        Logger.getLogger(MetadataUtil.class.getName());
    
    public MetadataUtil() {
        postClient = new HttpPoster();
    }

    /**
     * Make a mex/wxf request to a server.
     *
     * @param address The address to query for metadata.
     * @return The full response from the server.
     */
    InputStream getMetadata(final String address,
        final Protocol protocol) throws IOException {
        
        final String request = getMexWsdlRequest(address, protocol);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Request message:\n" + request + "\n");
        }
        String contentType = "application/soap+xml"; // soap 1.2
        if (protocol == Protocol.SOAP_1_1) {
            contentType = "text/xml; charset=\"utf-8\"";
        }
        return postClient.post(request, address, contentType);
    }
    
    private String getMexWsdlRequest(final String address,
        final Protocol protocol) {
        
        // start with soap 1.2
        String soapPrefix = "s12";
        String soapNamespace = SOAP_1_2;
        if (protocol == Protocol.SOAP_1_1) {
            soapPrefix = "soap-env";
            soapNamespace = SOAP_1_1;
        }
        return "<" + soapPrefix + ":Envelope " +
            "xmlns:" + soapPrefix + "='" + soapNamespace + "' " +
            "xmlns:" + WSA_PREFIX + "='" + AddressingVersion.W3C.nsUri + "'>" +
            "<" + soapPrefix + ":Header>" +
            "<" + WSA_PREFIX + ":Action>" +
            GET_REQUEST +
            "</" + WSA_PREFIX + ":Action>" +
            "<" + WSA_PREFIX + ":To>" + address + "</" + WSA_PREFIX + ":To>" +
            "<" + WSA_PREFIX + ":ReplyTo><" + WSA_PREFIX + ":Address>" +
            WSA_ANON +
            "</" + WSA_PREFIX + ":Address></" + WSA_PREFIX + ":ReplyTo>" +
            "<" + WSA_PREFIX + ":MessageID>" +
            "uuid:778b135f-3fdf-44b2-b53e-ebaab7441e40" +
            "</" + WSA_PREFIX + ":MessageID>" +
            "</" + soapPrefix + ":Header>" +
            "<" + soapPrefix + ":Body/>" +
            "</" + soapPrefix + ":Envelope>";
    }
    
}

