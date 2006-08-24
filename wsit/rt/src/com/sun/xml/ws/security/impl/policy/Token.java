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

import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyAssertion;
import java.util.Iterator;
import java.util.Map;
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
    private QName _name;
    private boolean isServer = false;
    /**
     * Creates a new instance of Token
     */
    
    public Token(){
        
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
    
//    public QName getName() {
//        return _name;
//    }
    
    public void setToken(com.sun.xml.ws.security.policy.Token token) {
        //TODO
    }
    
    public String getTokenId() {
        
        return _id;
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
                _includeToken = getAttributeValue(itQname);
                NestedPolicy policy = this.getNestedPolicy();
                if(policy == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                AssertionSet as = policy.getAssertionSet();
                Iterator<PolicyAssertion> ast = as.iterator();
                while(ast.hasNext()){
                    PolicyAssertion assertion = ast.next();
                    if(PolicyUtil.isToken(assertion)){
                        _token = (com.sun.xml.ws.security.policy.Token)assertion;
                    }else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"Token"});
                            }
                            if(isServer){
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                          assertion+" is not supported under Token assertion");
                            }
                        }
                    }
                }
                
                populated = true;
            }
        }
    }
}
