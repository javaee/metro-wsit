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
import java.util.logging.Level;

/**
 * From CMT EJB methods generate wsdl:binding/wsdl:operations with semantically equivalent WS-AT Policy Assertion(s).
 * <p/>
 * Known limitation: not accounting for ejb deployment descriptor, only working off of TransactionAttribute annotations.
 */
public class TxMapUpdateProvider implements PolicyMapUpdateProvider {

    final static private TxLogger logger = TxLogger.getATLogger(TxMapUpdateProvider.class);
    
    static private boolean nonJavaEEContainer = false;

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
    public void update(final PolicyMapExtender policyMapMutator, final PolicyMap policyMap, 
                       final SEIModel model, final WSBinding wsBinding) throws PolicyException {
        final String METHOD_NAME="update";
        
        if (nonJavaEEContainer) {
            return;
        }
        
        // For each method of a CMT EJB, map its effective javax.ejb.TransactionAttribute to semantically equivalent 
        // ws-at policy assertion.
        if (model != null) {
            final Collection<? extends JavaMethod> methods = model.getJavaMethods();
            Class CMTEJB = null;
            TransactionAttributeType classDefaultTxnAttr = null;
            WSDLPort port = null;
            for (JavaMethod method : methods) {

                if (CMTEJB == null) {
                    boolean isCMTEJB = false;
                    final Class theClass = method.getSEIMethod().getDeclaringClass();
                    try {
                        isCMTEJB = TransactionAnnotationProcessor.isContainerManagedEJB(theClass);
                    } catch (NoClassDefFoundError e) {
                      // running in a container that does not support EJBs; terminate processing of EJB annotations
                      nonJavaEEContainer = true;
                      if (logger.isLogging(Level.FINEST)) { 
                        logger.finest(METHOD_NAME, "handled exception " + e.getLocalizedMessage());
                      }
                      if (logger.isLogging(Level.INFO)) {
                        logger.info(METHOD_NAME, "running in a non Java EE container; disable mapping of Container Managed Transaction EJB to WS-AT Policy assertions");
                      }
                       return;  
                    }
                    if (isCMTEJB) {
                        // perform class level caching of info
                        CMTEJB = theClass;
                        classDefaultTxnAttr = TransactionAnnotationProcessor.getTransactionAttributeDefault(theClass);
                        port = method.getOwner().getPort();
                        if (port == null) {
                            if (logger.isLogging(Level.INFO)) {
                                logger.info(METHOD_NAME, theClass.getName() + " has a null ws port. Unable to map CMT EJB TransactionAttribute to WS-AT policy asssertion");
                            }
                            return;
                        }
                    } else {
                        // not a CMT EJB, no transaction attributes to look for; just return
                        return;
                    }
                }

                // we have a CMT EJB. Map its transaction attribute to proper ws-at policy assertion.
                final TransactionAttributeType txnAttr =
                        TransactionAnnotationProcessor.getEffectiveTransactionAttribute(method.getSEIMethod(), classDefaultTxnAttr);
                final Policy policy = mapTransactionAttribute2WSATPolicy(txnAttr);
                if (policy != null) {
                    // insert ws-at policy assertion in operation scope into policyMapMutator              
                    final WSDLBoundOperation wsdlBop = port.getBinding().getOperation(port.getName().getNamespaceURI(), method.getOperationName());
                    final PolicyMapKey operationKey =
                            PolicyMap.createWsdlOperationScopeKey(model.getServiceQName(),
                                    model.getPortName(), wsdlBop.getName());
                    final Policy existingPolicy = policyMap.getOperationEffectivePolicy(operationKey);
                    if (existingPolicy == null) {

                        // this is the case we should have for java to wsdl generation.  
                        // it is the only case we are intereste in for time being.
                        final PolicySubject wsatPolicySubject = new PolicySubject(wsdlBop, policy);
                        policyMapMutator.putOperationSubject(operationKey, wsatPolicySubject);
                        if (logger.isLogging(Level.INFO)) {
                            logger.info(METHOD_NAME, "for wsdl bounded operation " + wsdlBop.getName() + " add ws-at policy assertion(s) for effective javax.ejb.TransactionAttribute " +
                                    txnAttr.toString());
                        }
                    } else {
                        if (existingPolicy.contains(AT_ALWAYS_CAPABILITY) || policy.contains(AT_ASSERTION)) {
                            // TODO: Possible runtime check. 
                            // validate if existing WS-AT policy assertions are in synch with WS-AT Policy Assertions on CMT EJB. 
                            if (logger.isLogging(Level.FINE)) {
                                logger.fine(METHOD_NAME, "unexpected existing ws-at policy assertions");
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
        
        static private AssertionData createAssertionData(final QName assertionQName, final boolean isOptional) {
            final AssertionData result = AssertionData.createAssertionData(assertionQName);
            result.setOptionalAttribute(isOptional);
            return result;
        }
        
        WsatPolicyAssertion(final QName wsatPolicyAssertionName, final boolean isOptional) {
            super(createAssertionData(wsatPolicyAssertionName, isOptional), null, null);
        }
    }

    static final private WsatPolicyAssertion AT_ASSERTION_OPTIONAL = new WsatPolicyAssertion(AT_ASSERTION, true);
    static final private WsatPolicyAssertion AT_ASSERTION_REQUIRED = new WsatPolicyAssertion(AT_ASSERTION, false);
    static final private WsatPolicyAssertion AT_ALWAYS_CAPABILITY_PA = new WsatPolicyAssertion(AT_ALWAYS_CAPABILITY, false);

    static final private Policy MANDATORY_POLICY = createATPolicy(AT_ASSERTION_REQUIRED);
    static final private Policy SUPPORTS_POLICY = createATPolicy(AT_ASSERTION_OPTIONAL);
    static final private Policy REQUIRES_NEW_POLICY = createATPolicy(AT_ALWAYS_CAPABILITY_PA);
    static final private Policy REQUIRED_POLICY = createATPolicy(AT_ASSERTION_OPTIONAL, AT_ALWAYS_CAPABILITY_PA);

    /**
     * Pass in what the effective transaction attribute for a given Container Manager Transaction EJB method and return the
     * semantically closest WS-AT policy assertion.
     * <p/>
     * This is best match between Java EE Transaction Attribute and WS-AT Policy Assertion.
     * There are a number of differences between them.
     */
    private Policy mapTransactionAttribute2WSATPolicy(final TransactionAttributeType txnAttr) {

        switch (txnAttr) {
            case NOT_SUPPORTED:
            case NEVER:          // ws-at does not require exception thrown if txn propagated with no assertion.
                // no ws-at policy assertion on wsdl:binding/wsdl:operation is equivalent of no
                // claim.
                return null;

            case MANDATORY:
                return MANDATORY_POLICY;

            case SUPPORTS:
                return SUPPORTS_POLICY;

            case REQUIRES_NEW:
                return REQUIRES_NEW_POLICY;

            case REQUIRED:
                return REQUIRED_POLICY;

            default:
                return null;
        }
    }

    static private Policy createATPolicy(final WsatPolicyAssertion atpa) {
        return createATPolicy(atpa, null);
    }

    static private Policy createATPolicy(final WsatPolicyAssertion pa1, final WsatPolicyAssertion pa2) {
        final ArrayList<AssertionSet> assertionSets = new ArrayList<AssertionSet>(1);
        final int numAssertions = (pa2 == null ? 1 : 2);
        final ArrayList<PolicyAssertion> assertions = new ArrayList<PolicyAssertion>(numAssertions);
        assertions.add(pa1);
        if (pa2 != null) {
            assertions.add(pa2);
        }
        assertionSets.add(AssertionSet.createAssertionSet(assertions));
        return Policy.createPolicy(null, null, assertionSets);
    }

}
