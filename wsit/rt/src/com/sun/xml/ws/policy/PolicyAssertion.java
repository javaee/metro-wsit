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

package com.sun.xml.ws.policy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.policy.sourcemodel.ModelNode;

/**
 * Base class for any policy assertion implementations. It defines the common interface and provides some default
 * implentation of common policy assertion functionality.
 */
public abstract class PolicyAssertion {
    private final AssertionData data;
    private AssertionSet parameters;
    private NestedPolicy nestedPolicy;         
    
    protected PolicyAssertion() {
        this.data = AssertionData.createAssertionData(null);
    }
    
    /**
     * Creates generic assertionand stores the data specified in input parameters
     *
     * @param assertionData assertion creation data specifying the details of newly created assertion
     * @param assertionParameters collection of assertions parameters of this policy assertion. May be {@code null}.
     * @param nestedAlternative assertion set specifying nested policy alternative. May be {@code null}.
     */
    protected PolicyAssertion(
            final AssertionData assertionData, 
            final Collection<? extends PolicyAssertion> assertionParameters, 
            final AssertionSet nestedAlternative) {
        this.data = assertionData;
        if (nestedAlternative != null) {
            this.nestedPolicy = NestedPolicy.createNestedPolicy(nestedAlternative);
        }
        
        this.parameters = AssertionSet.createAssertionSet(assertionParameters);
    }
        
    /**
     * Returns the fully qualified name of the assertion.
     *
     * @return assertion's fully qualified name.
     */
    public final QName getName() {
        return data.getName();
    }
    
    /**
     * Returns the value of the assertion - the character data content contained in the assertion element representation.
     *
     * @return assertion's value. May return {@code null} if there is no value set for the assertion.
     */
    public final String getValue() {
        return data.getValue();
    }
    
    /**
     * Method specifies whether the assertion is otpional or not.
     * <p/>
     * This is a default implementation that may be overriden. The method returns {@code true} if the {@code wsp:optional} attribute
     * is present on the assertion and its value is {@code 'true'}. Otherwise the method returns {@code false}.
     *
     * @return {@code 'true'} if the assertion is optional. Returns {@code false} otherwise.
     */
    public boolean isOptional() {
        boolean result = false;
        final String attributeValue = getAttributeValue(PolicyConstants.OPTIONAL);
        if (attributeValue != null) {
            result = Boolean.parseBoolean(attributeValue);
        }
        
        return result;
    }
    
    /**
     * Method specifies whether the assertion is private or not. This is specified by our proprietary visibility element.
     *
     * @return {@code 'true'} if the assertion is marked as private (i.e. should not be marshalled int generated WSDL documents). Returns {@code false} otherwise.
     */
    public final boolean isPrivate() {
        return data.isPrivateAttributeSet();
    }
    
    /**
     * Returns the disconnected set of attributes attached to the assertion. Each attribute is represented as a single
     * {@code Map.Entry<attributeName, attributeValue>} element.
     * <p/>
     * 'Disconnected' means, that the result of this method will not be synchronized with any consequent assertion's attribute modification. It is
     * also important to notice that a manipulation with returned set of attributes will not have any effect on the actual assertion's
     * attributes.
     *
     * @return disconected set of attributes attached to the assertion.
     */
    public final Set<Map.Entry<QName, String>> getAttributesSet() {
        return data.getAttributesSet();
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
    public final Map<QName, String> getAttributes() {
        return data.getAttributes();
    }
    
    /**
     * Returns the value of an attribute. Returns null if an attribute with the given name does not exist.
     *
     * @param name The fully qualified name of the attribute
     * @return The value of the attribute. Returns {@code null} if there is no such attribute or if it's value is null.
     */
    public final String getAttributeValue(final QName name) {
        return data.getAttributeValue(name);
    }
    
    /**
     * Returns the boolean information whether this assertion contains any parameters.
     *
     * @return {@code true} if the assertion contains parameters. Returns {@code false} otherwise.
     */
    public final boolean hasNestedAssertions() {
        return !parameters.isEmpty();
    }
    
    /**
     * Returns the assertion's parameter collection iterator.
     *
     * @return the assertion's parameter collection iterator.
     */
    public final Iterator<PolicyAssertion> getNestedAssertionsIterator() {
        return parameters.iterator();
    }
    
    boolean isParameter() {
        return data.getNodeType() == ModelNode.Type.ASSERTION_PARAMETER_NODE;
    }
    
    /**
     * Returns the boolean information whether this assertion contains nested policy.
     *
     * @return {@code true} if the assertion contains child (nested) policy. Returns {@code false} otherwise.
     */
    public final boolean hasNestedPolicy() {
        return nestedPolicy != null;
    }
    
    /**
     * Returns the nested policy if any.
     *
     * @return the nested policy if the assertion contains a nested policy. Returns {@code null} otherwise.
     */
    public final NestedPolicy getNestedPolicy() {
        return nestedPolicy;
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
    protected StringBuffer toString(final int indentLevel, final StringBuffer buffer) {
        final String indent = PolicyUtils.Text.createIndent(indentLevel);
        final String innerIndent = PolicyUtils.Text.createIndent(indentLevel + 1);
        
        buffer.append(indent).append("Assertion {").append(PolicyUtils.Text.NEW_LINE);
        data.toString(indentLevel + 1, buffer);
        buffer.append(PolicyUtils.Text.NEW_LINE);
        
        if (hasNestedAssertions()) {
            buffer.append(innerIndent).append("parameters {").append(PolicyUtils.Text.NEW_LINE);
            for (PolicyAssertion parameter : parameters) {
                parameter.toString(indentLevel + 2, buffer);
            }
            buffer.append(innerIndent).append('}').append(PolicyUtils.Text.NEW_LINE);
        } else {
            buffer.append(innerIndent).append("no parameters").append(PolicyUtils.Text.NEW_LINE);
        }
        
        if (hasNestedPolicy()) {
            nestedPolicy.toString(indentLevel + 1, buffer).append(PolicyUtils.Text.NEW_LINE);
        } else {
            buffer.append(innerIndent).append("no nested policy").append(PolicyUtils.Text.NEW_LINE);
        }
        
        buffer.append(indent).append('}');
        
        return buffer;
    }
    
    /**
     * An {@code Object.equals(Object obj)} method override.
     */
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof PolicyAssertion)) {
            return false;
        }
        
        final PolicyAssertion that = (PolicyAssertion) obj;
        boolean result = true;
        
        result = result && this.data.equals(that.data);
        result = result && this.parameters.equals(that.parameters);
        result = result && ((this.nestedPolicy == null) ? ((that.nestedPolicy == null) ? true : false) : this.nestedPolicy.equals(that.nestedPolicy));
        
        return result;
    }
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + data.hashCode();
        result = 37 * result + ((hasNestedAssertions()) ? 17 : 0);
        result = 37 * result + ((hasNestedPolicy()) ? 17 : 0);
        
        return result;
    }
}
