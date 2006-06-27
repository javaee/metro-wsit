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

import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.Issuer;

import com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import javax.xml.ws.addressing.EndpointReference;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.com
 */


public class IssuedToken extends PolicyAssertion implements  com.sun.xml.ws.security.policy.IssuedToken, SecurityAssertionValidator{
    
    private boolean populated = false;
    protected String includeToken = Token.INCLUDE_ALWAYS;
    private RequestSecurityTokenTemplate rstTemplate;
    //EndpointReference er;
    private Issuer issuer = null;
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
//    String tokenType;
    private List referenceType;
    private String id;
    private AssertionData ad = null;
    
    private boolean reqDK=false;
    /**
     * Creates a new instance of IssuedToken
     */
    public IssuedToken() {
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public IssuedToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID uid = UUID.randomUUID();
        this.ad = name;
        id= uid.toString();
    }
    
    public RequestSecurityTokenTemplate getRequestSecurityTokenTemplate() {
        populate();
        return rstTemplate;
    }   
    
    public Iterator getTokenRefernceType() {
        populate();
        return referenceType.iterator();
    }
    
    public String getIncludeToken() {
        populate();
        return includeToken;
    }
    
    public void setIncludeToken(String type) {
        //includeToken = type;
        throw new UnsupportedOperationException();
    }   
    
    public String getTokenId() {
        return id;
    }
    
    public Issuer getIssuer() {
        populate();
        return issuer;
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
            return;
        }
        synchronized (this.getClass()){
            if(!populated){
                if(this.getAttributeValue(itQname)!=null){
                    this.includeToken = this.getAttributeValue(itQname);
                }
                if ( this.hasNestedAssertions() ) {
                    Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                    while ( it.hasNext() ) {
                        PolicyAssertion assertion = it.next();
                        if ( PolicyUtil.isIssuer(assertion) ) {
                            this.issuer = (Issuer) assertion;
                        } else if ( PolicyUtil.isRequestSecurityTokenTemplate(assertion)) {
                            this.rstTemplate = (RequestSecurityTokenTemplate) assertion;
                        }else{
                            if(!assertion.isOptional()){
                                if(logger.getLevel() == Level.SEVERE){
                                    logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"IssuedToken"});
                                }
                                throw new UnsupportedPolicyAssertion("Policy assertion "+
                                        assertion+" is not supported under IssuedToken assertion");
                                
                            }
                        }
                    }
                }
                NestedPolicy policy = this.getNestedPolicy();
                if(policy == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                AssertionSet as = policy.getAssertionSet();
                if(as == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE," Nested Policy is empty");
                    }
                    populated = true;
                    return;
                }
                Iterator<PolicyAssertion> ast = as.iterator();
                
                while(ast.hasNext()){
                    PolicyAssertion assertion = ast.next();
                    if(referenceType == null){
                        referenceType = new ArrayList();
                    }
                    if ( PolicyUtil.isRequireDerivedKeys(assertion)) {
                        reqDK = true;
                    } else if ( PolicyUtil.isRequireExternalReference(assertion)) {
                        referenceType.add(assertion.getName().getLocalPart().intern());
                    } else if ( PolicyUtil.isRequireInternalReference(assertion)) {
                        referenceType.add(assertion.getName().getLocalPart().intern());
                    } else{
                        if(logger.getLevel() == Level.SEVERE){
                            logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"IssuedToken"});
                        }
                        throw new UnsupportedPolicyAssertion("Policy assertion "+
                                assertion+" is not supported under IssuedToken assertion");
                        
                    }
                }
                
                populated = true;
            }
        }
    }
}