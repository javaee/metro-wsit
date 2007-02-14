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

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Iterator;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
import javax.xml.namespace.QName;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class ValidatorConfiguration extends PolicyAssertion implements com.sun.xml.ws.security.policy.ValidatorConfiguration, SecurityAssertionValidator{
    
    
    private boolean populated = false;
    private Iterator<PolicyAssertion> ast  = null;
    private static QName maxClockSkew =  new QName("maxClockSkew");
    private static QName timestampFreshnessLimit  =  new QName("timestampFreshnessLimit ");
    private static QName maxNonceAge =  new QName("maxNonceAge");
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    /** Creates a new instance of ValidatorConfiguration */
    public ValidatorConfiguration() {
    }
    
    public ValidatorConfiguration(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public Iterator<? extends PolicyAssertion> getValidators() {
        populate();
        return ast;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {        
        if(!populated){
            this.ast  = this.getNestedAssertionsIterator();
            populated  = true;
        }
        return fitness;        
    }
    
    public String getMaxClockSkew() {
        return this.getAttributeValue(maxClockSkew);
    }
    
    public String getTimestampFreshnessLimit() {
        return this.getAttributeValue(timestampFreshnessLimit);
    }
    
    public String getMaxNonceAge() {
        return this.getAttributeValue(maxNonceAge);
    }
}
