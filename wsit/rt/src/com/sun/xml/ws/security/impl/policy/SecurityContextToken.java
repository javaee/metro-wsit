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
 * SecurityContextToken.java
 */

package com.sun.xml.ws.security.impl.policy;

import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import javax.xml.namespace.QName;

/**
 *
 * @author Mayank.Mishra@Sun.com
 */
public class SecurityContextToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.SecurityContextToken, SecurityAssertionValidator{
    private String id;
    private List<String> tokenRefType;
    private boolean populated = false;
    private String tokenType;
    private String includeTokenType;
    private PolicyAssertion rdKey = null;
    private Set referenceType = null;
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    
    /** Creates a new instance of SecurityContextToken */
    public SecurityContextToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID id = UUID.randomUUID();
        this.id = id.toString();
    }
    
    public String getTokenType() {
        populate();
        return tokenType;
    }
    
    public Iterator getTokenRefernceType() {
        if ( tokenRefType != null ) {
            return tokenRefType.iterator();
        } else {
            return Collections.emptyList().iterator();
        }
    }
    
    public boolean isRequireDerivedKeys() {
        populate();
        if (rdKey != null ) {
            return true;
        }
        return false;
    }
    
    public String getIncludeToken() {
        populate();
        return includeTokenType;
    }
    
    
    public String getTokenId() {
        return id;
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
                NestedPolicy policy = this.getNestedPolicy();
                includeTokenType = this.getAttributeValue(itQname);
                if(policy == null){
                    if(logger.getLevel() == Level.FINE){
                        logger.log(Level.FINE,"NestedPolicy is null");
                    }
                    populated = true;
                    return;
                }
                AssertionSet as = policy.getAssertionSet();
                Iterator<PolicyAssertion> paItr = as.iterator();
                
                while(paItr.hasNext()){
                    PolicyAssertion assertion  = paItr.next();
                    if(PolicyUtil.isSecurityContextTokenType(assertion)){
                        tokenType = assertion.getName().getLocalPart().intern();
                    }else if(PolicyUtil.isRequireDerivedKeys(assertion)){
                        rdKey = assertion;
                    }else if(PolicyUtil.isRequireExternalUriReference(assertion)){
                        if(referenceType == null){
                            referenceType =new HashSet();
                        }
                        referenceType.add(assertion.getName().getLocalPart().intern());
                    } else{
                        if(!assertion.isOptional()){
                        if(logger.getLevel() == Level.SEVERE){
                            logger.log(Level.SEVERE,"SP0100.invalid.security.assertion",new Object[]{assertion,"SecurityContextToken"});
                        }
                        throw new UnsupportedPolicyAssertion("Policy assertion "+
                                assertion+" is not supported under SecurityContextToken assertion");
                        
                    }
                    }
                }
            }
            populated = true;
        }
    }
    
}
