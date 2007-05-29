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

import com.sun.xml.ws.policy.PolicyAssertion;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface PolicyAssertionValidator {
    
    public static enum Fitness {
        UNKNOWN,
        INVALID,
        UNSUPPORTED,
        SUPPORTED;
        
        public Fitness combine(Fitness other) {
            if (this.compareTo(other) < 0) {
                return other;
            } else {
                return this;
            }
        }
    }
    
    
    /**
     * An implementation of this method must return:
     * <ul>
     *      <li>
     *          {@code Fitness.UNKNOWN} if the policy assertion type is not recognized
     *      </li>
     *      <li>
     *          {@code Fitness.SUPPORTED} if the policy assertion is supported in the
     *          client-side context
     *      </li>
     *      <li>
     *          {@code Fitness.UNSUPPORTED} if the policy assertion is recognized however
     *          it's content is not supported. For each assetion that will be eventually marked with
     *          this validation value, the policy processor will log a WARNING message however
     *          an attempt to call the web service will be made.
     *      </li>
     *      <li>
     *          {@code Fitness.INVALID} if the policy assertion is recognized however
     *          its content (value, parameters, nested assertions) is invalid. For each assetion
     *          that will be eventually marked with this validation value, the policy processor
     *          will log a SEVERE error and throw an exception. No further attempts to call
     *          the web service will be made.
     *      </li>
     * </ul>
     *
     * @param assertion A policy asssertion (See {@link com.sun.xml.ws.policy.PolicyAssertion PolicyAssertion}).
     * May contain nested policies and assertions.
     * @return fitness of the {@code assertion} on in the client-side context. Must not be {@code null}.
     */
    public Fitness validateClientSide(PolicyAssertion assertion);
    
    /**
     * An implementation of this method must return:
     * <ul>
     *      <li>
     *          {@code Fitness.UNKNOWN} if the policy assertion type is not recognized
     *      </li>
     *      <li>
     *          {@code Fitness.SUPPORTED} if the policy assertion is supported in the
     *          server-side context
     *      </li>
     *      <li>
     *          {@code Fitness.UNSUPPORTED} if the policy assertion is recognized however
     *          it's content is not supported.
     *      </li>
     *      <li>
     *          {@code Fitness.INVALID} if the policy assertion is recognized however
     *          its content (value, parameters, nested assertions) is invalid.
     *      </li>
     * </ul>
     *
     * For each assetion that will be eventually marked with validation value of
     * UNKNOWN, UNSUPPORTED or INVALID, the policy processor will log a SEVERE error
     * and throw an exception.
     *
     * @param assertion A policy asssertion (See {@link com.sun.xml.ws.policy.PolicyAssertion PolicyAssertion}).
     * May contain nested policies and assertions.
     * @return fitness of the {@code assertion} on in the server-side context. Must not be {@code null}.
     */
    public Fitness validateServerSide(PolicyAssertion assertion);
    
    /**
     * Each service provider that implements this SPI must make sure to identify all possible domains it supports.
     * This operation must be implemented as idempotent (must return same values on multiple calls).
     * <p/>
     * It is legal for two or more {@code PolicyAssertionValidator}s to support the same domain. In such case,
     * the most significant result returned from validation methods will be eventually assigned to the assertion.
     * The significance of validation results is as follows (from most to least significant):
     * <ol>
     *      <li>SUPPORTED</li>
     *      <li>UNSUPPORTED</li>
     *      <li>INVALID</li>
     *      <li>UNKNOWN</li>
     * </ol>
     *
     *
     * @return {@code String} array holding {@code String} representations of identifiers of all supported domains.
     * Usually a domain identifier is represented by a namespace.
     */
    public String[] declareSupportedDomains();
}
