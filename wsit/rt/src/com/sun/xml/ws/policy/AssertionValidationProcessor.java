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
