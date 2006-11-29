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

import com.sun.xml.ws.policy.PolicyAssertion;

/**
 *
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public interface PolicyAssertionValidator {
    
    public enum Fitness {
        UNKNOWN,
        SUPPORTED,
        UNSUPPORTED
    }
    
    
    /**
     * An implementation of this method must return {@code Fitness.UNKNOWN} if the given policy
     * assertion is not known, {@code Fitness.SUPPORTED} if it is supported in the client-side context or 
     * {@code Fitness.UNSUPPORTED} otherwise.
     *
     * @param assertion A policy asssertion (See {@link com.sun.xml.ws.policy.PolicyAssertion PolicyAssertion}).
     * May contain nested policies and assertions.
     * @return fitness of the {@code assertion} on in the client-side context. Must not be {@code null}.
     */
    public Fitness validateClientSide(PolicyAssertion assertion);
    
    /**
     * An implementation of this method must return {@code Fitness.UNKNOWN} if the given policy
     * assertion is not known, {@code Fitness.SUPPORTED} if it is supported in the server-side context or 
     * {@code Fitness.UNSUPPORTED} otherwise.
     *
     * @param assertion A policy asssertion (See {@link com.sun.xml.ws.policy.PolicyAssertion PolicyAssertion}).
     * May contain nested policies and assertions.
     * @return fitness of the {@code assertion} on in the server-side context. Must not be {@code null}.
     */
    public Fitness validateServerSide(PolicyAssertion assertion);
    
    /**
     * Each service provider that implements this SPI must make sure to identify all possible domains it supports.
     * This operation must be implemented as idempotent (must return same values on multiple calls).
     *
     * @return {@code String} array holding {@code String} representations of identifiers of all supported domains. 
     * Usually a domain identifier is represented by a namespace.
     */
    public String[] declareSupportedDomains();
}
