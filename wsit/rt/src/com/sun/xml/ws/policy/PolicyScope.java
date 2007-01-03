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

import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A policy scope is a collection of equally ranked elements or subjects that
 * hold policies
 */
final class PolicyScope {
    private List<PolicySubject> subjects = new LinkedList<PolicySubject>();
    
    PolicyScope(List<PolicySubject> initialSubjects) {
        if (initialSubjects != null && !initialSubjects.isEmpty()) {
            this.subjects.addAll(initialSubjects);
        }
    }
    
    void attach(final PolicySubject subject) {
        if (subject == null) {
            throw new NullPointerException(LocalizationMessages.SUBJECT_PARAM_MUST_NOT_BE_NULL());
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
