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
package com.sun.xml.ws.rm.policy;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.WSDLPolicyMapWrapper;
import com.sun.xml.ws.rm.RmWsException;
import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.policy.assertion.Rm10Assertion;
import com.sun.xml.ws.rm.policy.assertion.Rm11Assertion;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public abstract class ConfigurationManager {

    private static final RmLogger LOGGER = RmLogger.getLogger(ConfigurationManager.class);

    public static ConfigurationManager createServiceConfigurationManager(WSDLPort wsdlPort, WSBinding binding) throws RmWsException {
        return new ConfigurationManager(wsdlPort, binding) {
            @Override
            protected void addNewConfiguration(AssertionSet set, WSBinding binding) throws RmWsException {
                if (set.contains(Rm11Assertion.NAME)) {
                    configurations.add(new Rm11ServiceConfiguration(set, binding.getSOAPVersion(), binding.getAddressingVersion()));
                } else if (set.contains(Rm10Assertion.NAME)) {
                    configurations.add(new Rm10ServiceConfiguration(set, binding.getSOAPVersion(), binding.getAddressingVersion()));
                }
            }
        };
    }

    public static ConfigurationManager createClientConfigurationManager(WSDLPort wsdlPort, WSBinding binding) throws RmWsException {
        return new ConfigurationManager(wsdlPort, binding) {
            @Override
            protected void addNewConfiguration(AssertionSet set, WSBinding binding) throws RmWsException {
                if (set.contains(Rm11Assertion.NAME)) {
                    configurations.add(new Rm11ClientConfiguration(set, binding.getSOAPVersion(), binding.getAddressingVersion()));
                } else if (set.contains(Rm10Assertion.NAME)) {
                    configurations.add(new Rm10ClientConfiguration(set, binding.getSOAPVersion(), binding.getAddressingVersion()));
                }
            }
        };
    }

    // TODO: improve the naive implementation - in the future we might want handle also message scope RM settings
    protected List<Configuration> configurations = new ArrayList<Configuration>();

    private ConfigurationManager(WSDLPort wsdlPort, WSBinding binding) throws RmWsException {
        PolicyMap policyMap = (wsdlPort != null) ? wsdlPort.getBinding().getOwner().getExtension(WSDLPolicyMapWrapper.class).getPolicyMap() : null;
        if (policyMap != null) {
            PolicyMapKey endpointScopeKey = PolicyMap.createWsdlEndpointScopeKey(wsdlPort.getOwner().getName(), wsdlPort.getName());

            Policy policy;
            try {
                policy = policyMap.getEndpointEffectivePolicy(endpointScopeKey);
            } catch (PolicyException ex) {
                // TODO L10N (same as above)
                throw LOGGER.logSevereException(new WebServiceException("Unable to initialize sequence configuration due to an unexpected exception", ex));
            }
            if (policy != null) {
                for (AssertionSet set : policy) {
                    if (!set.isEmpty()) {
                        addNewConfiguration(set, binding);
                    }
                }
            }
        }
    }

    public Configuration[] getConfigurationAlternatives(/*TODO: define arguments*/) {
        return configurations.toArray(new Configuration[configurations.size()]);
    }

    static <T extends PolicyAssertion> T extractAssertion(AssertionSet alternative, QName assertionName, Class<T> assertionClass) {
        List<PolicyAssertion> assertions = new ArrayList(alternative.get(assertionName));
        switch (assertions.size()) {
            case 0:
                return null;
            case 1: // TODO we fallback to default for now; we'll need to handle multiple assertion case later
            default:
                return assertionClass.cast(assertions.get(0));
        }
    }

    protected abstract void addNewConfiguration(AssertionSet set, WSBinding binding) throws RmWsException;
}
