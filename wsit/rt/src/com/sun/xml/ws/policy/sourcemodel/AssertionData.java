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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.privateutil.PolicyUtils;

/**
 * Wrapper class for possible assertion data that each ASSERTION policy source model node may have attached.
 * These data are intended to be used as input parameter when creating {@link com.sun.xml.ws.policy.PolicyAssertion}
 * objects via {@link com.sun.xml.ws.policy.spi.PolicyAssertionCreator} implementations.
 *
 * @author Marek Potociar
 */
public final class AssertionData implements Cloneable {
    
    private QName name;
    private String value;
    private HashMap<QName, String> attributes = new HashMap<QName, String>();
    
    /**
     * TODO: javadoc
     */
    public AssertionData(QName name) {
        this.name = name;
    }
    
    /**
     * TODO: javadoc
     */
    public AssertionData(QName name, String value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * TODO: javadoc
     */
    public AssertionData(QName name, String value, Map<QName, String> attributes) {
        this.name = name;
        this.value = value;
        if (attributes != null) {
            this.attributes.putAll(attributes);
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
     * TODO: javadoc
     */
    public void setAttribute(QName name, String value) {
        synchronized (attributes) {
            attributes.put(name, value);
        }
    }
    
    /**
     * TODO: javadoc
     */
    public String removeAttribute(QName name) {
        synchronized (attributes) {
            return attributes.remove(name);
        }
    }
    
    /**
     * TODO: javadoc
     */
    public boolean containsAttribute(QName name) {
        synchronized (attributes) {
            return attributes.containsKey(name);
        }
    }
    
    /**
     * TODO: javadoc
     */
    public String getAttributeValue(QName name) {
        synchronized (attributes) {
            return attributes.get(name);
        }
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
     * An {@code Object.equals(Object obj)} method override.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof AssertionData))
            return false;
        
        boolean result = true;
        AssertionData that = (AssertionData) obj;
        
        result = result && this.name.equals(that.name);
        result = result && ((this.value == null) ? that.value == null : this.value.equals(that.value));
        synchronized (attributes) {
            result = result && ((this.attributes == null) ? that.attributes == null : this.attributes.equals(that.attributes));
        }
        
        return result;
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
    public StringBuffer toString(int indentLevel, StringBuffer buffer) {
        String indent = PolicyUtils.Text.createIndent(indentLevel);
        String innerIndent = PolicyUtils.Text.createIndent(indentLevel + 1);
        String innerDoubleIndent = PolicyUtils.Text.createIndent(indentLevel + 2);
        
        buffer.append(indent).append("assertion data {").append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("namespace = '").append(name.getNamespaceURI()).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("prefix = '").append(name.getPrefix()).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("local name = '").append(name.getLocalPart()).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("value = '").append(value).append('\'').append(PolicyUtils.Text.NEW_LINE);
        
        synchronized (attributes) {
            if (attributes.isEmpty()) {
                buffer.append(innerIndent).append("no attributes");
            } else {
                
                buffer.append(innerIndent).append("attributes {").append(PolicyUtils.Text.NEW_LINE);
                for (Map.Entry<QName, String> entry : attributes.entrySet()) {
                    QName aName = entry.getKey();
                    buffer.append(innerDoubleIndent).append("name = '").append(aName.getNamespaceURI()).append(':').append(aName.getLocalPart());
                    buffer.append("', value = '").append(entry.getValue()).append('\'').append(PolicyUtils.Text.NEW_LINE);
                }
                buffer.append(innerIndent).append('}');
            }
        }
        
        buffer.append(PolicyUtils.Text.NEW_LINE).append(indent).append('}');
        
        return buffer;
    }
    
    protected AssertionData clone() throws CloneNotSupportedException {
        AssertionData clone = (AssertionData) super.clone();
        
        clone.attributes = new HashMap<QName, String>(this.attributes);
        
        return clone;
    }
}
