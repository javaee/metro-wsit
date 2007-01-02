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

package com.sun.xml.ws.policy.util;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import java.util.Collection;

/**
 * Utility methods for PolicyMap
 */
public final class PolicyMapUtil {

    /**
     * Throw an exception if the policy map contains any policy with at least two
     * policy alternatives.
     *
     * Optional assertions are not considered (unless they have been normalized into
     * two policy alternatives).
     *
     * @param A policy map
     * @throws PolicyException Thrown if the policy map contains at least one policy
     * with more than one policy alternative
     */
    public static void rejectAlternatives(final PolicyMap map) throws PolicyException {
        Collection<PolicyMapKey> keys = map.getAllServiceScopeKeys();
        for (PolicyMapKey key : keys) {
            final Policy policy = map.getServiceEffectivePolicy(key);
            if (policy.getNumberOfAssertionSets() > 1) {
                throw new PolicyException(LocalizationMessages.RECONFIGURE_ALTERNATIVES(policy.getIdOrName()));
            }
        }
        keys = map.getAllEndpointScopeKeys();
        for (PolicyMapKey key : keys) {
            final Policy policy = map.getEndpointEffectivePolicy(key);
            if (policy.getNumberOfAssertionSets() > 1) {
                throw new PolicyException(LocalizationMessages.RECONFIGURE_ALTERNATIVES(policy.getIdOrName()));
            }
        }
        keys = map.getAllOperationScopeKeys();
        for (PolicyMapKey key : keys) {
            final Policy policy = map.getOperationEffectivePolicy(key);
            if (policy.getNumberOfAssertionSets() > 1) {
                throw new PolicyException(LocalizationMessages.RECONFIGURE_ALTERNATIVES(policy.getIdOrName()));
            }
        }
        keys = map.getAllInputMessageScopeKeys();
        for (PolicyMapKey key : keys) {
            final Policy policy = map.getInputMessageEffectivePolicy(key);
            if (policy.getNumberOfAssertionSets() > 1) {
                throw new PolicyException(LocalizationMessages.RECONFIGURE_ALTERNATIVES(policy.getIdOrName()));
            }
        }
        keys = map.getAllOutputMessageScopeKeys();
        for (PolicyMapKey key : keys) {
            final Policy policy = map.getOutputMessageEffectivePolicy(key);
            if (policy.getNumberOfAssertionSets() > 1) {
                throw new PolicyException(LocalizationMessages.RECONFIGURE_ALTERNATIVES(policy.getIdOrName()));
            }
        }
        keys = map.getAllFaultMessageScopeKeys();
        for (PolicyMapKey key : keys) {
            final Policy policy = map.getFaultMessageEffectivePolicy(key);
            if (policy.getNumberOfAssertionSets() > 1) {
                throw new PolicyException(LocalizationMessages.RECONFIGURE_ALTERNATIVES(policy.getIdOrName()));
            }
        }
    }
}
