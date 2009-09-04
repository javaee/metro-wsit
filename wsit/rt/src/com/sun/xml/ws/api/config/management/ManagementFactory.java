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

package com.sun.xml.ws.api.config.management;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.config.management.policy.ManagedServiceAssertion.ImplementationRecord;

import java.util.Collection;
import java.util.LinkedList;
import javax.xml.ws.WebServiceException;

/**
 * Provides methods to create implementations for the Management APIs.
 *
 * @author Fabian Ritzmann
 */
public class ManagementFactory {

    private static final Logger LOGGER = Logger.getLogger(ManagementFactory.class);

    private static final String DEFAULT_COMMUNICATION_SERVER_CLASS_NAME = "com.sun.xml.ws.config.management.jmx.JMXAgent";
    private static final String DEFAULT_CONFIGURATOR_CLASS_NAME = "com.sun.xml.ws.config.management.server.DefaultConfigurator";
    private static final String DEFAULT_CONFIG_READER_CLASS_NAME = "com.sun.xml.ws.config.management.server.JDBCConfigReader";
    private static final String DEFAULT_CONFIG_SAVER_CLASS_NAME = "com.sun.xml.ws.config.management.persistence.JDBCConfigSaver";

    private final ManagedServiceAssertion assertion;
    

    /**
     * Creates a factory instance initialized with the given ManagedServiceAssertion.
     *
     * @param assertion A ManagedServiceAssertion. May not be null.
     */
    public ManagementFactory(ManagedServiceAssertion assertion) {
        this.assertion = assertion;
    }


    /**
     * Finds and returns all CommunicationServer implementations.
     *
     * By default it returns a JMX-based implementation for the CommunicationServer.
     *
     * @param parameters Parameters to initialize the implementations. Must not be null.
     * @return The initialized CommunicationServer implementations.
     * @throws WebServiceException If a CommunicationServer implementation could not
     *   be instantiated or initialized or if no CommunicationServer implementation
     *   was found.
     */
    public Collection<CommunicationServer> createCommunicationImpls(NamedParameters parameters) throws WebServiceException {
        final Collection<CommunicationServer> result = new LinkedList<CommunicationServer>();
        final Collection<ImplementationRecord> communicationServers = this.assertion.getCommunicationServerImplementations();
        if (communicationServers.isEmpty()) {
            final CommunicationServer implementation = instantiateImplementation(
                    DEFAULT_COMMUNICATION_SERVER_CLASS_NAME, CommunicationServer.class);
            implementation.init(parameters);
            result.add(implementation);
        }
        else {
            for (ImplementationRecord record : communicationServers) {
                final CommunicationServer implementation = instantiateImplementation(
                        record, DEFAULT_COMMUNICATION_SERVER_CLASS_NAME, CommunicationServer.class);
                parameters.putAll(record.getParameters());
                implementation.init(parameters);
                result.add(implementation);
            }
        }
        return result;
    }

    /**
     * Finds and returns a Configurator implementation.
     *
     * The default Configurator implementation passes the request into a ConfigSaver.
     *
     * @return A Configurator implementation
     * @throws WebServiceException If a Configurator implementation could not be
     *   instantiated or if no implementation was found.
     */
    public Configurator createConfiguratorImpl() throws WebServiceException {
        return instantiateImplementation(this.assertion.getConfiguratorImplementation(),
                DEFAULT_CONFIGURATOR_CLASS_NAME, Configurator.class);
    }

    /**
     * Finds and returns a ConfigSaver implementation.
     *
     * The default ConfigSaver implementation writes the configuration data to a
     * database with JDBC.
     *
     * @return A ConfigSaver implementation
     * @throws WebServiceException If a ConfigSaver implementation could not be
     *   instantiated or if no implementation was found.
     */
    public ConfigSaver createConfigSaverImpl() throws WebServiceException {
        return instantiateImplementation(this.assertion.getConfigSaverImplementation(),
                DEFAULT_CONFIG_SAVER_CLASS_NAME, ConfigSaver.class);
    }

    /**
     * Finds and returns a ConfigReader implementation.
     *
     * By default it returns an implementation that polls a database with JDBC for
     * changes to the configuration data.
     *
     * @param parameters Parameters to initialize the implementations.
     * @return The initialized ConfigReader implementation.
     * @throws WebServiceException If a ConfigReader implementation could not
     *   be instantiated or initialized or if no ConfigReader implementation
     *   was found.
     */
    public ConfigReader createConfigReaderImpl(NamedParameters parameters) throws WebServiceException {
        final ImplementationRecord record = this.assertion.getConfigReaderImplementation();
        final ConfigReader reader = instantiateImplementation(record,
                DEFAULT_CONFIG_READER_CLASS_NAME, ConfigReader.class);
        if (record != null) {
            parameters.putAll(record.getParameters());
        }
        reader.init(parameters);
        return reader;
    }

    private static <T> T instantiateImplementation(ImplementationRecord record,
            String defaultClassName, Class<T> type) throws WebServiceException {
        final String className;
        if (record != null) {
            final String implementation = record.getImplementation();
            if (implementation != null) {
                className = implementation;
            }
            else {
                className = defaultClassName;
            }
        }
        else {
            className = defaultClassName;
        }
        return instantiateImplementation(className, type);
    }

    private static <T> T instantiateImplementation(String className, Class<T> type) throws WebServiceException {
        try {
            final Class implementation = Class.forName(className);
            return type.cast(implementation.newInstance());
        } catch (ClassNotFoundException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5015_FAILED_LOAD_CLASS(className), e));
        } catch (InstantiationException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5016_FAILED_INSTANTIATE_OBJECT(type.getName()), e));
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new WebServiceException(
                    ManagementMessages.WSM_5016_FAILED_INSTANTIATE_OBJECT(type.getName()), e));
        }
    }

}