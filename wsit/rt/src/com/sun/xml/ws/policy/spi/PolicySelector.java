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

package com.sun.xml.ws.policy.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.namespace.QName;
import com.sun.xml.ws.policy.PolicyAssertion;

/**
 * A call-back interface to test if a policy assertion is valid and supported.
 */
public abstract class PolicySelector {
    
    private final ArrayList<String> domainNamespaces = new ArrayList<String>();
    private final HashSet<QName> supportedAssertions = new HashSet<QName>();
    
    /**
     * Initialize the list of supported namespaces and assertions.
     *
     * @param assertionNames A list of the QNames of all supported assertions
     */
    protected PolicySelector(List<QName> assertionNames) {
        for (QName assertionName : assertionNames) {
            domainNamespaces.add(assertionName.getNamespaceURI());
            supportedAssertions.add(assertionName);
        }
    }
    
    /**
     * An implementation of this method must return true if the given policy
     * assertion is supported. Otherwise it must return false.
     *
     * @param assertion A policy asssertion. May contain nested policies and
     * assertions.
     * @return True if the assertion is supported. False otherwise.
     */
    public boolean test(PolicyAssertion assertion) {
        // 1. Check if assertion belongs to known namespace
        String namespace = assertion.getName().getNamespaceURI();
        if (!domainNamespaces.contains(namespace)) {
            return false;
        }
        // 2. Check if assertion is known and supported
        return isAssertionSupported(assertion);
    };
    
    private boolean isAssertionSupported(PolicyAssertion assertion) {
        if (!supportedAssertions.contains(assertion.getName())) {
            return false;
        };
        /*if (assertion.hasNestedAssertions()) { // check nested assertions -- if any
            for (Iterator<PolicyAssertion> asIter = assertion.getNestedAssertionsIterator(); asIter.hasNext() ; ) {
                if (!isAssertionSupported(asIter.next())) {
                    return false;
                }
            }
        } // end if nested assertions exist
        if (assertion.hasNestedPolicy()) { // check nested policies -- if any
            for (PolicyAssertion nestedAssertion : assertion.getNestedPolicy().getAssertionSet()) {
                if (!isAssertionSupported(nestedAssertion)) {
                    return false;
                }
            }
        }  // end if has nested policy exists */
        return true;
    }
    
    /**
     * An implementation of this method must return true if the given namespace
     * is supported within its domain. Otherwise it must return false.
     *
     * @param namespaceUri A namespace URI
     * @return True if the namespace is supported. False otherwise.
     */
    public boolean isSupported(String namespaceUri) {
        return domainNamespaces.contains(namespaceUri);
    };
}
