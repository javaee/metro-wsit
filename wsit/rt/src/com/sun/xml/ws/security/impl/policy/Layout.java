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

import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.MessageLayout;
import java.util.Collection;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;

/**
 *
 * @author K.Venugopal@sun.com
 */

public class Layout extends PolicyAssertion implements SecurityAssertionValidator {
    
    MessageLayout ml;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean populated = false;
    /**
     * Creates a new instance of Layout
     */
    public Layout() {
    }
    
    public Layout(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    public MessageLayout getMessageLayout() {
        populate();
        return ml;
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
            AssertionSet assertionSet = policy.getAssertionSet();
            for(PolicyAssertion assertion : assertionSet){
                if(PolicyUtil.isLax(assertion)){
                    ml =  MessageLayout.Lax;
                }else if(PolicyUtil.isLaxTsFirst(assertion)){
                    ml = MessageLayout.LaxTsFirst;
                }else if(PolicyUtil.isLaxTsLast(assertion)){
                    ml = MessageLayout.LaxTsLast;
                }else if(PolicyUtil.isStrict(assertion)){
                    ml= MessageLayout.Strict;
                } else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,Layout);
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            populated = true;
        }
        return fitness;
    }
}
