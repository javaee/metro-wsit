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
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.NestedPolicy;
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import java.util.Collection;
import java.util.UUID;
import javax.xml.namespace.QName;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;

/**
 *
 * @author K.Venugopal@sun.com
 */
public class TransportToken extends Token implements com.sun.xml.ws.security.policy.TransportToken, SecurityAssertionValidator  {
    private QName itQname = new QName(Constants.SECURITY_POLICY_NS, Constants.IncludeToken);
    private String id;
    private String includeToken = "";
    private HttpsToken token = null;
    private boolean populated;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    /**
     * Creates a new instance of TransportToken
     */
    public TransportToken() {
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public TransportToken(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
        UUID uid = UUID.randomUUID();
        id= uid.toString();
    }
    
    public String getTokenId() {
        return id;
    }
    
    public String getIncludeToken() {
        throw new UnsupportedOperationException("This method is not supported for TransportToken");
    }
    
    public void setIncludeToken(String type) {
        throw new UnsupportedOperationException("This method is not supported for TransportToken");
    }
    
    public com.sun.xml.ws.security.policy.HttpsToken getHttpsToken() {
        populate();
        return token;
    }
    public void setHttpsToken(com.sun.xml.ws.security.policy.HttpsToken token){
        //TODO::
    }
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            this.includeToken = this.getAttributeValue(itQname);
            NestedPolicy policy = this.getNestedPolicy();
            AssertionSet assertionSet = policy.getAssertionSet();
            for(PolicyAssertion assertion: assertionSet){
                if(PolicyUtil.isHttpsToken(assertion)){
                    token = (HttpsToken) assertion;
                }else{
                    if(!assertion.isOptional()){
                        log_invalid_assertion(assertion, isServer,"TransportToken");
                        fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                    }
                }
            }
            this.populated  = true;
        }
        return fitness;
    }
}