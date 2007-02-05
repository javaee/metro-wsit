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

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.security.policy.Constants;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class Trust10 extends PolicyAssertion implements com.sun.xml.ws.security.policy.TrustAssertion, SecurityAssertionValidator{
    Set<String> requiredProps;
    String version = "1.0";
    private boolean populated = false;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    
    /**
     * Creates a new instance of Trust10
     */
    public Trust10() {
    }
    
    
    public Trust10(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public void addRequiredProperty(String requirement) {
        if(requiredProps == null){
            requiredProps = new HashSet<String>();
        }
        requiredProps.add(requirement);
    }
    
    public Set getRequiredProperties() {
        populate();
        return requiredProps;
    }
    
    public String getType() {
        return version;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            NestedPolicy policy = this.getNestedPolicy();
            if(policy == null){
                if(logger.getLevel() == Level.FINE){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                populated = true;
                return fitness;
            }
            AssertionSet as = policy.getAssertionSet();
            for(PolicyAssertion assertion:as){
                if(PolicyUtil.isSupportClientChallenge(assertion)){
                    addRequiredProperty(Constants.MUST_SUPPORT_CLIENT_CHALLENGE);
                }else if(PolicyUtil.isSupportServerChallenge(assertion)){
                    addRequiredProperty(Constants.MUST_SUPPORT_SERVER_CHALLENGE);
                }else if(PolicyUtil.isRequireClientEntropy(assertion)){
                    addRequiredProperty(Constants.REQUIRE_CLIENT_ENTROPY);
                }else if(PolicyUtil.isRequireServerEntropy(assertion)){
                    addRequiredProperty(Constants.REQUIRE_SERVER_ENTROPY);
                }else if(PolicyUtil.isSupportIssuedTokens(assertion)){
                    addRequiredProperty(Constants.MUST_SUPPORT_ISSUED_TOKENS);
                }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,"Trust10");
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            
            populated = true;
        }
        return fitness;
    }
    
}
