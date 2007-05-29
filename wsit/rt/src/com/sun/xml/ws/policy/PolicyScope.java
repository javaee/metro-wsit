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

import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A policy scope is a collection of equally ranked elements or subjects that
 * hold policies
 */
final class PolicyScope {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyScope.class);
    
    private final List<PolicySubject> subjects = new LinkedList<PolicySubject>();
    
    PolicyScope(final List<PolicySubject> initialSubjects) {
        if (initialSubjects != null && !initialSubjects.isEmpty()) {
            this.subjects.addAll(initialSubjects);
        }
    }
    
    void attach(final PolicySubject subject) {
        if (subject == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0020_SUBJECT_PARAM_MUST_NOT_BE_NULL()));
        }
        
        subjects.add(subject);
    }
    
    void dettachAllSubjects() {
        subjects.clear();
    }
    
    /**
     * Returns all policies of the scope merged into one policy
     *
     * @return effective policy of the scope
     */
    Policy getEffectivePolicy(final PolicyMerger merger) throws PolicyException {
        final LinkedList<Policy> policies = new LinkedList<Policy>();
        for (PolicySubject subject : subjects) {
            policies.add(subject.getEffectivePolicy(merger));
        }
        return merger.merge(policies);
    }
    
    /**
     * Returns policies of the scope merged into one policy.
     *<p/>
     * Only policies with vocabulary containing the namespaces provided are merged into effective policy.
     *
     * @return effective policy with respect to the provided namespaces.
     */
    Policy getEffectivePolicy(final Collection<String> namespaces, final PolicyMerger merger) throws PolicyException {
        final LinkedList<Policy> policies = new LinkedList<Policy>();
        for (PolicySubject subject: subjects) {
            policies.add(subject.getEffectivePolicy(namespaces, merger));
        }
        return merger.merge(policies);
    }
    
    /**
     * Returns all subjects contained by this scope
     *
     * @return The subjects contained by this scope
     */
    Collection<PolicySubject> getPolicySubjects() {
        return this.subjects;
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
        final String indent = PolicyUtils.Text.createIndent(indentLevel);
        
        buffer.append(indent).append("policy scope {").append(PolicyUtils.Text.NEW_LINE);
        for (PolicySubject policySubject : subjects) {
            policySubject.toString(indentLevel + 1, buffer).append(PolicyUtils.Text.NEW_LINE);
        }        
        buffer.append(indent).append('}');
        
        return buffer;
    }    
}
