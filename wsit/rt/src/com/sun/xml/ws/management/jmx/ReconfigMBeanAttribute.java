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

package com.sun.xml.ws.management.jmx;

//import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.management.ConfigurationAPI;
//import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.management.ManagementFactory;
import com.sun.xml.ws.api.management.ManagedEndpoint;
import com.sun.xml.ws.api.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.management.InitParameters;
import com.sun.xml.ws.management.ManagementConstants;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.io.StringReader;
//import java.net.MalformedURLException;
//import java.net.URL;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
//import javax.xml.stream.XMLInputFactory;
//import javax.xml.stream.XMLStreamException;
//import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Fabian Ritzmann
 */
public class ReconfigMBeanAttribute<T> implements MBeanAttribute {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(ReconfigMBeanAttribute.class);
    public final static String SERVICE_WSDL_ATTRIBUTE_NAME = "serviceWsdl";

    private final ManagedEndpoint<T> managedEndpoint;
    private final EndpointCreationAttributes endpointCreationAttributes;
    private final ClassLoader classLoader;

    private volatile String serviceWsdl;

    public ReconfigMBeanAttribute(ManagedEndpoint<T> endpoint,
            EndpointCreationAttributes creationAttributes,
            ClassLoader classLoader) {
        this.managedEndpoint = endpoint;
        this.endpointCreationAttributes = creationAttributes;
        this.classLoader = classLoader;
    }

    public Object get() {
        return serviceWsdl;
    }

    public String getDescription() {
        return "A service WSDL";
    }

    public OpenType getType() {
        return SimpleType.STRING;
    }

    public void update(Object value) throws InvalidAttributeValueException {
        if (String.class.isAssignableFrom(value.getClass())) {
            update((String) value);
        } else {
            throw LOGGER.logSevereException(new InvalidAttributeValueException(
                      "Cannot set attribute " + SERVICE_WSDL_ATTRIBUTE_NAME + " to a " +
                      value.getClass().getName() +
                      " object, String expected"));
        }
    }

    private void update(String value) throws InvalidAttributeValueException {
        try {
            serviceWsdl = value;
            StringReader input = new StringReader(value);
//            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(input);
//            XMLStreamBuffer buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
//            SDDocumentSource newWsdl = SDDocumentSource.create(new URL("http://test/"), buffer);
            ConfigurationAPI config = ManagementFactory.createConfigurationImpl();
            final InitParameters parameters = new InitParameters()
                    .put(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME, this.managedEndpoint)
                    .put(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME, this.endpointCreationAttributes)
                    .put(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME, this.classLoader)
                    .put(ManagementConstants.CONFIG_READER_PARAMETER_NAME, input);
            config.recreate(parameters);
//        } catch (XMLStreamException cause) {
//            final InvalidAttributeValueException exception = new InvalidAttributeValueException("The value passed into the attribute is not valid XML. You can find a detailed exception message in the server log.");
//            LOGGER.logSevereException(cause);
//            throw LOGGER.logSevereException(exception);
//        } catch (MalformedURLException cause) {
//            final InvalidAttributeValueException exception = new InvalidAttributeValueException("Failed to read XML input. You can find a detailed exception message in the server log.");
//            LOGGER.logSevereException(cause);
//            throw LOGGER.logSevereException(exception);
        } catch (WebServiceException cause) {
            final InvalidAttributeValueException exception = new InvalidAttributeValueException("Failed to reconfigure web service. You can find a detailed exception message in the server log.");
            LOGGER.logSevereException(cause);
            throw LOGGER.logSevereException(exception);
        }
    }
}
