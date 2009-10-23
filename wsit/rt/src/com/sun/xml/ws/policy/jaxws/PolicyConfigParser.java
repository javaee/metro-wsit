/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.policy.jaxws;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;

/**
 * Reads a policy configuration file and returns the WSDL model generated from it.
 *
 * @author Fabian Ritzmann
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class PolicyConfigParser {
    private static final Logger LOGGER = Logger.getLogger(PolicyConfigParser.class);
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    // Prefixing with META-INF/ instead of /META-INF/. /META-INF/ is working fine
    // when loading from a JAR file but not when loading from a plain directory.
    private static final String JAR_PREFIX = "META-INF/";
    private static final String WAR_PREFIX = "/WEB-INF/";

    /**
     * Private constructor for the utility class to prevent class instantiation
     */
    private PolicyConfigParser() {
    }

    /**
     * This is a helper method that returns directly {@link PolicyMap} instance populated
     * from information in WSIT config file. For more details on the whole process see
     * {@link #parseModel(String, Container, PolicyMapMutator[]) parseModel}
     * method.
     *
     * @param configFileIdentifier base of WSIT config file name (web service name for WSIT service
     *        config file or "client" for WSIT client configuration). Must not be {@code null}.
     * @param container if the application is run inside a web container, the container instance
     *        should be passed into this function, in order to get access to the servlet context
     *        that is used to load config file stored in {@code WEB-INF} directory of the application.
     *        May be {@code null}.
     * @param mutators to be registered with the populated {@link PolicyMap} object. May be
     *        ommited if user does not plan to modify the {@link PolicyMap} instance.
     *
     * @return A {@link WSDLModel} with a {@link PolicyMap} object populated with information read
     *         from the WSIT config file.
     * @throws PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     */
    public static PolicyMap parse(final String configFileIdentifier, final Container container, final PolicyMapMutator... mutators)
            throws PolicyException {
        LOGGER.entering(configFileIdentifier, container, mutators);
        PolicyMap map = null;
        try {
            WSDLModel configModel= parseModel(configFileIdentifier, container, mutators);
            //configModel is null in absense of wsit configuration file.
            if(configModel!= null)
                map = configModel.getPolicyMap();
            return map;
        } finally {
            LOGGER.exiting(map);
        }
    }

    /**
     * This is a helper method that returns directly {@link PolicyMap} instance populated
     * from information in WSIT config file. For more details on the whole process see
     * {@link #parseModel(URL, boolean, PolicyMapMutator[]) parseModel}
     * method.
     *
     * @param configFileUrl {@link URL} of the config file resource that should be parsed. Must not be {@code null}.
     * @param isClient must be {@code true} if this method is invoked to parse client configuration, {@code false} otherwise
     * @param mutators to be registered with the populated {@link PolicyMap} object. May be
     *        ommited if user does not plan to modify the {@link PolicyMap} instance.
     *
     * @return A {@link WSDLModel} with a {@link PolicyMap} object populated with information read
     *         from the WSIT config file.
     * @throws PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     * @throws IllegalArgumentException in case {@code configFileUrl} parameter is {@code null}.
     */
    public static PolicyMap parse(final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators)
            throws PolicyException, IllegalArgumentException {
        LOGGER.entering(configFileUrl, isClient, mutators);
        PolicyMap map = null;
        try {
            return map = parseModel(configFileUrl, isClient, mutators).getPolicyMap();
        } finally {
            LOGGER.exiting(map);
        }
    }

    /**
     * The method uses {@code configFileIdentifier} parameter to construct a WSIT config
     * file name according to following pattern:
     * <p />
     * <code>wsit-<i>[configFileIdentifier]</i>.xml</code>
     * <p />
     * After constructing the WSIT config file name, the function tries to find the WSIT
     * config file and read it from the following locations:
     * <ul>
     *      <li>{@code WEB-INF} - for servlet-based web service implementations</li>
     *      <li>{@code META-INF} - for EJB-based web service implementations</li>
     *      <li>{@code classpath} - for web service clients</li>
     * </ul>
     *
     * If the file is found it is parsed and resulting {@link WSDLModel} object containig the
     * populated {@link PolicyMap} instance is returned. If config file is not found,
     * {@code null} is returned as a result of this function call. In case
     * of any other problems that may occur while reading the WSIT config file, a
     * {@link PolicyException} is thrown.
     * <p/>
     * Since {@link PolicyMap} object is immutable as such, this function gives you also a chance
     * to register your own {@link PolicyMapMutator} objects so that you are able to modify the
     * {@link PolicyMap} object later if needed.
     *
     * @param configFileIdentifier base of WSIT config file name (web service name for WSIT service
     *        config file or "client" for WSIT client configuration). Must not be {@code null}.
     * @param container if the application is run inside a web container, the container instance
     *        should be passed into this function, in order to get access to the servlet context
     *        that is used to load config file stored in {@code WEB-INF} directory of the application.
     *        May be {@code null}.
     * @param mutators to be registered with the populated {@link PolicyMap} object. May be
     *        ommited if user does not plan to modify the {@link PolicyMap} instance.
     *
     * @return A {@link WSDLModel} with a {@link PolicyMap} object populated with information read
     *         from the WSIT config file.
     * @throws PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     */
    public static WSDLModel parseModel(final String configFileIdentifier, final Container container, final PolicyMapMutator... mutators)
            throws PolicyException {
        LOGGER.entering(configFileIdentifier, container, mutators);
        final URL configFileUrl = findConfigFile(configFileIdentifier, container);
        final WSDLModel model;
        if (configFileUrl != null) {
            model = parseModel(configFileUrl, PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER.equals(configFileIdentifier), mutators);
            LOGGER.info(LocalizationMessages.WSP_5018_LOADED_WSIT_CFG_FILE(configFileUrl.toExternalForm()));
        }
        else {
            model = null;
        }
        LOGGER.exiting(model);
        return model;
    }

    /**
     * Reads the WSIT config from a file denoted by {@code configFileUrl} parameter.
     * If the file exists it is parsed and resulting {@link WSDLModel} object containig the
     * populated {@link PolicyMap} instance is returned. If config file for given {@link URL}
     * does not exist or in case of any other problems that may occur while reading the
     * WSIT config file, a {@link PolicyException} is thrown.
     *
     * @param configFileUrl {@link URL} of the config file resource that should be parsed. Must not be {@code null}.
     * @param isClient must be {@code true} if this method is invoked to parse client configuration, {@code false} otherwise
     * @param mutators to be registered with the populated {@link PolicyMap} object. May be
     *        ommited if user does not plan to modify the {@link PolicyMap} instance.
     *
     * @return A {@link WSDLModel} with a {@link PolicyMap} object populated with information read
     *         from the WSIT config file.
     * @throws PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     * @throws IllegalArgumentException in case {@code configFileUrl} parameter is {@code null}.
     */
    public static WSDLModel parseModel(final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators)
            throws PolicyException, IllegalArgumentException {
        LOGGER.entering(configFileUrl, isClient, mutators);
        WSDLModel model = null;
        try {
            if (null == configFileUrl) {
                throw LOGGER.logSevereException(new IllegalArgumentException(
                        LocalizationMessages.WSP_5007_FAILED_TO_READ_NULL_WSIT_CFG()));
            }

            return PolicyResourceLoader.getWsdlModel(configFileUrl, isClient);
        } catch (XMLStreamException ex) {
            throw LOGGER.logSevereException(new PolicyException(
                    LocalizationMessages.WSP_5001_WSIT_CFG_FILE_PROCESSING_FAILED(configFileUrl.toString()), ex));
        } catch (IOException ex) {
            throw LOGGER.logSevereException(new PolicyException(
                    LocalizationMessages.WSP_5001_WSIT_CFG_FILE_PROCESSING_FAILED(configFileUrl.toString()), ex));
        } catch (SAXException ex) {
            throw LOGGER.logSevereException(new PolicyException(
                    LocalizationMessages.WSP_5001_WSIT_CFG_FILE_PROCESSING_FAILED(configFileUrl.toString()), ex));
        } finally {
            LOGGER.exiting(model);
        }
    }

    /**
     * Find a WSIT config file based on the {@code configFileIdentifier} parameter
     * according to following pattern:
     * <p />
     * <code>wsit-<i>[configFileIdentifier]</i>.xml</code>
     * <p />
     * After constructing the WSIT config file name, the function tries to find the WSIT
     * config file in the following locations:
     * <ul>
     *      <li>{@code WEB-INF} - for servlet-based web service implementations</li>
     *      <li>{@code META-INF} - for EJB-based web service implementations</li>
     *      <li>{@code classpath} - for web service clients</li>
     * </ul>
     *
     * If the file is found it is parsed and the resulting {@link WSDLModel} object containig the
     * populated {@link PolicyMap} instance is returned. If config file is not found,
     * {@code null} is returned as a result of this method call. In case
     * of any other problems that may occur while locating the WSIT config file, a
     * {@link PolicyException} is thrown.
     * <p/>
     *
     * @param configFileIdentifier base of WSIT config file name (web service name for WSIT service
     *        config file or "client" for WSIT client configuration). Must not be {@code null}.
     * @param container if the application is run inside a web container, the container instance
     *        should be passed into this function, in order to get access to the servlet context
     *        that is used to load config file stored in {@code WEB-INF} directory of the application.
     *        May be {@code null}.
     *
     * @return A {@link URL} pointing to the WSIT configuration file. Null if not found.
     * @throws PolicyException in case of any problems that may occur while locating the WSIT config file.
     */
    public static URL findConfigFile(final String configFileIdentifier, final Container container) throws PolicyException {
        final String configFileName = PolicyUtils.ConfigFile.generateFullName(configFileIdentifier);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(LocalizationMessages.WSP_5011_CONFIG_FILE_IS(configFileName));
        }
        final URL configFileUrl;
        final WsitConfigResourceLoader loader = new WsitConfigResourceLoader(container);
        try {
            configFileUrl = loader.getResource(configFileName);
        } catch (MalformedURLException e) {
            throw LOGGER.logSevereException(new PolicyException(
                    LocalizationMessages.WSP_5021_FAILED_RESOURCE_FROM_LOADER(configFileName, loader), e));
        }
        return configFileUrl;
    }

    /**
     * This ResourceLoader instance is implemented to retrieve a WSIT config file
     * resource URL from underlying parent resource loader. If that strategy does
     * not succeed, the resource loader tries to retrieve a javax.servlet.ServletContext
     * from the container and load the WSIT config file resource URL with it. If that
     * does not succeed either, it tries to locate the WSIT config file resource URL
     * directly from a class path using a current thread's context class loader.
     */
    private static class WsitConfigResourceLoader extends ResourceLoader {

        Container container; // TODO remove the field together with the code path using it (see below)
        ResourceLoader parentLoader;

        WsitConfigResourceLoader(ResourceLoader parentLoader) {
            this.parentLoader = parentLoader;
        }

        WsitConfigResourceLoader(Container container) {
            this((container != null) ? container.getSPI(ResourceLoader.class) : null);
            this.container = container;
        }

        @Override
        public URL getResource(String resource) throws MalformedURLException {
            LOGGER.entering(resource);
            URL resourceUrl = null;
            try {

                if (parentLoader != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(LocalizationMessages.WSP_5020_RESOURCE_FROM_LOADER(resource, parentLoader));
                    }

                    resourceUrl = parentLoader.getResource(resource);
                }

                if (resourceUrl == null && container != null) {
                    // TODO: we should remove this code path, the config file should be loaded using ResourceLoader only
                    Object context = null;
                    try {
                        final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                        context = container.getSPI(contextClass);
                        if (context != null) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine(LocalizationMessages.WSP_5022_RESOURCE_FROM_CONTEXT(resource, context));
                            }
                            resourceUrl = PolicyUtils.ConfigFile.loadFromContext(WAR_PREFIX + resource, context);
                        }
                    } catch (ClassNotFoundException e) {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(LocalizationMessages.WSP_5016_CAN_NOT_FIND_CLASS(SERVLET_CONTEXT_CLASSNAME));
                        }
                    }
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest(LocalizationMessages.WSP_5010_CONTEXT_IS(context));
                    }
                }

                // If we did not get a config file from the container, fall back to class path
                if (resourceUrl == null) {
                    // Try META-INF
                    StringBuilder examinedPath = new StringBuilder(JAR_PREFIX).append(resource);
                    resourceUrl = PolicyUtils.ConfigFile.loadFromClasspath(examinedPath.toString());

                    // Try root of class path
                    if (resourceUrl == null && isClientConfig(resource)) {
                        examinedPath.append(File.pathSeparator).append(resource);
                        resourceUrl = PolicyUtils.ConfigFile.loadFromClasspath(resource);
                    }
                    if (resourceUrl == null && LOGGER.isLoggable(Level.CONFIG)) {
                        LOGGER.config(LocalizationMessages.WSP_5009_COULD_NOT_LOCATE_WSIT_CFG_FILE(resource, examinedPath));
                    }
                }

                return resourceUrl;
            } finally {
                LOGGER.exiting(resourceUrl);
            }
        }

        private boolean isClientConfig(String resource) {
            return ("wsit-" + PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER + ".xml").equals(resource);
        }
    }

}