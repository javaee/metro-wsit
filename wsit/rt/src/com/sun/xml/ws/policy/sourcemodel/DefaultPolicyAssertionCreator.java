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

import com.sun.xml.ws.policy.*;
import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.spi.PolicyAssertionCreator;
import java.util.Collection;

/**
 * Default implementation of a policy assertion creator. This implementation is used to create policy assertions in case
 * no domain specific policy assertion creator is registered for the namespace of the policy assertion.
 *
 * This is the only PolicyAssertionCreator implementation that is allowed to break general contract, claiming that
 * {@code getSupportedDomainNamespaceUri()} must not return empty String without causing PolicyAssertionCreator registration
 * fail.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
class DefaultPolicyAssertionCreator implements PolicyAssertionCreator {    
    private static final class DefaultPolicyAssertion extends PolicyAssertion {
        DefaultPolicyAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative) {
            super (data, assertionParameters, nestedAlternative);
        }
    }
    
    /** 
     * Creates a new instance of DefaultPolicyAssertionCreator 
     */
    DefaultPolicyAssertionCreator() {
        // nothing to initialize
    }

    /**
     * See {@link PolicyAssertionCreator#getSupportedDomainNamespaceURIs() method documentation in interface}
     */
    public String[] getSupportedDomainNamespaceURIs() {
        return null;
    }

    /**
     * See {@link PolicyAssertionCreator#createAssertion(AssertionData, Collection, AssertionSet, PolicyAssertionCreator) method documentation in interface}
     */
    public PolicyAssertion createAssertion(final AssertionData data, final Collection<PolicyAssertion> assertionParameters, final AssertionSet nestedAlternative, final PolicyAssertionCreator defaultCreator) throws AssertionCreationException {
        return new DefaultPolicyAssertion(data, assertionParameters, nestedAlternative);
    }    
}
