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
 * A PolicySubject is an entity (e.g., a port, operation, binding,
 * service) with which a policy can be associated.
 */
public final class PolicySubject {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicySubject.class);
    
    private final List<Policy> policies = new LinkedList<Policy>();
    private final Object subject;
    
    /**
     * Constructs a policy subject instance.
     *
     * @param subject object to which the policies are attached. Must not be {@code null}.
     * @param policy first policy attached to the subject. Must not be {@code null}.
     *
     * @throws IllegalArgumentException in case any of the arguments is {@code null}.
     */
    public PolicySubject(Object subject, Policy policy) throws IllegalArgumentException {
        if (subject == null || policy == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0021_SUBJECT_AND_POLICY_PARAM_MUST_NOT_BE_NULL(subject, policy)));
        }
        
        this.subject = subject;
        this.attach(policy);
    }
    
    /**
     * Constructs a policy subject instance.
     *
     * @param subject object to which the policies are attached. Must not be {@code null}.
     * @param policies first policy attached to the subject. Must not be {@code null}.
     *
     * @throws IllegalArgumentException in case any of the arguments is {@code null} or 
     *         in case {@code policies} argument represents empty collection.
     */
    public PolicySubject(Object subject, Collection<Policy> policies) throws IllegalArgumentException {
        if (subject == null || policies == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0062_INPUT_PARAMS_MUST_NOT_BE_NULL()));
        }
        
        if (policies.isEmpty()) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0064_INITIAL_POLICY_COLLECTION_MUST_NOT_BE_EMPTY()));
        }
        
        this.subject = subject;
        this.policies.addAll(policies);
    }
    
    /**
     * Attaches another Policy instance to the policy subject.
     *
     * @param policy new policy instance to be attached to this subject
     *
     * @throws IllegalArgumentException in case {@code policy} argument is {@code null}.
     */
    public void attach(final Policy policy) {
        if (policy == null) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0038_POLICY_TO_ATTACH_MUST_NOT_BE_NULL()));
        }
        this.policies.add(policy);
    }
    
    /**
     * Returns the effective policy of the subject, i.e. all policies of the subject
     * merged together.
     * 
     * @return effective policy of the subject
     */
    public Policy getEffectivePolicy(final PolicyMerger merger) throws PolicyException {
        return merger.merge(policies);
    }
    
    /**
     * Retrieve only the assertions of the effective policy that match the given set of
     * namespaces
     */
    public Policy getEffectivePolicy(
            final Collection<String> namespaces, final PolicyMerger merger) throws PolicyException {
        final LinkedList<Policy> reducedPolicies = new LinkedList<Policy>();
        for (Policy policy : policies) {
            // Policy reducedPolicy = policy.reduce(namespaces);
            // reducedPolicies.add(reducedPolicy);
        }
        return merger.merge(reducedPolicies);
    }
    
    /**
     * A unique identifier of the subject
     *
     * Subjects may not always be uniquely identifiable. Also, once the subject is
     * assigned to a scope, its identity may not matter anymore. Therefore this
     * may be null.
     */
    public Object getSubject() {
        return this.subject;
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
        final String innerIndent = PolicyUtils.Text.createIndent(indentLevel + 1);
        
        buffer.append(indent).append("policy subject {").append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("subject = '").append(subject).append('\'').append(PolicyUtils.Text.NEW_LINE);
        for (Policy policy : policies) {
            policy.toString(indentLevel + 1, buffer).append(PolicyUtils.Text.NEW_LINE);
        }
        buffer.append(indent).append('}');
        
        return buffer;
    }    
}
