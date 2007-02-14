
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
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import javax.xml.namespace.QName;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
/**
 *
 * @author K.Venugopal@sun.com
 */

public class SecureConversationToken extends PolicyAssertion implements com.sun.xml.ws.security.policy.SecureConversationToken, SecurityAssertionValidator{
    private static QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private NestedPolicy bootstrapPolicy = null;
    private String id = null;
    protected String includeToken = Token.INCLUDE_ALWAYS;
    private boolean populated = false;
    private PolicyAssertion rdKey = null;
    private Set<String> referenceType = null;
    private Issuer issuer = null;
    private String tokenType = null;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    /**
     * Creates a new instance of SecureConversationToken
     */
    public SecureConversationToken() {
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public SecureConversationToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    
    public Set getTokenRefernceTypes() {
        populate();
        if(referenceType == null ){
            return Collections.emptySet();
        }
        return referenceType;
    }
    
    public boolean isRequireDerivedKeys() {
        populate();
        if( rdKey != null){
            return true;
        }
        return false;
    }
    
    public String getTokenType() {
        populate();
        return this.tokenType;
    }
    
    public Issuer getIssuer() {
        populate();
        return issuer;
    }
    
    
    public String getIncludeToken() {
        populate();
        return includeToken;
    }
    
    public void setIncludeToken(String type) {
        Map<QName, String> attrs = this.getAttributes();
        QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
        attrs.put(itQname,type);
    }
    
    
    
    public NestedPolicy getBootstrapPolicy() {
        populate();
        return bootstrapPolicy;
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
            String tmp = getAttributeValue(itQname);
            if(tmp != null)
                includeToken = tmp;
            NestedPolicy policy = this.getNestedPolicy();
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
                PolicyAssertion assertion = paItr.next();
                if(PolicyUtil.isBootstrapPolicy(assertion)){
                    bootstrapPolicy = assertion.getNestedPolicy();
                }else if(PolicyUtil.isRequireDerivedKeys(assertion)){
                    rdKey =  assertion;
                }else if(PolicyUtil.isRequireExternalUriReference(assertion)){
                    if(referenceType == null){
                        referenceType =new HashSet<String>();
                    }
                    referenceType.add(assertion.getName().getLocalPart().intern());
                }else if(PolicyUtil.isSC10SecurityContextToken(assertion)){
                    tokenType = assertion.getName().getLocalPart();
                }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,SecureConversationToken);
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                    
                }
            }
            if ( this.hasNestedAssertions() ) {
                Iterator <PolicyAssertion> it = this.getNestedAssertionsIterator();
                while(it.hasNext()){
                    PolicyAssertion assertion = it.next();
                    if(PolicyUtil.isIssuer(assertion)){
                        issuer = (Issuer)assertion;
                    }
                }
            }
            populated = true;
        }
        return fitness;
    }
}

