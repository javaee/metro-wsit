/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.config.metro.parser;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.config.metro.dev.ElementFeatureMapping;
import com.sun.xml.ws.config.metro.dev.FeatureReader;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * Parse metro-webservices.xml.
 *
 * @author Fabian Ritzmann
 */
class MetroWsParser {

    private static final Logger LOGGER = Logger.getLogger(MetroWsParser.class);

    private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private static final String CONFIG_NAMESPACE = "http://metro.dev.java.net/xmlns/metro-webservices";
    private static final QName CONFIG_ROOT_ELEMENT = new QName(CONFIG_NAMESPACE, "metro-webservices");
    private static final QName PORT_COMPONENT_ELEMENT = new QName(CONFIG_NAMESPACE, "port-component");
    private static final QName PORT_COMPONENT_REF_ELEMENT = new QName(CONFIG_NAMESPACE, "port-component-ref");
    private static final QName OPERATION_ELEMENT = new QName(CONFIG_NAMESPACE, "operation");
    private static final QName INPUT_ELEMENT = new QName(CONFIG_NAMESPACE, "input");
    private static final QName OUTPUT_ELEMENT = new QName(CONFIG_NAMESPACE, "output");
    private static final QName FAULT_ELEMENT = new QName(CONFIG_NAMESPACE, "fault");
    private static final QName NAME_ATTRIBUTE = new QName("name");
    private static final QName WSDL_NAME_ATTRIBUTE = new QName("wsdl-name");

    private static final QName TCP_TRANSPORT_ELEMENT_NAME = new QName(CONFIG_NAMESPACE, "tcp-transport");
    private static final QName TUBELINE_ELEMENT_NAME = new QName(CONFIG_NAMESPACE, "tubeline");

    private static final Map<QName, FeatureReader<?>> nameToReader = new HashMap<QName, FeatureReader<?>>();

    static {
        try {
            nameToReader.put(NamespaceVersion.v1_5.asQName(XmlToken.Policy),
                    instantiateFeatureReader("com.sun.xml.ws.policy.config.PolicyFeatureReader"));
            nameToReader.put(TCP_TRANSPORT_ELEMENT_NAME,
                    instantiateFeatureReader("com.sun.xml.ws.transport.tcp.dev.TcpTransportFeatureReader"));
            nameToReader.put(TUBELINE_ELEMENT_NAME,
                    instantiateFeatureReader("com.sun.xml.ws.runtime.config.TubelineFeatureReader"));
            // TODO move ServiceFinder to istack
            final ElementFeatureMapping[] elementFeatureMappings = PolicyUtils.ServiceProvider.load(ElementFeatureMapping.class);
            if (elementFeatureMappings != null) {
                for (int i = 0; i < elementFeatureMappings.length; i++) {
                    final ElementFeatureMapping elementFeatureMapping = elementFeatureMappings[i];
                    final QName elementName = elementFeatureMapping.getElementName();
                    if (nameToReader.containsKey(elementName)) {
                        // TODO: logging message
                        throw LOGGER.logSevereException(new WebServiceException("duplicate registration of reader ... for element ..."));
                    }
                    nameToReader.put(elementName, elementFeatureMapping.getFeatureReader());
                }
            }
        } catch (ClassNotFoundException ex) {
            // TODO logging message
            LOGGER.logSevereException(new WebServiceException("Failed to initialize feature readers", ex));
        } catch (InstantiationException ex) {
            // TODO logging message
            LOGGER.logSevereException(new WebServiceException("Failed to initialize feature readers", ex));
        } catch (IllegalAccessException ex) {
            // TODO logging message
            LOGGER.logSevereException(new WebServiceException("Failed to initialize feature readers", ex));
        }
    }

    private static FeatureReader instantiateFeatureReader(String className)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        return Class.forName(className).asSubclass(FeatureReader.class).newInstance();
    }

    public MetroWsParser() throws WebServiceException {
        if (nameToReader == null || nameToReader.isEmpty()) {
            // TODO logging message
            throw LOGGER.logSevereException(new WebServiceException("Failed to initialize feature readers"));
        }
    }

    public List<ParsedElement> unmarshal(final XMLStreamReader reader) throws WebServiceException {
        try {
            final XMLEventReader eventReader = inputFactory.createXMLEventReader(reader);
            return unmarshal(eventReader);
        } catch (XMLStreamException e) {
            // TODO: logging message
            throw LOGGER.logSevereException(new WebServiceException(e));
        }
    }

    protected List<ParsedElement> unmarshal(final XMLEventReader reader) throws WebServiceException {
        final List<ParsedElement> configElements = new LinkedList<ParsedElement>();
        loop:
        while (reader.hasNext()) {
            try {
                final XMLEvent event = reader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                    case XMLStreamConstants.COMMENT:
                        break; // skipping the comments and start document events
                    case XMLStreamConstants.CHARACTERS:
                        processCharacters(event.asCharacters(), null);
                        // we advance the reader only if there is no exception thrown from
                        // the processCharacters(...) call. Otherwise we don't modify the stream
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        if (CONFIG_ROOT_ELEMENT.equals(event.asStartElement().getName())) {
                            unmarshalComponents(configElements, reader);
                            break loop;
                        }
                        else {
                            // TODO logging message
                            throw LOGGER.logSevereException(new WebServiceException("metro-webservice element expected, instead got " + event));
                        }
                    default:
                        throw LOGGER.logSevereException(new WebServiceException("metro-webservice element expected, instead got " + event));
                }
            } catch (XMLStreamException e) {
                // TODO logging message
                throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal XML document", e));
            }
        }
        return configElements;
    }

    private void unmarshalComponents(final List<ParsedElement> configElements, final XMLEventReader reader)
            throws WebServiceException {
        unmarshal(configElements, CONFIG_ROOT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                try {
                    final StartElement element = reader.peek().asStartElement();
                    if (PORT_COMPONENT_ELEMENT.equals(element.getName())) {
                        final Attribute nameAttribute = element.getAttributeByName(NAME_ATTRIBUTE);
                        if (nameAttribute != null) {
                            reader.next();
                            unmarshalPortComponent(configElements, nameAttribute.getValue(), reader);
                        }
                        else {
                            // logging message
                            throw LOGGER.logSevereException(new WebServiceException("Expected name attribute"));
                        }
                    }
                    else if (PORT_COMPONENT_REF_ELEMENT.equals(element.getName())) {
                        final Attribute nameAttribute = element.getAttributeByName(NAME_ATTRIBUTE);
                        if (nameAttribute != null) {
                            reader.next();
                            unmarshalPortComponentRef(configElements, nameAttribute.getValue(), reader);
                        }
                        else {
                            // logging message
                            throw LOGGER.logSevereException(new WebServiceException("Expected name attribute"));
                        }
                    }
                    else {
                        // TODO logging message
                        throw new WebServiceException("Expected component element, got " + element);
                    }
                } catch (XMLStreamException e) {
                    throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal", e));
                }
            }
        });
    }

    private void unmarshalPortComponent(final List<ParsedElement> configElements, final String componentName,
            final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, PORT_COMPONENT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                try {
                    final StartElement element = reader.peek().asStartElement();
                    if (OPERATION_ELEMENT.equals(element.getName())) {
                        final Attribute nameAttribute = element.getAttributeByName(WSDL_NAME_ATTRIBUTE);
                        if (nameAttribute != null) {
                            reader.next();
                            unmarshalPortComponentOperation(configElements, componentName,
                                    nameAttribute.getValue(), reader);
                        }
                        else {
                            // logging message
                            throw LOGGER.logSevereException(
                                    new WebServiceException("Expected wsdl-name attribute"));
                        }
                    }
                    else {
                        final WebServiceFeature feature = parseElement(reader);
                        configElements.add(ParsedElement.createPortComponentElement(componentName, feature));
                    }
                } catch (XMLStreamException e) {
                    throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal", e));
                }
            }
        });
    }

    private void unmarshalPortComponentRef(final List<ParsedElement> configElements, final String componentName,
            final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, PORT_COMPONENT_REF_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                try {
                    final StartElement element = reader.peek().asStartElement();
                    if (OPERATION_ELEMENT.equals(element.getName())) {
                        final Attribute nameAttribute = element.getAttributeByName(WSDL_NAME_ATTRIBUTE);
                        if (nameAttribute != null) {
                            reader.next();
                            unmarshalPortComponentRefOperation(configElements, componentName,
                                    nameAttribute.getValue(), reader);
                        }
                        else {
                            // logging message
                            throw LOGGER.logSevereException(
                                    new WebServiceException("Expected wsdl-name attribute"));
                        }
                    }
                    else {
                        final WebServiceFeature feature = parseElement(reader);
                        configElements.add(ParsedElement.createPortComponentRefElement(componentName, feature));
                    }
                } catch (XMLStreamException e) {
                    throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal", e));
                }
            }
        });
    }

    private void unmarshalPortComponentOperation(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, OPERATION_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                try {
                    final StartElement element = reader.peek().asStartElement();
                    final QName childName = element.getName();
                    if (INPUT_ELEMENT.equals(childName)) {
                        reader.next();
                        unmarshalPortComponentInput(configElements, componentName, operationName, reader);
                    }
                    else if (OUTPUT_ELEMENT.equals(childName)) {
                        reader.next();
                        unmarshalPortComponentOutput(configElements, componentName, operationName, reader);
                    }
                    else if (FAULT_ELEMENT.equals(childName)) {
                        final Attribute nameAttribute = element.getAttributeByName(WSDL_NAME_ATTRIBUTE);
                        if (nameAttribute != null) {
                            reader.next();
                            unmarshalPortComponentFault(configElements, componentName, operationName,
                                    nameAttribute.getValue(), reader);
                        }
                        else {
                            // logging message
                            throw LOGGER.logSevereException(
                                    new WebServiceException("Expected wsdl-name attribute"));
                        }
                    }
                    else {
                        final WebServiceFeature feature = parseElement(reader);
                        configElements.add(ParsedElement.createPortComponentOperationElement(
                                componentName, operationName, feature));
                    }
                } catch (XMLStreamException e) {
                    throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal", e));
                }
            }
        });
    }

    private void unmarshalPortComponentRefOperation(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, OPERATION_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                try {
                    final StartElement element = reader.peek().asStartElement();
                    final QName childName = element.getName();
                    if (INPUT_ELEMENT.equals(childName)) {
                        reader.next();
                        unmarshalPortComponentRefInput(configElements, componentName, operationName, reader);
                    }
                    else if (OUTPUT_ELEMENT.equals(childName)) {
                        reader.next();
                        unmarshalPortComponentRefOutput(configElements, componentName, operationName, reader);
                    }
                    else if (FAULT_ELEMENT.equals(childName)) {
                        final Attribute nameAttribute = element.getAttributeByName(WSDL_NAME_ATTRIBUTE);
                        if (nameAttribute != null) {
                            reader.next();
                            unmarshalPortComponentRefFault(configElements, componentName, operationName,
                                    nameAttribute.getValue(), reader);
                        }
                        else {
                            // logging message
                            throw LOGGER.logSevereException(
                                    new WebServiceException("Expected wsdl-name attribute"));
                        }
                    }
                    else {
                        final WebServiceFeature feature = parseElement(reader);
                        configElements.add(ParsedElement.createPortComponentRefOperationElement(
                                componentName, operationName, feature));
                    }
                } catch (XMLStreamException e) {
                    throw LOGGER.logSevereException(new WebServiceException("Failed to unmarshal", e));
                }
            }
        });
    }

    private void unmarshalPortComponentInput(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, INPUT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                final WebServiceFeature feature = parseElement(reader);
                configElements.add(ParsedElement.createPortComponentInputElement(
                        componentName, operationName, feature));
            }
        });
    }

    private void unmarshalPortComponentOutput(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, OUTPUT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                final WebServiceFeature feature = parseElement(reader);
                configElements.add(ParsedElement.createPortComponentOutputElement(
                        componentName, operationName, feature));
            }
        });
    }

    private void unmarshalPortComponentFault(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final String faultName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, FAULT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                final WebServiceFeature feature = parseElement(reader);
                configElements.add(ParsedElement.createPortComponentFaultElement(
                        componentName, operationName, faultName, feature));
            }
        });
    }

    private void unmarshalPortComponentRefInput(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, INPUT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                final WebServiceFeature feature = parseElement(reader);
                configElements.add(ParsedElement.createPortComponentRefInputElement(
                        componentName, operationName, feature));
            }
        });
    }

    private void unmarshalPortComponentRefOutput(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, OUTPUT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                final WebServiceFeature feature = parseElement(reader);
                configElements.add(ParsedElement.createPortComponentRefOutputElement(
                        componentName, operationName, feature));
            }
        });
    }

    private void unmarshalPortComponentRefFault(final List<ParsedElement> configElements, final String componentName,
            final String operationName, final String faultName, final XMLEventReader reader) throws WebServiceException {
        unmarshal(configElements, FAULT_ELEMENT, reader, new ElementParser() {
            public void parse(XMLEventReader reader) {
                final WebServiceFeature feature = parseElement(reader);
                configElements.add(ParsedElement.createPortComponentRefFaultElement(
                        componentName, operationName, faultName, feature));
            }
        });
    }

    private void unmarshal(final List<ParsedElement> configElements, final QName endTag,
            final XMLEventReader reader, final ElementParser parser) throws WebServiceException {
        loop:
        while (reader.hasNext()) {
            try {
                final XMLEvent xmlParserEvent = reader.peek();
                switch (xmlParserEvent.getEventType()) {
                    case XMLStreamConstants.COMMENT:
                        reader.next();
                        break; // skipping the comments
                    case XMLStreamConstants.CHARACTERS:
                        processCharacters(reader.nextEvent().asCharacters(), null);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        checkEndTagName(endTag, reader.nextEvent().asEndElement());
                        break loop; // data exctraction for currently processed policy node is done
                    case XMLStreamConstants.START_ELEMENT:
                        parser.parse(reader);
                        break;
                    default:
                        // TODO logging messages
                        throw LOGGER.logSevereException(new WebServiceException("expected XML element"));
                }
            } catch (XMLStreamException e) {
                throw LOGGER.logSevereException(new WebServiceException("unmarshalling failed", e));
            }
        }
    }

    private WebServiceFeature parseElement(final XMLEventReader reader)
            throws WebServiceException {
        try {
            final StartElement element = reader.peek().asStartElement();
            final QName elementName = element.getName();
            final FeatureReader featureReader = nameToReader.get(elementName);
            if (featureReader != null) {
                return featureReader.parse(reader);
            }
            else {
                // TODO logging message
                throw LOGGER.logSevereException(new WebServiceException("unknown element " + element));
            }
        } catch (XMLStreamException e) {
            // TODO logging message
            throw LOGGER.logSevereException(new WebServiceException("failed to parse", e));
        }
    }
    
    /**
     * Method checks whether the actual name of the end tag is equal to the expected name - the name of currently unmarshalled
     * XML policy model element. Throws exception, if the two FQNs are not equal as expected.
     *
     * @param expected The expected element name.
     * @param element The actual element.
     * @throws WebServiceException If the actual element name did not match the expected element.
     */
    private void checkEndTagName(final QName expected, final EndElement element) throws WebServiceException {
        final QName actual = element.getName();
        if (!expected.equals(actual)) {
            // TODO logging message
            throw LOGGER.logSevereException(new WebServiceException("end tag does not match start tag"));
        }

    }

    /**
     * There is currently no CDATA in metro-webservices.xml allowed.
     *
     * @param characters
     * @param currentValueBuffer
     * @return
     * @throws WebServiceException
     */
    private StringBuilder processCharacters(final Characters characters,
            final StringBuilder currentValueBuffer) throws WebServiceException {
        if (characters.isWhiteSpace()) {
            return currentValueBuffer;
        }
        else {
            // TODO: logging message
            throw LOGGER.logSevereException(new WebServiceException("No character data allowed"));
        }
    }


    private interface ElementParser {

        void parse(final XMLEventReader reader) throws WebServiceException;

    }

}
