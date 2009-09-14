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

package com.sun.xml.ws.api.config.management.policy;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.SimpleAssertion;
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
 * Provides convenience methods to directly access the ManagedService policy
 * assertion parameters.
 *
 * @author Fabian Ritzmann
 */
public class ManagedServiceAssertion extends SimpleAssertion {

    /**
     * The name of the ManagedService policy assertion.
     */
    public static final QName MANAGED_SERVICE_QNAME = new QName(PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ManagedService");
    /**
     * The name of the id attribute of the ManagedService policy assertion.
     */
    public static final QName ID_ATTRIBUTE_QNAME = new QName("", "id");
    /**
     * The name of the start attribute of the ManagedService policy assertion.
     */
    static final QName START_ATTRIBUTE_QNAME = new QName("", "start");
    static final QName COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementations");
    static final QName COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "CommunicationServerImplementation");
    static final QName CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfiguratorImplementation");
    static final QName CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigSaverImplementation");
    static final QName CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "ConfigReaderImplementation");
    static final QName PARAMETER_PARAMETER_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "Parameter");
    static final QName CLASS_NAME_ATTRIBUTE_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "className");
    static final QName NAME_ATTRIBUTE_QNAME = new QName(
            PolicyConstants.SUN_MANAGEMENT_NAMESPACE, "name");
    
    private static final Logger LOGGER = Logger.getLogger(ManagedServiceAssertion.class);


    public ManagedServiceAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters)
            throws AssertionCreationException {
        super(data, assertionParameters);
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
    
    public Collection<ImplementationRecord> getCommunicationServerImplementations() {
        final Collection<ImplementationRecord> result = new LinkedList<ImplementationRecord>();
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (COMMUNICATION_SERVER_IMPLEMENTATIONS_PARAMETER_QNAME.equals(parameter.getName())) {
                final Iterator<PolicyAssertion> implementations = parameter.getParametersIterator();
                if (!implementations.hasNext()) {
                    throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5028_EXPECTED_COMMUNICATION_CHILD()));
                }
                while (implementations.hasNext()) {
                    final PolicyAssertion implementation = implementations.next();
                    if (COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME.equals(implementation.getName())) {
                        result.add(getImplementation(implementation));
                    }
                    else {
                        throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5013_EXPECTED_XML_TAG(
                            COMMUNICATION_SERVER_IMPLEMENTATION_PARAMETER_QNAME, implementation.getName())));
                    }
                }
            }
        }
        return result;
    }

    public ImplementationRecord getConfiguratorImplementation() {
        return findImplementation(CONFIGURATOR_IMPLEMENTATION_PARAMETER_QNAME);
    }

    public ImplementationRecord getConfigSaverImplementation() {
        return findImplementation(CONFIG_SAVER_IMPLEMENTATION_PARAMETER_QNAME);
    }

    public ImplementationRecord getConfigReaderImplementation() {
        return findImplementation(CONFIG_READER_IMPLEMENTATION_PARAMETER_QNAME);
    }

    private ImplementationRecord findImplementation(QName implementationName) {
        final Iterator<PolicyAssertion> parameters = getParametersIterator();
        while (parameters.hasNext()) {
            final PolicyAssertion parameter = parameters.next();
            if (implementationName.equals(parameter.getName())) {
                return getImplementation(parameter);
            }
        }
        return null;
    }

    private ImplementationRecord getImplementation(PolicyAssertion rootParameter) {
        final String className = rootParameter.getAttributeValue(CLASS_NAME_ATTRIBUTE_QNAME);
        final HashMap<QName, String> parameterMap = new HashMap<QName, String>();
        final Iterator<PolicyAssertion> implementationParameters = rootParameter.getParametersIterator();
        final Collection<NestedParameters> nestedParameters = new LinkedList<NestedParameters>();
        while (implementationParameters.hasNext()) {
            final PolicyAssertion parameterAssertion = implementationParameters.next();
            final QName parameterName = parameterAssertion.getName();
            if (parameterAssertion.hasParameters()) {
                final Map<QName, String> nestedParameterMap = new HashMap<QName, String>();
                final Iterator<PolicyAssertion> parameters = parameterAssertion.getParametersIterator();
                while (parameters.hasNext()) {
                    final PolicyAssertion parameter = parameters.next();
                    String value = parameter.getValue();
                    if (value != null) {
                        value = value.trim();
                    }
                    nestedParameterMap.put(parameter.getName(), value);
                }
                nestedParameters.add(new NestedParameters(parameterName, nestedParameterMap));
            }
            else {
                String value = parameterAssertion.getValue();
                if (value != null) {
                    value = value.trim();
                }
                parameterMap.put(parameterName, value);
            }
        }
        return new ImplementationRecord(className, parameterMap, nestedParameters);
    }


    /**
     * Return the implementation class name along with all parameters for the
     * implementation.
     */
    public static class ImplementationRecord {

        private final String implementation;
        private final Map<QName, String> parameters;
        private final Collection<NestedParameters> nestedParameters;

        protected ImplementationRecord(String implementation, Map<QName, String> parameters,
                Collection<NestedParameters> nestedParameters) {
            this.implementation = implementation;
            this.parameters = parameters;
            this.nestedParameters = nestedParameters;
        }

        public String getImplementation() {
            return this.implementation;
        }

        public Map<QName, String> getParameters() {
            return this.parameters;
        }

        public Collection<NestedParameters> getNestedParameters() {
            return this.nestedParameters;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ImplementationRecord other = (ImplementationRecord) obj;
            if ((this.implementation == null) ? (other.implementation != null) : !this.implementation.equals(other.implementation)) {
                return false;
            }
            if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
                return false;
            }
            if (this.nestedParameters != other.nestedParameters &&
                    (this.nestedParameters == null || !this.nestedParameters.equals(other.nestedParameters))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + (this.implementation != null ? this.implementation.hashCode() : 0);
            hash = 53 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
            hash = 53 * hash + (this.nestedParameters != null ? this.nestedParameters.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder("ImplementationRecord: ");
            text.append("implementation = \"").append(this.implementation).append("\", ");
            text.append("parameters = \"").append(this.parameters).append("\", ");
            text.append("nested parameters = \"").append(this.nestedParameters).append("\"");
            return text.toString();
        }

    }


    public static class NestedParameters {

        private final QName name;
        private final Map<QName, String> parameters;

        private NestedParameters(QName name, Map<QName, String> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        public QName getName() {
            return this.name;
        }

        public Map<QName, String> getParameters() {
            return this.parameters;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NestedParameters other = (NestedParameters) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 59 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder("NestedParameters: ");
            text.append("name = \"").append(this.name).append("\", ");
            text.append("parameters = \"").append(this.parameters).append("\"");
            return text.toString();
        }

    }

}