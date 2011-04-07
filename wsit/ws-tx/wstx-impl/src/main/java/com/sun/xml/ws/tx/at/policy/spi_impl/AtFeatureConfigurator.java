/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.tx.at.policy.spi_impl;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator;
import com.sun.xml.ws.api.tx.at.Transactional;
import com.sun.xml.ws.api.tx.at.Transactional.TransactionFlowType;
import com.sun.xml.ws.api.tx.at.TransactionalFeature;
import com.sun.xml.ws.api.tx.at.WsatNamespace;
import com.sun.xml.ws.tx.at.WSATImplInjection;
import com.sun.xml.ws.tx.at.localization.LocalizationMessages;
import com.sun.xml.ws.tx.at.policy.AtAssertion;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class AtFeatureConfigurator implements PolicyFeatureConfigurator {
    // TODO implement PolicyMapConfigurator as well

//    private static final Logger LOGGER = Logger.getLogger(AtFeatureConfigurator.class);
    private static final Class LOGGERCLASS = AtFeatureConfigurator.class;

    /**
     * Process WS-RM policy assertions and if found and is not optional then RM is enabled on the
     * {@link WSDLPort}
     *
     * @param endpointKey Key that identifies the endpoint scope
     * @param policyMap must not be {@code null}
     * @return The list of features
     * @throws PolicyException If retrieving the policy triggered an exception
     */
    public Collection<WebServiceFeature> getFeatures(PolicyMapKey endpointKey, PolicyMap policyMap) throws PolicyException {
        final Collection<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if (endpointKey == null || policyMap == null) {
            return features;
        }

        TransactionalFeature endpointFeature = getAtFeature(policyMap.getEndpointEffectivePolicy(endpointKey), false);
        if (endpointFeature != null) {
            features.add(endpointFeature);
        }

        for (PolicyMapKey key : policyMap.getAllOperationScopeKeys()) {
            if (!endpointKey.equals(key)) {
                continue;
            }

            final TransactionalFeature feature = getAtFeature(policyMap.getOperationEffectivePolicy(key), true);
            if (feature == null || !feature.isEnabled()) {
                continue;
            }

            if (endpointFeature == null) {
                endpointFeature = feature;
                features.add(endpointFeature);
            } else if (endpointFeature.getVersion() != feature.getVersion()) {
            String msg = WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.SEVERE, "WSAT1004_ENDPOINT_AND_OPERATION_POLICIES_DONT_MATCH",
                    endpointKey, null);
            throw new WebServiceException(msg);
//                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSAT_1004_ENDPOINT_AND_OPERATION_POLICIES_DONT_MATCH(endpointKey, key)));
            }

            endpointFeature.setExplicitMode(true);
            String opName = key.getOperation().getLocalPart();
            feature.setFlowType(opName, feature.getFlowType());
            feature.setEnabled(opName, true);
        }

        return features;
    }

    private TransactionalFeature getAtFeature(final Policy policy, final boolean setExplictMode) throws WebServiceException, PolicyException {
        if (policy == null) {
            return null;
        }

        TransactionalFeature resultFeature = null;
        for (AssertionSet alternative : policy) {
            TransactionalFeature feature = getAtFeature(alternative, setExplictMode);
            if (feature == null) {
                continue;
            }
            if (resultFeature == null) {
                resultFeature = feature;
            } else if (!areCompatible(resultFeature, feature)) { // Multiple Transactional features in a single effective policy must be compatible
            String msg = WSATImplInjection.getInstance().getLogging().log(
                    null, LOGGERCLASS, Level.SEVERE, "WSAT1003_INCOMPATIBLE_FEATURES_DETECTED", policy, null);
                throw new WebServiceException(msg);
//                throw LOGGER.logSevereException(new WebServiceException(LocalizationMessages.WSAT_1003_INCOMPATIBLE_FEATURES_DETECTED(policy.toString())));
            }
        } // end for all alternatives in policy

        return resultFeature;
    }

    private TransactionalFeature getAtFeature(final AssertionSet alternative, final boolean setExplicitMode) throws PolicyException {
        TransactionalFeature feature = null;
        for (PolicyAssertion assertion : alternative) {
            if (assertion instanceof AtAssertion) {
                if (feature != null) {
                    String msg = WSATImplInjection.getInstance().getLogging().log(
                            null, LOGGERCLASS, Level.SEVERE, "WSAT1001_DUPLICATE_ASSERTION_IN_POLICY",
                            alternative, null);
                    throw new WebServiceException(msg);
//                    throw LOGGER.logSevereException(
//                            new WebServiceException(LocalizationMessages.WSAT_1001_DUPLICATE_ASSERTION_IN_POLICY(alternative.toString())));
                }

                feature = new TransactionalFeature(true);
                feature.setExplicitMode(setExplicitMode);
                WsatNamespace version = WsatNamespace.forNamespaceUri(assertion.getName().getNamespaceURI());

                feature.setVersion(Transactional.Version.forNamespaceVersion(version));
                feature.setFlowType(assertion.isOptional() ? TransactionFlowType.SUPPORTS : TransactionFlowType.MANDATORY);
            }
        } // next assertion

        return feature;
    }

    private static boolean areCompatible(TransactionalFeature featureA, TransactionalFeature featureB) {
        boolean result = true;

        result = result && (featureA.isEnabled() == featureB.isEnabled());
        result = result && featureA.getVersion() == featureB.getVersion();
        result = result && featureA.getFlowType() == featureB.getFlowType();

        return result;
    }
//    public static TransactionalFeature buildFeatureFromWsdl(WsdlPort port, PolicyServer policyServer) {
//        TransactionalFeature feature = null;
//
//        try {
//            for (WsdlBindingOperation bindingOp : port.getBinding().getOperations().values()) {
//                String opName = bindingOp.getName().getLocalPart();
//                NormalizedExpression ne = WsdlPolicySubject.getOperationPolicySubject(policyServer, bindingOp, Collections.EMPTY_MAP);
//                Set assertionSet = ne.getPolicyAlternatives(ATAssertion.class);
//                if (assertionSet == null) {
//                    continue;
//                }
//
//                boolean hasATAssertion = false;
//
//                for (Iterator iterator = assertionSet.iterator(); iterator.hasNext();) {
//                    PolicyAlternative at = (PolicyAlternative) iterator.next();
//                    Set atSet = at.getAssertions(ATAssertion.class);
//                    for (Iterator iter = atSet.iterator(); iter.hasNext();) {
//                        PolicyAssertion pa = (PolicyAssertion) iter.next();
//                        if (pa instanceof ATAssertion) {
//                            if (feature == null) {
//                                feature = new TransactionalFeature();
//                                feature.setExplicitMode(true);
//                            }
//                            QName qName = pa.getName();
//                            Transactional.Version version = Transactional.Version.forNamespaceUri(qName.getNamespaceURI());
//
//                            if (hasATAssertion && version != feature.getVersion()) {
//                                feature.setVersion(Transactional.Version.DEFAULT);
//                                //todo what should we do if too version h
//                            } else {
//                                feature.setVersion(version);
//                            }
//
//                            feature.setFlowType(opName, toFlowType(pa.isOptional()));
//                            feature.setEnabled(opName, true);
//                            feature.setEnabled(true);
//                            hasATAssertion = true;
//                        }
//
//                    }
//
//                }
//
//                if (!hasATAssertion && feature != null) {
//                    feature.setEnabled(opName, false);
//                }
//
//            }
//        } catch (Exception ex) {
//            throw new WebServiceException(ex);
//        }
//
//        return feature;
//    }
}
