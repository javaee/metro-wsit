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
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator.Fitness;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Contains static methods for policy alternative selection. Given policy map is changed so that
 * each effective policy contains at most one policy alternative. Uses domain
 * specific @see com.sun.xml.ws.policy.spi.PolicySelector
 * to find out whether particular policy assertion is actually supported.
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class EffectiveAlternativeSelector {
    private enum AlternativeFitness {
        UNEVALUATED {
            AlternativeFitness combine(final Fitness assertionFitness) {
                switch (assertionFitness) {
                    case UNKNOWN:
                        return UNKNOWN;
                    case UNSUPPORTED:
                        return UNSUPPORTED;
                    case SUPPORTED:
                        return SUPPORTED;
                    case INVALID:
                        return INVALID;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        INVALID {
            AlternativeFitness combine(final Fitness assertionFitness) {
                return INVALID;
            }
        },
        UNKNOWN {
            AlternativeFitness combine(final Fitness assertionFitness) {
                switch (assertionFitness) {
                    case UNKNOWN:
                        return UNKNOWN;
                    case UNSUPPORTED:
                        return UNSUPPORTED;
                    case SUPPORTED:
                        return PARTIALLY_SUPPORTED;
                    case INVALID:
                        return INVALID;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        UNSUPPORTED {
            AlternativeFitness combine(final Fitness assertionFitness) {
                switch (assertionFitness) {
                    case UNKNOWN:
                    case UNSUPPORTED:
                        return UNSUPPORTED;
                    case SUPPORTED:
                        return PARTIALLY_SUPPORTED;
                    case INVALID:
                        return INVALID;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        PARTIALLY_SUPPORTED {
            AlternativeFitness combine(final Fitness assertionFitness) {
                switch (assertionFitness) {
                    case UNKNOWN:
                    case UNSUPPORTED:
                    case SUPPORTED:
                        return PARTIALLY_SUPPORTED;
                    case INVALID:
                        return INVALID;
                    default:
                        return UNEVALUATED;
                }
            }
        },
        SUPPORTED_EMPTY {
            AlternativeFitness combine(final Fitness assertionFitness) {
                // will not localize - this exception may not occur if there is no programatic error in this class
                throw new UnsupportedOperationException("Combine operation was called unexpectedly on 'SUPPORTED_EMPTY' alternative fitness enumeration state.");
            }
        },
        SUPPORTED {
            AlternativeFitness combine(final Fitness assertionFitness) {
                switch (assertionFitness) {
                    case UNKNOWN:
                    case UNSUPPORTED:
                        return PARTIALLY_SUPPORTED;
                    case SUPPORTED:
                        return SUPPORTED;
                    case INVALID:
                        return INVALID;
                    default:
                        return UNEVALUATED;
                }
            }
        };
        
        abstract AlternativeFitness combine(Fitness assertionFitness);
    }
    
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(EffectiveAlternativeSelector.class);
    
    /**
     * Does the selection for policy map bound to given modifier
     *
     * @param modifier @see EffectivePolicyModifier which the map is bound to
     */
    public static final void doSelection(final EffectivePolicyModifier modifier) throws PolicyException {
        final PolicyMap map = modifier.getMap();
        final AssertionValidationProcessor validationProcessor = AssertionValidationProcessor.getInstance();
        
        for (PolicyMapKey mapKey : map.getAllServiceScopeKeys()) {
            final Policy oldPolicy = map.getServiceEffectivePolicy(mapKey);
            modifier.setNewEffectivePolicyForServiceScope(mapKey, selectBestAlternative(oldPolicy, validationProcessor));
        }
        for (PolicyMapKey mapKey : map.getAllEndpointScopeKeys()) {
            final Policy oldPolicy = map.getEndpointEffectivePolicy(mapKey);
            modifier.setNewEffectivePolicyForEndpointScope(mapKey, selectBestAlternative(oldPolicy, validationProcessor));
        }
        for (PolicyMapKey mapKey : map.getAllOperationScopeKeys()) {
            final Policy oldPolicy = map.getOperationEffectivePolicy(mapKey);
            modifier.setNewEffectivePolicyForOperationScope(mapKey, selectBestAlternative(oldPolicy, validationProcessor));
        }
        for (PolicyMapKey mapKey : map.getAllInputMessageScopeKeys()) {
            final Policy oldPolicy = map.getInputMessageEffectivePolicy(mapKey);
            modifier.setNewEffectivePolicyForInputMessageScope(mapKey, selectBestAlternative(oldPolicy, validationProcessor));
        }
        for (PolicyMapKey mapKey : map.getAllOutputMessageScopeKeys()) {
            final Policy oldPolicy = map.getOutputMessageEffectivePolicy(mapKey);
            modifier.setNewEffectivePolicyForOutputMessageScope(mapKey, selectBestAlternative(oldPolicy, validationProcessor));
        }
        for (PolicyMapKey mapKey : map.getAllFaultMessageScopeKeys()) {
            final Policy oldPolicy = map.getFaultMessageEffectivePolicy(mapKey);
            modifier.setNewEffectivePolicyForFaultMessageScope(mapKey, selectBestAlternative(oldPolicy, validationProcessor));
        }
    }
    
    private static Policy selectBestAlternative(final Policy policy, final AssertionValidationProcessor validationProcessor) throws PolicyException {
        AssertionSet bestAlternative = null;
        AlternativeFitness bestAlternativeFitness = AlternativeFitness.UNEVALUATED;
        for (AssertionSet alternative : policy) {
            AlternativeFitness alternativeFitness = (alternative.isEmpty()) ? AlternativeFitness.SUPPORTED_EMPTY : AlternativeFitness.UNEVALUATED;
            for ( PolicyAssertion assertion : alternative ) {
                
                final Fitness assertionFitness = validationProcessor.validateClientSide(assertion);
                switch(assertionFitness) {
                    case UNKNOWN:
                    case UNSUPPORTED:
                    case INVALID:
                        LOGGER.warning(LocalizationMessages.WSP_0075_PROBLEMATIC_ASSERTION_STATE(assertion.getName(), assertionFitness));
                        break;
                    default:
                        break;
                }
                
                alternativeFitness = alternativeFitness.combine(assertionFitness);
            }
            
            if (bestAlternativeFitness.compareTo(alternativeFitness) < 0) {
                // better alternative found
                bestAlternative = alternative;
                bestAlternativeFitness = alternativeFitness;
            }
            
            if (bestAlternativeFitness == AlternativeFitness.SUPPORTED) {
                // all assertions supported by at least one selector
                break;
            }
        }
        
        switch (bestAlternativeFitness) {
            case INVALID:
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0053_INVALID_CLIENT_SIDE_ALTERNATIVE()));
            case UNKNOWN:
            case UNSUPPORTED:
            case PARTIALLY_SUPPORTED:
                LOGGER.warning(LocalizationMessages.WSP_0019_SUBOPTIMAL_ALTERNATIVE_SELECTED(bestAlternativeFitness));
                break;
            default:
                break;
        }
        
        Collection<AssertionSet> alternativeSet = null;
        if (bestAlternative != null) {
            // return a policy containing just the picked alternative
            alternativeSet = new LinkedList<AssertionSet>();
            alternativeSet.add(bestAlternative);
        }
        return Policy.createPolicy(policy.getName(), policy.getId(), alternativeSet);
    }
}
