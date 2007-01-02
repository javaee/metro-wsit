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
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Contains static methods for policy alternative selection. Given policy map is changed so that 
 * each effective policy contains at most one policy alternative. Uses domain 
 * specific @see com.sun.xml.ws.policy.spi.PolicySelector
 * to find out whether particular policy assertion is actually supported.
 *
 * @author japod
 */
public class EffectiveAlternativeSelector {
    
    private enum AssertionFitness {
        UNKNOWN {
            AssertionFitness considerFitness(final PolicyAssertionValidator.Fitness f) {
                switch (f) {
                    case UNKNOWN:
                        return AssertionFitness.UNKNOWN;
                    case SUPPORTED:
                        return AssertionFitness.SUPPORTED;
                    case UNSUPPORTED:
                    default:
                        return AssertionFitness.UNSUPPORTED;
                }
            }
        },
        SUPPORTED {
            AssertionFitness considerFitness(final PolicyAssertionValidator.Fitness f) {
                switch (f) {
                    case UNKNOWN:
                        return AssertionFitness.SUPPORTED;
                    case SUPPORTED:
                        return AssertionFitness.SUPPORTED;
                    case UNSUPPORTED:
                    default:
                        return AssertionFitness.AMBIVALENT;
                }
            }
        },
        UNSUPPORTED {
            AssertionFitness considerFitness(final PolicyAssertionValidator.Fitness f) {
                switch (f) {
                    case UNKNOWN:
                        return AssertionFitness.UNSUPPORTED;
                    case SUPPORTED:
                        return AssertionFitness.AMBIVALENT;
                    case UNSUPPORTED:
                    default:
                        return AssertionFitness.UNSUPPORTED;
                }
            }
        },
        AMBIVALENT {
            AssertionFitness considerFitness(final PolicyAssertionValidator.Fitness f) {
                return AssertionFitness.AMBIVALENT;
            }
        };
        
        abstract AssertionFitness considerFitness(PolicyAssertionValidator.Fitness f);
    }
    
    
    private enum AlternativeFitness {
        SUPPORTED {
            AlternativeFitness considerFitness(final AssertionFitness f) {
                switch (f) {
                    case UNKNOWN:
                        return PARTIALLY_SUPPORTED;
                    case UNSUPPORTED:
                        return AMBIVALENT;
                    case SUPPORTED:
                        return SUPPORTED;
                    case AMBIVALENT:
                        return AMBIVALENT;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        PARTIALLY_SUPPORTED {
            AlternativeFitness considerFitness(final AssertionFitness f) {
                switch (f) {
                    case UNKNOWN:
                        return PARTIALLY_SUPPORTED;
                    case UNSUPPORTED:
                        return AMBIVALENT;
                    case SUPPORTED:
                        return PARTIALLY_SUPPORTED;
                    case AMBIVALENT:
                        return AMBIVALENT;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        UNKNOWN {
            AlternativeFitness considerFitness(final AssertionFitness f) {
                switch (f) {
                    case UNKNOWN:
                        return UNKNOWN;
                    case UNSUPPORTED:
                        return UNSUPPORTED;
                    case SUPPORTED:
                        return PARTIALLY_SUPPORTED;
                    case AMBIVALENT:
                        return AMBIVALENT;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        AMBIVALENT {
            AlternativeFitness considerFitness(final AssertionFitness f) {
                        return AMBIVALENT;
            }
        },
        UNSUPPORTED {
            AlternativeFitness considerFitness(final AssertionFitness f) {
                switch (f) {
                    case UNKNOWN:
                        return UNSUPPORTED;
                    case UNSUPPORTED:
                        return UNSUPPORTED;
                    case SUPPORTED:
                        return AMBIVALENT;
                    case AMBIVALENT:
                        return AMBIVALENT;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        UNEVALUATED {
            AlternativeFitness considerFitness(final AssertionFitness f) {
                switch (f) {
                    case UNKNOWN:
                        return UNKNOWN;
                    case UNSUPPORTED:
                        return UNSUPPORTED;
                    case SUPPORTED:
                        return SUPPORTED;
                    case AMBIVALENT:
                        return AMBIVALENT;
                    default:
                        return UNEVALUATED;
                }
            }
        };
        
        abstract AlternativeFitness considerFitness(AssertionFitness f);
    }
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(EffectiveAlternativeSelector.class);
            
    /**
     * 
     * Does the selection for policy map bound to given modifier using only the given validators 
     * 
     * 
     * @param modifier @see EffectivePolicyModifier which the map is bound to
     * @param validators to be used
     */
    public static final void doSelection(final EffectivePolicyModifier modifier
            , final PolicyAssertionValidator... validators) throws PolicyException {
        
        final PolicyMap map = modifier.getMap();
        
        for (PolicyMapKey mapKey : map.getAllServiceScopeKeys()) {
            modifier.setNewEffectivePolicyForServiceScope(mapKey
                    ,getNewEffectivePolicy(map.getServiceEffectivePolicy(mapKey),validators));
        }
        for (PolicyMapKey mapKey : map.getAllEndpointScopeKeys()) {
            modifier.setNewEffectivePolicyForEndpointScope(mapKey
                    ,getNewEffectivePolicy(map.getEndpointEffectivePolicy(mapKey),validators));
        }
        for (PolicyMapKey mapKey : map.getAllOperationScopeKeys()) {
            modifier.setNewEffectivePolicyForOperationScope(mapKey
                    ,getNewEffectivePolicy(map.getOperationEffectivePolicy(mapKey),validators));
        }
        for (PolicyMapKey mapKey : map.getAllInputMessageScopeKeys()) {
            modifier.setNewEffectivePolicyForInputMessageScope(mapKey
                    ,getNewEffectivePolicy(map.getInputMessageEffectivePolicy(mapKey),validators));
        }
        for (PolicyMapKey mapKey : map.getAllOutputMessageScopeKeys()) {
            modifier.setNewEffectivePolicyForOutputMessageScope(mapKey
                    ,getNewEffectivePolicy(map.getOutputMessageEffectivePolicy(mapKey),validators));
        }
        for (PolicyMapKey mapKey : map.getAllFaultMessageScopeKeys()) {
            modifier.setNewEffectivePolicyForFaultMessageScope(mapKey
                    ,getNewEffectivePolicy(map.getFaultMessageEffectivePolicy(mapKey),validators));
        }
        
    }
    
    private static Policy getNewEffectivePolicy(
            final Policy oldPolicy, final PolicyAssertionValidator[] validators) throws PolicyException {
        
        if(null==validators || validators.length==0) {
            logger.warning("getNewEffectivePolicy", LocalizationMessages.NO_POLICY_SELECTORS_FOUND());
        }
        
        AssertionSet alternativePickedSoFar = null;
        AlternativeFitness bestFitnessSoFar = AlternativeFitness.UNEVALUATED;
        
        for (AssertionSet alternative : oldPolicy) {
            AlternativeFitness alternativeFitness = AlternativeFitness.UNEVALUATED;
            for ( PolicyAssertion assertion : alternative ) {  // foreach assertion in alternative
                AssertionFitness assertionFitness = AssertionFitness.UNKNOWN;
                for ( PolicyAssertionValidator selector : validators ) {   // foreach selector
                    assertionFitness = assertionFitness.considerFitness(selector.validateClientSide(assertion));
                } // end foreach selector
                alternativeFitness = alternativeFitness.considerFitness(assertionFitness);
                if (assertionFitness == assertionFitness.UNKNOWN) {
                    logger.warning("getNewEffectivePolicy", 
                            LocalizationMessages.ASSERTION_UNKNOWN(assertion.getName()));
                } else if (assertionFitness == assertionFitness.UNSUPPORTED) {
                    logger.warning("getNewEffectivePolicy", 
                            LocalizationMessages.ASSERTION_UNSUPPORTED(assertion.getName()));
                }
            } // end foreach assertion in current alternative
            
            if (alternativeFitness == AlternativeFitness.SUPPORTED) { // all assertions supported by at least one selector
                // will take this, alternativeSet will be instantiated just once
                final Collection<AssertionSet> alternativeSet = new LinkedList<AssertionSet>();
                alternativeSet.add(alternative);
                return Policy.createPolicy(oldPolicy.getName(), oldPolicy.getId(), alternativeSet);
            } // end-if all assertions supported
            
            if (bestFitnessSoFar.compareTo(alternativeFitness) > 0) { // better alternative found
                alternativePickedSoFar = alternative;
                bestFitnessSoFar = alternativeFitness;
            }
            // Make sure that if we have an empty alternative, at least one gets picked
            else if (alternativePickedSoFar == null) {
                alternativePickedSoFar = alternative;
            }
        }
        // return a policy containing just the picked alternative
        final Collection<AssertionSet> alternativeSet = new LinkedList<AssertionSet>();
        alternativeSet.add(alternativePickedSoFar);
        logger.warning("getNewEffectivePolicy", 
                LocalizationMessages.SUBOPTIMAL_ALTERNATIVE_PICKED_WITH_FITNESS(bestFitnessSoFar));        
        return Policy.createPolicy(oldPolicy.getName(), oldPolicy.getId(), alternativeSet);
    }
    
}
