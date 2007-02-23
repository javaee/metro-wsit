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

package com.sun.xml.ws.policy.jaxws;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.io.File;

/**
 * Reads a policy configuration file and returns the WSDL model generated from it.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class PolicyConfigParser {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyConfigParser.class);    
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();
    
    
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    // Prefixing with META-INF/ instead of /META-INF/. /META-INF/ is working fine
    // when loading from a JAR file but not when loading from a plain directory.
    private static final String JAR_PREFIX = "META-INF/";
    private static final String WAR_PREFIX = "/WEB-INF/";
    
    /**
     * This is a helper method that returns directly {@link PolicyMap} instance populated
     * from information in WSIT config file. For more details on the whole process see
     * {@link #parseModel(final String configFileIdentifier, final Container container, final PolicyMapMutator...  mutators) parseModel}
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
     * @throw PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     */
    public static PolicyMap parse(final String configFileIdentifier, final Container container, final PolicyMapMutator...  mutators) throws PolicyException {
        LOGGER.entering(configFileIdentifier, container, mutators);
        PolicyMap map = null;
        try {
            return map = extractPolicyMap(parseModel(configFileIdentifier, container, mutators));
        } finally {
            LOGGER.exiting(map);
        }
    }
    
    /**
     * This is a helper method that returns directly {@link PolicyMap} instance populated
     * from information in WSIT config file. For more details on the whole process see
     * {@link #parseModel(final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators) parseModel}
     * method.
     *
     * @param configFileUrl {@link URL} of the config file resource that should be parsed. Must not be {@code null}.
     * @param isClient must be {@code true} if this method is invoked to parse client configuration, {@code false} otherwise
     * @param mutators to be registered with the populated {@link PolicyMap} object. May be
     *        ommited if user does not plan to modify the {@link PolicyMap} instance.
     *
     * @return A {@link WSDLModel} with a {@link PolicyMap} object populated with information read
     *         from the WSIT config file.
     * @throw PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     * @throw IllegalArgumentException in case {@code configFileUrl} parameter is {@code null}.
     */
    public static PolicyMap parse(final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators) throws PolicyException, IllegalArgumentException {
        LOGGER.entering(configFileUrl, isClient, mutators);
        PolicyMap map = null;
        try {
            return map = extractPolicyMap(parseModel(configFileUrl, isClient, mutators));
        } finally {
            LOGGER.exiting(map);
        }
    }
    
    /**
     * Utility method that tries to retrieve a {@link PolicyMap} object from a given
     * {@link WSDLModel}. When succesfull, {@link PolicyMap} instance is returned,
     * otherwise result is {@code null}.
     *
     * @param model A {@link WSDLModel} (possibly) with a {@link PolicyMap} object
     *        populated with information read from the WSIT config file. May be {@code null};
     *        in that case, {@code null} is returned as a result of this function call.
     *
     * @return {@link PolicyMap} instance retrieved from a given {@link WSDLModel}
     *         if successful, {@code null} otherwise.
     */
    public static PolicyMap extractPolicyMap(WSDLModel model) {
        LOGGER.entering(model);
        PolicyMap result = null;
        try {
            if (model != null) {
                final WSDLPolicyMapWrapper wrapper = model.getExtension(WSDLPolicyMapWrapper.class);
                
                if (wrapper != null) {
                    result = wrapper.getPolicyMap();
                }
            }
            return result;
        } finally {
            LOGGER.exiting(result);
        }
    }
    
    /**
     * The function uses {@code configFileIdentifier} parameter to construct a WSIT config
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
     * populated {@link PolicyMap} instance is returned. If config file is not found, warning
     * message is logged and {@code null} is returned as a result of this function call. In case
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
     * @throw PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     */
    public static WSDLModel parseModel(final String configFileIdentifier, final Container container, final PolicyMapMutator...  mutators) throws PolicyException {
        LOGGER.entering(configFileIdentifier, container, mutators);
        WSDLModel model = null;
        try {
            final String configFileName = PolicyUtils.ConfigFile.generateFullName(configFileIdentifier);
            LOGGER.finest(LocalizationMessages.WSP_1037_CONFIG_FILE_IS(configFileName));
            
            Object context = null;
            if (container != null) {
                try {
                    final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                    context = container.getSPI(contextClass);
                } catch (ClassNotFoundException e) {
                    LOGGER.fine(LocalizationMessages.WSP_1043_CAN_NOT_FIND_CLASS(SERVLET_CONTEXT_CLASSNAME));
                }
                LOGGER.finest(LocalizationMessages.WSP_1036_CONTEXT_IS(context));
                
            }
            
            URL configFileUrl = null;
            final boolean isClientConfig = PolicyConstants.CLIENT_CONFIGURATION_IDENTIFIER.equals(configFileIdentifier);
            String examinedPath;
            if (context == null || isClientConfig) {
                examinedPath = JAR_PREFIX + configFileName;
                configFileUrl = PolicyUtils.ConfigFile.loadFromClasspath(examinedPath);
                if (configFileUrl == null && isClientConfig) {
                    examinedPath = examinedPath + File.pathSeparator + " " + configFileName;
                    configFileUrl = PolicyUtils.ConfigFile.loadFromClasspath(configFileName);
                }
            } else {
                examinedPath = WAR_PREFIX + configFileName;
                configFileUrl = PolicyUtils.ConfigFile.loadFromContext(examinedPath, context);
            }
            
            if (configFileUrl != null) {
                model = parseModel(configFileUrl, isClientConfig, mutators);
                LOGGER.info(LocalizationMessages.WSP_1049_LOADED_WSIT_CFG_FILE(configFileUrl.toExternalForm()));
            } else {
                LOGGER.config(LocalizationMessages.WSP_1035_COULD_NOT_LOCATE_WSIT_CFG_FILE(configFileIdentifier, examinedPath));
            }
            
            return model;
        } finally {
            LOGGER.exiting(model);
        }
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
     * @throw PolicyException in case of any problems that may occur while reading WSIT config file
     *        and constructing the {@link WSDLModel} object or populating {@link PolicyMap} instance.
     * @throw IllegalArgumentException in case {@code configFileUrl} parameter is {@code null}.
     */
    public static WSDLModel parseModel(final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators) throws PolicyException, IllegalArgumentException {
        LOGGER.entering(configFileUrl, isClient, mutators);
        WSDLModel model = null;
        InputStream configFileIS = null;
        try {
            if (null == configFileUrl) {
                throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_1028_FAILED_TO_READ_NULL_WSIT_CFG()));
            }
            
            configFileIS = configFileUrl.openStream();
            final XMLStreamBuffer buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(XML_INPUT_FACTORY.createXMLStreamReader(configFileIS));
            final SDDocumentSource doc = SDDocumentSource.create(configFileUrl, buffer);
            final XMLEntityResolver.Parser parser =  new XMLEntityResolver.Parser(doc);
            model = WSDLModel.WSDLParser.parse(
                    parser,
                    new PolicyConfigResolver(),
                    isClient,
                    new WSDLParserExtension[] { new PolicyWSDLParserExtension(true, mutators) }
            );
            
            return model;
        } catch (XMLStreamException ex) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_1002_WSIT_CFG_FILE_PROCESSING_FAILED(configFileUrl.toString()), ex));
        } catch (IOException ex) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_1002_WSIT_CFG_FILE_PROCESSING_FAILED(configFileUrl.toString()), ex));
        } catch (SAXException ex) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_1002_WSIT_CFG_FILE_PROCESSING_FAILED(configFileUrl.toString()), ex));
        } finally {
            PolicyUtils.IO.closeResource(configFileIS);
            LOGGER.exiting(model);
        }
    }
}

