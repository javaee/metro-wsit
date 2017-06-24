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
     * Makes the request to the service. It is expected that this
     * method may throw IOException several times before metadata
     * is returned successfully.
     *
     * @param request A String containing the xml that
     *     will be the payload of the message. 
     * @param address Address of the service.
     * @return The java.io.InputStream returned by the http
     *     url connection.
     */
    InputStream post(final String request, final String address,
        final String contentType) throws IOException {
        
        final URL url = new URL(address);
        final HttpURLConnection conn = createConnection(url);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("SOAPAction", "\"" + GET_REQUEST + "\"");

        final Writer writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(request);
        writer.flush();

        try {
            return conn.getInputStream();
        } catch (IOException ioe) {
            outputErrorStream(conn);
            
            // this exception is caught within the mex code and is logged there
            throw ioe;
        } finally {
            writer.close();
        }
    }
    
    // This method is simply for debugging/error output
    private void outputErrorStream(final HttpURLConnection conn) {
        final InputStream error = conn.getErrorStream();
        if (error != null) {
            final BufferedReader reader = new BufferedReader(
                new InputStreamReader(error));
            try {
                if (logger.isLoggable(ERROR_LOG_LEVEL)) {
                    logger.log(ERROR_LOG_LEVEL,
                        MessagesMessages.MEX_0010_ERROR_FROM_SERVER());
                    String line = reader.readLine();
                    while (line != null) {
                        logger.log(ERROR_LOG_LEVEL, line);
                        line = reader.readLine();
                    }
                    logger.log(ERROR_LOG_LEVEL,
                        MessagesMessages.MEX_0011_ERROR_FROM_SERVER_END());
                }
            } catch (IOException ioe) {
                // This exception has no more impact.
                logger.log(ERROR_LOG_LEVEL,
                    MessagesMessages.MEX_0012_READING_ERROR_STREAM_FAILURE(),
                    ioe);
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    // This exception has no more impact.
                    logger.log(ERROR_LOG_LEVEL,
                        MessagesMessages.MEX_0013_CLOSING_ERROR_STREAM_FAILURE(),
                        ex);
                }
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
    public InputStream makeGetCall(final String address) throws IOException {
        final URL url = new URL(address);
        final HttpURLConnection conn = createConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded"); // taken from wsimport
        try {
            return conn.getInputStream();
        } catch (IOException ioe) {
            outputErrorStream(conn);
            
            // this exception is caught within the mex code and is logged there
            throw ioe;
        }
    }
    
    /*
     * This method creates an http url connection and sets the
     * hostname verifier on it if it's an ssl connection.
     */
    private HttpURLConnection createConnection(final URL url)
        throws IOException {
        
        return (HttpURLConnection) url.openConnection();
    }
    
}
