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
 * X509Token.java
 *
 * Created on January 5, 2006, 3:53 PM
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
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.logger;
import javax.xml.namespace.QName;


/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.Com
 */

public class X509Token extends PolicyAssertion implements com.sun.xml.ws.security.policy.X509Token,Cloneable, SecurityAssertionValidator{
    
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private String includeToken = Token.INCLUDE_ALWAYS;
    
    private boolean populated = false;
    private String tokenType = null;
    private Set referenceType = null;
    
    /**
     * Creates a new instance of X509Token
     */
    private String id = null;
    private boolean reqDK=false;

    private boolean isServer = false;
    
    public X509Token() {
        UUID uid = UUID.randomUUID();
        id= uid.toString();
        referenceType = new HashSet();
    }
    
    public X509Token(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        
        UUID uid = UUID.randomUUID();
        id= uid.toString();
        referenceType = new HashSet();
    }
    
    
    
    public void addTokenReferenceType(String tokenRefType) {
        referenceType.add(tokenRefType);
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public String getTokenType() {
        populate();
        return tokenType;
    }
    
    public Set getTokenRefernceType() {
        populate();
        return referenceType;
    }
    
    public String getIncludeToken() {
        populate();
        return includeToken;
    }
    
    public void setIncludeToken(String type) {
        includeToken = type;
    }
    
//    public QName getName() {
//        return Constants._X509Token_QNAME;
//    }
    
    public String getTokenId() {
        return id;
    }
    
    public boolean isRequireDerivedKeys() {
        populate();
        return reqDK;
    }
    
    public boolean validate() {
        try{
            populate();
            return true;
        }catch(UnsupportedPolicyAssertion upaex) {
            return false;
        }
    }
    
    private void populate() {
        if(populated){
            return ;
        }
        synchronized (this.getClass()){
            if(!populated){
                if(this.getAttributeValue(itQname)!=null){
                    this.includeToken = this.getAttributeValue(itQname);
                }
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
                    if(PolicyUtil.isTokenReferenceType(assertion)){
                        referenceType.add(assertion.getName().getLocalPart().intern());
                    }else if(PolicyUtil.isTokenType(assertion)) {
                        tokenType = assertion.getName().getLocalPart();
                    }else if (PolicyUtil.isRequireDerivedKeys(assertion)) {
                        reqDK = true;
                    }else{
                        if(!assertion.isOptional()){
                            if(logger.getLevel() == Level.SEVERE){
                                logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"X509Token"});
                            }
                            if(isServer){
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                          assertion+" is not supported under X509Token assertion");
                            }
                        }
                    }
                }
            }
            populated = true;
        }
    }
    
    public Object clone()throws CloneNotSupportedException{
        throw new UnsupportedOperationException("Fix me");
        //return new X509Token(this.nestedPolicy,getAttributes(),id);
    }
}