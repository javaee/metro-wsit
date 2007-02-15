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
import com.sun.xml.ws.policy.PolicyAssertion;
import com.sun.xml.ws.policy.sourcemodel.AssertionData;
import com.sun.xml.ws.security.policy.Lifetime;
import java.util.Collection;
import java.util.Iterator;
import static com.sun.xml.ws.security.impl.policy.Constants.*;
import com.sun.xml.ws.security.policy.SecurityAssertionValidator;
/**
 *
 * @author K.Venugopal@sun.com Abhijit.Das@Sun.COM
 */
public class RequestSecurityTokenTemplate extends PolicyAssertion implements com.sun.xml.ws.security.policy.RequestSecurityTokenTemplate, SecurityAssertionValidator {
    
    private boolean populated = false;
    private AssertionFitness fitness = AssertionFitness.IS_VALID;
    String tokenType;
    String requestType;
    Lifetime lifeTime;
    String authenticationType;
    private String keyType;
    private int keySize;
    private String sigAlgo;
    private String encAlgo;
    private String canonAlgo;
    private boolean isProofEncRequired = false;
    private String computedKeyAlgo;
    private boolean isEncRequired = false;
    private String signWith;
    private String encryptWith;
    
    /**
     * Creates a new instance of RequestSecurityTokenTemplate
     */
    public RequestSecurityTokenTemplate() {
    }
    
    public RequestSecurityTokenTemplate(AssertionData name,Collection<PolicyAssertion> nestedAssertions, AssertionSet nestedAlternative) {
        super(name,nestedAssertions,nestedAlternative);
    }
    
    public String getTokenType() {
        populate();
        return tokenType;
    }
    
    public String getRequestType() {
        populate();
        return this.requestType;
    }
    
    public Lifetime getLifetime() {
        populate();
        return lifeTime;
    }
    
    
    public String getAuthenticationType() {
        populate();
        return authenticationType;
    }
    
    
    public String getKeyType() {
        populate();
        return keyType;
    }
    
    public int getKeySize() {
        populate();
        return keySize;
    }
    
    
    
    public String getSignatureAlgorithm() {
        populate();
        return sigAlgo;
    }
    
    
    public String getEncryptionAlgorithm() {
        populate();
        return encAlgo;
    }
    
    
    public String getCanonicalizationAlgorithm() {
        populate();
        return canonAlgo;
    }
    
    
    public boolean getProofEncryptionRequired() {
        populate();
        return isProofEncRequired;
    }
    
    
    
    public String getComputedKeyAlgorithm() {
        populate();
        return computedKeyAlgo;
    }
    
    
    
    public boolean getEncryptionRequired() {
        populate();
        return isEncRequired;
    }
    
    
    
    public String getSignWith() {
        populate();
        return signWith;
    }
    
    
    public String getEncryptWith() {
        populate();
        return encryptWith;
    }
    
    
    
    public String getTrustVersion() {
        throw new UnsupportedOperationException();
    }
    
    
    public AssertionFitness validate(boolean isServer) {
        return populate(isServer);
    }
    private void populate(){
        populate(false);
    }
    
    private synchronized AssertionFitness populate(boolean isServer) {
        if(!populated){
            if ( this.hasNestedAssertions() ) {
                
                Iterator <PolicyAssertion> it =this.getNestedAssertionsIterator();
                while( it.hasNext() ) {
                    PolicyAssertion assertion = (PolicyAssertion) it.next();
                    //TODO: Support all RequestSecurityTokenTemplate elements
                    if ( PolicyUtil.isKeyType(assertion) ) {
                        this.keyType = assertion.getValue();
                    } else if ( PolicyUtil.isKeySize(assertion) ) {
                        this.keySize = Integer.valueOf(assertion.getValue());
                    }  else if ( PolicyUtil.isEncryption(assertion) ) {
                        this.isEncRequired = true;
                    } else if ( PolicyUtil.isProofEncryption(assertion) ) {
                        this.isProofEncRequired = true;
                    } else if ( PolicyUtil.isLifeTime(assertion) ) {
                        this.lifeTime = (Lifetime) assertion;
                    }else if(PolicyUtil.isSignWith(assertion)){
                        this.signWith = assertion.getValue();
                    }else if(PolicyUtil.isTrustTokenType(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isRequestType(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isAuthenticationType(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isSignatureAlgorithm(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isEncryptionAlgorithm(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isCanonicalizationAlgorithm(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isComputedKeyAlgorithm(assertion)){
                        this.tokenType = assertion.getValue();
                    }else if(PolicyUtil.isProofEncryption(assertion)){
                        isProofEncRequired = true;
                    }else if(PolicyUtil.isEncryption(assertion)){
                        isEncRequired = true;
                    }else if(PolicyUtil.isClaimsElement(assertion)) {
                        // Valid assertion.
                    }else if(PolicyUtil.isEntropyElement(assertion)){
                        // Valid assertion.
                    }else {
                        if(!assertion.isOptional()){
                            log_invalid_assertion(assertion, isServer,RequestSecurityTokenTemplate);
                            fitness = AssertionFitness.HAS_UNKNOWN_ASSERTION;
                        }
                    }
                    
                }
            }
            populated = true;
        }
        return fitness;
    }
}
