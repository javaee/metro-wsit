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

/*
 * LifeTimeImpl.java
 *
 * Created on February 23, 2006, 12:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import java.util.logging.Level;
/**
 *
 * @author Abhijit Das
 */
public class Lifetime extends PolicyAssertion implements com.sun.xml.ws.security.policy.Lifetime, SecurityAssertionValidator {
    
    private String created;
    private String expires;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    private boolean populated = false;
    
    /** Creates a new instance of LifeTimeImpl */
    public Lifetime(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public String getCreated() {
        populate();
        return created;
    }
    
    public void setCreated(String created) {
        this.created = created;
    }
    
    public String getExpires() {
        populate();
        return expires;
    }
    
    public void setExpires(String expires) {
        this.expires = expires;
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
            for(PolicyAssertion pa : as){
                if ( PolicyUtil.isCreated(pa) ) {
                    this.created = pa.getValue();
                } else if ( PolicyUtil.isExpires(pa) ) {
                    this.expires = pa.getValue();
                }
            }
            populated = true;
        }
        return fitness;
    }
}
