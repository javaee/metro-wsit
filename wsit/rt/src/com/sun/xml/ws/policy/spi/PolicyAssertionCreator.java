/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.policy.spi;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;

/**
 * The interface defines contract for custom (domain specific) policy assertion 
 * factories. The implementations are discovered using service provider mechanism 
 * described in the 
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">J2SE JAR File Specification</a>.
 *<p/>
 * Every implementation of policy assertion creator is expected to <b>fully</b> 
 * handle the creation of assertions for the domain (namespace) it claims to 
 * support by returning the namespace string from the {link #getSupportedDomainNamespaceUri()} 
 * method. To handle creation of domain-specific assertions that are not intended 
 * to be customized, the default policy assertion creator (passed as one of the 
 * input parameters into the {@link #createAssertion(AssertionData, Collection, AssertionSet, PolicyAssertionCreator)} method) 
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
     * @param assertionParameters collection of assertions parameters of this policy assertion. May be {@code null}.
     * @param nestedAlternative assertion set specifying nested policy alternative. May be {@code null}.
     * @param defaultCreator default policy assertion creator implementation that shall be used to handle creation of assertions
     * which are not explicitly supported by this policy assertion creator implementation
     * @return domain specific policy assertion implementation according to assertion data provided.
     * 
     * @throws AssertionCreationException in case of assertion creation failure
     */
    PolicyAssertion createAssertion(AssertionData data, Collection<PolicyAssertion> assertionParameters, AssertionSet nestedAlternative, PolicyAssertionCreator defaultCreator) throws AssertionCreationException;
}
