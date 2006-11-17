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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.sun.xml.ws.mex.MessagesMessages;

import static com.sun.xml.ws.mex.MetadataConstants.ERROR_LOG_LEVEL;
import static com.sun.xml.ws.mex.MetadataConstants.GET_REQUEST;

/**
 * Class that handles making the HTTP POST request
 * to a service.
 */
public class HttpPoster {
    
    private static final Logger logger =
        Logger.getLogger(HttpPoster.class.getName());

    /**
     * Makes the request to the service.
     *
     * @param request A String containing the xml that
     *     will be the payload of the message. 
     * @param address Address of the service.
     * @return The java.io.InputStream returned by the http
     *     url connection.
     */
    InputStream post(String request, String address, String contentType)
        throws IOException {
        
        URL url = new URL(address);
        HttpURLConnection conn = createConnection(url);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("SOAPAction", GET_REQUEST);

        Writer writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(request);
        writer.flush();

        try {
            return conn.getInputStream();
        } catch (IOException ioe) {
            outputErrorStream(conn);
            throw ioe;
        }
    }
    
    private void outputErrorStream(HttpURLConnection conn) {
        InputStream error = conn.getErrorStream();
        if (error != null) {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(error));
            try {
                if (logger.isLoggable(ERROR_LOG_LEVEL)) {
                    logger.log(ERROR_LOG_LEVEL,
                        MessagesMessages.ERROR_FROM_SERVER());
                    String line = reader.readLine();
                    while (line != null) {
                        logger.log(ERROR_LOG_LEVEL, line);
                        line = reader.readLine();
                    }
                }
            } catch (IOException ioe) {
                // This exception has no more impact.
                logger.log(ERROR_LOG_LEVEL,
                    MessagesMessages.READING_ERROR_STREAM_FAILURE(), ioe);
            }
        }
    }

    /**
     * This method is called by ServiceDescriptorImpl when a
     * metadata response contains a mex location element. The
     * location element contains an address of a metadata document
     * that can be retrieved with an HTTP GET call.
     *
     * @param address The address of the document.
     * @return The java.io.InputStream returned by the http
     *     url connection.
     */
    public InputStream makeGetCall(String address) throws Exception {
        URL url = new URL(address);
        HttpURLConnection conn = createConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded"); // taken from wsimport
        try {
            return conn.getInputStream();
        } catch (IOException ioe) {
            outputErrorStream(conn);
            throw ioe;
        }
    }
    
    /*
     * This method creates an http url connection and sets the
     * hostname verifier on it if it's an ssl connection.
     */
    private HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setHostnameVerifier(
                new HostnameVerifier() {
                public boolean verify(String string, SSLSession sSLSession) {
                    return true;
                }
            });
        }
        return conn;
    }
    
}