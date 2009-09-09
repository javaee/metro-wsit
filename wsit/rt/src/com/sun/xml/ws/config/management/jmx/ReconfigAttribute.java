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

package com.sun.xml.ws.config.management.jmx;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.Configurator;
import com.sun.xml.ws.api.config.management.ManagementFactory;
import com.sun.xml.ws.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.api.config.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.config.management.NamedParameters;
import com.sun.xml.ws.config.management.ManagementConstants;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.config.management.ManagementUtil;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.xml.ws.WebServiceException;

/**
 * Processes the input of new configuration data.
 *
 * @param <T> The endpoint implementation class.
 * 
 * @author Fabian Ritzmann
 */
class ReconfigAttribute<T> {

    private static final Logger LOGGER = Logger.getLogger(ReconfigAttribute.class);
    public final static String SERVICE_WSDL_ATTRIBUTE_NAME = ManagementMessages.RECONFIG_ATTRIBUTE_NAME();

    private final ManagedEndpoint<T> managedEndpoint;
    private final Configurator<T> configurator;
    private final EndpointCreationAttributes endpointCreationAttributes;
    private final ClassLoader classLoader;

    private volatile String newPolicies;

    public ReconfigAttribute(ManagedEndpoint<T> endpoint, Configurator<T> configurator,
            EndpointCreationAttributes creationAttributes, ClassLoader classLoader) {
        this.managedEndpoint = endpoint;
        this.configurator = configurator;
        this.endpointCreationAttributes = creationAttributes;
        this.classLoader = classLoader;
    }

    public Object get() {
        return this.newPolicies;
    }

    public String getDescription() {
        return ManagementMessages.RECONFIG_ATTRIBUTE_DESCRIPTION();
    }

    public OpenType getType() {
        return SimpleType.STRING;
    }

    public void update(Object value) throws InvalidAttributeValueException {
        if (String.class.isAssignableFrom(value.getClass())) {
            update((String) value);
        } else {
            throw LOGGER.logSevereException(new InvalidAttributeValueException(
                    ManagementMessages.WSM_5010_EXPECTED_STRING(SERVICE_WSDL_ATTRIBUTE_NAME, value.getClass().getName())));
        }
    }

    private void update(String value) throws InvalidAttributeValueException {
        try {
            this.newPolicies = value;
            final ManagementFactory factory = new ManagementFactory(ManagementUtil.getAssertion(this.managedEndpoint));
            final NamedParameters parameters = new NamedParameters()
                    .put(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME, this.managedEndpoint)
                    .put(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME, this.endpointCreationAttributes)
                    .put(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME, this.classLoader)
                    .put(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME, value);
            this.configurator.recreate(parameters);
        } catch (WebServiceException cause) {
            final InvalidAttributeValueException exception = new InvalidAttributeValueException(
                    ManagementMessages.WSM_5009_RECONFIGURATION_FAILED());
            LOGGER.logSevereException(cause);
            throw LOGGER.logSevereException(exception);
        }
    }

}