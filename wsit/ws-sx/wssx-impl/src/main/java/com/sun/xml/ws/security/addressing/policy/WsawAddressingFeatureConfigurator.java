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

package com.sun.xml.ws.security.addressing.policy;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;

/**
 * This Policy extension configures the WSDLModel with AddressingFeature when
 * wsaw:UsingAddressing assertion is present in the PolicyMap.
 *
 * This class exists in WSIT to provide functionality for backwards compatibility with previously generated
 * wsaw:UsingAddressing assertion.
 *
 * @author Rama Pulavarthi
 */
public class WsawAddressingFeatureConfigurator implements PolicyFeatureConfigurator{

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(WsawAddressingFeatureConfigurator.class);

    private static final QName WSAW_ADDRESSING_ASSERTION =
        new QName(AddressingVersion.W3C.policyNsUri, "UsingAddressing");

    /**
     * Creates a new instance of WsawAddressingFeatureConfigurator
     */
    public WsawAddressingFeatureConfigurator() {
    }

    /**
     * process addressing policy assertions and if found and are not optional then addressing is enabled on the
     * {@link com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType}
     *
     * @param key Key that identifies the endpoint scope
     * @param policyMap must be non-null
     * @return The list of features
     * @throws PolicyException If retrieving the policy triggered an exception
     */
    public Collection<WebServiceFeature> getFeatures(PolicyMapKey key, PolicyMap policyMap) throws PolicyException {
        LOGGER.entering(key, policyMap);
        final Collection<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if ((key != null) && (policyMap != null)) {
            final Policy policy = policyMap.getEndpointEffectivePolicy(key);
            if (null != policy && policy.contains(WSAW_ADDRESSING_ASSERTION)) {
                final Iterator<AssertionSet> assertions = policy.iterator();
                while (assertions.hasNext()) {
                    final AssertionSet assertionSet = assertions.next();
                    final Iterator<PolicyAssertion> policyAssertion = assertionSet.iterator();
                    while (policyAssertion.hasNext()) {
                        final PolicyAssertion assertion = policyAssertion.next();
                        if (assertion.getName().equals(WSAW_ADDRESSING_ASSERTION)) {
                            final WebServiceFeature feature = new AddressingFeature(true, !assertion.isOptional());
                            features.add(feature);
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Added addressing feature \"" + feature + "\" to element \"" + key + "\"");
                            }
                        } // end-if non optional wsa assertion found
                    } // next assertion
                } // next alternative
            } // end-if policy contains wsa assertion
        }
        LOGGER.exiting(features);
        return features;
    }
}