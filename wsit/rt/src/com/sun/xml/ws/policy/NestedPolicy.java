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

package com.sun.xml.ws.policy;

import java.util.Arrays;
import java.util.Iterator;

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
    
    private NestedPolicy(final String name, final String policyId, final AssertionSet set) {
        super(NESTED_POLICY_TOSTRING_NAME, name, policyId, Arrays.asList(new AssertionSet[] { set }));
    }

    static NestedPolicy createNestedPolicy(final AssertionSet set) {
        return new NestedPolicy(set);
    }

    static NestedPolicy createNestedPolicy(final String name, final String policyId, final AssertionSet set) {
        return new NestedPolicy(name, policyId, set);
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
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof NestedPolicy)) {
            return false;
        }
        
        final NestedPolicy that = (NestedPolicy) obj;
        
        return super.equals(that);
    }    
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }    
    
    /**
     * An {@code Object.toString()} method override.
     */
    @Override
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
    @Override
    StringBuffer toString(final int indentLevel, final StringBuffer buffer) {
        return super.toString(indentLevel, buffer);
    }    
}
