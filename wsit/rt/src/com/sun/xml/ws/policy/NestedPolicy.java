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

import java.util.Arrays;
import java.util.Iterator;
import javax.xml.namespace.QName;

/**
 * A special policy implementation that assures that only no or single policy alternative is possible within this type of policy.
 *
 * @author Marek Potociar
 */
public final class NestedPolicy extends Policy {
    private static final String NESTED_POLICY_TOSTRING_NAME = "nested policy";
        
    private NestedPolicy(final AssertionSet set) {
        super(NESTED_POLICY_TOSTRING_NAME, Arrays.asList(new AssertionSet[] { set }));
    }
    
    private NestedPolicy(final String name, final String id, final AssertionSet set) {
        super(NESTED_POLICY_TOSTRING_NAME, name, id, Arrays.asList(new AssertionSet[] { set }));
    }

    static NestedPolicy createNestedPolicy(final AssertionSet set) {
        return new NestedPolicy(set);
    }

    static NestedPolicy createNestedPolicy(final String name, final String id, final AssertionSet set) {
        return new NestedPolicy(name, id, set);
    }
    
    /**
     * Returns the AssertionSet instance representing a single policy alterantive held wihtin this nested policy object.
     * If the nested policy represents a policy with no alternatives (i.e. nothing is allowed) the method returns {@code null}.
     *
     * @return nested policy alternative represented by AssertionSet object. May return {@code null} in case the nested policy
     * represents 'nothing allowed' policy.
     */
    public AssertionSet getAssertionSet() {
        final Iterator<AssertionSet> iterator = iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }
        
    /**
     * An {@code Object.equals(Object obj)} method override.
     */
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof NestedPolicy))
            return false;
        
        final NestedPolicy that = (NestedPolicy) obj;
        
        return super.equals(that);
    }
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        return super.hashCode();
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
    StringBuffer toString(final int indentLevel, final StringBuffer buffer) {
        return super.toString(indentLevel, buffer);
    }
    
}
