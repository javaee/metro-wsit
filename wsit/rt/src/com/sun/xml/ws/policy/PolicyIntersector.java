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

import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import java.util.ArrayList;

/**
 * The instance of this class is intended to provide policy intersection mechanism.
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public final class PolicyIntersector {
    static enum CompatibilityMode {
        STRICT,
        LAX
    }

    private static final PolicyIntersector STRICT_INTERSECTOR = new PolicyIntersector(CompatibilityMode.STRICT);
    private static final PolicyIntersector LAX_INTERSECTOR = new PolicyIntersector(CompatibilityMode.LAX);
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyIntersector.class);
    
    private CompatibilityMode mode;

    /**
     * Prevents direct instantiation of this class from outside
     * @param intersectionMode intersection mode
     */
    private PolicyIntersector(CompatibilityMode intersectionMode) {
        this.mode = intersectionMode;
    }
    
    /**
     * Returns a strict policy intersector that can be used to intersect group of policies.
     *
     * @return policy intersector instance.
     */
    public static PolicyIntersector createStrictPolicyIntersector() {
        return PolicyIntersector.STRICT_INTERSECTOR;
    }
    
    /**
     * Returns a strict policy intersector that can be used to intersect group of policies.
     *
     * @return policy intersector instance.
     */
    public static PolicyIntersector createLaxPolicyIntersector() {
        return PolicyIntersector.LAX_INTERSECTOR;
    }
    
    /**
     * Performs intersection on the input collection of policies and returns the resulting (intersected) policy. If input policy
     * collection contains only a single policy instance, no intersection is performed and the instance is directly returned
     * as a method call result.
     *
     * @param policies collection of policies to be intersected. Must not be {@code null} nor empty, otherwise exception is thrown.
     * @return intersected policy as a result of perfromed policy intersection. A {@code null} value is never returned.
     *
     * @throws IllegalArgumentException in case {@code policies} argument is either {@code null} or empty collection.
     */
    public Policy intersect(final Policy... policies) {
        if (policies == null || policies.length == 0) {
            throw LOGGER.logSevereException(new IllegalArgumentException(LocalizationMessages.WSP_0056_NEITHER_NULL_NOR_EMPTY_POLICY_COLLECTION_EXPECTED()));
        } else if (policies.length == 1) {
            return policies[0];
        }
        
        // check for "null" and "empty" policy: if such policy is found return "null" policy, 
        // or if all policies are "empty", return "empty" policy
        boolean found = false;
        boolean allPoliciesEmpty = true;
        NamespaceVersion latestVersion = null;
        for (Policy tested : policies) {
            if (tested.isEmpty()) {
                found = true;
            } else {
                if (tested.isNull()) {
                    found = true;
                }
                allPoliciesEmpty = false;
            }
            if (latestVersion == null) {
                latestVersion = tested.getNamespaceVersion();
            } else if (latestVersion.compareTo(tested.getNamespaceVersion()) < 0) {
                latestVersion = tested.getNamespaceVersion();
            }
            
            if (found && !allPoliciesEmpty) {
                return Policy.createNullPolicy(latestVersion, null, null);
            }
        }
        latestVersion = (latestVersion != null) ? latestVersion : NamespaceVersion.getLatestVersion();
        if (allPoliciesEmpty) {
            return Policy.createEmptyPolicy(latestVersion, null, null);
        }
        
        // simple tests didn't lead to final answer => let's performe some intersecting ;)       
        final List<AssertionSet> finalAlternatives = new LinkedList<AssertionSet>(policies[0].getContent());
        final Queue<AssertionSet> testedAlternatives = new LinkedList<AssertionSet>();
        final List<AssertionSet> alternativesToMerge = new ArrayList<AssertionSet>(2);
        for (int i = 1; i < policies.length; i++) {
            final Collection<AssertionSet> currentAlternatives = policies[i].getContent();

            testedAlternatives.clear();
            testedAlternatives.addAll(finalAlternatives);
            finalAlternatives.clear();
            
            AssertionSet testedAlternative;
            while ((testedAlternative = testedAlternatives.poll()) != null) {
                for (AssertionSet currentAlternative : currentAlternatives) {
                    if (testedAlternative.isCompatibleWith(currentAlternative, this.mode)) {
                        alternativesToMerge.add(testedAlternative);
                        alternativesToMerge.add(currentAlternative);                        
                        finalAlternatives.add(AssertionSet.createMergedAssertionSet(alternativesToMerge));
                        alternativesToMerge.clear();
                    }
                }
            }
        }
        
        return Policy.createPolicy(latestVersion, null, null, finalAlternatives);
    }    
}
