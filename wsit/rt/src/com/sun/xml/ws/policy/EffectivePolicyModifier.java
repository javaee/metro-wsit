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
     * Ensures that direct instantiation is not possible from outside of the class
     */
    private EffectivePolicyModifier() {
        // no initialization required
    }
            
    /**
     * Replaces current effective policy on the service scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter. If no policy was defined for the presented key, the new policy is simply
     * stored with the key.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throws IllegalArgumentException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForServiceScope( 
            final PolicyMapKey key, final Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.SERVICE, key, newEffectivePolicy);
    }
            
    /**
     * Replaces current effective policy on the endpoint scope (identified by a {@code key} parameter) with the new efective 
     * policy provided as a second input parameter.
     *
     * @param key identifier of the scope the effective policy should be replaced with the new one. Must not be {@code null}.
     * @param newEffectivePolicy the new policy to replace the old effective policy of the scope. Must not be {@code null}.
     *
     * @throws IllegalArgumentException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForEndpointScope(
            final PolicyMapKey key, final Policy newEffectivePolicy) {
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
     * @throws IllegalArgumentException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForOperationScope(
            final PolicyMapKey key, final Policy newEffectivePolicy) {
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
     * @throws IllegalArgumentException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForInputMessageScope(
            final PolicyMapKey key, final Policy newEffectivePolicy) {
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
     * @throws IllegalArgumentException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForOutputMessageScope(
            final PolicyMapKey key, final Policy newEffectivePolicy) {
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
     * @throws IllegalArgumentException in case any of the input parameters is {@code null}
     */
    public void setNewEffectivePolicyForFaultMessageScope(
            final PolicyMapKey key, final Policy newEffectivePolicy) {
        getMap().setNewEffectivePolicyForScope(PolicyMap.ScopeType.FAULT_MESSAGE, key, newEffectivePolicy);
    }
}
