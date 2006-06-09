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

import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.mex.client.MetadataClient.Protocol;

import static com.sun.xml.ws.mex.MetadataConstants.GET_WXF_REQUEST;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_1;
import static com.sun.xml.ws.mex.MetadataConstants.SOAP_1_2;
import static com.sun.xml.ws.mex.MetadataConstants.WXF_NAMESPACE;

/**
 * Class for making ws-transfer requests.
 */
public class WSTransferUtil {
    
    private HttpPoster postClient;
    
    private static final Logger logger =
        Logger.getLogger(WSTransferUtil.class.getName());
    
    public WSTransferUtil() {
        postClient = new HttpPoster();
    }

    /**
     * Make a mex/wxf request to a server.
     *
     * @param address The address to query for metadata.
     * @return The full response from the server.
     */
    InputStream getMetadata(String address, Protocol p) throws Exception {
        String request = getWxfWsdlRequest(address, p);
        logger.fine("Request message:\n" + request + "\n");
        String contentType = "application/soap+xml"; // soap 1.2
        if (p == Protocol.SOAP_1_1) {
            contentType = "text/xml; charset=\"utf-8\"";
        }
        return postClient.post(request, address, contentType);
    }
    
    private String getWxfWsdlRequest(String address, Protocol p) {
        // start with soap 1.2
        String soapPrefix = "s12";
        String soapNamespace = SOAP_1_2;
        if (p == Protocol.SOAP_1_1) {
            soapPrefix = "soap-env";
            soapNamespace = SOAP_1_1;
        }
        return "<" + soapPrefix + ":Envelope " +
            "xmlns:" + soapPrefix + "='" + soapNamespace + "' " +
            "xmlns:wsa='http://www.w3.org/2005/08/addressing' " +
            "xmlns:wxf='" + WXF_NAMESPACE + "'>" +
            "<" + soapPrefix + ":Header>" +
            "<wsa:Action>" +
            GET_WXF_REQUEST +
            "</wsa:Action>" +
            "<wsa:To>" + address + "</wsa:To>" +
            "<wsa:MessageID>" +
            "uuid:778b135f-3fdf-44b2-b53e-ebaab7441e40" +
            "</wsa:MessageID>" +
            "</" + soapPrefix + ":Header>" +
            "<" + soapPrefix + ":Body/>" +
            "</" + soapPrefix + ":Envelope>";
    }
    
}

