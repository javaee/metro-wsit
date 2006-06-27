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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.LinkedList;
import java.util.List;

/**
 * The AssertionSet is a set of assertions. It represents a single policy alternative.
 *
 * @author Fabian Ritzmann, Marek Potociar
 */
public final class AssertionSet implements Iterable<PolicyAssertion>, Comparable<AssertionSet> {
    private static final AssertionSet EMPTY_ASSERTION_SET = new AssertionSet(Collections.unmodifiableList(new LinkedList<PolicyAssertion>()));
    /**
     * The comparator comapres policy assertions according to their publicly accessible attributes, in the following
     * order of attributes:
     *
     * 1. namespace (not null String)
     * 2. local name (not null String)
     * 3. value (String): null < "" < "not empty"
     * 4. has nested assertions (boolean): false < true
     * 5. has nested policy (boolean): false < true
     * 6. hashCode comparison
     */
    private static final Comparator<PolicyAssertion> ASSERTION_COMPARATOR = new Comparator<PolicyAssertion>() {
        public int compare(PolicyAssertion pa1, PolicyAssertion pa2) {
            if (pa1 == pa2 || pa1.equals(pa2)) {
                return 0;
            }
            
            int result;
            
            result = PolicyUtils.Comparison.QNAME_COMPARATOR.compare(pa1.getName(), pa2.getName());
            if (result != 0) {
                return result;
            }
            
            result = PolicyUtils.Comparison.compareNullableStrings(pa1.getValue(), pa2.getValue());
            if (result != 0) {
                return result;
            }
            
            result = PolicyUtils.Comparison.compareBoolean(pa1.hasNestedAssertions(), pa2.hasNestedAssertions());
            if (result != 0) {
                return result;
            }
            
            result = PolicyUtils.Comparison.compareBoolean(pa1.hasNestedPolicy(), pa2.hasNestedPolicy());
            if (result != 0) {
                return result;
            }
            
            return Math.round(Math.signum(pa1.hashCode() - pa2.hashCode()));
        }
    };
    
    private List<PolicyAssertion> assertions;
    private Set<QName> vocabulary = new TreeSet<QName>(PolicyUtils.Comparison.QNAME_COMPARATOR);
    private Collection<QName> immutableVocabulary = Collections.unmodifiableCollection(vocabulary);
    
    private AssertionSet(List<PolicyAssertion> list) {
        assert (list != null) : "Private constructor must not receive 'null' argument as a initial policy assertion list";
        this.assertions = list;
    }
    
    private AssertionSet(Collection<AssertionSet> alternatives) {
        this.assertions = new LinkedList<PolicyAssertion>();
        for (AssertionSet alternative : alternatives) {
            addAll(alternative.assertions);
        }
    }
    
    private boolean add(PolicyAssertion assertion) {
        if (assertion == null) {
            return false;
        }
        
        if (!this.assertions.contains(assertion)) {
            this.assertions.add(assertion);
            this.vocabulary.add(assertion.getName());
            return true;
        } else {
            return false;
        }
    }
    
    private boolean addAll(Collection<PolicyAssertion> assertions) {
        boolean result = true;
        
        if (assertions != null) {
            for (PolicyAssertion assertion : assertions) {
                result &= add(assertion); // this is here to ensure that vocabulary is built correctly as well
            }
        }
        
        return result;
    }
    
    /**
     * Return all assertions contained in this assertion set.
     *
     * @return All assertions contained in this assertion set
     */
    Collection<PolicyAssertion> getAssertions() {
        return assertions;
    }
            
    /**
     * Retrieves the vocabulary of this policy expression. The vocabulary is represented by an immutable collection of 
     * unique QName objects. Each of those objects represents single assertion type contained in the assertion set.
     *
     * @return immutable collection of assertion types contained in the assertion set (a policy vocabulary).
     */
    Collection<QName> getVocabulary() {
        return immutableVocabulary;
    }
    
    /**
     * Checks whether this policy alternative is compatible with the provided policy alternative.
     *
     * @param alternative policy alternative used for compatibility test
     * @return {@code true} if the two policy alternatives are compatible, {@code false} otherwise
     */
    boolean isCompatibleWith(AssertionSet alternative) {
        return this.vocabulary.equals(alternative.vocabulary);
    }
    
    /**
     * Creates and returns new assertion set holding content of all provided policy assertion sets.
     * <p/>
     * This method should not be used to perform a merge of general Policy instances. A client should be aware of the
     * method's result meaning and the difference between merge of Policy instances and merge of AssertionSet instances.
     *
     *
     * @param alternatives collection of provided policy assertion sets which content is to be stored in the assertion set.
     *        May be {@code null} - empty assertion set is returned in such case.
     * @return new instance of assertion set holding the content of all provided policy assertion sets.
     */
    public static AssertionSet createMergedAssertionSet(Collection<AssertionSet> alternatives) {
        if (alternatives == null || alternatives.isEmpty()) {
            return EMPTY_ASSERTION_SET;
        }
        
        AssertionSet result = new AssertionSet(alternatives);
        Collections.sort(result.assertions, ASSERTION_COMPARATOR);
        
        return result;
    }
    
    /**
     * Creates and returns new assertion set holding a set of provided policy assertions.
     *
     * @param assertions collection of provided policy assertions to be stored in the assertion set. May be {@code null}.
     * @return new instance of assertion set holding the provided policy assertions
     */
    public static AssertionSet createAssertionSet(Collection<PolicyAssertion> assertions) {
        if (assertions == null || assertions.isEmpty()) {
            return EMPTY_ASSERTION_SET;
        }
        
        AssertionSet result = new AssertionSet(new LinkedList<PolicyAssertion>());
        result.addAll(assertions);
        Collections.sort(result.assertions, ASSERTION_COMPARATOR);
        
        return result;
    }
    
    public static AssertionSet emptyAssertionSet() {
        return EMPTY_ASSERTION_SET;
    }
    /**
     * Returns an iterator over a set of child policy assertion objects.
     *
     * @return policy assertion Iterator.
     */
    public Iterator<PolicyAssertion> iterator() {
        return this.assertions.iterator();
    }
    
    /**
     * Searches for assertions with given name. Only assertions that are contained as immediate children of the assertion set are
     * searched, i.e. nested policies are not searched.
     *
     * @param name The fully qualified name of searched assertion
     * @return List of all assertions matching the requested name. If no assertions are found, the returned list is empty
     * (i.e. {@code null} value is never returned).
     */
    public Collection<PolicyAssertion> get(QName name) {
        List<PolicyAssertion> matched = new LinkedList<PolicyAssertion>();
        
        if (vocabulary.contains(name)) {
            // we iterate the assertion set only if we are sure we contain such assertion name in our vocabulary
            for (PolicyAssertion assertion : assertions) {
                if (assertion.getName().equals(name)) {
                    matched.add(assertion);
                }
            }
        }
        
        return matched;
    }
    
    /**
     * Returns {@code true} if this assertion set contains no assertions.
     *
     * @return {@code true} if this assertion set contains no assertions.
     */
    public boolean isEmpty() {
        return assertions.isEmpty();
    }
    
    /**
     * Returns true if the assertion set contains the assertion name specified in its vocabulary
     *
     * @param assertionName the fully qualified name of the assertion
     * @return {@code true}, if an assertion with the given name could be found in the assertion set vocabulary {@code false} otherwise.
     */
    public boolean contains(QName assertionName) {
        return vocabulary.contains(assertionName);
    }

    /**
     * An {@code Comparable<T>.compareTo(T o)} interface method implementation.
     */
    public int compareTo(AssertionSet that) {
        if (this.equals(that)) {
            return 0;
        }
        
        { // comparing vocabularies
            Iterator<QName> vIterator1 = this.getVocabulary().iterator();
            Iterator<QName> vIterator2 = that.getVocabulary().iterator();
            while (vIterator1.hasNext()) {
                QName entry1 = vIterator1.next();
                if (vIterator2.hasNext()) {
                    QName entry2 = vIterator2.next();
                    int result = PolicyUtils.Comparison.QNAME_COMPARATOR.compare(entry1, entry2);
                    if (result != 0) {
                        return result;
                    }
                } else {
                    return 1; // we have more entries in this vocabulary
                }
            }
            
            if (vIterator2.hasNext()) {
                return -1;  // we have more entries in that vocabulary
            }
        }
        
        { // vocabularies are equal => comparing assertions
            Iterator<PolicyAssertion> pIterator1 = this.getAssertions().iterator();
            Iterator<PolicyAssertion> pIterator2 = that.getAssertions().iterator();
            while (pIterator1.hasNext()) {
                PolicyAssertion pa1 = pIterator1.next();
                if (pIterator2.hasNext()) {
                    PolicyAssertion pa2 = pIterator2.next();
                    int result = ASSERTION_COMPARATOR.compare(pa1, pa2);
                    if (result != 0) {
                        return result;
                    }
                } else {
                    return 1; // we have more entries in this assertion set
                }
            }
            
            if (pIterator2.hasNext()) {
                return -1;  // we have more entries in that assertion set
            }
        }
        
        // seems like objects are very simmilar although not equal => we must not return 0 otherwise the TreeSet
        // holding this element would discard the newly added element. Thus we return that the first argument is
        // greater than second (just because it is first...)
        return 1;
    }
    
    /**
     * An {@code Object.equals(Object obj)} method override.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof AssertionSet))
            return false;
        
        AssertionSet that = (AssertionSet) obj;
        
        boolean result = true;
        
        result = result && this.vocabulary.equals(that.vocabulary);
        result = result && this.assertions.size() == that.assertions.size() && this.assertions.containsAll(that.assertions);
        
        return result;
    }
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + vocabulary.hashCode();
        result = 37 * result + assertions.hashCode();
        
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
        
        buffer.append(indent).append("assertion set {").append(PolicyUtils.Text.NEW_LINE);
        
        if (assertions.isEmpty()) {
            buffer.append(innerIndent).append("no assertions").append(PolicyUtils.Text.NEW_LINE);
        } else {
            for (PolicyAssertion assertion : assertions) {
                assertion.toString(indentLevel + 1, buffer).append(PolicyUtils.Text.NEW_LINE);
            }
        }
        
        buffer.append(indent).append('}');
        
        return buffer;
    }
}
