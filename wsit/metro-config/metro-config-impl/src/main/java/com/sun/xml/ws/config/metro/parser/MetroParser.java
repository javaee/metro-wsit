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

package com.sun.xml.ws.config.metro.parser;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class MetroParser {

    private static final Logger LOGGER = Logger.getLogger(MetroParser.class);
    
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    // Prefixing with META-INF/ instead of /META-INF/. /META-INF/ is working fine
    // when loading from a JAR file but not when loading from a plain directory.
    private static final String JAR_PREFIX = "META-INF/";
    private static final String WAR_PREFIX = "/WEB-INF/";
    private static final String WEBSERVICES_NAME = "webservices.xml";
    private static final String METRO_WEBSERVICES_NAME = "metro-webservices.xml";

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    /**
     * Private constructor for the utility class to prevent class instantiation
     */
    private MetroParser() {
    }

    // Pulls together the results of MetroWsParser and WsParser

    public static void parse(final Container container) {
        findConfigFile(METRO_WEBSERVICES_NAME, container);

        findConfigFile(WEBSERVICES_NAME, container);
    }

    private static URL findConfigFile(final String configFileName, final Container container)
            throws WebServiceException {
        URL configFileUrl = null;
        try {
            final ResourceLoader resourceLoader = (container != null) ? container.getSPI(ResourceLoader.class) : null;
            if (resourceLoader != null) {
                configFileUrl = resourceLoader.getResource(configFileName);
            }

            if (configFileUrl == null && container != null) {
                final Object context;
                try {
                    final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                    context = container.getSPI(contextClass);
                    if (context != null) {
                        // Move PolicyUtils method into istack
                        configFileUrl = PolicyUtils.ConfigFile.loadFromContext(WAR_PREFIX + configFileName, context);
                    }
                } catch (ClassNotFoundException e) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        // TODO logging message
                        LOGGER.fine("Cannot find servlet context");
                    }
                }
            }

            // If we did not get a config file from the container, fall back to class path
            if (configFileUrl == null) {
                // Try META-INF
                final StringBuilder examinedPath = new StringBuilder(JAR_PREFIX).append(configFileName);
                configFileUrl = PolicyUtils.ConfigFile.loadFromClasspath(examinedPath.toString());
            }

            return configFileUrl;
        } catch (MalformedURLException e) {
            // TODO logging message
            throw LOGGER.logSevereException(new WebServiceException("Failed to load file", e));
        }
    }

    private static XMLStreamReader urlToReader(URL url) throws WebServiceException {
        try {
            return inputFactory.createXMLStreamReader(url.openStream());
        } catch (IOException e) {
            // TODO logging message
            throw LOGGER.logSevereException(new WebServiceException("Failed to load URL", e));
        } catch (XMLStreamException e) {
            // TODO logging message
            throw LOGGER.logSevereException(new WebServiceException("Failed to load URL", e));
        }
    }

}
