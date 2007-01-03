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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Merge policies and return the effective policy.
 *
 * WS-PolicyAttachment defines a merge algorithm for WSDL 1.1 policy attachments.
 */
public final class PolicyMerger {
    private static final PolicyMerger policyMerger = new PolicyMerger();
    
    private PolicyMerger() {
        
    }
    
    /**
     * Factory method for obtaining thread-safe policy merger instance.
     *
     * @return policy merger instance.
     */
    public static PolicyMerger getMerger() {
        return policyMerger;
    }
    
    /**
     * Takes collection of policies and merges them into a single policy using algorithm described in
     * WS-PolicyAttachment specification. None of the original policis in the collection is modified in
     * any way.
     *
     * @param collection of policies to be merged. The collection must not contain '{@code null}' elements!
     * @return merged policy containing combination of policy alternatives stored in all input policies.
     *         If provided collection of policies is {@code null} or empty, returns {@code null}. If provided
     *         collection of policies contains only single policy, the policy is returned.
     * @throws NullPointerException if any element of input policies collection is {@code null}.
     */
    public Policy merge(final Collection<Policy> policies) throws PolicyException {
        if (policies == null || policies.isEmpty()) {
            return null;
        } else if (policies.size() == 1) {
            return policies.iterator().next();
        }
        
        final Collection<Collection<AssertionSet>> alternativeSets = new LinkedList<Collection<AssertionSet>>();
        for (Policy policy : policies) {
            alternativeSets.add(policy.getContent());
        }
        
        final Collection<Collection<AssertionSet>> combinedAlternatives = PolicyUtils.Collections.combine(null, alternativeSets, false);
        
        if (combinedAlternatives == null || combinedAlternatives.isEmpty()) {
            return Policy.createNullPolicy();
        } else {
            final Collection<AssertionSet> mergedSetList = new ArrayList<AssertionSet>(combinedAlternatives.size());
            for (Collection<AssertionSet> toBeMerged : combinedAlternatives) {
                mergedSetList.add(AssertionSet.createMergedAssertionSet(toBeMerged));
            }
            return Policy.createPolicy(mergedSetList);
        }
    }
}
