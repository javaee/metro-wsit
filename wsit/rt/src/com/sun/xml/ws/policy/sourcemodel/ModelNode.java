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
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import javax.xml.namespace.QName;

/**
 * The general representation of a single node within a {@link com.sun.xml.ws.policy.sourcemodel.PolicySourceModel} instance.
 * The model node is created via factory methods of the {@link com.sun.xml.ws.policy.sourcemodel.PolicySourceModel} instance.
 * It may also hold {@link com.sun.xml.ws.policy.sourcemodel.AssertionData} instance in case its type is {@code ModelNode.Type.ASSERTION}.
 *
 * @author Marek Potociar
 */
public final class ModelNode implements Iterable<ModelNode>, Cloneable {
    /**
     * Policy source model node type enumeration
     */
    public static enum Type {
        POLICY(new QName(PolicyConstants.POLICY_NAMESPACE_URI, "Policy")),
        ALL(new QName(PolicyConstants.POLICY_NAMESPACE_URI, "All")),
        EXACTLY_ONE(new QName(PolicyConstants.POLICY_NAMESPACE_URI, "ExactlyOne")),
        POLICY_REFERENCE(new QName(PolicyConstants.POLICY_NAMESPACE_URI, "PolicyReference")),
        ASSERTION(null),
        ASSERTION_PARAMETER_NODE(null);
        
        private QName qName;
        
        Type(QName qName) {
            this.qName = qName;
        }
        
        public QName asQName() {
            return qName;
        }
    }
    
    // comon model node attributes
    private LinkedList<ModelNode> content;
    private Collection<ModelNode> unmodifiableViewOnContent;
    private final ModelNode.Type type;
    private ModelNode parentNode;
    private PolicySourceModel parentModel;
    
    // attributes used only in 'POLICY_REFERENCE' model node
    private PolicyReferenceData referenceData;
    private PolicySourceModel referencedModel;
    
    // attibutes used only in 'ASSERTION' or 'ASSERTION_PARAMETER_NODE' model node
    private AssertionData nodeData;
    
    /**
     * The factory method creates and initializes the POLICY model node and sets it's parent model reference to point to
     * the model supplied as an input parameter. This method is intended to be used ONLY from {@link PolicySourceModel} during
     * the initialization of its own internal structures.
     *
     * @param model policy source model to be used as a parent model of the newly created {@link ModelNode}. Must not be {@code null}
     * @return POLICY model node with the parent model reference initialized to the model supplied as an input parameter
     * @throws NullPointerException if the {@code model} input parameter is {@code null}
     */
    static ModelNode createRootPolicyNode(PolicySourceModel model) throws NullPointerException {
        if (model == null) {
            throw new NullPointerException("Policy source model input argument must not be 'null'.");
        }
        return new ModelNode(ModelNode.Type.POLICY, model);
    }
    
    private ModelNode(Type type, PolicySourceModel parentModel) {
        this.type = type;
        this.parentModel = parentModel;
        this.content = new LinkedList<ModelNode>();
        this.unmodifiableViewOnContent = Collections.unmodifiableCollection(this.content);
    }
    
    private ModelNode(Type type, PolicySourceModel parentModel, AssertionData data) {
        this(type, parentModel);
        
        this.nodeData = data;
    }
    
    private ModelNode(PolicySourceModel parentModel, PolicyReferenceData data) {
        this(Type.POLICY_REFERENCE, parentModel);
        
        this.referenceData = data;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildPolicyNode() {
        if (type == Type.ASSERTION_PARAMETER_NODE) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY', 'EXACTLY_ONE', 'ALL' and 'ASSERTION' node types. It is not supported for the 'ASSERTION_PARAMETER_NODE' node type");
        }
        
        ModelNode node = new ModelNode(ModelNode.Type.POLICY, parentModel);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildAllNode() {
        if (isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY', 'EXACTLY_ONE' and 'ALL' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(ModelNode.Type.ALL, parentModel);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildExactlyOneNode() {
        if (isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY', 'EXACTLY_ONE' and 'ALL' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(ModelNode.Type.EXACTLY_ONE, parentModel);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildAssertionNode() {
        if (isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY', 'EXACTLY_ONE' and 'ALL' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(ModelNode.Type.ASSERTION, parentModel);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildAssertionNode(AssertionData nodeData) {
        if (isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY', 'EXACTLY_ONE' and 'ALL' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(Type.ASSERTION, parentModel, nodeData);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildAssertionParameterNode() {
        if (!isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY', 'EXACTLY_ONE' and 'ALL' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(ModelNode.Type.ASSERTION_PARAMETER_NODE, parentModel);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildAssertionParameterNode(AssertionData nodeData) {
        if (!isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'ASSERTION' and 'ASSERTION_PARAMETER_NODE' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(Type.ASSERTION_PARAMETER_NODE, parentModel, nodeData);
        this.addChild(node);
        
        return node;
    }
    
    /**
     * TODO: proper java doc
     *
     * Factory method that creates new policy source model node as specified by a factory method name and input parameters.
     * Each node is created with respect to its enclosing policy source model.
     */
    public ModelNode createChildPolicyReferenceNode(PolicyReferenceData referenceData) {
        if (!isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'ASSERTION' and 'ASSERTION_PARAMETER_NODE' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        ModelNode node = new ModelNode(parentModel, referenceData);
        this.parentModel.addNewPolicyReference(node);
        this.addChild(node);
        
        return node;
    }
        
    Collection<ModelNode> getContent() {
        return unmodifiableViewOnContent;
    }
    
    Collection<ModelNode> getAssertionParameterNodeChildren() {
        Collection<ModelNode> result = null;
        
        if (isAssertionRelatedNode()) {
            
            result = new LinkedList<ModelNode>();
            
            for (ModelNode child : content) {
                if (child.type == Type.ASSERTION_PARAMETER_NODE) {
                    result.add(child);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Sets the parent model reference on the node and its children. The method may be invoked only on the root node
     * of the policy source model (or - in general - on a model node that dose not reference a parent node). Otherwise an
     * exception is thrown.
     *
     * @param model new parent policy source model to be set.
     * @throws IllegalAccessException in case this node references a parent node (i.e. is not a root node of the model).
     */
    void setParentModel(PolicySourceModel model) throws IllegalAccessException {
        if (parentNode != null) {
            throw new IllegalAccessException("The parent model may not be changed on a child node which is not a root of the policy source model tree.");
        }
        
        this.updateParentModelReference(model);
    }
    
    /**
     * The method updates the parentModel reference on current model node instance and all of it's children
     *
     * @param model new policy source model reference.
     */
    private void updateParentModelReference(PolicySourceModel model) {
        this.parentModel = model;
        
        for (ModelNode child : content) {
            child.updateParentModelReference(model);
        }
    }
    
    /**
     * Returns the parent policy source model that contains this model node.
     *
     * @return the parent policy source model
     */
    public PolicySourceModel getParentModel() {
        return parentModel;
    }
    
    /**
     * Returns the type of this policy source model node.
     *
     * @return actual type of this policy source model node
     */
    public ModelNode.Type getType() {
        return type;
    }
    
    /**
     * Returns the parent referenced by this policy source model node.
     *
     * @return current parent of this policy source model node or {@code null} if the node does not have a parent currently.
     */
    public ModelNode getParentNode() {
        return parentNode;
    }
        
    /**
     * Returns the data for this policy source model node (if any). The model node data are expected to be not {@code null} only in
     * case the type of this node is ASSERTION or ASSERTION_PARAMETER_NODE.
     *
     * @return the data of this policy source model node or {@code null} if the node does not have any data associated to it
     * attached.
     */
    public AssertionData getNodeData() {
        return nodeData;
    }
    
    /**
     * Returns the policy reference data for this policy source model node. The policy reference data are expected to be not {@code null} only in
     * case the type of this node is POLICY_REFERENCE.
     *
     * @return the policy reference data for this policy source model node or {@code null} if the node does not have any policy reference data
     * attached.
     */
    public PolicyReferenceData getPolicyReferenceData() {
        return referenceData;
    }
    
    /**
     * The method may be used to set or replace assertion data set for this node. If there are assertion data set already,
     * those are replaced by a new reference and eventualy returned from the method.
     * <p/>
     * This method is supported only in case this model node instance's type is {@code ASSERTION} or {@code ASSERTION_PARAMETER_NODE}.
     * If used from other node types, an exception is thrown.
     *
     * @param newData new assertion data to be set.
     * @return old and replaced assertion data if any or {@code null} otherwise.
     *
     * @throws UnsupportedOperationException in case this method is called on nodes of type other than {@code ASSERTION}
     * or {@code ASSERTION_PARAMETER_NODE}
     */
    public AssertionData setOrReplaceNodeData(AssertionData newData) {
        if (!isAssertionRelatedNode()) {
            throw new UnsupportedOperationException("This operation is supported only for 'ASSERTION' and 'ASSERTION_PARAMETER_NODE' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        AssertionData oldData = this.nodeData;
        this.nodeData = newData;
        
        return oldData;
    }
    
    /**
     * The method specifies whether the model node instance represents assertion related node, it means whether its type
     * is 'ASSERTION' or 'ASSERTION_PARAMETER_NODE'. This is, for example, the way to determine whether the node supports
     * setting a {@link AssertionData} object via {@link #setOrReplaceNodeData(AssertionData)} method or not.
     *
     * @return {@code true} or {@code false} according to whether the node instance represents assertion related node or not.
     */
    boolean isAssertionRelatedNode() {
        return type == Type.ASSERTION || type == Type.ASSERTION_PARAMETER_NODE;
    }
    
    /**
     * Appends the specified child node to the end of the children list of this node and sets it's parent to reference
     * this node.
     *
     * @param child node to be appended to the children list of this node.
     * @return {@code true} (as per the general contract of the {@code Collection.add} method).
     *
     * @throws NullPointerException if the specified node is {@code null}.
     * @throws IllegalArgumentException if child has a parent node set already to point to some node
     */
    private boolean addChild(ModelNode child) {
        content.add(child);
        child.parentNode = this;
        
        return true;
    }
    
    /**
     *
     */
    void setReferencedModel(PolicySourceModel model) {
        if (this.type != Type.POLICY_REFERENCE) {
            throw new UnsupportedOperationException("This operation is supported only for 'POLICY_REFERENCE' node types. It is not supported for the node type of this instance: '" +  type + "'");
        }
        
        referencedModel = model;
    }
    
    PolicySourceModel getReferencedModel() {
        return referencedModel;
    }
    
    /**
     * Returns the number of child policy source model nodes. If this model node contains
     * more than {@code Integer.MAX_VALUE} children, returns {@code Integer.MAX_VALUE}.
     *
     * @return the number of children of this node.
     */
    public int childrenSize() {
        return content.size();
    }
    
    /**
     * Returns true if the node has at least one child node.
     *
     * @return true if the node has at least one child node, false otherwise.
     */
    public boolean hasChildren() {
        return !content.isEmpty();
    }
    
    /**
     * Iterates through all child nodes.
     *
     * @return An iterator for the child nodes
     */
    public Iterator<ModelNode> iterator() {
        return content.iterator();
    }
    
    /**
     * An {@code Object.equals(Object obj)} method override. Method ignores the parent source model. It means that two
     * model nodes may be the same even if they belong to different models.
     * <p/>
     * If parent model comparison is desired, it must be accomplished separately. To perform that, the reference equality
     * test is sufficient ({@code nodeA.getParentModel() == nodeB.getParentModel()}), since all model nodes are created
     * for specific model instances.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof ModelNode))
            return false;
        
        boolean result = true;
        ModelNode that = (ModelNode) obj;
        
        result = result && this.type.equals(that.type);
        // result = result && ((this.parentNode == null) ? that.parentNode == null : this.parentNode.equals(that.parentNode));
        result = result && ((this.nodeData == null) ? that.nodeData == null : this.nodeData.equals(that.nodeData));
        result = result && ((this.content == null) ? that.content == null : this.content.equals(that.content));
        
        return result;
    }
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + this.type.hashCode();
        result = 37 * result + ((this.parentNode != null) ? this.parentNode.hashCode() : 0);
        result = 37 * result + ((this.nodeData != null) ? this.nodeData.hashCode() : 0);
        result = 37 * result + this.content.hashCode();
        
        return result;
    }
    
    /**
     * Returns a string representation of the object. In general, the <code>toString</code> method
     * returns a string that "textually represents" this object.
     *
     * @return  a string representation of the object.
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
        
        buffer.append(indent).append(type).append(" {").append(PolicyUtils.Text.NEW_LINE);
        if (type == Type.ASSERTION) {
            if (nodeData != null) {
                nodeData.toString(indentLevel + 1, buffer);
            } else {
                buffer.append(innerIndent).append("no assertion data set");
            }
            buffer.append(PolicyUtils.Text.NEW_LINE);
        } else if (type == Type.POLICY_REFERENCE) {
            if (referenceData != null) {
                referenceData.toString(indentLevel + 1, buffer);
            } else {
                buffer.append(innerIndent).append("no policy reference data set");
            }
            buffer.append(PolicyUtils.Text.NEW_LINE);
        }
        
        if (content.size() > 0) {
            for (ModelNode child : content) {
                child.toString(indentLevel + 1, buffer).append(PolicyUtils.Text.NEW_LINE);
            }
        } else {
            buffer.append(innerIndent).append("no child nodes").append(PolicyUtils.Text.NEW_LINE);
        }
        
        buffer.append(indent).append('}');
        return buffer;
    }
    
    protected ModelNode clone() throws CloneNotSupportedException {
        ModelNode clone = (ModelNode) super.clone();
        
        if (this.nodeData != null) {
            clone.nodeData = this.nodeData.clone();
        }
        
        // no need to clone PolicyReferenceData, since those are immutable
        
        if (this.referencedModel != null) {
            clone.referencedModel = this.referencedModel.clone();
        }
        
        
        clone.content = new LinkedList<ModelNode>();
        clone.unmodifiableViewOnContent = Collections.unmodifiableCollection(clone.content);
        
        for (ModelNode thisChild : this.content) {
            clone.addChild(thisChild.clone());
        }
        
        return clone;
    }
    
}
