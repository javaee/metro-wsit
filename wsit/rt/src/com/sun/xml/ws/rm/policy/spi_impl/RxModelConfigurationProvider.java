/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.rm.policy.spi_impl;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.commons.Logger;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.rm.MakeConnectionSupportedFeature;
import com.sun.xml.ws.rm.ReliableMessagingFeatureBuilder;
import com.sun.xml.ws.rm.policy.assertion.MakeConnectionSupportedAssertion;
import com.sun.xml.ws.rm.policy.assertion.Rm10Assertion;
import com.sun.xml.ws.rm.policy.assertion.Rm11Assertion;
import com.sun.xml.ws.rm.policy.assertion.RmAssertionTranslator;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class RxModelConfigurationProvider implements ModelConfiguratorProvider {
    // TODO implement PolicyMapUpdateProvider as well
    private static final Logger LOGGER = Logger.getLogger(RxModelConfigurationProvider.class);

    /**
     * process WS-RM policy assertions and if found and is not optional then RM is enabled on the
     * {@link WSDLPort}
     *
     * @param model must be non-null
     * @param policyMap must be non-null
     */
    public void configure(@NotNull WSDLModel model, @NotNull PolicyMap policyMap) throws PolicyException {
        assert model != null;
        assert policyMap != null;

        for (WSDLService service : model.getServices().values()) {
            for (WSDLPort port : service.getPorts()) {
                Policy policy = policyMap.getEndpointEffectivePolicy(PolicyMap.createWsdlEndpointScopeKey(service.getName(), port.getName()));
                if (policy != null) {
                    for (AssertionSet alternative : policy) {
                        if (isPresentAndMandatory(alternative, Rm10Assertion.NAME) || isPresentAndMandatory(alternative, Rm11Assertion.NAME)) {
                            ReliableMessagingFeatureBuilder rmFeatureBuilder = new ReliableMessagingFeatureBuilder();
                            for (PolicyAssertion assertion : alternative) {
                                if (assertion instanceof RmAssertionTranslator) {
                                    rmFeatureBuilder = RmAssertionTranslator.class.cast(assertion).update(rmFeatureBuilder);
                                }
                            } // next assertion
                            port.addFeature(rmFeatureBuilder.build());
                        } // end-if RM assertion is present and not optional
                        if (isPresentAndMandatory(alternative, MakeConnectionSupportedAssertion.NAME)) {
                            port.addFeature(new MakeConnectionSupportedFeature());
                        } // end-if MC assertion is present and not optional
                    } // next alternative
                } // end-if policy not null
            } // end foreach port
        } // end foreach service
    }

    private Collection<PolicyAssertion> getAssertionsWithName(AssertionSet alternative, QName name) throws PolicyException {
        Collection<PolicyAssertion> assertions = alternative.get(name);
        if (assertions.size() > 1) {
            // TODO L10N
            throw LOGGER.logSevereException(new PolicyException(String.format("%n duplicate [%s] policy assertions in a single policy alternative detected", assertions.size(), name)));
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
