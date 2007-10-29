/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.XmlToken;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

/**
 *
 * @author Marek Potociar
 */
final class XmlPolicyModelUnmarshaller extends PolicyModelUnmarshaller {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(XmlPolicyModelUnmarshaller.class);

    /**
     * Creates a new instance of XmlPolicyModelUnmarshaller
     */
    XmlPolicyModelUnmarshaller() {
    // nothing to initialize
    }

    /**
     * See {@link PolicyModelUnmarshaller#unmarshalModel(Object) base method documentation}.
     */
    public PolicySourceModel unmarshalModel(final Object storage) throws PolicyException {
        final XMLEventReader reader = createXMLEventReader(storage);
        PolicySourceModel model = null;

        loop:
        while (reader.hasNext()) {
            try {
                final XMLEvent event = reader.peek();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                    case XMLStreamConstants.COMMENT:
                        reader.nextEvent();
                        break; // skipping the comments and start document events
                    case XMLStreamConstants.CHARACTERS:
                        processCharacters(ModelNode.Type.POLICY, event.asCharacters(), null);
                        // we advance the reader only if there is no exception thrown from
                        // the processCharacters(...) call. Otherwise we don't modify the stream
                        reader.nextEvent();
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        if (NamespaceVersion.resolveAsToken(event.asStartElement().getName()) == XmlToken.Policy) {
                            StartElement rootElement = reader.nextEvent().asStartElement();

                            model = initializeNewModel(rootElement);
                            unmarshalNodeContent(model.getNamespaceVersion(), model.getRootNode(), rootElement.getName(), reader);

                            break loop;
                        } else {
                            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0048_POLICY_ELEMENT_EXPECTED_FIRST()));
                        }
                    default:
                        throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0048_POLICY_ELEMENT_EXPECTED_FIRST()));
                }
            } catch (XMLStreamException e) {
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0068_FAILED_TO_UNMARSHALL_POLICY_EXPRESSION(), e));
            }
        }
        return model;
    }

    private PolicySourceModel initializeNewModel(final StartElement element) throws PolicyException, XMLStreamException {
        PolicySourceModel model;

        final NamespaceVersion nsVersion = NamespaceVersion.resolveVersion(element.getName().getNamespaceURI());

        final Attribute policyName = getAttributeByName(element, nsVersion.asQName(XmlToken.Name));
        final Attribute xmlId = getAttributeByName(element, PolicyConstants.XML_ID);
        Attribute policyId = getAttributeByName(element, PolicyConstants.WSU_ID);

        if (policyId == null) {
            policyId = xmlId;
        } else if (xmlId != null) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0058_MULTIPLE_POLICY_IDS_NOT_ALLOWED()));
        }

        model = PolicySourceModel.createPolicySourceModel(nsVersion,
                (policyId == null) ? null : policyId.getValue(),
                (policyName == null) ? null : policyName.getValue());

        return model;
    }

    private ModelNode addNewChildNode(final NamespaceVersion nsVersion, final ModelNode parentNode, final StartElement childElement) throws PolicyException {
        ModelNode childNode;
        final QName childElementName = childElement.getName();
        if (parentNode.getType() == ModelNode.Type.ASSERTION_PARAMETER_NODE) {
            childNode = parentNode.createChildAssertionParameterNode();
        } else {
            XmlToken token = NamespaceVersion.resolveAsToken(childElementName);

            switch (token) {
                case Policy:
                    childNode = parentNode.createChildPolicyNode();
                    break;
                case All:
                    childNode = parentNode.createChildAllNode();
                    break;
                case ExactlyOne:
                    childNode = parentNode.createChildExactlyOneNode();
                    break;
                case PolicyReference:
                    final Attribute uri = getAttributeByName(childElement, nsVersion.asQName(XmlToken.Uri));
                    if (uri == null) {
                        throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0040_POLICY_REFERENCE_URI_ATTR_NOT_FOUND()));
                    } else {
                        try {
                            final URI reference = new URI(uri.getValue());
                            final Attribute digest = getAttributeByName(childElement, nsVersion.asQName(XmlToken.Digest));
                            PolicyReferenceData refData;
                            if (digest == null) {
                                refData = new PolicyReferenceData(reference);
                            } else {
                                final Attribute digestAlgorithm = getAttributeByName(childElement, nsVersion.asQName(XmlToken.DigestAlgorithm));
                                URI algorithmRef = null;
                                if (digestAlgorithm != null) {
                                    algorithmRef = new URI(digestAlgorithm.getValue());
                                }
                                refData = new PolicyReferenceData(reference, digest.getValue(), algorithmRef);
                            }
                            childNode = parentNode.createChildPolicyReferenceNode(refData);
                        } catch (URISyntaxException e) {
                            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0012_UNABLE_TO_UNMARSHALL_POLICY_MALFORMED_URI(), e));
                        }
                    }
                    break;
                default:
                    if (parentNode.isDomainSpecific()) {
                        childNode = parentNode.createChildAssertionParameterNode();
                    } else {
                        childNode = parentNode.createChildAssertionNode();
                    }
            }
        }

        return childNode;
    }

    private void parseAssertionData(NamespaceVersion nsVersion, String value, ModelNode childNode, final StartElement childElement) throws IllegalArgumentException, PolicyException {
        // finish assertion node processing: create and set assertion data...
        final Map<QName, String> attributeMap = new HashMap<QName, String>();
        boolean optional = false;
        boolean ignorable = false;
        
        final Iterator iterator = childElement.getAttributes();
        while (iterator.hasNext()) {
            final Attribute nextAttribute = (Attribute) iterator.next();
            final QName name = nextAttribute.getName();
            if (attributeMap.containsKey(name)) {
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0059_MULTIPLE_ATTRS_WITH_SAME_NAME_DETECTED_FOR_ASSERTION(nextAttribute.getName(), childElement.getName())));
            } else {
                if (nsVersion.asQName(XmlToken.Optional).equals(name)) {
                    optional = true;
                } else if (nsVersion.asQName(XmlToken.Ignorable).equals(name)) {
                    ignorable = true;
                } else {
                    attributeMap.put(name, nextAttribute.getValue());
                }
            }
        }
        final AssertionData nodeData = new AssertionData(childElement.getName(), value, attributeMap, childNode.getType(), optional, ignorable);

        // check visibility value syntax if present...
        if (nodeData.containsAttribute(PolicyConstants.VISIBILITY_ATTRIBUTE)) {
            final String visibilityValue = nodeData.getAttributeValue(PolicyConstants.VISIBILITY_ATTRIBUTE);
            if (!PolicyConstants.VISIBILITY_VALUE_PRIVATE.equals(visibilityValue)) {
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0004_UNEXPECTED_VISIBILITY_ATTR_VALUE(visibilityValue)));
            }
        }

        childNode.setOrReplaceNodeData(nodeData);
    }

    private Attribute getAttributeByName(final StartElement element,
            final QName attributeName) {
        // call standard API method to retrieve the attribute by name
        Attribute attribute = element.getAttributeByName(attributeName);

        // try to find the attribute without a prefix.
        if (attribute == null) {
            final String localAttributeName = attributeName.getLocalPart();
            final Iterator iterator = element.getAttributes();
            while (iterator.hasNext()) {
                final Attribute nextAttribute = (Attribute) iterator.next();
                final QName aName = nextAttribute.getName();
                final boolean attributeFoundByWorkaround = aName.equals(attributeName) || (aName.getLocalPart().equals(localAttributeName) && (aName.getPrefix() == null || "".equals(aName.getPrefix())));
                if (attributeFoundByWorkaround) {
                    attribute = nextAttribute;
                    break;
                }

            }
        }

        return attribute;
    }

    private String unmarshalNodeContent(final NamespaceVersion nsVersion, final ModelNode node, final QName nodeElementName, final XMLEventReader reader) throws PolicyException {
        StringBuffer valueBuffer = null;

        loop:
        while (reader.hasNext()) {
            try {
                final XMLEvent xmlParserEvent = reader.nextEvent();
                switch (xmlParserEvent.getEventType()) {
                    case XMLStreamConstants.COMMENT:
                        break; // skipping the comments
                    case XMLStreamConstants.CHARACTERS:
                        valueBuffer = processCharacters(node.getType(), xmlParserEvent.asCharacters(), valueBuffer);
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        checkEndTagName(nodeElementName, xmlParserEvent.asEndElement());
                        break loop; // data exctraction for currently processed policy node is done
                    case XMLStreamConstants.START_ELEMENT:
                        final StartElement childElement = xmlParserEvent.asStartElement();

                        ModelNode childNode = addNewChildNode(nsVersion, node, childElement);
                        String value = unmarshalNodeContent(nsVersion, childNode, childElement.getName(), reader);

                        if (childNode.isDomainSpecific()) {
                            parseAssertionData(nsVersion, value, childNode, childElement);
                        }
                        break;
                    default:
                        throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0011_UNABLE_TO_UNMARSHALL_POLICY_XML_ELEM_EXPECTED()));
                }
            } catch (XMLStreamException e) {
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0068_FAILED_TO_UNMARSHALL_POLICY_EXPRESSION(), e));
            }
        }

        return (valueBuffer == null) ? null : valueBuffer.toString();
    }

    /**
     * Method checks if the storage type is supported and transforms it to XMLEventReader instance which is then returned.
     * Throws PolicyException if the transformation is not succesfull or if the storage type is not supported.
     */
    private XMLEventReader createXMLEventReader(final Object storage)
            throws PolicyException {
        if (!(storage instanceof Reader)) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0022_STORAGE_TYPE_NOT_SUPPORTED(storage.getClass().getName())));
        }

        try {
            return XMLInputFactory.newInstance().createXMLEventReader((Reader) storage);
        } catch (XMLStreamException e) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0014_UNABLE_TO_INSTANTIATE_READER_FOR_STORAGE(), e));
        }

    }

    /**
     * Method checks whether the actual name of the end tag is equal to the expected name - the name of currently unmarshalled
     * XML policy model element. Throws exception, if the two FQNs are not equal as expected.
     */
    private void checkEndTagName(final QName expected, final EndElement element) throws PolicyException {
        final QName actual = element.getName();
        if (!expected.equals(actual)) {
            throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0003_UNMARSHALLING_FAILED_END_TAG_DOES_NOT_MATCH(expected, actual)));
        }

    }

    private StringBuffer processCharacters(final ModelNode.Type currentNodeType, final Characters characters,
            final StringBuffer currentValueBuffer)
            throws PolicyException {
        if (characters.isWhiteSpace()) {
            return currentValueBuffer;
        } else {
            final StringBuffer buffer = (currentValueBuffer == null) ? new StringBuffer() : currentValueBuffer;
            final String data = characters.getData();
            if (currentNodeType == ModelNode.Type.ASSERTION || currentNodeType == ModelNode.Type.ASSERTION_PARAMETER_NODE) {
                return buffer.append(data);
            } else {
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0009_UNEXPECTED_CDATA_ON_SOURCE_MODEL_NODE(currentNodeType, data)));
            }

        }
    }
}