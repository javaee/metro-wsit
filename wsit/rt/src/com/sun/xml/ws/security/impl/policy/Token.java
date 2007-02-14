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
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;

/**
 *
 * @author K.Venugopal@sun.com
 */

public class Token extends PolicyAssertion implements  com.sun.xml.ws.security.policy.Token, SecurityAssertionValidator{
    
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private String _includeToken;
    private String _id;
    private boolean populated= false;
    private com.sun.xml.ws.security.policy.Token _token;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    /**
     * Creates a new instance of Token
     */
    
    public Token(){
        UUID uid = UUID.randomUUID();
        _id= uid.toString();
    }
    
    public Token(QName name) {
        UUID uid = UUID.randomUUID();
        _id= uid.toString();
    }
    
    public Token(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        
        UUID uid = UUID.randomUUID();
        _id= uid.toString();
    }
    
    public com.sun.xml.ws.security.policy.Token getToken() {
        populate();
        return _token;
    }
    
    public String getIncludeToken() {
        populate();
        return _includeToken;
    }
    
    public void setIncludeToken(String type) {
    }
    
    public void setToken(com.sun.xml.ws.security.policy.Token token) {
        //TODO
    }
    
    public String getTokenId() {
        
        return _id;
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            _includeToken = getAttributeValue(itQname);
            NestedPolicy policy = this.getNestedPolicy();
            if(policy == null){
                if(logger.getLevel() == Level.FINE){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                populated = true;
                return fitness;
            }
            AssertionSet as = policy.getAssertionSet();
            Iterator<PolicyAssertion> ast = as.iterator();
            while(ast.hasNext()){
                PolicyAssertion assertion = ast.next();
                if(PolicyUtil.isToken(assertion)){
                    _token = (com.sun.xml.ws.security.policy.Token)assertion;
                }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,"Token");
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            
            populated = true;
        }
        return fitness;
    }
}
