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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Marek Potociar
 */
public final class PolicyModelGenerator {
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyModelTranslator.class);
    private static final PolicyModelGenerator generator = new PolicyModelGenerator();
    
    private PolicyModelGenerator () {
        
    }
    
    public static PolicyModelGenerator getGenerator() throws PolicyException {
        return generator;
    }
    
    /**
     * This method translates a {@link Policy} into a
     * {@link com.sun.xml.ws.policy.sourcemodel policy infoset}. The resulting
     * PolicySourceModel is disconnected from the input policy, thus any
     * additional changes in the policy will have no effect on the PolicySourceModel.
     *
     * @param policy The policy to be translated into an infoset. May be null.
     * @return translated The policy infoset. May be null if the input policy was
     * null.
     * @throw PolicyException in case Policy translation fails.
     */
    public PolicySourceModel translate(Policy policy) throws PolicyException {
        logger.entering("translate", policy);
        
        PolicySourceModel model = null;
        
        if (policy == null) {
            logger.fine("translate", "policy is null, returning");
        } else {
            model = PolicySourceModel.createPolicySourceModel(policy.getId(), policy.getName());
            ModelNode rootNode = model.getRootNode();
            ModelNode exactlyOneNode = rootNode.createChildExactlyOneNode();
            for (AssertionSet set : policy) {
                ModelNode alternativeNode = exactlyOneNode.createChildAllNode();
                for (PolicyAssertion assertion : set) {
                    // TODO Add getAssertionData method to PolicyAssertion?
                    AssertionData data = new AssertionData(assertion.getName(), assertion.getValue(), assertion.getAttributes());
                    ModelNode assertionNode = alternativeNode.createChildAssertionNode(data);
                    if (assertion.hasNestedPolicy()) {
                        NestedPolicy nestedPolicy = assertion.getNestedPolicy();
                        ModelNode nestedPolicyNode = translate(assertionNode, nestedPolicy);
                    }
                    if (assertion.hasNestedAssertions()) {
                        translate(assertion.getNestedAssertionsIterator(), assertionNode);
                    }
                }
            }
        }
        
        logger.exiting("translate", model);
        return model;
    }
    
    /**
     * Iterates through a nested policy and return the corresponding policy info model.
     *
     * @param policy The nested policy
     * @return The nested policy translated to the policy info model
     */
    private ModelNode translate(ModelNode parentAssertion, NestedPolicy policy) {
        ModelNode nestedPolicyRoot = parentAssertion.createChildPolicyNode();
        ModelNode exactlyOneNode = nestedPolicyRoot.createChildExactlyOneNode();
        AssertionSet set = policy.getAssertionSet();
        ModelNode alternativeNode = exactlyOneNode.createChildAllNode();
        for (PolicyAssertion assertion : set) {
            AssertionData data = new AssertionData(assertion.getName(), assertion.getValue(), assertion.getAttributes());
            ModelNode assertionNode = alternativeNode.createChildAssertionNode(data);
            if (assertion.hasNestedPolicy()) {
                NestedPolicy nestedPolicy = assertion.getNestedPolicy();
                ModelNode nestedPolicyNode = translate(assertionNode, nestedPolicy);
            }
            if (assertion.hasNestedAssertions()) {
                translate(assertion.getNestedAssertionsIterator(), assertionNode);
            }
        }
        return nestedPolicyRoot;
    }
    
    /**
     * Iterates through all contained assertions and adds them to the info model.
     *
     * @param assertions The set of contained assertions
     * @param assertionNode The node to which the assertions are added as child nodes
     */
    private void translate(Iterator<PolicyAssertion> nestedAssertionsIterator, ModelNode assertionNode) {
        while (nestedAssertionsIterator.hasNext()) {
            PolicyAssertion nestedAssertion = nestedAssertionsIterator.next();
            AssertionData data = new AssertionData(nestedAssertion.getName(), nestedAssertion.getValue(), nestedAssertion.getAttributes());
            ModelNode nestedAssertionNode = assertionNode.createChildAssertionNode(data);
            if (nestedAssertion.hasNestedPolicy()) {
                NestedPolicy nestedPolicy = nestedAssertion.getNestedPolicy();
                ModelNode nestedPolicyNode = translate(nestedAssertionNode, nestedPolicy);
            }
            if (nestedAssertion.hasNestedAssertions()) {
                translate(nestedAssertion.getNestedAssertionsIterator(), nestedAssertionNode);
            }
        }
    }    
}
