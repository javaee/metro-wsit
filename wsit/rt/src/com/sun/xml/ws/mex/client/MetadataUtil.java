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
package com.sun.xml.ws.mex.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;

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
    private HttpPoster postClient;
    
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
    InputStream getMetadata(String address, Protocol p) throws IOException {
        String request = getMexWsdlRequest(address, p);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Request message:\n" + request + "\n");
        }
        String contentType = "application/soap+xml"; // soap 1.2
        if (p == Protocol.SOAP_1_1) {
            contentType = "text/xml; charset=\"utf-8\"";
        }
        return postClient.post(request, address, contentType);
    }
    
    private String getMexWsdlRequest(String address, Protocol p) {
        // start with soap 1.2
        String soapPrefix = "s12";
        String soapNamespace = SOAP_1_2;
        if (p == Protocol.SOAP_1_1) {
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

