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
import javax.xml.stream.XMLStreamReader;

import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver.Parser;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapMutator;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import org.xml.sax.SAXException;

/**
 * Reads a policy configuration file and returns the WSDL model generated from it.
 */
public final class PolicyConfigParser {
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyConfigParser.class);
    private static final String SERVLET_CONTEXT_CLASSNAME = "javax.servlet.ServletContext";
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    
    
    /**
     * Reads a WSIT config file stored in META-INF or WEB-INF, parses it
     * and returns a PolicyMap. It gives you a chance to register policy map mutators
     * to be able to modify the policy map later.
     *
     * @param configFileIdentifier base for config file name
     * @param container May hold the servlet context if run inside a web container
     * @param mutators to be registered with the new policy map
     * @return A PolicyMap populated from the WSIT config file
     */
    public static PolicyMap parse(String configFileIdentifier, Container container, PolicyMapMutator...  mutators) throws PolicyException {
        logger.entering("parse", container);
        PolicyMap map = null;
        try {
            Object context = null;
            try {
                Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                if (null!=container) {
                    context = container.getSPI(contextClass);
                }
            } catch (ClassNotFoundException e) {
                logger.fine("parse", "Did not find class " + SERVLET_CONTEXT_CLASSNAME + ". We are apparently not running in a container.");
            }
            logger.finest("parse", "context = '" + context + "'");
            
            String cfgFile = PolicyUtils.ConfigFile.generateFullName(configFileIdentifier);
            logger.finest("parse", "config file = '" + cfgFile + "'");
            
            URL configFileUrl = PolicyUtils.ConfigFile.loadAsResource(cfgFile, context);
            
//TODO: remove after NB plugin starts generating differnet names
// BEGIN REMOVE
            if (configFileUrl == null) {
                configFileUrl = PolicyUtils.ConfigFile.loadAsResource("wsit.xml", context);
            }
// END REMOVE

            if (configFileUrl != null) {
                map = parse(configFileUrl, false, mutators);
            }

            return map;
        } finally {
            logger.exiting("parse", map);
        }
    }


    /**
     * Reads a WSIT config from an XMLStreamBuffer, parses it and returns a PolicyMap. It gives you a chance
     * to register policy map mutators to the newly created map.
     *
     * @param configFileUrl URL of the config file resource that should be parsed. Must not be {@code null}.
     * @param isClient must be true if this method is invoked to parse client configuration, false otherwise
     * @param mutators to be registered with the policy map
     *
     * @return A PolicyMap populated from the WSIT config file
     */
    public static PolicyMap parse(URL configFileUrl, boolean isClient, PolicyMapMutator... mutators) throws PolicyException {
        logger.entering("parse", new Object[] {configFileUrl, mutators});
        WSDLModel model = parseModel(configFileUrl, isClient, mutators);
        WSDLPolicyMapWrapper wrapper = model.getExtension(WSDLPolicyMapWrapper.class);

        PolicyMap map = null;
        if (wrapper != null) {
            map = wrapper.getPolicyMap();
        }
        logger.exiting("parse", map);
        return map;
    }
    
    
    /**
     * Reads a WSIT config from an XMLStreamBuffer, parses it and returns a WSDLModel
     * with a PolicyMap attached. It gives you a chance to register policy map mutators
     * to the newly created map.
     *
     * @param configFileUrl URL of the config file resource that should be parsed. Must not be {@code null}.
     * @param isClient must be true if this method is invoked to parse client configuration, false otherwise
     * @param mutators to be registered with the policy map
     *
     * @return A WSDLModel populated from the WSIT config file
     */
    public static WSDLModel parseModel(URL configFileUrl, boolean isClient, PolicyMapMutator... mutators) throws PolicyException {
        logger.entering("parseModel", new Object[] {configFileUrl, mutators});
        WSDLModel model = null;
        try {
            XMLStreamBuffer configFileSource = initConfigFileSource(configFileUrl);

            if (configFileSource == null) {
                throw new PolicyException(LocalizationMessages.BUFFER_NOT_EXIST(configFileUrl));
            }

            SDDocumentSource doc = SDDocumentSource.create(configFileUrl, configFileSource);
            Parser parser =  new Parser(doc);
            model = WSDLModel.WSDLParser.parse(parser, new PolicyConfigResolver(), isClient,
                                               new WSDLParserExtension[] { new PolicyWSDLParserExtension(true, mutators) } );
            return model;
        } catch (XMLStreamException ex) {
            throw new PolicyException(LocalizationMessages.WSDL_IMPORT_FAILED(), ex);
        } catch (IOException ex) {
            throw new PolicyException(LocalizationMessages.WSDL_IMPORT_FAILED(), ex);
        } catch (SAXException ex) {
            throw new PolicyException(LocalizationMessages.WSDL_IMPORT_FAILED(), ex);
        } finally {
            logger.exiting("parseModel", model);
        }
    }

    
    /**
     * Reads a WSIT config from an XMLStreamBuffer, parses it and returns a PolicyMap.
     *
     * @param configFileUrl URL of the config file resource that should be parsed. Must not be {@code null}.
     *
     * @return A PolicyMap populated from the WSIT config file
     */
    /*public static PolicyMap parse(URL configFileUrl) throws PolicyException {
        return parse(configFileUrl, null);
    }*/
    
    private static XMLStreamBuffer initConfigFileSource(URL configFileUrl) throws PolicyException {
        XMLStreamReader reader = null;
        try {
            if (configFileUrl != null) {
                InputStream input = configFileUrl.openStream();
                reader = xmlInputFactory.createXMLStreamReader(input);
                return XMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
            }
            
            return null;
        } catch (XMLStreamException e) {
            throw new PolicyException(LocalizationMessages.READER_CREATE_FAILED(configFileUrl), e);
        } catch (IOException e) {
            throw new PolicyException(LocalizationMessages.URL_OPEN_FAILED(configFileUrl), e);
        }
    }
}
