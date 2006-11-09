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

package com.sun.xml.ws.tx.common;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.jaxws.spi.PolicyMapUpdateProvider;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import static com.sun.xml.ws.tx.common.Constants.AT_ALWAYS_CAPABILITY;
import static com.sun.xml.ws.tx.common.Constants.AT_ASSERTION;
import com.sun.xml.ws.tx.common.TransactionAnnotationProcessor.TransactionAttributeType;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * From CMT EJB methods generate wsdl:binding/wsdl:operations with semantically equivalent WS-AT Policy Assertion(s).
 * <p/>
 * Known limitation: not accounting for ejb deployment descriptor, only working off of TransactionAttribute annotations.
 */
public class TxMapUpdateProvider implements PolicyMapUpdateProvider {

    final static private TxLogger logger = TxLogger.getATLogger(TxMapUpdateProvider.class);

    /**
     * Update policy map with operation scope of correct ws-at policy assertions.
     * <p/>
     * Only looking for this for java to wsdl at tool time.
     *
     * @param policyMapMutator
     * @param policyMap
     * @param model
     * @param wsBinding
     */
    public void update(PolicyMapExtender policyMapMutator, PolicyMap policyMap, SEIModel model, WSBinding wsBinding) throws PolicyException {

        // For each method of a CMT EJB, map its effective javax.ejb.TransactionAttribute to semantically equivalent 
        // ws-at policy assertion.
        if (model != null) {
            Collection<? extends JavaMethod> methods = model.getJavaMethods();
            Class CMTEJB = null;
            TransactionAttributeType classDefaultTxnAttr = null;
            WSDLPort port = null;
            for (JavaMethod method : methods) {

                if (CMTEJB == null) {
                    Class theClass = method.getSEIMethod().getDeclaringClass();
                    if (TransactionAnnotationProcessor.isContainerManagedEJB(theClass)) {
                        // perform class level caching of info
                        CMTEJB = theClass;
                        classDefaultTxnAttr = TransactionAnnotationProcessor.getTransactionAttributeDefault(theClass);
                        port = method.getOwner().getPort();
                    } else {
                        // not a CMT EJB, no transaction attributes to look for; just return
                        return;
                    }
                }

                // we have a CMT EJB. Map its transaction attribute to proper ws-at policy assertion.
                TransactionAttributeType txnAttr =
                        TransactionAnnotationProcessor.getEffectiveTransactionAttribute(method.getSEIMethod(), classDefaultTxnAttr);
                Policy policy = mapTransactionAttribute2WSATPolicy(txnAttr);
                if (policy != null) {
                    // insert ws-at policy assertion in operation scope into policyMapMutator
                    WSDLBoundOperation wsdlBop = port.getBinding().getOperation(port.getName().getNamespaceURI(), method.getOperationName());
                    PolicyMapKey operationKey =
                            PolicyMap.createWsdlOperationScopeKey(port.getOwner().getName(),
                                    port.getName(), wsdlBop.getName());
                    Policy existingPolicy = policyMap.getOperationEffectivePolicy(operationKey);
                    if (existingPolicy == null) {

                        // this is the case we should have for java to wsdl generation.  
                        // it is the only case we are intereste in for time being.
                        PolicySubject wsatPolicySubject = new PolicySubject(wsdlBop, policy);
                        policyMapMutator.putOperationSubject(operationKey, wsatPolicySubject);
                        if (logger.isLogging(Level.INFO)) {
                            logger.info("update", "for wsdl bounded operation " + wsdlBop.getName() + " add ws-at policy assertion(s) for effective javax.ejb.TransactionAttribute " +
                                    txnAttr.toString());
                        }
                    } else {
                        if (existingPolicy.contains(AT_ALWAYS_CAPABILITY) || policy.contains(AT_ASSERTION)) {
                            // TODO: Possible runtime check. 
                            // validate if existing WS-AT policy assertions are in synch with WS-AT Policy Assertions on CMT EJB. 
                            if (logger.isLogging(Level.FINE)) {
                                logger.fine("update", "unexpected existing ws-at policy assertions");
                            }
                        } else {
                            // merge mew ws-at policy assertions into existingPolicy. 
                            // Don't know how to merge "policy" into "existingPolicy" and relation to existing PolicySubject in policyMap.
                            // policyMapMutator.createPolicyMapExtender().putOperationSubject(operationKey, mergedPolicies);
                        }
                    }
                }
            } // for each method in CMT EJB
        }
    }


    static class WsatPolicyAssertion extends PolicyAssertion {

        static private Map<QName, String> isOptional = null;

        private static Map<QName, String> getOptional() {
            if (isOptional == null) {
                isOptional = new HashMap<QName, String>(1);
                isOptional.put(PolicyConstants.OPTIONAL, "true");
            }
            return isOptional;
        }

        WsatPolicyAssertion(QName wsatPolicyAssertionName, boolean isOptional) {
            super(AssertionData.createAssertionData(wsatPolicyAssertionName, null,
                    isOptional == true ? getOptional() : null),
                    null,
                    null);
        }
    }

    static private WsatPolicyAssertion AT_ASSERTION_OPTIONAL = new WsatPolicyAssertion(AT_ASSERTION, true);
    static private WsatPolicyAssertion AT_ASSERTION_REQUIRED = new WsatPolicyAssertion(AT_ASSERTION, false);
    static private WsatPolicyAssertion AT_ALWAYS_CAPABILITY_PA = new WsatPolicyAssertion(AT_ALWAYS_CAPABILITY, false);


    static private Policy MANDATORY_POLICY = null;
    static private Policy SUPPORTS_POLICY = null;
    static private Policy REQUIRES_NEW_POLICY = null;
    static private Policy REQUIRED_POLICY = null;

    /**
     * Pass in what the effective transaction attribute for a given Container Manager Transaction EJB method and return the
     * semantically closest WS-AT policy assertion.
     * <p/>
     * This is best match between Java EE Transaction Attribute and WS-AT Policy Assertion.
     * There are a number of differences between them.
     */
    private Policy mapTransactionAttribute2WSATPolicy(TransactionAttributeType txnAttr) {

        switch (txnAttr) {
            case NOT_SUPPORTED:
            case NEVER:          // ws-at does not require exception thrown if txn propagated with no assertion.
                // no ws-at policy assertion on wsdl:binding/wsdl:operation is equivalent of no
                // claim.
                return null;

            case MANDATORY:
                if (MANDATORY_POLICY == null) {
                    MANDATORY_POLICY = createATPolicy(AT_ASSERTION_REQUIRED);
                }
                return MANDATORY_POLICY;

            case SUPPORTS:
                if (SUPPORTS_POLICY == null) {
                    SUPPORTS_POLICY = createATPolicy(AT_ASSERTION_OPTIONAL);
                }
                return SUPPORTS_POLICY;

            case REQUIRES_NEW:
                if (REQUIRES_NEW_POLICY == null) {
                    REQUIRES_NEW_POLICY = createATPolicy(AT_ALWAYS_CAPABILITY_PA);
                }
                return REQUIRES_NEW_POLICY;

            case REQUIRED:
                if (REQUIRED_POLICY == null) {
                    REQUIRED_POLICY = createATPolicy(AT_ASSERTION_OPTIONAL, AT_ALWAYS_CAPABILITY_PA);
                }
                return REQUIRED_POLICY;

            default:
                return null;
        }
    }

    private Policy createATPolicy(WsatPolicyAssertion pa) {
        return createATPolicy(pa, null);
    }

    private Policy createATPolicy(WsatPolicyAssertion pa1, WsatPolicyAssertion pa2) {
        ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        int numAssertions = (pa2 == null ? 1 : 2);
        ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>(numAssertions);
        assertions.add(pa1);
        if (pa2 != null) {
            assertions.add(pa2);
        }
        assertionSets.add(AssertionSet.createAssertionSet(assertions));
        return Policy.createPolicy(null, null, assertionSets);
    }

}
