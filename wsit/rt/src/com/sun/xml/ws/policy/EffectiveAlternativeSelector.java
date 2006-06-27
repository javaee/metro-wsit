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

import com.sun.xml.ws.policy.privateutil.ServiceFinder;
import com.sun.xml.ws.policy.spi.PolicySelector;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author japod
 */
public class EffectiveAlternativeSelector {
    
    static PolicySelector[] selectors = null;
    
    private static PolicySelector[] getSelectors() {
        if (selectors==null) {
            selectors = ServiceFinder.find(PolicySelector.class).toArray();
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
            throw new PolicyException("No alternative selectors found.");
        }
        for (AssertionSet alternative : oldPolicy) {
            boolean alternativeIsOk = true;
            testing:
                for ( PolicyAssertion assertion : alternative ) {  // foreach assertion in alternative
                    boolean assertionIsOk = false;                  // consider it is not supported
                    for ( PolicySelector selector : selectors ) {   // foreach selector
                        if (selector.isSupported(assertion.getName().getNamespaceURI())) { // namespace supported?
                            if (!selector.test(assertion)) {                    // assertion as well?
                                alternativeIsOk = false;                        // not -> drop the alternative 
                                break testing;                                  //     do not need further testing
                            } else { // single selector test ok
                                assertionIsOk = true;                           // at least one module supports it
                            }  // end if single assertion test ok
                        }
                    } // end foreach selector
                    if (!assertionIsOk) {       // no supportive selector found
                        alternativeIsOk = false;    // drop the whole alternative 
                        break testing;
                    } // end if !assertionIsOk
                } // end foreach assertion, end of testing:
                if (alternativeIsOk) { // supported alternative has been found
                    Collection<AssertionSet> alternativeSet = new LinkedList<AssertionSet>();
                    alternativeSet.add(alternative);
                    return new Policy(null,alternativeSet);
                } // endif this alternative is ok
        }
        throw new PolicyException("No supported alternative found.");
    }
}
