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
package com.sun.xml.ws.policy;

import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;

import static com.sun.xml.ws.policy.privateutil.LocalizationMessages.WSP_0076_NO_SERVICE_PROVIDERS_FOUND;

/**
 * Singleton class that provides method for assertion validation.
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public final class AssertionValidationProcessor {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(AssertionValidationProcessor.class);
    
    private static AssertionValidationProcessor processor = new AssertionValidationProcessor();
    private static final PolicyAssertionValidator[] validators;
    static {
        validators = PolicyUtils.ServiceProvider.load(PolicyAssertionValidator.class);
    }
    
    /**
     * This private constructor prevents direct instantiation of this class.
     */
    private AssertionValidationProcessor() {
        // no instantiation outside the class.
    }
    
    /**
     * Factory method that returns singleton instance of the class.
     * 
     * @return singleton instance of the class.
     */
    public static AssertionValidationProcessor getInstance() throws PolicyException {
        if (validators.length == 0) {
            throw LOGGER.logSevereException(new PolicyException(WSP_0076_NO_SERVICE_PROVIDERS_FOUND(PolicyAssertionValidator.class.getName())));
        }
        return processor;
    }
    
    /**
     * Validates fitness of the {@code assertion} on the client side.
     *
     * return client side {@code assertion} fitness 
     */
    public PolicyAssertionValidator.Fitness validateClientSide(final PolicyAssertion assertion) throws PolicyException {
        PolicyAssertionValidator.Fitness assertionFitness = PolicyAssertionValidator.Fitness.UNKNOWN;
        for ( PolicyAssertionValidator validator : validators ) {
            assertionFitness = assertionFitness.combine(validator.validateClientSide(assertion));
            if (assertionFitness == PolicyAssertionValidator.Fitness.SUPPORTED) {
                break;
            }
        }
        
        return assertionFitness;
    }

    /**
     * Validates fitness of the {@code assertion} on the server side.
     *
     * return server side {@code assertion} fitness 
     */    
    public PolicyAssertionValidator.Fitness validateServerSide(final PolicyAssertion assertion) throws PolicyException {
        PolicyAssertionValidator.Fitness assertionFitness = PolicyAssertionValidator.Fitness.UNKNOWN;
        for (PolicyAssertionValidator validator : validators) {
            assertionFitness = assertionFitness.combine(validator.validateServerSide(assertion));
            if (assertionFitness == PolicyAssertionValidator.Fitness.SUPPORTED) {
                break;
            }
        }
        
        return assertionFitness;
    }
}
