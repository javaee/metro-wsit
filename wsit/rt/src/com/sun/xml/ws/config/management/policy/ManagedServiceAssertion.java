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

package com.sun.xml.ws.config.management.policy;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.spi.AssertionCreationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class ManagedServiceAssertion extends PolicyAssertion {

    private static final Logger LOGGER = Logger.getLogger(ManagedServiceAssertion.class);

    private static final QName MANAGED_SERVICE_QNAME = new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ManagedService");
    private static final QName ID_ATTRIBUTE_QNAME = new QName("", "id");
    private static final QName START_ATTRIBUTE_QNAME = new QName("", "start");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JMXConnectorServerEnvironment");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "Entry");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_KEY_ATTRIBUTE_QNAME = new QName("", "key");
    private static final QName JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_VALUE_ATTRIBUTE_QNAME = new QName("", "value");
    private static final QName COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementations");
    private static final QName COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementation");
    private static final QName CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfiguratorImplementation");
    private static final QName CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigSaverImplementation");
    private static final QName CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigReaderImplementation");
    private static final QName JDBC_DATA_SOURCE_NAME_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "JDBCDataSourceName");

    public ManagedServiceAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative)
            throws AssertionCreationException {
        super(data, assertionParameters, nestedAlternative);
        if (!MANAGED_SERVICE_QNAME.equals(data.getName())) {
            throw new AssertionCreationException(data, ManagementMessages.WSM_5011_EXPECTED_MANAGED_SERVICE_ASSERTION());
        }
        if (!data.containsAttribute(ID_ATTRIBUTE_QNAME)) {
            throw new AssertionCreationException(data, ManagementMessages.WSM_5012_MANAGED_SERVICE_MISSING_ID());
        }
    }

    public String getID() {
        return this.getAttributeValue((ID_ATTRIBUTE_QNAME));
    }

    public String getStart() {
        return this.getAttributeValue((START_ATTRIBUTE_QNAME));
    }
    
    public Map<String, String> getJMXConnectorServerEnvironment() {
        final Map<String, String> result = new HashMap<String, String>();
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (JMX_CONNECTOR_SERVER_ENVIRONMENT_PARAMETER_QNAME.equals(parameter.getName())) {
                final Iterator<PolicyAssertion> entries = parameter.getParametersIterator();
                while (entries.hasNext()) {
                    final PolicyAssertion entry = entries.next();
                    if (!JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_PARAMETER_QNAME.equals(entry.getName())) {
                        throw LOGGER.logSevereException(new WebServiceException(
                                ManagementMessages.WSM_5006_UNEXPECTED_ENTRY(entry)));
                    }
                    else {
                        final String key = entry.getAttributeValue(JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_KEY_ATTRIBUTE_QNAME);
                        final String value = entry.getAttributeValue(JMX_CONNECTOR_SERVER_ENVIRONMENT_ENTRY_VALUE_ATTRIBUTE_QNAME);
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }

    public Collection<String> getCommunicationServerImplementations() {
        final Collection<String> result = new LinkedList<String>();
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME.equals(parameter.getName())) {
                final Iterator<PolicyAssertion> implementations = parameter.getParametersIterator();
                while (implementations.hasNext()) {
                    final PolicyAssertion implementation = implementations.next();
                    if (!COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME.equals(implementation.getName())) {
                        throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5013_EXPECTED_XML_TAG(
                                COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, implementation.getName())));
                    }
                    else {
                        String value = parameter.getValue();
                        if (value != null) {
                            result.add(value.trim());
                        }
                        else {
                            throw LOGGER.logSevereException(new WebServiceException(
                                    ManagementMessages.WSM_5014_XML_VALUE_EMPTY(COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME)));
                        }
                    }
                }
            }
        }
        return result;
    }

    public String getConfiguratorImplementation() {
        return getParameterValue(CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME);
    }

    public String getConfigSaverImplementation() {
        return getParameterValue(CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME);
    }

    public String getConfigReaderImplementation() {
        return getParameterValue(CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME);
    }

    public String getJDBCDataSourceName() {
        return getParameterValue(JDBC_DATA_SOURCE_NAME_PARAMETER_QNAME);
    }

    private String getParameterValue(final QName name) {
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (name.equals(parameter.getName())) {
                String value = parameter.getValue();
                if (value != null) {
                    return value.trim();
                }
                else {
                    throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5014_XML_VALUE_EMPTY(name)));
                }
            }
        }
        return null;
    }

}