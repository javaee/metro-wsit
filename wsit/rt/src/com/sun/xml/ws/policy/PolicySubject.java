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
    
    private List<Policy> policies = new LinkedList<Policy>();
    private Object subject;
    
    /**
     * Constructs a policy subject instance.
     *
     * @param subject object to which the policies are attached. Must not be {@code null}.
     * @param policy first policy attached to the subject. Must not be {@code null}.
     *
     * @throws NullPointerException in case any of the arguments is {@code null}.
     */
    public PolicySubject(Object subject, Policy policy) {
        if (subject == null || policy == null) {
            LOGGER.severe("<init>", LocalizationMessages.SUBJECT_AND_POLICY_PARAM_MUST_NOT_BE_NULL(subject, policy));
            throw new NullPointerException(LocalizationMessages.SUBJECT_AND_POLICY_PARAM_MUST_NOT_BE_NULL(subject, policy));
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
     * @throws NullPointerException in case any of the arguments is {@code null}.
     * @throws IllegalArgumentException in case {@code policies} argument represents empty collection.
     */
    public PolicySubject(Object subject, Collection<Policy> policies) {
        if (subject == null || policies == null) {
            LOGGER.severe("<init>", LocalizationMessages.INPUT_PARAMS_MUST_NOT_BE_NULL());
            throw new NullPointerException(LocalizationMessages.INPUT_PARAMS_MUST_NOT_BE_NULL());
        }
        
        if (policies.isEmpty()) {
            LOGGER.severe("<init>",LocalizationMessages.INITIAL_POLICY_COLLECTION_MUST_NOT_BE_EMPTY());
            throw new IllegalArgumentException(LocalizationMessages.INITIAL_POLICY_COLLECTION_MUST_NOT_BE_EMPTY());
        }
        
        this.subject = subject;
        this.policies.addAll(policies);
    }
    
    /**
     * Attaches another Policy instance to the policy subject.
     *
     * @param policy new policy instance to be attached to this subject
     *
     * @throw NullPointerException in case {@code policy} argument is {@code null}.
     */
    public void attach(final Policy policy) {
        if (policy == null) {
            LOGGER.severe("attach", LocalizationMessages.POLICY_TO_ATTACH_MUST_NOT_BE_NULL());
            throw new NullPointerException(LocalizationMessages.POLICY_TO_ATTACH_MUST_NOT_BE_NULL());
        }
        this.policies.add(policy);
    }
    
    /**
     * Returns the effective policy of the subject, i.e. all policies of the subject
     * merged together
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
