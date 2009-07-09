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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.Container;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.runtime.config.MetroConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import com.sun.xml.ws.runtime.config.TubelineMapping;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.ws.WebServiceException;

/**
 * This class is responsible for locating and loading Metro configuration files 
 * (both application metro.xml and default metro-default.xml).
 * <p />
 * Once the configuration is loaded the class is able to resolve which tubeline 
 * configuration belongs to each endpoint or endpoint client. This information is
 * then used in {@link TubelineAssemblyController} to construct the list of
 * {@link TubeCreator} objects that are used in the actual tubeline construction.
 * 
 * @author Marek Potociar <marek.potociar at sun.com>
 */
// TODO Move the logic of this class directly into MetroConfig class.
class MetroConfigLoader {

    private static final Logger LOGGER = Logger.getLogger(MetroConfigLoader.class);
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

    MetroConfigLoader(Container container) {
        this(new MetroConfigUrlLoader(container));
    }

    private MetroConfigLoader(ResourceLoader loader) {
        this.defaultConfigUrl = locateResource(DEFAULT_METRO_CFG_NAME, loader);
        if (defaultConfigUrl == null) {
            throw LOGGER.logSevereException(new IllegalStateException("Default metro-default.xml configuration file was not found.")); // TODO L10N
        }

        LOGGER.info(String.format("Default metro-default.xml configuration file located at: '%s'", defaultConfigUrl)); // TODO L10N
        this.defaultConfig = MetroConfigLoader.loadMetroConfig(defaultConfigUrl);
        if (defaultConfig == null) {
            throw LOGGER.logSevereException(new IllegalStateException("Default metro-default.xml configuration file was not loaded")); // TODO L10N
        }
        if (defaultConfig.getTubelines() == null) {
            throw LOGGER.logSevereException(new IllegalStateException("No <tubelines> section found in the default metro-default.xml configuration file")); // TODO L10N
        }
        if (defaultConfig.getTubelines().getDefault() == null) {
            throw LOGGER.logSevereException(new IllegalStateException("No default tubeline is defined in the default metro-default.xml configuration file")); // TODO L10N
        }

        this.appConfigUrl = locateResource(APP_METRO_CFG_NAME, loader);
        if (appConfigUrl != null) {
            LOGGER.info(String.format("Application metro.xml configuration file located at: '%s'", appConfigUrl)); // TODO L10N
            this.appConfig = MetroConfigLoader.loadMetroConfig(appConfigUrl);
        } else {
            LOGGER.config("No application metro.xml configuration file found."); // TODO L10N
            this.appConfig = null;
        }
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
            throw LOGGER.logSevereException(new WebServiceException(String.format("Invalid URI reference: \'%s\'", reference), ex)); // TODO L10N
        }
    }


    private static URL locateResource(String resource, ResourceLoader loader) {
        try {
            return loader.getResource(resource);
        } catch (MalformedURLException ex) {
            LOGGER.severe(String.format("Cannot form a valid URL from the resource name '%s'. For more details see the nested exception.", resource), ex); // TODO L10N
        }
        return null;
    }

    private static MetroConfig loadMetroConfig(@NotNull URL resourceUrl) {
        MetroConfig result = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(MetroConfig.class.getPackage().getName());
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final JAXBElement<MetroConfig> configElement = unmarshaller.unmarshal(XMLInputFactory.newInstance().createXMLStreamReader(resourceUrl.openStream()), MetroConfig.class);
            result = configElement.getValue();
        } catch (Exception e) {
            // TODO L10N
            LOGGER.warning(String.format("Unable to unmarshall metro config file from location: '%s'", resourceUrl.toString()), e); // TODO L10N
        }
        return result;
    }

    private static class MetroConfigUrlLoader extends ResourceLoader {

        Container container; // TODO remove the field together with the code path using it (see below)
        ResourceLoader parentLoader;

        MetroConfigUrlLoader(ResourceLoader parentLoader) {
            this.parentLoader = parentLoader;
        }

        MetroConfigUrlLoader(Container container) {
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
                        LOGGER.fine(String.format("Trying to load '%s' via parent resouce loader '%s'", resource, parentLoader)); // TODO L10N
                    }

                    resourceUrl = parentLoader.getResource(resource);
                }

                if (resourceUrl == null) {
                    resourceUrl = loadViaClassLoaders("META-INF/" + resource);
                }

                if (resourceUrl == null && container != null) {
                    // TODO: we should remove this code path, the config file should be loaded using ResourceLoader only
                    resourceUrl = loadFromServletContext(resource);
                }

                return resourceUrl;
            } finally {
                LOGGER.exiting(resourceUrl);
            }
        }

        private static URL loadViaClassLoaders(final String resource) {
            URL resourceUrl = tryLoadFromClassLoader(resource, Thread.currentThread().getContextClassLoader());
            if (resourceUrl == null) {
                resourceUrl = tryLoadFromClassLoader(resource, MetroConfigLoader.class.getClassLoader());
                if (resourceUrl == null) {
                    return ClassLoader.getSystemResource(resource);
                }
            }

            return resourceUrl;
        }

        private static URL tryLoadFromClassLoader(final String resource, final ClassLoader loader) {
            return (loader != null) ? loader.getResource(resource) : null;
        }

        private URL loadFromServletContext(String resource) throws RuntimeException {
            Object context = null;
            try {
                final Class<?> contextClass = Class.forName("javax.servlet.ServletContext");
                context = container.getSPI(contextClass);
                if (context != null) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Trying to load '%s' via servlet context '%s'", resource, context)); // TODO L10N
                    }
                    try {
                        final Method method = context.getClass().getMethod("getResource", String.class);
                        final Object result = method.invoke(context, "/WEB-INF/" + resource);
                        return URL.class.cast(result);
                    } catch (Exception e) {
                        throw LOGGER.logSevereException(new RuntimeException("Unable to invoke getResource() method on servlet context instance"), e); // TODO L10N
                    }
                }
            } catch (ClassNotFoundException e) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Unable to load javax.servlet.ServletContext class"); // TODO L10N
                }
            }
            return null;
        }
    }
}
