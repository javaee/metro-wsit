/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.policy.sourcemodel;

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
    
    /** Creates a new instance of WsdlPolicyModelUnmarshaller */
    XmlPolicyModelUnmarshaller() {
    }
    
    public PolicySourceModel unmarshalModel(final Object storage) throws PolicyException {
        final XMLEventReader reader = createXMLEventReader(storage);
        PolicySourceModel model = null;
        
        loop : while (reader.hasNext()) {
            try {
                final XMLEvent event = reader.peek();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                    case XMLStreamConstants.COMMENT:
                        reader.nextEvent();
                        break; // skipping the comments and start document events
                    case XMLStreamConstants.CHARACTERS:
                        processCharacters(ModelNode.Type.POLICY, event.asCharacters(), null);
                        break;
                    case XMLStreamConstants.START_ELEMENT :
                        if (ModelNode.Type.POLICY.asQName().equals(event.asStartElement().getName())) {
                            final StartElement element = reader.nextEvent().asStartElement();
                            
                            Attribute policyId = getAttributeByName(element, PolicyConstants.WSU_ID);
                            final Attribute xmlId = getAttributeByName(element, PolicyConstants.XML_ID);
                            
                            if (policyId == null) {
                                policyId = xmlId;
                            } else if (xmlId != null) {
                                LOGGER.severe("unmarshalModel", LocalizationMessages.WSP_0058_MULTIPLE_POLICY_IDS_NOT_ALLOWED());
                                throw new PolicyException(LocalizationMessages.WSP_0058_MULTIPLE_POLICY_IDS_NOT_ALLOWED());
                            }
                            
                            final Attribute policyName = getAttributeByName(element, PolicyConstants.POLICY_NAME);
                            
                            model = PolicySourceModel.createPolicySourceModel((policyId == null) ? null : policyId.getValue(), (policyName == null) ? null : policyName.getValue());
                            
                            unmarshalNodeContent(model.getRootNode(), event.asStartElement(), reader);
                            break loop;
                        }
                        // else (this is not a policy tag) -> go to default => throw exception
                    default:
                        LOGGER.severe("unmarshalModel", LocalizationMessages.WSP_0048_POLICY_ELEMENT_EXPECTED_FIRST());
                        throw new PolicyException(LocalizationMessages.WSP_0048_POLICY_ELEMENT_EXPECTED_FIRST());
                }
            } catch (XMLStreamException e) {
                LOGGER.severe("unmarshalModel", LocalizationMessages.WSP_0068_FAILED_TO_UNMARSHALL_POLICY_EXPRESSION(), e);
                throw new PolicyException(LocalizationMessages.WSP_0068_FAILED_TO_UNMARSHALL_POLICY_EXPRESSION(), e);
            }
        }
        return model;
    }
    
    private void unmarshalNodeContent(final ModelNode lastNode, final StartElement lastStartElement, final XMLEventReader reader) throws PolicyException {
        final QName lastElementName = lastStartElement.getName();
        StringBuffer valueBuffer = null;
        
        loop : while (reader.hasNext()) {
            try {
                final XMLEvent xmlParserEvent = reader.nextEvent();
                switch (xmlParserEvent.getEventType()) {
                    case XMLStreamConstants.COMMENT:
                        break; // skipping the comments
                        
                    case XMLStreamConstants.CHARACTERS:
                        valueBuffer = processCharacters(lastNode.getType(), xmlParserEvent.asCharacters(), valueBuffer);
                        break;
                        
                    case XMLStreamConstants.END_ELEMENT:
                        checkEndTagName(lastElementName, xmlParserEvent.asEndElement());
                        break loop; // data exctraction for currently processed policy node is done
                        
                    case XMLStreamConstants.START_ELEMENT:
                        final StartElement childElement = xmlParserEvent.asStartElement();
                        final QName childElementName = childElement.getName();
                        
                        ModelNode childNode;
                        if (lastNode.getType() != ModelNode.Type.ASSERTION_PARAMETER_NODE){
                            if (ModelNode.Type.POLICY.asQName().equals(childElementName)) {
                                childNode = lastNode.createChildPolicyNode();
                            } else if (ModelNode.Type.ALL.asQName().equals(childElementName)) {
                                childNode = lastNode.createChildAllNode();
                            } else if (ModelNode.Type.EXACTLY_ONE.asQName().equals(childElementName)) {
                                childNode = lastNode.createChildExactlyOneNode();
                            } else if (ModelNode.Type.POLICY_REFERENCE.asQName().equals(childElementName)) {
                                final Attribute uri = getAttributeByName(childElement, PolicyReferenceData.ATTRIBUTE_URI);
                                if (uri == null) {
                                    LOGGER.severe("unmarshalNodeContent", LocalizationMessages.WSP_0040_POLICY_REFERENCE_URI_ATTR_NOT_FOUND());
                                    throw new PolicyException(LocalizationMessages.WSP_0040_POLICY_REFERENCE_URI_ATTR_NOT_FOUND());
                                } else {
                                    try {
                                        final URI reference = new URI(uri.getValue());
                                        final Attribute digest = getAttributeByName(childElement, PolicyReferenceData.ATTRIBUTE_DIGEST);
                                        PolicyReferenceData refData;
                                        if (digest == null) {
                                            refData = new PolicyReferenceData(reference);
                                        } else {
                                            final Attribute digestAlgorithm = getAttributeByName(childElement, PolicyReferenceData.ATTRIBUTE_DIGEST_ALGORITHM);
                                            URI algorithmRef = null;
                                            if (digestAlgorithm != null) {
                                                algorithmRef = new URI(digestAlgorithm.getValue());
                                            }
                                            refData = new PolicyReferenceData(reference, digest.getValue(), algorithmRef);
                                        }
                                        childNode = lastNode.createChildPolicyReferenceNode(refData);
                                    } catch (URISyntaxException e) {
                                        LOGGER.severe("unmarshalNodeContent", LocalizationMessages.WSP_0012_UNABLE_TO_UNMARSHALL_POLICY_MALFORMED_URI(), e);
                                        throw new PolicyException(LocalizationMessages.WSP_0012_UNABLE_TO_UNMARSHALL_POLICY_MALFORMED_URI(), e);
                                    }
                                }
                            } else {
                                if (!lastNode.isAssertionRelatedNode()) {
                                    childNode = lastNode.createChildAssertionNode();
                                } else {
                                    childNode = lastNode.createChildAssertionParameterNode();
                                }
                            }
                        } else {
                            childNode = lastNode.createChildAssertionParameterNode();
                        }
                        
                        unmarshalNodeContent(childNode, childElement, reader);
                        break;
                    default:
                        LOGGER.severe("unmarshalNodeContent", LocalizationMessages.WSP_0011_UNABLE_TO_UNMARSHALL_POLICY_XML_ELEM_EXPECTED());
                        throw new PolicyException(LocalizationMessages.WSP_0011_UNABLE_TO_UNMARSHALL_POLICY_XML_ELEM_EXPECTED());
                }
            } catch (XMLStreamException e) {
                LOGGER.severe("unmarshalNodeContent", LocalizationMessages.WSP_0068_FAILED_TO_UNMARSHALL_POLICY_EXPRESSION(), e);
                throw new PolicyException(LocalizationMessages.WSP_0068_FAILED_TO_UNMARSHALL_POLICY_EXPRESSION(), e);
            }
        }
        
        if (lastNode.isAssertionRelatedNode()) {
            // finish assertion node processing: create and set assertion data...
            final Map<QName, String> attributeMap = new HashMap<QName, String>();
            final Iterator iterator = lastStartElement.getAttributes();
            while (iterator.hasNext()) {
                final Attribute a = (Attribute) iterator.next();
                final QName name = a.getName();
                if (attributeMap.containsKey(name)) {
                    LOGGER.severe("unmarshalNodeContent", LocalizationMessages.WSP_0059_MULTIPLE_ATTRS_WITH_SAME_NAME_DETECTED_FOR_ASSERTION(a.getName(), lastElementName));
                    throw new PolicyException(LocalizationMessages.WSP_0059_MULTIPLE_ATTRS_WITH_SAME_NAME_DETECTED_FOR_ASSERTION(a.getName(), lastElementName));
                } else {
                    attributeMap.put(name , a.getValue());
                }
            }
            final AssertionData nodeData = new AssertionData(lastElementName, (valueBuffer != null) ? valueBuffer.toString() : null, attributeMap, lastNode.getType());
            
            // check visibility value syntax if present...
            if (nodeData.containsAttribute(PolicyConstants.VISIBILITY_ATTRIBUTE)) {
                final String visibilityValue = nodeData.getAttributeValue(PolicyConstants.VISIBILITY_ATTRIBUTE);
                if (!PolicyConstants.VISIBILITY_VALUE_PRIVATE.equals(visibilityValue)) {
                    LOGGER.severe("unmarshalNodeContent", LocalizationMessages.WSP_0004_UNEXPECTED_VISIBILITY_ATTR_VALUE(visibilityValue));
                    throw new PolicyException(LocalizationMessages.WSP_0004_UNEXPECTED_VISIBILITY_ATTR_VALUE(visibilityValue));
                }
            }
            
            lastNode.setOrReplaceNodeData(nodeData);
        }
    }
    
    /**
     * Method checks if the storage type is supported and transforms it to XMLEventReader instance which is then returned.
     * Throws PolicyException if the transformation is not succesfull or if the storage type is not supported.
     */
    private XMLEventReader createXMLEventReader(final Object storage) throws PolicyException {
        if (!(storage instanceof Reader)) {
            LOGGER.severe("createXMLEventReader", LocalizationMessages.WSP_0022_STORAGE_TYPE_NOT_SUPPORTED(storage.getClass().getName()));
            throw new PolicyException(LocalizationMessages.WSP_0022_STORAGE_TYPE_NOT_SUPPORTED(storage.getClass().getName()));
        }
        
        try {
            return XMLInputFactory.newInstance().createXMLEventReader((Reader) storage);
        } catch (XMLStreamException e) {
            LOGGER.severe("createXMLEventReader", LocalizationMessages.WSP_0014_UNABLE_TO_INSTANTIATE_READER_FOR_STORAGE(), e);
            throw new PolicyException(LocalizationMessages.WSP_0014_UNABLE_TO_INSTANTIATE_READER_FOR_STORAGE(), e);
        }
    }
    
    /**
     * Method checks whether the actual name of the end tag is equal to the expected name - the name of currently unmarshalled
     * XML policy model element. Throws exception, if the two FQNs are not equal as expected.
     */
    private void checkEndTagName(final QName expected, final EndElement element) throws PolicyException {
        final QName actual = element.getName();
        if (!expected.equals(actual)) {
            LOGGER.severe("checkEndTagName", LocalizationMessages.WSP_0003_UNMARSHALLING_FAILED_END_TAG_DOES_NOT_MATCH(expected, actual));
            throw new PolicyException(LocalizationMessages.WSP_0003_UNMARSHALLING_FAILED_END_TAG_DOES_NOT_MATCH(expected, actual));
        }
    }
    
    private StringBuffer processCharacters(final ModelNode.Type currentNodeType, final Characters characters, final StringBuffer currentValueBuffer) throws PolicyException {
        if (characters.isWhiteSpace()) {
            return currentValueBuffer;
        } else {
            final StringBuffer buffer = (currentValueBuffer == null) ? new StringBuffer() : currentValueBuffer;
            final String data = characters.getData();
            if (currentNodeType == ModelNode.Type.ASSERTION || currentNodeType == ModelNode.Type.ASSERTION_PARAMETER_NODE) {
                return buffer.append(data);
            } else {
                LOGGER.severe("processCharacters", LocalizationMessages.WSP_0009_UNEXPECTED_CDATA_ON_SOURCE_MODEL_NODE(currentNodeType, data));
                throw new PolicyException(LocalizationMessages.WSP_0009_UNEXPECTED_CDATA_ON_SOURCE_MODEL_NODE(currentNodeType, data));
            }
        }
    }
    
    private Attribute getAttributeByName(final StartElement element, final QName attributeName) {
        // call standard API method to retrieve the attribute by name
        Attribute attribute = element.getAttributeByName(attributeName);
        
        // try to find the attribute without a prefix.
        if (attribute == null) {
            final String localAttributeName = attributeName.getLocalPart();
            final Iterator iterator = element.getAttributes();
            while (iterator.hasNext()) {
                final Attribute a = (Attribute) iterator.next();
                final QName aName = a.getName();
                final boolean attributeFoundByWorkaround = aName.equals(attributeName) || (aName.getLocalPart().equals(localAttributeName) && (aName.getPrefix() == null || "".equals(aName.getPrefix())));
                if (attributeFoundByWorkaround) {
                    attribute = a;
                    break;
                }
            }
        }
        
        return attribute;
    }
}
