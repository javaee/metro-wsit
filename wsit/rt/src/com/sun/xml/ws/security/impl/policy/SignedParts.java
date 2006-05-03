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
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.security.policy.Header;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */


public class SignedParts extends PolicyAssertion implements com.sun.xml.ws.security.policy.SignedParts, SecurityAssertionValidator {
    
    private boolean body;
    private boolean populated = false;
    private Set<PolicyAssertion> targets = new HashSet<PolicyAssertion>();
    
    /**
     * Creates a new instance of SignedParts
     */
    public SignedParts() {
    }
    
    public SignedParts(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public void addBody() {
        
    }
    
    public boolean hasBody() {
        populate();
        return body;
    }
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    
    
    private void populate(){
        if(populated){
            return;
        }
        synchronized(this.getClass()){
            if(!populated){
                if(this.hasNestedAssertions()){
                    Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                    while( it.hasNext() ) {
                        PolicyAssertion as = (PolicyAssertion) it.next();
                        if(PolicyUtil.isBody(as)){
                            // assertions.remove(as);
                            body = true;
                            // break;
                        }else{
                            targets.add(as);
                        }
                    }
                    //targets = assertions;
                }
                populated = true;
            }
        }
    }
    
    public void addHeader(Header header) {
        
    }
    
    public Iterator getHeaders() {
        populate();
        if(targets == null){
            return Collections.emptyList().iterator();
        }
        return targets.iterator();
    }
}
