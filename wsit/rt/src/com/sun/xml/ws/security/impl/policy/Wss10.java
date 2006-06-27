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
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
import java.util.logging.Level;
/**
 *
 * @author K.Venugopal@sun.com
 */
public class Wss10 extends PolicyAssertion implements com.sun.xml.ws.security.policy.WSSAssertion, SecurityAssertionValidator {
    
    Set<String> requiredPropSet;
    String version = "1.0";
    QName name;
    boolean populated = false;
    /**
     * Creates a new instance of WSSAssertion
     */
    public Wss10() {
    }
    
    public Wss10(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    
    
    public void addRequiredProperty(String requirement) {
        if(requiredPropSet == null){
            requiredPropSet = new HashSet<String>();
        }
        requiredPropSet.add(requirement);
    }
    
    public Set getRequiredProperties() {
        populate();
        return requiredPropSet;
    }
    
    public String getType() {
        return version;
    }
    
//    public QName getName() {
//        return Constants._Wss10_QNAME;//fix this.
//    }
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    void populate(){
        if(populated){
            return ;
        }
        synchronized (this.getClass()){
            if(!populated){
                NestedPolicy policy = this.getNestedPolicy();
                if(policy == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                AssertionSet as = policy.getAssertionSet();
                for(PolicyAssertion pa:as){
                    if(PolicyUtil.isWSS10PolicyContent(pa)){
                        addRequiredProperty(pa.getName().getLocalPart().intern());
                    }else{
                        if(!pa.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{pa,"WSS10"});
                            }
                            throw new UnsupportedPolicyAssertion("Policy assertion "+
                                    pa+" is not supported under WSS10 assertion");
                            
                        }
                    }
                }
                
                populated = true;
            }
        }
    }
}
