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

import com.sun.xml.ws.policy.spi.AssertionCreationException;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;

/**
 * The interface defines contract for custom (domain specific) policy assertion factories. The implementations
 * are discovered using service provider mechanism described in the 
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">J2SE JAR File Specification</a>.
 *
 * Every implementation of policy assertion creator is expected to <b>fully</b> handle the creation of assertions for the 
 * domain (namespace) it claims to support by returning the namespace string from the {link #getSupportedDomainNamespaceUri()} 
 * method. To handle creation of domain-specific assertions that are not intended to be customized, the default policy assertion 
 * creator (passed as one of the input parameters into the {@link createAssertion(AssertionData, PolicyAssertionCreator)} method) 
 * shall be used.
 *
 * @author Marek Potociar
 */
public interface PolicyAssertionCreator {
    
    /**
     * This method returns the namespace URIs of the domains that are supported by the implementation of
     * this inteface. There can be multiple URIs supported per single implementation.
     * <p/>
     * Supporting domain namespace URI means that particular {@code PolicyAssertionCreator} implementation
     * is able to create assertion instances for the domains identified by the namespace URIs returned from this
     * method. It is required that each {@code PolicyAssertionCreator} implementation handles the policy
     * assertion creation for <b>each</b> assertion in every domain it claims to support.
     *
     * @return string array representing the namespace URIs of the supported domains. It is expected that multiple calls on this method return the 
     * same value each time. <b>Returned string array must be neither {@code null} nor empty. Also each string value in the array must not be {@code null} 
     * nor empty.</b>
     *
     */
    String[] getSupportedDomainNamespaceURIs();
    
    /**
     * Creates domain-specific policy assertion instance according to assertion data provided. For the provided
     * assertion data and this policy assertion creator instance, it will allways be true that assertion namespace 
     * URI equals to one of supported domain namespace URIs.
     *<p/>
     * Additional method parameter (which must not be {@code null}) supplied by the policy framework specifies a default policy
     * assertion creator that might be used to handle creation of unsupported domain assertion in the default way. This is
     * to give policy assertion creator a chance to handle also creation of "unsupported" domain assertions and to encourage
     * implemetors to use class composition instad of class inheritance.
     *
     * @param data assertion creation data specifying the details of newly created assertion
     * @param nestedAssertions collection of nested assertions of this policy alternative. May be {@code null}.
     * @param nestedAlternative assertion set specifying nested policy alternative. May be {@code null}.
     * @param defaultCreator default policy assertion creator implementation that shall be used to handle creation of assertions
     * which are not explicitly supported by this policy assertion creator implementation
     * @return domain specific policy assertion implementation according to assertion data provided.
     * 
     * @throw AssertionCreationException in case of assertion creation failure
     */
    PolicyAssertion createAssertion(AssertionData data, Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative, PolicyAssertionCreator defaultCreator) throws AssertionCreationException;
}
