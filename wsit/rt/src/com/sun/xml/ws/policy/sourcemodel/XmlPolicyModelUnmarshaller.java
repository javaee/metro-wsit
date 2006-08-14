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

/**
 *
 * @author Marek Potociar
 */
final class XmlPolicyModelUnmarshaller extends PolicyModelUnmarshaller {
    /** Creates a new instance of WsdlPolicyModelUnmarshaller */
    XmlPolicyModelUnmarshaller() {
    }
    
    public PolicySourceModel unmarshalModel(Object storage) throws PolicyException {
        XMLEventReader reader = createXMLEventReader(storage);
        PolicySourceModel model = null;
        
        loop : while (reader.hasNext()) {
            try {
                XMLEvent event = reader.peek();
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
                            StartElement element = reader.nextEvent().asStartElement();
                            Attribute policyId = getAttributeByName(element, PolicyConstants.POLICY_ID);
                            Attribute policyName = getAttributeByName(element, PolicyConstants.POLICY_NAME);
                            
                            model = PolicySourceModel.createPolicySourceModel((policyId == null) ? null : policyId.getValue(), (policyName == null) ? null : policyName.getValue());
                            
                            unmarshalNodeContent(model.getRootNode(), event.asStartElement(), reader);
                            break loop;
                        }
                        // else (this is not a policy tag) -> go to default => throw exception
                    default:
                        throw new PolicyException("Failed to unmarshal policy expression. Expected 'Policy' as a first XML element.");
                }
            } catch (XMLStreamException e) {
                throw new PolicyException("Failed to unmarshal policy expression", e);
            }
        }
        return model;
    }
    
    private void unmarshalNodeContent(ModelNode lastNode, StartElement lastStartElement, XMLEventReader reader) throws PolicyException {
        QName lastElementName = lastStartElement.getName();
        StringBuffer valueBuffer = null;
        
        loop : while (reader.hasNext()) {
            try {
                XMLEvent xmlParserEvent = reader.nextEvent();
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
                        StartElement childElement = xmlParserEvent.asStartElement();
                        QName childElementName = childElement.getName();
                        
                        ModelNode childNode;
                        if (lastNode.getType() != ModelNode.Type.ASSERTION_PARAMETER_NODE){
                            if (ModelNode.Type.POLICY.asQName().equals(childElementName)) {
                                childNode = lastNode.createChildPolicyNode();
                            } else if (ModelNode.Type.ALL.asQName().equals(childElementName)) {
                                childNode = lastNode.createChildAllNode();
                            } else if (ModelNode.Type.EXACTLY_ONE.asQName().equals(childElementName)) {
                                childNode = lastNode.createChildExactlyOneNode();
                            } else if (ModelNode.Type.POLICY_REFERENCE.asQName().equals(childElementName)) {
                                Attribute uri = getAttributeByName(childElement, PolicyReferenceData.ATTRIBUTE_URI);
                                if (uri == null) {
                                    throw new PolicyException("Policy reference 'URI' attribute not found");
                                } else {
                                    try {
                                        URI reference = new URI(uri.getValue());
                                        Attribute digest = getAttributeByName(childElement, PolicyReferenceData.ATTRIBUTE_DIGEST);
                                        PolicyReferenceData refData;
                                        if (digest == null) {
                                            refData = new PolicyReferenceData(reference);
                                        } else {
                                            Attribute digestAlgorithm = getAttributeByName(childElement, PolicyReferenceData.ATTRIBUTE_DIGEST_ALGORITHM);
                                            URI algorithmRef = null;
                                            if (digestAlgorithm != null) {
                                                algorithmRef = new URI(digestAlgorithm.getValue());
                                            }
                                            refData = new PolicyReferenceData(reference, digest.getValue(), algorithmRef);
                                        }
                                        childNode = lastNode.createChildPolicyReferenceNode(refData);
                                    } catch (URISyntaxException e) {
                                        throw new PolicyException("Unable to unmarshall policy referenced due to malformed URI value in attribute", e);
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
                        throw new PolicyException("Failed to unmarshal policy expression. Expected XML element.");
                }
            } catch (XMLStreamException e) {
                throw new PolicyException("Failed to unmarshal policy expression", e);
            }
        }
        
        if (lastNode.isAssertionRelatedNode()) {
            // finish assertion node processing: create and set assertion data...
            Map<QName, String> attributeMap = new HashMap<QName, String>();
            Iterator iterator = lastStartElement.getAttributes();
            while (iterator.hasNext()) {
                Attribute a = (Attribute) iterator.next();
                QName name = a.getName();
                if (attributeMap.containsKey(name)) {
                    throw new PolicyException("Multiple attributes with the same name '" + a.getName() + "' detected for assertion '" + lastElementName + "'");
                } else {
                    attributeMap.put(name , a.getValue());
                }
            }
            AssertionData nodeData = new AssertionData(lastElementName, (valueBuffer != null) ? valueBuffer.toString() : null, attributeMap, lastNode.getType());
            
            // check visibility value syntax if present...
            if (nodeData.containsAttribute(PolicyConstants.VISIBILITY_ATTRIBUTE)) {
                String visibilityValue = nodeData.getAttributeValue(PolicyConstants.VISIBILITY_ATTRIBUTE);
                if (!PolicyConstants.VISIBILITY_VALUE_PRIVATE.equals(visibilityValue)) {
                    throw new PolicyException("Unexpected visibility attribute value: '" + visibilityValue + "'");
                }
            }
            
            lastNode.setOrReplaceNodeData(nodeData);
        }
    }
    
    /**
     * Method checks if the storage type is supported and transforms it to XMLEventReader instance which is then returned.
     * Throws PolicyException if the transformation is not succesfull or if the storage type is not supported.
     */
    private XMLEventReader createXMLEventReader(Object storage) throws PolicyException {
        if (!(storage instanceof Reader)) {
            throw new PolicyException("Storage type '" + storage.getClass().getName() + "' not supported");
        }
        
        try {
            return XMLInputFactory.newInstance().createXMLEventReader((Reader) storage);
        } catch (XMLStreamException e) {
            throw new PolicyException("Unable to instantiate XMLEventReader for given storage", e);
        }
    }
    
    /**
     * Method checks whether the actual name of the end tag is equal to the expected name - the name of currently unmarshalled
     * XML policy model element. Throws exception, if the two FQNs are not equal as expected.
     */
    private void checkEndTagName(QName expected, EndElement element) throws PolicyException {
        QName actual = element.getName();
        if (!expected.equals(actual)) {
            throw new PolicyException("Policy model unmarshalling failed: Actual XML end tag does not match current element. Expected tag FQN: '" + expected + "', actual tag FQN: '" + actual + "'");
        }
    }
    
    private StringBuffer processCharacters(ModelNode.Type currentNodeType, Characters characters, StringBuffer currentValueBuffer) throws PolicyException {
        if (characters.isWhiteSpace()) {
            return currentValueBuffer;
        } else {
            StringBuffer buffer = (currentValueBuffer == null) ? new StringBuffer() : currentValueBuffer;
            String data = characters.getData();
            if (currentNodeType == ModelNode.Type.ASSERTION ) {
                return buffer.append(data);
            } else {
                throw new PolicyException("Unexpected character data on current policy source model node '" + currentNodeType + "' : data = '" + data + "'");
            }
        }
    }
    
    private Attribute getAttributeByName(StartElement element, QName attributeName) {
        // call standard API method to retrieve the attribute by name
        Attribute attribute = element.getAttributeByName(attributeName);
        
        // try to find the attribute without a prefix.
        if (attribute == null) {
            String localAttributeName = attributeName.getLocalPart();
            Iterator iterator = element.getAttributes();
            while (iterator.hasNext()) {
                Attribute a = (Attribute) iterator.next();
                QName aName = a.getName();
                boolean attributeFoundByWorkaround = aName.equals(attributeName) || (aName.getLocalPart().equals(localAttributeName) && (aName.getPrefix() == null || "".equals(aName.getPrefix())));
                if (attributeFoundByWorkaround) {
                    attribute = a;
                    break;
                }
            }
        }
        
        return attribute;
    }
}
