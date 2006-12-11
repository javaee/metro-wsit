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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;

/**
 * This class provides method for translating {@link PolicySourceModel} structure into normalized {@link Policy} expression.
 * The resulting Policy is disconnected from its model, thus any additional changes in model will have no effect on the Policy
 * expression.
 *
 * @author Marek Potociar
 */
public final class PolicyModelTranslator {
    private static final class ContentDecomposition {
        final List<Collection<ModelNode>> exactlyOneContents = new LinkedList<Collection<ModelNode>>();
        final List<ModelNode> assertions = new LinkedList<ModelNode>();
        
        void reset() {
            exactlyOneContents.clear();
            assertions.clear();
        }
    }
    
    private static final class RawAssertion {
        ModelNode originalNode; // used to initialize nestedPolicy and nestedAssertions in the constructor of RawAlternative
        Collection<RawAlternative> nestedAlternatives = null;
        final Collection<ModelNode> parameters;
        
        RawAssertion(ModelNode originalNode, Collection<ModelNode> parameters) {
            this.parameters = parameters;
            this.originalNode = originalNode;
        }
    }
    
    private static final class RawAlternative {
        final List<RawPolicy> allNestedPolicies = new LinkedList<RawPolicy>(); // used to track the nested policies which need to be normalized
        final Collection<RawAssertion> nestedAssertions;
        
        RawAlternative(Collection<ModelNode> assertionNodes) throws PolicyException {
            this.nestedAssertions = new LinkedList<RawAssertion>();
            for (ModelNode node : assertionNodes) {
                RawAssertion assertion = new RawAssertion(node, new LinkedList<ModelNode>());
                nestedAssertions.add(assertion);
                
                for (ModelNode assertionNodeChild : assertion.originalNode.getContent()) {
                    switch (assertionNodeChild.getType()) {
                        case ASSERTION_PARAMETER_NODE:
                            assertion.parameters.add(assertionNodeChild);
                            break;
                        case POLICY:
                        case POLICY_REFERENCE:
                            if (assertion.nestedAlternatives == null) {
                                assertion.nestedAlternatives = new LinkedList<RawAlternative>();
                                RawPolicy nestedPolicy;
                                if (assertionNodeChild.getType() == ModelNode.Type.POLICY) {
                                    nestedPolicy = new RawPolicy(assertionNodeChild, assertion.nestedAlternatives);
                                } else {
                                    nestedPolicy = new RawPolicy(getReferencedModelRootNode(assertionNodeChild), assertion.nestedAlternatives);
                                }
                                this.allNestedPolicies.add(nestedPolicy);
                            } else {
                                throw new PolicyException(LocalizationMessages.UNEXPECTED_MULTIPLE_POLICY_NODES());
                            }
                            break;
                        default:
                            throw new PolicyException(LocalizationMessages.UNEXPECTED_CHILD_MODEL_TYPE(assertionNodeChild.getType()));
                    }
                }
            }
        }
        
    }
    
    private static final class RawPolicy {
        final Collection<ModelNode> originalContent;
        final Collection<RawAlternative> alternatives;
        
        RawPolicy(ModelNode policyNode, Collection<RawAlternative> alternatives) {
            originalContent = policyNode.getContent();
            this.alternatives = alternatives;
        }
    }
    
    private static final PolicyLogger logger = PolicyLogger.getLogger(PolicyModelTranslator.class);
    private static final PolicyModelTranslator translator = new PolicyModelTranslator();
    
    private static final PolicyAssertionCreator defaultCreator = new DefaultPolicyAssertionCreator();
    private static final Map<String, PolicyAssertionCreator> assertionCreators;
    private static final PolicyException initialException;
    
    static {
        Map<String, PolicyAssertionCreator> tempMap = null;
        PolicyException tempException = null;
        try {
            tempMap = initPolicyAssertionCreatorsMap();
        } catch (PolicyException ex) {
            tempException = ex;
        } finally {
            assertionCreators = tempMap;
            initialException = tempException;
        }
    }
    
    /**
     * Initializes the map of domain-specific policy policy assertion creators
     */
    private static Map<String, PolicyAssertionCreator> initPolicyAssertionCreatorsMap() throws PolicyException {
        logger.entering("initPolicyAssertionCreatorsMap");
        Map<String, PolicyAssertionCreator> pacMap = new HashMap<String, PolicyAssertionCreator>();
        
        PolicyAssertionCreator[] creators = PolicyUtils.ServiceProvider.load(PolicyAssertionCreator.class);
        for (PolicyAssertionCreator creator : creators) {
            String[] supportedURIs = creator.getSupportedDomainNamespaceURIs();
            String creatorClassName = creator.getClass().getName();
            
            if (supportedURIs == null || supportedURIs.length == 0) {
                logger.warning("initPolicyAssertionCreatorsMap", LocalizationMessages.ASSERTION_CREATOR_DOES_NOT_SUPPORT_ANY_URI(creatorClassName));
                continue;
            }
            
            for (String supportedURI : supportedURIs) {
                logger.config("initPolicyAssertionCreatorsMap", LocalizationMessages.ASSERTION_CREATOR_DISCOVERED(creatorClassName, supportedURI));
                if (supportedURI == null || supportedURI.length() == 0) {
                    throw new PolicyException(LocalizationMessages.ERROR_REGISTERING_ASSERTION_CREATOR(creatorClassName));
                }
                
                PolicyAssertionCreator oldCreator = pacMap.put(supportedURI, creator);
                if (oldCreator != null) {
                    logger.severe("initPolicyAssertionCreatorsMap", 
                            LocalizationMessages.ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(supportedURI, oldCreator.getClass().getName(), creator.getClass().getName()));
                    throw new PolicyException(LocalizationMessages.ERROR_MULTIPLE_ASSERTION_CREATORS_FOR_NAMESPACE(supportedURI, oldCreator.getClass().getName(), creator.getClass().getName()));
                }
            }
        }
        
        pacMap = Collections.unmodifiableMap(pacMap);
        logger.exiting("initPolicyAssertionCreatorsMap", pacMap);
        return pacMap;
    }
    
    /**
     * Method returns thread-safe policy model translator instance.
     *
     * @return a policy model translator instance.
     */
    public static PolicyModelTranslator getTranslator() throws PolicyException {
        if (initialException != null)
            throw initialException;
        
        return translator;
    }
    
    /**
     * The method translates {@link PolicySourceModel} structure into normalized {@link Policy} expression. The resulting Policy
     * is disconnected from its model, thus any additional changes in model will have no effect on the Policy expression.
     *
     * @param model the model to be translated into normalized policy expression. Must not be {@code null}.
     * @return translated policy expression in it's normalized form.
     * @throws PolicyException in case of translation failure
     */
    public Policy translate(PolicySourceModel model) throws PolicyException {
        logger.entering("translate", model);
        
        if (model == null) {
            throw new PolicyException(LocalizationMessages.POLICY_MODEL_TRANSLATION_ERROR_INPUT_PARAM_NULL());
        }
        
        PolicySourceModel localPolicyModelCopy;
        try {
            localPolicyModelCopy = model.clone();
        } catch (CloneNotSupportedException e) {
            logger.severe("translate", LocalizationMessages.UNABLE_TO_CLONE_POLICY_SOURCE_MODEL(), e);
            throw new PolicyException(LocalizationMessages.UNABLE_TO_CLONE_POLICY_SOURCE_MODEL(), e);
        }
        
        String policyId = localPolicyModelCopy.getPolicyId();
        String policyName = localPolicyModelCopy.getPolicyName();
        
        Collection<AssertionSet> alternatives = createPolicyAlternatives(localPolicyModelCopy);
        logger.finest("translate", LocalizationMessages.NUMBER_OF_ALTERNATIVE_COMBINATIONS_CREATED(alternatives.size()));
        
        Policy policy = null;
        if (alternatives.size() == 0) {
            policy = Policy.createNullPolicy(policyName, policyId);
            logger.finest("translate", LocalizationMessages.NO_ALTERNATIVE_COMBINATIONS_CREATED());
        } else if (alternatives.size() == 1 && alternatives.iterator().next().isEmpty()) {
            policy = Policy.createEmptyPolicy(policyName, policyId);
            logger.finest("translate", LocalizationMessages.SINGLE_EMPTY_ALTERNATIVE_COMBINATION_CREATED());
        } else {
            policy = Policy.createPolicy(policyName, policyId, alternatives);
            logger.finest("translate", 
                    LocalizationMessages.N_ALTERNATIVE_COMBINATIONS_M_POLICY_ALTERNATIVES_CREATED(alternatives.size(), policy.getNumberOfAssertionSets()));
        }
        
        logger.exiting("translate", policy);
        return policy;
    }
    
    /**
     * Method creates policy alternatives according to provided model. The model structure is modified in the process.
     *
     * @return created policy alternatives resulting from policy source model.
     */
    private Collection<AssertionSet> createPolicyAlternatives(PolicySourceModel model) throws PolicyException {
        // creating global method variables
        boolean emptyAlternativeFound = false;
        final ContentDecomposition decomposition = new ContentDecomposition();
        
        // creating processing queue and starting the processing iterations
        Queue<RawPolicy> policyQueue = new LinkedList<RawPolicy>();
        Queue<Collection<ModelNode>> contentQueue = new LinkedList<Collection<ModelNode>>();
        
        RawPolicy rootPolicy = new RawPolicy(model.getRootNode(), new LinkedList<RawAlternative>());
        RawPolicy processedPolicy = rootPolicy;
        do {
            Collection<ModelNode> processedContent = processedPolicy.originalContent;
            do {
                decompose(processedContent, decomposition);
                if (decomposition.exactlyOneContents.isEmpty()) {
                    RawAlternative alternative = new RawAlternative(decomposition.assertions);
                    processedPolicy.alternatives.add(alternative);
                    if (!alternative.allNestedPolicies.isEmpty()) {
                        policyQueue.addAll(alternative.allNestedPolicies);
                    }
                } else { // we have a non-empty collection of exactly ones
                    Collection<Collection<ModelNode>> combinations = PolicyUtils.Collections.combine(decomposition.assertions, decomposition.exactlyOneContents, false);
                    if (combinations != null && !combinations.isEmpty()) {
                        // processed alternative was split into some new alternatives, which we need to process
                        contentQueue.addAll(combinations);
                    }
                }
            } while ((processedContent = contentQueue.poll()) != null);
        } while ((processedPolicy = policyQueue.poll()) != null);
        
        // normalize nested policies to contain single alternative only
        Collection<AssertionSet> assertionSets = new LinkedList<AssertionSet>();
        for (RawAlternative rootAlternative : rootPolicy.alternatives) {
            Collection<AssertionSet> normalizedAlternatives = normalizeRawAlternative(rootAlternative);
            assertionSets.addAll(normalizedAlternatives);
        }
        
        return assertionSets;
    }
    
    /**
     * Decomposes the unprocessed alternative content into two different collections:
     * <p/>
     * Content of 'EXACTLY_ONE' child nodes is expanded and placed in one list and
     * 'ASSERTION' nodes are placed into other list. Direct 'ALL' and 'POLICY' child nodes are 'dissolved' in the process.
     *
     * Method reuses precreated ContentDecomposition object, which is reset before reuse.
     */
    private void decompose(Collection<ModelNode> content, ContentDecomposition decomposition) throws PolicyException {
        decomposition.reset();
        
        Queue<ModelNode> allContentQueue = new LinkedList<ModelNode>(content);
        ModelNode node;
        while ((node = allContentQueue.poll()) != null) {
            // dissolving direct 'POLICY', 'POLICY_REFERENCE' and 'ALL' child nodes
            switch (node.getType()) {
                case POLICY :
                case ALL :
                    allContentQueue.addAll(node.getContent());
                    break;
                case POLICY_REFERENCE :
                    allContentQueue.addAll(getReferencedModelRootNode(node).getContent());
                    break;
                case EXACTLY_ONE :
                    decomposition.exactlyOneContents.add(expandsExactlyOneContent(node.getContent()));
                    break;
                case ASSERTION :
                    decomposition.assertions.add(node);
                    break;
                default :
                    throw new PolicyException(LocalizationMessages.UNEXPECTED_MODEL_NODE_TYPE_FOUND(node.getType()));
            }
        }
    }
    
    private static ModelNode getReferencedModelRootNode(ModelNode policyReferenceNode) throws PolicyException {
        PolicySourceModel referencedModel = policyReferenceNode.getReferencedModel();
        if (referencedModel != null) {
            return referencedModel.getRootNode();
        } else {
            PolicyReferenceData refData = policyReferenceNode.getPolicyReferenceData();
            if (refData != null) {
                throw new PolicyException(LocalizationMessages.UNEXPANDED_POLICY_REFERENCE_NODE_FOUND_REFERENCING(refData.getReferencedModelUri()));
            } else {
                throw new PolicyException(LocalizationMessages.POLICY_REFERENCE_NODE_FOUND_WITH_NO_POLICY_REFERENCE_IN_IT());
            }
        }
    }
    
    /**
     * Expands content of 'EXACTLY_ONE' node. Direct 'EXACTLY_ONE' child nodes are dissolved in the process.
     */
    private Collection<ModelNode> expandsExactlyOneContent(Collection<ModelNode> content) throws PolicyException {
        Collection<ModelNode> result = new LinkedList<ModelNode>();
        
        Queue<ModelNode> eoContentQueue = new LinkedList<ModelNode>(content);
        ModelNode node;
        while ((node = eoContentQueue.poll()) != null) {
            // dissolving direct 'EXACTLY_ONE' child nodes
            switch (node.getType()) {
                case POLICY :
                case ALL :
                case ASSERTION :
                    result.add(node);
                    break;
                case POLICY_REFERENCE :
                    result.add(getReferencedModelRootNode(node));
                    break;
                case EXACTLY_ONE :
                    eoContentQueue.addAll(node.getContent());
                    break;
                default :
                    throw new PolicyException(LocalizationMessages.UNSUPPORTED_MODEL_NODE_TYPE(node.getType()));
            }
        }
        
        return result;
    }
    
    private List<AssertionSet> normalizeRawAlternative(RawAlternative alternative) throws PolicyException {
        List<PolicyAssertion> normalizedContentBase = new LinkedList<PolicyAssertion>();
        Collection<List<PolicyAssertion>> normalizedContentOptions = new LinkedList<List<PolicyAssertion>>();
        if (!alternative.nestedAssertions.isEmpty()) {
            Queue<RawAssertion> nestedAssertionsQueue = new LinkedList<RawAssertion>(alternative.nestedAssertions);
            RawAssertion a;
            while((a = nestedAssertionsQueue.poll()) != null) {
                List<PolicyAssertion> normalized = normalizeRawAssertion(a);
                // if there is only a single result, we can add it direclty to the content base collection
                // more elements in the result indicate that we will have to create combinations
                if (normalized.size() == 1) {
                    normalizedContentBase.addAll(normalized);
                } else {
                    normalizedContentOptions.add(normalized);
                }
            }
        }
        
        List<AssertionSet> options = new LinkedList<AssertionSet>();
        if (normalizedContentOptions.isEmpty()) {
            // we do not have any options to combine => returning this assertion
            options.add(AssertionSet.createAssertionSet(normalizedContentBase));
        } else {
            // we have some options to combine => creating assertion options based on content combinations
            Collection<Collection<PolicyAssertion>> contentCombinations = PolicyUtils.Collections.combine(normalizedContentBase, normalizedContentOptions, true);
            for (Collection<PolicyAssertion> contentOption : contentCombinations) {
                options.add(AssertionSet.createAssertionSet(contentOption));
            }
        }
        return options;
    }
    
    private List<PolicyAssertion> normalizeRawAssertion(RawAssertion assertion) throws PolicyException {
        List<PolicyAssertion> parameters;
        if (assertion.parameters.isEmpty()) {
            parameters = null;
        } else {
            parameters = new ArrayList<PolicyAssertion>(assertion.parameters.size());
            for (ModelNode parameterNode : assertion.parameters) {
                parameters.add(createPolicyAssertionParameter(parameterNode));
            }
        }
        
        List<AssertionSet> nestedAlternatives = new LinkedList<AssertionSet>();
        if (assertion.nestedAlternatives != null && !assertion.nestedAlternatives.isEmpty()) {
            Queue<RawAlternative> nestedAlternativeQueue = new LinkedList<RawAlternative>(assertion.nestedAlternatives);
            RawAlternative a;
            while((a = nestedAlternativeQueue.poll()) != null) {
                nestedAlternatives.addAll(normalizeRawAlternative(a));
            }
            // if there is only a single result, we can add it direclty to the content base collection
            // more elements in the result indicate that we will have to create combinations
        }        
        
        List<PolicyAssertion> assertionOptions = new LinkedList<PolicyAssertion>();
        boolean nestedAlternativesAvailable = !nestedAlternatives.isEmpty();
        if (nestedAlternativesAvailable) {
            for (AssertionSet nestedAlternative : nestedAlternatives) {
                assertionOptions.add(createPolicyAssertion(assertion.originalNode.getNodeData(), parameters, nestedAlternative));
            }
        } else {
            assertionOptions.add(createPolicyAssertion(assertion.originalNode.getNodeData(), parameters, null));
        }
        return assertionOptions;
    }
    
    private static PolicyAssertion createPolicyAssertionParameter(ModelNode parameterNode) throws PolicyException {
        if (parameterNode.getType() != ModelNode.Type.ASSERTION_PARAMETER_NODE) {
            throw new PolicyException(LocalizationMessages.INCONSISTENCY_IN_POLICY_SOURCE_MODEL(parameterNode.getType()));
        }
        
        List<PolicyAssertion> childParameters = null;
        if (parameterNode.hasChildren()) {
            childParameters = new ArrayList<PolicyAssertion>(parameterNode.childrenSize());
            for (ModelNode childParameterNode : parameterNode) {
                childParameters.add(createPolicyAssertionParameter(childParameterNode));
            }
        }
        
        return createPolicyAssertion(parameterNode.getNodeData(), childParameters, null /* parameters do not have any nested alternatives */);
    }
    
    private static PolicyAssertion createPolicyAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) throws AssertionCreationException {
        String assertionNamespace = data.getName().getNamespaceURI();
        PolicyAssertionCreator domainSpecificPAC = assertionCreators.get(assertionNamespace);
        
        
        if (domainSpecificPAC != null) {
            return domainSpecificPAC.createAssertion(data, assertionParameters, nestedAlternative, defaultCreator);
        } else {
            return defaultCreator.createAssertion(data, assertionParameters, nestedAlternative, null);
        }
    }
}
