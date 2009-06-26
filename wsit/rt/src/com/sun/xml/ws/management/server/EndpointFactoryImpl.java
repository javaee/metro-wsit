/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.management.server;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.management.EndpointCreationAttributes;
import com.sun.xml.ws.api.management.ManagedEndpoint;
import com.sun.xml.ws.api.management.ManagedEndpointFactory;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;

import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

/**
 * Create a ManagedEndpoint if the policy of the endpoint requires it. Otherwise
 * returns the given endpoint.
 *
 * @author Fabian Ritzmann
 */
public class EndpointFactoryImpl implements ManagedEndpointFactory {

    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(EndpointFactoryImpl.class);
    // TODO: Replace by reference to PolicyConstants
    private static final String SUN_MANAGEMENT_NAMESPACE = "http://java.sun.com/xml/ns/metro/management";
    private static final QName SERVICE_ASSERTION_QNAME = new QName(SUN_MANAGEMENT_NAMESPACE, "ManagedService");

    public <T> WSEndpoint<T> createEndpoint(WSEndpoint<T> endpoint, EndpointCreationAttributes attributes) {
        final PolicyAssertion assertion = getAssertion(endpoint.getServiceName(), endpoint.getPortName(), endpoint.getPolicyMap());
        if (assertion != null) {
            final String id = assertion.getAttributeValue(new QName("", "id"));
            // TODO Put log string into properties
            LOGGER.info("Creating managed Metro endpoint with ID \"" + id + "\". JMX connection URL = service:jmx:rmi:///jndi/rmi://localhost:8686/jmxrmi");
            return new ManagedEndpoint<T>(id, endpoint, attributes);
        }
        else {
            return endpoint;
        }
    }

    /**
     * Return ManagedService assertion if there is one attached to the given port
     * in the policy map
     *
     * @param serviceName The name of the service. Must not be null.
     * @param portName The name of the port. Must not be null.
     * @param policyMap The policy map. Must not be null.
     * @return The policy assertion if found. Null otherwise.
     */
    private PolicyAssertion getAssertion(QName serviceName, QName portName, PolicyMap policyMap) {
        try {
            final PolicyMapKey key = PolicyMap.createWsdlEndpointScopeKey(serviceName, portName);
            final Policy policy = policyMap.getEndpointEffectivePolicy(key);
            PolicyAssertion assertion = null;
            if (policy != null) {
                final Iterator<AssertionSet> assertionSets = policy.iterator();
                if (assertionSets.hasNext()) {
                    final AssertionSet assertionSet = assertionSets.next();
                    final Iterator<PolicyAssertion> assertions = assertionSet.get(SERVICE_ASSERTION_QNAME).iterator();
                    if (assertions.hasNext()) {
                        assertion = assertions.next();
                    }
                }
            }
            return assertion;
        } catch (PolicyException ex) {
            // TODO: add error message
            throw LOGGER.logSevereException(new WebServiceException(ex));
        }
    }

}