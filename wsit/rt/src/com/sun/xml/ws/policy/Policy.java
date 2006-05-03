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

import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.namespace.QName;

/**
 * A policy represents normalized policy as a wrapper of available policy alternatives represented by
 * child {@link AssertionSet AssertionSets}.
 *
 * @author Fabian Ritzmann, Marek Potociar
 */
public class Policy implements Iterable<AssertionSet> {
    private static final String POLICY_TOSTRING_NAME = "policy";
    
    private static final List<AssertionSet> NULL_POLICY_ASSERTION_SETS = Collections.unmodifiableList(new LinkedList<AssertionSet>());
    private static final List<AssertionSet> EMPTY_POLICY_ASSERTION_SETS = Collections.unmodifiableList(new LinkedList<AssertionSet>(Arrays.asList(new AssertionSet[] {AssertionSet.emptyAssertionSet()})));
    
    private static final Set<QName> EMPTY_VOCABULARY = Collections.unmodifiableSet(new TreeSet<QName>(PolicyUtils.Comparison.QNAME_COMPARATOR));
    
    private static final Policy ANONYMOUS_NULL_POLICY = new Policy(null, null, NULL_POLICY_ASSERTION_SETS, EMPTY_VOCABULARY);
    private static final Policy ANONYMOUS_EMPTY_POLICY = new Policy(null, null, EMPTY_POLICY_ASSERTION_SETS, EMPTY_VOCABULARY);
    
    
    private String id;
    private String name;
    
    private List<AssertionSet> assertionSets = new LinkedList<AssertionSet>();
    private Set<QName> vocabulary = new TreeSet<QName>(PolicyUtils.Comparison.QNAME_COMPARATOR);
    private Collection<QName> immutableVocabulary = Collections.unmodifiableCollection(vocabulary);

    private String toStringName;
    
    /**
     * The factory method creates an <b>immutable</b> policy instance which represents a <emph>'nothing allowed'</emph>
     * policy expression.
     *
     * @return policy instance which represents a <emph>'nothing allowed'</emph> (no policy alternatives).
     */
    public static Policy createNullPolicy() {
        return ANONYMOUS_NULL_POLICY;
    }
    
    /**
     * The factory method creates an <b>immutable</b> policy instance which represents a <emph>'anything allowed'</emph>
     * policy expression.
     *
     * @return policy instance which represents a <emph>'anything allowed'</emph> (empty policy alternative with no plicy
     * assertions prescribed).
     */
    public static Policy createEmptyPolicy() {
        return ANONYMOUS_EMPTY_POLICY;
    }
    
    /**
     * The factory method creates an <b>immutable</b> policy instance which represents a <emph>'nothing allowed'</emph>
     * policy expression.
     *
     * @param name global URI of the policy. May be {@code null}.
     * @param id local URI of the policy. May be {@code null}.
     * @return policy instance which represents a <emph>'nothing allowed'</emph> (no policy alternatives).
     */
    public static Policy createNullPolicy(String name, String id) {
        if (name == null && id == null) {
            return ANONYMOUS_NULL_POLICY;
        } else {
            return new Policy(name, id, NULL_POLICY_ASSERTION_SETS, EMPTY_VOCABULARY);
        }
    }
    
    /**
     * The factory method creates an <b>immutable</b> policy instance which represents a <emph>'anything allowed'</emph>
     * policy expression.
     *
     * @param name global URI of the policy. May be {@code null}.
     * @param id local URI of the policy. May be {@code null}.
     * @return policy instance which represents a <emph>'anything allowed'</emph> (empty policy alternative with no plicy
     * assertions prescribed).
     */
    public static Policy createEmptyPolicy(String name, String id) {
        if (name == null && id == null) {
            return ANONYMOUS_EMPTY_POLICY;
        } else {
            return new Policy(name, id, EMPTY_POLICY_ASSERTION_SETS, EMPTY_VOCABULARY);
        }
    }
    
    public static Policy createPolicy(String name, String id) {
        return new Policy(POLICY_TOSTRING_NAME, name, id);
    }
    
    public static Policy createPolicy(Collection<AssertionSet> sets) {
        return new Policy(POLICY_TOSTRING_NAME, sets);
    }
    
    public static Policy createPolicy(String name, String id, Collection<AssertionSet> sets) {
        return new Policy(POLICY_TOSTRING_NAME, name, id, sets);
    }
    
    private Policy(String name, String id, List<AssertionSet> assertionSets, Set<QName> vocabulary) {
        this.toStringName = POLICY_TOSTRING_NAME;
        this.name = name;
        this.id = id;
        this.assertionSets = assertionSets;
        this.vocabulary = vocabulary;
    }
    
    Policy(String toStringName) {
        this.toStringName = toStringName;
        this.assertionSets = new LinkedList<AssertionSet>();
        this.vocabulary = new TreeSet<QName>(PolicyUtils.Comparison.QNAME_COMPARATOR);
    }
    
    Policy(String toStringName, Collection<AssertionSet> sets) {
        this(toStringName);
        
        addAll(sets);
    }
    
    Policy(String toStringName, String name, String id) {
        this(toStringName);
        
        this.name = name;
        this.id = id;
    }
    
    Policy(String toStringName, String name, String id, Collection<AssertionSet> sets) {
        this(toStringName, name, id);
        
        addAll(sets);
    }
    
    private boolean add(AssertionSet set) {
        if (set == null) {
            return false;
        }
        
        if (!this.assertionSets.contains(set)) {
            this.assertionSets.add(set);
            this.vocabulary.addAll(set.getVocabulary());
            return true;
        } else {
            return false;
        }
    }
    
    private boolean addAll(Collection<AssertionSet> sets) {
        boolean result = true;
        
        if (sets != null) {
            for (AssertionSet set : sets) {
                result &= add(set); // this is here to ensure that vocabulary is built correctly as well
            }
            
            Collections.sort(this.assertionSets);
        }
        
        return result;
    }
    
    Collection<AssertionSet> getContent() {
        return assertionSets;
    }
    
    /**
     * Returns the policy identifier that serves as a local relative policy URI.
     *
     * @return policy identifier - a local relative policy URI. If no policy identifier is set, returns {@code null}.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the policy name that serves as a global policy URI.
     *
     * @return policy name - a global policy URI. If no policy name is set, returns {@code null}.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the policy ID or if that is null the policy name. May return null
     * if both attributes are null.
     *
     * @see #getId()
     * @see #getName()
     * @return The policy ID if it was set, or the name or null if no attribute was set.
     */
    public String getIdOrName() {
        if (id != null) {
            return id;
        }
        return name;
    }
    
    /**
     * Method returns how many policy alternatives this policy instance contains.
     *
     * @return number of policy alternatives contained in this policy instance
     */
    public int getNumberOfAssertionSets() {
        return assertionSets.size();
    }
    
    /**
     * A policy usually contains one or more assertion sets. Each assertion set
     * corresponds to a policy alternative as defined by WS-Policy.
     *
     * @return An iterator to iterate through all contained assertion sets
     */
    public Iterator<AssertionSet> iterator() {
        return assertionSets.iterator();
    }
    
    /**
     * Returns {@code true} if the policy instance represents "nothing allowed" policy expression
     *
     * @return {@code true} if the policy instance represents "nothing allowed" policy expression, {@code false} otherwise.
     */
    public boolean isNull() {
        return assertionSets.size() == 0;
    }
    
    /**
     * Returns {@code true} if the policy instance represents "anything allowed" policy expression
     *
     * @return {@code true} if the policy instance represents "anything allowed" policy expression, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return assertionSets.size() == 1 && assertionSets.get(0).isEmpty();
    }
    
    /**
     * Returns true if the policy contains the assertion names with specified namespace in its vocabulary
     *
     * @param namespaceUri the assertion namespace URI (identifying assertion domain)
     * @return {@code true}, if an assertion with the given name could be found in the policy vocabulary {@code false} otherwise.
     */
    public boolean contains(String namespaceUri) {
        for (QName entry : vocabulary) {
            if (entry.getNamespaceURI().equals(namespaceUri)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Retrieves the vocabulary of this policy expression. The vocabulary is represented by an immutable collection of 
     * unique QName objects. Each of those objects represents single assertion type contained in the policy.
     *
     * @return immutable collection of assertion types contained in the policy (a policy vocabulary).
     */
    Collection<QName> getVocabulary() {
        return immutableVocabulary;
    }
    
    /**
     * Determines if the policy instance contains the assertion with the name specified in its vocabulary.
     * 
     * @param assertionName the name of the assertion to be tested.
     * 
     * @return {@code true} if the assertion with the specified name is part of the policy instance's vocabulary, 
     * {@code false} otherwise.
     */
    public boolean contains(QName assertionName) {
        return vocabulary.contains(assertionName);
    }
    
    /**
     * An {@code Object.equals(Object obj)} method override.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof Policy))
            return false;
        
        Policy that = (Policy) obj;
        
        boolean result = true;
        
        result = result && this.vocabulary.equals(that.vocabulary);
        result = result && this.assertionSets.size() == that.assertionSets.size() && this.assertionSets.containsAll(that.assertionSets);
        
        return result;
    }
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + vocabulary.hashCode();
        result = 37 * result + assertionSets.hashCode();
        
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
    StringBuffer toString(int indentLevel, StringBuffer buffer) {
        String indent = PolicyUtils.Text.createIndent(indentLevel);
        String innerIndent = PolicyUtils.Text.createIndent(indentLevel + 1);
        String innerDoubleIndent = PolicyUtils.Text.createIndent(indentLevel + 2);
        
        buffer.append(indent).append(toStringName).append(" {").append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("id = '").append(id).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("name = '").append(name).append('\'').append(PolicyUtils.Text.NEW_LINE);
        
        buffer.append(innerIndent).append("vocabulary {").append(PolicyUtils.Text.NEW_LINE);
        if (vocabulary.isEmpty()) {
            buffer.append(innerDoubleIndent).append("no entries").append(PolicyUtils.Text.NEW_LINE);
        } else {
            int index = 1;
            for (QName entry : vocabulary) {
                buffer.append(innerDoubleIndent).append(index++).append(". entry = '").append(entry.getNamespaceURI()).append(':').append(entry.getLocalPart()).append('\'').append(PolicyUtils.Text.NEW_LINE);
            }
        }
        buffer.append(innerIndent).append('}').append(PolicyUtils.Text.NEW_LINE);
        
        if (assertionSets.isEmpty()) {
            buffer.append(innerIndent).append("no assertion sets").append(PolicyUtils.Text.NEW_LINE);
        } else {
            for (AssertionSet set : assertionSets) {
                set.toString(indentLevel + 1, buffer).append(PolicyUtils.Text.NEW_LINE);
            }
        }
        
        buffer.append(indent).append('}');
        
        return buffer;
    }
}
