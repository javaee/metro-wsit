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

import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.privateutil.PolicyUtils;

/**
 * Wrapper class for possible data that each 'assertion' and 'assertion parameter content' policy source model node may
 * have attached.
 * <p/>
 * These data, when stored in an 'assertion' model node, are intended to be used as input parameter when creating
 * {@link com.sun.xml.ws.policy.PolicyAssertion} objects via {@link com.sun.xml.ws.policy.spi.PolicyAssertionCreator}
 * implementations.
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public final class AssertionData implements Cloneable {
    private QName name;
    private String value;
    private HashMap<QName, String> attributes = new HashMap<QName, String>();
    private ModelNode.Type type;
    
    /**
     * Constructs assertion data wrapper instance for an assertion that does not contain any value nor any attributes.
     *
     * @param name the FQN of the assertion or assertion parameter
     *
     * @throws IllegalArgumentException in case the {@code type} parameter is not {@link ModelNode.Type.ASSERTION} or {@link ModelNode.Type.ASSERTION_PARAMETER_NODE}
     */
    public static AssertionData createAssertionData(final QName name) throws IllegalArgumentException {
        return new AssertionData(name, null, null, ModelNode.Type.ASSERTION);
    }
    
    /**
     * Constructs assertion data wrapper instance for an assertion or assertion parameter that contains a value or
     *
     * @param name the FQN of the assertion or assertion parameter
     * @param value a {@link String} representation of model node value
     * @param attributes map of model node's &lt;attribute name, attribute value&gt; pairs
     *
     * @throws IllegalArgumentException in case the {@code type} parameter is not 'ModelNode.Type.ASSERTION' or 'ModelNode.Type.ASSERTION_PARAMETER_NODE'
     */
    public static AssertionData createAssertionParameterData(final QName name) throws IllegalArgumentException {
        return new AssertionData(name, null, null, ModelNode.Type.ASSERTION_PARAMETER_NODE);
    }
    
    /**
     * Constructs assertion data wrapper instance for an assertion that does not contain any value nor any attributes.
     *
     * @param name the FQN of the assertion or assertion parameter
     *
     * @throws IllegalArgumentException in case the {@code type} parameter is not {@link ModelNode.Type.ASSERTION} or {@link ModelNode.Type.ASSERTION_PARAMETER_NODE}
     */
    public static AssertionData createAssertionData(final QName name, final String value, final Map<QName, String> attributes) throws IllegalArgumentException {
        return new AssertionData(name, value, attributes, ModelNode.Type.ASSERTION);
    }
    
    /**
     * Constructs assertion data wrapper instance for an assertion or assertion parameter that contains a value or
     *
     * @param name the FQN of the assertion or assertion parameter
     * @param value a {@link String} representation of model node value
     * @param attributes map of model node's &lt;attribute name, attribute value&gt; pairs
     *
     * @throws IllegalArgumentException in case the {@code type} parameter is not 'ModelNode.Type.ASSERTION' or 'ModelNode.Type.ASSERTION_PARAMETER_NODE'
     */
    public static AssertionData createAssertionParameterData(final QName name, final String value, final Map<QName, String> attributes) throws IllegalArgumentException {
        return new AssertionData(name, value, attributes, ModelNode.Type.ASSERTION_PARAMETER_NODE);
    }
    
    /**
     * Constructs assertion data wrapper instance for an assertion or assertion parameter that contains a value or
     * some attributes. Whether the data wrapper is constructed for assertion or assertion parameter node is distinguished by
     * the supplied {@code type} parameter.
     *
     * @param name the FQN of the assertion or assertion parameter
     * @param value a {@link String} representation of model node value
     * @param attributes map of model node's &lt;attribute name, attribute value&gt; pairs
     * @param type specifies whether the data will belong to the assertion or assertion parameter node. This is
     *             a workaround solution that allows us to transfer this information about the owner node to
     *             a policy assertion instance factory without actualy having to touch the {@link PolicyAssertionCreator}
     *             interface and protected {@link PolicyAssertion} constructors.
     *
     * @throws IllegalArgumentException in case the {@code type} parameter is not 'ModelNode.Type.ASSERTION' or 'ModelNode.Type.ASSERTION_PARAMETER_NODE'
     */
    AssertionData(QName name, String value, Map<QName, String> attributes, ModelNode.Type type) throws IllegalArgumentException {
        this.name = name;
        this.value = value;
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
        setModelNodeType(type);
    }
    
    private void setModelNodeType(final ModelNode.Type type) throws IllegalArgumentException {
        if (type == ModelNode.Type.ASSERTION || type == ModelNode.Type.ASSERTION_PARAMETER_NODE) {
            this.type = type;
        } else {
            throw new IllegalArgumentException(
                    LocalizationMessages.CANNOT_CREATE_ASSERTION_BAD_TYPE(type, ModelNode.Type.ASSERTION, ModelNode.Type.ASSERTION_PARAMETER_NODE));
        }
    }
    
    /**
     * TODO: javadoc
     */
    AssertionData(final AssertionData data) {
        this.name = data.name;
        this.value = data.value;
        if (attributes != null) {
            this.attributes.putAll(data.attributes);
        }
        this.type = data.type;
    }
    
    protected AssertionData clone() throws CloneNotSupportedException {
        final AssertionData clone = (AssertionData) super.clone();
        
        clone.attributes = new HashMap<QName, String>(this.attributes);
        
        return clone;
    }
    
    /**
     * TODO: javadoc
     */
    public boolean containsAttribute(final QName name) {
        synchronized (attributes) {
            return attributes.containsKey(name);
        }
    }
    
    
    /**
     * An {@code Object.equals(Object obj)} method override.
     */
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof AssertionData))
            return false;
        
        boolean result = true;
        final AssertionData that = (AssertionData) obj;
        
        result = result && this.name.equals(that.name);
        result = result && ((this.value == null) ? that.value == null : this.value.equals(that.value));
        synchronized (attributes) {
            result = result && ((this.attributes == null) ? that.attributes == null : this.attributes.equals(that.attributes));
        }
        
        return result;
    }
    
    
    /**
     * TODO: javadoc
     */
    public String getAttributeValue(final QName name) {
        synchronized (attributes) {
            return attributes.get(name);
        }
    }
    
    
    /**
     * Returns the disconnected map of attributes attached to the assertion.
     * <p/>
     * 'Disconnected' means, that the result of this method will not be synchronized with any consequent assertion's attribute modification. It is
     * also important to notice that a manipulation with returned set of attributes will not have any effect on the actual assertion's
     * attributes.
     *
     * @return disconnected map of attributes attached to the assertion.
     */
    public Map<QName, String> getAttributes() {
        synchronized (attributes) {
            return new HashMap<QName, String>(attributes);
        }
    }
    
    
    /**
     * Returns the disconnected set of attributes attached to the assertion. Each attribute is represented as a single
     * {@code Map.Entry<attributeName, attributeValue>} element.
     * <p/>
     * 'Disconnected' means, that the result of this method will not be synchronized with any consequent assertion's attribute modification. It is
     * also important to notice that a manipulation with returned set of attributes will not have any effect on the actual assertion's
     * attributes.
     *
     * @return disconnected set of attributes attached to the assertion.
     */
    public Set<Map.Entry<QName, String>> getAttributesSet() {
        synchronized (attributes) {
            return new HashSet<Map.Entry<QName, String>>(attributes.entrySet());
        }
    }
    
    
    /**
     * Returns the name of the assertion.
     *
     * @return assetion's name
     */
    public QName getName() {
        return name;
    }
    
    
    /**
     * Returns the value of the assertion.
     *
     * @return assetion's value
     */
    public String getValue() {
        return value;
    }
    
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + this.name.hashCode();
        result = 37 * result + ((this.value != null) ? this.value.hashCode() : 0);
        synchronized (attributes) {
            result = 37 * result + ((this.attributes != null) ? this.attributes.hashCode() : 0);
        }
        return result;
    }
    
    
    /**
     * Method specifies whether the assertion data contain proprietary visibility element set to "private" value.
     *
     * @return {@code 'true'} if the attribute is present and set properly (i.e. the node containing this assertion data instance should
     * not be marshalled int generated WSDL documents). Returns {@code false} otherwise.
     */
    public boolean isPrivateAttributeSet() {
        return PolicyConstants.VISIBILITY_VALUE_PRIVATE.equals(getAttributeValue(PolicyConstants.VISIBILITY_ATTRIBUTE));
    }
    
    /**
     * TODO: javadoc
     */
    public String removeAttribute(final QName name) {
        synchronized (attributes) {
            return attributes.remove(name);
        }
    }
    
    /**
     * TODO: javadoc
     */
    public void setAttribute(final QName name, final String value) {
        synchronized (attributes) {
            attributes.put(name, value);
        }
    }
    
    /**
     * TODO: javadoc
     */
    public void setOptionalAttribute(final boolean value) {
        setAttribute(PolicyConstants.OPTIONAL, Boolean.toString(value));
    }
    
    /**
     * An {@code Object.toString()} method override.
     */
    public String toString() {
        return toString(0, new StringBuffer()).toString();
    }
    
    /**
     * A helper method that appends indented string representation of this instance to the input string buffer.
     *
     * @param indentLevel indentation level to be used.
     * @param buffer buffer to be used for appending string representation of this instance
     * @return modified buffer containing new string representation of the instance
     */
    public StringBuffer toString(final int indentLevel, final StringBuffer buffer) {
        final String indent = PolicyUtils.Text.createIndent(indentLevel);
        final String innerIndent = PolicyUtils.Text.createIndent(indentLevel + 1);
        final String innerDoubleIndent = PolicyUtils.Text.createIndent(indentLevel + 2);
        
        buffer.append(indent);
        if (type == ModelNode.Type.ASSERTION) {
            buffer.append("assertion data {");
        } else {
            buffer.append("assertion parameter data {");
        }
        buffer.append(PolicyUtils.Text.NEW_LINE);
        
        buffer.append(innerIndent).append("namespace = '").append(name.getNamespaceURI()).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("prefix = '").append(name.getPrefix()).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("local name = '").append(name.getLocalPart()).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("value = '").append(value).append('\'').append(PolicyUtils.Text.NEW_LINE);
        synchronized (attributes) {
            if (attributes.isEmpty()) {
                buffer.append(innerIndent).append("no attributes");
            } else {
                
                buffer.append(innerIndent).append("attributes {").append(PolicyUtils.Text.NEW_LINE);
                for(Map.Entry<QName, String> entry : attributes.entrySet()) {
                    final QName aName = entry.getKey();
                    buffer.append(innerDoubleIndent).append("name = '").append(aName.getNamespaceURI()).append(':').append(aName.getLocalPart());
                    buffer.append("', value = '").append(entry.getValue()).append('\'').append(PolicyUtils.Text.NEW_LINE);
                }
                buffer.append(innerIndent).append('}');
            }
        }
        
        buffer.append(PolicyUtils.Text.NEW_LINE).append(indent).append('}');
        
        return buffer;
    }
    
    public ModelNode.Type getNodeType() {
        return type;
    }
    
}
