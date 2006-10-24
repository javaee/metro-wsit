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
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.Messages;
import com.sun.xml.ws.policy.spi.PolicySelector;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author japod
 */
public class EffectiveAlternativeSelector {
    
    private static PolicySelector[] selectors = null;
    private static final PolicyLogger logger = PolicyLogger.getLogger(EffectiveAlternativeSelector.class);
    
    private static PolicySelector[] getSelectors() {
        if (selectors==null) {
            selectors = PolicyUtils.ServiceProvider.load(PolicySelector.class);
        }
        return selectors;
    }
    
    public static void doSelection(EffectivePolicyModifier modifier) throws PolicyException {
        doSelection(modifier,getSelectors());
    }
    
    /** Creates a new instance of EffectiveAlternativeSelector */
    public static void doSelection(EffectivePolicyModifier modifier
            , PolicySelector[] selectors) throws PolicyException {
        
        PolicyMap map = modifier.getMap();
        
        for (PolicyMapKey mapKey : map.getAllServiceScopeKeys()) {
            modifier.setNewEffectivePolicyForServiceScope(mapKey
                    ,getNewEffectivePolicy(map.getServiceEffectivePolicy(mapKey),selectors));
        }
        for (PolicyMapKey mapKey : map.getAllEndpointScopeKeys()) {
            modifier.setNewEffectivePolicyForEndpointScope(mapKey
                    ,getNewEffectivePolicy(map.getEndpointEffectivePolicy(mapKey),selectors));
        }
        for (PolicyMapKey mapKey : map.getAllOperationScopeKeys()) {
            modifier.setNewEffectivePolicyForOperationScope(mapKey
                    ,getNewEffectivePolicy(map.getOperationEffectivePolicy(mapKey),selectors));
        }
        for (PolicyMapKey mapKey : map.getAllInputMessageScopeKeys()) {
            modifier.setNewEffectivePolicyForInputMessageScope(mapKey
                    ,getNewEffectivePolicy(map.getInputMessageEffectivePolicy(mapKey),selectors));
        }
        for (PolicyMapKey mapKey : map.getAllOutputMessageScopeKeys()) {
            modifier.setNewEffectivePolicyForOutputMessageScope(mapKey
                    ,getNewEffectivePolicy(map.getOutputMessageEffectivePolicy(mapKey),selectors));
        }
        for (PolicyMapKey mapKey : map.getAllFaultMessageScopeKeys()) {
            modifier.setNewEffectivePolicyForFaultMessageScope(mapKey
                    ,getNewEffectivePolicy(map.getFaultMessageEffectivePolicy(mapKey),selectors));
        }
        
    }
    
    private static Policy getNewEffectivePolicy(
            Policy oldPolicy, PolicySelector[] selectors) throws PolicyException {
        
        if(null==selectors || selectors.length==0) {
            logger.warning("getNewEffectivePolicy", Messages.NO_POLICY_SELECTORS_FOUND.format());
        }
        
        AssertionSet alternativePickedSoFar = null;
        int fitnessBestSoFar = 0;
        
        for (AssertionSet alternative : oldPolicy) { 
            int supportedAssertions = 0;
            int weirdAssertions = 0;
            int unsupportedAssertions = 0;
            int unknownAssertions = 0;
            int numberOfAssertions = 0;
            for ( PolicyAssertion assertion : alternative ) {  // foreach assertion in alternative
                boolean supportedOne = false;
                boolean unsupportedOne = false;
                numberOfAssertions++;
                for ( PolicySelector selector : selectors ) {   // foreach selector
                    if (selector.isSupported(assertion.getName().getNamespaceURI())) { // namespace supported?
                        if (!selector.test(assertion)) {                    // assertion as well?
                            unsupportedOne = true;
                            logger.warning("getNewEffectivePolicy",
                                    Messages.ASSERTION_REJECTED_BY_POLICY_SELECTOR.format(assertion.getName(),
                                    selector.getClass().toString()));
                        } else { // single selector test ok
                            supportedOne = true;
                        }  // end if single assertion test ok
                    }
                } // end foreach selector
                if (supportedOne) {
                    if (unsupportedOne) {
                        weirdAssertions++;
                    } else {
                        supportedAssertions++;
                    }
                } else { // !supportedOne
                    if (unsupportedOne) {
                        unsupportedAssertions++;
                    } else {
                        unknownAssertions++;
                    }
                }
            } // end foreach assertion in current alternative
            
            if (supportedAssertions == numberOfAssertions) { // all assertions supported by at least one selector
                // will take this
                Collection<AssertionSet> alternativeSet = new LinkedList<AssertionSet>();
                alternativeSet.add(alternative);
                return new Policy(null,alternativeSet);
            } // end-if all assertions supported
            
            if (null == alternativePickedSoFar) { // first alternative, nothing to compare
                alternativePickedSoFar = alternative;
                fitnessBestSoFar = getFitness(supportedAssertions, unsupportedAssertions, 
                                            weirdAssertions, unknownAssertions, numberOfAssertions);
            } else {  // not the first alternative
                // if this one is better than that picked so far, pick this...
                if (fitnessBestSoFar < getFitness(supportedAssertions, unsupportedAssertions, 
                                            weirdAssertions, unknownAssertions, numberOfAssertions)) {
                alternativePickedSoFar = alternative;
                fitnessBestSoFar = getFitness(supportedAssertions, unsupportedAssertions, 
                                            weirdAssertions, unknownAssertions, numberOfAssertions);
                    
                }
            } // endif not the first alternative
        }
        // return a policy containing just the picked alternative
        Collection<AssertionSet> alternativeSet = new LinkedList<AssertionSet>();
        alternativeSet.add(alternativePickedSoFar);
        return new Policy(null,alternativeSet);
    }
    
    private static final int getFitness (int supp, int unsupp, int weird, int unkn, int total) {
        return (to3(supp, total) * 27) + (to3(unkn, total) * 9) + (to3(weird, total) * 3) + to3(unsupp,total);
    }
    
    private static final int to3(int op, int base) {
        if (op == 0) {
            return 0;
        } else {
            if (op == base) {
                return 2;
            } else {
                return 1;
            }
        }
    }
    
}
