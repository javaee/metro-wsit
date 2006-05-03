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
import com.sun.xml.ws.policy.sourcemodel.PolicyModelTranslator;
import com.sun.xml.ws.policy.sourcemodel.PolicySourceModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author japod
 */
abstract class BuilderHandler{
    
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
    
    abstract void populate(PolicyMapExtender policyMapExtender) throws PolicyException;
    
    Collection<Policy> getPolicies() throws PolicyException {
        if (null==policyURIs) {
            throw new PolicyException("Policy URIs can not be null.");
        }
        if (null==policyStore) {
            throw new PolicyException("No policies defined.");
        }
        
        Collection<Policy> result = new ArrayList<Policy>(policyURIs.size());
        
        for (String policyURI:policyURIs) {
            result.add(PolicyModelTranslator.getTranslator().translate(policyStore.get(policyURI)));
        }
        
        return result;
    }
    
    Collection<PolicySubject> getPolicySubjects() throws PolicyException {
        Collection<Policy> policies = getPolicies();
        Collection<PolicySubject> result =  new ArrayList<PolicySubject>(policies.size());
        for (Policy policy:policies) {
            result.add(new PolicySubject(policySubject,policy));
        }
        return result;
    }
}
