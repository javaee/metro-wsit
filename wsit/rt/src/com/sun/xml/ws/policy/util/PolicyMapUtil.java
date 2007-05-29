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

package com.sun.xml.ws.policy.util;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

/**
 * Utility methods for PolicyMap
 */
public final class PolicyMapUtil {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicyMapUtil.class);
    
    /**
     * Throw an exception if the policy map contains any policy with at least two
     * policy alternatives.
     *
     * Optional assertions are not considered (unless they have been normalized into
     * two policy alternatives).
     *
     * @param map policy map to be processed
     * @throws PolicyException Thrown if the policy map contains at least one policy
     * with more than one policy alternative
     */
    public static void rejectAlternatives(final PolicyMap map) throws PolicyException {
        for (Policy policy : map) {
            if (policy.getNumberOfAssertionSets() > 1) {
                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
            }            
        }
                
//        Collection<PolicyMapKey> keys = map.getAllServiceScopeKeys();
//        for (PolicyMapKey key : keys) {
//            final Policy policy = map.getServiceEffectivePolicy(key);
//            if (policy.getNumberOfAssertionSets() > 1) {
//                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
//            }
//        }
//        keys = map.getAllEndpointScopeKeys();
//        for (PolicyMapKey key : keys) {
//            final Policy policy = map.getEndpointEffectivePolicy(key);
//            if (policy.getNumberOfAssertionSets() > 1) {
//                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
//            }
//        }
//        keys = map.getAllOperationScopeKeys();
//        for (PolicyMapKey key : keys) {
//            final Policy policy = map.getOperationEffectivePolicy(key);
//            if (policy.getNumberOfAssertionSets() > 1) {
//                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
//            }
//        }
//        keys = map.getAllInputMessageScopeKeys();
//        for (PolicyMapKey key : keys) {
//            final Policy policy = map.getInputMessageEffectivePolicy(key);
//            if (policy.getNumberOfAssertionSets() > 1) {
//                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
//            }
//        }
//        keys = map.getAllOutputMessageScopeKeys();
//        for (PolicyMapKey key : keys) {
//            final Policy policy = map.getOutputMessageEffectivePolicy(key);
//            if (policy.getNumberOfAssertionSets() > 1) {
//                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
//            }
//        }
//        keys = map.getAllFaultMessageScopeKeys();
//        for (PolicyMapKey key : keys) {
//            final Policy policy = map.getFaultMessageEffectivePolicy(key);
//            if (policy.getNumberOfAssertionSets() > 1) {
//                throw LOGGER.logSevereException(new PolicyException(LocalizationMessages.WSP_0035_RECONFIGURE_ALTERNATIVES(policy.getIdOrName())));
//            }
//        }
    }
}
