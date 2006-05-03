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
 * Issuer.java
 *
 * Created on February 22, 2006, 5:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingException;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.Metadata;
import javax.xml.ws.addressing.ReferenceParameters;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author Abhijit Das
 */
public class Issuer extends PolicyAssertion implements com.sun.xml.ws.security.policy.Issuer, SecurityAssertionValidator {
    
    private EndpointReference endpointRef;
    private boolean populated = false;
    /**
     * Creates a new instance of Issuer
     */
    public Issuer() {
    }
    
    public Issuer(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
//    public QName getName(){
//        return Constants._Issuer;
//    }
    public EndpointReference getEndpointReference() {
        return endpointRef;
    }
    
    public void setEndpointReference(EndpointReference endpointReference) {
        try {
            this.endpointRef = (EndpointReference) endpointReference;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        synchronized (this.getClass()){
            if(!populated){
                if ( this.hasNestedAssertions() ) {
                    Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                    while ( it.hasNext() ) {
                        PolicyAssertion assertion = it.next();
                        if ( PolicyUtil.isEndpointReference(assertion) ) {
                            endpointRef = (EndpointReference) assertion;
                        } else{
                            if(!assertion.isOptional()){
                                if(logger.getLevel() == Level.SEVERE){
                                    logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"Issuer"});
                                }
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                        assertion+" is not supported under Issuer assertion");
                                
                            }
                        }
                    }
                }
//            Policy policy = this.getPolicy();
//            Iterator<AssertionSet> itr = policy.iterator();
//            while(itr.hasNext()){
//                AssertionSet as = itr.next();
//                Iterator<PolicyAssertion> ast = as.iterator();
//                while(ast.hasNext()){
//                    PolicyAssertion assertion = ast.next();
//                    if ( assertion.getName() == Constants._EndpointReference_QNAME) {
//
//                        this.endpointRef = new EndpointReference(assertion.getValue());
//                    }
//                }
//            }
                populated = true;
            }
        }
    }
}
