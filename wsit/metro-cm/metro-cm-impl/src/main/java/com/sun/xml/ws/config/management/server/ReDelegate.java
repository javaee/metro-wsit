/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.config.management.server;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.EndpointCreationAttributes;
import com.sun.xml.ws.metro.api.config.management.ManagedEndpoint;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.config.management.ManagementMessages;
import com.sun.xml.ws.server.EndpointFactory;
//import com.sun.xml.stream.buffer.XMLStreamBuffer;
//import com.sun.xml.ws.metro.api.config.management.NamedParameters;
//import com.sun.xml.ws.api.server.DocumentAddressResolver;
//import com.sun.xml.ws.api.server.PortAddressResolver;
//import com.sun.xml.ws.api.server.SDDocument;
//import com.sun.xml.ws.api.server.SDDocumentSource;
//import com.sun.xml.ws.api.server.ServiceDefinition;
//import com.sun.xml.ws.config.management.ManagementConstants;
//import com.sun.xml.ws.policy.Policy;
//import com.sun.xml.ws.policy.sourcemodel.attach.ExternalAttachmentsUnmarshaller;

import java.util.logging.Level;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
//import java.io.IOException;
//import java.io.StringReader;
//import java.io.StringWriter;
//import java.net.URI;
//import java.util.LinkedList;
//import java.util.Map;
//import javax.xml.namespace.QName;
//import javax.xml.stream.XMLInputFactory;
//import javax.xml.stream.XMLOutputFactory;
//import javax.xml.stream.XMLStreamException;
//import javax.xml.stream.XMLStreamReader;
//import javax.xml.stream.XMLStreamWriter;

/**
 * Create a new WSEndpoint instance and use it to replace the existing WSEndpoint
 * instance in a ManagedEndpoint.
 *
 * @author Fabian Ritzmann, Martin Grebac
 */
public class ReDelegate {

    private static final Logger LOGGER = Logger.getLogger(ReDelegate.class);
//    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
//    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    public static <T> void recreate(ManagedEndpoint<T> managedEndpoint, WebServiceFeature... features) {
//        final ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        try {
//            final ClassLoader classLoader = parameters.get(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME);
//            Thread.currentThread().setContextClassLoader(classLoader);
//            final String newConfig = parameters.get(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME);
//            Map<URI, Policy> urnToPolicy = ExternalAttachmentsUnmarshaller.unmarshal(new StringReader(newConfig));

            WSEndpoint<T> delegate = recreateEndpoint(managedEndpoint, features);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(ManagementMessages.WSM_5092_NEW_ENDPOINT_DELEGATE(delegate));
            }
            managedEndpoint.swapEndpointDelegate(delegate);

        } catch (Throwable e) {
            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5091_ENDPOINT_CREATION_FAILED(), e));
//        } finally {
//            Thread.currentThread().setContextClassLoader(savedClassLoader);
        }
    }
            

    private static <T> WSEndpoint<T> recreateEndpoint(ManagedEndpoint<T> endpoint, WebServiceFeature ... features) {
        endpoint.closeManagedObjectManager();
        EndpointCreationAttributes creationAttributes = endpoint.getCreationAttributes();
        final WSEndpoint<T> result = EndpointFactory.createEndpoint(endpoint.getImplementationClass(),
                creationAttributes.isProcessHandlerAnnotation(),
                creationAttributes.getInvoker(),
                endpoint.getServiceName(),
                endpoint.getPortName(),
                endpoint.getContainer(),
                endpoint.getBinding(),
                null,
                null,
                creationAttributes.getEntityResolver(),
                creationAttributes.isTransportSynchronous());
        result.getComponentRegistry().addAll(endpoint.getComponentRegistry());

        return result;
    }
    
//    public static <T> void recreate(NamedParameters parameters) {
//        final ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
//        try {
//            final ClassLoader classLoader = parameters.get(ManagedEndpoint.CLASS_LOADER_PARAMETER_NAME);
//            Thread.currentThread().setContextClassLoader(classLoader);
//            final String newConfig = parameters.get(ManagementConstants.CONFIGURATION_DATA_PARAMETER_NAME);
//            Map<URI, Policy> urnToPolicy = ExternalAttachmentsUnmarshaller.unmarshal(new StringReader(newConfig));
//
//            final ManagedEndpoint<T> managedEndpoint = parameters.get(ManagedEndpoint.ENDPOINT_INSTANCE_PARAMETER_NAME);
//            final EndpointCreationAttributes creationAttributes = parameters.get(ManagedEndpoint.CREATION_ATTRIBUTES_PARAMETER_NAME);
//            WSEndpoint<T> delegate = recreateEndpoint(managedEndpoint, creationAttributes, urnToPolicy);
//            if (LOGGER.isLoggable(Level.FINE)) {
//                LOGGER.fine(ManagementMessages.WSM_5092_NEW_ENDPOINT_DELEGATE(delegate));
//            }
//            managedEndpoint.swapEndpointDelegate(delegate);
//
//        } catch (Throwable e) {
//            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5091_ENDPOINT_CREATION_FAILED(), e));
//        } finally {
//            Thread.currentThread().setContextClassLoader(savedClassLoader);
//        }
//    }
//
//        private static <T> WSEndpoint<T> recreateEndpoint(WSEndpoint<T> endpoint,
//            EndpointCreationAttributes creationAttributes,
//            Map<URI, Policy> urnToPolicy) {
//        final ServiceDefinition serviceDefinition = endpoint.getServiceDefinition();
//        if (serviceDefinition == null) {
//            throw LOGGER.logSevereException(new WebServiceException(ManagementMessages.WSM_5093_NO_SERVICE_DEFINITION()));
//        }
//
//        final LinkedList<SDDocumentSource> documentSources = new LinkedList<SDDocumentSource>();
//        for (SDDocument doc: serviceDefinition) {
//            if (doc.isWSDL()) {
//                documentSources.add(replacePolicies(doc, urnToPolicy));
//            }
//            else {
//                documentSources.add(convertDocument(doc));
//            }
//        }
//
//        // This allows the new endpoint to register with the same name for monitoring
//        // as the old one.
//        endpoint.closeManagedObjectManager();
//
//        final WSEndpoint<T> result = EndpointFactory.createEndpoint(endpoint.getImplementationClass(),
//                creationAttributes.isProcessHandlerAnnotation(),
//                creationAttributes.getInvoker(),
//                endpoint.getServiceName(),
//                endpoint.getPortName(),
//                endpoint.getContainer(),
//                endpoint.getBinding(),
//                null,
//                documentSources,
//                creationAttributes.getEntityResolver(),
//                creationAttributes.isTransportSynchronous());
//        result.getComponentRegistry().addAll(endpoint.getComponentRegistry());
//
//        return result;
//    }
//
//    private static SDDocumentSource replacePolicies(SDDocument doc, Map<URI, Policy> urnToPolicy) {
//        try {
//            final StringWriter writer = new StringWriter();
//            final XMLStreamWriter xmlWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(writer);
//            doc.writeTo(new MockPortAddressResolver(), new MockDocumentAddressResolver(), xmlWriter);
//            xmlWriter.flush();
//
//            final ManagementWSDLPatcher patcher = new ManagementWSDLPatcher(urnToPolicy);
//            final StringReader reader = new StringReader(writer.toString());
//            final XMLStreamReader xmlReader = XML_INPUT_FACTORY.createXMLStreamReader(reader);
//            final StringWriter newWSDLWriter = new StringWriter();
//            final XMLStreamWriter newWSDLXMLWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(newWSDLWriter);
//            newWSDLXMLWriter.writeStartDocument();
//            patcher.bridge(xmlReader, newWSDLXMLWriter);
//            newWSDLXMLWriter.writeEndDocument();
//            newWSDLXMLWriter.flush();
//
//            final XMLStreamReader newWSDLXMLReader = XML_INPUT_FACTORY.createXMLStreamReader(new StringReader(newWSDLWriter.toString()));
//            final XMLStreamBuffer buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(newWSDLXMLReader);
//            return SDDocumentSource.create(doc.getURL(), buffer);
//        } catch (IOException e) {
//            throw LOGGER.logSevereException(new WebServiceException(
//                    ManagementMessages.WSM_5094_FAILED_POLICIES_REPLACE(doc), e));
//        } catch (XMLStreamException e) {
//            throw LOGGER.logSevereException(new WebServiceException(
//                    ManagementMessages.WSM_5094_FAILED_POLICIES_REPLACE(doc), e));
//        }
//    }
//
//    private static SDDocumentSource convertDocument(final SDDocument doc) {
//        try {
//            // The docs are usually of type SDDocumentImpl, which we can cast
//            // to a SDDocumentSource.
//            if (doc instanceof SDDocumentSource) {
//                return (SDDocumentSource) doc;
//            }
//            final StringWriter writer = new StringWriter();
//            final XMLStreamWriter xmlWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(writer);
//            doc.writeTo(new MockPortAddressResolver(), new MockDocumentAddressResolver(), xmlWriter);
//            writer.flush();
//            final StringReader reader = new StringReader(writer.toString());
//            final XMLStreamReader xmlReader = XML_INPUT_FACTORY.createXMLStreamReader(reader);
//            final XMLStreamBuffer buffer = XMLStreamBuffer.createNewBufferFromXMLStreamReader(xmlReader);
//            return SDDocumentSource.create(doc.getURL(), buffer);
//        } catch (IOException e) {
//            throw LOGGER.logSevereException(new WebServiceException(
//                    ManagementMessages.WSM_5095_FAILED_SDDOCUMENT_CONVERSION(doc), e));
//        } catch (XMLStreamException e) {
//            throw LOGGER.logSevereException(new WebServiceException(
//                    ManagementMessages.WSM_5095_FAILED_SDDOCUMENT_CONVERSION(doc), e));
//        }
//    }
//
//    /**
//     * We can return any address in this class because JAX-WS will later replace
//     * it with a valid address. It is not possible to compute the correct port
//     * address without receiving a GET request.
//     */
//    private static class MockPortAddressResolver extends PortAddressResolver {
//
//        @Override
//        public String getAddressFor(QName serviceName, String portName) {
//            return "temporary address after web service reconfiguration";
//        }
//        
//    }
//
//    /**
//     * We can return any address in this class because JAX-WS will later replace
//     * it with a valid address. It is not possible to compute the correct port
//     * address without receiving a GET request.
//     */
//    private static class MockDocumentAddressResolver implements DocumentAddressResolver {
//
//        public String getRelativeAddressFor(SDDocument current, SDDocument referenced) {
//            return referenced.getURL().toExternalForm();
//        }
//    }
    
}
