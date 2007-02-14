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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class Wss11 extends PolicyAssertion implements com.sun.xml.ws.security.policy.WSSAssertion, SecurityAssertionValidator {
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    Set<String> requiredPropSet;
    String version = "1.1";
    QName name;
    boolean populated = false;
    
    /**
     * Creates a new instance of WSSAssertion
     */
    public Wss11() {
    }
    
    public Wss11(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public void addRequiredProperty(String requirement) {
        if(requiredPropSet == null){
            requiredPropSet = new HashSet<String>();
        }
        requiredPropSet.add(requirement);
    }
    
    public Set<String> getRequiredProperties() {
        populate();
        return requiredPropSet;
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
            
            for(PolicyAssertion pa : as){
                if(PolicyUtil.isWSS11PolicyContent(pa)){
                    addRequiredProperty(pa.getName().getLocalPart().intern());
                }else{
                    if(!pa.isOptional()){
                        log_invalid_assertion(pa, isServer,"Wss11");
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
                
                
            }
            populated = true;
        }
        return fitness;
    }
}
