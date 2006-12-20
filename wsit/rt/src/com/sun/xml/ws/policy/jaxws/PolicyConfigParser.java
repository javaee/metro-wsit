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
import java.net.URL;

import javax.xml.stream.XMLStreamException;

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
    public static PolicyMap parse(
            final String configFileIdentifier, final Container container, final PolicyMapMutator...  mutators) throws PolicyException {
        logger.entering("parse", container);
        PolicyMap map = null;
        try {
            Object context = null;
            try {
                final Class<?> contextClass = Class.forName(SERVLET_CONTEXT_CLASSNAME);
                if (null!=container) {
                    context = container.getSPI(contextClass);
                }
            } catch (ClassNotFoundException e) {
                logger.fine("parse", LocalizationMessages.CAN_NOT_FIND_CLASS(SERVLET_CONTEXT_CLASSNAME));
            }
            logger.finest("parse", LocalizationMessages.CONTEXT_IS(context));
            
            final String cfgFile = PolicyUtils.ConfigFile.generateFullName(configFileIdentifier);
            logger.finest("parse", LocalizationMessages.CONFIG_FILE_IS(cfgFile));
            
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
    public static PolicyMap parse(
            final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators) throws PolicyException {
        logger.entering("parse", new Object[] {configFileUrl, mutators});
        final WSDLModel model = parseModel(configFileUrl, isClient, mutators);
        final WSDLPolicyMapWrapper wrapper = model.getExtension(WSDLPolicyMapWrapper.class);
        
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
    public static WSDLModel parseModel(
            final URL configFileUrl, final boolean isClient, final PolicyMapMutator... mutators) throws PolicyException {
        logger.entering("parseModel", new Object[] {configFileUrl, mutators});
        WSDLModel model = null;
        try {
            if (null == configFileUrl) {
                throw new PolicyException(LocalizationMessages.FAILED_TO_READ_NULL_WSIT_CFG());
            }
            
            final SDDocumentSource doc = SDDocumentSource.create(configFileUrl);//, configFileSource);
            final Parser parser =  new Parser(doc);
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
    
}
