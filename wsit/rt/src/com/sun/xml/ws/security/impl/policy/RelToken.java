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
 * RelToken.java
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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;

/**
 *
 * @author Mayank.Mishra@Sun.com
 */
public class RelToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.RelToken, SecurityAssertionValidator {
    private String id;
    private List<String> tokenRefType;
    private boolean populated = false;
    private String tokenType;
    private String includeTokenType;
    private PolicyAssertion rdKey = null;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private boolean isServer = false;
    
    /** Creates a new instance of RelToken */
    public RelToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
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
    
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            NestedPolicy policy = this.getNestedPolicy();
            includeTokenType = this.getAttributeValue(itQname);
            if(policy == null){
                if(logger.getLevel() == Level.FINE){
                    logger.log(Level.FINE,"NestedPolicy is null");
                }
                populated = true;
                return fitness;
            }
            AssertionSet as = policy.getAssertionSet();
            Iterator<PolicyAssertion> paItr = as.iterator();
            
            while(paItr.hasNext()){
                PolicyAssertion assertion  = paItr.next();
                if(PolicyUtil.isRelTokenType(assertion)){
                    tokenType = assertion.getName().getLocalPart().intern();
                }else if(PolicyUtil.isRequireDerivedKeys(assertion)){
                    rdKey = assertion;
                }else if(PolicyUtil.isRequireKeyIR(assertion)){
                    if(tokenRefType == null){
                        tokenRefType = new ArrayList<String>();
                    }
                    tokenRefType.add(assertion.getName().getLocalPart().intern());
                } else{
                    if(!assertion.isOptional()){
                        
                        log_invalid_assertion(assertion, isServer,RelToken);
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            populated = true;
        }
        return fitness;
    }
    
}
