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
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class UsernameToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.UserNameToken, java.lang.Cloneable, SecurityAssertionValidator {
    
    private String tokenType;
    private String id;
    private String includeToken;
    private boolean populated;
    private QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private boolean isServer = false;
    /**
     * Creates a new instance of UsernameToken
     */
    public UsernameToken() {
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public UsernameToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public void setType(String type) {
        this.tokenType = type;
    }
    
    public String getType() {
        populate();
        return tokenType;
    }
    
    
//    public QName getName() {
//        return Constants._UsernameToken_QNAME;
//    }
    
    
    public String getTokenId() {
        return id;
    }
    
    public void setTokenId(String _id) {
        this.id = _id;
    }
    
    public String getIncludeToken() {
        populate();
        return  includeToken;
    }
    
    public void setIncludeToken(String type) {
        Map<QName, String> attrs = this.getAttributes();
        QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
        attrs.put(itQname,type);
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
            return ;
        }
        synchronized (this.getClass()){
            if(!populated){
                this.includeToken = this.getAttributeValue(itQname);
                NestedPolicy policy = this.getNestedPolicy();
                if(policy == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                AssertionSet assertionSet = policy.getAssertionSet();
                for(PolicyAssertion assertion: assertionSet){
                    if(PolicyUtil.isUsernameTokenType(assertion)){
                        tokenType = assertion.getName().getLocalPart();
                    }else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"UsernameToken"});
                            }
                            if(isServer){
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                          assertion+" is not supported under UsernameToken assertion");
                            }
                        }
                    }
                }
            }
            populated = true;
        }
    }
    
    
    public Object clone() throws CloneNotSupportedException  {
        throw new UnsupportedOperationException();
//        UsernameToken ut = new UsernameToken();
//        ut.setIncludeToken(this.getIncludeToken());
//        ut.nestedPolicy = (WSPolicy) this.getPolicy();
//        ut.setTokenId(this.id);
//        return ut;
    }
    
}
