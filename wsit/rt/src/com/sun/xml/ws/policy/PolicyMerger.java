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

import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.sourcemodel.wspolicy.NamespaceVersion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Merge policies and return the effective policy.
 *
 * WS-PolicyAttachment defines a merge algorithm for WSDL 1.1 policy attachments.
 */
public final class PolicyMerger {
    private static final PolicyMerger merger = new PolicyMerger();
    
    /**
     * This private constructor is to avoid direct class instantiation from outsied of the package
     */
    private PolicyMerger() {
        // nothing to instantiate
    }
    
    /**
     * Factory method for obtaining thread-safe policy merger instance.
     *
     * @return policy merger instance.
     */
    public static PolicyMerger getMerger() {
        return merger;
    }
    
    /**
     * Takes collection of policies and merges them into a single policy using algorithm described in
     * WS-PolicyAttachment specification. None of the original policis in the collection is modified in
     * any way.
     *
     * @param policies collection of policies to be merged. The collection must not contain '{@code null}' elements!
     * @return merged policy containing combination of policy alternatives stored in all input policies.
     *         If provided collection of policies is {@code null} or empty, returns {@code null}. If provided
     *         collection of policies contains only single policy, the policy is returned.
     */
    public Policy merge(final Collection<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            return null;
        } else if (policies.size() == 1) {
            return policies.iterator().next();
        }
        
        final Collection<Collection<AssertionSet>> alternativeSets = new LinkedList<Collection<AssertionSet>>();
        NamespaceVersion mergedVersion = policies.iterator().next().getNamespaceVersion();
        for (Policy policy : policies) {
            alternativeSets.add(policy.getContent());
            if (mergedVersion.compareTo(policy.getNamespaceVersion()) < 0) {
                mergedVersion = policy.getNamespaceVersion();
            }
        }
        
        final Collection<Collection<AssertionSet>> combinedAlternatives = PolicyUtils.Collections.combine(null, alternativeSets, false);
        
        if (combinedAlternatives == null || combinedAlternatives.isEmpty()) {
            return Policy.createNullPolicy(mergedVersion, null, null);
        } else {
            final Collection<AssertionSet> mergedSetList = new ArrayList<AssertionSet>(combinedAlternatives.size());
            for (Collection<AssertionSet> toBeMerged : combinedAlternatives) {
                mergedSetList.add(AssertionSet.createMergedAssertionSet(toBeMerged));
            }
            return Policy.createPolicy(mergedVersion, null, null, mergedSetList);
        }
    }
}
