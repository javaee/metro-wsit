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

/**
 * The class serves as a policy map mutator that allows for replacement of current effective policies 
 * stored in the policy map with new effective policy provided by the mutator user.
 *
 * @author Marek Potociar (marek.potociar@sun.com)
 */
public final class EffectivePolicyModifier extends PolicyMapMutator {
    public static EffectivePolicyModifier createEffectivePolicyModifier() {
        return new EffectivePolicyModifier();
    }
    
    /**
     * Creates a new instance of PolicyMapModifier
     */
    private EffectivePolicyModifier() {
    }
            
    /**
     * Replaces current effective policy on the service scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForServiceScope(PolicyMapKey key, Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.SERVICE, key, newEffectivePolicy);
    }
            
    /**
     * Replaces current effective policy on the endpoint scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForEndpointScope(PolicyMapKey key, Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.ENDPOINT, key, newEffectivePolicy);
    }
            
    /**
     * Replaces current effective policy on the operation scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForOperationScope(PolicyMapKey key, Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.OPERATION, key, newEffectivePolicy);
    }
            
    /**
     * Replaces current effective policy on the input message scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForInputMessageScope(PolicyMapKey key, Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.INPUT_MESSAGE, key, newEffectivePolicy);
    }
            
    /**
     * Replaces current effective policy on the output message scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForOutputMessageScope(PolicyMapKey key, Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.OUTPUT_MESSAGE, key, newEffectivePolicy);
    }
            
    /**
     * Replaces current effective policy on the fault message scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throw NullPointerException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForFaultMessageScope(PolicyMapKey key, Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.FAULT_MESSAGE, key, newEffectivePolicy);
    }
}
