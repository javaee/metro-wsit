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

import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;

/**
 * The instance of this class is intended to provide policy intersection mechanism.
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public final class PolicyIntersector {
    private static final PolicyIntersector INSTANCE = new PolicyIntersector();
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyIntersector.class);
    
    /**
     * Creates a new instance of PolicyIntersector
     */
    private PolicyIntersector() {
    }
    
    /**
     * Returns a policy intersector that can be used to intersect group of policies.
     *
     * @return policy intersector instance.
     */
    public static PolicyIntersector createPolicyIntersector() {
        return PolicyIntersector.INSTANCE;
    }
    
    /**
     * Performs intersection on the input collection of policies and returns the resulting (intersected) policy. If input policy
     * collection contains only a single policy instance, no intersection is performed and the instance is directly returned
     * as a method call result.
     *
     * @param policies collection of policies to be intersected. Must not be {@code null} nor empty, otherwise exception is thrown.
     * @return intersected policy as a result of perfromed policy intersection. A {@code null} value is never returned.
     *
     * @throw IllegalArgumentException in case {@code policies} argument is either {@code null} or empty collection.
     */
    public Policy intersect(final Collection<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0056_NEITHER_NULL_NOR_EMPTY_POLICY_COLLECTION_EXPECTED()));
        } else if (policies.size() == 1) {
            return policies.iterator().next();
        }
        
        // check for "null" and "empty" policy: if such policy is found return "null" policy, or if all policies are "empty", return "empty" policy
        boolean found = false;
        boolean allPoliciesEmpty = true;
        for (Policy tested : policies) {
            if (tested.isEmpty()) {
                found = true;
            } else {
                if (tested.isNull()) {
                    found = true;
                }
                allPoliciesEmpty = false;
            }
            
            if (found && !allPoliciesEmpty) {
                return Policy.createNullPolicy();
            }
        }
        if (allPoliciesEmpty) {
            return Policy.createEmptyPolicy();
        }
        
        // simple tests didn't lead to final answer => let's performe some intersecting ;)
        final Iterator<Policy> policyIterator = policies.iterator();
        final List<AssertionSet> finalAlternatives = new LinkedList<AssertionSet>(policyIterator.next().getContent());
        while (policyIterator.hasNext()) {
            final Collection<AssertionSet> currentAlternatives = policyIterator.next().getContent();

            AssertionSet testedAlternative;
            final Queue<AssertionSet> testedAlternatives = new LinkedList<AssertionSet>(finalAlternatives);
            finalAlternatives.clear();
            while ((testedAlternative = testedAlternatives.poll()) != null) {
                for (AssertionSet currentAlternative : currentAlternatives) {
                    if (testedAlternative.isCompatibleWith(currentAlternative)) {
                        finalAlternatives.add(AssertionSet.createMergedAssertionSet(Arrays.asList(new AssertionSet[] {testedAlternative, currentAlternative})));
                    }
                }
            }
        }
        
        return Policy.createPolicy(finalAlternatives);
    }
    
    public Policy intersect(final Policy policyA, final Policy policyB) {
        return intersect(Arrays.asList(new Policy[] {policyA, policyB}));
    }
}
