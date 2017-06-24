/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.api.ha.StickyFeature;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.jaxws.spi.PolicyFeatureConfigurator;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

/**
 * Policy WS feature configurator implementation for the security domain
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class SecurityFeatureConfigurator implements PolicyFeatureConfigurator {
    /*
     * Empty marker class that is used to tell JAX-WS RI that client session
     * stickiness should be enabled.
     * <p />
     * This is used whenever we detect that NonceManager or SC was enabled.
     *
     */
    public static final class SecurityStickyFeature extends WebServiceFeature implements StickyFeature {
        public static final String ID = SecurityStickyFeature.class.getName();

        private boolean nonceManagerUsed;
        private boolean scUsed;

        @Override
        public String getID() {
            return ID;
        }

        public boolean isNonceManagerUsed() {
            return nonceManagerUsed;
        }

        public void nonceManagerUsed() {
            this.nonceManagerUsed = true;
        }

        public boolean isScUsed() {
            return scUsed;
        }

        public void scUsed() {
            this.scUsed = true;
        }
    }

    public Collection<WebServiceFeature> getFeatures(PolicyMapKey key, PolicyMap policyMap) throws PolicyException {
        SecurityStickyFeature stickyFeature = null;
        final Collection<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
        if ((key != null) && (policyMap != null)) {
            Policy policy = policyMap.getEndpointEffectivePolicy(key);
            if (policy != null) {
                for (AssertionSet alternative : policy) {
                    stickyFeature = resolveStickiness(alternative.iterator(), stickyFeature);

                    List<WebServiceFeature> singleAlternativeFeatures;
                    singleAlternativeFeatures = getFeatures(alternative);
                    if (!singleAlternativeFeatures.isEmpty()) {
                        features.addAll(singleAlternativeFeatures);
                    }
                }
            } // end-if policy not null
        }

        if (stickyFeature != null) {
           features.add(stickyFeature);
        }

        return features;
    }

    /**
     * NonceManager is used when there is sp:UsernameToken assertion in Policy of the Service with DigestAuthentication enabled.
     * SC feature is enabled by having a sp:SecureConversationToken in the Policy of the Service.
     */
    private static final String SC_LOCAL_NAME = "SecureConversationToken";
    private static final String DIGEST_PASSWORD_LOCAL_NAME = "HashPassword";
    private static final String NONCE_LOCAL_NAME = "Nonce";
    private static final Set<QName> STICKINESS_ENABLERS = Collections.unmodifiableSet(new HashSet(Arrays.asList(new QName[] {
        new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri, SC_LOCAL_NAME),
        new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri, SC_LOCAL_NAME),
        new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri, DIGEST_PASSWORD_LOCAL_NAME),
        new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri, DIGEST_PASSWORD_LOCAL_NAME),
        new QName(SecurityPolicyVersion.SECURITYPOLICY200507.namespaceUri, NONCE_LOCAL_NAME),
        new QName(SecurityPolicyVersion.SECURITYPOLICY12NS.namespaceUri, NONCE_LOCAL_NAME)
    })));

    /**
     *
     */
    private SecurityStickyFeature resolveStickiness(Iterator<PolicyAssertion> assertions, SecurityStickyFeature currentFeature) {
        while(assertions.hasNext()) {
            final PolicyAssertion assertion = assertions.next();
            if (STICKINESS_ENABLERS.contains(assertion.getName())) {
                if (currentFeature == null) {
                    currentFeature = new SecurityStickyFeature();
                }

                if (SC_LOCAL_NAME.equals(assertion.getName().getLocalPart())) {
                    currentFeature.scUsed();
                }

                if (NONCE_LOCAL_NAME.equals(assertion.getName().getLocalPart()) ||
                        DIGEST_PASSWORD_LOCAL_NAME.equals(assertion.getName().getLocalPart())) {
                    currentFeature.nonceManagerUsed();
                }
            }

            if (assertion.hasParameters()) {
                currentFeature = resolveStickiness(assertion.getParametersIterator(), currentFeature);
            }

            if (assertion.hasNestedPolicy()) {
                currentFeature = resolveStickiness(assertion.getNestedPolicy().getAssertionSet().iterator(), currentFeature);
            }
        }

        return currentFeature;
    }

    private List<WebServiceFeature> getFeatures(AssertionSet alternative) {
        // method will be useful in the future with unified config; do nothing for now
        return Collections.emptyList();
    }
}
