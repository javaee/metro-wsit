/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.runtime.config.MetroConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import com.sun.xml.ws.runtime.config.TubelineMapping;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
// TODO Logging and L10N
// TODO Move the logic of this class directly into MetroConfig class.
class MetroConfigLoader {

    private static final String APP_METRO_CFG_NAME = "metro.xml";
    private static final String DEFAULT_METRO_CFG_NAME = "metro-default.xml";

    private static interface TubeFactoryListResolver {

        TubeFactoryList getFactories(TubelineDefinition td);
    }
    private static final TubeFactoryListResolver ENDPOINT_SIDE_RESOLVER = new TubeFactoryListResolver() {

        public TubeFactoryList getFactories(TubelineDefinition td) {
            return (td != null) ? td.getEndpointSide() : null;
        }
    };
    private static final TubeFactoryListResolver CLIENT_SIDE_RESOLVER = new TubeFactoryListResolver() {

        public TubeFactoryList getFactories(TubelineDefinition td) {
            return (td != null) ? td.getClientSide() : null;
        }
    };
    //
    private final MetroConfig defaultConfig;
    private final URL defaultConfigUrl;
    private final MetroConfig appConfig;
    private final URL appConfigUrl;

    MetroConfigLoader(ResourceLoader loader) {
        this.defaultConfigUrl = locateResource(loader, DEFAULT_METRO_CFG_NAME);
        this.defaultConfig = MetroConfigLoader.loadMetroConfig(defaultConfigUrl);
        // TODO check that these are not null:
        // - defaultConfigUrl, defaultConfig
        // - defaultConfig.getTubelines()
        // - defaultConfig.getTubelines().getDefault()

        this.appConfigUrl = locateResource(loader, APP_METRO_CFG_NAME);
        this.appConfig = MetroConfigLoader.loadMetroConfig(appConfigUrl);
    }

    TubeFactoryList getEndpointSideTubeFactories(URI endpointReference) {
        return getTubeFactories(endpointReference, ENDPOINT_SIDE_RESOLVER);
    }

    TubeFactoryList getClientSideTubeFactories(URI endpointReference) {
        return getTubeFactories(endpointReference, CLIENT_SIDE_RESOLVER);
    }

    private TubeFactoryList getTubeFactories(URI endpointReference, TubeFactoryListResolver resolver) {
        if (appConfig != null && appConfig.getTubelines() != null) {
            for (TubelineMapping mapping : appConfig.getTubelines().getTubelineMappings()) {
                if (mapping.getEndpointRef().equals(endpointReference.toString())) {
                    TubeFactoryList list = resolver.getFactories(getTubeline(appConfig, resolveReference(mapping.getTubelineRef())));
                    if (list != null) {
                        return list;
                    } else {
                        break;
                    }
                }
            }

            if (appConfig.getTubelines().getDefault() != null) {
                TubeFactoryList list = resolver.getFactories(getTubeline(appConfig, resolveReference(appConfig.getTubelines().getDefault())));
                if (list != null) {
                    return list;
                }
            }
        }

        for (TubelineMapping mapping : defaultConfig.getTubelines().getTubelineMappings()) {
            if (mapping.getEndpointRef().equals(endpointReference.toString())) {
                TubeFactoryList list = resolver.getFactories(getTubeline(defaultConfig, resolveReference(mapping.getTubelineRef())));
                if (list != null) {
                    return list;
                } else {
                    break;
                }
            }
        }

        return resolver.getFactories(getTubeline(defaultConfig, resolveReference(defaultConfig.getTubelines().getDefault())));
    }

    TubelineDefinition getTubeline(MetroConfig config, URI tubelineDefinitionUri) {
        if (config != null && config.getTubelines() != null) {
            for (TubelineDefinition td : config.getTubelines().getTubelineDefinitions()) {
                if (td.getName().equals(tubelineDefinitionUri.getFragment())) {
                    return td;
                }
            }
        }

        return null;
    }

    private static URI resolveReference(String reference) {
        try {
            return new URI(reference);
        } catch (URISyntaxException ex) {
            // TODO log properly + L10N
            final String message = String.format("Invalid URI reference: \'%s\'", reference);
            Logger.getLogger(MetroConfigLoader.class.getName()).log(Level.SEVERE, message, ex);
            throw new WebServiceException(message, ex);
        }
    }

    private static URL locateResource(ResourceLoader loader, String resourceName) {
        URL resourceUrl = null;

        try {
            if (loader != null) {
                resourceUrl = loader.getResource(resourceName);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(MetroConfigLoader.class.getName()).log(Level.SEVERE, "Unable to resolve resource URL - see nested exception for more details.", ex);
        }

        if (resourceUrl == null) {
            resourceUrl = loadFromClasspath("META-INF/" + resourceName);
        }

        return resourceUrl;
    }

    private static MetroConfig loadMetroConfig(URL resourceUrl) {
        if (resourceUrl == null) {
            return null;
        }

        MetroConfig result = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MetroConfig.class.getPackage().getName());
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final JAXBElement<MetroConfig> configElement = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(resourceUrl.openStream()), MetroConfig.class);
            result = configElement.getValue();
        } catch (Exception e) {
            // TODO log properly + L10N
            Logger.getLogger(MetroConfigLoader.class.getName()).log(Level.WARNING, String.format("Unable to unmarshall metro config file from location: '%s'", resourceUrl.toString()), e);
        }
        return result;
    }

    private static URL loadFromClasspath(final String resource) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            return ClassLoader.getSystemResource(resource);
        } else {
            return cl.getResource(resource);
        }
    }
}
