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
package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
abstract class BuilderHandler{
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(BuilderHandler.class);
    
    Map<String,PolicySourceModel> policyStore;
    Collection<String> policyURIs;
    Object policySubject;
    
    /**
     * Creates a new instance of BuilderHandler
     */
    BuilderHandler(Collection<String> policyURIs, Map<String,PolicySourceModel> policyStore, Object policySubject) {
        this.policyStore = policyStore;
        this.policyURIs = policyURIs;
        this.policySubject = policySubject;
    }
    
    final void populate(final PolicyMapExtender policyMapExtender) throws PolicyException {
        if (null == policyMapExtender) {
            LOGGER.severe("populate", LocalizationMessages.POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL());
            throw new PolicyException(LocalizationMessages.POLICY_MAP_EXTENDER_CAN_NOT_BE_NULL());
        }
        
        doPopulate(policyMapExtender);
    }
    
    protected abstract void doPopulate(final PolicyMapExtender policyMapExtender) throws PolicyException;
    
    final Collection<Policy> getPolicies() throws PolicyException {
        if (null == policyURIs) {
            LOGGER.severe("getPolicies", LocalizationMessages.POLICY_URIS_CAN_NOT_BE_NULL());
            throw new PolicyException(LocalizationMessages.POLICY_URIS_CAN_NOT_BE_NULL());
        }
        if (null == policyStore) {
            LOGGER.severe("getPolicies", LocalizationMessages.NO_POLICIES_DEFINED());
            throw new PolicyException(LocalizationMessages.NO_POLICIES_DEFINED());
        }
        
        final Collection<Policy> result = new ArrayList<Policy>(policyURIs.size());
        
        for (String policyURI : policyURIs) {
            final PolicySourceModel sourceModel = policyStore.get(policyURI);
            if (sourceModel != null) {
                result.add(PolicyModelTranslator.getTranslator().translate(sourceModel));
            } else {
                LOGGER.severe("getPolicies", LocalizationMessages.POLICY_REFERENCE_DOES_NOT_EXIST(policyURI));
                throw new PolicyException(LocalizationMessages.POLICY_REFERENCE_DOES_NOT_EXIST(policyURI));
            }
        }
        
        return result;
    }
    
    final Collection<PolicySubject> getPolicySubjects() throws PolicyException {
        final Collection<Policy> policies = getPolicies();
        final Collection<PolicySubject> result =  new ArrayList<PolicySubject>(policies.size());
        for (Policy policy : policies) {
            result.add(new PolicySubject(policySubject, policy));
        }
        return result;
    }
}
