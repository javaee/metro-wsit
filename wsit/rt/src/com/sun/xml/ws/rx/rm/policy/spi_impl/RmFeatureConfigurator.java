/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rx.rm.policy.spi_impl;

import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rx.rm.policy.RmConfigurator;
import com.sun.xml.ws.rx.rm.api.ReliableMessagingFeature;
import com.sun.xml.ws.rx.rm.api.RmProtocolVersion;
import com.sun.xml.ws.rx.rm.localization.LocalizationMessages;
import java.util.Collection;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class RmFeatureConfigurator {
    // TODO implement PolicyMapConfigurator as well

    private static final Logger LOGGER = Logger.getLogger(RmFeatureConfigurator.class);

    /**
     * Process WS-RM policy assertions and if found and is not optional then RM is enabled on the
     * {@link WSDLPort}
     *
     * @param key Key that identifies the endpoint scope
     * @param policyMap must not be {@code null}
     * @return The list of features
     * @throws PolicyException If retrieving the policy triggered an exception
     */
    public Collection<WebServiceFeature> getFeatures(PolicyMapKey key, PolicyMap policyMap) throws PolicyException {
        final Collection<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if ((key != null) && (policyMap != null)) {
            Policy policy = policyMap.getEndpointEffectivePolicy(key);
            if (policy != null) {
                for (AssertionSet alternative : policy) {
                    WebServiceFeature feature;
                    feature = getRmFeature(alternative);
                    if (feature != null) {
                        features.add(feature);
                    }
                }
            } // end-if policy not null
        }
        return features;

    }

    private ReliableMessagingFeature getRmFeature(AssertionSet alternative) throws PolicyException {
        ReliableMessagingFeatureBuilder rmFeatureBuilder = null;
        for (RmProtocolVersion rmv : RmProtocolVersion.values()) {
            if (isPresentAndMandatory(alternative, rmv.rmAssertionName)) {
                rmFeatureBuilder = new ReliableMessagingFeatureBuilder(rmv);
                break;
            }
        }

        if (rmFeatureBuilder == null) {
            return null;
        }

        for (PolicyAssertion assertion : alternative) {
            if (assertion instanceof RmConfigurator) {
                final RmConfigurator rmAssertion = RmConfigurator.class.cast(assertion);
                if (!rmAssertion.isCompatibleWith(rmFeatureBuilder.getProtocolVersion())) {
                    LOGGER.warning(LocalizationMessages.WSRM_1009_INCONSISTENCIES_IN_POLICY(rmAssertion.getName(), rmFeatureBuilder.getProtocolVersion()));
                    // TODO replace warning with exception in Metro >2.0:
                    // throw new WebServiceException(/*message*/);
                }
                rmFeatureBuilder = rmAssertion.update(rmFeatureBuilder);
            }
        } // next assertion
        // next assertion
        return rmFeatureBuilder.build();
    }

    private Collection<PolicyAssertion> getAssertionsWithName(AssertionSet alternative, QName name) throws PolicyException {
        Collection<PolicyAssertion> assertions = alternative.get(name);
        if (assertions.size() > 1) {
            throw LOGGER.logSevereException(new PolicyException(
                    LocalizationMessages.WSRM_1008_DUPLICATE_ASSERTION_IN_POLICY(assertions.size(), name)));
        }
        return assertions;
    }

    private boolean isPresentAndMandatory(AssertionSet alternative, QName assertionName) throws PolicyException {
        Collection<PolicyAssertion> assertions;

        assertions = getAssertionsWithName(alternative, assertionName);
        for (PolicyAssertion assertion : assertions) {
            if (!assertion.isOptional()) {
                return true;
            }
        }

        return false;
    }
}
